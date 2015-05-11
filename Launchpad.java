import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;

/**
 * A Launch pad item in Jned.
 * @author James Porter
 */
public class Launchpad extends Item {

  /**
   * The set of right-click menu flags appropriate for a Launchpad.
   */
  public final int  MENU_FLAGS = 0b100101;
  
  private double xPower;
  private double yPower;
  private Polygon shape;
  
  /**
   * Constructs a new Launchpad with the given position and power.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Launchpad's x position
   * @param y this Launchpad's y position
   * @param powerX this Launchpad's horizontal power (positive for right, negative for left)
   * @param powerY this Launchpad's vertical power (positive for down, negative for up)
   */
  public Launchpad (Jned jned, int x, int y, double powerX, double powerY) {
    super(jned, 17, x, y);
    
    xPower = powerX;
    yPower = powerY;
    
    calculateRectangle();
    setImage(ImageBank.LAUNCH);
  }
  
  /**
   * Constructs a new Launchpad with the given position and direction.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Launchpad's x position
   * @param y this Launchpad's y position
   * @param direction the direction this Launchpad will initially face
   */
  public Launchpad (Jned jned, int x, int y, int direction){
    super(jned, 17, x, y);
    double root2 = Math.sqrt(2) / 2;
    switch (direction) {
      default:
        // fall through
      case 0:
        xPower = 1.0;
        yPower = 0.0; 
        break;  
      case 1:
        xPower = root2;
        yPower = root2;
        break;
      case 2:
        xPower = 0.0;
        yPower = 1.0;
        break;
      case 3:
        xPower = -root2;
        yPower = root2;
        break;
      case 4:
        xPower = -1.0;
        yPower = 0.0;
        break;
      case 5:
        xPower = -root2;
        yPower = -root2;
        break;
      case 6:
        xPower = 0.0;
        yPower = -1.0;
        break;
      case 7:
        xPower = root2;
        yPower = -root2;
        break;
    }
    calculateRectangle();
    setImage(ImageBank.LAUNCH);
  }
  
  public int getFlags() {
    return MENU_FLAGS;
  }
  
  /**
   * Returns a copy of this Launchpad.
   * @return a new Launchpad with the same properties as this Launchpad
   */
  public Launchpad duplicate() {
    return new Launchpad(jned, getX(), getY(), xPower, yPower);
  }
  
  private void calculateRectangle() {
    int direction;
    if (xPower == 0.0) {
      if (yPower > 0.0) {
        direction = 2;
      } else {
        direction = 6;
      }
    } else {
      if (xPower > 0.0) {
        if (yPower == 0.0) {
          direction = 0;
        } else {
          if (yPower > 0.0) {
            direction = 1;
          } else {
            direction = 7;
          }
        }
      } else {
        if (yPower == 0.0) {
          direction = 4;
        } else {
          if (yPower > 0.0) {
            direction = 3;
          } else {
            direction = 5;
          }
        }
      }
    }
    
    int[] xs = new int[4];
    int[] ys = new int[4];
    switch (direction) {
      default:
        // fall through
      case 0:
        // fall through
      case 4:
        xs[0] = xs[3] = getX();
        xs[1] = xs[2] = getX() + (direction == 0 ? 5 : -5);
        ys[0] = ys[1] = getY() - 7;
        ys[2] = ys[3] = getY() + 7;
        break;
      case 2:
        // fall through
      case 6:
        xs[0] = xs[1] = getX() - 7;
        xs[2] = xs[3] = getX() + 7;
        ys[0] = ys[3] = getY();
        ys[1] = ys[2] = getY() + (direction == 2 ? 5 : -5);
        break;
      case 1:
        // fall through
      case 3:
        // fall through
      case 5:
        // fall through
      case 7:
        xs[0] = getX() + (direction % 6 == 1 ? 5 : -5);
        ys[0] = getY() + (direction < 4 ? -5 : 5);
        xs[1] = getX() + (direction % 6 == 1 ? 9 : -9);
        ys[1] = getY() + (direction < 4 ? -1 : 1);
        xs[2] = getX() + (direction % 6 == 1 ? -1 : 1);
        ys[2] = getY() + (direction < 4 ? 9 : -9);
        xs[3] = getX() + (direction % 6 == 1 ? -5 : 5);
        ys[3] = getY() + (direction < 4 ? 5 : -5);
        break;
    }
    shape = new Polygon(xs, ys, 4);
  }
  
  /**
   * Returns the horizontal power of this Launchpad.
   * @return double representing this Launchpad's horizontal power
   */
  public double getPowerX() {
    return xPower;
  }
  
  /**
   * Returns the vertical power of this Launchpad.
   * @return double representing this Launchpad's vertical power
   */
  public double getPowerY() {
    return yPower;
  }
  
  /**
   * Returns the magnitude of the launch power of this Launchpad.
   * @return double representing this Launchpad's launch power magnitude
   */
  public double getPower() {
    return Math.hypot(xPower, yPower);
  }
  
  /**
   * Sets the horizontal power of this Launchpad.
   * @param power double representing horizontal power to set
   */
  public void setPowerX(double power) {
    xPower = power;
    calculateRectangle();
  }
  
  /**
   * Sets the vertical power of this Launchpad.
   * @param power double representing vertical power to set
   */
  public void setPowerY(double power) {
    yPower = power;
    calculateRectangle();
  }
  
  /**
   * Sets the magnitude of the launch power of this Launchpad without affecting the direction.
   * @param power double representing the power to set
   */
  public void setPower(double power) {
    double theta = Math.atan2(yPower, xPower);
    xPower = Math.cos(theta) * power;
    yPower = Math.sin(theta) * power;
    // Very near-zero values must be rounded, or Launchpad will never face non-diagonal directions
    if (Math.abs(xPower) < 0.00000000000001) {
      xPower = 0.0;
    }
    if (Math.abs(yPower) < 0.00000000000001) {
      yPower = 0.0;
    }
    calculateRectangle();
  }
  
