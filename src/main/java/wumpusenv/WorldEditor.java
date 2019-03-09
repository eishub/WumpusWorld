package wumpusenv;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Event;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.io.IOException;
import java.net.URL;

/**
 * WorldEditor allows user to edit a world, save it and use it to play the game.
 *
 * @author Jan Misker,
 */
public class WorldEditor extends Panel implements Listener {
	private static final long serialVersionUID = 1L;
	private CaveView worldView;
	private WorldModel worldModel;
	private static String ZOOMIN = "Zoom in";
	private static String ZOOMUIT = "Zoom out";
	private static String WALL_S = "Wall";
	private static String PIT_S = "Pit";
	private static String WUMPUS_S = "Wumpus";
	private static String AGENT_S = "Agent";
	private static String GOLD_S = "Gold";
	private static String GROUND_S = "Ground";
	private static String CLEAR_S = "Clear square";
	private TextArea status;
	private int state;
	private WumpusApp parent;
	private String[] stateArray = { GROUND_S, AGENT_S, WUMPUS_S, PIT_S, GOLD_S, WALL_S, CLEAR_S };
	public static final int GROUND = 0;
	public static final int AGENT = 1;
	public static final int WUMPUS = 2;
	public static final int PIT = 3;
	public static final int GOLD = 4;
	public static final int WALL = 5;
	public static final int CLEAR = 6;

	public WorldEditor(WumpusApp parent) {
		super();
		this.parent = parent;
		this.worldModel = new WorldModel();
		if (parent.isGuiVisible()) {
			setLayout(new BorderLayout());
			this.worldView = new CaveView("WorldEditor", this);
			this.worldView.setZoom(7);
			this.status = new TextArea("Status", 1, 1);
			Panel controls = new Panel();
			controls.setLayout(new GridLayout(10, 1));
			controls.add(new Button(ZOOMIN));
			controls.add(new Button(ZOOMUIT));
			controls.add(new Button(WALL_S));
			controls.add(new Button(PIT_S));
			controls.add(new Button(AGENT_S));
			controls.add(new Button(WUMPUS_S));
			controls.add(new Button(GOLD_S));
			controls.add(new Button(GROUND_S));
			controls.add(new Button(CLEAR_S));
			controls.add(new Button("Update"));
			Panel controlPanel = new Panel();
			controlPanel.setLayout(new BorderLayout());
			controlPanel.add("North", controls);
			// controlPanel.add("Center", status);
			add("Center", this.worldView);
			add("East", controlPanel);
			setState(GROUND);
		}
	}

	public void setScaleImagesMode(boolean b) {
		this.worldView.setScaleImagesMode(b);
	}

