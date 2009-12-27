package at.ac.ads.tuwien;

import java.util.ArrayList;
import java.util.List;

public class Input {
	
	private int amount;
	private List<List<Double>> dist;
	
	/**
	 * @param dist
	 */
	public Input() {
		this.dist = new ArrayList<List<Double>>();
	}
	/**
	 * @return the amount
	 */
	public int getAmount() {
		return amount;
	}
	/**
	 * @param amount the amount to set
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}
	/**
	 * @return the dist
	 */
	public List<List<Double>> getDist() {
		return dist;
	}
	
	public void addDistList (List<Double> distList) {
		dist.add(distList);
	}
	
	/**
	 * @param dist the dist to set
	 */
	public void setDist(List<List<Double>> dist) {
		this.dist = dist;
	}
	
	

}
