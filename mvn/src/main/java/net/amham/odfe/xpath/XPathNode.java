package net.amham.odfe.xpath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import net.amham.odfe.difference.DocumentDifference;

import org.odftoolkit.odfdom.pkg.OdfElement;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Object to capture a section of an XPath name child nodes attributes - list of
 * name value pairs hits - number of times this node was used
 * 
 * Creating this beast so we can compare it with those of another document Then
 * we can generate the XPATH to it
 * 
 * @author ian
 * 
 */
public class XPathNode {

	private final static Logger LOGGER = Logger.getLogger(XPathNode.class.getName());
	private String name;

	static int id = 1; //static counter to generate nodeIDs
	int nodeID;

	public enum State {
		ORIGINAL, NEW, INST, CHANGED, DIFF, TBD, HIDDEN
	};

	public enum Mode {
		ALL, CHANGED_ONLY
	};

	// If we just capture the Element nodes the have all the data we need
	// don't need to copy the info - attributes etc?
	private State state = XPathNode.State.ORIGINAL;
	static private Mode mode = XPathNode.Mode.ALL;
	static private SortedMap<String, XPathDifference> diffPaths = null;

	private String diffDetails = "";

	static private boolean secondDoc = false;

	private SortedMap<String, String> attributes = new TreeMap<String, String>();
	private SortedMap<String, String> diffAttributes = new TreeMap<String, String>();

	private SortedMap<String, List<XPathNode>> children = new TreeMap<String, List<XPathNode>>();


	static private int currentSrcNdx = 0;
	private List<Integer> srcList = new ArrayList<Integer>();

	private XPathNode parent;
	//private Node docNode = null;
	private String diffTooltip;
	private String ns;

	/**
	 * Called for top of tree or when init from JSON
	 * @param name
	 */
	public XPathNode(String name) {
		nodeID = id++;
		ns = name.split(":")[0];
		this.name = name;
		state = XPathNode.State.ORIGINAL;
	}

	/**
	 * Used when walking the tree 
	 * @param theNode
	 */
	private XPathNode(Node theNode) {
		nodeID = id++;
		name = theNode.getNodeName(); //probably don't need this - wrap the get?
		ns = name.split(":")[0];
		if(ns == null) {
			LOGGER.warning("Crosscheck on ns set to null from" + theNode.getNodeName());
		}
		state = XPathNode.State.ORIGINAL;
	}

	public XPathNode addChild(OdfElement theNode) {
		return addChild(theNode, true);
	}

	public XPathNode addChild(OdfElement theNode, boolean addAttributes) {
		XPathNode childNode = null;

		// if we know this is the merge of the second file
		// then we can change its state? INST if know - NEW if not
		// and bubble up from here
		//we already know about this node so it is an INST
	
		// Do we have a childList with this name
		String nodeName = theNode.getNodeName();
		List<XPathNode> childList = children.get(nodeName);
		if (childList == null) {
			childList = new ArrayList<XPathNode>();
			childNode = new XPathNode(theNode);
			// childNode = new XPathNode(theNode);
			if (theNode.hasAttributes() && addAttributes) {
				childNode.addAttributes(theNode.getAttributes());
			}
			addNewChild(childNode, childList);
			children.put(nodeName, childList);
		} else {
			// do we have a childNode with these attributes
			childNode = findChildNode(theNode, childList, addAttributes);
			if (childNode == null) {
				childNode = new XPathNode(theNode);
				if (theNode.hasAttributes() && addAttributes) {
					childNode.addAttributes(theNode.getAttributes());
				}
				addNewChild(childNode, childList);
			}
			else {
				//we already know about this node so it is an INST
				//UNLESS it is a repeat on a NEW node
				if(currentSrcNdx > 0 && childNode.getState() != State.NEW && childNode.getState() != State.CHANGED) {
					childNode.setState(State.INST);
//					System.out.println("Inst node " + childNode.name + " added to " + name);
				}
			}
		}
		return childNode;
	}

