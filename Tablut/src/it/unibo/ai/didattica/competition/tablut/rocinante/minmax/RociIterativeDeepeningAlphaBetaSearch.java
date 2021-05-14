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

	private List<String> citadels;

	Timer2 timer2;
	
	private int n0de = 0;

	public RociIterativeDeepeningAlphaBetaSearch(Game<State, Action, State.Turn> game, double utilMin, double utilMax, int time) {
		super(game, utilMin, utilMax, time);
		killerMovesWhite = new HashMap<>();
		killerMovesBlack = new HashMap<>();
		this.timer2 = new Timer2(time);

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
					// killer move if is a beta-cutoff and a non-capture move
					if(!this.captureAction(state, player, action)){
						this.addKillerMove(depth, action, player);
					}

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
					//this.addKillerMove(depth, action, player);
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
			//System.out.println("Ho il re sotto");
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

	 private boolean checkBlackCapture(State state, Action a){
		return checkCaptureBlackKingDown(state, a) ||
				checkCaptureBlackKingRight(state, a) ||
				checkCaptureBlackKingUp(state, a) ||
				checkCaptureBlackKingLeft(state, a) ||
				checkCaptureBlackPawnLeft(state, a) ||
				checkCaptureBlackPawnRight(state, a) ||
				checkCaptureBlackPawnUp(state, a) ||
				checkCaptureBlackPawnDown(state, a);
	 }

	 private boolean captureAction(State state, State.Turn player, Action a) {
		if(player.equalsTurn(State.Turn.WHITE)){
			return checkWhiteCapture(state, a);
		}else{
			return checkBlackCapture(state, a);
		}
	 }
}
