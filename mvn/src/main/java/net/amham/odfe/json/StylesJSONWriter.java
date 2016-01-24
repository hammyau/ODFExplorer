package net.amham.odfe.json;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.amham.odfe.style.FamilyStyleNode;
import net.amham.odfe.style.OdfeStyleFamily;
import net.amham.odfe.style.StyleFamilyDiffData;
import net.amham.odfe.style.StylesData;

import org.codehaus.jackson.JsonFactory;	
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;
import org.codehaus.jettison.json.JSONWriter;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.element.OdfStylePropertiesBase;
import org.odftoolkit.odfdom.dom.element.number.NumberBooleanStyleElement;
import org.odftoolkit.odfdom.dom.element.number.NumberTextStyleElement;
import org.odftoolkit.odfdom.dom.element.text.TextListLevelStyleElementBase;
import org.odftoolkit.odfdom.dom.style.props.OdfStylePropertiesSet;
import org.odftoolkit.odfdom.dom.style.props.OdfStyleProperty;
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
public class StylesJSONWriter {
		
	protected StyleFamilyDiffData data = new StyleFamilyDiffData();

/*	private class DiffNew extends ReportFamily {
		DiffNew() {
			type = DiffType.NewStyle;
		}
	}
	
	private class DiffChanged extends ReportFamily {
		DiffChanged() {
			type = DiffType.ChangedStyle;
		}
	}
	
	private class DiffMissing extends ReportFamily {
		DiffMissing() {
			type = DiffType.MissingStyle;
		}
	}
	
	private class DiffSame extends ReportFamily {
		DiffSame() {
			type = DiffType.SameStyle;
		}
	}*/
	
/*	private class DiffStyle {
		protected DiffType type;
		protected OdfStyle style;
		protected OdfStyle diffStyle;
		
		public DiffType getType() {
			return type;
		}

		public void setType(DiffType type) {
			this.type = type;
		}

		public OdfStyle getStyle() {
			return style;
		}

		public void setSyle(OdfStyle ref) {
			style = ref;
		}

		public void addTarget(OdfStyle diff) {
			diffStyle = diff;
		}

	}*/
	
	private static final String CHILDREN_TAG = "children";
	protected static final String HIDDEN_CHILDREN_TAG = "_children";

	private final static   Logger LOGGER = Logger.getLogger(StylesJSONWriter.class.getName());
	
	private static final String START_TAG = "odfestyles";
	private static final String SOURCES_TAG = "sources";
	private static final String SOURCE_TAG = "source";
	private static final String SOURCE_NUMBER_TAG = "source-number";
	private static final String DOC_TAG = "docname";
	private static final String RUN_TAG = "rundate";

	private static final String FAMILIES_TAG = "families";
	private static final String FAMILY_TAG = "family";
	protected static final String NAME_TAG = "name";
	protected static final String VALUE_TAG = "value";
	protected static final String VALUE1_TAG = "value1";
	protected static final String VALUE2_TAG = "value2";
	
	private static final String NODE_TYPE = "type";
	
	private static final String DEFAULT_STYLE_TAG = "defaultStyle";
	private static final String STYLE_TAG = "style";
	private static final String STYLE_NAME_TAG = "name";
	private static final String PROPSET_TAG = "propertyset";
	private static final String PROPSET_NAME_TAG = "name";
	private static final String ATTR_TAG = "attributes";
	private static final String ATTR_NAME_TAG = "name";
	private static final String ATTR_VALUE_TAG = "value";
	
	private static final String CURRENCY_STYLE_TAG = "currency_style";
	private static final String CURRENCY_STYLE_NAME_TAG = "name";

	private static final String TEXTLIST_STYLE_TAG = "textlist";
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
	
	private String mode;
	private JsonGenerator generator;
	private ObjectNode rootNode;
	protected ArrayNode rootArray;

	private Vector<String> sourceNames = new Vector<String>();
	protected Vector<StylesData> styles = new Vector<StylesData>();
	
//	private ArrayNode testListArrary = null;
	
	
	public StylesJSONWriter() {
		
	}
	
