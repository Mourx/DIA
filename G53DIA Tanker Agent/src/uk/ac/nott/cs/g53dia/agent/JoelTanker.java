package uk.ac.nott.cs.g53dia.agent;

import java.util.ArrayList;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.Point;
import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Tanker;
import uk.ac.nott.cs.g53dia.library.Task;
import uk.ac.nott.cs.g53dia.library.Well;

public class JoelTanker extends Tanker{

	Task currentTask = null;
	ArrayList<Station> NearbyStations;
	ArrayList<Well> NearbyWells;
	ArrayList<Task> AvailableTasks;
	ArrayList<Location> Locations;
	int currentX,currentY;
	@Override
	public Action senseAndAct(Cell[][] view, boolean actionFailed, long timestep) {
		// TODO Auto-generated method stub
		Point currentPoint = this.getPosition();
		for(int i = 0;i < this.VIEW_RANGE;i++) {
			for(int j = 0;j < this.VIEW_RANGE;j++) {
				if(view[j][i] instanceof Station) {
					Locations.add(new Location((Station)view[j][i],10,10));
				}else if(view[j][i] instanceof Well) {
					NearbyWells.add((Well) view[j][i]);
				}
			}
		}
		for(int i = 0;i<NearbyStations.size();i++) {
			Task task = NearbyStations.get(i).getTask();
			if(task != null) {
				AvailableTasks.add(NearbyStations.get(i).getTask());
			}
		}
		for(int i = 0;i<AvailableTasks.size();i++) {
			Task task = AvailableTasks.get(i);

		}
		if(getCurrentCell(view) instanceof Station ) {
			Station currentStation = (Station) getCurrentCell(view);
			currentTask = currentStation.getTask();
			
		}
		return null;
	}

}
