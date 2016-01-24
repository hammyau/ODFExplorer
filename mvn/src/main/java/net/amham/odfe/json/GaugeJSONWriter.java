package net.amham.odfe.json;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartDocument;

import net.amham.odfe.gauges.OdfAttributeGauge;
import net.amham.odfe.gauges.OdfElementGauge;
import net.amham.odfe.gauges.OdfGaugeStore;
import net.amham.odfe.report.AggregationSummary;
import net.amham.odfe.report.AggregationSummary.SummaryRow;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jettison.AbstractXMLStreamWriter;


/**
 * Write out the gauge values to an XML file
 * 
 * 
 * @author ian
 *
 */
public class GaugeJSONWriter {

	private final static   Logger LOGGER = Logger.getLogger(GaugeJSONWriter.class.getName());
	
	private static final String CHILDREN_TAG = "children";
	private static final String HIDDEN_CHILDREN_TAG = "_children";

	private String ODFE_XSL = "xml-stylesheet type='text/xsl' href='";
	
	private static final String START_TAG = "odfexplorer";
	private static final String SOURCES_TAG = "sources";
	private static final String SOURCE_TAG = "source";
	private static final String SOURCE_NUMBER_TAG = "source-number";
	private static final String DOC_TAG = "docname";
	private static final String RUN_TAG = "rundate";

	private static final String MANIFEST_TAG = "manifest";
	private static final String MANIFEST_FILE_TAG = "file";

	private static final String META_TAG = "meta";
	private static final String METAVALUES_TAG = "metaValues";
	private static final String STYLES_TAG = "styles";
	private static final String STYLES_NAME_TAG = "name";

	private static final String GAUGES_TAG = "gauges";
	private static final String NAMESPACE_TAG = "Namespaces";
	private static final String ELEMENTS_TAG = "elements";
	private static final String ELEMENT_TAG = "element";
	private static final String ATTR_GAUGES_TAG = "attributes";
	private static final String ATTRIBUTE_TAG = "attribute";
	private static final String MANDATORY_TAG = "mandatory";
	private static final String GAUGE_TAG = "gauge";
	private static final String GAUGE_NAME_TAG = "name";
	private static final String GAUGE_VALUE_TAG = "value";
	private static final String GAUGE_HITS_TAG = "hits";	
	private static final String NOT_USED_TAG = "notused";
	
	private XMLEventFactory m_eventFactory = XMLEventFactory.newInstance();
	
	private XMLOutputFactory m_xof =  XMLOutputFactory.newInstance();

	private AbstractXMLStreamWriter m_jsonWriter;
	private JsonGenerator generator = null;
	private ObjectNode rootNode = null;
	private ArrayNode rootArray = null;
	
	//private Vector<String> sourceNames = new Vector<String>();
	private OdfGaugeStore gauges = null;

	private String mode;

	private String depth;

	private boolean hitsOnly = false;

	private String comment;
	
