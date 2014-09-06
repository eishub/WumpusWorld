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
import java.awt.Polygon;
import java.awt.Rectangle;

/**
 * WumpusWorld is a grid of squares. This is a canvas with the wumpus world
 * painted on it. It needs a WumpusCanvasListener object that maintains the
 * Wumpus World model. The hierarchy is a bit messy because the WumpusCanvas has
 * a pointer back to the parent {@link CaveView} to handle listener calllbacks.
 */
class WumpusCanvas extends Canvas {

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
	private Image agentImg[], groundImg, wallImg, goldImg, wumpusImg,
			breezeImg, smellImg;
	private boolean scaleImageMode;
	private Polygon zoominpijltje, zoomuitpijltje;

	/**
	 * DOC
	 * 
	 * @param wcListener
	 */
	public WumpusCanvas(CaveView wcListener) {
		super();
		this.wcListener = wcListener;
		scaleImageMode = true;
		agentImg = new Image[4];
		agentImg[0] = getImage("agent0.gif");
		agentImg[1] = getImage("agent90.gif");
		agentImg[2] = getImage("agent180.gif");
		agentImg[3] = getImage("agent270.gif");
		wallImg = getImage("wall.gif");
		goldImg = getImage("gold.gif");
		wumpusImg = getImage("wumpus.gif");
		groundImg = getImage("ground.gif");
		breezeImg = getImage("breeze.gif");
		smellImg = getImage("smell.gif");
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
		mt.addImage(agentImg[0], 0);
		mt.addImage(agentImg[1], 1);
		mt.addImage(agentImg[2], 2);
		mt.addImage(agentImg[3], 3);
		mt.addImage(wallImg, 4);
		mt.addImage(groundImg, 5);
		mt.addImage(goldImg, 6);
		mt.addImage(wumpusImg, 7);
		mt.addImage(breezeImg, 8);
		mt.addImage(smellImg, 9);
		// mt.addImage(wallImg, 8);
		try {
			mt.waitForAll();
		} catch (Exception ex) {
			System.err.println(ex);
		}
		focus = new Point(1, 1);
		zoom = 5;
		drawSpace = new Rectangle(20, 20, size().width - 40, size().height - 40);
	}

	public void setScaleImagesMode(boolean b) {
		if (scaleImageMode != b) {
			scaleImageMode = b;
			offGraphics = null;
			repaint();
		}
	}

	public Image getImage(String name) {
		return wcListener.getImage(name);
	}

	public void paint(Graphics g) {
		update(g);
	}

	public void update(Graphics g) {
		worldModel = wcListener.getModel();
		// System.out.println("updating wumpuscanvas ="+worldModel.toString());
		drawSpace = new Rectangle(20, 20, size().width - 40, size().height - 40);
		center = new Point((drawSpace.width) / 2 + drawSpace.x,
				(drawSpace.height) / 2 + drawSpace.y);
		if (scaleImageMode) {
			squareSize = Math.min(drawSpace.width, drawSpace.height)
					/ (zoom + 1);
		} else {
			squareSize = 50;
		}
		xFit = drawSpace.width / squareSize + 3;
		yFit = drawSpace.height / squareSize + 3;

		if ((offGraphics == null) || (size().width != offDimension.width)
				|| (size().height != offDimension.height)) {
			offDimension = size();
			offImage = createImage(size().width, size().height);
			offGraphics = offImage.getGraphics();
		}
		// offGraphics.clipRect(0, 0, size().width, size().height);
		offGraphics.setFont(new Font("TimesRoman", Font.PLAIN, 12));
		offGraphics.setColor(Color.white);
		offGraphics.fillRect(0, 0, size().width, size().height);
		offGraphics.setColor(Color.black);
		offGraphics.drawRect(drawSpace.x, drawSpace.y, drawSpace.width,
				drawSpace.height);
		// offGraphics.clipRect(drawSpace.x, drawSpace.y, drawSpace.width,
		// drawSpace.height);
		drawGrid(offGraphics);
		for (int x = focus.x - (xFit - 1) / 2; x < focus.x + (xFit + 1) / 2; x++) {
			for (int y = focus.y - (yFit - 1) / 2; y < focus.y + (yFit + 1) / 2; y++) {
				if (worldModel.contains(new Point(x, y), WorldModel.GROUND)) {
					try {
						drawSquare(offGraphics, center.x - squareSize / 2
								+ (x - focus.x) * squareSize, center.y
								- squareSize / 2 - (y - focus.y) * squareSize,
								worldModel.getSquare(new Point(x, y)),
								worldModel.getAgentOrientation() % 360);
					} catch (Exception e) {
						new ErrorDetails(e, " on drawSquare type"
								+ worldModel.getSquare(new Point(x, y))
								+ "] orient: "
								+ worldModel.getAgentOrientation() % 360);
					}
				}
			}
		}
		if ((mouseStart != null) && (dragEnd != null)) {
			offGraphics.setColor(Color.blue);
			offGraphics.drawRect(Math.min(dragStart.x, dragEnd.x),
					Math.min(dragStart.y, dragEnd.y),
					Math.max(dragEnd.x - dragStart.x, dragStart.x - dragEnd.x),
					Math.max(dragEnd.y - dragStart.y, dragStart.y - dragEnd.y));
		}
		offGraphics.setColor(Color.white);
		offGraphics.fillRect(0, 0, size().width, 20);
		offGraphics.fillRect(0, 0, 20, size().height);
		offGraphics.fillRect(0, size().height - 19, size().width, 20);
		offGraphics.fillRect(size().width - 19, 0, 20, size().height);
		drawCoor(offGraphics);
		drawZoom(offGraphics);
		g.drawImage(offImage, 0, 0, this);
	}

