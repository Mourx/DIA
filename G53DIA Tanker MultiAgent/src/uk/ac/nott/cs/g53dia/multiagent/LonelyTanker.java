package uk.ac.nott.cs.g53dia.multiagent;

import java.util.ArrayList;
import java.util.Random;

import uk.ac.nott.cs.g53dia.multilibrary.Action;
import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;
import uk.ac.nott.cs.g53dia.multilibrary.Point;
import uk.ac.nott.cs.g53dia.multilibrary.Station;
import uk.ac.nott.cs.g53dia.multilibrary.Task;
import uk.ac.nott.cs.g53dia.multiagent.JoelFleet;

public class LonelyTanker extends JoelTanker {
	public LonelyTanker() {
		this(new Random(),null);
	}

	/**
	 * The tanker implementation makes random moves. For reproducibility, it
	 * can share the same random number generator as the environment.
	 * 
	 * @param r
	 *            The random number generator.
	 */
	public LonelyTanker(Random r, JoelFleet fleet) {
		this.r = r;
		Fleet = fleet;
		exploreSteps = EXPLORE_LIMIT;
	
	}
	
	int WEIGHT_NEARBY_TANKER = 10;
	
	
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
	
	//gets the best Task nearby
		public Task getBestTask() {
			double smallestDist = 9999;
			double tempDist = 0;
			double nextDist = 0;
			double pumpDist = 0;
			double wasteEff = 0;
			double bestPumpDist = 0;
			double tankerDist = 0;
			Task candTask;
			Task bestTask = currentTask;
			if(currentTask !=null && CheckIfInRange(getLocation(currentTask.getStationPosition()))) bestTask = currentTask;
			if(Fleet.AvailableTasks != null)
			for(int i = 0;i<Fleet.AvailableTasks.size();i++) {
				candTask = Fleet.AvailableTasks.get(i);
				if(CheckIfInRange(Fleet.Locations.get(getStationID(candTask.getStationPosition()))) &&
															getLocation(candTask.getStationPosition()).bTaskTaken == false &&
															candTask.getWasteRemaining() >0) {
					if (bestTask == null) {
						bestTask = candTask;
					}else {
						if(candTask.getWasteRemaining() > MIN_WASTE) {
							
							Station candStation = getStation(candTask.getStationPosition());
							Location candStationLocation = getLocation(candStation.getPoint());
							Location candWellLocation = getNearestWellLocation(candStation);
							tempDist = DistanceTo(candStationLocation) * WEIGHT_DISTANCE;
							nextDist = DistanceTo(candStationLocation,getNearestStation(candStationLocation))*WEIGHT_STATION;
							pumpDist = DistanceTo(candStationLocation,getNearestPump(candStationLocation))*WEIGHT_PUMP;
							wasteEff = 1.0/candTask.getWasteRemaining() * WEIGHT_WASTE;
							tankerDist =  1.0/Fleet.getNearbyTankerDistance(candStationLocation) * WEIGHT_NEARBY_TANKER;
							if(getBestPump()!= null) {
								bestPumpDist = DistanceTo(getBestPump()) * WEIGHT_BEST_PUMP;
							}else {
								bestPumpDist = 0;
							}
							tempDist = tempDist + nextDist + pumpDist + wasteEff + bestPumpDist + tankerDist;
							if(tempDist<= smallestDist) { 
								smallestDist = tempDist; 
								bestTask = candTask;
							}
								
						}
					}
						
				}
			}
			return bestTask;
		}
}
