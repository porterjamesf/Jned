import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * A Trap Door item in Jned.
 * @author James Porter
 */
public class Trapdoor extends SwitchDoor {
  private Rectangle knob1;
  private Rectangle knob2;
  
  /**
   * Constructs a Trapdoor with the given switch position, direction, and door cell.
   * @param jned a reference to the enclosing Jned instance
   * @param x the x coordinate of the switch for this Trapdoor
   * @param y the y coordinate of the switch for this Trapdoor
   * @param direction the direction this Trapdoor will initially face
   * @param row the row of the cell containing the door of this Trapdoor
   * @param column the column of the cell containing the door of this Trapdoor
   */
  public Trapdoor (Jned jned, int x, int y, int direction, int row, int column) {
    super(jned, 16, x, y, direction, row, column);
    calculateRectangle(true);
    setImage(ImageBank.TDOOR);
  }
  
  /**
   * Returns a copy of this Trapdoor.
   * @return a new Trapdoor with the same properties as this Trapdoor
   */
  public Trapdoor duplicate() {
    return new Trapdoor(jned, getSuperX(), getSuperY(), getDirection(), getRow(), getColumn());
  }
  
  /**
   * Calculates the rectangle representing the shape and position of this Trapdoor's door or
   * switch.
   * @param isDoor true to calculate rectangle of the door, false to calculate the rectangle of the
   * switch
   */
  public void calculateRectangle(boolean isDoor) {
    if (isDoor) {
      int x = 24 * getRow();
      int y = 24 * getColumn();
      switch (getDirection()) {
        case 0:
          setRectangle(new Rectangle(x + 19, y, 6, 25), true);
          knob1 = new Rectangle(x + 18, y + 4, 8, 5);
          knob2 = new Rectangle(x + 18, y + 15, 8, 5);
          break;
        case 1:
          setRectangle(new Rectangle(x, y + 19, 25, 6), true);
          knob1 = new Rectangle(x + 4, y + 18, 5, 8);
          knob2 = new Rectangle(x + 15, y + 18, 5, 8);
          break;
        case 2:
          setRectangle(new Rectangle(x, y, 5, 25), true);
          knob1 = new Rectangle(x - 1, y + 4, 7, 5);
          knob2 = new Rectangle(x - 1, y + 15, 7, 5);
          break;
        case 3:
          setRectangle(new Rectangle(x, y, 25, 5), true);
          knob1 = new Rectangle(x + 4, y - 1, 5, 7);
          knob2 = new Rectangle(x + 15, y - 1, 5, 7);
          break;
        default:
      }
    } else {
      setRectangle(new Rectangle(super.getSuperX() - 3, super.getSuperY() - 3, 6, 6), false);
    }
  }
  
  /**
   * Returns whether or not the given point intersects with the door of this Trapdoor.
   * @param x x coordinate of point to check for overlap
   * @param y y coordinate of point to check for overlap
   * @return true if point overlaps this Trapdoor's door, false if it does not
   */
  public boolean overlapsDoor(int x, int y) {
    return knob1.contains(x, y) || knob2.contains(x, y) || super.overlapsDoor(x, y);
  }
  
  /**
   * Returns whether or not the given Rectangle intersects with the door of this Trapdoor.
   * @param rectangle the Rectangle to check for overlap
   * @return true if rectangle overlaps this Trapdoor's door, false if it does not
   */
  public boolean overlapsDoor(Rectangle rectangle) {
    return knob1.intersects(rectangle) || knob2.intersects(rectangle) ||
        super.overlapsDoor(rectangle);
  }
  
  /**
   * Paints the door component of this Trapdoor.
   * @param g Graphics context with which to paint door
   */
  public void paintDoor(Graphics g) {
    super.paintDoor(g);
    
    if (getDirection() % 2 == 0) {
      g.drawLine(knob1.x, knob1.y, knob1.x, knob1.y + knob1.height - 1);
      g.drawLine(knob1.x + knob1.width - 1, knob1.y, knob1.x + knob1.width - 1, knob1.y +
          knob1.height - 1);
      g.drawLine(knob2.x, knob2.y, knob2.x, knob2.y + knob2.height - 1);
      g.drawLine(knob2.x + knob2.width - 1, knob2.y, knob2.x + knob2.width - 1, knob2.y +
          knob2.height - 1);
    } else {
      g.drawLine(knob1.x, knob1.y, knob1.x + knob1.width - 1, knob1.y);
      g.drawLine(knob1.x, knob1.y + knob1.height - 1, knob1.x + knob1.width - 1, knob1.y +
          knob1.height - 1);
      g.drawLine(knob2.x, knob2.y, knob2.x + knob2.width - 1, knob2.y);
      g.drawLine(knob2.x, knob2.y + knob2.height - 1, knob2.x + knob2.width - 1, knob2.y +
          knob2.height - 1);
    }
  }
  
  /**
   * Paints a translucent silhouette of a Trapdoor door with the given direction and cell
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
        g.fillRect(x + 19, y, 6, 25);
        g.drawLine(x + 18, y + 4, x + 18, y + 8);
        g.drawLine(x + 18, y + 15, x + 18, y + 19);
        g.drawLine(x + 25, y + 4, x + 25, y + 8);
        g.drawLine(x + 25, y + 15, x + 25, y + 19);
        break;
      case 1:
        g.fillRect(x, y + 19, 25, 6);        
        g.drawLine(x + 4, y + 18, x + 8, y + 18);
        g.drawLine(x + 15, y + 18, x + 19, y + 18);
        g.drawLine(x + 4, y + 25, x + 8, y + 25);
        g.drawLine(x + 15, y + 25, x + 19, y + 25);
        break;
      case 2:
        g.fillRect(x, y, 5, 25);        
        g.drawLine(x - 1, y + 4, x - 1, y + 8);
        g.drawLine(x - 1, y + 15, x - 1, y + 19);
        g.drawLine(x + 5, y + 4, x + 5, y + 8);
        g.drawLine(x + 5, y + 15, x + 5, y + 19);
        break;
      case 3:
        g.fillRect(x, y, 25, 5);
        g.drawLine(x + 4, y - 1, x + 8, y - 1);
        g.drawLine(x + 15, y - 1, x + 19, y - 1);
        g.drawLine(x + 4, y + 5, x + 8, y + 5);
        g.drawLine(x + 15, y + 5, x + 19, y + 5);
        break;
      default:
    }
  }
  
  /**
   * Paints a translucent silhouette of a Trapdoor switch with the given position and door with
   * the given direction and cell coordinates, as well as a trigger line connecting them.
   * @param x the x position to draw switch ghost at
   * @param y the y position to draw switch ghost at
   * @param direction the direction to draw door ghost facing
   * @param row the row coordinate to draw door ghost at
   * @param column the column coordinate to draw door ghost at
   * @param g Graphics context to draw door ghost with
   */
  public static void paintSwitchGhost(int x, int y, int direction, int row, int column, Graphics
      g) {
    Trapdoor.paintDoorGhost(direction, row, column, g);
    
    g.setColor(Colors.ITEM_GHOST);
    g.fillRect(x - 3, y - 3, 6, 6);
    
    g.setColor(Colors.DOOR_TRIGGER);
    g.drawLine(row * 24 + (24 * Math.abs(2 - direction)) / 2, column * 24 + (2 - Math.abs(1 -
        direction)) * 12, x, y);
  }
}