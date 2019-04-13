package uk.ac.nott.cs.g53dia.multiagent;

import java.util.Random;

import uk.ac.nott.cs.g53dia.multilibrary.Action;
import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.FuelPump;
import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;
import uk.ac.nott.cs.g53dia.multilibrary.Station;
import uk.ac.nott.cs.g53dia.multilibrary.Task;
import uk.ac.nott.cs.g53dia.multilibrary.Well;

public class LonelyScoutRecruitTanker extends JoelTanker {
	public LonelyScoutRecruitTanker() {
		this(new Random(),null);
	}

	/**
	 * The tanker implementation makes random moves. For reproducibility, it
	 * can share the same random number generator as the environment.
	 * 
	 * @param r
	 *            The random number generator.
	 */
	public LonelyScoutRecruitTanker(Random r, JoelFleet fleet) {
		this.r = r;
		Fleet = fleet;
		exploreSteps = EXPLORE_LIMIT;

	}
	int WEIGHT_NEARBY_TANKER = 300;
	int MAX_FLEET_SIZE = 10;
	int ADD_STEPS = 750;
	int SCOUT_STEPS = EXPLORE_LIMIT;
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
 			if(Fleet.size() == 1) {
				Fleet.add(new LonelyTanker(r,Fleet));
			}
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
	
	//scans the visible area; updates and adds found location features
	public void ScanArea(Cell[][] view) {
		//scan the viewable area for stations and wells
		// save them in an arraylist of locations to store them for later
		for(int i = 0;i < this.VIEW_RANGE*2;i++) {
			for(int j = 0;j < this.VIEW_RANGE*2;j++) {
				if(view[j][i] instanceof Station) {
					Station station = (Station)view[j][i];
					//if(getStation(station.getPoint()) == null) {
						int id = getStationID(station.getPoint());
						if(id != -1) {
							
							Fleet.Locations.set(id, new Location(station,station.getPoint(),j-20+currentX,20-i+currentY,getLocation(station.getPoint()).bTaskTaken,id));
						}else {
							Fleet.Locations.add(new Location(station,station.getPoint(),j-20+currentX,20-i+currentY,false,Fleet.Locations.size()));
							boolean bAdded = false;
							for(int k = 0;k<Clusters.size();k++) {
								if(DistanceTo(Fleet.Locations.get(Fleet.Locations.size()-1),Clusters.get(k).getCentreLocation()) <=10){
									Clusters.get(k).AddLocation(Fleet.Locations.get(Fleet.Locations.size()-1));
									bAdded = true;
								}
							}
							if(bAdded == false) {
							
								Clusters.add(new Cluster());
								Clusters.get(Clusters.size()-1).AddLocation(Fleet.Locations.get(Fleet.Locations.size()-1));;
							}
						}
					//}
				}else if(view[j][i] instanceof Well) {
					Well well = (Well)view[j][i];
					if(getWell(well.getPoint()) == null) {
						Fleet.Locations.add(new Location(well,well.getPoint(),j-20+currentX,20-i+currentY));
					}
				}else if(view[j][i] instanceof FuelPump) {
					FuelPump pump = (FuelPump)view[j][i];
					if(getPump(pump.getPoint()) == null) {
						pumpsFound += 1;
						Fleet.Locations.add(new Location(pump,pump.getPoint(),j-20+currentX,20-i+currentY));
						if(stepNumber <=ADD_STEPS && Fleet.size() < pumpsFound && Fleet.size() < MAX_FLEET_SIZE) {
							if(DistanceTo(FuelPumpLocation,getLocation(pump.getPoint())) >21) {
							//Fleet.add(new LeashTanker(r,Fleet,Fleet.Locations.get(Fleet.Locations.size()-1)));
								Fleet.add(new LonelyTanker(r,Fleet));
							}
						}
					}
				}
			}
		}
		
		for(int i = 0;i<Fleet.Locations.size();i++) {
			if(Fleet.Locations.get(i).getStation() != null) {
				Station station = Fleet.Locations.get(i).getStation();
				Task task = station.getTask();
				if(task != null) {
					boolean bExists = false;
					for(int j = 0;j<Fleet.AvailableTasks.size();j++) {
						if(task.equals(Fleet.AvailableTasks.get(j))) {
							bExists = true;
							if(Fleet.AvailableTasks.get(j).isComplete()) {
								Fleet.AvailableTasks.remove(j);
							}
							break;
						}
					}
					if(!bExists) {
						Fleet.AvailableTasks.add(task);
					}
				}
			}
		}
	}
	
	
}
