package uk.ac.nott.cs.g53dia.agent;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Tanker;
import uk.ac.nott.cs.g53dia.library.Task;

public class JoelTanker extends Tanker{

	Task currentTask = null;
	
	@Override
	public Action senseAndAct(Cell[][] view, boolean actionFailed, long timestep) {
		// TODO Auto-generated method stub
		for(int i = 0;i < this.VIEW_RANGE;i++) {
			for(int j = 0;j < this.VIEW_RANGE;j++) {
				if(view[j][i] instanceof Station) {
					
				}
			}
		}
		if(getCurrentCell(view) instanceof Station ) {
			Station currentStation = (Station) getCurrentCell(view);
			currentTask = currentStation.getTask();
			
		}
		return null;
	}

}
