package uk.ac.nott.cs.g53dia.multiagent;

import uk.ac.nott.cs.g53dia.multilibrary.DefaultCell;
import uk.ac.nott.cs.g53dia.multilibrary.FuelPump;
import uk.ac.nott.cs.g53dia.multilibrary.Point;
import uk.ac.nott.cs.g53dia.multilibrary.Station;
import uk.ac.nott.cs.g53dia.multilibrary.Well;

public class Location {

	DefaultCell feature;
	Station station = null;
	Well well = null;
	FuelPump pump = null;
	boolean bExplored = false;
	int x,y;
	Point point;
	protected int id;
	boolean bTaskTaken = false;
	public Location(Station s, Point p,int j, int i,boolean taken,int value) {
		
		station = s;
		
		
		x = j;
		y = i;
		point = p;
		id = value;
		bTaskTaken = taken;
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
	
	public int getID() {
		return id;
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
	
	public boolean getExplored() {
		return bExplored;
	}
	
	public void setExplored(boolean check) {
		bExplored = check;
	}
	public void setTaken(boolean taken) {
		bTaskTaken = taken;
	}
}
