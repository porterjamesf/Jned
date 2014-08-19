/*
KeySignature.java

James Porter

Manages all the keyboard input for Jned. 																												//LEFT OFF IN MIDDLE OF WRITING ACTION PERFORMED FOR KEY CHANGE BUTTONS
*/

/*Key commands are handled with an array. The array maps every key press and combo key press (e.g. ALT + K) to an action number.

The array is based on the key code of key events, which range as follows:		
			0		1		2		3		4		5		6		7		8		9
0's   -																		Backspace
10's  -		Enter			Clear*													Pause/Break			*Clear is the numbpad 5 when num lock is off
20's  -		Caps lk													Esc
30's  - 					Space	PageUp	PageDwn	End		Home	Left	Up		Right
40's  - 	Down	(bckspc)				,<		-_		.>		/?		0		1
50's  -		2		3		4		5		6		7		8		9				;:
60's  -				=+								A		B		C		D		E
70's  -		F		G		H		I		J		K		L		M		N		O
80's  -		P		Q		R		S		T		U		V		W		X		Y
90's  -		Z		[{		\|		]}						np0		np1		np2		np3
100's -		np4		np5		np6		np7		np8		np9		np*		np+				np-
110's -		np.		np/		F1		F2		F3		F4		F5		F6		F7		F8
120's -		F9				F11		F12								Delete
130's -		
140's -										Num lk	Scroll lk
150's -										Prnt sc	Insert
...
190's -						`~
...
220's -						'"

This boils down essentially to a block from 32 to 123, with 5 sparse key codes before it and 7 after. ***Update: the backspace key has been added to the mix
much later, making 6 sparse key codes before. But, since everything is already set up with the old index values, the backspace key code of 8 is just mapped
to the previously unused value of 41 (which gets mapped to 21).*** So, a method is used to consolidate that into
an array of 104 values, with 8 variations of combo keys indexed with binary flags:
0 -	000 -	none
1 -	001 -					ALT
2 -	010 -			SHIFT
3 -	011 -			SHIFT +	ALT
4 -	100 -	CTRL
5 -	101 -	CTRL 	  +		ALT
6 -	110 -	CTRL  +	SHIFT
7 -	111 -	CTRL  +	SHIFT +	ALT

The array itself is actually not an array, but a sparse array implemented as a linked list. Each element has its key (the array index as above) and some
arbitrary number of action values in an ArrayList (since some keys need to have multiple functions in different contexts).

For the list of action numbers, see the method getActionText(int actionNumber) below.
*/

import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

public class KeySignature implements KeyListener/*, ActionListener*/ {
	static final int	KEY_ARRAY_SIZE = 256,
						KCD_BORDER = 4,
						KCD_WIDTH = 256,
						CLD_WIDTH = 256,
						KCD_ROW_HEIGHT = 24,
						KCD_KEYNAME_WIDTH = 124,
						KCD_BUTTON_WIDTH = 58;
	private Jned mind;
	private LevelArea lvl;
	private KeyShortcuts shortcutsWindow;
	private String preset;							//The current key shortcut preset setting, remembered in the attr2 for "custom"
	private Nfile config;
	private JDialog keyChange, keyCol;
	private boolean	rDown, dDown, lDown, uDown;
	private ArrayList<KeySetting> settings;
	//Key change dialog components
	private int actionNumber, keyChangeIndex;
	private boolean isReplacing;
	private JLabel actionName;
	private JLabel[] keyNames;
	private Buttonwog[] removes,replaces;
	private Buttonwog kcdAdd, kcdClose;
	private DialogKeyListener dears;
	//Collision dialog components
	private JLabel cldKey, cldOldAct, cldNewAct;
	private Buttonwog cldOk, cldCancel;
	private int keyVal, collidingAction;
	
	private int modKeyFlags;	//Flags for CTRL, ALT and DELETE
	
	private SparseArrayNode	keyIndicesHead,
							actionIndicesHead;
	
