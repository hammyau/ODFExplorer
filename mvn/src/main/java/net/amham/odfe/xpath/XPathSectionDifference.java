package net.amham.odfe.xpath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.odftoolkit.odfdom.dom.element.text.TextParagraphElementBase;
import org.odftoolkit.odfdom.dom.element.text.TextSectionElement;
import org.odftoolkit.simple.text.Paragraph;
import org.w3c.dom.NodeList;

import net.amham.odfe.difference.DocumentDifference;

public class XPathSectionDifference extends XPathDifference {

	private final static   Logger LOGGER = Logger.getLogger(XPathSectionDifference.class.getName());

	public XPathSectionDifference() {
		type = Type.SECTION; 
	}

	public List <DocumentDifference>  getDocumentDifferences() {
		//look for style nodes
		List <DocumentDifference> retDiffs = new ArrayList<DocumentDifference>();
		if (newNode != null) {
			DocumentDifference diff = new DocumentDifference(DocumentDifference.DiffType.CHANGED);
			String paraStyle = original.getParent().getAttributeValue("style:name");
			diff.setName(paraStyle);
			String detail = null;
			for (DocumentDifference attDiff : original.getAttributeDifferences(newNode)) {
				detail += attDiff.getName() + " : " + attDiff.getDetail();
			}
			diff.setDetail(detail);
			retDiffs.add(diff);
		}
		return retDiffs;
	}

	public String getTarget() {
		String paraStyle = original.getParent().getParent().getParent().getAttributeValue("style:name");
		return "//text:section[@text:style-name='" + paraStyle + "']";
	}
	
	public String commentNodes(NodeList nodeList) {
		String added = null;
	    for (int i = 0; null!=nodeList && i < nodeList.getLength(); i++) {
	    	TextSectionElement section = (TextSectionElement) nodeList.item(i);
	    	NodeList paraNodeList = section.getElementsByTagName("text:p");
	    	if(null!=paraNodeList) {
		    	TextParagraphElementBase para = (TextParagraphElementBase) paraNodeList.item(0);
				Paragraph para1 = Paragraph.getInstanceof(para);
				String detail = "";
				if (newNode != null) {
					for (DocumentDifference attDiff : original.getAttributeDifferences(newNode)) {
						detail += attDiff.getName() + " : " + attDiff.getDetail() + "\n";
					}
				}
				para1.addComment(detail, "ODFExplorer");
				added = "Section Change:\n" + detail;
	    	}
	    }		
	    return added;
	}
	
	public XPathNode findNewNode() {
		XPathNode sibling = null;
		// for section columns the node line up in order
		//so this must be the first original
		//therefore the first NEW should be the corresponding node
		Iterator<List<XPathNode>> sibs = original.getParent().getChildren().iterator();
		boolean notFound = true;
		if (sibs.hasNext()) {
			List<XPathNode> siblings = sibs.next();
			Iterator<XPathNode> xpi = siblings.iterator();
			while (xpi.hasNext() && notFound) {
				sibling = xpi.next();
				if ( sibling.getState() == XPathNode.State.NEW )
				{
					notFound = false;
					LOGGER.info("MATCH column section node");
					StringBuilder atts = new StringBuilder();
					for (String att : original.getAttributeNames()) {
						atts.append(att);
						atts.append(":");
						atts.append(original.getAttributeValue(att));
					}
					LOGGER.info("Orignal " + atts.toString());
					StringBuilder sibatts = new StringBuilder();
					for (String att : sibling.getAttributeNames()) {
						sibatts.append(att);
						sibatts.append(":");
						sibatts.append(sibling.getAttributeValue(att));
					}
					LOGGER.info("Sibling " + sibatts.toString());
					
				}
				else {
					sibling = null;
				}
			}
		}
		if(sibling == null) {
			LOGGER.info("Unable to find a NEW column section node");
		}
		return sibling;
	}

//	protected boolean compareNamedSibling(XPathNode sibling) {
//		LOGGER.info("compareNamedSibling - returning false");
//		
//
//			//is this a match for a renamed auto style?
//			
//			//problem here is we are not sure if these really are different instances
//			//of the same xpath -
//			//need to be node type specific - case just looked at was for sections
///*			LOGGER.info("Remove " + sibling.getName() + " " + sibling.getState() + " "
//					+ diffDetails);
//			notFound = false;
//			newNodeToDelete = sibling;
//			LOGGER.info("diff found for " + state.toString() + " " + nodePath);
//			state = XPathNode.State.DIFF;
//			for (DocumentDifference attDiff : getAttributeDifferences(sibling)) {
//				diffDetails += attDiff.getName() + " : " + attDiff.getDetail() + " ";
//			}
//			xPathDiff.addSibling(sibling);
//		}*/
//		
//		return false;
//
//	}

}
