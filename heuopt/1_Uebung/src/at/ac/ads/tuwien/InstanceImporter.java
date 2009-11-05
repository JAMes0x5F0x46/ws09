package at.ac.ads.tuwien;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class InstanceImporter {
	
	private static Logger logger = Logger.getLogger(InstanceImporter.class);

	public static Map<Integer,Set<Integer>> importTspFile(String path) throws IllegalArgumentException {
		
		logger.info("Importing instance file: "+path);
		
		InputStream is = ClassLoader.getSystemResourceAsStream(path);
		
		BufferedReader reader = null;
		
		Map<Integer,Set<Integer>> schedule = new HashMap<Integer,Set<Integer>>();
		
		if(is != null) {
			
			reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			
			try {
				String[] split;
				Set<Integer> tools = null;
					// read information about TSP
				while((line=reader.readLine()) != null) {
					
					logger.debug(line);
					
					split = line.split("[:,#]");
					
					tools = new HashSet<Integer>();
					
					logger.debug("Job: "+Integer.valueOf(split[0]));
					
					for(int i=1; i<split.length;i++) {						
						tools.add(Integer.valueOf(split[i]));
						logger.debug("Tool "+i+": "+Integer.valueOf(split[i]));
					}
					schedule.put(Integer.valueOf(split[0]), tools);
				}
				
				logger.debug("Instance size: "+schedule.size());
				
				
			} catch (IOException e) {
				logger.error("Could not read Instance-File (IOException): "+path);
				throw new IllegalArgumentException("Could not read Instance-File (IOException): "+path);
			
			} catch (Exception e) {
				logger.error("Could not read Instance-File "+e.toString()+": "+path);
				throw new IllegalArgumentException("Could not read Instance-File "+e.toString()+" \n "+path);	
				
			} finally {
				
				try {
					if(reader != null)
						reader.close();
					
					if(is != null)
						is.close();
				} catch (IOException e) {
					logger.warn("Could not close stream. ");
				}
			}
			
		} else {
			logger.error("Could not find Instance-File: " + path);
			throw new IllegalArgumentException("Could not find Instance-File: " + path);
		}
		
		
		logger.info("Successfully imported Instance-File with "+schedule.size() + " jobs. ");
		
		return schedule;
	}
}
