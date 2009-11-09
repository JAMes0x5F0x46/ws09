package at.ac.ads.tuwien;

import java.util.ArrayList;
import java.util.List;

public class Solution {
	
	private List<Integer> list = null;
	
	private int costs = 0;

	public Solution(int startjob) {

		this.list = new ArrayList<Integer>();
		this.list.add(startjob);
		this.costs = 0;
	}

	public List<Integer> getList() {
		return list;
	}

	public void setList(List<Integer> list) {
		this.list = list;
	}

	public int getCosts() {
		return costs;
	}

	public void setCosts(int costs) {
		this.costs = costs;
	}
	
	public void addCosts(int costs) {
		this.costs += costs;
	}
}
