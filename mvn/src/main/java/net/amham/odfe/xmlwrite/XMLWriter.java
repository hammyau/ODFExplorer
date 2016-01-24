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
public class XMLWriter {
	
	private final static   Logger LOGGER = Logger.getLogger(XMLWriter.class.getName());
	
	private String ODFE_XSL = "xml-stylesheet type='text/xsl' href='";
	
	private static final String START_TAG = "odfepaths";
	private XMLOutputFactory m_xof =  XMLOutputFactory.newInstance();

	private XMLStreamWriter m_xmlWriter;
	
	
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
	public void open(File file, String xslFileName) throws XMLStreamException, IOException {
		m_xmlWriter = m_xof.createXMLStreamWriter(new FileWriter(file));
		LOGGER.info("Open " + file.getAbsolutePath());
		m_xmlWriter.writeStartDocument();
		ODFE_XSL += "../../../xsl/" + xslFileName + "'";
		LOGGER.fine("Style sheet from " + ODFE_XSL);
		m_xmlWriter.writeProcessingInstruction(ODFE_XSL);
		m_xmlWriter.writeStartElement(START_TAG);
	}
	
	
	public void writeJson(String jsonFileName) throws XMLStreamException {
		m_xmlWriter.writeStartElement("jsonfile");
		m_xmlWriter.writeCharacters(jsonFileName);
		m_xmlWriter.writeEndElement(); //SOURCES

	}
	

	public void close() throws XMLStreamException {
		m_xmlWriter.writeEndElement(); //START ELEMENT
		m_xmlWriter.writeEndDocument();
		m_xmlWriter.flush();
		m_xmlWriter.close();
	}


	public void writeNumPaths(int numPaths) throws XMLStreamException {
		m_xmlWriter.writeStartElement("numpaths");
		m_xmlWriter.writeCharacters(new Integer(numPaths).toString());
		m_xmlWriter.writeEndElement();
	}

	public void writeMinDepth(int minDepth) throws XMLStreamException {
		m_xmlWriter.writeStartElement("mindepth");
		m_xmlWriter.writeCharacters(new Integer(minDepth).toString());
		m_xmlWriter.writeEndElement();
	}


	public void writeMaxDepth(int maxDepth) throws XMLStreamException {
		m_xmlWriter.writeStartElement("maxdepth");
		m_xmlWriter.writeCharacters(new Integer(maxDepth).toString());
		m_xmlWriter.writeEndElement();
	}


	public void writeAvgDepth(int avgDepth) throws XMLStreamException {
		m_xmlWriter.writeStartElement("avgdepth");
		m_xmlWriter.writeCharacters(new Integer(avgDepth).toString());
		m_xmlWriter.writeEndElement();
	}

	public void addComment(String comment) throws XMLStreamException {
		m_xmlWriter.writeStartElement("comment");
		m_xmlWriter.writeCharacters(comment);
		m_xmlWriter.writeEndElement();
	}




}
