package it.unibo.ai.didattica.competition.tablut.rocinante.minmax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.Action;


/**
 * Tries (and succeed, I hope) at extending aima-core
 * {@link IterativeDeepeningAlphaBetaSearch} implementing a killer move
 * heuristic (orders the moves that caused last 3 a-b cutoff placing them first
 * in the list).
 * 
 * @see IterativeDeepeningAlphaBetaSearch
 * @author Mario Caniglia, Raffaele Battipaglia, Lorenzo Felletti
 *
 * @param <State>
 * @param <Action>
 * @param <P>
 */

public class RociIterativeDeepeningAlphaBetaSearch extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn> {
	HashMap<Integer, List<Action>> killerMovesWhite;
	HashMap<Integer, List<Action>> killerMovesBlack;
	Timer2 timer2;
	
	private int n0de = 0;

	public RociIterativeDeepeningAlphaBetaSearch(Game<State, Action, State.Turn> game, double utilMin, double utilMax, int time) {
		super(game, utilMin, utilMax, time);
		killerMovesWhite = new HashMap<>();
		killerMovesBlack = new HashMap<>();
		this.timer2 = new Timer2(time);
	}

	/**
	 * Add a killer move to the killer move list at the specified depth (ply).
	 * 
	 * @param depth - it is the ply, but I'm no good at choosing names
	 * @param a     - the killer move, I know a its a bad name for a killer move.
	 */
	private void addKillerMove(int depth, Action a, State.Turn player) {
		HashMap<Integer, List<Action>> killerMoves = (player == State.Turn.BLACK) ? killerMovesBlack : killerMovesWhite;
		
		if (killerMoves.get(depth) == null) {
			killerMoves.put(depth, new ArrayList<Action>());
		}
		// Ma-ma-se, ma-ma-se, ma-ma-ku-sa
		if (killerMoves.get(depth).contains(a)) {
			killerMoves.get(depth).add(0, a);
			killerMoves.get(depth).remove(killerMoves.get(depth).size() - 1);
			return;
		} else {
			killerMoves.get(depth).add(0, a);
			if (killerMoves.get(depth).size() > 3) {
				killerMoves.get(depth).remove(killerMoves.get(depth).size() - 1);
			}
		}
	}

	/**
	 * The other half of the minimax.
	 */
	@Override
	public double maxValue(State state, State.Turn player, double alpha, double beta, int depth) {
		if (game.isTerminal(state) || depth >= currDepthLimit || timer2.timeOutOccurred()) {
			return eval(state, player);
		} else {
			double value = Double.NEGATIVE_INFINITY;
			for (Action action : orderActions(state, game.getActions(state), player, depth)) {
				n0de++;
				value = Math.max(value, minValue(game.getResult(state, action), //
						player, alpha, beta, depth + 1));
				if (value >= beta) {
					this.addKillerMove(depth, action, player);
					return value;
				}
				alpha = Math.max(alpha, value);
			}
			//System.out.println("MAX " + i + " " + depth + " " + currDepthLimit); 
			return value;
		}
	}

	/**
	 * One half of the minimax.
	 */
	@Override
	public double minValue(State state, State.Turn player, double alpha, double beta, int depth) {
		//System.out.println("cdl: "+currDepthLimit +"   d: "+depth);
		if (game.isTerminal(state) || depth >= currDepthLimit || timer2.timeOutOccurred()) {
			return eval(state, player);
		} else {
			double value = Double.POSITIVE_INFINITY;
			for (Action action : orderActions(state, game.getActions(state), player, depth)) {
				n0de++;
				value = Math.min(value, maxValue(game.getResult(state, action), //
						player, alpha, beta, depth + 1));
				if (value <= alpha) {
					this.addKillerMove(depth, action, player);
					return value;
				}
				beta = Math.min(beta, value);
			}
			//System.out.println("min " + i + " " + depth + " " + currDepthLimit); 
			return value;
		}
	}

	/**
	 * "Order" the moves. I mean, order is a big word for this method, but still...
	 */
	@Override
	public List<Action> orderActions(State state, List<Action> actions, State.Turn player, int depth) {
		HashMap<Integer, List<Action>> killerMoves = (player == State.Turn.BLACK) ? killerMovesBlack : killerMovesWhite;
		if (killerMoves.get(depth) != null) {
			for (Action killerMove : killerMoves.get(depth)) {
				boolean idx = actions.contains(killerMove);
				if (idx) {
					actions.remove(idx);
					actions.add(0, killerMove);
				}

			}
		}

		return actions;
	}
	
	@Override
	protected double eval(State state, State.Turn player) {
		super.eval(state, player);
		return game.getUtility(state, player);
	}

	/**
	 * Idk why aima made this class private, they didn't want people to extend their
	 * class or what?
	 * 
	 * @author Lorenzo Felletti
	 *
	 */
	protected static class Timer2 {
		private long duration;
		private long startTime;

		Timer2(int maxSeconds) {
			this.duration = 1000 * maxSeconds;
		}

		void start() {
			startTime = System.currentTimeMillis();
		}

		boolean timeOutOccurred() {
			return System.currentTimeMillis() > (startTime + duration);
		}
	}
	
	 @Override
	 public Action makeDecision(State state) {
		 timer2.start();
	     Action a = super.makeDecision(state);
	     System.out.println("nodi esplorat: " + n0de + " a profondità: " + currDepthLimit);
	     return  a;
	 }
}
