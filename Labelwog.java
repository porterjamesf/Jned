/*
Label.java
James Porter	01/05/2013

Turns a grid cell or block of cells in Jned into a text lable.
*/

import java.awt.*;
import java.awt.event.*;

public class Label implements GridMember {
	public static final int	LEFT = 0,		//Aligment fields
							CENTER = 1,
							RIGHT = 2,
							
							PAD = 4;		//Amount of pixels to pad text from bounds
	private int	origx,
				origy,
				width,
				height,
				alignment,	//Corresponds to field above
				border;		//Border padding for text
	private String text;
	//private Jned mind;
	private Color color;
	private boolean isVisible;
	
	public Label (/*Jned blown, */String txt, int xpos, int ypos, int wid, int hei, int align) {
		origx = xpos;
		origy = ypos;
		width = wid;
		height = hei;
		text = txt;
		//mind = blown;
		border = Label.PAD;
		alignment = align;
		
		color = Jned.TEXT_COLOR;
		isVisible = true;
	}
	
	//Accessor/mutator for text
	public String getText() {return text;}
	public void setText(String txt) {text = txt;}
	public int getAlignment() {return alignment;}
	public void setAlignment(int align) {
		alignment = align % 3; //Accepts any value, uses modulus 3 to decide what it means for alignment
	}
	public int getBorder() {return border;}
	public void setBorder(int bord) {border = bord;}
	public Color getColor() {return color;}
	public void setColor(Color col) {color = col;}
	public boolean getVisible() {return isVisible;}
	public void setVisible(boolean visible) {isVisible = visible;}
	
	
	//Interface methods
	public void mouseMoved(MouseEvent me) {}
	public void mouseReleased(MouseEvent me) {}
	public void mousePressed(MouseEvent me) {}
	public void mouseDragged(MouseEvent me) {}
	public void paint(Graphics g) {paint(g,0,0);}
	public void paint(Graphics g, int xoff, int yoff) {
		if(isVisible) {
			g.setColor(color);
			g.setFont(Jned.DEF_FONT);
			switch(alignment) {
				default:
				case LEFT:
					g.drawString(text,origx+border-xoff,origy+height/2+Jned.DEF_FONT_YOFF-yoff);
				break;
				case RIGHT:
					g.drawString(text,origx+width-border-text.length()*Jned.DEF_FONT_XOFF-xoff,origy+height/2+Jned.DEF_FONT_YOFF-yoff);
				break;
				case CENTER:
					g.drawString(text,origx+width/2-text.length()*Jned.DEF_FONT_XOFF/2-xoff,origy+height/2+Jned.DEF_FONT_YOFF-yoff);
				break;
			}
		}
	}
	public void mouseOn(MouseEvent me) {}
	public void mouseOff(MouseEvent me) {}
	public Rectangle getBounds() {return new Rectangle(origx,origy,width,height);}
}