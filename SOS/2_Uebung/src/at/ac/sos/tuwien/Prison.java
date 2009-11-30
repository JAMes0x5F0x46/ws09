package at.ac.sos.tuwien;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

public class Prison {

	public static int RUNS = 1;
	
	/**
	 * 
	 */
	public Prison() {
		
		// Get a hold on JADE runtime
		Runtime rt = Runtime.instance();
		// Create a default profile
		Profile p = new ProfileImpl();
		
		// Create a new non-main container, connecting to the default
		// main container (i.e. on this host, port 1099)
		ContainerController cc = rt.createAgentContainer(p);
		
		
		// Create a new agent, a DummyAgent
		// and pass it a reference to an Object
		Object reference = new Object();
		Object args[] = new Object[1];
		args[0]=reference;
		
		AgentController guard;

		try {
			guard = cc.createNewAgent("guard","at.ac.sos.tuwien.Guard", args);
			
			// Fire up the agent
			guard.start();
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		new Prison();
	}

}
