package wumpusenv;

import java.awt.Image;

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
    public boolean handleSquareEvent(java.awt.Point pSquare, java.awt.Event pEvt);

	/**
	 * This method is called if user drags over an area on the CaveView.
	 */
    public boolean handleMultiSquareEvent(java.awt.Rectangle pSquares, java.awt.Event pEvt);

	/** 
	 * This method returns the model underlying the CaveView. Mainly used by the world editor.
	 */
    public WorldModel getModel();
    
	/**
	 * This method gets an image from a file with the name pName
	 */
    public Image getImage(String pName);
}