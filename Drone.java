/*
Drone.java
James Porter

A subclass of DirectionalItem for all the drones.
*/

import java.awt.*;
import java.util.ArrayList;

public class Drone extends DirectionalItem{
	public final int	MENU_FLAGS = 0b001100;
	
	//The edge permiability of tiles, i.e. whether or not a drone can pass through them in each direction,
	//coming from a filled tile. 1's digit is dir0, 2's digit is dir1, etc.
	//											Empty	Fill	Q		W		S		A
	public static final int[]	TILE_PERM = {	0b1111,	0b1111,	0b0011,	0b0110,	0b1100,	0b1001,		//45
																0b0001,	0b0100,	0b0100,	0b0001,		//63thin
																0b0010,	0b0010,	0b1000,	0b1000,		//27thin
																0b0011,	0b0110,	0b1100,	0b1001,		//concave
																0b0100,	0b1000,	0b0001,	0b0010,		//half
																0b0011,	0b0110,	0b1100,	0b1001,		//63thick
																0b0011,	0b0110,	0b1100,	0b1001,		//27thick
																0b0011,	0b0110,	0b1100,	0b1001},	//convex
								//Used for deciding which direction to check first for various behaviors when calculating paths
								CHECKS = {1,0,3,3,0,1,0,1,3,0,3,1,0,1,3,0,3,1};
								
	private int	beh,			//	0 - Surface follow clockwise
				dtype,			//	1 - Surface follow counter-clockwise
				deltx,			//	2 - Dumb clockwise
				delty,			/*	3 - Dumb counter - clockwise
									4 - Alternate
									5 - Quasi-random
									6 - Still	*/
				pathLength;		//The number of places to calculate for a drone path
	private boolean chPath;		//A variable to say whether or not the path needs recalculating
	private ArrayList<int[]>	path;	//The path points. The first arraylist stores different paths. The secondary arrays vary in length and store x and y points (in consecutive pairs) for each path
	private Color[]	pathShades;			//An array of colors, all of which are the drone path color. Each one is more transparent, with the last one a step away from invisible.
	
	//Constructor
	public Drone (Jned mind,int what, int xpos, int ypos, int direc, int behavior) {
		super(mind,what,xpos,ypos,direc);
		beh = behavior;
		dtype = what - 6; /*This results in:	-1 - Zap drone
												 0 - Seeker drone
												 1 - Laser drone
												 2 - Chaingun drone	*/
		checkBehavior();
		setPathLength(Jned.DEF_PATHLENGTH);
		path = new ArrayList<int[]>();
	}
	
	public int getFlags() {return MENU_FLAGS;}
	
	public Drone duplicate() {
		Drone temp = new Drone(mind, getType(), getX(), getY(), getDirection(), beh);
		temp.setPathLength(pathLength);
		return temp;
	}
	
	//Sets the path length and adjusts the path color array accordingly
	public void setPathLength(int steps) {
		pathLength = steps;
		pathShades = new Color[steps];
		int	r = Jned.DRONE_PATH.getRed(),
			g = Jned.DRONE_PATH.getGreen(),
			b = Jned.DRONE_PATH.getBlue(),
			a = Jned.DRONE_PATH.getAlpha();
		for(int i = 0; i < steps; i++) {
			pathShades[i] = new Color(r,g,b,a*i/pathLength);
		}
		chPath = true;
	}
	
	//Accessor/mutator for behavior
	public int getBehavior() {
		return beh;
	}
	public void setBehavior(int behavior) {
		beh = behavior;
		checkBehavior();
		chPath = true;
	}
	public void setDirection(int dir) {
		super.setDirection(dir);
		chPath = true;
	}
	
	private void checkBehavior() {
		if (beh < 0) beh = 0;
		if (beh > 6) beh = 6;
	}
	
	public String toString() {
		String res = super.toString();
		return res.substring(0,res.length()-1) + beh + "," + (dtype==0?1:0) + "," + (dtype<0?0:dtype) + res.substring(res.length()-2,res.length());
	}
	
	//Overrides Item - adjusts positions to be the center of cells
	public void moveTo(int xpos, int ypos) {
		super.moveTo(24*(xpos/24)+12,24*(ypos/24)+12);
		chPath = true;
	}
	public void moveRelative(int xpos, int ypos) {		//Moves to a new position offset by the difference stored in deltax and deltay
		super.moveTo(24*((xpos-deltx)/24)+12, 24*((ypos-delty)/24)+12);
		chPath = true;
	}
	public void setDelta(int xpos, int ypos) {			//Sets the offset (deltax/deltay) to the difference between current position and given position
		deltx = xpos-getX();
		delty = ypos-getY();
	}
	public void chPath() {
		chPath = true;
	}
	
