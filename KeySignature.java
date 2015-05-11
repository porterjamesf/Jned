import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

// TASK - cut out the conversion to key indicies and use KeyEvent.KEY_LAST as the multiplier for
// modifier flags. With the sparse array, this will be just as space efficient. 

/*
SOME NOTES ON KEYSIGNATURE KEY INDICES AND KEY CODES
The java key codes for various keys a spread through a rather wide range of numbers. Many of the
keys easily available are in a somewhat contiguous block, but some are sparsely spread out. To work
with this, KeySignature has its own system of key codes (referred to throughout this class as key
indices, for the sake of clarity). It maps the java key codes to its own key indices and vice versa
when necessary. To see how this mapping is done, see the convertKeyCode and covertKeyIndex methods
below. With this conversion, the key indices fill a mostly contiguous range of numbers, and these
values can act directly as indices into an array.
In addition, KeySignature keeps track of modifier keys (ctrl, alt, shift). These are represented
with bit flags, creating 8 different values. The consolidated key indices fit into a contiguous
range of 104, so any key stroke can be represented by multiplying the flags by 104 and adding the
key index.
*/

/**
 * KeySignature handles all keyboard input in Jned, as well as managing all of the keyboard
 * shortcut settings. In basic terms, it maps key presses to action numbers, filtering them through
 * the user defined shortcut settings along the way.
 * @author James Porter
 */
public class KeySignature implements KeyListener, ActionListener {
  // GUI layout constants
  public static final int  KEY_ARRAY_SIZE = 256;
  public static final int  KCD_BORDER = 4;
  public static final int  KCD_WIDTH = 256;
  public static final int  CLD_WIDTH = 256;
  public static final int  KCD_ROW_HEIGHT = 24;
  public static final int  KCD_KEYNAME_WIDTH = 124;
  public static final int  KCD_BUTTON_WIDTH = 58;
  
  private Jned jned;
  private LevelArea lvl;
  private KeyShortcuts shortcutsWindow;
  private Nfile config;
  
  private String preset;
  private SparseArrayNode keyIndicesHead;
  private SparseArrayNode actionIndicesHead;
  
  private int modKeyFlags;
  private boolean rDown;
  private boolean dDown;
  private boolean lDown;
  private boolean uDown;
  private boolean backSpace;
  
  // Key change dialog
  private JDialog keyChangeDialog;
  private int actionNumber;
  private int keyChangeIndex;
  private boolean isReplacing;
  private JLabel actionName;
  private JLabel[] keyNames;
  private Button[] removes;
  private Button[] replaces;
  private Button kcdAdd;
  private Button kcdClose;
  
  // Key collision dialog
  private JDialog keyCollisionDialog;
  private JLabel cldKey;
  private JLabel cldOldAction;
  private JLabel cldNewAction;
  private Button cldOk;
  private Button cldCancel;
  private int keyValue;
  private int collidingAction;
  
  private DialogKeyListener dears;
  private ArrayList<KeySetting> settings;
  
  /**
   * Constructs a new KeySigniture.
   * @param jned reference to enclosing Jned instance
   * @param level reference to associated LevelArea instance
   * @param config Nfile wrapping Jned's config file
   * @param frame the JFrame that should own this KeySignature's dialog windows
   */
  public KeySignature (Jned jned, LevelArea level, Nfile config, JFrame frame) {
    this.jned = jned;
    lvl = level;
    this.config = config;
    
    modKeyFlags = 0;
    rDown = false;
    dDown = false;
    lDown = false;
    uDown = false;
    
    keyChangeIndex = -1;
    isReplacing = false;
    settings = new ArrayList<KeySetting>();
    
    initializeSparseArray();
    initializeKeyChangeDialog(frame);
    initializeKeyCollisionDialog(frame);
    shortcutsWindow = null;
  }
  
  /**
   * Sets up a reference link to associated KeyShortcuts instance.
   * @param keyShortcuts the KeyShortcuts instance to register
   */
  public void register(KeyShortcuts keyShortcuts) {
    shortcutsWindow = keyShortcuts;
    shortcutsWindow.setPreset(preset);
  }
  
