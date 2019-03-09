package wumpusenv;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * WumpusWorld is a grid of squares. This is a canvas with the wumpus world
 * painted on it. It needs a WumpusCanvasListener object that maintains the
 * Wumpus World model. The hierarchy is a bit messy because the WumpusCanvas has
 * a pointer back to the parent {@link CaveView} to handle listener calllbacks.
 */
class WumpusCanvas extends Canvas {
	private static final long serialVersionUID = 1L;
	private int zoom, xFit, yFit;
	/**
	 * This is the current size of a square (pixels ?)
	 */
	private int squareSize;
	/**
	 * The grid position of the middle of the canvas. (block position)
	 */
	private Point focus; // squares
	private Point center; // pixels
	private Rectangle drawSpace, zoomIn, zoomUit;
	private CaveView wcListener;
	private WorldModel worldModel;
	private Image offImage;
	private Graphics offGraphics;
	private Dimension offDimension;
	private Point mouseStart, dragStart, dragEnd;
	private Image agentImg[], groundImg, wallImg, goldImg, wumpusImg, breezeImg, smellImg;
	private boolean scaleImageMode;

	/**
	 * DOC
	 *
	 * @param wcListener
	 */
	public WumpusCanvas(CaveView wcListener) {
		super();
		this.wcListener = wcListener;
		this.scaleImageMode = true;
		this.agentImg = new Image[4];
		this.agentImg[0] = getImage("agent0.gif");
		this.agentImg[1] = getImage("agent90.gif");
		this.agentImg[2] = getImage("agent180.gif");
		this.agentImg[3] = getImage("agent270.gif");
		this.wallImg = getImage("wall.gif");
		this.goldImg = getImage("gold.gif");
		this.wumpusImg = getImage("wumpus.gif");
		this.groundImg = getImage("ground.gif");
		this.breezeImg = getImage("breeze.gif");
		this.smellImg = getImage("smell.gif");
		// agentImg[0] = getImage("agent0");
		// agentImg[1] = getImage("agent90");
		// agentImg[2] = getImage("agent180");
		// agentImg[3] = getImage("agent270");
		// wallImg = getImage("wall");
		// goldImg = getImage("gold");
		// wumpusImg = getImage("wumpus");
		// groundImg = getImage("ground");
		// breezeImg = getImage("breeze");
		// smellImg = getImage("smell");
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(this.agentImg[0], 0);
		mt.addImage(this.agentImg[1], 1);
		mt.addImage(this.agentImg[2], 2);
		mt.addImage(this.agentImg[3], 3);
		mt.addImage(this.wallImg, 4);
		mt.addImage(this.groundImg, 5);
		mt.addImage(this.goldImg, 6);
		mt.addImage(this.wumpusImg, 7);
		mt.addImage(this.breezeImg, 8);
		mt.addImage(this.smellImg, 9);
		// mt.addImage(wallImg, 8);
		try {
			mt.waitForAll();
		} catch (Exception ex) {
			System.err.println(ex);
		}
		this.focus = new Point(1, 1);
		this.zoom = 5;
		this.drawSpace = new Rectangle(20, 20, size().width - 40, size().height - 40);
	}

	public void setScaleImagesMode(boolean b) {
		if (this.scaleImageMode != b) {
			this.scaleImageMode = b;
			this.offGraphics = null;
			repaint();
		}
	}

	public Image getImage(String name) {
		return this.wcListener.getImage(name);
	}

	@Override
	public void paint(Graphics g) {
		update(g);
	}

