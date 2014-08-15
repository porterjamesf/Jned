/*
Chaingundrone.java

James Porter
*/

public class Chaingundrone extends Drone {
	public Chaingundrone (Jned mind, int xpos, int ypos, int direc, int behavior) {
		super(mind,8,xpos,ypos,direc,behavior);
		setImage(ImageBank.CHAINGUN);
	}
	
	public Chaingundrone duplicate() {
		return new Chaingundrone(mind, getX(), getY(), getDirection(), getBehavior());
	}
}