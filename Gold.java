/*
Gold.java

James Porter
*/

import java.awt.*;

public class Gold extends Item {
	public Gold (Jned mind, int xpos, int ypos) {
		super(mind, 10,xpos,ypos);
		setImage(ImageBank.GOLD);
	}
	
	public Gold duplicate() {
		return new Gold(mind, getX(), getY());
	}
	
	public void calcShape() {
		int[] xs = {getX()-3,getX()+4,getX()+4,getX()-3};
		int[] ys = {getY()+4,getY()+4,getY()-3,getY()-3};
		setShape(xs,ys);
	}
	
	public static void paintGhost(int xpos, int ypos, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		int[] xs = {xpos-3,xpos+4,xpos+4,xpos-3};
		int[] ys = {ypos+4,ypos+4,ypos-3,ypos-3};
		g.fillPolygon(xs,ys,4);
	}
}