/*
Bounceblock.java

James Porter
*/

import java.awt.*;

public class Bounceblock extends Item {
	public Bounceblock (Jned mind, int xpos, int ypos) {
		super(mind, 11,xpos,ypos);
		setImage(ImageBank.BOUNCE);
	}
	
	public Bounceblock duplicate() {
		return new Bounceblock(mind, getX(), getY());
	}
	
	public void calcShape() {
		int[] xs = {getX()-10,getX()+10,getX()+10,getX()-10};
		int[] ys = {getY()+10,getY()+10,getY()-10,getY()-10};
		setShape(xs,ys);
	}
	
	public static void paintGhost(int xpos, int ypos, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		int[] xs = {xpos-10,xpos+10,xpos+10,xpos-10};
		int[] ys = {ypos+10,ypos+10,ypos-10,ypos-10};
		g.fillPolygon(xs,ys,4);
	}
}