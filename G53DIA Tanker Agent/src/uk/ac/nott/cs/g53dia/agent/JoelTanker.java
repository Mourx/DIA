package uk.ac.nott.cs.g53dia.agent;

import java.util.ArrayList;
import java.util.Random;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.Point;
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
	//ArrayList<Station> NearbyStations;
	//ArrayList<Well> NearbyWells;
	ArrayList<Task> AvailableTasks;
	ArrayList<Location> Locations = new ArrayList<Location>();
	int currentX,currentY = 0;
	int MovesLeft = 0;
	@Override
	public Action senseAndAct(Cell[][] view, boolean actionFailed, long timestep) {
		// TODO Auto-generated method stub
		currentPoint = (Point)this.getPosition().clone();
		ScanArea(view);
		for(int i = 0;i<AvailableTasks.size();i++) {
			Task task = AvailableTasks.get(i);
			task.getWasteRemaining();
			getStation(task.getStationPosition());
		}
		if(getCurrentCell(view) instanceof Station ) {
			Station currentStation = (Station) getCurrentCell(view);
			currentTask = currentStation.getTask();
		}
		return null;
	}
	
	//get a station with point = p
	public Station getStation(Point p) {
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getPoint().equals(p)) {
				return (Station)Locations.get(i).getFeature();
			}
		}
		return null;
	}
	
	//get a well with point = p;
	public Well getWell(Point p) {
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getPoint().equals(p)) {
				return (Well)Locations.get(i).getFeature();
			}
		}
		return null;
	}
	
	
	private void ScanArea(Cell[][] view) {
		//scan the viewable area for stations and wells
		// save them in an arraylist of locations to store them for later
		for(int i = 0;i < this.VIEW_RANGE;i++) {
			for(int j = 0;j < this.VIEW_RANGE;j++) {
				if(view[j][i] instanceof Station) {
					Station station = (Station)view[j][i];
					if(getStation(station.getPoint()) == null) {
					Locations.add(new Location(station,station.getPoint(),j,i));
					}
				}else if(view[j][i] instanceof Well) {
					Well well = (Well)view[j][i];
					if(getWell(well.getPoint()) == null) {
					Locations.add(new Location(well,well.getPoint(),j+currentX,i+currentY));
					}
				}
			}
		}
		for(int i = 0;i<Locations.size();i++) {
			if(Locations.get(i).getFeature() instanceof Station) {
				Station station = (Station)Locations.get(i).getFeature();
				Task task = station.getTask();
				if(task != null) {
					AvailableTasks.add(task);
				}
			}
		}
	}
	
	

}
