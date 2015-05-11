import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * An Exit Door item in Jned.
 * @author James Porter
 */
public class Exit extends Item {
  private int switchX;
  private int switchY;
  private int deltaSwitchX;
  private int deltaSwitchY;
  
  private Rectangle doorRectangle;
  private Rectangle switchRectangle;
  
  private boolean overswitch;
  private boolean overdoor;
  private boolean switchSelected;
  private boolean switchHighlighted;
  
  /**
   * Constructs a new Exit with the given positions.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Exit's door x position
   * @param y this Exit's door y position
   * @param switchX this Exit's switch x position
   * @param switchX this Exit's switch y position
   */
  public Exit (Jned jned, int x, int y, int switchX, int switchY) {
    super(jned, 12, x, y);
    this.switchX = switchX;
    this.switchY = switchY;
    deltaSwitchX = 0;
    deltaSwitchY = 0;
    
    overswitch = false;
    overdoor = false;
    switchSelected = false;
    switchHighlighted = false;
    
    calculateRectangle(true);
    calculateRectangle(false);
    
    setImage(ImageBank.EXIT);
  }
  
  /**
   * Returns a copy of this Exit.
   * @return a new Exit with the same properties as this Exit
   */
  public Exit duplicate() {
    return new Exit(jned, getX(), getY(), switchX, switchY);
  }
  
  
  private void calculateRectangle(boolean isDoor) {
    if (isDoor) {
      doorRectangle = new Rectangle(super.getX() - 13, super.getY() - 12, 26, 24);
    } else {
      switchRectangle = new Rectangle(switchX - 6, switchY - 4, 13, 7);
    }
  }
  
  /**
   * Normally, returns x position of this Exit's door. However, if the switch is highlighted, this
   * method will return the x position of the switch. This is important for consistant interaction 
   * with the mouse.
   * @return x position of Exit component under the mouse, or the door if no part is highlighted.
   */
  public int getX() {
    if (overswitch) {
      return switchX;
    }
    return super.getX();
  }
  
  /**
   * Normally, returns y position of this Exit's door. However, if the switch is highlighted, this
   * method will return the y position of the switch. This is important for consistant interaction 
   * with the mouse.
   * @return y position of Exit component under the mouse, or the door if no part is highlighted.
   */
  public int getY() {
    if (overswitch) {
      return switchY;
    }
    return super.getY();
  }
  
  /**
   * Returns the x position of this Exit's door.
   * @return x position of this Exit's door.
   */
  public int getSuperX() {
    return super.getX();
  }
  
  /**
   * Returns the y position of this Exit's door.
   * @return y position of this Exit's door.
   */
  public int getSuperY() {
    return super.getY();
  }
  
  /**
   * Returns the x position of this Exit's switch.
   * @return x position of this Exit's switch.
   */
  public int getSwitchX() {
    return switchX;
  }
  
  /**
   * Returns the y position of this Exit's switch.
   * @return y position of this Exit's switch.
   */
  public int getSwitchY() {
    return switchY;
  }
  
  /**
   * Moves the component of this Exit under the mouse to the given position. If no component
   * is highlighted, this method moves the door.
   * @param x the new x position
   * @param y the new y position
   */
  public void moveTo(int x, int y) {
    if (overswitch) {
      switchX = x;
      switchY = y;
      calculateRectangle(false);
    } else {
      super.moveTo(x, y);
      calculateRectangle(true);
    }
  }
  
  /**
   * Sets the reference point for relative movement, essentially the position of another Item to
   * match movement with. Once set, calling moveRelative() with a new point will move the switch and
   * door of this Exit by the same amount as the difference between the reference point and the
   * supplied point.
   * @param x the reference x position
   * @param y the reference y position
   */
  public void setDelta(int x, int y) {
    super.setDelta(x, y);
    deltaSwitchX = x - switchX;
    deltaSwitchY = y - switchY;
  }
  
  /**
   * Moves any selected components of this Exit to a new position relative to its reference points.
   * The components will move by the same amount as the difference between the supplied point and
   * this Exit's relevant reference point.
   * <p>
   * Optionally, all components can be moved regardless of selection using the moveAll parameter.
   * @param x the relative x position
   * @param y the relative y position
   * @param moveAll true to move both components, selceted or not, false to only move selected
   * components.
   */
  public void moveRelative(int x, int y, boolean moveAll) {
    if (moveAll || super.isSelected()) {
      super.moveRelative(x, y);
      calculateRectangle(true);
    }
    if (moveAll || switchSelected) {
      switchX = x - deltaSwitchX;
      switchY = y - deltaSwitchY;
      calculateRectangle(false);
    }
  }
  
  /**
   * Moves any selected components of this Exit to a new position relative to its reference points.
   * The components will move by the same amount as the difference between the supplied point and
   * this Exit's relevant reference point.
   * @param x the relative x position
   * @param y the relative y position
   */
  public void moveRelative(int x, int y) {
    moveRelative(x, y, false);
  }
  
  /**
   * Returns a String representation of this Exit, in n level code format.
   * @return n level code String for this Exit
   */
  public String toString() {
    return super.toString() + "," + switchX + "," + switchY;
  }
  