	/**
	 * @param childNode
	 * @param childList
	 */
	private void addNewChild(XPathNode childNode, List<XPathNode> childList) {
		childList.add(childNode);
		childNode.setParent(this);
		//order is important here - we want our parent to be Changed
		// extract this as a function
		if(currentSrcNdx > 0) {
			LOGGER.fine("NEW node " + childNode.name + " added to " + name);
			childNode.setState(State.NEW);
			childNode.bubbleChange();
		} else {
			LOGGER.fine("Not NEW node " + childNode.name + " added to " + name);
		}
	}

	protected XPathNode findNode(String name, List<XPathNode> childList) {
		XPathNode returnNode = null;
		Iterator<XPathNode> childIt = childList.iterator();
		while (childIt.hasNext() && returnNode == null) {
			XPathNode child = childIt.next();
			if (child.hasName(name)) {
				returnNode = child;
			}
		}
		return returnNode;
	}

	protected XPathNode findChildNode(Node theNode, List<XPathNode> childList, boolean addAttributes) {
		XPathNode returnNode = null;
		for (XPathNode listNode : childList) {
			if (listNode.hasName(theNode.getNodeName())) {
				// this is the type dependent bit
				if (addAttributes == true) {
					if (listNode.hasAttributes(theNode.getAttributes())) {
						returnNode = listNode;
						break;
					}
				} else {
					returnNode = listNode;
					break;
				}
			}
		}
		return returnNode;
	}

	private boolean hasName(String nodeName) {
		return name.equals(nodeName);
	}

	private boolean hasAttributes(NamedNodeMap attrs) {
		boolean match = true;
		if (attrs.getLength() != attributes.size()) {
			match = false;
			return match; // not the same number -> out of here
		}
		for (int a = 0; a < attrs.getLength(); a++) {
			Node aNode = attrs.item(a);
			String attName = aNode.getNodeName();
			if (attName.equals("xml:id") == false) { // filter these out
				String value = attributes.get(attName);
				if (value != null && value.equals(aNode.getNodeValue()) == false) {
					match = false;
				}
			}
		}
		return match;
	}

	private boolean hasDiffAttributes(NamedNodeMap attrs) {
		boolean match = true;
		if (attrs.getLength() != diffAttributes.size()) {
			match = false;
			return match; // not the same number -> out of here
		}
		for (int a = 0; a < attrs.getLength(); a++) {
			Node aNode = attrs.item(a);
			String attName = aNode.getNodeName();
			if (attName.equals("xml:id") == false) { // filter these out
				String value = diffAttributes.get(attName);
				if (value != null && value.equals(aNode.getNodeValue()) == false) {
					match = false;
				}
			}
		}
		return match;
	}

	private void addAttributes(NamedNodeMap attrs) {
		for (int a = 0; a < attrs.getLength(); a++) {
			Node aNode = attrs.item(a);
			String attName = aNode.getNodeName();
			attributes.put(attName, aNode.getNodeValue());
		}
	}

	private void addDiffAttributes(NamedNodeMap attrs) {
		for (int a = 0; a < attrs.getLength(); a++) {
			Node aNode = attrs.item(a);
			String attName = aNode.getNodeName();
			diffAttributes.put(attName, aNode.getNodeValue());
		}
	}

