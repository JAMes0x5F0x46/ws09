package at.ac.ads.tuwien;

public enum NeighborhoodStrategy {

	// Divide the full sequence into two parts and rotate them
	SPLIT,
	// Move a job to another position
	MOVE,
	// 2-exchange of 2 jobs
	SWITCH,
	// choose a sequence of variable length and rotate subsequences
	ROTATE;
}
