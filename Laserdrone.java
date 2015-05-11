/**
 * A Laser drone item in Jned.
 * @author James Porter
 */
public class Laserdrone extends Drone {

  /**
   * Constructs a new Laserdrone with the given position, direction and behavior.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Laserdrone's x position
   * @param y this Laserdrone's y position
   * @param direction the direction this Laserdrone will initially face
   * @param behavior the drone behavior this Laserdrone will initially exhibit
   */
  public Laserdrone (Jned jned, int x, int y, int direction, int behavior) {
    super(jned, 7, x, y, direction, behavior);
    setImage(ImageBank.LASER);
  }
  
  /**
   * Returns a copy of this Laserdrone.
   * @return a new Laserdrone with the same properties as this Laserdrone
   */
  public Laserdrone duplicate() {
    return new Laserdrone(jned, getX(), getY(), getDirection(), getBehavior());
  }
}