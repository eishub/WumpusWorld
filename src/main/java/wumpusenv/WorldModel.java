package wumpusenv;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author Jan Misker Wouter: WorldModel contains a "map" of the world. It can
 *         NOT draw itself, use a CaveView for that. IMPORTANT: A square in the
 *         WorldModel is drawn ONLY if that square also contains GROUND. I have
 *         no idea why Jan decided to do it this way. There is a simple test
 *         whether the agent is still alive, agentIsAlive(), based on the board
 *         situation.
 */

public class WorldModel {
	public static final int CLEAR = 0x0;
	public static final int GROUND = 0x1;
	public static final int PIT = 0x2;
	public static final int GOLD = 0x4;
	public static final int WALL = 0x8;
	public static final int AGENT = 0x10;
	public static final int WUMPUS = 0x20;
	public static final int START = 0x40;
	public static final int BREEZE = 0x80;
	public static final int SMELL = 0x100;
	public static final int VISITED = 0x200;
	public static final int OK = 0x400;
	public static final String HEADER = "WumpusWorldModelFile0.9\n";
	private Hashtable cave;
	private Rectangle bounds;
	// Position and status of several items in Wumpus World.
	private Point agent, wumpus, start, gold;
	private int fAgentOrientation;
	private boolean fAgentHasArrow = true, fAgentClimbedOut = false,
			fAgentHasGold = false, fWumpusIsAlive = true;

	public WorldModel() {
		cave = new Hashtable();
		bounds = new Rectangle(1, 1, 1, 1);
		setStartLocation(new Point(0, 0));
		setGoldLocation(new Point(2, 2));
		reset();
	}

	public void reset() {
		setAgentOrientation(0);
		setAgentLocation(getStartLocation());
		fAgentHasArrow = true;
		fAgentHasGold = false;
		fAgentClimbedOut = false;
		fWumpusIsAlive = true;

		removeBreeze();
		addBreeze();
		removeSmell();
		addSmell();
		setSquare(gold, GOLD | getSquare(gold));
	}

	public void addItem(int x, int y, int item) {
		addItem(new Point(x, y), item);
	}

	public void addItem(Point square, int item) {
		Integer oldData = (Integer) cave.get(square);
		if (oldData == null) {
			oldData = new Integer(GROUND);
		}
		setSquare(square, oldData.intValue() | item);
	}

	public void removeItem(Point square, int item) {
		if (cave.get(square) == null) {
			return;
		}
		Integer oldData = (Integer) cave.get(square);
		if ((item == GROUND) || (item == CLEAR)) {
			setSquare(square, CLEAR);
		} else {
			setSquare(square, oldData.intValue() & ~item);
		}
	}

	public int getSquare(Point square) {
		Object data = cave.get(square);
		if (data == null)
			return CLEAR;
		else
			return ((Integer) data).intValue();
	}

	/**
	 * Check if square contains given item.
	 * 
	 * @param square
	 *            is position on the grid
	 * @param item
	 *            is item that might be at location.
	 * @return true if given grid square contains the item
	 */
	public boolean contains(Point square, int item) {
		int tmp = getSquare(square);
		if (tmp == CLEAR)
			return false;
		else {
			return (tmp & item) == item;
		}
	}

	public void setSquare(int x, int y, int data) {
		setSquare(new Point(x, y), data);
	}