	/**
	 * Open an XML output file
	 * The file output may be derived from many input sources
	 * That is from a set of OdfGauges
	 * 
	 * @param name
	 * @param string 
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void open(File file) throws XMLStreamException, IOException {
		
		//gauges = new OdfGaugeStore(file.getName());
		JsonFactory f = new JsonFactory();
		try {
			generator = f.createJsonGenerator(new FileWriter(file));
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
			generator.setCodec(mapper);
			generator.useDefaultPrettyPrinter();

			rootNode = mapper.createObjectNode();
			rootNode.put("name", "odfegauges");
			rootArray = rootNode.putArray(CHILDREN_TAG);
			
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read from named file and load up the gaugeStore
	 * @param aggSum 
	 *
	 * @return true = read okay
	 */
	public boolean read(File lastExtractFile, OdfGaugeStore odfGauges, AggregationSummary aggSum) {
		boolean readOkay = false;


		JsonFactory jFactory = new JsonFactory();
		JsonParser jParser;
		try {
			jParser = jFactory.createJsonParser(lastExtractFile);
			ObjectMapper mapper = new ObjectMapper();
			jParser.setCodec(mapper);
			rootNode = (ObjectNode) jParser.readValueAsTree();
			if(rootNode != null){
				fillGaugeStore(odfGauges);
				//where are we going to put the Summary
				//it should live alongside the odfGauges?
				fillSummary(aggSum);
				readOkay = true;
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return readOkay;
	}
	
	/* If we are filling the gauge store we just total up
	 * the hits of a previous run and insert them to gauge[0]
	 * 
	 */
	

	private void fillGaugeStore(OdfGaugeStore odfGauges) {
		ArrayNode odfeGaugesArray = (ArrayNode) rootNode.get("children");
		JsonNode gauges = odfeGaugesArray.get(1);
		System.out.println("fillGaugeStore@Gauges: " + gauges.get("name"));
		JsonNode gaugeNamespacesArray = gauges.get("_children");
		//Don't get an iterator for an Array
		//step through by index?
		Iterator<JsonNode> namespaces = null;
		if (gaugeNamespacesArray.isArray()) {
			JsonNode namespacesArray = gaugeNamespacesArray.get(0).get("_children");
			namespaces = namespacesArray.getElements();
		}
		
		SortedMap <String, SortedMap <String, OdfElementGauge>> nsGaugeMap = odfGauges.getNamespaceGaugeMap();
		int ndx = 0;
		for(String ns : nsGaugeMap.keySet()) {
			int i = 0;
			JsonNode jns = namespaces.next();
			if (ns.equals(jns.get("name").asText())) {
				System.out.println("Synced ns: " + ns);

				JsonNode nsHitsArray = jns.get("hits");
				int nshits = 0;
				if(nsHitsArray.isArray()) {
					for(int nsi=0; nsi<nsHitsArray.size(); nsi++) { 
						nshits +=  nsHitsArray.get(nsi).asInt();
					}
				}
				if (nshits > 0) {

					// iterate the namespace elements
					Iterator<JsonNode> elements = null;
					JsonNode elementsArray = jns.get("_children");
					elements = elementsArray.getElements();

					SortedMap<String, OdfElementGauge> gaugeMap = nsGaugeMap.get(ns);
					for (String elementName : gaugeMap.keySet()) {
						JsonNode elem = elements.next();
						if (elementName.equals(elem.get("name").asText())) {
							// we are doing this for aggregations and hits will
							// only have a single entry - oh no they won't
							// so
							ArrayNode hitsArray = (ArrayNode) elem.get("hits");
							if (hitsArray != null && hitsArray.size() > 0) {

								//System.out.println("Write " + elementName + " hits to " + hits);
								OdfElementGauge elementGauge = odfGauges.getNamespaceGaugeMap().get(ns).get(elementName);
								int totalHits = 0;
								for(int h=0; h < hitsArray.size(); h++) {
									totalHits += hitsArray.get(h).asInt();
								}
								elementGauge.setHits(0, totalHits);
								// now nest into the attributes
								Iterator<JsonNode> attrs = null;
								JsonNode attrsArray = elem.get("_children");
								if (attrsArray != null) {
									attrs = attrsArray.getElements();
									SortedMap<String, OdfAttributeGauge> attrMap = elementGauge.getAttributesGaugeMap();
									if (attrMap.size() > 0) {
										for (String attrName : attrMap.keySet()) {
											JsonNode attr = attrs.next();
											if (attrName.equals(attr.get("name").asText())) {
												JsonNode attrHitsArray = attr.get("hits");
												if(attrHitsArray != null) {
													totalHits = 0;
													OdfAttributeGauge attribute = elementGauge.getAttributesGaugeMap().get(
															attrName);
													for(int h=0; h < attrHitsArray.size(); h++) {
														totalHits += attrHitsArray.get(h).asInt();
													}
													attribute.setHits(0, totalHits);
												}
											} else {
												StringBuilder syncMessage = new StringBuilder();
												syncMessage.append(attrName);
												syncMessage.append(" missing");
												System.out.println(syncMessage.toString());
											}
										}
									}
								}
							}
						} else {
							StringBuilder syncMessage = new StringBuilder();
							syncMessage.append(elementName);
							syncMessage.append(" missing");
							System.out.println(syncMessage.toString());
						}
					}
				}
				else {
					LOGGER.info("No hits ... skipping " + ns);
				}
				
			} else {
				StringBuilder syncMessage = new StringBuilder();
				syncMessage.append(ns);
				syncMessage.append(" missing");
				System.out.println(syncMessage.toString());
			}
		}
	}

	/**
	 * Add a named OdfGaugeStore to the writer
	 * 
	 * Will need a version of this that supplies the rundate too
	 * @param source
	 * @param odfGauges
	 * @throws XMLStreamException 
	 */
	public void addGauges(OdfGaugeStore odfGauges){
		gauges = odfGauges;
//		sourceNames.add(odfGauges.getName());
	}
	
	
	public void writeSources(List<String> sourceNames) throws XMLStreamException {
		ObjectNode srcs = rootArray.addObject();
		srcs.put("name", "sources");
		ArrayNode an = srcs.putArray(CHILDREN_TAG);
		for (String name : sourceNames) {
			ObjectNode doc = an.addObject();
			doc.put(DOC_TAG, name);
		}
	}
	
	private void writeRunInfo() throws XMLStreamException {
		rootNode.put("document", gauges.getName());
		rootNode.put("rundate", gauges.getRundate());
		rootNode.put("mode", mode);
		rootNode.put("depth", depth);
		rootNode.put("hits", hitsOnly);
		rootNode.put("comment", comment);
		//we could have some summary stats here
		//how do we get them?
//		ArrayNode summary = run.putArray(CHILDREN_TAG);
/*		for (String name : sourceNames) {
			ObjectNode doc = an.addObject();
			doc.put(DOC_TAG, name);
		}*/
	}
	
	/**
	 * Write out the gauges for each of the sources
	 * Iterate the namespaces and then the attributes within each element
	 * 
	 * We could make the attribute gauges optional?
	 * 
	 * @param odfGauges
	 * @throws XMLStreamException
	 */
	public void writeGauges() throws XMLStreamException {
		ObjectNode gaugesObj = rootArray.addObject();
		gaugesObj.put("name", GAUGES_TAG);
		ArrayNode gaugesArray = gaugesObj.putArray(HIDDEN_CHILDREN_TAG);

		// we can just use the first gauge set as the the key to iterate from

		OdfGaugeStore odfGauges = gauges;

		Integer totalHits = 0;
		List<Integer> nsHits = new ArrayList<Integer>();
		nsHits.add(0);

		ObjectNode nsCollector = gaugesArray.addObject();
		nsCollector.put("name", NAMESPACE_TAG);
		ArrayNode nsArray = nsCollector.putArray(HIDDEN_CHILDREN_TAG);
		SortedMap<String, SortedMap<String, OdfElementGauge>> nsGaugeMap = odfGauges.getNamespaceGaugeMap();
		//for each namespace iterate and write its element hits
		for (String ns : nsGaugeMap.keySet()) {
			//initialise the array as all 0 and the totalHits too
			for(int i=0; i<nsHits.size(); i++) {
				nsHits.set(i, 0);
			}
			totalHits = 0;

			ObjectNode nsObj = nsArray.addObject();
			nsObj.put("name", ns);
			ArrayNode elArray = nsObj.putArray(HIDDEN_CHILDREN_TAG);

			SortedMap<String, OdfElementGauge> gaugeMap = nsGaugeMap.get(ns);
			for (String elementName : gaugeMap.keySet()) {
				int j = 0;
				List<Integer> eleHits = writeGaugeValue(ns, elementName, elArray);
				//we will need to add nsHits elements
				while(eleHits.size() > nsHits.size()) {
					nsHits.add(0);
				}
				for (Integer h : eleHits) {
					//prob if more than one element gauge.... 
					//need parallel size of ns gauges?
					//where are the ns hits used? only in the table?
					Integer t = nsHits.get(j);
					t += h;
					nsHits.set(j, t);
					totalHits += h;
					j++;
				}
			}

			ArrayNode hitsArray = nsObj.putArray("hits");
			for (Integer hit : nsHits) {
				hitsArray.add(hit);
			}
		}
	}
	
	public void writeManifest(String name, SortedSet<String> files) throws XMLStreamException {
		ObjectNode manifestObj = rootArray.addObject();
		manifestObj.put("name", MANIFEST_TAG);
		ArrayNode manifestArray= manifestObj.putArray(HIDDEN_CHILDREN_TAG);

		for(String file : files)
		{
			ObjectNode  fileObj= manifestArray.addObject();
			fileObj.put("name", file);
		}
	}
	
	/**
	 * Write the element gauge and iterate the sources
	 * Writing the total hits is not needed
	 * but makes later processing a bit easier
	 * 
	 * Write that attributes within the gauge if any has hits > 0
	 * 
	 * @param elementName
	 * @param elArray 
	 * @param odfElementGauge
	 * @throws XMLStreamException 
	 */
	private List<Integer> writeGaugeValue(String ns, String elementName, ArrayNode elArray) throws XMLStreamException {
		ObjectNode  elObj= elArray.addObject();
		elObj.put("name", elementName);
		OdfElementGauge elementGauge = gauges.getNamespaceGaugeMap().get(ns).get(elementName);
		List<Integer> hitlist = elementGauge.getHits();
		ArrayNode hitsArray = elObj.putArray("hits");
		for (Integer hit : hitlist) {
			hitsArray.add(hit);
		}
		SortedMap<String, OdfAttributeGauge> attrMap = elementGauge.getAttributesGaugeMap();
		if(attrMap.size() > 0) {
			ArrayNode attrArray= elObj.putArray(HIDDEN_CHILDREN_TAG);
			writeGaugeAtributeValues(attrMap, ns, elementName, attrArray);
		}

		return hitlist;
	}

	private void writeGaugeAtributeValues(SortedMap<String, OdfAttributeGauge> attrMap, String ns, String elementName, ArrayNode attrArray) throws XMLStreamException {
		for (String attrName : attrMap.keySet()) {
			ObjectNode  attrObj= attrArray.addObject();
			attrObj.put("name", attrName);
			List<Integer> hits = new ArrayList<Integer>();
//			Integer totalHits = 0;

			SortedMap<String, OdfAttributeGauge> gsAttributesGaugeMap = gauges.getNamespaceGaugeMap().get(ns).get(elementName)
					.getAttributesGaugeMap();
			List<Integer> hitlist = gsAttributesGaugeMap.get(attrName).getHits();
			if(hitlist.size() > 0) {
				ArrayNode hitsArray = attrObj.putArray("hits");
				for (Integer hit : hitlist) {
					hitsArray.add(hit);
				}
			}
		}
	}

	public void close() throws XMLStreamException, JsonProcessingException, IOException {
		writeRunInfo();
		writeGauges();
		generator.writeTree(rootNode);
		generator.close(); 
		LOGGER.info("Gauges JSON Writer Closed");
	}

	public void writeContentAttrGauges(
			SortedMap<String, Integer> elementGaugeMap) {
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setDepth(String depth) {
		this.depth = depth;
	}

	public void setHitsOnly(boolean hitsOnly) {
		this.hitsOnly = hitsOnly;
	}

	public void addComment(String cmnt) {
		comment = cmnt;
		
	}

	public boolean getHitsOnly() {
		return hitsOnly;
	}
	
	private void addHits(ObjectNode pathObject, List<Integer> hitList) {
		if (hitList.size() > 0) {
			ArrayNode hitsArray = pathObject.putArray("hits");
			for (Integer hits : hitList) {
				hitsArray.add(hits);
			}
		}
	}
	
	public void writeSummary(SortedMap <String, SummaryRow> summary) {
		//iterate through the map and dump the data
		ArrayNode summaryArray = rootNode.putArray("summary");
		for(String ns : summary.keySet()) {
			SummaryRow row = summary.get(ns);
			ObjectNode rowObj = summaryArray.addObject();
			rowObj.put("ns", ns);
			rowObj.put("elements", row.numElements);
			ArrayNode elhits = rowObj.putArray("elementsHit");
			for(Integer h : row.elementsHit) {
				elhits.add(h);
			}
			rowObj.put("attributes", row.numAttributes);
			ArrayNode attrhits = rowObj.putArray("attrsHit");
			for(Integer h : row.attributesHit) {
				attrhits.add(h);
			}
		}
	}
	
	private void fillSummary(AggregationSummary aggSum) {
		JsonNode summaryArray = rootNode.get("summary");

		Iterator<JsonNode> namespaces = null;
		if (summaryArray.isArray()) {
			namespaces = summaryArray.getElements();
			while (namespaces.hasNext()) {
				JsonNode nsNode = namespaces.next();
				SummaryRow sumRow = aggSum.getSummaryRow();
				sumRow.numElements = nsNode.get("elements").asInt();
				JsonNode elHitsArray = nsNode.get("elementsHit");
				if (elHitsArray.isArray()) {
					for (int h = 0; h < elHitsArray.size(); h++) {
						sumRow.elementsHit.add(elHitsArray.get(h).asInt());
					}
				}
				sumRow.numAttributes = nsNode.get("attributes").asInt();
				JsonNode attrHitsArray = nsNode.get("attrsHit");
				if (attrHitsArray.isArray()) {
					for (int h = 0; h < attrHitsArray.size(); h++) {
						sumRow.attributesHit.add(attrHitsArray.get(h).asInt());
					}
				}
				aggSum.addSummaryRow(nsNode.get("ns").asText(), sumRow);
			}
		}
	}
}

