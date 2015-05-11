import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * A Locked Door item in Jned.
 * @author James Porter
 */
public class Lockeddoor extends SwitchDoor {
  private Rectangle knob;
  
  /**
   * Constructs a Lockeddoor with the given switch position, direction, and door cell.
   * @param jned a reference to the enclosing Jned instance
   * @param x the x coordinate of the switch for this Lockeddoor
   * @param y the y coordinate of the switch for this Lockeddoor
   * @param direction the direction this Lockeddoor will initially face
   * @param row the row of the cell containing the door of this Lockeddoor
   * @param column the column of the cell containing the door of this Lockeddoor
   */
  public Lockeddoor (Jned jned, int x, int y, int direction, int row, int column) {
    super(jned, 15, x, y, direction, row, column);
    calculateRectangle(true);
    setImage(ImageBank.LDOOR);
  }
  
  /**
   * Returns a copy of this Lockeddoor.
   * @return a new Lockeddoor with the same properties as this Lockeddoor
   */
  public Lockeddoor duplicate() {
    return new Lockeddoor(jned, getSuperX(), getSuperY(), getDirection(), getRow(), getColumn());
  }
  
  /**
   * Calculates the rectangle representing the shape and position of this Lockeddoor's door or
   * switch.
   * @param isDoor true to calculate rectangle of the door, false to calculate the rectangle of the
   * switch
   */
  public void calculateRectangle(boolean isDoor) {
    if (isDoor) {
      int x = 24 * getRow(), y = 24 * getColumn();
      switch (getDirection()) {
        case 0:
          setRectangle(new Rectangle(x + 22, y, 3, 24), true);
          knob = new Rectangle(x + 21, y + 6, 5, 12);
          break;
        case 1:
          setRectangle(new Rectangle(x, y + 22, 24, 3), true);
          knob = new Rectangle(x + 6, y + 21, 12, 5);
          break;
        case 2:
          setRectangle(new Rectangle(x, y, 2, 24), true);
          knob = new Rectangle(x - 1, y + 6, 5, 12);
          break;
        case 3:
          setRectangle(new Rectangle(x, y, 24, 2), true);
          knob = new Rectangle(x + 6, y - 1, 12, 5);
          break;
        default:
      }
    } else {
      setRectangle(new Rectangle(super.getSuperX() - 4, super.getSuperY() - 4, 8, 8), false);
    }
  }
  
  /**
   * Returns whether or not the given point intersects with the door of this Lockeddoor.
   * @param x x coordinate of point to check for overlap
   * @param y y coordinate of point to check for overlap
   * @return true if point overlaps this Lockeddoor's door, false if it does not
   */
  public boolean overlapsDoor(int x, int y) {
    return knob.contains(x, y) || super.overlapsDoor(x, y);
  }
  
  /**
   * Returns whether or not the given Rectangle intersects with the door of this Lockeddoor.
   * @param rectangle the Rectangle to check for overlap
   * @return true if rectangle overlaps this Lockeddoor's door, false if it does not
   */
  public boolean overlapsDoor(Rectangle rectangle) {
    return knob.intersects(rectangle) || super.overlapsDoor(rectangle);
  }
  
  /**
   * Paints the door component of this Lockeddoor.
   * @param g Graphics context with which to paint door
   */
  public void paintDoor(Graphics g) {
    super.paintDoor(g);
    
    if (getDirection() % 2 == 0) {
      g.drawLine(knob.x, knob.y, knob.x, knob.y + knob.height - 1);
      g.drawLine(knob.x + knob.width - 1, knob.y, knob.x + knob.width - 1, knob.y + knob.height -
          1);
      if (getDirection() > 1) {
        g.drawLine(knob.x + knob.width - 2, knob.y, knob.x + knob.width - 2, knob.y + knob.height -
            1);
      }
    } else {
      g.drawLine(knob.x, knob.y, knob.x + knob.width - 1, knob.y);
      g.drawLine(knob.x, knob.y + knob.height - 1, knob.x + knob.width - 1, knob.y + knob.height -
          1);
      if (getDirection() > 1) {
        g.drawLine(knob.x, knob.y + knob.height - 2, knob.x + knob.width - 1, knob.y + knob.height -
            2);
      }
    }
  }
  
  /**
   * Paints a translucent silhouette of a Lockeddoor door with the given direction and cell
   * coordinates.
   * @param direction the direction to draw door ghost facing
   * @param row the row coordinate to draw door ghost at
   * @param column the column coordinate to draw door ghost at
   * @param g Graphics context to draw door ghost with
   */
  public static void paintDoorGhost(int direction, int row, int column, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    int x = 24 * row;
    int y = 24 * column;
    switch (direction) {
      case 0:
        g.fillRect(x + 22, y, 3, 24);
        g.drawLine(x + 21, y + 6, x + 21, y + 18);
        g.drawLine(x + 25, y + 6, x + 25, y + 18);
        break;
      case 1:
        g.fillRect(x, y + 22, 24, 3);
        g.drawLine(x + 6, y + 21, x + 18, y + 21);
        g.drawLine(x + 6, y + 25, x + 18, y + 25);
        break;
      case 2:
        g.fillRect(x, y, 2, 24);
        g.drawLine(x - 1, y + 6, x - 1, y + 17);
        g.fillRect(x + 2, y + 6, 2, 12);
        break;
      case 3:
        g.fillRect(x, y, 24, 2);
        g.drawLine(x + 6, y - 1, x + 17, y - 1);
        g.fillRect(x + 6, y + 2, 12, 2);
        break;
      default:
    }
  }
  
  /**
   * Paints a translucent silhouette of a Lockeddoor switch with the given position and door with
   * the given direction and cell coordinates, as well as a trigger line connecting them.
   * @param x the x position to draw switch ghost at
   * @param y the y position to draw switch ghost at
   * @param direction the direction to draw door ghost facing
   * @param row the row coordinate to draw door ghost at
   * @param column the column coordinate to draw door ghost at
   * @param g Graphics context to draw ghosts with
   */
  public static void paintSwitchGhost(int x, int y, int direction, int row, int column, Graphics
      g) {
    Lockeddoor.paintDoorGhost(direction, row, column, g);
    
    g.setColor(Colors.ITEM_GHOST);
    g.fillRect(x - 4,y - 4, 8, 8);
    
    g.setColor(Colors.DOOR_TRIGGER);
    g.drawLine(row * 24 + (24 * Math.abs(2 - direction)) / 2, column * 24 + (2 - Math.abs(1 -
        direction)) * 12, x, y);
  }
}