	public void dump(String path) {
		// if hits > 0 then we must be at an end point
		// collect the path string to this point
		/*
		 * if (mode == XPathNode.Mode.ALL || ((state == XPathNode.State.CHANGED
		 * || state == XPathNode.State.NEW) || (hasChildren() == false && state
		 * == XPathNode.State.ORIGINAL))) {
		 */
		//want all regardless of state?
//		if (state == XPathNode.State.CHANGED || state == XPathNode.State.NEW || state == XPathNode.State.ORIGINAL) {
		{
			String nodePath = path + "/";
			if(name != null) {
				nodePath += name;
			} else {
				nodePath += "NULL named node???";
			}
			
			int numAttrs = attributes.size();
			if (numAttrs > 0) {
				// append a [name = "value" and ... ] string
				// if attributes are numeric not sure how to manage
				// have attributeNode Value base and derive a string version of
				// an integer or a float?
				// string will do for the moment
				nodePath += "[";
				int i = 0;
				for (String attrName : attributes.keySet()) {
					if (attrName.equals("xml:id") == false) { // filter these
																// out
						if (i > 0) {
							nodePath += " and ";
						}
						nodePath += "@" + attrName + " = '" + attributes.get(attrName) + "'";
						i++;
					}
				}
				nodePath += "]";
			}
			if (srcList.size() > 0) {
				//use a string builder here!!!
				LOGGER.fine(state.toString());
				LOGGER.fine(" - hits: ");
				for (Integer hit : srcList) {
					LOGGER.fine(hit + ", ");
				}
				LOGGER.fine(" - " + nodePath);
			}
			else {
				LOGGER.fine(state.toString());
				LOGGER.fine(" - " + nodePath);
			}
			for (List<XPathNode> childList : children.values()) {
				for (XPathNode child : childList) {
					child.dump(nodePath);
				}
			}
		}

	}

	public SortedMap<String, XPathDifference> getXPathDifferences() {
		
		//If we have not already generated the list then do so
		// if we have give it to the caller
		if (diffPaths == null) {
			diffPaths = new TreeMap<String, XPathDifference>();
		

			String nodePath = "/";
			
			if(state == State.CHANGED ) { //if nothing has CHANGED below then nothing to say
				
				//Can't we just follow the CHANGED nodes?
				//And pick up any ORIGINALs along the way? Same as we do when generating the dot file
				
				// What if document matches in all but not instantiating a low level node?
				// there will be no CHANGED at the top node - but that constitutes conforming
				//	we just didn't use everything

				//Using whiles so we can delete stuff if needed
				Iterator<List<XPathNode>> childListIterator = children.values().iterator();
				while (childListIterator.hasNext()) {
					List<XPathNode> childList = childListIterator.next();
					Iterator<XPathNode> childIterator = childList.iterator();
					while (childIterator.hasNext()) {
						XPathNode child = childIterator.next();
						XPathNode nodeToDelete = child.generateDiffNode(diffPaths, nodePath);
						
						//Do we need to delete nodes?
						if (nodeToDelete != null) {
							StringBuilder delatts = new StringBuilder();
							for (String att : nodeToDelete.getAttributeNames()) {
								delatts.append(att);
								delatts.append(":");
								delatts.append(nodeToDelete.getAttributeValue(att));
							}
							LOGGER.info("nodeToDelete " + delatts.toString());
							childList.remove(nodeToDelete);
						}
					}
				}
			}
		}			
		
		return diffPaths;
	}