	public KeySignature (Jned blown, LevelArea level, Nfile configurator, JFrame frame) {
		mind = blown;
		lvl = level;
		config = configurator;
		modKeyFlags = 0;
		keyChangeIndex = -1;
		rDown = dDown = lDown = uDown = isReplacing = false;
		settings = new ArrayList<KeySetting>();
		initializeSparseArray();
		initializeKeyChangeDialog(frame);
		initializeKeyCollisionDialog(frame);
		shortcutsWindow = null;
	}
	public void register(KeyShortcuts hotkeys) {
		shortcutsWindow = hotkeys;
		shortcutsWindow.setPreset(preset);
	}
	public void register(KeySetting setting) {
		settings.add(setting);
	}
	
	//Sets up the key indices array using the config file.
	private void initializeSparseArray() {
		keyIndicesHead = new SparseArrayNode(null, -1);
		
		preset = config.getAttr2("custom");
		String[] temp = config.getData(preset).split(";");
		if(shortcutsWindow!=null) shortcutsWindow.setPreset(preset);
		for(String str : temp) {
			try {
				String[] svals = str.split(",");
				int[] ivals = new int[svals.length];
				for(int i = 0; i < svals.length; i ++) {
					ivals[i] = Integer.parseInt(svals[i]);
				}
				addNode(keyIndicesHead,ivals);
			} catch (NumberFormatException e) {
				System.out.println("Problem in def key config settings:" + str);
			}
		}
		createReverseArray();
	}
	
	//Sets up the reverse array, i.e. an array in which the actions are the indices and the key numbers are the values. This is used when showing and modifying the
	//key shortcuts through the editor's preferences menu
	private void createReverseArray() {
		actionIndicesHead = new SparseArrayNode(null, -1);
		
		SparseArrayNode place = keyIndicesHead.next;
		while(place != null) {
			for(int val : place.values) {
				addNode(actionIndicesHead, val, place.key);
			}
			place = place.next;
		}
	}
	
	//Outputs the current key settings as a formatted string fit for writing into the config file
	public String getKeySettings() {
		String res = "";
		SparseArrayNode place = keyIndicesHead.next;
		while(place != null) {
			res += place.key + ",";
			for(int val : place.values) {
				res += val + ",";
			}
			res = res.substring(0,res.length()-1) + ";";
			place = place.next;
		}
		return res.substring(0,res.length()-1);
	}
	
