/*
Door.java
James Porter

A subclass of DirectionalItem for all the doors.
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class SwitchDoor extends Door {
	private int	row,	//Coordinates of the cell this door flanks (0,0 is filled border in upper left)
				col,
				deltaRow,
				deltaCol,
				dtype; /*Type of door:	0 - Normal door
										1 - Locked door
										2 - Trap door	*/
	private Rectangle	door,	//Rectangle representing door
						swtch;	//Rectangle representing switch
	private boolean	overswitch,		//True when the overlap method is true for the swtch rectangle
					overdoor,		//True when the overlap method is true for the door rectangle
					doorselected,	//True when the door is selected; the selected variable inherited from Item is for the switch only.
					doorhighlighted;	//True when the door is highlighted; the highlighted variable inherited from Item is for the switch only.
	private BufferedImage[] imgs;		//Image array and coordinate offset array. There are 5 entries for a door: 0-3 are the door in each
	private int[]			imgOffsets;	//  direction, 4 is the switch. Image offsets are from the upper left corner of the cell for the door, center for the switch
	
	//Constructor
	public SwitchDoor (Jned mind, int what, int xpos, int ypos, int direc, int ro, int co) {
		super(mind,what,xpos,ypos,direc);
		row = ro;
		col = co;
		deltaRow = deltaCol = 0;
		dtype = what - 14;
		calcRect(true);
		calcRect(false);
		overswitch = overdoor = doorhighlighted = doorselected = false;
		mind.calcDronePaths();
	}
	
	public int getFlags() {return MENU_FLAGS;}
	
	public SwitchDoor duplicate() {
		return new SwitchDoor(mind, getType(), getSuperX(), getSuperY(), getDirection(), row, col);
	}
	
	//Calculates the rectangles
	public void calcRect(boolean isDoor) {
		if(isDoor) {
			door = new Rectangle(row*24+(getDirection()==0?22:0),col*24+(getDirection()==1?22:0),
				(getDirection()%2==0?2:24),(getDirection()%2==0?24:2));
		} else {
			swtch = new Rectangle(super.getX()-2,super.getY()-2,4,4);
		}
	}
	public void setRect(Rectangle square, boolean isDoor) {
		if(isDoor) {
			door = square;
		} else {
			swtch = square;
		}
	}
	
	//Overrides Item.getX: returns the door's position if it is overlapped
	public int getX() {
		if(overdoor) {
			switch (getDirection()) {
				default:
				case 0: return 24*(row + 1);
				case 2: return 24*row;
				case 1:
				case 3: return 24*row + 12;
			}
		}
		return super.getX();
	}
	public int getSuperX() {return super.getX();}
	public int getY() {
		if(overdoor) {
			switch (getDirection()) {
				default:
				case 0:
				case 2: return 24*col + 12;
				case 1: return 24*(col + 1);
				case 3: return 24*col;
			}
		}
		return super.getY();
	}
	public int getSuperY() {return super.getY();}
	
	public void setDirection(int direc) {
		super.setDirection(direc);
		calcRect(true);
	}
	
	//Accessor/mutator for row & column
	public int getRow() {
		return row;
	}
	public int getColumn() {
		return col;
	}
	
	//Overrides Item.setDelta: takes into account delta of switch vs. door
	public void setDelta(int xpos, int ypos) {
		super.setDelta(xpos,ypos);
		deltaRow = xpos-(24*row+12);
		deltaCol = ypos-(24*col+12);
	}
	
	//Overrides Item.moveTo: takes into account movement of switch vs. door
	//       HIJACKED FOR QUICK FIX -- this is only ever used for the item under the mouse, so overlap is used to decide whether to move door or switch
	public void moveTo(int xpos, int ypos) {
		if(overdoor) {
			row = xpos/24;
			col = ypos/24;
			calcRect(true);
			mind.calcDronePaths();
		} else {
			super.moveTo(xpos,ypos);
			calcRect(false);
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
			calcRect(false);
		}
		if(force || doorselected) {
			row = (xpos-deltaRow)/24;
			col = (ypos-deltaCol)/24;
			calcRect(true);
			mind.calcDronePaths();
		}
	}
	
	public String toString() {
		String res = super.toString();
		return res.substring(0,res.length()-1) + (getDirection()%2==1?1:0) + "," + (dtype==2?1:0) + "," + row + "," + col + "," + 
			(dtype==1?1:0) + "," + (getDirection()==2?-1:0) + "," + (getDirection()==3?-1:0);
	}
	
	//Checks to see if a given coordinate overlaps this item
	public boolean overlaps(int xpos, int ypos) {
		overswitch = swtch.contains(xpos,ypos);
		overdoor = overlapsDoor(xpos,ypos);
		return overswitch || overdoor;
	}
	//Checks to see if a given rectangle overlaps this item
	public boolean overlaps(Rectangle rect) {
		overswitch = swtch.intersects(rect);
		overdoor = overlapsDoor(rect);
		return overswitch || overdoor;
	}
	public boolean overlapsDoor(int xpos, int ypos) {
		return door.contains(xpos,ypos);
	}
	public boolean overlapsDoor(Rectangle rect) {
		return door.intersects(rect);
	}
	
	//Overrides Item.setSelect: takes into account selection of switch vs. door
	public void setSelect(boolean select, boolean force) {
		if(select == false) {
			super.setSelect(false);
			doorselected = false;
		} else {
			if(overdoor || force) doorselected = true;
			if(overswitch || force) super.setSelect(true);
		}
		mind.calcDronePaths();
	}
	public void setSelect(boolean select) {
		setSelect(select, false);
	}
	//Overrides Item.isSelected: only returns true if the overlapped components are selected
	//  Used by the LevelArea method that checks to see if the item being clicked on is part of the selection or not (mousePressed)
	public boolean isSelected() {
		if(overdoor) {
			return doorselected;
		}
		return super.isSelected();
	}
	public boolean isAnySelected() {
		return doorselected || super.isSelected();
	}
	//Overrides Item.setHighlight: takes into account highlighting of switch vs. door
	public void setHighlight(boolean highlight) {
		if(highlight == false) {
			super.setHighlight(false);
			doorhighlighted = false;
		} else {
			if(overdoor) doorhighlighted = true;
			if(overswitch) super.setHighlight(true);
		}
	}
	
	//Paint methods
	public void paint(Graphics g) {
		//Door
		boolean[] layer = {true,doorhighlighted,doorselected};
		int d = getDirection();
		for(int i = 0; i < 3; i++) {
			if(i==0) {
				if(mind.drawImage(getImage(true), 24*row, 24*col, g)) {
					layer[0] = false;
				} else {
					g.setColor(Jned.ITEM);
				}
			}
			if(i==1)g.setColor(Jned.ITEM_HL_A);
			if(i==2)g.setColor(Jned.ITEM_SELECT_A);
			if(layer[i]) paintDoor(g);
		}
		//Switch
		layer[0] = true;
		layer[1] = isHighlighted();
		layer[2] = super.isSelected();
		for(int i = 0; i < 3; i++) {
			if(i==0) {
				if(mind.drawImage(getImage()+4, getSuperX(), getSuperY(), g)) {
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
	public void paintDoor(Graphics g) {
		g.fillRect(door.x,door.y,door.width,door.height);
	}
	public void paintTrigger(Graphics g) {
		g.setColor(Jned.DOOR_TRIGGER);
		g.drawLine(row*24+(24*Math.abs(2-getDirection()))/2,col*24+(2-Math.abs(1-getDirection()))*12,super.getX(),super.getY());
	}
	public static void paintSwitchGhost(int what, int xpos, int ypos, int direc, int ro, int co, Graphics g) { //Filler; always overided
		/*g.setColor(Jned.ITEM_HL_A);
		//Door
		g.fillRect(ro*24+(direc==0?22:0),co*24+(direc==1?22:0),
			(direc%2==0?2:24),(direc%2==0?24:2));
		g.setColor(Jned.ITEM_GHOST);
		//Switch
		g.fillRect(xpos-2,ypos-2,4,4);
		//Trigger line
		g.setColor(Jned.DOOR_TRIGGER);
		g.drawLine(ro*24+(24*Math.abs(2-direc))/2,co*24+(2-Math.abs(1-direc))*12,xpos,ypos);*/
	}
	public static void paintDoorGhost(int what, int direc, int ro, int co, Graphics g) { //Filler; always overided
		/*g.setColor(Jned.ITEM_GHOST);
		g.fillRect(ro*24+(direc==0?22:0),co*24+(direc==1?22:0),
			(direc%2==0?2:24),(direc%2==0?24:2));*/
	}
}