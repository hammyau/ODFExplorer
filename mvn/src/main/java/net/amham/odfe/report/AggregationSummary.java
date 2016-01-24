package net.amham.odfe.report;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import net.amham.odfe.gauges.OdfAttributeGauge;
import net.amham.odfe.gauges.OdfElementGauge;
import net.amham.odfe.gauges.OdfGaugeStore;
import net.amham.odfe.json.GaugeJSONWriter;
import net.amham.odfe.report.AggregationSummary.SummaryRow;

/**
 * Here we want to roll through the iterations of an aggregation
 * and build up a summary of what happened.
 * 
 * For each namespace show the # elements # elements hit, attributes - for each document/iteration
 *
 * Do we attempt to do this in a single table... me thinks not
 * 	Iterations have many documents so flatten that way?
 * 
 * Should we collection the data for each element?
 * What would that give?
 *
 */
public class AggregationSummary extends ReportBase {
	
	private final static   Logger LOGGER = Logger.getLogger(AggregationSummary.class.getName());

	private String aggregationName;
	
	public class SummaryRow{
		public int numElements;
		public ArrayList<Integer> elementsHit = new ArrayList<Integer>();
		public int numAttributes;
		public ArrayList<Integer> attributesHit  = new ArrayList<Integer>();
	};
	
	private SortedMap <String, SummaryRow> namespaceSummaryMap = new TreeMap <String, SummaryRow>();
 

	/* the aggregation name effectively tells us the directory
	 * 
	 */
	public AggregationSummary(String name) {
		aggregationName = name;
	}
	
	public boolean generate(File recordsDir) {
		boolean completed = true;
		File modeDir = new File(recordsDir, "Aggregations");
		File aggDir = new File(modeDir, aggregationName);
		if(aggDir.exists() == false) {
			//need to explain why
			LOGGER.severe("No Aggregations found for " + aggDir);
			completed = false;
		} else {
			// open the aggregation directory
			File[] iterations = aggDir.listFiles(new FilenameFilter() {
				public boolean accept(File current, String name) {
					return new File(current, name).isDirectory();
				}
			});
			List<File> iterationsList = Arrays.asList(iterations);
			
			// for each iteration
			// 		add info to data tables
			for(File iteration : iterationsList ) {
				fillGauges(iteration);
				fillSummary();
			}
		}
		
		return completed;
	}

	public void fillSummary() {
		SortedMap<String, SortedMap<String, OdfElementGauge>> nsGaugeMap = odfGauges.getNamespaceGaugeMap();
		//for each namespace iterate and get the number of elements hit
		for (String ns : nsGaugeMap.keySet()) {
			System.out.println(odfGauges.getName() + " Namespace:" + ns);
			//the number of elements hit on a ns is usually calculated in javascript
			//walk the elements and count those with >0 hits
			
			int nsElementsHit = 0;
			int nsElementsCount = 0;
			int nsAttributesHit = 0;
			int nsAttributesCount = 0;
			SortedMap<String, OdfElementGauge> gaugeMap = nsGaugeMap.get(ns);
			nsElementsCount = gaugeMap.size();
			for (String elementName : gaugeMap.keySet()) {
				//we will need to add nsHits elements
				OdfElementGauge elementGauge = odfGauges.getNamespaceGaugeMap().get(ns).get(elementName);
				List<Integer> hitlist = elementGauge.getHits();
				//has this element been hit?
				boolean beenHit=false;
				Iterator<Integer> it = hitlist.iterator();
				while(beenHit == false && it.hasNext()) {
					Integer hits = (Integer) it.next();
					if(hits > 0) {
						beenHit = true;
						nsElementsHit++; 
						LOGGER.info("ELEMENT USED " + ns + ":" + elementName + " count" + nsElementsHit);
					}
				}
				SortedMap<String, OdfAttributeGauge> attrMap = elementGauge.getAttributesGaugeMap();
				if(attrMap.size() > 0) {
					nsAttributesCount += attrMap.size(); 
					for (String attrName : attrMap.keySet()) {
						List<Integer> atthitlist = attrMap.get(attrName).getHits();
						if(hitlist.size() > 0) {
							boolean attrBeenHit=false;
							Iterator<Integer> attrit = atthitlist.iterator();
							while(attrBeenHit == false && attrit.hasNext()) {
								Integer attrhits = (Integer) attrit.next();
								if(attrhits > 0) {
									attrBeenHit = true;
									nsAttributesHit++;
								}
							}
						}
					}
				}
			}
			LOGGER.info("NS:" + ns + " num elements: " + nsElementsCount + " Hit: " + nsElementsHit +
					" num attributes: " + nsAttributesCount + " Hits: " + nsAttributesHit);
			
			 SummaryRow sumRow = namespaceSummaryMap.get(ns);
			 if(sumRow == null) {
				 sumRow = new SummaryRow();
			 }
			 sumRow.numElements = nsElementsCount;
			 sumRow.elementsHit.add(nsElementsHit);
			 sumRow.numAttributes = nsAttributesCount;
			 sumRow.attributesHit.add(nsAttributesHit);
			 namespaceSummaryMap.put(ns, sumRow);
		}
		
		//once we get this data where are we going to put it?
		//in the Aggregation json file as an additional section?
	}

	public void fillGauges(File iteration) {
		LOGGER.info("Aggregate Summary info from " + iteration);
		GaugeJSONWriter gaugesReader = new GaugeJSONWriter();   
		//
		File lastGaugesFile = new File(iteration, GAUGES_EXT);
		if (odfGauges == null) {
			//this should be the aggregation name
			odfGauges = new OdfGaugeStore(aggregationName);
		}
		gaugesReader.read(lastGaugesFile, odfGauges, this);
	}
	
	SortedMap<String, SummaryRow> getSummaryMap() {
		return namespaceSummaryMap;
	}

	public SummaryRow getSummaryRow() {
		return new SummaryRow();
	}
	
	public void addSummaryRow(String ns, SummaryRow sumRow) {
		 namespaceSummaryMap.put(ns, sumRow);
	}
	
}
