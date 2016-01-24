package net.amham.odfe.xmlwrite;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.SortedMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.amham.odfe.style.FamilyStyleNode;
import net.amham.odfe.style.OdfeStyleFamily;

import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.element.OdfStylePropertiesBase;
import org.odftoolkit.odfdom.dom.element.number.NumberBooleanStyleElement;
import org.odftoolkit.odfdom.dom.element.number.NumberTextStyleElement;
import org.odftoolkit.odfdom.dom.element.text.TextListLevelStyleElementBase;
import org.odftoolkit.odfdom.dom.style.props.OdfStylePropertiesSet;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberCurrencyStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberDateStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberPercentageStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberStyle;
import org.odftoolkit.odfdom.incubator.doc.number.OdfNumberTimeStyle;
import org.odftoolkit.odfdom.incubator.doc.style.OdfDefaultStyle;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextListStyle;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Write out the style values to an XML file
 * 
 * Want this to morph into a rules generator for testing styles
 * from a template.
 * 
 * This is there are many automatic styles too?
 * 
 * @author ian
 *
 */
public class StylesXMLWriter {
	
	private class StylesData {
		private String documentName;
		private SortedMap <String, OdfeStyleFamily> familyTree;
		private SortedMap<String, Integer> stylesUsed;
		private String rundate; 
		
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
		
	}

	private final static   Logger LOGGER = Logger.getLogger(StylesXMLWriter.class.getName());
	
	private String ODFE_XSL = "xml-stylesheet type='text/xsl' href='";
	
	private static final String START_TAG = "odfestyles";
	private static final String SOURCES_TAG = "sources";
	private static final String SOURCE_TAG = "source";
	private static final String SOURCE_NUMBER_TAG = "source-number";
	private static final String DOC_TAG = "docname";
	private static final String RUN_TAG = "rundate";

	private static final String STYLES_TAG = "styles";
	private static final String STYLES_NAME_TAG = "name";

	private static final String FAMILIES_TAG = "families";
	private static final String FAMILY_TAG = "family";
	private static final String FAMILY_NAME_TAG = "name";
	
	private static final String DEFAULT_STYLE_TAG = "default";
	private static final String STYLE_TAG = "style";
	private static final String STYLE_NAME_TAG = "name";
	private static final String PROPSET_TAG = "propertyset";
	private static final String PROPSET_NAME_TAG = "name";
	private static final String ATTR_TAG = "attribute";
	private static final String ATTR_NAME_TAG = "name";
	private static final String ATTR_VALUE_TAG = "value";
	
	private static final String CURRENCY_STYLE_TAG = "currency_style";
	private static final String CURRENCY_STYLE_NAME_TAG = "name";

	private static final String TEXTLIST_STYLE_TAG = "textlist_style";
	private static final String TEXTLIST_STYLE_NAME_TAG = "name";
	private static final String TEXTLIST_STYLE_LEVEL_TAG = "level";

	private static final String NUMBERDATE_STYLE_TAG = "numberdate_style";
	private static final String NUMBERDATE_STYLE_NAME_TAG = "name";

	private static final String NUMBER_STYLE_TAG = "number_style";
	private static final String NUMBER_STYLE_NAME_TAG = "name";

	private static final String PERCENTAGE_STYLE_TAG = "percentage_style";
	private static final String PERCENTAGE_STYLE_NAME_TAG = "name";

	private static final String BOOLEAN_STYLE_TAG = "boolean_style";
	private static final String BOOLEAN_STYLE_NAME_TAG = "name";

	private static final String NUMBERTEXT_STYLE_TAG = "numbertext_style";
	private static final String NUMBERTEXT_STYLE_NAME_TAG = "name";

	private static final String TIME_STYLE_TAG = "time_style";
	private static final String TIME_STYLE_NAME_TAG = "name";

	private static final String STYLE_HITS_TAG = "hits";
	
	private XMLOutputFactory m_xof =  XMLOutputFactory.newInstance();

	private XMLStreamWriter m_xmlWriter;
	
	
	private Vector<StylesData> styles = new Vector<StylesData>();
	
