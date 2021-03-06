package wumpusenv;

/**
 * Percept is an object being passed to the WumpusAgent, to tell about available
 * perceptions. In the wumpus world there are 5 basic percepts: breeze, stench,
 * bump, scream and glitter. The percept here is pretty simple: you either have
 * or not have a certian percept.
 */
public class WumpusWorldPercept {
	private boolean fBreeze, fStench, fBump, fScream, fGlitter;
	private int time;

	public WumpusWorldPercept() {
		reset();
	}

	public void reset() {
		this.fBreeze = this.fStench = this.fBump = this.fScream = this.fGlitter = false;
	}

	/**
	 * Sets current time, to add to percept.
	 *
	 * @param time current time, to be added to percept.
	 */
	public void setTime(final int time) {
		this.time = time;
	}

	public void setBreeze(final boolean pVal) {
		this.fBreeze = pVal;
	}

	public boolean getBreeze() {
		return this.fBreeze;
	}

	public void setStench(final boolean pVal) {
		this.fStench = pVal;
	}

	public boolean getStench() {
		return this.fStench;
	}

	public void setBump(final boolean pVal) {
		this.fBump = pVal;
	}

	public boolean getBump() {
		return this.fBump;
	}

	public void setScream(final boolean pVal) {
		this.fScream = pVal;
	}

	public boolean getScream() {
		return this.fScream;
	}

	public void setGlitter(final boolean pVal) {
		this.fGlitter = pVal;
	}

	public boolean getGlitter() {
		return this.fGlitter;
	}

	@Override
	public String toString() {
		return "percept([" + (getBreeze() ? "breeze" : "no breeze") + "," + (getStench() ? "stench" : "no stench") + ","
				+ (getBump() ? "bump" : "no bump") + "," + (getScream() ? "scream" : "no scream") + ","
				+ (getGlitter() ? "glitter" : "no glitter") + "]," + this.time + ")";
	}
}