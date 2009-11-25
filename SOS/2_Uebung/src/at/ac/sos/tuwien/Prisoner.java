package at.ac.sos.tuwien;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class Prisoner extends Agent {
	
	private Strategy strategy;
	
	private Response ownLastDecision;

	protected void setup() {
		
		
		if (getArguments() != null && getArguments().length >= 1){
			strategy = Strategy.create(Integer.valueOf(getArguments()[0].toString()));
		
			System.out.println("Setup Agent: " + getAID().getName() + " strategy: " + strategy.toString());
			
			addBehaviour(new myOneShot(this));
		}else{
			System.err.println("no strategy was given.");
			doDelete();
		}
		
	}
	
	private class myOneShot extends OneShotBehaviour {

		/**
		 * @param a
		 */
		public myOneShot(Agent a) {
			super(a);
		}
		
		public void action() {
			System.out.println("Action Agent: " + getAID().getName());
			
			Response lastDecisionOfTeammate=null;
			
			if (myAgent.getArguments().length > 1 && myAgent.getArguments()[1]!=null){
				lastDecisionOfTeammate = Response.create(myAgent.getArguments()[1].toString());
			}
			
			Response response = getResponse(lastDecisionOfTeammate);
			
			ACLMessage msg = new ACLMessage (ACLMessage.INFORM);
			msg.addReceiver(new AID("guard", AID.ISLOCALNAME));
			msg.setLanguage("English");
			msg.setContent(response.toString());
			send (msg);
			System.out.println(getAID().getName() + " sended decision: " + response);
	    } 
		
	    public int onEnd() {
	    	this.myAgent.doDelete();
	    	return 0;
	    }
	}
	
	private Response getResponse(Response lastDecisionOfTeammate) {
		
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
				else if(ownLastDecision == Response.BETRAY || lastDecisionOfTeammate == Response.BETRAY)
					return Response.BETRAY;
				else
					return Response.HUSH;
			}
			
			
			default: return null;
		}
	}
	
}
