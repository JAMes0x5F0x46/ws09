
package at.ac.ads.tuwien;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
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
	
	static int runs;
	
	private Map<Integer,Set<Integer>> schedule = null;
	
	private int[][] similarities = null;
	private int[][] differences = null;
	
	private static String DIR = "matrices"+ File.separator;
	
	private ToolSwitching() {
		
		try {
			logger.info("Read properties...");
			Properties properties = new Properties();
			FileInputStream stream = new FileInputStream("param.properties");
		
			properties.load(stream);
		
			stream.close();
			runs = Integer.valueOf(properties.getProperty("runs"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("Started algorithm...");
		
		schedule = InstanceImporter.importTspFile(DIR + "matrix_10j_10to_NSS_0.txt");
		
		logger.info("Compute cost graph.");
		computeCostGraph();
		
		/*
		// check similarities
		for(int i=0; i<schedule.size(); i++) {
			
			System.out.print(i+": ");
			for(int j=0; j<schedule.size(); j++) {
				System.out.print(similarities[i][j]+" ");
			}
			System.out.println("");
		}	
		System.out.println("");
		
		// ckeck differences
		for(int i=0; i<schedule.size(); i++) {
			
			System.out.print(i+": ");
			for(int j=0; j<schedule.size(); j++) {
				System.out.print(differences[i][j]+" ");
			}
			System.out.println("");
		}
		*/
		
		//TODO for
		
		//TODO greedy heuristic
		
		//TODO optimierungsalgorithmus
		
	}

	private void computeCostGraph() {

		similarities = new int[schedule.size()][schedule.size()];
		differences = new int[schedule.size()][schedule.size()];
		
		for(int i=0; i<schedule.size(); i++) {
			
			for(int j=i; j<schedule.size(); j++) {
				
				if(i == j) {
					similarities[i][j] = 0;
					differences[i][j] = Integer.MAX_VALUE;
					
				} else {
					
					for(int tool : schedule.get(i)) {
						
						if(schedule.get(j).contains(tool)) {
							similarities[i][j]++;
							similarities[j][i]++;
						} else {
							differences[i][j]++;
							differences[j][i]++;
						}
					}
					
					for(int tool : schedule.get(j)) {
						
						if(!(schedule.get(i).contains(tool))) {
							differences[i][j]++;
							differences[j][i]++;
						}
					}
				}	
			}
		}
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