	@Override
	public void update(Graphics g) {
		this.worldModel = this.wcListener.getModel();
		// System.out.println("updating wumpuscanvas ="+worldModel.toString());
		this.drawSpace = new Rectangle(20, 20, size().width - 40, size().height - 40);
		this.center = new Point((this.drawSpace.width) / 2 + this.drawSpace.x,
				(this.drawSpace.height) / 2 + this.drawSpace.y);
		if (this.scaleImageMode) {
			this.squareSize = Math.min(this.drawSpace.width, this.drawSpace.height) / (this.zoom + 1);
		} else {
			this.squareSize = 50;
		}
		this.xFit = this.drawSpace.width / this.squareSize + 3;
		this.yFit = this.drawSpace.height / this.squareSize + 3;

		if ((this.offGraphics == null) || (size().width != this.offDimension.width)
				|| (size().height != this.offDimension.height)) {
			this.offDimension = size();
			this.offImage = createImage(size().width, size().height);
			this.offGraphics = this.offImage.getGraphics();
		}
		// offGraphics.clipRect(0, 0, size().width, size().height);
		this.offGraphics.setFont(new Font("TimesRoman", Font.PLAIN, 12));
		this.offGraphics.setColor(Color.white);
		this.offGraphics.fillRect(0, 0, size().width, size().height);
		this.offGraphics.setColor(Color.black);
		this.offGraphics.drawRect(this.drawSpace.x, this.drawSpace.y, this.drawSpace.width, this.drawSpace.height);
		// offGraphics.clipRect(drawSpace.x, drawSpace.y, drawSpace.width,
		// drawSpace.height);
		drawGrid(this.offGraphics);
		for (int x = this.focus.x - (this.xFit - 1) / 2; x < this.focus.x + (this.xFit + 1) / 2; x++) {
			for (int y = this.focus.y - (this.yFit - 1) / 2; y < this.focus.y + (this.yFit + 1) / 2; y++) {
				if (this.worldModel.contains(new Point(x, y), WorldModel.GROUND)) {
					try {
						drawSquare(this.offGraphics,
								this.center.x - this.squareSize / 2 + (x - this.focus.x) * this.squareSize,
								this.center.y - this.squareSize / 2 - (y - this.focus.y) * this.squareSize,
								this.worldModel.getSquare(new Point(x, y)),
								this.worldModel.getAgentOrientation() % 360);
					} catch (Exception e) {
						new ErrorDetails(e, " on drawSquare type" + this.worldModel.getSquare(new Point(x, y))
								+ "] orient: " + this.worldModel.getAgentOrientation() % 360);
					}
				}
			}
		}
		if ((this.mouseStart != null) && (this.dragEnd != null)) {
			this.offGraphics.setColor(Color.blue);
			this.offGraphics.drawRect(Math.min(this.dragStart.x, this.dragEnd.x),
					Math.min(this.dragStart.y, this.dragEnd.y),
					Math.max(this.dragEnd.x - this.dragStart.x, this.dragStart.x - this.dragEnd.x),
					Math.max(this.dragEnd.y - this.dragStart.y, this.dragStart.y - this.dragEnd.y));
		}
		this.offGraphics.setColor(Color.white);
		this.offGraphics.fillRect(0, 0, size().width, 20);
		this.offGraphics.fillRect(0, 0, 20, size().height);
		this.offGraphics.fillRect(0, size().height - 19, size().width, 20);
		this.offGraphics.fillRect(size().width - 19, 0, 20, size().height);
		drawCoor(this.offGraphics);
		drawZoom(this.offGraphics);
		g.drawImage(this.offImage, 0, 0, this);
	}

	private void drawGrid(Graphics g) {
		int xStart = this.drawSpace.x + (this.center.x - this.squareSize / 2 - this.drawSpace.x) % this.squareSize;
		int yStart = this.drawSpace.y + (this.center.y - this.squareSize / 2 - this.drawSpace.y) % this.squareSize;
		g.setColor(Color.lightGray);
		for (int x = xStart; x < this.drawSpace.width + this.drawSpace.x; x += this.squareSize) {
			g.drawLine(x, this.drawSpace.y, x, this.drawSpace.y + this.drawSpace.height);
		}
		for (int y = yStart; y < this.drawSpace.height + this.drawSpace.y; y += this.squareSize) {
			g.drawLine(this.drawSpace.x, y, this.drawSpace.x + this.drawSpace.width, y);
		}
	}

	private void drawCoor(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		g.setColor(Color.black);
		for (int x = this.focus.x - (this.xFit - 1) / 2; x < this.focus.x + (this.xFit + 1) / 2; x++) {
			g.drawString(String.valueOf(x),
					this.center.x + (x - this.focus.x) * this.squareSize - fm.stringWidth(String.valueOf(x)) / 2,
					this.drawSpace.y + this.drawSpace.height + 5 + fm.getAscent());
		}
		for (int y = this.focus.y - (this.yFit - 1) / 2; y < this.focus.y + (this.yFit + 1) / 2; y++) {
			g.drawString(String.valueOf(y), this.drawSpace.x - 5 - fm.stringWidth(String.valueOf(y)),
					this.center.y - (y - this.focus.y) * this.squareSize - fm.getDescent());
		}
	}

