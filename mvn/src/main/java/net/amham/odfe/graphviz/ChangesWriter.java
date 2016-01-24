package net.amham.odfe.graphviz;

import java.util.List;

import net.amham.odfe.xpath.XPathNode;
import net.amham.odfe.xpath.XPathNode.State;

public class ChangesWriter extends DotWriter {

	public ChangesWriter() {
	}

	/**
	 * Walk the XPath tree and generate the dot information
	 * 
	 * 
	 * 
	 * @param jsonArray
	 * @param xPathParent
	 * @param hidden
	 * @param depth
	 */
	private void writePaths(XPathNode xPathParent, int depth, String parentNodeNum) {
		depth++;
		String parentName = null;
		// and want to build up the paths so nodeA => nodeB => nodeC

		
		if (xPathParent.getParent() != null
				&& (xPathParent.getState() == State.CHANGED || xPathParent.getState() == State.NEW)) {
			//use in the placement of the legend?
			Integer depthInt = new Integer(depth);
			
			String ns = xPathParent.getNS().replace("-", "_");
			addNsNode(ns, parentNodeNum, xPathParent.getDotLabel(), xPathParent.getToolTip(), false, false);
			
			System.out.println("Dot from " + xPathParent.getName());
			parentName = parentNodeNum;
		} else {
			parentName = title.replace(".", "_");
		}
		// make this a function of the node not get the information?
		if (xPathParent.hasChildren() && (xPathParent.getState() == State.CHANGED || xPathParent.getState() == State.NEW)) {
			for (List<XPathNode> childList : xPathParent.getChildren()) {
				for (XPathNode childNode : childList) {
					String childName = "DeadEnd";
					if (childNode.isDotable()) {
						Integer depthInt = new Integer(depth + 1);
						String ns = childNode.getNS().replace("-", "_");
						childName = new Integer(childNode.getNodeID()).toString();
						addNsNode(ns, childName, childNode.getDotLabel(), childNode.getToolTip(), false, false);
						String path = parentName + "->" + childName;
						if (paths.contains(path) == false) {
							paths.add(parentName + "->" + childName + childNode.getEdgeLabel());
						}
						writePaths(childNode, depth, childName);
					} else {
						// System.out.println("Dot ignore " +
						// childNode.getName() + " " + childNode.getState());
					}
				}
			}
		} else {
		}
	}

}
