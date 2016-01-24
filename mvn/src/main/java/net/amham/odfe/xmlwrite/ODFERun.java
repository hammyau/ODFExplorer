package net.amham.odfe.xmlwrite;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class ODFERun {
	
	private File runsFile;
	private XMLEventFactory m_eventFactory = XMLEventFactory.newInstance();
	
	private XMLOutputFactory m_xof =  XMLOutputFactory.newInstance();
	private XMLStreamWriter m_xmlWriter;
	
	private String ODFE_XSL = "xml-stylesheet type='text/xsl' href='";
	private static final String RUNS_TAG = "runs";
	private static final String RUNDIR_TAG = "rundir";
	private static final String DOC_TAG = "docname";
	private static final String CREATED_TAG = "created";
	private static final String DIR_TAG = "directory";
	
	//pass in the doc or the docname
	public ODFERun(File runsDir) throws XMLStreamException, IOException {
	
		runsFile = new File(runsDir, "odferuns.xml");
		m_xmlWriter = m_xof.createXMLStreamWriter(new FileWriter(runsFile));
		m_xmlWriter.writeStartDocument();
		ODFE_XSL += "../../xsl/odferesults.xsl'";
		m_xmlWriter.writeProcessingInstruction(ODFE_XSL);
		m_xmlWriter.writeStartElement(RUNS_TAG);
	}
	
	/**
	 * Create or update the document run details
	 * @throws XMLStreamException 
	 */
	public void writeDirs() throws XMLStreamException{
		String[] directories = runsFile.getParentFile().list(new FilenameFilter() {
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
		});
		
		for(String dir : directories) {
			m_xmlWriter.writeStartElement(RUNDIR_TAG);
			m_xmlWriter.writeCharacters(dir);
			
			//Also want the stats... 
			//	comment for test name
			//	num paths
			// min depth
			// max depth
			
			//may this should all be at the level above too
			//or so we can drill into it
			m_xmlWriter.writeEndElement();
		}
		
		m_xmlWriter.writeEndElement();
	}
	
	public void writeCreated(String created) throws XMLStreamException {
		m_xmlWriter.writeStartElement(CREATED_TAG);
		m_xmlWriter.writeCharacters(created);
		m_xmlWriter.writeEndElement();
	}

	public void writeDocname(String docname) throws XMLStreamException {
		m_xmlWriter.writeStartElement(DOC_TAG);
		m_xmlWriter.writeCharacters(docname);
		m_xmlWriter.writeEndElement();
	}
	public void close() throws XMLStreamException {
		m_xmlWriter.writeEndDocument();
		m_xmlWriter.close();
	}

	public void writeDirName(String dir) throws XMLStreamException {
		m_xmlWriter.writeStartElement(DIR_TAG);
		m_xmlWriter.writeCharacters(dir);
		m_xmlWriter.writeEndElement();
	}
}
