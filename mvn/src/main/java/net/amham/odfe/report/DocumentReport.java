package net.amham.odfe.report;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.stream.XMLStreamException;

import net.amham.odfe.CommandRunner;
import net.amham.odfe.gauges.OdfGaugeStore;
import net.amham.odfe.graphviz.DotWriter;
import net.amham.odfe.json.GaugeJSONWriter;
import net.amham.odfe.json.JsonODFERuns;
import net.amham.odfe.json.StylesJSONWriter;
import net.amham.odfe.json.XPathJSONWriter;
import net.amham.odfe.style.StyleExtractor;
import net.amham.odfe.style.StyleFamilyTreeBuilder;
import net.amham.odfe.style.StylesData;
import net.amham.odfe.xmlwrite.GaugeXMLWriter;
import net.amham.odfe.xmlwrite.ODFERun;
import net.amham.odfe.xmlwrite.ResultsXMLWriter;
import net.amham.odfe.xmlwrite.StylesXMLWriter;
import net.amham.odfe.xmlwrite.XMLWriter;
import net.amham.odfe.xpath.XPathNode;

import org.codehaus.jackson.JsonProcessingException;
import org.odftoolkit.odfdom.dom.OdfMetaDom;
import org.odftoolkit.odfdom.dom.OdfStylesDom;
import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.element.number.NumberBooleanStyleElement;
import org.odftoolkit.odfdom.dom.element.number.NumberTextStyleElement;
import org.odftoolkit.odfdom.dom.element.office.OfficeDocumentMetaElement;
import org.odftoolkit.odfdom.dom.element.office.OfficeDocumentStylesElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberCurrencyStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberDateStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberPercentageStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberTimeStyle;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfDefaultStyle;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextListStyle;
import org.odftoolkit.odfdom.pkg.OdfElement;
import org.odftoolkit.odfdom.pkg.OdfFileDom;
import org.odftoolkit.odfdom.pkg.OdfPackage;
import org.odftoolkit.odfdom.pkg.OdfValidationException;
import org.odftoolkit.simple.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.assembler.Mode;


/**
 * @author ian
 *
 */
public class DocumentReport extends ReportBase {

	
	protected StylesData stylesData = null;
	protected SortedMap<String, Integer> stylesUsed = new TreeMap<String, Integer>();
	protected SortedMap<String, OdfStyle> autoStylesMap = new TreeMap<String, OdfStyle>();
	
	private SortedSet<String> filePaths = new TreeSet<String>();
	private static List<String> sourceNames = new ArrayList<String>();

	protected StyleFamilyTreeBuilder styleFamilyTree = null;
	protected StyleExtractor styleExtractor = null;
	protected File commentedDocument;

	protected GaugeJSONWriter gaugesWriter;
	
	//Could base class our writers and choose
	//for the moment manually replace
	protected StylesJSONWriter stylesWriter;
	
	protected XPathJSONWriter xPathWriter;
	protected DotWriter xPathDotWriter;
	protected XMLWriter xPathXMLWriter;
	
	protected File documentFile;
	protected File extractDir;
	protected File modeDir;

	/**
	 * Poorly named? Which XML files to process
	 */
	private ProcessDepth processDepth = ProcessDepth.ALL;
	private Boolean includeLegend = false;
	

	protected StylesXMLWriter stylesXMLWriter;

	protected GaugeXMLWriter gaugesXMLWriter;
	protected ResultsXMLWriter resultsXMLWriter;

	private String runDate;
	private String docname;
	private String comment = "";
	
	protected Boolean closed = true;

	private boolean fixedDateMode;
	private boolean attributesOn = true;;

	protected File docDir;
	
	protected Document document;


	private String extractName = null;
	
	protected XPathNode xPathRoot = null;  
	
	protected int numDiffs = 0;
	protected int srcNdx = -1; //init to -1 make work

	protected boolean genCommentedDoc = false;

	private String outputName = "";
	protected AggregationSummary aggSum;
	
