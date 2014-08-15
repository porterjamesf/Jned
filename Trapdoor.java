/*
Trapdoor.java
James Porter
*/

import java.awt.*;

public class Trapdoor extends SwitchDoor {
	private Rectangle 	knob1,
						knob2;
	
	public Trapdoor (Jned mind,int xpos, int ypos, int direc, int ro, int co) {
		super(mind,16,xpos,ypos,direc,ro,co);
		calcRect(true);
		setImage(ImageBank.TDOOR);
	}
	
	public Trapdoor duplicate() {
		return new Trapdoor(mind, getSuperX(), getSuperY(), getDirection(), getRow(), getColumn());
	}
	
	public void calcRect(boolean isDoor) {
		if(isDoor) {
			int x = 24*getRow(), y = 24*getColumn();
			switch(getDirection()) {
				case 0:
					setRect(new Rectangle(x+19,y,6,25),true);
					knob1 = new Rectangle(x+18,y+4,8,5);
					knob2 = new Rectangle(x+18,y+15,8,5);
				break;
				case 1:
					setRect(new Rectangle(x,y+19,25,6),true);
					knob1 = new Rectangle(x+4,y+18,5,8);
					knob2 = new Rectangle(x+15,y+18,5,8);
				break;
				case 2:
					setRect(new Rectangle(x,y,5,25),true);
					knob1 = new Rectangle(x-1,y+4,7,5);
					knob2 = new Rectangle(x-1,y+15,7,5);
				break;
				case 3:
					setRect(new Rectangle(x,y,25,5),true);
					knob1 = new Rectangle(x+4,y-1,5,7);
					knob2 = new Rectangle(x+15,y-1,5,7);
				break;
			}
		} else {
			setRect(new Rectangle(super.getSuperX()-3,super.getSuperY()-3,6,6),false);
		}
	}
	
	public boolean overlapsDoor(int xpos, int ypos) {
		return knob1.contains(xpos, ypos) || knob2.contains(xpos, ypos) || super.overlapsDoor(xpos,ypos);
	}
	public boolean overlapsDoor(Rectangle rect) {
		return knob1.intersects(rect) || knob2.intersects(rect) || super.overlapsDoor(rect);
	}
	
	public void paintDoor(Graphics g) {
		super.paintDoor(g);
		
		if(getDirection()%2==0) {//Vertical
			g.drawLine(knob1.x,knob1.y,knob1.x,knob1.y+knob1.height-1);
			g.drawLine(knob1.x+knob1.width-1,knob1.y,knob1.x+knob1.width-1,knob1.y+knob1.height-1);
			g.drawLine(knob2.x,knob2.y,knob2.x,knob2.y+knob2.height-1);
			g.drawLine(knob2.x+knob2.width-1,knob2.y,knob2.x+knob2.width-1,knob2.y+knob2.height-1);
		} else {//Horizontal
			g.drawLine(knob1.x,knob1.y,knob1.x+knob1.width-1,knob1.y);
			g.drawLine(knob1.x,knob1.y+knob1.height-1,knob1.x+knob1.width-1,knob1.y+knob1.height-1);
			g.drawLine(knob2.x,knob2.y,knob2.x+knob2.width-1,knob2.y);
			g.drawLine(knob2.x,knob2.y+knob2.height-1,knob2.x+knob2.width-1,knob2.y+knob2.height-1);
		}
	}
	
	public static void paintSwitchGhost(int xpos, int ypos, int direc, int ro, int co, Graphics g) {
		//Door
		Trapdoor.paintDoorGhost(direc,ro,co,g);
		//Switch
		g.setColor(Jned.ITEM_GHOST);
		g.fillRect(xpos-3,ypos-3,6,6);
		//Trigger line
		g.setColor(Jned.DOOR_TRIGGER);
		g.drawLine(ro*24+(24*Math.abs(2-direc))/2,co*24+(2-Math.abs(1-direc))*12,xpos,ypos);
	}
	public static void paintDoorGhost(int direc, int ro, int co, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		int x = 24*ro, y = 24*co;
		switch(direc) {
			case 0:
				g.fillRect(x+19,y,6,25);
				g.drawLine(x+18,y+4,x+18,y+8);
				g.drawLine(x+18,y+15,x+18,y+19);
				g.drawLine(x+25,y+4,x+25,y+8);
				g.drawLine(x+25,y+15,x+25,y+19);
			break;
			case 1:
				g.fillRect(x,y+19,25,6);				
				g.drawLine(x+4,y+18,x+8,y+18);
				g.drawLine(x+15,y+18,x+19,y+18);
				g.drawLine(x+4,y+25,x+8,y+25);
				g.drawLine(x+15,y+25,x+19,y+25);
			break;
			case 2:
				g.fillRect(x,y,5,25);				
				g.drawLine(x-1,y+4,x-1,y+8);
				g.drawLine(x-1,y+15,x-1,y+19);
				g.drawLine(x+5,y+4,x+5,y+8);
				g.drawLine(x+5,y+15,x+5,y+19);
			break;
			case 3:
				g.fillRect(x,y,25,5);
				g.drawLine(x+4,y-1,x+8,y-1);
				g.drawLine(x+15,y-1,x+19,y-1);
				g.drawLine(x+4,y+5,x+8,y+5);
				g.drawLine(x+15,y+5,x+19,y+5);
			break;
		}
	}
}