	private void drawGrid(Graphics g) {
		int xStart = drawSpace.x + (center.x - squareSize / 2 - drawSpace.x)
				% squareSize;
		int yStart = drawSpace.y + (center.y - squareSize / 2 - drawSpace.y)
				% squareSize;
		g.setColor(Color.lightGray);
		for (int x = xStart; x < drawSpace.width + drawSpace.x; x += squareSize) {
			g.drawLine(x, drawSpace.y, x, drawSpace.y + drawSpace.height);
		}
		for (int y = yStart; y < drawSpace.height + drawSpace.y; y += squareSize) {
			g.drawLine(drawSpace.x, y, drawSpace.x + drawSpace.width, y);
		}
	}

	private void drawCoor(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		g.setColor(Color.black);
		for (int x = focus.x - (xFit - 1) / 2; x < focus.x + (xFit + 1) / 2; x++) {
			g.drawString(String.valueOf(x), center.x + (x - focus.x)
					* squareSize - fm.stringWidth(String.valueOf(x)) / 2,
					drawSpace.y + drawSpace.height + 5 + fm.getAscent());
		}
		for (int y = focus.y - (yFit - 1) / 2; y < focus.y + (yFit + 1) / 2; y++) {
			g.drawString(String.valueOf(y),
					drawSpace.x - 5 - fm.stringWidth(String.valueOf(y)),
					center.y - (y - focus.y) * squareSize - fm.getDescent());
		}
	}

	private void drawZoom(Graphics g) {
		int lineHeight = g.getFont().getSize();
		int xOff = size().width - 15;
		zoomUit = new Rectangle(xOff, 10, 10, 10);
		g.setColor(Color.blue);
		g.fillRect(xOff, 15, 10, 2);
		g.fillRect(xOff + 4, 11, 2, 10);
		g.setColor(Color.black);
		g.drawString(String.valueOf(zoom), size().width - 15, 22 + lineHeight);
		g.setColor(Color.red);
		zoomIn = new Rectangle(xOff, 35, 10, 10);
		g.fillRect(xOff, 40, 10, 2);
	}

	private void drawSquare(Graphics g, int x, int y, int data, int orientation)
			throws Exception {
		// System.out.println("drawSquare at "+x+","+y+". orientation="+orientation);
		if (groundImg == null)
			throw new Exception("ground image not available");
		if (smellImg == null)
			throw new Exception("smell image not available");
		if (breezeImg == null)
			throw new Exception("breeze image= not available");
		if (wallImg == null)
			throw new Exception("wall image not available");
		if (goldImg == null)
			throw new Exception("gold image not available");
		if (wumpusImg == null)
			throw new Exception("wumpus image not available");

		if ((data & worldModel.GROUND) == WorldModel.GROUND) {
			if (scaleImageMode)
				g.drawImage(groundImg, x, y, squareSize, squareSize, this);
			else
				g.drawImage(groundImg, x, y, this);
		}
		if ((data & WorldModel.PIT) == WorldModel.PIT) {
			g.setColor(Color.black);
			g.fillOval(x, y, squareSize, squareSize);
		}
		if ((data & worldModel.BREEZE) == WorldModel.BREEZE) {
			if (scaleImageMode)
				g.drawImage(breezeImg, x, y, squareSize, squareSize, this);
			else
				g.drawImage(breezeImg, x, y, this);
		}
		if ((data & worldModel.SMELL) == WorldModel.SMELL) {
			if (scaleImageMode)
				g.drawImage(smellImg, x, y, squareSize, squareSize, this);
			else
				g.drawImage(smellImg, x, y, this);
		}
		if ((data & WorldModel.WALL) == WorldModel.WALL) {
			/*
			 * g.setColor(Color.red.darker().darker()); g.fill3DRect(x, y,
			 * squareSize, squareSize, false);
			 */
			if (scaleImageMode)
				g.drawImage(wallImg, x, y, squareSize, squareSize, this);
			else
				g.drawImage(wallImg, x, y, this);
		}
		if ((data & WorldModel.GOLD) == WorldModel.GOLD) {
			if (scaleImageMode)
				g.drawImage(goldImg, x, y, squareSize, squareSize, this);
			else
				g.drawImage(goldImg, x, y, this);
		}
		if ((data & WorldModel.AGENT) == WorldModel.AGENT) {
			if (agentImg[orientation / 90] != null) {
				if (scaleImageMode)
					g.drawImage(agentImg[orientation / 90], x, y, squareSize,
							squareSize, this);
				else
					g.drawImage(agentImg[orientation / 90], x, y, this);
			}
		}
		if (((data & WorldModel.WUMPUS) == WorldModel.WUMPUS)
				&& worldModel.wumpusIsAlive()) {
			if (scaleImageMode)
				g.drawImage(wumpusImg, x, y, squareSize, squareSize, this);
			else
				g.drawImage(wumpusImg, x, y, this);
		}
		if ((data & worldModel.START) == WorldModel.START) {
			g.setColor(Color.red);
			g.drawRect(x, y, squareSize - 1, squareSize - 1);
		}
	}