	/**
	 * Open a JSON output file
	 * The file output may be derived from many input sources
	 *  
	 * @param name
	 * @param string 
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void open(File file) throws XMLStreamException, IOException {
		
        // Mapped convention
		JsonFactory f = new JsonFactory();
		try {
			generator = f.createJsonGenerator(new FileWriter(file));
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
			generator.setCodec(mapper);
			generator.useDefaultPrettyPrinter();

			rootNode = mapper.createObjectNode();
			rootNode.put("name", "odfestyles");
			rootArray = rootNode.putArray(CHILDREN_TAG);
			
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	protected void writeSources() throws XMLStreamException {
		Integer sourceNumber = 1;
		ObjectNode srcs = rootArray.addObject();
		srcs.put("name", "sources");
		ArrayNode an = srcs.putArray(CHILDREN_TAG);
		for (StylesData sd : styles) {
			ObjectNode doc = an.addObject();
			doc.put("source", sd.getDocumentName());
			doc.put("run", sd.getRunDate());
			sourceNumber += 1;
		}
	}
	

	public void close() throws JsonProcessingException, IOException, XMLStreamException {
		writeRunInfo();
		generator.writeTree(rootNode);
		generator.close(); 
		LOGGER.info("Syles JSON Writer Closed");
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
	public Integer writeFamilies(SortedMap<String, OdfeStyleFamily> styleFamiliesMap, SortedMap<String, Integer> stylesUsed) throws XMLStreamException {

		Integer familiesHits = 0;
		ObjectNode familiesObj = rootArray.addObject();
		familiesObj.put("name", "families");
		ArrayNode familiesArray = familiesObj.putArray(CHILDREN_TAG);

		for(String family : styleFamiliesMap.keySet()) {
			Integer totalHits = 0;
			OdfeStyleFamily styleFamily = styleFamiliesMap.get(family);
			OdfDefaultStyle defaultStyle = styleFamily.getDefaultStyle();
			
			ObjectNode familyObj = familiesArray.addObject();
			familyObj.put("name", family);
			ArrayNode familyArray = familyObj.putArray(HIDDEN_CHILDREN_TAG);
			if (defaultStyle != null) {
				ObjectNode defaultObj = familyArray.addObject();
				defaultObj.put("name", "Default");
				ArrayNode defaultArray = defaultObj.putArray(HIDDEN_CHILDREN_TAG);
				writePropertySets(defaultArray, defaultStyle);
			}			
			
			Iterable<FamilyStyleNode> roots = styleFamily.getFamilyStyles();
			for(FamilyStyleNode familyStyle : roots) {
				totalHits += walkFamilyTree(familyArray, familyStyle, stylesUsed);
			}
			if(totalHits > 0) {
				familyObj.put("totalHits", totalHits);	
				familiesHits += totalHits;
			}
		}
		if(familiesHits > 0) {
			familiesObj.put("totalHits", familiesHits);	
		}
		return familiesHits;
	}


	/**
	 * @param contentsArray
	 * @param name
	 * @param diffType 
	 * @return
	 */
	protected ArrayNode addTreeNode(ArrayNode contentsArray, String name, boolean hidden) {
		ObjectNode familyObj = contentsArray.addObject();
		familyObj.put("name", name);
		ArrayNode treeArray;
		if (hidden == true) {
			treeArray = familyObj.putArray(HIDDEN_CHILDREN_TAG);			
		}
		else {
			treeArray = familyObj.putArray(CHILDREN_TAG);
		}
		return treeArray;
	}

	protected ArrayNode addDiffTreeNode(ArrayNode contentsArray, String name, boolean hidden, DiffType diffType) {
		ObjectNode familyObj = contentsArray.addObject();
		familyObj.put("name", name);
		familyObj.put("diff", diffType.toString());
		ArrayNode treeArray;
		if (hidden == true) {
			treeArray = familyObj.putArray(HIDDEN_CHILDREN_TAG);			
		}
		else {
			treeArray = familyObj.putArray(CHILDREN_TAG);
		}
		
		return treeArray;
	}

	/**
	 * @param contentsArray
	 * @param name
	 * @return
	 */
	private ArrayNode addTreeObject(ObjectNode familyObj, String name, boolean hidden) {
		familyObj.put("name", name);
		ArrayNode treeArray;
		if (hidden == true) {
			treeArray = familyObj.putArray(HIDDEN_CHILDREN_TAG);			
		}
		else {
			treeArray = familyObj.putArray(CHILDREN_TAG);
		}
		return treeArray;
	}


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
		ArrayNode familyStyleArray = familyObj.putArray(HIDDEN_CHILDREN_TAG);			
		writePropertySets(familyStyleArray, style);


