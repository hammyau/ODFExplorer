package net.amham.odfe.xpath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.NodeList;

import net.amham.odfe.difference.DocumentDifference;
import net.amham.odfe.difference.DocumentDifference.DiffType;
import net.amham.odfe.xpath.XPathNode.State;

/**
 * Here we need to know the node name
 * for text:p nodes we only care about style name as a distinguishing attribute
 * 		does this lead us to an XPathNode Factory and many derived types
 * 			probably better that way -although addChild is a kind of factory method
 * 
 * 
 */
public class XPathDifference {
	
	private final static   Logger LOGGER = Logger.getLogger(XPathDifference.class.getName());
	
	protected String expression;
	protected XPathNode original;
	protected XPathNode newNode;
	
	public enum Type {IGNORE,PARAGRAPH, SECTION};
	protected Type type;

	public XPathDifference() {
		type = Type.IGNORE;
	}

	public void setState(State state) {
	}

	public void setExpression(String nodePath) {
		expression = nodePath;
	}

	public void addOriginalNode(XPathNode xPathNode) {
		original = xPathNode;
	}

	public void addNewNode(XPathNode sibling) {
		newNode = sibling;
	}
	
	public String getFullPath() {
		return expression + original.attributePath();
	}
	
	public void dump() {
		System.out.println(newNode.getName() + " " + newNode.getState().toString());
		System.out.println("Base " + expression);
		if(newNode.hasAttributes()) {
			System.out.println("Base Attributes" + newNode.attributePath());
		}
		if(original != null) {
			for( DocumentDifference attDiff : original.getAttributeDifferences(newNode) ) {
				System.out.println(attDiff.getType().toString() + " " + attDiff.getName() + " " + attDiff.getDetail());
			}
		}
	}


	public List<DocumentDifference> getDocumentDifferences() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getTarget() {
		System.out.println("Getting Target from base XPathdifference");
		return null;
	}

	public String commentNodes(NodeList nodeList) {
		return "Uncatagorised change";
		
	}

	/**
	 * find and return a New node for the original
	 * of this difference node
	 * 
	 * Override for node types as needed
	 * 
	 * or node types implement their own comparison function
	 * 
	 * @return
	 */
	public XPathNode findNewNode() {
		XPathNode sibling = null;
		// Iterate the node's siblings and see if there is a NEW
		Iterator<List<XPathNode>> sibs = original.getParent().getChildren().iterator();
		while (sibs.hasNext()) {
			// for (List<XPathNode> siblings :
			// getParent().getChildren()) {
			List<XPathNode> siblings = sibs.next();
			Iterator<XPathNode> xpi = siblings.iterator();
			boolean notFound = true;
			while (xpi.hasNext() && notFound) {
				sibling = xpi.next();
				if ( (sibling.getState() == XPathNode.State.NEW) &&
					 (hasSameName(sibling) == true) )
				{
					notFound = false;
				}
			}
			if(notFound) {
				sibling = null;
			}
		}
		return sibling;
	}

	public XPathNode findSiblingNode() {
		XPathNode sibling = null;
		// Iterate the node's siblings and see if there is a NEW
		if(newNode.getParent().isChanged() )
		{
			Iterator<List<XPathNode>> sibs = newNode.getParent().getChildren().iterator();
			while (sibs.hasNext()) {
				List<XPathNode> siblings = sibs.next();
				Iterator<XPathNode> xpi = siblings.iterator();
				
				boolean notFound = true;
				while (xpi.hasNext() && notFound) {
					sibling = xpi.next();
					if ( (sibling.isOriginal()) &&
						 (hasSameName(sibling) == true) )
					{
						notFound = false;
						original = sibling;
						original.hide();
						newNode.setModified();
					}
				}
				if(notFound) {
					sibling = null;
				}
			}
		}
		return sibling;
	}

	private boolean hasSameName(XPathNode sibling) {
		return newNode.getName().equals(sibling.getName());
	}

//	protected boolean compareNamedSibling(XPathNode sibling) {
//		LOGGER.info("compareNamedSibling - returning false");
//		//We know the sibling has the same name
//		//does it refer to the same node - if it has text we can look in there
//		//	if it's a style node we could look at the style name
//		//	a section column ... need to look for common parent section - a couple of levels above
//		//  if (xPathDiff.same(sibling))
//		//		add and remove... - but positional for section columns and nothing to directly link them
//		//			need to get column and go from there? What if only one column changed? 
//		
//	
//	// what other things do we want?
//	//want a path to affected paragraphs - for style changes
//	
//	//path for size changes - like the section
//	
//	//paths to items added (are they acceptable)
//	//	maybe to build up rules
//	
//	//attribute differences string to add to a comment in content
//	
//	// what kind of changes are we interested in
//	//		style changes
//	//		structural changes - paper size; paragraph layouts - section attributes
//	//		added bold/italics etc - not through styles
//	//		added headers or other features
//	//		missing captions on tables etc - this will require some sort of rule?
//	//			and rules engine?
//		
//		return false;
//	}
//
	public String getDiffDetails(boolean esc ) {
		String detail = "";
		if (newNode.isDiff()) {
			for (DocumentDifference attDiff : original.getAttributeDifferences(newNode)) {
				detail += attDiff.getName() + " : " + attDiff.getDetail() + (esc ? "&#10;" : " ");
			}
		}
		return  detail;
	}
	

}
