package net.amham.odfe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLStreamException;

import net.amham.odfe.log.ODFELogger;
import net.amham.odfe.report.AggregateReport;
import net.amham.odfe.report.AggregationSummary;
import net.amham.odfe.report.DifferenceReport;
import net.amham.odfe.report.DocumentReport;
import net.amham.odfe.report.FilteredXPathGraph;
import net.amham.odfe.report.ProcessDepth;
import net.amham.odfe.report.ReportBase;
import net.amham.odfe.report.ReportBase.ProcessMode;
import net.amham.odfe.xmlwrite.ODFERun;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

/**
 * @author ian
 *
 */
/**
 * @author ian
 *
 */
public class ODFExplorer {
	
	private static final String FILTERS = "filters";
	private static final String EXTRACT = "extract";
	private static final String FILTERDOC = "doc";
	private static final String ATTSON = "attson";
	private static final String ATTRIBUTES_ON = "AttributesOn";
	private static final String GENERATE_COMMENTED_DIFF_REPORT = "GenerateCommentedDiffReport";
	private static final String LAST_RUN_PROPERTIES = "lastRun.properties";
	private static final String RECORDS_DIRECTORY = "recordsDirectory";
	private static final String MODE = "Mode";
	private static final String LEGEND = "Legend";
	private static final String COMMENT = "Comment";
	private static final String GAUGES_HITS_ONLY = "GaugeHitsOnly";
	private static final String XPATH_CHANGES_ONLY = "XPathChangesOnly";
	private static final String PROCESS_DEPTH = "ProcesDepth";
	private static final String EXTRACT_NAME = "ExtractTo";
	private static final String FILES = "Files";
	
	private static final String BROWSERCOMMAND ="BrowserCommand";
	
	private static final String RECORDS = "records";
	private static final String USER_DIR = "user.dir";
	private static final String ODFE_PROPERTIES = "odfe.properties";
	private static final int LOG_SIZE = 1000;
	private static final int LOG_ROTATION_COUNT = 10000;


	private final static   Logger LOGGER = Logger.getLogger(ODFExplorer.class.getName());
	
	//make this and then set its attributes
	private DocumentReport docReport;
	private Boolean includeLegend = false;
	
	private String browserCommandStr;
	
	/*Modes
	 * single document (which can be repeated) - default
	 * 		so create outputs for each document
	 * aggregate
	 * 		one set of outputs created in the base document's records directory
	 * diff 
	 * 		one set of outputs created in the base document's records directory
	 * 		hits noted for each
	 */
	private DocumentReport.ProcessMode mode = ReportBase.ProcessMode.SINGLE;
	
	private File recordsFile = null;
	private Properties props = null;
	private ODFERun odferuns;
	private boolean commentedDoc = false;
	private List<String> processFileNames;
	private String outputOverride = "";
	
	private static ODFExplorer odfe;
	private static List<String> filterList;
	
	public ODFExplorer() {
	    docReport = new DocumentReport();
	}
	
	public void init() {
		try {
			printVersion();
			ODFELogger.setup(Level.CONFIG);
		} catch (IOException e) {
			e.printStackTrace();
		}

		LOGGER.setLevel(Level.CONFIG);
		LOGGER.info("ODF Explorer created");
	}
	
