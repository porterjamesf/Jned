/*
KeyShortcuts.java

James Porter

The frame used for modifying the keyboard shortcut settings.
*/

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class KeyShortcuts extends JPanel implements ActionListener {
	public static int	WINDOW_WIDTH = 389,
						WINDOW_HEIGHT = 480,
						BORDER = 4,
						ROW_HEIGHT = 24,
						DIALOG_WIDTH = 384,
						BUTTON_WIDTH = 64;
						
	private JDialog frame;
	private Jned mind;
	private KeySignature fFlat;
	private Nfile config;
	private JComboBox<String> keySelect;
	private JDialog saveDialog,
					deleteDialog;
	private JTextField saveText;
	private boolean freeze;			//Essentially a lock-out variable, to prevent code calls for setting the combo box from triggering a change in settings, like clicking on it should
	
	public KeyShortcuts (Jned blown, JFrame fred, KeySignature bSharp, Nfile configurator) {
		mind = blown;
		config = configurator;
		setBackground(Jned.BG_COLOR);
		setLayout(null);
		fFlat = bSharp;
		freeze = false;
		
		//Preset section
		int ycount = KeyShortcuts.BORDER;
		int xcount = KeyShortcuts.BORDER;
		add(makeJLabel("Preset:", xcount, ycount, 51, KeyShortcuts.ROW_HEIGHT, 1));
		xcount += 51 + KeyShortcuts.BORDER;
		 keySelect = new JComboBox<String>(config.getNames("keys",1));
		 keySelect.setBounds(xcount, ycount, KeyShortcuts.BORDER + 102, KeyShortcuts.ROW_HEIGHT);
		 keySelect.addActionListener(this);
		add(keySelect);
		xcount += KeyShortcuts.BORDER*2 + 102;
		add(new Buttonwog(mind,"keyShortcuts saveOpen",-2,xcount, ycount, 102, KeyShortcuts.ROW_HEIGHT,true,"Save"));
		xcount += 102 + KeyShortcuts.BORDER;
		add(new Buttonwog(mind,"keyShortcuts deleteOpen",-2,xcount, ycount, 102, KeyShortcuts.ROW_HEIGHT,true,"Delete"));
		
		//Column labels
		ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
		xcount = KeyShortcuts.WINDOW_WIDTH/2 - KeyShortcuts.BORDER - 30;
		add(makeJLabel("Action:", KeyShortcuts.BORDER, ycount, xcount, KeyShortcuts.ROW_HEIGHT, -1));
		add(makeJLabel("Shortcut:", KeyShortcuts.BORDER + xcount, ycount, xcount, KeyShortcuts.ROW_HEIGHT, -1));
		
		//KeySettings
		ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
		xcount = KeyShortcuts.WINDOW_WIDTH - 2*KeyShortcuts.BORDER - 8;
		for(int i = 1; i < 88; i++) {																																					//--------NUMBER OF KEY SETTING OBJECTS
			if(i==9 || i==30 || i==40) i++; //Action numbers not used
			if(i>=49 && i <= 52) i=53; //Diagonal direction actions - combinations of directional actions 45-48
			if(i==10) {
				add(makeJLabel("Left button column",KeyShortcuts.BORDER, ycount, xcount, KeyShortcuts.ROW_HEIGHT, -1));
				ycount += KeyShortcuts.ROW_HEIGHT + 1;
			}
			if(i==41) {
				add(makeJLabel("Right button column",KeyShortcuts.BORDER, ycount, xcount, KeyShortcuts.ROW_HEIGHT, -1));
				ycount += KeyShortcuts.ROW_HEIGHT + 1;
			}
			if(i==60) {
				ycount += KeyShortcuts.ROW_HEIGHT + 1;
			}
			add(new KeySetting(mind, fFlat, KeyShortcuts.BORDER, ycount, xcount, KeyShortcuts.ROW_HEIGHT, i));
			ycount += KeyShortcuts.ROW_HEIGHT + 1;
		}
		add(makeJLabel("Gridline settings",KeyShortcuts.BORDER, ycount, xcount, KeyShortcuts.ROW_HEIGHT, -1));
		ycount += KeyShortcuts.ROW_HEIGHT + 1;
		String[] setting = config.getNames("grid",1);
		for(String set : setting) {
			try {
				add(new KeySetting(mind, fFlat, KeyShortcuts.BORDER, ycount, xcount, KeyShortcuts.ROW_HEIGHT, Integer.parseInt(config.getAttr2(set))));
				ycount += KeyShortcuts.ROW_HEIGHT + 1;
			} catch (NumberFormatException ex) {}
		}
		add(makeJLabel("Snap settings",KeyShortcuts.BORDER, ycount, xcount, KeyShortcuts.ROW_HEIGHT, -1));
		ycount += KeyShortcuts.ROW_HEIGHT + 1;
		setting = config.getNames("snap",1);
		for(String set : setting) {
			try {
				add(new KeySetting(mind, fFlat, KeyShortcuts.BORDER, ycount, xcount, KeyShortcuts.ROW_HEIGHT, Integer.parseInt(config.getAttr2(set))));
				ycount += KeyShortcuts.ROW_HEIGHT + 1;
			} catch (NumberFormatException ex) {}
		}
		setPreferredSize(new Dimension(KeyShortcuts.WINDOW_WIDTH, ycount));
		
		//Dialog and scrollbar setup
		frame = new JDialog(fred,"Keyboard Shortcuts");
		 JScrollPane sp = new JScrollPane(this);
		 sp.setPreferredSize(new Dimension(KeyShortcuts.WINDOW_WIDTH, KeyShortcuts.WINDOW_HEIGHT));
		 sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		frame.getContentPane().add(sp);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		
		initializeSaveDialog(fred);
		initializeDeleteDialog(fred);
		fFlat.register(this);
	}
	//Helper method for JLabels
	private JLabel makeJLabel(String txt, int xpos, int ypos, int wid, int hei, int align) {
		JLabel lbl = new JLabel(txt,(align==-1?SwingConstants.LEFT:(align==1?SwingConstants.RIGHT:SwingConstants.CENTER)));
		lbl.setBounds(xpos, ypos, wid, hei);
		lbl.setForeground(Color.BLACK);
		return lbl;
	}
	
	//Sets up the key shortcut preset saving/removing dialogs
	private void initializeSaveDialog(JFrame fred) {
		saveDialog = new JDialog(fred, "Save Key Shortcuts Preset");
		saveDialog.getContentPane().setLayout(null);
		saveDialog.getContentPane().setBackground(Jned.BG_COLOR);
		
		int ycount = KeyShortcuts.BORDER;
		saveDialog.add(makeJLabel("Type in a name for this preset:",KeyShortcuts.BORDER,ycount,KeyShortcuts.DIALOG_WIDTH-2*KeyShortcuts.BORDER,KeyShortcuts.ROW_HEIGHT,-1));
		ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
		saveText = new JTextField();
		saveText.setFont(Jned.BOX_FONT);
		saveText.setHorizontalAlignment(JTextField.LEFT);
		saveText.setBounds(KeyShortcuts.BORDER,ycount,KeyShortcuts.DIALOG_WIDTH-2*KeyShortcuts.BORDER,KeyShortcuts.ROW_HEIGHT);
		saveText.addActionListener(this);
		saveDialog.add(saveText);
		ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
		saveDialog.add(new Buttonwog(mind,"keyShortcuts save",-2,KeyShortcuts.BORDER,ycount,KeyShortcuts.BUTTON_WIDTH,KeyShortcuts.ROW_HEIGHT,true,"Save"));
		saveDialog.add(new Buttonwog(mind,"keyShortcuts saveCancel",-2,KeyShortcuts.DIALOG_WIDTH-KeyShortcuts.BUTTON_WIDTH-KeyShortcuts.BORDER,ycount,KeyShortcuts.BUTTON_WIDTH,KeyShortcuts.ROW_HEIGHT,true,"Cancel"));
		ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
		
		saveDialog.setLocationRelativeTo(null);
		saveDialog.getContentPane().setPreferredSize(new Dimension(KeyShortcuts.DIALOG_WIDTH,ycount));
		saveDialog.pack();
	}
	private void initializeDeleteDialog(JFrame fred) {
		deleteDialog = new JDialog(fred, "Delete Key Shortcuts Preset");
		deleteDialog.getContentPane().setLayout(null);
		deleteDialog.getContentPane().setBackground(Jned.BG_COLOR);
		
		int ycount = KeyShortcuts.BORDER;
		deleteDialog.add(makeJLabel("Are you sure you want to delete this preset?",KeyShortcuts.BORDER,ycount,KeyShortcuts.DIALOG_WIDTH-2*KeyShortcuts.BORDER,KeyShortcuts.ROW_HEIGHT,0));
		ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
		deleteDialog.add(new Buttonwog(mind,"keyShortcuts delete",-2,KeyShortcuts.BORDER,ycount,KeyShortcuts.BUTTON_WIDTH,KeyShortcuts.ROW_HEIGHT,true,"Delete"));
		deleteDialog.add(new Buttonwog(mind,"keyShortcuts deleteCancel",-2,KeyShortcuts.DIALOG_WIDTH-KeyShortcuts.BUTTON_WIDTH-KeyShortcuts.BORDER,ycount,KeyShortcuts.BUTTON_WIDTH,KeyShortcuts.ROW_HEIGHT,true,"Cancel"));
		ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
		
		deleteDialog.setLocationRelativeTo(null);
		deleteDialog.getContentPane().setPreferredSize(new Dimension(KeyShortcuts.DIALOG_WIDTH,ycount));
		deleteDialog.pack();
	}
	//Sets the currently selected preset
	public void setPreset(String prsname) {
		freeze = true;
		keySelect.setSelectedItem(prsname);
		freeze = false;
	}
	
	//Brings up the frame
	public void open() {
		frame.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(saveText)) {
			push("save");
		}
		if(e.getSource().equals(keySelect)) {
			if(!freeze) { //Combobox was clicked on, not set via code
				config.setAttr2(keySelect.getSelectedItem().toString(),"custom"); //Remember new setting
				fFlat.updateKeySettings(false);
			}
		}
	}
	//Interface method: responds to combobox changes
	public void push(String com) {
		switch(com) {
			case "saveOpen":
				saveDialog.setVisible(true);
				break;
			case "save":
				String selName = saveText.getText();
				config.writeNew(selName,"keys","",fFlat.getKeySettings());
				keySelect.addItem(selName);
				setPreset(selName);
				config.setAttr2(selName,"custom");
			case "saveCancel":
				saveDialog.setVisible(false);
				break;
			case "deleteOpen":
				deleteDialog.setVisible(true);
				break;
			case "delete":
				String selIt = keySelect.getSelectedItem().toString();
				if(!selIt.equals("default") && !selIt.equals("custom")) {
					config.delete(selIt);
					keySelect.removeItem(selIt);
				}
			case "deleteCancel":
				deleteDialog.setVisible(false);
				break;
			default:
				System.out.println("?");
				break;
		}
	}
}