	//Calculates the path of the drone and stores it
	public void calcPathR(int[][] tiles, ArrayList<Door> doors, int num, int direc, int pind, boolean swap) {
		int[] mpath = path.get(pind);
		if(checkPath(tiles, mpath[num], mpath[num+1], (direc+Drone.CHECKS[3*beh])%4) || checkDoor(doors, mpath[num], mpath[num+1], (direc+Drone.CHECKS[3*beh])%4)) {
			if(checkPath(tiles, mpath[num], mpath[num+1], (direc+Drone.CHECKS[3*beh+(swap?2:1)])%4) || checkDoor(doors, mpath[num], mpath[num+1], (direc+Drone.CHECKS[3*beh+(swap?2:1)])%4)) {
				if(checkPath(tiles, mpath[num], mpath[num+1], (direc+Drone.CHECKS[3*beh+(swap?1:2)])%4) || checkDoor(doors, mpath[num], mpath[num+1], (direc+Drone.CHECKS[3*beh+(swap?1:2)])%4)) {
					if(checkPath(tiles, mpath[num], mpath[num+1], (direc+2)%4) || checkDoor(doors, mpath[num], mpath[num+1], (direc+2)%4)) {
						//Blocked on all 4 sides
						direc = 4; //Indicates no movement.
					} else {
						//Front and sides blocked, behind is clear
						direc = (direc+2)%4; //Turn around.
						if(beh == 4) swap = !swap; //If alternating, change which direction to check first.
					}
				} else {
					//Front and favored side blocked, unfavored side is clear
					direc = (direc+Drone.CHECKS[3*beh+(swap?1:2)])%4; //Turn in unfavored direction (check 2)
					if(beh == 4) swap = !swap; //If alternating, change which direction to check first.
				}
			} else {
				//First check blocked, second check clear
				if(beh > 1) { //For surface follow, second check is straight ahead, so no direction change happens
					if(beh == 5) { //If quasi-random, a second check must be done for the other direction as well
						if(!checkPath(tiles, mpath[num], mpath[num+1], (direc+Drone.CHECKS[3*beh+2])%4) && !checkDoor(doors, mpath[num], mpath[num+1], (direc+Drone.CHECKS[3*beh+2])%4)) {
							//Both directions clear: Set up new array and split it off.
							int[] spath = new int[mpath.length-num];
							spath[0] = mpath[num];
							spath[1] = mpath[num+1];
							path.add(spath);
							calcPathR(tiles, doors, 0, (direc+Drone.CHECKS[3*beh+(swap?1:2)])%4, path.size()-1, false);
						}
					}
					direc = (direc+Drone.CHECKS[3*beh+(swap?2:1)])%4; //Otherwise, turn in favored direction (check 1)
					if(beh == 4) swap = !swap; //If alternating, change which direction to check first.
				}
			}
		} else {
			//First check clear
			if(beh < 2) direc = (direc + Drone.CHECKS[3*beh])%4; //For surface follow, turn in favored direction (check 0). Otherwise, no direction change happens.
		}
		//Record point
		switch(direc) {
			case 0:
				mpath[num+2] = mpath[num]+1;
				mpath[num+3] = mpath[num+1];
			break;
			case 1:
				mpath[num+2] = mpath[num];
				mpath[num+3] = mpath[num+1]+1;
			break;
			case 2:
				mpath[num+2] = mpath[num]-1;
				mpath[num+3] = mpath[num+1];
			break;
			case 3:
				mpath[num+2] = mpath[num];
				mpath[num+3] = mpath[num+1]-1;
			break;
			case 4: //Case when drone is trapped
				mpath[num+2] = mpath[num];
				mpath[num+3] = mpath[num+1];
			break;
		}
		//Calculate next step
		if(num < mpath.length - 4) { //At the final step, num will equal 4 less than the size of the array
			calcPathR(tiles, doors, num + 2, direc, pind, swap);
		}
	}
	public void calcPath(int[][] tiles, ArrayList<Door> doors) {
		if(beh<6) {
			if(chPath) {
				path.clear();
				int[] mpath = new int[2*pathLength+2];
				mpath[0] = getX()/24;
				mpath[1] = getY()/24;
				path.add(mpath);
				
				calcPathR(tiles, doors, 0, getDirection(), 0, false);
				
				//With the whole path calculated, convert cell coordinates to pixel coordinates
				for(int[] ppath : path) {
					for(int i = 0; i < ppath.length; i++) {
						ppath[i] = ppath[i]*24+12;
					}
				}
				
				chPath = false;
			}
		}
	}
	//Checks one step of the path, returning whether or not a given direction is blocked by a tile
	private boolean checkPath(int[][] tiles, int xcell, int ycell, int Dir) {
		int cell1 = 1, cell2 = 1;
		if(xcell > 0 && xcell < 32 && ycell > 0 && ycell < 24) cell1 = tiles[xcell-1][ycell-1];
		else if(xcell < 0 || xcell > 32 || ycell < 0 || ycell > 24) cell1 = 0;
		switch(Dir) {
			case 0:
				if(xcell > -1 && xcell < 31 && ycell > 0 && ycell < 24) cell2 = tiles[xcell][ycell-1];
				else if(xcell < -1 || xcell > 31 || ycell < 0 || ycell > 24) cell2 = 0;
			break;
			case 1:
				if(xcell > 0 && xcell < 32 && ycell > -1 && ycell < 23) cell2 = tiles[xcell-1][ycell];
				else if(xcell < 0 || xcell > 32 || ycell < -1 || ycell > 23) cell2 = 0;
			break;
			case 2:
				if(xcell > 1 && xcell < 33 && ycell > 0 && ycell < 24) cell2 = tiles[xcell-2][ycell-1];
				else if(xcell < 1 || xcell > 33 || ycell < 0 || ycell > 24) cell2 = 0;
			break;
			case 3:
				if(xcell > 0 && xcell < 32 && ycell > 1 && ycell < 25) cell2 = tiles[xcell-1][ycell-2];
				else if(xcell < 0 || xcell > 32 || ycell < 1 || ycell > 25) cell2 = 0;
			break;
		}
		if(cell2==0) return false;	//If the cell to move to is empty, it is always unblocked
		if(cell1==0) return true; 	//If the cell the drone is in is empty and the cell to move to isn't, it's always blocked
									//	However, if both cells are not empty, it's more complicated and depends on permiability
		return 	(Drone.TILE_PERM[cell1] & (Dir==0?0b0001:(Dir==1?0b0010:(Dir==2?0b0100:0b1000)))) == 0 ||
				(Drone.TILE_PERM[cell2] & (Dir==0?0b0100:(Dir==1?0b1000:(Dir==2?0b0001:0b0010)))) == 0;
	}
	//Checks one step of the path, returning whether or not a given direction is blocked by a door
	private boolean checkDoor(ArrayList<Door> doors, int xcell, int ycell, int Dir) {
		for(Door dr : doors) {
			boolean inWay = false;
			//Doors in the same cell facing the same direction as the drone are in the way
			if(dr.getRow() == xcell && dr.getColumn() == ycell && dr.getDirection() == Dir) inWay = true;
			//Doors in the destination cell facing the opposite direction as the drone are also in the way
			switch (Dir) {
				case 0:
					if(dr.getRow() == xcell + 1 && dr.getColumn() == ycell && dr.getDirection() == 2) inWay = true;
				break;
				case 1:
					if(dr.getRow() == xcell && dr.getColumn() == ycell + 1 && dr.getDirection() == 3) inWay = true;
				break;
				case 2:
					if(dr.getRow() == xcell - 1 && dr.getColumn() == ycell && dr.getDirection() == 0) inWay = true;
				break;
				case 3:
					if(dr.getRow() == xcell && dr.getColumn() == ycell - 1 && dr.getDirection() == 1) inWay = true;
				break;
			}
			//Locked doors block be default, trap doors not. When the switch is activated (simulated by selecting the door), this
			//reverses. Normal doors always block.
			if(inWay) {
				switch(dr.getType()) {
					case 15: return !dr.isAnySelected();
					case 16: return dr.isAnySelected();
					default: return true;
				}
			}
		}
		return false;
	}
	public void calcShape() {
		int x = getX(), y = getY();
		int[] xs = {x-4,x+4,x+9,x+9,x+3,x-3,x-9,x-9};
		int[] ys = {y-9,y-9,y-4,y+3,y+9,y+9,y+3,y-4};
		setShape(xs,ys);
	}
	public void paintPath(Graphics g) {
		if(beh<6) {
			for(int[] ppath : path) {
				int colorIndex = ppath.length/2-2;
				for(int i = 2; i < ppath.length; i += 2) {
					g.setColor(pathShades[colorIndex--]);
					g.drawLine(ppath[i-2],ppath[i-1],ppath[i],ppath[i+1]);
				}
			}
		}
	}
	public static void paintGhost(int what, int xpos, int ypos, int direc, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		int[] xs = {xpos-4,xpos+4,xpos+9,xpos+9,xpos+3,xpos-3,xpos-9,xpos-9};
		int[] ys = {ypos-9,ypos-9,ypos-4,ypos+3,ypos+9,ypos+9,ypos+3,ypos-4};
		g.fillPolygon(xs,ys,8);
	}
}