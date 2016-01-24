package net.amham.odfe.report;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;

import javax.xml.stream.XMLStreamException;

import org.odftoolkit.simple.Document;

import net.amham.odfe.gauges.OdfGaugeStore;
import net.amham.odfe.json.GaugeJSONWriter;
import net.amham.odfe.json.JsonODFERuns;
import net.amham.odfe.style.StyleFamilyTreeBuilder;
import net.amham.odfe.style.StylesData;
import net.amham.odfe.xpath.XPathNode;

public class AggregateReport extends DocumentReport {
	
	private boolean buildOnExisting = true;

	public AggregateReport() {
		processMode = ProcessMode.AGGREGATE;
		gaugesWriter.setMode(processMode.toString());
		stylesWriter.setMode(processMode.toString());
		xPathWriter.setMode(processMode.toString());
	}

	@Override
	public void generate(File odfFile, File recordsDir) {
		documentFile = odfFile;

		modeDir = new File(recordsDir, "Aggregations");
		if(modeDir.exists() == false) {
			modeDir.mkdir();
		}
		openExtract(odfFile, modeDir);

		if (odfGauges == null) {
			odfGauges = new OdfGaugeStore(documentFile.getName());
			aggSum = new AggregationSummary(documentFile.getName());
			String lastRun = "";
			try {
				JsonODFERuns runs = new JsonODFERuns(docDir);
				runs.read();
				lastRun = runs.getLastRun();
				LOGGER.info("Last run " + lastRun);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(lastRun.length() > 0)
			{
				//need to get the name of the previous run
				//so we can initialise the GaugeStrore from it
				GaugeJSONWriter gaugesReader = new GaugeJSONWriter();   
				//
				File lastExtractDir = new File(docDir, lastRun);
				File lastGaugesFile = new File(lastExtractDir, GAUGES_EXT);
				gaugesReader.read(lastGaugesFile, odfGauges, aggSum);
				
				//so the summary of the old gauges will now be in the firsr array entry
				//we need to make sure all new entries are added to the array
				addSource("Previous Run(s)");
				
				//Use a FilteredXpathGraph with no filters to read the old xpath.json
				List<String> emtpyFilters = new ArrayList<String>();
				FilteredXPathGraph fxpg = new FilteredXPathGraph("Aggregations/"+docDir.getName(), 
						lastExtractDir.getName(), 
						emtpyFilters);
				xPathRoot = fxpg.generate(recordsDir, true);
				//we need to reset the state of each node to ORIG
				//can this be done as it is generated

			}
			gaugesWriter.addGauges(odfGauges);
		}

		//Aggregating the style data doesn't make sense?
		//Just log some sort of message to that effect?
		/*
		if (stylesData == null) {
			stylesData = new StylesData();
			stylesWriter.addData(stylesData);
		}
		else {
			stylesWriter.addSource(odfFile.getName());
		}

		if(styleFamilyTree == null) {
			styleFamilyTree = new StyleFamilyTreeBuilder(documentFile.getName());
			stylesData.setDocumentName(documentFile.getName());
			stylesData.setFamilyTree(styleFamilyTree.getStyleFamiliesMap());
		}
		*/
		
		//this doesn't make sense either
		//but world is broken if we don't include it
		//Perhaps this makes sense without attributes only
		setAttributesOn(false);		
		if(xPathRoot == null) {
			xPathRoot = new XPathNode("/");
		}
		addSource(documentFile.getName());
		xPathRoot.setSrcNdx(srcNdx);
		XPathNode.setMode(XPathNode.Mode.ALL); // override for aggregation mode		
		
		processDoc();
	}

	@Override
	public void processClose(Document document){
		//empty here to not close off until all files have been processed
	}
	
	@Override
	public void close() {
		System.out.println("Paths");
		xPathRoot.dump("");
		
		// TODO 
		//gotta be a better way than this
//		aggSum.setGauges(odfGauges);
//		aggSum.fillSummary();

//		gaugesWriter.writeSummary(aggSum.getSummaryMap());
		
		super.close();
	}	

}
