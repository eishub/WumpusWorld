package wumpusenv;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.CardLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

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
	private final WumpusApp owner;

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

	private Label actionLabel;

	private final Label perceptLabel = new Label("percept([null,null,null,null,null], 0)");
	private final Label scoreLabel = new Label("Score: XXXX");
	private final Label timeLabel = new Label("Time: 0");
	private final Label AgentLabel = new Label("Agent:");
	private final Label hasArrowLabel = new Label(HASARROW);
	private final Label hasGoldLabel = new Label(HASNOGOLD);

	// Wumpus Game Parameters
	private final TheGame game = new TheGame();
	private final WumpusAgent agent;
	// MASTER CLOCK
	private int time = 0;

	/**
	 * Records running or paused state. In paused state no actions can be done, and
	 * no percepts are provided.
	 */
	private boolean paused = false;

	public Runner(final WumpusApp owner) {
		super();
		this.owner = owner;
		this.realModel = new WorldModel();
		this.agent = new WumpusAgent();

		if (owner.isGuiVisible()) {
			setLayout(new BorderLayout());
			this.endView = new EndView(this);
			this.viewport = setupViews();
			this.controls = setupGameStatePanel();
			add(BorderLayout.CENTER, this.viewport);
			add(BorderLayout.EAST, this.controls);
			add(BorderLayout.SOUTH, this.perceptLabel);
			showView(REALVIEW);
			setSize(500, 400);
			setVisible(true);
		}
	}

	/**
	 * Returns the Wumpus entity.
	 *
	 * @return Wumpus agent (controllable entity).
	 */
	public WumpusAgent getAgent() {
		return this.agent;
	}

	public int getTime() {
		return this.time;
	}

	public WumpusWorldPercept getCurrentPercept() {
		return this.game.getPercept(this.realModel);
	}

	/**
	 * Returns true if the game is running, i.e. the runner view is visible and the
	 * game has not yet finished (agent climbed out or died), otherwise false.
	 *
	 * @return {@code true} if the game is running; {@code false} otherwise.
	 */
	public boolean gameRunning() {
		return !this.realModel.gameFinished() && !this.paused;
	}

	/**
	 * Puts Wumpus game runner into paused or non-paused (started) mode. Note that
	 * this does not change the viewer from EDITOR to RUNNER.
	 *
	 * @param value mode to put runner in: true is paused, false is started.
	 */
	public void setPaused(final boolean value) {
		this.paused = value;
		if (this.paused) {
			WumpusWorld.getInstance().notifyStateChange(EnvironmentState.PAUSED);
		} else {
			WumpusWorld.getInstance().notifyStateChange(EnvironmentState.RUNNING);
		}
	}

	/**
	 * Resets the runner view and Wumpus game (score, time, initial state).
	 */
	public void reset() {
		this.game.reset();
		this.realModel.reset();
		this.time = 0;
		this.paused = false;
		this.timeLabel.setText("Time: 0");
		this.scoreLabel.setText("Score: 0");
		this.perceptLabel.setText("percept([null,null,null,null,null], 0)");
		this.actionLabel.setText("Action:");
		showView(REALVIEW);
		// notify environment listeners. HACK see #1539
		WumpusWorld.getInstance().notifyStateChange(EnvironmentState.PAUSED);
	}

	public void setRealModel(final WorldModel real) {
		this.realModel = real;
		if (this.owner.isGuiVisible()) {
			this.realViewer.recenter();
			this.realViewer.update();
		}
	}

	/**
	 * nextStep does next perception-action cycle step. We want perception-action
	 * cycle to halt between percept and action. therefore we pre-initialized the
	 * agent with the perception, and wait till nextstep button is pressed in our
	 * interface before calling the agent's actin. After doing the action, we
	 * pre-initialize the next percept.
	 *
	 * We allow actions to be taken even when the game is not visible. This is
	 * because the GOAL system may assume that the action can be done just because
	 * it just succeeded in the check whether the game is visible. However the
	 * visibility might have changed in between.
	 */
	public void nextStep(final String pAction) {

		// First, check whether game has finished already.
		if (this.realModel.gameFinished()) {
			return;
		}

		this.time++;

		// Attempt to execute action pAction.
		final int lActionNr = this.agent.action(pAction);
		this.game.Action(lActionNr, this.realModel);

		// Check whether WE'RE FINISHED
		if (this.realModel.gameFinished()) {
			this.owner.notifyObservers(EnvironmentState.INITIALIZING);
			WumpusWorld.getInstance().unregisterEntity();
		}

		if (!this.owner.isGuiVisible()) {
			return;
		}

		// update state panel
		this.timeLabel.setText("Time:" + this.time);
		this.hasArrowLabel.setText(this.realModel.agentHasArrow() ? HASARROW : HASNOARROW);
		this.hasGoldLabel.setText(this.realModel.agentHasGold() ? HASGOLD : HASNOGOLD);

		this.actionLabel.setText("Action: " + pAction);
		this.scoreLabel.setText("Score: " + this.game.getScore());

		// Check whether game has finished, and, if so, show corresponding end
		// view
		if (this.realModel.gameFinished()) {
			if (this.realModel.getAgentLocation().equals(this.realModel.getWumpusLocation())) {
				this.endView.setState(EndView.WUMPUS);
			} else if (this.realModel.contains(this.realModel.getAgentLocation(), WorldModel.PIT)) {
				this.endView.setState(EndView.PIT);
			} else if (this.realModel.agentHasGold()) {
				this.endView.setState(EndView.RICH);
			} else {
				this.endView.setState(EndView.WUSS);
			}
			showView(ENDVIEW);
		} else { // if not, show the updated perceptual info. perceptLabel is
					// for the info window to inform user.
			getCurrentPercept().setTime(this.time);
			this.perceptLabel.setText("" + getCurrentPercept());
		}
		updateViews();
	}

	// ************************ VIEW *****************************
	public void setScaleImagesMode(final boolean b) {
		this.realViewer.setScaleImagesMode(b);
	}

	private Panel setupViews() {
		this.realViewer = new CaveView(REALVIEW, this);
		this.realViewer.setZoom(7);
		this.viewSelector = new CardLayout();

		// viewport is used to toggle between cave view and end view.
		final Panel viewport = new Panel();
		viewport.setLayout(this.viewSelector);
		viewport.add(REALVIEW, this.realViewer);
		viewport.add(ENDVIEW, this.endView);
		return viewport;
	}

	private Panel setupGameStatePanel() {
		final Panel gameState = new Panel();
		gameState.setLayout(new GridLayout(8, 1));
		this.actionLabel = new Label("Action: X");
		gameState.add(this.actionLabel);
		gameState.add(this.scoreLabel);
		gameState.add(this.timeLabel);
		gameState.add(this.AgentLabel);
		gameState.add(this.hasArrowLabel);
		gameState.add(this.hasGoldLabel);
		return gameState;
	}

	private void updateViews() {
		this.realViewer.update();
		this.controls.doLayout();
	}

	private void showView(final String view) {
		this.viewSelector.show(this.viewport, view); // selects realview or endview.
	}

	@Override
	public Image getImage(final String name) {
		return this.owner.getImage(name);
	}

	@Override
	public boolean handleSquareEvent(final Point square, final MouseEvent evt) {
		return false;
	}

	@Override
	public boolean handleMultiSquareEvent(final Rectangle sqaures, final MouseEvent evt) {
		return false;
	}

	@Override
	public WorldModel getModel() {
		return this.realModel;
	}

	public int getGameScore() {
		return this.game.getScore();
	}

	public int getReward() {
		return this.game.getReward();
	}
}

class EndView extends Canvas {
	private static final long serialVersionUID = 1L;
	public static final int WUMPUS = 0;
	public static final int PIT = 1;
	public static final int WUSS = 2;
	public static final int RICH = 3;
	private int state = -1;
	private final Image[] img = new Image[4];

	public void setState(final int state) {
		this.state = state;
	}

	public EndView(final Runner parent) {
		this.img[0] = parent.getImage("wumpusend.jpg");
		this.img[1] = parent.getImage("pitend.jpg");
		this.img[2] = parent.getImage("climbwuss.jpg");
		this.img[3] = parent.getImage("climbgold.jpg");
		final MediaTracker mt = new MediaTracker(this);
		mt.addImage(this.img[0], 0);
		mt.addImage(this.img[1], 1);
		mt.addImage(this.img[2], 2);
		mt.addImage(this.img[3], 3);
		try {
			mt.waitForAll();
		} catch (final Exception ex) {
			System.err.println(ex);
		}

	}

	@Override
	public void paint(final Graphics g) {
		if ((this.state >= 0) && (this.state <= 3)) {
			g.drawImage(this.img[this.state], 0, 0, this);
		}
	}
}
