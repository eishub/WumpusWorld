package wumpusenv;

/**
 * Percept is an object being passed to the WumpusAgent, to tell about available perceptions. 
 * In the wumpus world there are 5 basic percepts: breeze, stench, bump, scream and glitter.
 * The percept here is pretty simple: you either have or not have a certian percept.
 */
public class WumpusWorldPercept {
	
	private boolean fBreeze, fStench, fBump, fScream, fGlitter;
	private int time;

	/**
	 * DOC
	 */
	public WumpusWorldPercept() {
	  reset();
	}
	
	/**
	 * DOC
	 */
	public void reset() {
		fBreeze = fStench = fBump = fScream = fGlitter = false;
	}

	/**
	 * Sets current time, to add to percept.
	 * 
	 * @param time
	 *            current time, to be added to percept.
	 */
	public void setTime(int time) {
		this.time = time;
	}
	
	public void setBreeze(boolean pVal) {
		fBreeze = pVal;
	}
	
	public boolean getBreeze() {
		return fBreeze;
	}
	
	public void setStench(boolean pVal) {
		fStench = pVal;
	}
	
	public boolean getStench() {
		return fStench;
	}

	public void setBump(boolean pVal) {
		fBump = pVal;
	}
	
	public boolean getBump() {
	    return fBump;
	}
	
	public void setScream(boolean pVal) {
		fScream = pVal;
	}
	
	public boolean getScream() {
		return fScream;
	}
	
	public void setGlitter(boolean pVal) {
		fGlitter = pVal;
	}
	
	public boolean getGlitter() {
		return fGlitter;
	}

	/**
	 * String version of Wumpus world percept to display in environment window.
	 */
	public String toString() {
		return "percept([" + 
			(getBreeze() ? "breeze" : "no breeze") + "," +
			(getStench() ? "stench" : "no stench") + "," +
			(getBump()   ? "bump"   : "no bump") + "," +
			(getScream() ? "scream" : "no scream") + "," +
			(getGlitter() ? "glitter" : "no glitter") + "]," + 
			time + ")";
	}
}