package net.amham.odfe.json;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import net.amham.odfe.style.FamilyStyleNode;
import net.amham.odfe.style.OdfeStyleFamily;
import net.amham.odfe.style.StyleFamilyDiffData;
import net.amham.odfe.style.StylesData;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.element.OdfStylePropertiesBase;
import org.odftoolkit.odfdom.dom.style.props.OdfStylePropertiesSet;
import org.odftoolkit.odfdom.incubator.doc.style.OdfDefaultStyle;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class StylesJSONdiffWriter extends StylesJSONWriter {

	private SortedMap<String, StyleFamilyDiffData> reportMap = new TreeMap<String, StyleFamilyDiffData>();
	

	public StylesJSONdiffWriter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void writeStyles() throws XMLStreamException {
		writeSources();
		writeReport();
	}
	
	
	public void writeReport() throws XMLStreamException {
		StylesData sd = styles.elementAt(0);
		
		ArrayNode families = addTreeNode(rootArray, "families", false);
		for (String repKey : reportMap.keySet()) {
			StyleFamilyDiffData report = reportMap.get(repKey);
			OdfeStyleFamily refFamily = report.getReference();

			// Hey we still need to compare defaults
			OdfDefaultStyle defaultStyle = refFamily.getDefaultStyle();

			ArrayNode styleArray = addDiffTreeNode(families, repKey, true, report.getType());
			if (defaultStyle != null) {
				ArrayNode defaultArray = addTreeNode(styleArray, "Default", true);
				writePropertySets(defaultArray, defaultStyle);
			}

			Iterable<FamilyStyleNode> roots = refFamily.getFamilyStyles();
			for (FamilyStyleNode familyStyle : roots) {
				walkFamilyTree(styleArray, familyStyle, sd.getStylesUsed());
			}
		}
	}
	
	@Override
	protected Integer walkFamilyTree(ArrayNode styleArray, FamilyStyleNode familyStyle, SortedMap<String, Integer> stylesUsed)
			throws XMLStreamException {

		OdfStyle style = familyStyle.getStyle();
		ObjectNode familyObj = styleArray.addObject();
		familyObj.put("name", style.getStyleNameAttribute());

//		ArrayNode familyStyleArray = addTreeObject(familyObj, style.getStyleNameAttribute(), true);

		List<Integer> hitList = new ArrayList<Integer>();
		Integer totalHits = 0;
		for (StylesData sd : styles) {
			Integer hits = sd.getStylesUsed().get(style.getStyleNameAttribute());
			if (hits != null) {
				hitList.add(hits);
				totalHits += hits;
			} else {
				hitList.add(0);
			}
		}

		if (totalHits > 0) {
			ArrayNode hitsArray = familyObj.putArray("hits");
			for (Integer hit : hitList) {
				hitsArray.add(hit);
			}
		}
		// get the style properties and iterate them
		// basically should be a copy of what is in the original document
		familyObj.put("diff", familyStyle.getDiffType().toString());
		ArrayNode familyStyleArray = familyObj.putArray(HIDDEN_CHILDREN_TAG);			
		if (familyStyle.getDiffType() == DiffType.ChangedStyle) {
			writeDiffPropertySets(familyStyleArray, style, familyStyle.getDiffStyle());
		} else {
			writePropertySets(familyStyleArray, style);
		}

		// recurse through the trees
		Iterable<FamilyStyleNode> children = familyStyle.getChildren();
		for (FamilyStyleNode child : children) {
			totalHits += walkFamilyTree(familyStyleArray, child, stylesUsed);
		}
		
		return totalHits;
	}

	private Boolean writeDiffPropertySets(ArrayNode styleArray, OdfStyleBase style, OdfStyleBase diffStyle)
			throws XMLStreamException {
		// Need to merge into a diff report

		Boolean diffFound = false;
		for (OdfStylePropertiesSet propSet : OdfStylePropertiesSet.values()) {
			OdfStylePropertiesBase props = style.getOrCreatePropertiesElement(propSet);
			OdfStylePropertiesBase diffProps = diffStyle.getOrCreatePropertiesElement(propSet);

			NamedNodeMap attrs = props.getAttributes();
			NamedNodeMap diffAttrs = diffProps.getAttributes();

			// if we have some attrs - iterate and match diffs

			// issue is that we need to call back to set the props array to Diff
			// is change detected

			// could just flag and change at the end. Or even remove the node if
			// not changed

			int numAttributes = attrs.getLength();
			int numDiffAttributes = diffAttrs.getLength();
			if (numAttributes > 0 || numDiffAttributes > 0) {
				ArrayNode propsArray = addDiffTreeNode(styleArray, propSet.name(), true, DiffType.SameStyle);
				if (numAttributes >= numDiffAttributes) {
					for (int i = 0; i < attrs.getLength(); i++) {
						Node attr = attrs.item(i);
						String attrName = attr.getNodeName();
						String val1 = attr.getNodeValue();

						Node diffNode = diffAttrs.getNamedItem(attrName);
						if (writeAttributeDiffNode(propsArray, attrName, diffNode, val1, false) == true) {
							diffFound = true;
						}
					}

				} else {
					for (int i = 0; i < diffAttrs.getLength(); i++) {
						Node attr = diffAttrs.item(i);
						String attrName = attr.getNodeName();
						String val1 = attr.getNodeValue();

						Node diffNode = attrs.getNamedItem(attrName);
						if (writeAttributeDiffNode(propsArray, attrName, diffNode, val1, true) == true) {
							diffFound = true;
						}
					}
				}

				if (diffFound) {
					//Here we need to make sure the parent nodes are diffed - and it is at the end of the styleArray.
					ObjectNode diffValue = (ObjectNode) styleArray.get(styleArray.size() - 1);
					diffValue.put("diff", DiffType.ChangedStyle.toString());
				}
			}
		}
		
		return diffFound;
	}

	private Boolean writeAttributeDiffNode(ArrayNode propsArray, String attrName, Node diffNode, String val1, Boolean twist) {
		Boolean diffFound = false;
		ObjectNode attrObj = propsArray.addObject();
		attrObj.put(NAME_TAG, attrName);

		if (diffNode == null) {
			diffFound = true;
			writeDiffAttrubute(attrObj, val1, "null", twist);
		} else {
			String val2 = diffNode.getNodeValue();
			if (val1.equals(val2)) {
				attrObj.put(VALUE_TAG, val1);
			} else {
				diffFound = true;
				writeDiffAttrubute(attrObj, val1, val2, twist);
			}
		}
		return diffFound;
	}
	/**
	 * @param styleArray
	 * @param attrObj
	 * @param val1
	 * @param val2
	 */
	public void writeDiffAttrubute(ObjectNode attrObj, String val1, String val2, Boolean twist) {
		if (twist) {
			attrObj.put(VALUE1_TAG, val2);
			attrObj.put(VALUE2_TAG, val1);
		} else {
			attrObj.put(VALUE1_TAG, val1);
			attrObj.put(VALUE2_TAG, val2);
		}
	}

	public SortedMap<String, StyleFamilyDiffData> getReportMap() {
		return reportMap;
	}

	public void setReportMap(SortedMap<String, StyleFamilyDiffData> reportMap) {
		this.reportMap = reportMap;
	}
	
}
