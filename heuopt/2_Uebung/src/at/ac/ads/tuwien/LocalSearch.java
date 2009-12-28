package at.ac.ads.tuwien;

import java.util.ArrayList;
import java.util.List;

public class LocalSearch {

	public Solution getVNDSolution (Solution sol) {
		int rmax = 3;
		
		Solution bestSolution = sol;
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
		
		return bestSolution;
	}
	
	private Solution getRShrinkSolution (Solution sol, int r) {
		
		boolean nextParent = true;
		int level = sol.getLevelNodes().size()-1;
		
		int curLevel = level;
		while (curLevel > 0) {
			int numPar = sol.getLevelNodes().get(curLevel).size();
			
			int curNumParent = 0;
			while (curNumParent < numPar) {
				List<Integer> redNodes = new ArrayList<Integer>();
				int curParent = (Integer)sol.getLevelNodes().get(curLevel).
														toArray()[curNumParent];
				for (int i=0; i < r; i++) {
					redNodes.add(sol.getNeighbor().get(curParent).get(i));
				}
				
				for (int node : redNodes) {
					int bestParent = curParent;
					
					for (int parNode : sol.getLevelNodes().get(curLevel)) {
						if (Input.dist[bestParent][node] > Input.dist[parNode][node]) {
							bestParent = parNode;
						}
					}
					if (bestParent!=curParent) {
						nextParent=false;
						sol.getNeighbor().get(curParent).remove(node);
						sol.addOrderedNeighbor(curParent, node);
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
