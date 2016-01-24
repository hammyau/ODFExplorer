package net.amham.odfe.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.odftoolkit.odfdom.dom.OdfContentDom;
import org.odftoolkit.odfdom.dom.attribute.style.StyleNameAttribute;
import org.odftoolkit.odfdom.dom.element.style.StyleStyleElement;
import org.odftoolkit.odfdom.dom.element.text.TextParagraphElementBase;
import org.odftoolkit.odfdom.dom.element.text.TextSectionElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeAutomaticStyles;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.pkg.OdfFileDom;
import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.Document;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.ParagraphProperties;
import org.odftoolkit.simple.text.Paragraph;
import org.odftoolkit.simple.text.ParagraphStyleHandler;
import org.odftoolkit.simple.text.Section;
import org.w3c.dom.NodeList;

import net.amham.odfe.difference.DocumentDifference;
import net.amham.odfe.gauges.OdfGaugeStore;
import net.amham.odfe.json.DiffType;
import net.amham.odfe.json.StylesJSONdiffWriter;
import net.amham.odfe.style.OdfeStyleFamily;
import net.amham.odfe.style.StyleFamilyDiffData;
import net.amham.odfe.style.StyleFamilyTreeBuilder;
import net.amham.odfe.style.StylesData;
import net.amham.odfe.xpath.XPathDifference;
import net.amham.odfe.xpath.XPathNode;

public class DifferenceReport extends DocumentReport {

	private SortedMap<String, StyleFamilyDiffData> reportMap = new TreeMap<String, StyleFamilyDiffData>();

	
	public DifferenceReport() {
		LOGGER.info("Create Difference Processor");
		processMode = ProcessMode.DIFF;		
		gaugesWriter.setMode(processMode.toString());
		stylesWriter = new StylesJSONdiffWriter();
		stylesWriter.setMode(processMode.toString());
		xPathWriter.setMode(processMode.toString());
	}

