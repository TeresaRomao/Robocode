package man;

import robocode.ScannedRobotEvent;

public class Enemy {
	
	double bearing;
	double distance;
	double energy;
	double heading;
	double velocity;
	String name;
	
	public double getBearing(){
		return bearing;		
	}
	public double getDistance(){
		return distance;
	}
	public double getEnergy(){
		return energy;
	}
	public double getHeading(){
		return heading;
	}
	public String getName(){
		return name;
	}
	public double getVelocity(){
		return velocity;
	}
	public void update(ScannedRobotEvent e){
		bearing = e.getBearing();
		distance = e.getDistance();
		energy = e.getEnergy();
		heading = e.getHeading();
		velocity = e.getVelocity();
		name = e.getName();
	}
	public void reset(){
		bearing = 0.0;
		distance =0.0;
		energy= 0.0;
		heading =0.0;
		velocity = 0.0;
		name = null;
	}
	
	public Boolean none(){
		if (name == null || name == "")
			return true;
		else
			return false;
	}
	
	public Enemy(){
		reset();
	}

}
