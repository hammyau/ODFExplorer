package net.amham.odfe.xmlwrite;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartDocument;

import net.amham.odfe.gauges.OdfAttributeGauge;
import net.amham.odfe.gauges.OdfElementGauge;
import net.amham.odfe.gauges.OdfGaugeStore;


/**
 * Write out the gauge values to an XML file
 * 
 * 
 * @author ian
 *
 */
public class GaugeXMLWriter {

	private final static   Logger LOGGER = Logger.getLogger(GaugeXMLWriter.class.getName());
	
	private String ODFE_XSL = "xml-stylesheet type='text/xsl' href='";
	
	private static final String START_TAG = "odfegauges";
	private static final String SOURCES_TAG = "sources";
	private static final String SOURCE_TAG = "source";
	private static final String SOURCE_NUMBER_TAG = "source-number";
	private static final String DOC_TAG = "docname";
	private static final String RUN_TAG = "rundate";

	private static final String MANIFEST_TAG = "manifest";
	private static final String MANIFEST_FILE_TAG = "file";

	private static final String META_TAG = "meta";
	private static final String METAVALUES_TAG = "metaValues";
	private static final String STYLES_TAG = "styles";
	private static final String STYLES_NAME_TAG = "name";

	private static final String GAUGES_TAG = "gauges";
	private static final String NAMESPACE_TAG = "namespace";
	private static final String ELEMENTS_TAG = "elements";
	private static final String ELEMENT_TAG = "element";
	private static final String ATTR_GAUGES_TAG = "attributes";
	private static final String ATTRIBUTE_TAG = "attribute";
	private static final String MANDATORY_TAG = "mandatory";
	private static final String GAUGE_TAG = "gauge";
	private static final String GAUGE_NAME_TAG = "name";
	private static final String GAUGE_VALUE_TAG = "value";
	private static final String GAUGE_HITS_TAG = "hits";	
	private static final String NOT_USED_TAG = "notused";
	
	private XMLEventFactory m_eventFactory = XMLEventFactory.newInstance();
	
	private XMLOutputFactory m_xof =  XMLOutputFactory.newInstance();

	private XMLStreamWriter m_xmlWriter;
	
	private Vector<OdfGaugeStore> gauges = new Vector<OdfGaugeStore>();
	
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
		ODFE_XSL += "../../../xsl/odfeGaugesTree.xsl'";
		LOGGER.fine("Style sheet from " + ODFE_XSL);
		m_xmlWriter.writeProcessingInstruction(ODFE_XSL);
		m_xmlWriter.writeStartElement(START_TAG);
	}
	
	/**
	 * Add a named OdfGaugeStore to the writer
	 * 
	 * Will need a version of this that supplies the rundate too
	 * @param source
	 * @param odfGauges
	 * @throws XMLStreamException 
	 */
	public void addSource(OdfGaugeStore odfGauges) throws XMLStreamException {
		gauges.add(odfGauges);
	}
	
	public void writeJson() throws XMLStreamException {
		m_xmlWriter.writeStartElement("jsonfile");
		m_xmlWriter.writeCharacters("odfegauges.json");
		m_xmlWriter.writeEndElement();

	}
	
	private void writeSources() throws XMLStreamException {
		m_xmlWriter.writeStartElement(SOURCES_TAG);

		Integer sourceNumber = 1;
		for (OdfGaugeStore gaugeStore : gauges) {
			m_xmlWriter.writeStartElement(SOURCE_TAG);
			m_xmlWriter.writeAttribute(SOURCE_NUMBER_TAG, sourceNumber.toString());
			m_xmlWriter.writeAttribute(DOC_TAG, gaugeStore.getName());
			m_xmlWriter.writeAttribute(RUN_TAG, gaugeStore.getRundate());
			m_xmlWriter.writeEndElement(); //SOURCE
			sourceNumber += 1;
		}
		m_xmlWriter.writeEndElement(); //SOURCES

	}
	
	/**
	 * Write out the gauges for each of the sources
	 * Iterate the namespaces and then the attributes within each element
	 * 
	 * We could make the attribute gauges optional?
	 * 
	 * 
	 * @param odfGauges
	 * @throws XMLStreamException
	 */
