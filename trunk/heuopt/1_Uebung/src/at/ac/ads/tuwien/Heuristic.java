package at.ac.ads.tuwien;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class Heuristic {
	
	private Map<Integer,Set<Integer>> schedule = null;
	
	private static Logger logger = Logger.getLogger(Heuristic.class);
	
	/**
	 * @param schedule
	 */
	public Heuristic(Map<Integer, Set<Integer>> schedule) {
		super();
		this.schedule = schedule;
	}



	public Solution getSolution (Solution solution){
		
		Solution curSolution = solution.clone();
		Solution bestSolution = solution.clone();
		
		for (int i=0; i<schedule.size(); i++){
			for (int j=i+1; j<schedule.size(); j++){
				
				if (ToolSwitching.getNEIGHBORHOOD().equals("switch")){
					switchJobs(curSolution, i, j);
				}else if (ToolSwitching.getNEIGHBORHOOD().equals("move")){
					moveJob(curSolution, i, j);
				}else{
					logger.error("wrong neighborhood: " + ToolSwitching.getNEIGHBORHOOD());
				}
				curSolution = minSwitchesFixedSequence(curSolution.getList());
				if (bestSolution.getCosts()>curSolution.getCosts()){
					bestSolution = curSolution;
				}
				curSolution = solution.clone();
			}
		}
		
		return bestSolution;
	}
	
	public void switchJobs(Solution solution, int i, int j){
		
		int tmp = solution.getList().get(j);
		solution.getList().add(j, solution.getList().get(i));
		solution.getList().remove(j+1);
		solution.getList().add(i, tmp);
		solution.getList().remove(i+1);
	}
	
	public void moveJob(Solution solution, int i, int j){
		solution.getList().add(j, solution.getList().get(i));
		solution.getList().remove(i);
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
		boolean isToolUsed=false;
		
		for (int i=0; i < schedule.get(job).size(); i++){
			if (schedule.get(job).contains(tool)){
				isToolUsed=true;
				break;
			}
		}
		
		return isToolUsed;
	}
	
	private SortedSet<Integer> toolUsedInJobs (int tool){
		
		SortedSet<Integer> jobs = new TreeSet<Integer>();
		
		for (int i=0; i < schedule.size(); i++){
			if (isToolUsed(tool, i)){
				jobs.add(i);
			}
		}
		
		return jobs;
	}
	
}
