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

import net.amham.odfe.gauges.OdfGaugeStore;


/**
 * Write out the gauge values to an XML file
 * 
 * 
 * @author ian
 *
 */
public class ResultsXMLWriter {

	private final static   Logger LOGGER = Logger.getLogger(ResultsXMLWriter.class.getName());
	
	private String ODFE_XSL = "xml-stylesheet type='text/xsl' href='";
	
	private static final String START_TAG = "odfexplorer";
	private static final String SOURCES_TAG = "sources";
	private static final String SOURCE_TAG = "source";
	private static final String SOURCE_NUMBER_TAG = "source-number";
	private static final String DOC_TAG = "docname";
	private static final String RUN_TAG = "rundate";

	private static final String MANIFEST_TAG = "manifest";
	private static final String MANIFEST_FILE_TAG = "file";

	private static final String METAVALUES_TAG = "metaValues";
	private static final String CREATED_TAG = "created";
	private XMLEventFactory m_eventFactory = XMLEventFactory.newInstance();
	
	private XMLOutputFactory m_xof =  XMLOutputFactory.newInstance();

	private XMLStreamWriter m_xmlWriter;
	
	private Vector<OdfGaugeStore> gauges = new Vector<OdfGaugeStore>();
	
	private String created;
	private String docname;
	
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
		ODFE_XSL += "../../../xsl/odferesults.xsl'";
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
	
	public void close() throws XMLStreamException {
		m_xmlWriter.writeEndElement(); //START ELEMENT
		m_xmlWriter.writeEndDocument();
		m_xmlWriter.flush();
		m_xmlWriter.close();
		LOGGER.info("ResultsXMLWriter Closed");
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) throws XMLStreamException {
		m_xmlWriter.writeStartElement(CREATED_TAG);
		m_xmlWriter.writeCharacters(created);
		m_xmlWriter.writeEndElement();
		this.created = created;
	}

	public String getDocname() {
		return docname;
	}

	public void setDocname(String docname) throws XMLStreamException {
		m_xmlWriter.writeStartElement(DOC_TAG);
		m_xmlWriter.writeCharacters(docname);
		m_xmlWriter.writeEndElement();
		this.docname = docname;
	}

}