	//Sets up the dialog box that pops up whenever you go to change a keyboard command
	private void initializeKeyChangeDialog(JFrame fred) {
		keyChange = new JDialog(fred, "Change Keyboard Shortcut", true);
		keyChange.getContentPane().setLayout(null);
		keyChange.getContentPane().setBackground(Jned.BG_COLOR);
		
		actionNumber = 0;
		actionName = new JLabel("",SwingConstants.CENTER);
		actionName.setForeground(Color.BLACK);
		actionName.setBounds(KeySignature.KCD_BORDER, KeySignature.KCD_BORDER, KeySignature.KCD_WIDTH - 2*KeySignature.KCD_BORDER, KeySignature.KCD_ROW_HEIGHT);
		keyChange.add(actionName);
		
		keyNames = new JLabel[0];
		removes = new Buttonwog[0];
		replaces = new Buttonwog[0];
		
		kcdAdd = new Buttonwog(mind,"keySetting#add",-2,KeySignature.KCD_BORDER, KeySignature.KCD_BORDER*2 + KeySignature.KCD_ROW_HEIGHT, KeySignature.KCD_BUTTON_WIDTH, KeySignature.KCD_ROW_HEIGHT,true,"Add...");
		kcdClose = new Buttonwog(mind,"keySetting#close",-2,KeySignature.KCD_WIDTH/2-KeySignature.KCD_BUTTON_WIDTH/2, KeySignature.KCD_BORDER*3 + KeySignature.KCD_ROW_HEIGHT*2, KeySignature.KCD_BUTTON_WIDTH, KeySignature.KCD_ROW_HEIGHT,true,"Close");
		keyChange.add(kcdAdd);
		keyChange.add(kcdClose);
		
		setKeyChange(0);
		keyChange.setLocationRelativeTo(null);
		dears = new DialogKeyListener();
		keyChange.addKeyListener(dears);
	}
	//Sets the key change dialog's contents to the settings for a specific action
	private void setKeyChange(int actionNum) {
		actionNumber = actionNum;
		actionName.setText(getActionText(actionNumber));
		
		for(int i = 0; i < keyNames.length; i++) {
			keyChange.remove(keyNames[i]);
			keyChange.remove(removes[i]);
			keyChange.remove(replaces[i]);
		}
		int ycount = KeySignature.KCD_BORDER*2 + KeySignature.KCD_ROW_HEIGHT, xcount = KeySignature.KCD_BORDER;
		SparseArrayNode node = findNode(actionIndicesHead, actionNumber);
		if(node.key == actionNumber) {
			keyNames = new JLabel[node.values.size()];
			removes = new Buttonwog[node.values.size()];
			replaces = new Buttonwog[node.values.size()];
			for(int i = 0; i < node.values.size(); i++) {
				//Key shortctu name label
				keyNames[i] = new JLabel(getKeyName(node.values.get(i)));
				keyNames[i].setForeground(Color.BLACK);
				keyNames[i].setBounds(xcount, ycount, KeySignature.KCD_KEYNAME_WIDTH, KeySignature.KCD_ROW_HEIGHT);
				keyChange.add(keyNames[i]);
				xcount += KeySignature.KCD_KEYNAME_WIDTH + KeySignature.KCD_BORDER;
				//Rename button
				removes[i] = new Buttonwog(mind,"keySetting#rm," + i,-2, xcount, ycount, KeySignature.KCD_BUTTON_WIDTH, KeySignature.KCD_ROW_HEIGHT,true,"Remove");
				keyChange.add(removes[i]);
				xcount += KeySignature.KCD_BUTTON_WIDTH + KeySignature.KCD_BORDER;
				//Replace button
				replaces[i] = new Buttonwog(mind,"keySetting#rp," + i,-2, xcount, ycount, KeySignature.KCD_BUTTON_WIDTH, KeySignature.KCD_ROW_HEIGHT,true,"Replace");
				keyChange.add(replaces[i]);
				
				ycount += KeySignature.KCD_ROW_HEIGHT + 1;
				xcount = KeySignature.KCD_BORDER;
			}
		}
		ycount += KeySignature.KCD_BORDER - 1;
		kcdAdd.setLocation(xcount,ycount);
		ycount += KeySignature.KCD_ROW_HEIGHT + KeySignature.KCD_BORDER;
		kcdClose.setLocation(KeySignature.KCD_WIDTH/2-KeySignature.KCD_BUTTON_WIDTH/2,ycount);
		
		keyChange.getContentPane().setPreferredSize(new Dimension(KeySignature.KCD_WIDTH,ycount + KeySignature.KCD_ROW_HEIGHT + KeySignature.KCD_BORDER));
		keyChange.pack();
		keyChange.repaint();
	}
	
