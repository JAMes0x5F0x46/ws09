
package at.ac.ads.tuwien;

import java.io.File;
import java.util.Map;
import java.util.Set;


/**
 * @author Johannes REITER
 *
 */
public class ToolSwitching {

	private Map<Integer,Set<Integer>> schedule = null;
	
	private static String DIR = "matrices"+ File.separator;
	
	private ToolSwitching() {
		
		System.out.println("Started algorithm...");
		
		schedule = InstanceImporter.importTspFile(DIR+"matrix_10j_10to_NSS_0.txt");
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		new ToolSwitching();

	}

}
