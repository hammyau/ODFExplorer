package net.amham.odfe.json;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class JsonODFERuns {
	
	private static final String ODFERUNS_JSON = "odferuns.json";

	private final static   Logger LOGGER = Logger.getLogger(JsonODFERuns.class.getName());
	
	private File runsFile;
	
	private JsonGenerator generator;
	private ObjectNode rootNode;
	private ArrayNode runsArray;

	private ObjectNode newRun;

	private JsonFactory jFactory;

	private ObjectMapper mapper;
	//pass in the doc or the docname
	public JsonODFERuns(File runsDir) throws IOException {
	
		runsFile = new File(runsDir, ODFERUNS_JSON);
		//does the document already exist
		jFactory = new JsonFactory();
		mapper = new ObjectMapper();
	}
	
	public void open() {
		if(runsFile.exists() == false)	{
			//easy create it and write to it
			try {
				generator = jFactory.createJsonGenerator(new FileWriter(runsFile));
				mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
				generator.setCodec(mapper);
				generator.useDefaultPrettyPrinter();

				rootNode = mapper.createObjectNode();
				runsArray = rootNode.putArray("runs");
				
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		else {
			read();
			syncToDirectory();
		}
		newRun = runsArray.addObject();
	}
	
	public void read() {
		if(runsFile.exists())	{
			JsonParser jParser;
			try {
				jParser = jFactory.createJsonParser(runsFile);
				jParser.setCodec(mapper);
				rootNode = (ObjectNode) jParser.readValueAsTree();
				if(rootNode != null){
					System.out.println(rootNode.toString());
					runsArray = (ArrayNode) rootNode.findValue("runs");
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Read the runs directory for this document
	 * remove any deleted directories from the runsArray
	 * 
	 *   What about any runs that are there an have not been added
	 *   this should not happen but we TODO could/should crosscheck?
	 */
	private void syncToDirectory() {

		String[] directories = runsFile.getParentFile().list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		List<String> dirList = Arrays.asList(directories);
		
		//if we get an iterator to the JSON array we can remove as we go?
		Iterator<JsonNode> runsIterator = runsArray.iterator();
		while(runsIterator.hasNext()){
			JsonNode obj = runsIterator.next();
			if(obj.isObject()) {
				String runName = ((ObjectNode)obj).get("extract").asText();
				if( dirList.contains(runName) == false) {
					//need remove this one
					runsIterator.remove();
					System.out.println("Removed " +  runName);
				}
			} else {
				System.out.println("Not an object" +  obj.toString());
			}
		}

		//TODO - ditch this debug and above when we are happy it is all working as desired
		System.out.println(rootNode.toString());
		
	}

	public void writeCreated(String created) {
		newRun.put("created", created);
	}

	public void writeDocname(String docname) {
		newRun.put("docName", docname);
	}
	
	public void close() throws IOException {
		if(generator!= null && generator.isClosed() == false)
		{
			try {
				generator.writeTree(rootNode);
				generator.close(); 
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			LOGGER.info("JSON Runs Writer Closed");
		} else {
			if(rootNode != null) { //come through here when there is no existing file
				generator = jFactory.createJsonGenerator(new FileWriter(runsFile));
				mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
				generator.setCodec(mapper);
				generator.useDefaultPrettyPrinter();
				generator.writeTree(rootNode);
				generator.close(); 
				
				LOGGER.info("JSON Runs Written and closed");
			}
		}
	}

	public void writeExtract(File extractDir) {
		newRun.put("extract", extractDir.getName());
	}

	public void writeStats(int numPaths, int minDepth, int maxDepth, int avgDepth) {
		newRun.put("numPaths", numPaths);
		newRun.put("minDepth", minDepth);
		newRun.put("maxDepth", maxDepth);
		newRun.put("avgDepth", avgDepth);
	}

	public void writeComment(String comment) {
		newRun.put("comment", comment);
	}

	public void writeMode(String mode) {
		newRun.put("mode", mode);
	}

	public void writeProcessDepth(String depth) {
		newRun.put("process", depth);
	}	
	
	public void writeNoAttributes(boolean attributesOn) {
		newRun.put("atts", attributesOn);
	}

	public void writeXpathChangesOnly(boolean xPathChangesOnly) {
		newRun.put("xpathChangesOnly", xPathChangesOnly);
	}

	public String getLastRun() {
		String last = "";
		if(runsArray != null && runsArray.size() > 0) {
			JsonNode run = runsArray.get(runsArray.size()-1);
			last = run.get("extract").asText();
		}
		return last;
	}
}
