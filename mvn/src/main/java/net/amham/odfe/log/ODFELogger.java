package net.amham.odfe.log;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class ODFELogger {
  private static   Logger logger=null;

	
  static private FileHandler fileTxt;
  static private LogFormatter formatterTxt;
  static private ConsoleLogFormatter conFormatter;

  static private FileHandler fileHTML;
  static private Formatter formatterHTML;
  static private ConsoleHandler ch;

  static public void setup(Level logLevel) throws IOException {

    // Get the global logger to configure it
//	LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.INFO); 
    logger = Logger.getLogger("");

    Handler[] handlers = logger.getHandlers();
    for(Handler handler : handlers) {
        logger.removeHandler(handler);
    }
  
    logger.setLevel(logLevel);
    fileTxt = new FileHandler("ODFExplorerLog.log");
//    fileHTML = new FileHandler("ODFExplorerLog.html");

    // Create txt Formatter
    formatterTxt = new LogFormatter();
    fileTxt.setFormatter(formatterTxt);
    logger.addHandler(fileTxt);

    // Create HTML Formatter
//    formatterHTML = new HtmlFormatter();
//    fileHTML.setFormatter(formatterHTML);
//    logger.addHandler(fileHTML);
    
    ch = new ConsoleHandler();
    conFormatter = new ConsoleLogFormatter();
    ch.setFormatter(conFormatter);
    ch.setLevel(logLevel);
    logger.addHandler(ch);

  }
} 