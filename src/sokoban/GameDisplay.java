/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sokoban;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * 
 * @author steven
 */
public class GameDisplay {

	private JFrame frame;
	private GameState current;
	private int width;
	private int height;
	private int cellSize = 50;
	private Graphics2D bufferGraphics;
	private Graphics2D screenGraphics;
	BufferedImage bufferImage;
	BufferedImage screenImage;

	public GameDisplay(GameState state) {
		width = state.getWidth() * cellSize;
		height = state.getHeight() * cellSize;
		initFrame();
		updateState(state);
	}

	private void initFrame() {

		bufferImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		screenImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		bufferGraphics = bufferImage.createGraphics();
		screenGraphics = screenImage.createGraphics();

		RenderingHints hints = new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		bufferGraphics.addRenderingHints(hints);

		ImageIcon icon = new ImageIcon(screenImage);
		JLabel draw = new JLabel(icon);
		frame = new JFrame();
		frame.setContentPane(draw);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Sokoban");
		frame.pack();
		frame.requestFocusInWindow();
		frame.setVisible(true);
	}

	public void updateState(GameState newState) {
		current = newState;
		for (int i = 0; i < current.getHeight(); i++) {
			for (int j = 0; j < current.getWidth(); j++) {
				if (current.getType(i, j) == 'w')
					drawWall(i, j);
				else if (current.getType(i, j) == 'b'
						|| current.getType(i, j) == 'B')
					drawBlock(i, j);
				else if (current.getType(i, j) == 'g')
					drawGoal(i, j);
				else if (current.getType(i, j) == 'p'
						|| current.getType(i, j) == 'P')
					drawPlayer(i, j);
				else if (current.getType(i, j) == '.')
					drawEmpty(i, j);
				else {
					System.err.println("Unexpected level description: (" + i
							+ "," + j + ") =" + current.getType(i, j));
				}
			}
		}
		screenGraphics.drawImage(bufferImage, 0, 0, null);
		frame.repaint();
	}

	private void drawBlock(int row, int col) {
		drawEmpty(row, col);
		bufferGraphics.setColor(new Color(255, 0, 255));
		bufferGraphics.fillRect(col * cellSize + 6, row * cellSize + 6,
				cellSize - 12, cellSize - 12);
		bufferGraphics.setColor(new Color(255, 100, 255));
		bufferGraphics.setStroke(new BasicStroke(3));
		bufferGraphics.drawRect(col * cellSize + 6, row * cellSize + 6,
				cellSize - 12, cellSize - 12);
		bufferGraphics.setStroke(new BasicStroke(1));
	}

	private void drawWall(int row, int col) {
		bufferGraphics.setColor(new Color(0, 0, 255));
		bufferGraphics.fillRect(col * cellSize, row * cellSize, cellSize + 1,
				cellSize + 1);
		bufferGraphics.setColor(new Color(0, 0, 200));
		bufferGraphics.setStroke(new BasicStroke(3));
		bufferGraphics.drawRect(col * cellSize, row * cellSize, cellSize - 1,
				cellSize - 1);
		bufferGraphics.setStroke(new BasicStroke(1));
	}

	private void drawGoal(int row, int col) {
		drawEmpty(row, col);
		bufferGraphics.setColor(new Color(200, 200, 200));
		bufferGraphics.fillOval((int) ((col + 0.5) * cellSize - 2),
				(int) ((row + 0.5) * cellSize - 2), 4, 4);
	}

	private void drawPlayer(int row, int col) {
		drawEmpty(row, col);
		bufferGraphics.setColor(new Color(255, 200, 0));
		bufferGraphics.fillOval(col * cellSize + 4, row * cellSize + 4,
				cellSize - 8, cellSize - 8);
		bufferGraphics.setColor(new Color(255, 0, 0));
		bufferGraphics.setStroke(new BasicStroke(4));
		bufferGraphics.drawOval(col * cellSize + 4, row * cellSize + 4,
				cellSize - 8, cellSize - 8);
		bufferGraphics.fillOval((int) ((col + 0.25) * cellSize),
				(int) ((row + 0.4) * cellSize - 2), 8, 8);
		bufferGraphics.fillOval((int) ((col + 0.75) * cellSize - 8),
				(int) ((row + 0.4) * cellSize - 2), 8, 8);
		bufferGraphics.fillRect((int) ((col + 0.5) * cellSize - 3),
				(int) ((row + 0.7) * cellSize - 2), 6, 2);
		bufferGraphics.setStroke(new BasicStroke(1));
	}

	private void drawEmpty(int row, int col) {
		bufferGraphics.setColor(new Color(30, 30, 55));
		bufferGraphics.fillRect(col * cellSize, row * cellSize, cellSize,
				cellSize);
	}

	public void addKeyListener(KeyListener list) {
		frame.addKeyListener(list);
	}
}
