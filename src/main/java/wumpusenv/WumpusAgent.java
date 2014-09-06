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

	/**
	 * DOC
	 */
	public WumpusAgent() {
		// notify EIS interface, if present, that entity has been created
		WumpusWorld.getInstance().registerEntity();
	}

	/**
	 * The method action returns the action number that is associated with the
	 * action string pAction.
	 * 
	 * @param action 
	 */
	public int action(String action) {

		// Advance the time
		currentTime++;

		// We rely on the exact output of toString, since unpacking the term is
		// much more work and not worth it.
		if (action.equals("forward"))
			return TheGame.FORWARD;
		if (action.equals("grab"))
			return TheGame.GRAB;
		if (action.equals("shoot"))
			return TheGame.SHOOT;
		if (action.equals("climb"))
			return TheGame.CLIMB;
		if (action.equals("turn(left)"))
			return TheGame.TURN_LEFT;
		if (action.equals("turn(right)"))
			return TheGame.TURN_RIGHT;
		return TheGame.NO_ACTION;
	}

	/**
	 * Returns the current time according to the agent.
	 * 
	 * @return current time.
	 */
	public int getTime() {
		return currentTime;
	}
}