	public void setZoom(int zoom) {
		if ((zoom != this.zoom) && (zoom > 0)) {
			this.zoom = zoom;
			offGraphics = null;
			repaint();
		}
	}

	/**
	 * set focus position
	 * 
	 * @param x
	 *            is square position x coord
	 * @param y
	 *            is square pos y coord.
	 */
	public void setFocus(int x, int y) {
		focus = new Point(x, y);
		repaint();
	}

	public Point getFocus() {
		return focus;
	}

	public int getZoom() {
		return zoom;
	}

	public int getXFit() {
		return xFit;
	}

	public int getYFit() {
		return yFit;
	}

	public boolean mouseDown(Event evt, int x, int y) {
		mouseStart = canvasToCave(x, y);
		dragStart = new Point(x, y);
		return (mouseStart == null) ? false : true;
	}

	public boolean mouseDrag(Event evt, int x, int y) {
		if ((mouseStart != null) && drawSpace.inside(x, y)) {
			dragEnd = new Point(x, y);
			repaint();
			return true;
		} else {
			return false;
		}
	}

	public boolean mouseUp(Event evt, int x, int y) {
		boolean change;
		if (drawSpace.inside(x, y)) {
			Point targetPoint = canvasToCave(x, y);
			if ((mouseStart != null) && !targetPoint.equals(mouseStart)) {
				Rectangle target = new Rectangle(Math.min(mouseStart.x,
						targetPoint.x), Math.min(mouseStart.y, targetPoint.y),
						Math.max(targetPoint.x - mouseStart.x, mouseStart.x
								- targetPoint.x), Math.max(targetPoint.y
								- mouseStart.y, mouseStart.y - targetPoint.y));
				change = wcListener.handleMultiSquareEvent(target, evt) || true;
			} else {
				change = wcListener.handleSquareEvent(targetPoint, evt);
			}
			dragStart = null;
			dragEnd = null;
			mouseStart = null;
			if (change) {
				repaint();
				wcListener.update();
				return true;
			} else {
				return false;
			}
		} else if (zoomIn.inside(x, y)) {
			setZoom(zoom - 1);
			wcListener.update();
			return true;
		} else if (zoomUit.inside(x, y)) {
			setZoom(zoom + 1);
			wcListener.update();
			return true;
		} else {
			return false;
		}
	}

	public Dimension minimumSize() {
		return new Dimension(100, 100);
	}

	public Dimension preferredSize() {
		return minimumSize();
	}

	/**
	 * Check if a point is visible in the view right now.
	 * 
	 * @param x
	 *            is point x
	 * @param y
	 *            is point y
	 * @return true if visible, false if not visible.
	 */
	public boolean isVisible(int x, int y) {
		// code ripped out of draw function.
		drawSpace = new Rectangle(20, 20, size().width - 40, size().height - 40);
		center = new Point((drawSpace.width) / 2 + drawSpace.x,
				(drawSpace.height) / 2 + drawSpace.y);
		if (scaleImageMode) {
			squareSize = Math.min(drawSpace.width, drawSpace.height)
					/ (zoom + 1);
		} else {
			squareSize = 50;
		}
		xFit = drawSpace.width / squareSize + 3;
		yFit = drawSpace.height / squareSize + 3;

		int xmin = focus.x - (xFit - 1) / 2;
		int xmax = focus.x + (xFit + 1) / 2;
		int ymin = focus.y - (yFit - 1) / 2;
		int ymax = focus.y + (yFit + 1) / 2;

		return x > xmin && x < xmax && y > ymin && y < ymax;

	}

	private Point canvasToCave(int x, int y) {
		if (drawSpace.inside(x, y)) {
			int squarex = (x - center.x - squareSize / 2) / squareSize
					+ focus.x;
			int squarey = -(y - center.y - squareSize / 2) / squareSize
					+ focus.y;
			if (x > center.x + squareSize / 2)
				squarex++;
			if (y > center.y + squareSize / 2)
				squarey--;
			return new Point(squarex, squarey);
		} else {
			return null;
		}
	}
}