		// recurse through the trees
		Integer childHits = 0;
		Iterable<FamilyStyleNode> children = familyStyle.getChildren();
		for (FamilyStyleNode child : children) {
			childHits += walkFamilyTree(familyStyleArray, child, stylesUsed);
		}
		
		if (childHits > 0) {
			totalHits += childHits;
			familyObj.put("totalHits", totalHits);			
		}
		return totalHits;
	}

	protected Integer writePropertySets(ArrayNode styleArray, OdfStyleBase style) throws XMLStreamException {
		Integer NumPropertySets = 0;
		for (OdfStylePropertiesSet propSet : OdfStylePropertiesSet.values()) {
			OdfStylePropertiesBase props = style.getOrCreatePropertiesElement(propSet);

			NamedNodeMap attrs = props.getAttributes();
			int numAttrubutes = attrs.getLength();
			if (numAttrubutes > 0) {
				ArrayNode propsArray = addTreeNode(styleArray, propSet.name(), true);
				writeAttributes(propsArray, attrs);
				NumPropertySets++;
			}
		}
		return NumPropertySets;
	}


/*	public void writeTextListStyle(OdfTextListStyle listStyle) throws XMLStreamException {
		generator.writeStartElement(TEXTLIST_STYLE_TAG);
		m_jsonWriter.writeAttribute(TEXTLIST_STYLE_NAME_TAG, listStyle.getLocalName());
		NamedNodeMap attrs = listStyle.getAttributes();
//		writeAttributes(attrs);
		for(Integer lvl=1; lvl<=10; lvl++) {
			TextListLevelStyleElementBase levelElement = listStyle.getLevel(lvl);
			if(levelElement != null) {
				data.m_jsonWriter.writeStartElement(TEXTLIST_STYLE_LEVEL_TAG);
				data.m_jsonWriter.writeAttribute("num", lvl.toString());
//				writePropertySets(levelElement);
				NamedNodeMap lvlAttrs = levelElement.getAttributes();
				//writeAttributes(lvlAttrs);
				OdfStylePropertiesBase lvlProps = levelElement.getOrCreatePropertiesElement(OdfStylePropertiesSet.ListLevelProperties);
				NodeList lvlNodes = lvlProps.getChildNodes();
				for (int nd = 0; nd < lvlNodes.getLength(); nd++) {
					data.m_jsonWriter.writeStartElement(lvlNodes.item(nd).getLocalName());
					//writeAttributes(lvlNodes.item(nd).getAttributes());
					data.m_jsonWriter.writeEndElement();
				}
				data.m_jsonWriter.writeEndElement();
			}
		}
		data.m_jsonWriter.writeEndElement();
	}

	public void writeTextListPropertyElements(OdfTextListStyle listStyle) throws XMLStreamException {
		data.m_jsonWriter.writeStartElement(TEXTLIST_STYLE_TAG);
		data.m_jsonWriter.writeAttribute(TEXTLIST_STYLE_NAME_TAG, listStyle.getLocalName());
		NamedNodeMap attrs = listStyle.getAttributes();
		//writeAttributes(attrs);
		for(int lvl=1; lvl<10; lvl++) {
			TextListLevelStyleElementBase levelElement = listStyle.getLevel(lvl);
			if(levelElement != null) {
				data.m_jsonWriter.writeStartElement(TEXTLIST_STYLE_LEVEL_TAG);
//				writePropertySets(levelElement);
				NamedNodeMap lvlAttrs = levelElement.getAttributes();
				//writeAttributes(lvlAttrs);
				NodeList nodes = levelElement.getChildNodes();
				for (int nd = 0; nd < nodes.getLength(); nd++) {
					data.m_jsonWriter.writeStartElement(nodes.item(nd).getLocalName());
					//writeAttributes(nodes.item(nd).getAttributes());
					data.m_jsonWriter.writeEndElement();
				}
				data.m_jsonWriter.writeEndElement();
			}
		}
		data.m_jsonWriter.writeEndElement();
	}

	public void writeNumberCurrencyStyle(OdfNumberCurrencyStyle currencyStyle) throws XMLStreamException {
		data.m_jsonWriter.writeStartElement(CURRENCY_STYLE_TAG);
		data.m_jsonWriter.writeAttribute(CURRENCY_STYLE_NAME_TAG, currencyStyle.getLocalName());
		NamedNodeMap attrs = currencyStyle.getAttributes();
		//writeAttributes(attrs);
		data.m_jsonWriter.writeEndElement();
	}

	public void writeNumberDateStyle(OdfNumberDateStyle dateStyle) throws XMLStreamException {
		data.m_jsonWriter.writeStartElement(NUMBERDATE_STYLE_TAG);
		data.m_jsonWriter.writeAttribute(NUMBERDATE_STYLE_NAME_TAG, dateStyle.getLocalName());
		NamedNodeMap attrs = dateStyle.getAttributes();
		//writeAttributes(attrs);
		data.m_jsonWriter.writeEndElement();
	}

	public void writeNumberStyle(OdfNumberStyle numberStyle) throws XMLStreamException {
		data.m_jsonWriter.writeStartElement(NUMBER_STYLE_TAG);
		data.m_jsonWriter.writeAttribute(NUMBER_STYLE_NAME_TAG, numberStyle.getLocalName());
		NamedNodeMap attrs = numberStyle.getAttributes();
		//writeAttributes(attrs);
		data.m_jsonWriter.writeEndElement();
	}			


	public void writeNumberPercentageStyle(OdfNumberPercentageStyle percentageStyle) throws XMLStreamException {
		data.m_jsonWriter.writeStartElement(PERCENTAGE_STYLE_TAG);
		data.m_jsonWriter.writeAttribute(PERCENTAGE_STYLE_NAME_TAG, percentageStyle.getLocalName());
		NamedNodeMap attrs = percentageStyle.getAttributes();
		//writeAttributes(attrs);
		data.m_jsonWriter.writeEndElement();
	}

	public void writeBooleanStyle(NumberBooleanStyleElement booleanStyle) throws XMLStreamException {
		data.m_jsonWriter.writeStartElement(BOOLEAN_STYLE_TAG);
		data.m_jsonWriter.writeAttribute(BOOLEAN_STYLE_NAME_TAG, booleanStyle.getLocalName());
		NamedNodeMap attrs = booleanStyle.getAttributes();
		//writeAttributes(attrs);
		data.m_jsonWriter.writeEndElement();
	}

	public void writeNumberTextStyle(NumberTextStyleElement numberTextStyle) throws XMLStreamException {
		data.m_jsonWriter.writeStartElement(NUMBERTEXT_STYLE_TAG);
		data.m_jsonWriter.writeAttribute(NUMBERTEXT_STYLE_NAME_TAG, numberTextStyle.getLocalName());
		NamedNodeMap attrs = numberTextStyle.getAttributes();
		//writeAttributes(attrs);
		data.m_jsonWriter.writeEndElement();
	}

	public void writeNumberTimeStyle(OdfNumberTimeStyle timeStyle) throws XMLStreamException {
		data.m_jsonWriter.writeStartElement(TIME_STYLE_TAG);
		data.m_jsonWriter.writeAttribute(TIME_STYLE_NAME_TAG, timeStyle.getLocalName());
		NamedNodeMap attrs = timeStyle.getAttributes();
		//writeAttributes(attrs);
		data.m_jsonWriter.writeEndElement();
	}*/
	
	private void writeAttributes(ArrayNode propsArray, NamedNodeMap attrs) throws XMLStreamException {
		for (int i = 0; i < attrs.getLength(); i++) {
			Node attr = attrs.item(i);
			ObjectNode attrObj = propsArray.addObject();
			attrObj.put(NAME_TAG, attr.getNodeName());
			attrObj.put(VALUE_TAG, attr.getNodeValue());
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
		
			StylesData sd = styles.elementAt(0);
			writeFamilies(sd.getFamilyTree(), sd.getStylesUsed());
			writeTextListStyles();
			writeAutoStyles();
	}

	
/*	private Boolean compareto(OdfeStyleFamily trgFam) {
		Boolean retval = true;
		//what is equal?
		LOGGER.info("Compare Names: " + name + "=" + trgFam.getName());
		if (refFam.getName().equals(trgFam.getName())) {
			//want to do a AEDS check
		
			Integer numRefStyles = refFam.getNumStyles();
			Integer numTrgStyles = trgFam.getNumStyles();
			LOGGER.info("Compare Number of styles: " + numRefStyles + "=" + numTrgStyles);
			
			Iterable<String> refFamilyStyleNames = refFam.getFamilyStyleNames();
			Iterator<String> refFamilyNameIt = refFamilyStyleNames.iterator();

			//look for missing - changed or same
			while(refFamilyNameIt.hasNext()) {
				String refName = refFamilyNameIt.next();
				FamilyStyleNode refNode = refFam.find(refName);				
				FamilyStyleNode trgNode = trgFam.find(refName);
				
				if (trgNode != null) {
					if (compareFamilyNodes(refNode, trgNode) == true) {
						// same family;
						

					} else {
						// something different
						retval = false;
					}
				}
			}
		}
		
		return retval;
	} 

	private Boolean compareFamilyNodes(FamilyStyleNode refNode, FamilyStyleNode trgNode) {
		Boolean retval = false;
		//What does equvialence mean here
		OdfStyle refStyle = refNode.getStyle();
		OdfStyle trgStyle = trgNode.getStyle();
		String refName = refStyle.getStyleNameAttribute();
		String trgName = trgStyle.getStyleNameAttribute(); 
		LOGGER.info("Compare FamilyNode: " + refName 
				+ " = " + trgName);
		
		if(refName.equals(trgName)) {
			int c = refStyle.compareTo(trgStyle);
			if(c !=0 ) {
				retval = false;
				LOGGER.info("DIFFERENT!");
				//add the target style to the ref this will be a diff by default
				refNode.addDiffStyle(trgStyle);
			}
		}
		//Need to iterate through children too
		Integer numRefChildren = refNode.getNumChildren();
		Integer numTrgChildren = trgNode.getNumChildren();
		LOGGER.info("Compare FamilyNode Children: " + numRefChildren
				+ "=" + numTrgChildren);
		//Even if they are not the same we need to iterate for AEDS
		if (numRefChildren == numTrgChildren) {
			//iterate and compare the children
			//Should do anyway? A E D same business
			Iterable<FamilyStyleNode> refChildren = refNode.getChildren();
			Iterable<FamilyStyleNode> trgChildren = trgNode.getChildren();
			
			Iterator<FamilyStyleNode> refChildNode = refChildren.iterator();
			Iterator<FamilyStyleNode> trgChildNode = trgChildren.iterator();
			if(refChildNode.hasNext()) {
				FamilyStyleNode refChild = refChildNode.next();
				FamilyStyleNode trgChild = trgChildNode.next();
				
				retval = compareFamilyNodes(refChild, trgChild);
			}
		}
		else {
			retval = false;
			LOGGER.info("DIFFERENT Children!");
		}
		return retval;
	} */
	
	private void writeRunInfo() throws XMLStreamException {
		StylesData sd = styles.elementAt(0);
		rootNode.put("source", sd.getDocumentName());
		rootNode.put("run", sd.getRunDate());
		rootNode.put("mode", mode);
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void addData(StylesData stylesData) {
		styles.add(stylesData); 		
	}

	public void addSource(String name) {
		sourceNames.add(name);
	}

	public SortedMap<String, OdfeStyleFamily> getStyleFamilyTreeAt(int i) {
		return styles.elementAt(i).getFamilyTree();
	}

	public void writeTextListStyles() throws XMLStreamException {
		StylesData sd = styles.elementAt(0);
		
		ArrayNode testListArrary = null;
		ObjectNode textListObj = null;
		Integer textListHits = 0;
		Iterable<OdfTextListStyle> it = sd.getTextListStyles();
		for (OdfTextListStyle listStyle :it) {
			if (testListArrary == null) {
				textListObj = rootArray.addObject();
				textListObj.put("name", TEXTLIST_STYLE_TAG);
				testListArrary = textListObj.putArray(HIDDEN_CHILDREN_TAG);
			}
			ObjectNode txtList = testListArrary.addObject();
			txtList.put("name", listStyle.getStyleNameAttribute());
			
			List<Integer> hitList = new ArrayList<Integer>();
			Integer totalHits = 0;
			for (StylesData sdata : styles) {
				Integer hits = sdata.getStylesUsed().get(listStyle.getStyleNameAttribute());
				if (hits != null) {
					hitList.add(hits);
					totalHits += hits;
				} else {
					hitList.add(0);
				}
			}

			if (totalHits > 0) {
				ArrayNode hitsArray = txtList.putArray("hits");
				for (Integer hit : hitList) {
					hitsArray.add(hit);
				}
				textListHits += totalHits;
			}
			//The only attributes at this level are
			//	styleName
			//	displayName - not set if style does not have a space
			//	consecutiveNumbering - not see it set
			
			ArrayNode txtListChildren = txtList.putArray(HIDDEN_CHILDREN_TAG);
			NamedNodeMap attrs = listStyle.getAttributes();
			writeAttributes(txtListChildren, attrs);
	
			ObjectNode lvlObj = txtListChildren.addObject();
			lvlObj.put("name", "Levels");
			ArrayNode levelArray = lvlObj.putArray(HIDDEN_CHILDREN_TAG);

			//there are always 10 levels?
			for(Integer lvl=1; lvl<=10; lvl++) {
				
				TextListLevelStyleElementBase levelElement = listStyle.getLevel(lvl);
				if(levelElement != null) {
					ObjectNode lvlDetailArray = levelArray.addObject();
					lvlDetailArray.put("name", TEXTLIST_STYLE_LEVEL_TAG + lvl.toString());
					ArrayNode lvlDetails = lvlDetailArray.putArray(HIDDEN_CHILDREN_TAG);
					ObjectNode attsObj = lvlDetails.addObject();
					attsObj.put("name", "attributes");
					ArrayNode attrArray = attsObj.putArray(HIDDEN_CHILDREN_TAG);
					NamedNodeMap lvlAttrs = levelElement.getAttributes();
					writeAttributes(attrArray, lvlAttrs);
					
					
					ObjectNode propsObj= lvlDetails.addObject();
					propsObj.put("name", "properties");
					ArrayNode propsArray = propsObj.putArray(HIDDEN_CHILDREN_TAG);
					writePropertySets(propsArray, levelElement);
				}
			}
		}		
		if (textListObj != null && textListHits > 0) {
			textListObj.put("totalHits", textListHits);
		}
	}

	public void writeNumberCurrencyStyle(OdfNumberCurrencyStyle currencyStyle) {
		ObjectNode srcs = rootArray.addObject();
		srcs.put("name", CURRENCY_STYLE_TAG);
		ArrayNode numberCurrencyArrary = srcs.putArray(HIDDEN_CHILDREN_TAG);
		ObjectNode numberCurrency = numberCurrencyArrary.addObject();
		numberCurrency.put("name", currencyStyle.getLocalName());
	}

	public void writeNumberDateStyle(OdfNumberDateStyle dateStyle) {
		ObjectNode srcs = rootArray.addObject();
		srcs.put("name", NUMBERDATE_STYLE_TAG);
		ArrayNode numberDateArrary = srcs.putArray(HIDDEN_CHILDREN_TAG);
		ObjectNode numberDate = numberDateArrary.addObject();
		numberDate.put("name", dateStyle.getLocalName());
	}

	public void writeNumberStyle(OdfNumberStyle numberStyle) {
		ObjectNode srcs = rootArray.addObject();
		srcs.put("name", NUMBER_STYLE_TAG);
		ArrayNode numberStyleArrary = srcs.putArray(HIDDEN_CHILDREN_TAG);
		ObjectNode number = numberStyleArrary.addObject();
		number.put("name", numberStyle.getLocalName());
	}

	public void writeNumberPercentageStyle(OdfNumberPercentageStyle percentageStyle) {
		ObjectNode srcs = rootArray.addObject();
		srcs.put("name", PERCENTAGE_STYLE_TAG);
		ArrayNode percentageStyleArrary = srcs.putArray(HIDDEN_CHILDREN_TAG);
		ObjectNode percentage = percentageStyleArrary.addObject();
		percentage.put("name", percentageStyle.getLocalName());
	}

	public void writeBooleanStyle(NumberBooleanStyleElement booleanStyle) {
		ObjectNode srcs = rootArray.addObject();
		srcs.put("name", BOOLEAN_STYLE_TAG);
		ArrayNode booleanStyleArrary = srcs.putArray(HIDDEN_CHILDREN_TAG);
		ObjectNode booleanNode = booleanStyleArrary.addObject();
		booleanNode.put("name", booleanStyle.getLocalName());
	}

	public void writeNumberTextStyle(NumberTextStyleElement numberTextStyle) {
		ObjectNode srcs = rootArray.addObject();
		srcs.put("name", NUMBERTEXT_STYLE_TAG);
		ArrayNode numberTextArrary = srcs.putArray(HIDDEN_CHILDREN_TAG);
		ObjectNode numberText = numberTextArrary.addObject();
		numberText.put("name", numberTextStyle.getLocalName());
	}

	public void writeNumberTimeStyle(OdfNumberTimeStyle timeStyle) {
		ObjectNode srcs = rootArray.addObject();
		srcs.put("name", TIME_STYLE_TAG);
		ArrayNode timeStyleArrary = srcs.putArray(HIDDEN_CHILDREN_TAG);
		ObjectNode timeStyleNode = timeStyleArrary.addObject();
		timeStyleNode.put("name", timeStyle.getLocalName());
	}
	
	private void writeAutoStyles() throws XMLStreamException {
		StylesData sd = styles.elementAt(0);
		
		//put together the styles use and the auto names
		ArrayNode autoStylesArrary = null;
		ObjectNode autoStylesObj = null;
		Integer autoStylesHits = 0;
		Iterable<OdfStyle> it = sd.getAutoStyles();
		for (OdfStyle autoStyle :it) {
			if (autoStylesArrary == null) {
				autoStylesObj = rootArray.addObject();
				autoStylesObj.put("name", "Automatic Styles");
				autoStylesArrary = autoStylesObj.putArray(HIDDEN_CHILDREN_TAG);
			}
			ObjectNode autoStyleObj = autoStylesArrary.addObject();
			String autoName = autoStyle.getStyleNameAttribute();
			String displayStyle = autoStyle.getAttribute("style:parent-style-name");
			displayStyle = autoName + " - from " + displayStyle;
			String autoListStyle = autoStyle.getAttribute("style:list-style-name");
			if(autoListStyle.length() > 0) {
				displayStyle += " list style : " + autoListStyle;
			}
			autoStyleObj.put("name", displayStyle + " - ");
			
			//was it hit?
			List<Integer> hitList = new ArrayList<Integer>();
			Integer totalHits = 0;
			for (StylesData sdata : styles) {
				Integer hits = sdata.getStylesUsed().get(autoName);
				if (hits != null) {
					hitList.add(hits);
					totalHits += hits;
				} else {
					hitList.add(0);
				}
			}

			if (totalHits > 0) {
				ArrayNode hitsArray = autoStyleObj.putArray("hits");
				for (Integer hit : hitList) {
					hitsArray.add(hit);
				}
				autoStylesHits += totalHits;
			}
			
			int objNdx = 0;
			ArrayNode detailsArray = autoStyleObj.putArray(HIDDEN_CHILDREN_TAG);
			ObjectNode familyObj = detailsArray.addObject();
			objNdx++;
			familyObj.put("name", "family");
			familyObj.put("value", autoStyle.getFamilyName());
			ObjectNode parentObj = detailsArray.addObject();
			objNdx++;
			parentObj.put("name", "parent");
			parentObj.put("value", autoStyle.getStyleParentStyleNameAttribute());
			String listStyle = autoStyle.getStyleListStyleNameAttribute();
			if(listStyle != null) {
				ObjectNode listStlyeObj = detailsArray.addObject();
				objNdx++;
				listStlyeObj.put("name", "list style");
				listStlyeObj.put("value", autoStyle.getStyleListStyleNameAttribute());
			}
			ObjectNode propsObj= detailsArray.addObject();
			objNdx++;
			propsObj.put("name", "properties");
			ArrayNode propsArray = propsObj.putArray(HIDDEN_CHILDREN_TAG);
			int propSets = writePropertySets(propsArray, autoStyle);
			if(propSets == 0) {
				detailsArray.remove(objNdx - 1);
			}

		}
		if (autoStylesObj != null && autoStylesHits > 0) {
			autoStylesObj.put("totalHits", autoStylesHits);
		}		
	}

	public boolean hasTwoStyles() {
		// TODO Auto-generated method stub
		return styles.size() == 2;
	}

}
