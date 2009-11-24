package at.ac.sos.tuwien;

public enum Response {

	HUSH,	// schweigen
	BETRAY;	// verraten

	
	public static Response create(String response) {
		
		for(Response rep : Response.values()) {
			if(response.equals(rep.toString()))
				return rep;
		}
		// to the given number is no strategy assigned
		return null;		
	}

}
