/*
Zapdrone.java

James Porter
*/

public class Zapdrone extends Drone {
	public Zapdrone (Jned mind, int xpos, int ypos, int direc, int behavior) {
		super(mind,5,xpos,ypos,direc,behavior);
		//setImage(Jned.imagesPath + "enemies/Drones/ZapdroneA.gif",-9,-9);
		setImage(ImageBank.ZAP);
	}
	
	public Zapdrone duplicate() {
		return new Zapdrone(mind, getX(), getY(), getDirection(), getBehavior());
	}
}