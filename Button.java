import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Custom representation of a Button in the Jned application. These buttons have a hard-coded look
 * and feel designed for Jned, and lack many of the customization features of swing Buttons. They do
 * support highlighting, radio button groups (one is always pushed), action mode (depresses and
 * returns to normal after a moment) and enabled/disabled states.
 * <m>
 * These Buttons may be assigned an image index for use with Jned's ImageBank class. If no image is
 * assigned, the Button will draw itself with a simple border and colors from Jned's Colors class.
 * @author James Porter
 */
public class Button extends JPanel implements MouseListener, ActionListener, Pushable {  
  private Jned jned;
  private ActionListener listener;
  private Timer tim;
  
  // Button state
  private boolean highlight;
  private boolean pushed;
  private boolean enabled;
  
  // Button properties
  private int img;
  private int shftimg;
  private boolean action;
  private boolean radio;
  private ArrayList<Button> group;
  
  /**
   * Label that appears on this Button.
   */
  public String label;
  
  /**
   * Action command that this Button will send to Jned when pushed.
   */
  public String actionCommand;
  
  /**
   * Constructs a new Button with the given action mode, label and shift-Image index.
   * @param jned a reference to enclosing Jned instance
   * @param command action command String, sent to Jned when button is pushed and unpushed
   * @param image index of unpushed image for this button in ImageBank, or -2 for no image
   * @param x the x coordinate 
   * @param y the y coordinate 
   * @param width the width
   * @param height the height
   * @param isAction true when this Button is an action button (pushes and unpushes in one click)
   * @param label Label that appears on this Button
   * @param shiftImage index of unpushed image for this button in ImageBank when the shift key is
   * held down, or -2 for no alternate shift image
   */
  public Button (Jned jned, String command, int image, int x, int y, int width, int heigth,
      boolean isAction, String label, int shiftImage) {
    this.jned = jned;
    listener = jned;
    actionCommand = command;
    img = image;
    setBounds(x, y, width, heigth);
    action = isAction;
    this.label = label;
    shftimg = shiftImage;
    
    highlight = false;
    pushed = false;
    enabled = true;
    
    radio = false;
    group = new ArrayList<Button>();
    tim = new Timer(50,this);
    
    addMouseListener(this);
  }
  /**
   * Constructs a new Button with the given action mode and label.
   * @param jned a reference to enclosing Jned instance
   * @param command action command String, sent to Jned when button is pushed and unpushed
   * @param image index of unpushed image for this button in ImageBank, or -2 for no image
   * @param x the x coordinate 
   * @param y the y coordinate 
   * @param width the width
   * @param height the height
   * @param isAction true when this Button is an action button (pushes and unpushes in one click)
   * @param label Label that appears on this Button
   */
  public Button (Jned jned, String command, int image, int x, int y, int width, int height,
      boolean isAction, String label) {
    this(jned, command, image, x, y, width, height, isAction, label, -2);
  }
  /**
   * Constructs a new Button with the given action listener, action mode and label. The action
   * listener will receive the button's action command on press events, rather than Jned.
   * @param listener this Button's ActionListener
   * @param command action command String, sent to Jned when button is pushed and unpushed
   * @param image index of unpushed image for this button in ImageBank, or -2 for no image
   * @param x the x coordinate 
   * @param y the y coordinate 
   * @param width the width
   * @param height the height
   * @param isAction true when this Button is an action button (pushes and unpushes in one click)
   * @param label Label that appears on this Button
   */
  public Button (ActionListener listener, String command, int image, int x, int y, int width,
      int height, boolean isAction, String label) {
    this(null, command, image, x, y, width, height, isAction, label, -2);
    this.listener = listener;
  }
  /**
   * Constructs a new Button with the given action mode.
   * @param jned a reference to enclosing Jned instance
   * @param command action command String, sent to Jned when button is pushed and unpushed
   * @param image index of unpushed image for this button in ImageBank, or -2 for no image
   * @param x the x coordinate 
   * @param y the y coordinate 
   * @param width the width
   * @param height the height
   * @param isAction true when this Button is an action button (pushes and unpushes in one click)
   */
  public Button (Jned jned, String command, int image, int x, int y, int width, int height,
      boolean isAction) {
    this(jned, command, image, x, y, width, height, isAction, "", -2);
  }
  /**
   * Constructs a new Button with the given label.
   * @param jned a reference to enclosing Jned instance
   * @param command action command String, sent to Jned when button is pushed and unpushed
   * @param image index of unpushed image for this button in ImageBank, or -2 for no image
   * @param x the x coordinate 
   * @param y the y coordinate 
   * @param width the width
   * @param height the height
   * @param label Label that appears on this Button
   */
  public Button (Jned jned, String command, int image, int x, int y, int width, int height,
      String label) {
    this(jned, command, image, x, y, width, height, false, label, -2);
  }
  /**
   * Constructs a new Button with the given shift-Image index.
   * @param jned a reference to enclosing Jned instance
   * @param command action command String, sent to Jned when button is pushed and unpushed
   * @param image index of unpushed image for this button in ImageBank, or -2 for no image
   * @param x the x coordinate 
   * @param y the y coordinate 
   * @param width the width
   * @param height the height
   * @param shiftImage index of unpushed image for this button in ImageBank when the shift key is
   * held down, or -2 for no alternate shift image
   */
  public Button (Jned jned, String command, int image, int x, int y, int width, int height,
      int shiftImage) {
    this(jned, command, image, x, y, width, height, false, "", shiftImage);
  }
  /**
   * Constructs a new Button.
   * @param jned a reference to enclosing Jned instance
   * @param command action command String, sent to Jned when button is pushed and unpushed
   * @param image index of unpushed image for this button in ImageBank, or -2 for no image
   * @param x the x coordinate 
   * @param y the y coordinate 
   * @param width the width
   * @param height the height
   */
  public Button (Jned jned, String command, int image, int x, int y, int width, int height) {
    this(jned, command, image, x, y, width, height, false, "", -2);
  }
  
