import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

/**
 * The window that displays all of the keyboard shortcut settings in Jned and the controls to change
 * them.
 * @author James Porter
 */
public class KeyShortcuts extends JPanel implements ActionListener {
  // GUI layout constants
  public static final int WINDOW_WIDTH = 389;
  public static final int WINDOW_HEIGHT = 480;
  public static final int BORDER = 4;
  public static final int ROW_HEIGHT = 24;
  public static final int DIALOG_WIDTH = 384;
  public static final int BUTTON_WIDTH = 64;
  public static final int PRESET_WIDTH = 102;
  
  private JDialog mainDialog;  
  private JDialog saveDialog;
  private JDialog deleteDialog;
  
  private KeySignature keys;
  private Nfile config;
  
  private boolean lockedOut;
  private JComboBox<String> keySelect;
  private JTextField saveText;
  
  private int gridlinesYPos;
  private KeySetting[] gridSnapKeySettings;
  private JLabel snapLabel;
  
  /**
   * Constructs a KeyShortcuts object, initially invisible.
   * @param frame the JFrame that owns all of this KeyShortcuts's JDialogs
   * @param keySignature a reference to the associated KeySignature instance
   * @param config the Nfile wrapping the Jned config file with all the keyboard settings
   */
  public KeyShortcuts (JFrame frame, KeySignature keySignature, Nfile config) {
    this.config = config;
    setBackground(Colors.BG_COLOR);
    setLayout(null);
    keys = keySignature;
    lockedOut = false;
    
    // GUI setup
    // Preset bar
    int ycount = KeyShortcuts.BORDER;
    int xcount = KeyShortcuts.BORDER;
    add(makeJLabel("Preset:", xcount, ycount, 51, KeyShortcuts.ROW_HEIGHT, 1));
    xcount += 51 + KeyShortcuts.BORDER;
    keySelect = new JComboBox<String>(config.getNames("keys", 1));
    keySelect.setBounds(xcount, ycount, KeyShortcuts.BORDER + KeyShortcuts.PRESET_WIDTH,
        KeyShortcuts.ROW_HEIGHT);
    keySelect.addActionListener(this);
    add(keySelect);
    xcount += KeyShortcuts.BORDER * 2 + KeyShortcuts.PRESET_WIDTH;
    add(new Button(this, "keyShortcuts#saveOpen", -2, xcount, ycount, KeyShortcuts.PRESET_WIDTH,
        KeyShortcuts.ROW_HEIGHT, true, "Save"));
    xcount += KeyShortcuts.PRESET_WIDTH + KeyShortcuts.BORDER;
    add(new Button(this, "keyShortcuts#deleteOpen", -2, xcount, ycount, KeyShortcuts.PRESET_WIDTH,
        KeyShortcuts.ROW_HEIGHT, true, "Delete"));
    // Column labels
    ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
    xcount = KeyShortcuts.WINDOW_WIDTH / 2 - KeyShortcuts.BORDER - 30;
    add(makeJLabel("Action:", KeyShortcuts.BORDER, ycount, xcount, KeyShortcuts.ROW_HEIGHT, -1));
    add(makeJLabel("Shortcut:", KeyShortcuts.BORDER + xcount, ycount, xcount,
        KeyShortcuts.ROW_HEIGHT, -1));
    // KeySettings
    ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
    xcount = KeyShortcuts.WINDOW_WIDTH - 2 * KeyShortcuts.BORDER - 8;
    for (int i = 1; i < Jned.ACTION_COUNT; i++) {    
      // Unused actions
      if(i == 30 || i == 40) {
        break;
      }
      // Diagonal directions
      if(i >= 49 && i <= 52) {
        break;
      }
      // Interspersed labels/breaks
      if(i == 10) {
        add(makeJLabel("Left button column", KeyShortcuts.BORDER, ycount, xcount,
            KeyShortcuts.ROW_HEIGHT, -1));
        ycount += KeyShortcuts.ROW_HEIGHT + 1;
      }
      if(i == 41) {
        add(makeJLabel("Right button column", KeyShortcuts.BORDER, ycount, xcount,
            KeyShortcuts.ROW_HEIGHT, -1));
        ycount += KeyShortcuts.ROW_HEIGHT + 1;
      }
      if(i == 60) {
        ycount += KeyShortcuts.ROW_HEIGHT + 1;
      }
      
      add(new KeySetting(keys, KeyShortcuts.BORDER, ycount, xcount, KeyShortcuts.ROW_HEIGHT, i));
      ycount += KeyShortcuts.ROW_HEIGHT + 1;
    }
    add(makeJLabel("Gridline settings", KeyShortcuts.BORDER, ycount, xcount,
        KeyShortcuts.ROW_HEIGHT, -1));
    ycount += KeyShortcuts.ROW_HEIGHT + 1;
    gridlinesYPos = ycount;
    addGridSnapSettings();
    
    // Dialog and scrollbar setup
    mainDialog = new JDialog(frame, "Keyboard Shortcuts");
    JScrollPane sp = new JScrollPane(this);
    sp.setPreferredSize(new Dimension(KeyShortcuts.WINDOW_WIDTH, KeyShortcuts.WINDOW_HEIGHT));
    sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    mainDialog.getContentPane().add(sp);
    mainDialog.pack();
    mainDialog.setLocationRelativeTo(null);
    mainDialog.setResizable(false);
    
    initializeSaveDialog(frame);
    initializeDeleteDialog(frame);
    keys.register(this);
  }
  private JLabel makeJLabel(String txt, int xpos, int ypos, int wid, int hei, int align) {
    JLabel lbl = new JLabel(txt, (align == -1 ? SwingConstants.LEFT : (align == 1 ?
        SwingConstants.RIGHT : SwingConstants.CENTER)));
    lbl.setBounds(xpos, ypos, wid, hei);
    lbl.setForeground(Color.BLACK);
    return lbl;
  }
  