	private void drawZoom(Graphics g) {
		int lineHeight = g.getFont().getSize();
		int xOff = size().width - 15;
		this.zoomUit = new Rectangle(xOff, 10, 10, 10);
		g.setColor(Color.blue);
		g.fillRect(xOff, 15, 10, 2);
		g.fillRect(xOff + 4, 11, 2, 10);
		g.setColor(Color.black);
		g.drawString(String.valueOf(this.zoom), size().width - 15, 22 + lineHeight);
		g.setColor(Color.red);
		this.zoomIn = new Rectangle(xOff, 35, 10, 10);
		g.fillRect(xOff, 40, 10, 2);
	}

	private void drawSquare(Graphics g, int x, int y, int data, int orientation) throws Exception {
		// System.out.println("drawSquare at "+x+","+y+". orientation="+orientation);
		if (this.groundImg == null) {
			throw new Exception("ground image not available");
		}
		if (this.smellImg == null) {
			throw new Exception("smell image not available");
		}
		if (this.breezeImg == null) {
			throw new Exception("breeze image= not available");
		}
		if (this.wallImg == null) {
			throw new Exception("wall image not available");
		}
		if (this.goldImg == null) {
			throw new Exception("gold image not available");
		}
		if (this.wumpusImg == null) {
			throw new Exception("wumpus image not available");
		}

		if ((data & WorldModel.GROUND) == WorldModel.GROUND) {
			if (this.scaleImageMode) {
				g.drawImage(this.groundImg, x, y, this.squareSize, this.squareSize, this);
			} else {
				g.drawImage(this.groundImg, x, y, this);
			}
		}
		if ((data & WorldModel.PIT) == WorldModel.PIT) {
			g.setColor(Color.black);
			g.fillOval(x, y, this.squareSize, this.squareSize);
		}
		if ((data & WorldModel.BREEZE) == WorldModel.BREEZE) {
			if (this.scaleImageMode) {
				g.drawImage(this.breezeImg, x, y, this.squareSize, this.squareSize, this);
			} else {
				g.drawImage(this.breezeImg, x, y, this);
			}
		}
		if ((data & WorldModel.SMELL) == WorldModel.SMELL) {
			if (this.scaleImageMode) {
				g.drawImage(this.smellImg, x, y, this.squareSize, this.squareSize, this);
			} else {
				g.drawImage(this.smellImg, x, y, this);
			}
		}
		if ((data & WorldModel.WALL) == WorldModel.WALL) {
			/*
			 * g.setColor(Color.red.darker().darker()); g.fill3DRect(x, y, squareSize,
			 * squareSize, false);
			 */
			if (this.scaleImageMode) {
				g.drawImage(this.wallImg, x, y, this.squareSize, this.squareSize, this);
			} else {
				g.drawImage(this.wallImg, x, y, this);
			}
		}
		if ((data & WorldModel.GOLD) == WorldModel.GOLD) {
			if (this.scaleImageMode) {
				g.drawImage(this.goldImg, x, y, this.squareSize, this.squareSize, this);
			} else {
				g.drawImage(this.goldImg, x, y, this);
			}
		}
		if ((data & WorldModel.AGENT) == WorldModel.AGENT) {
			if (this.agentImg[orientation / 90] != null) {
				if (this.scaleImageMode) {
					g.drawImage(this.agentImg[orientation / 90], x, y, this.squareSize, this.squareSize, this);
				} else {
					g.drawImage(this.agentImg[orientation / 90], x, y, this);
				}
			}
		}
		if (((data & WorldModel.WUMPUS) == WorldModel.WUMPUS) && this.worldModel.wumpusIsAlive()) {
			if (this.scaleImageMode) {
				g.drawImage(this.wumpusImg, x, y, this.squareSize, this.squareSize, this);
			} else {
				g.drawImage(this.wumpusImg, x, y, this);
			}
		}
		if ((data & WorldModel.START) == WorldModel.START) {
			g.setColor(Color.red);
			g.drawRect(x, y, this.squareSize - 1, this.squareSize - 1);
		}
	}

