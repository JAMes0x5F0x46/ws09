package at.ac.sos.tuwien;

public enum Strategy {
	
	TITFORTAT(0),
	MISTRUST(1),
	SPITE(2),
	PUNISHER(3),
	PAVLOV(4);
	
	private final int strategy;
	
	private Strategy(int strategy) {
		this.strategy = strategy;
	}
	
	public static Strategy create(int strategy) {
		
		for(Strategy instance : Strategy.values()) {
			if(instance.strategy == strategy)
				return instance;
		}
		// to the given number is no strategy assigned
		return null;		
	}

	public int getStrategy() {
		return strategy;
	}
}
