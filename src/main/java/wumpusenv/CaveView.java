package wumpusenv;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.event.MouseEvent;

/**
 * CaveView is a window with zoom buttonos, scroll bars and a
 * {@link WumpusCanvas}.
 */
class CaveView extends Panel {
	private static final long serialVersionUID = 6308929665339569526L;

	public String id;
	private final WumpusCanvas wumpusCanvas;
	private final Scrollbar hbar;
	private final Scrollbar vbar;
	private final Listener caveListener;

	public CaveView(final String id, final Listener caveListener) {
		super();
		this.id = id;
		this.caveListener = caveListener;
		this.wumpusCanvas = new WumpusCanvas(this);
		setLayout(new BorderLayout());
		this.hbar = new Scrollbar(Scrollbar.HORIZONTAL);
		this.vbar = new Scrollbar(Scrollbar.VERTICAL);
		add(BorderLayout.CENTER, this.wumpusCanvas);
		add(BorderLayout.SOUTH, this.hbar);
		add(BorderLayout.EAST, this.vbar);
		doScrollSet();
	}

	public boolean handleSquareEvent(final Point square, final MouseEvent evt) {
		return this.caveListener.handleSquareEvent(square, evt);
	}

	public boolean handleMultiSquareEvent(final Rectangle squares, final MouseEvent evt) {
		return this.caveListener.handleMultiSquareEvent(squares, evt);
	}

	public WorldModel getModel() {
		return this.caveListener.getModel();
	}

	public int getZoom() {
		return this.wumpusCanvas.getZoom();
	}

	public void setZoom(final int zoom) {
		if (zoom != this.wumpusCanvas.getZoom()) {
			this.wumpusCanvas.setZoom(zoom);
			doScrollSet();
		}
	}

	public void setScaleImagesMode(final boolean b) {
		this.wumpusCanvas.setScaleImagesMode(b);
	}

	public void update() {
		doScrollSet();
		this.wumpusCanvas.repaint();
	}

	public Image getImage(final String name) {
		return this.caveListener.getImage(name);
	}

	public void doScrollSet() {
		final int bubbleX = Math.max(1, (this.wumpusCanvas.getXFit() - 1));
		final int bubbleY = Math.max(1, (this.wumpusCanvas.getYFit() - 1));
		final Rectangle bounds = this.caveListener.getModel(/* id */).getBounds();
		this.hbar.setBlockIncrement(bubbleX);
		this.vbar.setBlockIncrement(bubbleY);
		this.hbar.setValues(this.wumpusCanvas.getFocus().x, bubbleX, bounds.x, bounds.width + bounds.x + bubbleX - 1);
		this.vbar.setValues(-this.wumpusCanvas.getFocus().y, bubbleY, -(bounds.height + bounds.y) + 1,
				-bounds.y + bubbleY);
	}

	@Override
	public void processEvent(final AWTEvent evt) {
		if (evt.getSource() instanceof Scrollbar) {
			this.wumpusCanvas.setFocus(this.hbar.getValue(), -this.vbar.getValue());
		} else {
			super.processEvent(evt);
		}
	}

	/**
	 * place the agent back into the center. Added W.Pasman 23feb2011 trac #434
	 */
	public void recenter() {
		final Point agentpos = this.caveListener.getModel().getAgentLocation();
		if (!this.wumpusCanvas.isVisible(agentpos.x, agentpos.y)) {
			this.wumpusCanvas.setFocus(agentpos.x, agentpos.y);
		}
	}
}
