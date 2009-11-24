package at.ac.sos.tuwien;

public enum Strategy {
	
	TITFORTAT(1),
	MISTRUST(2),
	SPITE(3),
	PUNISHER(4),
	PAVLOV(5);
	
	private final int strategy;
	
	private Strategy(int strategy) {
		this.strategy = strategy;
	}
	/*
	public static Strategy create(int strategy) {
		
		return (new Strategy(strategy));
		
	}*/
}