	public void setSquare(Point square, int data) {
		// System.out.println("setSquare "+data+" at "+square);
		if (square.x < bounds.x) {
			bounds.width += bounds.x - square.x;
			bounds.x = square.x;
		}
		if (square.x >= bounds.width + bounds.x) {
			bounds.width = square.x - bounds.x + 1;
		}
		if (square.y < bounds.y) {
			bounds.height += bounds.y - square.y;
			bounds.y = square.y;
		}
		if (square.y >= bounds.height + bounds.y) {
			bounds.height = square.y - bounds.y + 1;
		}
		cave.put(square, new Integer(data));
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public void setAgentLocation(Point p) {
		if (agent == null) {
			agent = new Point(p.x, p.y);
			addItem(agent, AGENT);
			return;
		}
		removeItem(agent, AGENT);
		agent = new Point(p.x, p.y);
		addItem(agent, AGENT);
	}

	public Point getAgentLocation() {
		if (agent == null)
			return null;
		return new Point(agent.x, agent.y);
	}

	public void setGoldLocation(Point p) {
		if (gold == null) {
			gold = new Point(p.x, p.y);
			addItem(gold, GOLD);
			return;
		}
		removeItem(gold, GOLD);
		gold = new Point(p.x, p.y);
		addItem(gold, GOLD);
	}

	public Point getGoldLocation() {
		if (gold == null)
			return null;
		return new Point(gold.x, gold.y);
	}

	public void setStartLocation(Point p) {
		if (start == null) {
			start = new Point(p.x, p.y);
			addItem(start, START);
			return;
		}
		removeItem(start, START);
		start = new Point(p.x, p.y);
		addItem(start, START);
	}

	public Point getStartLocation() {
		if (start == null)
			return null;
		return new Point(start.x, start.y);
	}

	public void setWumpusLocation(Point p) {
		if (wumpus == null) {
			wumpus = new Point(p.x, p.y);
			addItem(wumpus, WUMPUS);
			return;
		}
		removeItem(wumpus, WUMPUS);
		wumpus = new Point(p.x, p.y);
		addItem(wumpus, WUMPUS);
	}

	public Point getWumpusLocation() {
		if (wumpus == null)
			return null;
		return new Point(wumpus.x, wumpus.y);
	}

	public void setAgentOrientation(int o) {
		fAgentOrientation = o % 360;
	}

	public int getAgentOrientation() {
		return fAgentOrientation;
	}

	public void setAgentHasArrow(boolean b) {
		fAgentHasArrow = b;
	}

	public boolean agentHasArrow() {
		return fAgentHasArrow;
	}

	public void setAgentHasGold(boolean b) {
		fAgentHasGold = b;
	}

	public boolean agentHasGold() {
		return fAgentHasGold;
	}

	public void setWumpusIsAlive(boolean b) {
		fWumpusIsAlive = b;
	}

	public boolean wumpusIsAlive() {
		return fWumpusIsAlive;
	}

	public void setAgentClimbedOut() {
		fAgentClimbedOut = true;
	}

	/**
	 * Checks whether the game has finished. Game finishes if agent climbs out
	 * of the cave or the agent is dead.
	 * <p>
	 * Agent is dead if he is on top of living wumpus or in a pit.
	 * </p>
	 * 
	 * @author W.Pasman
	 */
	public boolean gameFinished() {
		boolean finished = fAgentClimbedOut;
		finished = finished || agentKilled();
		return finished;
	}

	/**
	 * DOC
	 * 
	 * @return
	 */
	public boolean agentKilled() {
		boolean killed = (contains(agent, WUMPUS) && fWumpusIsAlive);
		killed = killed || contains(agent, PIT);
		return killed;
	}

	/**
	 * Checks whether the world is runnable as it is now.
	 * 
	 * @return {@code true} if runnable; {@code false} otherwise.
	 */
	public boolean isRunnable() {
		return agent != null && !agentKilled();
	}

	/**
	 * DOC
	 */
	public void addBreeze() {
		Enumeration squares = cave.keys();
		while (squares.hasMoreElements()) {
			Point square = (Point) squares.nextElement();
			if (contains(square, PIT)) {
				addBreeze(square.x, square.y + 1);
				addBreeze(square.x, square.y - 1);
				addBreeze(square.x + 1, square.y);
				addBreeze(square.x - 1, square.y);
			}
		}
	}

	private void addBreeze(int x, int y) {
		Point p = new Point(x, y);
		int old = getSquare(p);
		if (old == CLEAR)
			setSquare(p, BREEZE);
		else
			addItem(p, BREEZE);
	}

	public void removeBreeze() {
		Enumeration squares = cave.keys();
		while (squares.hasMoreElements()) {
			Point square = (Point) squares.nextElement();
			if (contains(square, BREEZE))
				removeItem(square, BREEZE);
		}
	}

	public void addSmell() {
		Point square = getWumpusLocation();
		if (square == null)
			return;
		addSmell(square.x, square.y + 1);
		addSmell(square.x, square.y - 1);
		addSmell(square.x + 1, square.y);
		addSmell(square.x - 1, square.y);
	}

	private void addSmell(int x, int y) {
		Point p = new Point(x, y);
		int old = getSquare(p);
		if (old == CLEAR)
			setSquare(p, SMELL);
		else
			addItem(p, SMELL);
	}

	public void removeSmell() {
		Enumeration squares = cave.keys();
		while (squares.hasMoreElements()) {
			Point square = (Point) squares.nextElement();
			if (contains(square, SMELL))
				removeItem(square, SMELL);
		}
	}

	/** Wouter: added for convenient debugging. */
	/*
	 * It's beyond me why Jan did not use a simple format like this for the
	 * files as well
	 */
	public String toString() {
		String result = "";

		Enumeration enu = cave.keys();
		while (enu.hasMoreElements()) {
			Point key = (Point) enu.nextElement();
			result = result + "point(" + key.x + "," + key.y + " ";
			result = result + ((Integer) cave.get(key)).intValue() + ") ";
		}
		return result;
	}

	public String saveTo(File file) {
		try {
			if (file.exists())
				file.delete();
			DataOutputStream fileOutput = new DataOutputStream(
					new FileOutputStream(file));
			fileOutput.writeUTF(HEADER);
			Enumeration enu = cave.keys();
			fileOutput.writeInt(cave.size());
			while (enu.hasMoreElements()) {
				Point key = (Point) enu.nextElement();
				fileOutput.writeInt(key.x);
				fileOutput.writeInt(key.y);
				fileOutput.writeInt(((Integer) cave.get(key)).intValue());
			}
			fileOutput.flush();
			fileOutput.close();
			return "";
		} catch (Exception ex) {
			System.err.println(ex.toString());
			return "ex.toString()";
		}
	}

	public WorldModel loadFrom(File file) throws Exception {
		return loadFrom(new FileInputStream(file));
	}

	/**
	 * DOC
	 * 
	 * @param input
	 * @return 
	 * @throws IOException 
	 */
	public WorldModel loadFrom(InputStream input) throws IOException {
		DataInputStream fileInput = new DataInputStream(input);
		if (!fileInput.readUTF().equals(HEADER))
			throw new IOException("Invalid WorldModel-file");
		int size = fileInput.readInt();
		WorldModel loadModel = new WorldModel();
		for (int i = 0; i < size; i++) {
			Point square = new Point(fileInput.readInt(), fileInput.readInt());
			int value = (new Integer(fileInput.readInt())).intValue();
			loadModel.setSquare(square, value);
			if ((value & WUMPUS) == WUMPUS) {
				loadModel.setWumpusLocation(square);
			}
			if ((value & AGENT) == AGENT) {
				loadModel.setAgentLocation(square);
				loadModel.setStartLocation(square);
			}
			if ((value & GOLD) == GOLD) {
				loadModel.setGoldLocation(square);
			}
		}
		fileInput.close();
		return loadModel;
	}
}
