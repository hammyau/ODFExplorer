package net.amham.odfe.xpath;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.odftoolkit.odfdom.dom.element.text.TextParagraphElementBase;
import org.odftoolkit.simple.text.Paragraph;
import org.w3c.dom.NodeList;

import net.amham.odfe.difference.DocumentDifference;

public class XPathParagraphDifference extends XPathDifference {

	private final static   Logger LOGGER = Logger.getLogger(XPathParagraphDifference.class.getName());
	
	public XPathParagraphDifference() {
		type = Type.PARAGRAPH;
	}

	@Override
	public List<DocumentDifference> getDocumentDifferences() {
		List<DocumentDifference> retDiffs = new ArrayList<DocumentDifference>();
		if (newNode != null) {
			DocumentDifference diff = new DocumentDifference(DocumentDifference.DiffType.CHANGED);
			String paraStyle = original.getParent().getAttributeValue("style:name");
			diff.setName(paraStyle);
			String detail = "";
			for (DocumentDifference attDiff : original.getAttributeDifferences(newNode)) {
				detail += attDiff.getName() + " : " + attDiff.getDetail() + "\n";
			}
			diff.setDetail(detail);
			retDiffs.add(diff);
		}
		return retDiffs;
	}

	@Override
	public String getTarget() {
		String paraStyle = newNode.getParent().getAttributeValue("style:name");
		return "//text:p[@text:style-name='" + paraStyle + "']";
	}

	public String commentNodes(NodeList nodeList) {
		String added = null;
		for (int i = 0; null != nodeList && i < nodeList.getLength(); i++) {
			TextParagraphElementBase para = (TextParagraphElementBase) nodeList.item(i);
			Paragraph para1 = Paragraph.getInstanceof(para);
			String detail = "";
			if(original != null) {
				for (DocumentDifference attDiff : original.getAttributeDifferences(newNode)) {
					detail += "Change:\n" + attDiff.getName() + " : " + attDiff.getDetail() + "\n";
				}
			} else {
				//could switch here?
				if(newNode.getState() == XPathNode.State.NEW)
				{
					detail = "Added:\n" + newNode.getName() + " with\n";
					if(newNode.hasAttributes()) {
						for (String attName : original.getAttributeNames()) {
							detail += attName + " : " + original.getAttributeValue(attName) + "\n";
						}
					}
				}
				else {
					detail = original.getName() + " No attributes changed";
				}
			}
			para1.addComment(detail, "ODFExplorer");
			added = newNode.getParent().getAttributeValue("style:name") + " Paragraph " + detail;
		}
		return added;
	}
	
//	@Override
//	protected   boolean compareNamedSibling(XPathNode sibling) {
//		boolean match = false;
//		String origTextContent = original.getDocNode().getTextContent();
//		if (origTextContent.isEmpty() == false) {
//			if (origTextContent.equals(sibling.getDocNode().getTextContent())) {
//				LOGGER.info("diff found");
//				match = true;
//				String diffDetails= ""; //should be done with stringbuilder
//				for (DocumentDifference attDiff : original.getAttributeDifferences(sibling)) {
//					diffDetails += attDiff.getName() + " : " + attDiff.getDetail() + " ";
//				}
////				addSibling(sibling);
//				LOGGER.info("Remove " + sibling.getName() + " " + sibling.getState() + " "
//						+ diffDetails);
//			}
//		}
//		else {
//			//empty paragraph... how do we handle that?
//		}
//		return match;
//	}

}
