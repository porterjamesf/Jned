/*
DropPanel.java
James Porter 03/20/2013

A drop-down panel for the gridlines and snapping drop-down panels.

Note: These drop panels are simply overlapping other JPanels in the program. This would cause problems, but the drop panels are kept on top by
implementing the MouseListener interface. Not sure why that works. The mouseExited method, with a little coordinate checking, makes sure that 
the panel gets closed when the mouse moves off of it.
*/

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.awt.*;

public class DropPanel extends JPanel implements MouseListener {
	public Buttonwog	master;
	
	//Constructor
	public DropPanel(int xorig, int yorig, int wid, int hei, Buttonwog mstr/*, Jned blown*/) {
		super();
		setBackground(Jned.DROP_COLOR);
		setLayout(null);
		setBounds(xorig,yorig,wid,hei);
		
		master = mstr;
		setVisible(false);
		addMouseListener(this);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(Jned.DROP_BORDER);
		g.drawRect(0,0,getWidth()-1,getHeight()-1);
	}
	
	//Interface methods
	public void mouseClicked (MouseEvent me) {}
	public void mouseEntered (MouseEvent me) {}
	public void mouseExited (MouseEvent me) {
		if(!(new Rectangle(0,0,getWidth(),getHeight())).contains(me.getPoint())) { //This check ensures the panel won't close when the mouse "exits" because it entered a component on the drop panel
			master.unpush();
			master.repaint();
			setVisible(false);
		}
	}
	public void mousePressed (MouseEvent me) {
	}
	public void mouseReleased(MouseEvent me) {
	}
}