  /**
   * Returns the precise direction of this Launchpad.
   * @return double representing exact direction of this Launchpad in radians, in the range of -pi
   * to pi (0 meaning facing to the right)
   */
  public double getDirection() {
    return Math.atan2(yPower, xPower);
  }
  
  /**
   * Returns the direction of this Launchpad as one of eight discrete directions. Direction
   * corresponds to the direction a launchpad in n will be drawn facing with this Launchpad's power.
   * @return int representing direction of this Launchpad. 0 means facing right, 1 right-down,
   * ...7 right-up.
   */
  public int getDirInt() {
    if (xPower == 0.0) {
      if (yPower < 0.0) {
        return 6;
      }
      return 2;
    }
    if (yPower == 0.0) {
      if (xPower < 0.0) {
        return 4;
      }
      return 0;
    }
    if (xPower < 0.0) {
      if (yPower < 0.0) {
        return 5;
      }
      return 3;
    }
    if (yPower < 0.0) {
      return 7;
    }
    return 1;
  }
  
  /**
   * Sets the precise direction of this Launchpad without affecting the power.
   * @param theta double representing exact direction to set in radians, in the range of -pi to pi
   * (0 meaning facing to the right)
   */
  public void setDirection(double theta) {
    double power = Math.hypot(yPower, xPower);
    xPower = Math.cos(theta) * power;
    yPower = Math.sin(theta) * power;
    // Very near-zero values must be rounded, or Launchpad will never face non-diagonal directions
    if (Math.abs(xPower) < 0.00000000000001) {
      xPower = 0.0;
    }
    if (Math.abs(yPower) < 0.00000000000001) {
      yPower = 0.0;
    }
    calculateRectangle();
  }
  
  public void moveTo(int x, int y) {
    super.moveTo(x, y);
    calculateRectangle();
  }
  
  public void moveRelative(int x, int y) {
    super.moveRelative(x, y);
    calculateRectangle();
  }
  
  /**
   * Returns a String representation of this Launchpad, in n level code format.
   * @return n level code String for this Launchpad
   */
  public String toString() {
    return super.toString() + "," + format(xPower) + "," + format(yPower);
  }
  
  private String format(double num) {
    if ((double) ((int) num) == num) {
      return "" + ((int) num);
    }
    String result = String.valueOf(Math.abs(num));
    if (result.length() > 17) {
      result = result.substring(0, 17);
    }
    return (num < 0 ? "-" : "") + result;
  }
  
  /**
   * Returns whether or not the given point intersects with this Launchpad.
   * @param x x coordinate of point to check for overlap
   * @param y y coordinate of point to check for overlap
   * @return true if point overlaps this Launchpad, false if it does not
   */
  public boolean overlaps(int x, int y) {
    return shape.contains(x, y);
  }
  
  /**
   * Returns whether or not the given Rectangle intersects with this Launchpad.
   * @param rectangle the Rectangle to check for overlap
   * @return true if rectangle overlaps this Launchpad, false if it does not
   */
  public boolean overlaps(Rectangle rectangle) {
    return shape.intersects(rectangle);
  }
  
  public void paint(Graphics g) {
    boolean[] layer = {true, isHighlighted(), isSelected()};
    for (int i = 0; i < 3; i++) {
      switch (i) {
        case 0:
          int d = getDirInt();
          if (jned.drawImage(getImage() + d, getX(), getY(), g)) {
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
        g.fillPolygon(shape);
      }
    }
  }
  
  /**
   * Paints a line representing the power and direction of this Launchpad.
   * @param g Graphics context with which to draw line
   */
  public void paintLine(Graphics g) {
    g.setColor(Colors.LAUNCHPAD_LINE);
    g.drawLine(getX(), getY(), getX() + (int) (xPower * 24), getY() + (int) (yPower * 24));
  }
  
  /**
   * Paints a translucent silhouette of a Launchpad at the given position.
   * @param x the x position to draw Launchpad ghost at
   * @param y the y position to draw Launchpad ghost at
   * @param direction the direction to draw Launchpad ghost facing
   * @param g Graphics context to draw Launchpad ghost with
   */
  public static void paintGhost(int x, int y, int direction, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    int[] xs = new int[4];
    int[] ys = new int[4];
    switch (direction) {
      default:
        // fall through
      case 0:
        // fall through
      case 4:
        xs[0] = xs[3] = x;
        xs[1] = xs[2] = x + (direction == 0 ? 5 : -5);
        ys[0] = ys[1] = y - 7;
        ys[2] = ys[3] = y + 7;
      break;
      case 2:
        // fall through
      case 6:
        xs[0] = xs[1] = x - 7;
        xs[2] = xs[3] = x + 7;
        ys[0] = ys[3] = y;
        ys[1] = ys[2] = y + (direction == 2 ? 5 : -5);
      break;
      case 1:
        // fall through
      case 3:
        // fall through
      case 5:
        // fall through
      case 7:
        xs[0] = x + (direction % 6 == 1 ? 5 : -5);
        ys[0] = y + (direction < 4 ? -5 : 5);
        xs[1] = x + (direction % 6 == 1 ? 9 : -9);
        ys[1] = y + (direction < 4 ? -1 : 1);
        xs[2] = x + (direction % 6 == 1 ? -1 : 1);
        ys[2] = y + (direction < 4 ? 9 : -9);
        xs[3] = x + (direction % 6 == 1 ? -5 : 5);
        ys[3] = y + (direction < 4 ? 5 : -5);
      break;
    }
    g.fillPolygon(xs, ys, 4);
  }  
}