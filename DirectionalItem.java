/*
DirectionalItem.java
James Porter

A subclass of Item for all items that face on of the four directions.
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class DirectionalItem extends Item{
	public final int	MENU_FLAGS = 0b000101;
	private int	dir;/*	0 - Right
						1 - Down
						2 - Left
						3 - Up
					*/
	private int	imgBase;
	
	//Constructor
	public DirectionalItem (Jned mind, int what, int xpos, int ypos, int direc) {
		super(mind, what,xpos,ypos);
		dir = direc;
		checkDirection();
		calcShape();
		imgBase = -1;
	}
	
	public int getFlags() {return MENU_FLAGS;}
	
	public DirectionalItem duplicate() {
		return new DirectionalItem(mind, getType(), getX(), getY(), dir);
	}
	
	//Tries to get images
	public void setImage(int image) {
		super.setImage(image+dir);
		imgBase = image;
	}
	public int getImage() {
		return imgBase;
	}
	public int getImage(boolean getDirectional) {
		if(getDirectional) return super.getImage();
		return imgBase;
	}
	
	//Accessor/mutator for direction
	public int getDirection() {
		return dir;
	}
	public void setDirection(int direc) {
		dir = direc;
		checkDirection();
		calcShape();
		super.setImage(imgBase+dir);
	}
	
	private void checkDirection() {
		if (dir < 0) dir = 0;
		if (dir > 3) dir = 3;
	}
	
	public String toString() {
		return super.toString() + "," + dir;
	}
	
	public static void paintGhost(int type, int xpos, int ypos, int dir, Graphics g) {
		Item.paintGhost(type, xpos, ypos, g);
	}	
}