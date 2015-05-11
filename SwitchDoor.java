import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * A superclass for doors that have switches, e.g. locked doors or trap doors.
 * @author James Porter
 */
public class SwitchDoor extends Door {
  private int row;
  private int column;
  private int deltaRow;
  private int deltaColumn;
  // Values: 0,1,2 = normal door, locked door, trap door
  private int doorType;
  
  private Rectangle doorRectangle;
  private Rectangle switchRectangle;
  
  private boolean overSwitch;
  private boolean overDoor;
  
  // Selection and highlighting for the switch are done using inherited Item methods
  private boolean doorSelected;
  private boolean doorHighlighted;
  
  
  /**
   * Constructs a new SwitchDoor with the given switch position, door cell, and direction.
   * @param jned a reference to the enclosing Jned instance
   * @param type the Jned item id of this SwitchDoor
   * @param x the x coordinate of the switch for this SwitchDoor
   * @param y the y coordinate of the switch for this SwitchDoor
   * @param direction the direction this SwitchDoor will initially face
   * @param row the row of the cell containing the door of this SwitchDoor
   * @param column the column of the cell containing the door of this SwitchDoor
   */
  public SwitchDoor (Jned jned, int type, int x, int y, int direction, int row, int column) {
    super(jned, type, x, y, direction);
    this.row = row;
    this.column = column;
    
    deltaRow = 0;
    deltaColumn = 0;
    doorType = type - 14;
    
    calculateRectangle(true);
    calculateRectangle(false);
    
    overSwitch = false;
    overDoor = false;
    doorHighlighted = false;
    doorSelected = false;
    
    jned.calculateDronePaths();
  }
  
  public int getFlags() {
    return MENU_FLAGS;
  }
  
  /**
   * Returns a copy of this SwitchDoor.
   * @return a new SwitchDoor with the same properties as this SwitchDoor
   */
  public SwitchDoor duplicate() {
    return new SwitchDoor(jned, getType(), getSuperX(), getSuperY(), getDirection(), row, column);
  }
  
  /**
   * Calculates the rectangle representing the shape and position of this SwitchDoor's door or
   * switch.
   * @param isDoor true to calculate rectangle of the door, false to calculate the rectangle of the
   * switch
   */
  public void calculateRectangle(boolean isDoor) {
    if (isDoor) {
      doorRectangle = new Rectangle(row * 24 + (getDirection() == 0 ? 22 : 0), column * 24 +
          (getDirection() == 1 ? 22 : 0), (getDirection() % 2 == 0 ? 2 : 24), (getDirection() % 2
          == 0 ? 24 : 2));
    } else {
      switchRectangle = new Rectangle(super.getX() - 2, super.getY() - 2, 4, 4);
    }
  }
  
  /**
   * Sets the rectangle representing the shape and position of this SwitchDoor's door or switch.
   * @param rectangle the Rectangle to set
   * @param isDoor true to calculate rectangle of the door, false to calculate the rectangle of the
   * switch
   */
  public void setRectangle(Rectangle rectangle, boolean isDoor) {
    if (isDoor) {
      doorRectangle = rectangle;
    } else {
      switchRectangle = rectangle;
    }
  }
  
  /**
   * Normally, returns x position of this SwitchDoor's switch. However, if the door is highlighted,
   * this method will return the x position of the door. This is important for consistant
   * interaction with the mouse.
   * @return x position of SwitchDoor component under the mouse, or the switch if no part is
   * highlighted.
   */
  public int getX() {
    if (overDoor) {
      switch (getDirection()) {
        default:
          // fall through
        case 0:
          return 24 * (row + 1);
        case 2:
          return 24 * row;
        case 1:
          // fall through
        case 3: return 24 * row + 12;
      }
    }
    return super.getX();
  }
  
  /**
   * Normally, returns y position of this SwitchDoor's switch. However, if the door is highlighted,
   * this method will return the y position of the door. This is important for consistant
   * interaction with the mouse.
   * @return y position of SwitchDoor component under the mouse, or the switch if no part is
   * highlighted.
   */
  public int getY() {
    if (overDoor) {
      switch (getDirection()) {
        default:
          // fall through
        case 0:
          // fall through
        case 2:
          return 24 * column + 12;
        case 1:
          return 24 * (column + 1);
        case 3:
          return 24 * column;
      }
    }
    return super.getY();
  }
  
  /**
   * Returns the x position of this SwitchDoor's switch.
   * @return x position of this SwitchDoor's switch.
   */
  public int getSuperX() {
    return super.getX();
  }
  
