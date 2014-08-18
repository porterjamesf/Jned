/*
Jned.java
James Porter 01/05/2013

Primary panel of the Jned application. Contains main method.
Acts as the view and controller for the whole application. Contains panels and buttons, mouse interface, and primary paint method.

Screen area is divided up into a grid. Cells of the grid act as panels and buttons in the interface. Certain contiguous square blocks of cells
are delegated to sub-objects, such as the work area of the n map and the text editor.
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.image.BufferedImage;

public class Jned extends JPanel implements ActionListener, MouseListener {
	public static final Color	BUTTON_HL = new Color(255,255,255,96),		//The highlight translucent layer for unpushed buttons
								BUTTON_PDHL = new Color(255,255,255,48),	//The highlight translucent layer for pushed buttons
								BUTTON_DIS = new Color(128,128,128,128),	//The translucent layer for disabled buttons
								BUTTON_SEL = new Color(255,64,64),			//The border drawn around a selected level button
								BG_COLOR = new Color(0x70707F),				//Color used for borders and blank neutral space
								DROP_COLOR = new Color(0x80808F),			//Color of drop-down panels for gridlines and snapping
								DROP_BORDER = new Color(0x40404F),			//Drop-down panel border color
								TILE_FILL = new Color(0x797988),			//Color used for tiles in level
								TILE_SPACE = new Color(0xCACAD0),			//Color used for level area
								TILE_SELECT = new Color(0xF0F0F0),			//Selection box for tile editing box
								SELECTION_BOX = new Color(0xF0F0F0),		//Draggable selection box for items
								PUSHED = new Color(0x2A2A26),				//Pushed button
								PUSHED_BORDER = new Color(0x9A9A96),		//Pushed button border
								PUSHEDTXT = new Color(0x80808F),			//Text on a pushed button
								UNPUSHED = new Color(0xD5D5D9),				//Unpushed button
								UNPUSHED_BORDER = new Color(0x555559),		//Unpushed button border
								UNPUSHEDTXT = new Color(0x20202F),			//Text on an unpushed button
								PANEL_COLOR = new Color(0x9999A8),			//Drop-down panels, other overlay neutral space
								TEXT_COLOR = new Color(0x020205),			//Textbox text color
								PRIMARY_GRID = new Color(0xEFEFF3),			//Primary grid lines
								SECONDARY_GRID = new Color(0xDFDFE3),		//Secondary grid lines
								TERTIARY_GRID = new Color(0xBDBDC1),		//Tertiary grid lines
								SNAP_POINTS = new Color(0x8A8A92),			//Snap point grid
								ITEM = new Color(0x25252F),				//TEMPORARY - Items
								ITEM_GHOST = new Color(0,0,0,128),		//TEMPORARY - Item ghosts (faint version shown for adding mode
								ITEM_HL_A = new Color(255,255,255,64),		//The highlight translucent layer for highlighted items
								ITEM_SELECT_A = new Color(128,128,255,128),	//The highlight translucent layer for selected items
								DOOR_TRIGGER = new Color(121,121,136,128),	//Lines between doors and switches
								DRONE_PATH = new Color(0,0,0,196),			//Lines showing the path a drone will follow
								LAUNCHPAD_LINE = new Color(240,64,64,196);	//Line showing the power and direction of a launchpad
	public static final Font	DEF_FONT = new Font(Font.MONOSPACED,Font.PLAIN,12),	//Font of most text
								BOX_FONT = new Font(Font.MONOSPACED,Font.PLAIN,12); //Font of text box
	protected static final int	DEF_FONT_XOFF = 7,									//Width of each character in the default (monospaced) font
								DEF_FONT_YOFF = 4,									//Distance from center of text to baseline in the default font
								
								DEF_PATHLENGTH = 16,
								
								//FIELDS FOR ITEM MODES
								GAUSS = 0,
								HOMING = 1,
								MINE = 2,
								FLOOR = 3,
								THWUMP = 4,
								ZAP = 8,
								SEEKER = 36,
								LASER = 64,
								CHAINGUN = 92,
								PLAYER = 120,
								GOLD = 121,
								BOUNCE = 122,
								EXIT = 123,
								ONEWAY = 124,
								NDOOR = 128,
								LDOOR = 132,
								TDOOR = 136,
								LAUNCH = 140,
								
								//FIELDS FOR TEXT ALIGNMENT
								LEFT = -1,
								CENTER = 0,
								RIGHT = 1;
	public static final String	BLANK_LEVEL = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000|";
						
	public final int	BORDER = 4,						//Width of padding border on interface panels
						SHORT_BUTTON_HT = 24,			//Height of the short buttons
						TALL_BUTTON = 51,				//Width/height of the square tall buttons
						LVL_SQUARE = 24,				//Screeen size of one tile square in the level
						LVL_AREA_HT = 25*LVL_SQUARE,	//Height of level area = (23 tiles + 2 border tiles)
						LVL_AREA_WD = 33*LVL_SQUARE,	//Width of level area = (31 tiles + 2 border tiles)
						TXT_ED_HT = 211,				//Height of the text portion of the text editor
						TOTAL_WD = BORDER*4+TALL_BUTTON*2+LVL_AREA_WD,	//Calculated width of entire editor window
						CONFIRM_WINDOW = 384,
						DOWN_ARROW_WD = 20;				//Width of the down arrow buttons in the gridlines and snapping buttons
						
	public boolean		ctrl,		//Set to true while either control key is held down
						shift,		//Set to true while either shift key is held down
						alt,		//Set to true while either alt key is held down
						backsp,		//Set to true while the backspace key is held down
						
						drawTriggers,	//Whether or not to draw the door triggers
						drawPaths,		//Whether or not to draw the drone paths
						
						savedAs;		//Whether or not this level has already been saved
						
	public static final String imagesPath = "images/";		//Path to the folder of image files
	private JFrame		freddy;						//Link to the frame, used mainly for exiting the program
	private int			wotxtht,					//The height of the Jned window without the text box below it
						wotxtwd,					//The width of the Jned window without the text box beside it
						tilestate,					//Memory for which direction of tile (QWSA) was last selected
						itemdir,					//Memory for which direction for an item (AWDS) was last selected. Runs in pairs (01=D, 23=S, etc.) second of pair is diagonal for launch pad
						dronebeh,					//Memory for which drone behavior was last selected. 0=surfacecw, 1=surfaceccw, 2=dumbcw ...6=still 
						overlayIndex;				//Memory for which of the gridlines is selected (0 - primary, 1 - secondary, 2 - tertiary)
	private JPanel					tboxpanel;		//The panel containing the text box and text-box-related buttons
	private Pushable[]				buttons;		//Array of buttons, so they can be pushed via menu selections and key shortcuts in addition to just clicking on them. Indices match action numbers.
	private MultiButtonwog[]		mbuttons;		//Array of multibuttons, 0-9 is left column, 10-19 is right column
	private LevelArea				lvl;			//Link to the level area object
	private TextBox					tbox;			//Link to the text area
	private History					hist;			//Link to the history object
	private DropPanel				gridPanel,		//Drop panels. Links used for mouseoff events
									snapPanel;
	private Overlay[] 	grOverlay;					//Link to the three gridlines overlays
	private Overlay		snOverlay;
	private JLabel 		grdblspctxt;				//Links to label and text fields for double line spacing - used to make it invisible when in single line mode
	private JTextField 	grdblspc,
						grsavename,
						snsavename;
	private JComboBox<String>	gridSelect,
								snapSelect;
	private Nfile	config;				//File reader/writer for the config file (gridlines, snap settings, other options) and userlevels file
	private String	userlevels;
	private ImageBank imgBank;
	private KeySignature keys;
	private KeyShortcuts keySetWindow;
	private FileChooser fileChooser;
	private JMenu	mGridSet,			//Links to the menu bar lists of grid and snap settings, so that they can be dynamically updated.
					mSnapSet;
	private JDialog	grSaveDialog,
					grDeleteDialog,
					snSaveDialog,
					snDeleteDialog;
	protected String	lvlName,
						lvlAuthor,
						lvlGenre;
	
	//Constructor: first operation at start of application - sets up entire display, including interface menus and buttons, work area, and text editor
	public Jned (JFrame fred) {
		super();
		setLayout(null);
		setBackground(Jned.BG_COLOR);
		addMouseListener(this);
		freddy = fred;
		tilestate = 0;
		itemdir = 0;
		dronebeh = 1;
		drawTriggers = drawPaths = savedAs = false;
		lvlName = lvlAuthor = lvlGenre = "";
		freddy.setTitle("New level");
		
		imgBank = new ImageBank();
		config = new Nfile("config.txt");
		userlevels = config.getData("fpath");
		
		buttons = new Pushable[90];																																				//BUTTON ARRAY SIZE SET
		hist = new History();
		
		//Sets up key listener
		keys = new KeySignature(this, lvl, config, freddy);
		addKeyListener(keys);
		keySetWindow = new KeyShortcuts(this,freddy,keys,config);
		
		fileChooser = new FileChooser(this,freddy,config,userlevels);
		
		//Top row of buttons for tiles/gridlines/snapping/triggers and undo/redo
		//Tiles button
		int ycount = BORDER, xcount = BORDER;
		Buttonwog tiles = new Buttonwog(this,"tiles",-2,xcount,ycount,2*TALL_BUTTON+BORDER,SHORT_BUTTON_HT,"TILES");
		buttons[1] = tiles;
		add(tiles);
		
		//Gridlines
		xcount += 2*TALL_BUTTON+2*BORDER;
		Buttonwog gridtoggle = new Buttonwog(this,"gridtoggle",-2,xcount,ycount,TALL_BUTTON*2+BORDER-DOWN_ARROW_WD,SHORT_BUTTON_HT,"Gridlines");
		xcount += TALL_BUTTON*2+BORDER-DOWN_ARROW_WD;
		Buttonwog gridlines = new Buttonwog(this,"gridlines",ImageBank.BT_DOWN_ARROW,xcount,ycount,DOWN_ARROW_WD,SHORT_BUTTON_HT);
		xcount += DOWN_ARROW_WD + BORDER;
		  //Presets
		  gridSelect = new JComboBox<String>(config.getNames("grid",1));
		  gridSelect.setBounds(TALL_BUTTON+BORDER,ycount,BORDER+3*TALL_BUTTON,SHORT_BUTTON_HT);
		  gridSelect.addActionListener(this);
		  JLabel grsltxt = makeJLabel("Preset:",0,ycount,TALL_BUTTON+BORDER-3,SHORT_BUTTON_HT,Jned.RIGHT);
		  ycount += SHORT_BUTTON_HT+BORDER;
		  Buttonwog grsave = new Buttonwog(this,"grsave",-2,BORDER,ycount,2*TALL_BUTTON,SHORT_BUTTON_HT,true,"Save");
		  Buttonwog grdelete = new Buttonwog(this,"grdelete",-2,2*BORDER+2*TALL_BUTTON,ycount,2*TALL_BUTTON,SHORT_BUTTON_HT,true,"Delete");
		  //Primary/secondary/tertiary buttons
		  int tbw = (4*TALL_BUTTON - BORDER)/3;
		  ycount += SHORT_BUTTON_HT+BORDER;
		  Buttonwog[] grpst = new Buttonwog[3];
		  grpst[0] = new Buttonwog(this,"grpri",-2,BORDER,ycount,tbw,SHORT_BUTTON_HT,"PRIMARY");
		  grpst[1] = new Buttonwog(this,"grsec",-2,2*BORDER+tbw,ycount,tbw+2,SHORT_BUTTON_HT,"SECONDARY");
		  grpst[2] = new Buttonwog(this,"grter",-2,3*BORDER+2*tbw+2,ycount,tbw,SHORT_BUTTON_HT,"TERTIARY");
		  makeGroup(grpst,true);
		  //Spacing
		  ycount += SHORT_BUTTON_HT+BORDER;
		  JLabel grspacing = makeJLabel("Spacing:",BORDER,ycount,tbw+BORDER-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  JLabel grspcx = makeJLabel("x",2*BORDER+tbw,ycount,SHORT_BUTTON_HT/2-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  JTextField grspcxtxt = new JTextField();
		  grspcxtxt.setFont(Jned.BOX_FONT);
		  grspcxtxt.setHorizontalAlignment(JTextField.RIGHT);
		  grspcxtxt.setBounds(2*BORDER+tbw+SHORT_BUTTON_HT/2,ycount,tbw+1-SHORT_BUTTON_HT/2,SHORT_BUTTON_HT);
		  JLabel grspcy = makeJLabel("y",3*BORDER+2*tbw+1,ycount,SHORT_BUTTON_HT/2-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  JTextField grspcytxt = new JTextField();
		  grspcytxt.setFont(Jned.BOX_FONT);
		  grspcytxt.setHorizontalAlignment(JTextField.RIGHT);
		  grspcytxt.setBounds(3*BORDER+2*tbw+1+SHORT_BUTTON_HT/2,ycount,tbw+1-SHORT_BUTTON_HT/2,SHORT_BUTTON_HT);
		  //Offset
		  ycount += SHORT_BUTTON_HT+BORDER;
		  JLabel groffset = makeJLabel("Offset:",BORDER,ycount,tbw+BORDER-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  JLabel groffx = makeJLabel("x",2*BORDER+tbw,ycount,SHORT_BUTTON_HT/2-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  JTextField groffxtxt = new JTextField();
		  groffxtxt.setFont(Jned.BOX_FONT);
		  groffxtxt.setHorizontalAlignment(JTextField.RIGHT);
		  groffxtxt.setBounds(2*BORDER+tbw+SHORT_BUTTON_HT/2,ycount,tbw+1-SHORT_BUTTON_HT/2,SHORT_BUTTON_HT);
		  JLabel groffy = makeJLabel("y",3*BORDER+2*tbw+1,ycount,SHORT_BUTTON_HT/2-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  JTextField groffytxt = new JTextField();
		  groffytxt.setFont(Jned.BOX_FONT);
		  groffytxt.setHorizontalAlignment(JTextField.RIGHT);
		  groffytxt.setBounds(3*BORDER+2*tbw+1+SHORT_BUTTON_HT/2,ycount,tbw+1-SHORT_BUTTON_HT/2,SHORT_BUTTON_HT);
		  //Symmetry
		  ycount += SHORT_BUTTON_HT+BORDER;
		  String[] gridSymmetries = {"None","Left/Right","Top/Bottom","Quadrants"};
		  JComboBox<String> gridSymm = new JComboBox<String>(gridSymmetries);
		  gridSymm.setBounds(tbw+2*BORDER,ycount,BORDER+2+2*tbw,SHORT_BUTTON_HT);
		  JLabel grsymtxt = makeJLabel("Symmetry:",BORDER,ycount,tbw+BORDER-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  //Single/Double
		  ycount += SHORT_BUTTON_HT+BORDER;
		  JLabel grsingle = makeJLabel("Single",BORDER,ycount,2*TALL_BUTTON-SHORT_BUTTON_HT-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  Buttonwog[] singdub = new Buttonwog[2];
		  singdub[0] = new Buttonwog(this,"grsinglebt",-2,BORDER+2*TALL_BUTTON-SHORT_BUTTON_HT,ycount,SHORT_BUTTON_HT,SHORT_BUTTON_HT);
		  JLabel grdouble = makeJLabel("Double",2*BORDER+2*TALL_BUTTON,ycount,2*TALL_BUTTON-SHORT_BUTTON_HT-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  singdub[1] = new Buttonwog(this,"grdoublebt",-2,2*BORDER+4*TALL_BUTTON-SHORT_BUTTON_HT,ycount,SHORT_BUTTON_HT,SHORT_BUTTON_HT);
		  makeGroup(singdub,true);
		  //On/off, double spacing
		  ycount += SHORT_BUTTON_HT+BORDER;
		  JLabel gronofftxt = makeJLabel("On/off:",BORDER,ycount,2*TALL_BUTTON-SHORT_BUTTON_HT-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  Buttonwog gronoff = new Buttonwog(this,"gronoff",-2,BORDER+2*TALL_BUTTON-SHORT_BUTTON_HT,ycount,SHORT_BUTTON_HT,SHORT_BUTTON_HT);
		  grdblspctxt = makeJLabel("Spacing",2*BORDER+2*TALL_BUTTON,ycount,TALL_BUTTON+2*BORDER-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  grdblspctxt.setVisible(false);
		  grdblspc = new JTextField();
		  grdblspc.setFont(Jned.BOX_FONT);
		  grdblspc.setHorizontalAlignment(JTextField.RIGHT);
		  grdblspc.setBounds(4*BORDER+3*TALL_BUTTON,ycount,TALL_BUTTON-2*BORDER,SHORT_BUTTON_HT);
		  grdblspc.setVisible(false);
		 gridPanel = new DropPanel(gridtoggle.getX(),gridtoggle.getY()+SHORT_BUTTON_HT,3*BORDER+4*TALL_BUTTON,ycount+SHORT_BUTTON_HT+BORDER,gridlines);
		 gridPanel.add(grsltxt);
		 gridPanel.add(gridSelect);
		 gridPanel.add(grsave);
		 gridPanel.add(grdelete);
		 gridPanel.add(grpst[0]);
		 gridPanel.add(grpst[1]);
		 gridPanel.add(grpst[2]);
		 gridPanel.add(grspacing);
		 gridPanel.add(grspcx);
		 gridPanel.add(grspcxtxt);
		 gridPanel.add(grspcy);
		 gridPanel.add(grspcytxt);
		 gridPanel.add(groffset);
		 gridPanel.add(groffx);
		 gridPanel.add(groffxtxt);
		 gridPanel.add(groffy);
		 gridPanel.add(groffytxt);
		 gridPanel.add(grsymtxt);
		 gridPanel.add(gridSymm);
		 gridPanel.add(grsingle);
		 gridPanel.add(singdub[0]);
		 gridPanel.add(grdouble);
		 gridPanel.add(singdub[1]);
		 gridPanel.add(gronofftxt);
		 gridPanel.add(gronoff);
		 gridPanel.add(grdblspctxt);
		 gridPanel.add(grdblspc);
		grOverlay = new Overlay[3];
		grOverlay[0] = new Overlay(grspcxtxt,grspcytxt,groffxtxt,groffytxt,gridSymm,singdub[0],singdub[1],grdblspc,gronoff,true,LVL_AREA_WD-2*LVL_SQUARE,LVL_AREA_HT-2*LVL_SQUARE,this);
		grOverlay[1] = new Overlay(grspcxtxt,grspcytxt,groffxtxt,groffytxt,gridSymm,singdub[0],singdub[1],grdblspc,gronoff,false,LVL_AREA_WD-2*LVL_SQUARE,LVL_AREA_HT-2*LVL_SQUARE,this);
		grOverlay[2] = new Overlay(grspcxtxt,grspcytxt,groffxtxt,groffytxt,gridSymm,singdub[0],singdub[1],grdblspc,gronoff,false,LVL_AREA_WD-2*LVL_SQUARE,LVL_AREA_HT-2*LVL_SQUARE,this);
		  singdub[0].push();
		  gronoff.push();
		  grpst[0].push();
		//Save dialog
		 grSaveDialog = new JDialog(fred, "Save Gridlines Preset");
		 grSaveDialog.getContentPane().setLayout(null);
		 grSaveDialog.getContentPane().setBackground(Jned.BG_COLOR);
		 ycount = BORDER;
		  JLabel grSaveConf = new JLabel("Type a name for this grid setting:");
		   grSaveConf.setForeground(Color.BLACK);
		   grSaveConf.setBounds(BORDER, ycount, CONFIRM_WINDOW-2*BORDER, SHORT_BUTTON_HT);
		 grSaveDialog.add(grSaveConf);
		 ycount += BORDER + SHORT_BUTTON_HT;
		  grsavename = new JTextField();
		   grsavename.setFont(Jned.BOX_FONT);
		   grsavename.setHorizontalAlignment(JTextField.LEFT);
		   grsavename.setBounds(BORDER,ycount,CONFIRM_WINDOW-2*BORDER,SHORT_BUTTON_HT);
		   grsavename.addActionListener(this);
		 grSaveDialog.add(grsavename);
		 ycount += BORDER + SHORT_BUTTON_HT;
		 Buttonwog grSaveSave = new Buttonwog(this,"grsavesave",-2,BORDER,ycount,2*TALL_BUTTON+BORDER,SHORT_BUTTON_HT,true,"Save");
		 Buttonwog grSaveCancel = new Buttonwog(this,"grsavecancel",-2,CONFIRM_WINDOW-2*BORDER-2*TALL_BUTTON,ycount,2*TALL_BUTTON+BORDER,SHORT_BUTTON_HT,true,"Cancel");
		 grSaveDialog.add(grSaveSave);
		 grSaveDialog.add(grSaveCancel);
		 grSaveDialog.setLocationRelativeTo(null);
		 grSaveDialog.getContentPane().setPreferredSize(new Dimension(CONFIRM_WINDOW,ycount + BORDER + SHORT_BUTTON_HT));
		 grSaveDialog.pack();
		//Delete dialog
		 grDeleteDialog = new JDialog(fred, "Delete Gridlines Preset");
		 grDeleteDialog.getContentPane().setLayout(null);
		 grDeleteDialog.getContentPane().setBackground(Jned.BG_COLOR);
		 ycount = BORDER;
		  JLabel grDeleteConf = new JLabel("Are you sure you want to delete this grid setting?");
		   grDeleteConf.setForeground(Color.BLACK);
		   grDeleteConf.setBounds(BORDER, ycount, CONFIRM_WINDOW-2*BORDER, SHORT_BUTTON_HT);
		 grDeleteDialog.add(grDeleteConf);
		 ycount += BORDER + SHORT_BUTTON_HT;
		 Buttonwog grDeleteDelete = new Buttonwog(this,"grdeletedelete",-2,BORDER,ycount,2*TALL_BUTTON+BORDER,SHORT_BUTTON_HT,true,"Delete");
		 Buttonwog grDeleteCancel = new Buttonwog(this,"grdeletecancel",-2,CONFIRM_WINDOW-2*BORDER-2*TALL_BUTTON,ycount,2*TALL_BUTTON+BORDER,SHORT_BUTTON_HT,true,"Cancel");
		 grDeleteDialog.add(grDeleteDelete);
		 grDeleteDialog.add(grDeleteCancel);
		 grDeleteDialog.setLocationRelativeTo(null);
		 grDeleteDialog.getContentPane().setPreferredSize(new Dimension(CONFIRM_WINDOW,ycount + BORDER + SHORT_BUTTON_HT));
		 grDeleteDialog.pack();
		overlayIndex = 0;
		add(gridPanel);
		buttons[73] = gridtoggle;
		add(gridlines);
		add(gridtoggle);
		
		gridSelect.setSelectedItem("classic l");
		loadGridLines("classic l");
		
		//Snapping
		ycount = BORDER;
		Buttonwog snaptoggle = new Buttonwog(this,"snaptoggle",-2,xcount,ycount,TALL_BUTTON*2+BORDER-DOWN_ARROW_WD,SHORT_BUTTON_HT,"Snapping");
		xcount += TALL_BUTTON*2+BORDER-DOWN_ARROW_WD;
		Buttonwog snapping = new Buttonwog(this,"snapping",ImageBank.BT_DOWN_ARROW,xcount,ycount,DOWN_ARROW_WD,SHORT_BUTTON_HT);
		xcount += DOWN_ARROW_WD + BORDER;
		  snapSelect = new JComboBox<String>(config.getNames("snap",1));
		  snapSelect.setBounds(TALL_BUTTON+BORDER,ycount,BORDER+3*TALL_BUTTON,SHORT_BUTTON_HT);
		  snapSelect.addActionListener(this);
		  JLabel snsltxt = makeJLabel("Preset:",0,ycount,TALL_BUTTON+BORDER-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  ycount += SHORT_BUTTON_HT+BORDER;
		  Buttonwog snsave = new Buttonwog(this,"snsave",-2,BORDER,ycount,2*TALL_BUTTON,SHORT_BUTTON_HT,true,"Save");
		  Buttonwog sndelete = new Buttonwog(this,"sndelete",-2,2*BORDER+2*TALL_BUTTON,ycount,2*TALL_BUTTON,SHORT_BUTTON_HT,true,"Delete");
		  //Spacing
		  ycount += SHORT_BUTTON_HT+BORDER;
		  JLabel snspacing = makeJLabel("Spacing:",BORDER,ycount,tbw+BORDER-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  JLabel snspcx = makeJLabel("x",2*BORDER+tbw,ycount,SHORT_BUTTON_HT/2-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  JTextField snspcxtxt = new JTextField();
		  snspcxtxt.setFont(Jned.BOX_FONT);
		  snspcxtxt.setHorizontalAlignment(JTextField.RIGHT);
		  snspcxtxt.setBounds(2*BORDER+tbw+SHORT_BUTTON_HT/2,ycount,tbw+1-SHORT_BUTTON_HT/2,SHORT_BUTTON_HT);
		  JLabel snspcy = makeJLabel("y",3*BORDER+2*tbw+1,ycount,SHORT_BUTTON_HT/2-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  JTextField snspcytxt = new JTextField();
		  snspcytxt.setFont(Jned.BOX_FONT);
		  snspcytxt.setHorizontalAlignment(JTextField.RIGHT);
		  snspcytxt.setBounds(3*BORDER+2*tbw+1+SHORT_BUTTON_HT/2,ycount,tbw+1-SHORT_BUTTON_HT/2,SHORT_BUTTON_HT);
		  //Offset
		  ycount += SHORT_BUTTON_HT+BORDER;
		  JLabel snoffset = makeJLabel("Offset:",BORDER,ycount,tbw+BORDER-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  JLabel snoffx = makeJLabel("x",2*BORDER+tbw,ycount,SHORT_BUTTON_HT/2-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  JTextField snoffxtxt = new JTextField();
		  snoffxtxt.setFont(Jned.BOX_FONT);
		  snoffxtxt.setHorizontalAlignment(JTextField.RIGHT);
		  snoffxtxt.setBounds(2*BORDER+tbw+SHORT_BUTTON_HT/2,ycount,tbw+1-SHORT_BUTTON_HT/2,SHORT_BUTTON_HT);
		  JLabel snoffy = makeJLabel("y",3*BORDER+2*tbw+1,ycount,SHORT_BUTTON_HT/2-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  JTextField snoffytxt = new JTextField();
		  snoffytxt.setFont(Jned.BOX_FONT);
		  snoffytxt.setHorizontalAlignment(JTextField.RIGHT);
		  snoffytxt.setBounds(3*BORDER+2*tbw+1+SHORT_BUTTON_HT/2,ycount,tbw+1-SHORT_BUTTON_HT/2,SHORT_BUTTON_HT);
		  //Symmetry
		  ycount += SHORT_BUTTON_HT+BORDER;
		  JComboBox<String> snapSymm = new JComboBox<String>(gridSymmetries);
		  snapSymm.setBounds(tbw+2*BORDER,ycount,BORDER+2+2*tbw,SHORT_BUTTON_HT);
		  JLabel snsymtxt = makeJLabel("Symmetry:",BORDER,ycount,tbw+BORDER-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  //Show/hide
		  ycount += SHORT_BUTTON_HT+BORDER;
		  JLabel snshowtxt = makeJLabel("Show snap points:",BORDER,ycount,4*TALL_BUTTON+BORDER-SHORT_BUTTON_HT-4,SHORT_BUTTON_HT,Jned.RIGHT);
		  Buttonwog snshow = new Buttonwog(this,"snshow",-2,2*BORDER+4*TALL_BUTTON-SHORT_BUTTON_HT,ycount,SHORT_BUTTON_HT,SHORT_BUTTON_HT);
		 snapPanel = new DropPanel(snaptoggle.getX(),snaptoggle.getY()+SHORT_BUTTON_HT,3*BORDER+4*TALL_BUTTON,ycount+SHORT_BUTTON_HT+BORDER,snapping);
		 snapPanel.add(snsltxt);
		 snapPanel.add(snapSelect);
		 snapPanel.add(snsave);
		 snapPanel.add(sndelete);
		 snapPanel.add(snspacing);
		 snapPanel.add(snspcx);
		 snapPanel.add(snspcxtxt);
		 snapPanel.add(snspcy);
		 snapPanel.add(snspcytxt);
		 snapPanel.add(snoffset);
		 snapPanel.add(snoffx);
		 snapPanel.add(snoffxtxt);
		 snapPanel.add(snoffy);
		 snapPanel.add(snoffytxt);
		 snapPanel.add(snsymtxt);
		 snapPanel.add(snapSymm);
		 snapPanel.add(snshowtxt);
		 snapPanel.add(snshow);
		 buttons[79] = snshow;
		snOverlay = new Overlay(snspcxtxt,snspcytxt,snoffxtxt,snoffytxt,snapSymm,null,null,null,null,true,LVL_AREA_WD,LVL_AREA_HT,this);
		//Save dialog
		 snSaveDialog = new JDialog(fred, "Save Snap Preset");
		 snSaveDialog.getContentPane().setLayout(null);
		 snSaveDialog.getContentPane().setBackground(Jned.BG_COLOR);
		 ycount = BORDER;
		  JLabel snSaveConf = new JLabel("Type a name for this snap setting:");
		   snSaveConf.setForeground(Color.BLACK);
		   snSaveConf.setBounds(BORDER, ycount, CONFIRM_WINDOW-2*BORDER, SHORT_BUTTON_HT);
		 snSaveDialog.add(snSaveConf);
		 ycount += BORDER + SHORT_BUTTON_HT;
		  snsavename = new JTextField();
		   snsavename.setFont(Jned.BOX_FONT);
		   snsavename.setHorizontalAlignment(JTextField.LEFT);
		   snsavename.setBounds(BORDER,ycount,CONFIRM_WINDOW-2*BORDER,SHORT_BUTTON_HT);
		   snsavename.addActionListener(this);
		 snSaveDialog.add(snsavename);
		 ycount += BORDER + SHORT_BUTTON_HT;
		 Buttonwog snSaveSave = new Buttonwog(this,"snsavesave",-2,BORDER,ycount,2*TALL_BUTTON+BORDER,SHORT_BUTTON_HT,true,"Save");
		 Buttonwog snSaveCancel = new Buttonwog(this,"snsavecancel",-2,CONFIRM_WINDOW-2*BORDER-2*TALL_BUTTON,ycount,2*TALL_BUTTON+BORDER,SHORT_BUTTON_HT,true,"Cancel");
		 snSaveDialog.add(snSaveSave);
		 snSaveDialog.add(snSaveCancel);
		 snSaveDialog.setLocationRelativeTo(null);
		 snSaveDialog.getContentPane().setPreferredSize(new Dimension(CONFIRM_WINDOW,ycount + BORDER + SHORT_BUTTON_HT));
		 snSaveDialog.pack();
		//Delete dialog
		 snDeleteDialog = new JDialog(fred, "Delete Snap Preset");
		 snDeleteDialog.getContentPane().setLayout(null);
		 snDeleteDialog.getContentPane().setBackground(Jned.BG_COLOR);
		 ycount = BORDER;
		  JLabel snDeleteConf = new JLabel("Are you sure you want to delete this snap setting?");
		   snDeleteConf.setForeground(Color.BLACK);
		   snDeleteConf.setBounds(BORDER, ycount, CONFIRM_WINDOW-2*BORDER, SHORT_BUTTON_HT);
		 snDeleteDialog.add(snDeleteConf);
		 ycount += BORDER + SHORT_BUTTON_HT;
		 Buttonwog snDeleteDelete = new Buttonwog(this,"sndeletedelete",-2,BORDER,ycount,2*TALL_BUTTON+BORDER,SHORT_BUTTON_HT,true,"Delete");
		 Buttonwog snDeleteCancel = new Buttonwog(this,"sndeletecancel",-2,CONFIRM_WINDOW-2*BORDER-2*TALL_BUTTON,ycount,2*TALL_BUTTON+BORDER,SHORT_BUTTON_HT,true,"Cancel");
		 snDeleteDialog.add(snDeleteDelete);
		 snDeleteDialog.add(snDeleteCancel);
		 snDeleteDialog.setLocationRelativeTo(null);
		 snDeleteDialog.getContentPane().setPreferredSize(new Dimension(CONFIRM_WINDOW,ycount + BORDER + SHORT_BUTTON_HT));
		 snDeleteDialog.pack();
		add(snapPanel);
		buttons[76] = snaptoggle;
		add(snapping);
		add(snaptoggle);
		
		snapSelect.setSelectedItem("classic x");
		loadSnapPoints("classic x");
		
		//Show/hide buttons for triggers and paths
		ycount = BORDER;
		Buttonwog trigger = new Buttonwog(this,"trigger",-2,xcount,ycount,2*TALL_BUTTON + BORDER,SHORT_BUTTON_HT,"Door triggers");
		xcount += 2*TALL_BUTTON + 2*BORDER;
		Buttonwog dpath = new Buttonwog(this,"dpath",-2,xcount,ycount,2*TALL_BUTTON + BORDER,SHORT_BUTTON_HT,"Drone paths");	
		buttons[60] = trigger;
		buttons[63] = dpath;
		add(trigger);
		add(dpath);
		
		//Undo and redo buttons
		xcount = LVL_AREA_WD-2*TALL_BUTTON; //End minus the space for two buttons (BDR + TB+BDR+TB + BDR + LVL_AREA_WD  -  TB+BDR+TB - BDR - TB+BDR+TB)
		Buttonwog undo = new Buttonwog(this,"undo",-2,xcount,ycount,2*TALL_BUTTON+BORDER,SHORT_BUTTON_HT,true,"Undo");
		xcount += 2*TALL_BUTTON+2*BORDER;
		Buttonwog redo = new Buttonwog(this,"redo",-2,xcount,ycount,2*TALL_BUTTON+BORDER,SHORT_BUTTON_HT,true,"Redo"); 
		buttons[4] = undo;
		buttons[5] = redo;
		add(undo);
		add(redo);
		
		//Items button
		xcount = BORDER;
		ycount += SHORT_BUTTON_HT + BORDER;
		Buttonwog items = new Buttonwog(this,"items",-2,xcount,ycount,2*TALL_BUTTON+BORDER,SHORT_BUTTON_HT,"ITEMS");
		buttons[2] = items;
		add(items);
		
		//Sets up the level area		
		xcount += 2*TALL_BUTTON+2*BORDER;
		lvl = new LevelArea(xcount,ycount,LVL_AREA_WD,LVL_AREA_HT,LVL_SQUARE,this);
		lvl.setOverlay(0,grOverlay[0]);
		lvl.setOverlay(1,grOverlay[1]);
		lvl.setOverlay(2,grOverlay[2]);
		lvl.setOverlay(3,snOverlay);
		add(lvl);
		
		//Enemies button
		xcount = BORDER;
		ycount += SHORT_BUTTON_HT + BORDER;
		Buttonwog enemies = new Buttonwog(this,"enemies",-2,xcount,ycount,2*TALL_BUTTON+BORDER,SHORT_BUTTON_HT,"ENEMIES");
		buttons[3] = enemies;
		add(enemies);
		
		//Columns
		mbuttons = new MultiButtonwog[20];
		ycount += SHORT_BUTTON_HT + BORDER;
		
		//Left 0 - 45 degree slope tile, Gauss turret, Player
		Buttonwog tile45 = new Buttonwog(this,"45tile",ImageBank.BT_TILE45,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_HALF);
		Buttonwog gaussturret = new Buttonwog(this,"Gaussturret",ImageBank.BT_GAUSS,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog player = new Buttonwog(this,"Player",ImageBank.BT_PLAYER,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] left0 = {tile45, gaussturret, player};
		add(mbuttons[0] = new MultiButtonwog(left0,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[11] = tile45;
		buttons[21] = player;
		buttons[31] = gaussturret;
		xcount += TALL_BUTTON + BORDER;
		
		//Right 0 - First 8 tiles in rotation Q; Thwump and drones in rotation D; Exit door, Oneway-platforms doors and launchpad in rotation D
		//Tile sub-menus:
		Buttonwog tile45Q = new Buttonwog(this,"45tileQ",ImageBank.BT_TILE45+6,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_HALF+6);
		Buttonwog thintile63Q = new Buttonwog(this,"63thintileQ",ImageBank.BT_THIN63+6,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THICK63+6);
		Buttonwog thintile27Q = new Buttonwog(this,"27thintileQ",ImageBank.BT_THIN27+6,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THICK27+6);
		Buttonwog concavetileQ = new Buttonwog(this,"ConcavetileQ",ImageBank.BT_CONCAVE+6,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_CONVEX+6);
		Buttonwog halftileQ = new Buttonwog(this,"HalftileQ",ImageBank.BT_HALF+6,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_TILE45+6);
		Buttonwog thicktile63Q = new Buttonwog(this,"63thicktileQ",ImageBank.BT_THICK63+6,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THIN63+6);
		Buttonwog thicktile27Q = new Buttonwog(this,"27thicktileQ",ImageBank.BT_THICK27+6,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THIN27+6);
		Buttonwog convextileQ = new Buttonwog(this,"ConvextileQ",ImageBank.BT_CONVEX+6,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_CONCAVE+6);
		//Enemies sub-menus
		Buttonwog thwumpD = new Buttonwog(this,"ThwumpD",ImageBank.BT_THWUMP,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog zapdroneD = new Buttonwog(this,"ZapdroneD",ImageBank.BT_ZAP,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog seekerdroneD = new Buttonwog(this,"SeekerdroneD",ImageBank.BT_SEEKER,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog laserdroneD = new Buttonwog(this,"LaserdroneD",ImageBank.BT_LASER,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog chaingundroneD = new Buttonwog(this,"ChaingundroneD",ImageBank.BT_CHAINGUN,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog onewayD = new Buttonwog(this,"OnewayD",ImageBank.BT_ONEWAY,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog normaldoorD = new Buttonwog(this,"NormaldoorD",ImageBank.BT_NDOOR,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog lockeddoorD = new Buttonwog(this,"LockeddoorD",ImageBank.BT_LDOOR,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog trapdoorD = new Buttonwog(this,"TrapdoorD",ImageBank.BT_TDOOR,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog launchpadD = new Buttonwog(this,"LaunchpadD",ImageBank.BT_LAUNCH,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] right0ar = {	tile45Q, thintile63Q,thintile27Q, concavetileQ, halftileQ, thicktile63Q, thicktile27Q, convextileQ, null, null,	//10 selections of tile menus
									null, null, null, null, thwumpD, zapdroneD, seekerdroneD, laserdroneD, chaingundroneD, 							//9 selections of enemies menus
									null, null, null, null, onewayD, normaldoorD, lockeddoorD, trapdoorD, launchpadD};								//9 selections of objects menus
		add(mbuttons[10] = new MultiButtonwog(right0ar,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[41] = buttons[45] = mbuttons[10];
		ycount += TALL_BUTTON + BORDER;
		xcount = BORDER;
		
		//Left 1 - Thin steep slope tile, Homing missile launcher, Gold
		Buttonwog thintile63 = new Buttonwog(this,"63thintile",ImageBank.BT_THIN63,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THICK63);
		Buttonwog hominglauncher = new Buttonwog(this,"Hominglauncher",ImageBank.BT_HOMING,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog gold = new Buttonwog(this,"Gold",ImageBank.BT_GOLD,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] left1 = {thintile63, hominglauncher, gold};
		add(mbuttons[1] = new MultiButtonwog(left1,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[12] = thintile63;
		buttons[22] = gold;
		buttons[32] = hominglauncher;
		xcount += TALL_BUTTON + BORDER;
		
		//Right 1 - First 8 tiles in rotation W; Thwump and drones in rotation S; Exit switch, Oneway-platforms and doors in rotation S, launchpad in rotation DS
		//Tile sub-menus:
		Buttonwog tile45W = new Buttonwog(this,"45tileW",ImageBank.BT_TILE45,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_HALF);
		Buttonwog thintile63W = new Buttonwog(this,"63thintileW",ImageBank.BT_THIN63,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THICK63);
		Buttonwog thintile27W = new Buttonwog(this,"27thintileW",ImageBank.BT_THIN27,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THICK27);
		Buttonwog concavetileW = new Buttonwog(this,"ConcavetileW",ImageBank.BT_CONCAVE,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_CONVEX);
		Buttonwog halftileW = new Buttonwog(this,"HalftileW",ImageBank.BT_HALF,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_TILE45);
		Buttonwog thicktile63W = new Buttonwog(this,"63thicktileW",ImageBank.BT_THICK63,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THIN63);
		Buttonwog thicktile27W = new Buttonwog(this,"27thicktileW",ImageBank.BT_THICK27,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THIN27);
		Buttonwog convextileW = new Buttonwog(this,"ConvextileW",ImageBank.BT_CONVEX,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_CONCAVE);
		//Enemies sub-menus
		Buttonwog thwumpS = new Buttonwog(this,"ThwumpS",ImageBank.BT_THWUMP+2,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog zapdroneS = new Buttonwog(this,"ZapdroneS",ImageBank.BT_ZAP+2,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog seekerdroneS = new Buttonwog(this,"SeekerdroneS",ImageBank.BT_SEEKER+2,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog laserdroneS = new Buttonwog(this,"LaserdroneS",ImageBank.BT_LASER+2,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog chaingundroneS = new Buttonwog(this,"ChaingundroneS",ImageBank.BT_CHAINGUN+2,0,0,TALL_BUTTON,TALL_BUTTON);
		//Objects sub-menus
		Buttonwog onewayS = new Buttonwog(this,"OnewayS",ImageBank.BT_ONEWAY+2,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog normaldoorS = new Buttonwog(this,"NormaldoorS",ImageBank.BT_NDOOR+2,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog lockeddoorS = new Buttonwog(this,"LockeddoorS",ImageBank.BT_LDOOR+2,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog trapdoorS = new Buttonwog(this,"TrapdoorS",ImageBank.BT_TDOOR+2,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog launchpadDS = new Buttonwog(this,"LaunchpadDS",ImageBank.BT_LAUNCH+2,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] right1ar = {	tile45W, thintile63W,thintile27W, concavetileW, halftileW, thicktile63W, thicktile27W, convextileW, null, null,
									null, null, null, null, thwumpS, zapdroneS, seekerdroneS, laserdroneS, chaingundroneS,
									null, null, null, null, onewayS, normaldoorS, lockeddoorS, trapdoorS, launchpadDS};
		add(mbuttons[11] = new MultiButtonwog(right1ar,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[42] = buttons[46] = mbuttons[11];
		ycount += TALL_BUTTON + BORDER;
		xcount = BORDER;
		
		//Left 2 - Thin shallow slope tile, Mine, Bounce block
		Buttonwog thintile27 = new Buttonwog(this,"27thintile",ImageBank.BT_THIN27,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THICK27);
		Buttonwog mine = new Buttonwog(this,"Mine",ImageBank.BT_MINE,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog bounceblock = new Buttonwog(this,"Bounceblock",ImageBank.BT_BOUNCE,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] left2 = {thintile27, mine, bounceblock};
		add(mbuttons[2] = new MultiButtonwog(left2,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[13] = thintile27;
		buttons[23] = bounceblock;
		buttons[33] = mine;
		xcount += TALL_BUTTON + BORDER;
		
		//Right 2 - First 8 tiles in rotation A; Thwump and drones in rotation A; Oneway-platforms and doors in rotation A, launchpad in rotation S
		//Tile sub-menus:
		Buttonwog tile45A = new Buttonwog(this,"45tileA",ImageBank.BT_TILE45+4,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_HALF+4);
		Buttonwog thintile63A = new Buttonwog(this,"63thintileA",ImageBank.BT_THIN63+4,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THICK63+4);
		Buttonwog thintile27A = new Buttonwog(this,"27thintileA",ImageBank.BT_THIN27+4,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THICK27+4);
		Buttonwog concavetileA = new Buttonwog(this,"ConcavetileA",ImageBank.BT_CONCAVE+4,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_CONVEX+4);
		Buttonwog halftileA = new Buttonwog(this,"HalftileA",ImageBank.BT_HALF+4,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_TILE45+4);
		Buttonwog thicktile63A = new Buttonwog(this,"63thicktileA",ImageBank.BT_THICK63+4,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THIN63+4);
		Buttonwog thicktile27A = new Buttonwog(this,"27thicktileA",ImageBank.BT_THICK27+4,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THIN27+4);
		Buttonwog convextileA = new Buttonwog(this,"ConvextileA",ImageBank.BT_CONVEX+4,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_CONCAVE+4);
		//Enemies sub-menus
		Buttonwog thwumpA = new Buttonwog(this,"ThwumpA",ImageBank.BT_THWUMP+4,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog zapdroneA = new Buttonwog(this,"ZapdroneA",ImageBank.BT_ZAP+4,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog seekerdroneA = new Buttonwog(this,"SeekerdroneA",ImageBank.BT_SEEKER+4,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog laserdroneA = new Buttonwog(this,"LaserdroneA",ImageBank.BT_LASER+4,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog chaingundroneA = new Buttonwog(this,"ChaingundroneA",ImageBank.BT_CHAINGUN+4,0,0,TALL_BUTTON,TALL_BUTTON);
		//Objects sub-menus
		Buttonwog onewayA = new Buttonwog(this,"OnewayA",ImageBank.BT_ONEWAY+4,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog normaldoorA = new Buttonwog(this,"NormaldoorA",ImageBank.BT_NDOOR+4,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog lockeddoorA = new Buttonwog(this,"LockeddoorA",ImageBank.BT_LDOOR+4,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog trapdoorA = new Buttonwog(this,"TrapdoorA",ImageBank.BT_TDOOR+4,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog launchpadS = new Buttonwog(this,"LaunchpadS",ImageBank.BT_LAUNCH+4,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] right2ar = {	tile45A, thintile63A,thintile27A, concavetileA, halftileA, thicktile63A, thicktile27A, convextileA, null, null,
									null, null, null, null, thwumpA, zapdroneA, seekerdroneA, laserdroneA, chaingundroneA,
									null, null, null, null, onewayA, normaldoorA, lockeddoorA, trapdoorA, launchpadS};
		add(mbuttons[12] = new MultiButtonwog(right2ar,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[43] = buttons[47] = mbuttons[12];
		ycount += TALL_BUTTON + BORDER;
		xcount = BORDER;
		
		//Left 3 - Concave tile, Floor guard, Exit Door
		Buttonwog concavetile = new Buttonwog(this,"Concavetile",ImageBank.BT_CONCAVE,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_CONVEX);
		Buttonwog floorguard = new Buttonwog(this,"Floorguard",ImageBank.BT_FLOOR,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog exit = new Buttonwog(this,"Exitdoor",ImageBank.BT_EXIT,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] left3 = {concavetile, floorguard, exit};
		add(mbuttons[3] = new MultiButtonwog(left3,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[14] = concavetile;
		buttons[24] = exit;
		buttons[34] = floorguard;
		xcount += TALL_BUTTON + BORDER;
		
		//Right 3 - First 8 tiles in rotation S; Thwump and drones in rotation W; Oneway-platforms and doors in rotation W, launchpad in rotation AS
		//Tile sub-menus:
		Buttonwog tile45S = new Buttonwog(this,"45tileS",ImageBank.BT_TILE45+2,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_HALF+2);
		Buttonwog thintile63S = new Buttonwog(this,"63thintileS",ImageBank.BT_THIN63+2,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THICK63+2);
		Buttonwog thintile27S = new Buttonwog(this,"27thintileS",ImageBank.BT_THIN27+2,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THICK27+2);
		Buttonwog concavetileS = new Buttonwog(this,"ConcavetileS",ImageBank.BT_CONCAVE+2,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_CONVEX+2);
		Buttonwog halftileS = new Buttonwog(this,"HalftileS",ImageBank.BT_HALF+2,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_TILE45+2);
		Buttonwog thicktile63S = new Buttonwog(this,"63thicktileS",ImageBank.BT_THICK63+2,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THIN63+2);
		Buttonwog thicktile27S = new Buttonwog(this,"27thicktileS",ImageBank.BT_THICK27+2,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THIN27+2);
		Buttonwog convextileS = new Buttonwog(this,"ConvextileS",ImageBank.BT_CONVEX+2,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_CONCAVE+2);
		//Enemies sub-menus
		Buttonwog thwumpW = new Buttonwog(this,"ThwumpW",ImageBank.BT_THWUMP+6,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog zapdroneW = new Buttonwog(this,"ZapdroneW",ImageBank.BT_ZAP+6,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog seekerdroneW = new Buttonwog(this,"WeekerdroneW",ImageBank.BT_SEEKER+6,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog laserdroneW = new Buttonwog(this,"LaserdroneW",ImageBank.BT_LASER+6,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog chaingundroneW = new Buttonwog(this,"ChaingundroneW",ImageBank.BT_CHAINGUN+6,0,0,TALL_BUTTON,TALL_BUTTON);
		//Objects sub-menus
		Buttonwog onewayW = new Buttonwog(this,"OnewayW",ImageBank.BT_ONEWAY+6,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog normaldoorW = new Buttonwog(this,"NormaldoorW",ImageBank.BT_NDOOR+6,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog lockeddoorW = new Buttonwog(this,"LockeddoorW",ImageBank.BT_LDOOR+6,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog trapdoorW = new Buttonwog(this,"TrapdoorW",ImageBank.BT_TDOOR+6,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog launchpadAS = new Buttonwog(this,"LaunchpadAS",ImageBank.BT_LAUNCH+6,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] right3ar = {	tile45S, thintile63S,thintile27S, concavetileS, halftileS, thicktile63S, thicktile27S, convextileS, null, null,
									null, null, null, null, thwumpW, zapdroneW, seekerdroneW, laserdroneW, chaingundroneW,
									null, null, null, null, onewayW, normaldoorW, lockeddoorW, trapdoorW, launchpadAS};
		add(mbuttons[13] = new MultiButtonwog(right3ar,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[44] = buttons[48] = mbuttons[13];
		ycount += TALL_BUTTON + BORDER;
		xcount = BORDER;
		
		//Left 4 - Half tile, Thwump, Oneway platform
		Buttonwog halftile = new Buttonwog(this,"Halftile",ImageBank.BT_HALF,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_TILE45);
		Buttonwog thwump = new Buttonwog(this,"Thwump",ImageBank.BT_THWUMP,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog oneway = new Buttonwog(this,"Oneway",ImageBank.BT_ONEWAY+6,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] left4 = {halftile, thwump, oneway};
		add(mbuttons[4] = new MultiButtonwog(left4,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[15] = halftile;
		buttons[25] = oneway;
		buttons[35] = thwump;
		xcount += TALL_BUTTON + BORDER;
		
		//Right 4 - No tiles; Drones behavior - surface CW; Launchpad in rotation D
		//Enemies sub-menus
		Buttonwog zapdronesurfacecw = new Buttonwog(this,"Zapdronesurfacecw",ImageBank.BT_ZAP+8,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog seekerdronesurfacecw = new Buttonwog(this,"Seekerdronesurfacecw",ImageBank.BT_SEEKER+8,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog laserdronesurfacecw = new Buttonwog(this,"Laserdronesurfacecw",ImageBank.BT_LASER+8,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog chaingundronesurfacecw = new Buttonwog(this,"Chaingundronesurfacecw",ImageBank.BT_CHAINGUN+8,0,0,TALL_BUTTON,TALL_BUTTON);
		//Objects sub-menus
		Buttonwog launchpadA = new Buttonwog(this,"LaunchpadA",ImageBank.BT_LAUNCH+8,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] right4ar = {	null, null, null, null, null, null, null, null, null, null,
									null, null, null, null, null, zapdronesurfacecw, seekerdronesurfacecw, laserdronesurfacecw, chaingundronesurfacecw,
									null, null, null, null, null, null, null, null, launchpadA};
		add(mbuttons[14] = new MultiButtonwog(right4ar,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[53] = buttons[49] = mbuttons[14];
		ycount += TALL_BUTTON + BORDER;
		xcount = BORDER;
		
		//Left 5 - Thick steep slope tile, Zap drone, Door
		Buttonwog thicktile63 = new Buttonwog(this,"63thicktile",ImageBank.BT_THICK63,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THIN63);
		Buttonwog zapdrone = new Buttonwog(this,"Zapdrone",ImageBank.BT_ZAP,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog normaldoor = new Buttonwog(this,"Normaldoor",ImageBank.BT_NDOOR,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] left5 = {thicktile63, zapdrone, normaldoor};
		add(mbuttons[5] = new MultiButtonwog(left5,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[16] = thicktile63;
		buttons[26] = normaldoor;
		buttons[36] = zapdrone;
		xcount += TALL_BUTTON + BORDER;
		
		//Right 5 - No tiles; Drones behavior - surface CCW; Launchpad in rotation DS
		//Enemies sub-menus
		Buttonwog zapdronesurfaceccw = new Buttonwog(this,"Zapdronesurfaceccw",ImageBank.BT_ZAP+10,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog seekerdronesurfaceccw = new Buttonwog(this,"Seekerdronesurfaceccw",ImageBank.BT_SEEKER+10,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog laserdronesurfaceccw = new Buttonwog(this,"Laserdronesurfaceccw",ImageBank.BT_LASER+10,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog chaingundronesurfaceccw = new Buttonwog(this,"Chaingundronesurfaceccw",ImageBank.BT_CHAINGUN+10,0,0,TALL_BUTTON,TALL_BUTTON);
		//Objects sub-menus
		Buttonwog launchpadAW = new Buttonwog(this,"LaunchpadAW",ImageBank.BT_LAUNCH+10,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] right5ar = {	null, null, null, null, null, null, null, null, null, null,	
									null, null, null, null, null, zapdronesurfaceccw, seekerdronesurfaceccw, laserdronesurfaceccw, chaingundronesurfaceccw, 
									null, null, null, null, null, null, null, null, launchpadAW};
		add(mbuttons[15] = new MultiButtonwog(right5ar,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[54] = buttons[50] = mbuttons[15];
		ycount += TALL_BUTTON + BORDER;
		xcount = BORDER;
		
		//Left 6 - Thick shallow slope tile, Seeker zap drone, Locked door
		Buttonwog thicktile27 = new Buttonwog(this,"27thicktile",ImageBank.BT_THICK27,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_THIN27);
		Buttonwog seekerdrone = new Buttonwog(this,"Seekerdrone",ImageBank.BT_SEEKER,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog lockeddoor = new Buttonwog(this,"Lockeddoor",ImageBank.BT_LDOOR+8,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] left6 = {thicktile27, seekerdrone, lockeddoor};
		add(mbuttons[6] = new MultiButtonwog(left6,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[17] = thicktile27;
		buttons[27] = lockeddoor;
		buttons[37] = seekerdrone;
		xcount += TALL_BUTTON + BORDER;
		
		//Right 6 - No tiles; Drones behavior - dumb CW; Launchpad in rotation S
		//Enemies sub-menus
		Buttonwog zapdronedumbcw = new Buttonwog(this,"Zapdronedumbcw",ImageBank.BT_ZAP+12,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog seekerdronedumbcw = new Buttonwog(this,"Seekerdronedumbcw",ImageBank.BT_SEEKER+12,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog laserdronedumbcw = new Buttonwog(this,"Laserdronedumbcw",ImageBank.BT_LASER+12,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog chaingundronedumbcw = new Buttonwog(this,"Chaingundronedumbcw",ImageBank.BT_CHAINGUN+12,0,0,TALL_BUTTON,TALL_BUTTON);
		//Objects sub-menus
		Buttonwog launchpadW = new Buttonwog(this,"LaunchpadW",ImageBank.BT_LAUNCH+12,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] right6ar = {	null, null, null, null, null, null, null, null, null, null,	
									null, null, null, null, null, zapdronedumbcw, seekerdronedumbcw, laserdronedumbcw, chaingundronedumbcw,
									null, null, null, null, null, null, null, null, launchpadW};
		add(mbuttons[16] = new MultiButtonwog(right6ar,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[55] = buttons[51] = mbuttons[16];
		ycount += TALL_BUTTON + BORDER;
		xcount = BORDER;
		
		//Left 7 - Convex tile, Laserdrone, Trap door
		Buttonwog convextile = new Buttonwog(this,"Convextile",ImageBank.BT_CONVEX,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_CONCAVE);
		Buttonwog laserdrone = new Buttonwog(this,"Laserdrone",ImageBank.BT_LASER,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog trapdoor = new Buttonwog(this,"Trapdoor",ImageBank.BT_TDOOR+8,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] left7 = {convextile, laserdrone, trapdoor};
		add(mbuttons[7] = new MultiButtonwog(left7,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[18] = convextile;
		buttons[28] = trapdoor;
		buttons[38] = laserdrone;
		xcount += TALL_BUTTON + BORDER;
		
		//Right 7 - No tiles; Drones behavior - dumb CCW; Launchpad in rotation SA
		//Enemies sub-menus
		Buttonwog zapdronedumbccw = new Buttonwog(this,"Zapdronedumbccw",ImageBank.BT_ZAP+14,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog seekerdronedumbccw = new Buttonwog(this,"Seekerdronedumbccw",ImageBank.BT_SEEKER+14,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog laserdronedumbccw = new Buttonwog(this,"Laserdronedumbccw",ImageBank.BT_LASER+14,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog chaingundronedumbccw = new Buttonwog(this,"Chaingundronedumbccw",ImageBank.BT_CHAINGUN+14,0,0,TALL_BUTTON,TALL_BUTTON);
		//Objects sub-menus
		Buttonwog launchpadDW = new Buttonwog(this,"LaunchpadDW",ImageBank.BT_LAUNCH+14,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] right7ar = {	null, null, null, null, null, null, null, null, null, null,
									null, null, null, null, null, zapdronedumbccw, seekerdronedumbccw, laserdronedumbccw, chaingundronedumbccw,
									null, null, null, null, null, null, null, null, launchpadDW};
		add(mbuttons[17] = new MultiButtonwog(right7ar,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[56] = buttons[52] = mbuttons[17];
		ycount += TALL_BUTTON + BORDER;
		xcount = BORDER;
		
		//Left 8 - Erase tile, Chaingun drone, Launch pad
		Buttonwog erase = new Buttonwog(this,"Erase",ImageBank.BT_ERASE,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_FILL);
		Buttonwog chaingundrone = new Buttonwog(this,"Chaingundrone",ImageBank.BT_CHAINGUN,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog launchpad = new Buttonwog(this,"Launchpad",ImageBank.BT_LAUNCH+12,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] left8 = {erase, chaingundrone, launchpad};
		add(mbuttons[8] = new MultiButtonwog(left8,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[19] = erase;
		buttons[29] = launchpad;
		buttons[39] = chaingundrone;
		xcount += TALL_BUTTON + BORDER;
		
		//Right 8 - No tiles; Drones behavior - alternate; No objects
		//Enemies sub-menus
		Buttonwog zapdronealt = new Buttonwog(this,"Zapdronealt",ImageBank.BT_ZAP+16,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog seekerdronealt = new Buttonwog(this,"Seekerdronealt",ImageBank.BT_SEEKER+16,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog laserdronealt = new Buttonwog(this,"Laserdronealt",ImageBank.BT_LASER+16,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog chaingundronealt = new Buttonwog(this,"Chaingundronealt",ImageBank.BT_CHAINGUN+16,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] right8ar = {	null, null, null, null, null, null, null, null, null, null,
									null, null, null, null, null, zapdronealt, seekerdronealt, laserdronealt, chaingundronealt, 
									null, null, null, null, null, null, null, null, null};
		add(mbuttons[18] = new MultiButtonwog(right8ar,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[57] = mbuttons[18];
		ycount += TALL_BUTTON + BORDER;
		xcount = BORDER;
		
		//Left 9 - Fill tile, no options for enemies or objects
		Buttonwog fill = new Buttonwog(this,"Fill",ImageBank.BT_FILL,0,0,TALL_BUTTON,TALL_BUTTON,ImageBank.BT_ERASE);
		Buttonwog[] left9 = {fill, null, null};
		add(mbuttons[9] = new MultiButtonwog(left9,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[20] = fill;
		xcount += TALL_BUTTON + BORDER;
	
		//Right 9 - No tiles; Drones behavior - quasi random; No objects
		//Enemies sub-menus
		Buttonwog zapdronerand = new Buttonwog(this,"Zapdronerand",ImageBank.BT_ZAP+18,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog seekerdronerand = new Buttonwog(this,"Seekerdronerand",ImageBank.BT_SEEKER+18,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog laserdronerand = new Buttonwog(this,"Laserdronerand",ImageBank.BT_LASER+18,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog chaingundronerand = new Buttonwog(this,"Chaingundronerand",ImageBank.BT_CHAINGUN+18,0,0,TALL_BUTTON,TALL_BUTTON);
		Buttonwog[] right9ar = {	null, null, null, null, null, null, null, null, null, null,
									null, null, null, null, null, zapdronerand, seekerdronerand, laserdronerand, chaingundronerand,
									null, null, null, null, null, null, null, null, null};
		add(mbuttons[19] = new MultiButtonwog(right9ar,xcount,ycount,TALL_BUTTON,TALL_BUTTON));
		buttons[58] = mbuttons[19];
		ycount += TALL_BUTTON + BORDER;
		xcount = BORDER;

		//Menu button groups
		Buttonwog[][] sewbuttons = {//Top three
									{tiles,items,enemies},
											
									//The tile sub-menus
									{tile45A,tile45Q,tile45S,tile45W},
									{thintile63A,thintile63S,thintile63W,thintile63Q},
									{thintile27A,thintile27S,thintile27W,thintile27Q},
									{concavetileA,concavetileS,concavetileW,concavetileQ},
									{halftileA,halftileQ,halftileS,halftileW},
									{thicktile63A,thicktile63S,thicktile63W,thicktile63Q},
									{thicktile27A,thicktile27S,thicktile27W,thicktile27Q},
									{convextileA,convextileS,convextileW,convextileQ},
									
									//The enemies directional sub-menus (note: drones can have a direction and a behavior pushed at the same time)
									{thwumpA,thwumpW,thwumpD,thwumpS},
									{zapdroneA,zapdroneW,zapdroneD,zapdroneS},
									{seekerdroneA,seekerdroneW,seekerdroneD,seekerdroneS},
									{laserdroneA,laserdroneW,laserdroneD,laserdroneS},
									{chaingundroneA,chaingundroneW,chaingundroneD,chaingundroneS},
																		
									//The objects sub-menus
									{onewayA,onewayW,onewayD,onewayS},
									{normaldoorA,normaldoorW,normaldoorD,normaldoorS},
									{lockeddoorA,lockeddoorW,lockeddoorD,lockeddoorS},
									{trapdoorA,trapdoorW,trapdoorD,trapdoorS},
									{launchpadA,launchpadAW,launchpadW,launchpadDW,launchpadD,launchpadDS,launchpadS,launchpadAS},
									
									//The left menus 
									{tile45,thintile63,thintile27,concavetile,halftile,thicktile63,thicktile27,convextile,erase,fill},
									{gaussturret,hominglauncher,mine,floorguard,thwump,zapdrone,seekerdrone,laserdrone,chaingundrone},
									{player,gold,bounceblock,exit,oneway,normaldoor,lockeddoor,trapdoor,launchpad},
											
									//The drone behavior menus
									{zapdronedumbcw,zapdronedumbccw,zapdronesurfacecw,zapdronesurfaceccw,zapdronealt,zapdronerand},
									{seekerdronedumbcw,seekerdronedumbccw,seekerdronesurfacecw,seekerdronesurfaceccw,seekerdronealt,seekerdronerand},
									{laserdronedumbcw,laserdronedumbccw,laserdronesurfacecw,laserdronesurfaceccw,laserdronealt,laserdronerand},
									{chaingundronedumbcw,chaingundronedumbccw,chaingundronesurfacecw,chaingundronesurfaceccw,chaingundronealt,chaingundronerand}
								};
		
		for (int i = 0; i < sewbuttons.length; i++) {	//The first 19 groups are the 3 mode buttons on the top and the right column. These are all radio-style,
			makeGroup(sewbuttons[i], i<19);				//meaning some button in the group must always be pushed. The following groups are the left column and the
		}												//drone behavior buttons. These are not radio-style. You can select nothing on the left for selection mode,
														//and you can have no drone behavior button selected to make a drone that stands still.

		//To begin with, the tiles menu should be selected
		tiles.push();
		
		//--------END OF MAIN WINDOW----------
		
		wotxtwd = TOTAL_WD;
		wotxtht = ycount;
		//Sets up the text area at the bottom
		tbox = new TextBox(this,0,ycount,TOTAL_WD,TXT_ED_HT + 3*BORDER + SHORT_BUTTON_HT);
		add(tbox.getPane());
		buttons[66] = tbox.tboxButtons[0];
		buttons[67] = tbox.tboxButtons[1];
		buttons[68] = tbox.tboxButtons[2];
		ycount += TXT_ED_HT + 3*BORDER + SHORT_BUTTON_HT;
		
		setPreferredSize(new Dimension(TOTAL_WD, ycount));
	
		//Sets up the menu bar and all menus and menu items
		JMenuBar mbar = new JMenuBar();
		
		 JMenu mFile = new JMenu("File");
		 mFile.add(makeMenuItem("New","new"));
		 mFile.add(makeMenuItem("Open","open"));
		 mFile.add(makeMenuItem("Save","save"));
		 mFile.add(makeMenuItem("Save As","saveAs"));
		 mFile.add(makeMenuItem("Exit","Exit"));
		 
		 JMenu mEdit = new JMenu("Edit");
		 mEdit.add(makeMenuItem("Undo","undo"));
		 mEdit.add(makeMenuItem("Redo","redo"));
		 mEdit.add(makeMenuItem("Cut","action#6"));
		 mEdit.add(makeMenuItem("Copy","action#7"));
		 mEdit.add(makeMenuItem("Paste","action#8"));
		 mEdit.add(makeMenuItem("Delete","action#9"));
		 mEdit.add(makeMenuItem("Select All","action#89"));
		 mEdit.add(makeMenuItem("Snapping","action#78"));
		  mSnapSet = new JMenu("Snap setting");
		  setSnapSettingMenu();
		 mEdit.add(mSnapSet);
		 mEdit.add(makeMenuItem("Keyboard Shortcuts","keyShortcuts"));
		 
		 JMenu mView = new JMenu("View");
		 mView.add(makeMenuItem("Gridlines","action#75"));
		  mGridSet = new JMenu("Gridline setting");
		  setGridlineSettingMenu();
		 mView.add(mGridSet);
		 mView.add(makeMenuItem("Snap points","action#81"));
		 
		mbar.add(mFile);
		mbar.add(mEdit);
		mbar.add(mView);
		freddy.setJMenuBar(mbar);
		
		updateText(lvl.outputLevel());
		
		repaint();
	} //End of constructor
	
	//Constructs a JLabel with the given text and alignment in the given bounds, then returns it. Alignment is defined by fields above
	private JLabel makeJLabel(String txt, int xpos, int ypos, int wid, int hei, int align) {
		JLabel res = new JLabel(txt);
		res.setBounds(xpos, ypos, wid, hei);
		res.setFont(Jned.DEF_FONT);
		res.setForeground(Jned.TEXT_COLOR);
		res.setHorizontalAlignment(align==LEFT?SwingConstants.LEFT:(align==RIGHT?SwingConstants.RIGHT:SwingConstants.CENTER));
		return res;
	}
	private JMenuItem makeMenuItem(String text, String command) {
		JMenuItem mi = new JMenuItem(text);
		mi.addActionListener(this);
		mi.setActionCommand(command);
		return mi;
	}
	
	//Fills out the grilines settings and snap settings menus for the top menu bar
	private void setGridlineSettingMenu() {
		mGridSet.removeAll();
		for(String nom : config.getNames("grid",1)) {
			JMenuItem mi = new JMenuItem(nom);
			mGridSet.add(nom);
		}
		for(int i = 0; i < mGridSet.getItemCount(); i++) { //For some reason, this only works when done in a separate loop.
			JMenuItem mi = mGridSet.getItem(i);
			mi.addActionListener(this);
			mi.setActionCommand("gridlineSetting#" + mi.getText());
		}
	}
	private void setSnapSettingMenu() {
		mSnapSet.removeAll();
		for(String nom : config.getNames("snap",1)) {
			JMenuItem mi = new JMenuItem(nom);
			mSnapSet.add(nom);
		}
		for(int i = 0; i < mSnapSet.getItemCount(); i++) {
			JMenuItem mi = mSnapSet.getItem(i);
			mi.addActionListener(this);
			mi.setActionCommand("snapSetting#" + mi.getText());
		}
	}
	//Retrieves an image
	public BufferedImage img(int index) {
		return imgBank.get(index);
	}
	
	//Pops out the textbox into its own window
	public void popText() {
		remove(tbox.getPane());
		setPreferredSize(new Dimension(wotxtwd, wotxtht));
		freddy.pack();
		
		tbox.popOut();
		repaint();
	}
	//Puts the text box in the main window on the bottom
	public void pushTextBelow() {
		tbox.popIn(true);
		setPreferredSize(new Dimension(wotxtwd, wotxtht + tbox.getHeight()));
		add(tbox.getPane());
		freddy.pack();
		repaint();
	}
	//Puts the text box in the main window on the side
	public void pushTextBeside() {
		tbox.popIn(false);
		setPreferredSize(new Dimension(wotxtwd + tbox.getWidth(), wotxtht));
		add(tbox.getPane());
		freddy.pack();
		repaint();
	}
	
	//Updates the textbox
	public void updateText(String text) {
		if(!hist.current().equals(text)) { //No action if new text is unchanged
			tbox.setText(text);
			hist.add(text);
		}
	}
	
	//Loads a level, including level attributes
	public void loadLevel(String name, String author, String genre, String data) {
		setAttributes(name, author, genre);
		updateText(data);
		push("tboxlvl");
		hist.clear();
	}
	//Starts a new level
	public void newLevel() {
		setAttributes("","","");
		updateText(Jned.BLANK_LEVEL);
		push("tboxlvl");
		hist.clear();
		freddy.setTitle("New level");
		savedAs = false;
	}
	//Highlights an item in the text box, given the starting index of its text
	public void highlightItem(int index) {
		tbox.highlightItem(index);
	}
	public void highlightTile(int index) {
		tbox.highlightTile(index);
	}
	public void unHighlight() {
		tbox.unHighlight();
	}
	//Loads attribute data to the level area
	public void setAttributes(String[] attrs) {
		if(attrs.length >= 3) {
			setAttributes(attrs[0],attrs[1],attrs[2]);
		}
	}
	public void setAttributes(String name, String author, String genre) {
		lvlName = name;
		lvlAuthor = author;
		lvlGenre = genre;
		freddy.setTitle(name + " - " + author + " - " + genre);
	}
	public String[] getAttributes() {
		String[] res = new String[3];
		res[0] = lvlName;
		res[1] = lvlAuthor;
		res[2] = lvlGenre;
		return res;
	}
	
	//Makes a button group out of an array of buttons. Set isRadio to true if you want the group to always have one option pushed
	private void makeGroup(Buttonwog[] group, boolean isRadio) {
		for(int i = 0; i < group.length; i++) {
			if(isRadio) {
				group[i].radio();
				//if(i == 0) group[i].push();
			}
			for(int j = 0; j < group.length; j++) {
				if(i != j) {
					group[i].add(group[j]);
				}
			}
		}
	}
	//Takes action numbers from the KeySignature and maps them to action commands for the push method
	public void doActionNumber (int an) {
		//Grid and snap settings
		if(an >= 256) {
			String[] setting = config.getNames(("" + an),2);
			if(setting.length > 0) {
				String type = config.getAttr1(setting[0]);
				if(type.equals("grid")) {
					gridSelect.setSelectedItem(setting[0]);
					loadGridLines(setting[0]);
				}
				if(type.equals("snap")) {
					snapSelect.setSelectedItem(setting[0]);
					loadSnapPoints(setting[0]);
				}
			}
			return;
		}
		//Filtering of mode-dependant actions:
		int md = lvl.getIntMode();
		if(an >= 53 && an <= 59 && (md < Jned.ZAP || md >= Jned.CHAINGUN + 27)) return; //Drone behavior keys
		if(lvl.getBoolMode()) { //Tile mode
			if(	an >= 21 && an <= 40 ||
				an >= 45 && an <=48 ||
				an >= 53 && an <= 59 ||
				an >= 69 && an <= 72) return;
		} else { //Items or Enemies modes
			if(	an >= 11 && an <= 20 ||
				an >= 41 && an <= 44) return;
			if(an >= 21 && an <= 30) buttons[2].push(); //When in items or enemies modes, the keys for different objects
			if(an >= 31 && an <= 40) buttons[3].push(); //have no conflicts, and should work regardless of which of the two modes you're in
			if(md >= Jned.LAUNCH && md <= Jned.LAUNCH + 7) { //Since the launchpad directions are layed out differently, these action numbers are
				//if(an >= 45 && an <= 52) {						//re-routed to the multibuttons as appropriate when in launchpad mode
				switch(an) {
					case 45: //Right (already correct)
						break;
					case 46: //Down
						an = 47;
						break;
					case 47: //Left
						an = 49;
						break;
					case 48: //Up
						an = 51;
						break;
					case 49: //Right-down
						an = 46;
						break;
					case 50: //Down-left
						an = 48;
						break;
					case 51: //Left-up
						an = 50;
						break;
					case 52: //Up-right (already correct)
						break;
					default: break;
				}
				//}
			}
		}
		if(buttons[an] != null) {
			buttons[an].push();
		} else {
			switch(an) {
				case 6: //Cut
					if(lvl.getBoolMode()) lvl.push("cut");
					else lvl.push("itemCut");
					break;
				case 7: //Copy
					if(lvl.getBoolMode()) lvl.push("copy");
					else lvl.push("itemCopy");
					break;
				case 8: //Paste
					if(lvl.getBoolMode()) lvl.push("paste");
					else lvl.push("itemPaste");
					break;
				case 9: //Delete
					pushDelete();
					break;
				case 10: //Selection mode
					lvl.setMode(-1);
					for (int i = 0; i <= 9; i++) {
						mbuttons[i].unpush();
						mbuttons[i+10].setIndex(8);
					}
					break;
				case 59: //No drone behavior
					for (int i = 53; i <= 58; i++) {
						buttons[i].unpush();
					}
					switch(lvl.getType(lvl.getIntMode())) {
						case 5:
							lvl.setMode(Jned.ZAP+7*itemdir/2+6);
						break;
						case 6:
							lvl.setMode(Jned.SEEKER+7*itemdir/2+6);
						break;
						case 7:
							lvl.setMode(Jned.LASER+7*itemdir/2+6);
						break;
						case 8:
							lvl.setMode(Jned.CHAINGUN+7*itemdir/2+6);
						break;
					}
					dronebeh=6;
					break;
				case 62: //Toggle triggers
					if(!drawTriggers) {
						buttons[60].push();
						break;
					}
					//Intentional continuation into next case
				case 61: //Hide triggers
					buttons[60].unpush();
					drawTriggers = false;
					break;
				case 65: //Toggle drone paths
					if(!drawPaths) {
						buttons[63].push();
						break;
					}
					//Intentional continuation into next case
				case 64: //Hide drone paths
					buttons[63].unpush();
					drawPaths = false;
					break;
				case 69: //Nudge right
					lvl.push("nudgeRight");
					break;
				case 70: //Nudge down
					lvl.push("nudgeDown");
					break;
				case 71: //Nudge left
					lvl.push("nudgeLeft");
					break;
				case 72: //Nudge up
					lvl.push("nudgeUp");
					break;
				case 75: //Toggle gridlines
					if(!lvl.drawGrid()) {
						buttons[73].push();
						break;
					}
					//Intentional continuation into next case
				case 74: //Hide gridlines
					buttons[73].unpush();
					lvl.drawGrid(false);
					break;
				case 78: //Toggle snapping
					if(!lvl.snapTo()) {
						buttons[76].push();
						break;
					}
					//Intentional continuation into next case
				case 77: //Snapping off
					buttons[76].unpush();
					lvl.snapTo(false);
					break;
				case 81: //Toggle snap points
					if(!lvl.drawSnap()) {
						buttons[79].push();
						break;
					}
					//Intentional continuation into next case
				case 80: //Hide snap points
					buttons[79].unpush();
					lvl.drawSnap(false);
					break;
				case 82: //Save
					push("save");
					break;
				case 83: //Save As
					push("saveAs");
					break;
				case 84: //Open
					push("open");
					break;
				case 85: //Pop text
					popText();
					break;
				case 86: //Push text below
					pushTextBelow();
					break;
				case 87: //Push text beside	
					pushTextBeside();
					break;
				case 88: //New
					newLevel();
					break;
				case 89: //Select All
					lvl.selectAll();
				break;
				default:
					System.out.println("No action for number " + an);
					break;
			}
		}
	}
	//Listener method for menu button pushes
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(grsavename)) {
			push("grsavename");
			return;
		}
		if(e.getSource().equals(snsavename)) {
			push("snsavename");
			return;
		}
		String comm = e.getActionCommand();
		if(comm.equals("comboBoxChanged")) {
			if(((JComboBox)e.getSource()).equals(gridSelect)) {
				loadGridLines((String)gridSelect.getSelectedItem());
			}
			if(((JComboBox)e.getSource()).equals(snapSelect)) {
				loadSnapPoints((String)snapSelect.getSelectedItem());
			}
		} else {
			push(comm);
		}
	}
	//Button push method
	public void push(String button) {
		//System.out.println(button);
		switch(button) {
		//Top 3
			case "tiles":
				//Set left vertical row to tiles multibutton (index 0)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i].setIndex(0);
					mbuttons[i+10].setIndex(8);
				}
				lvl.setMode(true);
			break;
			case "items":
				//Set left vertical row to items multibutton (index 2)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i].setIndex(2);
					mbuttons[i+10].setIndex(8);
				}
				lvl.setMode(false);
			break;
			case "enemies":
				//Set left vertical row to enemies multibutton (index 1)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i].setIndex(1);
					mbuttons[i+10].setIndex(8);
				}
				lvl.setMode(false);
			break;
			
		//Tiles
			//45 tile 
			case "45tile":
				//Set right vertical row to 45tile multibutton (index 0)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(0,i==tilestate);
				}
			break;
				case "45tileoff":
					lvl.setMode(-1);
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
				break;
				case "45tileQ":
					tilestate = 0;
					lvl.setMode(2);
				break;
				case "45tileW":
					tilestate = 1;
					lvl.setMode(3);
				break;
				case "45tileA":
					tilestate = 2;
					lvl.setMode(5);
				break;
				case "45tileS":
					tilestate = 3;
					lvl.setMode(4);
			break;
			
			//63 thin tile
			case "63thintile":
				//Set right vertical row to 63thintile multibutton (index 1)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(1,i==tilestate);
				}
			break;
				case "63thintileoff":
					lvl.setMode(-1);
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
				break;
				case "63thintileQ":
					tilestate = 0;
					lvl.setMode(6);
				break;
				case "63thintileW":
					tilestate = 1;
					lvl.setMode(7);
				break;
				case "63thintileA":
					tilestate = 2;
					lvl.setMode(9);
				break;
				case "63thintileS":
					tilestate = 3;
					lvl.setMode(8);
				break;
				
			//27 thin tile
			case "27thintile":
				//Set right vertical row to 27thintile multibutton (index 2)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(2,i==tilestate);
				}
			break;
				case "27thintileoff":
					lvl.setMode(-1);
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
				break;
				case "27thintileQ":
					tilestate = 0;
					lvl.setMode(10);
				break;
				case "27thintileW":
					tilestate = 1;
					lvl.setMode(11);
				break;
				case "27thintileA":
					tilestate = 2;
					lvl.setMode(13);
				break;
				case "27thintileS":
					tilestate = 3;
					lvl.setMode(12);
				break;
			
			//Concave tile
			case "Concavetile":
				//Set right vertical row to Concavetile multibutton (index 3)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(3,i==tilestate);
				}
			break;
				case "Concavetileoff":
					lvl.setMode(-1);
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
				break;
				case "ConcavetileQ":
					tilestate = 0;
					lvl.setMode(14);
				break;
				case "ConcavetileW":
					tilestate = 1;
					lvl.setMode(15);
				break;
				case "ConcavetileA":
					tilestate = 2;
					lvl.setMode(17);
				break;
				case "ConcavetileS":
					tilestate = 3;
					lvl.setMode(16);
				break;
			
			//Half tile
			case "Halftile":
				//Set right vertical row to Halftile multibutton (index 4)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(4,i==tilestate);
				}
			break;
				case "Halftileoff":
					lvl.setMode(-1);
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
				break;
				case "HalftileQ":
					tilestate = 0;
					lvl.setMode(18);
				break;
				case "HalftileW":
					tilestate = 1;
					lvl.setMode(19);
				break;
				case "HalftileA":
					tilestate = 2;
					lvl.setMode(21);
				break;
				case "HalftileS":
					tilestate = 3;
					lvl.setMode(20);
				break;
			
			//63 thick tile
			case "63thicktile":
				//Set right vertical row to 63thicktile multibutton (index 5)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(5,i==tilestate);
				}
			break;
				case "63thicktileoff":
					lvl.setMode(-1);
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
				break;
				case "63thicktileQ":
					tilestate = 0;
					lvl.setMode(22);
				break;
				case "63thicktileW":
					tilestate = 1;
					lvl.setMode(23);
				break;
				case "63thicktileA":
					tilestate = 2;
					lvl.setMode(25);
				break;
				case "63thicktileS":
					tilestate = 3;
					lvl.setMode(24);
				break;
			
			//27 thick tile
			case "27thicktile":
				//Set right vertical row to 27thicktile multibutton (index 6)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(6,i==tilestate);
				}
			break;
				case "27thicktileoff":
					lvl.setMode(-1);
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
				break;
				case "27thicktileQ":
					tilestate = 0;
					lvl.setMode(26);
				break;
				case "27thicktileW":
					tilestate = 1;
					lvl.setMode(27);
				break;
				case "27thicktileA":
					tilestate = 2;
					lvl.setMode(29);
				break;
				case "27thicktileS":
					tilestate = 3;
					lvl.setMode(28);
				break;
			
			//Convex tile
			case "Convextile":
				//Set right vertical row to Convextile multibutton (index 7)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(7,i==tilestate);
				}
			break;
				case "Convextileoff":
					lvl.setMode(-1);
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
				break;
				case "ConvextileQ":
					tilestate = 0;
					lvl.setMode(30);
				break;
				case "ConvextileW":
					tilestate = 1;
					lvl.setMode(31);
				break;
				case "ConvextileA":
					tilestate = 2;
					lvl.setMode(33);
				break;
				case "ConvextileS":
					tilestate = 3;
					lvl.setMode(32);
				break;
			
			//Erase
			case "Erase":
				//Set right vertical row to Erase multibutton (no options) (index 8)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(8);
				}
				lvl.setMode(0);
			break;
				case "Eraseoff":
					lvl.setMode(-1);
				break;
			
			//Fill
			case "Fill":
				//Set right vertical row to Fill multibutton (no options) (index 9)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(9);
				}
				lvl.setMode(1);
			break;
				case "Filloff":
					lvl.setMode(-1);
				break;
				
		//Enemies
			//Gauss turret
			case "Gaussturret":
				//Set right vertical row to Gaussturret multibutton (no options) (index 10)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(10);
				}
				lvl.setMode(Jned.GAUSS);
			break;
				case "Gaussturretoff":
					lvl.setMode(-1);
				break;
			
			//Homing launcher
			case "Hominglauncher":
				//Set right vertical row to Hominglauncher multibutton (no options) (index 11)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(11);
				}
				lvl.setMode(Jned.HOMING);
			break;
				case "Hominglauncheroff":
					lvl.setMode(-1);
				break;
			
			//Mine
			case "Mine":
				//Set right vertical row to mine multibutton (no options) (index 12)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(12);
				}
				lvl.setMode(Jned.MINE);
			break;
				case "Mineoff":
					lvl.setMode(-1);
				break;
			
			//Floor guard
			case "Floorguard":
				//Set right vertical row to Floorguard multibutton (no options) (index 13)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(13);
				}
				lvl.setMode(Jned.FLOOR);
			break;
				case "Floorguardoff":
					lvl.setMode(-1);
				break;
			
			//Thwump
			case "Thwump":
				//Set right vertical row to Thwump multibutton (index 14)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(14,i==(itemdir/2));
				}
			break;
				case "Thwumpoff":
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
					lvl.setMode(-1);
				break;
				case "ThwumpA":
					lvl.setMode(Jned.THWUMP+2);
					itemdir = itemdir%2+4;
				break;
				case "ThwumpD":
					lvl.setMode(Jned.THWUMP);
					itemdir = itemdir%2;
				break;
				case "ThwumpS":
					lvl.setMode(Jned.THWUMP+1);
					itemdir = itemdir%2+2;
				break;
				case "ThwumpW":
					lvl.setMode(Jned.THWUMP+3);
					itemdir = itemdir%2+6;
				break;
			
			//Zap drone
			case "Zapdrone":
				//Set right vertical row to Zapdrone multibutton (index 15)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(15,i==(itemdir/2)||i==4+dronebeh);
				}
			break;
				case "Zapdroneoff":
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
					lvl.setMode(-1);
				break;
				case "ZapdroneA":
					itemdir = itemdir%2+4;
					lvl.setMode(Jned.ZAP+14+dronebeh);					
				break;
				case "ZapdroneD":
					itemdir = itemdir%2;
					lvl.setMode(Jned.ZAP+dronebeh);	
				break;
				case "ZapdroneS":
					itemdir = itemdir%2+2;
					lvl.setMode(Jned.ZAP+7+dronebeh);	
				break;
				case "ZapdroneW":
					itemdir = itemdir%2+6;
					lvl.setMode(Jned.ZAP+21+dronebeh);	
				break;
				case "Zapdronedumbcw":
					dronebeh=2;
					lvl.setMode(Jned.ZAP+7*itemdir/2+2);
				break;
				case "Zapdronedumbccw":
					dronebeh=3;
					lvl.setMode(Jned.ZAP+7*itemdir/2+3);
				break;
				case "Zapdronesurfacecw":
					dronebeh=0;
					lvl.setMode(Jned.ZAP+7*itemdir/2+0);
				break;
				case "Zapdronesurfaceccw":
					dronebeh=1;
					lvl.setMode(Jned.ZAP+7*itemdir/2+1);
				break;
				case "Zapdronealt":
					dronebeh=4;
					lvl.setMode(Jned.ZAP+7*itemdir/2+4);
				break;
				case "Zapdronerand":
					dronebeh=5;
					lvl.setMode(Jned.ZAP+7*itemdir/2+5);
				break;
				case "Zapdronedumbcwoff":
				case "Zapdronedumbccwoff":
				case "Zapdronesurfacecwoff":
				case "Zapdronesurfaceccwoff":
				case "Zapdronealtoff":
				case "Zapdronerandoff":
					dronebeh=6;
					lvl.setMode(Jned.ZAP+7*itemdir/2+6);
				break;
			
			//Seeker drone
			case "Seekerdrone":
				//Set right vertical row to Seekerdrone multibutton (index 16)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(16,i==(itemdir/2)||i==4+dronebeh);
				}
			break;
				case "Seekerdroneoff":
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
					lvl.setMode(-1);
				break;
				case "SeekerdroneA":
					itemdir = itemdir%2+4;
					lvl.setMode(Jned.SEEKER+14+dronebeh);					
				break;
				case "SeekerdroneD":
					itemdir = itemdir%2;
					lvl.setMode(Jned.SEEKER+dronebeh);	
				break;
				case "SeekerdroneS":
					itemdir = itemdir%2+2;
					lvl.setMode(Jned.SEEKER+7+dronebeh);	
				break;
				case "SeekerdroneW":
					itemdir = itemdir%2+6;
					lvl.setMode(Jned.SEEKER+21+dronebeh);	
				break;
				case "Seekerdronedumbcw":
					dronebeh=2;
					lvl.setMode(Jned.SEEKER+7*itemdir/2+2);
				break;
				case "Seekerdronedumbccw":
					dronebeh=3;
					lvl.setMode(Jned.SEEKER+7*itemdir/2+3);
				break;
				case "Seekerdronesurfacecw":
					dronebeh=0;
					lvl.setMode(Jned.SEEKER+7*itemdir/2+0);
				break;
				case "Seekerdronesurfaceccw":
					dronebeh=1;
					lvl.setMode(Jned.SEEKER+7*itemdir/2+1);
				break;
				case "Seekerdronealt":
					dronebeh=4;
					lvl.setMode(Jned.SEEKER+7*itemdir/2+4);
				break;
				case "Seekerdronerand":
					dronebeh=5;
					lvl.setMode(Jned.SEEKER+7*itemdir/2+5);
				break;
				case "Seekerdronedumbcwoff":
				case "Seekerdronedumbccwoff":
				case "Seekerdronesurfacecwoff":
				case "Seekerdronesurfaceccwoff":
				case "Seekerdronealtoff":
				case "Seekerdronerandoff":
					dronebeh=6;
					lvl.setMode(Jned.SEEKER+7*itemdir/2+6);
				break;

			//Laser drone
			case "Laserdrone":
				//Set right vertical row to Laserdrone multibutton (index 17)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(17,i==(itemdir/2)||i==4+dronebeh);
				}
			break;
				case "Laserdroneoff":
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
					lvl.setMode(-1);
				break;
				case "LaserdroneA":
					itemdir = itemdir%2+4;
					lvl.setMode(Jned.LASER+14+dronebeh);					
				break;
				case "LaserdroneD":
					itemdir = itemdir%2;
					lvl.setMode(Jned.LASER+dronebeh);	
				break;
				case "LaserdroneS":
					itemdir = itemdir%2+2;
					lvl.setMode(Jned.LASER+7+dronebeh);	
				break;
				case "LaserdroneW":
					itemdir = itemdir%2+6;
					lvl.setMode(Jned.LASER+21+dronebeh);	
				break;
				case "Laserdronedumbcw":
					dronebeh=2;
					lvl.setMode(Jned.LASER+7*itemdir/2+2);
				break;
				case "Laserdronedumbccw":
					dronebeh=3;
					lvl.setMode(Jned.LASER+7*itemdir/2+3);
				break;
				case "Laserdronesurfacecw":
					dronebeh=0;
					lvl.setMode(Jned.LASER+7*itemdir/2+0);
				break;
				case "Laserdronesurfaceccw":
					dronebeh=1;
					lvl.setMode(Jned.LASER+7*itemdir/2+1);
				break;
				case "Laserdronealt":
					dronebeh=4;
					lvl.setMode(Jned.LASER+7*itemdir/2+4);
				break;
				case "Laserdronerand":
					dronebeh=5;
					lvl.setMode(Jned.LASER+7*itemdir/2+5);
				break;
				case "Laserdronedumbcwoff":
				case "Laserdronedumbccwoff":
				case "Laserdronesurfacecwoff":
				case "Laserdronesurfaceccwoff":
				case "Laserdronealtoff":
				case "Laserdronerandoff":
					dronebeh=6;
					lvl.setMode(Jned.LASER+7*itemdir/2+6);
				break;

			//Chaingun drone
			case "Chaingundrone":
				//Set right vertical row to Chaingundrone multibutton (index 18)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(18,i==(itemdir/2)||i==4+dronebeh);
				}
			break;
				case "Chaingundroneoff":
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
					lvl.setMode(-1);
				break;
				case "ChaingundroneA":
					itemdir = itemdir%2+4;
					lvl.setMode(Jned.CHAINGUN+14+dronebeh);					
				break;
				case "ChaingundroneD":
					itemdir = itemdir%2;
					lvl.setMode(Jned.CHAINGUN+dronebeh);	
				break;
				case "ChaingundroneS":
					itemdir = itemdir%2+2;
					lvl.setMode(Jned.CHAINGUN+7+dronebeh);	
				break;
				case "ChaingundroneW":
					itemdir = itemdir%2+6;
					lvl.setMode(Jned.CHAINGUN+21+dronebeh);	
				break;
				case "Chaingundronedumbcw":
					dronebeh=2;
					lvl.setMode(Jned.CHAINGUN+7*itemdir/2+2);
				break;
				case "Chaingundronedumbccw":
					dronebeh=3;
					lvl.setMode(Jned.CHAINGUN+7*itemdir/2+3);
				break;
				case "Chaingundronesurfacecw":
					dronebeh=0;
					lvl.setMode(Jned.CHAINGUN+7*itemdir/2+0);
				break;
				case "Chaingundronesurfaceccw":
					dronebeh=1;
					lvl.setMode(Jned.CHAINGUN+7*itemdir/2+1);
				break;
				case "Chaingundronealt":
					dronebeh=4;
					lvl.setMode(Jned.CHAINGUN+7*itemdir/2+4);
				break;
				case "Chaingundronerand":
					dronebeh=5;
					lvl.setMode(Jned.CHAINGUN+7*itemdir/2+5);
				break;
				case "Chaingundronedumbcwoff":
				case "Chaingundronedumbccwoff":
				case "Chaingundronesurfacecwoff":
				case "Chaingundronesurfaceccwoff":
				case "Chaingundronealtoff":
				case "Chaingundronerandoff":
					dronebeh=6;
					lvl.setMode(Jned.CHAINGUN+7*itemdir/2+6);
				break;
			
			//Player
			case "Player":
				//Set right vertical row to Player multibutton (no options) (index 19)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(19);
				}
				lvl.setMode(Jned.PLAYER);
			break;
				case "Playeroff":
					lvl.setMode(-1);
				break;
			
			//Gold
			case "Gold":
				//Set right vertical row to Gold multibutton (no options) (index 20)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(20);
				}
				lvl.setMode(Jned.GOLD);
			break;
				case "Goldoff":
					lvl.setMode(-1);
				break;
				
			//Bounce block
			case "Bounceblock":
				//Set right vertical row to Bounceblock multibutton (no options) (index 21)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(21);
				}
				lvl.setMode(Jned.BOUNCE);
			break;
				case "Bounceblockoff":
					lvl.setMode(-1);
				break;
							
			//Exit
			case "Exitdoor":
				//Set right vertical row to Exit multibutton (index 22)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(22);
				}
				lvl.setMode(Jned.EXIT);
			break;
				case "Exitdooroff":
					lvl.setMode(-1);
				break;
			
			//Oneway platform
			case "Oneway":
				//Set right vertical row to Oneway multibutton (index 23)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(23,i==(itemdir/2));
				}
			break;
				case "Onewayoff":
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
					lvl.setMode(-1);
				break;
				case "OnewayA":
					lvl.setMode(Jned.ONEWAY+2);
					itemdir = itemdir%2+4;
				break;
				case "OnewayD":
					lvl.setMode(Jned.ONEWAY);
					itemdir = itemdir%2;
				break;
				case "OnewayS":
					lvl.setMode(Jned.ONEWAY+1);
					itemdir = itemdir%2+2;
				break;
				case "OnewayW":
					lvl.setMode(Jned.ONEWAY+3);
					itemdir = itemdir%2+6;
				break;
			
			//Normal door
			case "Normaldoor":
				//Set right vertical row to Normaldoor multibutton (index 24)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(24,i==(itemdir/2));
				}
			break;
				case "Normaldooroff":
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
					lvl.setMode(-1);
				break;
				case "NormaldoorA":
					lvl.setMode(Jned.NDOOR+2);
					itemdir = itemdir%2+4;
				break;
				case "NormaldoorD":
					lvl.setMode(Jned.NDOOR);
					itemdir = itemdir%2;
				break;
				case "NormaldoorS":
					lvl.setMode(Jned.NDOOR+1);
					itemdir = itemdir%2+2;
				break;
				case "NormaldoorW":
					lvl.setMode(Jned.NDOOR+3);
					itemdir = itemdir%2+6;
				break;
			
			//Locked door
			case "Lockeddoor":
				//Set right vertical row to Lockeddoor multibutton (index 25)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(25,i==(itemdir/2));
				}
			break;
				case "Lockeddooroff":
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
					lvl.setMode(-1);
				break;
				case "LockeddoorA":
					lvl.setMode(Jned.LDOOR+2);
					itemdir = itemdir%2+4;
				break;
				case "LockeddoorD":
					lvl.setMode(Jned.LDOOR);
					itemdir = itemdir%2;
				break;
				case "LockeddoorS":
					lvl.setMode(Jned.LDOOR+1);
					itemdir = itemdir%2+2;
				break;
				case "LockeddoorW":
					lvl.setMode(Jned.LDOOR+3);
					itemdir = itemdir%2+6;
				break;
			
			//Trap door
			case "Trapdoor":
				//Set right vertical row to Trapdoor multibutton (index 26)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(26,i==(itemdir/2));
				}
			break;
				case "Trapdooroff":
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
					lvl.setMode(-1);
				break;
				case "TrapdoorA":
					lvl.setMode(Jned.TDOOR+2);
					itemdir = itemdir%2+4;
				break;
				case "TrapdoorD":
					lvl.setMode(Jned.TDOOR);
					itemdir = itemdir%2;
				break;
				case "TrapdoorS":
					lvl.setMode(Jned.TDOOR+1);
					itemdir = itemdir%2+2;
				break;
				case "TrapdoorW":
					lvl.setMode(Jned.TDOOR+3);
					itemdir = itemdir%2+6;
				break;
			
			//Launchpad
			case "Launchpad":
				//Set right vertical row to Launchpad multibutton (index 27)
				for (int i = 0; i <= 9; i++) {
					mbuttons[i+10].setIndex(27,i==itemdir);
				}
			break;
				case "Launchpadoff":
					for (int i = 0; i <= 9; i++) {
						mbuttons[i+10].setIndex(8);
					}
					lvl.setMode(-1);
				break;
				case "LaunchpadA":
					lvl.setMode(Jned.LAUNCH+4);
					itemdir = 4;
				break;
				case "LaunchpadAS":
					lvl.setMode(Jned.LAUNCH+3);
					itemdir = 3;
				break;
				case "LaunchpadAW":
					lvl.setMode(Jned.LAUNCH+5);
					itemdir = 5;
				break;
				case "LaunchpadD":
					lvl.setMode(Jned.LAUNCH);
					itemdir = 0;
				break;
				case "LaunchpadDS":
					lvl.setMode(Jned.LAUNCH+1);
					itemdir = 1;
				break;
				case "LaunchpadDW":
					lvl.setMode(Jned.LAUNCH+7);
					itemdir = 7;
				break;
				case "LaunchpadS":
					lvl.setMode(Jned.LAUNCH+2);
					itemdir = 2;
				break;
				case "LaunchpadW":
					lvl.setMode(Jned.LAUNCH+6);
					itemdir = 6;
				break;
			
			//Text box buttons
			case "cpylvl":
				tbox.copyToClipboard();
			break;
			case "pstlvl":
				System.out.println(tbox.pasteFromClipboard());
			break;
			case "tboxlvl":
				lvl.inputLevel(tbox.getText());
			break;
			case "undo":
				tbox.setText(hist.undo());
				lvl.inputLevel(tbox.getText());
			break;
			case "redo":
				tbox.setText(hist.redo());
				lvl.inputLevel(tbox.getText());
			break;
			case "Exit":
				System.exit(0);
			break;
			
			//Gridline panel buttons
			case "gridlines":
				gridPanel.setVisible(true);
			break;
				case "gridtoggle":
					lvl.drawGrid(true);
				break;
				case "gridtoggleoff":
					lvl.drawGrid(false);
				break;
				case "grsave":
					grsavename.setText("");
					grSaveDialog.setVisible(true);
				break;
				case "grdelete":
					grDeleteDialog.setVisible(true);
				break;
				case "grpri":
					grOverlay[overlayIndex].setActive(false);
					overlayIndex = 0;
					grOverlay[overlayIndex].setActive(true);
				break;
				case "grsec":
					grOverlay[overlayIndex].setActive(false);
					overlayIndex = 1;
					grOverlay[overlayIndex].setActive(true);
				break;
				case "grter":
					grOverlay[overlayIndex].setActive(false);
					overlayIndex = 2;
					grOverlay[overlayIndex].setActive(true);
				break;
				case "gridlinesoff":
					gridPanel.setVisible(false);
				break;
				case "grsinglebt":
					if(grOverlay != null) {
						grOverlay[overlayIndex].push("single");
						grdblspctxt.setVisible(false);
						grdblspc.setVisible(false);
					}
				break;
				case "grdoublebt":
					if(grOverlay != null) {
						grOverlay[overlayIndex].push("double");
						grdblspctxt.setVisible(true);
						grdblspc.setVisible(true);
					}
				break;
				case "gronoff":
					if(grOverlay != null)
						grOverlay[overlayIndex].push("on");
				break;
				case "gronoffoff":
					if(grOverlay != null)
						grOverlay[overlayIndex].push("off");
				break;
				case "grsavename":
				case "grsavesave":
					saveGridLines(grsavename.getText());
					grSaveDialog.setVisible(false);
				break;
				case "grsavecancel":
					grSaveDialog.setVisible(false);
				break;
				case "grdeletedelete":
					deleteGridLines((String)gridSelect.getSelectedItem());
					grDeleteDialog.setVisible(false);
				break;
				case "grdeletecancel":
					grDeleteDialog.setVisible(false);
				break;
			
			//Snap panel buttons
			case "snapping":
				snapPanel.setVisible(true);
			break;
				case "snaptoggle":
					lvl.snapTo(true);
				break;
				case "snaptoggleoff":
					lvl.snapTo(false);
				break;
				case "snsave":
					snsavename.setText("");
					snSaveDialog.setVisible(true);
				break;
				case "sndelete":
					snDeleteDialog.setVisible(true);
				break;
				case "snappingoff":
					snapPanel.setVisible(false);
				break;
				case "snshow":
					lvl.drawSnap(true);
				break;
				case "snshowoff":
					lvl.drawSnap(false);
				break;
				case "snsavename":
				case "snsavesave":
					saveSnapPoints(snsavename.getText());
					snSaveDialog.setVisible(false);
				break;
				case "snsavecancel":
					snSaveDialog.setVisible(false);
				break;
				case "sndeletedelete":
					deleteSnapPoints((String)snapSelect.getSelectedItem());
					snDeleteDialog.setVisible(false);
				break;
				case "sndeletecancel":
					snDeleteDialog.setVisible(false);
				break;
			
			//Triggers and paths
			case "trigger":
				drawTriggers = true;
			break;
				case "triggeroff":
					drawTriggers = false;
				break;
			case "dpath":
				drawPaths = true;
			break;
				case "dpathoff":
					drawPaths = false;
				break;
				
			//Menu bar
			case "new":
				newLevel();
			break;
			case "save":
				if(savedAs) {
					Nfile usrlvls = new Nfile(userlevels);
					usrlvls.setData(tbox.getText(),lvlName);
					usrlvls.close();
				} else {
					fileChooser.open(true);
				}
			break;
			case "saveAs":
				fileChooser.open(true);
			break;
			case "open":
				fileChooser.open(false);
			break;
			case "keyShortcuts":
				keySetWindow.open();
			break;
			
			//Other
			default:
				String[] multicmd = button.split("#");
				if(multicmd.length > 1) { //Testing for commands from the gridline or snap setting menu buttons, keyboard shortcuts dialogs, file chooser dialogs, or action number calls
					switch(multicmd[0]) {
						case "action":
							try { //An alternative way to push buttons. Menu buttons, for example, call actionPerformed, which redirects here. But some need to actually push a physical button, so
								doActionNumber(Integer.parseInt(multicmd[1])); //this mechanism redirects to an action number, which calls that button's push method, pushing it and redirecting back
							} catch (NumberFormatException e) {}               //to this method with the actual action command.
						break;
						case "gridlineSetting":
							gridSelect.setSelectedItem(multicmd[1]);
						break;
						case "snapSetting":
							snapSelect.setSelectedItem(multicmd[1]);
						break;
						case "keySetting":
							keys.push(multicmd[1]);
						break;
						case "keyShortcuts":
							keySetWindow.push(multicmd[1]);
						break;
						case "fileChooser":
							fileChooser.push(multicmd[1]);
						break;
					}
				}
			break;
		}
		repaint();
	}
	public void saveAs(String name, String author, String genre) {
		setAttributes(name,author,genre);
		Nfile usrlvls = new Nfile(userlevels);
		usrlvls.writeNew(name,author,genre,tbox.getText());
		usrlvls.close();
		savedAs = true;
	}
	protected void setUserlevels(String usrlvls) {
		userlevels = usrlvls;
	}
	
	//Tells the level area object to make every drone recalculate its path
	public void calcDronePaths() {
		lvl.chDronePaths();
	}
	
	//Finds an unused action number for grid/snap settings
	private int findNextActionNumber() {
		ArrayList<Integer> used = new ArrayList<Integer>();
		String[] gnames = config.getNames("grid",1);
		String[] snames = config.getNames("snap",1);
		for(String nom : gnames) {
			try {
				used.add(Integer.parseInt(config.getAttr2(nom)));
			} catch (NumberFormatException e) {
			}
		}
		for(String nom : snames) {
			try {
				used.add(Integer.parseInt(config.getAttr2(nom)));
			} catch (NumberFormatException e) {
			}
		}
		Collections.sort(used);
		for(int i = 0; i < used.size(); i++) {
			if(used.get(i) - 256 != i) return i + 256;
		}
		return used.size() + 256;
	}
	//Saves gridline data to config file
	public void saveGridLines(String name) {
		String data = "";
		for (int i = 0; i < 3; i++) {
			data += grOverlay[i].saveData();
			if(i<2) data += "@";
		}
		config.writeNew(name,"grid","" + findNextActionNumber(),data);
		gridSelect.addItem(name);
		gridSelect.setSelectedItem(name);
		setGridlineSettingMenu();
																																											//Invoke dynamic method in KeyShortcuts
	}
	//Deletes a preset from the config file
	public void deleteGridLines(String name) {
		if(!name.split(" ")[0].equals("classic")) {
			config.delete(name);
			gridSelect.removeItem(name);
			setGridlineSettingMenu();
		}
	}
	//Loads gridline data from config file
	public void loadGridLines(String name) {
		String setting = config.getData(name);
		if(setting != null) {
			String[] overdat = setting.split("@"); //Change to make it load the data from file under name
			for (int i = 0; i < 3; i++) {
				grOverlay[i].loadData(overdat[i]);
			}
		}
	}
	//Saves gridline data to config file
	public void saveSnapPoints(String name) {
		config.writeNew(name,"snap","" + findNextActionNumber(),snOverlay.saveData());
		snapSelect.addItem(name);
		snapSelect.setSelectedItem(name);
		setSnapSettingMenu();
	}
	//Deletes a preset from the config file
	public void deleteSnapPoints(String name) {
		if(!name.split(" ")[0].equals("classic")) {
			config.delete(name);
			snapSelect.removeItem(name);
			setSnapSettingMenu();
		}
	}
	//Loads gridline data from config file
	public void loadSnapPoints(String name) {
		String setting = config.getData(name);
		if(setting != null) snOverlay.loadData(setting);
	}

	//Attempts to draw an image from the image bank at selected coordinates
	public boolean drawImage(int index, int xor, int yor, Graphics g) {
		BufferedImage image = imgBank.get(index);
		if(image == null) return false;
		g.drawImage(image,xor+imgBank.getXoff(index),yor+imgBank.getYoff(index),null);
		return true;
	}
	
	//Main Method: launching point for application - sets up frame and adds new Jned object, see constructor at top
	public static void main (String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Jned jned = new Jned(frame);
		frame.getContentPane().add(jned);
		
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
		jned.grabFocus();
	}

	public void pushDelete() {
		lvl.pushDelete();
		repaint();
	}
	
	public void mouseClicked(MouseEvent me) {}
	public void mousePressed(MouseEvent me) {}
	public void mouseReleased(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {
		grabFocus();
	}
	public void mouseExited(MouseEvent me) {}
}