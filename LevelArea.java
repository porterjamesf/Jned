/*
LevelArea.java
James Porter

The area representing the actual level in the Jned application. Contains the level data.
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;

public class LevelArea extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
	private int		cell,		//The size of one tile in pixels, n standard is 24
					buttonDown,	//Set to a value when a mouse button is held down, to distinguish left and right button drag events. 0 = none, 1 = left, 2 = right
					mode,		//Current editing mode (what will be placed with a click) Using in conjunction with isTiles
								//For tile mode, this will use the same fields as the tiles 2d array
								//For objects mode, this corresponds to fields outlined under FIELDS FOR ITEM MODES in Jned
								//In both, the mode -1 represents selection mode
								//Additionally, there are 3 modes for launchpad property editing: -2: power, -3: free direction, -4: power/direction
					col,		//Tile column the mouse is over
					row,		//Tile row the mouse is over
					dcol,		//Tile mode: Width and height, measured in cells, of a dragged selected area. Item selection mode: the other corner of the selection box.
					drow,		//	Item adding mode: the coordinates to add the object at (adjusted for snap position). Launchpad edit mode: reference point against mouse
					orow,		//Original row and column clicked at the start of a drag operation
					ocol,		//	Used during dragging to calculate drow and dcol in tile mode, or to start the selection box or store a right-click point in item mode
					thePlayer,	//Index of the most recently added Player item, or -1 if none exists
					itClX,		//Amalgamate X position of item clipboard contents
					itClY;		//Amalgamate Y position of item clipboard contents
	private boolean isTiles,	
					mouseon,	//Whether the mouse is in the play area or not
					dragged,	//Turns true if mouse is dragged between press and release, to distinguish clicks from drags
					drawGrid,	//Whether or not to draw the gridlines
					drawSnap,	//Whether or not to draw the snap points
					snapTo,		//Whether or not to snap to the grid or ignore it
					swtch,		//True whenever adding the switch part of a door/exit object
					selectBox,	//True when dragging a selection box, false otherwise
					grabPoint;	//Used to grab the mouse coordinates immediately after exiting a right-click menu (caught by the mouseon event).
	private int[][] 		tiles,	//The tile data. Ints used correspond to indices in the charvals array below.
							clip;	//Clipboard for sets of tiles. Used for copying/pasting tile blocks.
	private ArrayList<Item>	items,	//Items and enemies of the level
							selection,	//The list of currently selected items
							itemClip;	//The clipboard for items, used for copying/pasting.
	private ArrayList<Door>	doors;		//List of all doors on level: used for drone path calculations
	private ArrayList<Drone> drones;	//List of all drones on level: used for drone path calculations
	private ArrayList<Launchpad> pads;	//List of launchpads: variable; used for launchpad editing operations
	private int[]			itemIndices;//Indices of the start of each item in the string representation, refreshed on each outputlevel call
	private Item			last,	//The item currently under the mouse
							rclick;	//The item that was just right-clicked on
	public Jned mind;
	private Overlay[] overlays;	//The grid line (0,1,2) and snapping (3) overlays
	private ArrayList<Integer>	scratch1,	//Scratch variables used in drawing grid lines and snap points
								scratch2;	//	Kept at global level to speed up drawing
	private JPopupMenu	cpypst,		//The drop-down menu that allows the user to copy or paste tile blocks in tile selection mode
						itemMenu;	//The drop_down menu that appears when you right-click on an item, or right-click with items selected
	private JMenuItem[]	menuItems;	//An array of the different options that can appear on the itemMenu drop-down menu.
	
	//private TextBox tbox;		//Link to the text area of Jned
	
	/*		empty	filled	45		63thin	27thin	concave	half	63thick	27thick	convex
		Q|	0		1		2		6		10		14		18		22		26		30
		W|					3		7		11		15		19		23		27		31
		S|					4		8		12		16		20		24		28		32
		A|					5		9		13		17		21		25		29		33
	*/
	
	private final char[] charvals = {'0','1',	'3','2','5','4',	//The characters used by n code
												'G','F','I','H',	//for the various tiles. Indices
												'?','>','A','@',	//in this array correspond to integer
												'7','6','9','8',	//fields used in the tiles array
												'Q','P','O','N',
												'K','J','M','L',
												'C','B','E','D',
												';',':','=','<'};

	//Constructor
	public LevelArea (int xpos, int ypos, int wid, int hei, int square, Jned blown) {
		setLayout(null);
		setBounds(xpos,ypos,wid,hei);
		setBackground(Jned.TILE_SPACE);
		addMouseListener(this);
		addMouseMotionListener(this);
		cell = square;
		mind = blown;
		isTiles = true;
		mouseon = dragged = grabPoint = false;
		col = row = dcol = drow = itClX = itClY = buttonDown = 0;
		mode = thePlayer = -1;
		swtch = false;
		
		tiles = new int[31][23];
		items = new ArrayList<Item>();
		itemClip = new ArrayList<Item>();
		selection = new ArrayList<Item>();
		doors = new ArrayList<Door>();
		drones = new ArrayList<Drone>();
		pads = new ArrayList<Launchpad>();
		last = rclick = null;
		overlays = new Overlay[4];
		scratch1 = scratch2 = null;
		
		//Tile copy/paste drop-down menu
		cpypst = new JPopupMenu();
		 JMenuItem miCopy = new JMenuItem("Copy");
		  miCopy.addActionListener(this);
		  miCopy.setActionCommand("copy");
		 JMenuItem miCut = new JMenuItem("Cut");
		  miCut.addActionListener(this);
		  miCut.setActionCommand("cut");
		 JMenuItem miPaste = new JMenuItem("Paste");
		  miPaste.addActionListener(this);
		  miPaste.setActionCommand("paste");
		 JMenuItem miErase = new JMenuItem("Erase");
		  miErase.addActionListener(this);
		  miErase.setActionCommand("erase");
		 JMenuItem miFill = new JMenuItem("Fill");
		  miFill.addActionListener(this);
		  miFill.setActionCommand("fill");
		cpypst.add(miCopy);
		cpypst.add(miCut);
		cpypst.add(miPaste);
		cpypst.add(miErase);
		cpypst.add(miFill);
		
		//Item drop-down menu
		menuItems = new JMenuItem[11];
		itemMenu = new JPopupMenu();
		menuItems[0] = new JMenuItem("Copy");
		 menuItems[0].addActionListener(this);
		 menuItems[0].setActionCommand("itemCopy");
		menuItems[1] = new JMenuItem("Cut");
		 menuItems[1].addActionListener(this);
		 menuItems[1].setActionCommand("itemCut");
		menuItems[2] = new JMenuItem("Paste");
		 menuItems[2].addActionListener(this);
		 menuItems[2].setActionCommand("itemPaste");
		menuItems[3] = new JMenuItem("Delete");
		 menuItems[3].addActionListener(this);
		 menuItems[3].setActionCommand("itemDelete");		
		JMenu nudgeMenu = new JMenu("Nudge");
		 JMenuItem nuR = new JMenuItem("right");
		  nuR.addActionListener(this);
		  nuR.setActionCommand("nudgeRight");
		 JMenuItem nuD = new JMenuItem("down");
		  nuD.addActionListener(this);
		  nuD.setActionCommand("nudgeDown");
		 JMenuItem nuL = new JMenuItem("left");
		  nuL.addActionListener(this);
		  nuL.setActionCommand("nudgeLeft");
		 JMenuItem nuU = new JMenuItem("up");
		  nuU.addActionListener(this);
		  nuU.setActionCommand("nudgeUp");
		 nudgeMenu.add(nuR);
		 nudgeMenu.add(nuD);
		 nudgeMenu.add(nuL);
		 nudgeMenu.add(nuU);
		menuItems[4] = nudgeMenu;
		JMenu hfnudgeMenu = new JMenu("Nudge");
		 JMenuItem hfnuR = new JMenuItem("right");
		  hfnuR.addActionListener(this);
		  hfnuR.setActionCommand("nudgeRight");
		 JMenuItem hfnuL = new JMenuItem("left");
		  hfnuL.addActionListener(this);
		  hfnuL.setActionCommand("nudgeLeft");
		 hfnudgeMenu.add(hfnuR);
		 hfnudgeMenu.add(hfnuL);
		menuItems[5] = hfnudgeMenu;
		JMenu direct = new JMenu("Direction");
		 JMenuItem dirR = new JMenuItem("right");
		  dirR.addActionListener(this);
		  dirR.setActionCommand("dirRight");
		 JMenuItem dirD = new JMenuItem("down");
		  dirD.addActionListener(this);
		  dirD.setActionCommand("dirDown");
		 JMenuItem dirL = new JMenuItem("left");
		  dirL.addActionListener(this);
		  dirL.setActionCommand("dirLeft");
		 JMenuItem dirU = new JMenuItem("up");
		  dirU.addActionListener(this);
		  dirU.setActionCommand("dirUp");
		 direct.add(dirR);
		 direct.add(dirD);
		 direct.add(dirL);
		 direct.add(dirU);
		menuItems[6] = direct;
		JMenu direct8 = new JMenu("Direction");
		 JMenuItem dirR8 = new JMenuItem("right");
		  dirR8.addActionListener(this);
		  dirR8.setActionCommand("dirRight");
		 JMenuItem dirRD8 = new JMenuItem("right/down");
		  dirRD8.addActionListener(this);
		  dirRD8.setActionCommand("dirRightDown");
		 JMenuItem dirD8 = new JMenuItem("down");
		  dirD8.addActionListener(this);
		  dirD8.setActionCommand("dirDown");
		 JMenuItem dirLD8 = new JMenuItem("down/left");
		  dirLD8.addActionListener(this);
		  dirLD8.setActionCommand("dirLeftDown");
		 JMenuItem dirL8 = new JMenuItem("left");
		  dirL8.addActionListener(this);
		  dirL8.setActionCommand("dirLeft");
		 JMenuItem dirLU8 = new JMenuItem("left/up");
		  dirLU8.addActionListener(this);
		  dirLU8.setActionCommand("dirLeftUp");
		 JMenuItem dirU8 = new JMenuItem("up");
		  dirU8.addActionListener(this);
		  dirU8.setActionCommand("dirUp");
		 JMenuItem dirRU8 = new JMenuItem("up/right");
		  dirRU8.addActionListener(this);
		  dirRU8.setActionCommand("dirRightUp");
		 direct8.add(dirR8);
		 direct8.add(dirRD8);
		 direct8.add(dirD8);
		 direct8.add(dirLD8);
		 direct8.add(dirL8);
		 direct8.add(dirLU8);
		 direct8.add(dirU8);
		 direct8.add(dirRU8);
		menuItems[7] = direct8;
		JMenu behav = new JMenu("Behavior");
		 JMenuItem behSCW = new JMenuItem("Surface-follow Clockwise");
		  behSCW.addActionListener(this);
		  behSCW.setActionCommand("surfCW");
		 JMenuItem behSCCW = new JMenuItem("Surface-follow Counter-clockwise");
		  behSCCW.addActionListener(this);
		  behSCCW.setActionCommand("surfCCW");
		 JMenuItem behDCW = new JMenuItem("Dumb Clockwise");
		  behDCW.addActionListener(this);
		  behDCW.setActionCommand("dumbCW");
		 JMenuItem behDCCW = new JMenuItem("Dumb Counter-clockwise");
		  behDCCW.addActionListener(this);
		  behDCCW.setActionCommand("dumbCCW");
		 JMenuItem behALT = new JMenuItem("Alternating");
		  behALT.addActionListener(this);
		  behALT.setActionCommand("alt");
		 JMenuItem behRAND = new JMenuItem("Quasi-random");
		  behRAND.addActionListener(this);
		  behRAND.setActionCommand("rand");
		 JMenuItem behNONE = new JMenuItem("None (still)");
		  behNONE.addActionListener(this);
		  behNONE.setActionCommand("none");
		 behav.add(behSCW);
		 behav.add(behSCCW);
		 behav.add(behDCW);
		 behav.add(behDCCW);
		 behav.add(behALT);
		 behav.add(behRAND);
		 behav.add(behNONE);
		menuItems[8] = behav;
		menuItems[9] = new JMenuItem("Set to Active Player");
		 menuItems[9].addActionListener(this);
		 menuItems[9].setActionCommand("activePlayer");
		JMenu launch = new JMenu("Launchpad options");
		 JMenuItem lauPow = new JMenuItem("Power");
		  lauPow.addActionListener(this);
		  lauPow.setActionCommand("launchPower");
		 JMenuItem lauDir = new JMenuItem("Free direction");
		  lauDir.addActionListener(this);
		  lauDir.setActionCommand("launchDir");
		 JMenuItem lauPD = new JMenuItem("Power/direction");
		  lauPD.addActionListener(this);
		  lauPD.setActionCommand("launchPowDir");
		 launch.add(lauPow);
		 launch.add(lauDir);
		 launch.add(lauPD);
		menuItems[10] = launch;
	}
	
	//Accessor/mutator methods
	public boolean getBoolMode() {
		return isTiles;
	}
	public int getIntMode() {
		return mode;
	}
	public void setMode(int m, boolean isT) {
		setMode(m);
		setMode(isT);
	}
	public void setMode(int m) {
		mode = m;
		swtch = false;
	}
	public void setMode(boolean isT) {
		isTiles = isT;
		if(isTiles) clearSelection();
		drow = dcol = orow = ocol = 0; 	//Cancel a dragged selection
		setMode(-1);					//Put into selection mode
	}
	//Edits tile data according to already-stored mouse position and mode information
	private void edit() {
		for (int i = Math.min(col, col+dcol) - 1; i < Math.max(col, col+dcol); i++) {
			for (int j = Math.min(row, row+drow) - 1; j < Math.max(row, row+drow); j++) {
				if(mind.shift) {
					if(mode > 1) {
						tiles[i][j] = (mode > 17?mode-16:mode+16);
					} else {
						tiles[i][j] = 1 - mode;
					}
				} else {
					tiles[i][j] = mode;
				}
				chDronePaths();
			}
		}
	}
	//Puts together all the options that should appear on the drop-down menu, based on flag parameters passed in
	private void compileMenu(int flags) {
		for(JMenuItem  mi : menuItems) { //Clears all optional menu items from the list
			itemMenu.remove(mi);
		}
		if(selection.size() > 0 || rclick != null) { //Copy, cut
			itemMenu.add(menuItems[0]);
			itemMenu.add(menuItems[1]);
		}
		if(itemClip.size() > 0) { //Paste
			itemMenu.add(menuItems[2]);
		}
		if(selection.size() > 0 || rclick != null) { //Delete
			itemMenu.add(menuItems[3]);
		}
		if((flags & 0b000001) > 0) { //Nudge, all 4 directions
			itemMenu.add(menuItems[4]);
		}
		if((flags & 0b000010) > 0 && (flags & 0b000001) == 0) { //Nudge, left & right only (floorguard)
			itemMenu.add(menuItems[5]);
		}
		if((flags & 0b000100) > 0) { //Direction
			itemMenu.add(menuItems[6]);
		}
		if((flags & 0b100000) > 0) {
			itemMenu.remove(menuItems[6]);
			itemMenu.add(menuItems[7]);
		}
		if((flags & 0b001000) > 0) { //Behavior (drones)
			itemMenu.add(menuItems[8]);
		}
		if((flags & 0b010000) > 0) { //Active player (only if 1 player is selected and more than 1 exist)
			int pcount = 0, plind = -1;
			for (Item it : selection) {
				if(it.getType()==9) {
					pcount++; 								//Counts number of selected players
					plind = items.indexOf(it); 				//Stores index of most recently added one
				}
			}
			if(pcount<=1) { 								//If there's only one selected, then plind stores its index
				if(pcount==0) plind = items.indexOf(rclick); 	//If none are selected, plind should just be set to the one under the mouse
				if(thePlayer != plind) { 					//If the sole selected player (or player under mouse) is not the active player,
					itemMenu.add(menuItems[9]);				//then the menu shows the option to make it the active player
				}
			}
		}
		if((flags & 0b100000) > 0) { //Launchpad options (incomplete)
			itemMenu.add(menuItems[10]);
		}
	}
	
	//Copies the currently selected item or items to the item clipboard
	private void copyItems(boolean cut) {
		if(selection.size()==0) {
			if(rclick != null) {
				itemClip.clear();
				itemClip.add(rclick.duplicate());
				if(rclick.getType() == 12) { //Exit - need to also add the switch position
					Exit itex =(Exit)rclick;
					itClX = (itex.getSuperX() + itex.getSwitchX())/2;
					itClY = (itex.getSuperY() + itex.getSwitchY())/2;
				} else {
					if(rclick.getType() == 15 || rclick.getType() == 16) { //Switchdoor - need to also add the door position
						SwitchDoor itsw = (SwitchDoor)rclick;
						int dir = itsw.getDirection();
						itClX = (itsw.getSuperX() + 24*itsw.getRow() + (dir==0?24:(dir==2?0:12)))/2;
						itClY = (itsw.getSuperY() + 24*itsw.getColumn() + (dir==1?24:(dir==3?0:12)))/2;
					} else {
						itClX = rclick.getX();
						itClY = rclick.getY();
					}
				}
				if(cut) {
					selection.add(rclick);
					pushDelete();
				}
			}
		} else {
			itemClip.clear();
			int tot = itClX = itClY = 0;
			for(Item it : selection) { //Gets the X and Y positions of each selected item and totals them,
				itemClip.add(it.duplicate()); // while also adding duplicates to the clipboard
				tot++;
				if(it.getType() == 12) { //Exit - need to also add the switch position
					Exit itex =(Exit)it;
					itClX += itex.getSuperX() + itex.getSwitchX();
					itClY += itex.getSuperY() + itex.getSwitchY();
					tot++;
				} else {
					if(it.getType() == 15 || it.getType() == 16) { //Switchdoor - need to also add the door position
						SwitchDoor itsw = (SwitchDoor)it;
						int dir = itsw.getDirection();
						itClX += itsw.getSuperX() + 24*itsw.getRow() + (dir==0?24:(dir==2?0:12));
						itClY += itsw.getSuperY() + 24*itsw.getColumn() + (dir==1?24:(dir==3?0:12));
						tot++;
					} else {
						itClX += it.getX();
						itClY += it.getY();
					}
				}
			}
			itClX /= tot; //Sets values to the average of all the objects' positions
			itClY /= tot;
						
			if(cut) pushDelete();
		}
	}
	//Pastes the contents of the item clipboard, centered around the mouse location
	private void pasteItems() {
		clearSelection();
		Item nitem;
		for(Item it : itemClip) {						//All the copied items
			nitem = it.duplicate();						// are duplicated, and the duplicates
			items.add(nitem);							// are placed in the level
			selection.add(nitem);						// and selected,
			if(nitem.getType()==12) {						// including the dual components of
				((Exit)nitem).setSelect(true, true);		// exits and switch doors,
			} else {									// ensuring that both parts will be moved appropriately
				if(nitem.getType()==15 || nitem.getType()==16) {
					((SwitchDoor)nitem).setSelect(true, true);
				} else {
					nitem.setSelect(true);
				}
			}
			if(nitem.getType()==9) {thePlayer = items.size() - 1;} //If a player is added, it is set to the active player
			nitem.setDelta(itClX, itClY);					//The delta value is set to the average position of all copied items
			nitem.moveRelative(orow, ocol);				// and then it is moved relative to this, so pasted items are centered around the mouse
			
			if(nitem.getType()==14 || nitem.getType()==15 || nitem.getType()==16) doors.add((Door)nitem);					//Finally, doors and drones are added
			if(nitem.getType()==5 || nitem.getType()==6 || nitem.getType()==7 || nitem.getType()==8) drones.add((Drone)nitem);	// to their respective lists
		}
		
		mind.updateText(outputLevel());
		chDronePaths();
		calcDronePaths();
	}
	//Copies the currently selected tile or tile block to the tile clipboard
	private void copyTiles(boolean cut) {
		clip = new int[Math.abs(dcol)+1][Math.abs(drow)+1];
		ocol = Math.min(col, col+dcol)-1;
		orow = Math.min(row, row+drow)-1;
		for(int i = 0; i < clip.length; i++) {
			for(int j = 0; j < clip[0].length; j++) {
				clip[i][j] = tiles[ocol+i][orow+j];
				if(cut) tiles[ocol+i][orow+j] = 0;
			}
		}
		if(cut) mind.updateText(outputLevel());
	}
	//Pastes the tile clipboard contents to the level. If the selected tile block is smaller than the tile clipboard block, it will
	// paste as much as possible, matching the upper left corner. If it is larger, it will loop and paste multiple copies of the tile
	// clipboard block.
	private void pasteTiles() {
		if(clip != null) {
			ocol = Math.min(col, col+dcol)-1;
			orow = Math.min(row, row+drow)-1;
			for(int i = 0; i <= Math.abs(dcol); i++) {
				for(int j = 0; j <= Math.abs(drow); j++) {
					tiles[ocol+i][orow+j] = clip[i%clip.length][j%clip[0].length];
				}
			}
			chDronePaths();
			calcDronePaths();
			mind.updateText(outputLevel());
		}
	}
	//Sets all the selected tiles to either filled (true) or empty (false)
	private void setTiles(boolean fill) {
		ocol = Math.min(col, col+dcol)-1;
		orow = Math.min(row, row+drow)-1;
		for(int i = 0; i <= Math.abs(dcol); i++) {
			for(int j = 0; j <= Math.abs(drow); j++) {
				tiles[ocol+i][orow+j] = (fill?1:0);
			}
		}
		chDronePaths();
		calcDronePaths();
		mind.updateText(outputLevel());
	}
	public void chDronePaths() {
		for(Drone dr : drones) {
			dr.chPath();
		}
	}
	private void calcDronePaths() {
		for(Drone dr : drones) {
			dr.calcPath(tiles, doors);
		}
	}
	public void setOverlay(int index, Overlay over) {
		overlays[index] = over;
	}
	public void drawGrid(boolean dgr) { //Sets whether or not to draw the grid lines
		drawGrid = dgr;
	}
	public void drawSnap(boolean dsn) { //Sets whether or not to draw the snap points
		drawSnap = dsn;
	}
	public void snapTo(boolean snt) { //Sets whether or not to snap objects to the snap points
		snapTo = snt;
	}
	public boolean drawGrid() {return drawGrid;}
	public boolean drawSnap() {return drawSnap;}
	public boolean snapTo() {return snapTo;}
	public String outputLevel() { //Outputs level data as a string in n editor format
		//Tiles
		char[] tls = new char[713];
		for(int c = 0; c < 31; c++) {
			for(int r = 0; r < 23; r++) {
				tls[r+23*c] = charvals[tiles[c][r]];
			}
		}
		String result = new String(tls) + "|";
		
		//Items
		String things = "";
		itemIndices = new int[items.size()];
		int pos = 0;
		if(thePlayer >= 0) {
			things += items.get(thePlayer).toString() + (items.size()>1?"!":"");
			itemIndices[thePlayer] = pos;
			pos = things.length();
		}
		for(int i = 0; i < items.size(); i++) {
			if(i!=thePlayer) {
				things += items.get(i).toString() + (i==items.size()-1?"":"!");
				itemIndices[i] = pos;
				pos = things.length();
			}
		}
		return result + things;
	}
	/*public void inputLevel(String name, String author, String genre, String data) {
		setAttributes(name,author,genre);
		inputLevel(data);
	}*/
	public void inputLevel(String data) { //Edits level data to match a string in n editor format
		String valchars = new String(charvals);
		//Tiles
		for(int c = 0; c < 31; c++) {
			for(int r = 0; r < 23; r++) {
				tiles[c][r] = valchars.indexOf(data.charAt(r+23*c));
			}
		}
		
		//Items
		thePlayer = -1;
		items.clear();
		selection.clear();
		drones.clear();
		doors.clear();
		if(data.length() > 714) {	//Index 713 (the 714th character) is the "|" between tile data and item data
			String[] itemCodes = data.substring(714).split("!");
			String[] typedat, sparams;
			double[] params;
			for(String it : itemCodes) {			//For each item,
				typedat = it.split("\\^");			//typedat will contain the type number in [0] and the rest of the info in [1], 
				sparams = typedat[1].split(",");	//and params will contain that info split up into each comma-separated parameter
				params = new double[sparams.length];
				for(int i = 0; i < sparams.length; i++) params[i] = Double.parseDouble(sparams[i]);
				Item nitem = null;
				switch (typedat[0]) {
					case "0": //Gold
						nitem = new Gold(mind,(int)params[0],(int)params[1]);
					break;
					case "1": //Bounce block
						nitem = new Bounceblock(mind,(int)params[0],(int)params[1]);
					break;
					case "2": //Launch pad
						nitem = new Launchpad(mind,(int)params[0],(int)params[1],params[2],params[3]);
					break;
					case "3": //Gauss turret
						nitem = new Gaussturret(mind,(int)params[0],(int)params[1]);
					break;
					case "4": //Floor guard
						nitem = new Floorguard(mind,(int)params[0],(int)params[1]);
					break;
					case "5": //Player
						nitem = new Player(mind,(int)params[0],(int)params[1]);
						thePlayer = 0;
					break;
					case "6": //Drone
						if(params[3]==1) { //Seeker
							nitem = new Seekerdrone(mind,(int)params[0],(int)params[1],(int)params[5],(int)params[2]);
						} else {
							switch((int)params[4]) {
								default:
								case 0: //Zap
									nitem = new Zapdrone(mind,(int)params[0],(int)params[1],(int)params[5],(int)params[2]);
								break;
								case 1: //Laser
									nitem = new Laserdrone(mind,(int)params[0],(int)params[1],(int)params[5],(int)params[2]);
								break;
								case 2: //Chaingun
									nitem = new Chaingundrone(mind,(int)params[0],(int)params[1],(int)params[5],(int)params[2]);
								break;
							}
						}
						drones.add((Drone)nitem);
					break;
					case "7": //Oneway platform
						nitem = new Oneway(mind,(int)params[0],(int)params[1],(int)params[2]);
					break;
					case "8": //Thwump
						nitem = new Thwump(mind,(int)params[0],(int)params[1],(int)params[2]);
					break;
					case "9": //Door
						int dir = (int)params[2];
						if(params[7]==-1) dir = 2;
						if(params[8]==-1) dir = 3;
						
						if(params[3]==1) { //Trap
							nitem = new Trapdoor(mind,(int)params[0],(int)params[1],dir,(int)params[4],(int)params[5]);
						} else {
							if(params[6]==1) { //Locked
								nitem = new Lockeddoor(mind,(int)params[0],(int)params[1],dir,(int)params[4],(int)params[5]);
							} else { //Normal
								nitem = new Normaldoor(mind,dir,(int)params[4],(int)params[5]);
							}
						}
						doors.add((Door)nitem);
					break;
					case "10": //Homing launcher
						nitem = new Hominglauncher(mind,(int)params[0],(int)params[1]);
					break;
					case "11": //Exit
						nitem = new Exit(mind,(int)params[0],(int)params[1],(int)params[2],(int)params[3]);
					break;
					case "12": //Mine
						nitem = new Mine(mind,(int)params[0],(int)params[1]);
					default:
					break;
				}
				if(nitem != null) {
					items.add(nitem);
				}
			}
		}
		mind.updateText(outputLevel()); //Called simply to update the item indices array
		chDronePaths();
		calcDronePaths();
	}
	
	//Interface methods
	public void mouseClicked(MouseEvent me) {}
	public void mouseMoved(MouseEvent me) {
		if(isTiles) {//Tile mode
			orow = row;
			ocol = col;
			mouseMoveTile(me);
			if(orow != row || ocol != col) cpypst.setVisible(false);
			mind.highlightTile(row-1+23*(col-1));
		} else {	//Item mode
			if(mode < 0) {
				if(mode ==-1) { //Selection mode: check under the mouse and do highlighting
					boolean getnew = false;
					orow = me.getX();
					ocol = me.getY();
					if(last != null) {
						if(!last.overlaps(me.getX(),me.getY())) {
							last.setHighlight(false);
							last = null;
							getnew = true;
							mind.unHighlight();
						}
					} else {getnew = true;}
					if(getnew) {
						for(int i = items.size()-1; i >= 0; i--) {
							if(items.get(i).overlaps(me.getX(),me.getY())) { //Adjust coordinates to n-level-space
								last = items.get(i);
								last.setHighlight(true);
								mind.highlightItem(itemIndices[i]);
								i = -1;
							}
						}
					}
				} else { //Launchpad editing mode
					double val;
					switch(mode) {
						case -2: //Power
							double theta = -pads.get(0).getDirection();
							val = ((me.getX()-drow)*Math.cos(theta) - (me.getY()-dcol)*Math.sin(theta))/24.0;
							if(val > 0.000000000000001 || val < -0.000000000000001) {
								pads.get(0).setPower(val);
								for(int i = 1; i < pads.size(); i++) {
									pads.get(i).setPower(val);
								}
							}
						break;
						case -3: //Direction
							val = Math.atan2(me.getY()-dcol,me.getX()-drow);
							pads.get(0).setDirection(val);
							for(int i = 1; i < pads.size(); i++) {
								pads.get(i).setDirection(val);
							}
						break;
						case -4: //Both
							val = (me.getX()-drow)/24.0;
							double val2 = (me.getY()-dcol)/24.0;
							pads.get(0).setPowerX(val);
							pads.get(0).setPowerY(val2);
							for(int i = 1; i < pads.size(); i++) {
								pads.get(i).setPowerX(val);
								pads.get(i).setPowerY(val2);
							}
						break;
					}
				}
			} else {		//Item adding mode
				drow = me.getX();
				dcol = me.getY();
				if(mode >= Jned.NDOOR && mode <= Jned.TDOOR+3 && !swtch) {//Door adding mode: set drow & dcol to the cell containing the mouse
					drow /= cell;
					dcol /= cell;
				} else {
					if(mode >= Jned.ZAP && mode <= Jned.CHAINGUN+27) {//Drone adding mode: snap to centers of cells
						drow = (drow/cell)*cell+cell/2;
						dcol = (dcol/cell)*cell+cell/2;
					} else {
						if(snapTo) { //Snap the x coordinate to nearest snap point
							drow = snapCoord(drow, true);
						}
						if(mode == Jned.FLOOR) {//Floorguard adding mode: snap the y coordinate to cell edges + 18
							dcol = (dcol/cell)*cell+3*cell/4;
						} else {
							if(snapTo) {//Snap the y coordinate to nearest snap point
								dcol = snapCoord(dcol, false);
							}
						}
					}
				}
			}
		}
		repaint();
	}
	public void mouseDragged(MouseEvent me) {
		if(buttonDown==1) { //Left
			if(isTiles) {
				mouseMoveTile(me);
				if(mode == -1) { //Selection mode: drag selection box
					if(ocol != col || orow != row) {
						dcol = ocol-col;
						drow = orow-row;
						dragged = true;
					}
				} else {	//Not selection mode: edit all selected tiles
					edit();
				}
			} else { //Items mode
				if(mode == -1) { //Selection mode
					drow = me.getX();		//In selection box mode, the paint method will just draw a box from (orow,ocol) to (drow,dcol)
					dcol = me.getY();		// Otherwise, these values are used to move the selected items
					if(!selectBox) {
						if(snapTo) {
							last.moveTo(snapCoord(drow,true),snapCoord(dcol,false));
							for(Item it : selection) {
								it.moveRelative(last.getX(), last.getY());
							}
						} else {
							for(Item it : selection) {
								it.moveRelative(drow, dcol);
							}
						}
					}
				} else { //Item adding mode, launchpad editing mode
					//No action
				}
			}
		}
		repaint();
	}
	//Returns the nearest snap point to a given coordinate
	protected int snapCoord(int coord, boolean isX) {
		scratch1 = overlays[3].getPoints(isX);
		int ind = Collections.binarySearch(scratch1,coord); 		//Finds nearest snap point
		if(ind < 0) ind = -ind - 1;									//Corrects for the binarySearch method's output format when an exact value is not found
		try {
			if(coord-scratch1.get(ind-1)<scratch1.get(ind)-coord) {	//Figures out which neighboring snap point is closer
				return scratch1.get(ind-1);							//  and returns that value
			} else {
				return scratch1.get(ind);
			}
		} catch (IndexOutOfBoundsException ioobe) {return coord;}	//If the mouse goes outside the level area, the given coordinate is returned
	}
	private void mouseMoveTile (MouseEvent me) {
		col = (me.getX())/cell;
		row = (me.getY())/cell;
		
		//Border checks - work using magic
		col -= Math.min(Math.min(0,dcol)+col-1,0) + Math.max(Math.max(0,dcol)+col-31,0);
		row -= Math.min(Math.min(0,drow)+row-1,0) + Math.max(Math.max(0,drow)+row-23,0);
	}
	public void mousePressed(MouseEvent me) {
		if(me.getButton()==MouseEvent.BUTTON1) {//Left
			buttonDown = 1;
			if(isTiles) {
				orow = row;
				ocol = col;
				dragged = false;
			} else { //Items mode
				if(mode < 0) {
					if(mode == -1) { //Selection mode
						orow = me.getX();						//Coordinates of the click are stored, to be used for dragging reference points	
						ocol = me.getY();						//  when dragging items, or box drawing when dragging a selection box
						if(last == null || mind.shift) {		//Selection box dragging begins when clicking on nothing or shift-clicking
							if(!mind.ctrl) clearSelection();	//Ctrl-shift clicking or ctrl-clicking nothing keeps previous selection, otherwise it is cleared
							drow = me.getX();					//These are set to the same points as orow & ocol immediately, to prevent drawing a selection
							dcol = me.getY();					//  box from orow,ocol to their previous position for one frame
							selectBox = true;					
						} else {								//Clicking on an item (not shift-click)
							if(last.isSelected() && mind.ctrl) {	//Ctrl clicking on a selected item
								selection.remove(last);				//  results in removing it from the selection
								last.setSelect(false);				//  and beginning a box-drag operation, just as if
								drow = me.getX();					//  you had ctrl-clicked on nothing	
								dcol = me.getY();
								selectBox = true;
							} else {
								if(!last.isSelected() && !mind.ctrl) clearSelection(); //If the item is outside the selection and this isn't a ctrl-click, the selection is cleared
								selection.add(last);				//Adds item to selection
								last.setSelect(true);
								if(snapTo) {
									for(Item it : selection) {
											it.setDelta(last.getX(), last.getY());
									}
								} else {
									for(Item it : selection) {			//This sets the internal reference values for each selected item, so it knows where it is relative to the click
										it.setDelta(orow, ocol);
									}
								}
							}
						}
					} else { //Launchpad editing mode
						//No action
					}
				} else { //Item adding mode
					//No action
				}
			}
		}
		if(me.getButton()==MouseEvent.BUTTON3) {//Right
			buttonDown = 2;
		}
		repaint();
	}
	public void mouseReleased(MouseEvent me) {
		buttonDown = 0;
		if(me.getButton()==MouseEvent.BUTTON1) {//Left
			if(isTiles) {//Tile mode
				if(mode == -1) { //Selection mode
					if(!dragged) {
						drow = dcol = 0; //Cancel a dragged selection
					}
				} else {	//Editing mode
					edit();
				}
			} else {//Item mode
				if(mode < 0) {
					if(mode == -1) { //Selection mode
						if(selectBox) {
							Rectangle serect = new Rectangle(Math.min(orow,drow),Math.min(ocol,dcol),Math.abs(drow-orow),Math.abs(dcol-ocol));
							for (Item it : items) {
								if(it.overlaps(serect)) {
									selection.add(it);
									it.setSelect(true);
								}
							}
							selectBox = false;
						}
					} else { //Launchpad editing mode
						mode = -1;
					}
				} else { //Item adding mode
					int type = getType(mode);
					Item nitem = null;
					switch(type) {
						case 0: //Gauss turrent
							nitem = new Gaussturret(mind, drow, dcol);
						break;
						case 1: //Homing launcher
							nitem = new Hominglauncher(mind, drow, dcol);
						break;
						case 2: //Mine
							nitem = new Mine(mind, drow, dcol);
						break;
						case 3: //Floorguard
							nitem = new Floorguard(mind, drow, dcol);
						break;
						case 4: //Thwump
							nitem = new Thwump(mind, drow, dcol, mode - Jned.THWUMP);
						break;
						case 5: //Zap drone
							nitem = new Zapdrone(mind, drow,dcol,(mode - Jned.ZAP)/7,(mode - Jned.ZAP)%7);
							drones.add((Drone)nitem);
						break;
						case 6: //Seeker drone
							nitem = new Seekerdrone(mind, drow,dcol,(mode - Jned.SEEKER)/7,(mode - Jned.SEEKER)%7);
							drones.add((Drone)nitem);
						break;
						case 7: //Laser drone
							nitem = new Laserdrone(mind, drow,dcol,(mode - Jned.LASER)/7,(mode - Jned.LASER)%7);
							drones.add((Drone)nitem);
						break;
						case 8: //Chaingun drone
							nitem = new Chaingundrone(mind, drow,dcol,(mode - Jned.CHAINGUN)/7,(mode - Jned.CHAINGUN)%7);
							drones.add((Drone)nitem);
						break;
						case 9: //Player
							nitem = new Player(mind, drow, dcol);
							thePlayer = items.size();
						break;
						case 10: //Gold
							nitem = new Gold(mind, drow, dcol);
						break;
						case 11: //Bounce block
							nitem = new Bounceblock(mind, drow, dcol);
						break;
						case 12: //Exit door
							if(swtch) {
								nitem = new Exit(mind, orow,ocol,drow,dcol);
								swtch = false;
							} else {
								orow = drow;
								ocol = dcol;
								drow = me.getX();
								dcol = me.getY();
								swtch = true;
							}
						break;
						case 13: //Oneway platform
							nitem = new Oneway(mind, drow,dcol,mode - Jned.ONEWAY);
						break;
						case 14: //Normal door
							nitem = new Normaldoor(mind, mode-Jned.NDOOR,drow,dcol);
							doors.add((Door)nitem);
						break;
						case 15:
						case 16:
							if(swtch) {
								if(type==15) {//Locked door
									nitem = new Lockeddoor(mind, drow,dcol,mode-Jned.LDOOR,orow,ocol);
								} else {//Trap door
									nitem = new Trapdoor(mind, drow,dcol,(mode - Jned.NDOOR)%4,orow,ocol);
								}
								swtch = false;
								doors.add((Door)nitem);
							} else { //Either
								orow = drow;
								ocol = dcol;
								drow = me.getX();
								dcol = me.getY();
								swtch = true;
							}
						break;
						case 17: //Launch pad
							nitem = new Launchpad(mind, drow,dcol,mode-Jned.LAUNCH);
						break;
						default: case -1: break;
					}
					if(nitem != null) {
						items.add(nitem);
					}
				}
			}
			mind.updateText(outputLevel());
			calcDronePaths();
		}
		if(me.getButton()==MouseEvent.BUTTON3) {//Right
			if(isTiles) {
				if(mode == -1) { //Tile selection mode
					cpypst.show(this, me.getX(), me.getY());
				}
			} else {
				if(mode == -1) { //Item Selection mode
					orow = me.getX();
					ocol = me.getY();
					if(selectBox) { //Selection box being dragged
						selectBox = false; //Cancel a dragged selection box
					} else {
						if(selection.size() > 0) { //One or more items are selected
							int flags = 0;
							for(Item it : selection) {
								flags = flags | it.getFlags();
							}
							compileMenu(flags);
							itemMenu.show(this, me.getX(), me.getY());
						} else { //Nothing selected
							if(last != null) { //Right-click on an item
								rclick = last;
								compileMenu(rclick.getFlags());
								itemMenu.show(this, me.getX(), me.getY());
							} else { //Right-click on nothing
								rclick = null;
								compileMenu(0);
								itemMenu.show(this, me.getX(), me.getY());
							}
						}
					}
				} else {
					if(mode < -1) { //Launchpad editing mode
						mode = -1;
						mind.updateText(outputLevel());
					}
				}
			}
		}
		repaint();
	}
	
	//Returns the type number for an item, given the mode. Type numbers correspond to Jned indices for button menus
	public int getType(int code) {
		if(code == Jned.GAUSS) return 0;
		if(code == Jned.HOMING) return 1;
		if(code == Jned.MINE) return 2;
		if(code == Jned.FLOOR) return 3;
		if(code >= Jned.THWUMP && code <= Jned.THWUMP + 3) return 4;
		if(code >= Jned.ZAP && code <= Jned.ZAP + 27) return 5;
		if(code >= Jned.SEEKER && code <= Jned.SEEKER + 27) return 6;
		if(code >= Jned.LASER && code <= Jned.LASER + 27) return 7;
		if(code >= Jned.CHAINGUN && code <= Jned.CHAINGUN + 27) return 8;
		if(code == Jned.PLAYER) return 9;
		if(code == Jned.GOLD) return 10;
		if(code == Jned.BOUNCE) return 11;
		if(code == Jned.EXIT) return 12;
		if(code >= Jned.ONEWAY && code <= Jned.ONEWAY + 3) return 13;
		if(code >= Jned.NDOOR && code <= Jned.NDOOR + 3) return 14;
		if(code >= Jned.LDOOR && code <= Jned.LDOOR + 3) return 15;
		if(code >= Jned.TDOOR && code <= Jned.TDOOR + 3) return 16;
		if(code >= Jned.LAUNCH && code <= Jned.LAUNCH + 7) return 17;
		return -1;
	}
	//Clears the list of selected items and tells each one it isn't selected anymore
	private void clearSelection() {
		for(Item it: selection) {
			it.setSelect(false);
		}
		selection.clear();
	}
	//Happens when the backspace key is pushed in the main window, or the delete command is selected from a drop-down menu or the edit menu
	public void pushDelete() {
		if(selection.size() > 0 ) {
			for(Item it: selection) {
				removeItem(it);
			}
			selection.clear();
		} else {
			if(rclick != null) {
				removeItem(rclick);
			}
		}
		mind.updateText(outputLevel());
	}
	private void removeItem(Item it) {
		int ind = items.indexOf(it);
		if(ind==thePlayer) { //If deleted item is the active Player, a new one must be found
			boolean notfound = true;
			for(int i = items.size() - 1; i >= 0; i--) {
				if(items.get(i).getType()==9 && thePlayer != i) {
					thePlayer = i;
					i = -2;
					notfound = false;
				}
			}
			if(notfound) thePlayer = -1;
		}
		if(thePlayer > ind) thePlayer--;
		items.remove(it);
		//Remove from drones/doors list if necessary
		drones.remove(it);
		if (doors.remove(it)) { //If it was a door, the drone paths should be recalculated
			chDronePaths();
			calcDronePaths();
		}
	}
	
	public void paintComponent(Graphics g) {
		//First, the outer border is drawn
		super.paintComponent(g);
		g.setColor(Jned.TILE_FILL);
		g.fillRect(0,0,getWidth(),cell);
		g.fillRect(0,getHeight()-cell,getWidth(),cell);
		g.fillRect(0,cell,cell,getHeight()-2*cell);
		g.fillRect(getWidth()-cell,cell,cell,getHeight()-2*cell);
		
		//Next, each tile is drawn
		g.setColor(Jned.TILE_FILL);
		int tx, ty;
		for(int i = 0; i < 31; i++) {
			for (int j = 0; j < 23; j++) {
				if(tiles[i][j] != 0) { //If it's empty (=0), nothing needs to be done
					//Calculates origin of cell in question
					tx = (i+1)*cell;
					ty = (j+1)*cell;
					switch (tiles[i][j]) {
					
						case 1: //Filled
							g.fillRect(tx,ty,cell,cell);
						break;
						
						//45 tile
						case 2: //Q
							{int[]	xs = {tx+cell,tx,tx+cell},
									ys = {ty,ty+cell,ty+cell};
							g.fillPolygon(xs,ys,3);}
						break;
						case 3: //W
							{int[]	xs = {tx,tx,tx+cell},
									ys = {ty,ty+cell,ty+cell};
							g.fillPolygon(xs,ys,3);}
						break;
						case 4: //S
							{int[]	xs = {tx,tx+cell,tx},
									ys = {ty,ty,ty+cell};
							g.fillPolygon(xs,ys,3);}
						break;
						case 5: //A
							{int[]	xs = {tx,tx+cell,tx+cell},
									ys = {ty,ty,ty+cell};
							g.fillPolygon(xs,ys,3);}
						break;
						
						//63thin tile
						case 6: //Q
							{int[]	xs = {tx+cell,tx+cell/2,tx+cell},
									ys = {ty,ty+cell,ty+cell};
							g.fillPolygon(xs,ys,3);}
						break;
						case 7: //W
							{int[]	xs = {tx,tx,tx+cell/2},
									ys = {ty,ty+cell,ty+cell};
							g.fillPolygon(xs,ys,3);}
						break;
						case 8: //S
							{int[]	xs = {tx,tx+cell/2,tx},
									ys = {ty,ty,ty+cell};
							g.fillPolygon(xs,ys,3);}
						break;
						case 9: //A
							{int[]	xs = {tx+cell/2,tx+cell,tx+cell},
									ys = {ty,ty,ty+cell};
							g.fillPolygon(xs,ys,3);}
						break;
						
						//27thin tile
						case 10: //Q
							{int[]	xs = {tx+cell,tx,tx+cell},
									ys = {ty+cell/2,ty+cell,ty+cell};
							g.fillPolygon(xs,ys,3);}
						break;
						case 11: //W
							{int[]	xs = {tx,tx,tx+cell},
									ys = {ty+cell/2,ty+cell,ty+cell};
							g.fillPolygon(xs,ys,3);}
						break;
						case 12: //S
							{int[]	xs = {tx,tx+cell,tx},
									ys = {ty,ty,ty+cell/2};
							g.fillPolygon(xs,ys,3);}
						break;
						case 13: //A
							{int[]	xs = {tx,tx+cell,tx+cell},
									ys = {ty,ty,ty+cell/2};
							g.fillPolygon(xs,ys,3);}
						break;
						
						//concave tile
						case 14: //Q
							g.fillRect(tx,ty,cell,cell);
							g.setColor(Jned.TILE_SPACE);
							g.fillArc(tx-cell,ty-cell,cell*2-1,cell*2-1,270,90);
							g.setColor(Jned.TILE_FILL);
						break;
						case 15: //W
							g.fillRect(tx,ty,cell,cell);
							g.setColor(Jned.TILE_SPACE);
							g.fillArc(tx,ty-cell,cell*2-1,cell*2-1,180,90);
							g.setColor(Jned.TILE_FILL);
						break;
						case 16: //S
							g.fillRect(tx,ty,cell,cell);
							g.setColor(Jned.TILE_SPACE);
							g.fillArc(tx,ty,cell*2-1,cell*2-1,90,90);
							g.setColor(Jned.TILE_FILL);
						break;
						case 17: //A
							g.fillRect(tx,ty,cell,cell);
							g.setColor(Jned.TILE_SPACE);
							g.fillArc(tx-cell,ty,cell*2-1,cell*2-1,0,90);
							g.setColor(Jned.TILE_FILL);
						break;
						
						//half tile
						case 18: //Q
							g.fillRect(tx,ty,cell/2,cell);
						break;
						case 19: //W
							g.fillRect(tx,ty,cell,cell/2);
						break;
						case 20: //S
							g.fillRect(tx+cell/2,ty,cell/2,cell);
						break;
						case 21: //A
							g.fillRect(tx,ty+cell/2,cell,cell/2);
						break;
						
						//63thick tile
						case 22: //Q
							{int[]	xs = {tx+cell/2,tx+cell,tx+cell,tx},
									ys = {ty,ty,ty+cell,ty+cell};
							g.fillPolygon(xs,ys,4);}
						break;
						case 23: //W
							{int[]	xs = {tx,tx+cell/2,tx+cell,tx},
									ys = {ty,ty,ty+cell,ty+cell};
							g.fillPolygon(xs,ys,4);}
						break;
						case 24: //S
							{int[]	xs = {tx,tx+cell,tx+cell/2,tx},
									ys = {ty,ty,ty+cell,ty+cell};
							g.fillPolygon(xs,ys,4);}
						break;
						case 25: //A
							{int[]	xs = {tx,tx+cell,tx+cell,tx+cell/2},
									ys = {ty,ty,ty+cell,ty+cell};
							g.fillPolygon(xs,ys,4);}
						break;
						
						//27thick tile
						case 26: //Q
							{int[]	xs = {tx,tx+cell,tx+cell,tx},
									ys = {ty+cell/2,ty,ty+cell,ty+cell};
							g.fillPolygon(xs,ys,4);}
						break;
						case 27: //W
							{int[]	xs = {tx,tx+cell,tx+cell,tx},
									ys = {ty,ty+cell/2,ty+cell,ty+cell};
							g.fillPolygon(xs,ys,4);}
						break;
						case 28: //S
							{int[]	xs = {tx,tx+cell,tx+cell,tx},
									ys = {ty,ty,ty+cell/2,ty+cell};
							g.fillPolygon(xs,ys,4);}
						break;
						case 29: //A
							{int[]	xs = {tx,tx+cell,tx+cell,tx},
									ys = {ty,ty,ty+cell,ty+cell/2};
							g.fillPolygon(xs,ys,4);}
						break;
						
						//convex tile
						case 30: //Q
							g.fillArc(tx,ty,cell*2,cell*2,90,90);
						break;
						case 31: //W
							g.fillArc(tx-cell,ty,cell*2,cell*2,0,90);
						break;
						case 32: //S
							g.fillArc(tx-cell,ty-cell,cell*2,cell*2,270,90);
						break;
						case 33: //A
							g.fillArc(tx,ty-cell,cell*2,cell*2,180,90);
						break;
						default:
						break;
					}
				}
			}
		}
		//Next, the gridlines are drawn
		if(drawGrid) {
			for(int i = 2; i >= 0; i--) {
				if(overlays[i] != null) {
					if(overlays[i].isOn()) {
						switch(i) {
							case 0: g.setColor(Jned.PRIMARY_GRID); break;
							case 1: g.setColor(Jned.SECONDARY_GRID); break;
							case 2: g.setColor(Jned.TERTIARY_GRID); break;
							default: break;
						}
						scratch1 = overlays[i].getPoints(true);
						for(Integer ind : scratch1) {
							if(ind > 0 && ind < getWidth()-2*cell-1)
								g.drawLine(ind+cell,cell,ind+cell,getHeight()-cell-1);
						}
						scratch1 = overlays[i].getPoints(false);
						for(Integer ind : scratch1) {
							if(ind > 0 && ind < getHeight()-2*cell-1)
								g.drawLine(cell,ind+cell,getWidth()-cell-1,ind+cell);
						}
					}
				}
			}
		}
		//Then, the snap points
		if(drawSnap) {
			if(overlays[3] != null) {
				g.setColor(Jned.SNAP_POINTS);
				scratch1 = overlays[3].getPoints(true);
				scratch2 = overlays[3].getPoints(false);
				for(Integer xind : scratch1) {
					if(xind > 0 && xind < getWidth()-1) {
						for(Integer yind : scratch2) {
							if(yind > 0 && yind < getHeight()-1)
								g.drawLine(xind,yind,xind,yind);
						}
					}
				}
			}
		}
		if(mouseon || cpypst.isVisible()) {
			if(isTiles) { //Draw a square around selected tile(s)
				g.setColor(Jned.TILE_SELECT);
				g.drawRect((Math.min(0,dcol)+col)*cell,(Math.min(0,drow)+row)*cell,(Math.abs(dcol)+1)*cell-1,(Math.abs(drow)+1)*cell-1);
			}
		}
		
		//Draws the objects
		for(Item it : items) {
			it.paint(g);
			if(mind.drawTriggers) {
				it.paintTrigger(g);
			}
			if(mode < -1) {
				for(Launchpad lp : pads) {
					lp.paintLine(g);
				}
			}
		}
		if(mind.drawPaths) {
			for(Drone dr : drones) {
				dr.paintPath(g);
			}
		}
		if(mouseon) {
			if(!isTiles) {
				if(mode == -1) {
					if(selectBox) {//Draws the selection box, if it is being dragged
						g.setColor(Jned.SELECTION_BOX);
						g.drawRect(Math.min(orow,drow),Math.min(ocol,dcol),Math.abs(drow-orow),Math.abs(dcol-ocol));
					}
				} else {//Draws ghost (faint version of current object-to-add at snap location)
					int type = getType(mode);
					switch(type){
						case 0: //Gauss turrent
							Gaussturret.paintGhost(drow, dcol, g);
						break;
						case 1: //Homing launcher
							Hominglauncher.paintGhost(drow, dcol, g);
						break;
						case 2: //Mine
							Mine.paintGhost(drow, dcol, g);
						break;
						case 3: //Floorguard
							Floorguard.paintGhost(drow, dcol, g);
						break;
						case 4: //Thwump
							Thwump.paintGhost(drow,dcol,g);
						break;
						case 5: //Zap drone
							Zapdrone.paintGhost(type,drow,dcol,(mode - Jned.ZAP)/4, g);
						break;
						case 6: //Seeker drone
							Seekerdrone.paintGhost(type,drow,dcol,(mode - Jned.SEEKER)/4, g);
						break;
						case 7: //Laser drone
							Laserdrone.paintGhost(type,drow,dcol,(mode - Jned.LASER)/4, g);
						break;
						case 8: //Chaingun drone
							Chaingundrone.paintGhost(type,drow,dcol,(mode - Jned.CHAINGUN)/4, g);
						break;
						case 9: //Player
							Player.paintGhost(drow, dcol, g);
						break;
						case 10: //Gold
							Gold.paintGhost(drow,dcol,g);
						break;
						case 11: //Bounce block
							Bounceblock.paintGhost(drow,dcol,g);
						break;
						case 12: //Exit door
							if(swtch) {
								Exit.paintSwitchGhost(orow,ocol,drow,dcol, g);
							} else {
								Exit.paintDoorGhost(drow,dcol, g);
							}
						break;
						case 13: //Oneway platform
							Oneway.paintGhost(drow,dcol,mode - Jned.ONEWAY, g);
						break;
						case 14: //Normal door
							Normaldoor.paintGhost(mode-Jned.NDOOR,drow,dcol,g);
						break;
						case 15: //Locked door
							if(swtch) {
								Lockeddoor.paintSwitchGhost(drow,dcol,mode - Jned.LDOOR,orow,ocol, g);
							} else {
								Lockeddoor.paintDoorGhost(mode - Jned.LDOOR,drow,dcol, g);
							}
						break;
						case 16: //Trap door
							if(swtch) {
								Trapdoor.paintSwitchGhost(drow,dcol,(mode - Jned.NDOOR)%4,orow,ocol, g);
							} else {
								Trapdoor.paintDoorGhost(mode - Jned.TDOOR,drow,dcol, g);
							}
						break;
						case 17: //Launch pad
							Launchpad.paintGhost(drow,dcol,mode-Jned.LAUNCH, g);
						break;
						default: case -1: break;
					}
				}
			}
		}
	}
	public void mouseEntered(MouseEvent me) {
		mouseon = true;
		if(grabPoint) {
			orow = me.getX();
			ocol = me.getY();
			findLPreference();
			grabPoint = false;
		}
		repaint();
	}
	public void mouseExited(MouseEvent me) {
		mouseon = false;
		if(isTiles) mind.unHighlight();
		repaint();
	}
	
	//Listener for menu button pushes
	public void actionPerformed(ActionEvent ae) {
		push(ae.getActionCommand());
	}
	public void push(String button) {
		switch(button) {
			case "copy":
				copyTiles(false);
			break;
			case "paste":
				pasteTiles();
			break;
			case "cut":
				copyTiles(true);
			break;
			case "erase":
				setTiles(false);
			break;
			case "fill":
				setTiles(true);
			break;
			case "itemCopy":
				copyItems(false);
			break;
			case "itemCut":
				copyItems(true);
			break;
			case "itemPaste":
				pasteItems();
			break;
			case "itemDelete":
				pushDelete();
			break;
			case "nudgeRight":
				nudge(1,0);
			break;
			case "nudgeDown":
				nudge(0,1);
			break;
			case "nudgeLeft":
				nudge(-1,0);
			break;
			case "nudgeUp":
				nudge(0,-1);
			break;
			case "dirRight":
				changeDirection(0);
			break;
			case "dirRightDown":
				changeDirection(1);
			break;
			case "dirDown":
				changeDirection(2);
			break;
			case "dirLeftDown":
				changeDirection(3);
			break;
			case "dirLeft":
				changeDirection(4);
			break;
			case "dirLeftUp":
				changeDirection(5);
			break;
			case "dirUp":
				changeDirection(6);
			break;
			case "dirRightUp":
				changeDirection(7);
			break;
			case "surfCW":
				changeBehavior(0);
			break;
			case "surfCCW":
				changeBehavior(1);
			break;
			case "dumbCW":
				changeBehavior(2);
			break;
			case "dumbCCW":
				changeBehavior(3);
			break;
			case "alt":
				changeBehavior(4);
			break;
			case "rand":
				changeBehavior(5);
			break;
			case "none":
				changeBehavior(6);
			break;
			case "activePlayer":
				setActivePlayer();
			break;
			case "launchPower":
				setMode(-2);
				findLaunchpads();
			break;
			case "launchDir":
				setMode(-3);
				findLaunchpads();
			break;
			case "launchPowDir":
				setMode(-4);
				findLaunchpads();
			break;
			default: break;
		}
	}
	//Nudges items (either full selection, or item under mouse) position(s) by given amount
	public void nudge(int xamount, int yamount) {
		if(selection.size() > 0) {
			for(Item it : selection) {
				it.setDelta(orow, ocol);
				it.moveRelative(orow+xamount,ocol+yamount);
			}
		} else {
			if(rclick != null) {
				if(rclick.getType() == 15 || rclick.getType() == 16) {
					if(!((SwitchDoor)rclick).overlapsDoor(orow,ocol)) rclick.moveTo(rclick.getX()+xamount,rclick.getY()+yamount);
				} else {
					rclick.moveTo(rclick.getX()+xamount,rclick.getY()+yamount);
				}
			}
		}
		mind.updateText(outputLevel());
	}
	//Changes direction of items (either full selection, or item under mouse) to given value
	public void changeDirection(int newDir) {
		if(selection.size() > 0) {
			for(Item it : selection) {
				if(	(it.getType() >= 4 && it.getType() <= 8) ||
					(it.getType() >= 13 && it.getType() <=16) ) {
					((DirectionalItem)it).setDirection(newDir/2);
				}
				if(it.getType()==17) ((Launchpad)it).setDirection(newDir*2*Math.PI/8.0);
			}
		} else {
			if (rclick != null) {
				if(	(rclick.getType() >= 4 && rclick.getType() <= 8) ||
					(rclick.getType() >= 13 && rclick.getType() <=16) ) {
					((DirectionalItem)rclick).setDirection(newDir/2);
				}
				if(rclick.getType()==17) {
					((Launchpad)rclick).setDirection(newDir*2*Math.PI/8.0);
				}
			}
		}
		chDronePaths();
		calcDronePaths();
		mind.updateText(outputLevel());
	}
	//Changes the behavior of selected (or under-mouse) drones to given value
	public void changeBehavior(int newBeh) {
		if(selection.size() > 0) {
			for(Item it : selection) {
				if(it.getType() >= 5 && it.getType() <= 8) {
					((Drone)it).setBehavior(newBeh);
				}
			}
		} else {
			if (rclick != null) {
				if(rclick.getType() >= 5 && rclick.getType() <= 8) {
					((Drone)rclick).setBehavior(newBeh);
				}
			}
		}
		chDronePaths();
		calcDronePaths();
		mind.updateText(outputLevel());
	}
	//Sets the selected (or under-mouse) player to be the active player
	public void setActivePlayer() {
		if(selection.size() > 0) {
			for(Item it : selection) {
				if(it.getType() == 9) {
					thePlayer = items.indexOf(it);
				}
			}
		} else {
			if (rclick != null) {
				if(rclick.getType() == 9) {
					thePlayer = items.indexOf(rclick);
				}
			}
		}
		mind.updateText(outputLevel());
	}
	//Finds all launchpads selected (or under-mouse) for launchpad edit mode and places them in an array
	public void findLaunchpads() {
		pads.clear();
		if(selection.size() > 0) {
			for(Item it : selection) {
				if(it.getType() == 17) {
					pads.add((Launchpad)it);
				}
			}
		} else {
			if (rclick != null) {
				if(rclick.getType() == 17) {
					pads.add((Launchpad)rclick);
				}
			}
		}
		grabPoint = true; 	//Makes it so that the mouse coordinates will be saved to orow/ocol immediately after the right-click menu closes
	}						// This is accomplished using the mouseOn() method
	public void findLPreference() { //Part 2 of findLaunchpads(): runs after the mouse coordinates have been grabbed
		drow = orow - (int)(pads.get(0).getPowerX()*24); 	//Sets the reference point. All mouse move events will use this point as the origin to
		dcol = ocol - (int)(pads.get(0).getPowerY()*24); 	// calculate the new power/direction to set launchpads to. It is originally set to where the
															// position of the (first) launchpad would be if the mouse was at the peak of its power/direction line.
		
	}
}