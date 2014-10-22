/*
Launchpad.java
James Porter

A subclass of Item for launchpads.
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class Launchpad extends Item {
	public final int	MENU_FLAGS = 0b100101;
	private double	xpow,	//Launching power in x and y dimensions. Determines the direction.
					ypow;
	private Polygon box;	//Rectangular bounding box of this launchpad
	
	public Launchpad (Jned mind, int xpos, int ypos, double powerx, double powery) {
		super(mind,17,xpos,ypos);
		xpow = powerx;
		ypow = powery;
		calcRect();
		setImage(ImageBank.LAUNCH);
	}
	
	public int getFlags() {return MENU_FLAGS;}
	
	public Launchpad duplicate() {
		return new Launchpad(mind, getX(), getY(), xpow, ypow);
	}
	
	public Launchpad (Jned mind, int xpos, int ypos, int dir){//Takes 1 number for direction
		super(mind,17,xpos,ypos);
		double r2 = Math.sqrt(2)/2;
		switch(dir) {
			default:
			case 0:
				xpow = 1.0;
				ypow = 0.0; 
			break;	
			case 1:
				xpow = r2;
				ypow = r2;
			break;
			case 2:
				xpow = 0.0;
				ypow = 1.0;
			break;
			case 3:
				xpow = -r2;
				ypow = r2;
			break;
			case 4:
				xpow = -1.0;
				ypow = 0.0;
			break;
			case 5:
				xpow = -r2;
				ypow = -r2;
			break;
			case 6:
				xpow = 0.0;
				ypow = -1.0;
			break;
			case 7:
				xpow = r2;
				ypow = -r2;
			break;
		}
		calcRect();
		setImage(ImageBank.LAUNCH);
	}
	
	private void calcRect() {//Calculates the coordinates of the bounding box
		//Finds which way to point the pad
		int dir;
		if(xpow==0.0) {
			if(ypow > 0.0) {
				dir = 2;
			} else {
				dir = 6;
			}
		} else {
			if(xpow > 0.0) {
				if(ypow==0.0) {
					dir = 0;
				} else {
					if(ypow > 0.0) {
						dir = 1;
					} else {
						dir = 7;
					}
				}
			} else {
				if(ypow==0.0) {
					dir = 4;
				} else {
					if(ypow > 0.0) {
						dir = 3;
					} else {
						dir = 5;
					}
				}
			}
		}
		int[] xs = new int[4];
		int[] ys = new int[4];
		switch(dir) {
			case 0:
			case 4:
				xs[0]=xs[3]= getX();
				xs[1]=xs[2]= getX() + (dir==0?5:-5);
				ys[0]=ys[1]= getY() - 7;
				ys[2]=ys[3]= getY() + 7;
			break;
			case 2:
			case 6:
				xs[0]=xs[1]= getX() - 7;
				xs[2]=xs[3]= getX() + 7;
				ys[0]=ys[3]= getY();
				ys[1]=ys[2]= getY() + (dir==2?5:-5);
			break;
			case 1:
			case 3:
			case 5:
			case 7:
				xs[0]= getX() + (dir%6==1?5:-5);
				ys[0]= getY() + (dir<4?-5:5);
				xs[1]= getX() + (dir%6==1?9:-9);
				ys[1]= getY() + (dir<4?-1:1);
				xs[2]= getX() + (dir%6==1?-1:1);
				ys[2]= getY() + (dir<4?9:-9);
				xs[3]= getX() + (dir%6==1?-5:5);
				ys[3]= getY() + (dir<4?5:-5);
			break;
		}
		box = new Polygon(xs,ys,4);
	}
	
	//Accessors/mutators
	public double getPowerX() {
		return xpow;
	}
	public double getPowerY() {
		return ypow;
	}
	public double getPower() {//Returns the vector magnitude of the power
		return Math.hypot(xpow,ypow);
	}
	public double getDirection() {//Returns the vector direction
		return Math.atan2(ypow,xpow);
	}
	public int getDirInt() { //Returns an int from 0 to 7 for the 45 degree direction value 
		if(xpow == 0.0) {
			if(ypow < 0.0) return 6;
			return 2;
		}
		if(ypow == 0.0) {
			if(xpow < 0.0) return 4;
			return 0;
		}
		if(xpow < 0.0) {
			if(ypow < 0.0) return 5;
			return 3;
		}
		if(ypow < 0.0) return 7;
		return 1;
	}
	public void setPowerX(double pow) {
		xpow = pow;
		calcRect();
	}
	public void setPowerY(double pow) {
		ypow = pow;
		calcRect();
	}
	public void setPower(double pow) { //Adjusts the power without changing the direction at all
		double theta = Math.atan2(ypow,xpow);
		xpow = Math.cos(theta)*pow;
		ypow = Math.sin(theta)*pow;
		if(Math.abs(xpow) < 0.00000000000001) xpow = 0.0;
		if(Math.abs(ypow) < 0.00000000000001) ypow = 0.0;
		calcRect();
	}
	public void setDirection(double theta) { //Adjusts the direction without changing the power at all
		double power = Math.hypot(ypow,xpow);
		xpow = Math.cos(theta)*power;
		ypow = Math.sin(theta)*power;
		if(Math.abs(xpow) < 0.00000000000001) xpow = 0.0;
		if(Math.abs(ypow) < 0.00000000000001) ypow = 0.0;
		calcRect();
	}
	public void moveTo(int xpos, int ypos) {
		super.moveTo(xpos,ypos);
		calcRect();
	}
	public void moveRelative(int xpos, int ypos) {
		super.moveRelative(xpos,ypos);
		calcRect();
	}
	
	//Returns the n code for this object
	public String toString() {
		return super.toString() + "," + format(xpow) + "," + format(ypow);
	}
	private String format(double num) {
		if((double)((int)num)==num) return "" + ((int)num);
		String res = String.valueOf(Math.abs(num));
		if(res.length() > 17) res = res.substring(0,17);
		return (num<0?"-":"") + res;
	}
	
	//Checks to see if a given coordinate overlaps this item
	public boolean overlaps(int xpos, int ypos) {
		return box.contains(xpos,ypos);
	}
	//Checks to see if a given rectangle overlaps this item
	public boolean overlaps(Rectangle rect) {
		return box.intersects(rect);
	}
	
	//Paint methods
	public void paint(Graphics g) {
		boolean[] layer = {true,isHighlighted(),isSelected()};
		for(int i = 0; i < 3; i++) {
			if(i==0) {
				int d = getDirInt();
				if(mind.drawImage(getImage()+d,getX(),getY(),g)) {
					layer[0] = false;
				} else {
					g.setColor(Jned.ITEM);
				}
			}
			if(i==1)g.setColor(Jned.ITEM_HL_A);
			if(i==2)g.setColor(Jned.ITEM_SELECT_A);
			if(layer[i]) {
				g.fillPolygon(box);
			}
		}
	}
	public void paintLine(Graphics g) {
		g.setColor(Jned.LAUNCHPAD_LINE);
		g.drawLine(getX(),getY(),getX()+(int)(xpow*24),getY()+(int)(ypow*24));
	}
	public static void paintGhost(int xpos, int ypos, int dir, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		int[] xs = new int[4];
		int[] ys = new int[4];
		switch(dir) {
			case 0:
			case 4:
				xs[0]=xs[3]= xpos;
				xs[1]=xs[2]= xpos + (dir==0?5:-5);
				ys[0]=ys[1]= ypos - 7;
				ys[2]=ys[3]= ypos + 7;
			break;
			case 2:
			case 6:
				xs[0]=xs[1]= xpos - 7;
				xs[2]=xs[3]= xpos + 7;
				ys[0]=ys[3]= ypos;
				ys[1]=ys[2]= ypos + (dir==2?5:-5);
			break;
			case 1:
			case 3:
			case 5:
			case 7:
				xs[0]= xpos + (dir%6==1?5:-5);
				ys[0]= ypos + (dir<4?-5:5);
				xs[1]= xpos + (dir%6==1?9:-9);
				ys[1]= ypos + (dir<4?-1:1);
				xs[2]= xpos + (dir%6==1?-1:1);
				ys[2]= ypos + (dir<4?9:-9);
				xs[3]= xpos + (dir%6==1?-5:5);
				ys[3]= ypos + (dir<4?5:-5);
			break;
		}
		g.fillPolygon(xs,ys,4);
	}	
}