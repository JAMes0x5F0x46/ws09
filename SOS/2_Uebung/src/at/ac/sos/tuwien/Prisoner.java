package at.ac.sos.tuwien;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class Prisoner extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Strategy strategy;
	
	private Response myDecision=null;
	private Response lastDecisionOfTeammate=null;
	private int lastPenaltyFirst;
	private int lastPenaltySecond;
	private ACLMessage sendMsg;

	protected void setup() {
		
		// configure agent
		sendMsg = new ACLMessage (ACLMessage.INFORM);
		sendMsg.addReceiver(new AID("guard", AID.ISLOCALNAME));
		sendMsg.setLanguage("English");
		
		addBehaviour(new PrisonerBehaviour(this));
		
	}
	
	private class PrisonerBehaviour extends CyclicBehaviour {
		
		private static final long serialVersionUID = 1L;
		
		public PrisonerBehaviour(Agent a) {
			super(a);
		}
		
		public void action() {
			
			ACLMessage instruction = myAgent.blockingReceive();
			
			//if first command is stop then stop agent
			if (instruction.getContent().equals("stop")){
				myAgent.doDelete();
				return;
			}

			strategy = Strategy.create(Integer.valueOf(instruction.getContent().toString()));
			
			System.out.println("strategy: " + strategy);
			
			//generate decision
			Response response = getResponse();
			
			//send my decision to guard
			sendMsg.setContent(response.toString());
			send (sendMsg);
			System.out.println(getAID().getName() + " sended decision: " + response);
			
			//waiting for decision of other agent
			ACLMessage other = myAgent.blockingReceive();
			lastDecisionOfTeammate = Response.create(other.getContent());
			
			//waiting for penalty
			ACLMessage penalty = myAgent.blockingReceive();
			String[] pen = penalty.getContent().split(":");
			lastPenaltyFirst = Integer.valueOf(pen[0]);
			lastPenaltySecond = Integer.valueOf(pen[1]);
			
	    } 
		
	}
	
	private Response getResponse() {
		
		switch (this.strategy) {
		
			case TITFORTAT: {
				if(lastDecisionOfTeammate == null)
					return Response.HUSH;
				else
					return lastDecisionOfTeammate;
			}
			case MISTRUST: {
				if(lastDecisionOfTeammate == null)
					return Response.BETRAY;
				else
					return lastDecisionOfTeammate;
			}
			case SPITE: {
				if(lastDecisionOfTeammate == null) 
					return Response.HUSH;
				else if(myDecision == Response.BETRAY || lastDecisionOfTeammate == Response.BETRAY)
					return Response.BETRAY;
				else
					return Response.HUSH;
			}
			
			
			default: return null;
		}
	}
	
}