  /**
   * Adds a KeySetting instance to this KeySignature's list of settings to update with changes.
   * @param keySetting KeySetting to add 
   */
  public void register(KeySetting keySetting) {
    settings.add(keySetting);
  }
  
  private void initializeSparseArray() {
    keyIndicesHead = new SparseArrayNode(null, -1);
    
    preset = config.getAttr2("custom");
    String[] presetData = config.getData(preset).split(";");
    if (shortcutsWindow != null) {
      shortcutsWindow.setPreset(preset);
    }
    for (String datum : presetData) {
      try {
        String[] svals = datum.split(",");
        int[] ivals = new int[svals.length];
        for (int i = 0; i < svals.length; i ++) {
          ivals[i] = Integer.parseInt(svals[i]);
        }
        addNode(keyIndicesHead, ivals);
      } catch (NumberFormatException e) {
        System.err.println("Problem in key config settings: " + datum);
      }
    }
    createReverseArray();
  }
  
  private void createReverseArray() {
    actionIndicesHead = new SparseArrayNode(null, -1);
    
    SparseArrayNode place = keyIndicesHead.next;
    while (place != null) {
      for (int val : place.values) {
        addNode(actionIndicesHead, val, place.key);
      }
      place = place.next;
    }
  }
  
  private void initializeKeyChangeDialog(JFrame fred) {
    keyChangeDialog = new JDialog(fred, "Change Keyboard Shortcut", true);
    keyChangeDialog.getContentPane().setLayout(null);
    keyChangeDialog.getContentPane().setBackground(Colors.BG_COLOR);
    
    actionNumber = 0;
    actionName = new JLabel("", SwingConstants.CENTER);
    actionName.setForeground(Color.BLACK);
    actionName.setBounds(KeySignature.KCD_BORDER, KeySignature.KCD_BORDER, KeySignature.KCD_WIDTH -
        2 * KeySignature.KCD_BORDER, KeySignature.KCD_ROW_HEIGHT);
    keyChangeDialog.add(actionName);
    
    keyNames = new JLabel[0];
    removes = new Button[0];
    replaces = new Button[0];
    
    kcdAdd = new Button(jned, "keySetting#add", -2, KeySignature.KCD_BORDER,
        KeySignature.KCD_BORDER * 2 + KeySignature.KCD_ROW_HEIGHT, KeySignature.KCD_BUTTON_WIDTH,
        KeySignature.KCD_ROW_HEIGHT, true, "Add...");
    kcdClose = new Button(jned, "keySetting#close", -2, KeySignature.KCD_WIDTH / 2 -
        KeySignature.KCD_BUTTON_WIDTH / 2, KeySignature.KCD_BORDER * 3 +
        KeySignature.KCD_ROW_HEIGHT * 2, KeySignature.KCD_BUTTON_WIDTH,
        KeySignature.KCD_ROW_HEIGHT, true, "Close");
    keyChangeDialog.add(kcdAdd);
    keyChangeDialog.add(kcdClose);
    
    setKeyChange(0);
    keyChangeDialog.setLocationRelativeTo(null);
    dears = new DialogKeyListener();
    keyChangeDialog.addKeyListener(dears);
  }
  
