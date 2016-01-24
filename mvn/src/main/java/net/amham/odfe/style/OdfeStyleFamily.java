package net.amham.odfe.style;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.amham.odfe.json.DiffType;

import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.element.OdfStylePropertiesBase;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.dom.style.props.OdfStylePropertiesSet;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfDefaultStyle;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class OdfeStyleFamily {

	private final static   Logger LOGGER = Logger.getLogger(OdfeStyleFamily.class.getName());
	private String name = null; 
	private Integer number = 0;
	
	private DiffType type = DiffType.SameStyle;
	
	private OdfDefaultStyle defaultStyle = null;
	
	/**
	 * There can be many top level nodes... just to make life interesting
	 */
	private SortedMap<String, FamilyStyleNode> roots = new TreeMap<String, FamilyStyleNode>();
	
	private List<OdfStyle> orphans = new ArrayList<OdfStyle>();
	
	public OdfeStyleFamily(String name, OdfStyleFamily family) {
		this.name = name; 
	}

	public void addStyle(OdfStyle familyStyle) {

		String parent = familyStyle.getStyleParentStyleNameAttribute();
		number++;
		
		LOGGER.finest(number.toString() + ":Add Style " + familyStyle.getStyleNameAttribute()+ " from " + parent);
		
		if (parent == null) {
			// this should be the root of the tree
			// but is the current root its child?
			// we need to keep the fragments here?
			if (!roots.containsKey(familyStyle.getStyleNameAttribute())) {
				FamilyStyleNode root = new FamilyStyleNode(familyStyle);
				roots.put(familyStyle.getStyleNameAttribute(), root);
				// iterate the orphans and add here.
				LOGGER.finest("Root!");
				List<OdfStyle> deleteThese = new ArrayList<OdfStyle>();
				for (OdfStyle orphan : orphans) {
					FamilyStyleNode parentNode = root.findParent(orphan);
					if (parentNode != null && parentNode == root) {
						LOGGER.finest("Found Orphan " + orphan.getStyleNameAttribute());
						root.addChildStyle(new FamilyStyleNode(orphan));
						deleteThese.add(orphan);
					}
				}
				for (OdfStyle del : deleteThese) {
					orphans.remove(del);
				}
			}

			// Eeks what about the old tree?

			// we could get the first style and keep getting its parent until
			// null?
			// familyStyle.getParentStyle(); // some strangeness under here...
		} else {
			FamilyStyleNode parentNode = null;
			if (roots.size() > 0) {
				for(FamilyStyleNode root : roots.values()) {
					parentNode = root.findParent(familyStyle);
					if (parentNode != null) {
						break; //don't like doing this
					}
				}
			}
			// do we have the parent in the tree?
			if (parentNode != null) {
				// so this node is a child - if not already there
				if(!parentNode.exists(familyStyle.getStyleNameAttribute()))
				{
					parentNode.addChildStyle(new FamilyStyleNode(familyStyle));
					LOGGER.finest("Added to Parent");
				}
			} else {
				// what now?
				// keep a list of orphans
				orphans.add(familyStyle);
				LOGGER.finest("Added to Orphans");
			}
		}
	}
	
	public Boolean placeRemainingOrphans() {
		Boolean retval = true;
		LOGGER.finest("Starting orphans:" + orphans.size());
		List<OdfStyle> deleteThese = new ArrayList<OdfStyle>();
		//repeat this until we the number of orphans does not reduce or is null
		boolean removedSome = true;
		while (orphans.size() > 0 && removedSome) {
			for (OdfStyle orphan : orphans) {
				for (FamilyStyleNode root : roots.values()) {
					FamilyStyleNode parentNode = root.findParent(orphan);
					if (parentNode != null) {
						if(!parentNode.exists(orphan.getStyleNameAttribute()))
						{
							parentNode.addChildStyle(new FamilyStyleNode(orphan));
							LOGGER.finest("Found parent  " +  parentNode.getStyle().getStyleNameAttribute() + " for " + orphan.getStyleNameAttribute());
						}
						deleteThese.add(orphan);
					}
				}
			}

			if (deleteThese.size() == 0) {
				removedSome = false;
			}

			for (OdfStyle del : deleteThese) {
				orphans.remove(del);
			}
			LOGGER.finest("Remaining orphans:" + orphans.size());
		}
		return retval;
	}
	
	public void print() {
		int num = 0;
		
		dumpDefault();
		
		for (FamilyStyleNode root : roots.values()) {
			num = root.print(0, num);
		}
	}
	
	private void dumpDefault() {
		if (defaultStyle != null) {
			LOGGER.fine("\n\nDefault Style: " + defaultStyle.getFamilyName());
			OdfStyleBase parentFamily = defaultStyle.getParentStyle();
			if (parentFamily != null)
				LOGGER.fine(" parent " + defaultStyle.getParentStyle().getFamilyName());

			for (OdfStylePropertiesSet propSet : OdfStylePropertiesSet.values()) {
				OdfStylePropertiesBase props = defaultStyle.getOrCreatePropertiesElement(propSet);

				NamedNodeMap attrs = props.getAttributes();
				int numAttrubutes = attrs.getLength();
				if (numAttrubutes > 0) {
					LOGGER.fine(props.getLocalName());
					for (int i = 0; i < numAttrubutes; i++) {
						Node attr = attrs.item(i);
						LOGGER.fine(attr.getNodeName() + " " + attr.getNodeValue());
					}
				}
			}
		}
	}

	public boolean addProperties(OdfStyle familyStyle, OdfStylePropertiesBase props) {
		//find the familyStyle
		boolean found = false;
		for (FamilyStyleNode root : roots.values()) {
			found = root.addProperties(familyStyle, props);
			if (found) {
				break;
			}
		}
		
		return found;
	}

	public void dump(OdfOfficeStyles styles) {
		int num = 0;
		dumpDefault();
		
		for (FamilyStyleNode root : roots.values()) {
			num = root.print(0, num);
		}
/*		dumpDefault();
		for (FamilyStyleNode root : roots) {
			root.dump(styles);
		}*/
	}

	public OdfDefaultStyle getDefaultStyle() {
		return defaultStyle;
	}

	public void setDefaultStyle(OdfDefaultStyle defaultStyle) {
		this.defaultStyle = defaultStyle;
	}

	public Iterable<FamilyStyleNode>  getFamilyStyles() {
		return roots.values();
	}
	
	public Iterable<String>  getFamilyStyleNames() {
		return roots.keySet();
	}
	
	public String getName() {
		return name;
	}

	public Integer getNumStyles() {
		return roots.size();
	}

	public Boolean equals(OdfeStyleFamily trg) {
		Boolean retval = false;
		//what is equal?
		LOGGER.fine("Compare Names: " + name + "=" + trg.getName());
		if (name.equals(trg.getName())) {
			LOGGER.fine("Compare Rootsize: " + roots.size() + "=" + trg.getNumStyles());
			if(roots.size() == trg.getNumStyles()) {
				Iterator<String> styles = roots.keySet().iterator();
				while(styles.hasNext()) {
					String style = styles.next();
					FamilyStyleNode refNode = roots.get(style);
					FamilyStyleNode trgNode = trg.find(style);
					if(refNode.equals(trgNode)) {
						retval = true;
					}
				}
			}
		}
		return retval;
	}

	public Boolean compareTo(OdfeStyleFamily trgFam) {
		Boolean retval = true;
		//what is equal?
		//this must already be equal ?
		LOGGER.fine("Compare Names: " + name + "=" + trgFam.getName());
		if (name.equals(trgFam.getName())) {
			//want to do a AEDS check
		
			Integer numRefStyles = getNumStyles();
			Integer numTrgStyles = trgFam.getNumStyles();
			LOGGER.fine("Compare Number of styles: " + numRefStyles + "=" + numTrgStyles);
			
			Iterator<String> refFamilyNameIt = roots.keySet().iterator();
			//look for missing - changed or same
			while(refFamilyNameIt.hasNext()) {
				String refName = refFamilyNameIt.next();
				FamilyStyleNode refNode = roots.get(refName);				
				FamilyStyleNode trgNode = trgFam.find(refName);
				
				if (trgNode != null) {
					if (refNode.compareStyles(trgNode) == true) {
						// same node nothing to do
						refNode.setDiffType(DiffType.SameStyle);
					} else {
						// something different
						// we need a handle on this target node
						// can't put it in this map with same name
						// but can copy its style to this node and mark it as diff
						refNode.addDiffStyle(trgNode.getStyle());
						refNode.setDiffType(DiffType.ChangedStyle);
						retval = false;
					}
				}
				else {
					//this one is missing
					refNode.setDiffType(DiffType.MissingStyle);
				}
			}
			
			//Need to find new Nodes
			//iterate the target names
			//any not found locally are new - could tick then off in the trgFamily?
			//this needs to be recursive too?
			for(String style : trgFam.getFamilyStyleNames()) {
				if(roots.get(style) == null) {
					FamilyStyleNode newNode = trgFam.find(style);
					newNode.setDiffType(DiffType.NewStyle);
					roots.put(style, newNode);
				}
			}
			
		}
		
		return retval;
	}
	
	public FamilyStyleNode find(String style) {
		return roots.get(style);
		
	}

	public DiffType getType() {
		return type;
	}

	public void setType(DiffType type) {
		this.type = type;
	}
	
	public List<String> getDifferenceNodes() {
		List<String> returnStrings = new ArrayList<String>();;
		for (FamilyStyleNode node : roots.values()) {
			returnStrings.addAll(node.dumpDiff());
		}
		return returnStrings;
	}

}
