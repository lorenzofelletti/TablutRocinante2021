package it.unibo.ai.didattica.competition.tablut.rocinante.minmax;

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
		extends RociIterativeDeepeningAlphaBetaSearch {

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

}