  /**
   * Adds another Button to this Button's button group. If it is a radio Button, turning this Button
   * on will turn all the other Buttons in its group off.
   * @param button Button to add to this Button's button group
   */
  public void add(Button button) {
    group.add(button);
  }

  /**
   * Returns whether or not this Button is pushed.
   * @return true when Button is pushed, false when it is not
   */
  public boolean isPushed() {
    return pushed;
  }
  
  /**
   * Sets button to pushed or unpushed.
   * @param isPushed true to push the Button, false to unpush it.
   */
  public void setPushed (boolean isPushed) {
    if (isPushed) {
      if (!pushed) {
        pushed = true;
        if (listener != null) {
          listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
              actionCommand));
        }
        for (Button button : group) {
          button.setPushed(false);
        }
        if (action) {
          tim.start();
        }
        repaint();
      }
    } else {
      pushed = false;
    }
  }
  
  /**
   * Returns whether or not this Button is highlighted.
   * @return true when Button is highlighted, false when it is not
   */
  public boolean isHighlighted() {
    return highlight;
  }
  
  /**
   * Returns whether or not this Button is a radio button.
   * @return true when Button is a radio button, false when it is not
   */
  public boolean isRadio() {
    return radio;
  }
  
  /**
   * Sets button to be a radio button or to not be one.
   * @param isRadio true to make into a radio button, false to make it not a radio button
   */
  public void setRadio (boolean isRadio) {
    radio = isRadio;
  }
  
  /**
   * Returns whether or not this Button is enabled.
   * @return true when Button is enabled, false when it is not
   */
  public boolean isEnabled() {
    return enabled;
  }
  
  /**
   * Sets button to enabled or disabled.
   * @param isEnabled true to enable the Button, false to disable it.
   */
  public void setEnabled (boolean isEnabled) {
    enabled = isEnabled;
    repaint();
  }
  
  /**
   * Returns the label of this Button.
   * @return this Button's label
   */
  public String getLabel() {
    return label;
  }
  
  /**
   * Sets the label of this Button.
   * @param String label for this Button to display
   */
  public void setLabel (String label) {
    this.label = label;
    repaint();
  }
  
  public void actionPerformed(ActionEvent e) {
    setPushed(false);
    tim.stop();
    repaint();
  }
  
  public void mouseEntered(MouseEvent me) {
    highlight = enabled;
    repaint();
  }
  
  public void mouseExited(MouseEvent me) {
    highlight = false;
    repaint();
  }
  
  public void mouseReleased(MouseEvent me) {
    if (enabled) {
      if (pushed) {
        if (!radio) {
          setPushed(false);
          if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                actionCommand + "off"));
          }
        }
      } else {
        setPushed(true);
      }
    }
  }
  
  public void paintComponent(Graphics g) {
    setBackground(pushed ? Colors.PUSHED : Colors.UNPUSHED);
    super.paintComponent(g);
    if (pushed) {
      BufferedImage image = (jned == null ? null : jned.img((shftimg > -1 && jned.isShiftPushed()) ?
          shftimg + 1 : img + 1));
      if (image == null) {
        g.setColor(Colors.PUSHED_BORDER);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
      } else {
        g.drawImage(image, 0, 0, Colors.UNPUSHED, null);
      }
    } else {
      BufferedImage image = (jned == null ? null : jned.img((shftimg > -1 && jned.isShiftPushed()) ?
          shftimg : img));
      if (image == null) {
        g.setColor(Colors.UNPUSHED_BORDER);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
      } else {
        g.drawImage(image, 0, 0, Colors.UNPUSHED, null);
      }
    }
    g.setFont(Jned.DEF_FONT);
    paintText(g);
    if (enabled) {
      if (highlight) {
        g.setColor(pushed ? Colors.BUTTON_PDHL : Colors.BUTTON_HL);
        g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
      }
    } else {
      g.setColor(Colors.BUTTON_DIS);
      g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
    }
  }
  public void paintText(Graphics g) {
    g.setColor(pushed ? Colors.PUSHEDTXT : Colors.UNPUSHEDTXT);
    g.drawString(label, getWidth() / 2 - label.length() * Jned.DEF_FONT_XOFF / 2, getHeight() / 2 +
        Jned.DEF_FONT_YOFF);
  }
  
  public void mousePressed(MouseEvent me) {}
  public void mouseClicked(MouseEvent me) {}
}