    void printVersion()
    {
        try
        {         
            Enumeration<URL> resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
            while (resEnum.hasMoreElements()) {
                try {
                    URL url = (URL)resEnum.nextElement();
                    InputStream is = url.openStream();
                    if (is != null) {
                        Manifest manifest = new Manifest(is);
                        Attributes attributes = manifest.getMainAttributes();
                        String impTitle = attributes.getValue("Implementation-Title");
                        String impVersion = attributes.getValue("Implementation-Version");
                        String impBuildDate = attributes.getValue("Built-Date");

                        if (impTitle != null)
                        {
                            System.out.println(impTitle);
                        }            
                        if (impVersion != null)
                        {
                            System.out.println("Version: " + impVersion);
                        }
                        if (impBuildDate != null)
                        {
                            System.out.println("Built-Date: " + impBuildDate + "\n");
                        }
                    }
                }
                catch (Exception e) {
                    // Silently ignore wrong manifests on classpath?
                }
            }
            System.out.println("");
        } catch (IOException e1) {
            // Silently ignore wrong manifests on classpath?
        }
    }
    /**
	 * Create a ODFExplorer configure it from
	 * the properties file (create if does not exist)
	 * 
	 * Parse the command line to
	 * 	get the ODF documents to process - none supplied use a FileChooser
	 *  override the output location
	 *  set difference mode
	 * 		
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		OptionBuilder.withArgName("Y/N");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("include legend");
		Option incLegend = OptionBuilder.create("incLegend");
		
		OptionBuilder.withArgName("Y/N");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Produce a commented with a difference report");
		Option commentedDoc = OptionBuilder.create("commentedDoc");
		
		//OptionBuilder.withArgName("files");
		OptionBuilder.hasArgs();
		//OptionBuilder.hasArg(true);
		OptionBuilder.withValueSeparator(',');
		OptionBuilder.withDescription("Files to process");
		Option processFiles = OptionBuilder.create("f");
		
		OptionBuilder.withArgName("Node IDs");
		OptionBuilder.hasArgs();
		//OptionBuilder.hasArg(true);
		OptionBuilder.withValueSeparator(',');
		OptionBuilder.withDescription("XPath Graph Filters");
		Option filters = OptionBuilder.create(FILTERS);
		
		OptionBuilder.withArgName("name");
		OptionBuilder.hasArg();
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("document to filter");
		Option filterDoc = OptionBuilder.create(FILTERDOC);

		OptionBuilder.withArgName("name");
		OptionBuilder.hasArg();
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("extract directory");
		Option extractDir = OptionBuilder.create(EXTRACT);

		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Extract to fixed directory");
		Option extractTo = OptionBuilder.create("e");

		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Output path");
		Option outputTo = OptionBuilder.create("o");
		
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Supply a properties file");
		Option propertiesFile = OptionBuilder.create("p");
		
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Note/comment at top level");
		Option note = OptionBuilder.create("n");
		
		OptionBuilder.withArgName("name");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Generate aggregation summary");
		Option aggSummary = OptionBuilder.create("G");
		
		OptionBuilder.withArgName("Y/N");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("attributes on");
		
		
		Option attsOn = OptionBuilder.create(ATTSON);
		//Boolean options
		Option aggr = new Option("a", "aggregation mode");
		Option diff = new Option("d", "Diff mode");
		
		Option contentOnly = new Option("c", "Content data only");
		Option metaOnly = new Option("m", "Meta data only");
		Option stylesOnly = new Option("s", "Styles data only");
		
		Option hitGaugesOnly = new Option("g", "Gauges hit only");
		Option xpathChangesOnly = new Option("x", "XPath changes only");
		
		Option lastRun = new Option("l", "Use the last run properties");
		

		Option help = new Option("help", "display help");
		
		Options options = new Options();
		options.addOption(help);
		options.addOption(aggr);
		options.addOption(diff);
		
		options.addOption(contentOnly);
		options.addOption(metaOnly);
		options.addOption(stylesOnly);

		options.addOption(hitGaugesOnly);
		options.addOption(xpathChangesOnly); 
		options.addOption(lastRun);
		
		options.addOption(extractTo); 
		options.addOption(attsOn);
		options.addOption(filterDoc);
		options.addOption(extractDir);
		options.addOption(outputTo);
		options.addOption(filters);
		options.addOption(processFiles); //"f", "files", true, "ODF File(s)");
		options.addOption(propertiesFile);
		options.addOption(note);

		options.addOption(incLegend); //"i", "includeLegend", true, "Include Legend");
		options.addOption(extractTo);
		
		options.addOption(commentedDoc); 
		options.addOption(aggSummary); 

		odfe = new ODFExplorer();
		odfe.init();
		odfe.getGeneralProperties();
		
		try {
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(options, args, true);
			LOGGER.info("Command " + Arrays.asList(args));
			
			
			if(cmd.hasOption("help")) {
				HelpFormatter helpFormat = new HelpFormatter();
				helpFormat.printHelp( "ODFE", options );
				return;
			}
			
			
			if(cmd.getOptionValue(FILTERS) != null) {
				filterList = new LinkedList<String>(Arrays.asList(cmd.getOptionValues(FILTERS)));
				LOGGER.config("filter list" + filterList);
				
				//Set xpath changes only true - default false
				if(cmd.hasOption("x")) {
					//inject a 0 to show changes only
					filterList.add(0, new String("0"));
					LOGGER.config("show changes only");
				}
				//we must also have an document name and extract file
				//or we are not going to play... so there
				String docToFilter = cmd.getOptionValue(FILTERDOC);
				String extractDirName = cmd.getOptionValue(EXTRACT);
				LOGGER.config("Filter document: " + docToFilter);
				LOGGER.config("Extract: " + extractDirName);
				FilteredXPathGraph fxpg = new FilteredXPathGraph(docToFilter, extractDirName, filterList);
				fxpg.generate(odfe.getRecordsFile(), false);
				fxpg.close();
				return;
			}			

			List<String> fileNames = null;
			if (cmd.hasOption("l")) {
				LOGGER.config("Reading last run properties");
				fileNames = odfe.readProperties(LAST_RUN_PROPERTIES);
			} 
			
			LOGGER.config("Overrides");
			
			String propsArg = cmd.getOptionValue('p');
			if (propsArg != null) {
				fileNames = odfe.readProperties(propsArg);
			} 

			String noteArg = cmd.getOptionValue('n');
			if (noteArg != null) {
				odfe.addComment(noteArg);
			} 

			//override the output records location
			String outputName = cmd.getOptionValue("o");
			if(outputName != null) {
				LOGGER.config("Output to " + outputName);
				odfe.setOutputName(outputName);
			}
			
			if(cmd.getOptionValues("f") != null) {
				fileNames = Arrays.asList(cmd.getOptionValues("f"));
				LOGGER.info("Command option f returned" + fileNames);
			}
			else
			{
				LOGGER.info("Command option f returned null" + 	cmd.getOptionValues("f"));
			}

			//Difference the files
			//Not sure what will happen if more than two
			//should limit to two?
			if(cmd.hasOption("d")) {
				odfe.setDiffMode();
			}
			
			//Set gauges only true - default false
			if(cmd.hasOption("g")) {
				odfe.setGaugesHitOnly();
			}
			
			if(cmd.hasOption("incLegend")) {
				odfe.setIncludeLegend(cmd.getOptionValue("incLegend").toUpperCase().startsWith("Y") ? true : false);
			}
			
			if(cmd.hasOption("commentedDoc")) {
				odfe.genCommentedDoc(cmd.getOptionValue("commentedDoc").toUpperCase().startsWith("Y") ? true : false);
			}
			
			//Set gauges only true - default false
			String attsStr = cmd.getOptionValue(ATTSON);
			if(attsStr != null) {
				odfe.setAttributesOn(attsStr.toUpperCase().startsWith("Y") ? true : false);
			}
			
			
			//Don't use dated run directories - extract to defined
			if(cmd.hasOption("e")) {
				String extractToName = cmd.getOptionValue("e");
				odfe.setExtractFixed(extractToName);
			}
			
			//Only look at the styles.xml file
			if(cmd.hasOption("s")) {
				odfe.setProcessDepth(ProcessDepth.Styles);
			}
			
			//Only look at the content.xml file
			if(cmd.hasOption("c")) {
				odfe.setProcessDepth(ProcessDepth.Content);
			}
			
			//Only look at the meta.xml file
			if(cmd.hasOption("m")) {
				odfe.setProcessDepth(ProcessDepth.MetaData);
			}
			
			//Aggregate the files
			if(cmd.hasOption("a")) {
				odfe.setAggregateMode();
			}
			
			//Set gauges only true - default false
			if(cmd.hasOption("G")) {
				String aggregationName = cmd.getOptionValue("G");
				odfe.generateAggregationSummary(aggregationName);
				return;
			}
			
			//Set xpath changes only true - default false
			if(cmd.hasOption("x")) {
				odfe.setXPathChangestOnly();
			}

			if (fileNames != null && fileNames.size() > 0) {
				odfe.processFilesList(fileNames);
			} else {
				File[] files = null;
				List<File> filesArray = new ArrayList<File>();
				JFileChooser fc = new JFileChooser();
				// do we know the mode here?
				// single only open one
				// diff make two iterations
				// aggregate - select many files
				fc.setDialogTitle("Choose an ODF file");
				fc.setMultiSelectionEnabled(false);

				int iterations = 1;
				ProcessMode procmode = odfe.getDocReport().getProcessMode();
				switch (procmode) {
				case DIFF: {
					fc.setDialogTitle("Choose Difference ODF file ");
					iterations = 2;
					break;
				}
				case AGGREGATE: {
					iterations = 22;
					fc.setDialogTitle("Choose ODF file(s)");
					fc.setMultiSelectionEnabled(true);
					break;
				}
				}

				FileNameExtensionFilter ff = new FileNameExtensionFilter("ODF Files", "odt", "ods", "odp", "ott");
				fc.setFileFilter(ff);
				fc.setCurrentDirectory(new File(System.getProperty(USER_DIR)));

				int retval = JFileChooser.APPROVE_OPTION;

				int numChoosers = 0;
				while (iterations > numChoosers && retval == JFileChooser.APPROVE_OPTION) {
					retval = fc.showOpenDialog(fc);
					if (retval == JFileChooser.APPROVE_OPTION) {
						if (iterations < 22) {
							filesArray.add(fc.getSelectedFile());
						} else {
							files = fc.getSelectedFiles();
							for (File f : files) {
								filesArray.add(f);
							}
						}
						numChoosers++;
					}
				}
				odfe.processFiles(filesArray);
				LOGGER.fine("File Chooser closed");
			}

		} catch (ParseException e) {
			LOGGER.severe(e.getMessage());
			HelpFormatter helpFormat = new HelpFormatter();
			helpFormat.printHelp( "TBD", options );
		}
	}

	private void generateAggregationSummary(String name) {
		AggregationSummary summary = new AggregationSummary(name);
		summary.generate(getRecordsFile());
	}

	private void setOutputName(String outputName) {
		outputOverride = outputName;	
	}

	private File getRecordsFile() {
		return recordsFile;
	}

	private void updateProcessedDocs() throws IOException {
		//	would be good to have the info on report type etc?
		
		File modeDir = docReport.getModeDirectory();
		File[] directories = modeDir.listFiles(new FilenameFilter() {
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
		});
		FileWriter docFiles = new FileWriter(new File(modeDir, "docs.json"));
		docFiles.write("{\"docs\":[");
		int numEntries = directories.length;
		if(numEntries > 0)
		{
			docFiles.write("\n{\"name\": " + "\"" + directories[0].getName() + "\",");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date(directories[0].lastModified());
			String modDate = dateFormat.format(date);
			docFiles.write("\n\"lastMod\": " + "\"" + modDate + "\",");
			
			File[] runs = directories[0].listFiles(new FilenameFilter() {
				  public boolean accept(File current, String name) {
				    return new File(current, name).isDirectory();
				  }
			});
			docFiles.write("\n\"runs\": " + runs.length );

			int ndx = 1 ;
			while(ndx < numEntries) {
					docFiles.write("},\n{\"name\": " + "\"" + directories[ndx].getName() + "\",");
					dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					date = new Date(directories[ndx].lastModified());
					modDate = dateFormat.format(date);
					docFiles.write("\n\"lastMod\": " + "\"" + modDate + "\",");
					runs = directories[ndx].listFiles(new FilenameFilter() {
						  public boolean accept(File current, String name) {
						    return new File(current, name).isDirectory();
						  }
					});
					docFiles.write("\n\"runs\": " + runs.length );

					ndx++;
			}
			docFiles.write("}\n");
		}
	
		docFiles.write("]}");
		docFiles.close();
	}

	public void setAttributesOn(boolean b) {
		LOGGER.config("Attributes On " + new Boolean(b).toString());
		docReport.setAttributesOn(b);
	}

	public void setXPathChangestOnly() {
		LOGGER.config("Set XPath Changes Only");
		docReport.setXPathChangesOnly();
	}

	public void setGaugesHitOnly() {
		LOGGER.config("Set Gauges Hit Only");
		docReport.setHitsOnly(true);
	}

	public void setExtractFixed(String extractDir) {
		LOGGER.config("Set Extract Dir " + extractDir);
		docReport.setExtractName(extractDir);
	}
	

	/**
	 * @throws IOException
	 * @throws FileNotFoundExceptiongaugesWriter.isOpen()
	 */
	private void getGeneralProperties() throws IOException, FileNotFoundException {
		String runDir = System.getProperty(USER_DIR);
		props = new Properties();
		File runFile = new File(runDir);
		File propsFile = new File(runFile, ODFE_PROPERTIES);
		recordsFile = new File(runFile, RECORDS);
		try {
		       //load a properties file from class path, inside static method
			props.load(new FileReader(propsFile));
		}
		catch (IOException e) {
			LOGGER.config("Generating properties file " + propsFile.getAbsolutePath());
			props.setProperty(RECORDS_DIRECTORY, recordsFile.getAbsolutePath());
			props.setProperty(BROWSERCOMMAND, "");
			props.store(new FileOutputStream(propsFile), "Created at startup");
		}
		//this is the one to use
		recordsFile = new File(props.getProperty(RECORDS_DIRECTORY));
		LOGGER.config("Records directory: " + recordsFile.getAbsolutePath());
		
		String recordDates = props.getProperty("RecordDates");
		if(recordDates != null) {
			if (recordDates.equals("N")) {
				LOGGER.config("Not recording run dates");
				docReport.setExtractName("OdfeData");
			}
		}
		if(!recordsFile.exists()) {
			recordsFile.mkdir();
			LOGGER.config("Records directory created");
		}
		
		browserCommandStr = props.getProperty(BROWSERCOMMAND);
		LOGGER.config("Browswer Command: " + browserCommandStr);
	}
	
