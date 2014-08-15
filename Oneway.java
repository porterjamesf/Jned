/*
Oneway.java

James Porter
*/

import java.awt.*;

public class Oneway extends DirectionalItem {
	public Oneway (Jned mind, int xpos, int ypos, int direc) {
		super(mind, 13,xpos,ypos,direc);
		setImage(ImageBank.ONEWAY);
	}
	
	public Oneway duplicate() {
		return new Oneway(mind, getX(), getY(), getDirection());
	}
	
	public void calcShape() {
		int d = getDirection(), x = getX(), y = getY();
		int[] xs = {	x+(d>1?-7:7),
						x+(d>1?-12:12),
						x+(d%3==0?12:-12),
						x+(d%3==0?7:-7)};
		int[] ys = {	y+(d%3==0?-7:7),
						y+(d%3==0?-12:12),
						y+(d>1?-12:12),
						y+(d>1?-7:7)};
		setShape(xs,ys);
	}
	
	public static void paintGhost(int xpos, int ypos, int dir, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		int[] xs = {	xpos+(dir>1?-7:7),
						xpos+(dir>1?-12:12),
						xpos+(dir%3==0?12:-12),
						xpos+(dir%3==0?7:-7)};
		int[] ys = {	ypos+(dir%3==0?-7:7),
						ypos+(dir%3==0?-12:12),
						ypos+(dir>1?-12:12),
						ypos+(dir>1?-7:7)};
		g.fillPolygon(xs,ys,4);
	}
}