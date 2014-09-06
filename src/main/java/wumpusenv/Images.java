package wumpusenv;

import java.awt.Image;
import java.io.File;
import java.util.HashMap;

public class Images {
	
	// Class fields
	HashMap<String,Image> fImages = new HashMap<String,Image>();
	private static String fPath = "wumpusenv/images"; // Wouter: HUH, does not exist and yet the stuff works??
	
	// Class constructor
	public Images(WumpusApp pWA) {
		// Used in WumpusApp, WumpusWorld and WumpusCanvas
		java.net.URL u = getClass().getClassLoader().getResource(fPath+ File.separator + "wumpus.gif");
		fImages.put("wumpus", pWA.getToolkit().getImage(u)); // TODO: Duplicate same code below... 
		// Used in WumpusWorld
		fImages.put("agent", pWA.getToolkit().getImage(fPath + File.separator + "agent.gif"));
		// Used in WumpusCanvas
		fImages.put("agent0", pWA.getToolkit().getImage(fPath + File.separator + "agent0.gif"));
		fImages.put("agent90", pWA.getToolkit().getImage(fPath + File.separator + "agent90.gif"));
		fImages.put("agent180", pWA.getToolkit().getImage(fPath + File.separator + "agent180.gif"));
		fImages.put("agent270", pWA.getToolkit().getImage(fPath + File.separator + "agent270.gif"));
		fImages.put("wall", pWA.getToolkit().getImage(fPath + File.separator + "wal.gif"));
		fImages.put("gold", pWA.getToolkit().getImage(fPath + File.separator + "gold.gif"));
		fImages.put("agent", pWA.getToolkit().getImage(fPath + File.separator + "agent.gif"));
		fImages.put("ground", pWA.getToolkit().getImage(fPath + File.separator + "ground.gif"));
		fImages.put("breeze", pWA.getToolkit().getImage(fPath + File.separator + "breeze.gif"));
		fImages.put("smell", pWA.getToolkit().getImage(fPath + File.separator + "smell.gif"));
		// Used in Scheduler
		fImages.put("wumpusend", pWA.getToolkit().getImage(fPath + File.separator + "wumpusend.gif"));
		fImages.put("pitend", pWA.getToolkit().getImage(fPath + File.separator + "pitend.gif"));
		fImages.put("climbwuss", pWA.getToolkit().getImage(fPath + File.separator + "clumbwuss.gif"));
		fImages.put("climbgold", pWA.getToolkit().getImage(fPath + File.separator + "climbgold.gif"));
	}
	
	// Class methods
	public Image getImage(String pName) {
		return fImages.get(pName);
	}

}
