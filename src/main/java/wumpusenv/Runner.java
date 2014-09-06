package wumpusenv;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.CardLayout;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.Panel;
import java.awt.Point;

import eis.iilang.EnvironmentState;

/**
 * Runner deals with control of running the Agent. It maintains the current
 * state of the board, called world model (see WorldModel), and asks the game
 * rule engine to evaluate actions and calculate percepts (see TheGame). The
 * percepts are sent to the Agent (see WumpusAgent), and then the Agent is asked
 * for its next action. The Runner provides a map of the world, showing the
 * position of all the items in the world. The Runner can do a single step, or
 * do automatic stepping (2 steps per second).
 * 
 * @see WorldModel
 * @see WumpusAgent
 * @see TheGame
 */

public class Runner extends Panel implements Listener {

	private static final long serialVersionUID = -6170967833446044717L;

	// owner
	private WumpusApp owner;

	// view
	public static final String REALVIEW = "Real world view";
	public static final String ENDVIEW = "End of game view";

	private static final String HASARROW = "carries arrow";
	private static final String HASNOARROW = "no arrow";
	private static final String HASGOLD = "has gold";
	private static final String HASNOGOLD = "no gold";

	private CardLayout viewSelector;
	private Panel viewport, controls;
	private CaveView realViewer;
	private WorldModel realModel;
	private EndView endView;

	private Label actionLabel, perceptLabel = new Label(
			"percept([null,null,null,null,null], 0)");
	private Label scoreLabel = new Label("Score: XXXX");
	private Label timeLabel = new Label("Time: 0");
	private Label AgentLabel = new Label("Agent:");
	private Label hasArrowLabel = new Label(HASARROW);
	private Label hasGoldLabel = new Label(HASNOGOLD);

	// Wumpus Game Parameters
	private TheGame game = new TheGame();
	private WumpusAgent agent;
	// MASTER CLOCK
	private int time = 0;

	/**
	 * Records running or paused state. In paused state no actions can be done,
	 * and no percepts are provided.
	 */
	private boolean paused = false;

	/**
	 * DOC
	 * 
	 * @param owner
	 */
	public Runner(WumpusApp owner) {
		super();
		this.owner = owner;
		realModel = new WorldModel();
		agent = new WumpusAgent();

		if(owner.isGuiVisible()) { 
			setLayout(new BorderLayout());
			endView = new EndView(this);
			viewport = setupViews();
			controls = setupGameStatePanel();
			add("Center", viewport);
			add("East", controls);
			add("South", perceptLabel);
			showView(REALVIEW);
			resize(500, 400);
			show();
		}
	}

	/**
	 * Returns the Wumpus entity.
	 * 
	 * @return Wumpus agent (controllable entity).
	 */
	public WumpusAgent getAgent() {
		return agent;
	}

	public int getTime() {
		return time;
	}

	public WumpusWorldPercept getCurrentPercept() {
		return game.getPercept(realModel);
	}

	/**
	 * Returns true if the game is running, i.e. the runner view is visible and
	 * the game has not yet finished (agent climbed out or died), otherwise
	 * false.
	 * 
	 * @return {@code true} if the game is running; {@code false} otherwise.
	 */
	public boolean gameRunning() {
		return !realModel.gameFinished() && !paused;
	}

	/**
	 * Puts Wumpus game runner into paused or non-paused (started) mode. Note
	 * that this does not change the viewer from EDITOR to RUNNER.
	 * 
	 * @param value
	 *            mode to put runner in: true is paused, false is started.
	 */
	public void setPaused(boolean value) {
		paused = value;
		if (paused) {
			WumpusWorld.getInstance()
					.notifyStateChange(EnvironmentState.PAUSED);
		} else {
			WumpusWorld.getInstance().notifyStateChange(
					EnvironmentState.RUNNING);
		}
	}

	/**
	 * Resets the runner view and Wumpus game (score, time, initial state).
	 */
	public void reset() {
		game.reset();
		realModel.reset();
		time = 0;
		paused = false;
		timeLabel.setText("Time: 0");
		scoreLabel.setText("Score: 0");
		perceptLabel.setText("percept([null,null,null,null,null], 0)");
		actionLabel.setText("Action:");
		showView(REALVIEW);
		// notify environment listeners. HACK see #1539
		WumpusWorld.getInstance().notifyStateChange(EnvironmentState.PAUSED);
	}

	public void setRealModel(WorldModel real) {
		realModel = real;
		if (owner.isGuiVisible()) {
			realViewer.recenter();
			realViewer.update();
		}
	}

