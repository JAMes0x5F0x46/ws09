package at.ac.ads.tuwien;

public final class Edge {

	private final int startNode;
	private final int endNode;
	
	public Edge(int startNode, int endNode) {
		
		this.startNode = startNode;
		this.endNode = endNode;
	}

	public int getStartNode() {
		return startNode;
	}

	public int getEndNode() {
		return endNode;
	}
}