	//Sets up the key collision (trying to set a key setting to one already in use) dialog box
	private void initializeKeyCollisionDialog(JFrame fred) {
		keyCol = new JDialog(fred, "Keyboard Shortcut In Use", true);
		keyCol.getContentPane().setLayout(null);
		keyCol.getContentPane().setBackground(Jned.BG_COLOR);
		
		int ycount = KeySignature.KCD_BORDER;
		cldKey = new JLabel("");
		cldKey.setForeground(Color.BLACK);
		cldKey.setBounds(KeySignature.KCD_BORDER,ycount,KeySignature.CLD_WIDTH-2*KeySignature.KCD_BORDER,KeySignature.KCD_ROW_HEIGHT);
		ycount += KeySignature.KCD_BORDER + KeySignature.KCD_ROW_HEIGHT;
		cldOldAct = new JLabel("");
		cldOldAct.setForeground(Color.BLACK);
		cldOldAct.setBounds(KeySignature.KCD_BORDER,ycount,KeySignature.CLD_WIDTH-2*KeySignature.KCD_BORDER,KeySignature.KCD_ROW_HEIGHT);
		ycount += KeySignature.KCD_BORDER + KeySignature.KCD_ROW_HEIGHT;
		cldNewAct = new JLabel("");
		cldNewAct.setForeground(Color.BLACK);
		cldNewAct.setBounds(KeySignature.KCD_BORDER,ycount,KeySignature.CLD_WIDTH-2*KeySignature.KCD_BORDER,KeySignature.KCD_ROW_HEIGHT);
		ycount += KeySignature.KCD_BORDER + KeySignature.KCD_ROW_HEIGHT;
		
		cldOk = new Buttonwog(mind,"keySetting#cldOk",-2,KeySignature.KCD_BORDER, ycount, KeySignature.KCD_BUTTON_WIDTH, KeySignature.KCD_ROW_HEIGHT,true,"OK");
		cldCancel = new Buttonwog(mind,"keySetting#cldCancel",-2,KeySignature.CLD_WIDTH-KeySignature.KCD_BORDER-KeySignature.KCD_BUTTON_WIDTH, ycount, KeySignature.KCD_BUTTON_WIDTH, KeySignature.KCD_ROW_HEIGHT,true,"Cancel");
		ycount += KeySignature.KCD_BORDER + KeySignature.KCD_ROW_HEIGHT;
		
		keyCol.add(cldKey);
		keyCol.add(cldOldAct);
		keyCol.add(cldNewAct);
		keyCol.add(cldOk);
		keyCol.add(cldCancel);
		
		keyCol.setLocationRelativeTo(null);
		keyCol.getContentPane().setPreferredSize(new Dimension(KeySignature.CLD_WIDTH,ycount));
		keyCol.pack();
	}
	private void setKeyCollision(int key, int act1, int act2) {
		cldKey.setText(getKeyName(key) + " is already in use for");
		cldOldAct.setText("'" + getActionText(act1) + "'.");
		cldNewAct.setText("Change to '" + getActionText(act2) + "'?");
		keyCol.repaint();
	}
	
	//Converts a keycode from a key event into an index for the key mapping array
	private int convertKeyCode(int kc) {
		if(kc < 32 || kc > 123) {
			switch(kc) {
				case 8: return 21;		//Backspace
				case 10: return 0;		//Enter
				case 12: return 1;		//Clear
				case 19: return 2;		//Pause/break
				case 20: return 3;		//Caps lock
				case 27: return 4;		//Esc
				case 127: return 5;		//Delete
				case 144: return 6;		//Num lock
				case 145: return 7;		//Scroll lock
				case 154: return 8;		//Print screen
				case 155: return 9;		//Insert
				case 192: return 10;	//`~
				case 222: return 11;	//'"
				default: return -1;
			}
		}
		return kc - 20;					//All the rest, with 32 (space) being mapped to 12, up through 123 (F12) being mapped to 103.
										//There are many holes in this range, but there should be no case where the holes are called upon during runtime
	}
	//Does the opposite: converts an index from the key mapping array into its corresponding key code
	private int convertKeyIndex(int ki) {
		if(ki < 12) {
			switch(ki) {
				case 0: return 10;		//Enter
				case 1: return 12;		//Clear
				case 2: return 19;		//Pause/break
				case 3: return 20;		//Caps lock
				case 4: return 27;		//Esc
				case 5: return 127;		//Delete
				case 6: return 144;		//Num lock
				case 7: return 145;		//Scroll lock
				case 8: return 154;		//Print screen
				case 9: return 155;		//Insert
				case 10: return 192;	//`~
				case 11: return 222;	//'"
				default: return -1;
			}
		}
		if(ki==21) return 8;			//Backspace
		return ki + 20;					//All the rest, with 12 being mapped to 32 (space), up through 103 being mapped to 123 (F12).
	}
	
