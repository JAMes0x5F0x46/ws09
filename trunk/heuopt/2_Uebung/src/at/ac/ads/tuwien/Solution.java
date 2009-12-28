package at.ac.ads.tuwien;

import java.util.HashSet;
import java.util.Set;

public class Solution implements Cloneable {

	private double weight;
	
	private Set<Edge> edges;
	
	public Solution(Set<Edge> edges) {
		
		this.edges = edges;
		computeObjectiveFunctionValue();
	}
	public Solution(double weight, Set<Edge> edges) {
		
		this.edges = edges;
		this.weight = weight;
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
	
	public void computeObjectiveFunctionValue() {
		double[] maxdist = new double[Input.amount];
		
		for (Edge edge : edges) {
			if (maxdist[edge.getStartNode()] < Input.dist[edge.getStartNode()][edge.getEndNode()]) {
				maxdist[edge.getStartNode()] = Input.dist[edge.getStartNode()][edge.getEndNode()];
			}
		}
		
		double sum = 0.0;
		for (int i = 0; i < Input.amount; i++) {
			sum += maxdist[i];
		}
		
		this.weight = sum;
	}
}