	/**
	 * Iterate the files array and process each
	 * @param filesArray
	 * @throws IOException 
	 */
	private void processFiles(List<File> filesArray) throws IOException {
		processFileNames = new ArrayList<String>();
        for (File file : filesArray) {
            LOGGER.info("Processing: " + file.getAbsolutePath());
			processFileNames.add(file.getAbsolutePath());
            processFile(file);
        }
		// update the processed documents list
        updateProcessedDocs();
        
		close();
	}

	/**
	 * A commented Doc is a feature of a difference report
	 * should not be here?
	 * /TODO
	 * Also don't do this if we are running behind a node.js http server
	 * @param diffs
	 */
	private void checkGenerateCommentDoc(int diffs) {
		if (commentedDoc == true) {
			int option = 0;
			if (diffs == 0) {
				JOptionPane.showMessageDialog(null, "No differences found");
			} else {
				Object[] options = { "Yes, please", "No thanks" };
				option = JOptionPane.showOptionDialog(null, "Differences found see "
						+ docReport.getCommentedDocument().getAbsolutePath() + " for details]n Open Now?", "Differences",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, // the
																								// titles
																								// of
																								// buttons
						options[1]); // default button title

				if (option == 0) {
					CommandRunner runner = new CommandRunner();
					String command = "soffice " + docReport.getCommentedDocument().getAbsolutePath();
					try {
						// this needs to be done in the extract directory
						runner.run(command, docReport.getCommentedDocument().getParentFile());
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Iterate the array of filenames and process each
	 * @param fileNames
	 * @throws IOException 
	 */
	public void processFilesList(List<String> fileNames) 
	{
		LOGGER.info("Files:" + fileNames.toString());
		processFileNames = fileNames;
        for (String file : fileNames) {
            LOGGER.info("Opening: " + file);
            //processFileNames.add(file);
            processFile(new File(file));
        }
        try {
			updateProcessedDocs();
		} catch (IOException e) {
			e.printStackTrace();
		}
        close();
		return;
	}

	/**
	 * @return
	 */
	private void close() {
        docReport.close();
        
        //this only makes sense for a difference report?
   	 	//TODO move me
		int diffs = docReport.getNumDiffs();
		checkGenerateCommentDoc(diffs);
		
   	 	//TODO move me
		//this only makes sense if running from command line
		//not behind a node.js server
		if(browserCommandStr != null && browserCommandStr.length() > 0) {
			docReport.startBrowser(browserCommandStr);
		}
		
		try {
			saveProperties();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
	
	
	/**
	 * Create a DocumentProcessor if not already available
	 * Configure it from the values passed in
	 * And process the document
	 * 
	 * If the DocumentProcessor already exists then additional 
	 * files are passed to it via the setFile method.
	 * Which depending on the settings will be aggregated or differenced.
	 * 
	 * @param odfFile
	 */
	private void processFile(File odfFile) {

		try {
			docReport.generate(odfFile, recordsFile);
		} catch (Exception e) {
			LOGGER.warning("Processing Failed for file " + odfFile.toString() + " " + e.getMessage());
		}
	}
	
	public void setProcessDepth(ProcessDepth pd) {
		LOGGER.config("Set Process Depth " + pd.toString());
		docReport.setProcessDepth(pd);
	}

	public Boolean getIncludeLegend() {
		return docReport.getIncludeLegend();
	}

	public void setIncludeLegend(Boolean includeLegend) {
		LOGGER.config("Set Include Legend " + includeLegend.toString());
		this.includeLegend = includeLegend; 
		docReport.setIncludeLegend(includeLegend);
	}
	
	/**
	 * Generate a Difference Report
	 */
	public void setDiffMode() {
		LOGGER.config("Difference Mode");
		String extractDir = docReport.getExtractName();
		String comment = docReport.getComment();
		docReport = new DifferenceReport();
		docReport.setIncludeLegend(includeLegend);
		if(extractDir.length() > 0)
			docReport.setExtractName(extractDir);
		docReport.addComment(comment);
	}

	/**
	 * Generate an Aggregate Report
	 */
	public void setAggregateMode() {
		LOGGER.config("Aggregate Mode");
		//in case already set 
		String extractDir = docReport.getExtractName();
		String comment = docReport.getComment();
		docReport = new AggregateReport();
		if(extractDir.length() > 0) {
			docReport.setExtractName(extractDir);
		}
		if (outputOverride.length() > 0 )
			docReport.setOutputName(outputOverride);
		docReport.addComment(comment);
	}

	/**
	 * Override the records directory
	 * Intended for testing
	 * @param recordsFile
	 */
	public void setRecordsFile(String recordsTo) {
		LOGGER.config("Records to "+ recordsTo);
		this.recordsFile = new File(recordsTo);
	}

	public void genCommentedDoc(boolean genDoc) {
		LOGGER.config("Genereate Commented Document " + new Boolean(genDoc).toString());
		docReport.setGenerateCommentedDoc(genDoc);
		commentedDoc = genDoc;
	}

	public DocumentReport getDocReport() {
		return docReport;
	}
	
	public void addComment(String cmnt) {
		LOGGER.config("Note " + cmnt);
		docReport.addComment(cmnt);
	}

	/**
	 * @throws IOException
	 * @throws FileNotFoundExceptiongaugesWriter.isOpen()
	 */
	public List <String> readProperties(String propsName) throws IOException, FileNotFoundException {
		String runDir = System.getProperty(USER_DIR);
		props = new Properties();
		File runFile = new File(runDir);
		File propsFile = new File(runFile, propsName);
		try {
		       //load a properties file from class path, inside static method
			props.load(new FileReader(propsFile));
		}
		catch (IOException e) {
			LOGGER.config("Generating properties file " + propsFile.getAbsolutePath());
			props.setProperty(RECORDS_DIRECTORY, recordsFile.getAbsolutePath());
			props.store(new FileOutputStream(propsFile), "Created at startup");
		}
		//this is the one to use
		recordsFile = new File(props.getProperty(RECORDS_DIRECTORY));
		LOGGER.config("Records directory: " + recordsFile.getAbsolutePath());
		
		if(!recordsFile.exists()) {
			recordsFile.mkdir();
			LOGGER.config("Records directory created");
		}

		String mode = props.getProperty(MODE);
		if (mode != null) {
			// we only first character
			mode = mode.toLowerCase();
			switch (mode.charAt(0)) {
			case 'a':
				odfe.setAggregateMode();
				break;
			case 'd':
				odfe.setDiffMode();
				break;
			default: //default mode is Single 
				break;

			}
		}
		String legend = props.getProperty(LEGEND);
		if (legend!= null) {
			legend = legend.toUpperCase();
			odfe.setIncludeLegend(legend.equals("Y") ? true : false);
		}
		String genComDoc = props.getProperty(GENERATE_COMMENTED_DIFF_REPORT);
		if (genComDoc!= null) {
			genComDoc = genComDoc.toUpperCase();
			odfe.genCommentedDoc(genComDoc.equals("Y") ? true : false);
		}
		String comment = props.getProperty(COMMENT);
		if (comment!= null) {
			docReport.addComment(comment);
		}
		String hitsOnly = props.getProperty(GAUGES_HITS_ONLY);
		if (hitsOnly!= null) {
			hitsOnly = hitsOnly.toUpperCase();
			if(hitsOnly.equals("Y")) {
				odfe.setGaugesHitOnly();
			}
		}
		String noAtts = props.getProperty(ATTRIBUTES_ON);
		if (noAtts!= null) {
			noAtts = noAtts.toUpperCase();
			if(noAtts.equals("Y")) {
				odfe.setAttributesOn(true);
			} else {
				odfe.setAttributesOn(false);
			}
		}
		String xpathChangesOnly = props.getProperty(XPATH_CHANGES_ONLY);
		if (xpathChangesOnly!= null) {
			xpathChangesOnly = xpathChangesOnly.toUpperCase();
			if(xpathChangesOnly.equals("Y")) {
				odfe.setXPathChangestOnly();
			}
		}
		String processDepth = props.getProperty(PROCESS_DEPTH);
		if (processDepth != null) {
			processDepth = processDepth.toLowerCase();
			// we only first character
			switch (processDepth.charAt(0)) {
			case 'c':
				odfe.setProcessDepth(ProcessDepth.Content);
				break;
			case 's':
				odfe.setProcessDepth(ProcessDepth.Styles);
				break;
			case 'm':
				odfe.setProcessDepth(ProcessDepth.MetaData);
				break;
			case 'a':
				odfe.setProcessDepth(ProcessDepth.ALL);
				break;
			default:
				break;
			}
		}
		String extractTo = props.getProperty(EXTRACT_NAME);
		if (extractTo != null && extractTo.length() > 0) {
			docReport.setExtractName(extractTo);
		}
		else {
			LOGGER.config("Use dated extracts");
		}
		
		String files = props.getProperty(FILES);
		return getTrimmedFileNames(files);
	}

	/**
	 * @param files
	 * @param fileNames
	 */
	private List <String>  getTrimmedFileNames(String files) {
		List <String> fileNames = new ArrayList<String>();
		if (files != null) {
			String[] splits = files.split(","); 
			if (splits[0].length() > 0){
				for(String name : splits) {
					name.trim();
					fileNames.add(name);
				}
			}
		}
		return fileNames;
	}
	
	private void saveProperties() throws FileNotFoundException, IOException
	{
		String runDir = System.getProperty(USER_DIR);
		props = new Properties();
		File runFile = new File(runDir);
		File propsFile = new File(runFile, LAST_RUN_PROPERTIES);
		LOGGER.info("Saving last run properties file " + propsFile.getAbsolutePath());
		props.setProperty(RECORDS_DIRECTORY, recordsFile.getAbsolutePath());
		
		if (docReport.isFixedDateMode()) {
		}

		switch (docReport.getProcessDepth()) {
			case Content:
				props.setProperty(PROCESS_DEPTH, "Content");
				break;
			case Styles:
				props.setProperty(PROCESS_DEPTH, "Styles");
				break;
			case MetaData:
				props.setProperty(PROCESS_DEPTH, "Metadata");
				break;
			case ALL:
				props.setProperty(PROCESS_DEPTH, "All");
				break;
			default:
				break;
		}
		if (docReport.getIncludeLegend()) {
			props.setProperty(LEGEND, "Y");
		}
		else {
			props.setProperty(LEGEND, "N");
		}
		
		props.setProperty(EXTRACT_NAME, docReport.getExtractName());
		
		props.setProperty(COMMENT, docReport.getComment());
		
		if (docReport.getGenerateCommentedDoc()) {
			props.setProperty(GENERATE_COMMENTED_DIFF_REPORT, "Y");
		}
		else {
			props.setProperty(GENERATE_COMMENTED_DIFF_REPORT, "N");
		}

		if (docReport.istHitsOnly()) {
			props.setProperty(GAUGES_HITS_ONLY, "Y");
		} else {
			props.setProperty(GAUGES_HITS_ONLY, "N");
		}
		if (docReport.isXPathChangesOnly()) {
			props.setProperty(XPATH_CHANGES_ONLY, "Y");
		} else {
			props.setProperty(XPATH_CHANGES_ONLY, "N");
		}
		
		if (docReport.isAttributesOn()) {
			props.setProperty(ATTRIBUTES_ON, "Y");
		} else {
			props.setProperty(ATTRIBUTES_ON, "N");
		}
		
		switch (docReport.getProcessMode()) {
			case SINGLE:
				props.setProperty(MODE, "Single");
				break;
			case DIFF:
				props.setProperty(MODE, "Difference");
				break;
			case AGGREGATE:
				props.setProperty(MODE, "Aggregate");
				break;
			default:
				break;
		}
		String extractTo = props.getProperty(EXTRACT_NAME);
		if (extractTo != null) {
			extractTo.toUpperCase();
			
		}
		StringBuilder filesStringBuilder = new StringBuilder();
		boolean first = true;
		for(String file : processFileNames) {
			if(first == false) {
				filesStringBuilder.append(',');
			}
			else {
				first = false;
			}
			filesStringBuilder.append(file);
		}
		props.setProperty(FILES, filesStringBuilder.toString());
		props.store(new FileOutputStream(propsFile), "Last Run");
	}
	
}
