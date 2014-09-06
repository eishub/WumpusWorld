package wumpusenv;

import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Scrollbar;

/**
 * CaveView is a window with zoom buttonos, scroll bars and a
 * {@link WumpusCanvas}.
 * 
 */
class CaveView extends Panel {

	private static final long serialVersionUID = 6308929665339569526L;

	public String id;
	private WumpusCanvas wumpusCanvas;
	private Scrollbar hbar;
	private Scrollbar vbar;
	private Listener caveListener;

	/**
	 * DOC
	 * 
	 * @param id
	 * @param caveListener
	 */
	public CaveView(String id, Listener caveListener) {
		super();
		this.id = id;
		this.caveListener = caveListener;
		wumpusCanvas = new WumpusCanvas(this);
		setLayout(new BorderLayout());
		hbar = new Scrollbar(Scrollbar.HORIZONTAL);
		vbar = new Scrollbar(Scrollbar.VERTICAL);
		add("Center", wumpusCanvas);
		add("South", hbar);
		add("East", vbar);
		doScrollSet();
	}

	public boolean handleSquareEvent(java.awt.Point square, java.awt.Event evt) {
		return caveListener.handleSquareEvent(square, evt);
	}

	public boolean handleMultiSquareEvent(java.awt.Rectangle squares,
			java.awt.Event evt) {
		return caveListener.handleMultiSquareEvent(squares, evt);
	}

	public WorldModel getModel() {
		return caveListener.getModel();
	}

	public int getZoom() {
		return wumpusCanvas.getZoom();
	}

	public void setZoom(int zoom) {
		if (zoom != wumpusCanvas.getZoom()) {
			wumpusCanvas.setZoom(zoom);
			doScrollSet();
		}
	}

	public void setScaleImagesMode(boolean b) {
		wumpusCanvas.setScaleImagesMode(b);
	}

	public void update() {
		doScrollSet();
		wumpusCanvas.repaint();
	}

	public Image getImage(String name) {
		return caveListener.getImage(name);
	}

	public void doScrollSet() {
		int bubbleX = Math.max(1, (wumpusCanvas.getXFit() - 1));
		int bubbleY = Math.max(1, (wumpusCanvas.getYFit() - 1));
		Rectangle bounds = caveListener.getModel(/* id */).getBounds();
		hbar.setPageIncrement(bubbleX);
		vbar.setPageIncrement(bubbleY);
		hbar.setValues(wumpusCanvas.getFocus().x, bubbleX, bounds.x,
				bounds.width + bounds.x + bubbleX - 1);
		vbar.setValues(-wumpusCanvas.getFocus().y, bubbleY,
				-(bounds.height + bounds.y) + 1, -bounds.y + bubbleY);
	}

	public boolean handleEvent(Event evt) {
		int zoom = wumpusCanvas.getZoom();
		if (evt.target instanceof Scrollbar) {
			wumpusCanvas.setFocus(hbar.getValue(), -vbar.getValue());
			return true;
		} else {
			return super.handleEvent(evt);
		}
	}

	/**
	 * place the agent back into the center. Added W.Pasman 23feb2011 trac #434
	 */
	public void recenter() {
		Point agentpos = caveListener.getModel().getAgentLocation();
		if (!wumpusCanvas.isVisible(agentpos.x, agentpos.y)) {
			wumpusCanvas.setFocus(agentpos.x, agentpos.y);
		}
	}
}
