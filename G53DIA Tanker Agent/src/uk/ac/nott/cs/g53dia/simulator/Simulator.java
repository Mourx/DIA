package uk.ac.nott.cs.g53dia.simulator;

import uk.ac.nott.cs.g53dia.agent.*;
import uk.ac.nott.cs.g53dia.library.*;
import java.util.Random;

/**
 * An example of how to simulate execution of a tanker agent in the sample
 * (task) environment.
 * <p>
 * Creates a default {@link Environment}, a {@link DemoTanker} and a GUI window
 * (a {@link TankerViewer}) and executes the Tanker for DURATION days in the
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

public class Simulator {

	/**
	 * Time for which execution pauses so that GUI can update. Reducing this
	 * value causes the simulation to run faster.
	 */
	private static int DELAY = 00;

	/**
	 * Number of timesteps to execute.
	 */
	private static int DURATION = 10000;

    /**
     * Whether the action attempted at the last timestep failed.
     */
    private static boolean actionFailed = false;

	public static void main(String[] args) {
		double total = 0;
		for(int i = 0;i<400;i++) {
			// Note: to obtain reproducible behaviour, you can set the Random seed
			Random r = new Random();
			// Create an environment
			Environment env = new Environment(Tanker.MAX_FUEL/2, r);
			// Create a tanker
			//Tanker tank = new DemoTanker(r);
			Tanker tank = new JoelTanker(r);
			// Create a GUI window to show the tanker
			//TankerViewer tv = new TankerViewer(tank);
			//tv.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
			// Start executing the Tanker
			while (env.getTimestep() < DURATION) {
				// Advance the environment timestep
				env.tick();
				//System.out.println(" at timestep " + env.getTimestep() + " , score: " + tank.getScore());
				// Update the GUI
				//tv.tick(env);
				// Get the current view of the tanker
				Cell[][] view = env.getView(tank.getPosition(), Tanker.VIEW_RANGE);
				// Let the tanker choose an action
				Action act = tank.senseAndAct(view, actionFailed, env.getTimestep());
				// Try to execute the action
				try {
					actionFailed = act.execute(env, tank);
				} catch (OutOfFuelException ofe) {
					System.err.println(ofe.getMessage());
					System.exit(-1);
				} catch (IllegalActionException afe) {
					System.err.println(afe.getMessage());
					actionFailed = false;
				}
				try {
					Thread.sleep(DELAY);
				} catch (Exception e) {
				}
			}
			System.out.println("Seed: "+i+" Simulation completed at timestep " + env.getTimestep() + " , score: " + tank.getScore());
			total+= tank.getScore();
		}
		total = total/400;
		System.out.println("Average over 30: " + total);
	}
}
