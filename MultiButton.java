import javax.swing.JPanel;

/**
 * A multilayered Button. Essentially a group of Buttons that all occupy the same spot, any of
 * which may be on top and functional at a given time. MultiButton holds a Button array and has an
 * index number for which Button to display and pass push/unpush events to.
 * @author James Porter
 */
public class MultiButton extends JPanel implements Pushable {
  private Button[] buttons;
  private int index;
  
  /**
   * Constructs a MultiButton using the given Button array and dimensions. Buttons in the Button
   * array should all have the same dimensions as the MultiButton.
   * @param buttons Button array to occupy this MultiButton
   * @param x the x coordinate
   * @param y the y coordinate
   * @param width the width
   * @param height the height
   */
  public MultiButton(Button[] buttons, int x, int y, int width, int heigth) {
    super();
    setLayout(null);
    this.buttons = buttons;
    setBounds(x, y, width, heigth);
    setBackground(Colors.BG_COLOR);
    setIndex(0);
  }
  
  /**
   * Returns the index of the presently active Button.
   * @return index of active Button
   */
  public int getIndex () {
    return index;
  }
  
  /**
   * Sets the active Button to the Button with the given index.
   * @param buttonIndex index of Button to become active
   */
  public void setIndex (int buttonIndex) {
    if (buttonIndex >= 0 && buttonIndex < buttons.length) {
      index = buttonIndex;
    }
    for (int i = 0; i < buttons.length; i++) {
      if (buttons[i] != null) {
        remove(buttons[i]);
      }
      if (i != index && buttons[i] != null) {
        buttons[i].setPushed(false);
      }
    }
    if (buttons[index] != null) {
      add(buttons[index]);
    }
    repaint();
  }
  /**
   * Sets the active Button to the Button with the given index and optionally pushes Button.
   * @param buttonIndex index of Button to become active
   * @param push true to push newly active Button
   */
  public void setIndex (int buttonIndex, boolean push) {
    setIndex(buttonIndex);
    if(push) {
      setPushed(true);
    }
  }
  
  /**
   * Pushes or unpushed the presently active button.
   * @param isPushed true to push button, false to unpush it
   */
  public void setPushed(boolean isPushed) {
    if (buttons[index] != null) {
      buttons[index].setPushed(isPushed);
    }
  }
}