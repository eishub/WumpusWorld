package EnvironmentInterface;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Map;

import wumpusenv.WumpusApp;
import wumpusenv.WumpusWorld;
import wumpusenv.WumpusWorldPercept;
import eis.EIDefaultImpl;
import eis.exceptions.ActException;
import eis.exceptions.EntityException;
import eis.exceptions.ManagementException;
import eis.exceptions.NoEnvironmentException;
import eis.exceptions.PerceiveException;
import eis.exceptions.RelationException;
import eis.iilang.Action;
import eis.iilang.EnvironmentState;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;

/**
 * <p>
 * Provides an EIS environment interface for connecting the Wumpus environment
 * with GOAL.
 * </p>
 * <p>
 * Interface is said to be connected to Wumpus world if a Wumpus game is
 * running. Wumpus world need to be launched again if no WumpusApp is available.
 * </p>
 * 
 * @author KH
 */
public class WumpusEnvironment extends EIDefaultImpl {

	private static final long serialVersionUID = 773327222037240183L;

	private WumpusWorld world;
	/**
	 * Single CONTROLLABLE entity living in the Wumpus world.
	 */
	private static final String ENTITY = "caveExplorer";
	private boolean entityRegistered = false;

	enum InitKey {
		FILE, GUI, UNKNOWN;

		static InitKey toKey(String key) {
			try {
				return valueOf(key.toUpperCase());
			} catch (Exception ex) {
				return UNKNOWN;
			}
		}
	}

	enum WumpusAction {
		FORWARD, GRAB, SHOOT, CLIMB, TURN, UNKNOWN;

		static WumpusAction toKey(String act) {
			try {
				return valueOf(act.toUpperCase());
			} catch (Exception ex) {
				return UNKNOWN;
			}
		}

	}

	enum WumpusQuery {
		REWARD, UNKNOWN;

		static WumpusQuery toKey(String act) {
			try {
				return valueOf(act.toUpperCase());
			} catch (Exception ex) {
				return UNKNOWN;
			}
		}

	}

	/**
	 * Main method to start Wumpus environment stand alone.
	 * 
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {
		new WumpusEnvironment();
	}

	/**************************************************************/
	/******************** Suppport functions **********************/
	/**************************************************************/

	/**
	 * Each call to executeAction will increment the current time.
	 * 
	 * @throws NoEnvironmentException
	 */
	private void executeAction(String pAgent, String pAct)
			throws NoEnvironmentException {
		if (getApplication().getRunner().gameRunning()) {
			getApplication().getRunner().nextStep(pAct);
		} else {
			throw new NoEnvironmentException("Game is not running");
		}
	}

	/**
	 * Returns Wumpus application associated with Wumpus world interface.
	 * 
	 * @return Wumpus application object, may be null.
	 */
	private WumpusApp getApplication() {
		return world.getApplication();
	}

