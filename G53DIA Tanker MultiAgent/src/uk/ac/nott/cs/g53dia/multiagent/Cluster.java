package uk.ac.nott.cs.g53dia.multiagent;

import java.util.ArrayList;

public class Cluster {

	
	ArrayList<Location> Stations = new ArrayList<Location>();
	Location nearestWell = null;
	double ClusterCentreX =0;
	double ClusterCentreY =0;
	public Cluster() {
		
	}
	
	public void AddLocation(Location loc) {
		Stations.add(loc);
		calculateCentre();
	}
	
	public void setWell(Location well) {
		nearestWell = well;
	}
	
	public int getSize() {
		return Stations.size();
	}
	
	void calculateCentre() {
		double tempx=0,tempy=0;
		for(int i =0;i<Stations.size();i++) {
			tempx+=Stations.get(i).x;
			tempy+=Stations.get(i).y;
		}
		ClusterCentreX = tempx/Stations.size();
		ClusterCentreY = tempy/Stations.size();
	}
	
	public Location getCentreLocation() {
		return new Location(null,null,(int)ClusterCentreX,(int)ClusterCentreY,false,0);
	}
}