	//Takes a keycode from a key event and retrieves the appropriate action for it
	private void doActions(int key, boolean press) {
		SparseArrayNode node = findNode(keyIndicesHead, key);
		if(node.key == key) { //Must make sure this is the right node, not the preceding node of where it would be if it existed
			for(int val : node.values) {
				if(val >= 45 && val <= 48) { //These are the object direction actions. They can be pressed in combination for the diagonal launch pad directions.
					switch(val) {
						case 45:
							rDown = press;
							if(press) {
								if(dDown) {
									mind.doActionNumber(49);
								} else {
									if(uDown) {
										mind.doActionNumber(52);
									} else {
										mind.doActionNumber(45);
									}
								}
							}
						break;
						case 46:
							dDown = press;
							if(press) {
								if(rDown) {
									mind.doActionNumber(49);
								} else {
									if(lDown) {
										mind.doActionNumber(50);
									} else {
										mind.doActionNumber(46);
									}
								}
							}
						break;
						case 47:
							lDown = press;
							if(press) {
								if(dDown) {
									mind.doActionNumber(50);
								} else {
									if(uDown) {
										mind.doActionNumber(51);
									} else {
										mind.doActionNumber(47);
									}
								}
							}
						break;
						case 48:
							uDown = press;
							if(press) {
								if(rDown) {
									mind.doActionNumber(52);
								} else {
									if(lDown) {
										mind.doActionNumber(51);
									} else {
										mind.doActionNumber(48);
									}
								}
							}
						break;
					}
				} else {
					if(val == 9) mind.backsp = press;
					if(press) mind.doActionNumber(val);
				}
			}
		}
	}
	
