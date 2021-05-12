package it.unibo.ai.didattica.competition.tablut.rocinante.minmax;

import java.util.HashMap;
import java.util.List;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;

/**
 * Tries (and succeed, I hope) at extending aima-core
 * {@link IterativeDeepeningAlphaBetaSearch} implementing a killer move
 * heuristic (orders the moves that caused last 3 a-b cutoff placing them first
 * in the list).
 * 
 * @see IterativeDeepeningAlphaBetaSearch
 * @author Lorenzo Felletti
 *
 * @param <S>
 * @param <A>
 * @param <P>
 */
public class RociIterativeDeepeningAlphaBetaSearch<S, A, P> extends IterativeDeepeningAlphaBetaSearch<S, A, P> {
	HashMap<Integer, List<A>> killerMoves;
	Timer timer;

	public RociIterativeDeepeningAlphaBetaSearch(Game<S, A, P> game, double utilMin, double utilMax, int time) {
		super(game, utilMin, utilMax, time);
		killerMoves = new HashMap<>();
		this.timer = new Timer(time);
	}

	/**
	 * Add a killer move to the killer move list at the specified depth (ply).
	 * 
	 * @param depth - it is the ply, but I'm no good at choosing names
	 * @param a     - the killer move, I know a its a bad name for a killer move.
	 */
	private void addKillerMove(int depth, A a) {
		// Ma-ma-se, ma-ma-se, ma-ma-ku-sa
		if (this.killerMoves.get(depth).contains(a)) {
			this.killerMoves.get(depth).add(0, a);
			this.killerMoves.get(depth).remove(this.killerMoves.get(depth).size() - 1);
			return;
		} else {
			this.killerMoves.get(depth).add(0, a);
			if (this.killerMoves.get(depth).size() > 3) {
				this.killerMoves.get(depth).remove(this.killerMoves.get(depth).size() - 1);
			}
		}
	}

	/**
	 * The other half of the minimax.
	 */
	@Override
	public double maxValue(S state, P player, double alpha, double beta, int depth) {
		if (game.isTerminal(state) || depth >= currDepthLimit || timer.timeOutOccurred()) {
			return eval(state, player);
		} else {
			double value = Double.NEGATIVE_INFINITY;
			for (A action : orderActions(state, game.getActions(state), player, depth)) {
				value = Math.max(value, minValue(game.getResult(state, action), //
						player, alpha, beta, depth + 1));
				if (value >= beta) {
					this.addKillerMove(depth, action);
					return value;
				}
				alpha = Math.max(alpha, value);
			}
			return value;
		}
	}

	/**
	 * One half of the minimax.
	 */
	@Override
	public double minValue(S state, P player, double alpha, double beta, int depth) {
		if (game.isTerminal(state) || depth >= currDepthLimit || timer.timeOutOccurred()) {
			return eval(state, player);
		} else {
			double value = Double.POSITIVE_INFINITY;
			for (A action : orderActions(state, game.getActions(state), player, depth)) {
				value = Math.min(value, maxValue(game.getResult(state, action), //
						player, alpha, beta, depth + 1));
				if (value <= alpha) {
					this.addKillerMove(depth, action);
					return value;
				}
				beta = Math.min(beta, value);
			}
			return value;
		}
	}

	/**
	 * "Order" the moves. I mean, order is a big word for this method, but still...
	 */
	@Override
	public List<A> orderActions(S state, List<A> actions, P player, int depth) {
		for (A killerMove : killerMoves.get(depth)) {
			boolean idx = actions.contains(killerMove);
			if (idx) {
				actions.remove(idx);
				actions.add(0, killerMove);
			}

		}
		actions.sort(null);
		return actions;
	}

	/**
	 * Idk why aima made this class private, they didn't want people to extend their
	 * class or what?
	 * 
	 * @author Lorenzo Felletti
	 *
	 */
	protected static class Timer {
		private long duration;
		private long startTime;

		Timer(int maxSeconds) {
			this.duration = 1000 * maxSeconds;
		}

		void start() {
			startTime = System.currentTimeMillis();
		}

		boolean timeOutOccurred() {
			return System.currentTimeMillis() > startTime + duration;
		}
	}
}