	/**
	 * A Document Report accepts an ODF File
	 * and extracts content and style information to
	 * the records directory
	 * 
	 * The extracted data will be written to a subdirectory of the records
	 * with a name based on that of the ODF document. 
	 * 
	 * Additional files may be aggregated into the results via the setFile method
	 * or, if difference mode is set, each file will be compared to the first set.
	 * 
	 * @param odfFile
	 * @param extractDir
	 */
	public DocumentReport() {

		LOGGER.info("Create Document Processor");

		resultsXMLWriter = new ResultsXMLWriter();
		gaugesWriter = new GaugeJSONWriter();
		gaugesWriter.setMode(processMode.toString());
		gaugesXMLWriter = new GaugeXMLWriter();
		stylesXMLWriter = new StylesXMLWriter();
		stylesWriter = new StylesJSONWriter();
		stylesWriter.setMode(processMode.toString());
		
		xPathWriter = new XPathJSONWriter();
		xPathDotWriter = new DotWriter();
		xPathXMLWriter = new XMLWriter();
		xPathWriter.setMode(processMode.toString());
		
//		dotWriter = new DotWriter();
	}

	/**
	 * @param recordsDir 
	 * @throws Exception 
	 * 
	 */
	public void generate(File odfFile, File recordsDir) throws XMLStreamException {
		documentFile = odfFile;
		
		modeDir = new File(recordsDir, "Singles");
		if(modeDir.exists() == false) {
			modeDir.mkdir();
		}
		openExtract(odfFile, modeDir);
		
		//can the sources ever not be clear?
		odfGauges = new OdfGaugeStore(documentFile.getName());
		gaugesWriter.addGauges(odfGauges);
		aggSum = new AggregationSummary(documentFile.getName());
		
		stylesData = new StylesData();
		stylesWriter.addData(stylesData);

		//can this be null? - set at construct time?
		//or always set here - ah for diff may not be null?
		if(styleFamilyTree == null) {
			styleFamilyTree = new StyleFamilyTreeBuilder(documentFile.getName());
			stylesData.setDocumentName(documentFile.getName());
			stylesData.setFamilyTree(styleFamilyTree.getStyleFamiliesMap());
		}
		
		if(xPathRoot == null) {
			xPathRoot = new XPathNode("/");
		}
		addSource(documentFile.getName());
		xPathRoot.setSrcNdx(srcNdx);
		
		try {
			processDoc();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	protected void updateRuns() throws XMLStreamException, IOException {
		// change this to be an open the xml document - or create if it doen't exist
		//
		// do so as an XML document
		//
		// then append the relevant information
		//		= problem this is with syncing to the directory tree when runs are deleted
		//		- just more smarts into the writeDirs function?
		
		if (docDir != null) {
			ODFERun odferuns = new ODFERun(docDir);
			odferuns.writeDocname(docDir.getName());
			odferuns.writeCreated(runDate);
			odferuns.writeDirs();
			odferuns.close();
			
			JsonODFERuns jsonRuns = new JsonODFERuns(docDir);
			jsonRuns.open();
			//what do we want here... the stuff that appears in the current runs report
			//	Run Date	Test Name	Num Paths	Min Depth	Max Depth	Avg Depth
			jsonRuns.writeExtract(extractDir);
			jsonRuns.writeCreated(runDate);
			jsonRuns.writeStats(xPathWriter.getNumPaths(), xPathWriter.getMinDepth(), xPathWriter.getMaxDepth(),
					xPathWriter.getAvgDepth());
			jsonRuns.writeDocname(docDir.getName());
			jsonRuns.writeComment(comment);
			jsonRuns.writeMode(processMode.toString());
			jsonRuns.writeProcessDepth(processDepth.toString()); 
			jsonRuns.writeNoAttributes(attributesOn);
			jsonRuns.writeXpathChangesOnly(isXPathChangesOnly());

			jsonRuns.close();
		}
	}

	/**
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	protected void writeRunError() throws XMLStreamException, IOException {
		// change this to be an open the xml document - or create if it doen't exist
		//
		// do so as an XML document
		//
		// then append the relevant information
		//		= problem this is with syncing to the directory tree when runs are deleted
		//		- just more smarts into the writeDirs function?
		
		if (docDir != null) {
			ODFERun odferuns = new ODFERun(docDir);
			odferuns.writeDocname(docDir.getName());
			odferuns.writeCreated(runDate);
			odferuns.writeDirs();
			odferuns.close();
			
			JsonODFERuns jsonRuns = new JsonODFERuns(docDir);
			jsonRuns.open();
			jsonRuns.writeDocname(docname);
			jsonRuns.writeComment(comment);
			jsonRuns.close();
			
		}
	}

	protected void openExtract(File odfFile, File recordsDir) {

		//If we are in aggregate mode we only need one directory
		//if not then we make multiple
		
		if (closed == true) {
			//Directory names with spaces in them cause hassles
			if(outputName.length() > 0) {
				docDir = new File(recordsDir, outputName.replace(' ', '_'));
			} else {
				docDir = new File(recordsDir, odfFile.getName().replace(' ', '_'));
			}
			if (!docDir.exists()) {
				docDir.mkdir();
				LOGGER.info("Created document directory:" + docDir.getAbsolutePath());
			}
			
			if (extractName != null) {
				extractDir = new File(docDir, extractName);
				LOGGER.info("Writing to extract directory: " + extractName);
			} 
			else {
				DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmmss_SSS");
				Date date = new Date();
				String rundate = dateFormat.format(date);

				extractDir = new File(docDir, rundate);
			}
			//Problem if two tests run in the same second
			// we lose one.
			if (!extractDir.exists()) {
				extractDir.mkdir();
				LOGGER.info("Created extract directory:" + extractDir.getAbsolutePath());
			}
				
			try {
				File resultsFile = new File(extractDir, METAFILE);
				resultsXMLWriter.open(resultsFile);

				File gaugesFile = new File(extractDir, GAUGES_EXT);
				gaugesWriter.open(gaugesFile);
				File gaugesXMLFile = new File(extractDir, "gauges.xml");
				gaugesXMLWriter.open(gaugesXMLFile);

				// keep styles info in a separate file?
				File stylesFile = new File(extractDir, "odfestyles.json");
				stylesWriter.open(stylesFile);
				File stylesXMLFile = new File(extractDir, "styles.xml");
				stylesXMLWriter.open(stylesXMLFile);
				
				File xPathFile = new File(extractDir, "xpath.json");
				xPathWriter.open(xPathFile);
				File xPathXMLFile = new File(extractDir, PATHSXMLFILE);
				xPathXMLWriter.open(xPathXMLFile, PATHSXSLFILE);
				xPathXMLWriter.writeJson(PATHSJSONFILE);
				
				File xPathDotFile = new File(extractDir, "xpath.dot");
				xPathDotWriter.open(xPathDotFile);
				xPathDotWriter.setOdfFilename(odfFile.getName());
				xPathDotWriter.setIncludeLegend(includeLegend);
			} catch (XMLStreamException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			closed = false;
		}
	}

	public boolean isFixedDateMode() {
		return fixedDateMode;
	}

	public void setFixedDateMode(boolean fixedDateMode) {
		this.fixedDateMode = fixedDateMode;
	}

	public void setFile(File file) {
		documentFile = file;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	protected void processDoc() {
		// this is where we call the ODFDOM

		// try {
		try {
			document = Document.loadDocument(documentFile);

			// ODFERun odfeRun = new ODFERun(documentFile.getName(), document,
			// extractDir.getParentFile());

			filePaths.clear();
			odfContentList(document);
			docname = documentFile.getName();
			DateFormat dateFormat = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
			Calendar cal = document.getOfficeMetadata().getCreationDate();
			if (cal != null) {
				Date date = document.getOfficeMetadata().getCreationDate().getTime();
				runDate = dateFormat.format(date);
			}
			// resultsXMLWriter.writeManifest(documentFile.getName(),
			// filePaths);
			// gaugesWriter.writeManifest(documentFile.getName(), filePaths);

			incrementGauges(document);

			// writeDotFile(document);

			// Separate this out as a selectable function
			// processStyles

			// We should have this as a difference tool like the gauges
			//
			// to make this a difference tool or accumulation tool
			// does it make sense to be an accumulation tool?
			// I think not.
			// Scenario would be to diff against a template
			// or simply examine the styles in a document
			//
			// to diff we need to add sources like we did in the odf gauges
			// but the underlying sets can be different?
			// Template has style set S
			// document has style set d which can be a subset of S or contain
			// new
			// elements
			//
			// we don't know if other document(s) are templates - just diffing
			// styles like in the gauges case
			// don't really need to know
			//
			//

			if(processMode != ProcessMode.AGGREGATE)
			{
				getStylesData(document);
				getStyleUsage(document);
			}
			//call back down to derived reports so they can handle the end of the run
			aggSum.setGauges(odfGauges);
			aggSum.fillSummary();
			processClose(document);
		} catch (Exception e) {
			LOGGER.severe("Unable to process " + documentFile.getName() + "\n" + e.getMessage());
			comment = "Unable to process " + documentFile.getName() + "\n" + e.getMessage();
			try {
				updateRuns();
			} catch (XMLStreamException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			closed = true;
		}
	}
	
	public void incrementGauges(Document document) {
		try {
			switch (processDepth) {
			case ALL: {
				contentWalker(document.getContentDom());
				stylesWalker(document.getStylesDom());
				metaWalker(document.getMetaDom());
				break;
			}
			case Styles: {
				stylesWalker(document.getStylesDom());
				break;
			}
			case MetaData: {
				metaWalker(document.getMetaDom());
				break;
			}
			default: {
				contentWalker(document.getContentDom());
			}
			}
		} catch (Exception e) {
			LOGGER.severe("Unable to increment the namespace gauges for " + documentFile.getName());
			e.printStackTrace();
		}
	}

	public void processClose(Document document) {
		close();
	}

	public void getStyleUsage(Document document) {
		try {
			styleExtractor = StyleExtractor.newOdfStyleExtractor(document.getContentRoot());

			styleExtractor.setStylesMap(stylesUsed);
			styleExtractor.setAutoStylesMap(autoStylesMap);

			styleExtractor.getStyles();

			stylesData.setStylesUsed(stylesUsed);
			stylesData.setAutoStyles(autoStylesMap);

			styleExtractor.dump();
		} catch (Exception e) {
			LOGGER.severe("Unable to get style usage from " + documentFile.getName());
			e.printStackTrace();
		}
	}

	private void getStylesData(Document document) {
		
		OdfOfficeStyles styles = document.getDocumentStyles();
		if (styles != null) { // null for chart docs

			// Create our own list of families and iterate it
			// Are there subsets of the list below that apply only to a given
			// document type?
			ArrayList<OdfStyleFamily> familyList = new ArrayList<OdfStyleFamily>();
			familyList.add(OdfStyleFamily.Chart);
			familyList.add(OdfStyleFamily.DrawingPage);
			familyList.add(OdfStyleFamily.Graphic);
			familyList.add(OdfStyleFamily.List);
			familyList.add(OdfStyleFamily.Paragraph);
			familyList.add(OdfStyleFamily.Presentation);
			familyList.add(OdfStyleFamily.Ruby);
			familyList.add(OdfStyleFamily.Section);
			familyList.add(OdfStyleFamily.Table);
			familyList.add(OdfStyleFamily.TableCell);
			familyList.add(OdfStyleFamily.TableColumn);
			familyList.add(OdfStyleFamily.TableRow);
			familyList.add(OdfStyleFamily.Text);

			// iterate the list of possible families
			for (OdfStyleFamily family : familyList) {
				Iterable<OdfStyle> familyStyles = styles.getStylesForFamily(family);

				// if there are styles in this family create a family tree to
				// track them
				if (familyStyles.iterator().hasNext()) {
					LOGGER.fine("Family: " + family.getName());
					styleFamilyTree.addFamily(family.getName(), family);
				}
				// get the styles in this family and add to the tree
				for (OdfStyle familyStyle : familyStyles) {
					LOGGER.fine("\nStyle name:\t" + familyStyle.getStyleNameAttribute());
					String parent = familyStyle.getStyleParentStyleNameAttribute();
					if (parent != null) {
						LOGGER.fine("\t from:" + parent);
					}

					styleFamilyTree.addFamilyStyle(family.getName(), familyStyle);
				}

				styleFamilyTree.closeFamily(family.getName());
			}

			// Get the Default Style data
			// Store in the family tree
			Iterable<OdfDefaultStyle> defaultStyles = styles.getDefaultStyles();
			for (OdfDefaultStyle defaultStyle : defaultStyles) {
				LOGGER.fine("\n\nDefault Style: " + defaultStyle.getFamilyName());
				OdfStyleBase parentFamily = defaultStyle.getParentStyle();
				if (parentFamily != null)
					System.out.println(" parent " + defaultStyle.getParentStyle().getFamilyName());

				styleFamilyTree.setDefault(defaultStyle.getFamilyName(), defaultStyle);
				styleFamilyTree.dump(defaultStyle.getFamilyName(), styles);
			}

			// This next lot is for the set of different predefined styles types
			// It also applies to the automatic styles section too
			// but for a document which strictly adheres to its template
			// there will be no automatic styles??
			/*
			 * <ref name="style-style"/> <ref name="text-list-style"/> <ref
			 * name="number-number-style"/> <ref name="number-currency-style"/>
			 * <ref name="number-percentage-style"/> <ref
			 * name="number-date-style"/> <ref name="number-time-style"/> <ref
			 * name="number-boolean-style"/> <ref name="number-text-style"/>
			 */

			// <ref name="text-list-style"/>
			// There is a content section here too.
			// But for the moment we will stick to the attributes
			//
			Iterable<OdfTextListStyle> listStyles = styles.getListStyles();
			for (OdfTextListStyle listStyle : listStyles) {
				stylesData.addTextListStyle(listStyle);
				// stylesWriter.writeTextListStyle(listStyle);
			}

			Iterable<OdfNumberCurrencyStyle> currencyStyles = styles.getCurrencyStyles();
			for (OdfNumberCurrencyStyle currencyStyle : currencyStyles) {
				stylesWriter.writeNumberCurrencyStyle(currencyStyle);
			}

			Iterable<OdfNumberDateStyle> dateStyles = styles.getDateStyles();
			for (OdfNumberDateStyle dateStyle : dateStyles) {
				stylesWriter.writeNumberDateStyle(dateStyle);
			}

			Iterable<OdfNumberStyle> numberStyles = styles.getNumberStyles();
			for (OdfNumberStyle numberStyle : numberStyles) {
				stylesWriter.writeNumberStyle(numberStyle);
			}

			Iterable<OdfNumberPercentageStyle> percentageStyles = styles.getPercentageStyles();
			for (OdfNumberPercentageStyle percentageStyle : percentageStyles) {
				stylesWriter.writeNumberPercentageStyle(percentageStyle);
			}

			Iterable<NumberBooleanStyleElement> booleanStyles = styles.getBooleanStyles();
			for (NumberBooleanStyleElement booleanStyle : booleanStyles) {
				stylesWriter.writeBooleanStyle(booleanStyle);
			}

			Iterable<NumberTextStyleElement> textStyles = styles.getTextStyles();
			for (NumberTextStyleElement numberTextStyle : textStyles) {
				stylesWriter.writeNumberTextStyle(numberTextStyle);
			}

			Iterable<OdfNumberTimeStyle> timeStyles = styles.getTimeStyles();
			for (OdfNumberTimeStyle timeStyle : timeStyles) {
				stylesWriter.writeNumberTimeStyle(timeStyle);
			}
		}

	}

