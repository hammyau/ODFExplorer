package net.amham.odfe.style;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import net.amham.odfe.json.DiffType;
import net.amham.odfe.style.FamilyStyleNode.DiffValues;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.odftoolkit.odfdom.dom.element.OdfStylePropertiesBase;
import org.odftoolkit.odfdom.dom.style.props.OdfStylePropertiesSet;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class FamilyStyleNode {
	
	public class DiffValues {
		
		private String value1 = null; 
		private String value2 = null;
		
		DiffValues(String v1, String v2) {
			value1 = v1;
			value2 = v2;
		}

		public String getValue1() {
			return value1;
		}

		public String getValue2() {
			return value2;
		}
		
	}

	private final static   Logger LOGGER = Logger.getLogger(FamilyStyleNode.class.getName());
	
	private OdfStyle style = null;
	private OdfStyle diffStyle = null;;
	private DiffType diffType = DiffType.SameStyle;
	
	private SortedMap<String, SortedMap<String, DiffValues>> propertyDiffs = new TreeMap<String, SortedMap<String, DiffValues>>();

	private FamilyStyleNode parent = null;

	//We want to make sure the children are unique
	//so use a sorted Map based on name at the moment... may need a deeper comparison later
	//private List<FamilyStyleNode> children = new ArrayList<FamilyStyleNode>();
	private SortedMap<String, FamilyStyleNode> children = new TreeMap<String, FamilyStyleNode>();

	public FamilyStyleNode(OdfStyle familyStyle) {
		style = familyStyle;
	}

	public void addChildStyle(FamilyStyleNode child) {
		LOGGER.finest("Family:add child " + child.getStyle().getStyleNameAttribute() + " to " + style.getStyleNameAttribute());
			children.put(child.getStyle().getStyleNameAttribute(), child);
	}

	public FamilyStyleNode findParent(OdfStyle familyStyle) {
		if (style.getStyleNameAttribute().equals(familyStyle.getStyleParentStyleNameAttribute())) {
			LOGGER.finest("Direct Find " + style.getStyleNameAttribute() + " for " + familyStyle.getStyleParentStyleNameAttribute());
			return this;
		}
		else {
			for (FamilyStyleNode child : children.values()) {
				FamilyStyleNode parent = child.findParent(familyStyle);
				if (parent != null) {
					this.parent = parent;
					return parent;
				}
			}
		}
		return null;
	}

	public OdfStyle getStyle() {
		return style;
	}

	public OdfStyle getDiffStyle() {
		return diffStyle;
	}

	public int print(int level, int num) {
//		for(int i=0; i<level; i++)
//			System.out.print("    ");
		num++;
		LOGGER.fine(num + ": " + style.getStyleNameAttribute() + " " + diffType);
		
		dumpStyle(level);
		
		for(FamilyStyleNode child : children.values()) {
			num = child.print(level + 1, num);
		}
		
		return num;
	}
	
	public boolean equals(OdfStyle style) {
		return this.style.equals(style);
	}
	
	public boolean nameMatches(String name) {
		return this.style.getOdfName().equals(name);
	}

	public boolean addProperties(OdfStyle familyStyle, OdfStylePropertiesBase props) {
		boolean found = false;
		if (style.equals(familyStyle)) {
			found = true;
		}
		else {
			for (FamilyStyleNode child : children.values()) {
				found = child.addProperties(familyStyle, props);
				if (found) {
					return found;
				}
			}
		}
		
		return found;
	}

	public void dump(OdfOfficeStyles styles) {
		//dumpStyle(styles);
		for (FamilyStyleNode child : children.values()) {
			child.dump(styles);
		}
	}

	private void dumpStyle(int level) {
		String indent = "    ";
		for(int i=0; i<level; i++)
			indent += indent;
		for (OdfStylePropertiesSet propSet : OdfStylePropertiesSet.values()) {
			OdfStylePropertiesBase props = style.getOrCreatePropertiesElement(propSet);

			NamedNodeMap attrs = props.getAttributes();
			int numAttrubutes = attrs.getLength();
			if (numAttrubutes > 0) {
				LOGGER.fine(indent + props.getLocalName());
				for (int i = 0; i < numAttrubutes; i++) {
					Node attr = attrs.item(i);
					LOGGER.fine(indent + "\t" + attr.getNodeName() + " " + attr.getNodeValue());
				}
			}
		}
	}

	public Iterable<FamilyStyleNode>   getChildren() {
		return children.values();	
	}
	
	public Set<String>   getChildNames() {
		return children.keySet();	
	}
	
	public Boolean exists(String styleName) {
		return children.containsKey(styleName);
	}
	
	public Integer getNumChildren() {
		return children.size();
	}
	
	public Boolean compareStyles(FamilyStyleNode trg) {
		Boolean retval = true;
		//What does equvialence mean here
		LOGGER.fine("Compare FamilyNode: " + style.getStyleNameAttribute() 
				+ "=" + trg.getStyle().getStyleNameAttribute());
/*		if(style.compareTo(trg.getStyle()) != 0) {
			retval = false;
			setDifferent(trg);
		} else {
			LOGGER.info(style.getStyleNameAttribute() + " SAME");
		}*/
		
		if(this.propertiesEqual(trg) == false) {
			retval = false;
			LOGGER.finest("Props comparison");
			setDifferent(trg);
		}else {
			LOGGER.fine(style.getStyleNameAttribute() + " SAME");
		}
		
		//Need to iterate through children too
		LOGGER.fine("Compare FamilyNode Children: " + children.size() 
				+ "=" + trg.getNumChildren());
		
		//iterate and compare the children
		//Should do anyway? A E D same business
		for(String  childNode : children.keySet()) {
			FamilyStyleNode refChild = children.get(childNode);
			FamilyStyleNode trgChild = trg.getNode(childNode);
			if(trgChild != null) {
				if(!refChild.compareStyles(trgChild)) {
					retval = false;
					LOGGER.finest("Children comparison");
					setDifferent(trg);
				}
			}
			else {
				diffType = DiffType.MissingStyle;
				retval = false;
				LOGGER.finest("Missing diff");
				setDifferent(trg);
			}
		}
		
		//find new children
		for(String trgName : trg.getChildNames()) {
			if(!children.containsKey(trgName)) {
				FamilyStyleNode newNode = trg.getNode(trgName);
				newNode.setDiffType(DiffType.NewStyle);
				retval = false;
				setDifferent(trg);
				LOGGER.finest("New diff");
				children.put(trgName, newNode);
			}
		}		
		return retval;
	}

	/**
	 * @param trg
	 */
	public void setDifferent(FamilyStyleNode trg) {
		LOGGER.fine(style.getStyleNameAttribute() + " DIFFERENT!");
		diffStyle = trg.getStyle();
		diffType = DiffType.ChangedStyle;
	}

	private FamilyStyleNode getNode(String childNode) {
		return children.get(childNode);
	}

	public void addDiffStyle(OdfStyle trgStyle) {
		diffStyle = trgStyle;
	}

	public DiffType getDiffType() {
		return diffType;
	}

	public void setDiffType(DiffType diffType) {
		this.diffType = diffType;
	}

	public FamilyStyleNode getParent() {
		return parent;
	}

	public void setParent(FamilyStyleNode parent) {
		this.parent = parent;
	}
	
	public List<String> dumpDiff() {
		List<String> strings = new ArrayList<String>();
		
		if(diffType != DiffType.SameStyle) {
			LOGGER.fine("Diff node " + style.getStyleNameAttribute() );
			
			for(String props : propertyDiffs.keySet()) {
				SortedMap<String, DiffValues> attrDiffs = propertyDiffs.get(props);
				for (String attrName : attrDiffs.keySet()) {
					DiffValues diffs = attrDiffs.get(attrName);
					String retval = style.getStyleDisplayNameAttribute() + " " + props + " " + attrName + " " + diffs.getValue1() + " -> " + diffs.getValue2();
					LOGGER.fine(retval);
					strings.add(retval);
				}
			}
		}		
		for (FamilyStyleNode child : children.values()) {
			strings.addAll(child.dumpDiff());
		}
		return strings;
	}

	private Boolean propertiesEqual(FamilyStyleNode trg) {
		Boolean noDiff = true;
		for (OdfStylePropertiesSet propSet : OdfStylePropertiesSet.values()) {
			OdfStylePropertiesBase props = style.getOrCreatePropertiesElement(propSet);
			OdfStylePropertiesBase diffProps = trg.getStyle().getOrCreatePropertiesElement(propSet);

			NamedNodeMap attrs = props.getAttributes();
			NamedNodeMap diffAttrs = diffProps.getAttributes();

			// if we have some attrs - iterate and match diffs
			int numAttributes = attrs.getLength();
			int numDiffAttributes = diffAttrs.getLength();
			if (numAttributes > 0 || numDiffAttributes > 0) {
				if (numAttributes >= numDiffAttributes) {
					for (int i = 0; i < attrs.getLength(); i++) {
						Node attr = attrs.item(i);
						String attrName = attr.getNodeName();
						String val1 = attr.getNodeValue();

						Node diffNode = diffAttrs.getNamedItem(attrName);
						String val2 = "";
						if(diffNode != null) {
							val2 = diffNode.getNodeValue(); 
						}
						
						if(!val1.equals(val2))
						{
							DiffValues dvs = new DiffValues(val1, val2);
							String propsName = propSet.name();
							
							SortedMap<String, DiffValues> attrDiffs = propertyDiffs.get(propsName);
							if(attrDiffs == null) {
								attrDiffs = new TreeMap<String, DiffValues>();
								attrDiffs.put(attrName, dvs);
								propertyDiffs.put(propsName, attrDiffs);
							}
							else{
								attrDiffs.put(attrName, dvs);
							}
							LOGGER.fine("Props " + props + " Attr: " + attrName + " " + val1 + " -> " + val2);
							noDiff = false;
						}
					}
				} else {
					for (int i = 0; i < diffAttrs.getLength(); i++) {
						Node attr = diffAttrs.item(i);
						String attrName = attr.getNodeName();
						String val1 = attr.getNodeValue();

						//Note this is the LHS of the comparison
						Node diffNode = attrs.getNamedItem(attrName);
						String val2 = "";
						if(diffNode != null) {
							val2 = diffNode.getNodeValue();
						}
						
						if(!val1.equals(val2))
						{
							DiffValues dvs = new DiffValues(val2, val1);
							String propsName = propSet.name();
							
							SortedMap<String, DiffValues> attrDiffs = propertyDiffs.get(propsName);
							if(attrDiffs == null) {
								attrDiffs = new TreeMap<String, DiffValues>();
								attrDiffs.put(attrName, dvs);
								propertyDiffs.put(propsName, attrDiffs);
							}
							else{
								attrDiffs.put(attrName, dvs);
							}
							LOGGER.fine("Props " + props + " Attr: " + attrName + " " + val1 + " -> " + val2);
							noDiff = false;
						}
					}
				}
			}
		}
		LOGGER.finest("Equal " + noDiff);

		return noDiff;
	}

	public SortedMap<String, SortedMap<String, DiffValues>> getPropertyDiffs() {
		return propertyDiffs;
	}

}