  /**
   * Returns whether or not the given point intersects with any part of this Exit. Internal values
   * will record which part (door or switch) overlaps.
   * @param x x coordinate of point to check for overlap
   * @param y y coordinate of point to check for overlap
   * @return true if point overlaps this Exit, false if it does not
   */
  public boolean overlaps(int x, int y) {
    if (switchRectangle.contains(x, y)) {
      overswitch = true;
    } else {
      overswitch = false;
    }
    if (doorRectangle.contains(x, y)) {
      overdoor = true;
    } else {
      overdoor = false;
    }
    return overswitch || overdoor;
  }
  
  /**
   * Returns whether or not the given Rectangle intersects with any part of this Exit. Internal
   * values will record which part (door or switch) overlaps.
   * @param rectangle the Rectangle to check for overlap
   * @return true if rectangle overlaps this Exit, false if it does not
   */
  public boolean overlaps(Rectangle rectangle) {
    if (doorRectangle.intersects(rectangle)) {
      overdoor = true;
    } else {
      overdoor = false;
    }
    if (switchRectangle.intersects(rectangle)) {
      overswitch = true;
    } else {
      overswitch = false;
    }
    return overswitch || overdoor;
  }
  
  /**
   * Returns whether the component of this Exit that is currently under the mouse is selected.
   * @return true if the highlighted component of this Exit is currently selected, false if it is
   * not
   */
  public boolean isSelected() {
    if (overswitch) {
      return switchSelected;
    }
    return super.isSelected();
  }
  
  /**
   * Sets whether this Exit is selected. Will select only the component (switch or door) that is
   * under the mouse. Optionally, both components can be selected using the selectBoth variable.
   * @param select true to select component of Exit under the mouse, false to deselect all
   * components
   * @param selectBoth true to select both components of this Exit
   */
  public void setSelect(boolean select, boolean selectBoth) {
    if (select == false) {
      super.setSelect(false);
      switchSelected = false;
    } else {
      if (overswitch || selectBoth) {
        switchSelected = true;
      }
      if (overdoor || selectBoth) {
        super.setSelect(true);
      }
    }
  }
  
  /**
   * Sets whether this Exit is selected. Will select only the component (switch or door) that is
   * under the mouse.
   * @param select true to select component of Exit under the mouse, false to deselect all
   * components
   */
  public void setSelect(boolean select) {
    setSelect(select, false);
  }
  
  //Overrides Item.setHighlight: takes into account highlighting of switch vs. door
  public void setHighlight(boolean highlight) {
    if(highlight == false) {
      super.setHighlight(false);
      switchHighlighted = false;
    } else {
      if(overswitch) switchHighlighted = true;
      if(overdoor) super.setHighlight(true);
    }
  }
  
  public void paint(Graphics g) {
    // Door
    boolean[] layer = {true, isHighlighted(), super.isSelected()};
    for (int i = 0; i < 3; i++) {
      switch (i) {
        case 0:
          if(jned.drawImage(getImage(), super.getX(), super.getY(), g)) {
            layer[0] = false;
          } else {
            g.setColor(Colors.ITEM);
          }
          break;
        case 1:
          g.setColor(Colors.ITEM_HL_A);
          break;
        case 2:
          g.setColor(Colors.ITEM_SELECT_A);
          break;
        default:
      }
      if (layer[i]) {
        g.fillRect(doorRectangle.x, doorRectangle.y, doorRectangle.width, doorRectangle.height);
      }
    }
    // Switch
    layer[0] = true;
    layer[1] = switchHighlighted;
    layer[2] = switchSelected;
    for (int i = 0; i < 3; i++) {
      switch (i) {
        case 0:
          if (jned.drawImage(getImage()+1, switchX, switchY, g)) {
            layer[0] = false;
          } else {
            g.setColor(Colors.ITEM);
          }
          break;
        case 1:
          g.setColor(Colors.ITEM_HL_A);
          break;
        case 2:
          g.setColor(Colors.ITEM_SELECT_A);
          break;
        default:
      }
      if(layer[i]) {
        g.fillRect(switchRectangle.x, switchRectangle.y, switchRectangle.width,
            switchRectangle.height);
      }
    }
  }
  
  /**
   * Paints a line connecting the door to the switch of this Exit.
   * @param g Graphics context with which to paint trigger line
   */
  public void paintTrigger(Graphics g) {
    g.setColor(Colors.DOOR_TRIGGER);
    g.drawLine(super.getX(), super.getY(), switchX, switchY);
  }
  
  /**
   * Paints a translucent silhouette of an Exit door with the given position
   * @param x the x coordinate to draw door ghost at
   * @param y the y coordinate to draw door ghost at
   * @param g Graphics context to draw door ghost with
   */
  public static void paintDoorGhost(int x, int y, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    g.fillRect(x - 13, y - 12, 26, 24);
  }
  
  /**
   * Paints a translucent silhouette of an Exit switch with the given coordinates and door with
   * the given position, as well as a trigger line connecting them.
   * @param x the x position to draw door ghost at
   * @param y the y position to draw door ghost at
   * @param switchX the x coordinate to draw switch ghost at
   * @param switchY the y coordinate to draw switch ghost at
   * @param g Graphics context to draw ghosts with
   */
  public static void paintSwitchGhost(int x, int y, int switchX, int switchY, Graphics g) {
    Exit.paintDoorGhost(x, y, g);
    
    g.setColor(Colors.ITEM_GHOST);
    g.fillRect(switchX - 6, switchY - 4, 13, 7);
    
    g.setColor(Colors.DOOR_TRIGGER);
    g.drawLine(x, y, switchX, switchY);
  }
}