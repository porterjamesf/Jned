import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * A superclass for all of the Drone items in Jned.
 * @author James Porter
 */
public class Drone extends DirectionalItem {
  public static final int DEFAULT_PATHLENGTH = 8;
  
  /**
   * The set of right-click menu flags appropriate for an Item with direction and behavior.
   */
  public final int MENU_FLAGS = 0b001100;
  
  /**
   * A static array containing the edge permeability settings of n tiles. Each value represents the
   * permeability of the tile for that index, where indices correspond to standard Jned tile index
   * order (as outlined in the header of LevelArea.java). Values are four-digit binary flags. The
   * least significant digit represents direction 0, and the most significant direction 3. A 0
   * means the tile is permeable coming from that direction, while a 1 means it is blocking.
   */
  public static final int[] TILE_PERM = {0b1111, 0b1111, 0b0011, 0b0110, 0b1100, 0b1001, 0b0001,
      0b0100, 0b0100, 0b0001, 0b0010, 0b0010, 0b1000, 0b1000, 0b0011, 0b0110, 0b1100, 0b1001,
      0b0100, 0b1000, 0b0001, 0b0010, 0b0011, 0b0110, 0b1100, 0b1001, 0b0011, 0b0110, 0b1100,
      0b1001, 0b0011, 0b0110, 0b1100, 0b1001};
      
  /**
   * A static array containing data on which directions to check in what order for each of the 
   * different drone behaviors. Each behavior has a primary, secondary, and tertiary direction
   * preference. Each successive group of 3 integers in the array belongs to one behavior. The
   * values in this array correspond to direction values, added to the currently facing direction
   * (e.g. 0 means forward, 1 means to the right, etc...).
   */
  public static final int[] CHECKS = {1, 0, 3, 3, 0, 1, 0, 1, 3, 0, 3, 1, 0, 1, 3, 0, 3, 1};
  
  // Values: 0,1,2,3,4,5,6 = surfaceCW, surfaceCCW, dumbCW, dumbCCW, alt, quasi-rand, none
  private int behavior;
  
  // Values: -1,0,1,2 = zap, seeker, laser, chaingun
  private int droneType;
  
  private int deltaX;
  private int deltaY;
  private int pathLength;
  private boolean pathRecalculationNeeded;
  private ArrayList<int[]> path;
  private Color[] pathShades;
  
  /**
   * Constructs a new Drone with the given position, direction and behavior.
   * @param jned a reference to the enclosing Jned instance
   * @param type the Jned item id of this Drone
   * @param x this Drone's x position
   * @param y this Drone's y position
   * @param direction the direction this Drone will initially face
   * @param behavior the drone behavior this Drone will initially exhibit
   */
  public Drone (Jned jned, int type, int x, int y, int direction, int behavior) {
    super(jned, type, x, y, direction);
    this.behavior = behavior;
    
    droneType = type - 6;
    setPathLength(Drone.DEFAULT_PATHLENGTH);
    path = new ArrayList<int[]>();
  }
  
  public int getFlags() {
    return MENU_FLAGS;
  }
  
  /**
   * Returns a copy of this Drone.
   * @return a new Drone with the same properties as this Drone
   */
  public Drone duplicate() {
    Drone temp = new Drone(jned, getType(), getX(), getY(), getDirection(), behavior);
    temp.setPathLength(pathLength);
    return temp;
  }
  
  /**
   * Sets the length of the path for this Drone to draw.
   * @param steps the number of cells in path
   */
  public void setPathLength(int steps) {
    pathLength = steps;
    pathShades = new Color[steps];
    int r = Colors.DRONE_PATH.getRed();
    int g = Colors.DRONE_PATH.getGreen();
    int b = Colors.DRONE_PATH.getBlue();
    int a = Colors.DRONE_PATH.getAlpha();
    for (int i = 0; i < steps; i++) {
      pathShades[i] = new Color(r, g, b, a * i / pathLength);
    }
    pathRecalculationNeeded = true;
  }
  
  /**
   * Returns this Drone's current behavior.
   * @return the current behavior
   */
  public int getBehavior() {
    return behavior;
  }
  
  /**
   * Sets this Drone's behavior.
   * @param the new behavior. Use 0 for surface-follow clockwise, 1 for surface-follow
   * counter-clockwise, 2 for dumb clockwise, 3 for dumb counter-clockwise, 4 for alternating, 5
   * for quasi-random, and 6 for none (still).
   */
  public void setBehavior(int behavior) {
    this.behavior = behavior;
    checkBehavior();
    pathRecalculationNeeded = true;
  }
  
