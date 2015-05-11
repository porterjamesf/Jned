/**
 * An object that can be pushed or unpushed, such as a Button or a MultiButton.
 * @author James Porter
 */
public interface Pushable {
  /**
   * Pushes or unpushed the object.
   * @param isPushed true to push, false to unpush
   */
  public void setPushed(boolean isPushed);
}