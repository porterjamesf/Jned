/*
	MultiButtonwog.java
	
	Holds an array of Buttonwog objects. Will show/allow interaction with only one at a time, using modifiable state variable.
	Any buttons it contains but does not show will be unpushed. User must ensure that all buttons have the same origin and
	size, but should not place them in Jned. Instead, put the multibutton there.
	
	If one of the spots in the argument array is null, that button place will be a blank panel
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MultiButtonwog extends JPanel implements Pushable {
	private Buttonwog [] buttons;
	private int index,			//Index of currently shown button
				origx,			//Top left corner and dimensions of grid cell(s) for this multibutton
				origy,
				width,
				height;
	
	//Constructor
	public MultiButtonwog(Buttonwog[] butts, int xpos, int ypos, int wid, int hei) {
		super();
		setLayout(null);
		buttons = butts;
		setBounds(xpos,ypos,wid,hei);
		setBackground(Jned.BG_COLOR);
		setIndex(0);
	}
	
	//Accessor/mutator methods for index
	public int getIndex () {
		return index;
	}
	public void setIndex (int ind) {
		if (ind >= 0 && ind < buttons.length) index = ind;
		for (int i = 0; i < buttons.length; i++) {
			if(buttons[i] != null) remove(buttons[i]);
			if (i != index && buttons[i] != null) buttons[i].unpush();
		}
		if(buttons[index] != null) add(buttons[index]);
		repaint();
	}
	public void setIndex (int ind, boolean push) { //Optional boolean argument to push the button after switching to it
		setIndex(ind);
		if(push) push();
	}
	
	
	//interface methods - passed off to top button
	/*public void mouseMoved(MouseEvent me) 	{if(buttons[index] != null) buttons[index].mouseMoved(me);}
	public void mouseDragged(MouseEvent me) 	{if(buttons[index] != null) buttons[index].mouseDragged(me);}
	public void mousePressed(MouseEvent me) 	{if(buttons[index] != null) buttons[index].mousePressed(me);}
	public void mouseReleased(MouseEvent me) {if(buttons[index] != null) buttons[index].mouseReleased(me);}*/

	/*public void mouseOn(MouseEvent me)					{if(buttons[index] != null) buttons[index].mouseOn(me);}
	public void mouseOff(MouseEvent me) 					{if(buttons[index] != null) buttons[index].mouseOff(me);}
	public Rectangle getBounds() {
		return new Rectangle(origx,origy,width,height);
	}*/
	public void push() {if(buttons[index] != null) buttons[index].push();}
	public void unpush() {if(buttons[index] != null) buttons[index].unpush();}
}