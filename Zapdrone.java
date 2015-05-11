/**
 * A Zap drone item in Jned.
 * @author James Porter
 */
public class Zapdrone extends Drone {

  /**
   * Constructs a new Zapdrone with the given position, direction and behavior.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Zapdrone's x position
   * @param y this Zapdrone's y position
   * @param direction the direction this Zapdrone will initially face
   * @param behavior the drone behavior this Zapdrone will initially exhibit
   */
  public Zapdrone (Jned jned, int x, int y, int direction, int behavior) {
    super(jned, 5, x, y, direction, behavior);
    setImage(ImageBank.ZAP);
  }
  
  /**
   * Returns a copy of this Zapdrone.
   * @return a new Zapdrone with the same properties as this Zapdrone
   */
  public Zapdrone duplicate() {
    return new Zapdrone(jned, getX(), getY(), getDirection(), getBehavior());
  }
}