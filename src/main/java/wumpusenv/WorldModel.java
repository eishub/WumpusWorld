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
import java.util.HashMap;
import java.util.Map;

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
	private Map<Point, Integer> cave;
	private Rectangle bounds;
	// Position and status of several items in Wumpus World.
	private Point agent, wumpus, start, gold;
	private int fAgentOrientation;
	private boolean fAgentHasArrow = true, fAgentClimbedOut = false, fAgentHasGold = false, fWumpusIsAlive = true;

	public WorldModel() {
		this.cave = new HashMap<>();
		this.bounds = new Rectangle(1, 1, 1, 1);
		setStartLocation(new Point(0, 0));
		setGoldLocation(new Point(2, 2));
		reset();
	}

	public void reset() {
		setAgentOrientation(0);
		setAgentLocation(getStartLocation());
		this.fAgentHasArrow = true;
		this.fAgentHasGold = false;
		this.fAgentClimbedOut = false;
		this.fWumpusIsAlive = true;

		removeBreeze();
		addBreeze();
		removeSmell();
		addSmell();
		setSquare(this.gold, GOLD | getSquare(this.gold));
	}

	public void addItem(int x, int y, int item) {
		addItem(new Point(x, y), item);
	}

	public void addItem(Point square, int item) {
		Integer oldData = this.cave.get(square);
		if (oldData == null) {
			oldData = new Integer(GROUND);
		}
		setSquare(square, oldData.intValue() | item);
	}

	public void removeItem(Point square, int item) {
		if (this.cave.get(square) == null) {
			return;
		}
		Integer oldData = this.cave.get(square);
		if ((item == GROUND) || (item == CLEAR)) {
			setSquare(square, CLEAR);
		} else {
			setSquare(square, oldData.intValue() & ~item);
		}
	}

	public int getSquare(Point square) {
		Object data = this.cave.get(square);
		if (data == null) {
			return CLEAR;
		} else {
			return ((Integer) data).intValue();
		}
	}

	/**
	 * Check if square contains given item.
	 *
	 * @param square is position on the grid
	 * @param item   is item that might be at location.
	 * @return true if given grid square contains the item
	 */
	public boolean contains(Point square, int item) {
		int tmp = getSquare(square);
		if (tmp == CLEAR) {
			return false;
		} else {
			return (tmp & item) == item;
		}
	}

	public void setSquare(int x, int y, int data) {
		setSquare(new Point(x, y), data);
	}

	public void setSquare(Point square, int data) {
		// System.out.println("setSquare "+data+" at "+square);
		if (square.x < this.bounds.x) {
			this.bounds.width += this.bounds.x - square.x;
			this.bounds.x = square.x;
		}
		if (square.x >= this.bounds.width + this.bounds.x) {
			this.bounds.width = square.x - this.bounds.x + 1;
		}
		if (square.y < this.bounds.y) {
			this.bounds.height += this.bounds.y - square.y;
			this.bounds.y = square.y;
		}
		if (square.y >= this.bounds.height + this.bounds.y) {
			this.bounds.height = square.y - this.bounds.y + 1;
		}
		this.cave.put(square, new Integer(data));
	}

	public Rectangle getBounds() {
		return this.bounds;
	}

	public void setAgentLocation(Point p) {
		if (this.agent == null) {
			this.agent = new Point(p.x, p.y);
			addItem(this.agent, AGENT);
			return;
		}
		removeItem(this.agent, AGENT);
		this.agent = new Point(p.x, p.y);
		addItem(this.agent, AGENT);
	}

	public Point getAgentLocation() {
		if (this.agent == null) {
			return null;
		}
		return new Point(this.agent.x, this.agent.y);
	}

	public void setGoldLocation(Point p) {
		if (this.gold == null) {
			this.gold = new Point(p.x, p.y);
			addItem(this.gold, GOLD);
			return;
		}
		removeItem(this.gold, GOLD);
		this.gold = new Point(p.x, p.y);
		addItem(this.gold, GOLD);
	}

	public Point getGoldLocation() {
		if (this.gold == null) {
			return null;
		}
		return new Point(this.gold.x, this.gold.y);
	}

	public void setStartLocation(Point p) {
		if (this.start == null) {
			this.start = new Point(p.x, p.y);
			addItem(this.start, START);
			return;
		}
		removeItem(this.start, START);
		this.start = new Point(p.x, p.y);
		addItem(this.start, START);
	}

	public Point getStartLocation() {
		if (this.start == null) {
			return null;
		}
		return new Point(this.start.x, this.start.y);
	}

	public void setWumpusLocation(Point p) {
		if (this.wumpus == null) {
			this.wumpus = new Point(p.x, p.y);
			addItem(this.wumpus, WUMPUS);
			return;
		}
		removeItem(this.wumpus, WUMPUS);
		this.wumpus = new Point(p.x, p.y);
		addItem(this.wumpus, WUMPUS);
	}

	public Point getWumpusLocation() {
		if (this.wumpus == null) {
			return null;
		}
		return new Point(this.wumpus.x, this.wumpus.y);
	}

	public void setAgentOrientation(int o) {
		this.fAgentOrientation = o % 360;
	}

	public int getAgentOrientation() {
		return this.fAgentOrientation;
	}

	public void setAgentHasArrow(boolean b) {
		this.fAgentHasArrow = b;
	}

	public boolean agentHasArrow() {
		return this.fAgentHasArrow;
	}

	public void setAgentHasGold(boolean b) {
		this.fAgentHasGold = b;
	}

	public boolean agentHasGold() {
		return this.fAgentHasGold;
	}

	public void setWumpusIsAlive(boolean b) {
		this.fWumpusIsAlive = b;
	}

	public boolean wumpusIsAlive() {
		return this.fWumpusIsAlive;
	}

	public void setAgentClimbedOut() {
		this.fAgentClimbedOut = true;
	}

	/**
	 * Checks whether the game has finished. Game finishes if agent climbs out of
	 * the cave or the agent is dead.
	 * <p>
	 * Agent is dead if he is on top of living wumpus or in a pit.
	 * </p>
	 *
	 * @author W.Pasman
	 */
	public boolean gameFinished() {
		boolean finished = this.fAgentClimbedOut;
		finished = finished || agentKilled();
		return finished;
	}

	/**
	 * DOC
	 *
	 * @return
	 */
	public boolean agentKilled() {
		boolean killed = (contains(this.agent, WUMPUS) && this.fWumpusIsAlive);
		killed = killed || contains(this.agent, PIT);
		return killed;
	}

	/**
	 * Checks whether the world is runnable as it is now.
	 *
	 * @return {@code true} if runnable; {@code false} otherwise.
	 */
	public boolean isRunnable() {
		return this.agent != null && !agentKilled();
	}

	/**
	 * DOC
	 */
	public void addBreeze() {
		for (Point square : this.cave.keySet()) {
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
		if (old == CLEAR) {
			setSquare(p, BREEZE);
		} else {
			addItem(p, BREEZE);
		}
	}

	public void removeBreeze() {
		for (Point square : this.cave.keySet()) {
			if (contains(square, BREEZE)) {
				removeItem(square, BREEZE);
			}
		}
	}

	public void addSmell() {
		Point square = getWumpusLocation();
		if (square == null) {
			return;
		}
		addSmell(square.x, square.y + 1);
		addSmell(square.x, square.y - 1);
		addSmell(square.x + 1, square.y);
		addSmell(square.x - 1, square.y);
	}

	private void addSmell(int x, int y) {
		Point p = new Point(x, y);
		int old = getSquare(p);
		if (old == CLEAR) {
			setSquare(p, SMELL);
		} else {
			addItem(p, SMELL);
		}
	}

	public void removeSmell() {
		for (Point square : this.cave.keySet()) {
			if (contains(square, SMELL)) {
				removeItem(square, SMELL);
			}
		}
	}

	/** Wouter: added for convenient debugging. */
	/*
	 * It's beyond me why Jan did not use a simple format like this for the files as
	 * well
	 */
	@Override
	public String toString() {
		String result = "";
		for (Point key : this.cave.keySet()) {
			result = result + "point(" + key.x + "," + key.y + " ";
			result = result + this.cave.get(key).intValue() + ") ";
		}
		return result;
	}

	public String saveTo(File file) {
		try {
			if (file.exists()) {
				file.delete();
			}
			DataOutputStream fileOutput = new DataOutputStream(new FileOutputStream(file));
			fileOutput.writeUTF(HEADER);
			fileOutput.writeInt(this.cave.size());
			for (Point key : this.cave.keySet()) {
				fileOutput.writeInt(key.x);
				fileOutput.writeInt(key.y);
				fileOutput.writeInt(this.cave.get(key).intValue());
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
		if (!fileInput.readUTF().equals(HEADER)) {
			throw new IOException("Invalid WorldModel-file");
		}
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