	//KeyListener methods
	public void keyPressed(KeyEvent ke) { 
		switch(ke.getKeyCode()) {
			case KeyEvent.VK_CONTROL:
				mind.ctrl = true;
				modKeyFlags |= 0b100;
			break;
			case KeyEvent.VK_SHIFT:
				mind.shift = true;
				modKeyFlags |= 0b010;
			break;
			case KeyEvent.VK_ALT:
				mind.alt = true;
				modKeyFlags |= 0b001;
			break;
			default:
				doActions(104*modKeyFlags + convertKeyCode(ke.getKeyCode()),true);
			break;
		}
		mind.repaint();
	}
	public void keyReleased(KeyEvent ke) { 
		switch(ke.getKeyCode()) {
			case KeyEvent.VK_CONTROL:
				mind.ctrl = false;
				modKeyFlags &= 0b011;
			break;
			case KeyEvent.VK_SHIFT:
				mind.shift = false;
				modKeyFlags &= 0b101;
			break;
			case KeyEvent.VK_ALT:
				mind.alt = false;
				modKeyFlags &= 0b110;
			break;
			default:
				doActions(104*modKeyFlags + convertKeyCode(ke.getKeyCode()),false); //This method call is simply checking for the 4 object direction keys or delete key, to lift them if they are pressed
			break;
		}
		mind.repaint();
	}
	public void keyTyped(KeyEvent ke) {
	}
	//Returns a description of a given action
	public String getActionText(int action) {
		/*ACTION NUMBERS
		These are the action numbers for Jned. In the Jned file, they are used as indices in the array of buttons whenever an action has a 1:1 correspondance with a button press. 
		Other than that, they are simply individually mapped to different actionCommand strings by the doActionNumber method (also in Jned)
		*/
		//After adding a new action, update the size of the button array in Jned (near top of constructor, line ~165) and number of key setting objects in KeyShortcuts (under KeySettings section of constructor, line ~65)
		switch(action) {
			case 0: return "No action";
			case 1: return "tiles mode";
			case 2: return "items mode";
			case 3: return "enemies mode";
			case 4: return "undo";
			case 5: return "redo";
			case 6: return "cut";
			case 7: return "copy";
			case 8: return "paste";
			case 9: return "delete";
			case 10: return "none";
			
			//TILES MODE ONLY
			case 11: return "45 degree incline";
			case 12: return "thin 63 degree incline";
			case 13: return "thin 27 degree incline";
			case 14: return "concave curve";
			case 15: return "half tile";
			case 16: return "thick 63 degree incline";
			case 17: return "thick 27 degree incline";
			case 18: return "convex curve";
			case 19: return "erase/blank tile";
			case 20: return "fill/full tile";
			//END TILES MODE ONLY
			
			//OBJECT MODES ONLY
			// ITEMS MODE
			case 21: return "player";
			case 22: return "gold";
			case 23: return "bounce block";
			case 24: return "exit door";
			case 25: return "oneway platform";
			case 26: return "normal door";
			case 27: return "locked door";
			case 28: return "trap door";
			case 29: return "launch pad";
			case 30: return "teleporter";
			// ENEMIES MODE
			case 31: return "gauss turret";
			case 32: return "homing launcher";
			case 33: return "mine";
			case 34: return "floor guard";
			case 35: return "thwump";
			case 36: return "zap drone";
			case 37: return "seeker drone";
			case 38: return "laser drone";
			case 39: return "chaingun drone";
			case 40: return "raptors";
			//END OBJECT MODES ONLY
			
			case 41: return "tile in lower right";
			case 42: return "tile in lower left";
			case 43: return "tile in upper right";
			case 44: return "tile in upper left";
			case 45: return "right facing";
			case 46: return "down facing";
			case 47: return "left facing";
			case 48: return "up facing";
			 case 49: return "down-right facing";
			 case 50: return "down-left facing";
			 case 51: return "up-left facing";
			 case 52: return "up-right facing";
			case 53: return "surfacefollow CW";
			case 54: return "surfacefollow CCW";
			case 55: return "dumb CW";
			case 56: return "dumb CCW";
			case 57: return "alternating";
			case 58: return "quasi-random";
			case 59: return "no behavior/still";
			
			case 60: return "show triggers";
			case 61: return "hide triggers";
			case 62: return "toggle triggers";
			case 63: return "show drone paths";
			case 64: return "hide drone paths";
			case 65: return "toggle drone paths";
			
			case 66: return "copy level to clipboard";
			case 67: return "paste level from clipboard";
			case 68: return "load level from textbox";
			
			case 69: return "nudge right";
			case 70: return "nudge down";
			case 71: return "nudge left";
			case 72: return "nudge up";
			
			case 73: return "show gridlines";
			case 74: return "hide gridlines";
			case 75: return "toggle gridlines";
			case 76: return "snapping on";
			case 77: return "snapping off";
			case 78: return "toggle snapping";
			case 79: return "show snap points";
			case 80: return "hide snap points";
			case 81: return "toggle snap points";
			
			case 82: return "save";
			case 83: return "save As";
			case 84: return "open";
			
			case 85: return "pop-out text box";
			case 86: return "put text-box below window";
			case 87: return "put text-box on right side";
			
			case 88: return "new";
			case 89: return "select all";
			
			default:
				if(action >= 256) {	//	Grid and snap presets
					String[] setting = config.getNames(("" + action),2);
					if(setting.length > 0) return setting[0];
					return "Unknown grid/snap setting";
				} else {
					return "Unknown action";
				}
		}
	}
	//Checks for mutually exlusive actions
	private int collide(ArrayList<Integer> vals, int act) {
		int collision = -1;
		for(int value : vals) {
			boolean collide = true;
			if(value == act) collide = false;			//An action won't collide with itself
			if(	value >= 11 && value <= 20 ||	//Actions available in tiles mode
				value >= 41 && value <= 44) {
					if(	act >= 21 && act <= 40 ||		//...will not collide with actions in items mode
					act >= 45 && act <= 59 ||
					act >= 69 && act <= 72) collide = false;
			}
			if(	value >= 21 && value <= 40 ||	//Actions available in items mode
				value >= 45 && value <= 59 ||
				value >= 69 && value <= 72) {
					if(	act >= 11 && act <= 20 ||		//...will not collide with actions in tiles mode
					act >= 41 && act <= 44) collide = false;
			}
			if(collide) return value;			//In all other cases, the action collides
		}
		return -1;
	}
	
