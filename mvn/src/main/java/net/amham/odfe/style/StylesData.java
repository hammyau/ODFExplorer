package net.amham.odfe.style;

import java.util.Collection;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;


import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.odftoolkit.odfdom.dom.element.number.NumberBooleanStyleElement;
import org.odftoolkit.odfdom.dom.element.number.NumberTextStyleElement;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberCurrencyStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberDateStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberPercentageStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberTimeStyle;
import org.odftoolkit.odfdom.incubator.doc.style.OdfDefaultStyle;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextListStyle;

public class StylesData {

	private String documentName;
	private SortedMap<String, OdfeStyleFamily> familyTree;
	private SortedMap<String, Integer> stylesUsed;
	private String rundate;

	private SortedMap<String, OdfTextListStyle> mListStyles = new TreeMap<String, OdfTextListStyle>();;
	private SortedMap<String, OdfNumberStyle> mNumberStyles;
	private SortedMap<String, OdfNumberDateStyle> mDateStyles;
	private SortedMap<String, OdfNumberPercentageStyle> mPercentageStyles;
	private SortedMap<String, OdfNumberCurrencyStyle> mCurrencyStyles;
	private SortedMap<String, OdfNumberTimeStyle> mTimeStyles;
	private SortedMap<String, NumberBooleanStyleElement> mBooleanStyles;
	private SortedMap<String, NumberTextStyleElement> mTextStyles;
	
	private SortedMap<String, OdfStyle> autoStylesMap;
	
	public StylesData() {
	}

	public String getDocumentName() {
		return documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public SortedMap<String, OdfeStyleFamily> getFamilyTree() {
		return familyTree;
	}

	public void setFamilyTree(SortedMap<String, OdfeStyleFamily> familyTree) {
		this.familyTree = familyTree;
	}

	public SortedMap<String, Integer> getStylesUsed() {
		return stylesUsed;
	}

	public void setStylesUsed(SortedMap<String, Integer> stylesUsed) {
		this.stylesUsed = stylesUsed;
	}

	public void setRunDate(String date) {
		rundate = date;
	}

	public String getRunDate() {
		return rundate;
	}

	public void addTextListStyle(OdfTextListStyle listStyle) {
		mListStyles.put(listStyle.getStyleNameAttribute(), listStyle);
	}
	
	public Iterable<OdfTextListStyle>  getTextListStyles() {
		return mListStyles.values();
	}

	public void setAutoStyles(SortedMap<String, OdfStyle> autoStylesMap) {
		this.autoStylesMap = autoStylesMap;	
	}
	
	public Iterable<OdfStyle>  getAutoStyles() {
		return autoStylesMap.values();
	}

	

}