package it.unibo.ai.didattica.competition.tablut.rocinante.heuristics;

import java.util.HashMap;
import java.util.Map;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class BlackHeuristics extends Heuristics {
	// number of b/w pawns
	private final int NUM_WHITE = 8;
	private final int NUM_BLACK = 16;
	// other variables
	private final byte THRESHOLD = 10;
	private final byte NUM_TILES_ON_RHOMBUS = 8;
	private final Map<KEYS, Double> weights;
	private KEYS[] keys;
	private boolean flag = false;
	private final int[][] rhombus = { { 1, 2 }, { 1, 6 }, { 2, 1 }, { 2, 7 }, { 6, 1 }, { 6, 7 }, { 7, 2 }, { 7, 6 } };

	public BlackHeuristics(State state) {
		super(state);
		weights = new HashMap<>();
		weights.put(KEYS.rhombusPosition, 2.0); // 2.0
		weights.put(KEYS.whiteEaten, 50.0); // 48.0
		weights.put(KEYS.blackAlive, 32.0); // 35.0
		weights.put(KEYS.blackSurroundKing, 17.0); // 15.0

		keys = KEYS.values();
	}

	/**
	 * Evaluate the "goodness" of the state from black player's POV.
	 */
	@Override
	public double evaluateState() {
		double utilityValue = 0.0;

		// Atomic functions to combine to get utility value through the weighted sum
		double numberOfBlack = (double) state.getNumberOf(State.Pawn.BLACK) / this.NUM_BLACK;
		double numberOfWhiteEaten = (double) (this.NUM_WHITE - state.getNumberOf(State.Pawn.WHITE)) / this.NUM_WHITE;
		double pawnsNearKing = (double) countNearPawns(state, kingPos(state), State.Turn.BLACK.toString())
				/ this.getNumberNeededPositionsToEatKing(state);
		double numberOfPawnsOnRhombus = (double) getNumberOnRhombus() / NUM_TILES_ON_RHOMBUS;

		// Weighted sum of functions to get final utility value
		Map<KEYS, Double> atomicUtilities = new HashMap<KEYS, Double>();
		atomicUtilities.put(KEYS.blackAlive, numberOfBlack);
		atomicUtilities.put(KEYS.whiteEaten, numberOfWhiteEaten);
		atomicUtilities.put(KEYS.blackSurroundKing, pawnsNearKing);
		atomicUtilities.put(KEYS.rhombusPosition, numberOfPawnsOnRhombus);

		for (int i = 0; i < weights.size(); i++) {
			utilityValue += weights.get(keys[i]) * atomicUtilities.get(keys[i]);
			if (flag) {
				System.out.println(keys[i] + ": " + weights.get(keys[i]) + "*" + atomicUtilities.get(keys[i]) + "= "
						+ weights.get(keys[i]) * atomicUtilities.get(keys[i]));
			}
		}
		return utilityValue;
	}

	/**
	 *
	 * @return number of black pawns on tiles if blacks are more than the threshold
	 *         true, 0 otherwise.
	 */
	public int getNumberOnRhombus() {

		if (state.getNumberOf(State.Pawn.BLACK) >= THRESHOLD) {
			return getValuesOnRhombus();
		} else {
			return 0;
		}
	}

	/**
	 * @return number of black pawns in strategic position rhombus.
	 */
	public int getValuesOnRhombus() {

		int count = 0;
		for (int[] position : rhombus) {
			if (state.getPawn(position[0], position[1]).equalsPawn(State.Pawn.BLACK.toString())) {
				count++;
			}
		}
		return count;

	}

	/*--------- PROTECTED AREA ---------*/
	/*--------- relatives only ---------*/

	/**
	 * 
	 * @author Raffaele Battipaglia
	 *
	 */
	protected enum KEYS {
		rhombusPosition, whiteEaten, blackAlive, blackSurroundKing
	};
}