  // Sets the key change dialog's contents to the settings for a specific action
  private void setKeyChange (int actionNum) {
    actionNumber = actionNum;
    actionName.setText(getActionText(actionNumber));
    
    for (int i = 0; i < keyNames.length; i++) {
      keyChangeDialog.remove(keyNames[i]);
      keyChangeDialog.remove(removes[i]);
      keyChangeDialog.remove(replaces[i]);
    }
    int ycount = KeySignature.KCD_BORDER * 2 + KeySignature.KCD_ROW_HEIGHT;
    int xcount = KeySignature.KCD_BORDER;
    SparseArrayNode node = findNode(actionIndicesHead, actionNumber);
    if (node.key == actionNumber) {
      keyNames = new JLabel[node.values.size()];
      removes = new Button[node.values.size()];
      replaces = new Button[node.values.size()];
      for (int i = 0; i < node.values.size(); i++) {
        keyNames[i] = new JLabel(getKeyName(node.values.get(i)));
        keyNames[i].setForeground(Color.BLACK);
        keyNames[i].setBounds(xcount, ycount, KeySignature.KCD_KEYNAME_WIDTH,
            KeySignature.KCD_ROW_HEIGHT);
        keyChangeDialog.add(keyNames[i]);
        xcount += KeySignature.KCD_KEYNAME_WIDTH + KeySignature.KCD_BORDER;
        
        removes[i] = new Button(jned, "keySetting#rm," + i, -2, xcount, ycount,
            KeySignature.KCD_BUTTON_WIDTH, KeySignature.KCD_ROW_HEIGHT, true, "Remove");
        keyChangeDialog.add(removes[i]);
        xcount += KeySignature.KCD_BUTTON_WIDTH + KeySignature.KCD_BORDER;
        
        replaces[i] = new Button(jned, "keySetting#rp," + i, -2, xcount, ycount,
            KeySignature.KCD_BUTTON_WIDTH, KeySignature.KCD_ROW_HEIGHT, true, "Replace");
        keyChangeDialog.add(replaces[i]);
        
        ycount += KeySignature.KCD_ROW_HEIGHT + 1;
        xcount = KeySignature.KCD_BORDER;
      }
    }
    ycount += KeySignature.KCD_BORDER - 1;
    kcdAdd.setLocation(xcount, ycount);
    ycount += KeySignature.KCD_ROW_HEIGHT + KeySignature.KCD_BORDER;
    kcdClose.setLocation(KeySignature.KCD_WIDTH / 2 - KeySignature.KCD_BUTTON_WIDTH / 2, ycount);
    
    keyChangeDialog.getContentPane().setPreferredSize(new Dimension(KeySignature.KCD_WIDTH, ycount
        + KeySignature.KCD_ROW_HEIGHT + KeySignature.KCD_BORDER));
    keyChangeDialog.pack();
    keyChangeDialog.repaint();
  }
  
  private void initializeKeyCollisionDialog (JFrame fred) {
    keyCollisionDialog = new JDialog(fred, "Keyboard Shortcut In Use", true);
    keyCollisionDialog.getContentPane().setLayout(null);
    keyCollisionDialog.getContentPane().setBackground(Colors.BG_COLOR);
    
    int ycount = KeySignature.KCD_BORDER;
    cldKey = new JLabel("");
    cldKey.setForeground(Color.BLACK);
    cldKey.setBounds(KeySignature.KCD_BORDER, ycount, KeySignature.CLD_WIDTH - 2 *
        KeySignature.KCD_BORDER, KeySignature.KCD_ROW_HEIGHT);
    ycount += KeySignature.KCD_BORDER + KeySignature.KCD_ROW_HEIGHT;
    
    cldOldAction = new JLabel("");
    cldOldAction.setForeground(Color.BLACK);
    cldOldAction.setBounds(KeySignature.KCD_BORDER, ycount, KeySignature.CLD_WIDTH - 2 *
        KeySignature.KCD_BORDER, KeySignature.KCD_ROW_HEIGHT);
    ycount += KeySignature.KCD_BORDER + KeySignature.KCD_ROW_HEIGHT;
    
    cldNewAction = new JLabel("");
    cldNewAction.setForeground(Color.BLACK);
    cldNewAction.setBounds(KeySignature.KCD_BORDER, ycount, KeySignature.CLD_WIDTH - 2 *
        KeySignature.KCD_BORDER, KeySignature.KCD_ROW_HEIGHT);
    ycount += KeySignature.KCD_BORDER + KeySignature.KCD_ROW_HEIGHT;
    
    cldOk = new Button(jned, "keySetting#cldOk", -2, KeySignature.KCD_BORDER, ycount,
        KeySignature.KCD_BUTTON_WIDTH, KeySignature.KCD_ROW_HEIGHT, true, "OK");
    cldCancel = new Button(jned, "keySetting#cldCancel", -2, KeySignature.CLD_WIDTH -
        KeySignature.KCD_BORDER - KeySignature.KCD_BUTTON_WIDTH, ycount,
        KeySignature.KCD_BUTTON_WIDTH, KeySignature.KCD_ROW_HEIGHT, true, "Cancel");
    ycount += KeySignature.KCD_BORDER + KeySignature.KCD_ROW_HEIGHT;
    
    keyCollisionDialog.add(cldKey);
    keyCollisionDialog.add(cldOldAction);
    keyCollisionDialog.add(cldNewAction);
    keyCollisionDialog.add(cldOk);
    keyCollisionDialog.add(cldCancel);
    
    keyCollisionDialog.setLocationRelativeTo(null);
    keyCollisionDialog.getContentPane().setPreferredSize(new Dimension(KeySignature.CLD_WIDTH,
        ycount));
    keyCollisionDialog.pack();
  }
  
