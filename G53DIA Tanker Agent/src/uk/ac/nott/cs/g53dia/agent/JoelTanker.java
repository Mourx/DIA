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
	@Override
	public Action senseAndAct(Cell[][] view, boolean actionFailed, long timestep) {
		// TODO Auto-generated method stub
		currentPoint = (Point)this.getPosition().clone();
		if(FuelPumpLocation == null) FuelPumpLocation = new Location((FuelPump)this.getCurrentCell(view),this.getPosition(),0,0);
		FuelPumpLocation = getNearestPump();
		ScanArea(view);
		currentTask = getBestTask();
		if(currentTask != null && this.getWasteLevel() <= 0 && currentTask.isComplete()) {
			currentTask = null;
		}
		
		
		if(this.getFuelLevel()<=MovesToFuel*2 +6) {
			MoveAction action = JoelMoveToward(FuelPumpLocation);
			if(action != null) {
				MovesToFuel-= 1;
				return action;
			}else {
				return new RefuelAction();
			}
		}else if(currentTask != null) {
			if(this.getWasteLevel()>0) {
				targetWell = getNearestWellLocation(getStation(currentTask.getStationPosition()));
				MoveAction action = JoelMoveToward(targetWell);
				if(action != null) {
					MovesToFuel+= 1;
					return action; 
				}else {
					return new DisposeWasteAction();
				}
			}else {
				MoveAction action = JoelMoveToward(getLocation(currentTask.getStationPosition()));
				if(action!=null){
					MovesToFuel+= 1;
					return action;
				}else {
					return new LoadWasteAction(currentTask);
				}
			}
			
		}else {
			MovesToFuel+= 1;
			currentX += 1;
			currentY += 1;
			return new MoveAction(MoveAction.NORTHEAST);
		}
	}
	
	public Location getNearestPump() {
		int smallestDist = MovesToFuel;
		int tempDist = 0;
		Location BestLoc = FuelPumpLocation;
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getPump() != null) {
				int diffX = Math.abs(currentX - Locations.get(i).x);
				int diffY = Math.abs(currentY - Locations.get(i).y);
				if(diffX > diffY) {
					tempDist = diffX;
				}else {
					tempDist = diffY;
				}
				if(tempDist<= smallestDist) {
					smallestDist = tempDist;
					BestLoc = Locations.get(i);
				}else if (tempDist == smallestDist){
					//check closest to current refuel if equal
				}
			}
		}
		MovesToFuel = smallestDist;
		return BestLoc;
	}
	
	public Task getBestTask() {
		int smallestDist = 9999;
		int tempDist = 0;
		Task candTask;
		Task bestTask = null;
		if(AvailableTasks != null)
		for(int i = 0;i<AvailableTasks.size();i++) {
			candTask = AvailableTasks.get(i);
			if(candTask.getWasteRemaining() >=100) {
				
				Station candStation = getStation(candTask.getStationPosition());
				Location candStationLocation = getLocation(candStation.getPoint());
				Location candWellLocation = getNearestWellLocation(candStation);
				//int diffX = Math.abs(candStationLocation.x - candWellLocation.x);
				//int diffY = Math.abs(candStationLocation.y - candWellLocation.y);
				int diffX = Math.abs(currentX - candWellLocation.x);
				int diffY = Math.abs(currentY - candWellLocation.y);
				if(diffX > diffY) {
					tempDist = diffX;
				}else {
					tempDist = diffY;
				}
				if(tempDist< smallestDist) {
					smallestDist = tempDist;
					bestTask = candTask;
				}
			}else {
				if (bestTask == null) {
					bestTask = candTask;
				}
			}
		}
		return bestTask;
	}
	
	//get the nearest well to the station
	public Location getNearestWellLocation(Station station) {
		int smallestDist = 9999;
		int tempDist = 0;
		Location BestLoc = null;
		Location loc = getLocation(station.getPoint());
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getWell() != null) {
				int diffX = Math.abs(loc.x - Locations.get(i).x);
				int diffY = Math.abs(loc.y - Locations.get(i).y);
				if(diffX > diffY) {
					tempDist = diffX;
				}else {
					tempDist = diffY;
				}
				if(tempDist< smallestDist) {
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
				currentY += 1;
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
					if(getStation(station.getPoint()) == null) {
						Locations.add(new Location(station,station.getPoint(),j-20+currentX,20-i+currentY));
					}
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
					AvailableTasks.add(task);
				}
			}
		}
	}
	
	
	

}
