package wumpusenv;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

/**
 * Listener is an interface that defines communication from an application with
 * a CaveView or a WumpusCanvas. It updates the world model if triggered so (by
 * update), and it handles clicks in the CaveView.
 *
 * @see CaveView
 */
public interface Listener {
	/**
	 * This method is called if the user clicks on a square on a CaveView.
	 */
	boolean handleSquareEvent(Point pSquare, MouseEvent pEvt);

	/**
	 * This method is called if user drags over an area on the CaveView.
	 */
	boolean handleMultiSquareEvent(Rectangle pSquares, MouseEvent pEvt);

	/**
	 * This method returns the model underlying the CaveView. Mainly used by the
	 * world editor.
	 */
	WorldModel getModel();

	/**
	 * This method gets an image from a file with the name pName
	 */
	Image getImage(String pName);
}