  private void checkBehavior() {
    if (behavior < 0) {
      behavior = 0;
    }
    if (behavior > 6) {
      behavior = 6;
    }
  }
  
  public void setDirection(int direction) {
    super.setDirection(direction);
    pathRecalculationNeeded = true;
  }
  
  
  /**
   * Returns a String representation of this Drone, in n level code format.
   * @return n level code String for this Drone
   */
  public String toString() {
    String res = super.toString();
    return res.substring(0, res.length()-1) + behavior + "," + (droneType == 0 ? 1 : 0) + "," +
        (droneType < 0 ? 0 : droneType) + res.substring(res.length() - 2, res.length());
  }
  
  /**
   * Moves this Drone to a new position, adjusting coordinates to be at the center of a cell.
   * @param x the new x position
   * @param y the new y position
   */
  public void moveTo(int x, int y) {
    super.moveTo(24 * (x / 24) + 12, 24 * (y / 24) + 12);
    pathRecalculationNeeded = true;
  }
  
  /**
   * Sets the reference point for relative movement, essentially the position of another Item to
   * match movement with. Once set, calling moveRelative() with a new point will move this Drone by
   * the same amount as the difference between the reference point and the supplied point.
   * <p>
   * Performs the same function as the overridden method setDelta() in Item, but uses delta
   * variables visible to Drone.
   * @param x the reference x position
   * @param y the reference y position
   */
  public void setDelta(int x, int y) {
    deltaX = x - getX();
    deltaY = y - getY();
  }
  
  /**
   * Moves this Drone to a new position relative to its reference point, adjusting coordinates to be
   * at the center of a cell. This Drone will move by the same amount as the difference between the
   * supplied point and this Drone's reference point.
   * @param x the relative x position
   * @param y the relative y position
   */
  public void moveRelative(int x, int y) {
    super.moveTo(24 * ((x - deltaX) / 24) + 12, 24 * ((y - deltaY) / 24) + 12);
    pathRecalculationNeeded = true;
  }
  
  /**
   * Tells this Drone that its path may not be valid, and needs to be recalculated at the next
   * opportunity.
   */
  public void recalculatePath() {
    pathRecalculationNeeded = true;
  }
  
  /**
   * Calculates this Drone's path, storing it in an internal array. When drone paths are turned on,
   * this Drone will paint its path on the level from this data.
   * @param tiles the 2d array of tile data from LevelArea
   * @param doors the ArrayList of Door objects from LevelArea
   */
  public void calculatePath(int[][] tiles, ArrayList<Door> doors) {
    if (behavior < 6) {
      if (pathRecalculationNeeded) {
        path.clear();
        int[] pathArray = new int[2 * pathLength + 2];
        pathArray[0] = getX() / 24;
        pathArray[1] = getY() / 24;
        path.add(pathArray);
        
        calculatePathStep(tiles, doors, 0, getDirection(), 0, false);
        
        // Path has been calculated as cell indices; needs to be changed to coordinates
        for (int[] aPath : path) {
          for(int i = 0; i < aPath.length; i++) {
            aPath[i] = aPath[i] * 24 + 12;
          }
        }
        
        pathRecalculationNeeded = false;
      }
    }
  }
  
