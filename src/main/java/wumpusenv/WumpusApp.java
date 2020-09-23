package wumpusenv;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Properties;

import javax.swing.SwingUtilities;

import eis.iilang.EnvironmentState;

/**
 * WumpusApp is a combination of editor and runner for the agent in the Wumpus
 * World. It is called from the WumpusWorld class. It contains TWO PANELS: the
 * Runner and the WorldEditor.
 */
public class WumpusApp extends Frame {
	private static final long serialVersionUID = 8714859195624414371L;

	// World editor
	private WorldEditor worldEditor;
	private Menu worldEditorMenu;

	// Runner
	private Runner runner;

	// Graphics
	private static String fPath = "wumpusenv/images";
	private CardLayout cardLayout;
	private Panel mainPanel;
	private Properties preferences;
	private final Dialog errorDialog = new Dialog(this, "Error!", true);
	private final Label errorLabel = new Label("");

	/** observer, to be called when state change happens */
	WumpusWorld myObserver;

	private final boolean guiVisible;

	/**
	 * constructor. Takes the "parent" as observer, to call it back when a state
	 * change happens. State changes are directly coupled to the selected panel. If
	 * the panel is the World Editor, the mode is PAUSED, and when the panel is the
	 * Runner, the mode is RUNNING.
	 *
	 * @param showGui true if GUI should be shown, false if not.
	 */
	public WumpusApp(final WumpusWorld obs, final boolean showGui) {
		super("Wumpus environment editor and simulator");
		this.guiVisible = showGui;
		InitWumpusApp();
		this.myObserver = obs;
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(final WindowEvent e) {
				// clean up first
				WumpusWorld.getInstance().unregisterEntity();
				// close
				WumpusWorld.getInstance().close();
			}
		});
	}

	/**
	 * init the frame, set window position
	 */
	public void InitWumpusApp() {
		this.worldEditor = new WorldEditor(this);
		this.runner = new Runner(this);
		this.preferences = loadPrefs();

		if (isGuiVisible()) {
			setLayout(new BorderLayout());

			try {
				setIconImage(getToolkit().getImage(fPath + "/wumpus.gif"));
			} catch (final Exception e) {
				e.printStackTrace();
			}
			this.cardLayout = new CardLayout();

			this.mainPanel = new Panel();
			this.mainPanel.setLayout(this.cardLayout);
			final Panel buttonPanel = new Panel();
			buttonPanel.setLayout(new GridLayout(1, 3));
			final Button worldEditor = new Button("World Editor");
			this.mainPanel.add(worldEditor.getName(), this.worldEditor);
			worldEditor.addActionListener(e -> {
				enableWorldEditor();
			});
			final Button runner = new Button("Runner");
			this.mainPanel.add(runner.getName(), this.runner);
			runner.addActionListener(e -> {
				enableRunner();
			});
			buttonPanel.add(worldEditor);
			buttonPanel.add(runner);
			add(BorderLayout.CENTER, this.mainPanel);
			add(BorderLayout.SOUTH, buttonPanel);
			setMenuBar(setupMenuBar());

			setSize(WumpusSettings.getWidth(), WumpusSettings.getHeight());
			setLocation(WumpusSettings.getX(), WumpusSettings.getY());
			setVisible(true);
			addWindowsListeners();
		}
	}

	public void enableWorldEditor() {
		this.cardLayout.show(this.mainPanel, "World Editor");
		notifyObservers(EnvironmentState.PAUSED);
	}

	public void enableRunner() {
		this.cardLayout.show(this.mainPanel, "Runner");
		this.runner.setRealModel(this.worldEditor.getModel());
		if (!this.worldEditor.getModel().gameFinished()) {
			notifyObservers(EnvironmentState.RUNNING);
		}
	}

	private void addWindowsListeners() {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(final ComponentEvent e) {
				saveWindowSettings();
			}

			@Override
			public void componentResized(final ComponentEvent e) {
				saveWindowSettings();
			}
		});
	}

	public Runner getRunner() {
		return this.runner;
	}

	private MenuBar setupMenuBar() {
		this.worldEditorMenu = new Menu("World Editor");
		this.worldEditorMenu.add("Load world");
		this.worldEditorMenu.add("Save world");
		this.worldEditorMenu.addActionListener(e -> {
			final String obj = e.getActionCommand();
			if ("Load world".equals(obj)) {
				final FileDialog fd = new FileDialog(this, "Load a world", FileDialog.LOAD);
				final FilenameFilter fnf = new ExtensionFilter(".wld");
				fd.setFilenameFilter(fnf);
				fd.setDirectory(this.preferences.getProperty("homedir"));
				fd.setFile("*.wld");
				fd.setVisible(true);
				if (fd.getFile() != null) {
					this.worldEditor.loadFrom(new File(fd.getDirectory() + fd.getFile()));
				}
				this.cardLayout.show(this.mainPanel, "World Editor");
				System.out.println("New world loaded");
				this.runner.reset();
				System.out.println("Runner reset");
			} else if ("Save world".equals(obj)) {
				final FileDialog fd = new FileDialog(this, "Save a world", FileDialog.SAVE);
				final FilenameFilter fnf = new ExtensionFilter(".wld");
				fd.setFilenameFilter(fnf);
				fd.setDirectory(this.preferences.getProperty("homedir"));
				fd.setFile("*.wld");
				fd.setVisible(true);
				if (fd.getFile() != null) {
					this.worldEditor.saveTo(new File(fd.getDirectory() + fd.getFile()));
				}
			}
		});

		final MenuBar menuBar = new MenuBar();
		menuBar.add(this.worldEditorMenu);

		return menuBar;
	}

	private Properties loadPrefs() {
		return new Properties();
	}

	public Image getImage(final String pName) {
		try {
			final java.net.URL u = getClass().getClassLoader().getResource(fPath + "/" + pName);
			return getToolkit().getImage(u);
		} catch (final Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void reportError(final String msg) {
		this.errorLabel.setText(msg);
		this.errorDialog.pack();
		this.errorDialog.setLocation(getLocation().x + (getSize().width - this.errorDialog.getSize().width) / 2,
				getLocation().y + (getSize().height - this.errorDialog.getSize().height) / 2);
		this.errorDialog.setVisible(true);
	}

	public WorldEditor getEditor() {
		return this.worldEditor;
	}

	/**
	 * Notifies observers that the state of the Wumpus environment has changed.
	 */
	public void notifyObservers(final EnvironmentState state) {
		this.myObserver.notifyStateChange(state);
	}

	/**
	 * Closes the window.
	 */
	public void close() {
		if (isGuiVisible()) {
			saveWindowSettings();
		}
		closeWindows();
	}

	/**
	 * Close our window. Effect will be later as this uses
	 * SwingUtilities.invokeLater.
	 *
	 * @param app
	 */
	protected void closeWindows() {
		SwingUtilities.invokeLater(() -> {
			dispose();
			setVisible(false);
		});
	}

	private void saveWindowSettings() {
		WumpusSettings.setWindowParams(getX(), getY(), getWidth(), getHeight());
	}

	/**
	 * check if GUI should be visible.
	 *
	 * @return true if GUI is visible, false if GUI should not be rendered.
	 */
	public boolean isGuiVisible() {
		return this.guiVisible;
	}
}

class ExtensionFilter implements FilenameFilter {
	private final String extension;

	public ExtensionFilter(final String extension) {
		this.extension = extension;
	}

	@Override
	public boolean accept(final File dir, final String name) {
		return (name.indexOf(this.extension) != -1);
	}
}

class PrefDialog extends Dialog {
	private static final long serialVersionUID = 4093273691750133525L;

	protected TextField homeDir = new TextField();

	public PrefDialog(final Frame owner) {
		super(owner, "Preferences", true);
		setLayout(new BorderLayout());
		final Panel prefs = new Panel();
		prefs.setLayout(new FlowLayout());
		prefs.add(new Label("Home directory"));
		prefs.add(this.homeDir);
	}
}
