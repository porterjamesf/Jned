import java.awt.Graphics;

/**
 * A superclass for all Items that face in one of the four directions.
 * @author James Porter
 */
public class DirectionalItem extends Item {
  /**
   * The set of right-click menu flags appropriate for an Item with direction.
   */
  public final int MENU_FLAGS = 0b000101;
  
  // Values: 0,1,2,3 = right,down,left,up
  private int direction;
  private int imageIndexBase;
  
  /**
   * Constructs a new DirectionalItem with the given position and direction.
   * @param jned a reference to the enclosing Jned instance
   * @param type the Jned item id of this DirectionalItem
   * @param x this DirectionalItem's x position
   * @param y this DirectionalItem's y position
   * @param direction the direction this DirectionalItem will initially face
   */
  public DirectionalItem (Jned jned, int type, int x, int y, int direction) {
    super(jned, type, x, y);
    this.direction = direction;
    checkDirection();
    calculateShape();
    imageIndexBase = -1;
  }
  
  public int getFlags() {
    return MENU_FLAGS;
  }
  
  /**
   * Returns a copy of this DirectionalItem.
   * @return a new DirectionalItem with the same properties as this DirectionalItem
   */
  public DirectionalItem duplicate() {
    return new DirectionalItem(jned, getType(), getX(), getY(), direction);
  }
  
  /**
   * Returns this DirectionalItem's baseline image index, i.e. the image index for direction 0.
   * @return the baseline image index assigned to this DirectionalItem
   */
  public int getImage() {
    return imageIndexBase;
  }
  
  /**
   * Returns this DirectionalItem's image index, or optionally its baseline image index.
   * @param getDirectional true to get the correctly facing directional image index, false to get
   * the baseline image index.
   * @return the image index assigned to this DirectionalItem
   */
  public int getImage(boolean getDirectional) {
    if (getDirectional) {
      return super.getImage();
    }
    return imageIndexBase;
  }
  
  /**
   * Sets the baseline image index for this DirectionalItem, corresponding to the item's set of
   * images in ImageBank, beginning with the image for direction 0.
   * @param imageIndex the baseline image index for this DirectionalItem
   */
  public void setImage(int image) {
    super.setImage(image + direction);
    imageIndexBase = image;
  }
  
  /**
   * Returns this DirectionalItem's current direction.
   * @return the current direction
   */
  public int getDirection() {
    return direction;
  }
  
  /**
   * Sets this DirectionalItem's direction.
   * @param direction the new direction. Use 0 for right, 1 for down, 2 for left, and 3 for up.
   */
  public void setDirection(int direction) {
    this.direction = direction;
    checkDirection();
    calculateShape();
    super.setImage(imageIndexBase + direction);
  }
  
  private void checkDirection() {
    if (direction < 0) {
      direction = 0;
    }
    if (direction > 3) {
      direction = 3;
    }
  }
  
  /**
   * Returns a String representation of this DirectionalItem, in n level code format.
   * @return n level code String for this DirectionalItem
   */
  public String toString() {
    return super.toString() + "," + direction;
  }
  
  /**
   * Paints a translucent silhouette of this type of DirectionalItem at the given position.
   * @param type the Jned item id for this DirectionalItem
   * @param x the x position to draw ghost at
   * @param y the y position to draw ghost at
   * @param direction the direction to draw the ghost facing
   * @param g Graphics context to draw ghost with
   */
  public static void paintGhost(int type, int x, int y, int direction, Graphics g) {
    Item.paintGhost(type, x, y, g);
  }  
}