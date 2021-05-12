package it.unibo.ai.didattica.competition.tablut.rocinante.heuristics;

import java.util.ArrayList;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class Heuristics {
	protected State state;

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Heuristics(State state) {
		this.state = state;
	}

	public double evaluateState() {
		return 0;
	}

	/**
	 * @param state
	 * @return the position of the king in the passed state
	 */
	public int[] kingPos(State state) {
		int[] k = new int[2];
		State.Pawn[][] board = state.getBoard();
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board.length; ++j) {
				if (state.getPawn(i, j).equalsPawn('K')) {
					k[0] = i;
					k[1] = j;
					break;
				}
			}
		}
		return k;
	}

	/**
	 * @param state
	 * @return true if K is on the throne, false otherwise.
	 */
	public boolean checkKingInStartPosition(State state) {
		return state.getPawn(4, 4).equalsPawn('K');
	}

	/**
	 * @param state
	 * @param pos    the position to check
	 * @param target the target Pawns (black or white)
	 * @return the number of near target pawns
	 */
	public int countNearPawns(State state, int[] pos, char target) {
		int count = 0;
		State.Pawn[][] board = state.getBoard();
		count += (board[pos[0] - 1][pos[1]].equalsPawn(target)) ? 1 : 0;
		count += (board[pos[0] + 1][pos[1]].equalsPawn(target)) ? 1 : 0;
		count += (board[pos[0]][pos[1] - 1].equalsPawn(target)) ? 1 : 0;
		count += (board[pos[0]][pos[1] + 1].equalsPawn(target)) ? 1 : 0;
		return count;
	}

	/**
	 * @param state
	 * @param pos
	 * @param target
	 * @return true if there are target pawns nearby pos.
	 */
	public boolean checkNearPawns(State state, int[] pos, char target) {
		return countNearPawns(state, pos, target) > 0;
	}

	/**
	 * @param state
	 * @param pos
	 * @param target
	 * @return ArrayList<int[]> of the positions occupied near pos.
	 */
	protected ArrayList<int[]> positionNearPawns(State state, int[] pos, char target) {
		ArrayList<int[]> occupiedPosition = new ArrayList<int[]>();
		int[] p = new int[2];
		State.Pawn[][] board = state.getBoard();

		p[0] = pos[0] - 1;
		p[1] = pos[1];
		if (board[p[0]][p[1]].equalsPawn(target))
			occupiedPosition.add(p);

		p[0] = pos[0] + 1;
		p[1] = pos[1];
		if (board[p[0]][p[1]].equalsPawn(target))
			occupiedPosition.add(p);

		p[0] = pos[0];
		p[1] = pos[1] - 1;
		if (board[p[0]][p[1]].equalsPawn(target))
			occupiedPosition.add(p);

		p[0] = pos[0];
		p[1] = pos[1] + 1;
		if (board[p[0]][p[1]].equalsPawn(target))
			occupiedPosition.add(p);

		return occupiedPosition;
	}

	/**
	 * @param state
	 * @param pos
	 * @return true if king is near pos, false otherwise
	 */
	protected boolean checkNearKing(State state, int[] pos) {
		return checkNearPawns(state, pos, 'K');
	}

	/**
	 * @return how many pawns are in the block positions.
	 */
	protected int getNumberOfBlockedEscape() {
		int count = 0;
		int[][] blockedEscapes = { { 1, 1 }, { 1, 2 }, { 1, 6 }, { 1, 7 }, { 2, 1 }, { 2, 7 }, { 6, 1 }, { 6, 7 },
				{ 7, 1 }, { 7, 2 }, { 7, 6 }, { 7, 7 } };
		for (int[] pos : blockedEscapes) {
			if (state.getPawn(pos[0], pos[1]).equalsPawn(State.Pawn.BLACK))
				++count;
		}

		return count;
	}

	/**
	 * @return true if king is on an escape tile, false otherwise
	 */
	public boolean hasWhiteWon() {
		int[] kingPos = kingPos(state);
		return kingPos[0] == 0 || kingPos[0] == 8 || kingPos[1] == 0 || kingPos[1] == 8;
	}

	/**
	 * @param state
	 * @param kingPos
	 * @return true if the king is in a safe position, false otherwise.
	 */
	public boolean safePositionKing(State state, int[] kingPos) {
		return kingPos[0] > 2 && kingPos[0] < 6 && kingPos[1] > 2 && kingPos[1] < 6;
	}

	/**
	 * @param state
	 * @return true if the king has some way to escape
	 */
	public boolean kingGoesForWin(State state) {
		int[] kingPos = this.kingPos(state);
		int col = 0, row = 0;

		if (!safePositionKing(state, kingPos)) {
			col = countFreeColumn(state, kingPos);
			row = countFreeRow(state, kingPos);
		} else if (kingPos[1] > 2 && kingPos[1] < 6) {
			row = countFreeRow(state, kingPos);
		} else if (kingPos[0] > 2 && kingPos[0] < 6) {
			col = countFreeColumn(state, kingPos);
		}

		return (row + col) > 0;
	}

	/**
	 * @param state
	 * @return number of escapes the king can reach.
	 */
	public int countWinWays(State state) {
		int[] kingPos = this.kingPos(state);
		int col = (kingPos[0] > 2 && kingPos[0] < 6) ? this.countFreeColumn(state, kingPos) : 0;
		int row = (kingPos[1] > 2 && kingPos[1] < 6) ? countFreeRow(state, kingPos) : 0;
		return (col + row);

	}

	private int countFreeRow(State state, int[] pos) {
		int row = pos[0], col = pos[1];
		int[] currPos = new int[2];
		int countR = 1;
		int countL = 1;

		currPos[1] = col;
		for (int i = 1; i <= 8; i++) {
			// R
			if (row + i <= 8 && countR == 1) {
				currPos[0] = row + i;
				if (checkOccupiedPosition(state, currPos))
					countR--;
			}
			// L
			if (row - i >= 0 && countL == 1) {
				currPos[0] = row - i;
				if (checkOccupiedPosition(state, currPos))
					countL--;
			}
		}

		return countR + countL;
	}

	public int countFreeColumn(State state, int[] pos) {
		int row = pos[0], col = pos[1];
		int[] currPos = new int[2];
		int countUp = 1;
		int countDown = 1;

		currPos[1] = col;
		for (int i = 1; i <= 8; i++) {
			// Up
			if (row + i <= 8 && countUp == 1) {
				currPos[0] = row + i;
				if (checkOccupiedPosition(state, currPos))
					countUp--;
			}
			// Down
			if (row - i >= 0 && countDown == 1) {
				currPos[0] = row - i;
				if (checkOccupiedPosition(state, currPos))
					countDown--;
			}
		}

		return countUp + countDown;
	}

	/**
	 * @param state board state
	 * @param pos   the position to check
	 * @return true if pos is occupied, false otherwise
	 */
	public boolean checkOccupiedPosition(State state, int[] pos) {
		return !state.getPawn(pos[0], pos[1]).equals(State.Pawn.EMPTY);
	}

	/**
	 * 
	 * @param state
	 * @return the number of positions needed to eat the king in the current state
	 */
	public int getNumberNeededPositionsToEatKing(State state) {
		int[] kingPos = this.kingPos(state);

		if (kingPos[0] == 4 && kingPos[1] == 4)
			return 4;
		else if ((kingPos[0] == 3 && kingPos[1] == 4) || (kingPos[0] == 4 && kingPos[1] == 3)
				|| (kingPos[0] == 5 && kingPos[1] == 4) || (kingPos[0] == 4 && kingPos[1] == 5))
			return 3;
		else
			return 2;
	}
}
