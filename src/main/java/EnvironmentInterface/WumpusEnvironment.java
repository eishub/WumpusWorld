package EnvironmentInterface;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eis.EIDefaultImpl;
import eis.PerceptUpdate;
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
import wumpusenv.WumpusApp;
import wumpusenv.WumpusWorld;
import wumpusenv.WumpusWorldPercept;

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

		static InitKey toKey(final String key) {
			try {
				return valueOf(key.toUpperCase());
			} catch (final Exception ex) {
				return UNKNOWN;
			}
		}
	}

	enum WumpusAction {
		FORWARD, GRAB, SHOOT, CLIMB, TURN, UNKNOWN;

		static WumpusAction toKey(final String act) {
			try {
				return valueOf(act.toUpperCase());
			} catch (final Exception ex) {
				return UNKNOWN;
			}
		}
	}

	enum WumpusQuery {
		REWARD, UNKNOWN;

		static WumpusQuery toKey(final String act) {
			try {
				return valueOf(act.toUpperCase());
			} catch (final Exception ex) {
				return UNKNOWN;
			}
		}
	}

	/**
	 * Main method to start Wumpus environment stand alone.
	 *
	 * @param args arguments
	 */
	public static void main(final String[] args) {
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
	private void executeAction(final String pAgent, final String pAct) throws NoEnvironmentException {
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
		return this.world.getApplication();
	}

	/**
	 * Register entity with EIS. There is a single controllable entity in this
	 * Wumpus world called 'agent'.
	 */
	public void registerEntity() {
		try {
			if (!this.entityRegistered) {
				this.addEntity(ENTITY);
				this.entityRegistered = true;
			}
		} catch (final EntityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Unregister entity with EIS.
	 */
	public void unregisterEntity() {
		try {
			deleteEntity(ENTITY);
			this.entityRegistered = false;
		} catch (final EntityException e) {
			e.printStackTrace();
		} catch (final RelationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Notify environment listeners of environment event.
	 *
	 * @param state environment event. see EIDefaultImpl. TODO Link @see not working
	 */
	public void notifyStateChange(final EnvironmentState state) {
		if (state != getState()) {
			try {
				setState(state);
			} catch (final ManagementException e) {
				// should not happen. Throw stack trace to screen.
				e.printStackTrace();
			}
		}
	}

	/**
	 * Override standard concept of state changes, too restricted.
	 */
	@Override
	public boolean isStateTransitionValid(final EnvironmentState oldState, final EnvironmentState newState) {
		return true;
	}

	/**************************************************************/
	/********** Implements EnvironmentInterface *******************/
	/**************************************************************/

	private final Map<String, List<Percept>> previousPercepts = new HashMap<>();

	@Override
	protected PerceptUpdate getPerceptsForEntity(final String entity) throws PerceiveException, NoEnvironmentException {
		final WumpusWorldPercept wumpusWorldPercept = getApplication().getRunner().getCurrentPercept();
		if (wumpusWorldPercept == null) {
			throw new NoEnvironmentException("environment is not available");
		}

		// EIS percepts.
		final List<Percept> percepts = new LinkedList<>();

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
		percepts.add(new Percept("time", new Numeral(getApplication().getRunner().getTime())));

		List<Percept> previous = this.previousPercepts.get(entity);
		if (previous == null) {
			previous = new ArrayList<>(0);
		}
		final List<Percept> addList = new ArrayList<>(percepts);
		addList.removeAll(previous);
		final List<Percept> delList = new ArrayList<>(previous);
		delList.removeAll(percepts);
		this.previousPercepts.put(entity, percepts);

		return new PerceptUpdate(addList, delList);
	}

	@Override
	public void kill() throws ManagementException {
		// If entity is still registered, unregister entity.
		if (this.entityRegistered) {
			unregisterEntity();
		}

		// Close GUI.
		if (getApplication() != null) { // Wumpus application not yet killed.
			this.world.close();
		}

		// Clean up.
		System.out.println("Handing wumpus world to garbage collector");
		this.world = null;
	}

	@Override
	public void pause() throws ManagementException {
		getApplication().getRunner().setPaused(true);
	}

	@Override
	public void start() throws ManagementException {
		if (!getApplication().isGuiVisible()) {
			getApplication().getRunner().setRealModel(getApplication().getEditor().getModel());
			notifyStateChange(EnvironmentState.RUNNING);
			return;
		}
		if (!getApplication().getRunner().isVisible()) {
			getApplication().enableRunner();
		} else {
			notifyStateChange(EnvironmentState.RUNNING);
		}
	}

	@Override
	public void init(final Map<String, Parameter> parameters) throws ManagementException {
		setState(EnvironmentState.INITIALIZING);
		reset(parameters);
	}

	@Override
	public void reset(final Map<String, Parameter> parameters) throws ManagementException {
		parseParameters(parameters);
		setState(EnvironmentState.PAUSED);
		// notify EIS interface, if present, that entity has been created
		WumpusWorld.getInstance().registerEntity();
	}

	private void parseParameters(final Map<String, Parameter> parameters) throws ManagementException {
		// GUI is enabled by default
		boolean guimode = true;
		String filename = null;

		for (final String key : parameters.keySet()) {
			final Parameter p = parameters.get(key);
			switch (InitKey.toKey(key)) {
			case FILE:
				if (!(p instanceof Identifier)) {
					throw new ManagementException("String expected as value for key " + InitKey.FILE + " but got " + p);
				}
				// load that map
				filename = ((Identifier) p).getValue();
				break;
			case GUI:
				if (p instanceof Identifier) {
					guimode = Boolean.parseBoolean(((Identifier) p).getValue());
				} else {
					throw new ManagementException(
							"Boolean 'true' or 'false' expected as value for key" + "'gui' but got " + p);
				}
				break;
			default:
				throw new ManagementException("Init key " + key + " unknown.");
			}
		}

		this.world = WumpusWorld.getInstance();
		// do not change order!
		this.world.setInterface(this);
		// set up needs the interface to register entity
		this.world.setUp(guimode);

		final URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
		try {
			final Path p = Paths.get(url.toURI());
			final File mapfile = p.getParent().resolve(filename).toFile();
			if (!mapfile.exists()) {
				System.out.println("Warning: wumpus environment can't open map " + mapfile);
			} else {
				this.world.getApplication().getEditor().loadFrom(mapfile);
			}
		} catch (final URISyntaxException e) {
			throw new ManagementException("failed to get path to " + url);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isSupportedByEntity(final Action action, final String arg1) {
		return WumpusAction.toKey(action.getName()) != WumpusAction.UNKNOWN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isSupportedByEnvironment(final Action arg0) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isSupportedByType(final Action arg0, final String arg1) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void performEntityAction(final Action action, final String entity) throws ActException {
		if (getState() != EnvironmentState.RUNNING) {
			throw new ActException("environment is not running");
		}
		try {
			switch (WumpusAction.toKey(action.getName())) {
			case CLIMB:
				executeAction(entity, "climb");
				break;
			case FORWARD:
				executeAction(entity, "forward");
				break;
			case GRAB:
				executeAction(entity, "grab");
				break;
			case SHOOT:
				executeAction(entity, "shoot");
				break;
			case TURN:
				if (action.getParameters().size() != 1) {
					throw new ActException(ActException.FAILURE,
							"turn requires exactly 1 parameter, but received " + action.getParameters());
				}
				final Parameter param0 = action.getParameters().get(0);
				if (!(param0 instanceof Identifier)) {
					throw new ActException(ActException.FAILURE,
							"turn takes Identifier as parameter but received " + param0);
				}
				final String direction = ((Identifier) param0).getValue();
				if (direction.equals("left")) {
					executeAction(entity, "turn(left)");
				} else if (direction.equals("right")) {
					executeAction(entity, "turn(right)");
				} else {
					throw new ActException(ActException.FAILURE,
							"turn takes only 'left' and 'right' as parameter, but received " + direction);
				}
				break;
			default: // UNKNOWN
				throw new ActException(ActException.FAILURE, "unknown action: " + action);
			}
		} catch (final NoEnvironmentException e) {
			throw new ActException(ActException.FAILURE, "Environment is not available");
		}
	}
}
