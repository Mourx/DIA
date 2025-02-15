package uk.ac.nott.cs.g53dia.multiagent;

import java.util.Random;

import uk.ac.nott.cs.g53dia.multilibrary.Action;
import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;

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
	

	int SCOUT_STEPS = 1500;
	@Override
 	public Action senseAndAct(Cell[][] view, boolean actionFailed, long timestep) {
		Initialise(view);
		
		stepNumber++;
		exploreSteps++;
		ScanArea(view);
		UpdateClusterWells();
		if(stepNumber <= SCOUT_STEPS) {
			MovesToFuel = DistanceTo(FuelPumpLocation);
			//check before fuel to avoid arriving at station then leaving before loading
			Action faction = CheckFuel();
			if(faction!=null) {
				return faction;
			}
			faction = MoveToFuel();
			if(faction != null) {
				return faction;
			}
			MovesToFuel+= 1;
			incrementXY(Direction);
			return new MoveAction(Direction);
		}else {
			if(exploreSteps < 400) {
				exploreSteps = 400;
			}
			CheckModes();
	
			//don't switch if close to task
			UpdateTask();
			
			MovesToFuel = DistanceTo(FuelPumpLocation);
			//check before fuel to avoid arriving at station then leaving before loading
			Action faction = CheckFuel();
			if(faction!=null) {
				return faction;
			}
			VerifyTask();
			
			
			faction = MoveToFuel();
			if(faction != null) {
				return faction;
			}
			if(bFindPumps) {
				MovesToFuel+= 1;
				incrementXY(Direction);
				return new MoveAction(Direction);
			}else if(currentTask != null) {
				if(this.getWasteLevel() <=MAX_WASTE && currentTask.getWasteRemaining()<= 1000-this.getWasteLevel()) {
					
					faction = LoadWasteAction();
					if(faction != null) {
						return faction;
					}
				}else {
					faction = DisposeWasteAction();
					if(faction!=null) {
						return faction;
					}
				}
				
			}else {
				if(bHomeTime) {
					Action action = CustomMoveToward(startLoc);
					if(action!=null) {
						return action;
						
					}else {
						//stepNumber = 0;
						bHomeTime = false;
					}
				}
				if(!bHomeTime){
					MovesToFuel+= 1;
					incrementXY(Direction);
					return new MoveAction(Direction);
				}
				else return null;
			}
		}
		return null;
	}
}
