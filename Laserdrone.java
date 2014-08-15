/*
Laserdrone.java

James Porter
*/

public class Laserdrone extends Drone {
	public Laserdrone (Jned mind, int xpos, int ypos, int direc, int behavior) {
		super(mind,7,xpos,ypos,direc,behavior);
		//setImage(Jned.imagesPath + "enemies/Drones/LaserdroneA.gif",-9,-9);
		setImage(ImageBank.LASER);
	}
	
	public Laserdrone duplicate() {
		return new Laserdrone(mind, getX(), getY(), getDirection(), getBehavior());
	}
}