package it.unibo.ai.didattica.competition.tablut.rocinante.minmax;

public class TTNode {
	public double score;
	public int depth;
	public int type;

	public TTNode(double score, int depth, int type) {
		this.score = score;
		this.depth = depth;
		this.type = type;
	}
}