	/**
	 * @param diffPaths
	 * @param path
	 * @return
	 */
	private XPathNode generateDiffNode(SortedMap<String, XPathDifference> diffPaths, String path) {
		XPathNode newNodeToDelete = null;
		String nodePath = path;
		// if has no children then it is a leaf
		// if ORIGINAL
		// if has NEW sibling -> diff
		// if NEW
		// if has ORIGINAL sibling repeat of above
		// else
		// just new

		// NEW IDEA
		// if this is a changed node keep looking
		// If this not CHANGED and not INST there is something we need to know
		if ((state != State.CHANGED) && (state != State.INST)) {
			nodePath = addToNodepath(nodePath);

			// so what are the possibilities here
			switch (state) {
			case NEW: {
				// we need to know is this a genuine addition
				// or is is a change to an existing node
				// and this is probably only applicable to certain types
				// style definition nodes are always just going to be new
				// and the children will also be new
				// if a style is edited then it properties are the nodes that
				// will have changed
				// if a properties node look back at parent and if CHANGED there
				// should be a matching original
				//
				//Let the Difference object sort that
				// if we have a paragraph and change its named style
				//	it will look like a new para - its original will still be there 
				//  - but only its internal text will tell identify which - but probably don't care
				//		we will still find the paragraph
				XPathDifference xPathDiff = diffFactory();
				xPathDiff.setState(state);
				xPathDiff.setExpression(nodePath);
				xPathDiff.addNewNode(this);
				xPathDiff.findSiblingNode();
				diffTooltip = xPathDiff.getDiffDetails(true);
				diffDetails = xPathDiff.getDiffDetails(true);
				diffPaths.put(nodePath, xPathDiff);
				break;
			}
			case ORIGINAL: {
				// we could be a text:p and have had our style changed
				// so search for a matching NEW text paragraph - sibling match -
				// applies to docnodes in general
				// if no sibling then it was missed... which we are saying is
				// acceptable?
				//
				// else need to get

				// no just simple ignore the node and do not descend below
				break;
			}
			case DIFF: {
				// this we care about - but is anything ever DIFF?
				//
				break;
			}
			default:
				// t'aint supposed to be here
			}
		} else if (state == State.CHANGED) {

			// if this is a CHANGED node go to its children
			Iterator<List<XPathNode>> childListIterator = children.values().iterator();
			while (childListIterator.hasNext()) {
				// for (List<XPathNode> childList : children.values()) {
				List<XPathNode> childList = childListIterator.next();
				Iterator<XPathNode> childIterator = childList.iterator();
				List<XPathNode> killList = new ArrayList<XPathNode>();
				while (childIterator.hasNext()) {
					// for (XPathNode child : childList) {
					XPathNode child = childIterator.next();
					XPathNode nodeToDelete = child.generateDiffNode(diffPaths, nodePath);
					if (nodeToDelete != null) {
						killList.add(nodeToDelete);
					}
				}

				for (XPathNode killMe : killList) {
					StringBuilder delatts = new StringBuilder();
					for (String att : killMe.getAttributeNames()) {
						delatts.append(att);
						delatts.append(":");
						delatts.append(killMe.getAttributeValue(att));
					}
					LOGGER.info("nodeToDelete " + delatts.toString());
					childList.remove(killMe);
				}
			}
		}

		// (hasChildren() == false)) - do we care at the minute?

		// if hits > 1 was at least

		// if it is not ORIGNAL OR has no children then it is a branch we are
		// interested in
		// or better has changed in some way or is a leaf node
		// if ((state != XPathNode.State.ORIGINAL) || (hasChildren() == false))
		// {
		// nodePath = addToNodepath(nodePath);
		//
		// if (hitList.size() > 0) { // leaf node so may be
		// // how can we get the differences?
		// // if NEW and parent also has an ORIGINAL child then changed
		// // and can get changes
		// //
		// // inverse also true - we should always find the ORIGINAL
		// // first?
		//
		// if (state == XPathNode.State.ORIGINAL) {
		// // make a XPathDifference Factory class
		// // XPathDifference xPathDiff = factory.get(this)
		// // looking at this gets the type and any appropriate info
		// // or get it when needed
		//
		// XPathDifference xPathDiff = diffFactory();// new
		// // XPathDifference();
		// xPathDiff.setExpression(nodePath);
		// xPathDiff.setLeafNode(this);
		// diffPaths.put(nodePath, xPathDiff);
		//
		// XPathNode newSibling = xPathDiff.findNewNode();
		// if (newSibling != null) {
		// state = XPathNode.State.DIFF;
		// // we should get rid of it?
		// for (DocumentDifference attDiff :
		// getAttributeDifferences(newSibling)) {
		// diffDetails += attDiff.getName() + " : " + attDiff.getDetail() +
		// "\n";
		// }
		// newNodeToDelete = newSibling;
		// }
		// } else if (state == XPathNode.State.NEW) {
		// LOGGER.info("NEW node " + path + " " + name + " " +
		// docNode.getTextContent());
		//
		// // is there a NEW with no matching ORIGINAL
		// boolean noMatch = true;
		// Iterator<List<XPathNode>> sibs =
		// getParent().getChildren().iterator();
		// while (sibs.hasNext() && noMatch) {
		// List<XPathNode> siblings = sibs.next();
		// Iterator<XPathNode> xpi = siblings.iterator();
		// while (xpi.hasNext() && noMatch) {
		// XPathNode sibling = xpi.next();
		// if (sibling.getState() == XPathNode.State.ORIGINAL) {
		// if (name.equals(sibling.getName()) == true) {
		// LOGGER.info("compare sibling " + sibling.getState() + " " +
		// sibling.getName() + " "
		// + sibling.docNode.getTextContent());
		// if
		// (docNode.getTextContent().equals(sibling.docNode.getTextContent())) {
		// LOGGER.info("diff found for " + state.toString() + " " + nodePath);
		// if (diffPaths.containsKey(nodePath) == false) {
		// XPathDifference xPathDiff = diffFactory();
		// xPathDiff.setState(state);
		// xPathDiff.setExpression(nodePath);
		// xPathDiff.setLeafNode(this);
		// state = XPathNode.State.DIFF;
		// diffPaths.put(nodePath, xPathDiff);
		// noMatch = false;
		// LOGGER.info("New matched " + nodePath);
		// } else {
		// LOGGER.info("Ignore existing path for " + state.toString() + " " +
		// nodePath
		// + " original: " + sibling.getName());
		// }
		// }
		// } else {
		// LOGGER.info("Ignore names do not match " + state.toString() + " " +
		// nodePath
		// + " original: " + sibling.getName());
		// }
		// }
		// }
		// }
		// if (noMatch) { // this is a new node
		// LOGGER.info("This is genuinely NEW");
		// XPathDifference xPathDiff = diffFactory();
		// xPathDiff.setState(state);
		// xPathDiff.setExpression(nodePath);
		// xPathDiff.setLeafNode(this);
		// diffPaths.put(nodePath, xPathDiff);
		// }
		// }
		// } else {
		// if (state != XPathNode.State.CHANGED) {
		// LOGGER.warning("Hit list 0 length" + state.toString() + " " +
		// nodePath);
		// }
		// }
		//
		// Iterator<List<XPathNode>> childListIterator =
		// children.values().iterator();
		// while (childListIterator.hasNext()) {
		// // for (List<XPathNode> childList : children.values()) {
		// List<XPathNode> childList = childListIterator.next();
		// Iterator<XPathNode> childIterator = childList.iterator();
		// List<XPathNode> killList = new ArrayList<XPathNode>();
		// while (childIterator.hasNext()) {
		// // for (XPathNode child : childList) {
		// XPathNode child = childIterator.next();
		// XPathNode nodeToDelete = child.generateDiffNode(diffPaths, nodePath);
		// if (nodeToDelete != null) {
		// killList.add(nodeToDelete);
		// }
		// }
		//
		//
		// for (XPathNode killMe : killList) {
		// StringBuilder delatts = new StringBuilder();
		// for (String att : killMe.getAttributeNames()) {
		// delatts.append(att);
		// delatts.append(":");
		// delatts.append(killMe.getAttributeValue(att));
		// }
		// LOGGER.info("nodeToDelete " + delatts.toString());
		// childList.remove(killMe);
		// }
		// }
		// }
		return newNodeToDelete;

	}

