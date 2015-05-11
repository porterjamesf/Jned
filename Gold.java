import java.awt.Graphics;

/**
 * A Gold item in Jned.
 * @author James Porter
 */
public class Gold extends Item {

  /**
   * Constructs a new Gold with the given position.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Gold's x position
   * @param y this Gold's y position
   */
  public Gold (Jned jned, int x, int y) {
    super(jned, 10, x, y);
    setImage(ImageBank.GOLD);
  }
  
  /**
   * Returns a copy of this Gold.
   * @return a new Gold with the same properties as this Gold
   */
  public Gold duplicate() {
    return new Gold(jned, getX(), getY());
  }
  
  /**
   * Calculates the polygon representing the shape and position of this Gold.
   */
  public void calculateShape() {
    int[] xs = {getX() - 3, getX() + 4, getX() + 4, getX() - 3};
    int[] ys = {getY() + 4, getY() + 4, getY() - 3, getY() - 3};
    setShape(xs, ys);
  }
  
  /**
   * Paints a translucent silhouette a Gold at the given position.
   * @param x the x position to draw Gold ghost at
   * @param y the y position to draw Gold ghost at
   * @param g Graphics context to draw Gold ghost with
   */
  public static void paintGhost(int x, int y, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    int[] xs = {x - 3, x + 4, x + 4, x - 3};
    int[] ys = {y + 4, y + 4, y - 3, y - 3};
    g.fillPolygon(xs, ys, 4);
  }
}