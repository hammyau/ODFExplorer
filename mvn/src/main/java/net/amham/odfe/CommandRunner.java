package net.amham.odfe;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;



public final class CommandRunner {
	private final static   Logger LOGGER = Logger.getLogger(CommandRunner.class.getName());
	
	private StringBuffer cmdOutput = new StringBuffer();

	public StringBuffer getCmdOutput() {
		return cmdOutput;
	}

	private class ProcessHandler extends Thread {

		InputStream inpStr;
		
		public ProcessHandler(InputStream inpStr, String strType) {
			this.inpStr = inpStr;
		}

		public void run() {
			try {
				InputStreamReader inpStrd = new InputStreamReader(inpStr);
				BufferedReader buffRd = new BufferedReader(inpStrd);
				String line = null;
				while((line = buffRd.readLine()) != null) {
					cmdOutput.append(line);
					cmdOutput.append('\n');
				}
				buffRd.close();

			} catch(Exception e) {
				System.out.println(e);
			}

		}
	}

	public int run(String cmd, File dir) throws IOException, InterruptedException {

		LOGGER.fine("Run :" + cmd + " - From " + dir.toString());

		Process proc = Runtime.getRuntime().exec(cmd, null, dir );

		ProcessHandler inputStream =
			new ProcessHandler(proc.getInputStream(),"INPUT");
		ProcessHandler errorStream =
			new ProcessHandler(proc.getErrorStream(),"ERROR");

		/* start the stream threads */
		inputStream.start();
		errorStream.start();

		return proc.waitFor();
	}
}
