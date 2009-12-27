package at.ac.ads.tuwien;

import java.util.HashSet;
import java.util.Set;

public class ACO {
	
	private float pheromone[][];
	
	private Solution ib = null;
	private Solution rb = null;
	private Solution bs = null;
	
	private final float THIRD = 1/3f;
	private final float TWOTHIRDS = 2/3f;

	private void initPheromone() {
		
		for(int i=0; i< Input.dist.length ; i++) {
			for(int j=i; j < Input.dist.length; j++) {
				pheromone[i][j] = 0.5f;
				pheromone[j][i] = 0.5f;
			}
		}
	}
	
	public void runACO(int numberOfAnts) {
		
		initPheromone();
		
		Set<Set<Edge>> constructedSolutions = new HashSet<Set<Edge>>();
		
		int iterationCounter = 1;
		
		// convergence factor
		float cf = 0;
		// bs_update
		boolean restart = false;
		
		while(iterationCounter < 100) {
		
			for(int ant=0; ant < numberOfAnts; ant++) {
				
				constructedSolutions.add(constructBroadcastTree());
			}
			
			Solution computedSolution;
			for(Set<Edge> edges : constructedSolutions) {
				
				computedSolution = createSolution(edges);
				if(ib == null)
					ib = computedSolution;
				else if(ib.getWeight() > computedSolution.getWeight())
					ib = computedSolution;
			}
			
			// update best so far and restart best if found a better solution
			if(bs == null)
				bs = ib;
			else if(bs.getWeight() > ib.getWeight())
				bs = ib;
			
			if(rb == null)
				rb = ib;
			else if(rb.getWeight() > ib.getWeight())
				rb = ib;
			
			applyPheromoneUpdate(cf,restart);
			
			cf = computeConvergenceFactor();
			
			if(cf >= 0.99f) {
				
				if(restart) {
					initPheromone();
					rb = null;
					restart = false;
				} else {
					restart = true;
				}
			}
			
			iterationCounter++;
		}	
	}
	
	private float computeConvergenceFactor() {
		// TODO Auto-generated method stub
		return 0;
	}

	private void applyPheromoneUpdate(float cf, boolean restart) {


		float xi = 0f;
		Edge e = null;
		for(int i=0; i < Input.dist.length; i++) {
			for(int j=0; j < Input.dist.length; j++) {
				
				e = new Edge(i,j);
				xi = getKib(cf,restart)*getDelta(ib,e)
						+ getKrb(cf,restart)*getDelta(rb,e)
						+ getKbs(restart)*getDelta(bs,e);
			}
		}
		
	}
	
	private int getDelta(Solution solution, Edge edge) {
		
		if(solution.getEdges().contains(edge))
			return 1;
		else return 0;
	}
	
	private float getKib(float cf, boolean restart) {
		if(restart)
			return 0f;
		
		if(cf < 0.7)
			return TWOTHIRDS;
		else if(cf < 0.9)
			return THIRD;
		else
			return 0f;
	}
	private float getKrb(float cf, boolean restart) {	
		if(restart)
			return 0f;
		
		if(cf < 0.7)
			return THIRD;
		else if(cf < 0.9)
			return TWOTHIRDS;
		else
			return 1f;
	}
	private float getKbs(boolean restart) {		
		if(restart)
			return 1f;
		else 
			return 0f;
	}

	private Set<Edge> constructBroadcastTree() {
		
		return null;
	}
	
	private Solution createSolution(Set<Edge> edges) {
		
		return new Solution(computeObjectiveFunctionValue(edges),edges);
	}
	
	private double computeObjectiveFunctionValue(Set<Edge> edges) {
		
		return 0;
	}
}
