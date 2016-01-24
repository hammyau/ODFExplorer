package net.amham.odfe.xpath;

import org.odftoolkit.odfdom.pkg.OdfElement;

public interface XPathNodeProvider {

	public XPathNode  newXPathNode(OdfElement theElement);
	
}
