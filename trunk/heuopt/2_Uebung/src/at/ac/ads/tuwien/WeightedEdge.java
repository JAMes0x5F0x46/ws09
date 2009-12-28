package at.ac.ads.tuwien;

public class WeightedEdge extends Edge implements Comparable<WeightedEdge> {
	
	private final double weight;
	
	public WeightedEdge(int startNode, int endNode, double weight) {
		
		super(startNode,endNode);
		this.weight = weight;
	}
	
	@Override
	public boolean equals(Object o) {
		
		 if (this == o) return true;
		 
		 if (o == null || getClass() != o.getClass()) return false;
		   
		 WeightedEdge edge = (WeightedEdge) o;
		  
		 if(edge.startNode != this.startNode || edge.endNode != this.endNode || edge.weight != this.weight)
			 return false;
		 else  
			 return true;
	}
	
	@Override
	public int hashCode() {
		 return (int) (this.weight + 29 * (this.endNode + 37 * this.startNode));
	}
	
	@Override
	public int compareTo(WeightedEdge e) {
		
		if(e == this)
			return 0;
		
		if(this.weight < e.weight) {
			return -1;
		} else if(this.weight > e.weight) {		
			return 1;
		} 
			
		if(this.startNode < e.startNode) {
			return -1;
		} else if(this.startNode > e.startNode) {
			return 1;
		}
		
		if(this.endNode < e.endNode) {
			return -1;
		} else if(this.endNode > e.endNode) {
			return 1;
		}
			
		return 0;
	}
	
	@Override
	public String toString() {
		 return this.weight+": "+this.startNode + "->"+this.endNode;
	}

	public double getWeight() {
		return weight;
	}

}
