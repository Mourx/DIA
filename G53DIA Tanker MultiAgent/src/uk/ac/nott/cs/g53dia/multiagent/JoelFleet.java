package uk.ac.nott.cs.g53dia.multiagent;
import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.*;

public class JoelFleet extends Fleet {

    /** 
     * Number of tankers in the fleet (this is just an example, not a requirement).
     */
    private static int EFFICIENT_TANKERS_SIZE = 0;
    private static int SCOUT_TANKERS_SIZE = 0;
    private static int LONELY_TANKERS_SIZE = 0;
    private static int LONELY_SCOUT_TANKERS_SIZE = 0;
    private static int RECRUIT_SCOUT_TANKERS_SIZE = 1;
    private static int LEASH_TANKERS_SIZE = 0;
    
    
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
		for (int i=0; i<EFFICIENT_TANKERS_SIZE; i++) {
		    this.add(new EfficientTanker(r,this));
		}
		for (int i=0; i<SCOUT_TANKERS_SIZE; i++) {
			this.add(new ScoutTanker(r,this));
		}
		for (int i=0; i<LONELY_TANKERS_SIZE; i++) {
			this.add(new LonelyTanker(r,this));
		}
		for (int i=0; i<LONELY_SCOUT_TANKERS_SIZE; i++) {
			this.add(new LonelyTanker(r,this));
		}
		for (int i= 0;i<RECRUIT_SCOUT_TANKERS_SIZE; i++) {
			this.add(new LonelyScoutRecruitTanker(r,this));

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
	
	public double getNearbyTankerDistance(Location here) {
		double tempDist = 0;
		double bestDist = 9999999;
		for(int i = 0;i<this.size();i++) {
			if(!this.get(i).equals(here)) {
				JoelTanker tanker = (JoelTanker) this.get(i);
				double xDiff = Math.abs(here.x - tanker.currentX);
				double yDiff = Math.abs(here.y - tanker.currentY);
				tempDist = xDiff > yDiff ? xDiff : yDiff;
				
				if(tempDist < bestDist) {
					bestDist = tempDist;
				}
			}
		}
		
		return bestDist;
	}
}
