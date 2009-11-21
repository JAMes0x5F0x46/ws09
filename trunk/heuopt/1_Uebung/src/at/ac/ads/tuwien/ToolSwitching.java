
package at.ac.ads.tuwien;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;


/**
 * @author Johannes REITER
 *
 */
public class ToolSwitching {

	// Define a static logger variable so that it references the
	   // Logger instance named "MyApp".
	private static Logger logger = Logger.getLogger(ToolSwitching.class);
	
	private static int RUNS;
	private static int MAGAZINE_SIZE;
	
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
			RUNS = Integer.valueOf(properties.getProperty("runs"));
			MAGAZINE_SIZE = Integer.valueOf(properties.getProperty("magazineSize"));
			
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("Started algorithm...");
		
		schedule = InstanceImporter.importTspFile(DIR + "matrix_40j_30to_NSS_0.txt");
		
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
		
		Solution bestSolution = null;
		GreedyHeuristic gh = new GreedyHeuristic(this.schedule);
		Heuristic heu = new Heuristic(this.schedule);
		// try fixed sequence
		List<Integer> testSequence = new ArrayList<Integer>();
		SortedSet<Integer> sorted = new TreeSet<Integer>();
		sorted.addAll(schedule.keySet());
		testSequence.addAll(sorted);
		Solution fixedSequence = heu.minSwitchesFixedSequence(testSequence);
		logger.info(fixedSequence.toString());
		
		for(int i=0; i < RUNS; i++) {
			
			bestSolution = gh.createInitialSolution(0);
			
			heu.getSolution(bestSolution);
			
			logger.info(bestSolution.toString());
			//TODO optimierungsalgorithmus
			
		}
		
		
		
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
//	    BasicConfigurator.configure();
		
		new ToolSwitching();
	}

	public static int getMAGAZINE_SIZE() {
		return MAGAZINE_SIZE;
	}

}
