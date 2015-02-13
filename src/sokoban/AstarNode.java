/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sokoban;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author steven edited by Adam
 * Taken from labs.
 * 
 */

/**
 * TO SWITCH BETWEEN HEURISTIC1 AND HEURISTIC2: Change lines 67, 76 & 77 to
 * relevant call. (Either "state.heuristic1" or "state.heuristic2")
 */

// A class that holds the current gameState and previous gameState
public class AstarNode implements Comparable<AstarNode> {

	protected GameState state; // state corresponding to this node
	protected AstarNode previousNode; // parent of this node in the search tree
	protected int depth; // Current depth of node
	protected int costToThisNode; // Cost from start to current node
	protected int totalCost; // costToThisNode + heuristic
	protected int move; // 0 = player moved left, 1 = ..right, 2 = ..up, 3 = ..down

	// Constuctor for node >=2
	public AstarNode(GameState st, AstarNode previousNode, int move) {

		this.state = st;
		this.previousNode = previousNode;
		depth = previousNode.depth + 1;
		costToThisNode = previousNode.costToThisNode + 1; // +1 for one move
		totalCost = costToThisNode + getCost();
		this.move = move;
	}

	// Constructor for node ==1.
	public AstarNode(GameState st) {
		this.state = st;
		previousNode = null;
		depth = 0;
		costToThisNode = 0;
		totalCost = getCost();
	}

	public GameState getState() {
		return state;
	}

	public List<GameState> getPath() {
		List<GameState> res;
		if (previousNode != null)
			res = previousNode.getPath();
		else
			res = new ArrayList();
		res.add(state);
		return res;
	}

	public int getCost() {
		return state.heuristic2();
	}

	public AstarNode getParent() {
		return previousNode;
	}

	@Override
	public int compareTo(AstarNode n) {
		int score = costToThisNode + state.heuristic2();
		int scoreN = n.costToThisNode + n.state.heuristic2();
		return score - scoreN;

	}

}
