package at.ac.ads.tuwien;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LocalSearch {

	// Define a static logger variable so that it references the
	private static Logger logger = Logger.getLogger(LocalSearch.class);
	
	public static Solution getVNDSolution (Solution sol) {
		
		logger.setLevel(Level.INFO);
		int rmax = Input.amount-1;
		double oldWeight = sol.getWeight();
		Solution bestSolution = sol;
		
		bestSolution.computeObjectiveFunctionValue();
		Solution curSolution;
		
		int r = 1;
		while (r <= rmax) {
			curSolution = getRShrinkSolution(bestSolution, r);
			curSolution.computeObjectiveFunctionValue();
			if (curSolution.getWeight() < bestSolution.getWeight()) {
				bestSolution = curSolution;
				r=1;
			} else {
				r++;
			}
		}
		
		if(oldWeight > bestSolution.getWeight())
			logger.debug("Local search improved solution to "+bestSolution.toString());
		
		return bestSolution;
	}
	
	private static Solution getRShrinkSolution (Solution solution, int r) {
		Solution sol = solution.clone();
		boolean nextParent = true;
		int level = sol.getLevelNodes().size()-2;
		
		int curLevel = level;
		while (curLevel > 0) {
			int numPar = sol.getLevelNodes().get(curLevel).size();
			
//			logger.debug("Level: " + curLevel + "; numPar: " + numPar);
//			logger.debug("r = " + r);
			int curNumParent = 0;
			while (curNumParent < numPar) {
				nextParent=true;
				List<Integer> redNodes = new ArrayList<Integer>();
				int curParent = (Integer)sol.getLevelNodes().get(curLevel).
														toArray()[curNumParent];
				if (sol.getNeighbor().containsKey(curParent)) {
//					logger.debug("curNumParent: " + curNumParent + " ;curParent: " + curParent);
//					logger.debug("List: " + sol.getNeighbor());
//					logger.debug("ParentList: " + sol.getNeighbor().get(curParent));
					for (int i=0; i < r; i++) {
						if (i >= sol.getNeighbor().get(curParent).size()) break;
						redNodes.add(sol.getNeighbor().get(curParent).get(i));
					}
					
					for (int node : redNodes) {
						int bestParent = curParent;
						
						for (int parNode : sol.getLevelNodes().get(curLevel)) {
							if (Input.dist[bestParent][node] > Input.dist[parNode][node]) {
								bestParent = parNode;
							}
						}
//						logger.debug("bestParent: " + bestParent + " curParent: "+ curParent);
						if (bestParent!=curParent) {
//							logger.debug("node: " + node);
//							logger.debug("List: " + sol.getNeighbor());
//							logger.debug("bevor cost: " + sol.getWeight());
							nextParent=false;
							sol.removeEdge(new Edge(curParent, node));
							sol.addEdge(new Edge(bestParent, node));
							sol.getNeighbor().get(curParent).remove(sol.getNeighbor().get(curParent).indexOf(node));
							sol.addOrderedNeighbor(bestParent, node);
							sol.computeObjectiveFunctionValue();
//							logger.debug("after cost: " + sol.getWeight());
//							logger.debug("After List: " + sol.getNeighbor());
//							logger.debug("After ParentList: " + sol.getNeighbor().get(curParent));
						}
					}
				}
				if (nextParent){
					curNumParent++;
				}
			}
			curLevel--;
		}
		
		return sol;
	}
}
