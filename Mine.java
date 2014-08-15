/*
Mine.java

James Porter
*/

import java.awt.*;

public class Mine extends Item {
	public Mine (Jned mind, int xpos, int ypos) {
		super(mind, 2,xpos,ypos);
		setImage(ImageBank.MINE);
	}
	
	public Mine duplicate() {
		return new Mine(mind, getX(), getY());
	}
	
	public void calcShape() {
		int[] xs = {getX()-4,getX()+4,getX()+4,getX()-4};
		int[] ys = {getY()+4,getY()+4,getY()-4,getY()-4};
		setShape(xs,ys);
	}
	
	public static void paintGhost(int xpos, int ypos, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		int[] xs = {xpos-4,xpos+4,xpos+4,xpos-4};
		int[] ys = {ypos+4,ypos+4,ypos-4,ypos-4};
		g.fillPolygon(xs,ys,4);
	}
}