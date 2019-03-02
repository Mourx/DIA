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
	Task currentTask = null;
	ArrayList<Task> AvailableTasks = new ArrayList<Task>();
	ArrayList<Location> Locations = new ArrayList<Location>();
	int currentX,currentY = 0;
	int MovesLeft = 0;
	Station targetStation = null;
	int MovesToFuel = 0;
	Point currentPump = FUEL_PUMP_LOCATION;
	Location FuelPumpLocation = null;
	Location targetWell = null;
	Location startLoc = null;
	int stepNumber = 0;
	boolean bFuelTime = false;
	boolean bDisposeTime = false;
	boolean bHomeTime = false;
	boolean bFindPumps = false;
	int Direction = MoveAction.NORTHEAST;
	@Override
	public Action senseAndAct(Cell[][] view, boolean actionFailed, long timestep) {
		// TODO Auto-generated method stub
		{
			stepNumber++;
			currentPoint = (Point)this.getPosition();
			if(startLoc == null) startLoc = new Location(null,currentPoint,0,0,0);
			if(FuelPumpLocation == null) FuelPumpLocation = new Location((FuelPump)this.getCurrentCell(view),this.getPosition(),0,0);
			FuelPumpLocation = getNearestPump();
		}	
		if(stepNumber > 400) {
			bFindPumps = false;
		}
		
		ScanArea(view);
		if(stepNumber >= 3250) {
			bHomeTime = !bHomeTime;
			stepNumber = 0;
		}
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
				currentTask = getBestTaskHOME();
				}
		}
		MovesToFuel = DistanceTo(FuelPumpLocation);

		
		if(currentTask!=null) {
			if(!bDisposeTime && !CheckIfInRange(Locations.get(getStationID(currentTask.getStationPosition())))) {
				if(this.getFuelLevel() < 200) {
					bFuelTime = true;
				}else{
					currentTask =null;
				};
			}
		}
		if(this.getFuelLevel() <= MovesToFuel*2 +4) {
			
			if(!bFuelTime) {
				Direction += 1;
				if(Direction >7) {
					Direction = 4;
				}
				bFuelTime = true;
			}
		}
		
		if(bFuelTime){
			MoveAction action = JoelMoveToward(FuelPumpLocation);
			if(action != null) {
				MovesToFuel-= 1;
				return action;
			}else {
				bFuelTime = false;
				return new RefuelAction();
			}
		}else if(bFindPumps) {
			MovesToFuel+= 1;
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
			return new MoveAction(Direction);
		}else if(currentTask != null) {
			if(this.getWasteLevel() <1000 && currentTask.getWasteRemaining()<= 1000-this.getWasteLevel()) {
				
				MoveAction action = JoelMoveToward(getLocation(currentTask.getStationPosition()));
				if(action!=null){
					MovesToFuel+= 1;
					return action;
				}else {
					//set currentTask to null so we can find next task and deliver to well near it
					Task temp = currentTask;
					currentTask = null;
					return new LoadWasteAction(temp);
				}
			}else {
				
				targetWell = getNearestWellLocation(getStation(currentTask.getStationPosition()));
				bDisposeTime = true;
				
				/*
				 * if(!CheckIfInRange(targetWell)) { MoveAction action =
				 * JoelMoveToward(FuelPumpLocation); MovesToFuel-= 1; bFuelTime = true; return
				 * action; }
				 */
				 
				MoveAction action = JoelMoveToward(targetWell);
				if(action != null) {
					MovesToFuel+= 1;
					return action; 
				}else {
					bDisposeTime = false;
					return new DisposeWasteAction();
				}
			}
			
		}else {
			if(bHomeTime) {
				Action action = JoelMoveToward(startLoc);
				if(action!=null) {
					return action;
					
				}else {
					bHomeTime = false;
				}
			}
			if(!bHomeTime){
				MovesToFuel+= 1;
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
				return new MoveAction(Direction);
			}
			else return null;
		}
	}
	
	public boolean CheckIfInRange(Location there) {
		int distanceThere = DistanceTo(there);
		int distanceToFuel = DistanceTo(there,getNearestPump(there));
		if((distanceThere + distanceToFuel)*2 <= this.getFuelLevel() + 4) {
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
				tempDist = DistanceTo(here,Locations.get(i));
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
	
	public Task getBestTask() {
		double smallestDist = 9999;
		double tempDist = 0;
		double nextDist = 0;
		double bestEfficiency = 8888;
		double efficiency = 0;
		double pumpDist = 0;
		Task candTask;
		Task bestTask = null;
		if(currentTask !=null && CheckIfInRange(getLocation(currentTask.getStationPosition()))) bestTask = currentTask;
		if(AvailableTasks != null)
		for(int i = 0;i<AvailableTasks.size();i++) {
			candTask = AvailableTasks.get(i);
			if(CheckIfInRange(Locations.get(getStationID(candTask.getStationPosition()))) && 
						candTask.getWasteRemaining() >0) {
				if (bestTask == null) {
					bestTask = candTask;
				}else {
					if(candTask.getWasteRemaining() > 200) {
						
						Station candStation = getStation(candTask.getStationPosition());
						Location candStationLocation = getLocation(candStation.getPoint());
						Location candWellLocation = getNearestWellLocation(candStation);
						tempDist = DistanceTo(candStationLocation);
						nextDist = DistanceTo(candStationLocation,getNearestStation(candStationLocation));
						pumpDist = DistanceTo(candStationLocation,getNearestPump(candStationLocation));
						tempDist = tempDist + nextDist*0.1 + pumpDist*0.2;
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
	
	public Task getBestTaskHOME() {
		double smallestDist = 9999;
		double tempDist = 0;
		double nextDist = 0;
		double bestEfficiency = 8888;
		double efficiency = 0;
		double pumpDist = 0;
		Task candTask;
		Task bestTask = null;
		if(currentTask !=null && CheckIfInRange(getLocation(currentTask.getStationPosition()))) bestTask = currentTask;
		if(AvailableTasks != null)
		for(int i = 0;i<AvailableTasks.size();i++) {
			candTask = AvailableTasks.get(i);
			if(CheckIfInRange(Locations.get(getStationID(candTask.getStationPosition()))) && 
						candTask.getWasteRemaining() >0) {
				if (bestTask == null) {
					bestTask = candTask;
				}else {
					if(candTask.getWasteRemaining() > 200) {
						
						Station candStation = getStation(candTask.getStationPosition());
						Location candStationLocation = getLocation(candStation.getPoint());
						Location candWellLocation = getNearestWellLocation(candStation);
						tempDist = DistanceTo(candStationLocation);
						nextDist = DistanceTo(candStationLocation,startLoc);
						pumpDist = DistanceTo(candStationLocation,getNearestPump(candStationLocation));
						tempDist = tempDist + nextDist*0.17+ pumpDist*0.2;
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
				nextDist = DistanceTo(Locations.get(i),FuelPumpLocation);
				statDist = DistanceTo(Locations.get(i),getNearestStation(Locations.get(i)));
				tempDist = tempDist + statDist*0.54+ nextDist*0;
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
			int smallestDist = 9999;
			int tempDist = 0;
			int nextDist;
			Location BestLoc = null;
			Location loc = getLocation(station.getPoint());
			for(int i = 0;i<Locations.size();i++) {
				if(Locations.get(i).getWell() != null) {
					tempDist = DistanceTo(getLocation(Locations.get(i).getWell().getPoint()));
					nextDist = DistanceTo(Locations.get(i),startLoc);
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
	
	public FuelPump getPump(Point p) {
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getPoint().equals(p)) {
				return Locations.get(i).getPump();
			}
		}
		return null;
	}
	
	public MoveAction JoelMoveToward(Location loc) {
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
	
	private void ScanArea(Cell[][] view) {
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
	
	
	

}
