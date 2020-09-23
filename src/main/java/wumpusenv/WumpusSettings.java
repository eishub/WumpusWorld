package wumpusenv;

import java.util.prefs.Preferences;

/**
 * Static object to store the WumpusWorld preferences settings
 */
public class WumpusSettings {
	private enum Pref {
		width, height, x, y
	}

	static private Preferences prefs = Preferences.userNodeForPackage(WumpusSettings.class);

	/**
	 * get preferred width of the window.
	 *
	 * @return preferred width set by user, or 640 by default
	 */
	public static int getWidth() {
		return prefs.getInt(Pref.width.toString(), 640);
	}

	/**
	 * get preferred height of the window.
	 *
	 * @return preferred height set by user, or 480 by default
	 */

	public static int getHeight() {
		return prefs.getInt(Pref.height.toString(), 480);
	}

	/**
	 * get preferred x position of top left corner of the window.
	 *
	 * @return preferred x pos of top left corner set by user, or 450 by default
	 */
	public static int getX() {
		return prefs.getInt(Pref.x.toString(), 450);
	}

	/**
	 * get preferred y position of top left corner of the window.
	 *
	 * @return preferred y pos of top left corner set by user, or 0 by default
	 */
	public static int getY() {
		return prefs.getInt(Pref.y.toString(), 0);
	}

	/**
	 * save the window settings
	 *
	 * @param x      :x pos of top left corner
	 * @param y      :y pos of top left corner
	 * @param width  :width of the window
	 * @param height :height of the window
	 */
	public static void setWindowParams(final int x, final int y, final int width, final int height) {
		prefs.putInt(Pref.width.toString(), width);
		prefs.putInt(Pref.height.toString(), height);
		prefs.putInt(Pref.x.toString(), x);
		prefs.putInt(Pref.y.toString(), y);
	}
}