	/**
	 * Open an XML output file
	 * The file output may be derived from many input sources
	 * That is from a set of OdfGauges
	 * 
	 * @param name
	 * @param string 
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void open(File file) throws XMLStreamException, IOException {
		
		m_xmlWriter = m_xof.createXMLStreamWriter(new FileWriter(file));
		LOGGER.info("Open " + file.getAbsolutePath());
		m_xmlWriter.writeStartDocument();
		ODFE_XSL += "../../../xsl/odfeStylesTree.xsl'";
		LOGGER.fine("Style sheet from " + ODFE_XSL);
		m_xmlWriter.writeProcessingInstruction(ODFE_XSL);
		m_xmlWriter.writeStartElement(START_TAG);
		
	}
	
	private void writeSources() throws XMLStreamException {
		m_xmlWriter.writeStartElement(SOURCES_TAG);

		Integer sourceNumber = 1;
		for (StylesData sd : styles) {
			m_xmlWriter.writeStartElement(SOURCE_TAG);
			m_xmlWriter.writeAttribute(SOURCE_NUMBER_TAG, sourceNumber.toString());
			m_xmlWriter.writeAttribute(DOC_TAG, sd.getDocumentName());
			m_xmlWriter.writeAttribute(RUN_TAG, sd.getRunDate());
			m_xmlWriter.writeEndElement(); //SOURCE
			sourceNumber += 1;
		}
		m_xmlWriter.writeEndElement(); //SOURCES

	}
	
	public void writeJson() throws XMLStreamException {
		m_xmlWriter.writeStartElement("jsonfile");
		m_xmlWriter.writeCharacters("odfestyles.json");
		m_xmlWriter.writeEndElement(); //SOURCES

	}
	

	public void close() throws XMLStreamException {
		writeJson();
		m_xmlWriter.writeEndElement(); //START ELEMENT
		m_xmlWriter.writeEndDocument();
		m_xmlWriter.flush();
		m_xmlWriter.close();
		LOGGER.info("StylesXMLWriter Closed");
	}

	/**
	 * Iterate through the styles families
	 * 	Generate the default properties data
	 * 		walk the tree of styles and note the hits of each
	 * 
	 * Possible issue is when a style has been removed from a document
	 * or when a new style has been added 
	 * 
	 * Maybe we need to normalise/merge the style sets first?
	 * Or we will get into trouble with the hit details
	 * 
	 * 
	 * 
	 * @param styleNum 
	 * 
	 * @param styleFamiliesMap
	 * @param stylesUsed
	 * @throws XMLStreamException
	 */
	public void writeFamilies(Integer styleNum, SortedMap<String, OdfeStyleFamily> styleFamiliesMap, SortedMap<String, Integer> stylesUsed) throws XMLStreamException {
		
		m_xmlWriter.writeStartElement(FAMILIES_TAG);
		for(String family : styleFamiliesMap.keySet()) {
			m_xmlWriter.writeStartElement(FAMILY_TAG);
			m_xmlWriter.writeAttribute(FAMILY_NAME_TAG, family);
			
			OdfeStyleFamily styleFamily = styleFamiliesMap.get(family);
			OdfDefaultStyle defaultStyle = styleFamily.getDefaultStyle();
			if (defaultStyle != null) {
				m_xmlWriter.writeStartElement(DEFAULT_STYLE_TAG);
				writePropertySets(defaultStyle);
				m_xmlWriter.writeEndElement();
			}			
			
			Iterable<FamilyStyleNode> roots = styleFamily.getFamilyStyles();
			for(FamilyStyleNode familyStyle : roots) {
				walkFamilyTree(familyStyle, stylesUsed);
			}
			m_xmlWriter.writeEndElement();
		}
		m_xmlWriter.writeEndElement();
		
	}

	private void walkFamilyTree(FamilyStyleNode familyStyle, SortedMap<String, Integer> stylesUsed) throws XMLStreamException {
		OdfStyle style = familyStyle.getStyle();
		m_xmlWriter.writeStartElement(STYLE_TAG);
		m_xmlWriter.writeAttribute(STYLE_NAME_TAG, style.getStyleNameAttribute());
		
		Integer totalHits = 0;
		for(StylesData sd : styles) {
			Integer hits = sd.getStylesUsed().get(style.getStyleNameAttribute()) ;
			if (hits != null) {
				totalHits += hits;
			}
		}
		m_xmlWriter.writeAttribute("totalHits", totalHits.toString());
		
		if (totalHits > 0) {
			Integer sourceNumber = 1;
			for(StylesData sd : styles) {
				String srcElem = "src" + sourceNumber.toString() + "hits";
				Integer hits = sd.getStylesUsed().get(style.getStyleNameAttribute()) ;
				if (hits != null) {
					m_xmlWriter.writeAttribute(srcElem, hits.toString() );
				}
				else {
					m_xmlWriter.writeAttribute(srcElem, "0" );
				}
//				m_xmlWriter.writeAttribute(srcElem, sd.getStylesUsed().get(style.getStyleNameAttribute()).toString() );
				sourceNumber +=1;
			}
		}
		// get the style properties and iterate them
		// basically should be a copy of what is in the original document
		writePropertySets(style);
		
		//recurse through the trees
		Iterable<FamilyStyleNode> children = familyStyle.getChildren();
		for(FamilyStyleNode child : children) {
			walkFamilyTree(child, stylesUsed);
		}
		m_xmlWriter.writeEndElement();
	}

