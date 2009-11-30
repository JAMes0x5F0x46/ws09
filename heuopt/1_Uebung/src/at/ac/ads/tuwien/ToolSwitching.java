
package at.ac.ads.tuwien;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;


/**
 * @author Johannes REITER
 *
 */
public class ToolSwitching {

	// Define a static logger variable so that it references the
	private static Logger logger = Logger.getLogger(ToolSwitching.class);
	
	private static int RUNS;
	private static int MAGAZINE_SIZE;
	private static int NUMBER_OF_TOOLS;
	private static int NEIGHBORHOOD_SIZE;
	
	private Map<Integer,Set<Integer>> schedule = null;
	
	private int[][] similarities = null;
	private int[][] differences = null;
	
	private Solution bestSolution = null;
	
	private static String DIR = "matrices"+ File.separator;
	
	private static String HEURISTIC;
	private static String NEIGHBORHOOD;
	private static String STEP;
	
	private ToolSwitching(String[] args) {
		
		float relativeNeighborhoodSize = 0f;
		
		try {
			
			if ((args.length < 1) || (args.length>3)){
				printUsage();
			}
			
			initParameter(args);
			
			logger.info("Read properties...");
			Properties properties = new Properties();
			FileInputStream stream = new FileInputStream("param.properties");
		
			properties.load(stream);
		
			stream.close();
			RUNS = Integer.valueOf(properties.getProperty("runs"));
			MAGAZINE_SIZE = Integer.valueOf(properties.getProperty("magazineSize"));
			relativeNeighborhoodSize = Float.valueOf(properties.getProperty("neighborhoodSize"));
			
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		logger.info("Started algorithm...");

		schedule = InstanceImporter.importTspFile(DIR + "matrix_40j_60to_NSS_0.txt");
		//matrix_30j_40to_NSS_0.txt
		//matrix_40j_60to_NSS_0.txt
		
		// set the size of the neighborhood for the GVNS
		NEIGHBORHOOD_SIZE = (int) (schedule.size() * relativeNeighborhoodSize);
		logger.debug("Neighborhood size for the GVNS: "+NEIGHBORHOOD_SIZE);
		
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
		
		if(HEURISTIC.equals("fixed")){
			Heuristic heu = new Heuristic(this.schedule);
			
			List<Integer> testSequence = new ArrayList<Integer>();
			SortedSet<Integer> sorted = new TreeSet<Integer>();
			sorted.addAll(schedule.keySet());
			testSequence.addAll(sorted);
			
			Solution fixedSequence = heu.minSwitchesFixedSequence(testSequence);
			
			logger.info(fixedSequence.toString());
			
		} else if (HEURISTIC.equals("local")||HEURISTIC.equals("vnd")||HEURISTIC.equals("gvns")){
		
			Solution currentSolution = null;
			GreedyHeuristic gh = new GreedyHeuristic(this.schedule);
			Heuristic heu = new Heuristic(this.schedule);
			
			bestSolution = gh.createInitialSolution(0);
			
			Random random = new Random();
			int averageResult = 0;
			Set<Integer> solutionValues = new HashSet<Integer>();
			
			for(int i=0; i < RUNS; i++) {
				
				currentSolution = gh.createInitialSolution(random.nextInt(schedule.size()));
				logger.info((i+1)+".run initial solution: "+currentSolution.toString());

				if (HEURISTIC.equals("local")){
					// improve solution of construction heuristic with a local search
					currentSolution = heu.getLocalSolution(currentSolution);
				}else if (HEURISTIC.equals("vnd")){
					// improve solution of construction heuristic with a VND
					currentSolution = heu.getVNDSolution(currentSolution);
				}else if (HEURISTIC.equals("gvns")){
					// improve solution of construction heuristic with a VND
					currentSolution = heu.getGVNSSolution(currentSolution);
				}
				
				averageResult += currentSolution.getCosts();
				solutionValues.add(currentSolution.getCosts());
				
				logger.info("Result of LS: "+currentSolution.toString());
				// Found new best solution?
				if(this.bestSolution.getCosts() > currentSolution.getCosts()) {
					this.bestSolution = currentSolution;
				}
				logger.info("Best solution: "+bestSolution.toString());
			}
			logger.info("Best solution after "+RUNS+" runs: "+bestSolution.toString());
			logger.info("Average result: "+averageResult/RUNS);
			logger.info("Standard deviation: "+computeStdDeviation(solutionValues,averageResult/RUNS));
			
		} else {
			logger.error("wrong heuristic: " + HEURISTIC);
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
				
		new ToolSwitching(args);
	}

	public static int getMAGAZINE_SIZE() {
		return MAGAZINE_SIZE;
	}
	
	private void printUsage() {
		
		System.err.println("Usage: ToolSwitching <heuristic> <step> [neighborhood]");
		System.exit(1);
	}

	private void initParameter(String[] args){
		
		if (args[0]!=null && args[0].length()!=0){
			HEURISTIC=args[0];
		}else{
			System.err.println("heuristic is missing");
			printUsage();
		}
		
		if (args[1]!=null && args[1].length()!=0){
			STEP=args[1];
		}else{
			System.err.println("step is missing");
			printUsage();
		}
		
		if (HEURISTIC.equals("local")){
			if (args.length==3 && args[2]!=null && args[2].length()!=0){
				NEIGHBORHOOD = args[2];
			}else{
				System.err.println("neighborhood is missing");
				printUsage();
			}
		}
		
	}
	
	private double computeStdDeviation(Set<Integer> values,int average) {
		
		double result = 0d;
		
		for(int value : values) {
			
			result += Math.pow(value - average, 2);
		}
		
		result = result / values.size();		
		result = Math.sqrt(result);
		
		return result;
	}
	
	/**
	 * @return the sTEP
	 */
	public static String getSTEP() {
		return STEP;
	}

	/**
	 * @return the nEIGHBORHOOD
	 */
	public static String getNEIGHBORHOOD() {
		return NEIGHBORHOOD;
	}

	public static void setNUMBER_OF_TOOLS(int nUMBER_OF_TOOLS) {
		NUMBER_OF_TOOLS = nUMBER_OF_TOOLS;
	}

	public static int getNUMBER_OF_TOOLS() {
		return NUMBER_OF_TOOLS;
	}

	public static void setNEIGHBORHOOD_SIZE(int nEIGHBORHOOD_SIZE) {
		NEIGHBORHOOD_SIZE = nEIGHBORHOOD_SIZE;
	}

	public static int getNEIGHBORHOOD_SIZE() {
		return NEIGHBORHOOD_SIZE;
	}
	
}
