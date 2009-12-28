package at.ac.ads.tuwien;

public class Edge {

	protected final int startNode;
	protected final int endNode;
	
	public Edge(int startNode, int endNode) {
		
		this.startNode = startNode;
		this.endNode = endNode;
	}
	
	@Override
	public boolean equals(Object o) {
		
		 if (this == o) return true;
		 
		 if (o == null || getClass() != o.getClass()) return false;
		   
		 Edge edge = (Edge) o;
		  
		 if(edge.startNode != this.startNode || edge.endNode != this.endNode)
			 return false;
		 else  
			 return true;
	}
	
	@Override
	public int hashCode() {
		 return this.endNode + 37 * this.startNode;
	}
	
	@Override
	public String toString() {
		 return this.startNode + "->"+this.endNode;
	}

	public int getStartNode() {
		return startNode;
	}

	public int getEndNode() {
		return endNode;
	}
}