	private void writePropertySets(OdfStyleBase style) throws XMLStreamException {
		for (OdfStylePropertiesSet propSet : OdfStylePropertiesSet.values()) {
			OdfStylePropertiesBase props = style.getOrCreatePropertiesElement(propSet);

			NamedNodeMap attrs = props.getAttributes();
			int numAttrubutes = attrs.getLength();
			if (numAttrubutes > 0) {
				m_xmlWriter.writeStartElement(PROPSET_TAG);
				m_xmlWriter.writeAttribute(PROPSET_NAME_TAG, propSet.name());
				for (int i = 0; i < numAttrubutes; i++) {
					Node attr = attrs.item(i);
					m_xmlWriter.writeStartElement(ATTR_TAG);
					m_xmlWriter.writeAttribute(ATTR_NAME_TAG, attr.getNodeName());
					m_xmlWriter.writeAttribute(ATTR_VALUE_TAG, attr.getNodeValue());
					m_xmlWriter.writeEndElement();
				}
				m_xmlWriter.writeEndElement();
			}
		}
	}

	public void writeTextListStyle(OdfTextListStyle listStyle) throws XMLStreamException {
		m_xmlWriter.writeStartElement(TEXTLIST_STYLE_TAG);
		m_xmlWriter.writeAttribute(TEXTLIST_STYLE_NAME_TAG, listStyle.getLocalName());
		NamedNodeMap attrs = listStyle.getAttributes();
		writeAttributes(attrs);
		for(Integer lvl=1; lvl<=10; lvl++) {
			TextListLevelStyleElementBase levelElement = listStyle.getLevel(lvl);
			if(levelElement != null) {
				m_xmlWriter.writeStartElement(TEXTLIST_STYLE_LEVEL_TAG);
				m_xmlWriter.writeAttribute("num", lvl.toString());
				writePropertySets(levelElement);
				NamedNodeMap lvlAttrs = levelElement.getAttributes();
				writeAttributes(lvlAttrs);
				OdfStylePropertiesBase lvlProps = levelElement.getOrCreatePropertiesElement(OdfStylePropertiesSet.ListLevelProperties);
				NodeList lvlNodes = lvlProps.getChildNodes();
				for (int nd = 0; nd < lvlNodes.getLength(); nd++) {
					m_xmlWriter.writeStartElement(lvlNodes.item(nd).getLocalName());
					writeAttributes(lvlNodes.item(nd).getAttributes());
					m_xmlWriter.writeEndElement();
				}
				m_xmlWriter.writeEndElement();
			}
		}
		m_xmlWriter.writeEndElement();
	}

	public void writeTextListPropertyElements(OdfTextListStyle listStyle) throws XMLStreamException {
		m_xmlWriter.writeStartElement(TEXTLIST_STYLE_TAG);
		m_xmlWriter.writeAttribute(TEXTLIST_STYLE_NAME_TAG, listStyle.getLocalName());
		NamedNodeMap attrs = listStyle.getAttributes();
		writeAttributes(attrs);
		for(int lvl=1; lvl<10; lvl++) {
			TextListLevelStyleElementBase levelElement = listStyle.getLevel(lvl);
			if(levelElement != null) {
				m_xmlWriter.writeStartElement(TEXTLIST_STYLE_LEVEL_TAG);
				writePropertySets(levelElement);
				NamedNodeMap lvlAttrs = levelElement.getAttributes();
				writeAttributes(lvlAttrs);
				NodeList nodes = levelElement.getChildNodes();
				for (int nd = 0; nd < nodes.getLength(); nd++) {
					m_xmlWriter.writeStartElement(nodes.item(nd).getLocalName());
					writeAttributes(nodes.item(nd).getAttributes());
					m_xmlWriter.writeEndElement();
				}
				m_xmlWriter.writeEndElement();
			}
		}
		m_xmlWriter.writeEndElement();
	}

	public void writeNumberCurrencyStyle(OdfNumberCurrencyStyle currencyStyle) throws XMLStreamException {
		m_xmlWriter.writeStartElement(CURRENCY_STYLE_TAG);
		m_xmlWriter.writeAttribute(CURRENCY_STYLE_NAME_TAG, currencyStyle.getLocalName());
		NamedNodeMap attrs = currencyStyle.getAttributes();
		writeAttributes(attrs);
		m_xmlWriter.writeEndElement();
	}

	public void writeNumberDateStyle(OdfNumberDateStyle dateStyle) throws XMLStreamException {
		m_xmlWriter.writeStartElement(NUMBERDATE_STYLE_TAG);
		m_xmlWriter.writeAttribute(NUMBERDATE_STYLE_NAME_TAG, dateStyle.getLocalName());
		NamedNodeMap attrs = dateStyle.getAttributes();
		writeAttributes(attrs);
		m_xmlWriter.writeEndElement();
	}