  // Calculates one step of a drone path. Requires references to map tile array, as well as all
  // doors on the map. This method may be called once for the first step of the path, and it will
  // recursively call itself for each subsequent step until the path length is reached. This method
  // will also recursively branch into multiple paths when the behavior is set to quasi-random.
  private void calculatePathStep(int[][] tiles, ArrayList<Door> doors, int step, int direction,
      int pathIndex, boolean swap) {
    int[] thisPath = path.get(pathIndex);
    
    // Direction checks are done in preferred order based on behavior (using the CHECKS array).
    // Check primary direction
    if (checkPath(tiles, thisPath[step], thisPath[step + 1], (direction + Drone.CHECKS[3 *
        behavior]) % 4) || checkDoor(doors, thisPath[step], thisPath[step + 1], (direction +
        Drone.CHECKS[3 * behavior]) % 4)) {
      // Check secondary direction
      if (checkPath(tiles, thisPath[step], thisPath[step + 1], (direction + Drone.CHECKS[3 *
          behavior + (swap ? 2 : 1)]) % 4) || checkDoor(doors, thisPath[step], thisPath[step + 1],
          (direction + Drone.CHECKS[3 * behavior + (swap ? 2 : 1)]) % 4)) {
        // Check tertiary direction
        if (checkPath(tiles, thisPath[step], thisPath[step + 1], (direction + Drone.CHECKS[3 *
            behavior + (swap ? 1 : 2)]) % 4) || checkDoor(doors, thisPath[step], thisPath[step + 1],
            (direction + Drone.CHECKS[3 * behavior + (swap ? 1 : 2)]) % 4)) {
          // Check behind
          if (checkPath(tiles, thisPath[step], thisPath[step + 1], (direction + 2) % 4) ||
              checkDoor(doors, thisPath[step], thisPath[step + 1], (direction + 2) % 4)) {
            // Blocked on all 4 sides
            direction = 4;
          } else {
            // Front and sides blocked, behind is clear
            direction = (direction + 2) % 4;
            if (behavior == 4) {
              swap = !swap;
            }
          }
        } else {
          // Front and favored side blocked, unfavored side is clear
          direction = (direction + Drone.CHECKS[3 * behavior + (swap ? 1 : 2)]) % 4;
          if (behavior == 4) {
            swap = !swap;
          }
        }
      } else {
        // First check blocked, second check clear
        // For surface follow (behavior <= 1), the second check is forward, so no change is needed.
        if (behavior > 1) {
          // For all other behaviors, the direction is changed to the secondary preference.
          // Quasi-random behavior also requires an additional check in tertiary direction, so that
          // a new path may split off if it is clear.
          if (behavior == 5) {
            if (!checkPath(tiles, thisPath[step], thisPath[step + 1], (direction + Drone.CHECKS[3 *
                behavior + 2]) % 4) && !checkDoor(doors, thisPath[step], thisPath[step + 1],
                (direction + Drone.CHECKS[3 * behavior + 2]) % 4)) {
              int[] anotherPath = new int[thisPath.length - step];
              anotherPath[0] = thisPath[step];
              anotherPath[1] = thisPath[step + 1];
              path.add(anotherPath);
              calculatePathStep(tiles, doors, 0, (direction + Drone.CHECKS[3 * behavior + (swap ? 1
                  : 2)]) % 4, path.size() - 1, false);
            }
          }
          direction = (direction + Drone.CHECKS[3 * behavior + (swap ? 2 : 1)]) % 4;
          if (behavior == 4) {
            swap = !swap;
          }
        }
      }
    } else {
      // First check clear
      // This Drone will move in preferred direction. Only in the case of surface follow does this
      // require a direction change
      if (behavior < 2) {
        direction = (direction + Drone.CHECKS[3 * behavior]) % 4;
      }
    }
    
    // Records new point
    switch (direction) {
      case 0:
        thisPath[step + 2] = thisPath[step] + 1;
        thisPath[step + 3] = thisPath[step + 1];
        break;
      case 1:
        thisPath[step + 2] = thisPath[step];
        thisPath[step + 3] = thisPath[step + 1] + 1;
        break;
      case 2:
        thisPath[step + 2] = thisPath[step] - 1;
        thisPath[step + 3] = thisPath[step + 1];
        break;
      case 3:
        thisPath[step + 2] = thisPath[step];
        thisPath[step + 3] = thisPath[step + 1] - 1;
        break;
      case 4:
        thisPath[step + 2] = thisPath[step];
        thisPath[step + 3] = thisPath[step + 1];
        break;
      default:
    }
    
    // Next step
    if (step < thisPath.length - 4) {
      calculatePathStep(tiles, doors, step + 2, direction, pathIndex, swap);
    }
  }
  
