/*
Thwump.java

James Porter
*/

import java.awt.*;

public class Thwump extends DirectionalItem {
	public Thwump (Jned mind, int xpos, int ypos, int direc) {
		super(mind,4,xpos,ypos,direc);
		int[]	xs = {-10,-10,-10,-10},
				ys = {-10,-10,-10,-10};
		setImage(ImageBank.THWUMP);
	}
	
	public Thwump duplicate() {
		return new Thwump(mind, getX(), getY(), getDirection());
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