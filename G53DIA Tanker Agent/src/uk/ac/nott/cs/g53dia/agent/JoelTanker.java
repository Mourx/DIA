package uk.ac.nott.cs.g53dia.agent;

import java.util.ArrayList;
import java.util.Random;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.DisposeWasteAction;
import uk.ac.nott.cs.g53dia.library.FuelPump;
import uk.ac.nott.cs.g53dia.library.LoadWasteAction;
import uk.ac.nott.cs.g53dia.library.MoveAction;
import uk.ac.nott.cs.g53dia.library.Point;
import uk.ac.nott.cs.g53dia.library.RefuelAction;
import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Tanker;
import uk.ac.nott.cs.g53dia.library.Task;
import uk.ac.nott.cs.g53dia.library.Well;

public class JoelTanker extends Tanker{

	
	public JoelTanker() {
		this(new Random());
	}

	/**
	 * The tanker implementation makes random moves. For reproducibility, it
	 * can share the same random number generator as the environment.
	 * 
	 * @param r
	 *            The random number generator.
	 */
	public JoelTanker(Random r) {
		this.r = r;
	}
	
	private Point currentPoint;
	int pumpsFound = 0;
	Task currentTask = null;
	ArrayList<Task> AvailableTasks = new ArrayList<Task>();
	ArrayList<Location> Locations = new ArrayList<Location>();
	ArrayList<Cluster> Clusters = new ArrayList<Cluster>();
	int currentX,currentY = 0;
	int MovesLeft = 0;
	Station targetStation = null;
	int MovesToFuel = 0;
	Point currentPump = FUEL_PUMP_LOCATION;
	Location FuelPumpLocation = null;
	Location targetWell = null;
	Location startLoc = null;
	Location bestPumpLocation = null;
	int stepNumber = 0;
	int exploreSteps = 0;
	boolean bFuelTime = false;
	boolean bDisposeTime = false;
	boolean bHomeTime = false;
	boolean bFindPumps = true;
	boolean bSecondSearch = false;
	boolean bMoveToPump = false;
	double WEIGHT_WASTE =5750;
	double WEIGHT_HOME = 0.17;
	double WEIGHT_PUMP = 0.1;
	double WEIGHT_STATION = 0.2;
	double WEIGHT_BEST_PUMP = 0.0;
	double WEIGHT_WELL_PUMP = 0.2;
	double WEIGHT_WELL_STATION = 0.54;
	double WEIGHT_DISTANCE = 1.25;
	int EXPLORE_LIMIT = 390;
	int MIN_WASTE = 150;
	int MAX_WASTE = 845;
	int BEST_PUMP_INTERVAL = 2500;
	int HOME_MODE_INTERVAL = 4000;
	int Direction = MoveAction.NORTHEAST;
	
	
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
		if(startLoc == null) startLoc = new Location(null,currentPoint,0,0,0);
		if(FuelPumpLocation == null) FuelPumpLocation = new Location((FuelPump)this.getCurrentCell(view),this.getPosition(),0,0);
		if(currentTask == null) {
			FuelPumpLocation = getNearestPump();
		}else {
			if(CheckIfInRange((getLocation(currentTask.getStationPosition())))) {
				FuelPumpLocation = getNearestPump(getLocation(currentTask.getStationPosition()));
			}else {
				FuelPumpLocation = getNearestPump();	
			}
		}
	}
	
	//checks all behaviour modes
	public void CheckModes() {
		if(exploreSteps > EXPLORE_LIMIT) {
			if(pumpsFound >= 2) {
				bFindPumps = false;
			}else if(!bSecondSearch){
				bSecondSearch = true;
				Direction = MoveAction.NORTH;
				exploreSteps = 0;
			} else {
				bFindPumps = false;
			}
		}
		if(stepNumber % BEST_PUMP_INTERVAL ==0) {
			bHomeTime = false;
			bestPumpLocation = getBestPump();
			bMoveToPump = true;
		}		
		
		if(stepNumber % HOME_MODE_INTERVAL ==0) {
			bHomeTime = !bHomeTime;
			
		}
	}
	
	//Updates the current Task if necessary
	public void UpdateTask() {
		if(currentTask != null && DistanceTo(getLocation(currentTask.getStationPosition())) >= 15) {
			if(!bHomeTime) {
				currentTask = getBestTask();
			}else {
				currentTask = getBestTaskHOME();
			}
		}else if(currentTask == null){
			if(!bHomeTime) {
				currentTask = getBestTask();
			}else {
				//return JoelMoveToward(startLoc);
				currentTask = getBestTaskHOME();
			}
		}
	}
	
	//checks fuel conditions
	public Action CheckFuel() {
		if(this.getWasteLevel() >=MAX_WASTE) {
			bDisposeTime = true;
		}else {
			//bDisposeTime = false;
		}
		if(this.getFuelLevel() <= DistanceTo(FuelPumpLocation)*2 +4) {
			if(!bDisposeTime && currentTask!=null && DistanceTo(getLocation(currentTask.getStationPosition())) == 0) {
				Task temp = currentTask;
				currentTask = null;
				return new LoadWasteAction(temp);
				
			}
			if(!bFuelTime) {
				Direction += 1;
				if(!bSecondSearch) {
					if(Direction >7) {
						Direction = 4;
					}
				}else {
					if(Direction >4) {
						Direction = 0;
					}
				}
				bFuelTime = true;
			}
		}
		return null;
	}
	
	//verifies tasks are in range
	public void VerifyTask() {
		if(!bFindPumps && currentTask!=null) {
			if(!bDisposeTime && !CheckIfInRange(Locations.get(getStationID(currentTask.getStationPosition())))) {
				if(this.getFuelLevel() < 200) {
					bFuelTime = true;
				}else{
					currentTask =null;
				};
			}
		}
	}
	
	//moves towards fuel pump if necessary
	public Action MoveToFuel() {
		if(bFuelTime){
			MoveAction action = CustomMoveToward(FuelPumpLocation);
			if(action != null) {
				MovesToFuel-= 1;
				return action;
			}else {
				bFuelTime = false;
				return new RefuelAction();
			}
		}
		if(bMoveToPump && bestPumpLocation != null) {
			
			Action action = CustomMoveToward(bestPumpLocation);
			if(action!=null) {
				MovesToFuel-= 1;
				return action;
			}else {
				bMoveToPump = false;
				if(this.getFuelLevel()<200) {
					bFuelTime = false;
					return new RefuelAction();
				}
			}
		}
		return null;
	}
	
	//moves toward task and pickups waste if necessary
	public Action LoadWasteAction() {			
		MoveAction action = CustomMoveToward(getLocation(currentTask.getStationPosition()));
		if(action!=null){
			MovesToFuel+= 1;
			return action;
		}else {
			//set currentTask to null so we can find next task and deliver to well near it
			Task temp = currentTask;
			currentTask = null;
			return new LoadWasteAction(temp);
		}

	}
	
	//moves toward wells and disposes of waste if necessary
	public Action DisposeWasteAction() {
		Task tempTask;

		if(bHomeTime) {
			tempTask = getBestTaskHOME();
			targetWell = getNearestWellLocation(getStation(tempTask.getStationPosition()));
		}else {
			tempTask = getBestTask();
			Point point = tempTask.getStationPosition();
			targetWell = getNearestWellLocation(getStation(point));
		}
		bDisposeTime = true;
		
		//Make sure we can reach the well
	 	if(!CheckIfInRange(targetWell)) { 
	 		FuelPumpLocation = getNearestPump(targetWell);
		 	MoveAction action = CustomMoveToward(FuelPumpLocation);
		 	if(action!=null) {
			 	MovesToFuel-= 1;
			 	bFuelTime = true; 
			 	return action;
		 	}else {
		 		MovesToFuel+= 1;
		 		bDisposeTime = false;
				incrementXY(Direction);
				bFindPumps = true;
				exploreSteps = 0;
				return new MoveAction(Direction);
		 	}		 		
	 	}
		 
		 
		MoveAction action = CustomMoveToward(targetWell);
		if(action != null) {
			MovesToFuel+= 1;
			return action; 
		}else {
			bDisposeTime = false;
			
			return new DisposeWasteAction();
		}
	}
	
	//moves toward other locations 
	public Action MoveAroundAction() {
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
	
	public Location getBestPump() {
		Location bestPump = bestPumpLocation;
		double pumpScore = 0;
		double bestScore = 0;
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getPump()!= null && CheckIfInRange(Locations.get(i))) {
				pumpScore = 0;
				if(bestPump == null) {
					bestPump = Locations.get(i);
				}else {	
					for(int j = 0;j<Clusters.size();j++) {
						if(DistanceTo(Clusters.get(j).getCentreLocation(),Locations.get(i)) <= 15 && Clusters.get(j).Stations.size() > 0){
							if(Clusters.get(j).nearestWell != null && DistanceTo(Clusters.get(j).getCentreLocation(),Clusters.get(j).nearestWell) != 0) {
								pumpScore += Clusters.get(j).getSize() * 1/DistanceTo(Clusters.get(j).getCentreLocation(),Clusters.get(j).nearestWell);
							}else {
								pumpScore += Clusters.get(j).getSize() * 1;
							}													
						}
						
					}
					if(pumpScore >= bestScore) {
						bestScore = pumpScore;
						bestPump = Locations.get(i);
					}
				}
			}
		}
		return bestPump;
	}

	public void incrementXY(int Direction) {
		if(Direction == 0) {
			currentY += 1;
		}else if(Direction == 1) {
			currentY -= 1;
		}else if(Direction == 2) {
			currentX += 1;
		}else if(Direction == 3) {
			currentX -= 1;
		}else if(Direction == 4) {
			currentX += 1;
			currentY += 1;
		}else if(Direction == 5) {
			currentX -= 1;
			currentY += 1;
		}else if(Direction == 6) {
			currentX += 1;
			currentY -= 1;
		}else if(Direction == 7) {
			currentX -= 1;
			currentY -= 1;
		}
	}
	
	public boolean CheckIfInRange(Location there) {
		int distanceThere = DistanceTo(there);
		int distanceToFuel = DistanceTo(there,getNearestPump(there));
		if((distanceThere + distanceToFuel)*2 <= this.getFuelLevel() - 2) {
			return true;
		}
		return false;
	}
	
	public int DistanceTo(Location here, Location there) {
		int diffX = Math.abs(here.x - there.x);
		int diffY = Math.abs(here.y - there.y);
		if(diffX > diffY) {
			return diffX;
		}else {
			return diffY;
		}
	}
	
	public int DistanceTo(Location loc) {
		int diffX = Math.abs(currentX - loc.x);
		int diffY = Math.abs(currentY - loc.y);
		if(diffX > diffY) {
			return diffX;
		}else {
			return diffY;
		}
	}
	
	public Location getNearestPump(Location here) {
		int smallestDist = DistanceTo(here);
		int tempDist = 0;
		Location BestLoc = FuelPumpLocation;
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getPump() != null) {
				if(currentTask != null) {
					tempDist = DistanceTo(getLocation(currentTask.getStationPosition()),Locations.get(i));
				}else {
					tempDist = DistanceTo(here,Locations.get(i));
				}
				if(tempDist<= smallestDist) {
					smallestDist = tempDist;
					BestLoc = Locations.get(i);
				}else if (tempDist == smallestDist){
					//check closest to current refuel if equal
				}	
			}
		}
		
		return BestLoc;
	}
	
	public Location getNearestPump() {
		int smallestDist = MovesToFuel;
		int tempDist = 0;
		Location BestLoc = FuelPumpLocation;
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getPump() != null) {
				tempDist = DistanceTo(Locations.get(i));
				if(tempDist<= smallestDist) {
					smallestDist = tempDist;

					BestLoc = Locations.get(i);
				}else if (tempDist == smallestDist){
					//check closest to current refuel if equal
				}
			}
		}

		return BestLoc;
	}
	
	//gets the nearest station to the given location
	public Location getNearestStation(Location here) {
		int smallestDist = 99999;
		int tempDist = 0;
		Location BestLoc = here;
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getStation() != null && Locations.get(i).getStation().getTask() != null && Locations.get(i).getStation().getTask().getWasteRemaining() > 0) {
				
				tempDist = DistanceTo(here,Locations.get(i));
				if(tempDist != 0) {
					if(tempDist< smallestDist) {
						smallestDist = tempDist;
						BestLoc = Locations.get(i);
					}else if (tempDist == smallestDist){
						//check closest to current refuel if equal
					}
				}
			}
		}

		return BestLoc;
	}
	
	//gets the best Task nearby
	public Task getBestTask() {
		double smallestDist = 9999;
		double tempDist = 0;
		double nextDist = 0;
		double pumpDist = 0;
		double wasteEff = 0;
		double bestPumpDist = 0;
		Task candTask;
		Task bestTask = currentTask;
		if(currentTask !=null && CheckIfInRange(getLocation(currentTask.getStationPosition()))) bestTask = currentTask;
		if(AvailableTasks != null)
		for(int i = 0;i<AvailableTasks.size();i++) {
			candTask = AvailableTasks.get(i);
			if(CheckIfInRange(Locations.get(getStationID(candTask.getStationPosition()))) && 
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
						if(getBestPump()!= null) {
							bestPumpDist = DistanceTo(getBestPump()) * WEIGHT_BEST_PUMP;
						}else {
							bestPumpDist = 0;
						}
						tempDist = tempDist + nextDist + pumpDist + wasteEff + bestPumpDist;
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
	
	//getBestTask but with alternative biases; used for Home Mode
	public Task getBestTaskHOME() {
		double smallestDist = 9999;
		double tempDist = 0;
		double nextDist = 0;
		double pumpDist = 0;
		double wasteEff = 0;
		double bestPumpDist = 0;
		Task candTask;
		Task bestTask = currentTask;
		if(currentTask !=null && CheckIfInRange(getLocation(currentTask.getStationPosition()))) bestTask = currentTask;
		if(AvailableTasks != null)
		for(int i = 0;i<AvailableTasks.size();i++) {
			candTask = AvailableTasks.get(i);
			if(CheckIfInRange(Locations.get(getStationID(candTask.getStationPosition()))) && 
						candTask.getWasteRemaining() >0) {
				if (bestTask == null) {
					bestTask = candTask;
				}else {
					if(candTask.getWasteRemaining() > MIN_WASTE) {
						
						Station candStation = getStation(candTask.getStationPosition());
						Location candStationLocation = getLocation(candStation.getPoint());
						Location candWellLocation = getNearestWellLocation(candStation);
						tempDist = DistanceTo(candStationLocation) * WEIGHT_DISTANCE;
						nextDist = DistanceTo(candStationLocation,startLoc) * WEIGHT_HOME;
						pumpDist = DistanceTo(candStationLocation,getNearestPump(candStationLocation))*WEIGHT_PUMP;
						wasteEff = 1.0/candTask.getWasteRemaining() * WEIGHT_WASTE;
						if(getBestPump()!= null) {
							bestPumpDist = DistanceTo(getBestPump()) * WEIGHT_BEST_PUMP;
						}else {
							bestPumpDist = 0;
						}
						tempDist = tempDist + nextDist + pumpDist + wasteEff + bestPumpDist;
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
	
	//get the nearest well to the station
	public Location getNearestWellLocation(Station station) {
		double smallestDist = 9999;
		double tempDist = 0;
		double statDist = 0;
		double nextDist = 0;
		Location BestLoc = null;
		Location loc = getLocation(station.getPoint());
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getWell() != null) {
				tempDist = DistanceTo(getLocation(Locations.get(i).getWell().getPoint()));
				nextDist = DistanceTo(Locations.get(i),FuelPumpLocation)*WEIGHT_WELL_PUMP;
				statDist = DistanceTo(Locations.get(i),getNearestStation(Locations.get(i)))*WEIGHT_WELL_STATION;
				tempDist = tempDist + statDist+ nextDist;
				if(tempDist<= smallestDist) {
					smallestDist = tempDist;
					BestLoc = Locations.get(i);
				}else if (tempDist == smallestDist){
					//check closest to current refuel if equal
				}
			}
		}
		
		return BestLoc;
	}
	
	//get the nearest well to the station
	public Location getNearestWellLocationHOMETIME(Station station) {
			double smallestDist = 9999;
			double tempDist = 0;
			double nextDist = 0;
			double statDist = 0;
			Location BestLoc = null;
			Location loc = getLocation(station.getPoint());
			for(int i = 0;i<Locations.size();i++) {
				if(Locations.get(i).getWell() != null) {
					tempDist = DistanceTo(getLocation(Locations.get(i).getWell().getPoint()));
					nextDist = DistanceTo(Locations.get(i),startLoc);
					statDist = DistanceTo(Locations.get(i),getNearestStation(Locations.get(i)))*WEIGHT_WELL_STATION;
					tempDist = tempDist + statDist + nextDist*0.5;
					if(tempDist<= smallestDist) {
						smallestDist = tempDist;
						BestLoc = Locations.get(i);
					}else if (tempDist == smallestDist){
						//check closest to current refuel if equal
					}
				}
			}
			
			return BestLoc;
		}
	
	//get generic location
	public Location getLocation(Point p) {
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getPoint().equals(p)) {
				return Locations.get(i);
			}
		}
		return null;
	}
	
	//get a station with point = p
	public Station getStation(Point p) {
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getPoint().equals(p)) {
				return Locations.get(i).getStation();
			}
		}
		return null;
	}
	
	//gets a stationID from the station with point = p
	public int getStationID(Point p) {
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getPoint().equals(p)) {
				return Locations.get(i).getID();
			}
		}
		return -1;
	}
	
	//get a well with point = p;
	public Well getWell(Point p) {
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getPoint().equals(p)) {
				return Locations.get(i).getWell();
			}
		}
		return null;
	}
	
	//gets a pump with point = p
	public FuelPump getPump(Point p) {
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getPoint().equals(p)) {
				return Locations.get(i).getPump();
			}
		}
		return null;
	}
	
	//uses MoveActions to reliably move toward a location
	public MoveAction CustomMoveToward(Location loc) {
		int diffX = currentX-loc.getX();
		int diffY = currentY-loc.getY();
		if(diffX <=-1) {
			if(diffY<=-1) {
				currentX += 1;
				currentY += 1;
				return new MoveAction(MoveAction.NORTHEAST);
			}
			if(diffY==0) {
				currentX += 1;
				return new MoveAction(MoveAction.EAST);
			}
			if(diffY>=1) {
				currentY -= 1;
				currentX += 1;

				return new MoveAction(MoveAction.SOUTHEAST);
			}
		}
		if(diffX ==0) {
			if(diffY<=-1) {
				currentY += 1;
				return new MoveAction(MoveAction.NORTH);
			}
			if(diffY==0) {
				return null;
			}
			if(diffY>=1) {
				currentY -= 1;
				return new MoveAction(MoveAction.SOUTH);
			}
		}
		if(diffX >= 1) {
			if(diffY<=-1) {
				currentX -= 1;
				currentY += 1;
				return new MoveAction(MoveAction.NORTHWEST);
			}
			if(diffY==0) {
				currentX -= 1;
				return new MoveAction(MoveAction.WEST);
			}
			if(diffY>=1) {
				currentX -= 1;
				currentY -= 1;
				return new MoveAction(MoveAction.SOUTHWEST);
			}
		}
		return null;
		
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
							Locations.set(id, new Location(station,station.getPoint(),j-20+currentX,20-i+currentY,id));
						}else {
							Locations.add(new Location(station,station.getPoint(),j-20+currentX,20-i+currentY,Locations.size()));
							boolean bAdded = false;
							for(int k = 0;k<Clusters.size();k++) {
								if(DistanceTo(Locations.get(Locations.size()-1),Clusters.get(k).getCentreLocation()) <=10){
									Clusters.get(k).AddLocation(Locations.get(Locations.size()-1));
									bAdded = true;
								}
							}
							if(bAdded == false) {
							
								Clusters.add(new Cluster());
								Clusters.get(Clusters.size()-1).AddLocation(Locations.get(Locations.size()-1));;
							}
						}
					//}
				}else if(view[j][i] instanceof Well) {
					Well well = (Well)view[j][i];
					if(getWell(well.getPoint()) == null) {
						Locations.add(new Location(well,well.getPoint(),j-20+currentX,20-i+currentY));
					}
				}else if(view[j][i] instanceof FuelPump) {
					FuelPump pump = (FuelPump)view[j][i];
					if(getPump(pump.getPoint()) == null) {
						pumpsFound += 1;
						Locations.add(new Location(pump,pump.getPoint(),j-20+currentX,20-i+currentY));
					}
				}
			}
		}
		
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getStation() != null) {
				Station station = Locations.get(i).getStation();
				Task task = station.getTask();
				if(task != null) {
					boolean bExists = false;
					for(int j = 0;j<AvailableTasks.size();j++) {
						if(task.equals(AvailableTasks.get(j))) {
							bExists = true;
							if(AvailableTasks.get(j).isComplete()) {
								AvailableTasks.remove(j);
							}
							break;
						}
					}
					if(!bExists) {
						AvailableTasks.add(task);
					}
				}
			}
		}
	}
	
	//updates nearest wells for clusters
	public void UpdateClusterWells() {
		
		for(int i = 0;i<Clusters.size();i++) {
			double smallestDist = 99999;
			for(int j = 0;j<Locations.size();j++){
				if(Locations.get(j).getWell()!= null) {
					if(DistanceTo(Locations.get(j),Clusters.get(i).getCentreLocation()) <= smallestDist){
						smallestDist = DistanceTo(Locations.get(j),Clusters.get(i).getCentreLocation());
						Clusters.get(i).setWell(Locations.get(i));
					}
				}
			}
		}
	}
	
	/*
	 * private void CompileClusters() {
	 * 
	 * for(int i = 0;i<Locations.size();i++) { boolean bAdded = false;
	 * if(Locations.get(i).getStation()!= null) {
	 * 
	 * for(int j = 0;j<Clusters.size();j++) {
	 * if(DistanceTo(Locations.get(i),Clusters.get(j).getCentreLocation()) <=10){
	 * Clusters.get(j).AddLocation(Locations.get(i)); bAdded = true; } } if(bAdded
	 * == false) {
	 * 
	 * Clusters.add(new Cluster());
	 * Clusters.get(Clusters.size()-1).AddLocation(Locations.get(i));; } } } }
	 */

}
