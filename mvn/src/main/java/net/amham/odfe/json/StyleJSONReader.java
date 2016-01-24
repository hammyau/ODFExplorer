package net.amham.odfe.json;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;

public class StyleJSONReader {

	private static final String USER_DIR = "user.dir";
	private static File file;
	private static Integer propDiffCount = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			JsonFactory jfactory = new JsonFactory();

	    	JFileChooser fc = new JFileChooser();

			fc.setDialogTitle("Choose JSON file(s)");
			FileNameExtensionFilter ff = new FileNameExtensionFilter("JSON Files", "json");
			fc.setFileFilter(ff);
			fc.setCurrentDirectory(new File(System.getProperty(USER_DIR)));
			fc.setMultiSelectionEnabled(true);
			
			int retval = fc.showOpenDialog(fc);
			
			while (retval == JFileChooser.APPROVE_OPTION) {
				//can make the file chooser select multiple files
				// can they be in different directories?
	            file = fc.getSelectedFile();

				retval = fc.showOpenDialog(fc);
	        }

			/*** read from file ***/
			JsonParser jParser = jfactory.createJsonParser(file);

			// loop until token equal to "}"
			while (jParser.nextToken() != JsonToken.END_OBJECT) {

				String fieldname = jParser.getCurrentName();
				if ("name".equals(fieldname)) {

					// current token is "name",
					// move to next, which is "name"'s value
					jParser.nextToken();
//					System.out.println(jParser.getText());

				}
				if ("children".equals(fieldname)) {
					 
					  jParser.nextToken(); // current token is "[", move next
			 
					  // messages is array, loop until token equal to "]"
					  while (jParser.nextToken() != JsonToken.END_ARRAY) {
			 
							String fieldname1 = jParser.getCurrentName();
							if ("name".equals(fieldname1)) {

								// current token is "name",
								// move to next, which is "name"'s value
								jParser.nextToken();
								String val = jParser.getText();
								System.out.println(val); // display mkyong
								if(val.equals("families")) {
//									System.out.println("Yippee");
									findDiffChildren(jParser);
								}
								if(val.equals("sources")) {
//									System.out.println("Skipping sources");
									findSkipChildren(jParser);
								}

							}

					  }
				}
			}
			System.out.println("Property Diffs found " + propDiffCount);

			jParser.close();

		} catch (JsonGenerationException e) {

			e.printStackTrace();

		} catch (JsonMappingException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

	/**
	 * @param jParser
	 * @param fieldname1
	 * @throws IOException
	 * @throws JsonParseException
	 */
	public static void findSkipChildren(JsonParser jParser) throws IOException, JsonParseException {
		while (jParser.nextToken() != JsonToken.END_OBJECT) {

			String fieldname = jParser.getCurrentName();
			if (fieldname != null && fieldname.contains("children")) {
			 
//				System.out.println("Skipping children");
				jParser.nextToken(); // current token is "[", move next
				jParser.skipChildren();
			}
		}
	}

	public static void findDiffChildren(JsonParser jParser) throws IOException, JsonParseException {
		while (jParser.nextToken() != JsonToken.END_OBJECT) {

			String fieldname = jParser.getCurrentName();
			if (fieldname != null && fieldname.contains("children")) {
//				System.out.println("found children");
				jParser.nextToken(); // current token is "[", move next
				findDiffNode(jParser);
			}
		}
	}

	private static void findDiffNode(JsonParser jParser) throws JsonParseException, IOException {
		String nodeName = null;
		while (jParser.nextToken() != JsonToken.END_ARRAY) {

			String fieldname1 = jParser.getCurrentName();
			if ("name".equals(fieldname1)) {
				jParser.nextToken(); 
				nodeName = jParser.getText();
			}
			if ("hits".equals(fieldname1)) {
				jParser.nextToken(); //the [
				jParser.skipChildren();
				System.out.println("Node " + nodeName + " hit");
			}
			if ("diff".equals(fieldname1)) {
				jParser.nextToken(); 
				String diff = jParser.getText();
				if(!"SameStyle".equals(diff)) {
					System.out.println("found diff node " + nodeName + ":" + diff);
					if(nodeName.contains("Properties")) {
						findDiffProperties(jParser);
					}
					else {
						findDiffChildren(jParser);
					}
				}
				else {
//					System.out.println("Skipping node " + nodeName);
					findSkipChildren(jParser);
				}
			}
			if (fieldname1 != null && fieldname1.contains("children")) {
//				System.out.println("Skipping children of " + nodeName);
				jParser.nextToken(); // current token is "[", move next
				jParser.skipChildren();
			}
		}

	}

	private static void findDiffProperties(JsonParser jParser) throws JsonParseException, IOException {
		while (jParser.nextToken() != JsonToken.END_OBJECT) {

			String fieldname = jParser.getCurrentName();
			if (fieldname != null && fieldname.contains("children")) {
//				System.out.println("iterating children");
				jParser.nextToken(); // current token is "[", move next
				findDiffProp(jParser);
			}
		}
	}

	private static void findDiffProp(JsonParser jParser) throws JsonParseException, IOException {
		String propName = null;
		String val1 = null;
		String val2 = null;
		while (jParser.nextToken() != JsonToken.END_ARRAY) {

			String fieldname = jParser.getCurrentName();
			if ("name".equals(fieldname)) {
//				System.out.println("iterating children");
				jParser.nextToken(); 
				propName = jParser.getText();
			}
			if ("value1".equals(fieldname)) {
//				System.out.println("Diff property");
				jParser.nextToken(); 
				val1 = jParser.getText();
			}
			if ("value2".equals(fieldname)) {
				jParser.nextToken(); 
				val2 = jParser.getText();
				System.out.println("Diff " + propName + " " + val1 + " -> " + val2);
				propDiffCount++;
			}
		}
		
	}

}
