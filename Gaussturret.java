/*
Gaussturret.java

James Porter
*/

public class Gaussturret extends Turret {
	public Gaussturret (Jned mind, int xpos, int ypos) {
		super(mind, 0,xpos,ypos);
		setImage(ImageBank.GAUSS);
	}
	
	public Gaussturret duplicate() {
		return new Gaussturret(mind, getX(), getY());
	}
}