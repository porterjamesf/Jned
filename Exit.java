/*
Exit.java
James Porter

A subclass of Item for the exit door and switch.
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class Exit extends Item {
	private int	xsw,	//Coordinates of the door switch
				ysw,
				deltaxsw,
				deltaysw;
	private Rectangle	door,	//Rectangle representing door
						swtch;	//Rectangel representing switch
	private boolean	overswitch,		//True when the overlap method is true for the swtch rectangle
					overdoor,		//True when the overlap method is true for the door rectangle
					switchselected,	//True when the switch is selected; the selected variable inherited from Item is for the door only.
					switchhighlighted;	//True when the switch is highlighted; the highlighted variable inherited from Item is for the door only.
					
	//Constructor
	public Exit (Jned mind, int xpos, int ypos, int switchx, int switchy) {
		super(mind, 12,xpos,ypos);
		xsw = switchx;
		ysw = switchy;
		deltaxsw = deltaysw = 0;
		overswitch = overdoor = switchselected = switchhighlighted = false;
		
		calcRect(true);
		calcRect(false);
		
		setImage(ImageBank.EXIT);
	}
	
	public Exit duplicate() {
		return new Exit(mind, getX(), getY(), xsw, ysw);
	}
	
	//Calculates the rectangles
	private void calcRect(boolean isDoor) {
		if(isDoor) {
			door = new Rectangle(super.getX()-13,super.getY()-12,26,24);
		} else {
			swtch = new Rectangle(xsw-6,ysw-4,13,7);
		}
	}
	
	//Overrides getZ in Item: returns whether the switch's position is overlapped
	public int getX() {
		if(overswitch) {
			return xsw;
		}
		return super.getX();
	}
	public int getY() {
		if(overswitch) {
			return ysw;
		}
		return super.getY();
	}
	public int getSuperX() {
		return super.getX();
	}
	public int getSuperY() {
		return super.getY();
	}
	
	//Accessors/mutators for switch position
	public int getSwitchX() {
		return xsw;
	}
	public int getSwitchY() {
		return ysw;
	}
	
	//Overrides Item.setDelta: takes into account delta of switch vs. door
	public void setDelta(int xpos, int ypos) {
		super.setDelta(xpos,ypos);
		deltaxsw = xpos-xsw;
		deltaysw = ypos-ysw;
	}
	//Overrides Item.moveTo: takes into account movement of switch vs. door
	public void moveTo(int xpos, int ypos) {
		if(overswitch) {
			xsw = xpos;
			ysw = ypos;
			calcRect(false);
		} else {
			super.moveTo(xpos,ypos);
			calcRect(true);
		}
	}
	//Overrides Item.moveRelative: takes into account movement of switch vs. door, using the appropriate set of delta values
	// Normally will only move parts that are selected (for drag operations). Alternately, you can use the force parameter to 
	// make sure both parts move whether or not they are selected (used for pasting).
	public void moveRelative(int xpos, int ypos) {
		moveRelative(xpos, ypos, false);
	}
	public void moveRelative(int xpos, int ypos, boolean force) {
		if(force || super.isSelected()) {
			super.moveRelative(xpos,ypos);
			calcRect(true);
		}
		if(force || switchselected) {
			xsw = xpos - deltaxsw;
			ysw = ypos - deltaysw;
			calcRect(false);
		}
	}
	
	//Returns the n code for this object
	public String toString() {
		return super.toString() + "," + xsw + "," + ysw;
	}
	
	//Checks to see if a given coordinate overlaps this item
	public boolean overlaps(int xpos, int ypos) {
		if(swtch.contains(xpos,ypos)) {
			overswitch = true;
		} else {
			overswitch = false;
		}
		if(door.contains(xpos,ypos)) {
			overdoor = true;
		} else {
			overdoor = false;
		}
		return overswitch || overdoor;
	}
	//Checks to see if a given rectangle overlaps this item
	public boolean overlaps(Rectangle rect) {
		if(door.intersects(rect)) {
			overdoor = true;
		} else {
			overdoor = false;
		}
		if(swtch.intersects(rect)) {
			overswitch = true;
		} else {
			overswitch = false;
		}
		return overswitch || overdoor;
	}
	
	//Overrides Item.setSelect: takes into account selection of switch vs. door
	public void setSelect(boolean select, boolean force) {
		if(select == false) {
			super.setSelect(false);
			switchselected = false;
		} else {
			if(overswitch || force) switchselected = true;
			if(overdoor || force) super.setSelect(true);
		}
	}
	public void setSelect(boolean select) {
		setSelect(select, false);
	}
	//Overrides Item.isSelected: only returns true if the overlapped components are selected
	//  Used by the LevelArea method that checks to see if the item being clicked on is part of the selection or not (mousePressed)
	public boolean isSelected() {
		if(overswitch) {
			return switchselected;
		}
		return super.isSelected();
	}
	//Overrides Item.setHighlight: takes into account highlighting of switch vs. door
	public void setHighlight(boolean highlight) {
		if(highlight == false) {
			super.setHighlight(false);
			switchhighlighted = false;
		} else {
			if(overswitch) switchhighlighted = true;
			if(overdoor) super.setHighlight(true);
		}
	}
	
	//Paint methods
	public void paint(Graphics g) {
		//Door
		boolean[] layer = {true,isHighlighted(),super.isSelected()};
		for(int i = 0; i < 3; i++) {
			if(i==0) {
				if(mind.drawImage(getImage(), super.getX(), super.getY(), g)) {
					layer[0] = false;
				} else {
					g.setColor(Jned.ITEM);
				}
			}
			if(i==1)g.setColor(Jned.ITEM_HL_A);
			if(i==2)g.setColor(Jned.ITEM_SELECT_A);
			if(layer[i]) g.fillRect(door.x,door.y,door.width,door.height);
		}
		//Switch
		layer[0] = true;
		layer[1] = switchhighlighted;
		layer[2] = switchselected;
		for(int i = 0; i < 3; i++) {
			if(i==0) {
				if(mind.drawImage(getImage()+1, xsw, ysw, g)) {
					layer[0] = false;
				} else {
					g.setColor(Jned.ITEM);
				}
			}
			if(i==1)g.setColor(Jned.ITEM_HL_A);
			if(i==2)g.setColor(Jned.ITEM_SELECT_A);
			if(layer[i]) g.fillRect(swtch.x,swtch.y,swtch.width,swtch.height);
		}
	}
	public void paintTrigger(Graphics g) {
		g.setColor(Jned.DOOR_TRIGGER);
		g.drawLine(super.getX(),super.getY(),xsw,ysw);
	}
	public static void paintSwitchGhost(int xpos, int ypos, int switchx, int switchy, Graphics g) {
		//Door
		Exit.paintDoorGhost(xpos,ypos,g);
		//Switch
		g.setColor(Jned.ITEM_GHOST);
		g.fillRect(switchx-6,switchy-4,13,7);
		//Trigger line
		g.setColor(Jned.DOOR_TRIGGER);
		g.drawLine(xpos,ypos,switchx,switchy);
	}
	public static void paintDoorGhost(int xpos, int ypos, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		g.fillRect(xpos-13,ypos-12,26,24);
	}
}