/*
Turret.java

James Porter
*/

import java.awt.*;

public class Turret extends Item {
	public static final int rad = 6;
	
	public Turret (Jned mind, int type, int xpos, int ypos) {
		super(mind, type,xpos,ypos);
	}
	
	public Turret duplicate() {
		return new Turret(mind, getType(), getX(), getY());
	}

	//Checks to see if a given coordinate overlaps this turret
	public boolean overlaps(int xpos, int ypos) {
		return Math.hypot(xpos - getX(), ypos - getY()) < Turret.rad;
	}
	//Checks to see if a given rectangle overlaps this turret
	public boolean overlaps(Rectangle rect) {
		int nx = rect.x + rect.width, ny = rect.y + rect.height;
		if(getX() <= rect.x) {
			nx = rect.x;
		} else {
			if(getX() <= nx) {
				nx = getX();
			}
		}
		if(getY() <= rect.y) {
			ny = rect.y;
		} else {
			if(getY() <= ny) {
				ny = getY();
			}
		}
		return Math.hypot(nx-getX(),ny-getY()) < Turret.rad;
	}
	
	//Paint methods
	public void paint(Graphics g) {
		boolean[] layer = {true,isHighlighted(),isSelected()};
		for(int i = 0; i < 3; i++) {
			if(i==0) {
				if(mind.drawImage(getImage(),getX(),getY(),g)) {
					layer[0] = false;
				} else {
					g.setColor(Jned.ITEM);
				}
			}
			if(i==1)g.setColor(Jned.ITEM_HL_A);
			if(i==2)g.setColor(Jned.ITEM_SELECT_A);
			if(layer[i]) {
				g.fillOval(getX()-Turret.rad,getY()-Turret.rad,2*Turret.rad,2*Turret.rad);
			}
		}
	}
	public static void paintGhost(int xpos, int ypos, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		g.fillOval(xpos-Turret.rad,ypos-Turret.rad,2*Turret.rad,2*Turret.rad);
	}
}