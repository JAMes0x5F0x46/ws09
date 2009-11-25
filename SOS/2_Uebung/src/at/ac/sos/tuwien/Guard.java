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
		
		int penaltyFirst;
		int penaltySecond;
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
					if (lastDecisionSecond!=null){
						argsfirst[1]=lastDecisionSecond.toString();
					}else{
						argsfirst[1]=null;
					}
					
					AgentController firstPrisoner = myAgent.getContainerController().
											createNewAgent("first_prisoner", "at.ac.sos.tuwien.Prisoner", argsfirst);
					
					String argssecond[] = new String[2];
					argssecond[0]=String.valueOf(rand.nextInt(Strategy.values().length));
					if (lastDecisionFirst!=null){
						argssecond[1]=lastDecisionFirst.toString();
					}else{
						lastDecisionFirst=null;
					}
					
					AgentController secondPrisoner = myAgent.getContainerController().
											createNewAgent("second_prisoner", "at.ac.sos.tuwien.Prisoner", argssecond);
	
					System.out.println(Response.create("HUSH"));
					firstPrisoner.start();
					secondPrisoner.start();
					
					ACLMessage msg = null;
					for (int i=0; i <= 1; i ++){
						
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
					
					
					if (lastDecisionFirst==Response.HUSH && lastDecisionSecond==Response.HUSH){
						penaltyFirst=2;
						penaltySecond=2;
					}else if (lastDecisionFirst==Response.HUSH && lastDecisionSecond==Response.BETRAY){
						penaltyFirst=5;
						penaltySecond=0;
					}else if (lastDecisionFirst==Response.BETRAY && lastDecisionSecond==Response.HUSH){
						penaltyFirst=0;
						penaltySecond=5;
					}else if (lastDecisionFirst==Response.BETRAY && lastDecisionSecond==Response.BETRAY){
						penaltyFirst=4;
						penaltySecond=4;
					}
					System.out.println("penalty: ");
					System.out.println("First agent: " + penaltyFirst);
					System.out.println("Second agent: " + penaltySecond);
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
