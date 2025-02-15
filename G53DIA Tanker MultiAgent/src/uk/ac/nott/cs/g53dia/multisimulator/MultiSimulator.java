package uk.ac.nott.cs.g53dia.multisimulator;

import uk.ac.nott.cs.g53dia.multiagent.*;
import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.Random;

/**
 * An example of how to simulate execution of a fleet of tanker agents in the sample
 * (task) environment.
 * <p>
 * Creates a default {@link Environment}, a {@link DemoFleet} and a GUI window
 * (a {@link FleetViewer}) and executes the DemoFleet for DURATION days in the
 * environment.
 * 
 * @author Julian Zappala
 */

/*
 * Copyright (c) 2005 Neil Madden. Copyright (c) 2011 Julian Zappala
 * (jxz@cs.nott.ac.uk)
 * 
 * See the file "license.terms" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */

public class MultiSimulator {

	/**
	 * Time for which execution pauses so that GUI can update. Reducing this
	 * value causes the simulation to run faster.
	 */
	private static int DELAY = 0;

	/**
	 * Number of timesteps to execute.
	 */
	private static int DURATION = 10000;

    /**
     * Whether the action attempted at the last timestep failed.
     */
    private static boolean actionFailed = false;

    public static void main(String[] args) {
    	int total =0;
    	int waste = 0;
    	int runs = 30;
    	for(int i = 0;i<runs;i++) {

    	// Note: to obtain reproducible behaviour, you can set the Random seed
    	Random r = new Random(i);
    	// Create an environment
    	Environment env = new Environment(Tanker.MAX_FUEL/2, r);
    	// Create a fleet
    	Fleet fleet = new JoelFleet(r);
    	// Create a GUI window to show the fleet
    	//FleetViewer fv = new FleetViewer(fleet);
    	//fv.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    	// Start executing the tankers in the Fleet
    	while (env.getTimestep() < DURATION) {
    		// Advance the environment timestep
    		env.tick();
    		// Update the GUI
    		//fv.tick(env);
    		for (int j = 0;j<fleet.size();j++) {
    			Tanker t = fleet.get(j);
    			// Get the current view of the tanker
    			Cell[][] view = env.getView(t.getPosition(), Tanker.VIEW_RANGE);
    			// Let the tanker choose an action
    			Action act = t.senseAndAct(view, actionFailed, env.getTimestep());
    			// Try to execute the action
    			try {
    				actionFailed = act.execute(env, t);
    			} catch (OutOfFuelException ofe) {
    				System.err.println(ofe.getMessage());
    				System.exit(-1);
    			} catch (IllegalActionException afe) {
    				System.err.println(afe.getMessage());
    				actionFailed = false;
    			}
    			try {
    				Thread.sleep(DELAY);
    			} catch (Exception e) { }
    		}
    	}
    	System.out.println("Seed: "+i+" Simulation completed at timestep " + env.getTimestep() + " , score: " + fleet.getScore() + ", Total waste: " + fleet.getTotal());
		total+= fleet.getScore();
		waste += fleet.getTotal();
	}
	total = total/runs;
	waste = waste/runs;
	System.out.println("Average over "+runs+": " + total + ", Average waste: "+waste+ " loneliness 300");
    }
}

