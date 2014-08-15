/*
Floorguard.java

James Porter
*/

import java.awt.*;

public class Floorguard extends Item {
	public final int	MENU_FLAGS = 0b000010;
	private int deltx,
				delty;
	
	public Floorguard (Jned mind, int xpos, int ypos) {
		super(mind, 3, xpos, ypos);
		deltx = delty = 0;
		calcShape();
		setImage(ImageBank.FLOOR);
	}
	
	public int getFlags() {return MENU_FLAGS;}
	
	public Floorguard duplicate() {
		return new Floorguard(mind, getX(), getY());
	}
	
	public void calcShape() {
		int[] xs = new int[6], ys = new int[6];
		xs[0] = xs[5] = getX() + 6;
		xs[1] = xs[2] = getX() - 6;
		xs[3] = getX() - 2;
		xs[4] = getX() + 2;
		ys[0] = ys[1] = getY() + 6;
		ys[2] = ys[5] = getY() - 2;
		ys[3] = ys[4] = getY() - 6;
		setShape(xs,ys);
	}
	
	public String toString() {
		return super.toString() + ",1";
	}
	
	//Overrides Item - adjusts y position to be the center of cells + 6
	public void moveTo(int xpos, int ypos) {
		super.moveTo(xpos,24*(ypos/24)+18);
	}
	public void moveRelative(int xpos, int ypos) {		//Moves to a new position offset by the difference stored in deltax and deltay
		super.moveTo(xpos-deltx, 24*((ypos-6-delty)/24)+18);
	}
	public void setDelta(int xpos, int ypos) {			//Sets the offset (deltax/deltay) to the difference between current position and given position
		deltx = xpos-getX();
		delty = ypos-getY();
	}
	
	//Overrides Item
	public static void paintGhost(int xpos, int ypos, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		int[] xs = {xpos+6,xpos-6,xpos-6,xpos-2,xpos+2,xpos+6};
		int[] ys = {ypos+6,ypos+6,ypos-2,ypos-6,ypos-6,ypos-2};
		g.fillPolygon(xs,ys,6);
	}	
}