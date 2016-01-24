package net.amham.odfe.graphviz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import net.amham.odfe.CommandRunner;
import net.amham.odfe.xpath.XPathNode;
import net.amham.odfe.xpath.XPathNode.State;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class DotWriter {

	private class DotEntry {
		public String name;
		public String label;
		public String tooltip;
		public boolean diff;
		public boolean newed;
	}

	private final static Logger LOGGER = Logger.getLogger(DotWriter.class.getName());

	protected String title = "";
	private FileOutputStream fs;
	private OutputStreamWriter os;

	private SortedMap<String, String> nsColourMap = new TreeMap<String, String>();

	private static final String HEADER = "digraph tree {"
			+ " node [fontname = \"Bitstream Vera Sans\" shape = \"Mrecord\" fontsize = \"8\" ]";
	private static final String CLOSER = "}";

	private SortedMap<String, List<DotEntry>> nsNodeMap = new TreeMap<String, List<DotEntry>>();

	protected Vector<String> paths = new Vector<String>();

	private Integer maxLevel = 0;

	private String deepestNode = "0";

	private File dotFile = null;

	private Boolean includeLegend = true;

	private Integer currentLevel = 0;

	public void open(File file) throws FileNotFoundException {
		dotFile = file;
		fs = new FileOutputStream(file);
		LOGGER.info("Open " + file.getAbsolutePath());
		os = new OutputStreamWriter(fs);

		title = file.getName();

		nsColourMap.put("title", "white");
		nsColourMap.put("text", "pink");
		nsColourMap.put("office", "lightblue");
		nsColourMap.put("table", "beige");
		nsColourMap.put("style", "lightgreen");
		nsColourMap.put("svg", "lightseagreen");
		nsColourMap.put("form", "sandybrown");
		nsColourMap.put("number", "lightgray");
		nsColourMap.put("draw", "palevioletred");
		nsColourMap.put("presentation", "green");
		nsColourMap.put("anim", "gold");
		nsColourMap.put("dc", "lime");
		nsColourMap.put("meta", "deepskyblue");
		nsColourMap.put("chart", "yellowgreen");
	}

	public void writeHeader() throws IOException {
		os.write(HEADER);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String rundate = dateFormat.format(date);
		os.write("labelloc=\"t\"\n" + "label=\"" + title + " " + rundate + "\"\n");
	}

	public void addTwig(String base, String leaf, Integer level) throws IOException {
		if (leaf != null && leaf.length() > 0) {
			String bw = base.replace("-", "_");
			String lw = leaf.replace("-", "_");
			System.out.println(bw + " -> " + lw);
			os.write(bw + " -> " + lw + " [label = \"4\"]\n");

			if (level > maxLevel) {
				maxLevel = level;
				deepestNode = leaf;
			}
		}
	}

	public boolean close() throws IOException {
		boolean worked = true;
		if (includeLegend == true) {
			writeLegend();
		}
		os.write(CLOSER);
		os.close();
		fs.flush();
		fs.close();

		LOGGER.info("Dot writer closed");

		if (dotFile.exists()) {

			// Need to ensure the file is really closed
			// Generate an svg file
			CommandRunner runner = new CommandRunner();
			String command = "dot -Gconcentrate=true -Tsvg -O " + dotFile.getAbsolutePath();
			try {
				// this needs to be done in the extract directory
				runner.run(command, dotFile.getParentFile());
				LOGGER.info("Generated XPath graph");
			} catch (IOException  e) {
				//e.printStackTrace();
				LOGGER.warning("Dot writer failed " + e);
				worked = false;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.warning("Dot writer failed " + e);
				worked = false;
			}
			System.out.println(runner.getCmdOutput());
		} else {
			LOGGER.info("Eeks the file is not there!!! " + dotFile.getAbsolutePath());
		}
		return worked;
	}

	private void writeSubGraphs() throws IOException {

		// write subgraphs
		for (String ns : nsNodeMap.keySet()) {
			String fill = nsColourMap.get(ns) != null ? nsColourMap.get(ns) : nsColourMap.get("title");
			os.write("subgraph \"" + ns + "\" {node[style=\"filled\",fillcolor=\"" + fill  +  "\"];");
			List<DotEntry> nodeList = nsNodeMap.get(ns);
			for (DotEntry node : nodeList) {
				StringBuilder nodeStrB = new StringBuilder();
				nodeStrB.append(node.name);
				nodeStrB.append("[");
				if(node.diff) {
					nodeStrB.append("shape=doubleoctagon color=red ");
				}
				if(node.newed) {
					nodeStrB.append("shape=doubleoctagon color=green ");
				}
				nodeStrB.append("label=\"");
				nodeStrB.append(node.label);
				nodeStrB.append("\" tooltip=\"");
				nodeStrB.append(node.tooltip);
				nodeStrB.append("\" URL=\"javascript:nodeClick('");
				nodeStrB.append(node.label);
				nodeStrB.append("'," );
				nodeStrB.append(node.name);
				nodeStrB.append(")\" ];\n");
				os.write(nodeStrB.toString());
			}
			os.write("}\n");
		}
	}

	/**
	 * Walk the XPath tree and generate the dot information
	 * 
	 * @param xPathRoot
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void writePaths(XPathNode xPathRoot){
		try {
			writeHeader();
			writePaths(xPathRoot, 0, "");
			writeSubGraphs();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//write out the paths
		for (String path : paths) {
			try {
				os.write(path + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param jsonArray
	 * @param xPathParent
	 * @param hidden
	 * @param depth
	 */
	private void writePaths(XPathNode xPathParent, int depth, String parentNodeNum) {
		depth++;
		
		//only the root node has a null parent
		//so use the title (which defaults to the document name)
		String parentName = null;
		if (xPathParent.getParent() != null) {
			if (xPathParent.isDotable()) {
				Integer depthInt = new Integer(depth);
				// node names are just numbers!!
				String ns = xPathParent.getNS();
				if(ns == null) {
					ns = xPathParent.getName().split(":")[0].replace("-", "_"); //strange fixup
				} else {
					ns = ns.replace("-", "_");
				}
				addNsNode(ns, parentNodeNum, xPathParent.getDotLabel(), xPathParent.getToolTip(),false, false);
				parentName = parentNodeNum;
			}
		}
		else {
			//should be able to do this from a calling function?
			//can label without any hassle create title node as title?
			parentName = "title";
			addNsNode("title", "title", title, "", false, false);
		}
		//get the dot nodes and paths we are interested in from here 
		if (xPathParent.hasChildren()) {
			//is this a parent we are interested in 
			if (xPathParent.isDotable()) {
				for (List<XPathNode> childList : xPathParent.getChildren()) {
					for (XPathNode childNode : childList) {
						String childName = "DeadEnd";
						//are we interested in the childNode
						if (childNode.isDotable()) {
							Integer depthInt = new Integer(depth + 1);
							String ns = childNode.getNS();
							if(ns == null) {
								ns = childNode.getName().split(":")[0].replace("-", "_"); //strange fixup
							} else {
								ns = ns.replace("-", "_");
							}
							childName = new Integer(childNode.getNodeID()).toString();
							//remember node if we have not already seen it
							if(childNode.isDiff()) {
								addNsNode(ns, childName, childNode.getDotLabel(), childNode.getToolTip(), true, false);
							} else if(childNode.isNew()) {
								addNsNode(ns, childName, childNode.getDotLabel(), childNode.getToolTip(), true, true);
							} else {
								addNsNode(ns, childName, childNode.getDotLabel(), childNode.getToolTip(), false, false);
							}
							//if we don't know about it remember the path						
							String path = parentName + "->" + childName;
							if (paths.contains(path) == false) {
								paths.add(parentName + "->" + childName + childNode.getEdgeLabel());
							}
							//recurse into the childNode
							writePaths(childNode, depth, childName);
						}
					}
				}
			}
		}
	}

	/**
	 * Build up a map of namespace keyed nodes
	 * to form the subgraphs
	 * the node names are just numbers
	 * 	the label and the tooltip are what is displayed
	 * 
	 * the nodes are written out as dot nodes
	 * 
	 * @param ns
	 * @param name
	 * @param label
	 * @param tooltip
	 * @param b 
	 * @param newd 
	 */
	public void addNsNode(String ns, String name, String label, String tooltip, boolean b, boolean newd) {
		List<DotEntry> nodeList = nsNodeMap.get(ns);
		DotEntry entry = new DotEntry();
		entry.name = name;
		entry.label = label;
		entry.tooltip = tooltip;
		entry.diff = b;
		entry.newed = newd;
		if (nodeList != null) {
			Iterator<DotEntry> it = nodeList.iterator();
			boolean notFound = true;
			while (it.hasNext() && notFound) {
				DotEntry dotty = it.next();
				if (dotty.name.equals(name)) {
					notFound = false;
				}
			}
			if (notFound) {
				nodeList.add(entry);
			}
		} else {
			List<DotEntry> nodes = new ArrayList<DotEntry>();
			nodes.add(entry);
			nsNodeMap.put(ns, nodes);
		}
	}

	private void writeLegend() throws IOException {

		os.write("subgraph \"cluster_legend\" {rank=\"same\"; label=\"Namespaces Legend\";\n");
		for (String key : nsColourMap.keySet()) {
			os.write(key + "L" + "[label=\"" + key + "\",style=\"filled\",fillcolor=\"" + nsColourMap.get(key) + "\"];\n");
		}
		os.write("}\n");

		// Fiddle to force the Legend to the base of the Graph
		os.write("dummy1[style=\"invis\"]\n");
		os.write("dummy2[style=\"invis\"]\n");
		os.write(deepestNode + " -> dummy1[style=\"invis\"]\n" + "dummy1->dummy2[style=\"invis\"]\n");
		os.write("textL -> dummy2[style=\"invis\"]\n");
	}

	public void setMaxLevel(Integer level, String node) {
		if (level > maxLevel) {
			maxLevel = level;
			deepestNode = node.replace("-", "_");
		}
	}

	public Boolean getIncludeLegend() {
		return includeLegend;
	}

	public void setIncludeLegend(Boolean includeLegend) {
		this.includeLegend = includeLegend;
	}

	public void clear() {
		nsNodeMap.clear();
		maxLevel = 0;
		deepestNode = "";
		currentLevel = 0;
	}

	public void setOdfFilename(String name) {
		title = name;
	}
}