	/**
	 * based on the leaf node we can create different the correct type of
	 * XPathDifference Paragraph Section
	 * 
	 * @param xPathNode
	 * @return
	 */
	private XPathDifference diffFactory() {

		XPathDifference retDiff = null;

		if (name.equals("style:text-properties") || name.equals("style:paragraph-properties")) {
			// this could be an auto style

			retDiff = new XPathParagraphDifference();

		} else if (name.equals("style:style")) {
			//needs its own type - so can get the target correctly
			//and manage its own comment
			retDiff = new XPathStyleDifference();
			
		} else if (name.equals("style:table-properties")) {
			// this could be an auto style

			retDiff = new XPathTableDifference();

		} else if (name.equals("style:column")) {
			retDiff = new XPathSectionDifference();
		} else {
			retDiff = new XPathDifference();
		}

		return retDiff;
	}

	/**
	 * @param nodePath
	 * @return
	 */
	public String addToNodepath(String nodePath) {
		nodePath += "/" + name;
		int numAttrs = attributes.size();
		if (srcList.size() == 0 && numAttrs > 0) {
			// append a [name = "value" and ... ] string
			// if attributes are numeric not sure how to manage
			// have attributeNode Value base and derive a string version of
			// an integer or a float?
			// string will do for the moment
			nodePath += "[";
			int i = 0;
			for (String attrName : attributes.keySet()) {
				if (attrName.equals("xml:id") == false) { // filter these
															// out
					if (i > 0) {
						nodePath += " and ";
					}
					nodePath += "@" + attrName + " = '" + attributes.get(attrName) + "'";
					i++;
				}
			}
			nodePath += "]";
		}
		return nodePath;
	}

