package net.amham.odfe.json;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import net.amham.odfe.difference.DocumentDifference;
import net.amham.odfe.xpath.XPathNode;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.w3c.dom.Node;

public class XPathJSONWriter {

	private final static   Logger LOGGER = Logger.getLogger(XPathJSONWriter.class.getName());
	private static final String ATTRIBUTES = "attributes";


	private static final String CHILDREN_TAG = "children";
	private static final String HIDDEN_CHILDREN_TAG = "_children";
	protected static final String NAME_TAG = "name";
	protected static final String VALUE_TAG = "value";

	private String mode;

	private JsonGenerator generator;
	private ObjectNode rootNode;
	protected ArrayNode rootArray;
	private int numPaths;
	private int maxDepth;
	private int minDepth;
	private int sumDepth;

	
	public void open(File file) throws XMLStreamException, IOException {
		
        // Mapped convention
		JsonFactory f = new JsonFactory();
		try {
			generator = f.createJsonGenerator(new FileWriter(file));
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
			generator.setCodec(mapper);
			generator.useDefaultPrettyPrinter();

			rootNode = mapper.createObjectNode();
			rootNode.put("name", "odfpaths");
			rootArray = rootNode.putArray(CHILDREN_TAG);
			
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	protected void writeSources(List<String> sourceNames) throws XMLStreamException {
		Integer sourceNumber = 1;
		ObjectNode srcs = rootArray.addObject();
		srcs.put("name", "sources");
		ArrayNode an = srcs.putArray(CHILDREN_TAG);
		for (String src : sourceNames) {
			ObjectNode doc = an.addObject();
			doc.put("source", src);
//			doc.put("run", sd.getRunDate());
			sourceNumber += 1;
		}
	}

	public void writePaths(List<String> sourceNames, XPathNode xPathRoot) throws XMLStreamException {
		numPaths = 0;
		maxDepth = 0;
		minDepth = 99;
		writeSources(sourceNames);
		writePaths(rootArray, xPathRoot, false, 0);
	}	
	
	private void writePaths(ArrayNode jsonArray, XPathNode xPathRoot, boolean hidden, int depth) {
		depth++;
		if (XPathNode.getMode() == XPathNode.Mode.ALL
				|| (xPathRoot.getState() == XPathNode.State.CHANGED || xPathRoot.getState() == XPathNode.State.NEW)
				|| (xPathRoot.getState() == XPathNode.State.DIFF)
				|| (xPathRoot.hasChildren() == false && xPathRoot.getState() == XPathNode.State.ORIGINAL)) {
			ObjectNode xpathObj = jsonArray.addObject();
			xpathObj.put("id", xPathRoot.getNodeID());
			xpathObj.put("name", xPathRoot.getName());
			xpathObj.put("state", xPathRoot.getState().toString());
			if(xPathRoot.getState() == XPathNode.State.DIFF) {
				xpathObj.put("diffDetails", xPathRoot.getDiffDetails());
			}
			addAttributes(xpathObj, xPathRoot);
			addHits(xpathObj, xPathRoot.getHits());

			if (xPathRoot.hasChildren()) {
				ArrayNode childArray;
				if (hidden == true) {
					childArray = xpathObj.putArray(HIDDEN_CHILDREN_TAG);
				} else {
					childArray = xpathObj.putArray(CHILDREN_TAG);
				}

				for (List<XPathNode> childList : xPathRoot.getChildren()) {
					for (XPathNode childNode : childList) {
						writePaths(childArray, childNode, true, depth);
					}
				}
			}
			else {
				if(depth > maxDepth) {
					maxDepth = depth;
				}
				if(depth < minDepth ) {
					minDepth = depth;
				}
				sumDepth += depth;
				
				numPaths++;
			}
		}
	}
	
	private void addAttributes(ObjectNode pathObject, XPathNode xPathNode) {
		if (xPathNode.hasAttributes()) {
			ArrayNode attsArray = pathObject.putArray(ATTRIBUTES);

			for (String attr : xPathNode.getAttributeNames()) {
				ObjectNode attrObj = attsArray.addObject();
				attrObj.put(NAME_TAG, attr);
				attrObj.put(VALUE_TAG, xPathNode.getAttributeValue(attr));
			}
		}
	}

	private void addHits(ObjectNode pathObject, List<Integer> hitList) {
		if (hitList.size() > 0) {
			ArrayNode hitsArray = pathObject.putArray("hits");
			for (Integer hits : hitList) {
				hitsArray.add(hits);
			}
		}
	}

	protected ArrayNode addTreeNode(ArrayNode contentsArray, String name, boolean hidden) {
		ObjectNode xpathObj = contentsArray.addObject();
		xpathObj.put("name", name);
		ArrayNode treeArray;
		if (hidden == true) {
			treeArray = xpathObj.putArray(HIDDEN_CHILDREN_TAG);			
		}
		else {
			treeArray = xpathObj.putArray(CHILDREN_TAG);
		}
		return treeArray;
	}
	
	public void close() throws JsonProcessingException, IOException, XMLStreamException {
		writeRunInfo();
		generator.writeTree(rootNode);
		generator.close(); 
		LOGGER.info("XPath JSON Writer Closed");
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
	
	private void writeRunInfo() throws XMLStreamException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		rootNode.put("run", dateFormat.format(new Date()));
		rootNode.put("mode", mode);
		rootNode.put("numPaths", numPaths);
		rootNode.put("minDepth", minDepth);
		rootNode.put("maxDepth", maxDepth);
		if(numPaths != 0) {
			rootNode.put("avgDepth", sumDepth/numPaths);
		}
		
	}
	
	public int getNumPaths() {
		return numPaths;
	}

	public int getMinDepth() {
		return minDepth;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public int getAvgDepth() {
		if(numPaths != 0) {
			return sumDepth/numPaths;
		} else {
			return 0;
		}
	}
}
