package at.ac.ads.tuwien;

import java.util.Set;

public class Solution {

	private final double weight;
	
	private final Set<Edge> edges;
	
	public Solution(double weight, Set<Edge> edges) {
		
		this.weight = weight;
		this.edges = edges;
	}

	public double getWeight() {
		return weight;
	}

	public Set<Edge> getEdges() {
		return edges;
	}
}
