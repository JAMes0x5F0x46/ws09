package at.ac.sos.tuwien;

import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Guard extends Agent{
	
	protected void setup() {
		
	    addBehaviour(new myOneShot(this));
		
	}
	
	private class myOneShot extends OneShotBehaviour {

		Response lastDecisionFirst;
		Response lastDecisionSecond;
		/**
		 * @param a
		 */
		public myOneShot(Agent a) {
			super(a);
		}
		
		public void action() {
			System.out.println("Guard-action");
			
			Random rand = new Random();
			
			try {
				for (int j=0; j < 1; j++){
				
					String argsfirst[] = new String[2];
					argsfirst[0]= String.valueOf(rand.nextInt(Strategy.values().length));
					argsfirst[1]=lastDecisionSecond.toString();
					
					AgentController firstPrisoner = myAgent.getContainerController().
											createNewAgent("first_prisoner", "at.ac.sos.tuwien.Prisoner", argsfirst);
					
					String argssecond[] = new String[2];
					argssecond[0]=String.valueOf(rand.nextInt(Strategy.values().length));
					argssecond[1]=lastDecisionFirst.toString();
					
					AgentController secondPrisoner = myAgent.getContainerController().
											createNewAgent("second_prisoner", "at.ac.sos.tuwien.Prisoner", argssecond);
	
					System.out.println(Response.create("HUSH"));
					firstPrisoner.start();
					secondPrisoner.start();
					
					ACLMessage msg = null;
					for (int i=0; i < 2; i ++){
						
						msg = myAgent.blockingReceive();
						String name = msg.getSender().getName().substring(0, msg.getSender().getName().indexOf("@"));
						if (name.equals("first_prisoner")){
							lastDecisionFirst=Response.create(msg.getContent());
						}else if (name.equals("second_prisoner")){
							lastDecisionSecond=Response.create(msg.getContent());
						}
					}
					
					System.out.println("DecisionFirst: " + lastDecisionFirst);
					System.out.println("DecisionSecond: " + lastDecisionSecond);
				}
				
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
	    } 
		
	    public int onEnd() {
	    	this.myAgent.doDelete();
	    	return 0;
	    }
	}
}
