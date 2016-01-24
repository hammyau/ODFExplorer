package net.amham.odfe.xpath;

import java.util.HashMap;
import java.util.Map;

import org.odftoolkit.odfdom.dom.element.style.StyleStyleElement;
import org.odftoolkit.odfdom.pkg.OdfElement;

public final class XPathNodes {
	
	
	private static final Map<Class, XPathNodeProvider> providers =
			new HashMap<Class, XPathNodeProvider>();
	
	XPathNodes() {
		providers.put(StyleStyleElement.class, new XPathStyleNodeProvider());
	}
	
	public static XPathNode  newNode(OdfElement theElement) {
		
		return null;
	}

}
