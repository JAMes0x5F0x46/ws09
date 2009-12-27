package at.ac.ads.tuwien;

import java.util.Set;

public class ACO {
	
	private float pheromone[][];

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
		
		for(int ant=0; ant < numberOfAnts; ant++) {
			
			
		}
	}
	
	private Set<Edge> constructBroadcastTree() {
		
		return null;
	}
}
