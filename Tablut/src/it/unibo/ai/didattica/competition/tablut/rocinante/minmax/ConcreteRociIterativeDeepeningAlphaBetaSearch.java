package it.unibo.ai.didattica.competition.tablut.rocinante.minmax;

import java.util.ArrayList;
import java.util.List;

import aima.core.search.adversarial.Game;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

/**
 * Mica pizza e fichi. (EN: Ain't pizza and figs.)
 * 
 * Concrete implementation of {@link RociIterativeDeepeningAlphaBetaSearch}
 * @see it.unibo.ai.didattica.competition.tablut.rocinante.minmax.RociIterativeDeepeningAlphaBetaSearch
 * @author Lorenzo Felletti
 */
public class ConcreteRociIterativeDeepeningAlphaBetaSearch
		extends RociIterativeDeepeningAlphaBetaSearch<State, Action, State.Turn> {

	public ConcreteRociIterativeDeepeningAlphaBetaSearch(Game<State, Action, Turn> game, double utilMin, double utilMax,
			int time) {
		super(game, utilMin, utilMax, time);
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
		List<KillerMovesStore<Action>> killerMoves = (player == State.Turn.BLACK) ? killerMovesBlack : killerMovesWhite;
		try {
			for(Action km : killerMoves.get(depth).getActions()) {
				int idx = actions.indexOf(killerMoves);
				if (idx > -1) {
					Action ac = actions.get(idx);
					actions.remove(idx);
					actions.add(0, ac);
				}
			}
			return actions;
		} catch(Exception e) {
			return actions;
		}
		
	}
	
	@Override
	public void addKillerMove(int depth, Action a, Turn player, double value) {
		ArrayList<KillerMovesStore<Action>> km = (player == State.Turn.BLACK) ? killerMovesBlack : killerMovesWhite;
		try {
			if (km.get(depth) == null) {
				km.add(depth, new KillerMovesStore<>());
			}
			// Ma-ma-se, ma-ma-se, ma-ma-ku-sa
			if (!km.get(depth).contains(a)) {
				km.get(depth).add(a, value);
			}
		} catch(Exception e) {
			km.add(depth, new KillerMovesStore<>());
			this.addKillerMove(depth, a, player, value);
		}
		
	}

}
