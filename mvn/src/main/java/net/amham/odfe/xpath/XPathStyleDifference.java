package net.amham.odfe.xpath;

import java.util.ArrayList;
import java.util.List;

import net.amham.odfe.difference.DocumentDifference;

import org.odftoolkit.odfdom.dom.element.text.TextParagraphElementBase;
import org.odftoolkit.simple.text.Paragraph;
import org.w3c.dom.NodeList;

/**
 * This can only be a NEW node?
 * 
 * @author ian
 *
 */
public class XPathStyleDifference extends XPathDifference {

	public XPathStyleDifference() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getTarget() {
		String paraStyle = newNode.getAttributeValue("style:name");
		return "//text:p[@text:style-name='" + paraStyle + "']";
	}

	@Override
	public String commentNodes(NodeList nodeList) {
		String added = null;
		for (int i = 0; null != nodeList && i < nodeList.getLength(); i++) {
			TextParagraphElementBase para = (TextParagraphElementBase) nodeList.item(i);
			Paragraph para1 = Paragraph.getInstanceof(para);
			added = "Added Style: " + newNode.getAttributeValue("style:name") + "\n";
			added += "Derived from " + newNode.getAttributeValue("style:parent-style-name") + "\n";
			para1.addComment(added , "ODFExplorer");
		}
		return added;
	}
	
}
