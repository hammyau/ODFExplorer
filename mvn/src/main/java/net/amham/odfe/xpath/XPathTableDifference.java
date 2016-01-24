package net.amham.odfe.xpath;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.amham.odfe.difference.DocumentDifference;

public class XPathTableDifference extends XPathDifference {
	private final static   Logger LOGGER = Logger.getLogger(XPathTableDifference.class.getName());
	
	
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

	protected boolean compareNamedSibling(XPathNode sibling) {
		boolean match = false;
		LOGGER.info("compareNamedSibling");
		//see if the parent style names are the same
		String origParentStyle = original.getParent().getAttributeValue("style:name");
		String siblParentStyle = sibling.getParent().getAttributeValue("style:name");
		
		if(origParentStyle.equals(siblParentStyle))
		{
			LOGGER.info("parent style names match");
			match = true;
		}
		return match;

	}
}
