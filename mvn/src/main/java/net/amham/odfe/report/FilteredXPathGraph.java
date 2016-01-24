package net.amham.odfe.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import net.amham.odfe.graphviz.DotWriter;
import net.amham.odfe.json.JsonODFERuns;
import net.amham.odfe.xpath.XPathNode;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class FilteredXPathGraph {

	private final static   Logger LOGGER = Logger.getLogger(FilteredXPathGraph.class.getName());
	
	private String doc;
	private String extract;
	private List<String> filters;
	
	protected XPathNode xPathRoot = null;  
	
	protected DotWriter xPathDotWriter;

	private ArrayNode filtersArray;

	private JsonFactory jFactory;

	private ObjectMapper mapper;

	private ObjectNode rootNode;

	private ArrayNode srcsArray;

	private File jsonFile;

	private File docDir;
	
	private boolean asOriginal = false;

	public FilteredXPathGraph(String docToFilter, String extractDir, List<String> filterList) {
		doc = docToFilter;
		extract = extractDir;
		filters = filterList;
	}
	
	// we could add filter on changes only
	// 

	public XPathNode generate(File records, boolean asOriginalVal) {
		asOriginal = asOriginalVal;
		// do we have records for the doc
		
		// does it have the associated extract
		docDir = new File(records, doc.replace(' ', '_'));
		if (docDir.exists()) {
			File extractDir = new File(docDir, extract);
			if (extractDir.exists()) {
				// do we have an existing filtered file?
				// just need a filters section in THE path.JSON file
				// if we use THE file we will need a filter reset method
				
				// the JSON file then becomes an intermediate form from which the dot file is generated
				// should this replace the existing XPathNodes form
				//	or do we read the JSON file and build up an XPath node tree?
				
				// grab the xpath.json
				// update its tree with new filter information
				//	and generate a dot file from it
				//	ignoring the filtered branches
				//
				jsonFile = new File(extractDir, "xpath.json");
				//does the document already exist
				jFactory = new JsonFactory();
				mapper = new ObjectMapper();
				JsonParser jParser;
				try {
					jParser = jFactory.createJsonParser(jsonFile);
					jParser.setCodec(mapper);
					rootNode = (ObjectNode) jParser.readValueAsTree();
					
					
					rootNode.remove("filters");
					//add a filtersArray - always make a new one?
					//since the incoming filters list will be an accumulation of filters
					filtersArray = rootNode.putArray("filters");
					
//					System.out.println(rootNode.toString());
					ArrayNode childArray = (ArrayNode) rootNode.findValue("children");
					JsonNode srcs = childArray.get(0);
					srcsArray = (ArrayNode)srcs.get("children");
					
					LOGGER.info("Filtering " + docDir.getName() + " first source " + srcsArray.get(0).get("source").getTextValue());
					JsonNode rootPaths = childArray.get(1);
					
					xPathRoot = new XPathNode(rootPaths.get("name").asText());
					xPathRoot.setNodeID(rootPaths.get("id").asInt());
					if(!asOriginal) {
						xPathRoot.setState(rootPaths.get("state").asText());
					}
					
					filterNodes(xPathRoot, rootPaths);
					
					
					//Add the filters to the JSON file and write back
					
					//call graphviz to generate the svg
					
					
				} catch (JsonParseException e) {
					LOGGER.severe(e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.severe(jsonFile.getName() + " not found");
					e.printStackTrace();
				}
			} else {
				// want the return to go back the user...
			}
		} 
		else {
		}		
		return xPathRoot;
	}
	
	public void close() {
		// close the json file and write filters
		JsonGenerator generator;
		try {
			generator = jFactory.createJsonGenerator(new FileWriter(jsonFile));
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
			generator.setCodec(mapper);
			generator.useDefaultPrettyPrinter();
			generator.writeTree(rootNode);
			generator.close(); 
	
	
			//now generate the dot file
			File extractDir = new File(docDir, extract);
			File xPathDotFile = new File(extractDir, "xpath.dot"); 
			xPathDotWriter = new DotWriter();
			xPathDotWriter.open(xPathDotFile);
			xPathDotWriter.setOdfFilename(srcsArray.get(0).get("source").getTextValue());
			xPathDotWriter.setIncludeLegend(false);
			xPathDotWriter.writePaths(xPathRoot);
			
			xPathDotWriter.close();
		} catch (IOException e) {
			LOGGER.severe(jsonFile.getName() + " IO Error");
			e.printStackTrace();
		}
	}

	private void filterNodes(XPathNode rootNode, JsonNode rootPaths) {
		// just walk the node get the ids and state
		String id = rootPaths.get("id").asText();

		// is this in the filter list - can we do this with ints instead?
		
		//just try a brute force check for the moment
		boolean notfound = true; //filter not found so we want this node
		Iterator<String> fit = filters.iterator();
		boolean changesOnly = false;
		while(notfound && fit.hasNext()) {
			String f = fit.next(); 
			if( f.equals(id) ) {
				notfound = false;
			}
			//to look for changes only
			// check if filter id is '0'
			// and node state != (CHANGED or NEW or DIFF)
			// set notfound true so we filter out the node 
			if(f.equals("0")) {
				String state = rootPaths.get("state").asText();
				if(state.equals("ORIGINAL") || state.equals("INST") ){
					notfound = false;
				}
				changesOnly = true;
			}
		}
			
		if (notfound) {
//			if (filters.contains(id) == false) {
//			System.out.println("id:" + rootPaths.get("id").asText() + " " + rootPaths.get("state").asText());
			ArrayNode pathsArray = (ArrayNode) rootPaths.get("children");
			if (pathsArray == null) {
				pathsArray = (ArrayNode) rootPaths.get("_children");
			}
			if (pathsArray != null) {
				Iterator<JsonNode> pathsIterator = pathsArray.iterator();
				while (pathsIterator.hasNext()) {
					JsonNode childNode = pathsIterator.next();
					String state = childNode.get("state").asText();
					if (changesOnly == false || (state.equals("CHANGED") || state.equals("NEW") || state.equals("DIFF"))) {
						XPathNode xPathChildNode = new XPathNode(childNode.get("name").asText());
						xPathChildNode.setNodeID(childNode.get("id").asInt());
						if(!asOriginal) {
							xPathChildNode.setState(childNode.get("state").asText());
						}
						JsonNode dd = childNode.get("diffDetails");
						if(dd != null) {
							xPathChildNode.setDiffToolTip(dd.asText());
						}
						// need to get the hits of the previous run
						ArrayNode hitsArray = (ArrayNode) childNode.get("hits");
						if (hitsArray != null && hitsArray.size() > 0) {
							int totalHits = 0;
							for(int h=0; h < hitsArray.size(); h++) {
								totalHits += hitsArray.get(h).asInt();
							}
							xPathChildNode.initHits(totalHits);
						}
						LOGGER.fine("Added " + xPathChildNode.getName() + " " + xPathChildNode.getState() + ":" + xPathChildNode.getHits().size());  
						// need to add in the attribute too!!!
						JsonNode attrs = childNode.get("attributes");
						if (attrs != null) {
							// check it is an array?
							// iterate and get the names and values of the
							// attributes
							// add each to the Xpath node - either as we go or
							// build
							// our own list?
							// System.out.println("atts here");
							// !!!!!
							// we should change the way attributes are stored in
							// the
							// JSON
							// use the properites of the associative array -
							// should
							// not need explicit name value pairs

							// meantime
							if (attrs.isArray()) { // just a cross check
								Iterator<JsonNode> attrsIt = ((ArrayNode) attrs).iterator();
								while (attrsIt.hasNext()) {
									JsonNode attrsNVP = attrsIt.next();
									// xPathChildNode.addAttribute(attr.)
									// System.out.println(attrsNVP.toString());
									Iterator<String> fs = attrsNVP.getFieldNames();
									while (fs.hasNext()) {
										String attrName = fs.next();
										String attrValue = fs.next();
										xPathChildNode.addAttribute(attrsNVP.get(attrName).asText(), attrsNVP.get(attrValue)
												.asText());
									}
								}
							}
						}
						rootNode.addChild(xPathChildNode);
						filterNodes(xPathChildNode, childNode);
					} else {
						System.out.println("Filtered child id:" + rootPaths.get("id").asText() + " " + rootPaths.get("state").asText());
					}
				}
			}
		} else {
			System.out.println("Filtered id:" + rootPaths.get("id").asText() + " " + rootPaths.get("state").asText());
			ObjectNode filterObj = filtersArray.addObject();
			filterObj.put(rootPaths.get("name").asText(), rootPaths.get("id").asText());
		}
		
	}

}