  // Sets the key collision dialog to a certain action and pair of key settings
  private void setKeyCollision(int key, int act1, int act2) {
    cldKey.setText(getKeyName(key) + " is already in use for");
    cldOldAction.setText("'" + getActionText(act1) + "'.");
    cldNewAction.setText("Change to '" + getActionText(act2) + "'?");
    keyCollisionDialog.repaint();
  }
  
  /**
   * Converts current keyboard shortcut settings to String format for saving in the config file.
   * @return String format of keyboard shortcut settings
   */
  public String getKeySettings() {
    String result = "";
    SparseArrayNode place = keyIndicesHead.next;
    while (place != null) {
      result += place.key + ",";
      for (int val : place.values) {
        result += val + ",";
      }
      result = result.substring(0, result.length() - 1) + ";";
      place = place.next;
    }
    return result.substring(0, result.length() - 1);
  }
  
  // Converts from java key code into KeySignature index for the key mapping array
  private int convertKeyCode(int kc) {
    if (kc < 32 || kc > 123) {
      switch (kc) {
        case 8: return 21; // Backspace
        case 10: return 0; // Enter
        case 12: return 1; // Clear
        case 19: return 2; // Pause/break
        case 20: return 3; // Caps lock
        case 27: return 4; // Esc
        case 127: return 5; // Delete
        case 144: return 6; // Num lock
        case 145: return 7; // Scroll lock
        case 154: return 8; // Print screen
        case 155: return 9; // Insert
        case 192: return 10; // `~
        case 222: return 11; // '"
        default: return -1;
      }
    }
    return kc - 20;
  }
  
  // Converts KeySigniture key mapping array index into java key code
  private int convertKeyIndex(int ki) {
    if (ki < 12) {
      switch (ki) {
        case 0: return 10; // Enter
        case 1: return 12; // Clear
        case 2: return 19; // Pause/break
        case 3: return 20; // Caps lock
        case 4: return 27; // Esc
        case 5: return 127; // Delete
        case 6: return 144; // Num lock
        case 7: return 145; // Scroll lock
        case 8: return 154; // Print screen
        case 9: return 155; // Insert
        case 10: return 192; // `~
        case 11: return 222; // '"
        default: return -1;
      }
    }
    if (ki == 21) {
      return 8; // Backspace
    }
    return ki + 20;
  }
  
  /*
  A NOTE ABOUT DIRECTION KEYS
  Jned actions are called after key press events. However, this doActions method is also called
  after key release events, with the boolean argument set to false. This is for the direction keys.
  KeySignature has to keep track of several key combo possibilities, namely the modifier keys and 
  the direction keys (for diagonal directions). The modifier keys never change, so they can be
  handled directly in the key listener methods. The direction keys, however, can be set to different
  actual keys by the user, so those can only be processed here, in doActions (i.e. after the key
  code has been converted to the action number). Consequently, keyReleased has to call this method
  as well, if only to trigger the release of the four direction keys.
  */
  // Calls to Jned to perform actions asssociated with a given KeySignature key index.
  private void doActions(int key, boolean press) {
    SparseArrayNode node = findNode(keyIndicesHead, key);
    if (node.key == key) {
      for (int val : node.values) {
        if (val >= 45 && val <= 48) {
          switch (val) {
            case 45:
              rDown = press;
              if( press) {
                if (dDown) {
                  jned.doActionNumber(49);
                } else {
                  if (uDown) {
                    jned.doActionNumber(52);
                  } else {
                    jned.doActionNumber(45);
                  }
                }
              }
              break;
            case 46:
              dDown = press;
              if (press) {
                if (rDown) {
                  jned.doActionNumber(49);
                } else {
                  if (lDown) {
                    jned.doActionNumber(50);
                  } else {
                    jned.doActionNumber(46);
                  }
                }
              }
              break;
            case 47:
              lDown = press;
              if (press) {
                if (dDown) {
                  jned.doActionNumber(50);
                } else {
                  if (uDown) {
                    jned.doActionNumber(51);
                  } else {
                    jned.doActionNumber(47);
                  }
                }
              }
              break;
            case 48:
              uDown = press;
              if (press) {
                if (rDown) {
                  jned.doActionNumber(52);
                } else {
                  if (lDown) {
                    jned.doActionNumber(51);
                  } else {
                    jned.doActionNumber(48);
                  }
                }
              }
              break;
            default:
          }
        } else {
          if (val == 9) {
            backSpace = press;
          }
          if (press) {
            jned.doActionNumber(val);
          }
        }
      }
    }
  }
  