/*	public void writeGauges() throws XMLStreamException {
		writeSources();		

		m_xmlWriter.writeStartElement(GAUGES_TAG);
		
		// we can just use the first gauge set as the the key to iterate from
		
		if( gauges.size() > 0) {
			OdfGaugeStore odfGauges = gauges.elementAt(0);

			SortedMap <String, SortedMap <String, OdfElementGauge>> nsGaugeMap = odfGauges.getNamespaceGaugeMap();
			for(String ns : nsGaugeMap.keySet()) {
				m_xmlWriter.writeStartElement(NAMESPACE_TAG);
				m_xmlWriter.writeAttribute(GAUGE_NAME_TAG, ns);

				SortedMap <String, OdfElementGauge> gaugeMap = nsGaugeMap.get(ns);
				Integer numElements = gaugeMap.size();
				m_xmlWriter.writeAttribute(ELEMENTS_TAG, numElements.toString());
				for(String elementName : gaugeMap.keySet())
				{
					writeGaugeValue(ns, elementName);
				}
				m_xmlWriter.writeEndElement();
			}
		}
		else {
			LOGGER.warning("There are no gauges to write");
		}
		m_xmlWriter.writeEndElement();
	}*/
	


/*	public void writeMetaGauges(OdfGaugeStore odfGauges) throws XMLStreamException {
		m_xmlWriter.writeStartElement(META_TAG);		
		SortedMap <String, SortedMap <String, OdfElementGauge>> nsGaugeMap = odfGauges.getNamespaceGaugeMap();
		for(String ns : nsGaugeMap.keySet()) {
			m_xmlWriter.writeStartElement(NAMESPACE_TAG);
			m_xmlWriter.writeAttribute(GAUGE_NAME_TAG, ns);

			SortedMap <String, OdfElementGauge> gaugeMap = nsGaugeMap.get(ns);
			Integer numElements = gaugeMap.size();
			m_xmlWriter.writeAttribute(ELEMENTS_TAG, numElements.toString());
			for(String elementName : gaugeMap.keySet())
			{
				writeGaugeValue(elementName, gaugeMap.get(elementName));
			}
			m_xmlWriter.writeEndElement();
		}
		m_xmlWriter.writeEndElement();
	}*/

	public void writeMetaValues(Map <String, Integer> gaugesMap) throws XMLStreamException {
		m_xmlWriter.writeStartElement(METAVALUES_TAG);		
		for(String elementName : gaugesMap.keySet())
		{
			m_xmlWriter.writeStartElement(elementName.substring(elementName.lastIndexOf(':') + 1));		
			m_xmlWriter.writeCharacters(gaugesMap.get(elementName).toString());
			m_xmlWriter.writeEndElement();
		}
		m_xmlWriter.writeEndElement();
	}

	public void writeManifest(String name, SortedSet<String> files) throws XMLStreamException {
		m_xmlWriter.writeStartElement(MANIFEST_TAG);		
		m_xmlWriter.writeAttribute(DOC_TAG, name);
		for(String file : files)
		{
			m_xmlWriter.writeStartElement(MANIFEST_FILE_TAG);
			m_xmlWriter.writeCharacters(file);
			m_xmlWriter.writeEndElement();
		}
		m_xmlWriter.writeEndElement();
	}
	
	/**
	 * could write out
	 * <element>
	 * 		<name>elementName<\name>
	 * 		<hits>453<\hits>
	 * 		<attributes>
	 * 		    <attribute>
	 * 			    <name>attrName<\name>
	 * 		        <hits>453<\hits>
	 *              <manditory>1<\manditory>
	 * 		    <\attribute>
	 *          ...
	 *      <\attributes>
	 * <\element>
	 * 
	 * or
	 * 
	 * <element @name="elementName">
	 * 		<hits>453<\hits>
	 * 		<attributes>
	 * 		    <attribute @name="attrName">
	 * 		        <hits>453<\hits>
	 *              <mandatory>1<\mandatory>
	 * 		    <\attribute>
	 *          ...
	 *          <unused>
	 * 		         <attribute @name="attrName"\>
	 * 				 ...
	 *          <\ unused>
	 *      <\attributes>
	 * <\element>
	 * 
	 * <unusedElements>
	 * 	    <element @name="elementName"\>
	 * <unusedElements>
	 * 
	 * wrap the above in a namespace section
	 * 
	 * @param name
	 * @param elementName
	 * @throws XMLStreamException
	 */
	
