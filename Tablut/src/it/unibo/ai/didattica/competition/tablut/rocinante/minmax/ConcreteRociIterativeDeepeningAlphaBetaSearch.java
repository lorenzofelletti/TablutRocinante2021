package it.unibo.ai.didattica.competition.tablut.rocinante.minmax;

import java.util.List;
import java.util.Map;

import aima.core.search.adversarial.Game;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

/**
 * Mica pizza e fichi. (EN: Ain't pizza and figs.)
 * 
 * Concrete implementation of {@link RociIterativeDeepeningAlphaBetaSearch}
 * 
 * @see it.unibo.ai.didattica.competition.tablut.rocinante.minmax.RociIterativeDeepeningAlphaBetaSearch
 * @author Lorenzo Felletti
 */
public class ConcreteRociIterativeDeepeningAlphaBetaSearch
		extends RociIterativeDeepeningAlphaBetaSearch<State, Action, State.Turn> {

	public ConcreteRociIterativeDeepeningAlphaBetaSearch(Game<State, Action, Turn> game, double utilMin, double utilMax,
			int time) {
		super(game, utilMin, utilMax, time/*, citadels*/);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double eval(State state, State.Turn player) {
		super.eval(state, player);
		return game.getUtility(state, player);
	}
	
	
	
	@Override
	protected List<Action> sortActions(State state, List<Action> actions, Turn player, int depth) {
		// TODO Auto-generated method stub
		Map<Integer, KillerMovesStore<Action>> killerMoves = (player == State.Turn.BLACK) ? killerMovesBlack
				: killerMovesWhite;
		try {
			Action km1 = (killerMoves.get(depth).size() > 0) ? killerMoves.get(depth).getActions().get(0) : null;
			Action km2 = (killerMoves.get(depth).size() > 1) ? killerMoves.get(depth).getActions().get(1) : null;
			if (km1 != null) {
				if (actions.indexOf(km1) > -1) {
					actions.remove(actions.indexOf(km1));
					actions.add(0, km1.clone());
				}
			}
			if (km2 != null) {
				if (actions.indexOf(km2) > -1) {
					actions.remove(actions.indexOf(km2));
					actions.add(0, km2.clone());
				}
			}
			return actions;
		} catch (Exception e) {
			return actions;
		}

	}
	
	@Override
	public void addKillerMove(int depth, Action a, Turn player, double value, State state) {
		Map<Integer, KillerMovesStore<Action>> killerMoves = (player == State.Turn.BLACK) ? killerMovesBlack : killerMovesWhite;
		try {
			if (killerMoves.get(depth) == null) {
				killerMoves.put(depth, new KillerMovesStore<>());
			}
			// Ma-ma-se, ma-ma-se, ma-ma-ku-sa
			if (!killerMoves.get(depth).contains(a)) {
				if(!captureAction(state, player, a))
					killerMoves.get(depth).add(a, value);
			}
		} catch (Exception e) {
			return;
		}

	}
	
	private boolean checkWhiteCapture(State state, Action a) {
		// controllo se mangio a destra
		if (a.getColumnTo() < state.getBoard().length - 2
				&& state.getPawn(a.getRowTo(), a.getColumnTo() + 1).equalsPawn(State.Pawn.BLACK)
				&& (state.getPawn(a.getRowTo(), a.getColumnTo() + 2).equalsPawn(State.Pawn.WHITE)
						|| state.getPawn(a.getRowTo(), a.getColumnTo() + 2).equalsPawn(State.Pawn.THRONE)
						|| state.getPawn(a.getRowTo(), a.getColumnTo() + 2).equalsPawn(State.Pawn.KING)
						|| (this.citadels.contains(state.getBox(a.getRowTo(), a.getColumnTo() + 2))
								&& !(a.getColumnTo() + 2 == 8 && a.getRowTo() == 4)
								&& !(a.getColumnTo() + 2 == 4 && a.getRowTo() == 0)
								&& !(a.getColumnTo() + 2 == 4 && a.getRowTo() == 8)
								&& !(a.getColumnTo() + 2 == 0 && a.getRowTo() == 4)))) {
			return true;
		}

		// controllo se mangio a sinistra
		if (a.getColumnTo() > 1 && state.getPawn(a.getRowTo(), a.getColumnTo() - 1).equalsPawn('B')
				&& (state.getPawn(a.getRowTo(), a.getColumnTo() - 2).equalsPawn('W')
						|| state.getPawn(a.getRowTo(), a.getColumnTo() - 2).equalsPawn('T')
						|| state.getPawn(a.getRowTo(), a.getColumnTo() - 2).equalsPawn('K')
						|| (this.citadels.contains(state.getBox(a.getRowTo(), a.getColumnTo() - 2))
								&& !(a.getColumnTo() - 2 == 8 && a.getRowTo() == 4)
								&& !(a.getColumnTo() - 2 == 4 && a.getRowTo() == 0)
								&& !(a.getColumnTo() - 2 == 4 && a.getRowTo() == 8)
								&& !(a.getColumnTo() - 2 == 0 && a.getRowTo() == 4)))) {
			return true;
		}

		// controllo se mangio sopra
		if (a.getRowTo() > 1 && state.getPawn(a.getRowTo() - 1, a.getColumnTo()).equalsPawn('B')
				&& (state.getPawn(a.getRowTo() - 2, a.getColumnTo()).equalsPawn('W')
						|| state.getPawn(a.getRowTo() - 2, a.getColumnTo()).equalsPawn('T')
						|| state.getPawn(a.getRowTo() - 2, a.getColumnTo()).equalsPawn('K')
						|| (this.citadels.contains(state.getBox(a.getRowTo() - 2, a.getColumnTo()))
								&& !(a.getColumnTo() == 8 && a.getRowTo() - 2 == 4)
								&& !(a.getColumnTo() == 4 && a.getRowTo() - 2 == 0)
								&& !(a.getColumnTo() == 4 && a.getRowTo() - 2 == 8)
								&& !(a.getColumnTo() == 0 && a.getRowTo() - 2 == 4)))) {
			return true;
		}

		// controllo se mangio sotto
		if (a.getRowTo() < state.getBoard().length - 2
				&& state.getPawn(a.getRowTo() + 1, a.getColumnTo()).equalsPawn('B')
				&& (state.getPawn(a.getRowTo() + 2, a.getColumnTo()).equalsPawn('W')
						|| state.getPawn(a.getRowTo() + 2, a.getColumnTo()).equalsPawn('T')
						|| state.getPawn(a.getRowTo() + 2, a.getColumnTo()).equalsPawn('K')
						|| (this.citadels.contains(state.getBox(a.getRowTo() + 2, a.getColumnTo()))
								&& !(a.getColumnTo() == 8 && a.getRowTo() + 2 == 4)
								&& !(a.getColumnTo() == 4 && a.getRowTo() + 2 == 0)
								&& !(a.getColumnTo() == 4 && a.getRowTo() + 2 == 8)
								&& !(a.getColumnTo() == 0 && a.getRowTo() + 2 == 4)))) {
			return true;
		}

		return false;
	}

	private boolean checkCaptureBlackKingLeft(State state, Action a) {
		// ho il re sulla sinistra
		if (a.getColumnTo() > 1 && state.getPawn(a.getRowTo(), a.getColumnTo() - 1).equalsPawn('K')) {
			// re sul trono
			if (state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("e5")) {
				if (state.getPawn(3, 4).equalsPawn('B') && state.getPawn(4, 3).equalsPawn('B')
						&& state.getPawn(5, 4).equalsPawn('B')) {
					return true;
				}
			}

			// re adiacente al trono
			if (state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("e4")) {
				if (state.getPawn(2, 4).equalsPawn('B') && state.getPawn(3, 3).equalsPawn('B')) {
					return true;
				}
			}

			if (state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("f5")) {
				if (state.getPawn(5, 5).equalsPawn('B') && state.getPawn(3, 5).equalsPawn('B')) {
					return true;
				}
			}

			if (state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("e6")) {
				if (state.getPawn(6, 4).equalsPawn('B') && state.getPawn(5, 3).equalsPawn('B')) {
					return true;
				}
			}

			// sono fuori dalle zone del trono
			if (!state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("e5")
					&& !state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("e6")
					&& !state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("e4")
					&& !state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("f5")) {
				if (state.getPawn(a.getRowTo(), a.getColumnTo() - 2).equalsPawn('B')
						|| this.citadels.contains(state.getBox(a.getRowTo(), a.getColumnTo() - 2))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkCaptureBlackKingRight(State state, Action a) {
		// ho il re sulla destra
		if (a.getColumnTo() < state.getBoard().length - 2
				&& (state.getPawn(a.getRowTo(), a.getColumnTo() + 1).equalsPawn('K'))) {
			// re sul trono
			if (state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("e5")) {
				if (state.getPawn(3, 4).equalsPawn('B') && state.getPawn(4, 5).equalsPawn('B')
						&& state.getPawn(5, 4).equalsPawn('B')) {
					return true;
				}
			}

			// re adiacente al trono
			if (state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("e4")) {
				if (state.getPawn(2, 4).equalsPawn('B') && state.getPawn(3, 5).equalsPawn('B')) {
					state.setTurn(State.Turn.BLACKWIN);
					return true;
				}
			}

			if (state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("e6")) {
				if (state.getPawn(5, 5).equalsPawn('B') && state.getPawn(6, 4).equalsPawn('B')) {
					return true;
				}
			}

			if (state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("d5")) {
				if (state.getPawn(3, 3).equalsPawn('B') && state.getPawn(5, 3).equalsPawn('B')) {
					return true;
				}
			}

			// sono fuori dalle zone del trono
			if (!state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("d5")
					&& !state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("e6")
					&& !state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("e4")
					&& !state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("e5")) {
				if (state.getPawn(a.getRowTo(), a.getColumnTo() + 2).equalsPawn('B')
						|| this.citadels.contains(state.getBox(a.getRowTo(), a.getColumnTo() + 2))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkCaptureBlackKingDown(State state, Action a) {
		// ho il re sotto
		if (a.getRowTo() < state.getBoard().length - 2
				&& state.getPawn(a.getRowTo() + 1, a.getColumnTo()).equalsPawn('K')) {
			// System.out.println("Ho il re sotto");
			// re sul trono
			if (state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("e5")) {
				if (state.getPawn(5, 4).equalsPawn('B') && state.getPawn(4, 5).equalsPawn('B')
						&& state.getPawn(4, 3).equalsPawn('B')) {
					return true;
				}
			}

			// re adiacente al trono
			if (state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("e4")) {
				if (state.getPawn(3, 3).equalsPawn('B') && state.getPawn(3, 5).equalsPawn('B')) {
					return true;
				}
			}

			if (state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("d5")) {
				if (state.getPawn(4, 2).equalsPawn('B') && state.getPawn(5, 3).equalsPawn('B')) {
					return true;
				}
			}

			if (state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("f5")) {
				if (state.getPawn(4, 6).equalsPawn('B') && state.getPawn(5, 5).equalsPawn('B')) {
					return true;
				}
			}

			// sono fuori dalle zone del trono
			if (!state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("d5")
					&& !state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("e4")
					&& !state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("f5")
					&& !state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("e5")) {
				if (state.getPawn(a.getRowTo() + 2, a.getColumnTo()).equalsPawn('B')
						|| this.citadels.contains(state.getBox(a.getRowTo() + 2, a.getColumnTo()))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkCaptureBlackKingUp(State state, Action a) {
		// ho il re sopra
		if (a.getRowTo() > 1 && state.getPawn(a.getRowTo() - 1, a.getColumnTo()).equalsPawn('K')) {
			// re sul trono
			if (state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("e5")) {
				if (state.getPawn(3, 4).equalsPawn('B') && state.getPawn(4, 5).equalsPawn('B')
						&& state.getPawn(4, 3).equalsPawn('B')) {
					return true;
				}
			}

			// re adiacente al trono
			if (state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("e6")) {
				if (state.getPawn(5, 3).equalsPawn('B') && state.getPawn(5, 5).equalsPawn('B')) {
					return true;
				}
			}

			if (state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("d5")) {
				if (state.getPawn(4, 2).equalsPawn('B') && state.getPawn(3, 3).equalsPawn('B')) {
					return true;
				}
			}

			if (state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("f5")) {
				if (state.getPawn(4, 6).equalsPawn('B') && state.getPawn(3, 5).equalsPawn('B')) {
					return true;
				}
			}

			// sono fuori dalle zone del trono
			if (!state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("d5")
					&& !state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("e4")
					&& !state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("f5")
					&& !state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("e5")) {
				if (state.getPawn(a.getRowTo() - 2, a.getColumnTo()).equalsPawn('B')
						|| this.citadels.contains(state.getBox(a.getRowTo() - 2, a.getColumnTo()))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkCaptureBlackPawnRight(State state, Action a) {
		// mangio a destra
		if (a.getColumnTo() < state.getBoard().length - 2
				&& state.getPawn(a.getRowTo(), a.getColumnTo() + 1).equalsPawn('W')) {
			if (state.getPawn(a.getRowTo(), a.getColumnTo() + 2).equalsPawn('B')) {
				return true;
			}

			if (state.getPawn(a.getRowTo(), a.getColumnTo() + 2).equalsPawn('T')) {
				return true;
			}

			if (this.citadels.contains(state.getBox(a.getRowTo(), a.getColumnTo() + 2))) {
				return true;
			}

			if (state.getBox(a.getRowTo(), a.getColumnTo() + 2).equals("e5")) {
				return true;
			}

		}

		return false;
	}

	private boolean checkCaptureBlackPawnLeft(State state, Action a) {
		// mangio a sinistra
		if (a.getColumnTo() > 1 && state.getPawn(a.getRowTo(), a.getColumnTo() - 1).equalsPawn('W')
				&& (state.getPawn(a.getRowTo(), a.getColumnTo() - 2).equalsPawn('B')
						|| state.getPawn(a.getRowTo(), a.getColumnTo() - 2).equalsPawn('T')
						|| this.citadels.contains(state.getBox(a.getRowTo(), a.getColumnTo() - 2))
						|| (state.getBox(a.getRowTo(), a.getColumnTo() - 2).equals("e5")))) {
			return true;
		}

		return false;
	}

	private boolean checkCaptureBlackPawnUp(State state, Action a) {
		// controllo se mangio sopra
		if (a.getRowTo() > 1 && state.getPawn(a.getRowTo() - 1, a.getColumnTo()).equalsPawn('W')
				&& (state.getPawn(a.getRowTo() - 2, a.getColumnTo()).equalsPawn('B')
						|| state.getPawn(a.getRowTo() - 2, a.getColumnTo()).equalsPawn('T')
						|| this.citadels.contains(state.getBox(a.getRowTo() - 2, a.getColumnTo()))
						|| (state.getBox(a.getRowTo() - 2, a.getColumnTo()).equals("e5")))) {
			return true;
		}

		return false;
	}

	private boolean checkCaptureBlackPawnDown(State state, Action a) {
		// controllo se mangio sotto
		if (a.getRowTo() < state.getBoard().length - 2
				&& state.getPawn(a.getRowTo() + 1, a.getColumnTo()).equalsPawn('W')
				&& (state.getPawn(a.getRowTo() + 2, a.getColumnTo()).equalsPawn('B')
						|| state.getPawn(a.getRowTo() + 2, a.getColumnTo()).equalsPawn('T')
						|| this.citadels.contains(state.getBox(a.getRowTo() + 2, a.getColumnTo()))
						|| (state.getBox(a.getRowTo() + 2, a.getColumnTo()).equals("e5")))) {
			return true;
		}

		return false;
	}

	private boolean checkBlackCapture(State state, Action a) {
		return checkCaptureBlackKingDown(state, a) || checkCaptureBlackKingRight(state, a)
				|| checkCaptureBlackKingUp(state, a) || checkCaptureBlackKingLeft(state, a)
				|| checkCaptureBlackPawnLeft(state, a) || checkCaptureBlackPawnRight(state, a)
				|| checkCaptureBlackPawnUp(state, a) || checkCaptureBlackPawnDown(state, a);
	}

	@Override
	protected boolean captureAction(State state, State.Turn player, Action a) {
		if (player.equalsTurn(State.Turn.WHITE.toString())) {
			return checkWhiteCapture(state, a);
		} else {
			return checkBlackCapture(state, a);
		}
	}

	/**
	 * Method controlling the search. It is based on iterative deepening and tries
	 * to make to a good decision in limited time. It is overrided to print metrics.
	 */
	@Override
	public Action makeDecision(State state) {
		Action a = super.makeDecision(state);
		System.out.println("Explored a total of " + getMetrics().get(METRICS_NODES_EXPANDED)
				+ " nodes, reaching a depth limit of " + getMetrics().get(METRICS_MAX_DEPTH));
		return a;
	}
	
	public Map<Integer, TTNode> getTransposition() {
		// TODO Auto-generated method stub
		return this.transposition;
	}

}
