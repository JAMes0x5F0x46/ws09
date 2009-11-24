package at.ac.sos.tuwien;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class Prisoner extends Agent{

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
	
	
	
}
