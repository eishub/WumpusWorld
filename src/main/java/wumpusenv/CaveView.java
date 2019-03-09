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
		this.wumpusCanvas = new WumpusCanvas(this);
		setLayout(new BorderLayout());
		this.hbar = new Scrollbar(Scrollbar.HORIZONTAL);
		this.vbar = new Scrollbar(Scrollbar.VERTICAL);
		add("Center", this.wumpusCanvas);
		add("South", this.hbar);
		add("East", this.vbar);
		doScrollSet();
	}

	public boolean handleSquareEvent(java.awt.Point square, java.awt.Event evt) {
		return this.caveListener.handleSquareEvent(square, evt);
	}

	public boolean handleMultiSquareEvent(java.awt.Rectangle squares, java.awt.Event evt) {
		return this.caveListener.handleMultiSquareEvent(squares, evt);
	}

	public WorldModel getModel() {
		return this.caveListener.getModel();
	}

	public int getZoom() {
		return this.wumpusCanvas.getZoom();
	}

	public void setZoom(int zoom) {
		if (zoom != this.wumpusCanvas.getZoom()) {
			this.wumpusCanvas.setZoom(zoom);
			doScrollSet();
		}
	}

	public void setScaleImagesMode(boolean b) {
		this.wumpusCanvas.setScaleImagesMode(b);
	}

	public void update() {
		doScrollSet();
		this.wumpusCanvas.repaint();
	}

	public Image getImage(String name) {
		return this.caveListener.getImage(name);
	}

	public void doScrollSet() {
		int bubbleX = Math.max(1, (this.wumpusCanvas.getXFit() - 1));
		int bubbleY = Math.max(1, (this.wumpusCanvas.getYFit() - 1));
		Rectangle bounds = this.caveListener.getModel(/* id */).getBounds();
		this.hbar.setPageIncrement(bubbleX);
		this.vbar.setPageIncrement(bubbleY);
		this.hbar.setValues(this.wumpusCanvas.getFocus().x, bubbleX, bounds.x, bounds.width + bounds.x + bubbleX - 1);
		this.vbar.setValues(-this.wumpusCanvas.getFocus().y, bubbleY, -(bounds.height + bounds.y) + 1,
				-bounds.y + bubbleY);
	}

	@Override
	public boolean handleEvent(Event evt) {
		if (evt.target instanceof Scrollbar) {
			this.wumpusCanvas.setFocus(this.hbar.getValue(), -this.vbar.getValue());
			return true;
		} else {
			return super.handleEvent(evt);
		}
	}

	/**
	 * place the agent back into the center. Added W.Pasman 23feb2011 trac #434
	 */
	public void recenter() {
		Point agentpos = this.caveListener.getModel().getAgentLocation();
		if (!this.wumpusCanvas.isVisible(agentpos.x, agentpos.y)) {
			this.wumpusCanvas.setFocus(agentpos.x, agentpos.y);
		}
	}
}
