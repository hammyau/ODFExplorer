package net.amham.odfe.style;

import java.util.SortedMap;
import java.util.TreeMap;


import org.odftoolkit.odfdom.dom.element.OdfStylePropertiesBase;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfDefaultStyle;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;

/**
 * Class to map the tree family tree of Styles
 * 
 * 
 * @author ian
 *
 */
public class StyleFamilyTreeBuilder {
	
	private String docName;

    SortedMap <String, OdfeStyleFamily> styleFamiliesMap = new TreeMap <String, OdfeStyleFamily>();

    //keep a collection of the styles which we can then put into tree once we find the top level
    SortedMap <String, OdfStyle> stylesMap = new TreeMap <String, OdfStyle>();
    
    
    public StyleFamilyTreeBuilder(String name) {
    	docName = name;
	}

	/**
	 * We know the named family is in the document styles
	 * 
	 * @param name
	 * @param family
	 */
	public void addFamily(String name, OdfStyleFamily family) {
		OdfeStyleFamily sFamily = styleFamiliesMap.get(name);
		if(sFamily == null) {
			//create our own object to tree the styles
			OdfeStyleFamily styleFamily = new OdfeStyleFamily(name, family);
			styleFamiliesMap.put(name, styleFamily);		
		}
		else {
			//log that we have found the same family again?
			//Need to do this if we are to aggregate or diff
			
		}
	}

	public void addFamilyStyle(String name, OdfStyle familyStyle) {
		// find the right tree
		// get the parent 
		// find the correct level to add it too
		// add it
		OdfeStyleFamily family = styleFamiliesMap.get(name);
		if(family != null) {
			family.addStyle(familyStyle);
		}
	}

	public void closeFamily(String name) {
		OdfeStyleFamily family = styleFamiliesMap.get(name);
		if(family != null) {
			family.placeRemainingOrphans();
			//family.print();
		}
	}

	public boolean addProperties(String name, OdfStyle familyStyle, OdfStylePropertiesBase props) {
		// TODO Auto-generated method stub
		OdfeStyleFamily family = styleFamiliesMap.get(name);
		return family.addProperties(familyStyle, props);
	}

	public void dump(String name, OdfOfficeStyles styles) {
		OdfeStyleFamily family = styleFamiliesMap.get(name);
		if(family != null) {
			family.dump(styles);
		}
	}

	public void setDefault(String familyName, OdfDefaultStyle defaultStyle) {
		OdfeStyleFamily sFamily = styleFamiliesMap.get(familyName);
		if(sFamily == null) {
			//create our own object to tree the styles
			sFamily = new OdfeStyleFamily(familyName, null);
			styleFamiliesMap.put(familyName, sFamily);		
		}
		sFamily.setDefaultStyle(defaultStyle);
	}

	public SortedMap<String, OdfeStyleFamily> getStyleFamiliesMap() {
		return styleFamiliesMap;
	}

}
