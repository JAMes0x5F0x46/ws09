
package at.ac.ads.tuwien;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
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
		
		Solution bestSolution = null;
		GreedyHeuristic gh = new GreedyHeuristic(this.schedule);
		
		// try fixed sequence
		Solution fixedSequence = minSwitchesFixedSequence();
		logger.info(fixedSequence.toString());
		
		for(int i=0; i < RUNS; i++) {
			
			bestSolution = gh.createInitialSolution(0);
			
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
	
	private Solution minSwitchesFixedSequence() {
		
		Set<Integer> magazine = new HashSet<Integer>();
		
			// initialize magazine
		magazine.addAll(schedule.get(0));
		for(int i=1; i < schedule.size(); i++) {
			
			for(int tool : schedule.get(i)) {
				
				if(magazine.size() >= MAGAZINE_SIZE) {
					i=schedule.size();
					break;
				}
				
				magazine.add(tool);
			}
		}
		Solution sol = new Solution(0);
				
		for(int i=1; i < schedule.size(); i++) {
			
			sol.addJob(i, fillMagazine(magazine,i));
			logger.debug("Added job: "+i+" magazine: "+magazine.toString());
		}
		
		return sol;
	}
	
	private int fillMagazine(Set<Integer> magazine, int nextJob) {
		
		int costs = 0;
		Set<Integer> possibleTools = new HashSet<Integer>();	
		for(int tool : schedule.get(nextJob)) {
			
				// is tool already in the magazine?
			if(!(magazine.contains(tool))) {
				
				possibleTools.clear();
				
					// find possible tools to remove
				for(int toolToRemove : magazine) {
					
					if(isToolUsed(toolToRemove,nextJob)) {
						continue;
					} else {
						possibleTools.add(toolToRemove);
					}
				}
					// just one tool can be remove => remove it
				if(possibleTools.size() == 1) {
					magazine.removeAll(possibleTools);
					magazine.add(tool);
					// error, if no tool can be removed
				} else if(possibleTools.size() == 0) {
					logger.error("At least one tool has to be removeable! ");
					// more tools are possible for removing
					// find the tool which is used at latest again
				} else {
					
						// check which of the possible tools is used next time as latest
					int latestJob = -1;
					int removeTool = -1;
					for(int toolToRemove : possibleTools) {
						
						if(latestJob < toolUsedInJobs(toolToRemove).tailSet(nextJob).first()) {
							
							latestJob = toolUsedInJobs(toolToRemove).tailSet(nextJob).first();
							removeTool = toolToRemove;
						}
					}
					magazine.remove(removeTool);
					magazine.add(tool);
				}			
				costs++;
			}
		}

		return costs;
	}
	
	public boolean isToolUsed(int tool, int job){
		boolean isToolUsed=false;
		
		for (int i=0; i < schedule.get(job).size(); i++){
			if (schedule.get(job).contains(tool)){
				isToolUsed=true;
				break;
			}
		}
		
		return isToolUsed;
	}
	
	public SortedSet<Integer> toolUsedInJobs (int tool){
		
		SortedSet<Integer> jobs = new TreeSet<Integer>();
		
		for (int i=0; i < schedule.size(); i++){
			if (isToolUsed(tool, i)){
				jobs.add(i);
			}
		}
		
		return jobs;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Set up a simple configuration that logs on the console.
	    BasicConfigurator.configure();
		
		new ToolSwitching();
	}

	public static int getMAGAZINE_SIZE() {
		return MAGAZINE_SIZE;
	}

}