	public void writeNumberStyle(OdfNumberStyle numberStyle) throws XMLStreamException {
		m_xmlWriter.writeStartElement(NUMBER_STYLE_TAG);
		m_xmlWriter.writeAttribute(NUMBER_STYLE_NAME_TAG, numberStyle.getLocalName());
		NamedNodeMap attrs = numberStyle.getAttributes();
		writeAttributes(attrs);
		m_xmlWriter.writeEndElement();
	}

	public void writeNumberPercentageStyle(OdfNumberPercentageStyle percentageStyle) throws XMLStreamException {
		m_xmlWriter.writeStartElement(PERCENTAGE_STYLE_TAG);
		m_xmlWriter.writeAttribute(PERCENTAGE_STYLE_NAME_TAG, percentageStyle.getLocalName());
		NamedNodeMap attrs = percentageStyle.getAttributes();
		writeAttributes(attrs);
		m_xmlWriter.writeEndElement();
	}

	public void writeBooleanStyle(NumberBooleanStyleElement booleanStyle) throws XMLStreamException {
		m_xmlWriter.writeStartElement(BOOLEAN_STYLE_TAG);
		m_xmlWriter.writeAttribute(BOOLEAN_STYLE_NAME_TAG, booleanStyle.getLocalName());
		NamedNodeMap attrs = booleanStyle.getAttributes();
		writeAttributes(attrs);
		m_xmlWriter.writeEndElement();
	}

	public void writeNumberTextStyle(NumberTextStyleElement numberTextStyle) throws XMLStreamException {
		m_xmlWriter.writeStartElement(NUMBERTEXT_STYLE_TAG);
		m_xmlWriter.writeAttribute(NUMBERTEXT_STYLE_NAME_TAG, numberTextStyle.getLocalName());
		NamedNodeMap attrs = numberTextStyle.getAttributes();
		writeAttributes(attrs);
		m_xmlWriter.writeEndElement();
	}

	public void writeNumberTimeStyle(OdfNumberTimeStyle timeStyle) throws XMLStreamException {
		m_xmlWriter.writeStartElement(TIME_STYLE_TAG);
		m_xmlWriter.writeAttribute(TIME_STYLE_NAME_TAG, timeStyle.getLocalName());
		NamedNodeMap attrs = timeStyle.getAttributes();
		writeAttributes(attrs);
		m_xmlWriter.writeEndElement();
	}
	
	private void writeAttributes(NamedNodeMap attrs) throws XMLStreamException {
		for (int i = 0; i < attrs.getLength(); i++) {
			Node attr = attrs.item(i);
			m_xmlWriter.writeStartElement(ATTR_TAG);
			m_xmlWriter.writeAttribute(ATTR_NAME_TAG, attr.getNodeName());
			m_xmlWriter.writeAttribute(ATTR_VALUE_TAG, attr.getNodeValue());
			m_xmlWriter.writeEndElement();
		}
	}

	public void addSource(String doc, SortedMap<String, OdfeStyleFamily> familyTree, SortedMap<String, Integer> stylesUsed) {
		// TODO Auto-generated method stub
		StylesData sd = new StylesData();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		sd.setRunDate(dateFormat.format(date));
		
		sd.setDocumentName(doc);
		sd.setFamilyTree(familyTree);
		sd.setStylesUsed(stylesUsed);
		
		styles.add(sd);
	}

	/**
	 * Iterate the style data gathered for each source
	 * and write to XML
	 * 
	 * @throws XMLStreamException
	 */
	public void writeStyles() throws XMLStreamException {
/*		Gson gson = new Gson();
		
		String json = gson.toJson(SOURCES_TAG);
		
		Integer sourceNumber = 1;
		for (StylesData sd : styles) {
			json += gson.toJson(sd.getDocumentName());
			json += gson.toJson(sd.getRunDate());
			sourceNumber += 1;
		}
		
		System.out.println(json); */
		writeSources();

		//What we do here depends on how many sets of style data we have
		// One we just write it... :-)
		//
		// For more than one? Merge in some way
		//	added will have 0 hits on first style
		//  deleted - will just appear as unused
		//
		//  or report new styles separately - so assume family of source one is the reference
		//
		
		Integer styleNum = 1;
		StylesData sd =styles.elementAt(0);
		writeFamilies(styleNum, sd.getFamilyTree(), sd.getStylesUsed());
		
		// report new Styles somehow? Swapping works if done manually
		// so for a template check make the template the second document
		// better still find the template automatically from the meta data
	}
	
}
