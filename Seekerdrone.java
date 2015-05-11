/**
 * A Seeker drone item in Jned.
 * @author James Porter
 */
public class Seekerdrone extends Drone {

  /**
   * Constructs a new Seekerdrone with the given position, direction and behavior.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Seekerdrone's x position
   * @param y this Seekerdrone's y position
   * @param direction the direction this Seekerdrone will initially face
   * @param behavior the drone behavior this Seekerdrone will initially exhibit
   */
  public Seekerdrone (Jned jned, int x, int y, int direction, int behavior) {
    super(jned, 6, x, y, direction, behavior);
    setImage(ImageBank.SEEKER);
  }
  
  /**
   * Returns a copy of this Seekerdrone.
   * @return a new Seekerdrone with the same properties as this Seekerdrone
   */
  public Seekerdrone duplicate() {
    return new Seekerdrone(jned, getX(), getY(), getDirection(), getBehavior());
  }
}