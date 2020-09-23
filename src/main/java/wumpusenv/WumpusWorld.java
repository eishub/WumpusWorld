/**
 Made separate environment package W.Pasman 28oct08.
 compile with
	javac -classpath ../../bin wumpusenv/*.java

 Make jar with...

 jar cf wumpusenv.jar wumpusenv/*.class wumpusenv/images/*

 and move it to the appropriate position with eg

 mv wumpusenv.jar AI4010/

 to debug,
 add the path to env to the build path in the project properties
 and then modify the path that you give in the MAS file.
 */
package wumpusenv;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import EnvironmentInterface.WumpusEnvironment;
import eis.iilang.EnvironmentState;

/**
 * <p>
 * WumpusWorld is the main Applet that starts the Wumpus simulator and world
 * editor.
 * </p>
 * <p>
 * On macintosh, this application works stable on Java 1.4.2 and higher. Minimal
 * version details: "1.4.2_09". Java(TM) 2 Runtime Environment, Standard Edition
 * (build 1.4.2_09-233) Java HotSpot(TM) Client VM (build 1.4.2-56, mixed mode)
 * Under 1.4.1 it crashes very frequenty (Usually Bus errors) while loading the
 * world.
 * </p>
 * <p>
 * Remember to set the classpath right, e.g. setenv CLASSPATH
 * $CLASSPATH\:/wumpus
 * </p>
 *
 * <p>
 * see {link http://www-cse.uta.edu/~holder/courses/cse5361/wumpus.html}.
 * Extensive information is available on {link http
 * ://www.kr.tuwien.ac.at/students/prak_wumpusjava/simulator/Welcome.html} and
 * {link http://cl3512.inf.tu-dresden.de:8180/TomcatFlux/wumpus/}.
 * </p>
 *
 * TODO Links not working
 */
public class WumpusWorld extends Panel {
	private static final long serialVersionUID = 1L;

	private WumpusEnvironment wumpusInterface;
	private WumpusApp wumpusApplication = null;

	/**
	 * Main method to start Wumpus environment stand alone.
	 *
	 * @param args
	 */
	public static void main(final String[] args) {
		getInstance().setUp(true);
	}

	/**
	 * Creates a new Wumpus world object and creates a Wumpus application.
	 */
	private WumpusWorld() {
		System.out.println("Initializing the Wumpus World.");
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				WumpusWorld.this.wumpusApplication.setVisible(true);
			}
		});
	}

	/**
	 * WumpusWorldHolder is loaded on the first execution of
	 * WumpusWorld.getInstance() or the first access to WumpusWorldHolder.INSTANCE,
	 * not before.
	 */
	private static class WumpusWorldHolder {
		private static final WumpusWorld INSTANCE = new WumpusWorld();
	}

	/**
	 * Create or get a reference to the unique singleton Wumpus world.
	 *
	 * @return singleton Wumpus world object. see Threadsafe, see: {link http
	 *         ://en.wikipedia.org/wiki/Singleton_pattern
	 *         #The_solution_of_Bill_Pugh}. TODO Links not working
	 */
	public static WumpusWorld getInstance() {
		return WumpusWorldHolder.INSTANCE;
	}

	/**
	 * Sets EIS interface object.
	 */
	public void setInterface(final WumpusEnvironment wumpusInterface) {
		this.wumpusInterface = wumpusInterface;
	}

	/**
	 * Registers entity with EIS interface if such an interface is available.
	 */
	public void registerEntity() {
		if (this.wumpusInterface != null) {
			this.wumpusInterface.registerEntity();
		}
	}

	/**
	 * Unregister entity with EIS interface if such an interface is available.
	 */
	public void unregisterEntity() {
		if (this.wumpusInterface != null) {
			this.wumpusInterface.unregisterEntity();
		}
	}

	/**
	 * See {link WumpusEnvironment#notifyStateChange(EnvironmentEvent)}. This is
	 * using a hard observer pattern. TODO Link is broken.
	 *
	 * @param state is the new {@link EnvironmentState}.
	 *
	 */
	public void notifyStateChange(final EnvironmentState state) {
		if (this.wumpusInterface != null) {
			this.wumpusInterface.notifyStateChange(state);
		}
	}

	/**
	 * Returns Wumpus application.
	 *
	 * @return wumpus application.
	 */
	public WumpusApp getApplication() {
		return this.wumpusApplication;
	}

	/**
	 * Sets up a new Wumpus world.
	 *
	 * @param guimode
	 */
	public void setUp(final boolean guimode) {
		if (this.wumpusApplication != null) {
			this.wumpusApplication.closeWindows();
		}
		this.wumpusApplication = new WumpusApp(this, guimode);
	}

	@Override
	public void paint(final Graphics g) {
		g.setColor(Color.green.darker().darker());
		setBackground(Color.white);
		g.drawImage(this.wumpusApplication.getImage("wumpus.gif"), 0, 0, this);
		final int midden = getSize().width / 2;
		g.setFont(new Font("Serif", Font.BOLD, 14));
		final FontMetrics fm = g.getFontMetrics();
		g.drawString("Wumpus", midden - fm.stringWidth("Wumpus") / 2, 15);
		g.drawString("Applet", midden - fm.stringWidth("Applet") / 2, 30);
		g.drawString("Stub", midden - fm.stringWidth("Stub") / 2, 45);
		g.drawImage(this.wumpusApplication.getImage("agent.gif"), getSize().width - 50, 0, this);
	}

	/**
	 * Closes the Wumpus world. Sets the environment state to KILLED.
	 */
	public void close() {
		this.wumpusApplication.close();
		this.wumpusApplication = null;
		notifyStateChange(EnvironmentState.KILLED);
	}
}
