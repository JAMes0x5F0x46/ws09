package at.ac.ads.tuwien;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MEBP {

	// Define a static logger variable so that it references the
	private static Logger logger = Logger.getLogger(MEBP.class);
	
	private final int ANTS = 10;
	private final int MAX_RUNS = 30;
	
	/**
	 * 
	 */
	public MEBP() {
		
		logger.setLevel(Level.INFO);
		
		String filename = "mebp-06.dat";
		
		this.readInput("input" + File.separator + filename);
		
		ACO aco = new ACO();
		
		Solution solution, best = null;
		double average=0d;
		Set<Double> values = new HashSet<Double>();
		long startTime = System.currentTimeMillis();
		for(int i=1; i <= MAX_RUNS; i++) {
			
			solution = aco.runACO(ANTS);
			
			if(best == null)
				best = solution;
			else if(best.getWeight() > solution.getWeight())
				best = solution;
			
			average += solution.getWeight();
			values.add(solution.getWeight());
			logger.warn("Finished "+i+".run; Best so far "+solution.toString());
		}
		average = average / MAX_RUNS;
		logger.warn("Best after "+MAX_RUNS+" runs "+best.toString());
		logger.warn("Average weight: "+average+" standard deviation: "+computeStdDeviation(values,average));
		logger.warn("Average run time: "+((System.currentTimeMillis() - startTime) / MAX_RUNS));
	}
	
	private double computeStdDeviation(Set<Double> values,double average) {
		
		double result = 0d;
		
		for(double value : values) {
			
			result += Math.pow(value - average, 2);
		}
		
		result = result / values.size();		
		result = Math.sqrt(result);
		
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		new MEBP();

	}

	/**
	 * read input-data from the input-file
	 * @param path
	 */
	private void readInput(String path) {
		
		logger.info("readInput...");
		
		BufferedReader reader = null;
		DataInputStream is = null;
		
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(path);
			is = new DataInputStream(fstream);
			reader = new BufferedReader(new InputStreamReader(is));
			
			String[] split;
			String line; 
			
			List<Double> x = new ArrayList<Double>();
			List<Double> y = new ArrayList<Double>();
			int amount = 0;
			
			while((line=reader.readLine()) != null) {
				split = line.split("[\\s]+");
				
				amount++;
				x.add(Double.valueOf(split[0]));
				y.add(Double.valueOf(split[1]));
				
				logger.debug("x = " + split[0] + " y = " + split[1]);
			}
			
			Input.dist = new double[amount][amount];
			Input.amount = amount;
			
			for (int i = 0; i < amount; i++) {
				for (int j = 0; j < amount; j++) {
					if (x!=y) {
						Input.dist[i][j] = Math.pow(this.calcdist(x.get(i), y.get(i), x.get(j), y.get(j)),3);
					} else {
						Input.dist[i][j] = 0d;
					}
				}
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("readInput finished");
	}
	
	/**
	 * calculate the distance of two nodes
	 */
	private double calcdist(double x1, double y1, double x2, double y2) {
		
		double difX = Math.abs(x1-x2);
		double difY = Math.abs(y1-y2);
		
		return Math.sqrt(difX*difX + difY*difY);
	}
}
