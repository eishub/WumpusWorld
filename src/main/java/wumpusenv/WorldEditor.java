package wumpusenv;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.SwingUtilities;

/**
 * WorldEditor allows user to edit a world, save it and use it to play the game.
 */
public class WorldEditor extends Panel implements Listener {
	private static final long serialVersionUID = 1L;

	private CaveView worldView;
	private WorldModel worldModel;
	private int state;
	private final WumpusApp parent;
	public static final int GROUND = 0;
	public static final int AGENT = 1;
	public static final int WUMPUS = 2;
	public static final int PIT = 3;
	public static final int GOLD = 4;
	public static final int WALL = 5;
	public static final int CLEAR = 6;

	public WorldEditor(final WumpusApp parent) {
		super();
		this.parent = parent;
		this.worldModel = new WorldModel();

		if (parent.isGuiVisible()) {
			setLayout(new BorderLayout());
			this.worldView = new CaveView("WorldEditor", this);
			this.worldView.setZoom(7);
			final Panel controls = new Panel();
			controls.setLayout(new GridLayout(10, 1));
			final Button zoomIn = new Button("Zoom in");
			zoomIn.addActionListener(e -> WorldEditor.this.worldView.setZoom(WorldEditor.this.worldView.getZoom() - 1));
			final Button zoomOut = new Button("Zoom out");
			zoomIn.addActionListener(e -> WorldEditor.this.worldView.setZoom(WorldEditor.this.worldView.getZoom() + 1));
			final Button wall = new Button("Wall");
			wall.addActionListener(e -> setState(WALL));
			final Button pit = new Button("Pit");
			pit.addActionListener(e -> setState(PIT));
			final Button agent = new Button("Agent");
			agent.addActionListener(e -> setState(AGENT));
			final Button wumpus = new Button("Wumpus");
			wumpus.addActionListener(e -> setState(WUMPUS));
			final Button gold = new Button("Gold");
			gold.addActionListener(e -> setState(GOLD));
			final Button ground = new Button("Ground");
			ground.addActionListener(e -> setState(GROUND));
			final Button clear = new Button("Clear square");
			clear.addActionListener(e -> setState(CLEAR));
			final Button update = new Button("Update");
			update.addActionListener(e -> {
				WorldEditor.this.worldModel.reset();
				WorldEditor.this.worldView.update();
			});
			controls.add(zoomIn);
			controls.add(zoomOut);
			controls.add(wall);
			controls.add(pit);
			controls.add(agent);
			controls.add(wumpus);
			controls.add(gold);
			controls.add(ground);
			controls.add(clear);
			controls.add(update);
			final Panel controlPanel = new Panel();
			controlPanel.setLayout(new BorderLayout());
			controlPanel.add(BorderLayout.NORTH, controls);
			add(BorderLayout.CENTER, this.worldView);
			add(BorderLayout.EAST, controlPanel);
			setState(GROUND);
		}
	}

	public void setScaleImagesMode(final boolean b) {
		this.worldView.setScaleImagesMode(b);
	}

	@Override
	public boolean handleSquareEvent(final Point square, final MouseEvent evt) {
		final boolean right = SwingUtilities.isRightMouseButton(evt);
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
				final Point p = this.worldModel.getWumpusLocation();
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
	public boolean handleMultiSquareEvent(final Rectangle squares, final MouseEvent evt) {
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
	public Image getImage(final String name) {
		return this.parent.getImage(name);
	}

	public WorldModel getModel(final String id) {
		return this.worldModel;
	}

	@Override
	public WorldModel getModel() {
		return this.worldModel;
	}

	public void setModel(final WorldModel model) {
		this.worldModel = model;
		this.worldModel.reset();
		this.worldView.update();
	}

	public String saveTo(final java.io.File file) {
		return this.worldModel.saveTo(file);
	}

	public String loadFrom(final java.io.File file) {
		try {
			this.worldModel = this.worldModel.loadFrom(file);
			this.worldView.update();
			return "";
		} catch (final Exception ex) {
			System.out.println(ex.toString());
			return ex.toString();
		}
	}

	public void loadFrom(final URL url) throws IOException {
		this.worldModel = this.worldModel.loadFrom(url.openStream());
		if (this.parent.isGuiVisible()) {
			this.worldView.update();
		}
	}

	private void setState(final int state) {
		this.state = state;
	}
}
