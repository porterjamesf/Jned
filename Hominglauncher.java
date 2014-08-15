/*
Hominglauncher.java

James Porter
*/

public class Hominglauncher extends Turret {
	public Hominglauncher (Jned mind, int xpos, int ypos) {
		super(mind, 1,xpos,ypos);
		setImage(ImageBank.HOMING);
	}
	
	public Hominglauncher duplicate() {
		return new Hominglauncher(mind, getX(), getY());
	}
}