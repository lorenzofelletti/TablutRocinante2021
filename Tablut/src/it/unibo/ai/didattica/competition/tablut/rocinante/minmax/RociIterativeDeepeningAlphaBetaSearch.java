package it.unibo.ai.didattica.competition.tablut.rocinante.minmax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aima.core.search.adversarial.AdversarialSearch;
import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import aima.core.search.framework.Metrics;
import it.unibo.ai.didattica.competition.tablut.domain.Action;

/**
 * SOOOOO, AIMA, WHY DI U MAKE THINGS PRIVATEEEEE?? USE PROTECTED SO I CAN
 * EXTEND! This is a copypaste of aima + game spaecific code & our "optimizations".
 * IterativeDeepeningAlphaBetaSeach, but with less private sh*t. We don't want
 * to steal any IP, BUT we hate that extending the original class is so so
 * difficult.
 * 
 * (then we also added specific code for our game)
 * @author Mario Caniglia, Raffaele Battipaglia, Lorenzo Felletti
 */
public class RociIterativeDeepeningAlphaBetaSearch<S, A, P> implements AdversarialSearch<S, A> {

	public final static String METRICS_NODES_EXPANDED = "nodesExpanded";
	public final static String METRICS_MAX_DEPTH = "maxDepth";

	protected Game<S, A, P> game;
	protected double utilMax;
	protected double utilMin;
	protected int currDepthLimit;
	private boolean heuristicEvaluationUsed; // indicates that non-terminal nodes have been evaluated.
	private Timer timer;
	private boolean logEnabled = false;

	private Metrics metrics = new Metrics();

	Map<Integer, KillerMovesStore<A>> killerMovesWhite;
	Map<Integer, KillerMovesStore<A>> killerMovesBlack;
	List<String> citadels;

	/* TRYnsposition table */
	protected Map<Integer, TTNode> transposition;
	/* transp table end */

	public Map<Integer, TTNode> getTransposition() {
		return transposition;
	}

	public void setTransposition(Map<Integer, TTNode> transposition) {
		this.transposition = transposition;
	}

	/**
	 * Creates a new search object for a given game.
	 *
	 * @param game    The game.
	 * @param utilMin Utility value of worst state for this player. Supports
	 *                evaluation of non-terminal states and early termination in
	 *                situations with a safe winner.
	 * @param utilMax Utility value of best state for this player. Supports
	 *                evaluation of non-terminal states and early termination in
	 *                situations with a safe winner.
	 * @param time    Maximal computation time in seconds.
	 */
	public static <STATE, ACTION, PLAYER> IterativeDeepeningAlphaBetaSearch<STATE, ACTION, PLAYER> createFor(
			Game<STATE, ACTION, PLAYER> game, double utilMin, double utilMax, int time) {
		return new IterativeDeepeningAlphaBetaSearch<>(game, utilMin, utilMax, time);
	}

	/**
	 * Creates a new search object for a given game.
	 *
	 * @param game    The game.
	 * @param utilMin Utility value of worst state for this player. Supports
	 *                evaluation of non-terminal states and early termination in
	 *                situations with a safe winner.
	 * @param utilMax Utility value of best state for this player. Supports
	 *                evaluation of non-terminal states and early termination in
	 *                situations with a safe winner.
	 * @param time    Maximal computation time in seconds.
	 */
	public RociIterativeDeepeningAlphaBetaSearch(Game<S, A, P> game, double utilMin, double utilMax, int time,
			Map<Integer, TTNode> transposition) {
		this.game = game;
		this.utilMin = utilMin;
		this.utilMax = utilMax;
		this.timer = new Timer(time);
		this.transposition = transposition; // new HashMap<>();

		// game-specific
		killerMovesWhite = new HashMap<>();
		killerMovesBlack = new HashMap<>();

		initializeCitadels();
	}

	private void initializeCitadels() {
		this.citadels = new ArrayList<String>();
		this.citadels.add("a4");
		this.citadels.add("a5");
		this.citadels.add("a6");
		this.citadels.add("b5");
		this.citadels.add("d1");
		this.citadels.add("e1");
		this.citadels.add("f1");
		this.citadels.add("e2");
		this.citadels.add("i4");
		this.citadels.add("i5");
		this.citadels.add("i6");
		this.citadels.add("h5");
		this.citadels.add("d9");
		this.citadels.add("e9");
		this.citadels.add("f9");
		this.citadels.add("e8");

	}

