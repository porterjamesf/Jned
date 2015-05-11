import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * A Gauss turret item in Jned.
 * @author James Porter
 */
public class Player extends Item {

  /**
   * The set of right-click menu flags appropriate for a Player.
   */
  public final int  MENU_FLAGS = 0b010001;
  
  // Each pair of ints is a coordinate, and each pair of coordinates are endpoints of a line. The
  // set of these lines represent the shape of the Player.
  private static int[][] PIX = {
      {2, 9}, {2, 7},
      {1, 9}, {1, 3},
      {1, 0}, {1, -3},
      {0, 9}, {0, -6},
      {-1, 1}, {-1, -8},
      {-2, 9}, {-2, -7},
      {-3, 9}, {-3, 4},
      {-3, -1}, {-3, -6},
      {-4, 9}, {-4, 7},
      {-4, -1}, {-4, -5},
      {0, -9}, {-1, -9},
      {0, -10}, {-2, -10},
      {-1, -11}, {-3, -11}};
  
  /**
   * Constructs a new Player with the given position.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Player's x position
   * @param y this Player's y position
   */
  public Player(Jned jned, int x, int y) {
    super(jned, 9, x, y);
    setImage(ImageBank.PLAYER);
  }
  
  public int getFlags() {
    return MENU_FLAGS;
  }
  
  /**
   * Returns a copy of this Player.
   * @return a new Player with the same properties as this Player
   */
  public Player duplicate() {
    return new Player(jned, getX(), getY());
  }
  
  /**
   * Returns whether or not the given point intersects with this Player.
   * @param x x coordinate of point to check for overlap
   * @param y y coordinate of point to check for overlap
   * @return true if point overlaps this Player, false if it does not
   */
  public boolean overlaps(int x, int y) {
    int spotx = x - getX();
    int spoty = y - getY();
    // Check vertical lines
    for (int i = 0; i < Player.PIX.length; i += 2) {
      if (Player.PIX[i][0] == spotx && Player.PIX[i + 1][0] == spotx) {
        if (spoty <= Player.PIX[i][1] && spoty >= Player.PIX[i + 1][1]) {
          return true;
        }
      }
    }
    // Check horizontal lines
    for (int i = 0; i < Player.PIX.length; i += 2) {
      if (Player.PIX[i][1] == spoty && Player.PIX[i + 1][1] == spoty) {
        if (spotx <= Player.PIX[i][0] && spotx >= Player.PIX[i + 1][0]) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Returns whether or not the given Rectangle intersects with this Player.
   * @param rectangle the Rectangle to check for overlap
   * @return true if rectangle overlaps this Player, false if it does not
   */
  public boolean overlaps(Rectangle rectangle) {
    int nearX = rectangle.x + rectangle.width;
    int nearY = rectangle.y + rectangle.height;
    if (getX() <= rectangle.x) {
      nearX = rectangle.x;
    } else {
      if (getX() <= nearX) {
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
    return overlaps(nearX, nearY);
  }
  
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
        for (int j = 0; j < Player.PIX.length; j += 2) {
          g.drawLine(getX() + Player.PIX[j][0], getY() + Player.PIX[j][1], getX() +
              Player.PIX[j + 1][0], getY() + Player.PIX[j + 1][1]);
        }
      }
    }
  }
  
  /**
   * Paints a translucent silhouette of this Player at the given position.
   * @param x the x position to draw Player ghost at
   * @param y the y position to draw Player ghost at
   * @param g Graphics context to draw Player ghost with
   */
  public static void paintGhost(int x, int y, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    for (int i = 0; i < PIX.length; i += 2) {
      g.drawLine(x + PIX[i][0], y + PIX[i][1], x + PIX[i + 1][0], y + PIX[i + 1][1]);
    }
  }
}