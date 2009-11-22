package at.ac.ads.tuwien;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class Heuristic {
	
	private Map<Integer,Set<Integer>> schedule = null;
	private List<Set<Integer>> toolUsage = null;
	
	private static Logger logger = Logger.getLogger(Heuristic.class);
	
	/**
	 * @param schedule
	 */
	public Heuristic(Map<Integer, Set<Integer>> schedule) {
		
		this.schedule = schedule;
		this.toolUsage = new ArrayList<Set<Integer>>(ToolSwitching.getNUMBER_OF_TOOLS());
		
		// initialize the tool usage of the different jobs
		Set<Integer> usage = null;
		for(int i=0; i < schedule.size(); i++) {
			
			usage = new HashSet<Integer>();
			this.toolUsage.add(usage);
		}
		for(int i=0; i < schedule.size(); i++) {
			
			for(int tool : schedule.get(i)) {
				
				this.toolUsage.get(tool).add(i);
			}
		}
		
		for(int i=0; i < ToolSwitching.getNUMBER_OF_TOOLS(); i++) {
			
			logger.debug("Usage of tool "+i+": "+this.toolUsage.get(i).toString());
		}
	}


	public Solution getSolution (Solution solution){
		Solution bestSolution = solution;
		Solution curSolution;
		boolean run = true;
		
		while (run){
			
			if (ToolSwitching.getNEIGHBORHOOD().equals("switch")){
				curSolution = getSolutionSwitch(bestSolution);
			}else if (ToolSwitching.getNEIGHBORHOOD().equals("move")){
				curSolution = getSolutionMove(bestSolution);
			}else{
				logger.error("wrong neighborhood: " + ToolSwitching.getNEIGHBORHOOD());
				break;
			}
			
			if (curSolution.getCosts() < bestSolution.getCosts()){
				bestSolution = curSolution;
			}else{
				run=false;
			}
			
		}
		
		return bestSolution;
	}
	

	private Solution getSolutionSwitch (Solution solution){
		
		Solution curSolution = solution;
		Solution bestSolution = solution;
		
		for (int i=0; i<schedule.size(); i++){
			for (int j=i+1; j<schedule.size(); j++){
				
				curSolution = minSwitchesFixedSequence(switchJobs(curSolution.getList(), i, j));
				if (bestSolution.getCosts()>curSolution.getCosts()){
					bestSolution = curSolution;
				}
				curSolution = solution;
			}
		}
		
		return bestSolution;
	}
	
	private Solution getSolutionMove(Solution solution){
		
		Solution curSolution = solution;
		Solution bestSolution = solution;
		
		for (int i=0; i<schedule.size(); i++){
			for (int j=0; j<schedule.size(); j++){
				
				if (i==j) continue;

				curSolution = minSwitchesFixedSequence(moveJob(curSolution.getList(), i, j));
				
				if (bestSolution.getCosts()>curSolution.getCosts()){
					bestSolution = curSolution;
				}
				curSolution = solution;
			}
		}
		
		return bestSolution;
	}
	
	public List<Integer> switchJobs(List<Integer> jobs, int i, int j){
		List<Integer> retJobs = new ArrayList<Integer>();
		
		retJobs.addAll(jobs);
		int tmp = retJobs.get(j);
		retJobs.add(j, retJobs.get(i));
		retJobs.remove(j+1);
		retJobs.add(i, tmp);
		retJobs.remove(i+1);
		
		return retJobs;
	}
	
	public List<Integer> moveJob(List<Integer> jobs, int i, int j){
		List<Integer> retJobs = new ArrayList<Integer>();
		
		retJobs.addAll(jobs);
		
		retJobs.add(j, retJobs.get(i));
		if (i<j){
			retJobs.remove(i);
		}else{
			retJobs.remove(i+1);
		}
		
		return retJobs;
	}
	
	public Solution minSwitchesFixedSequence(List<Integer> sequence) {
		
		if(sequence.size() != schedule.size()) {
			logger.error("The given sequence is not correct. ");
			return null;
		}
					
		Set<Integer> magazine = new HashSet<Integer>();
		
			// initialize magazine
		magazine.addAll(schedule.get(sequence.get(0)));
		for(int i=1; i < schedule.size(); i++) {
			
			for(int tool : schedule.get(sequence.get(i))) {
				
				if(magazine.size() >= ToolSwitching.getMAGAZINE_SIZE()) {
					i=schedule.size();
					break;
				}
				
				magazine.add(tool);
			}
		}
		
		logger.debug("Initial magazine: "+magazine.toString());
		Solution sol = new Solution(sequence.get(0));
				
		for(int i=1; i < schedule.size(); i++) {
			
			sol.addJob(sequence.get(i), fillMagazine(magazine,sequence,i));
			logger.debug("Added job: "+i+" magazine: "+magazine.toString());
		}
		
		return sol;
	}
	
	private int fillMagazine(Set<Integer> magazine, List<Integer> sequence, int positionNextJob) {
		
		int costs = 0;
		Set<Integer> possibleTools = new HashSet<Integer>();
		int nextJob = sequence.get(positionNextJob);
		
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
					int latestNeed = -1;
					int removeTool = -1;
					int firstNeed;
					for(int toolToRemove : possibleTools) {
						/*
						if(toolUsedInJobs(toolToRemove).tailSet(nextJob).size() == 0) {
						
							removeTool = toolToRemove;
							break;
							
						} if(latestJob < toolUsedInJobs(toolToRemove).tailSet(nextJob).first()) {
							
							latestJob = toolUsedInJobs(toolToRemove).tailSet(nextJob).first();
							removeTool = toolToRemove;
						}*/
						
						firstNeed = firstNeedInSequence(sequence,toolUsedInJobs(toolToRemove),positionNextJob);

							// tool is not used any more and will be removed
						if(firstNeed == -1) {
							
							removeTool = toolToRemove;
							break;
							
						} if(latestNeed < firstNeed) {
							
							latestNeed = firstNeed;
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
	
	/**
	 * Returns -1 if no job runs after the offset specified in the sequence
	 * 
	 * @param sequence
	 * @param jobs
	 * @param offset
	 * @return
	 */
	private int firstNeedInSequence(List<Integer> sequence, Set<Integer> jobs, int offset) {
		
		int firstPosition=sequence.size();
		int firstJob=-1;
		List<Integer> subSequence = sequence.subList(offset, sequence.size());
		//logger.debug(offset+": "+subSequence.toString()+" jobs: "+jobs.toString());
		int index;
		for(int job : jobs) {
			
			index = subSequence.indexOf(job);
			if(index != -1 && index < firstPosition) {
				firstPosition = index;
				firstJob = job;
			}
		}
		if(firstPosition == sequence.size())
			firstJob = -1;
		//logger.debug("first need at "+sequence.indexOf(firstJob)+" of "+firstJob);
		return sequence.indexOf(firstJob);		
	}
	
	private boolean isToolUsed(int tool, int job) {
		
		return this.schedule.get(job).contains(tool);
	}
	
	private Set<Integer> toolUsedInJobs (int tool){
		
		return this.toolUsage.get(tool);
	}
	
}
