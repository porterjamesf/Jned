import java.awt.Graphics;

/**
 * A Floor guard item in Jned.
 * @author James Porter
 */
public class Floorguard extends Item {

  /**
   * The set of right-click menu flags appropriate for an Item with only horizontal nudging.
   */
  public final int  MENU_FLAGS = 0b000010;
  
  private int deltaX;
  private int deltaY;
  
  /**
   * Constructs a new Floorguard with the given position.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Floorguard's x position
   * @param y this Floorguard's y position
   */
  public Floorguard (Jned jned, int x, int y) {
    super(jned, 3, x, y);
    
    deltaX = 0;
    deltaY = 0;
    
    calculateShape();
    setImage(ImageBank.FLOOR);
  }
  
  public int getFlags() {
    return MENU_FLAGS;
  }
  
  /**
   * Returns a copy of this Floorguard.
   * @return a new Floorguard with the same properties as this Floorguard
   */
  public Floorguard duplicate() {
    return new Floorguard(jned, getX(), getY());
  }
  
  /**
   * Calculates the polygon representing the shape and position of this Floorguard.
   */
  public void calculateShape() {
    int[] xs = new int[6];
    int[] ys = new int[6];
    xs[0] = xs[5] = getX() + 6;
    xs[1] = xs[2] = getX() - 6;
    xs[3] = getX() - 2;
    xs[4] = getX() + 2;
    ys[0] = ys[1] = getY() + 6;
    ys[2] = ys[5] = getY() - 2;
    ys[3] = ys[4] = getY() - 6;
    setShape(xs, ys);
  }
  
  /**
   * Returns a String representation of this Floorguard, in n level code format.
   * @return n level code String for this Floorguard
   */
  public String toString() {
    return super.toString() + ",1";
  }
  
  /**
   * Moves this Floorguard to a new position, adjusting y coordinate to snap to the nearest row.
   * @param x the new x position
   * @param y the new y position
   */
  public void moveTo(int x, int y) {
    super.moveTo(x, 24 * (y / 24) + 18);
  }
  
  /**
   * Sets the reference point for relative movement, essentially the position of another Item to
   * match movement with. Once set, calling moveRelative() with a new point will move this
   * Floorguard by the same amount as the difference between the reference point and the supplied
   * point.
   * @param x the reference x position
   * @param y the reference y position
   */
  public void setDelta(int x, int y) {
    deltaX = x - getX();
    deltaY = y - getY();
  }
  
  /**
   * Moves this Floorguard to a new position relative to its reference point. This Floorguard will
   * move by the same amount as the difference between the supplied point and this Floorguard's
   * reference point, with the y coordinate adjusted to snap to the nearest row.
   * @param x the relative x position
   * @param y the relative y position
   */
  public void moveRelative(int x, int y) {
    super.moveTo(x - deltaX, 24 * ((y - 6 - deltaY) / 24) + 18);
  }
  
  /**
   * Paints a translucent silhouette of this Floorguard at the given position.
   * @param x the x position to draw Floorguard ghost at
   * @param y the y position to draw Floorguard ghost at
   * @param g Graphics context to draw Floorguard ghost with
   */
  public static void paintGhost(int x, int y, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    int[] xs = {x + 6, x - 6, x - 6, x - 2, x + 2, x + 6};
    int[] ys = {y + 6, y + 6, y - 2, y -  6, y - 6, y - 2};
    g.fillPolygon(xs, ys, 6);
  }  
}