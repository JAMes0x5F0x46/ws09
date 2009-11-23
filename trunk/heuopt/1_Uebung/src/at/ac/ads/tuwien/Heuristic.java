package at.ac.ads.tuwien;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Level;
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
		
		logger.setLevel(Level.INFO);
		
		// initialize the tool usage of the different jobs
		Set<Integer> usage = null;
		for(int i=0; i < ToolSwitching.getNUMBER_OF_TOOLS(); i++) {
			
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

	public Solution getGVNSSolution (Solution solution){
		Solution bestSolution = solution;
		Solution curSolution = solution;
		
		// neighborhood size starts with one
		int l=1;
		do {
			curSolution =  MultiMoveJob(bestSolution, 2*l);
			curSolution = minSwitchesFixedSequence(curSolution.getList());
			logger.info("Generated new solution in "+l+".neighborhood: "+curSolution.toString());
			
			curSolution = getVNDSolution(curSolution);
			
			if (curSolution.getCosts() < bestSolution.getCosts()) {
				bestSolution = curSolution;
				// start again with the smallest neighborhood
				l = 1;
			} else {
				// increase the size of the neighborhood
				l++;
				logger.info("Increased neighborhood to: "+l+" result with last neighborhood size: "
						+curSolution.getCosts());
			}
			
		} while (l <=  ToolSwitching.getNEIGHBORHOOD_SIZE());
		
		return bestSolution;
	}
	
	/**
	 * 
	 * @param jobs
	 * @param n
	 * @return
	 */

	public Solution MultiMoveJob(Solution solution, int n) {
		
		Solution retSolution = solution.clone();

		for (int i=0; i < n; i++){
			retSolution = this.getSolutionRandomMove(retSolution);
		}
		
		return retSolution;
	}
	
	public Solution getVNDSolution (Solution solution) {
		Solution bestSolution = solution;
		Solution curSolution = solution;
		boolean run = true;
			// start with the neighborhood split
		NeighborhoodStrategy strategy = NeighborhoodStrategy.SPLIT;
		
		while (run) {
			
			if (strategy.equals(NeighborhoodStrategy.SPLIT)) {
				
				if(ToolSwitching.getSTEP().equals("best") || ToolSwitching.getSTEP().equals("next")) {
					
					curSolution = getSolutionSplit(bestSolution);
					
				} else if (ToolSwitching.getSTEP().equals("random")){
					curSolution = getSolutionRandomSplit(bestSolution);
				}
				
				if(curSolution.getCosts() < bestSolution.getCosts()){
					//if current solution is better then best solution : best Solution = current solution
					bestSolution = curSolution;
					logger.info("Found new best solution with strategy: "+strategy.toString()+" "+bestSolution.toString());
				} else{
					//if we found no better solution, we go to the next neighborhood
					strategy = NeighborhoodStrategy.MOVE;
				}
				
			} else if(strategy.equals(NeighborhoodStrategy.SWITCH)) {
				
				if (ToolSwitching.getSTEP().equals("best")||ToolSwitching.getSTEP().equals("next")){
					curSolution = getSolutionSwitch(bestSolution);
				} else if (ToolSwitching.getSTEP().equals("random")){
					curSolution = getSolutionRandomSwitch(bestSolution);
				}
				
				if (curSolution.getCosts() < bestSolution.getCosts()){
					//if current solution is better then best solution : best Solution = current solution
					bestSolution = curSolution;
					logger.info("Found new best solution with strategy: "+strategy.toString()+" "+bestSolution.toString());
					strategy = NeighborhoodStrategy.SPLIT;
	
				} else{
					//if we found no better solution, we go to the next neighborhood
					strategy = NeighborhoodStrategy.ROTATE;
				}
				
			} else if(strategy.equals(NeighborhoodStrategy.MOVE)) {
				
				if(ToolSwitching.getSTEP().equals("best")||ToolSwitching.getSTEP().equals("next")) {
					curSolution = getSolutionMove(bestSolution);
				} else if (ToolSwitching.getSTEP().equals("random")){
					curSolution = getSolutionRandomMove(bestSolution);
				}
				
				if (curSolution.getCosts() < bestSolution.getCosts()){
					//if we found a better solution, we go to the first neighborhood
					bestSolution = curSolution;
					logger.info("Found new best solution with strategy: "+strategy.toString()+" "+bestSolution.toString());
					strategy = NeighborhoodStrategy.SPLIT;					
					
				} else{
					//if we found no better solution, we go to the next neighborhood
					strategy = NeighborhoodStrategy.SWITCH;
				}
			} else if(strategy.equals(NeighborhoodStrategy.ROTATE)) {
				if(ToolSwitching.getSTEP().equals("best")||ToolSwitching.getSTEP().equals("next")) {
					curSolution = getSolutionRotate(bestSolution);
				} else if (ToolSwitching.getSTEP().equals("random")){
					curSolution = getSolutionRandomRotate(bestSolution);
				}
				
				if(curSolution.getCosts() < bestSolution.getCosts()){
					//if we found a better solution, we go to the first neighborhood
					bestSolution = curSolution;
					logger.info("Found new best solution with strategy: "+strategy.toString()+" "+bestSolution.toString());
					strategy = NeighborhoodStrategy.SPLIT;
					
					
				} else{
					//if we found no better solution, we stop 
					run=false;
				}
			}
		}
		
		return bestSolution;
	}

	/**
	 * return the best solution of local search
	 * @param solution initial solution
	 * @return best Solution of local search
	 */
	public Solution getLocalSolution (Solution solution){
		Solution bestSolution = solution;
		Solution curSolution = solution;
		boolean run = true;
		
		//run until bestSolution was found
		while (run){
			
			//check which neighborhood is selected and get the next Solution of the neighborhood
			if (ToolSwitching.getNEIGHBORHOOD().equals("switch")){
				if (ToolSwitching.getSTEP().equals("best")||ToolSwitching.getSTEP().equals("next")){
					curSolution = getSolutionSwitch(bestSolution);
				}else if (ToolSwitching.getSTEP().equals("random")){
					curSolution = getSolutionRandomSwitch(bestSolution);
				}
			}else if (ToolSwitching.getNEIGHBORHOOD().equals("move")){
				if (ToolSwitching.getSTEP().equals("best")||ToolSwitching.getSTEP().equals("next")){
					curSolution = getSolutionMove(bestSolution);
				}else if (ToolSwitching.getSTEP().equals("random")){
					curSolution = getSolutionRandomMove(bestSolution);
				}
			}else if (ToolSwitching.getNEIGHBORHOOD().equals("split")){
				if (ToolSwitching.getSTEP().equals("best")||ToolSwitching.getSTEP().equals("next")){
					curSolution = getSolutionSplit(bestSolution);
				}else if (ToolSwitching.getSTEP().equals("random")){
					curSolution = getSolutionRandomSplit(bestSolution);
				}
			}else if (ToolSwitching.getNEIGHBORHOOD().equals("rotate")){
				if (ToolSwitching.getSTEP().equals("best")||ToolSwitching.getSTEP().equals("next")){
					curSolution = getSolutionRotate(bestSolution);
				}else if (ToolSwitching.getSTEP().equals("random")){
					curSolution = getSolutionRandomRotate(bestSolution);
				}
			}else{
				logger.error("wrong neighborhood: " + ToolSwitching.getNEIGHBORHOOD());
				break;
			}
			
			//if current solution is better then best solution : best Solution = current solution
			if (curSolution.getCosts() < bestSolution.getCosts()){
				bestSolution = curSolution;
			}else{
				run=false;
			}
			
		}
		
		return bestSolution;
	}
	
	
	/**
	 * return the best solution (best improvement) or the first best solution (next improvement)
	 * of the neighborhood
	 * @param solution
	 * @return
	 */
	private Solution getSolutionSplit (Solution solution){
		
		Solution curSolution = solution;
		Solution bestSolution = solution;
		
		for (int i=0; i<schedule.size(); i++){
			//generate new Solution (switch two jobs)
			curSolution = minSwitchesFixedSequence(splitJobs(curSolution.getList(), i));
			
			//if current solution is better then best solution : best Solution = current solution
			if (bestSolution.getCosts() > curSolution.getCosts()){
				bestSolution = curSolution;
				//next improvement: stop if we found a better solution
				if (ToolSwitching.getSTEP().equals("next")){
					return bestSolution;
				}
			}
			curSolution = solution;
		}
		
		return bestSolution;
	}
	
	public List<Integer> splitJobs(List<Integer> jobs, int i){
		List<Integer> retJobs = new ArrayList<Integer>();
		
		for (int job : jobs.subList(i, jobs.size())){
			retJobs.add(job);
		}
		for (int job : jobs.subList(0, i)){
			retJobs.add(job);
		}
		
		return retJobs;
	}
	
	/**
	 * return the best solution (best improvement) or the first best solution (next improvement)
	 * of the neighborhood
	 * @param solution
	 * @return
	 */
	private Solution getSolutionRotate(Solution solution){
		
		Random rand = new Random();
		
		Solution curSolution = solution;
		Solution bestSolution = solution;
		
		for (int i=0; i<schedule.size(); i++){
			for (int j=i+1; j<schedule.size(); j++){
				//logger.info(Math.log(j-1));
				for (int k=0; k <= Math.log(j-i+1); k++){
					int s = rand.nextInt(j-i) + i;
					
					//generate new Solution (switch two jobs)
					curSolution = minSwitchesFixedSequence(rotateJobs(curSolution.getList(), i, j, s));
					
					//if current solution is better then best solution : best Solution = current solution
					if (bestSolution.getCosts()>curSolution.getCosts()){
						bestSolution = curSolution;
						//next improvement: stop if we found any better solution
						if (ToolSwitching.getSTEP().equals("next")){
							return bestSolution;
						}
					}
					curSolution = solution;
				}
			}
		}
		
		return bestSolution;
	}
	
	/**
	 * rotate a sequenz (begin: i; end: j) at index s
	 * @param jobs
	 * @param i
	 * @param j
	 * @param s
	 * @return
	 */
	public List<Integer> rotateJobs(List<Integer> jobs, int i, int j, int s){
		List<Integer> retJobs = new ArrayList<Integer>();
		
		for (int job : jobs.subList(0, i)){
			retJobs.add(job);
		}
		for (int job : jobs.subList(s , j)){
			retJobs.add(job);
		}
		for (int job : jobs.subList(i , s)){
			retJobs.add(job);
		}
		for (int job : jobs.subList(j , jobs.size())){
			retJobs.add(job);
		}
		
		return retJobs;
	}
	
	/**
	 * generate random solution from the rotate-neighborhood
	 * @param solution
	 * @return
	 */
	private Solution getSolutionRandomRotate(Solution solution){
		
		Solution curSolution = solution.clone();
		
		Random ran = new Random();
		
		//genereat random index i
		int i = ran.nextInt(schedule.size());
		
		//generate random index j; i!=j
		int j = -1;
		do{
			j = ran.nextInt(schedule.size());
			if (j==i) j=-1;
		}while(j == -1);
		
		Random rand = new Random();
		int s = rand.nextInt(j-i) + i;
		
		//generate new solution
		curSolution.setList(rotateJobs(curSolution.getList(), i, j, s));
		
		return curSolution;
	}

	/**
	 * return the best solution (best improvement) or the first best solution (next improvement)
	 * of the neighborhood
	 * @param solution
	 * @return
	 */
	private Solution getSolutionSwitch (Solution solution){
		
		Solution curSolution = solution;
		Solution bestSolution = solution;
		
		for (int i=0; i<schedule.size(); i++){
			for (int j=i+1; j<schedule.size(); j++){
				
				//generate new Solution (switch two jobs)
				curSolution = minSwitchesFixedSequence(switchJobs(curSolution.getList(), i, j));
				
				//if current solution is better then best solution : best Solution = current solution
				if (bestSolution.getCosts()>curSolution.getCosts()){
					bestSolution = curSolution;
					//next improvement: stop if we found any better solution
					if (ToolSwitching.getSTEP().equals("next")){
						return bestSolution;
					}
				}
				curSolution = solution;
			}
		}
		
		return bestSolution;
	}
	
	/**
	 * return the best solution (best improvement) or the first best solution (next improvement)
	 * of the neighborhood
	 * @param solution
	 * @return
	 */
	private Solution getSolutionMove(Solution solution){
		
		Solution curSolution = solution;
		Solution bestSolution = solution;
		
		for (int i=0; i<schedule.size(); i++){
			for (int j=0; j<schedule.size(); j++){
				
				if (i==j) continue;

				//generate new Solution (move one job)
				curSolution = minSwitchesFixedSequence(moveJob(curSolution.getList(), i, j));
				
				//if current solution is better then best solution : best Solution = current solution
				if (bestSolution.getCosts()>curSolution.getCosts()){
					bestSolution = curSolution;
					//next improvement: stop if we found a better solution
					if (ToolSwitching.getSTEP().equals("next")){
						return bestSolution;
					}
				}
				curSolution = solution;
			}
		}
		
		return bestSolution;
	}
	
	/**
	 * generate random solution from the switch-neighborhood
	 * @param solution
	 * @return
	 */
	private Solution getSolutionRandomSwitch (Solution solution){
		
		Solution curSolution = solution.clone();
		
		Random ran = new Random();
		
		//genereat random index i
		int i = ran.nextInt(schedule.size());
		
		//generate random index j; i!=j
		int j = -1;
		do{
			j = ran.nextInt(schedule.size());
			if (j==i) j=-1;
		}while(j == -1);
		
		//generate new solution (switch two jobs)
		curSolution.setList(switchJobs(curSolution.getList(), i, j));
		
		return curSolution;
	}
	
	private Solution getSolutionRandomMove (Solution solution){
		
		Solution curSolution = solution.clone();
		
		Random ran = new Random();
		
		//generate random index i
		int i = ran.nextInt(schedule.size());
		
		//generate random index j; i!=j
		int j = -1;
		do{
			j = ran.nextInt(schedule.size());
			if (j==i) j=-1;
		}while(j == -1);
		
		//generate new solution (move one jobs)
		curSolution.setList(moveJob(curSolution.getList(), i, j));
		
		return curSolution;
	}
	
	/**
	 * generate random solution from the split-neighborhood
	 * @param solution
	 * @return
	 */
	private Solution getSolutionRandomSplit(Solution solution){
		
		Solution curSolution = solution.clone();
		
		Random ran = new Random();
		
		//genereat random index i
		int i = ran.nextInt(schedule.size());
		
		//generate new solution (switch two jobs)
		curSolution.setList(splitJobs(curSolution.getList(), i));
		
		return curSolution;
	}
	
	/**
	 * swap job with index i with job with index j
	 * @param jobs
	 * @param i
	 * @param j
	 * @return
	 */
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
	
	/**
	 * move job with index i to index j
	 * @param jobs
	 * @param i
	 * @param j
	 * @return
	 */
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