	// if we set a node to missed as they are generated
	// then cascade changes as needed.
	// if parent already set stop
	// order of precedence missed -> inst -> added : enum
	public void addSourceNdx(int srcNdx) {
		Integer val = null;

		if (srcList.size() == srcNdx + 1) {
			val = srcList.get(srcNdx);
			val++;
			srcList.set(srcNdx, val);
		} else { // iterate to work for a hit on new entry
			int listndx = srcList.size();
			while (listndx < srcNdx + 1) {
				val = new Integer(0);
				srcList.add(listndx++, val);
			}
			val++;
			srcList.set(srcNdx, val);
		}
	}

	private void bubbleChange() {
		XPathNode parentNode = parent;
		boolean notDone = true;
		while (parentNode != null && notDone) {
			if (parentNode.getState() != XPathNode.State.CHANGED && parentNode.getState() != XPathNode.State.NEW) {
				parentNode.setState(XPathNode.State.CHANGED);
				LOGGER.fine("CHANGED " + parentNode.name + " from " + name);
				parentNode = parentNode.getParent();
			}
			else{
				notDone = false;;
				LOGGER.fine("NOT CHANGED " + parentNode.name + " " + parentNode.getState() + " from " + name);
			}
		}
	}

	private void setState(State changed) {
		this.state = changed;
	}

	public void setState(String stateName) {
		if(stateName.equals("ORIGINAL")) {
			state = XPathNode.State.ORIGINAL;
		}
		else if(stateName.equals("DIFF")) {
			state = XPathNode.State.DIFF;
		}
		else if(stateName.equals("NEW")) {
			state = XPathNode.State.NEW;
		}
		else if(stateName.equals("CHANGED")) {
			state = XPathNode.State.CHANGED;
		}
		else if(stateName.equals("INST")) {
			state = XPathNode.State.INST;
		}
		else if(stateName.equals("HIDDEN")) {
			state = XPathNode.State.HIDDEN;
		} else {
			LOGGER.info("Node at default state from " + stateName);
		}
	}

	public State getState() {
		return state;
	}

	public XPathNode getParent() {
		return parent;
	}

