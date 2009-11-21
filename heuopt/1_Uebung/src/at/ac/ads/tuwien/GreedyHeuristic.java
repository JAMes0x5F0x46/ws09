package at.ac.ads.tuwien;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class GreedyHeuristic {
	
	private static Logger logger = Logger.getLogger(GreedyHeuristic.class);
	
	private Map<Integer,Set<Integer>> schedule = null;
	private Set<Integer> magazine = null;
	
	public GreedyHeuristic(Map<Integer,Set<Integer>> schedule) {
		
		magazine = new HashSet<Integer>();
		this.schedule = schedule;
	}

	public Solution createInitialSolution(int startJob) {
		
		Solution sol = new Solution(startJob);		
		
		Set<Integer> openJobs = new HashSet<Integer>();
		for(int i=0; i<schedule.size(); i++) {
			if(i != startJob)
				openJobs.add(i);
		}
		
		// initialize magazine
		magazine.clear();
		
		magazine.addAll(schedule.get(startJob));
		int lastJob = startJob;
		while(magazine.size() < ToolSwitching.getMAGAZINE_SIZE()) {
			
			lastJob = chooseNextJob(openJobs,lastJob);
			for(int tool : schedule.get(lastJob)) {
				
				if(magazine.size() >= ToolSwitching.getMAGAZINE_SIZE()) {
					break;
				}
				
				magazine.add(tool);
			}
			openJobs.remove(lastJob);
		}
		openJobs.clear();
		for(int i=0; i<schedule.size(); i++) {
			if(i != startJob)
				openJobs.add(i);
		}
		
		logger.debug("magazine: "+magazine.toString());
		
		lastJob = startJob;
		int costs;
		
		while(sol.getList().size() < schedule.size()) {
			
			lastJob = chooseNextJob(openJobs,lastJob);
			
			costs = 0;
			for(int tool : schedule.get(lastJob)) {
				
				if(!(magazine.contains(tool))) {
					
					for(int toolToRemove : magazine) {
					;	if(!(schedule.get(lastJob).contains(toolToRemove))) {
							
							magazine.remove(toolToRemove);
							magazine.add(tool);
							costs++;
							break;
						}
					}
				}
			}
			sol.addJob(lastJob, costs);			
			logger.debug("next job: "+lastJob+" magazine: "+magazine.toString()+" costs: "+sol.getCosts());
						
			openJobs.remove(lastJob);
		}
		
		return sol;
	}
	
	private int chooseNextJob(Set<Integer> openJobs, int lastJob) {
		
		int bestValue = Integer.MAX_VALUE;
		int bestJob = -1;
		int diff;
		
		for(int job : openJobs) {

			diff = 0;
			for(int tool : schedule.get(job)) {
				if(!(magazine.contains(tool)))
					diff++;
			}
			
			if(bestValue > diff) {
				bestValue = diff;
				bestJob = job;
			}
		}
				
		return bestJob;
	}
}
