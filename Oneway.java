import java.awt.Graphics;

/**
 * A Oneway platform item in Jned.
 * @author James Porter
 */
public class Oneway extends DirectionalItem {

  /**
   * Constructs a new Oneway with the given position and direction.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Oneway's x position
   * @param y this Oneway's y position
   * @param direction the direction this Oneway will initially face
   */
  public Oneway (Jned jned, int x, int y, int direction) {
    super(jned, 13, x, y, direction);
    setImage(ImageBank.ONEWAY);
  }
  
  /**
   * Returns a copy of this Oneway.
   * @return a new Oneway with the same properties as this Oneway
   */
  public Oneway duplicate() {
    return new Oneway(jned, getX(), getY(), getDirection());
  }
  
  /**
   * Calculates the polygon representing the shape and position of this Oneway.
   */
  public void calculateShape() {
    int d = getDirection();
    int x = getX();
    int y = getY();
    int[] xs = {x + (d > 1 ? -7 : 7), x + (d > 1 ? -12 : 12), x + (d % 3 == 0 ? 12 : -12), x + (d %
        3 == 0 ? 7 : -7)};
    int[] ys = {y + (d % 3 == 0 ? -7 : 7), y + (d % 3 == 0 ? -12 : 12), y + (d > 1 ? -12 : 12), y +
        (d > 1 ? -7 : 7)};
    setShape(xs, ys);
  }
  
  /**
   * Paints a translucent silhouette of this type of Oneway at the given position.
   * @param x the x position to draw Oneway ghost at
   * @param y the y position to draw Oneway ghost at
   * @param direction the direction to draw Oneway ghost facing
   * @param g Graphics context to draw Oneway ghost with
   */
  public static void paintGhost(int x, int y, int direction, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    int[] xs = {x + (direction > 1 ? -7 : 7), x + (direction > 1 ? -12 : 12), x + (direction % 3 ==
        0 ? 12 : -12), x + (direction % 3 == 0 ? 7 : -7)};
    int[] ys = {y + (direction % 3 == 0 ? -7 : 7), y + (direction % 3 == 0 ? -12 : 12), y +
        (direction > 1 ? -12 : 12), y + (direction > 1 ? -7 : 7)};
    g.fillPolygon(xs, ys, 4);
  }
}