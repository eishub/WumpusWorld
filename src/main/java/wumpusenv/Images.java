package wumpusenv;

import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Images {
	// Class fields
	private Map<String, Image> fImages = new HashMap<>();
	private static String fPath = "wumpusenv/images"; // Wouter: HUH, does not exist and yet the stuff works??

	// Class constructor
	public Images(WumpusApp pWA) {
		// Used in WumpusApp, WumpusWorld and WumpusCanvas
		java.net.URL u = getClass().getClassLoader().getResource(fPath + File.separator + "wumpus.gif");
		this.fImages.put("wumpus", pWA.getToolkit().getImage(u)); // TODO: Duplicate same code below...
		// Used in WumpusWorld
		this.fImages.put("agent", pWA.getToolkit().getImage(fPath + File.separator + "agent.gif"));
		// Used in WumpusCanvas
		this.fImages.put("agent0", pWA.getToolkit().getImage(fPath + File.separator + "agent0.gif"));
		this.fImages.put("agent90", pWA.getToolkit().getImage(fPath + File.separator + "agent90.gif"));
		this.fImages.put("agent180", pWA.getToolkit().getImage(fPath + File.separator + "agent180.gif"));
		this.fImages.put("agent270", pWA.getToolkit().getImage(fPath + File.separator + "agent270.gif"));
		this.fImages.put("wall", pWA.getToolkit().getImage(fPath + File.separator + "wal.gif"));
		this.fImages.put("gold", pWA.getToolkit().getImage(fPath + File.separator + "gold.gif"));
		this.fImages.put("agent", pWA.getToolkit().getImage(fPath + File.separator + "agent.gif"));
		this.fImages.put("ground", pWA.getToolkit().getImage(fPath + File.separator + "ground.gif"));
		this.fImages.put("breeze", pWA.getToolkit().getImage(fPath + File.separator + "breeze.gif"));
		this.fImages.put("smell", pWA.getToolkit().getImage(fPath + File.separator + "smell.gif"));
		// Used in Scheduler
		this.fImages.put("wumpusend", pWA.getToolkit().getImage(fPath + File.separator + "wumpusend.gif"));
		this.fImages.put("pitend", pWA.getToolkit().getImage(fPath + File.separator + "pitend.gif"));
		this.fImages.put("climbwuss", pWA.getToolkit().getImage(fPath + File.separator + "clumbwuss.gif"));
		this.fImages.put("climbgold", pWA.getToolkit().getImage(fPath + File.separator + "climbgold.gif"));
	}

	// Class methods
	public Image getImage(String pName) {
		return this.fImages.get(pName);
	}
}
