package at.ac.ads.tuwien;

import java.util.HashSet;
import java.util.Set;

public class Solution implements Cloneable {

	private double weight;
	
	private Set<Edge> edges;
	
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
	
	public double computeObjectiveFunctionValue() {
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
		
		return sum;
	}
}