	/**
	 * nextStep does next perception-action cycle step. We want
	 * perception-action cycle to halt between percept and action. therefore we
	 * pre-initialized the agent with the perception, and wait till nextstep
	 * button is pressed in our interface before calling the agent's actin.
	 * After doing the action, we pre-initialize the next percept.
	 * 
	 * We allow actions to be taken even when the game is not visible. This is
	 * because the GOAL system may assume that the action can be done just
	 * because it just succeeded in the check whether the game is visible.
	 * However the visibility might have changed in between.
	 */
	public void nextStep(String pAction) {

		// First, check whether game has finished already.
		if (realModel.gameFinished()) {
			return;
		}

		time++;

		// Attempt to execute action pAction.
		int lActionNr = agent.action(pAction);
		game.Action(lActionNr, realModel);
		
		// Check whether WE'RE FINISHED
		if (realModel.gameFinished()) {
			owner.notifyObservers(EnvironmentState.INITIALIZING);
			WumpusWorld.getInstance().unregisterEntity();
		}

		if (!owner.isGuiVisible()) {
			return;
		}

		// update state panel
		timeLabel.setText("Time:" + time);
		hasArrowLabel
				.setText(realModel.agentHasArrow() ? HASARROW : HASNOARROW);
		hasGoldLabel.setText(realModel.agentHasGold() ? HASGOLD : HASNOGOLD);

		actionLabel.setText("Action: " + pAction);
		scoreLabel.setText("Score: " + game.getScore());

		// Check whether game has finished, and, if so, show corresponding end
		// view
		if (realModel.gameFinished()) {
			if (realModel.getAgentLocation().equals(
					realModel.getWumpusLocation()))
				endView.setState(EndView.WUMPUS);
			else if (realModel.contains(realModel.getAgentLocation(),
					WorldModel.PIT))
				endView.setState(EndView.PIT);
			else if (realModel.agentHasGold())
				endView.setState(EndView.RICH);
			else
				endView.setState(EndView.WUSS);
			showView(ENDVIEW);
		} else { // if not, show the updated perceptual info. perceptLabel is
					// for the info window to inform user.
			getCurrentPercept().setTime(time);
			perceptLabel.setText("" + getCurrentPercept());
		}
		updateViews();
	}

	// ************************ VIEW *****************************
	public void setScaleImagesMode(boolean b) {
		realViewer.setScaleImagesMode(b);
	}

	private Panel setupViews() {
		realViewer = new CaveView(REALVIEW, this);
		realViewer.setZoom(7);
		viewSelector = new CardLayout();

		// viewport is used to toggle between cave view and end view.
		Panel viewport = new Panel();
		viewport.setLayout(viewSelector);
		viewport.add(REALVIEW, realViewer);
		viewport.add(ENDVIEW, endView);
		return viewport;
	}

	/**
	 * DOC
	 * 
	 * @return
	 */
	private Panel setupGameStatePanel() {
		Panel gameState = new Panel();
		gameState.setLayout(new GridLayout(8, 1));
		actionLabel = new Label("Action: X");
		gameState.add(actionLabel);
		gameState.add(scoreLabel);
		gameState.add(timeLabel);
		gameState.add(AgentLabel);
		gameState.add(hasArrowLabel);
		gameState.add(hasGoldLabel);
		return gameState;
	}

	private void updateViews() {
		realViewer.update();
		controls.doLayout();
	}

	private void showView(String view) {
		viewSelector.show(viewport, view); // selects realview or endview.
	}

	public Image getImage(String name) {
		return owner.getImage(name);
	}

	public boolean handleSquareEvent(Point square, Event evt) {
		return false;
	}

	public boolean handleMultiSquareEvent(java.awt.Rectangle sqaures,
			java.awt.Event evt) {
		return false;
	}

	public WorldModel getModel() {
		return realModel;
	}
	
	public int getGameScore() {
		return game.getScore();
	}

	public int getReward() {
		return game.getReward();
	}
}

class EndView extends Canvas {
	public static final int WUMPUS = 0;
	public static final int PIT = 1;
	public static final int WUSS = 2;
	public static final int RICH = 3;
	private int state = -1;
	private Image[] img = new Image[4];

	public void setState(int state) {
		this.state = state;
	}

	public EndView(Runner parent) {
		img[0] = parent.getImage("wumpusend.jpg");
		img[1] = parent.getImage("pitend.jpg");
		img[2] = parent.getImage("climbwuss.jpg");
		img[3] = parent.getImage("climbgold.jpg");
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(img[0], 0);
		mt.addImage(img[1], 1);
		mt.addImage(img[2], 2);
		mt.addImage(img[3], 3);
		try {
			mt.waitForAll();
		} catch (Exception ex) {
			System.err.println(ex);
		}

	}

	public void paint(Graphics g) {
		if ((state >= 0) && (state <= 3)) {
			g.drawImage(img[state], 0, 0, this);
		}
	}
}
