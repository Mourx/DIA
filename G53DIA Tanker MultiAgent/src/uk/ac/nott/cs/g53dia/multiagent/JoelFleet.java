package uk.ac.nott.cs.g53dia.multiagent;
import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.*;

public class JoelFleet extends Fleet {

    /** 
     * Number of tankers in the fleet (this is just an example, not a requirement).
     */
    private static int FLEET_SIZE = 3;
    
    public JoelFleet() {
    	this(new Random());
    }

	/**
	 * The DemoTanker implementation makes random moves. For reproducibility, it
	 * can share the same random number generator as the environment.
	 * 
	 * @param r
	 *            The random number generator.
	 */
    public JoelFleet(Random r) {
	// Create the tankers
	for (int i=0; i<FLEET_SIZE; i++) {
	    this.add(new EfficientTanker(r,this));
		}
    }
    public ArrayList<Task> AvailableTasks = new ArrayList<Task>();
	public ArrayList<Location> Locations = new ArrayList<Location>();
	public void ClaimTask(int ID) {
		Locations.get(ID).setTaken(true);
	}
	public void DropTask(int ID) {
		Locations.get(ID).setTaken(false);
	}
}