	@Override
	public boolean handleSquareEvent(Point square, Event evt) {
		boolean right = (evt.modifiers & Event.META_MASK) == Event.META_MASK;
		this.status.appendText("\n" + right + " (" + square.x + "," + square.y + ")");
		switch (this.state) {
		case GROUND:
			if (!right) {
				this.worldModel.setSquare(square, WorldModel.GROUND);
			}
			return true;
		case WALL:
			if (right) {
				this.worldModel.removeItem(square, WorldModel.WALL);
			} else {
				this.worldModel.setSquare(square, WorldModel.WALL | WorldModel.GROUND);
			}
			return true;
		case PIT:
			if (right) {
				this.worldModel.removeItem(square, WorldModel.PIT);
			} else {
				this.worldModel.setSquare(square, WorldModel.PIT | WorldModel.GROUND);
			}
			this.worldModel.removeBreeze();
			this.worldModel.addBreeze();
			return true;
		case AGENT:
			if (!right) {
				this.worldModel.setAgentLocation(square);
				this.worldModel.setStartLocation(square);
			}
			return true;
		case WUMPUS:
			if (!right) {
				Point p = this.worldModel.getWumpusLocation();
				if (p != null) {
					this.worldModel.removeItem(new Point(p.x, p.y + 1), WorldModel.SMELL);
					this.worldModel.removeItem(new Point(p.x, p.y - 1), WorldModel.SMELL);
					this.worldModel.removeItem(new Point(p.x + 1, p.y), WorldModel.SMELL);
					this.worldModel.removeItem(new Point(p.x - 1, p.y), WorldModel.SMELL);
				}
				this.worldModel.setWumpusLocation(square);
				this.worldModel.addItem(square.x, square.y + 1, WorldModel.SMELL);
				this.worldModel.addItem(square.x, square.y - 1, WorldModel.SMELL);
				this.worldModel.addItem(square.x + 1, square.y, WorldModel.SMELL);
				this.worldModel.addItem(square.x - 1, square.y, WorldModel.SMELL);
			}
			return true;
		case GOLD:
			if (!right) {
				this.worldModel.setGoldLocation(square);
			}
			return true;
		case CLEAR:
			if (!right) {
				this.worldModel.setSquare(square, WorldModel.CLEAR);
			}
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean handleMultiSquareEvent(Rectangle squares, Event evt) {
		boolean right = (evt.modifiers & Event.META_MASK) == Event.META_MASK;
		this.status.appendText(
				"\n" + right + " (" + squares.x + "," + squares.y + "," + squares.width + "," + squares.height + ")");
		switch (this.state) {
		case GROUND:
			for (int i = squares.x; i <= squares.x + squares.width; i++) {
				for (int j = squares.y; j <= squares.y + squares.height; j++) {
					this.worldModel.setSquare(i, j, WorldModel.GROUND);
				}
			}
			return true;
		case CLEAR:
			for (int i = squares.x; i <= squares.x + squares.width; i++) {
				for (int j = squares.y; j <= squares.y + squares.height; j++) {
					this.worldModel.setSquare(i, j, WorldModel.CLEAR);
				}
			}
			return true;
		case WALL:
			for (int i = squares.x; i <= squares.x + squares.width; i++) {
				this.worldModel.setSquare(i, squares.y, WorldModel.WALL | WorldModel.GROUND);
				this.worldModel.setSquare(i, squares.y + squares.height, WorldModel.WALL | WorldModel.GROUND);
			}
			for (int j = squares.y; j <= squares.y + squares.height; j++) {
				this.worldModel.setSquare(squares.x, j, WorldModel.WALL | WorldModel.GROUND);
				this.worldModel.setSquare(squares.x + squares.width, j, WorldModel.WALL | WorldModel.GROUND);
			}
			return true;
		default:
			return false;
		}
	}

	@Override
	public Image getImage(String name) {
		return this.parent.getImage(name);
	}

	public WorldModel getModel(String id) {
		return this.worldModel;
	}

	@Override
	public WorldModel getModel() {
		return this.worldModel;
	}

	public void setModel(WorldModel model) {
		this.worldModel = model;
		this.worldModel.reset();
		this.worldView.update();
	}

	public String saveTo(java.io.File file) {
		return this.worldModel.saveTo(file);
	}

	public String loadFrom(java.io.File file) {
		try {
			this.worldModel = this.worldModel.loadFrom(file);
			this.worldView.update();
			return "";
		} catch (Exception ex) {
			System.out.println(ex.toString());
			return ex.toString();
		}
	}

	/**
	 * DOC
	 *
	 * @param url
	 * @throws IOException
	 */
	public void loadFrom(URL url) throws IOException {
		this.worldModel = this.worldModel.loadFrom(url.openStream());
		if (this.parent.isGuiVisible()) {
			this.worldView.update();
		}
	}

	@Override
	public boolean handleEvent(Event evt) {
		return super.handleEvent(evt);
	}

	private void setState(int state) {
		this.state = state;
		this.status.appendText("\n" + this.stateArray[state]);
	}

	@Override
	public boolean action(Event evt, Object what) {
		if (what == ZOOMIN) {
			this.worldView.setZoom(this.worldView.getZoom() - 1);
			return true;
		} else if (what == ZOOMUIT) {
			this.worldView.setZoom(this.worldView.getZoom() + 1);
			return true;
		} else if (what == GOLD_S) {
			setState(GOLD);
			return true;
		} else if (what == PIT_S) {
			setState(PIT);
			return true;
		} else if (what == AGENT_S) {
			setState(AGENT);
			return true;
		} else if (what == WALL_S) {
			setState(WALL);
			return true;
		} else if (what == WUMPUS_S) {
			setState(WUMPUS);
			return true;
		} else if (what == CLEAR_S) {
			setState(CLEAR);
			return true;
		} else if (what == GROUND_S) {
			setState(GROUND);
			return true;
		} else if ("Update".equals(what)) {
			this.worldModel.reset();
			this.worldView.update();
			return true;
		} else {
			return false;
		}
	}
}