	public void close() {
		try {
			if(!closed) {
				/* Looks like this summary should be common to all reports */
				//And not just an aggregation thing

				gaugesWriter.writeSummary(aggSum.getSummaryMap());
				
				gaugesWriter.setDepth(processDepth.toString());
				gaugesWriter.addComment(comment);
				gaugesWriter.writeSources(sourceNames);
				gaugesWriter.close();

				gaugesXMLWriter.close();

				if(processMode != ProcessMode.AGGREGATE)
				{
					stylesWriter.writeStyles();
					stylesWriter.close();
	
					stylesXMLWriter.close();
				}
				resultsXMLWriter.close();
				
				xPathWriter.writePaths(sourceNames, xPathRoot);
				xPathWriter.close();
				
				xPathXMLWriter.writeNumPaths(xPathWriter.getNumPaths());
				xPathXMLWriter.writeMinDepth(xPathWriter.getMinDepth());
				xPathXMLWriter.writeMaxDepth(xPathWriter.getMaxDepth());
				xPathXMLWriter.writeAvgDepth(xPathWriter.getAvgDepth());
				xPathXMLWriter.addComment(comment);
				xPathXMLWriter.close();
				xPathRoot.clearHitNdx();
				
				xPathDotWriter.writePaths(xPathRoot);
				if( xPathDotWriter.close() == false) {
					comment = "Dot writer failed. Is Graphiz installed correctly?";
				}
				
				updateRuns();

				closed = true;
				xPathRoot = null;
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Extract what is in the ODF File
	 * 
	 * @param document
	 */
	private void odfContentList(Document document) {
		// mimetype
		// content
		// styles
		// meta
		// settings
		// META-INF/manifest - this thing should tell us what is in the document
		// Versions
		// Thumbnails
		// Pictures

		OdfPackage pkg = document.getPackage();

		for (String file : pkg.getFilePaths()) {
			if (file != null)
				filePaths.add(file);
		}

	}

	/**
	 * Walk the DOM of the provided content and extract the gauges
	 * 
    ElementVisitor
	 * @param content
	 *            - XML DOM for the content
	 * @throws Exception 
	 */
	private void contentWalker(OdfFileDom content) {

		OdfElement root = content.getRootElement();
		//TextDocument textDoc = TextDocument.loadDocument(documentFile);
		
		//if we want just the content of the text we can treat the document
		//as a TextDocument. Then use TextDocument.loadDocument(file)
		// and getContentRoot for it

		
		//Node textRoot = textDoc.getContentRoot();

		XPathNode rootXNode = xPathRoot.addChild(root, false);
		rootXNode.setParent(xPathRoot);
		
		walkNode(root, rootXNode);
	}

	private void metaWalker(OdfMetaDom meta) {
		OfficeDocumentMetaElement root = meta.getRootElement();
		XPathNode rootXNode = xPathRoot.addChild(root, false);
		rootXNode.setParent(xPathRoot);
		walkNode(root, rootXNode);
	}

	// Within the styles we can itemise fonts, styles
	// then correlate which are actually used in the content

	// In a more general sense we can also gauge the attributes used

	private void stylesWalker(OdfStylesDom odfStylesDom) {
		OfficeDocumentStylesElement root = odfStylesDom.getRootElement();
		if(root != null) { //chart documents return null here?
			XPathNode rootXNode = xPathRoot.addChild(root, false);
			rootXNode.setParent(xPathRoot);
			walkNode(root, rootXNode);
		}
	}

	private void walkNode(Node theNode, XPathNode xPathParent) {
		hitNodeGauge(theNode);
		if(theNode.hasChildNodes()) {
			NodeList children = theNode.getChildNodes();
	/*		if(theNode.getNodeType() == Node.ELEMENT_NODE) {
				xPathParent.hit();
			}*/
			int numElements = 0;
			for (int i = 0; i < children.getLength(); i++) {
				Node aNode = children.item(i);
				if(aNode.getNodeType() == Node.ELEMENT_NODE) {
					OdfElement odfNode = (OdfElement) aNode;
					XPathNode xchild = null;
						xchild = xPathParent.addChild(odfNode, attributesOn);
						xchild.setParent(xPathParent);
					numElements++;
					walkNode(aNode, xchild);
				}
			}
			if(numElements == 0) { //e.g. a text paragraph with only text nodes
				xPathParent.addSourceNdx(srcNdx);
			}
		}
		else {
			xPathParent.addSourceNdx(srcNdx);
		}
	}

	/**
	 * We want to increment the Element Gauge each time an element is found If
	 * the node has many attributes it should only be hit once for the element
	 * and each Attribute Gauge will be hit
	 * 
	 * @param node
	 */
	private void hitNodeGauge(Node node) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			odfGauges.hit(srcNdx, node.getPrefix(), node.getNodeName(), null);
			if (node.hasAttributes()) {
				NamedNodeMap attrs = node.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++) {
					Node aNode = attrs.item(i);
					odfGauges.hit(srcNdx, node.getPrefix(), node.getNodeName(), aNode.getNodeName());
				}
			}
		}
	}

