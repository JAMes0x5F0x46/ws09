package at.ac.sos.tuwien;

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
			
//			String args[] = new String[1];
//			args[0]="test";
			
			try {
				AgentController firstPrisoner = myAgent.getContainerController().
										createNewAgent("first_prisoner", "at.ac.sos.tuwien.Prisoner", null);
				
				AgentController secondPrisoner = myAgent.getContainerController().
										createNewAgent("second_prisoner", "at.ac.sos.tuwien.Prisoner", null);

				System.out.println(Response.create("HUSH"));
				firstPrisoner.start();
				secondPrisoner.start();
				
				ACLMessage msg = null;
				for (int i=0; i < 2; i ++){
					msg = myAgent.blockingReceive();
					if (msg.getSender().equals("first_prisoner")){
						lastDecisionFirst=Response.create(msg.getContent());
					}else if (msg.getSender().equals("second_prisoner")){
						lastDecisionSecond=Response.create(msg.getContent());
					}
				}
				
				System.out.println("DecisionFirst: " + lastDecisionFirst);
				System.out.println("DecisionSecond: " + lastDecisionSecond);
				
				
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
