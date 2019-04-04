package uk.ac.nott.cs.g53dia.multiagent;

import java.util.ArrayList;
import java.util.Random;

import uk.ac.nott.cs.g53dia.multilibrary.Action;
import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.FuelPump;
import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;
import uk.ac.nott.cs.g53dia.multilibrary.Point;
import uk.ac.nott.cs.g53dia.multilibrary.Station;
import uk.ac.nott.cs.g53dia.multilibrary.Task;
import uk.ac.nott.cs.g53dia.multiagent.JoelFleet;

public class LeashTanker extends JoelTanker {
	public LeashTanker() {
		this(new Random(),null, null);
	}

	/**
	 * The tanker implementation makes random moves. For reproducibility, it
	 * can share the same random number generator as the environment.
	 * 
	 * @param r
	 *            The random number generator.
	 */
	public LeashTanker(Random r, JoelFleet fleet, Location FuelPumpLoc) {
		this.r = r;
		Fleet = fleet;
		FuelPumpLocation = FuelPumpLoc;
	}
	

	
	@Override
 	public Action senseAndAct(Cell[][] view, boolean actionFailed, long timestep) {
		
		
		stepNumber++;
		exploreSteps++;
		if(stepNumber == 9700) {
			int thisIntISforDebugging = 1;
			thisIntISforDebugging++;
		}
		Initialise(view);
		
			
		ScanArea(view);
		UpdateClusterWells();
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
		return null;
	}
	
	//refreshes important variables
	public void Initialise(Cell[][] view) {
		currentPoint = (Point)this.getPosition();
		if(startLoc == null) startLoc = new Location(null,currentPoint,0,0,false,0);
		
		
	}
	
	public boolean CheckIfInRange(Location there) {
		int distanceThere = DistanceTo(there);
		int distanceToFuel = DistanceTo(there,FuelPumpLocation);
		if((distanceThere + distanceToFuel)*2 <= this.getFuelLevel() - 2) {
			return true;
		}
		return false;
	}
	
	
}
