package it.unibo.ai.didattica.competition.tablut.rocinante.clients;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.rocinante.minmax.ConcreteRociIterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.rocinante.minmax.TTNode;

public class RociCrewClient extends TablutClient {
	private int game;
	private boolean debug;
	protected Map<Integer, TTNode> transposition = new HashMap<>();

	public RociCrewClient(String player, String name, int timeout, String ipAddress, int game, boolean debug)
			throws UnknownHostException, IOException {
		super(player, name, timeout, ipAddress);
		
		this.game = game;
		this.debug = debug;
	}

	public static void main(String[] args) throws IOException {
		int gameType = 4;
		String role = "";
		String name = "Rocinante";
		String ipAddress = "localhost";
		int timeout = 60;
		boolean debug = false;
		
		for(String arg : args) {
			System.out.println(arg);
		}
		
		if (args.length < 1) {
			System.out.println("You must specify which player you are (WHITE or BLACK)");
			System.out.println("USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip> <debug>");
			System.exit(-1);
		} else {
			role = (args[0]);
		}
		if (args.length == 2) {
			try {
				timeout = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.out.println("Timeout must be an integer representing seconds");
				System.out.println("USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip> <debug>");
				System.exit(-1);
			}
		}
		if (args.length == 3) {
			try {
				timeout = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.out.println("Timeout must be an integer representing seconds");
				System.out.println("USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip> <debug>");
				System.exit(-1);
			}
			ipAddress = args[2];
		}

		if (args.length == 4) {
			try {
				timeout = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.out.println("Timeout must be an integer representing seconds");
				System.out.println("USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip> <debug>");
				System.exit(-1);
			}
			ipAddress = args[2];
			if (args[3].equals("debug") || args[3].equals("true")) {
				debug = true;
			} else {
				System.out.println("The last argument can be only 'debug' or 'true' and it allow (nope) to print logs during search");
				System.out.println("USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip> <debug>");
				System.exit(-1);
			}
		}

		RociCrewClient client = new RociCrewClient(role, name, timeout, ipAddress, gameType, debug);
		client.run();
	}

	@Override
	public void run() {

		// send name of your group to the server saved in variable "name"
		try {
			this.declareName();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// set type of state and WHITE must do the first player
		State state = new StateTablut();
		state.setTurn(State.Turn.WHITE);

		// set type of game
		GameAshtonTablut tablutGame = new GameAshtonTablut(0, -1, "logs", "white_ai", "black_ai");
		;

		System.out.println(""
				+ "+--------------------------  Ashton Tablut Game Challenge 2021  --------------------------+\n"
				+ "|                                                                                         |\n"
				+ "|                                    R o c i n a n t e                                    |\n"
				+ "|                   c r e w   w e l c o m e s   y o u   o n   b o a r d                   |\n"
				+ "|                                                                                         |\n"
				+ "+--------------------------------  A Legitimate Salvage.  --------------------------------+\n"
				);

		// attributes depends to parameters passed to main
		System.out.println("Player: " + (this.getPlayer().equals(State.Turn.BLACK) ? "BLACK" : "WHITE"));
		System.out.println("Timeout: " + this.timeout + " s");
		System.out.println("Server: " + this.serverIp);
		System.out.println("Debug mode: " + this.debug + "\n");

		// still alive until you are playing
		while (true) {

			// update the current state from the server
			try {
				this.read();
			} catch (ClassNotFoundException | IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}

			// print current state
			System.out.println("Current state:");
			state = this.getCurrentState();
			System.out.println(state.toString());

			// if i'm WHITE
			if (this.getPlayer().equals(State.Turn.WHITE)) {

				// if is my turn (WHITE)
				if (state.getTurn().equals(StateTablut.Turn.WHITE)) {

					System.out.println("\nSearching a suitable move... ");

					// search the best move in search tree
					Action a = findBestMove(tablutGame, state);

					System.out.println("\nAction selected: " + a.toString());
					try {
						this.write(a);
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
					}

				}

				// if is turn of oppenent (BLACK)
				else if (state.getTurn().equals(StateTablut.Turn.BLACK)) {
					System.out.println("Waiting for your opponent move...\n");
				}
				// if I WIN
				else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
					System.out.println("YOU WIN!");
					System.exit(0);
				}
				// if I LOSE
				else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
					System.out.println("YOU LOSE!");
					System.exit(0);
				}
				// if DRAW
				else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}

			}
			// if i'm BLACK
			else {

				// if is my turn (BLACK)
				if (this.getCurrentState().getTurn().equals(StateTablut.Turn.BLACK)) {

					System.out.println("\nSearching a suitable move... ");

					// search the best move in search tree
					Action a = findBestMove(tablutGame, state);

					System.out.println("\nAction selected: " + a.toString());
					try {
						this.write(a);
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
					}

				}

				// if is turn of oppenent (WHITE)
				else if (state.getTurn().equals(StateTablut.Turn.WHITE)) {
					System.out.println("Waiting for your opponent move...\n");
				}

				// if I LOSE
				else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
					System.out.println("YOU LOSE!");
					System.exit(0);
				}

				// if I WIN
				else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
					System.out.println("YOU WIN!");
					System.exit(0);
				}

				// if DRAW
				else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}
			}
		}
	}

	/**
	 * Method that find a suitable moves searching in game tree
	 * 
	 * @param tablutGame Current game
	 * @param state      Current state
	 * @return Action that is been evaluated as best
	 */
	private Action findBestMove(GameAshtonTablut tablutGame, State state) {

		ConcreteRociIterativeDeepeningAlphaBetaSearch search = new ConcreteRociIterativeDeepeningAlphaBetaSearch(
				tablutGame, Double.MIN_VALUE, Double.MAX_VALUE, this.timeout - 1, transposition);
		search.setLogEnabled(debug);
		
		return search.makeDecision(state);
		//return action;
	}
}