	public void setLogEnabled(boolean b) {
		logEnabled = b;
	}

	/**
	 * Implement it in subclass
	 * 
	 * @param depth
	 * @param a
	 * @param player
	 */
	public void addKillerMove(int depth, A a, P player, double value, S s) {
		return;
	}

	/**
	 * Implement it in subclass!!
	 * 
	 * @param s
	 * @param p
	 * @param a
	 * @return
	 */
	protected boolean captureAction(S s, P p, A a) {
		return true;
	}

	/**
	 * Template method controlling the search. It is based on iterative deepening
	 * and tries to make to a good decision in limited time. Credit goes to Behi
	 * Monsio who had the idea of ordering actions by utility in subsequent
	 * depth-limited search runs.
	 */
	@Override
	public A makeDecision(S state) {
		metrics = new Metrics();
		StringBuffer logText = null;
		P player = game.getPlayer(state);
		List<A> results = orderActions(state, game.getActions(state), player, 0);
		timer.start();
		currDepthLimit = 0;
		do {
			incrementDepthLimit();
			heuristicEvaluationUsed = false;
			ActionStore<A> newResults = new ActionStore<>();
			for (A action : results) {
				double value = minValue(game.getResult(state, action), player, Double.NEGATIVE_INFINITY,
						Double.POSITIVE_INFINITY, 1);
				if (timer.timeOutOccurred())
					break; // exit from action loop
				newResults.add(action, value);
				if (newResults.size() > 0) {
					results = newResults.actions;
					if (!timer.timeOutOccurred()) {
						if (hasSafeWinner(newResults.utilValues.get(0)))
							break; // exit from iterative deepening loop
					}
				}
			}
		} while (!timer.timeOutOccurred() && heuristicEvaluationUsed);
		return results.get(0);
	}

	/**
	 * Override in subclass
	 * 
	 * @param state
	 * @param actions
	 * @param player
	 * @param depth
	 * @return
	 */
	protected List<A> sortActions(S state, List<A> actions, P player, int depth) {
		return actions;
	}

	// returns an utility value
	public double maxValue(S state, P player, double alpha, double beta, int depth) {
		updateMetrics(depth);
		double origAlpha = alpha;
		if (game.isTerminal(state) || depth >= currDepthLimit || timer.timeOutOccurred()) {
			double score = eval(state, player);
			if (!timer.timeOutOccurred()) {
				if (game.isTerminal(state)) {
					transposition.put(state.hashCode(), new TTNode(score, depth, 0));
				} else if (score <= alpha) {
					transposition.put(state.hashCode(), new TTNode(score, depth, 1));
				} else if (score >= beta) {
					transposition.put(state.hashCode(), new TTNode(score, depth, -1));
				}
			}
			return score;
		} else if (transposition.containsKey(state.hashCode()) && transposition.get(state.hashCode()).depth > depth) {
			TTNode n = transposition.get(state.hashCode());
			if (n.type == 1)
				beta = Math.min(beta, n.score);
			else if (n.type == -1)
				alpha = Math.max(alpha, n.score);
			else
				return n.score;
			if (alpha >= beta)
				return n.score;
		}
		double value = Double.NEGATIVE_INFINITY;
		for (A action : sortActions(state, game.getActions(state), player, depth)) {
			value = Math.max(value, minValue(game.getResult(state, action), player, alpha, beta, depth + 1));
			alpha = Math.max(alpha, value);
			if (value >= beta) {
				if (!transposition.containsKey(state.hashCode()))
					transposition.put(state.hashCode(), new TTNode(value, depth, 0));

				if (!this.captureAction(state, player, action)) {
					this.addKillerMove(depth, action, player, value, state);
				}

				return value;
			}
			// alpha = Math.max(alpha, value);

		}
		if (value <= origAlpha) {
			transposition.put(state.hashCode(), new TTNode(value, depth, 1));
		} else if (value >= beta) {
			transposition.put(state.hashCode(), new TTNode(value, depth, -1));
		}
		return value;

	}

