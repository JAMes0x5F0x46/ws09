package at.ac.ads.tuwien;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Solution implements Cloneable {

	private double weight;
	
	private Set<Edge> edges;
	
	//Map: key=level value=node
	private Map<Integer, Set<Integer>> levelNodes;
	
	//TODO list should be sorted
	//Map: key=node value=list of neighbors
	private Map<Integer, List<Integer>> neighbor;
	
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

	/**
	 * @return the levelNodes
	 */
	public Map<Integer, Set<Integer>> getLevelNodes() {
		return levelNodes;
	}

	/**
	 * @param levelNodes the levelNodes to set
	 */
	public void setLevelNodes(Map<Integer, Set<Integer>> levelNodes) {
		this.levelNodes = levelNodes;
	}

	/**
	 * @return the neighbor
	 */
	public Map<Integer, List<Integer>> getNeighbor() {
		return neighbor;
	}

	/**
	 * @param neighbor the neighbor to set
	 */
	public void setNeighbor(Map<Integer, List<Integer>> neighbor) {
		this.neighbor = neighbor;
	}
	
	public void addOrderedNeighbor (int parent, int newNeighbor){
		List<Integer> newNeighbors = new ArrayList<Integer>();
		if (!neighbor.containsKey(parent)){
			newNeighbors.add(newNeighbor);
			neighbor.put(parent, newNeighbors);
			return;
		}
		
		for (int node : neighbor.get(parent)) {
			if (Input.dist[parent][node] < Input.dist[parent][newNeighbor]){
				newNeighbors.add(newNeighbor);
			}
			newNeighbors.add(node);
		}
		neighbor.remove(parent);
		neighbor.put(parent, newNeighbors);
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * @param edges the edges to set
	 */
	public void setEdges(Set<Edge> edges) {
		this.edges = edges;
	}
	
	public void addEdge(Edge e) {
		this.edges.add(e);
	}
	public void removeEdge(Edge e) {
		this.edges.remove(e);
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
		
		this.weight = sum;
		
		return this.weight;
	}
}
