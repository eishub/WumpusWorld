package wumpusenv;

/**
 * WumpusAgent implements the Java part of the user's agent. But the WumpusAgent
 * class is the "body" of the user's agent, actually doing what the brain
 * decides to do.
 */
public class WumpusAgent {
	// first perceive is at t=0
	static int STARTTIME = 0;
	// Time according to the agent. Perception of time is 'subjective'.
	private int currentTime = STARTTIME;

	public WumpusAgent() {
	}

	/**
	 * The method action returns the action number that is associated with the
	 * action string pAction.
	 *
	 * @param action
	 */
	public int action(final String action) {
		// Advance the time
		this.currentTime++;

		// We rely on the exact output of toString, since unpacking the term is
		// much more work and not worth it.
		if (action.equals("forward")) {
			return TheGame.FORWARD;
		} else if (action.equals("grab")) {
			return TheGame.GRAB;
		} else if (action.equals("shoot")) {
			return TheGame.SHOOT;
		} else if (action.equals("climb")) {
			return TheGame.CLIMB;
		} else if (action.equals("turn(left)")) {
			return TheGame.TURN_LEFT;
		} else if (action.equals("turn(right)")) {
			return TheGame.TURN_RIGHT;
		} else {
			return TheGame.NO_ACTION;
		}
	}

	/**
	 * Returns the current time according to the agent.
	 *
	 * @return current time.
	 */
	public int getTime() {
		return this.currentTime;
	}
}
