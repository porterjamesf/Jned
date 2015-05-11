import java.awt.Graphics;

/**
 * A Bounce Block item in Jned.
 * @author James Porter
 */
public class Bounceblock extends Item {

  /**
   * Constructs a new Bounceblock with the given position.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Bounceblock's x position
   * @param y this Bounceblock's y position
   */
  public Bounceblock (Jned jned, int x, int y) {
    super(jned, 11, x, y);
    setImage(ImageBank.BOUNCE);
  }
  
  /**
   * Returns a copy of this Bounceblock.
   * @return a new Bounceblock with the same properties as this Bounceblock
   */
  public Bounceblock duplicate() {
    return new Bounceblock(jned, getX(), getY());
  }
  
  
  public void calculateShape() {
    int[] xs = {getX() - 10, getX() + 10, getX() + 10, getX() - 10};
    int[] ys = {getY() + 10, getY() + 10, getY() - 10, getY() - 10};
    setShape(xs, ys);
  }
  
  public static void paintGhost(int x, int y, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    int[] xs = {x - 10, x + 10, x + 10, x - 10};
    int[] ys = {y + 10, y + 10, y - 10, y - 10};
    g.fillPolygon(xs, ys, 4);
  }
}