  public void keyPressed(KeyEvent ke) { 
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_CONTROL:
        modKeyFlags |= 0b100;
        break;
      case KeyEvent.VK_SHIFT:
        modKeyFlags |= 0b010;
        break;
      case KeyEvent.VK_ALT:
        modKeyFlags |= 0b001;
        break;
      default:
        doActions(104 * modKeyFlags + convertKeyCode(ke.getKeyCode()), true);
    }
    jned.repaint();
  }
  
  public void keyReleased(KeyEvent ke) { 
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_CONTROL:
        modKeyFlags &= 0b011;
        break;
      case KeyEvent.VK_SHIFT:
        modKeyFlags &= 0b101;
        break;
      case KeyEvent.VK_ALT:
        modKeyFlags &= 0b110;
        break;
      default:
        doActions(104 * modKeyFlags + convertKeyCode(ke.getKeyCode()), false);
    }
    jned.repaint();
  }
  
  /**
   * Returns whether or not ctrl is currently pushed.
   * @return true if ctrl is pushed, false if it is not
   */
  public boolean isCtrlPushed() {
    return (modKeyFlags & 0b100) > 0;
  }
  
  /**
   * Returns whether or not shift is currently pushed.
   * @return true if shift is pushed, false if it is not
   */
  public boolean isShiftPushed() {
    return (modKeyFlags & 0b010) > 0;
  }
  
  /**
   * Returns whether or not alt is currently pushed.
   * @return true if alt is pushed, false if it is not
   */
  public boolean isAltPushed() {
    return (modKeyFlags & 0b001) > 0;
  }
  
  /**
   * Returns whether or not the backspace key is currently pushed. This key may be a different key
   * than backspace is the settings are configured as such.
   * @return true if backspace is pushed, false if it is not
   */
  public boolean isBackspacePushed() {
    return backSpace;
  }
  
  // NOTE - if adding a new action, update value of Jned.ACTION_COUNT
  /**
   * Returns a String description of the given action number. Action numbers are the same as those
   * used for various purposes in Jned. These descriptions are what appear on KeySetting objects.
   * @param the Jned action number
   */
  public String getActionText(int action) {
    switch (action) {
      // General
      case 0: return "No action";
      case 1: return "tiles mode";
      case 2: return "items mode";
      case 3: return "enemies mode";
      case 4: return "undo";
      case 5: return "redo";
      case 6: return "cut";
      case 7: return "copy";
      case 8: return "paste";
      case 9: return "delete";
      case 10: return "none";
      
      // TILES MODE ONLY
      case 11: return "45 degree incline";
      case 12: return "thin 63 degree incline";
      case 13: return "thin 27 degree incline";
      case 14: return "concave curve";
      case 15: return "half tile";
      case 16: return "thick 63 degree incline";
      case 17: return "thick 27 degree incline";
      case 18: return "convex curve";
      case 19: return "erase/blank tile";
      case 20: return "fill/full tile";
      
      // ITEM MODES ONLY
      case 21: return "player";
      case 22: return "gold";
      case 23: return "bounce block";
      case 24: return "exit door";
      case 25: return "oneway platform";
      case 26: return "normal door";
      case 27: return "locked door";
      case 28: return "trap door";
      case 29: return "launch pad";
      case 30: return "teleporter";
      case 31: return "gauss turret";
      case 32: return "homing launcher";
      case 33: return "mine";
      case 34: return "floor guard";
      case 35: return "thwump";
      case 36: return "zap drone";
      case 37: return "seeker drone";
      case 38: return "laser drone";
      case 39: return "chaingun drone";
      case 40: return "raptors";
      
      // Directions
      case 41: return "tile in lower right";
      case 42: return "tile in lower left";
      case 43: return "tile in upper right";
      case 44: return "tile in upper left";
      case 45: return "right facing";
      case 46: return "down facing";
      case 47: return "left facing";
      case 48: return "up facing";
       case 49: return "down-right facing";
       case 50: return "down-left facing";
       case 51: return "up-left facing";
       case 52: return "up-right facing";
       
      // Behaviors
      case 53: return "surfacefollow CW";
      case 54: return "surfacefollow CCW";
      case 55: return "dumb CW";
      case 56: return "dumb CCW";
      case 57: return "alternating";
      case 58: return "quasi-random";
      case 59: return "no behavior/still";
      
      case 60: return "show triggers";
      case 61: return "hide triggers";
      case 62: return "toggle triggers";
      case 63: return "show drone paths";
      case 64: return "hide drone paths";
      case 65: return "toggle drone paths";
      
      case 66: return "copy level to clipboard";
      case 67: return "paste level from clipboard";
      case 68: return "load level from textbox";
      
      case 69: return "nudge right";
      case 70: return "nudge down";
      case 71: return "nudge left";
      case 72: return "nudge up";
      
      case 73: return "show gridlines";
      case 74: return "hide gridlines";
      case 75: return "toggle gridlines";
      case 76: return "snapping on";
      case 77: return "snapping off";
      case 78: return "toggle snapping";
      case 79: return "show snap points";
      case 80: return "hide snap points";
      case 81: return "toggle snap points";
      
      case 82: return "save";
      case 83: return "save As";
      case 84: return "open";
      
      case 85: return "pop-out text box";
      case 86: return "put text-box below window";
      case 87: return "put text-box on right side";
      
      case 88: return "new";
      case 89: return "select all";
      
      default:
        // Gridline/snap presets
        if (action >= 256) {
          String[] setting = config.getNames(("" + action), 2);
          if (setting.length > 0) {
            return setting[0];
          }
          return "Unknown grid/snap setting";
        } else {
          return "Unknown action";
        }
    }
  }
  
  // Checks for mutually exlusive actions
  private int collide(ArrayList<Integer> vals, int act) {
    int collision = -1;
    for (int value : vals) {
      boolean collide = true;
      if (value == act) {
        collide = false;
      }
      // Tile mode actions won't collide with item mode actions, and vice versa
      if (value >= 11 && value <= 20 || value >= 41 && value <= 44) {
        if(act >= 21 && act <= 40 || act >= 45 && act <= 59 || act >= 69 && act <= 72) {
          collide = false;
        }
      }
      if(value >= 21 && value <= 40 || value >= 45 && value <= 59 || value >= 69 && value <= 72) {
        if(act >= 11 && act <= 20 || act >= 41 && act <= 44) {
          collide = false;
        }
      }
      if (collide) {
        return value;
      }
    }
    return -1;
  }
  
  /**
   * Returns s String description of the keys controlling the given action number.
   * @param Jned action number
   * @return String listing keys that perform the given action
   */
  public String getKeyText(int action) {
    SparseArrayNode node = findNode(actionIndicesHead, action);
    if(node.key != action) {
      return "";
    }
    String result = "";
    for(int i = 0; i < node.values.size(); i++) {
      result = getKeyName(node.values.get(i)) + (i==1?" or ":(i>1?", ":"")) + result;
    }
    return result;
  }
  
  /**
   * Returns a String description of the given key.
   * @param keyIndex the KeySignature index for desired key
   * @return a String describing the key (e.g. ALT Q or Page Up)
   */
  public String getKeyName(int keyIndex) {
    String result = "";
    int mkf = keyIndex/104;
    if((mkf & 0b100) > 0) result += "CTRL ";
    if((mkf & 0b010) > 0) result += "SHIFT ";
    if((mkf & 0b001) > 0) result += "ALT ";
    return result + KeyEvent.getKeyText(convertKeyIndex(keyIndex % 104));
  }
  
  /**
   * Returns a KeyStroke object for the given action number.
   * @param Jned action number
   * @return KeyStroke of the first key combination associated with given action
   */
  public KeyStroke getKeyStroke(int action) {
    SparseArrayNode node = findNode(actionIndicesHead, action);
    if (node.key != action) {
      return null;
    }
    int keyIndex = node.values.get(node.values.size() - 1);
    int mkf = keyIndex / 104;
    int modifiers = 0;
    if ((mkf & 0b100) > 0) {
      modifiers |= InputEvent.CTRL_MASK;
    }
    if ((mkf & 0b010) > 0) {
      modifiers |= InputEvent.SHIFT_MASK;
    }
    if ((mkf & 0b001) > 0) {
      modifiers |= InputEvent.ALT_MASK;
    }
    return KeyStroke.getKeyStroke(convertKeyIndex(keyIndex % 104), modifiers, true);
  }
  
  /**
   * Returns the KeySetting object corresponding to a given action number
   * @param Jned action number
   * @return KeySetting corresponding to given action
   */
  public KeySetting getKeySetting(int action) {
    for (KeySetting ks : settings) {
      if (ks.actionNumber == action) {
        return ks;
      }
    }
    return null;
  }
  
  public void actionPerformed (ActionEvent e) {
    String[] multiCommand = e.getActionCommand().split("#");
    if (multiCommand.length > 1) {
      if (multiCommand[0].equals("keySetting")) {
        push(multiCommand[1]);
      }
    }
  }
  
  /**
   * Performs actions appropriate for a button push with the given action command. Pushing buttons
   * on KeySetting objects and associated Dialogs triggers these actions.
   * @param command action command String of desired action
   */
  public void push(String command) {
    try {
      setKeyChange(Integer.parseInt(command));
      keyChangeDialog.setVisible(true);
    } catch (NumberFormatException e) {
      switch (command) {
        case "add":
          listen(false, keyNames.length);
          break;
        case "close":
          stopListening();
          keyChangeDialog.setVisible(false);
          break;
        case "cldOk":
          removeValue(keyIndicesHead, keyValue, collidingAction);
          completeKeyChange();
        case "cldCancel":
          keyCollisionDialog.setVisible(false);
          break;
        default:
          String[] parts = command.split(",");
          try {
            if (parts[0].equals("rm")) {
              SparseArrayNode node = findNode(actionIndicesHead, actionNumber);
              removeValue(keyIndicesHead, node.values.get(Integer.parseInt(parts[1])),
                  actionNumber);
              updateKeySettings(true);
            }
            if (parts[0].equals("rp")) {
              listen(true, Integer.parseInt(parts[1]));
            }
          } catch (NumberFormatException ex) {}
      }
    }
  }
  
  private void listen(boolean isReplace, int index) {
    for (int i = 0; i < keyNames.length; i++) {
      removes[i].setEnabled(false);
      replaces[i].setEnabled(false);
    }
    kcdAdd.setEnabled(false);
    
    isReplacing = isReplace;
    keyChangeIndex = index;
    dears.listening = true;
    keyChangeDialog.repaint();
  }
  
  private void stopListening() {
    for (int i = 0; i < keyNames.length; i++) {
      removes[i].setEnabled(true);
      replaces[i].setEnabled(true);
    }
    kcdAdd.setEnabled(true);
    
    dears.listening = false;
    keyChangeDialog.repaint();
  }
  
  // TASK - find better mechansim for setting change vs. preset selection. Should be invisible to
  // users
  /**
   * Updates all KeySetting objects, the open key change dialog, and the config file with any
   * changes that have been made to key settings. 
   * @param isChange set to true when a setting has been changed, and to false when a different
   * preset is selected
   */
  protected void updateKeySettings(boolean isChange) {
    if (isChange) {
      config.setAttr2("custom", "custom");
      config.setData(getKeySettings(), "custom");
    }
    initializeSparseArray();
    for (KeySetting setting : settings) {
      setting.refresh();
    }
    setKeyChange(actionNumber);
  }
  
  private void completeKeyChange() {
    if (isReplacing) {
      removeValue(keyIndicesHead, findNode(actionIndicesHead, actionNumber).values.get(
          keyChangeIndex), actionNumber);
    }
    addNode(keyIndicesHead, keyValue, actionNumber);
    updateKeySettings(true);
    stopListening();
  }
  
  // Returns either node with matching key, or node just before where it would be
  private SparseArrayNode findNode(SparseArrayNode head, int key) {
    SparseArrayNode place = head;
    while (place.next != null) {
      if (place.next.key > key) {
        break;
      }
      place = place.next;
    }
    return place;
  }
  
  // Adds a node, or adds values to existing node. values[] contains key in index 0
  private void addNode(SparseArrayNode head, int[] values) {
    if (values.length >= 2) {
      SparseArrayNode last = findNode(head, values[0]);
      if (last.key == values[0]) {
        for(int i = 1; i < values.length; i++) {
          last.values.add(values[i]);
        }
      } else {
        SparseArrayNode newNode = new SparseArrayNode(last, values);
      }
    } else {
      System.err.println("KeySignature.addNode(): Cannot add a node without at least a key and a "
          + "value");
    }
  }
  private void addNode(SparseArrayNode head, int key, int value) {
    int[] vals = {key, value};
    addNode(head, vals);
  }
  
  // Removes value from node, and entire node if it has no more values
  private void removeValue(SparseArrayNode head, int key, int value) {
    SparseArrayNode last = findNode(head, key);
    if (last.key == key) {
      last.values.remove(new Integer(value));
      if (last.values.size() == 0) {
        SparseArrayNode prev = findNode(head, key - 1);
        prev.next = last.next;
      }
    }
  }
  
  public void keyTyped(KeyEvent ke) {}
  
  private class SparseArrayNode {
    protected int key;
    protected ArrayList<Integer> values;
    protected SparseArrayNode next;
    
    public SparseArrayNode (SparseArrayNode last, int key) {
      if (last != null) {
        next = last.next;
        last.next = this;
      } else {
        next = null;
      }
      this.key = key;
      values = new ArrayList<Integer>();
    }
    public SparseArrayNode (SparseArrayNode last, int key, int value) {
      this(last, key);
      values.add(value);
    }
    public SparseArrayNode (SparseArrayNode last, int key, int[] vals) {
      this(last, key);
      for (int val : vals) {
        values.add(val);
      }
    }
    public SparseArrayNode (SparseArrayNode last, int[] vals) {
      this(last, vals[0]);
      for (int i = 1; i < vals.length; i++) {
        values.add(vals[i]);
      }
    }
  }

  private class DialogKeyListener implements KeyListener {
    protected boolean listening = false;
    protected int dialogMKF = 0;
    
    public void keyPressed(KeyEvent ke) { 
      switch (ke.getKeyCode()) {
        case KeyEvent.VK_CONTROL:
          dialogMKF |= 0b100;
          break;
        case KeyEvent.VK_SHIFT:
          dialogMKF |= 0b010;
          break;
        case KeyEvent.VK_ALT:
          dialogMKF |= 0b001;
          break;
        default:
          if (listening) {
            keyValue = 104 * dialogMKF + convertKeyCode(ke.getKeyCode());
            
            // Collision check
            SparseArrayNode node = findNode(keyIndicesHead, keyValue);
            if (node.key == keyValue) {
              collidingAction = collide(node.values, actionNumber);
              if (collidingAction > -1) {
                setKeyCollision(keyValue, collidingAction, actionNumber);
                keyCollisionDialog.setVisible(true);
                break;
              }
            }
            
            completeKeyChange();
          }
      }
    }
    
    public void keyReleased(KeyEvent ke) { 
      switch (ke.getKeyCode()) {
        case KeyEvent.VK_CONTROL:
          dialogMKF &= 0b011;
          break;
        case KeyEvent.VK_SHIFT:
          dialogMKF &= 0b101;
          break;
        case KeyEvent.VK_ALT:
          dialogMKF &= 0b110;
          break;
        default:
      }
    }
    
    public void keyTyped(KeyEvent ke) {}
  }
}