/**
 * A Homing launcher item in Jned.
 * @author James Porter
 */
public class Hominglauncher extends Turret {

  /**
   * Constructs a new Hominglauncher with the given position.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Hominglauncher's x position
   * @param y this Hominglauncher's y position
   */
  public Hominglauncher (Jned jned, int x, int y) {
    super(jned, 1, x, y);
    setImage(ImageBank.HOMING);
  }
  
  /**
   * Returns a copy of this Hominglauncher.
   * @return a new Hominglauncher with the same properties as this Hominglauncher
   */
  public Hominglauncher duplicate() {
    return new Hominglauncher(jned, getX(), getY());
  }
}