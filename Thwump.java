import java.awt.Graphics;

/**
 * A Thwump item in Jned.
 * @author James Porter
 */
public class Thwump extends DirectionalItem {

  /**
   * Constructs a new Thwump with the given position and direction.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Thwump's x position
   * @param y this Thwump's y position
   * @param direction the direction this Thwump will initially face
   */
  public Thwump (Jned jned, int x, int y, int direction) {
    super(jned, 4, x, y, direction);
    setImage(ImageBank.THWUMP);
  }
  
  /**
   * Returns a copy of this Thwump.
   * @return a new Thwump with the same properties as this Thwump
   */
  public Thwump duplicate() {
    return new Thwump(jned, getX(), getY(), getDirection());
  }
  
  /**
   * Calculates the polygon representing the shape and position of this Oneway.
   */
  public void calculateShape() {
    int[] xs = {getX() - 10, getX() + 10, getX() + 10, getX() - 10};
    int[] ys = {getY() + 10, getY() + 10, getY() - 10, getY() - 10};
    setShape(xs, ys);
  }
  
  /**
   * Paints a translucent silhouette of this Thwump at the given position.
   * @param x the x position to draw Thwump ghost at
   * @param y the y position to draw Thwump ghost at
   * @param g Graphics context to draw Thwump ghost with
   */
  public static void paintGhost(int x, int y, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    int[] xs = {x - 10, x + 10, x + 10, x - 10};
    int[] ys = {y + 10, y + 10, y - 10, y - 10};
    g.fillPolygon(xs, ys, 4);
  }
}