package uk.ac.nott.cs.g53dia.agent;

import uk.ac.nott.cs.g53dia.library.DefaultCell;
import uk.ac.nott.cs.g53dia.library.FuelPump;
import uk.ac.nott.cs.g53dia.library.Point;
import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Well;

public class Location {

	DefaultCell feature;
	Station station = null;
	Well well = null;
	FuelPump pump = null;
	int x,y;
	Point point;
	public Location(Station s, Point p,int j, int i) {
		
		station = s;
		
		
		x = j;
		y = i;
		point = p;
	}
	public Location(FuelPump s, Point p,int j, int i) {
	
		
		pump = s;
		
		x = j;
		y = i;
		point = p;
	}
	public Location(Well s, Point p,int j, int i) {
		
		
		well = s;
		
		
		x = j;
		y = i;
		point = p;
	}
	
	public Station getStation() {
		return station;
	}
	
	public Well getWell() {
		return well;
	}
	
	public FuelPump getPump() {
		return pump;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public Point getPoint() {
		return point;
	}
}
