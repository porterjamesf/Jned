/*
Door.java
James Porter

A grouping for doors, allowing queries of row/column
*/

import java.awt.*;

public abstract class Door extends DirectionalItem {

	public Door (Jned mind, int what, int xpos, int ypos, int direc) {
		super(mind, what, xpos, ypos, direc);
	}

	//Accessors for row & column
	public abstract int getRow();
	public abstract int getColumn();
	public boolean isAnySelected() {
		return super.isSelected();
	}
}