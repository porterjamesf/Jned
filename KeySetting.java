/*
KeySetting.java

Author: James Porter

A small panel that displays an action and its keyboard shortcut, along with a button to change that shortcut
*/

import javax.swing.*;
import java.awt.*;

public class KeySetting extends JPanel {
	private KeySignature master;
	protected int actionNumber;
	private JLabel	aText,
					sText;
	private JMenuItem soulmate;
	
	public KeySetting(Jned mind, KeySignature lord, int xpos, int ypos, int width, int height, int actionNum) {
		soulmate = null;
		setLayout(null);
		master = lord;
		master.register(this);
		setBounds(xpos,ypos,width,height);
		setBackground(Jned.PANEL_COLOR);
		actionNumber = actionNum;
		aText = new JLabel(master.getActionText(actionNumber), SwingConstants.LEFT);
		aText.setBounds(4, 0, width/2 - 34, height);
		aText.setForeground(Color.BLACK);
		add(aText);
		sText = new JLabel(master.getKeyText(actionNumber), SwingConstants.LEFT);
		sText.setBounds(width/2 - 26, 0, width/2 - 34, height);
		sText.setForeground(Color.BLACK);
		add(sText);
		add(new Buttonwog(mind,"keySetting#" + actionNumber,-2,width - 52,2,50,height-4,true,"Change"));
	}
	
	//Updates with any changes
	public void refresh() {
		aText.setText(master.getActionText(actionNumber));
		sText.setText(master.getKeyText(actionNumber));
		repaint();
		
		//Update soulmate menu item key text
		if(soulmate != null) {
			KeyStroke tap = master.getKeyStroke(actionNumber);
			soulmate.setAccelerator(tap);
			soulmate.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(tap,"none");
		}
	}
	
	//Registers a menu item representing the same action
	public void setSoulmate(JMenuItem mi) {
		soulmate = mi;
		refresh();
	}
	public JMenuItem getSoulMate() {
		return soulmate;
	}
}