package it.unibo.ai.didattica.competition.tablut.rocinante.minmax;

import java.util.HashMap;
import java.util.List;

import aima.core.search.adversarial.Game;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class AlphaBetaPruningSearch {
	private Game<State, Action, State.Turn> game;
	private int maxDepth;
	private HashMap<Integer, Integer> nodesExpanded;
	private boolean logEnabled = false;
	private AlphaBetaPruningSearch.Timer timer;

	public static AlphaBetaPruningSearch create(Game<State, Action, State.Turn> game, int maxDepth, int time) {
		return new AlphaBetaPruningSearch(game, maxDepth, time);
	}

	public AlphaBetaPruningSearch(Game<State, Action, State.Turn> game, int maxDepth, int time) {
		this.game = game;
		this.maxDepth = maxDepth;
		this.timer = new AlphaBetaPruningSearch.Timer(time);

		this.nodesExpanded = new HashMap<>();
		for (int i = 0; i <= maxDepth; i++)
			nodesExpanded.put(i, 0);
	}

	/**
	 * Method performing the minmax algorithm.
	 * 
	 * @param state
	 * @return Best action to perform.
	 */
	public Action makeDecision(State state) {
		this.timer.start();

		Action result = null;
		double resultValue = Double.NEGATIVE_INFINITY;

		// first level
		nodesExpanded.put(0, 1);

		State.Turn player = game.getPlayer(state);

		List<Action> list = game.getActions(state);

		for (Action action : list) {
			State nextState = game.getResult(state, action);
			double value = minValue(nextState, player, maxDepth - 1, Double.NEGATIVE_INFINITY,
					Double.POSITIVE_INFINITY);

			if (value > resultValue) {
				result = action;
				resultValue = value;
			}
		}

		return result;
	}

	private double minValue(State state, Turn player, int depth, double alpha, double beta) {
		nodesExpanded.put(this.maxDepth - depth, nodesExpanded.get(this.maxDepth - depth) + 1);

		if (game.isTerminal(state) || depth == 0 || this.timer.timeoutOccurred()) {
			return game.getUtility(state, player);
		}

		double minEval = Double.POSITIVE_INFINITY;

		for (Action action : game.getActions(state)) {
			State nextState = game.getResult(state.clone(), action);

			double eval = maxValue(nextState, player, depth - 1, alpha, beta);
			minEval = minEval < eval ? minEval : eval;

			beta = beta < eval ? beta : eval;

			if (beta <= alpha)
				break;
		}

		return minEval;
	}

	private double maxValue(State state, Turn player, int depth, double alpha, double beta) {
		nodesExpanded.put(maxDepth - depth, nodesExpanded.get(maxDepth - depth) + 1);

		if (game.isTerminal(state) || depth == 0 || this.timer.timeoutOccurred())
			return game.getUtility(state, player);

		double maxEval = Double.NEGATIVE_INFINITY;

		for (Action action : game.getActions(state)) {
			State nextState = game.getResult(state.clone(), action);

			double eval = minValue(nextState, player, depth - 1, alpha, beta);
			maxEval = maxEval > eval ? maxEval : eval;
			alpha = alpha > eval ? alpha : eval;

			if (beta <= alpha)
				break;
		}

		return maxEval;
	}

	public void setLogEnabled(boolean logEnabled) {
		this.logEnabled = logEnabled;
	}

	private static class Timer {
		private long duration;
		private long startTime;

		public Timer(int maxSec) {
			this.duration = 1000L * (long) maxSec;
		}

		public void start() {
			this.startTime = System.currentTimeMillis();
		}

		public long getTimer() {
			return System.currentTimeMillis() - this.startTime;
		}

		public boolean timeoutOccurred() {
			return System.currentTimeMillis() > (this.startTime + this.duration);
		}

	}
}