  /**
   * Returns the y position of this SwitchDoor's switch.
   * @return y position of this SwitchDoor's switch.
   */
  public int getSuperY() {
    return super.getY();
  }
  
  /**
   * Sets this SwitchDoor's direction.
   * @param direction the new direction. Use 0 for right, 1 for down, 2 for left, and 3 for up.
   */
  public void setDirection(int direction) {
    super.setDirection(direction);
    calculateRectangle(true);
  }
  
  /**
   * Returns the row of this SwitchDoor's door.
   * @return row of cell containing door for this SwitchDoor
   */
  public int getRow() {
    return row;
  }
  
  /**
   * Returns the column of this SwitchDoor's door.
   * @return column of cell containing door for this SwitchDoor
   */
  public int getColumn() {
    return column;
  }
  
  /**
   * Moves the component of this SwitchDoor under the mouse to the given position. If no component
   * is highlighted, this method moves the switch.
   * @param x the new x position
   * @param y the new y position
   */
  public void moveTo(int x, int y) {
    if (overDoor) {
      row = x / 24;
      column = y / 24;
      calculateRectangle(true);
      jned.calculateDronePaths();
    } else {
      super.moveTo(x, y);
      calculateRectangle(false);
    }
  }
  
  /**
   * Sets the reference point for relative movement, essentially the position of another Item to
   * match movement with. Once set, calling moveRelative() with a new point will move the switch of
   * this SwitchDoor by the same amount as the difference between the reference point and the
   * supplied point. The door will also move by the same amount, but is always snapped into the
   * closest cell.
   * @param x the reference x position
   * @param y the reference y position
   */
  public void setDelta(int x, int y) {
    super.setDelta(x, y);
    deltaRow = x - (24 * row + 12);
    deltaColumn = y - (24 * column + 12);
  }
  
  /**
   * Moves any selected components of this SwitchDoor to a new position relative to its reference
   * points. The components will move by the same amount as the difference between the supplied
   * point and this SwitchDoors's relevant reference point. Door positions will always be snapped
   * to the nearest cell.
   * <p>
   * Optionally, all components can be moved regardless of selection using the moveAll parameter.
   * @param x the relative x position
   * @param y the relative y position
   * @param moveAll true to move both components, selceted or not, false to only move selected
   * components.
   */
  public void moveRelative(int xpos, int ypos, boolean moveAll) {
    if (moveAll || super.isSelected()) {
      super.moveRelative(xpos, ypos);
      calculateRectangle(false);
    }
    if (moveAll || doorSelected) {
      row = (xpos - deltaRow) / 24;
      column = (ypos - deltaColumn) / 24;
      calculateRectangle(true);
      jned.calculateDronePaths();
    }
  }
  
  /**
   * Moves any selected components of this SwitchDoor to a new position relative to its reference
   * points. The components will move by the same amount as the difference between the supplied
   * point and this SwitchDoors's relavent reference point. Door positions will always be snapped
   * to the nearest cell.
   * @param x the relative x position
   * @param y the relative y position
   */
  public void moveRelative(int x, int y) {
    moveRelative(x, y, false);
  }
  
  /**
   * Returns a String representation of this SwitchDoor, in n level code format.
   * @return n level code String for this SwitchDoor
   */
  public String toString() {
    String result = super.toString();
    return result.substring(0, result.length() - 1) + (getDirection() % 2 == 1 ? 1 : 0) + "," +
        (doorType == 2 ? 1 : 0) + "," + row + "," + column + "," + (doorType == 1 ? 1 : 0) + "," +
        (getDirection() == 2 ? - 1 : 0) + "," + (getDirection() == 3 ? - 1 : 0);
  }
  
  /**
   * Returns whether or not the given point intersects with any part of this SwitchDoor. Internal
   * values will record which part (door or switch) overlaps.
   * @param x x coordinate of point to check for overlap
   * @param y y coordinate of point to check for overlap
   * @return true if point overlaps this SwitchDoor, false if it does not
   */
  public boolean overlaps(int x, int y) {
    overSwitch = switchRectangle.contains(x, y);
    overDoor = overlapsDoor(x, y);
    return overSwitch || overDoor;
  }
  
  /**
   * Returns whether or not the given Rectangle intersects with any part of this SwitchDoor.
   * Internal values will record which part (door or switch) overlaps.
   * @param rectangle the Rectangle to check for overlap
   * @return true if rectangle overlaps this SwitchDoor, false if it does not
   */
  public boolean overlaps(Rectangle rectangle) {
    overSwitch = switchRectangle.intersects(rectangle);
    overDoor = overlapsDoor(rectangle);
    return overSwitch || overDoor;
  }
  