	public void setParent(XPathNode parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public List<Integer> getHits() {
		return srcList;
	}

	public Iterable<List<XPathNode>> getChildren() {
		return children.values();
	}

	public String getAttributeValue(String name) {
		return attributes.get(name);
	}

	public Iterable<String> getAttributeNames() {
		return attributes.keySet();
	}

	private SortedMap<String, String> getAttributesMap() {
		return attributes;
	}

	public boolean hasAttributes() {
		return (attributes.size() > 0);
	}

	public boolean hasChildren() {
		return (children.size() > 0);
	}

	public static Mode getMode() {
		return mode;
	}

	public static void setMode(Mode mode) {
		XPathNode.mode = mode;
	}

	public String attributePath() {
		String retval = "";
		int numAttrs = attributes.size();
		if (numAttrs > 0) {
			// append a [name = "value" and ... ] string
			// if attributes are numeric not sure how to manage
			// have attributeNode Value base and derive a string version of
			// an integer or a float?
			// string will do for the moment
			retval = "[";
			int i = 0;
			for (String attrName : attributes.keySet()) {
				if (attrName.equals("xml:id") == false) { // filter these
															// out
					if (i > 0) {
						retval += " and ";
					}
					retval += "@" + attrName + " = '" + attributes.get(attrName) + "'";
					i++;
				}
			}
			retval += "]";
		}
		return retval;
	}

	public String attributesTooltip() {
		String retval = "";
		int numAttrs = attributes.size();
		if (numAttrs > 0) {
			// append a [name = "value" and ... ] string
			// if attributes are numeric not sure how to manage
			// have attributeNode Value base and derive a string version of
			// an integer or a float?
			// string will do for the moment
			int i = 0;
			for (String attrName : attributes.keySet()) {
				if (attrName.equals("xml:id") == false) { // filter these
					retval += attrName + " = " + attributes.get(attrName) + "&#10;";
					i++;
				}
			}
		}
		return retval;
	}

	public List<DocumentDifference> getAttributeDifferences(XPathNode newNode) {
		List<DocumentDifference> differenceList = new ArrayList<DocumentDifference>();

		SortedMap<String, String> newAttributes = newNode.getAttributesMap();

		// if we have some attrs - iterate and match diffs

		// issue is that we need to call back to set the props array to Diff
		// is change detected

		// could just flag and change at the end. Or even remove the node if
		// not changed

		int numAttributes = attributes.size();
		int numNewAttributes = newAttributes.size();
		if (numAttributes > 0 || numNewAttributes > 0) {
			if (numAttributes >= numNewAttributes) {
				for (String name : attributes.keySet()) {
					String newValue = newAttributes.get(name);
					String value = attributes.get(name);
					if (newValue != null) {
						if (value.equals(newValue) == false) {
							// we have a difference
							DocumentDifference attDiff = new DocumentDifference(DocumentDifference.DiffType.CHANGED);
							attDiff.setName(name);
							attDiff.setDetail(value + " -> " + newValue);
							differenceList.add(attDiff);
						}
					} else {
						// deleted attribute - this would make them different
						// nodes?
						DocumentDifference attDiff = new DocumentDifference(DocumentDifference.DiffType.MISSING);
						attDiff.setName(name);
						attDiff.setDetail(value);
						differenceList.add(attDiff);
					}
				}
			} else {
				for (String name : newAttributes.keySet()) {
					String value = attributes.get(name);
					String newValue = newAttributes.get(name);
					if (value != null) {
						if (value.equals(newValue) == false) {
							// we have a difference
							DocumentDifference attDiff = new DocumentDifference(DocumentDifference.DiffType.CHANGED);
							attDiff.setName(name);
							attDiff.setDetail(value + " -> " + newValue);
							differenceList.add(attDiff);
						}
					} else {
						// added attribute
						DocumentDifference attDiff = new DocumentDifference(DocumentDifference.DiffType.ADDED);
						attDiff.setName(name);
						attDiff.setDetail(newValue);
						differenceList.add(attDiff);
					}
				}
			}
		}
		return differenceList;
	}

	public String getDiffDetails() {
		return diffDetails;
	}

	public void setDiffDetails(String val) {
		diffDetails = val;
	}

	// to avoid the ifs here we could/should create derived classes - or use instance and look for styleable elements?
	public String getDotLabel() {
		String label = name.replace("-", "_");
		if (name.equals("text:p")) {
			String style = getAttributeValue("text:style-name");
			if (style != null) {
				style = style.replace("-", "_");
			}
			label = label + "(" + style + ")";
		} else if (name.equals("text:h")) {
			String style = getAttributeValue("text:style-name");
			if (style != null) {
				style = style.replace("-", "_");
			}
			label = label + "(" + style + ")";
		} else if (name.equals("text:span")) {
			String style = getAttributeValue("text:style-name");
			if (style != null) {
				style = style.replace("-", "_");
			}
			label = label + "(" + style + ")";
		} else if (name.equals("style:style")) {
			String style = getAttributeValue("style:name");
			if (style != null) {
				style = style.replace("-", "_");
			}
			label = label + "(" + style + ")";
		}
		return label;
	}

	public String getToolTip() {
		// If the node is a diff want the tooltip to say how
		String ttip = "";
		if(state == State.DIFF) {
			ttip = diffTooltip;
		}
		else {
			ttip = attributesTooltip();
		}
		
		return ttip;
	}

	public void setDiffToolTip(String val) {
		diffTooltip = val;
	}
	// TODO State based classes to avoid the switch
	
	// O do not need a count
	// N should only have a single count - how many news
	// D - the tool tip should indicate what the attribute differences were
	// I won't show up
	// C ?? nothing - is worked out - probably don't need label at all
	public String getEdgeLabel() {
		String label = "";
		switch (state) {
		case NEW:
			if (srcList.size() == 2) {
				label = " [label=\" N: " + srcList.get(1).toString() + "\"]";
			} else {
				label = " [label=\"N" + "\"]";
			}
			break;
		case ORIGINAL: {
			if (srcList.size() > 0) {
				label = " [label=\"" + srcList.get(0).toString() + "\"]";
		} else {
			label = " [label=\"O" + "\"]";
		}
		}
			break;
		case INST: {
			Integer sum = 0;
			for(Integer val : srcList) {
				sum += val;
			}
			if(sum > 0) {
				label = " [label=\" I: " + sum.toString() + "\"]";
			}
		}
			break;
		case DIFF: {
			if (srcList.size() == 1) {
				label = " [label=\" D: " + srcList.get(0).toString() + "\"]";
			} else if (srcList.size() == 2) {
				if( srcList.get(0) > 0) {
					label = " [label=\" D: " + srcList.get(0).toString() + " ->  " + srcList.get(1).toString() + "\"]";
				} else {
					label = " [label=\" D: " + srcList.get(1).toString() + "\"]";
				}
			}
		}
			break;
		case CHANGED: {
			if (srcList.size() == 1) {
				label = " [label=\" C: " + srcList.get(0).toString() + "\"]";
			} else if (srcList.size() == 2) {
				label = " [label=\" C: " + srcList.get(0).toString() + " ->  " + srcList.get(1).toString() + "\"]";
			} else {
				label = " [label=\"C" + "\"]";
			}
		}
			break;
		default:
			label = " [label=\"X:" + name + "-" + getState().toString() + "\"]";
		}

		return label;
	}

	public boolean isDotable() {
		return ((mode == XPathNode.Mode.ALL && state != State.HIDDEN) || (state == State.CHANGED || state == State.NEW || state == State.DIFF));
	}

	public boolean isChanged() {
		return (state == State.CHANGED);
	}

	public boolean isOriginal() {
		return (state == State.ORIGINAL);
	}

	public void setModified() {
		state = State.DIFF;		
	}

	public boolean isDiff() {
		return (state == State.DIFF);
	}

	public void hide() {
		state = State.HIDDEN;		
	}
	
	public int getNodeID() {
		return nodeID;
	}

	public void setNodeID(int id) {
		nodeID = id;
	}

	/**
	 * JSON created nodes - so don't really need to do much checking
	 * 
	 * @param xPathChildNode
	 */
	public void addChild(XPathNode xPathChildNode) {
		// Do we have a childList with this name
		String nodeName = xPathChildNode.getName();
		List<XPathNode> childList = children.get(nodeName);
		if (childList == null) {
			childList = new ArrayList<XPathNode>();
			// childNode = new XPathNode(theNode);
			addNewChild(xPathChildNode, childList);
			children.put(nodeName, childList);
		} else {
			addNewChild(xPathChildNode, childList);
		}
	}
	
	public String getNS() {
		return ns;
	}
	
	//When creating from JSON
	public void addAttribute(String name, String value) {
		attributes.put(name, value);
	}

	public boolean isNew() {
		return (state == State.NEW);

	}

	public void setSrcNdx(int val) {
		currentSrcNdx = val;
	}

	public void clearHitNdx() {
		currentSrcNdx = -1;		// do we need this? and -1 will cause issues
	}

	public void initHits(int totalHits) {
		Integer val = new Integer(totalHits);
		srcList.add(0, val);
	}

}
