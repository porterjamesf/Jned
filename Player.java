/*
Player.java

James Porter	
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class Player extends Item {
	public final int	MENU_FLAGS = 0b010001;
	private static int[][] PIX = {	{2,9},		//Each pair of coordinates represents the endpoint of a line. Two pairs
									{2,7},		//  together are the starting and ending coordinates. The figure is drawn
									{1,9},		//  by simply drawing each line. Furthermore, collision checking can be
									{1,3},		//  done by checking to see if each line contains a given point.
									{1,0},
									{1,-3},
									{0,9},
									{0,-6},
									{-1,1},
									{-1,-8},
									{-2,9},
									{-2,-7},
									{-3,9},
									{-3,4},
									{-3,-1},
									{-3,-6},
									{-4,9},
									{-4,7},
									{-4,-1},
									{-4,-5},
									{0,-9},
									{-1,-9},
									{0,-10},
									{-2,-10},
									{-1,-11},
									{-3,-11}};

	public Player(Jned mind, int xpos, int ypos) {
		super(mind, 9,xpos,ypos);
		setImage(ImageBank.PLAYER);
	}
	
	public int getFlags() {return MENU_FLAGS;}
	
	public Player duplicate() {
		return new Player(mind, getX(), getY());
	}
	
	//Checks to see if a given coordinate overlaps this turret
	public boolean overlaps(int xpos, int ypos) {
		int	spotx = xpos - getX(),
			spoty = ypos - getY();
		for(int i = 0; i < Player.PIX.length; i += 2) {
			if(Player.PIX[i][0]==spotx && Player.PIX[i+1][0]==spotx) { 							//Coordinate on same vertical line
				if(spoty <= Player.PIX[i][1] && spoty >= Player.PIX[i+1][1]) return true; //  and within the top and bottom endpoints
			}
		}
		for(int i = 0; i < Player.PIX.length; i += 2) {
			if(Player.PIX[i][1]==spoty && Player.PIX[i+1][1]==spoty) { 							//Coordinate on same horizontal line
				if(spotx <= Player.PIX[i][0] && spotx >= Player.PIX[i+1][0]) return true; //  and within the left and right endpoints
			}
		}
		return false;
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
		return overlaps(nx, ny);
	}
	
	//Paint methods
	public void paint(Graphics g) {
		boolean[] layer = {true,isHighlighted(),isSelected()};
		for(int i = 0; i < 3; i++) {
			if(i==0) {
				if(mind.drawImage(getImage(), getX(), getY(), g)) {
					layer[0] = false;
				} else {
					g.setColor(Jned.ITEM);
				}
			}
			if(i==1)g.setColor(Jned.ITEM_HL_A);
			if(i==2)g.setColor(Jned.ITEM_SELECT_A);
			if(layer[i]) {
				for(int j = 0; j < Player.PIX.length; j += 2) {
					g.drawLine(getX()+Player.PIX[j][0],getY()+Player.PIX[j][1],getX()+Player.PIX[j+1][0],getY()+Player.PIX[j+1][1]);
				}
			}
		}
	}
	public static void paintGhost(int xpos, int ypos, Graphics g) {
		g.setColor(Jned.ITEM_GHOST);
		for(int i = 0; i < PIX.length; i += 2) {
			g.drawLine(xpos+PIX[i][0],ypos+PIX[i][1],xpos+PIX[i+1][0],ypos+PIX[i+1][1]);
		}
	}
}