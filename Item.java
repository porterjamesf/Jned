import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;

/**
 * A superclass for all items in Jned.
 * @author James Porter
 */
public class Item {
  /**
   * n level data values of item types. The indices of this array correspond to Jned item id's. The
   * values are the n level data code numbers for each item type.
   */
  public static int[] typeCodes = {3, 10, 12, 4, 8, 6, 6, 6, 6, 5, 0, 1, 11, 7, 9, 9, 9, 2};
  
  /**
   * The set of right-click menu flags appropriate for a generic Item.
   */
  public final int MENU_FLAGS = 0b000001;
  
  protected Jned jned;
  private int x;
  private int y;
  private int deltax;
  private int deltay;
  
  private int type;
  private int image;
  
  private boolean highlighted;
  private boolean selected;
  
  private Polygon shape;
  
  /**
   * Constructs a new Item with the given type and position.
   * @param jned a reference to the enclosing Jned instance
   * @param type the Jned item id of this Item
   * @param x this Item's x position
   * @param y this Item's y position
   */
  public Item (Jned jned, int type, int x, int y) {
    this.jned = jned;
    this.x = x;
    this.y = y;
    this.type = type;
    
    deltax = 0;
    deltay = 0;
    highlighted = false;
    selected = false;
    image = -1;
    
    calculateShape();
  }
  
  /**
   * Returns the menu flags constant for this Item, for use in compiling right-click menus.
   * @return this Item's MENU_FLAGS constant
   */
  public int getFlags() {
    return MENU_FLAGS;
  }
  
  /**
   * Returns a copy of this Item.
   * @return a new Item with the same properties as this Item
   */
  public Item duplicate() {
    return new Item(jned, type, x, y);
  }
  
  /**
   * Returns whether or not this Item has an image index set.
   * @return true if this Item has an image index set, false if it does not
   */
  public boolean hasImage() {
    return image >= 0;
  }
  
  /**
   * Returns this Item's image index.
   * @return the image index assigned to this Item
   */
  public int getImage() {
    return image;
  }
  
  /**
   * Sets the image index for this Item, corresponding to the item's image or set of images in
   * ImageBank.
   * @param imageIndex the image index for this Item
   */
  public void setImage(int imageIndex) {
    image = imageIndex;
  }
  
  /**
   * Returns this Item's x position.
   * @return this Item's x position
   */
  public int getX() {
    return x;
  }
  
  /**
   * Returns this Item's y position.
   * @return this Item's y position
   */
  public int getY() {
    return y;
  }
  
  /**
   * Returns this Item's Jned item id number.
   * @return this Item's type
   */
  public int getType() {
    return type;
  }
  
  /**
   * Moves this Item to a new position.
   * @param x the new x position
   * @param y the new y position
   */
  public void moveTo(int x, int y) {
    this.x = x;
    this.y = y;
    calculateShape();
  }
  
  /**
   * Sets the reference point for relative movement, essentially the position of another Item to
   * match movement with. Once set, calling moveRelative() with a new point will move this Item by
   * the same amount as the difference between the reference point and the supplied point.
   * @param x the reference x position
   * @param y the reference y position
   */
  public void setDelta(int x, int y) {
    deltax = x - this.x;
    deltay = y - this.y;
  }
  
  /**
   * Moves this Item to a new position relative to its reference point. This Item will move by the
   * same amount as the difference between the supplied point and this Item's reference point.
   * @param x the relative x position
   * @param y the relative y position
   */
  public void moveRelative(int x, int y) {
    this.x = x - deltax;
    this.y = y - deltay;
    calculateShape();
  }
  
  /**
   * Returns whether this Item is currently highlighted.
   * @return true if this Item is highlighted, false if it is not
   */
  public boolean isHighlighted() {
    return highlighted;
  }
  
  /**
   * Sets whether this Item is highlighted.
   * @param highlight true to highlight this Item, false to unhighlight it
   */
  public void setHighlight(boolean highlight) {
    highlighted = highlight;
  }
  
  /**
   * Returns whether this Item is currently selected.
   * @return true if this Item is selected, false if it is not
   */
  public boolean isSelected() {
    return selected;
  }
  
  /**
   * Sets whether this Item is selected.
   * @param select true to select this Item, false to unselect it
   */
  public void setSelect(boolean select) {
    selected = select;
  }
  
  /**
   * Returns the shape of this Item.
   * @return Polygon representing the shape and position of this Item
   */
  public Polygon getShape() {
    return shape;
  }
  
  /**
   * Calculates the polygon representing the shape and position of this Item.
   */
  public void calculateShape() {
    int[] xs = {x - 2, x + 2, x + 2, x - 2};
    int[] ys = {y + 2, y + 2, y - 2, y - 2};
    setShape(xs, ys);
  }
  
  /**
   * Sets the shape of this Item to the given Polygon.
   * @param Polygon representing the new shape and position of this Item
   */
  public void setShape(int[] xs, int[] ys) {
    shape = new Polygon(xs, ys, xs.length);
  }
  
  /**
   * Returns a String representation of this Item, in n level code format.
   * @return n level code String for this Item
   */
  public String toString() {
    return Item.typeCodes[type] + "^" + x + "," + y;
  }
  
  /**
   * Returns whether or not the given point intersects with this Item.
   * @param x x coordinate of point to check for overlap
   * @param y y coordinate of point to check for overlap
   * @return true if point overlaps this Item, false if it does not
   */
  public boolean overlaps(int x, int y) {
    return shape.contains(x, y);
  }
  
  /**
   * Returns whether or not the given Rectangle intersects with this Item.
   * @param rectangle the Rectangle to check for overlap
   * @return true if rectangle overlaps this Item, false if it does not
   */
  public boolean overlaps(Rectangle rectangle) {
    return shape.intersects(rectangle);
  }
  
  /**
   * Paints this Item, including highlighting and selection shading.
   * @param g Graphics context to paint this Item with
   * @param drawPolygon true to draw this Item's shape as a silhouette, false to not draw shape
   */
  public void paint(Graphics g, boolean drawPolygon) {
    boolean[] layer = {drawPolygon, highlighted, selected};
    for (int i = 0; i < 3; i++) {
      switch (i) {
        case 0:
          if (jned.drawImage(image, x, y, g)) {
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
      if (layer[i] && shape != null) {
        g.fillPolygon(shape);
      }
    }
  }
  
  /**
   * Paints this Item, including highlighting and selection shading.
   * @param g Graphics context to paint this Item with
   */
  public void paint(Graphics g) {
    paint(g, true);
  }
  
  /**
   * Paints a line connecting disparate parts of this Item. Does nothing for Items that aren't
   * doors.
   */
  public void paintTrigger(Graphics g) {}
  
  /**
   * Paints a translucent silhouette of this type of Item at the given position.
   * @param type the Jned item id for this Item
   * @param x the x position to draw Item ghost at
   * @param y the y position to draw Item ghost at
   * @param g Graphics context to draw Item ghost with
   */
  public static void paintGhost(int type, int x, int y, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    int[] xs = {x - 3, x + 3, x + 3, x - 3};
    int[] ys = {y + 3, y + 3, y - 3, y - 3};
    g.fillPolygon(xs, ys, 4);
  }  
}