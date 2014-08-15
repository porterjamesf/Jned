/*
Lockeddoor.java
James Porter
*/

import java.awt.*;

public class Lockeddoor extends SwitchDoor {
	private Rectangle knob;
	
	public Lockeddoor (Jned mind, int xpos, int ypos, int direc, int ro, int co) {
		super(mind,15,xpos,ypos,direc,ro,co);
		calcRect(true);
		setImage(ImageBank.LDOOR);
	}
	
	public Lockeddoor duplicate() {
		return new Lockeddoor(mind, getSuperX(), getSuperY(), getDirection(), getRow(), getColumn());
	}
	
	public void calcRect(boolean isDoor) {
		if(isDoor) {
			int x = 24*getRow(), y = 24*getColumn();
			switch(getDirection()) {
				case 0:
					setRect(new Rectangle(x+22,y,3,24),true);
					knob = new Rectangle(x+21,y+6,5,12);
				break;
				case 1:
					setRect(new Rectangle(x,y+22,24,3),true);
					knob = new Rectangle(x+6,y+21,12,5);
				break;
				case 2:
					setRect(new Rectangle(x,y,2,24),true);
					knob = new Rectangle(x-1,y+6,5,12);
				break;
				case 3:
					setRect(new Rectangle(x,y,24,2),true);
					knob = new Rectangle(x+6,y-1,12,5);
				break;
			}
		} else {
			setRect(new Rectangle(super.getSuperX()-4,super.getSuperY()-4,8,8),false);
		}
	}
	
	public boolean overlapsDoor(int xpos, int ypos) {
		return knob.contains(xpos, ypos) || super.overlapsDoor(xpos,ypos);
	}
	public boolean overlapsDoor(Rectangle rect) {
		return knob.intersects(rect) || super.overlapsDoor(rect);
	}
	
	public void paintDoor(Graphics g) {
		super.paintDoor(g);
		
		if(getDirection()%2==0) {//Vertical
			g.drawLine(knob.x,knob.y,knob.x,knob.y+knob.height-1);
			g.drawLine(knob.x+knob.width-1,knob.y,knob.x+knob.width-1,knob.y+knob.height-1);
			if(getDirection()>1) g.drawLine(knob.x+knob.width-2,knob.y,knob.x+knob.width-2,knob.y+knob.height-1);
		} else {//Horizontal
			g.drawLine(knob.x,knob.y,knob.x+knob.width-1,knob.y);
			g.drawLine(knob.x,knob.y+knob.height-1,knob.x+knob.width-1,knob.y+knob.height-1);
			if(getDirection()>1) g.drawLine(knob.x,knob.y+knob.height-2,knob.x+knob.width-1,knob.y+knob.height-2);
		}
	}
	
	public static void paintSwitchGhost(int xpos, int ypos, int direc, int ro, int co, Graphics g) {
		//Door
		Lockeddoor.paintDoorGhost(direc,ro,co,g);
		//Switch
		g.setColor(Jned.ITEM_GHOST);
		g.fillRect(xpos-4,ypos-4,8,8);
		//Trigger line
		g.setColor(Jned.DOOR_TRIGGER);
		g.drawLine(ro*24+(24*Math.abs(2-direc))/2,co*24+(2-Math.abs(1-direc))*12,xpos,ypos);
	}
	public static void paintDoorGhost(int direc, int ro, int co, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		int x = 24*ro, y = 24*co;
		switch(direc) {
			case 0:
				g.fillRect(x+22,y,3,24);
				g.drawLine(x+21,y+6,x+21,y+18);
				g.drawLine(x+25,y+6,x+25,y+18);
			break;
			case 1:
				g.fillRect(x,y+22,24,3);
				g.drawLine(x+6,y+21,x+18,y+21);
				g.drawLine(x+6,y+25,x+18,y+25);
			break;
			case 2:
				g.fillRect(x,y,2,24);
				g.drawLine(x-1,y+6,x-1,y+17);
				g.fillRect(x+2,y+6,2,12);
			break;
			case 3:
				g.fillRect(x,y,24,2);
				g.drawLine(x+6,y-1,x+17,y-1);
				g.fillRect(x+6,y+2,12,2);
			break;
		}
	}
}