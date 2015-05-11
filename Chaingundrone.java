/**
 * A Chaingun Drone item in Jned.
 * @author James Porter
 */
public class Chaingundrone extends Drone {

  /**
   * Constructs a new Chaingundrone with the given position, direction and behavior.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Drone's x position
   * @param y this Drone's y position
   * @param direction the direction this Drone will initially face
   * @param behavior the drone behavior this Drone will initially exhibit
   */
  public Chaingundrone (Jned jned, int x, int y, int direction, int behavior) {
    super(jned, 8, x, y, direction, behavior);
    setImage(ImageBank.CHAINGUN);
  }
  
  public Chaingundrone duplicate() {
    return new Chaingundrone(jned, getX(), getY(), getDirection(), getBehavior());
  }
}