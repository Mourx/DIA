package uk.ac.nott.cs.g53dia.multiagent;

import java.util.Random;

import uk.ac.nott.cs.g53dia.multilibrary.Action;
import uk.ac.nott.cs.g53dia.multilibrary.Cell;

public class ScoutTanker extends JoelTanker {
	public ScoutTanker() {
		this(new Random(),null);
	}

	/**
	 * The tanker implementation makes random moves. For reproducibility, it
	 * can share the same random number generator as the environment.
	 * 
	 * @param r
	 *            The random number generator.
	 */
	public ScoutTanker(Random r, JoelFleet fleet) {
		this.r = r;
		Fleet = fleet;
	}
	

	
	@Override
 	public Action senseAndAct(Cell[][] view, boolean actionFailed, long timestep) {
		Initialise(view);
		
		stepNumber++;

		ScanArea(view);
		UpdateClusterWells();
		return null;
	}
}
