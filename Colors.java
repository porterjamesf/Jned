import java.awt.Color;

/**
 * Repository of defined static Color classes for use throughout Jned.
 * @author James Porter
 */
public class Colors {
  public static Color BUTTON_HL = new Color(255,255,255,96); // The highlight translucent layer for unpushed buttons
  public static Color BUTTON_PDHL = new Color(255,255,255,48); // The highlight translucent layer for pushed buttons
  public static Color BUTTON_DIS = new Color(128,128,128,128); // The translucent layer for disabled buttons
  public static Color BUTTON_SEL = new Color(255,64,64); // The border drawn around a selected level button
  public static Color BG_COLOR = new Color(0x70707F); // Color used for borders and blank neutral space
  public static Color DROP_COLOR = new Color(0x80808F); // Color of drop-down panels for gridlines and snapping
  public static Color DROP_BORDER = new Color(0x40404F); // Drop-down panel border color
  public static Color TILE_FILL = new Color(0x797988); // Color used for tiles in level
  public static Color TILE_SPACE = new Color(0xCACAD0); // Color used for level area
  public static Color TILE_SELECT = new Color(0xF0F0F0); // Selection box for tile editing box
  public static Color SELECTION_BOX = new Color(0xF0F0F0); // Draggable selection box for items
  public static Color PUSHED = new Color(0x2A2A26); // Pushed button
  public static Color PUSHED_BORDER = new Color(0x9A9A96); // Pushed button border
  public static Color PUSHEDTXT = new Color(0x80808F); // Text on a pushed button
  public static Color UNPUSHED = new Color(0xD5D5D9); // Unpushed button
  public static Color UNPUSHED_BORDER = new Color(0x555559); // Unpushed button border
  public static Color UNPUSHEDTXT = new Color(0x20202F); // Text on an unpushed button
  public static Color PANEL_COLOR = new Color(0x9999A8); // Drop-down panels, other overlay neutral space
  public static Color TEXT_COLOR = new Color(0x020205); // Textbox text color
  public static Color PRIMARY_GRID = new Color(0xEFEFF3); // Primary grid lines
  public static Color SECONDARY_GRID = new Color(0xDFDFE3); // Secondary grid lines
  public static Color TERTIARY_GRID = new Color(0xBDBDC1); // Tertiary grid lines
  public static Color SNAP_POINTS = new Color(0x8A8A92); // Snap point grid
  public static Color ITEM = new Color(0x25252F); // TEMPORARY - Items
  public static Color ITEM_GHOST = new Color(0,0,0,128); // TEMPORARY - Item ghosts (faint version shown for adding mode
  public static Color ITEM_HL_A = new Color(255,255,255,64); // The highlight translucent layer for highlighted items
  public static Color ITEM_SELECT_A = new Color(128,128,255,128); // The highlight translucent layer for selected items
  public static Color DOOR_TRIGGER = new Color(121,121,136,128); // Lines between doors and switches
  public static Color DRONE_PATH = new Color(0,0,0,196); // Lines showing the path a drone will follow
  public static Color LAUNCHPAD_LINE = new Color(240,64,64,196); // Line showing the power and direction of a launchpad
}