  // Checks one step of the path against the tile array, returning true if moving in the given
  // direction from the given cell indices is blocked.
  private boolean checkPath(int[][] tiles, int xCell, int yCell, int direction) {
    int cell1 = 1;
    if (xCell > 0 && xCell < 32 && yCell > 0 && yCell < 24) {
      cell1 = tiles[xCell - 1][yCell - 1];
    } else {
      if (xCell < 0 || xCell > 32 || yCell < 0 || yCell > 24) {
        cell1 = 0;
      }
    }
    
    int cell2 = 1;
    switch (direction) {
      case 0:
        if (xCell > -1 && xCell < 31 && yCell > 0 && yCell < 24) {
          cell2 = tiles[xCell][yCell-1];
        } else {
          if (xCell < -1 || xCell > 31 || yCell < 0 || yCell > 24) {
            cell2 = 0;
          }
        }
        break;
      case 1:
        if (xCell > 0 && xCell < 32 && yCell > -1 && yCell < 23) {
          cell2 = tiles[xCell-1][yCell];
        } else {
          if (xCell < 0 || xCell > 32 || yCell < -1 || yCell > 23) {
            cell2 = 0;
          }
        }
        break;
      case 2:
        if (xCell > 1 && xCell < 33 && yCell > 0 && yCell < 24) {
          cell2 = tiles[xCell-2][yCell-1];
        } else {
          if (xCell < 1 || xCell > 33 || yCell < 0 || yCell > 24) {
            cell2 = 0;
          }
        }
        break;
      case 3:
        if (xCell > 0 && xCell < 32 && yCell > 1 && yCell < 25) {
          cell2 = tiles[xCell-1][yCell-2];
        } else {
          if (xCell < 0 || xCell > 32 || yCell < 1 || yCell > 25) {
            cell2 = 0;
          }
        }
        break;
      default:
    }
    // Moving into an empty cell is always allowed.
    if(cell2 == 0) {
      return false;
    }
    // Moving from an empty cell into a non-empty cell always blocks.
    if(cell1 == 0) {
      return true;
    }
    // When both cells are non-empty, blocking depends in the tile permeability
    return (Drone.TILE_PERM[cell1] & (direction == 0 ? 0b0001 : (direction == 1 ? 0b0010 :
        (direction == 2 ? 0b0100 : 0b1000)))) == 0 || (Drone.TILE_PERM[cell2] & (direction == 0 ?
        0b0100 : (direction == 1 ? 0b1000 : (direction == 2 ? 0b0001 : 0b0010)))) == 0;
  }
  
  // Checks one step of the path against the door list, returning true if moving in the given
  // direction from the given cell indices is blocked.
  private boolean checkDoor(ArrayList<Door> doors, int xCell, int yCell, int direction) {
    for (Door door : doors) {
      boolean inWay = false;
      // Doors in the same cell facing the same direction as the drone are in the way
      if (door.getRow() == xCell && door.getColumn() == yCell && door.getDirection() == direction) {
        inWay = true;
      }
      // Doors in the destination cell facing the opposite direction as the drone are in the way
      switch (direction) {
        case 0:
          if (door.getRow() == xCell + 1 && door.getColumn() == yCell && door.getDirection() == 2) {
            inWay = true;
          }
          break;
        case 1:
          if (door.getRow() == xCell && door.getColumn() == yCell + 1 && door.getDirection() == 3) {
            inWay = true;
          }
          break;
        case 2:
          if (door.getRow() == xCell - 1 && door.getColumn() == yCell && door.getDirection() == 0) {
            inWay = true;
          }
          break;
        case 3:
          if (door.getRow() == xCell && door.getColumn() == yCell - 1 && door.getDirection() == 1) {
            inWay = true;
          }
          break;
        default:
      }
      // Locked doors block by default but not when activated, and trap doors do not block by
      // default but do when activated. Activation (hitting a switch) is simulated by selecting the
      // door(s).
      if (inWay) {
        switch (door.getType()) {
          case 15:
            if (!door.isAnySelected()) {
              return true;
            }
          case 16:
            if (door.isAnySelected()) {
              return true;
            }
          default:
            return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Calculates the polygon representing the shape and position of this Drone.
   */
  public void calculateShape() {
    int x = getX();
    int y = getY();
    int[] xs = {x - 4, x + 4, x + 9, x + 9, x + 3, x - 3, x - 9, x - 9};
    int[] ys = {y - 9, y - 9, y - 4, y + 3, y + 9, y + 9, y + 3, y - 4};
    setShape(xs, ys);
  }
  
  /**
   * Paints this Drone's path.
   * @param g Graphics context to paint path with
   */
  public void paintPath(Graphics g) {
    if (behavior < 6) {
      for (int[] currentPath : path) {
        int colorIndex = currentPath.length / 2 - 2;
        for (int i = 2; i < currentPath.length; i += 2) {
          g.setColor(pathShades[colorIndex--]);
          g.drawLine(currentPath[i - 2], currentPath[i - 1], currentPath[i], currentPath[i + 1]);
        }
      }
    }
  }
  
  /**
   * Paints a translucent silhouette of this Drone at the given position.
   * @param type the Jned item id for this Drone
   * @param x the x position to draw ghost at
   * @param y the y position to draw ghost at
   * @param direction the direction to draw the ghost facing
   * @param g Graphics context to draw ghost with
   */
  public static void paintGhost(int type, int x, int y, int direction, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    int[] xs = {x - 4, x + 4, x + 9, x + 9, x + 3, x - 3, x - 9, x - 9};
    int[] ys = {y - 9, y - 9, y - 4, y + 3, y + 9, y + 9, y + 3, y - 4};
    g.fillPolygon(xs, ys, 8);
  }
}