  /**
   * Reads the gridlines and snap point keyboard settings and updates them in the KeyShortcuts
   * window. Since these have variable number and names, they are handled differently and separately
   * from the other KeySettings.
   */
  public void addGridSnapSettings() {
    if (gridSnapKeySettings != null) {
      for (KeySetting ks : gridSnapKeySettings) {
        remove(ks);
      }
      remove(snapLabel);
    }
    String[] gridSettings = config.getNames("grid", 1);
    String[] snapSettings = config.getNames("snap", 1);
    gridSnapKeySettings = new KeySetting[gridSettings.length + snapSettings.length];
    
    int ycount = gridlinesYPos;
    int xcount = KeyShortcuts.WINDOW_WIDTH - 2 * KeyShortcuts.BORDER - 8;
    for (int i = 0; i < gridSettings.length; i++) {
      try {
        gridSnapKeySettings[i] = new KeySetting(keys, KeyShortcuts.BORDER, ycount, xcount,
            KeyShortcuts.ROW_HEIGHT, Integer.parseInt(config.getAttr2(gridSettings[i])));
        add(gridSnapKeySettings[i]);
        ycount += KeyShortcuts.ROW_HEIGHT + 1;
      } catch (NumberFormatException e) {}
    }
    snapLabel = makeJLabel("Snap settings", KeyShortcuts.BORDER, ycount, xcount,
        KeyShortcuts.ROW_HEIGHT, -1);
    add(snapLabel);
    ycount += KeyShortcuts.ROW_HEIGHT + 1;
    for (int i = 0; i < snapSettings.length; i++) {
      try {
        gridSnapKeySettings[i + gridSettings.length] = new KeySetting(keys, KeyShortcuts.BORDER,
            ycount, xcount, KeyShortcuts.ROW_HEIGHT, Integer.parseInt(config.getAttr2(
            snapSettings[i])));
        add(gridSnapKeySettings[i + gridSettings.length]);
        ycount += KeyShortcuts.ROW_HEIGHT + 1;
      } catch (NumberFormatException e) {}
    }
    
    setPreferredSize(new Dimension(KeyShortcuts.WINDOW_WIDTH, ycount));
  }
  
  private void initializeSaveDialog(JFrame frame) {
    saveDialog = new JDialog(frame, "Save Key Shortcuts Preset");
    saveDialog.getContentPane().setLayout(null);
    saveDialog.getContentPane().setBackground(Colors.BG_COLOR);
    
    int ycount = KeyShortcuts.BORDER;
    saveDialog.add(makeJLabel("Type in a name for this preset:", KeyShortcuts.BORDER, ycount,
        KeyShortcuts.DIALOG_WIDTH - 2 * KeyShortcuts.BORDER, KeyShortcuts.ROW_HEIGHT, -1));
    ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
    saveText = new JTextField();
    saveText.setFont(Jned.BOX_FONT);
    saveText.setHorizontalAlignment(JTextField.LEFT);
    saveText.setBounds(KeyShortcuts.BORDER, ycount, KeyShortcuts.DIALOG_WIDTH - 2 *
        KeyShortcuts.BORDER, KeyShortcuts.ROW_HEIGHT);
    saveText.addActionListener(this);
    saveDialog.add(saveText);
    ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
    saveDialog.add(new Button(this, "keyShortcuts#save", -2, KeyShortcuts.BORDER, ycount,
        KeyShortcuts.BUTTON_WIDTH, KeyShortcuts.ROW_HEIGHT, true, "Save"));
    saveDialog.add(new Button(this, "keyShortcuts#saveCancel", -2, KeyShortcuts.DIALOG_WIDTH -
        KeyShortcuts.BUTTON_WIDTH - KeyShortcuts.BORDER, ycount, KeyShortcuts.BUTTON_WIDTH,
        KeyShortcuts.ROW_HEIGHT, true, "Cancel"));
    
    ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
    saveDialog.setLocationRelativeTo(null);
    saveDialog.getContentPane().setPreferredSize(new Dimension(KeyShortcuts.DIALOG_WIDTH, ycount));
    saveDialog.pack();
  }
  
