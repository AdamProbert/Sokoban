/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sokoban;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author steven edited by Adam
 */
public class GameState {

	protected char[][] state;
	protected int width;
	protected int height;
	int playerRow;
	int playerCol;
	List<Position> goalPositions;

	public GameState(String filename) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String line = in.readLine();
		List<char[]> l = new ArrayList();
		while (line != null) {
			l.add(line.toCharArray());
			line = in.readLine();
		}
		state = l.toArray(new char[0][0]);
		height = state.length;
		width = state[0].length;
		for (int i = 1; i < state.length; i++) {
			if (state[i].length != width) {
				throw new Exception();
			}
		}
		in.close();
		findPlayer();
		findGoalPositions();
	}

	public GameState(char[][] state, int playerRow, int playerCol, List<Position> goalPositions) {
		this.state = state;
		this.playerRow = playerRow;
		this.playerCol = playerCol;
		this.goalPositions = goalPositions;
		height = state.length;
		width = state[0].length;
	}

	// method to find all legal player moves
	// Stores moves in relevant index to allow processing of deadStates to specific block in ComputerPlayer.getSolution.
	public GameState[] findLegalMoves() {
		GameState[] res = new GameState[4];
		if (moveLeftLegal())
			res[0] = moveLeft();
		if (moveRightLegal())
			res[1] = moveRight();
		if (moveUpLegal())
			res[2] = moveUp();
		if (moveDownLegal())
			res[3] = moveDown();
		return res;
	}

	// Heuristic one
	// Gives the sum of distance from each block to closest goal

	public int heuristic1() {
		List<Position> blocks = findBlockPositions();
		int totalScore = 0;

		// Loop over blocks
		for (Position pos : blocks) {
			int distance = (int) Double.POSITIVE_INFINITY;

			// for each block find closest goal
			for (int e = 0; e < goalPositions.size(); e++) {
				Position goal = new Position(goalPositions.get(e).row, goalPositions.get(e).col);

				// find distance from block to goal
				int newDistance = pos.distance(goal);

				// If new distance is shorter swap variables
				if (newDistance < distance) {
					distance = newDistance;

				}
			}
			totalScore += distance;
		}
		return totalScore;
	}

	// Heuristic 2
	// 1-1 mapping blocks to goals - tries goals-1^2 combinations before determining
	// the cheapest mapping of blocks to goals.

	public int heuristic2() {

		List<Position> blocks = findBlockPositions();
		List<Position> goals = goalPositions; // Copy of goalPositions for
												// reordering
		int bestDistance = (int) Double.POSITIVE_INFINITY;

		int rotateCount = goals.size() - 1;

		// Testing for goals.size() * goals.size()-1 combinations
		for (int i = 0; i < (goals.size() * goals.size() - 1); i++) {
			int distance = 0;

			// Calculate distance for current ordering
			for (int e = 0; e < goals.size(); e++) {
				distance += goals.get(e).distance(blocks.get(e));
			}

			// If current distance > bestDistance = swap.
			if (distance < bestDistance) {
				bestDistance = distance;
				distance = 0;
			}

			// Rotate goal ordering
			if (rotateCount > 0) {
				Position goalsFirstIndex = null;

				// Rotate remaining goals
				for (int b = 1; b < goals.size(); b++) {

					if (b == 1) {
						// Store goals[1] to replace with goals[goals.size()]
						// after rotation.
						goalsFirstIndex = goals.get(b);
					}

					if (b != goals.size() - 1) {
						// Rotate elements from goals[1] to end.
						goals.set(b, goals.get(b + 1));

					} else {
						// Swap end goal with goals[1]
						goals.set(b, goalsFirstIndex);
					}

				}

				rotateCount--;
			} else {
				// All other goals have been rotated.. now swap first two
				// elements and repeat rotations
				Position goalZero = goals.get(0);
				goals.set(0, goals.get(1));
				goals.set(1, goalZero);
				rotateCount = goals.size() - 1;

			}
		}
		return bestDistance;
	}

	// Method to help the isDeadState method by checking occupied spaces
	public boolean occupied(int row, int col) {

		// Checks position for blocks or walls
		if (state[row][col] == 'w' || state[row][col] == 'b') {
			return true;
		} else {
			return false;
		}
	}

	// Method for checking of the current game state is dead
	public boolean isDeadState(int row, int col) {

		// Check if block is on goal position
		if (state[row][col] == 'b') {

			/**
			 * The following checks for a 4 block dead state 
			 * E.g. ww or wb or bb or bw 
			 * 		wb 	  wb    bb    bb
			 */
			// Check top & bottom of block
			if (occupied(row - 1, col) || occupied(row + 1, col)) {
				// Check left & right for block in corner
				if (occupied(row, col - 1) || occupied(row, col + 1)) {
					return true;
				}

			}

			/** 
			 * Check for blocks against walls 
			 * 
			 * E.g.     wwwwwwwwwwwwwwwww
			 *          w........b......w
			 *          w...............w
			 * 
			 */

			// If there is a wall above block
			if (occupied(row - 1, col)) {

				//Make a copy of the blocks co-ordinates
				int row2 = row;
				int col2 = col;

				// Search to left side of wall
				while (state[row2][col2 - 1] != 'w') {
					col2--;
				}

				// Search from left to right finding gaps or goals
				while (state[row2][col2] != 'w') {

					// Check for goals along wall - if so no dead state!
					if (state[row2][col2] == 'g' || state[row2][col2] == 'B') {
						return false;
					}

					// If there is a gap in wall - no dead state!
					if (state[row2 - 1][col2] != 'w') {
						return false;
					}
					col2++;

				}
				return true;
			}

			// If there is a wall or block below block
			if (occupied(row + 1, col)) {
				//Make a copy of blocks co-ordinates
				int row2 = row;
				int col2 = col;

				// Search to left side of wall
				while (state[row2][col2 - 1] != 'w') {
					col2--;
				}

				// Search from left to right finding gaps or goals
				while (state[row2][col2] != 'w') {
					col2++;

					// Check for goals along wall
					if (state[row2][col2] == 'g' || state[row2][col2] == 'B') {
						return false;
					}

					// If there is a gap in wall - no dead state!
					if (state[row2 + 1][col2] != 'w') {
						return false;
					}

				}
				return true;
			}

			// If there is a wall to the left of block
			if (occupied(row, col - 1)) {
				int row2 = row;
				int col2 = col;

				// Search to the top of wall
				while (state[row2 - 1][col2] != 'w') {
					row2--;
				}

				// Search from top to bottom finding gaps or goals
				while (state[row2][col2] != 'w') {
					row2++;

					// Check for goals along wall
					if (state[row2][col2] == 'g' || state[row2][col2] == 'B') {
						return false;
					}

					// If there is a gap in wall - no dead state!
					if (state[row2][col2 + 1] != 'w') {
						return false;
					}

				}
				return true;
			}

			// If there is a wall to the right of block
			if (occupied(row, col + 1)) {
				int row2 = row;
				int col2 = col;

				// Search to the top of wall
				while (state[row2 - 1][col2] != 'w') {
					row2--;
				}

				// Search from top to bottom finding gaps or goals
				while (state[row2][col2] != 'w') {
					row2++;

					// Check for goals along wall
					if (state[row2][col2] == 'g' || state[row2][col2] == 'B') {
						return false;
					}

					// If there is a gap in wall - no dead state!
					if (state[row2][col2 + 1] != 'w') {
						return false;
					}

				}
				return true;
			}
		}
		//If block is on goal position, return false.
		return false;

	}

	private void findPlayer() {
		boolean found = false;
		for (int i = 0; i < height && !found; i++) {
			for (int j = 0; j < width && !found; j++) {
				if (state[i][j] == 'p' || state[i][j] == 'P') {
					playerRow = i;
					playerCol = j;
					found = true;
				}
			}
		}
	}

	private void findGoalPositions() {
		goalPositions = new ArrayList();
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (state[i][j] == 'g' || state[i][j] == 'P' || state[i][j] == 'B') {
					goalPositions.add(new Position(i, j));
				}
			}
		}
	}

	protected List<Position> findBlockPositions() {
		List<Position> res = new ArrayList();
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (state[i][j] == 'b' || state[i][j] == 'B') {
					res.add(new Position(i, j));
				}
			}
		}
		return res;
	}

	public boolean isGoalState() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (state[i][j] == 'g' || state[i][j] == 'P') {
					return false;
				}
			}
		}
		return true;
	}

	public GameState moveLeft() {
		return move(playerRow, playerCol, playerRow, playerCol - 1, playerRow, playerCol - 2);
	}

	public GameState moveRight() {
		return move(playerRow, playerCol, playerRow, playerCol + 1, playerRow, playerCol + 2);
	}

	public GameState moveUp() {
		return move(playerRow, playerCol, playerRow - 1, playerCol, playerRow - 2, playerCol);
	}

	public GameState moveDown() {
		return move(playerRow, playerCol, playerRow + 1, playerCol, playerRow + 2, playerCol);
	}

	public GameState move(int row, int col, int row1, int col1, int row2, int col2) {
		if (row1 < 0 || col1 < 0 || row1 >= height || col1 >= width || state[row1][col1] == 'w') {
			return null;
		} else {
			char[][] newState = new char[height][width];
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					newState[i][j] = state[i][j];
				}
			}
			if (state[row][col] == 'p') {
				newState[row][col] = '.';
			} else if (state[row][col] == 'P') {
				newState[row][col] = 'g';
			}
			if (state[row1][col1] == '.' || state[row1][col1] == 'g') {

				if (state[row1][col1] == '.') {
					newState[row1][col1] = 'p';
				} else if (state[row1][col1] == 'g') {
					newState[row1][col1] = 'P';
				}
			} else if (state[row1][col1] == 'b' || state[row1][col1] == 'B') {
				if (row2 < 0 || col2 < 0 || row2 >= height || col2 >= width || state[row2][col2] == 'w'
						|| state[row2][col2] == 'b' || state[row2][col2] == 'B') {
					return null;
				}
				if (state[row1][col1] == 'B') {
					newState[row1][col1] = 'P';
				} else if (state[row1][col1] == 'b') {
					newState[row1][col1] = 'p';
				}
				if (state[row2][col2] == 'g') {
					newState[row2][col2] = 'B';
				} else if (state[row2][col2] == '.') {
					newState[row2][col2] = 'b';
				}
			}
			return new GameState(newState, row1, col1, goalPositions);
		}
	}

	public boolean moveLeftLegal() {
		return moveLeftLegal(playerRow, playerCol);
	}

	public boolean moveRightLegal() {
		return moveRightLegal(playerRow, playerCol);
	}

	public boolean moveDownLegal() {
		return moveDownLegal(playerRow, playerCol);
	}

	public boolean moveUpLegal() {
		return moveUpLegal(playerRow, playerCol);
	}

	public boolean moveLeftLegal(int row, int col) {
		return moveLegal(row, col, row, col - 1, row, col - 2);
	}

	public boolean moveRightLegal(int row, int col) {
		return moveLegal(row, col, row, col + 1, row, col + 2);
	}

	public boolean moveUpLegal(int row, int col) {
		return moveLegal(row, col, row - 1, col, row - 2, col);
	}

	public boolean moveDownLegal(int row, int col) {
		return moveLegal(row, col, row + 1, col, row + 2, col);
	}

	public boolean moveLegal(int row, int col, int row1, int col1, int row2, int col2) {
		if (row1 < 0 || col1 < 0 || row1 >= height || col1 >= width || state[row1][col1] == 'w') {
			return false;
		} else if ((state[row1][col1] == 'b' || state[row1][col1] == 'B')
				&& (row2 < 0 || col2 < 0 || row2 >= height || col2 >= width || state[row2][col2] == 'w'
						|| state[row2][col2] == 'b' || state[row2][col2] == 'B')) {
			return false;
		}
		return true;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public char getType(int x, int y) {
		return state[x][y];
	}

	public void printState() {
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state[0].length; j++) {
				System.out.print(state[i][j]);
			}
			System.out.println();
		}
	}

	private boolean isGoalPosition(Position pos) {
		return state[pos.row][pos.col] == 'g' || state[pos.row][pos.col] == 'B' || state[pos.row][pos.col] == 'P';
	}

	private boolean isBlockPosition(Position pos) {
		return state[pos.row][pos.col] == 'b' || state[pos.row][pos.col] == 'B';
	}

	public boolean equals(Object o) {
		if (!(o instanceof GameState)) {
			return false;
		}
		GameState gs = (GameState) o;
		if (gs.height != height || gs.width != width || playerRow != gs.playerRow || playerCol != gs.playerCol) {
			return false;
		}
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (state[row][col] != gs.state[row][col]) {
					return false;
				}
			}
		}
		return true;
	}

	public int hashCode() {
		List<Position> blockPositions = findBlockPositions();
		int res = playerRow + 17 * playerCol;
		for (Position pos : blockPositions) {
			res += 29 * pos.row + 47 * pos.col;
		}
		return res;
	}

	public static class Position {

		public int row;
		public int col;

		public Position(int row, int col) {
			this.row = row;
			this.col = col;
		}

		// Calculates Manhattan distance between two positions
		public int distance(Position pos) {
			return Math.abs(row - pos.row) + Math.abs(col - pos.col);
		}

	}

}