	public String addAttributesFrom(Node toNode, String basePath) {
		String attPath = basePath;
		if (toNode.hasAttributes()) {
			attPath += "[";
			NamedNodeMap attrs = toNode.getAttributes();
			Node aNode = attrs.item(0);
			attPath += "@" + aNode.getNodeName() + " = '" + aNode.getNodeValue() + "'";
			for (int a = 1; a < attrs.getLength(); a++) {
				aNode = attrs.item(a);
				//need to quote attribute strings - but not numbers?
				attPath += " and @" + aNode.getNodeName() + " = '" + aNode.getNodeValue() + "'";
			}
			attPath += "]";
		}
		return attPath;		
	}

	public ProcessDepth getProcessDepth() {
		return processDepth;
	}

	public void setProcessDepth(ProcessDepth processDepth) {
		this.processDepth = processDepth;
	}

	public Boolean getIncludeLegend() {
		return includeLegend;
	}

	public void setIncludeLegend(Boolean includeLegend) {
		this.includeLegend = includeLegend;
	}

	public ProcessMode getProcessMode() {
		return processMode;
	}

	public String getCreated() {
		return runDate;
	}

	public String getDocname() {
		return docname;
	}

	public Boolean getClosed() {
		return closed;
	}

	public void setHitsOnly(boolean b) {
		gaugesWriter.setHitsOnly(b);
	}

