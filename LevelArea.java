import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * LevelArea is a panel displaying the level in Jned. It contains all the level items and tile 
 * data. All manipulation of level items is done through LevelArea. Also, LevelArea contains
 * pop-up menus for the objects.
 * @author James Porter
 */
public class LevelArea extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
  private Jned jned;
  private TextBox textBox;
  private KeySignature keys;
  
  private int cellSize;
  
  // Mode variables  
  private boolean isTiles;
  private int mode;
  // -1 represents selection mode
  // Item editing mode fields - skipped numbers are for different directions/behaviors
  public static final int GAUSS = 0;
  public static final int HOMING = 1;
  public static final int MINE = 2;
  public static final int FLOOR = 3;
  public static final int THWUMP = 4;
  public static final int ZAP = 8;
  public static final int SEEKER = 36;
  public static final int LASER = 64;
  public static final int CHAINGUN = 92;
  public static final int PLAYER = 120;
  public static final int GOLD = 121;
  public static final int BOUNCE = 122;
  public static final int EXIT = 123;
  public static final int ONEWAY = 124;
  public static final int NDOOR = 128;
  public static final int LDOOR = 132;
  public static final int TDOOR = 136;
  public static final int LAUNCH = 140;
  // Tile editing mode values - these also match the values in the tiles array, and the indices in
  // the charvals array (for converting tile data to n code)
  /*
  Q   W   S   A   TILE
  0               empty
  1               filled
  2   3   4   5   45 degree
  6   7   8   9   63 degree thin
  10  11  12  13  27 degree thin
  14  15  16  17  concave curve
  18  19  20  21  half tile
  22  23  24  25  63 degree thick
  26  27  28  29  27 degree thick
  30  31  32  33  convex curve
  */
  private final char[] charvals = {'0', '1', '3', '2', '5', '4', 'G', 'F', 'I', 'H', '?', '>', 'A',
      '@', '7', '6', '9', '8', 'Q', 'P', 'O', 'N', 'K', 'J', 'M', 'L', 'C', 'B', 'E', 'D', ';',
      ':', '=', '<'};
      
  private int[][] tiles;
  private ArrayList<Item> items;
  // Sub-lists
  private ArrayList<Door> doors;
  private ArrayList<Drone> drones;
  private ArrayList<Launchpad> launchPads;
  private int[] itemIndices; // Positions of item data in text box
  private int thePlayer;
  // Clipboard
  private ArrayList<Item> selection;
  private int[][] tileClipboard;
  private ArrayList<Item> itemClipboard;
  private int clipboardAverageX;
  private int clipboardAverageY;
  
  // Mouse state variables
  private int mouseRow;
  private int mouseColumn;
  private int deltaRow; // Multi-use: TILE MODE = drag box width/height; ITEM SELECTION MODE = 
  private int deltaColumn; // opposite corner of drag box; ITEM ADDING MODE = adjusted add point
  private int originRow; // Multi-use: TILE MODE = drag box origin; ITEM MODE = drag box origin or
  private int originColumn; // right-click coordinate
  
  private boolean mouseOn;
  private int buttonDown; // 0 = none, 1 = left button, 2 = right button
  private boolean dragged;
  private boolean drawingSelectionBox;
  private Item lastItem;
  private Item rightClickedItem;
  private boolean snapTo;
  private boolean addingSwitch;
  private boolean grabPoint; // TASK - get rid of this
  
  // Menus
  private JPopupMenu copyPasteMenu;
  private JPopupMenu itemMenu;
  private JMenuItem[] menuItems;
  
  // Drawing variables
  private boolean drawingTriggers;
  private boolean drawingDronePaths;
  private boolean drawingGrid;
  private boolean drawingSnapPoints;
  
  // Grid/snap
  private Overlay[] overlays;
  private ArrayList<Integer> scratch1;
  private ArrayList<Integer> scratch2;
  
  /**
   * Create an instance of LevelArea.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param width the width
   * @param height the height
   * @param squareSize the length of one side of an N tile
   * @param jned a reference to the associated Jned instance
   */
  public LevelArea (int x, int y, int width, int heigth, int squareSize, Jned jned,
      KeySignature keys) {
    setLayout(null);
    setBounds(x, y, width, heigth);
    setBackground(Colors.TILE_SPACE);
    addMouseListener(this);
    addMouseMotionListener(this);
    
    cellSize = squareSize;
    this.jned = jned;
    this.keys = keys;
    
    tiles = new int[31][23];
    items = new ArrayList<Item>();
    doors = new ArrayList<Door>();
    drones = new ArrayList<Drone>();
    launchPads = new ArrayList<Launchpad>();
    thePlayer = -1;
    selection = new ArrayList<Item>();
    itemClipboard = new ArrayList<Item>();
    clipboardAverageX = 0;
    clipboardAverageY = 0;
    
    isTiles = true;
    mode = -1;
    
    mouseRow = 0;
    mouseColumn = 0;
    deltaRow = 0;
    deltaColumn = 0;
    mouseOn = false;
    buttonDown = 0;
    dragged = false;
    lastItem = null;
    rightClickedItem = null;
    addingSwitch = false;
    grabPoint = false;
    
    // Tile copy/paste drop-down menu
    copyPasteMenu = new JPopupMenu();
    JMenuItem miCut = makeJMenuItem("Cut", "cut");
    JMenuItem miCopy = makeJMenuItem("Copy", "copy");
    JMenuItem miPaste = makeJMenuItem("Paste", "paste");
    JMenuItem miErase = makeJMenuItem("Erase", "erase");
    JMenuItem miFill = makeJMenuItem("Fill", "fill");
    copyPasteMenu.add(miCut);
    copyPasteMenu.add(miCopy);
    copyPasteMenu.add(miPaste);
    copyPasteMenu.add(miErase);
    copyPasteMenu.add(miFill);
    
    // Item drop-down menu
    menuItems = new JMenuItem[11];
    itemMenu = new JPopupMenu();
    menuItems[0] = makeJMenuItem("Cut", "itemCut");
    menuItems[1] = makeJMenuItem("Copy", "itemCopy");
    menuItems[2] = makeJMenuItem("Paste", "itemPaste");
    menuItems[3] = makeJMenuItem("Delete", "itemDelete");
    JMenu nudgeMenu = new JMenu("Nudge");
    JMenuItem nuR = makeJMenuItem("right", "nudgeRight");
    JMenuItem nuD = makeJMenuItem("down", "nudgeDown");
    JMenuItem nuL = makeJMenuItem("left", "nudgeLeft");
    JMenuItem nuU = makeJMenuItem("up", "nudgeUp");
    nudgeMenu.add(nuR);
    nudgeMenu.add(nuD);
    nudgeMenu.add(nuL);
    nudgeMenu.add(nuU);
    menuItems[4] = nudgeMenu;
    JMenu halfNudgeMenu = new JMenu("Nudge");
    JMenuItem hfnuR = makeJMenuItem("right", "nudgeRight");
    JMenuItem hfnuL = makeJMenuItem("left", "nudgeLeft");
    halfNudgeMenu.add(hfnuR);
    halfNudgeMenu.add(hfnuL);
    menuItems[5] = halfNudgeMenu;
    JMenu direct = new JMenu("Direction");
    JMenuItem dirR = makeJMenuItem("right", "dirRight");
    JMenuItem dirD = makeJMenuItem("down", "dirDown");
    JMenuItem dirL = makeJMenuItem("left", "dirLeft");
    JMenuItem dirU = makeJMenuItem("up", "dirUp");
    direct.add(dirR);
    direct.add(dirD);
    direct.add(dirL);
    direct.add(dirU);
    menuItems[6] = direct;
    JMenu direct8 = new JMenu("Direction");
    JMenuItem dirR8 = makeJMenuItem("right", "dirRight");
    JMenuItem dirRD8 = makeJMenuItem("right/down", "dirRightDown");
    JMenuItem dirD8 = makeJMenuItem("down", "dirDown");
    JMenuItem dirLD8 = makeJMenuItem("down/left", "dirLeftDown");
    JMenuItem dirL8 = makeJMenuItem("left", "dirLeft");
    JMenuItem dirLU8 = makeJMenuItem("left/up", "dirLeftUp");
    JMenuItem dirU8 = makeJMenuItem("up", "dirUp");
    JMenuItem dirRU8 = makeJMenuItem("up/right", "dirRightUp");
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
    JMenuItem behSCW = makeJMenuItem("Surface-follow Clockwise", "surfCW");
    JMenuItem behSCCW = makeJMenuItem("Surface-follow Counter-clockwise", "surfCCW");
    JMenuItem behDCW = makeJMenuItem("Dumb Clockwise", "dumbCW");
    JMenuItem behDCCW = makeJMenuItem("Dumb Counter-clockwise", "dumbCCW");
    JMenuItem behALT = makeJMenuItem("Alternating", "alt");
    JMenuItem behRAND = makeJMenuItem("Quasi-random", "rand");
    JMenuItem behNONE = makeJMenuItem("None (still)", "none");
    behav.add(behSCW);
    behav.add(behSCCW);
    behav.add(behDCW);
    behav.add(behDCCW);
    behav.add(behALT);
    behav.add(behRAND);
    behav.add(behNONE);
    menuItems[8] = behav;
    menuItems[9] = makeJMenuItem("Set to Active Player", "activePlayer");
    JMenu launch = new JMenu("Launchpad options");
    JMenuItem lauPow = makeJMenuItem("Power", "launchPower");
    JMenuItem lauDir = makeJMenuItem("Free direction", "launchDir");
    JMenuItem lauPD = makeJMenuItem("Power/direction", "launchPowDir");
    launch.add(lauPow);
    launch.add(lauDir);
    launch.add(lauPD);
    menuItems[10] = launch;
    
    drawingTriggers = false;
    drawingDronePaths = false;
    
    overlays = new Overlay[4];
    scratch1 = null;
    scratch2 = null;
  }
  private JMenuItem makeJMenuItem(String label, String command) {
    JMenuItem menuItem = new JMenuItem(label);
    menuItem.addActionListener(this);
    menuItem.setActionCommand(command);
    return menuItem;
  }
  
  /**
   * Returns the boolean 'tiles/items' portion of the current editing mode.
   * @return true when in tiles mode, false when in items/enemies mode
   */
  public boolean getBoolMode() {
    return isTiles;
  }
  /**
   * Returns the integer portion of the current editing mode, corresponding to one of the item
   * adding mode fields, one of the tile mode values, or -1 for selection mode.
   * @return the integer value of the current editing mode
   */
  public int getIntMode() {
    return mode;
  }
  /**
   * Sets the current editing mode.
   * @param mode integer value of editing mode
   * @param isTiles boolean value of editing mode
   */
  public void setMode(int mode, boolean isTiles) {
    setMode(isTiles);
    setMode(mode);
  }
  /**
   * Sets the current editing mode integer value.
   * @param mode integer value of editing mode
   */
  public void setMode(int mode) {
    this.mode = mode;
    addingSwitch = false;
  }
  /**
   * Sets the current editing mode boolean value
   * @param isTiles boolean value of editing mode. True for tile mode, false for item/enemy mode.
   */
  public void setMode(boolean isTiles) {
    this.isTiles = isTiles;
    
    // Any dragged selection is cancelled
    if(isTiles) clearSelection();
    deltaRow = 0;
    deltaColumn = 0;
    originRow = 0;
    originColumn = 0;
    
    setMode(-1);
  }
  
  /**
   * Returns whether triggers are being drawn.
   * @return true when triggers are being drawn, false otherwise
   */
  public boolean drawingTriggers () {
    return drawingTriggers;
  }
  
  /**
   * Sets value of whether to draw triggers.
   * @param isDrawingTriggers boolean value, true to draw triggers, false to stop drawing
   */
  public void setDrawTriggers (boolean isDrawingTriggers) {
    drawingTriggers = isDrawingTriggers;
  }
  
  /**
   * Returns whether drone paths are being drawn.
   * @return true when drone paths are being drawn, false otherwise
   */
  public boolean drawingDronePaths () {
    return drawingDronePaths;
  }
    
  /**
   * Sets value of whether to draw drone paths.
   * @param isDrawingDronePaths boolean value, true to draw drone paths, false to stop drawing
   */
  public void setDrawDronePaths (boolean isDrawingDronePaths) {
    drawingDronePaths = isDrawingDronePaths;
  }
  
  /**
   * Returns whether the grid is being drawn.
   * @return true when grid is being drawn, false otherwise
   */
  public boolean drawingGrid() {
    return drawingGrid;
  }
    
  /**
   * Sets value of whether to draw the grid.
   * @param isDrawingGrid boolean value, true to draw grid, false to stop drawing
   */
  public void setDrawGrid (boolean isDrawingGrid) {
    drawingGrid = isDrawingGrid;
  }
  
  /**
   * Returns whether the snap points are being drawn.
   * @return true when snap points are being drawn, false otherwise
   */
  public boolean drawingSnapPoints() {
    return drawingSnapPoints;
  }
  
  /**
   * Sets value of whether to draw snap points.
   * @param isDrawingSnapPoints boolean value, true to draw snap points, false to stop drawing
   */
  public void setDrawSnapPoints (boolean isDrawingSnapPoints) {
    drawingSnapPoints = isDrawingSnapPoints;
  }
  
  /**
   * Returns whether snapping is turned on.
   * @return true when snapping is turned on, false otherwise
   */
  public boolean isSnappingOn() {
    return snapTo;
  }
  
  /**
   * Sets snapping to on or off.
   * @param isOn true to turn on snapping, false otherwise
   */
  public void setSnapping (boolean isOn) {
    snapTo = isOn;
  }  
  
  // Edits tile data according to already-stored mouse position and mode information
  private void edit() {
    for (int i = Math.min(mouseColumn, mouseColumn + deltaColumn) - 1; i < Math.max(mouseColumn,
        mouseColumn + deltaColumn); i++) {
      for (int j = Math.min(mouseRow, mouseRow + deltaRow) - 1; j < Math.max(mouseRow, mouseRow +
          deltaRow); j++) {
        if (keys.isShiftPushed()) {
          if (mode > 1) {
            tiles[i][j] = (mode > 17 ? mode - 16 : mode + 16);
          } else {
            tiles[i][j] = 1 - mode;
          }
        } else {
          tiles[i][j] = mode;
        }
        recalculateDronePaths();
      }
    }
  }
  
  // Puts together options that should appear on drop-down menu, based on flag parameters
  private void compileMenu(int flags) {
    for (JMenuItem  mi : menuItems) {
      itemMenu.remove(mi);
    }
    
    if(selection.size() > 0 || rightClickedItem != null) { // Copy, cut
      itemMenu.add(menuItems[0]);
      itemMenu.add(menuItems[1]);
    }
    if(itemClipboard.size() > 0) { // Paste
      itemMenu.add(menuItems[2]);
    }
    if(selection.size() > 0 || rightClickedItem != null) { // Delete
      itemMenu.add(menuItems[3]);
    }
    if((flags & 0b000001) > 0) { // Nudge
      itemMenu.add(menuItems[4]);
    }
    if((flags & 0b000010) > 0 && (flags & 0b000001) == 0) { // Half nudge
      itemMenu.add(menuItems[5]);
    }
    if((flags & 0b000100) > 0) { // Direction
      itemMenu.add(menuItems[6]);
    }
    if((flags & 0b100000) > 0) { // 8 directions
      itemMenu.remove(menuItems[6]);
      itemMenu.add(menuItems[7]);
    }
    if((flags & 0b001000) > 0) { // Behavior
      itemMenu.add(menuItems[8]);
    }
    if((flags & 0b010000) > 0) { // Active player
      int playerCount = 0;
      int playerIndex = -1;
      for (Item item : selection) {
        if(item.getType() == 9) {
          playerCount++;
          playerIndex = items.indexOf(item);
        }
      }
      // This menu item is only shown when 1 player is selected, and it is not active
      if(playerCount <= 1) {
        if(playerCount == 0) {
          playerIndex = items.indexOf(rightClickedItem); 
        }
        if(thePlayer != playerIndex) {
          itemMenu.add(menuItems[9]);
        }
      }
    }
    if((flags & 0b100000) > 0) { // Launchpad
      itemMenu.add(menuItems[10]);
    }
  }
  
  /**
   * Returns a String version of the level, formatted as useable n level data ready to be pasted
   * into the game.
   * @return String of current level data, in n code
   */
  public String outputLevel() {
    // Tiles
    char[] tileString = new char[713];
    for (int c = 0; c < 31; c++) {
      for (int r = 0; r < 23; r++) {
        tileString[r + 23 * c] = charvals[tiles[c][r]];
      }
    }
    String result = new String(tileString) + "|";
    
    // Items
    String things = "";
    itemIndices = new int[items.size()];
    int position = 0;
    // Active player is always the first item listed in n code
    if (thePlayer >= 0) {
      things += items.get(thePlayer).toString() + (items.size() > 1 ? "!" : "");
      itemIndices[thePlayer] = position;
      position = things.length();
    }
    for (int i = 0; i < items.size(); i++) {
      if (i != thePlayer) {
        things += items.get(i).toString() + (i == items.size() - 1 ? "" : "!");
        itemIndices[i] = position;
        position = things.length();
      }
    }
    return result + things;
  }
  
  /**
   * Loads a level into LevelArea from a String of properly formatted n level data.
   * @param data the String of level data to load
   */
  public void inputLevel (String data) {
    // Tiles
    String charvalsString = new String(charvals);
    for (int c = 0; c < 31; c++) {
      for (int r = 0; r < 23; r++) {
        tiles[c][r] = charvalsString.indexOf(data.charAt(r + 23 * c));
      }
    }
    
    // Items
    items.clear();
    selection.clear();
    drones.clear();
    doors.clear();
    thePlayer = -1;
    if(data.length() > 714) {
      String[] itemCodes = data.substring(714).split("!");
      String[] itemType;
      String[] stringParameters;
      double[] parameters;
      for(String item : itemCodes) {
        itemType = item.split("\\^");
        stringParameters = itemType[1].split(",");
        parameters = new double[stringParameters.length];
        for(int i = 0; i < stringParameters.length; i++) {
          parameters[i] = Double.parseDouble(stringParameters[i]);
        }
        Item anItem = null;
        switch (itemType[0]) {
          case "0": // Gold
            anItem = new Gold(jned, (int) parameters[0], (int) parameters[1]);
            break;
          case "1": // Bounce block
            anItem = new Bounceblock(jned, (int) parameters[0], (int) parameters[1]);
            break;
          case "2": // Launch pad
            anItem = new Launchpad(jned, (int) parameters[0], (int) parameters[1], parameters[2],
                parameters[3]);
            break;
          case "3": // Gauss turret
            anItem = new Gaussturret(jned, (int) parameters[0], (int) parameters[1]);
            break;
          case "4": // Floor guard
            anItem = new Floorguard(jned, (int) parameters[0], (int) parameters[1]);
            break;
          case "5": // Player
            anItem = new Player(jned, (int) parameters[0], (int) parameters[1]);
            thePlayer = 0;
            break;
          case "6": // Drone
            if (parameters[3] == 1) { // Seeker
              anItem = new Seekerdrone(jned, (int) parameters[0], (int) parameters[1],
                  (int) parameters[5], (int) parameters[2]);
            } else {
              switch((int) parameters[4]) {
                default:
                  // fall through
                case 0: // Zap
                  anItem = new Zapdrone(jned, (int) parameters[0], (int) parameters[1],
                      (int) parameters[5], (int) parameters[2]);
                  break;
                case 1: // Laser
                  anItem = new Laserdrone(jned, (int) parameters[0], (int) parameters[1],
                      (int) parameters[5], (int) parameters[2]);
                  break;
                case 2: // Chaingun
                  anItem = new Chaingundrone(jned, (int) parameters[0], (int) parameters[1],
                      (int) parameters[5], (int) parameters[2]);
                  break;
              }
            }
            drones.add((Drone) anItem);
            break;
          case "7": // Oneway platform
            anItem = new Oneway(jned, (int) parameters[0], (int) parameters[1],
                (int) parameters[2]);
            break;
          case "8": // Thwump
            anItem = new Thwump(jned, (int) parameters[0], (int) parameters[1],
                (int) parameters[2]);
            break;
          case "9": // Door
            int direction = (int) parameters[2];
            if (parameters[7] == -1) {
              direction = 2;
            }
            if (parameters[8] == -1) {
              direction = 3;
            }
            if (parameters[3] == 1) { // Trap
              anItem = new Trapdoor(jned, (int) parameters[0], (int) parameters[1], direction,
                  (int) parameters[4], (int) parameters[5]);
            } else {
              if (parameters[6] == 1) { // Locked
                anItem = new Lockeddoor(jned, (int) parameters[0], (int) parameters[1], direction,
                    (int) parameters[4], (int) parameters[5]);
              } else { // Normal
                anItem = new Normaldoor(jned, direction, (int) parameters[4], (int) parameters[5]);
              }
            }
            doors.add((Door) anItem);
            break;
          case "10": // Homing launcher
            anItem = new Hominglauncher(jned, (int) parameters[0], (int) parameters[1]);
            break;
          case "11": // Exit
            anItem = new Exit(jned, (int) parameters[0], (int) parameters[1], (int) parameters[2],
                (int) parameters[3]);
            break;
          case "12": // Mine
            anItem = new Mine(jned, (int) parameters[0], (int) parameters[1]);
            break;
          default:
        }
        if(anItem != null) {
          items.add(anItem);
        }
      }
    }
    jned.updateText(outputLevel());
    recalculateDronePaths();
    calculateDronePaths();
  }
  
  // ACTIONS
  public void actionPerformed(ActionEvent ae) {
    push(ae.getActionCommand());
  }
  /**
   * Performs actions that should result from pushing a button or menu item with the given name or
   * action command. Right click menu items route through this method, as do many actions numbers
   * in Jned.
   * @param button action command String 
   */
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
        nudge(1, 0);
        break;
      case "nudgeDown":
        nudge(0, 1);
        break;
      case "nudgeLeft":
        nudge(-1, 0);
        break;
      case "nudgeUp":
        nudge(0, -1);
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
    }
  }
    
  /**
   * In tile mode, expands selection box to whole level. In items/enemies mode, selects every
   * item in the level.
   */
  public void selectAll() {
    if (mode == -1) {
      if (isTiles) {
        deltaColumn = 30;
        deltaRow = 22;
        borderCheck();
      } else {
        clearSelection();
        for(Item item : items) {
          selection.add(item);
          int type = item.getType();
          if (type == 12 ) {
            ((Exit) item).setSelect(true, true);
          } else {
            if (type == 15 || type == 16) {
              ((SwitchDoor) item).setSelect(true, true);
            } else {
              item.setSelect(true);
            }
          }
        }
      }
    }
  }
  
  private void copyTiles (boolean isCut) {
    tileClipboard = new int[Math.abs(deltaColumn) + 1][Math.abs(deltaRow) + 1];
    originColumn = Math.min(mouseColumn, mouseColumn + deltaColumn) - 1;
    originRow = Math.min(mouseRow, mouseRow + deltaRow) - 1;
    for (int i = 0; i < tileClipboard.length; i++) {
      for (int j = 0; j < tileClipboard[0].length; j++) {
        tileClipboard[i][j] = tiles[originColumn + i][originRow + j];
        if (isCut) {
          tiles[originColumn + i][originRow + j] = 0;
        }
      }
    }
    if(isCut) {
      jned.updateText(outputLevel());
      recalculateDronePaths();
      calculateDronePaths();
    }
  }

  private void pasteTiles() {
    if (tileClipboard != null) {
      originColumn = Math.min(mouseColumn, mouseColumn + deltaColumn) - 1;
      originRow = Math.min(mouseRow, mouseRow + deltaRow) - 1;
      for (int i = 0; i <= Math.abs(deltaColumn); i++) {
        for (int j = 0; j <= Math.abs(deltaRow); j++) {
          tiles[originColumn + i][originRow + j] = tileClipboard[i % tileClipboard.length][j % 
               tileClipboard[0].length];
        }
      }
      jned.updateText(outputLevel());
      recalculateDronePaths();
      calculateDronePaths();
    }
  }
  
  private void copyItems(boolean isCut) {
    if (selection.size() == 0) {
      if (rightClickedItem != null) {
        itemClipboard.clear();
        itemClipboard.add(rightClickedItem.duplicate());
        if (rightClickedItem.getType() == 12) {
          Exit exit = (Exit) rightClickedItem;
          clipboardAverageX = (exit.getSuperX() + exit.getSwitchX()) / 2;
          clipboardAverageY = (exit.getSuperY() + exit.getSwitchY()) / 2;
        } else {
          if (rightClickedItem.getType() == 15 || rightClickedItem.getType() == 16) {
            SwitchDoor switchDoor = (SwitchDoor) rightClickedItem;
            int dir = switchDoor.getDirection();
            clipboardAverageX = (switchDoor.getSuperX() + 24 * switchDoor.getRow() + (dir == 0 ? 
                24 : (dir == 2 ? 0 : 12))) / 2;
            clipboardAverageY = (switchDoor.getSuperY() + 24 * switchDoor.getColumn() + (dir == 1
                ? 24 : (dir == 3 ? 0 : 12))) / 2;
          } else {
            clipboardAverageX = rightClickedItem.getX();
            clipboardAverageY = rightClickedItem.getY();
          }
        }
        if(isCut) {
          selection.add(rightClickedItem);
          pushDelete();
        }
      }
    } else {
      itemClipboard.clear();
      int tot = 0;
      clipboardAverageX = 0;
      clipboardAverageY = 0;
      for (Item item : selection) { 
        itemClipboard.add(item.duplicate());
        tot++;
        if (item.getType() == 12) {
          Exit exit = (Exit) item;
          clipboardAverageX += exit.getSuperX() + exit.getSwitchX();
          clipboardAverageY += exit.getSuperY() + exit.getSwitchY();
          tot++;
        } else {
          if (item.getType() == 15 || item.getType() == 16) {
            SwitchDoor switchDoor = (SwitchDoor) item;
            int dir = switchDoor.getDirection();
            clipboardAverageX += switchDoor.getSuperX() + 24 * switchDoor.getRow() + (dir == 0 ?
                24 : (dir == 2 ? 0 : 12));
            clipboardAverageY += switchDoor.getSuperY() + 24 * switchDoor.getColumn() + (dir == 1
                ? 24 : (dir == 3 ? 0 : 12));
            tot++;
          } else {
            clipboardAverageX += item.getX();
            clipboardAverageY += item.getY();
          }
        }
      }
      clipboardAverageX /= tot;
      clipboardAverageY /= tot;
    
      if (isCut) {
        pushDelete();
      }
    }
  }
  
  private void pasteItems() {
    clearSelection();
    Item anItem;
    for (Item item : itemClipboard) {
      anItem = item.duplicate();
      items.add(anItem);
      selection.add(anItem);
      if (anItem.getType() == 12) {
        ((Exit) anItem).setSelect(true, true);
      } else {
        if (anItem.getType() == 15 || anItem.getType() == 16) {
          ((SwitchDoor) anItem).setSelect(true, true);
        } else {
          anItem.setSelect(true);
        }
      }
      if (anItem.getType() == 9) {
        thePlayer = items.size() - 1;
      }
      anItem.setDelta(clipboardAverageX, clipboardAverageY);
      anItem.moveRelative(originRow, originColumn);
      
      if(anItem.getType() == 14 || anItem.getType() == 15 || anItem.getType() == 16) {
        doors.add((Door)anItem);
      }
      if(anItem.getType() == 5 || anItem.getType() == 6 || anItem.getType() == 7 ||
          anItem.getType() == 8) {
        drones.add((Drone)anItem);
      }
    }
    
    jned.updateText(outputLevel());
    recalculateDronePaths();
    calculateDronePaths();
  }
  
  /**
   * Sets all selected tiles to either filled or empty.
   * @param fill true to fill tiles, false to erase them
   */
  public void setTiles (boolean fill) {
    originColumn = Math.min(mouseColumn, mouseColumn + deltaColumn) - 1;
    originRow = Math.min(mouseRow, mouseRow + deltaRow) - 1;
    for (int i = 0; i <= Math.abs(deltaColumn); i++) {
      for (int j = 0; j <= Math.abs(deltaRow); j++) {
        tiles[originColumn + i][originRow + j] = (fill ? 1 : 0);
      }
    }
    jned.updateText(outputLevel());
    recalculateDronePaths();
    calculateDronePaths();
  }
  
  /**
   * Signals all drone objects that their paths are invalid and need to be recalculated.
   */
  public void recalculateDronePaths() {
    for (Drone drone : drones) {
      drone.recalculatePath();
    }
  }
  
  /**
   * Signals all drone objects to recalculate their paths if they are invalid.
   */
  private void calculateDronePaths() {
    for (Drone drone : drones) {
      drone.calculatePath(tiles, doors);
    }
  }
  
  /**
   * Registers an Overlay object with the level area. Indices 0-2 are used for the primary,
   * secondary, and tertiary grid overlays, and index 3 is used for the snap points overlay.
   * @param index the index to place the Overlay at
   * @param overlay the Overlay object to place at that index
   */
  public void setOverlay(int index, Overlay overlay) {
    overlays[index] = overlay;
  }
  
  // MOUSE EVENTS
  /*
  In LevelArea, the mouse interaction is handled at a very low level. A variety of global variables
  are kept pertaining to the state of mouse operations. The MouseListener and MouseMotionListener
  interface methods work in tandem, using said variables to keep track of context. Comments are
  more frequent and explicit in these methods to clarify the various operations.
  */
  public void mouseEntered(MouseEvent me) {
    mouseOn = true;
    if (grabPoint) {
      originRow = me.getX();
      originColumn = me.getY();
      findLaunchpadReference();
      grabPoint = false;
    }
    repaint();
  }
  public void mouseExited(MouseEvent me) {
    mouseOn = false;
    if (isTiles) {
      jned.unHighlight(); // TASK - move to text box
    }
    repaint();
  }
  public void mouseMoved(MouseEvent me) {
    if (isTiles) {
      // Tile mode: previous cell is saved, call to mouseMoveTile finds new cell
      originRow = mouseRow;
      originColumn = mouseColumn;
      mouseMoveTile(me);
      if(originRow != mouseRow || originColumn != mouseColumn) {
        copyPasteMenu.setVisible(false);
      }
      jned.highlightTile(mouseRow - 1 + 23 * (mouseColumn - 1)); // TASK - move to TextBox
    } else {
      if (mode < 0) {
        if (mode == -1) {
          // Selection mode: keep track of item under mouse, do highlighting
          originRow = me.getX();
          originColumn = me.getY();
          boolean getnew = false;
          if (lastItem != null) {
            if (!lastItem.overlaps(me.getX(), me.getY())) {
              lastItem.setHighlight(false);
              lastItem = null;
              getnew = true;
              jned.unHighlight(); // TASK - move to TextBox
            }
          } else {
            getnew = true;
          }
          if(getnew) {
            for (int i = items.size()-1; i >= 0; i--) {
              if (items.get(i).overlaps(me.getX(),me.getY())) {
                lastItem = items.get(i);
                lastItem.setHighlight(true);
                jned.highlightItem(itemIndices[i]); // TASK - move to TextBox
                break;
              }
            }
          }
        } else { //Launchpad editing mode
          double val;
          switch (mode) {
            case -2: // Power
              double theta = -launchPads.get(0).getDirection();
              val = ((me.getX() - deltaRow) * Math.cos(theta) - (me.getY() - deltaColumn) *
                  Math.sin(theta)) / 24.0;
              if (val > 0.000000000000001 || val < -0.000000000000001) {
                launchPads.get(0).setPower(val);
                for(int i = 1; i < launchPads.size(); i++) {
                  launchPads.get(i).setPower(val);
                }
              }
              break;
            case -3: // Direction
              val = Math.atan2(me.getY() - deltaColumn, me.getX() - deltaRow);
              launchPads.get(0).setDirection(val);
              for(int i = 1; i < launchPads.size(); i++) {
                launchPads.get(i).setDirection(val);
              }
              break;
            case -4: // Both
              val = (me.getX() - deltaRow) / 24.0;
              double val2 = (me.getY() - deltaColumn) / 24.0;
              launchPads.get(0).setPowerX(val);
              launchPads.get(0).setPowerY(val2);
              for(int i = 1; i < launchPads.size(); i++) {
                launchPads.get(i).setPowerX(val);
                launchPads.get(i).setPowerY(val2);
              }
              break;
              default:
          }
        }
      } else {
        // Item adding mode: store the position to add an item in the delta variables
        deltaRow = me.getX();
        deltaColumn = me.getY();
        if (mode >= LevelArea.NDOOR && mode <= LevelArea.TDOOR + 3 && !addingSwitch) {
          // Door positions are cell coordinates
          deltaRow /= cellSize;
          deltaColumn /= cellSize;
        } else {
          if (mode >= LevelArea.ZAP && mode <= LevelArea.CHAINGUN + 27) {
            // Drones should always be snapped to the centers of cells
            deltaRow = (deltaRow / cellSize) * cellSize + cellSize / 2;
            deltaColumn = (deltaColumn / cellSize) * cellSize + cellSize / 2;
          } else {
            if (snapTo) {
              deltaRow = snapCoord(deltaRow, true);
            }
            if(mode == LevelArea.FLOOR) {
              // Floorguards should always have the y coordinate snapped to 3/4 past a cell edge
              deltaColumn = (deltaColumn / cellSize) * cellSize + 3 * cellSize / 4;
            } else {
              if(snapTo) {
                deltaColumn = snapCoord(deltaColumn, false);
              }
            }
          }
        }
      }
    }
    repaint();
  }
  public void mouseDragged(MouseEvent me) {
    if (buttonDown==1) {
      if (isTiles) {
        mouseMoveTile(me);
        if (mode == -1) {
          // Selection mode: drag selection box
          if (originColumn != mouseColumn || originRow != mouseRow) {
            deltaColumn = originColumn - mouseColumn;
            deltaRow = originRow - mouseRow;
            dragged = true;
          }
        } else {
          //Not selection mode: edit all selected tiles
          edit();
        }
      } else {
        if(mode == -1) {
          // Item selection mode: drag selection box, or drag selected items
          deltaRow = me.getX();
          deltaColumn = me.getY();
          
          if (!drawingSelectionBox) {
            if (snapTo) {
              lastItem.moveTo(snapCoord(deltaRow, true), snapCoord(deltaColumn, false));
              for (Item it : selection) {
                it.moveRelative(lastItem.getX(), lastItem.getY());
              }
            } else {
              for (Item it : selection) {
                it.moveRelative(deltaRow, deltaColumn);
              }
            }
          }
        }
      }
    }
    repaint();
  }
  public void mousePressed(MouseEvent me) {
    if (me.getButton() == MouseEvent.BUTTON1) {
      buttonDown = 1;
      if (isTiles) {
        // Tile mode: save the click point in preparation for a drag
        originRow = mouseRow;
        originColumn = mouseColumn;
        dragged = false;
      } else {
        if (mode < 0) {
          if (mode == -1) { //Selection mode
            // Item selection mode: save click point in preparation for a drag
            originRow = me.getX();
            originColumn = me.getY();
            if(lastItem == null || keys.isShiftPushed()) {
              // Clicking on nothing or shift-clicking initiates a drag
              if (!keys.isCtrlPushed()) {
                clearSelection();
              }
              drawingSelectionBox = true;
              // ! - These must be set now to prevent a drag box being drawn from this point to
              // their previous values in the first frame 
              deltaRow = me.getX();
              deltaColumn = me.getY();
            } else {
              if (lastItem.isSelected() && keys.isCtrlPushed()) {
                // Ctrl-clicking on a selected item de-selects it
                selection.remove(lastItem);
                lastItem.setSelect(false);
                // ...and initiates a drag, just like clicking on nothing
                drawingSelectionBox = true;
                deltaRow = me.getX();
                deltaColumn = me.getY();
              } else {
                if (!lastItem.isSelected() && !keys.isCtrlPushed()) {
                  // Normal-clicking a non-selected item clears the selection
                  clearSelection();
                }
                selection.remove(lastItem);
                selection.add(lastItem);
                lastItem.setSelect(true);
                
                // All selected items are given reference coordinates in preparation for a drag
                if(snapTo) {
                  for (Item item : selection) {
                      item.setDelta(lastItem.getX(), lastItem.getY());
                  }
                } else {
                  for (Item item : selection) {
                    item.setDelta(originRow, originColumn);
                  }
                }
              }
            }
          }
        }
      }
    }
    if(me.getButton() == MouseEvent.BUTTON3) {
      buttonDown = 2;
    }
    repaint();
  }
  public void mouseReleased(MouseEvent me) {
    buttonDown = 0;
    if (me.getButton() == MouseEvent.BUTTON1) {
      if (isTiles) {
        if (mode == -1) {
          // Tile selection mode: clicking without dragging reduces selection box back to 1 cell
          if (!dragged) {
            deltaRow = 0;
            deltaColumn = 0;
          }
        } else {
          edit();
        }
      } else {
        if (mode < 0) {
          if (mode == -1) {
            // Item selection mode: selects all items in dragged selection box
            if (drawingSelectionBox) {
              Rectangle selectionRectangle = new Rectangle(Math.min(originRow, deltaRow),
                  Math.min(originColumn, deltaColumn), Math.abs(deltaRow - originRow),
                  Math.abs(deltaColumn - originColumn));
              for (Item item : items) {
                if (item.overlaps(selectionRectangle)) {
                  selection.remove(item);
                  selection.add(item);
                  item.setSelect(true);
                }
              }
              drawingSelectionBox = false;
            }
          } else {
            // Launchpad modes: mouse release exits mode
            mode = -1;
          }
        } else {
          // Item adding modes: adds item
          int type = getType(mode);
          Item anItem = null;
          switch(type) {
            case 0:
              anItem = new Gaussturret(jned, deltaRow, deltaColumn);
              break;
            case 1:
              anItem = new Hominglauncher(jned, deltaRow, deltaColumn);
              break;
            case 2:
              anItem = new Mine(jned, deltaRow, deltaColumn);
              break;
            case 3:
              anItem = new Floorguard(jned, deltaRow, deltaColumn);
              break;
            case 4:
              anItem = new Thwump(jned, deltaRow, deltaColumn, mode - LevelArea.THWUMP);
              break;
            case 5:
              anItem = new Zapdrone(jned, deltaRow, deltaColumn, (mode - LevelArea.ZAP) / 7, (mode
                  - LevelArea.ZAP) % 7);
              drones.add((Drone) anItem);
              break;
            case 6:
              anItem = new Seekerdrone(jned, deltaRow, deltaColumn, (mode - LevelArea.SEEKER) / 7,
                  (mode - LevelArea.SEEKER) % 7);
              drones.add((Drone) anItem);
              break;
            case 7:
              anItem = new Laserdrone(jned, deltaRow, deltaColumn, (mode - LevelArea.LASER) / 7,
                  (mode - LevelArea.LASER) % 7);
              drones.add((Drone) anItem);
              break;
            case 8:
              anItem = new Chaingundrone(jned, deltaRow, deltaColumn, (mode - LevelArea.CHAINGUN)
              / 7, (mode - LevelArea.CHAINGUN) % 7);
              drones.add((Drone) anItem);
              break;
            case 9:
              anItem = new Player(jned, deltaRow, deltaColumn);
              thePlayer = items.size();
              break;
            case 10:
              anItem = new Gold(jned, deltaRow, deltaColumn);
              break;
            case 11:
              anItem = new Bounceblock(jned, deltaRow, deltaColumn);
              break;
            case 12:
              if (addingSwitch) {
                // Exit adding completed
                anItem = new Exit(jned, originRow, originColumn, deltaRow, deltaColumn);
                addingSwitch = false;
              } else {
                // Door added, change to switch adding mode
                originRow = deltaRow;
                originColumn = deltaColumn;
                deltaRow = me.getX();
                deltaColumn = me.getY();
                addingSwitch = true;
              }
              break;
            case 13:
              anItem = new Oneway(jned, deltaRow, deltaColumn, mode - LevelArea.ONEWAY);
              break;
            case 14:
              anItem = new Normaldoor(jned, mode-LevelArea.NDOOR,deltaRow,deltaColumn);
              doors.add((Door) anItem);
              break;
            case 15:
              // fall through
            case 16:
              if(addingSwitch) {
                // Switchdoor adding completed
                if (type == 15) {
                  anItem = new Lockeddoor(jned, deltaRow, deltaColumn, mode - LevelArea.LDOOR,
                      originRow, originColumn);
                } else {
                  anItem = new Trapdoor(jned, deltaRow, deltaColumn, (mode - LevelArea.NDOOR) % 4,
                      originRow, originColumn);
                }
                addingSwitch = false;
                doors.add((Door) anItem);
              } else {
                // Door added, change to switch adding mode
                originRow = deltaRow;
                originColumn = deltaColumn;
                deltaRow = me.getX();
                deltaColumn = me.getY();
                addingSwitch = true;
              }
              break;
            case 17:
              anItem = new Launchpad(jned, deltaRow, deltaColumn, mode - LevelArea.LAUNCH);
              break;
            case -1:
              // fall through
            default:
          }
          if (anItem != null) {
            items.add(anItem);
          }
        }
      }
      jned.updateText(outputLevel());
      calculateDronePaths(); // TASK - test adding a door in a drone path
    }
    if (me.getButton() == MouseEvent.BUTTON3) {
      if (isTiles) {
        if( mode == -1) {
          copyPasteMenu.show(this, me.getX(), me.getY());
        }
      } else {
        if (mode == -1) {
          // Item selection mode right-click: saves mouse point, shows drop-down menu
          originRow = me.getX();
          originColumn = me.getY();
          if (drawingSelectionBox) {
            drawingSelectionBox = false;
          } else {
            // Right-click menu is compiled with appropriate actions per item using items' flags
            if (selection.size() > 0) {
              int flags = 0;
              for (Item item : selection) {
                flags = flags | item.getFlags();
              }
              compileMenu(flags);
              itemMenu.show(this, me.getX(), me.getY());
            } else {
              if (lastItem != null) {
                rightClickedItem = lastItem;
                compileMenu(rightClickedItem.getFlags());
                itemMenu.show(this, me.getX(), me.getY());
              } else {
                rightClickedItem = null;
                compileMenu(0);
                itemMenu.show(this, me.getX(), me.getY());
              }
            }
          }
        } else {
          if(mode < -1) {
            mode = -1;
            jned.updateText(outputLevel());
          }
        }
      }
    }
    repaint();
  }
  // Returns the nearest snap point to a given coordinate
  protected int snapCoord(int coordinate, boolean isX) {
    scratch1 = overlays[3].getPoints(isX);
    int ind = Collections.binarySearch(scratch1, coordinate);
    if (ind < 0) {
      ind = -ind - 1;
    }
    try {
      if(coordinate - scratch1.get(ind - 1) < scratch1.get(ind) - coordinate) {
        return scratch1.get(ind - 1);
      } else {
        return scratch1.get(ind);
      }
    } catch (IndexOutOfBoundsException ioobe) {
      // Coordinates outside the level area are not snapped
      return coordinate;
    }
  }
  // Updates mouse row and column
  private void mouseMoveTile (MouseEvent me) {
    mouseColumn = (me.getX()) / cellSize;
    mouseRow = (me.getY()) / cellSize;
    borderCheck();
  }
  // Keeps the position of the tile selection box inside the level bounds
  private void borderCheck() {
    mouseColumn -= Math.min(Math.min(0, deltaColumn) + mouseColumn - 1, 0) + Math.max(Math.max(0,
        deltaColumn) + mouseColumn - 31, 0);
    mouseRow -= Math.min(Math.min(0, deltaRow) + mouseRow - 1, 0) + Math.max(Math.max(0, deltaRow)
        + mouseRow - 23, 0);
  }  
  
  /**
   * Returns the item type number for a given mode. Type numbers range from 0 to 17, corresponding
   * to standard index order used throughout Jned.
   * @param code item mode, corresponding to the item editing mode numbers of LevelArea
   * @return item type, or -1 for an invalid mode
   */
  public int getType(int code) {
    if (code == LevelArea.GAUSS) return 0;
    if (code == LevelArea.HOMING) return 1;
    if (code == LevelArea.MINE) return 2;
    if (code == LevelArea.FLOOR) return 3;
    if (code >= LevelArea.THWUMP && code <= LevelArea.THWUMP + 3) return 4;
    if (code >= LevelArea.ZAP && code <= LevelArea.ZAP + 27) return 5;
    if (code >= LevelArea.SEEKER && code <= LevelArea.SEEKER + 27) return 6;
    if (code >= LevelArea.LASER && code <= LevelArea.LASER + 27) return 7;
    if (code >= LevelArea.CHAINGUN && code <= LevelArea.CHAINGUN + 27) return 8;
    if (code == LevelArea.PLAYER) return 9;
    if (code == LevelArea.GOLD) return 10;
    if (code == LevelArea.BOUNCE) return 11;
    if (code == LevelArea.EXIT) return 12;
    if (code >= LevelArea.ONEWAY && code <= LevelArea.ONEWAY + 3) return 13;
    if (code >= LevelArea.NDOOR && code <= LevelArea.NDOOR + 3) return 14;
    if (code >= LevelArea.LDOOR && code <= LevelArea.LDOOR + 3) return 15;
    if (code >= LevelArea.TDOOR && code <= LevelArea.TDOOR + 3) return 16;
    if (code >= LevelArea.LAUNCH && code <= LevelArea.LAUNCH + 7) return 17;
    return -1;
  }
  
  private void clearSelection() {
    for (Item item: selection) {
      item.setSelect(false);
    }
    selection.clear();
  }
  
  public void pushDelete() {
    if (selection.size() > 0 ) {
      for (Item item: selection) {
        removeItem(item);
      }
      selection.clear();
    } else {
      if (rightClickedItem != null) {
        removeItem(rightClickedItem);
      }
    }
    jned.updateText(outputLevel());
  }
  
  private void removeItem(Item item) {
    int ind = items.indexOf(item);
    if (ind == thePlayer) {
      // When active player is deleted, a new active player is selected
      boolean notfound = true;
      for (int i = items.size() - 1; i >= 0; i--) {
        if (items.get(i).getType() == 9 && thePlayer != i) {
          thePlayer = i;
          notfound = false;
          break;
        }
      }
      if (notfound) {
        thePlayer = -1;
      }
    }
    if (thePlayer > ind) {
      thePlayer--;
    }
    items.remove(item);
    
    drones.remove(item);
    if (doors.remove(item)) {
      recalculateDronePaths();
      calculateDronePaths();
    }
  }
  
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    // Border
    g.setColor(Colors.TILE_FILL);
    g.fillRect(0, 0, getWidth(), cellSize);
    g.fillRect(0, getHeight() - cellSize, getWidth(), cellSize);
    g.fillRect(0, cellSize, cellSize, getHeight() - 2 * cellSize);
    g.fillRect(getWidth() - cellSize, cellSize, cellSize, getHeight() - 2 * cellSize);
    
    // Tiles
    //g.setColor(Colors.TILE_FILL); // TASK - remove after testing this
    int tx;
    int ty;
    for (int i = 0; i < 31; i++) {
      for (int j = 0; j < 23; j++) {
        if (tiles[i][j] != 0) {
          tx = (i + 1) * cellSize;
          ty = (j + 1) * cellSize;
          switch (tiles[i][j]) {
            case 1:
              g.fillRect(tx, ty, cellSize, cellSize);
              break;
            
            // 45 degree
            case 2: // Q
              {int[] xs = {tx + cellSize, tx, tx + cellSize};
              int[] ys = {ty, ty + cellSize, ty + cellSize};
              g.fillPolygon(xs, ys, 3);}
              break;
            case 3: // W
              {int[] xs = {tx, tx, tx + cellSize};
              int[] ys = {ty, ty + cellSize, ty + cellSize};
              g.fillPolygon(xs, ys, 3);}
              break;
            case 4: // S
              {int[] xs = {tx, tx + cellSize, tx};
              int[] ys = {ty, ty, ty + cellSize};
              g.fillPolygon(xs, ys, 3);}
              break;
            case 5: // A
              {int[] xs = {tx, tx + cellSize, tx + cellSize};
              int[] ys = {ty, ty, ty + cellSize};
              g.fillPolygon(xs, ys, 3);}
              break;
            // 63 degree thin
            case 6: // Q
              {int[] xs = {tx + cellSize, tx + cellSize / 2, tx + cellSize};
              int[] ys = {ty, ty + cellSize, ty + cellSize};
              g.fillPolygon(xs, ys, 3);}
              break;
            case 7: // W
              {int[] xs = {tx, tx, tx + cellSize / 2};
              int[] ys = {ty, ty + cellSize, ty + cellSize};
              g.fillPolygon(xs, ys, 3);}
              break;
            case 8: // S
              {int[] xs = {tx, tx + cellSize / 2, tx};
              int[] ys = {ty, ty, ty + cellSize};
              g.fillPolygon(xs, ys, 3);}
              break;
            case 9: // A
              {int[] xs = {tx + cellSize / 2, tx + cellSize, tx + cellSize};
              int[] ys = {ty, ty, ty + cellSize};
              g.fillPolygon(xs, ys, 3);}
              break;
            
            // 27 degree thin
            case 10: // Q
              {int[] xs = {tx + cellSize, tx, tx + cellSize};
              int[] ys = {ty + cellSize / 2, ty + cellSize, ty + cellSize};
              g.fillPolygon(xs, ys, 3);}
              break;
            case 11: // W
              {int[] xs = {tx, tx, tx + cellSize};
              int[] ys = {ty + cellSize / 2, ty + cellSize, ty + cellSize};
              g.fillPolygon(xs, ys, 3);}
              break;
            case 12: // S
              {int[] xs = {tx, tx + cellSize, tx};
              int[] ys = {ty, ty, ty + cellSize / 2};
              g.fillPolygon(xs, ys, 3);}
              break;
            case 13: // A
              {int[] xs = {tx, tx + cellSize, tx + cellSize};
              int[] ys = {ty, ty, ty + cellSize / 2};
              g.fillPolygon(xs, ys, 3);}
              break;
            
            // Concave curve
            case 14: // Q
              g.fillRect(tx, ty, cellSize, cellSize);
              g.setColor(Colors.TILE_SPACE);
              g.fillArc(tx - cellSize, ty - cellSize, cellSize * 2 - 1, cellSize * 2 - 1, 270, 90);
              g.setColor(Colors.TILE_FILL);
              break;
            case 15: // W
              g.fillRect(tx, ty, cellSize, cellSize);
              g.setColor(Colors.TILE_SPACE);
              g.fillArc(tx, ty - cellSize, cellSize * 2 - 1, cellSize * 2 - 1, 180, 90);
              g.setColor(Colors.TILE_FILL);
              break;
            case 16: // S
              g.fillRect(tx, ty, cellSize, cellSize);
              g.setColor(Colors.TILE_SPACE);
              g.fillArc(tx, ty, cellSize * 2 - 1, cellSize * 2 - 1, 90, 90);
              g.setColor(Colors.TILE_FILL);
              break;
            case 17: // A
              g.fillRect(tx, ty, cellSize, cellSize);
              g.setColor(Colors.TILE_SPACE);
              g.fillArc(tx - cellSize, ty, cellSize * 2 - 1, cellSize * 2 - 1, 0, 90);
              g.setColor(Colors.TILE_FILL);
              break;
            
            // Half tile
            case 18: // Q
              g.fillRect(tx, ty, cellSize / 2, cellSize);
              break;
            case 19: // W
              g.fillRect(tx, ty, cellSize, cellSize / 2);
              break;
            case 20: // S
              g.fillRect(tx + cellSize / 2, ty, cellSize / 2, cellSize);
              break;
            case 21: // A
              g.fillRect(tx, ty + cellSize / 2, cellSize, cellSize / 2);
              break;
            
            // 63 degree thick
            case 22: // Q
              {int[] xs = {tx + cellSize / 2, tx + cellSize, tx + cellSize, tx};
              int[] ys = {ty, ty, ty + cellSize, ty + cellSize};
              g.fillPolygon(xs, ys, 4);}
              break;
            case 23: // W
              {int[] xs = {tx, tx + cellSize / 2, tx + cellSize, tx};
              int[] ys = {ty, ty, ty + cellSize, ty + cellSize};
              g.fillPolygon(xs, ys, 4);}
              break;
            case 24: // S
              {int[] xs = {tx, tx + cellSize, tx + cellSize / 2, tx};
              int[] ys = {ty, ty, ty + cellSize, ty + cellSize};
              g.fillPolygon(xs, ys, 4);}
              break;
            case 25: // A
              {int[] xs = {tx, tx + cellSize, tx + cellSize, tx + cellSize / 2};
              int[] ys = {ty, ty, ty + cellSize, ty + cellSize};
              g.fillPolygon(xs, ys, 4);}
              break;
            
            // 27 degree thick
            case 26: // Q
              {int[] xs = {tx, tx + cellSize, tx + cellSize, tx};
              int[] ys = {ty + cellSize / 2, ty, ty + cellSize, ty + cellSize};
              g.fillPolygon(xs, ys, 4);}
              break;
            case 27: // W
              {int[] xs = {tx, tx + cellSize, tx + cellSize, tx};
              int[] ys = {ty, ty + cellSize / 2, ty + cellSize, ty + cellSize};
              g.fillPolygon(xs, ys, 4);}
              break;
            case 28: // S
              {int[] xs = {tx, tx + cellSize, tx + cellSize, tx};
              int[] ys = {ty, ty, ty + cellSize / 2, ty + cellSize};
              g.fillPolygon(xs, ys, 4);}
              break;
            case 29: // A
              {int[] xs = {tx, tx + cellSize, tx + cellSize, tx};
              int[] ys = {ty, ty, ty + cellSize, ty + cellSize / 2};
              g.fillPolygon(xs, ys, 4);}
              break;
            
            // Convex curve
            case 30: // Q
              g.fillArc(tx, ty, cellSize * 2, cellSize * 2, 90, 90);
              break;
            case 31: // W
              g.fillArc(tx - cellSize, ty, cellSize * 2, cellSize * 2, 0, 90);
              break;
            case 32: // S
              g.fillArc(tx - cellSize, ty - cellSize, cellSize * 2, cellSize * 2, 270, 90);
              break;
            case 33: // A
              g.fillArc(tx, ty - cellSize, cellSize * 2, cellSize * 2, 180, 90);
              break;
              
            default:
          }
        }
      }
    }
    
    // Gridlines
    if (drawingGrid) {
      for (int i = 2; i >= 0; i--) {
        if (overlays[i] != null) {
          if (overlays[i].isOn()) {
            switch (i) {
              case 0:
                g.setColor(Colors.PRIMARY_GRID);
                break;
              case 1:
                g.setColor(Colors.SECONDARY_GRID);
                break;
              case 2:
                g.setColor(Colors.TERTIARY_GRID);
                break;
              default:
            }
            scratch1 = overlays[i].getPoints(true);
            for (Integer coordinate : scratch1) {
              if (coordinate > 0 && coordinate < getWidth() - 2 * cellSize - 1) {
                g.drawLine(coordinate + cellSize, cellSize, coordinate + cellSize, getHeight() -
                    cellSize - 1);
              }
            }
            scratch1 = overlays[i].getPoints(false);
            for (Integer coordinate : scratch1) {
              if (coordinate > 0 && coordinate < getHeight() - 2 * cellSize - 1) {
                g.drawLine(cellSize, coordinate + cellSize, getWidth() - cellSize - 1, coordinate
                    + cellSize);
              }
            }
          }
        }
      }
    }
    
    // Snap points
    if (drawingSnapPoints) {
      if( overlays[3] != null) {
        g.setColor(Colors.SNAP_POINTS);
        scratch1 = overlays[3].getPoints(true);
        scratch2 = overlays[3].getPoints(false);
        for (Integer xCoordinate : scratch1) {
          if (xCoordinate > 0 && xCoordinate < getWidth() - 1) {
            for (Integer yCoordinate : scratch2) {
              if (yCoordinate > 0 && yCoordinate < getHeight() - 1) {
                g.drawLine(xCoordinate, yCoordinate, xCoordinate, yCoordinate);
              }
            }
          }
        }
      }
    }
    
    // Tile selection box
    if (mouseOn || copyPasteMenu.isVisible()) {
      if (isTiles) {
        g.setColor(Colors.TILE_SELECT);
        g.drawRect((Math.min(0, deltaColumn) + mouseColumn) * cellSize, (Math.min(0, deltaRow)
            + mouseRow) * cellSize, (Math.abs(deltaColumn) + 1) * cellSize - 1, (Math.abs(deltaRow)
            + 1) * cellSize - 1);
      }
    }
    
    // Items
    for (Item item : items) {
      item.paint(g);
      if (drawingTriggers) {
        item.paintTrigger(g);
      }
      if (mode < -1) {
        for (Launchpad lp : launchPads) {
          lp.paintLine(g);
        }
      }
    }
    if (drawingDronePaths) {
      for (Drone drone : drones) {
        drone.paintPath(g);
      }
    }
    
    if (mouseOn) {
      if (!isTiles) {
        if (mode == -1) {
          // Item selection box
          if (drawingSelectionBox) {
            g.setColor(Colors.SELECTION_BOX);
            g.drawRect(Math.min(originRow, deltaRow), Math.min(originColumn, deltaColumn),
                Math.abs(deltaRow - originRow), Math.abs(deltaColumn - originColumn));
          }
        } else {
          // Ghosts
          int type = getType(mode);
          switch (type){
            case 0:
              Gaussturret.paintGhost(deltaRow, deltaColumn, g);
              break;
            case 1:
              Hominglauncher.paintGhost(deltaRow, deltaColumn, g);
              break;
            case 2:
              Mine.paintGhost(deltaRow, deltaColumn, g);
              break;
            case 3:
              Floorguard.paintGhost(deltaRow, deltaColumn, g);
              break;
            case 4:
              Thwump.paintGhost(deltaRow,deltaColumn,g);
              break;
            case 5:
              Zapdrone.paintGhost(type, deltaRow, deltaColumn, (mode - LevelArea.ZAP) / 4, g);
              break;
            case 6:
              Seekerdrone.paintGhost(type, deltaRow, deltaColumn, (mode - LevelArea.SEEKER) / 4, g);
              break;
            case 7:
              Laserdrone.paintGhost(type, deltaRow, deltaColumn, (mode - LevelArea.LASER) / 4, g);
              break;
            case 8:
              Chaingundrone.paintGhost(type, deltaRow, deltaColumn, (mode - LevelArea.CHAINGUN) /
                  4, g);
              break;
            case 9:
              Player.paintGhost(deltaRow, deltaColumn, g);
              break;
            case 10:
              Gold.paintGhost(deltaRow, deltaColumn, g);
              break;
            case 11:
              Bounceblock.paintGhost(deltaRow, deltaColumn, g);
              break;
            case 12:
              if (addingSwitch) {
                Exit.paintSwitchGhost(originRow, originColumn, deltaRow, deltaColumn, g);
              } else {
                Exit.paintDoorGhost(deltaRow, deltaColumn, g);
              }
              break;
            case 13:
              Oneway.paintGhost(deltaRow, deltaColumn, mode - LevelArea.ONEWAY, g);
              break;
            case 14:
              Normaldoor.paintGhost(mode - LevelArea.NDOOR, deltaRow, deltaColumn, g);
              break;
            case 15:
              if (addingSwitch) {
                Lockeddoor.paintSwitchGhost(deltaRow, deltaColumn, mode - LevelArea.LDOOR,
                    originRow, originColumn, g);
              } else {
                Lockeddoor.paintDoorGhost(mode - LevelArea.LDOOR, deltaRow, deltaColumn, g);
              }
              break;
            case 16:
              if (addingSwitch) {
                Trapdoor.paintSwitchGhost(deltaRow, deltaColumn, (mode - LevelArea.NDOOR) % 4,
                    originRow, originColumn, g);
              } else {
                Trapdoor.paintDoorGhost(mode - LevelArea.TDOOR, deltaRow, deltaColumn, g);
              }
              break;
            case 17:
              Launchpad.paintGhost(deltaRow, deltaColumn, mode - LevelArea.LAUNCH, g);
              break;
            default:
          }
        }
      }
    }
  }
  
  /**
   * Nudges positions of all selected items, or the item under the mouse if none are selected.
   * @param xAmount pixels to move horizontally. Negative for left, positive for right. 
   * @param yAmount pixels to move vertically. Negative for up, positive for down.
   */
  public void nudge(int xAmount, int yAmount) {
    if (selection.size() > 0) {
      for (Item item : selection) {
        item.setDelta(originRow, originColumn);
        item.moveRelative(originRow + xAmount, originColumn + yAmount);
      }
    } else {
      if (rightClickedItem != null) {
        if (rightClickedItem.getType() == 15 || rightClickedItem.getType() == 16) {
          if (!((SwitchDoor) rightClickedItem).overlapsDoor(originRow, originColumn)) {
            rightClickedItem.moveTo(rightClickedItem.getX() + xAmount, rightClickedItem.getY() +
              yAmount);
          }
        } else {
          rightClickedItem.moveTo(rightClickedItem.getX() + xAmount, rightClickedItem.getY() +
            yAmount); // TASK - remember why this can't be done with setDelta/moveRelative
        }
      }
    }
    jned.updateText(outputLevel());
  }
  
  /**
   * Changes the direction of all selected items, or the item under the mouse if none are selected.
   * @param newDirection direction to change to
   */
  public void changeDirection(int newDirection) {
    if (selection.size() > 0) {
      for (Item item : selection) {
        if ((item.getType() >= 4 && item.getType() <= 8) || (item.getType() >= 13 &&
            item.getType() <= 16)) {
          ((DirectionalItem) item).setDirection(newDirection / 2);
        }
        if (item.getType() == 17) {
          ((Launchpad) item).setDirection(newDirection * Math.PI / 4.0);
        }
      }
    } else {
      if (rightClickedItem != null) {
        if ((rightClickedItem.getType() >= 4 && rightClickedItem.getType() <= 8) ||
          (rightClickedItem.getType() >= 13 && rightClickedItem.getType() <= 16)) {
          ((DirectionalItem) rightClickedItem).setDirection(newDirection / 2);
        }
        if (rightClickedItem.getType() == 17) {
          ((Launchpad) rightClickedItem).setDirection(newDirection * Math.PI / 4.0);
        }
      }
    }
    jned.updateText(outputLevel());
    recalculateDronePaths();
    calculateDronePaths();
  }
  
  /**
   * Changes the behavior of all selected drones, or the item under the mouse if none are selected.
   * @param newBehavior behavior to change to
   */
  public void changeBehavior(int newBehavior) {
    if (selection.size() > 0) {
      for (Item item : selection) {
        if (item.getType() >= 5 && item.getType() <= 8) {
          ((Drone) item).setBehavior(newBehavior);
        }
      }
    } else {
      if (rightClickedItem != null) {
        if (rightClickedItem.getType() >= 5 && rightClickedItem.getType() <= 8) {
          ((Drone) rightClickedItem).setBehavior(newBehavior);
        }
      }
    }
    jned.updateText(outputLevel());
    recalculateDronePaths();
    calculateDronePaths();
  }
  
  /**
   * Changes the active player to one selected Player, or the Player under the mouse if none are
   * selected.
   */
  public void setActivePlayer() {
    if (selection.size() > 0) {
      for (Item item : selection) {
        if (item.getType() == 9) {
          thePlayer = items.indexOf(item);
        }
      }
    } else {
      if (rightClickedItem != null) {
        if (rightClickedItem.getType() == 9) {
          thePlayer = items.indexOf(rightClickedItem);
        }
      }
    }
    jned.updateText(outputLevel());
  }
  
  // TASK - CHANGE ALL LAUNCHPAD EDITING OPERATIONS
  // Finds all selected launchpads or launchpad undermouse and places in launchPads array
  private void findLaunchpads() {
    launchPads.clear();
    if (selection.size() > 0) {
      for (Item item : selection) {
        if (item.getType() == 17) {
          launchPads.add((Launchpad) item);
        }
      }
    } else {
      if (rightClickedItem != null) {
        if (rightClickedItem.getType() == 17) {
          launchPads.add((Launchpad) rightClickedItem);
        }
      }
    }
    // To get around the problem of grabbing the mouse position while mouse events are being
    // absorbed by a pop-up menu, this boolean value is set. The mouseEnterred() event will read it
    // and immediately record the mouse position. Used for launchpad editing.
    grabPoint = true;
  }
  // Called after the mouseEnterred() event. Sets reference point for mouse-following launchpad
  // editing operations. Point is set to position of imaginary launchpad object with the mouse
  // point at the peak of its power/direction line.
  private void findLaunchpadReference() {
    deltaRow = originRow - (int)(launchPads.get(0).getPowerX() * 24); 
    deltaColumn = originColumn - (int)(launchPads.get(0).getPowerY() * 24);
  }
  
  public void mouseClicked(MouseEvent me) {}
}