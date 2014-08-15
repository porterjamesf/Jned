/*
Buttonwog.java

A functional button in Jned.
Stores the image files and paint methods, state variables, and position/dimensions of button.
*/

import java.util.ArrayList;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Buttonwog extends JPanel implements MouseListener, ActionListener, Pushable {	
	public Jned mind;
	private boolean highlight,		//State variables
					pushed,	
					radio,			//If a button group must always have one button selected, this is set to true; prevents button from turning off with a click
					action,			//If the button is a one-time action button, this is set to true; turns off button a split second later
					enabled;
	private ArrayList<Buttonwog> group;
	private int	img,				//The index (in ImageBank) of the image file for this button
				shftimg;			//The index of the image file for this button when shift is held (used mainly for tile editing buttons)
	public String	tag,			//The string that the button sends to Jned when it gets pushed
					text;			//The label displayed on the button, if it doesn't have an image
	private Timer 	tim;			//Timer for the unpushing of action buttons
	
	//Constructor
	public Buttonwog (Jned blown, String youre_it, int image, int xpos, int ypos, int wid, int hei, boolean isAction, String label, int shiftImage) {
		tag = youre_it;
		mind = blown;
		highlight = false;
		pushed = false;
		radio = false;
		group = new ArrayList<Buttonwog>();
		action = isAction;
		tim = new Timer(50,this);
		addMouseListener(this);
		enabled = true;
		
		setBounds(xpos,ypos,wid,hei);
		text = label;
		
		img = image;
		shftimg = shiftImage;
	}
	public Buttonwog (Jned blown, String youre_it, int image, int xpos, int ypos, int wid, int hei, boolean isAction, String label) {
		this(blown,youre_it,image,xpos,ypos,wid,hei,isAction,label,-2);
	}
	public Buttonwog (Jned blown, String youre_it, int image, int xpos, int ypos, int wid, int hei, boolean isAction) {
		this(blown,youre_it,image,xpos,ypos,wid,hei,isAction,"",-2);
	}
	public Buttonwog (Jned blown, String youre_it, int image, int xpos, int ypos, int wid, int hei, String label) {
		this(blown,youre_it,image,xpos,ypos,wid,hei,false,label,-2);
	}
	public Buttonwog (Jned blown, String youre_it, int image, int xpos, int ypos, int wid, int hei) {
		this(blown,youre_it,image,xpos,ypos,wid,hei,false,"",-2);
	}
	public Buttonwog (Jned blown, String youre_it, int image, int xpos, int ypos, int wid, int hei, int shiftImage) {
		this(blown,youre_it,image,xpos,ypos,wid,hei,false,"",shiftImage);
	}
	
	//Adds a Buttonwog to the group
	public void add(Buttonwog bt) {
		group.add(bt);
	}

	//Accessor methods
	public boolean isPushed() {
		return pushed;
	}
	public boolean isHighlighted() {
		return highlight;
	}
	public boolean isRadio() {
		return radio;
	}
	public boolean isEnabled() {
		return enabled;
	}
	
	//Mutators
	public void unpush() {
		pushed = false;
	}
	public void push() {
		if(pushed) return;
		pushed = true;
		mind.push(tag);
		for (Buttonwog bt : group) {
			bt.unpush();
		}
		if(action) tim.start();
	}
	public void radio() {
		radio = true;
	}
	public void enableButton () {
		enabled = true;
		repaint();
	}
	public void disableButton () {
		enabled = false;
		repaint();
	}
	public void setText(String txt) {
		text = txt;
		repaint();
	}
	
	//Interface methods
	public void actionPerformed(ActionEvent ae) { //Timer tick, used to unpush action buttons
		unpush();
		tim.stop();
		repaint();
	}
	public void mousePressed(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {
		highlight = enabled;
		repaint();
	}
	public void mouseExited(MouseEvent me) {
		highlight = false;
		repaint();
	}
	public void mouseClicked(MouseEvent me) {}
	public void mouseReleased(MouseEvent me) {
		if(enabled) {
			if(pushed) {
				if(!radio) {
					unpush();
					mind.push(tag + "off");
				}
			} else {
				push();
				
			}
		}
	}
	public void paintComponent(Graphics g) {
		setBackground(pushed?Jned.PUSHED:Jned.UNPUSHED);
		super.paintComponent(g);
		g.setFont(Jned.DEF_FONT);
		if(pushed) {
			BufferedImage image = mind.img((shftimg>-1&&mind.shift)?shftimg+1:img+1);
			if(image == null) {
				g.setColor(Jned.PUSHED_BORDER);
				g.drawRect(0, 0, getWidth()-1, getHeight()-1);
			} else {
				g.drawImage(image,0,0,Color.gray,null);
			}
		} else {
			BufferedImage image = mind.img((shftimg>-1&&mind.shift)?shftimg:img);
			if(image == null) {
				g.setColor(Jned.UNPUSHED_BORDER);
				g.drawRect(0, 0, getWidth()-1, getHeight()-1);
			} else {
				g.drawImage(image,0,0,Color.gray,null);
			}
		}
		paintText(g);
		if(enabled) {
			if(highlight) {
				g.setColor(pushed?Jned.BUTTON_PDHL:Jned.BUTTON_HL);
				g.fillRect(1, 1, getWidth()-2, getHeight()-2);
			}
		} else {
			g.setColor(Jned.BUTTON_DIS);
			g.fillRect(1, 1, getWidth()-2, getHeight()-2);
		}
	}
	public void paintText(Graphics g) {
		g.setColor(pushed?Jned.PUSHEDTXT:Jned.UNPUSHEDTXT);
		g.drawString(text,getWidth()/2-text.length()*Jned.DEF_FONT_XOFF/2,getHeight()/2+Jned.DEF_FONT_YOFF);
	}
}