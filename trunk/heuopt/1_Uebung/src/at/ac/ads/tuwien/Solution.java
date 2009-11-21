package at.ac.ads.tuwien;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Solution implements Cloneable{
	
	private List<Integer> list = null;
	private List<Set<Integer>> magazineConfiguration = null;
	
	private int costs = 0;

	public Solution(int startjob) {

		this.list = new ArrayList<Integer>();
		this.list.add(startjob);
		this.costs = 0;
	}
	
	public Solution() {

		this.list = new ArrayList<Integer>();
		this.costs = 0;
		this.magazineConfiguration = new ArrayList<Set<Integer>>();
	}
	
	@Override
	public String toString() {
		
		return this.costs+": "+this.list.toString();
	}
	
	public void addJob(int nextJob, int costs) {
		
		this.list.add(nextJob);
		this.costs += costs;
	}
	
	public void addJob(int nextJob, int costs,Set<Integer> magazine) {
		
		this.list.add(nextJob);
		this.magazineConfiguration.add(magazine);
		this.costs += costs;
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

	public List<Set<Integer>> getMagazineConfiguration() {
		return magazineConfiguration;
	}

	public void setMagazineConfiguration(List<Set<Integer>> magazineConfiguration) {
		this.magazineConfiguration = magazineConfiguration;
	}
	
	@Override
	public Solution clone(){
		Solution sol = new Solution();
		for (int i=0; i<this.getList().size(); i++){
			sol.getList().add(this.getList().get(i));
			Set<Integer> magazin = new HashSet<Integer>();
			if (this.getMagazineConfiguration()!=null){
				for (int mag : this.getMagazineConfiguration().get(i)){
					magazin.add(mag);
				}
			}
			sol.getMagazineConfiguration().add(magazin);
		}
		sol.setCosts(this.getCosts());
		return sol;
	}
}
