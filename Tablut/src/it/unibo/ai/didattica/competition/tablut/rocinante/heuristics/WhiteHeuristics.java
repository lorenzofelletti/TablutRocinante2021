package it.unibo.ai.didattica.competition.tablut.rocinante.heuristics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class WhiteHeuristics extends Heuristics {
	// number of b/w pawns
	private final int NUM_WHITE = 8;
	private final int NUM_BLACK = 16;
	private boolean flag = false;

	// below this threshold best config is less powerful
	private final static int THRESHOLD_BEST = 2;

	private final static int[][] bestPositions = { { 2, 3 }, { 3, 5 }, { 5, 3 }, { 6, 5 } };

	private final int numBestPositions = bestPositions.length;

	private Map<KEYS, Double> weights;
	private KEYS[] keys;

	public WhiteHeuristics(State state) {
		super(state);
		weights = new HashMap<KEYS, Double>();
		weights.put(KEYS.bestPosition, 2.0);
		weights.put(KEYS.blackEaten, 20.0);
		weights.put(KEYS.whiteAlive, 40.0);
		weights.put(KEYS.numEscapeKing, 15.0);
		weights.put(KEYS.blackSurroundKing, 7.0);
		weights.put(KEYS.protectionKing, 18.0);

		keys = KEYS.values();
	}

	/**
	 *
	 * @return the evaluation of the states using a weighted sum
	 */
	@Override
	public double evaluateState() {

		double utilityValue = 0;
		// Atomic functions to combine to get utility value through the weighted sum
		double bestPositions = (double) getNumberOnBestPositions() / numBestPositions;
		double numberOfWhiteAlive = (double) (state.getNumberOf(State.Pawn.WHITE)) / this.NUM_WHITE;
		double numberOfBlackEaten = (double) (this.NUM_BLACK - state.getNumberOf(State.Pawn.BLACK)) / this.NUM_BLACK;
		double blackSurroundKing = (double) (getNumberNeededPositionsToEatKing(state)
				- countNearPawns(state, kingPos(state), State.Turn.BLACK.toChar()))
				/ getNumberNeededPositionsToEatKing(state);
		double protectionKing = protectionKing();

		int numberWinWays = countWinWays(state);
		double numberOfWinEscapesKing = numberWinWays > 1 ? (double) countWinWays(state) / 4 : 0.0;

		if (flag) {
			System.out.println("Number of white alive: " + numberOfWhiteAlive);
			System.out.println("Number of white pawns in best positions " + bestPositions);
			System.out.println("Number of escapes: " + numberOfWinEscapesKing);
			System.out.println("Number of black surrounding king: " + blackSurroundKing);
		}

		Map<KEYS, Double> values = new HashMap<>();
		values.put(KEYS.bestPosition, bestPositions);
		values.put(KEYS.whiteAlive, numberOfWhiteAlive);
		values.put(KEYS.blackEaten, numberOfBlackEaten);
		values.put(KEYS.numEscapeKing, numberOfWinEscapesKing);
		values.put(KEYS.blackSurroundKing, blackSurroundKing);
		values.put(KEYS.protectionKing, protectionKing);

		for (KEYS key : this.keys) {
			utilityValue += weights.get(key) * values.get(key);
		}

		return utilityValue;
	}

	/**
	 *
	 * @return number of white pawns on best positions
	 */
	private int getNumberOnBestPositions() {

		int num = 0;

		if (state.getNumberOf(State.Pawn.WHITE) >= this.NUM_WHITE - THRESHOLD_BEST) {
			for (int[] pos : bestPositions) {
				if (state.getPawn(pos[0], pos[1]).equalsPawn(State.Pawn.WHITE)) {
					num++;
				}
			}
		}

		return num;
	}

	/***
	 *
	 * @return value according to the protection level of the king whether an enemy
	 *         pawn is next to it
	 */
	private double protectionKing() {

		// Values whether there is only a white pawn near to the king
		final double VAL_NEAR = 0.6;
		final double VAL_TOT = 1.0;

		double result = 0.0;

		int[] king = kingPos(state);
		// Pawns near to the king
		ArrayList<int[]> pawnsPositions = positionNearPawns(state, king, State.Pawn.BLACK.getChar());

		// There is a black pawn that threatens the king and 2 pawns are enough to eat
		// the king

		for (int[] enemy : pawnsPositions) {
			if (getNumberNeededPositionsToEatKing(state) == 2) {
				int[] targP = new int[2];
				targP[0] = -1;
				targP[1] = -1;

				if (enemy[0] == king[0]) {
					targP[0] = king[0];
					// is enemy to the king's right?
					if (enemy[1] == king[1] + 1)
						targP[1] = king[1] - 1;
					// left?
					if (enemy[1] == king[1] - 1)
						targP[1] = king[1] + 1;
				} else if (enemy[1] == king[1]) {
					targP[1] = king[1];
					// check if enemy is down the king
					if (enemy[0] == king[0] + 1)
						targP[0] = king[0] - 1;
					// enemy is up the king
					if (enemy[0] == king[0] - 1)
						targP[0] = king[0] + 1;
				}

				// if targP[0] != -1 => i set it in one of the prev ifs
				if (targP[0] != -1 && state.getPawn(targP[0], targP[1]).equalsPawn(State.Pawn.WHITE))
					result += VAL_NEAR;

				// Considering whites to use as barriers for the target pawn
				double otherPoints = VAL_TOT - VAL_NEAR;
				double contributionPerN = 0.0;

				// Whether it is better to keep free the position
				if (targP[0] == 0 || targP[0] == 8 || targP[1] == 0 || targP[1] == 8) {
					if (state.getPawn(targP[0], targP[1]).equalsPawn(State.Pawn.EMPTY)) {
						result = 1.0;
					} else {
						result = 0.0;
					}
				} else {
					// Considering a reduced number of neighbours whether target is near to citadels
					// or throne
					if (targP[0] == 4 && targP[1] == 2 || targP[0] == 4 && targP[1] == 6
							|| targP[0] == 2 && targP[1] == 4 || targP[0] == 6 && targP[1] == 4
							|| targP[0] == 3 && targP[1] == 4 || targP[0] == 5 && targP[1] == 4
							|| targP[0] == 4 && targP[1] == 3 || targP[0] == 4 && targP[1] == 5) {
						contributionPerN = otherPoints / 2;
					} else {
						contributionPerN = otherPoints / 3;
					}

					result += contributionPerN * countNearPawns(state, targP, State.Pawn.WHITE.getChar());
				}
			}
		}

		return result;
	}

	/*---------  ---------*/

	/**
	 * 
	 * @author Raffaele Battipaglia
	 *
	 */
	protected enum KEYS {
		bestPosition, blackEaten, whiteAlive, numEscapeKing, blackSurroundKing, protectionKing
	};
}
