/*
Normaldoor.java

James Porter
*/

import java.awt.*;

public class Normaldoor extends Door {
	public final int	MENU_FLAGS = 0b000100;
	int row, col, deltaRow, deltaCol;
	
	public Normaldoor (Jned mind, int direc, int ro, int co) {
		super(mind,14,24*ro+(direc==0?24:(direc==2?0:12)),24*co+(direc==1?24:(direc==3?0:12)),direc);
		row = ro;
		col = co;
		deltaRow = deltaCol = 0;
		calcShape();
		setImage(ImageBank.NDOOR);
		mind.calcDronePaths();
	}
	
	public int getFlags() {return MENU_FLAGS;}
	
	public Normaldoor duplicate() {
		return new Normaldoor(mind, getDirection(), row, col);
	}
	
	//Overrides Item: returns door position
	public int getX() {
		return 24*row+(getDirection()==0?24:(getDirection()==2?0:12));
	}
	public int getY() {
		return 24*col+(getDirection()==1?24:(getDirection()==3?0:12));
	}
	
	//Accessor/mutator for row & column
	public int getRow() {
		return row;
	}
	public int getColumn() {
		return col;
	}
	
	//Overrides Item.setDelta: only sets delta of door
	public void setDelta(int xpos, int ypos) {
		deltaRow = xpos-(24*row+12);
		deltaCol = ypos-(24*col+12);
	}
	
	//Overrides Item.moveTo: moves door
	public void moveTo(int xpos, int ypos) {
		row = xpos/24;
		col = ypos/24;
		setItemCoordinates();
		calcShape();
		mind.calcDronePaths();
	}
	
	//Overrides Item.moveRelative: moves door, using the door delta values
	public void moveRelative(int xpos, int ypos) {
		row = (xpos-deltaRow)/24;
		col = (ypos-deltaCol)/24;
		setItemCoordinates();
		calcShape();
		mind.calcDronePaths();
	}
	
	public void setDirection(int direc) {
		super.setDirection(direc);
		setItemCoordinates();
	}
	
	//Updates the xorig/yorig values in Item, so the door paints in the correct location
	private void setItemCoordinates() {
		super.moveTo(24*row+(getDirection()==0?24:(getDirection()==2?0:12)),24*col+(getDirection()==1?24:(getDirection()==3?0:12)));
	}
	
	public void setSelect(boolean select) {
		super.setSelect(select);
		mind.calcDronePaths();
	}
	
	public String toString() {
		String res = super.toString();
		return res.substring(0,res.length()-1) + (getDirection()%2==1?1:0) + ",0," + row + "," + col + ",0," + 
			(getDirection()==2?-1:0) + "," + (getDirection()==3?-1:0);
	}
	
	public void calcShape() {
		int x = 24*row, y = 24*col;
		switch(getDirection()) {
			case 0:
				int[] xs0 = {x+22,x+25,x+25,x+22};
				int[] ys0 = {y,y,y+24,y+24};
				setShape(xs0,ys0);
			break;
			case 1:
				int[] xs1 = {x+24,x+24,x,x};
				int[] ys1 = {y+22,y+25,y+25,y+22};
				setShape(xs1,ys1);
			break;
			case 2:
				int[] xs2 = {x+3,x,x,x+3};
				int[] ys2 = {y+24,y+24,y,y};
				setShape(xs2,ys2);
			break;
			case 3:
				int[] xs3 = {x,x,x+24,x+24};
				int[] ys3 = {y+3,y,y,y+3};
				setShape(xs3,ys3);
			break;
		}
	}
	
	public static void paintGhost(int dir, int row, int col, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		int x = 24*row, y = 24*col;
		switch(dir) {
			case 0:
				int[] xs0 = {x+22,x+25,x+25,x+22};
				int[] ys0 = {y,y,y+24,y+24};
				g.fillPolygon(xs0,ys0,4);
			break;
			case 1:
				int[] xs1 = {x+24,x+24,x,x};
				int[] ys1 = {y+22,y+25,y+25,y+22};
				g.fillPolygon(xs1,ys1,4);
			break;
			case 2:
				int[] xs2 = {x+3,x,x,x+3};
				int[] ys2 = {y+24,y+24,y,y};
				g.fillPolygon(xs2,ys2,4);
			break;
			case 3:
				int[] xs3 = {x,x,x+24,x+24};
				int[] ys3 = {y+3,y,y,y+3};
				g.fillPolygon(xs3,ys3,4);
			break;
		}
	}	
}