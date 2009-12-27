package at.ac.ads.tuwien;

import java.util.HashSet;
import java.util.Set;

public class Solution implements Cloneable{

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
	
	@Override
	public Solution clone() {
		
		Set<Edge> newEdges = new HashSet<Edge>();
		
		for(Edge edge : this.edges) {
			newEdges.add(edge);
		}
		
		return new Solution(this.weight, newEdges);
	}
}
