package uk.ac.nott.cs.g53dia.agent;

import uk.ac.nott.cs.g53dia.library.DefaultCell;
import uk.ac.nott.cs.g53dia.library.FuelPump;
import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Well;

public class Location {

	DefaultCell feature;
	int x,y;
	public Location(DefaultCell s, int posX, int posY) {
		if(s instanceof Station){
			feature = (Station)s;
		}
		if(s instanceof Well) {
			feature = (Well)s;
		}
		if(s instanceof FuelPump) {
			feature = (FuelPump)s;
		}
		x = posX;
		y = posY;
	}
	
	public DefaultCell getFeature() {
		return feature;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
}
