/*
PanelButton.java

Author: James Porter

A simple button without any bells or whistles, used in dialog boxes, preferences windows, and other contexts outside of the Jned grid when a button is needed.
*/
import java.awt.event.*;
import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;

///public class PanelButton extends JPanel implements MouseListener, ActionListener {
	private ActionListener ears;
	private Timer tim;
	private boolean pushed, highlight, enabled;
	private String	text,
					command;
	
	public PanelButton (ActionListener dumbo) {
		ears = dumbo;
		tim = new Timer(50,this);
		pushed = highlight = false;
		enabled = true;
		text = command = "";
		addMouseListener(this);
	}
	public PanelButton (ActionListener dumbo, int xpos, int ypos, int width, int height) {
		this(dumbo);
		setBounds(xpos, ypos, width, height);
	}
	public PanelButton (ActionListener dumbo, String txt, int xpos, int ypos, int width, int height) {
		this(dumbo, xpos, ypos, width, height);
		text = command = txt;
	}
	public PanelButton (ActionListener dumbo, String txt, String cmd, int xpos, int ypos, int width, int height) {
		this(dumbo, xpos, ypos, width, height);
		text = txt;
		command = cmd;
	}
	
	public void push() {
		pushed = true;
		tim.start();
		ears.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, command));
		repaint();
	}
	/*private void unpush() {
		pushed = false;
	}*/
	public void disable() {
		enabled = false;
		repaint();
	}
	public void enable() {
		enabled = true;
		repaint();
	}
	public void setText(String txt) {
		text = txt;
	}
	
	public void paintComponent(Graphics g) {
		setBackground(pushed?Jned.PUSHED:Jned.UNPUSHED);
		super.paintComponent(g);
		
		g.setColor(pushed?Jned.PUSHED_BORDER:Jned.UNPUSHED_BORDER);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
		
		g.setColor(pushed?Jned.PUSHEDTXT:Jned.UNPUSHEDTXT);
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
		g.setFont(Jned.DEF_FONT);
		g.drawString(text,getWidth()/2-text.length()*Jned.DEF_FONT_XOFF/2,getHeight()/2+Jned.DEF_FONT_YOFF);
	}
	
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {
		highlight = enabled;
		repaint();
	}
	public void mouseExited(MouseEvent e) {
		highlight = false;
		repaint();
	}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {
		if(!pushed && enabled) {
			push();
		}
	}
	public void actionPerformed(ActionEvent e) {
		//unpush();
		pushed = false;
		tim.stop();
		repaint();
	}
}