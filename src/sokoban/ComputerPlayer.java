/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sokoban;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * 
 * @author steven edited by Adam
 */
public class ComputerPlayer {

	GameDisplay display;
	PriorityQueue<AstarNode> frontier;
	AstarNode node;
	Set<GameState> closedList = new HashSet<GameState>();

	public ComputerPlayer(GameState state) {
		display = new GameDisplay(state);
		frontier = new PriorityQueue<AstarNode>();
		node = new AstarNode(state);
		node.getCost();
		frontier.add(node);
		closedList.add(node.getState());
	}

	// Method for finding which block was pushed in the previous state, if no block was pushed returns null.
	private int[] blockMoved(AstarNode parent) {

		// If first node return null
		if (parent.getParent() == null) {
			return null;
		}

		int playerRow = parent.getState().playerRow;
		int playerCol = parent.getState().playerCol;
		// An integer value corresponding to the direction the entity moved to get to this state.
		int move = parent.move;
		int[] blockCo = new int[2];

		// Switch statement for direction of player movement.
		// Checks if space in font of player direction holds a block.
		// If so, this is the block that was moved, then returns blocks co-ordinates
		// to check for dead state
		switch (move) {
		case 0:
			if (parent.getState().state[playerRow][playerCol - 1] == 'b') {
				blockCo[0] = playerRow;
				blockCo[1] = playerCol - 1;
				return blockCo;

			} else {
				return null;
			}

		case 1:
			if (parent.getState().state[playerRow][playerCol + 1] == 'b') {
				blockCo[0] = playerRow;
				blockCo[1] = playerCol + 1;
				return blockCo;

			} else {
				return null;
			}

		case 2:
			if (parent.getState().state[playerRow - 1][playerCol] == 'b') {
				blockCo[0] = playerRow - 1;
				blockCo[1] = playerCol;
				return blockCo;

			} else {
				return null;
			}

		case 3:
			if (parent.getState().state[playerRow + 1][playerCol] == 'b') {
				blockCo[0] = playerRow + 1;
				blockCo[1] = playerCol;
				return blockCo;

			} else {
				return null;
			}

		default:
			return null;
		}

	}

	// Get solution method
	public List<GameState> getSolution() {

		int deadStateCounter = 0;
		while (!frontier.isEmpty()) {
			AstarNode parent = frontier.poll();

			// Check for dead states
			int blockCo[] = blockMoved(parent);
			if (blockCo != null) {
				if (parent.state.isDeadState(blockCo[0], blockCo[1])) {
					deadStateCounter++;
					continue;
				}
			}
			// Check if state is goal state
			if (!parent.getState().isGoalState()) {

				// add children to frontier
				GameState[] moves = parent.getState().findLegalMoves();
				for (int i = 0; i < moves.length; i++) {
					if (moves[i] != null) {
						// Multiple path pruning
						if (!closedList.contains(moves[i])) {

							AstarNode child = new AstarNode(moves[i], parent, i);
							frontier.add(child);
							closedList.add(child.getState());
						}
					}

				}
			} else {
				//Goal state found
				System.out.println("Depth of tree: " + parent.depth);
				System.out.println("Frontier size: " + frontier.size());
				System.out.println("Closed list size: " + closedList.size());
				System.out.println("Dead states: " + deadStateCounter);
				return parent.getPath();

			}

		}
		//Frontier is empty
		System.out.println("No solution found.");
		return null;
	}

	public void showSolution(List<GameState> solution) {
		for (GameState st : solution) {
			display.updateState((GameState) st);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		GameState state = args.length == 0 ? new GameState("levels/level3.txt") : new GameState(args[0]);
		long t1 = System.currentTimeMillis();
		ComputerPlayer player = new ComputerPlayer(state);
		List<GameState> solution = player.getSolution();
		long t2 = System.currentTimeMillis();
		System.out.println("Time: " + (t2 - t1));
		player.showSolution(solution);

	}
}