	public void generate(File odfFile, File recordsDir) {
		documentFile = odfFile;

		modeDir = new File(recordsDir, "Comparisons");
		if(modeDir.exists() == false) {
			modeDir.mkdir();
		}
		openExtract(odfFile, modeDir);

		if(odfGauges == null) {
			odfGauges = new OdfGaugeStore(documentFile.getName());
			gaugesWriter.addGauges(odfGauges);
			aggSum = new AggregationSummary(documentFile.getName());
		} else {
			//because we are comparing we need the summary of the previous runs to be added
			aggSum.setGauges(odfGauges);
			aggSum.fillSummary();
			addComment(documentFile.getName());
		}

		stylesData = new StylesData();
		stylesWriter.addData(stylesData);
		
		styleFamilyTree = new StyleFamilyTreeBuilder(documentFile.getName());
		stylesData.setDocumentName(documentFile.getName());
		stylesData.setFamilyTree(styleFamilyTree.getStyleFamiliesMap());
		
		if(xPathRoot == null) {
			xPathRoot = new XPathNode("/");
		}
		addSource(documentFile.getName());
		xPathRoot.setSrcNdx(srcNdx);

		closed = false;

		try {
			processDoc();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	@Override
	public void close() {
		// here we want to build a difference report to be written out by the
		// JSON writer
		System.out.println("Paths");
		xPathRoot.dump("");
		
		System.out.println("XPath differences:");
		SortedMap<String, XPathDifference> diffPaths = xPathRoot.getXPathDifferences();
		for (XPathDifference xPathDiff : diffPaths.values()) {
			String pathToFind = xPathDiff.getTarget(); // xPathDiff.getTarget()
			System.out.println("Difference Target: " + pathToFind);
			xPathDiff.dump();
		}
		try {
			if(genCommentedDoc) {
				createCommentedDocument();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}		
		try {
			System.out.println("Styles Report differences:");
			generateStylesReportMap();
		} catch (Exception e) {
			e.printStackTrace();
		}
		((StylesJSONdiffWriter) stylesWriter).setReportMap(reportMap);

		numDiffs = diffPaths.size(); 
		super.close();
	}
	
	private void generateStylesReportMap() throws Exception {
		if(stylesWriter.hasTwoStyles())
		{
			SortedMap<String, OdfeStyleFamily> sdRef = stylesWriter.getStyleFamilyTreeAt(0);
			SortedMap<String, OdfeStyleFamily> sdTrg = stylesWriter.getStyleFamilyTreeAt(1);
			
			Iterator<String> refKeys = sdRef.keySet().iterator();
			//If we remove the items then what it left is new
			while(refKeys.hasNext()) {
				String refKey = refKeys.next();
				StyleFamilyDiffData diffFam = new StyleFamilyDiffData();
				OdfeStyleFamily refFam = sdRef.get(refKey);
				OdfeStyleFamily trgFam = sdTrg.remove(refKey);
				if(trgFam == null) {
					// This is missing in the trg
					// Flag in the ref set in some way - no this should be an external thing
					// here create some sort of New, Edited, Missing, Same object and collect them
					// just wrapper objects 
					diffFam.setType(DiffType.MissingStyle);
					LOGGER.fine("Missing Family " + refKey);
				}
				else {
					//we should check for equivalence here
					//will need our own operation - which will walk the FamilyTree
					if (refFam.compareTo(trgFam)) {
						diffFam.setType(DiffType.SameStyle);
						LOGGER.fine("Same Family " + refKey);
					}
					else {
						diffFam.setType(DiffType.ChangedStyle);
						diffFam.addTarget(trgFam);
						LOGGER.fine("Changed Family " + refKey);
					}
					
				}
				diffFam.setReference(refFam);
				reportMap.put(refKey, diffFam);
			}
			
			//What ever is left in target set is new
			Iterator<String> trgKeys = sdTrg.keySet().iterator();
			while(trgKeys.hasNext()) {
				String trgKey = trgKeys.next();
				OdfeStyleFamily trgFam = sdTrg.get(trgKey);
				StyleFamilyDiffData diffFam = new StyleFamilyDiffData();
				diffFam.setType(DiffType.NewStyle);
				diffFam.setReference(trgFam);
				reportMap.put(trgKey, diffFam);
				LOGGER.fine("New Family " + trgKey);
			}
			
			//so now we should have diffMap with a highlighted set of Families
			//write them out
			for(String fam : reportMap.keySet()) {
				LOGGER.fine("Report " + fam + " diff " + reportMap.get(fam).getType());
				reportMap.get(fam).getReference().dump(null);
				reportMap.get(fam).getReference().getDifferenceNodes();
			}
		}
		
//		createCommentedDocument();
	}

	@Override
	public void processClose(Document document){
	}
	
	/**
	 * We want to add the comment to the item where a style changed occurred
	 * 
	 * We could use a visitor to find the paragraphs with a given style
	 * the we apply the comment
	 * 		If the base paragraph is changed there will be heaps of comments
	 * 
	 * We also need to process automatic styles
	 * 
	 * Could the comment be added at the time we detect the difference?
	 *  No it is the families in the styles.xml we are processing
	 *  	but we do look for style hits somewhere
	 *  	the hit function is under the visitor and could indicate that a comment is needed
	 *  		Do we know the style has been changed at this stage?
	 *  		Can we make sure we do... I think we do 
	 * 	So copy the original and iterate through it for differences and add comments as we find them
	 * 
	 * @throws Exception
	 */
	public void createCommentedDocument() throws Exception {
		
		TextDocument reportDoc = TextDocument.newTextDocument();
		Paragraph first = reportDoc.getParagraphByIndex(0, false);
		first.remove();
		
		TextDocument document = TextDocument.loadDocument(documentFile);

		//get a copy of the document contents
		List<String> comments = new ArrayList<String>();
		boolean differencesNoted = false;
		OdfContentDom contentDom = document.getContentDom();
		XPath xPath = contentDom.getXPath();
		
		SortedMap<String, XPathDifference> diffPaths = xPathRoot.getXPathDifferences();
		for (XPathDifference xPathDiff : diffPaths.values()) {

			// will there be more than one?
			// List<DocumentDifference> diffStyles =
			// xPathDiff.getDocumentDifferences();
			// for(DocumentDifference diff : diffStyles) {
			String pathToFind = xPathDiff.getTarget(); // xPathDiff.getTarget()

			// + diff.getName() + "']";
			if (pathToFind != null) {
				NodeList nodeList = (NodeList) xPath.compile(pathToFind).evaluate(contentDom, XPathConstants.NODESET);

				comments.add(xPathDiff.commentNodes(nodeList));
				differencesNoted = true;
			}
		}
		String lead = null;
		if (differencesNoted) {
			//add para to end then copy and delete
			lead = "Differences Found see comments";
			
		} else {
			lead = "Document conforms.";
		}
		Paragraph header = reportDoc.addParagraph(lead);
		
		Paragraph last = null;
		for(String comment : comments) {
			last = reportDoc.addParagraph(comment);
		}
		
		if(last == null) {
			last = reportDoc.addParagraph("no comments added");
		}
		OdfOfficeAutomaticStyles styles = reportDoc.getContentDom().getAutomaticStyles();
		OdfStyle style = styles.newStyle(OdfStyleFamily.Paragraph);
		// set break after paragraph not before

		style.newStyleParagraphPropertiesElement().setFoBreakAfterAttribute("page");
		last.getOdfElement().setStyleName(style.getStyleNameAttribute());
		Paragraph more = reportDoc.addParagraph("even more");
		reportDoc.insertContentFromDocumentAfter(document, more, true);
		more.remove();

		commentedDocument = new File(extractDir, documentFile.getName()); 
		reportDoc.save(commentedDocument);
		reportDoc.close();
		LOGGER.info("Commented in " + documentFile.getName());

	}

}
