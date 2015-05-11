/**
 * An abstract superclass for all door Items. Specifies methods for accessing row and column.
 * @author James Porter 
 */
public abstract class Door extends DirectionalItem {
  
  /**
   * Constructs a new Door with the given position and direction.
   * <p>
   * Note: the x and y values are coordinates, not cell indices.
   * @param jned a reference to the enclosing Jned instance
   * @param type the Jned item id of this Door
   * @param x this Door's x position
   * @param y this Door's y position
   * @param direction the direction this Door will initially face
   */
  public Door (Jned jned, int type, int x, int y, int direction) {
    super(jned, type, x, y, direction);
  }

  /**
   * Returns the row of the cell that this Door occupies.
   * @return this Door's row
   */
  public abstract int getRow();
  
  /**
   * Returns the column of the cell that this Door occupies.
   * @return this Door's column
   */
  public abstract int getColumn();
  
  /**
   * Returns whether or not any part of this Door (including possibly a switch) is selected.
   * @return true if any part (door or switch) of this Door is selected, false if no part is selected
   */
  public boolean isAnySelected() {
    return super.isSelected();
  }
}