import java.awt.Graphics;

/**
 * A Mine item in Jned.
 * @author James Porter
 */
public class Mine extends Item {
  
  /**
   * Constructs a new Mine with the given position.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Mine's x position
   * @param y this Mine's y position
   */
  public Mine (Jned jned, int x, int y) {
    super(jned, 2, x, y);
    setImage(ImageBank.MINE);
  }
  
  /**
   * Returns a copy of this Mine.
   * @return a new Mine with the same properties as this Mine
   */
  public Mine duplicate() {
    return new Mine(jned, getX(), getY());
  }
  
  /**
   * Calculates the polygon representing the shape and position of this Mine.
   */
  public void calculateShape() {
    int[] xs = {getX() - 4, getX() + 4, getX() + 4, getX() - 4};
    int[] ys = {getY() + 4, getY() + 4, getY() - 4, getY() - 4};
    setShape(xs, ys);
  }
  
  /**
   * Paints a translucent silhouette of a Mine at the given position.
   * @param x the x position to draw Mine ghost at
   * @param y the y position to draw Mine ghost at
   * @param g Graphics context to draw Mine ghost with
   */
  public static void paintGhost(int x, int y, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    int[] xs = {x - 4, x + 4, x + 4, x - 4};
    int[] ys = {y + 4, y + 4, y - 4, y - 4};
    g.fillPolygon(xs, ys, 4);
  }
}