/*	public void writeGaugeValue(String name, String elementName) throws XMLStreamException {
		m_xmlWriter.writeStartElement(ELEMENT_TAG);
		m_xmlWriter.writeAttribute(GAUGE_NAME_TAG, name);
		m_xmlWriter.writeStartElement(GAUGE_HITS_TAG);
		m_xmlWriter.writeCharacters(elementName.toString());
		
	
		m_xmlWriter.writeEndElement();
		m_xmlWriter.writeStartElement(GAUGE_VALUE_TAG);
		m_xmlWriter.writeCharacters(elementName.toString());
		m_xmlWriter.writeEndElement();
		m_xmlWriter.writeEndElement(); 
	}*/
	

	/**
	 * Write the element gauge and iterate the sources
	 * Writing the total hits is not needed
	 * but makes later processing a bit easier
	 * 
	 * Write that attributes within the gauge if any has hits > 0
	 * 
	 * @param elementName
	 * @param odfElementGauge
	 */
/*	private void writeGaugeValue(String ns, String elementName) {
		try {
			Integer sourceNumber = 1;
			m_xmlWriter.writeStartElement(ELEMENT_TAG);
			m_xmlWriter.writeAttribute(GAUGE_NAME_TAG, elementName);
			Integer totalHits = 0; 
			for(OdfGaugeStore gs : gauges) {
				OdfElementGauge elementGauge = gs.getNamespaceGaugeMap().get(ns).get(elementName);
				totalHits += elementGauge.getHits();
			}
			
			m_xmlWriter.writeAttribute("totalHits", totalHits.toString());
			
			if (totalHits > 0) {
				for(OdfGaugeStore gs : gauges) {
					OdfElementGauge elementGauge = gs.getNamespaceGaugeMap().get(ns).get(elementName);
					String srcElem = "src" + sourceNumber.toString() + "hits";
					m_xmlWriter.writeAttribute(srcElem, elementGauge.getHits().toString());
					sourceNumber +=1;
				}
			}
			
			if (totalHits > 0) {
				writeGaugeAtributeValues(ns, elementName);
			}
			m_xmlWriter.writeEndElement();
		} 
		catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	private void writeGaugeAtributeValues(String ns, String elementName) throws XMLStreamException {
		m_xmlWriter.writeStartElement(ATTR_GAUGES_TAG);
		SortedMap<String, OdfAttributeGauge> attributesGaugeMap = gauges.elementAt(0).getNamespaceGaugeMap().get(ns).get(elementName).getAttributesGaugeMap();

		for (String attrName : attributesGaugeMap.keySet()) {
			Integer sourceNumber = 1;
			m_xmlWriter.writeStartElement(ATTRIBUTE_TAG);			
			m_xmlWriter.writeAttribute(GAUGE_NAME_TAG, attrName);
			m_xmlWriter.writeAttribute(MANDATORY_TAG, attributesGaugeMap.get(attrName).getMandatory().toString());
			for(OdfGaugeStore gs : gauges) {
				SortedMap<String, OdfAttributeGauge> gsAttributesGaugeMap = gs.getNamespaceGaugeMap().get(ns).get(elementName).getAttributesGaugeMap();
				String srcAtt = "src" + sourceNumber.toString() + "hits";
				m_xmlWriter.writeAttribute(srcAtt, gsAttributesGaugeMap.get(attrName).getHits().toString());
				sourceNumber +=1;
			}
			m_xmlWriter.writeEndElement();
		}

		m_xmlWriter.writeEndElement();
	}*/

	public void close() throws XMLStreamException {
		writeJson();
		m_xmlWriter.writeEndElement(); //START ELEMENT
		m_xmlWriter.writeEndDocument();
		m_xmlWriter.flush();
		m_xmlWriter.close();
		LOGGER.info("GaugeWriter Closed");
	}

/*	public void writeContentAttrGauges(
			SortedMap<String, Integer> elementGaugeMap) {
		// TODO Auto-generated method stub
		
	}
	
	private void writeNotUsedElements(SortedMap<String, OdfElementGauge> gaugeMap) throws XMLStreamException {
		m_xmlWriter.writeStartElement(NOT_USED_TAG);

		for(String element : gaugeMap.keySet()) {
			OdfElementGauge elementGauge = gaugeMap.get(element);
			if (elementGauge.getHits().equals(0)) {
				m_xmlWriter.writeStartElement(ELEMENT_TAG);
				m_xmlWriter.writeAttribute(GAUGE_NAME_TAG, element);
				m_xmlWriter.writeEndElement();
			}
		}

		m_xmlWriter.writeEndElement();

	}*/
}