	public boolean istHitsOnly() {
		return gaugesWriter.getHitsOnly();
	}

	public void setExtractName(String toDirName) {
		extractName = toDirName;
	}

	public void setXPathChangesOnly() {
		XPathNode.setMode(XPathNode.Mode.CHANGED_ONLY);
	}
	
	public File getCommentedDocument() {
		return commentedDocument;
	}
	
	public void addComment(String cmt) {
		if(comment.length() > 0) {
			comment += " " + cmt;
		} else {
			comment = cmt;
		}
	}

	public String getComment() {
		return comment;
	}

	public boolean isXPathChangesOnly() {
		return (XPathNode.getMode() == XPathNode.Mode.CHANGED_ONLY) ? true : false;
	}

	public int getNumDiffs() {
		return numDiffs;
	}

	public void setGenerateCommentedDoc(boolean genDoc) {
		genCommentedDoc = genDoc;
	}
	public boolean getGenerateCommentedDoc() {
		return genCommentedDoc;
	}

	public void setAttributesOn(boolean b) {
		attributesOn = b;
	}
	public boolean isAttributesOn() {
		return attributesOn;
	}

	public void startBrowser(String browserCommandStr) {
		CommandRunner runner = new CommandRunner();
		try {
			// this needs to be done in the extract directory
			runner.run(browserCommandStr + " odferuns.xml", extractDir.getParentFile());
			LOGGER.info("Open browser with " + browserCommandStr + " from " + extractDir.getParentFile());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public String getExtractName() {
		return extractName == null ? "" : extractName;
	}
	
	public File getModeDirectory() {
		return modeDir;
	}

	public void setOutputName(String outputOverride) {
		outputName = outputOverride;
		
	}
	
	public void addSource(String sourceName) {
		LOGGER.info("Add Source " + sourceName);
		sourceNames.add(sourceName);
		srcNdx++;
	}
}
