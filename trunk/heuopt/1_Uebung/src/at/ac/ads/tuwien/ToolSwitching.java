
package at.ac.ads.tuwien;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;


/**
 * @author Johannes REITER
 *
 */
public class ToolSwitching {

	// Define a static logger variable so that it references the
	   // Logger instance named "MyApp".
	static Logger logger = Logger.getLogger(ToolSwitching.class);
	
	private Map<Integer,Set<Integer>> schedule = null;
	
	//private static String DIR = "matrices"+ File.separator;
	
	private ToolSwitching() {
		
		System.out.println("Started algorithm...");
		logger.info("Started algorithm...");
		
		schedule = InstanceImporter.importTspFile("matrix_10j_10to_NSS_0.txt");
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Set up a simple configuration that logs on the console.
	    BasicConfigurator.configure();
		
		new ToolSwitching();
	}

}
