package at.ac.ads.tuwien;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GreedyHeuristic {
	
	private Map<Integer,Set<Integer>> schedule = null;
	private Set<Integer> magazine = null;
	
	public GreedyHeuristic(Map<Integer,Set<Integer>> schedule) {
		
		magazine = new HashSet<Integer>();
		this.schedule = schedule;
	}

	public Solution createInitialSolution(int startJob) {
		
		Solution sol = new Solution(startJob);
		// initialize magazine
		magazine.clear();
		
		magazine.addAll(schedule.get(startJob));
		
		
		Set<Integer> openJobs = new HashSet<Integer>();
		for(int i=0; i<schedule.size(); i++) {
			if(i != startJob)
				openJobs.add(i);
		}
		
		int lastJob = startJob;
		
		while(sol.getList().size() < schedule.size()) {
			
			lastJob = chooseNextJob(openJobs,lastJob);
			
			// TODO compute costs
			sol.addCosts(0);
			sol.getList().add(lastJob);
			
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
