import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * A superclass for turret type Items in Jned.
 * @author James Porter
 */
public class Turret extends Item {
  /**
   * The pixel radius of all turrent type Items.
   */
  public static final int RADIUS = 6;
  
  /**
   * Constructs a new Turret at the given position.
   * @param jned a reference to the enclosing Jned instance
   * @param type the Jned item id of this Turret
   * @param x this Turret's x position
   * @param y this Turret's y position
   */
  public Turret (Jned jned, int type, int x, int y) {
    super(jned, type, x, y);
  }
  
  /**
   * Returns a copy of this Turret.
   * @return a new Turret with the same properties as this Turret
   */
  public Turret duplicate() {
    return new Turret(jned, getType(), getX(), getY());
  }

  /**
   * Returns whether or not the given point intersects with this Turret.
   * @param x x coordinate of point to check for overlap
   * @param y y coordinate of point to check for overlap
   * @return true if point overlaps this Turret, false if it does not
   */
  public boolean overlaps(int x, int y) {
    return Math.hypot(x - getX(), y - getY()) < Turret.RADIUS;
  }
  
  /**
   * Returns whether or not the given Rectangle intersects with this Turret.
   * @param rectangle the Rectangle to check for overlap
   * @return true if rectangle overlaps this Turret, false if it does not
   */
  public boolean overlaps(Rectangle rectangle) {
    int nearX = rectangle.x + rectangle.width;
    int nearY = rectangle.y + rectangle.height;
    if (getX() <= rectangle.x) {
      nearX = rectangle.x;
    } else {
      if(getX() <= nearX) {
        nearX = getX();
      }
    }
    if (getY() <= rectangle.y) {
      nearY = rectangle.y;
    } else {
      if (getY() <= nearY) {
        nearY = getY();
      }
    }
    return Math.hypot(nearX - getX(), nearY - getY()) < Turret.RADIUS;
  }
  
  /**
   * Paints this Turret, including highlighting and selection shading.
   * @param g Graphics context to paint this Turret with
   */
  public void paint(Graphics g) {
    boolean[] layer = {true, isHighlighted(), isSelected()};
    for (int i = 0; i < 3; i++) {
      switch (i) {
        case 0:
          if (jned.drawImage(getImage(), getX(), getY(), g)) {
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
        g.fillOval(getX() - Turret.RADIUS, getY() - Turret.RADIUS, 2 * Turret.RADIUS, 2 *
            Turret.RADIUS);
      }
    }
  }
  
  /**
   * Paints a translucent silhouette of this Turret at the given position.
   * @param x the x position to draw Turret ghost at
   * @param y the y position to draw Turret ghost at
   * @param g Graphics context to draw Turret ghost with
   */
  public static void paintGhost(int x, int y, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    g.fillOval(x - Turret.RADIUS, y - Turret.RADIUS, 2 * Turret.RADIUS, 2 * Turret.RADIUS);
  }
}