	/**
	 * Register entity with EIS. There is a single controllable entity in this
	 * Wumpus world called 'agent'.
	 */
	public void registerEntity() {
		try {
			if (!entityRegistered) {
				this.addEntity(ENTITY);
				entityRegistered = true;
			}
		} catch (EntityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Unregister entity with EIS.
	 */
	public void unregisterEntity() {
		try {
			deleteEntity(ENTITY);
			entityRegistered = false;
		} catch (EntityException e) {
			e.printStackTrace();
		} catch (RelationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Notify environment listeners of environment event.
	 * 
	 * @param state
	 *            environment event. see EIDefaultImpl. TODO Link @see not
	 *            working
	 */
	public void notifyStateChange(EnvironmentState state) {
		if (state != getState()) {
			try {
				setState(state);
			} catch (ManagementException e) {
				// should not happen. Throw stack trace to screen.
				e.printStackTrace();
			}
		}
	}

	/**
	 * Override standard concept of state changes, too restricted.
	 */
	@Override
	public boolean isStateTransitionValid(EnvironmentState oldState,
			EnvironmentState newState) {
		return true;
	}

	/**************************************************************/
	/********** Implements EnvironmentInterface *******************/
	/**************************************************************/

	@Override
	protected LinkedList<Percept> getAllPerceptsFromEntity(String arg0)
			throws PerceiveException, NoEnvironmentException {
		// EIS percepts.
		LinkedList<Percept> percepts = new LinkedList<Percept>();

		WumpusWorldPercept wumpusWorldPercept = getApplication().getRunner()
				.getCurrentPercept();

		if (wumpusWorldPercept == null) {
			throw new NoEnvironmentException("environment is not available");
		}

		// construct the EIS percepts from the Wumpus World percept
		if (wumpusWorldPercept.getBreeze()) {
			percepts.add(new Percept("breeze"));
		}
		if (wumpusWorldPercept.getStench()) {
			percepts.add(new Percept("stench"));
		}
		if (wumpusWorldPercept.getBump()) {
			percepts.add(new Percept("bump"));
		}
		if (wumpusWorldPercept.getScream()) {
			percepts.add(new Percept("scream"));
		}
		if (wumpusWorldPercept.getGlitter()) {
			percepts.add(new Percept("glitter"));
		}
		percepts.add(new Percept("time", new Numeral(getApplication()
				.getRunner().getTime())));

		return percepts;
	}

	@Override
	public void kill() throws ManagementException {

		// If entity is still registered, unregister entity.
		if (entityRegistered) {
			unregisterEntity();
		}

		// Close GUI.
		if (getApplication() != null) { // Wumpus application not yet killed.
			world.close();
		}

		// Clean up.
		System.out.println("Handing wumpus world to garbage collector");
		world = null;
	}

	@Override
	public void pause() throws ManagementException {
		getApplication().getRunner().setPaused(true);
	}

	@Override
	public void start() throws ManagementException {
		if (!getApplication().isGuiVisible()) {
			getApplication().getRunner().setRealModel(
					getApplication().getEditor().getModel());
			notifyStateChange(EnvironmentState.RUNNING);
			return;
		}
		if (!getApplication().getRunner().isVisible()) {
			// make runner visible
			getApplication().action(null, WumpusApp.RUNNER);
		} else {
			notifyStateChange(EnvironmentState.RUNNING);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(Map<String, Parameter> parameters)
			throws ManagementException {
		setState(EnvironmentState.INITIALIZING);
		parseParameters(parameters);
		setState(EnvironmentState.PAUSED);
	}

	/**
	 * {@inheritDoc}
	 */
	// @Override
	public void reset(Map<String, Parameter> parameters)
			throws ManagementException {

		parseParameters(parameters);
		setState(EnvironmentState.PAUSED);
	}

	/**
	 * DOC
	 * 
	 * @param parameters
	 * @throws ManagementException
	 */
	private void parseParameters(Map<String, Parameter> parameters)
			throws ManagementException {

		// GUI is enabled by default
		boolean guimode = true;
		String filename = null;

		for (String key : parameters.keySet()) {
			Parameter p = parameters.get(key);
			switch (InitKey.toKey(key)) {
			case FILE:
				if (!(p instanceof Identifier)) {
					throw new ManagementException(
							"String expected as value for key " + InitKey.FILE
									+ " but got " + p);
				}
				// load that map
				filename = ((Identifier) p).getValue();
				break;
			case GUI:
				if (p instanceof Identifier) {
					guimode = Boolean.parseBoolean(((Identifier) p).getValue());
				} else {
					throw new ManagementException(
							"Boolean 'true' or 'false' expected as value for key"
									+ "'gui' but got " + p);
				}
				break;
			default:
				throw new ManagementException("Init key " + key + " unknown.");
			}
		}

		world = WumpusWorld.getInstance();
		// do not change order!
		world.setInterface(this);
		// set up needs the interface to register entity
		world.setUp(guimode);
		URL url = getClass().getProtectionDomain().getCodeSource()
				.getLocation();
		Path p = Paths.get(url.getFile());
		File mapfile = p.getParent().resolve(filename).toFile();
		if (!mapfile.exists()) {
			System.out.println("Warning: wumpus environment can't open map "
					+ mapfile);
		} else {
			world.getApplication().getEditor().loadFrom(mapfile);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String requiredVersion() {
		return "0.3";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isSupportedByEntity(Action action, String arg1) {
		return WumpusAction.toKey(action.getName()) != WumpusAction.UNKNOWN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isSupportedByEnvironment(Action arg0) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isSupportedByType(Action arg0, String arg1) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Percept performEntityAction(String entity, Action action)
			throws ActException {
		try {
			switch (WumpusAction.toKey(action.getName())) {
			case CLIMB:
				executeAction(entity, "climb");
				return null;
			case FORWARD:
				executeAction(entity, "forward");
				return null;
			case GRAB:
				executeAction(entity, "grab");
				return null;
			case SHOOT:
				executeAction(entity, "shoot");
				return null;
			case TURN:
				if (action.getParameters().size() != 1) {
					throw new ActException(ActException.FAILURE,
							"turn requires exactly 1 parameter, but received "
									+ action.getParameters());
				}
				Parameter param0 = action.getParameters().get(0);
				if (!(param0 instanceof Identifier)) {
					throw new ActException(ActException.FAILURE,
							"turn takes Identifier as parameter but received "
									+ param0);
				}
				String direction = ((Identifier) param0).getValue();
				if (direction.equals("left")) {
					executeAction(entity, "turn(left)");
					return null;
				} else if (direction.equals("right")) {
					executeAction(entity, "turn(right)");
					return null;
				} else {
					throw new ActException(ActException.FAILURE,
							"turn takes only 'left' and 'right' as parameter, but received "
									+ direction);
				}
			default: // UNKNOWN
				throw new ActException(ActException.FAILURE, "unknown action: "
						+ action);
			}
		} catch (NoEnvironmentException e) {
			throw new ActException(ActException.FAILURE,
					"Environment is not available");
		}
	}

	/**
	 * Queries the interface of a certain property.
	 * 
	 * @param property
	 * @return
	 */
	/*
	 * public String queryProperty(String property) throws QueryException {
	 * switch (WumpusQuery.toKey(property)) { case REWARD: // return the reward
	 * from the last action return new
	 * Integer(getApplication().getRunner().getReward()).toString(); default: //
	 * UNKNOWN throw new QueryException("unknown query: " + property); } }
	 */
}
