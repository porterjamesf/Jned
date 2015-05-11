import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

/**
 * A small Panel displaying one action and the associated key shortcut(s), as well as a Button that
 * will open a dialog to change the settings. Each KeySetting is permanently associated with its
 * action number, but can be refreshed to show changes in key shortcuts or the text description of
 * the action.
 * Additionally, a KeySetting instance can be tied to a JMenuItem (referred to as its 'soulmate').
 * This JMenuItem will have its displayed keyboard shortcut accelerator changed to match every time
 * the KeySetting is refreshed.
 * @author James Porter
 */
public class KeySetting extends JPanel {
  private KeySignature keys;
  protected int actionNumber;
  private JLabel actionText;
  private JLabel keyText;
  private JMenuItem soulmate;
  
  /**
   * Constructs a new KeySetting for the given Jned action with the given dimensions.
   * @param keySignature a reference to the enclosing KeySignature instance
   * @param x the x position
   * @param y the y position
   * @param width the width
   * @param height the height
   * @param action the Jned action number for this KeySetting
   */
  public KeySetting(KeySignature keySignature, int x, int y, int width, int height, int action) {
    keys = keySignature;
    keys.register(this);
    
    setLayout(null);
    setBounds(x, y, width, height);
    setBackground(Colors.PANEL_COLOR);
    
    actionNumber = action;
    actionText = new JLabel(keys.getActionText(actionNumber), SwingConstants.LEFT);
    actionText.setBounds(4, 0, width / 2 - 34, height);
    actionText.setForeground(Color.BLACK);
    add(actionText);
    keyText = new JLabel(keys.getKeyText(actionNumber), SwingConstants.LEFT);
    keyText.setBounds(width / 2 - 26, 0, width / 2 - 34, height);
    keyText.setForeground(Color.BLACK);
    add(keyText);
    add(new Button(keySignature, "keySetting#" + actionNumber, -2, width - 52, 2, 50, height - 4,
        true, "Change"));
        
    soulmate = null;
  }
  
  /**
   * Updates this KeySetting with any changes to key settings or action text. Also, updates the
   * associated JMenuItem, if any.
   */
  public void refresh() {
    actionText.setText(keys.getActionText(actionNumber));
    keyText.setText(keys.getKeyText(actionNumber));
    repaint();
    
    if(soulmate != null) {
      KeyStroke tap = keys.getKeyStroke(actionNumber);
      soulmate.setAccelerator(tap);
      soulmate.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(tap, "none");
    }
  }
  
  /**
   * Pairs this KeySetting with a JMenuItem.
   * @param menuItem the JMenuItem to pair with this KeySetting
   */
  public void setSoulmate(JMenuItem menuItem) {
    soulmate = menuItem;
    refresh();
  }
  
  /**
   * Returns the JMenuItem paired with this KeySetting.
   * @return this KeySetting's associated JMenuItem, or null if there is none
   */
  public JMenuItem getSoulMate() {
    return soulmate;
  }
}