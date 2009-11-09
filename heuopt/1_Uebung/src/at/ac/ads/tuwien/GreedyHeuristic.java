package at.ac.ads.tuwien;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GreedyHeuristic {
	
	private Map<Integer,Set<Integer>> schedule = null;
	

	public Solution createInitialSolution(int[][] differences, 
			int[][] similarities, int startjob) {
		
		Solution sol = new Solution(startjob);
		
		Set<Integer> jobs = new HashSet<Integer>();
		for(int i=0; i<differences[0].length; i++) {
			if(i!=startjob)
				jobs.add(i);
		}
		
		int lastJob = startjob;
		
		while(sol.getList().size() < differences[0].length) {
			
			lastJob = chooseNextJob(jobs,lastJob,differences,similarities);
			
			// TODO compute costs
			sol.addCosts(0);
			sol.getList().add(lastJob);
			
			jobs.remove(lastJob);
		}
		
		return sol;
	}
	
	private int chooseNextJob(Set<Integer> jobs, int lastJob, int[][] differences, 
			int[][] similarities) {
		
		// TODO choosing
		for(int job : jobs) {
			
		}
		
		
		return 0;
	}

}
