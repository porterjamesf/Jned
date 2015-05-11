import java.awt.Graphics;

/**
 * A Normal door item in Jned.
 * @author James Porter
 */
public class Normaldoor extends Door {
  
  /**
   * The set of right-click menu flags appropriate for an Item with direction.
   */
  public final int  MENU_FLAGS = 0b000100;
  
  int row;
  int column;
  int deltaRow;
  int deltaColumn;
  
  /**
   * Constructs a new Normaldoor with the given direction and cell indices.
   * @param jned a reference to the enclosing Jned instance
   * @param direction the direction this Normaldoor will initially face
   * @param row this Normaldoor's row
   * @param column this Normaldoor's column
   */
  public Normaldoor (Jned jned, int direction, int row, int column) {
    super(jned, 14, Normaldoor.findX(row, direction), Normaldoor.findY(column, direction),
        direction);
    this.row = row;
    this.column = column;
    
    deltaRow = 0;
    deltaColumn = 0;
    
    calculateShape();
    setImage(ImageBank.NDOOR);
    jned.calculateDronePaths();
  }
  
  public int getFlags() {
    return MENU_FLAGS;
  }
  
  /**
   * Returns a copy of this Normaldoor.
   * @return a new Normaldoor with the same properties as this Normaldoor
   */
  public Normaldoor duplicate() {
    return new Normaldoor(jned, getDirection(), row, column);
  }
  
  /**
   * Returns this Normaldoor's x coordinate
   * @return this Normaldoor's x coordinate
   */
  public int getX() {
    return Normaldoor.findX(row, getDirection());
  }
  
  /**
   * Returns this Normaldoor's y coordinate
   * @return this Normaldoor's y coordinate
   */
  public int getY() {
    return Normaldoor.findY(column, getDirection());
  }
  
  /**
   * Returns this Normaldoor's row index
   * @return this Normaldoor's row index
   */
  public int getRow() {
    return row;
  }
  
  /**
   * Returns this Normaldoor's column index
   * @return this Normaldoor's column index
   */
  public int getColumn() {
    return column;
  }
  
  /**
   * Moves this Normaldoor to a new position, snapping it to the nearest cell.
   * @param x the new x position
   * @param y the new y position
   */
  public void moveTo(int x, int y) {
    row = x / 24;
    column = y / 24;
    setItemCoordinates();
    calculateShape();
    jned.calculateDronePaths();
  }
  
  /**
   * Sets the reference point for relative movement, essentially the position of another Item to
   * match movement with. Once set, calling moveRelative() with a new point will move this
   * Normaldoor by the same amount as the difference between the reference point and the supplied
   * point.
   * @param x the reference x position
   * @param y the reference y position
   */
  public void setDelta(int x, int y) {
    deltaRow = x - (24 * row + 12);
    deltaColumn = y - (24 * column + 12);
  }
  
  /**
   * Moves this Normaldoor to a new position relative to its reference point. This Normaldoor will
   * move by the same amount as the difference between the supplied point and this Normaldoor's
   * reference point.
   * @param x the relative x position
   * @param y the relative y position
   */
  public void moveRelative(int x, int y) {
    row = (x - deltaRow) / 24;
    column = (y - deltaColumn) / 24;
    setItemCoordinates();
    calculateShape();
    jned.calculateDronePaths();
  }
  
  public void setDirection(int direction) {
    super.setDirection(direction);
    setItemCoordinates();
  }
  
  // Updates the x/y values in Item after cell indices have changed
  private void setItemCoordinates() {
    super.moveTo(Normaldoor.findX(row, getDirection()), Normaldoor.findY(column, getDirection()));
  }
  
  //Adjusts cell index/direction values to corresponding x/y values
  private static int findX(int row, int direction) {
    return 24 * row + (direction == 0 ? 24 : (direction == 2 ? 0 : 12));
  }
  private static int findY(int column, int direction) {
    return 24 * column + (direction == 1 ? 24 : (direction == 3 ? 0 : 12));
  }
  
  public void setSelect(boolean select) {
    super.setSelect(select);
    jned.calculateDronePaths();
  }
  
  /**
   * Returns a String representation of this Normaldoor, in n level code format.
   * @return n level code String for this Normaldoor
   */
  public String toString() {
    String res = super.toString();
    return res.substring(0, res.length() - 1) + (getDirection() % 2 == 1 ? 1 : 0) + ",0," + row +
        "," + column + ",0," + (getDirection() == 2 ? -1 : 0) + "," + (getDirection() == 3 ? -1 :
        0);
  }
  
  /**
   * Calculates the polygon representing the shape and position of this Normaldoor.
   */
  public void calculateShape() {
    int x = 24 * row;
    int y = 24 * column;
    switch (getDirection()) {
      case 0:
        int[] xs0 = {x + 22, x + 25, x + 25, x + 22};
        int[] ys0 = {y, y, y + 24, y + 24};
        setShape(xs0, ys0);
        break;
      case 1:
        int[] xs1 = {x + 24, x + 24, x, x};
        int[] ys1 = {y + 22, y + 25, y + 25, y + 22};
        setShape(xs1, ys1);
        break;
      case 2:
        int[] xs2 = {x + 3, x, x, x + 3};
        int[] ys2 = {y + 24, y + 24, y, y};
        setShape(xs2, ys2);
        break;
      case 3:
        int[] xs3 = {x, x, x + 24, x + 24};
        int[] ys3 = {y + 3, y, y, y + 3};
        setShape(xs3, ys3);
        break;
      default:
    }
  }
  
  /**
   * Paints a translucent silhouette of a Normaldoor at the given cell indices
   * @param direction the direction to draw Normaldoor ghost facing
   * @param row the row of the cell to draw Normaldoor ghost in
   * @param column the column of the cell to draw Normaldoor ghost in
   * @param g Graphics context to draw Normaldoor ghost with
   */
  public static void paintGhost(int direction, int row, int column, Graphics g) {
    g.setColor(Colors.ITEM_GHOST);
    int x = 24 * row;
    int y = 24 * column;
    switch (direction) {
      case 0:
        int[] xs0 = {x + 22, x + 25, x + 25, x + 22};
        int[] ys0 = {y, y, y + 24, y + 24};
        g.fillPolygon(xs0, ys0, 4);
        break;
      case 1:
        int[] xs1 = {x + 24, x + 24, x, x};
        int[] ys1 = {y + 22, y + 25, y + 25, y + 22};
        g.fillPolygon(xs1, ys1, 4);
        break;
      case 2:
        int[] xs2 = {x + 3, x, x, x + 3};
        int[] ys2 = {y + 24, y + 24, y, y};
        g.fillPolygon(xs2, ys2, 4);
        break;
      case 3:
        int[] xs3 = {x, x, x + 24, x + 24};
        int[] ys3 = {y + 3, y, y, y + 3};
        g.fillPolygon(xs3, ys3, 4);
        break;
      default:
    }
  }  
}