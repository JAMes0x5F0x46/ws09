package at.ac.ads.tuwien;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Solution implements Cloneable {

	private double weight;
	
	private Set<Edge> edges;
	
	//Map: key=level value=node
	private Map<Integer, Set<Integer>> levelNodes;
	
	//Map: key=node value level
	private Map<Integer, Integer> levelOfNode;
	
	//TODO list should be sorted
	//Map: key=node value=list of neighbors
	private Map<Integer, List<Integer>> neighbor;
	
	public Solution() {
		
		this.edges = new HashSet<Edge>();
		this.levelNodes = new HashMap<Integer, Set<Integer>>();
		this.neighbor = new HashMap<Integer, List<Integer>>();
		this.levelOfNode = new HashMap<Integer, Integer>();
		levelOfNode.put(0, 0);
		this.weight = 0d;
	}
	
	public Solution(Set<Edge> edges) {
		
		this.edges = edges;
		this.levelNodes = new HashMap<Integer, Set<Integer>>();
		this.neighbor = new HashMap<Integer, List<Integer>>();
		this.levelOfNode = new HashMap<Integer, Integer>();
		levelOfNode.put(0, 0);
		computeObjectiveFunctionValue();
	}

	public Solution(double weight, Set<Edge> edges) {
		
		this.edges = edges;
		this.levelNodes = new HashMap<Integer, Set<Integer>>();
		this.neighbor = new HashMap<Integer, List<Integer>>();
		this.levelOfNode = new HashMap<Integer, Integer>();
		levelOfNode.put(0, 0);
		this.weight = weight;
	}

	
	
	/**
	 * @param weight
	 * @param edges
	 * @param levelNodes
	 * @param levelOfNode
	 * @param neighbor
	 */
	public Solution(double weight, Set<Edge> edges,
			Map<Integer, Set<Integer>> levelNodes,
			Map<Integer, Integer> levelOfNode,
			Map<Integer, List<Integer>> neighbor) {
		super();
		this.weight = weight;
		this.edges = edges;
		this.levelNodes = levelNodes;
		this.levelOfNode = levelOfNode;
		this.neighbor = neighbor;
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
		int i=0;
		for (i=0; i<this.neighbor.get(parent).size(); i++) {
			if (Input.dist[parent][neighbor.get(parent).get(i)] < Input.dist[parent][newNeighbor]){
				break;
			}
		}
		neighbor.get(parent).add(i, newNeighbor);
		
//		boolean added = false;
//		for (int node : neighbor.get(parent)) {
//			if (Input.dist[parent][node] < Input.dist[parent][newNeighbor]&&!!added){
//				added=true;
//				newNeighbors.add(newNeighbor);
//				
//			}
//			newNeighbors.add(node);
//		}
//		if (!added) {
//			newNeighbors.add(newNeighbor);
//		}
//		neighbor.remove(parent);
//		neighbor.put(parent, newNeighbors);
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
		this.addOrderedNeighbor(e.getStartNode(), e.getEndNode());
		int level = levelOfNode.get(e.getStartNode())+1;
		this.levelOfNode.put(e.getEndNode(), level);
		
		if (levelNodes.containsKey(level)) {
			this.levelNodes.get(level).add(e.endNode);
		} else {
			Set<Integer> nodes = new HashSet<Integer>();
			nodes.add(e.endNode);
			levelNodes.put(level, nodes);
		}
		
	}
	public void removeEdge(Edge e) {
		this.edges.remove(e);
	}
	
	@Override
	public String toString() {
		 return "weight: "+this.weight + ", edges: "+this.edges.toString();
	}

	@Override
	public Solution clone() {
		
		Set<Edge> newEdges = new HashSet<Edge>();
	
		Map<Integer, Set<Integer>> newMaplevelNodes = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Integer> newMaplevelOfNode = new HashMap<Integer, Integer>();
		Map<Integer, List<Integer>> newMapneighbor = new HashMap<Integer, List<Integer>>();
		
		for(Edge edge : this.edges) {
			newEdges.add(edge);
		}
		
		for (Entry entry : this.levelNodes.entrySet()) {
			Set<Integer> newlevelNodes = new HashSet<Integer>();
			for (int node : (Set<Integer>)entry.getValue()) {
				newlevelNodes.add(node);
			}
			newMaplevelNodes.put((Integer)entry.getKey(), newlevelNodes);
		}
		
		for (Entry entry : this.neighbor.entrySet()) {
			List<Integer> newneighbor = new ArrayList<Integer>();
			for (int node : (List<Integer>)entry.getValue()) {
				newneighbor.add(node);
			}
			newMapneighbor.put((Integer)entry.getKey(), newneighbor);
		}
		
		for (Entry entry : this.levelOfNode.entrySet()) {
			newMaplevelOfNode.put((Integer)entry.getKey(), (Integer)entry.getValue());
		}
		
		return new Solution(this.weight, newEdges,newMaplevelNodes,newMaplevelOfNode,newMapneighbor);
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
