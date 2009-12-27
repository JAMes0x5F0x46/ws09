package at.ac.ads.tuwien;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class MEBP {

	// Define a static logger variable so that it references the
	private static Logger logger = Logger.getLogger(MEBP.class);
	
	private Input input;
	
	/**
	 * 
	 */
	public MEBP() {
		
		String filename = "mebp-01.dat";
		
		this.readInput("input" + File.separator + filename);
		
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
		
		logger.debug("readInput...");
		
		input = new Input();
		
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
				split = line.split("  ");
				
				amount++;
				x.add(Double.valueOf(split[0]));
				y.add(Double.valueOf(split[1]));
				
				logger.debug("x = " + split[0] + " y = " + split[1]);
			}
			
			input.setAmount(amount);
			
			for (int i = 0; i < amount; i++) {
				List<Double> distList = new ArrayList<Double>();
				for (int j = 0; j < amount; j++) {
					
					if (x!=y) {
						distList.add(this.calcdist(x.get(i), y.get(i), x.get(j), y.get(j)));
					} else {
						distList.add(0d);
					}
					
				}
				input.addDistList(distList);
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("readInput finished");
	}
	
	/**
	 * calculate the distance of two nodes
	 */
	private double calcdist(double x1, double y1, double x2, double y2) {
		
		double difX = Math.abs(x1-x2);
		double difY = Math.abs(x1-x2);
		
		return Math.sqrt(difX*difX + difY*difY);
	}
}
