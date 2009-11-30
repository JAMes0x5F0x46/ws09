package at.ac.sos.tuwien;

import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Guard extends Agent{
	
	private static final long serialVersionUID = 1L;

	protected void setup() {
		
	    addBehaviour(new GuardBehaviour(this));
		
	}
	
	private class GuardBehaviour extends OneShotBehaviour {

		private static final long serialVersionUID = 1L;
		
		Response lastDecisionFirst;
		Response lastDecisionSecond;
		
		int penaltyFirst;
		int penaltySecond;
		
		private int scoreFirst = 0;
		private int scoreSecond = 0;
		
		private Strategy strFirst;
		private Strategy strSecond;
		
		private ACLMessage sendMsgFirst;
		private ACLMessage sendMsgSecond;
		
		AgentController firstPrisoner;
		AgentController secondPrisoner;
		/**
		 * @param a
		 */
		public GuardBehaviour(Agent a) {
			super(a);
			
			try {
				firstPrisoner = myAgent.getContainerController().
					createNewAgent("first_prisoner", "at.ac.sos.tuwien.Prisoner", null);
				secondPrisoner = myAgent.getContainerController().
					createNewAgent("second_prisoner", "at.ac.sos.tuwien.Prisoner", null);

				firstPrisoner.start();
				secondPrisoner.start();
				
				// configuration of ACL Messages
				sendMsgFirst = new ACLMessage (ACLMessage.INFORM);
				sendMsgFirst.addReceiver(new AID("first_prisoner", AID.ISLOCALNAME));
				sendMsgFirst.setLanguage("English");
				
				sendMsgSecond = new ACLMessage (ACLMessage.INFORM);
				sendMsgSecond.addReceiver(new AID("second_prisoner", AID.ISLOCALNAME));
				sendMsgSecond.setLanguage("English");
				
			} catch (StaleProxyException e) {
				System.err.println("can't create Agents.");
				myAgent.doDelete();
			}
		}
		
		public void action() {
			
			System.out.println("Guard-action");
			
			Random rand = new Random();
			//TODO new strategies
			strFirst = Strategy.create(rand.nextInt(Strategy.values().length-2));
			strSecond = Strategy.create(rand.nextInt(Strategy.values().length-2));
			
			for (int j=1; j <= Prison.RUNS; j++){
				
				System.out.println("run " + j + ":");
				
				
				sendMsgFirst.setContent(String.valueOf(strFirst.getStrategy()));
				send(sendMsgFirst);
				
				
				sendMsgSecond.setContent(String.valueOf(strSecond.getStrategy()));
				send(sendMsgSecond);
				
				// waiting for answers of the 2 players
				ACLMessage msg = null;
				for (int i=0; i < 2; i ++){
					
					msg = myAgent.blockingReceive();
					String name = msg.getSender().getName().substring(0, msg.getSender().getName().indexOf("@"));
					
					if (name.equals("first_prisoner")) {
						lastDecisionFirst=Response.create(msg.getContent());
					} else if (name.equals("second_prisoner")){
						lastDecisionSecond=Response.create(msg.getContent());
					}
				}
				
				System.out.println("DecisionFirst: " + lastDecisionFirst);
				System.out.println("DecisionSecond: " + lastDecisionSecond);
				
				sendMsgFirst.setContent(lastDecisionSecond.toString());
				send(sendMsgFirst);
				
				sendMsgSecond.setContent(lastDecisionFirst.toString());
				send(sendMsgSecond);
				
				getPenalty();

				// send judgment to prisoners
				sendMsgFirst.setContent(String.valueOf(penaltyFirst)+":"+String.valueOf(penaltySecond));
				send(sendMsgFirst);
				
				sendMsgSecond.setContent(String.valueOf(penaltyFirst)+":"+String.valueOf(penaltySecond));
				send(sendMsgSecond);
				
				System.out.println("penalty: ");
				System.out.println("First agent: " + penaltyFirst);
				System.out.println("Second agent: " + penaltySecond);
			}
			
			// shutdown players/agents
			sendMsgFirst.setContent("stop");
			send(sendMsgFirst);			
			sendMsgSecond.setContent("stop");
			send(sendMsgSecond);
			
			System.out.println("Final score:");
			System.out.println("First agent: "+scoreFirst+" strategy: "+strFirst.toString());
			System.out.println("Second agent: "+scoreSecond+" strategy: "+strSecond.toString());
			
	    } 
		
	    public int onEnd() {
	    	this.myAgent.doDelete();
	    	return 0;
	    }
	    
	    private void getPenalty() {
	    	
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
	    	
	    	scoreFirst += penaltyFirst;
	    	scoreSecond += penaltySecond;
	    }
	    
	}
}