  private void initializeDeleteDialog(JFrame frame) {
    deleteDialog = new JDialog(frame, "Delete Key Shortcuts Preset");
    deleteDialog.getContentPane().setLayout(null);
    deleteDialog.getContentPane().setBackground(Colors.BG_COLOR);
    
    int ycount = KeyShortcuts.BORDER;
    deleteDialog.add(makeJLabel("Are you sure you want to delete this preset?", KeyShortcuts.BORDER,
        ycount, KeyShortcuts.DIALOG_WIDTH - 2 * KeyShortcuts.BORDER, KeyShortcuts.ROW_HEIGHT, 0));
    ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
    deleteDialog.add(new Button(this, "keyShortcuts#delete", -2, KeyShortcuts.BORDER, ycount,
        KeyShortcuts.BUTTON_WIDTH, KeyShortcuts.ROW_HEIGHT, true, "Delete"));
    deleteDialog.add(new Button(this, "keyShortcuts#deleteCancel", -2, KeyShortcuts.DIALOG_WIDTH -
        KeyShortcuts.BUTTON_WIDTH - KeyShortcuts.BORDER, ycount, KeyShortcuts.BUTTON_WIDTH,
        KeyShortcuts.ROW_HEIGHT, true, "Cancel"));
    
    ycount += KeyShortcuts.BORDER + KeyShortcuts.ROW_HEIGHT;
    deleteDialog.setLocationRelativeTo(null);
    deleteDialog.getContentPane().setPreferredSize(new Dimension(KeyShortcuts.DIALOG_WIDTH,
        ycount));
    deleteDialog.pack();
  }
  
  /**
   * Sets the selected item in the Preset list. This method only changes the displayed item; it does
   * not actually load the new preset settings.
   * @param presetName name of the desired keyboard settings preset
   */
  public void setPreset(String presetName) {
    // A lock-out is necessary to avoid a loop. Clicking on the comboBox creates an event that
    // triggers changing the settings. However, setting it via code creates the same event, which
    // is undesirable when merely changing the selected name during initialization or saving.
    lockedOut = true;
    keySelect.setSelectedItem(presetName);
    lockedOut = false;
  }
  
  /**
   * Opens the KeyShortcuts dialog.
   */
  public void open() {
    mainDialog.setVisible(true);
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getSource().equals(saveText)) {
      push("save");
      return;
    }
    if (e.getSource().equals(keySelect)) {
      if (!lockedOut) {
        config.setAttr2(keySelect.getSelectedItem().toString(), "custom");
        keys.updateKeySettings(false);
      }
      return;
    }
    String[] multiCommand = e.getActionCommand().split("#");
    if (multiCommand.length > 1) {
      if (multiCommand[0].equals("keyShortcuts")) {
        push(multiCommand[1]);
      }
    }
  }
  
  /**
   * Performs actions appropriate for a button push with the given action command. Pushing buttons
   * related to saving or deleting presets triggers these actions.
   * @param command action command String of desired action
   */
  public void push(String com) {
    switch (com) {
      case "saveOpen":
        saveDialog.setVisible(true);
        break;
      case "save":
        String selName = saveText.getText();
        config.writeNew(selName, "keys", "", keys.getKeySettings());
        keySelect.addItem(selName);
        setPreset(selName);
        config.setAttr2(selName, "custom");
        // fall through
      case "saveCancel":
        saveDialog.setVisible(false);
        break;
      case "deleteOpen":
        deleteDialog.setVisible(true);
        break;
      case "delete":
        String selected = keySelect.getSelectedItem().toString();
        if (!selected.equals("default") && !selected.equals("custom")) {
          config.delete(selected);
          keySelect.removeItem(selected);
        }
        // fall through
      case "deleteCancel":
        deleteDialog.setVisible(false);
        break;
        default:
    }
  }
}