	//Returns a text description of the key shortcuts for a given action
	public String getKeyText(int action) {
		SparseArrayNode node = findNode(actionIndicesHead, action);
		if(node.key != action) {
			return "";
		}
		String res = "";
		for(int i = 0; i < node.values.size(); i++) {
			res = getKeyName(node.values.get(i)) + (i==1?" or ":(i>1?", ":"")) + res;
		}
		return res;
	}
	public String getKeyName(int keyIndex) {
		String res = "";
		int mkf = keyIndex/104;
		if((mkf & 0b100) > 0) res += "CTRL ";
		if((mkf & 0b010) > 0) res += "SHIFT ";
		if((mkf & 0b001) > 0) res += "ALT ";
		return res + KeyEvent.getKeyText(convertKeyIndex(keyIndex%104));
	}
	//Returns a KeyStroke object for the given action number
	public KeyStroke getKeyStroke(int action) {
		SparseArrayNode node = findNode(actionIndicesHead, action);
		if(node.key != action) {
			return null;
		}
		int keyIndex = node.values.get(node.values.size()-1); //Only the most recent key added is returned
		int mkf = keyIndex/104;
		int modifiers = 0;
		if((mkf & 0b100) > 0) modifiers |= InputEvent.CTRL_MASK;
		if((mkf & 0b010) > 0) modifiers |= InputEvent.SHIFT_MASK;
		if((mkf & 0b001) > 0) modifiers |= InputEvent.ALT_MASK;
		return KeyStroke.getKeyStroke(convertKeyIndex(keyIndex%104),modifiers,true);
	}
	
	//Returns the KeySetting object corresponding to a given action number
	public KeySetting getKeySetting(int action) {
		for(KeySetting ks : settings) {
			if(ks.actionNumber == action) return ks;
		}
		return null;
	}
	
	//Listener methods for the change buttons in KeySetting panels and buttons on the key change dialog
	public void push(String com) {
		try{
			setKeyChange(Integer.parseInt(com));
			keyChange.setVisible(true);
		} catch (NumberFormatException ex) {
			switch(com) {
				case "add":
					listen(false, keyNames.length);
					break;
				case "close":
					stopListening();
					keyChange.setVisible(false);
					break;
				case "cldOk":
					removeValue(keyIndicesHead,keyVal,collidingAction);
					completeKeyChange();
				case "cldCancel":
					keyCol.setVisible(false);
					break;
				default:
					String[] parts = com.split(",");
					try {
						if(parts[0].equals("rm")) {
							SparseArrayNode node = findNode(actionIndicesHead, actionNumber);
							removeValue(keyIndicesHead, node.values.get(Integer.parseInt(parts[1])), actionNumber);
							updateKeySettings(true);
						}
						if(parts[0].equals("rp")) {
							listen(true, Integer.parseInt(parts[1]));
						}
					} catch (NumberFormatException nfex) {}
					break;
			}
		}
	}
	private void listen(boolean isReplace, int index) {
		//Disable buttons
		for(int i = 0; i < keyNames.length; i++) {
			removes[i].disableButton();
			replaces[i].disableButton();
		}
		kcdAdd.disableButton();
		isReplacing = isReplace;
		keyChangeIndex = index;
		dears.listening = true;
		keyChange.repaint();
	}
	private void stopListening() {
		//Enable buttons
		for(int i = 0; i < keyNames.length; i++) {
			removes[i].enableButton();
			replaces[i].enableButton();
		}
		kcdAdd.enableButton();
		dears.listening = false;
		keyChange.repaint();
	}
	//Updates the key settings objects and any open key change dialog with latest info from the config file
	protected void updateKeySettings(boolean isChange) {
		if(isChange) {
			config.setAttr2("custom","custom");				//Sets the currently used preset to custom
			config.setData(getKeySettings(),"custom");		// while also setting the current configuration in the custom setting
		}
		initializeSparseArray();
		for(KeySetting setting : settings) {
			setting.refresh();
		}
		setKeyChange(actionNumber);
	}
	//The completion of the act of listening for a new key type after pushing "Add" or "Replace" in the key change dialog.
	//Separated for the purpose of being able to be called both by a key press event or by the closing of the collision dialog
	private void completeKeyChange() {
		if(isReplacing) {
			SparseArrayNode anode = findNode(actionIndicesHead, actionNumber);
			removeValue(keyIndicesHead, anode.values.get(keyChangeIndex), actionNumber);
		}
		addNode(keyIndicesHead,keyVal,actionNumber);
		updateKeySettings(true);
		stopListening();
	}
	