	public void setZoom(int zoom) {
		if ((zoom != this.zoom) && (zoom > 0)) {
			this.zoom = zoom;
			this.offGraphics = null;
			repaint();
		}
	}

	/**
	 * set focus position
	 *
	 * @param x is square position x coord
	 * @param y is square pos y coord.
	 */
	public void setFocus(int x, int y) {
		this.focus = new Point(x, y);
		repaint();
	}

	public Point getFocus() {
		return this.focus;
	}

	public int getZoom() {
		return this.zoom;
	}

	public int getXFit() {
		return this.xFit;
	}

	public int getYFit() {
		return this.yFit;
	}

	@Override
	public boolean mouseDown(Event evt, int x, int y) {
		this.mouseStart = canvasToCave(x, y);
		this.dragStart = new Point(x, y);
		return (this.mouseStart == null) ? false : true;
	}

	@Override
	public boolean mouseDrag(Event evt, int x, int y) {
		if ((this.mouseStart != null) && this.drawSpace.inside(x, y)) {
			this.dragEnd = new Point(x, y);
			repaint();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseUp(Event evt, int x, int y) {
		boolean change;
		if (this.drawSpace.inside(x, y)) {
			Point targetPoint = canvasToCave(x, y);
			if ((this.mouseStart != null) && !targetPoint.equals(this.mouseStart)) {
				Rectangle target = new Rectangle(Math.min(this.mouseStart.x, targetPoint.x),
						Math.min(this.mouseStart.y, targetPoint.y),
						Math.max(targetPoint.x - this.mouseStart.x, this.mouseStart.x - targetPoint.x),
						Math.max(targetPoint.y - this.mouseStart.y, this.mouseStart.y - targetPoint.y));
				change = this.wcListener.handleMultiSquareEvent(target, evt) || true;
			} else {
				change = this.wcListener.handleSquareEvent(targetPoint, evt);
			}
			this.dragStart = null;
			this.dragEnd = null;
			this.mouseStart = null;
			if (change) {
				repaint();
				this.wcListener.update();
				return true;
			} else {
				return false;
			}
		} else if (this.zoomIn.inside(x, y)) {
			setZoom(this.zoom - 1);
			this.wcListener.update();
			return true;
		} else if (this.zoomUit.inside(x, y)) {
			setZoom(this.zoom + 1);
			this.wcListener.update();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Dimension minimumSize() {
		return new Dimension(100, 100);
	}

	@Override
	public Dimension preferredSize() {
		return minimumSize();
	}

	/**
	 * Check if a point is visible in the view right now.
	 *
	 * @param x is point x
	 * @param y is point y
	 * @return true if visible, false if not visible.
	 */
	public boolean isVisible(int x, int y) {
		// code ripped out of draw function.
		this.drawSpace = new Rectangle(20, 20, size().width - 40, size().height - 40);
		this.center = new Point((this.drawSpace.width) / 2 + this.drawSpace.x,
				(this.drawSpace.height) / 2 + this.drawSpace.y);
		if (this.scaleImageMode) {
			this.squareSize = Math.min(this.drawSpace.width, this.drawSpace.height) / (this.zoom + 1);
		} else {
			this.squareSize = 50;
		}
		this.xFit = this.drawSpace.width / this.squareSize + 3;
		this.yFit = this.drawSpace.height / this.squareSize + 3;

		int xmin = this.focus.x - (this.xFit - 1) / 2;
		int xmax = this.focus.x + (this.xFit + 1) / 2;
		int ymin = this.focus.y - (this.yFit - 1) / 2;
		int ymax = this.focus.y + (this.yFit + 1) / 2;

		return x > xmin && x < xmax && y > ymin && y < ymax;

	}

	private Point canvasToCave(int x, int y) {
		if (this.drawSpace.inside(x, y)) {
			int squarex = (x - this.center.x - this.squareSize / 2) / this.squareSize + this.focus.x;
			int squarey = -(y - this.center.y - this.squareSize / 2) / this.squareSize + this.focus.y;
			if (x > this.center.x + this.squareSize / 2) {
				squarex++;
			}
			if (y > this.center.y + this.squareSize / 2) {
				squarey--;
			}
			return new Point(squarex, squarey);
		} else {
			return null;
		}
	}
}