  /**
   * Returns whether or not the given point intersects with the door of this SwitchDoor.
   * @param x x coordinate of point to check for overlap
   * @param y y coordinate of point to check for overlap
   * @return true if point overlaps this SwitchDoor's door, false if it does not
   */
  public boolean overlapsDoor(int x, int y) {
    return doorRectangle.contains(x, y);
  }
  
  /**
   * Returns whether or not the given Rectangle intersects with the door of this SwitchDoor.
   * @param rectangle the Rectangle to check for overlap
   * @return true if rectangle overlaps this SwitchDoor's door, false if it does not
   */
  public boolean overlapsDoor(Rectangle rectangle) {
    return doorRectangle.intersects(rectangle);
  }
  
  /**
   * Returns whether the component of this SwitchDoor that is currently under the mouse is selected.
   * @return true if the highlighted component of this SwitchDoor is currently selected, false if it
   * is not
   */
  public boolean isSelected() {
    if (overDoor) {
      return doorSelected;
    }
    return super.isSelected();
  }
  
  /**
   * Sets whether this SwitchDoor is selected. Will select only the component (switch or door) that
   * is under the mouse. Optionally, both components can be selected using the selectBoth variable.
   * @param select true to select component of SwitchDoor under the mouse, false to deselect all
   * components
   * @param selectBoth true to select both components of this SwitchDoor
   */
  public void setSelect(boolean select, boolean selectBoth) {
    if (select == false) {
      super.setSelect(false);
      doorSelected = false;
    } else {
      if (overDoor || selectBoth) {
        doorSelected = true;
      }
      if (overSwitch || selectBoth) {
        super.setSelect(true);
      }
    }
    jned.calculateDronePaths();
  }
  
  /**
   * Sets whether this SwitchDoor is selected. Will select only the component (switch or door) that
   * is under the mouse.
   * @param select true to select component of SwitchDoor under the mouse, false to deselect all
   * components
   */
  public void setSelect(boolean select) {
    setSelect(select, false);
  }
  
  /**
   * Returns whether any part of this SwitchDoor is currently selected.
   * @return true if either the door or switch of this SwitchDoor is selected, false if not
   */
  public boolean isAnySelected() {
    return doorSelected || super.isSelected();
  }
  
  /**
   * Sets whether this SwitchDoor is highlighted. Will highlight only the component (switch or door)
   * that is under the mouse.
   * @param highlight true to highlight component of SwitchDoor under the mouse, false to
   * unhighlight all components
   */
  public void setHighlight(boolean highlight) {
    if (highlight == false) {
      super.setHighlight(false);
      doorHighlighted = false;
    } else {
      if (overDoor) {
        doorHighlighted = true;
      }
      if (overSwitch) {
        super.setHighlight(true);
      }
    }
  }
  
  public void paint(Graphics g) {
    // Door
    boolean[] layer = {true, doorHighlighted, doorSelected};
    for (int i = 0; i < 3; i++) {
      switch (i) {
        case 0:
          if (jned.drawImage(getImage(true), 24 * row, 24 * column, g)) {
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
        paintDoor(g);
      }
    }
    // Switch
    layer[0] = true;
    layer[1] = isHighlighted();
    layer[2] = super.isSelected();
    for (int i = 0; i < 3; i++) {
      switch (i) {
        case 0:
          if (jned.drawImage(getImage() + 4, getSuperX(), getSuperY(), g)) {
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
        g.fillRect(switchRectangle.x, switchRectangle.y, switchRectangle.width,
            switchRectangle.height);
      }
    }
  }
  
  /**
   * Paints the door component of this SwitchDoor.
   * @param g Graphics context with which to paint door
   */
  public void paintDoor(Graphics g) {
    g.fillRect(doorRectangle.x, doorRectangle.y, doorRectangle.width, doorRectangle.height);
  }
  
  /**
   * Paints a line connecting the door to the switch of this SwitchDoor.
   * @param g Graphics context with which to paint trigger line
   */
  public void paintTrigger(Graphics g) {
    g.setColor(Colors.DOOR_TRIGGER);
    g.drawLine(row * 24 + (24 * Math.abs(2 - getDirection())) / 2, column * 24 + (2 - Math.abs(1 -
        getDirection())) * 12, super.getX(), super.getY());
  }
}