	//Adds a node in the correct place, or adds the values to an existing node of the same key if it exists
	private void addNode(SparseArrayNode head, int[] values) {
		if(values.length >= 2) {
			SparseArrayNode last = findNode(head, values[0]);
			if(last.key == values[0]) { //Node with this key already exists
				for(int i = 1; i < values.length; i++) {
					last.values.add(values[i]);
				}
			} else {					//Node with this key does not exist
				SparseArrayNode newNode = new SparseArrayNode(last, values);
			}
		} else {
			System.out.println("Cannot add a node without at least a key and a value");
		}
	}
	private void addNode(SparseArrayNode head, int key, int value) {
		int[] vals = {key, value};
		addNode(head, vals);
	}
	//Removes a value from a node, also removing the node if it has no remaining values
	private void removeValue(SparseArrayNode head, int key, int value) {
		SparseArrayNode last = findNode(head, key);
		if(last.key == key) {
			last.values.remove(new Integer(value));
			if(last.values.size() == 0) {
				SparseArrayNode prev = findNode(head, key-1);
				prev.next = last.next;
			}
		}
	}
	
	//Searches for a node by key, returning either the node with a matching key or the node just before where that key would go
	private SparseArrayNode findNode(SparseArrayNode head, int key) {
		SparseArrayNode place = head;
		while(place.next != null) {
			if(place.next.key > key) break;
			place = place.next;
		}
		return place;
	}
	
	//Private node class
	private class SparseArrayNode {
		protected int					key;
		protected ArrayList<Integer> 	values;
		protected SparseArrayNode		next;
		
		public SparseArrayNode (SparseArrayNode last, int tonic) {
			if(last != null) {
				next = last.next;
				last.next = this;
			} else {
				next = null;
			}
			key = tonic;
			values = new ArrayList<Integer>();
		}
		public SparseArrayNode (SparseArrayNode last, int tonic, int value) {
			this(last, tonic);
			values.add(value);
		}
		public SparseArrayNode (SparseArrayNode last, int tonic, int[] vals) {
			this(last, tonic);
			for (int val : vals) {
				values.add(val);
			}
		}
		public SparseArrayNode (SparseArrayNode last, int[] vals) {
			this(last, vals[0]);
			for (int i = 1; i < vals.length; i++) {
				values.add(vals[i]);
			}
		}
	}

	//Private dialog key listener class
	private class DialogKeyListener implements KeyListener {
		protected boolean	listening = false;
		protected int		dialogMKF = 0;
		
		public void keyPressed(KeyEvent ke) { 
			switch(ke.getKeyCode()) {
				case KeyEvent.VK_CONTROL:
					dialogMKF |= 0b100;
				break;
				case KeyEvent.VK_SHIFT:
					dialogMKF |= 0b010;
				break;
				case KeyEvent.VK_ALT:
					dialogMKF |= 0b001;
				break;
				default:
					if(listening) {
						keyVal = 104*dialogMKF + convertKeyCode(ke.getKeyCode());
						SparseArrayNode node = findNode(keyIndicesHead, keyVal);
						if(node.key == keyVal) { //This key is already in use
							collidingAction = collide(node.values, actionNumber);
							if(collidingAction > -1) { //Checks if the action being assigned is mutually exclusive to anything the key is already assigned to
								setKeyCollision(keyVal,collidingAction,actionNumber);
								keyCol.setVisible(true);
								break;
							}
						}
						completeKeyChange();
					}
				break;
			}
		}
		public void keyReleased(KeyEvent ke) { 
			switch(ke.getKeyCode()) {
				case KeyEvent.VK_CONTROL:
					dialogMKF &= 0b011;
				break;
				case KeyEvent.VK_SHIFT:
					dialogMKF &= 0b101;
				break;
				case KeyEvent.VK_ALT:
					dialogMKF &= 0b110;
				break;
				case KeyEvent.VK_BACK_SPACE:
				break;
				default:
				break;
			}
		}
		public void keyTyped(KeyEvent ke) { }
	}
}