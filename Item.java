/*
Item.java
James Porter

A superclass for all items and enemies. Has information on type and position
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class Item {
	//N code type numbers for objects, in order of their Jned indices
	public static int[] typeCodes = {3,10,12,4,8,6,6,6,6,5,0,1,11,7,9,9,9,2};
	public final int	MENU_FLAGS = 0b000001;
	protected Jned mind;
	private int	xorig,
				yorig,
				deltax,
				deltay,
				type, //Corresponds to Jned mode index, not n code
				img;
	private boolean highlighted,
					selected;
	private Polygon shape;
	
	//Constructor
	public Item (Jned blown, int what, int xpos, int ypos) {
		mind = blown;
		xorig = xpos;
		yorig = ypos;
		deltax = deltay = 0;
		type = what;
		highlighted = false;
		selected = false;
		calcShape();
		img = -1;
	}
	
	public int getFlags() {return MENU_FLAGS;}
	
	public Item duplicate() {
		return new Item(mind, type, xorig, yorig);
	}
	
	//Tries to get image
	public void setImage(int imgind) {
		img = imgind;
	}
	public boolean hasImage() {
		return img >= 0;
	}
	public int getImage() {
		return img;
	}
	
	//Accessors/mutators for position/highlighting
	public int getX() {
		return xorig;
	}
	public int getY() {
		return yorig;
	}
	public int getType() {
		return type;
	}
	public void moveTo(int xpos, int ypos) {
		xorig = xpos;
		yorig = ypos;
		calcShape();
	}
	public void moveRelative(int xpos, int ypos) {		//Moves to a new position offset by the difference stored in deltax and deltay
		xorig = xpos-deltax;
		yorig = ypos-deltay;
		calcShape();
	}
	public void setDelta(int xpos, int ypos) {			//Sets the offset (deltax/deltay) to the difference between current position and given position
		deltax = xpos-xorig;
		deltay = ypos-yorig;
	}
	public boolean isHighlighted() {
		return highlighted;
	}
	public void setHighlight(boolean highlight) {
		highlighted = highlight;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelect(boolean select) {
		selected = select;
	}
	public Polygon getShape() {
		return shape;
	}
	public void calcShape() {
		int[] xs = {xorig-2,xorig+2,xorig+2,xorig-2};
		int[] ys = {yorig+2,yorig+2,yorig-2,yorig-2};
		setShape(xs,ys);
	}
	public void setShape(int[] xs, int[] ys) {
		shape = new Polygon(xs,ys,xs.length);
	}
	//Returns the n code for this object
	public String toString() {
		return Item.typeCodes[type] + "^" + xorig + "," + yorig;
	}
	
	//Checks to see if a given coordinate overlaps this item
	public boolean overlaps(int xpos, int ypos) {
		return shape.contains(xpos,ypos);
	}
	//Checks to see if a given rectangle overlaps this item
	public boolean overlaps(Rectangle rect) {
		return shape.intersects(rect);
	}
	
	//Paint methods
	public void paint(Graphics g) {paint(g, true);}
	public void paint(Graphics g, boolean drawPolygon) {
		boolean[] layer = {drawPolygon,highlighted,selected};
		for(int i = 0; i < 3; i++) {
			if(i==0) {
				if(mind.drawImage(img,xorig,yorig,g)) {
					layer[0] = false;
				} else {
					g.setColor(Jned.ITEM);
				}
			}
			if(i==1)g.setColor(Jned.ITEM_HL_A);
			if(i==2)g.setColor(Jned.ITEM_SELECT_A);
			if(layer[i] && shape != null) g.fillPolygon(shape);
		}
	}
	public void paintTrigger(Graphics g) {}
	public static void paintGhost(int what, int xpos, int ypos, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		int[] xs = {xpos-3,xpos+3,xpos+3,xpos-3};
		int[] ys = {ypos+3,ypos+3,ypos-3,ypos-3};
		g.fillPolygon(xs,ys,4);
	}	
}