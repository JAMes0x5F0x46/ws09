package at.ac.sos.tuwien;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class Prisoner extends Agent {
	
	private Strategy strategy;
	
	private Response ownLastDecision;

	protected void setup() {
		System.out.println("Hello World!");
		
	    addBehaviour(new myOneShot(this));
		
	}
	
	private class myOneShot extends OneShotBehaviour {

		/**
		 * @param a
		 */
		public myOneShot(Agent a) {
			super(a);
		}
		
		public void action() {
	      System.out.println("OneShot");
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
