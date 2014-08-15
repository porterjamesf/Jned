/*
Seekerdrone.java

James Porter
*/

public class Seekerdrone extends Drone {
	public Seekerdrone (Jned mind, int xpos, int ypos, int direc, int behavior) {
		super(mind,6,xpos,ypos,direc,behavior);
		setImage(ImageBank.SEEKER);
	}
	
	public Seekerdrone duplicate() {
		return new Seekerdrone(mind, getX(), getY(), getDirection(), getBehavior());
	}
}