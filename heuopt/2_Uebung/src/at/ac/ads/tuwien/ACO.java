package at.ac.ads.tuwien;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ACO {
	
	// Define a static logger variable so that it references the
	private static Logger logger = Logger.getLogger(ACO.class);
	
	private float pheromone[][];
	
	private Solution ib = null;
	private Solution rb = null;
	private Solution bs = null;
	
	private final float THIRD = 1/3f;
	private final float TWOTHIRDS = 2/3f;
	
	private final float TAUMIN = 0.01f;
	private final float TAUMAX = 0.99f;
	
	private final int RESTRICTION_SIZE = 10;
	
	private final int MAX_ITERATIONS = 1000;
	
	private final float p = 0.1f;

	private void initPheromone() {
		
		for(int i=0; i< Input.dist.length ; i++) {
			for(int j=i; j < Input.dist.length; j++) {
				
				if(i==j)
					pheromone[i][j] = 0;
				
				pheromone[i][j] = 0.5f;
				pheromone[j][i] = 0.5f;
			}
		}
	}
	
	public void runACO(int numberOfAnts) {
		
		pheromone = new float[Input.amount][Input.amount];
		
		initPheromone();
		
		Set<Solution> constructedSolutions = new HashSet<Solution>();
		
		int iterationCounter = 1;
		
		// convergence factor
		float cf = 0;
		// bs_update
		boolean restart = false;
		double average = 0;
		
		logger.info("Initialization completed. Start ACO...");
		
		logger.setLevel(Level.INFO);
		
		while(iterationCounter <= MAX_ITERATIONS) {
		
			constructedSolutions.clear();
			for(int ant=0; ant < numberOfAnts; ant++) {
				constructedSolutions.add(LocalSearch.getVNDSolution(
						LocalSearch.getVNDSolution(constructBroadcastTree())));
			}
			
			average = 0d;
			for(Solution sol : constructedSolutions) {
				
				if(ib == null)
					ib = sol;
				else if(ib.getWeight() > sol.getWeight())
					ib = sol;
				
				average += sol.getWeight();
			}
			
			logger.info("Average solution weight: "+(average/numberOfAnts)+" best solution in "+iterationCounter+".run: "+ib.toString());
			
			// update best so far and restart best if found a better solution
			if(bs == null)
				bs = ib;
			else if(bs.getWeight() > ib.getWeight())
				bs = ib;
			
			if(rb == null)
				rb = ib;
			else if(rb.getWeight() > ib.getWeight())
				rb = ib;
			
			applyPheromoneUpdate(cf,restart);
			
			cf = computeConvergenceFactor();
			logger.info("cf: "+cf);
			
			if(cf >= 0.99f) {
				
				if(restart) {
					initPheromone();
					logger.info("Restarted! Best solution in this turn: "+rb.toString());
					rb = null;
					restart = false;
				} else {
					restart = true;
				}
			}
			
			if(iterationCounter % 10 == 1 || iterationCounter < 10) {
				if(rb != null)
					logger.info("Since restart best: "+rb.toString());
				logger.info("Best so far: "+bs.toString());
			}
			iterationCounter++;
		}	
	}
	
	private float computeConvergenceFactor() {

		float sum = 0f;
		for(Edge e : rb.getEdges()) {
			
			sum += pheromone[e.getStartNode()][e.getEndNode()];
		}
		
		return sum / ((rb.getEdges().size() - 1) * TAUMAX);
	}

	private void applyPheromoneUpdate(float cf, boolean restart) {

		float xi = 0f;
		Edge e = null;
		for(int i=0; i < Input.dist.length; i++) {
			for(int j=0; j < Input.dist.length; j++) {
				
				if(i==j)
					continue;
				
				e = new Edge(i,j);
				xi = getKib(cf,restart)*getDelta(ib,e)
						+ getKrb(cf,restart)*getDelta(rb,e)
						+ getKbs(restart)*getDelta(bs,e);

				pheromone[i][j] = Math.min(Math.max(pheromone[i][j] + (p * (xi - pheromone[i][j])), TAUMIN), TAUMAX);
				if(xi!=0)
					logger.debug("New pheromone value for edge "+e.toString()+": "+pheromone[i][j]+ " xi: "+xi);
			}
		}		
	}
	
	private int getDelta(Solution solution, Edge edge) {
			
		if(solution.getEdges().contains(edge))
			return 1;
		else return 0;
	}
	
	private float getKib(float cf, boolean restart) {
		
		if(restart)
			return 0f;
		
		if(cf < 0.7)
			return TWOTHIRDS;
		else if(cf < 0.9)
			return THIRD;
		else
			return 0f;
	}
	private float getKrb(float cf, boolean restart) {
		
		if(restart)
			return 0f;
		
		if(cf < 0.7)
			return THIRD;
		else if(cf < 0.9)
			return TWOTHIRDS;
		else
			return 1f;
	}
	private float getKbs(boolean restart) {		
		if(restart)
			return 1f;
		else 
			return 0f;
	}

	private Solution constructBroadcastTree() {
		
		Set<Integer> linkedNodes = new HashSet<Integer>();
		Set<Integer> unlinkedNodes = new HashSet<Integer>();
		int nextNode = 0;

		linkedNodes.add(0);
		for(int i=1; i<Input.amount; i++)
			unlinkedNodes.add(i);
		
		TreeSet<WeightedEdge> candidates = new TreeSet<WeightedEdge>();
		TreeSet<WeightedEdge> restrictedCandidates = new TreeSet<WeightedEdge>();
		double probabilitySum;
		double probability[] = new double[RESTRICTION_SIZE];
		
		Random rand = new Random();
		
		Solution partialSol = new Solution();
		
		while(!unlinkedNodes.isEmpty()) {
			
			candidates = updateCandidates(candidates,linkedNodes,unlinkedNodes,nextNode);
			
			restrictedCandidates.clear();			
			for(WeightedEdge e : candidates) {
				
				restrictedCandidates.add(e);
				
				if(restrictedCandidates.size() >= RESTRICTION_SIZE)
					break;
			}
			
			logger.debug("Restricted candidates: "+restrictedCandidates.toString());
			
			// choose next node according to the probability values
			probabilitySum = 0.0d;
			int i=0;
			for(WeightedEdge e : restrictedCandidates) {
				probability[i] = computeEdgeProbability(restrictedCandidates,e,partialSol);
				probabilitySum += probability[i];
				i++;
			}
			// normalize computed probabilities
			for(i=0; i < restrictedCandidates.size(); i++) {
				
				probability[i] = probability[i] / probabilitySum;
			}
			double r = rand.nextDouble();
			
			double currentSum = 0f;
			i=0;
			for(WeightedEdge e : restrictedCandidates) {
				currentSum += probability[i];
				if(currentSum >= r) {
						// next node is chosen
					nextNode = e.getEndNode();
						// update new partial solution
					partialSol.addEdge(new Edge(e.getStartNode(),e.getEndNode()));
					partialSol.computeObjectiveFunctionValue();
					
					logger.debug("Chosen edge: "+e.toString()+" with probability "+probability[i]);
					logger.debug("Updated partial solution: "+partialSol.toString());
					
					linkedNodes.add(nextNode);
					unlinkedNodes.remove(nextNode);
					
						// remove chosen link/edge from candidates list
					//logger.debug("cand old: "+candidates.toString());
					candidates.remove(e);
					//logger.debug("cand new: "+candidates.toString());
					
						// check if other nodes are also in the new coverage
					for(WeightedEdge edge : restrictedCandidates.headSet(e)) {
						if(edge.getStartNode() == e.getStartNode() && edge.getWeight() < e.getWeight()) {
							linkedNodes.add(edge.getEndNode());
							unlinkedNodes.remove(edge.getEndNode());
							
								// update new partial solution
							partialSol.addEdge(new Edge(edge.getStartNode(),edge.getEndNode()));
								// should not be needed
							//partialSol.computeObjectiveFunctionValue();
							logger.debug("Updated partial solution: "+partialSol.toString());
							candidates = updateCandidates(candidates,linkedNodes,unlinkedNodes,edge.getEndNode());
						}
					}
					break;
				}
				i++;
			}
		}
		logger.debug("Completed solution "+partialSol.toString());
		return partialSol;
	}
	
	private TreeSet<WeightedEdge> updateCandidates(TreeSet<WeightedEdge> cand, Set<Integer> linkedNodes, Set<Integer> unlinkedNodes, int lastAdded) {
		
		for(Integer node : linkedNodes) {
			cand.remove(new WeightedEdge(node,lastAdded,Input.dist[node][lastAdded]));
		}
		
		for(Integer node : unlinkedNodes) {
			cand.add(new WeightedEdge(lastAdded,node,Input.dist[lastAdded][node]));
		}
		
		return cand;
	}
	
	private double computeEdgeProbability(Set<WeightedEdge> candidates,Edge e, Solution partialSol) {
		
		Solution copiedSol = partialSol.clone();
		
		double sum = 0d;
		for(Edge candidate : candidates) {
			
			copiedSol.addEdge(candidate);
			copiedSol.computeObjectiveFunctionValue();
			sum += pheromone[candidate.getStartNode()][candidate.getEndNode()] 
			         * (1 / (copiedSol.getWeight() - partialSol.getWeight()));
			//logger.debug(" new:"+copiedSol.getWeight()+" old:"+partialSol.getWeight()+" pher:"+pheromone[candidate.getStartNode()][candidate.getEndNode()]+" sum:"+sum);
			copiedSol.removeEdge(candidate);
		}
		
		double result = 0d;
		copiedSol.addEdge(e);
		result = pheromone[e.getStartNode()][e.getEndNode()] 
		             * (1 / (copiedSol.computeObjectiveFunctionValue() - partialSol.getWeight()));
		copiedSol.removeEdge(e);
		//logger.debug("r:"+result+" sum: "+sum);
		logger.debug("Computed probability for edge "+e.toString()+": "+result/sum);
		
		return result / sum;
	}
}