	// returns an utility value
	public double minValue(S state, P player, double alpha, double beta, int depth) {
		updateMetrics(depth);
		double origAlpha = alpha;
		if (game.isTerminal(state) || depth >= currDepthLimit || timer.timeOutOccurred()) {
			double score = eval(state, player);
			if (!timer.timeOutOccurred()) {
				if (game.isTerminal(state)) {
					transposition.put(state.hashCode(), new TTNode(score, depth, 0));
				} else if (score <= alpha) {
					transposition.put(state.hashCode(), new TTNode(score, depth, 1));
				} else if (score >= beta) {
					transposition.put(state.hashCode(), new TTNode(score, depth, -1));
				}
			}
			return eval(state, player);
		} else if (transposition.containsKey(state.hashCode()) && transposition.get(state.hashCode()).depth >= depth) {
			TTNode n = transposition.get(state.hashCode());
			if (n.type == 1)
				beta = Math.min(beta, n.score);
			else if (n.type == -1)
				alpha = Math.max(alpha, n.score);
			else
				return n.score;
			if (alpha >= beta)
				return n.score;
		}

		double value = Double.POSITIVE_INFINITY;
		List<A> actions = sortActions(state, game.getActions(state), player, depth);
		for (int i = 0; i < actions.size(); i++) {
			value = Math.min(value, maxValue(game.getResult(state, actions.get(i)), player, alpha, beta, depth + 1));
			beta = Math.min(beta, value);
			if (value <= alpha) {
				if (!transposition.containsKey(state.hashCode()))
					transposition.put(state.hashCode(), new TTNode(value, depth, 0));
				return value;
			}
		}
		if (value <= origAlpha) {
			transposition.put(state.hashCode(), new TTNode(value, depth, 1));
		} else if (value >= beta) {
			transposition.put(state.hashCode(), new TTNode(value, depth, -1));
		}
		return value;

	}

	public void updateMetrics(int depth) {
		metrics.incrementInt(METRICS_NODES_EXPANDED);
		metrics.set(METRICS_MAX_DEPTH, Math.max(metrics.getInt(METRICS_MAX_DEPTH), depth));
	}

	/**
	 * Returns some statistic data from the last search.
	 */
	@Override
	public Metrics getMetrics() {
		return metrics;
	}

	/**
	 * Primitive operation which is called at the beginning of one depth limited
	 * search step. This implementation increments the current depth limit by one.
	 */
	protected void incrementDepthLimit() {
		currDepthLimit++;
	}

	/**
	 * Primitive operation which is used to stop iterative deepening search in
	 * situations where a clear best action exists. This implementation returns
	 * always false.
	 */
	protected boolean isSignificantlyBetter(double newUtility, double utility) {
		return false;
	}

	/**
	 * Primitive operation which is used to stop iterative deepening search in
	 * situations where a safe winner has been identified. This implementation
	 * returns true if the given value (for the currently preferred action result)
	 * is the highest or lowest utility value possible.
	 */
	protected boolean hasSafeWinner(double resultUtility) {
		return resultUtility <= utilMin || resultUtility >= utilMax;
	}

	/**
	 * Primitive operation, which estimates the value for (not necessarily terminal)
	 * states. This implementation returns the utility value for terminal states and
	 * <code>(utilMin + utilMax) / 2</code> for non-terminal states. When
	 * overriding, first call the super implementation!
	 */
	protected double eval(S state, P player) {
		if (game.isTerminal(state)) {
			return game.getUtility(state, player);
		} else {
			heuristicEvaluationUsed = true;
			return (utilMin + utilMax) / 2;
		}
	}

	/**
	 * Primitive operation for action ordering. This implementation preserves the
	 * original order (provided by the game).
	 */
	public List<A> orderActions(S state, List<A> actions, P player, int depth) {
		return actions;
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	// nested helper classes

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

	/**
	 * Orders actions by utility.
	 */
	public static class ActionStore<A> {
		protected List<A> actions = new ArrayList<>();
		protected List<Double> utilValues = new ArrayList<>();

		public void add(A action, double utilValue) {
			int idx = 0;
			while (idx < actions.size() && utilValue <= utilValues.get(idx))
				idx++;
			actions.add(idx, action);
			utilValues.add(idx, utilValue);
		}

		public int size() {
			return actions.size();
		}
	}

	public static class KillerMovesStore<A> extends ActionStore<A> {
		final int maxSize = 2;

		@Override
		public void add(A action, double utilValue) {
			if (actions.contains(action))
				return;
			actions.add(0, action);
			utilValues.add(0, utilValue);
			if (this.size() > maxSize) {
				actions.remove(maxSize);
				utilValues.remove(maxSize);
			}
		}

		List<A> getActions() {
			return actions;
		}

		public boolean contains(Action a) {
			return actions.contains(a);
		}
	}
}