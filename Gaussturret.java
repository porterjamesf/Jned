/**
 * A Gauss turret item in Jned.
 * @author James Porter
 */
public class Gaussturret extends Turret {

  /**
   * Constructs a new Gaussturret with the given position.
   * @param jned a reference to the enclosing Jned instance
   * @param x this Gaussturret's x position
   * @param y this Gaussturret's y position
   */
  public Gaussturret (Jned jned, int x, int y) {
    super(jned, 0, x, y);
    setImage(ImageBank.GAUSS);
  }
  
  /**
   * Returns a copy of this Gaussturret.
   * @return a new Gaussturret with the same properties as this Gaussturret
   */
  public Gaussturret duplicate() {
    return new Gaussturret(jned, getX(), getY());
  }
}