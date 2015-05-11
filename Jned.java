import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

/**
 * Jned
 * @author James Porter
 */
public class Jned extends JPanel implements ActionListener, MouseListener {
  // Number of actions
  public static final int ACTION_COUNT = 90;

  // Font constants
  public static Font DEF_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
  public static Font BOX_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
  public static final int DEF_FONT_XOFF = 7;
  public static final int DEF_FONT_YOFF = 4;
  public static final int LEFT = -1;
  public static final int CENTER = 0;
  public static final int RIGHT = 1;
  
  // GUI spacing constants
  public static final int BORDER = 4;
  public static final int SHORT_BUTTON_HT = 24;
  public static final int TALL_BUTTON = 51;
  public static final int LVL_SQUARE = 24;
  public static final int LVL_AREA_HT = 25 * Jned.LVL_SQUARE; // 23 tiles + 2 border tiles
  public static final int LVL_AREA_WD = 33 * Jned.LVL_SQUARE; // 31 tiles + 2 border tiles
  public static final int TXT_ED_HT = 211;
  public static final int TOTAL_WD = Jned.BORDER * 4 + Jned.TALL_BUTTON * 2 + Jned.LVL_AREA_WD;
  public static final int CONFIRM_WINDOW = 384;
  public static final int DOWN_ARROW_WD = 20;
  
  private Pushable[] buttons; // Indices match action numbers corresponding to buttons
  private MultiButton[] mbuttons; // 0-9 is left column, 10-19 is right column
  
  // Frame stuff
  private JFrame frame;
  private int heightWithoutTextBox;
  private int widthWithoutTextBox;
  
  // File objects
  private FileChooser fileChooser;
  public String lvlName;
  public String lvlAuthor;
  public String lvlGenre;
  public boolean savedAs; // True when level has previously been saved under present title
  private Nfile config;
  private String userlevels; // userlevels is not kept with a wrapping Nfile object so that the
      // file can be updated while Jned is open, and so that changes will be reflected each time
      // a level is opened from the file.
  
  // Grid and snap setting objects
  private JDialog gridSaveDialog;
  private JDialog gridDeleteDialog;
  private JDialog snapSaveDialog;
  private JDialog snapDeleteDialog;
  private JMenu mGridSettings;
  private JMenu mSnapSettings;
  private DropPanel gridDropPanel;
  private DropPanel snapDropPanel;
  private Overlay[] gridOverlays;
  private Overlay snapOverlay;
  private JLabel lGridDoubleLine;
  private JTextField tfGridDoubleLine;
  private JTextField tfGridSaveName;
  private JTextField tfSnapSaveName;
  private JComboBox<String> gridSelect;
  private JComboBox<String> snapSelect;
  
  // Button sub-group memory variables
  private int tileDirection;
  private int itemDirection; // 8 values: 0,2,3,4=right,down,left,up 1,3,5,7=diagonals
  private int droneBehavior;
  private int overlayIndex;
  
  // Other large single-instance application objects
  private ImageBank imgBank;
  private KeySignature keys;
  private KeyShortcuts keyShortcuts;
  private LevelArea lvl;
  private TextBox tbox;
  private History hist;
  
  public static final String BLANK_LEVEL = "000000000000000000000000000000000000000000000000000000"
      + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
      + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
      + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
      + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
      + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
      + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
      + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
      + "000000000000000000000000000000000000|";

  /**
   * Create an instance of Jned, which itself creates an instance of all other large
   * single-instance application objects (ImageBank, KeySigniture, KeyShortcuts, LevelArea,
   * TextBox, History). 
   * @param frame the JFrame that encloses this instance of Jned
   */
  public Jned (JFrame frame) {
    super();
    this.frame = frame;
    
    setLayout(null);
    setBackground(Colors.BG_COLOR);
    addMouseListener(this);
    
    imgBank = new ImageBank();
    config = new Nfile("config.txt");
    userlevels = config.getData("fpath");
    fileChooser = new FileChooser(this, frame, config, userlevels);
    hist = new History();
    keys = new KeySignature(this, lvl, config, frame);
    addKeyListener(keys);
    keyShortcuts = new KeyShortcuts(frame, keys, config);
    
    tileDirection = 0;
    itemDirection = 0;
    droneBehavior = 1;
    
    savedAs = false;
    lvlName = "";
    lvlAuthor = "";
    lvlGenre = "";
    frame.setTitle("New level");
    
    buttons = new Pushable[Jned.ACTION_COUNT];
    
    // GUI SET-UP
    
    // TOP ROW
    int ycount = Jned.BORDER;
    int xcount = Jned.BORDER;
    
    // Tiles button
    Button tiles = new Button(this, "tiles", -2, xcount, ycount, 2 * Jned.TALL_BUTTON +
        Jned.BORDER, Jned.SHORT_BUTTON_HT, "TILES");
    buttons[1] = tiles;
    add(tiles);
    
    // Gridlines
    xcount += 2 * Jned.TALL_BUTTON + 2 * Jned.BORDER;
    Button gridtoggle = new Button(this, "gridtoggle", -2, xcount, ycount, Jned.TALL_BUTTON *
        2 + Jned.BORDER - Jned.DOWN_ARROW_WD, Jned.SHORT_BUTTON_HT, "Gridlines");
    xcount += Jned.TALL_BUTTON * 2 + Jned.BORDER - Jned.DOWN_ARROW_WD;
    Button gridlines = new Button(this, "gridlines", ImageBank.BT_DOWN_ARROW, xcount, ycount,
        Jned.DOWN_ARROW_WD, Jned.SHORT_BUTTON_HT);
    xcount += Jned.DOWN_ARROW_WD + Jned.BORDER;
      // Presets
      gridSelect = new JComboBox<String>(config.getNames("grid", 1));
      gridSelect.setBounds(Jned.TALL_BUTTON + Jned.BORDER, ycount, Jned.BORDER + 3 *
          Jned.TALL_BUTTON, Jned.SHORT_BUTTON_HT);
      gridSelect.addActionListener(this);
      JLabel grsltxt = makeJLabel("Preset:", 0, ycount, Jned.TALL_BUTTON + Jned.BORDER - 3,
          Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
      Button grsave = new Button(this, "grsave", -2, Jned.BORDER, ycount, 2 * Jned.TALL_BUTTON,
          Jned.SHORT_BUTTON_HT, true, "Save");
      Button grdelete = new Button(this, "grdelete", -2, 2 * Jned.BORDER + 2 * Jned.TALL_BUTTON,
          ycount, 2 * Jned.TALL_BUTTON, Jned.SHORT_BUTTON_HT, true, "Delete");
      // Primary/secondary/tertiary buttons
      int tbw = (4 * Jned.TALL_BUTTON - Jned.BORDER) / 3;
      ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
      Button[] grpst = new Button[3];
      grpst[0] = new Button(this, "grpri", -2, Jned.BORDER, ycount, tbw, Jned.SHORT_BUTTON_HT,
          "PRIMARY");
      grpst[1] = new Button(this, "grsec", -2, 2 * Jned.BORDER + tbw, ycount, tbw + 2,
          Jned.SHORT_BUTTON_HT, "SECONDARY");
      grpst[2] = new Button(this, "grter", -2, 3 * Jned.BORDER + 2 * tbw + 2, ycount, tbw,
          Jned.SHORT_BUTTON_HT, "TERTIARY");
      makeGroup(grpst, true);
      // Spacing
      ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
      JLabel grspacing = makeJLabel("Spacing:", Jned.BORDER, ycount, tbw + Jned.BORDER - 4,
          Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      JLabel grspcx = makeJLabel("x", 2 * Jned.BORDER + tbw, ycount, Jned.SHORT_BUTTON_HT / 2 - 4,
          Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      JTextField grspcxtxt = new JTextField();
      grspcxtxt.setFont(Jned.BOX_FONT);
      grspcxtxt.setHorizontalAlignment(JTextField.RIGHT);
      grspcxtxt.setBounds(2 * Jned.BORDER + tbw + Jned.SHORT_BUTTON_HT / 2, ycount, tbw + 1 -
          Jned.SHORT_BUTTON_HT / 2, Jned.SHORT_BUTTON_HT);
      JLabel grspcy = makeJLabel("y", 3 * Jned.BORDER + 2 * tbw + 1, ycount, Jned.SHORT_BUTTON_HT
          / 2 - 4, Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      JTextField grspcytxt = new JTextField();
      grspcytxt.setFont(Jned.BOX_FONT);
      grspcytxt.setHorizontalAlignment(JTextField.RIGHT);
      grspcytxt.setBounds(3 * Jned.BORDER + 2 * tbw + 1 + Jned.SHORT_BUTTON_HT / 2, ycount, tbw +
          1 - Jned.SHORT_BUTTON_HT / 2, Jned.SHORT_BUTTON_HT);
      // Offset
      ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
      JLabel groffset = makeJLabel("Offset:", Jned.BORDER, ycount, tbw + Jned.BORDER - 4,
          Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      JLabel groffx = makeJLabel("x", 2 * Jned.BORDER + tbw, ycount, Jned.SHORT_BUTTON_HT / 2 - 4,
          Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      JTextField groffxtxt = new JTextField();
      groffxtxt.setFont(Jned.BOX_FONT);
      groffxtxt.setHorizontalAlignment(JTextField.RIGHT);
      groffxtxt.setBounds(2 * Jned.BORDER + tbw + Jned.SHORT_BUTTON_HT / 2, ycount, tbw + 1 -
          Jned.SHORT_BUTTON_HT / 2, Jned.SHORT_BUTTON_HT);
      JLabel groffy = makeJLabel("y", 3 * Jned.BORDER + 2 * tbw + 1, ycount, Jned.SHORT_BUTTON_HT
          / 2 - 4, Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      JTextField groffytxt = new JTextField();
      groffytxt.setFont(Jned.BOX_FONT);
      groffytxt.setHorizontalAlignment(JTextField.RIGHT);
      groffytxt.setBounds(3 * Jned.BORDER + 2 * tbw + 1 + Jned.SHORT_BUTTON_HT / 2, ycount, tbw +
          1 - Jned.SHORT_BUTTON_HT / 2, Jned.SHORT_BUTTON_HT);
      // Symmetry
      ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
      String[] gridSymmetries = {"None", "Left/Right", "Top/Bottom", "Quadrants"};
      JComboBox<String> gridSymm = new JComboBox<String>(gridSymmetries);
      gridSymm.setBounds(tbw + 2 * Jned.BORDER, ycount, Jned.BORDER + 2 + 2 * tbw,
          Jned.SHORT_BUTTON_HT);
      JLabel grsymtxt = makeJLabel("Symmetry:", Jned.BORDER, ycount, tbw + Jned.BORDER - 4,
          Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      // Single/Double
      ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
      JLabel grsingle = makeJLabel("Single", Jned.BORDER, ycount, 2 * Jned.TALL_BUTTON -
          Jned.SHORT_BUTTON_HT - 4, Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      Button[] singdub = new Button[2];
      singdub[0] = new Button(this, "grsinglebt", -2, Jned.BORDER + 2 * Jned.TALL_BUTTON -
          Jned.SHORT_BUTTON_HT, ycount, Jned.SHORT_BUTTON_HT, Jned.SHORT_BUTTON_HT);
      JLabel grdouble = makeJLabel("Double", 2 * Jned.BORDER + 2 * Jned.TALL_BUTTON, ycount, 2 *
          Jned.TALL_BUTTON - Jned.SHORT_BUTTON_HT - 4, Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      singdub[1] = new Button(this, "grdoublebt", -2, 2 * Jned.BORDER + 4 * Jned.TALL_BUTTON -
          Jned.SHORT_BUTTON_HT, ycount, Jned.SHORT_BUTTON_HT, Jned.SHORT_BUTTON_HT);
      makeGroup(singdub, true);
      // On/off, double spacing
      ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
      JLabel gronofftxt = makeJLabel("On/off:", Jned.BORDER, ycount, 2 * Jned.TALL_BUTTON -
          Jned.SHORT_BUTTON_HT - 4, Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      Button gronoff = new Button(this, "gronoff", -2, Jned.BORDER + 2 * Jned.TALL_BUTTON -
          Jned.SHORT_BUTTON_HT, ycount, Jned.SHORT_BUTTON_HT, Jned.SHORT_BUTTON_HT);
      lGridDoubleLine = makeJLabel("Spacing", 2 * Jned.BORDER + 2 * Jned.TALL_BUTTON, ycount,
          Jned.TALL_BUTTON + 2 * Jned.BORDER - 4, Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      lGridDoubleLine.setVisible(false);
      tfGridDoubleLine = new JTextField();
      tfGridDoubleLine.setFont(Jned.BOX_FONT);
      tfGridDoubleLine.setHorizontalAlignment(JTextField.RIGHT);
      tfGridDoubleLine.setBounds(4 * Jned.BORDER + 3 * Jned.TALL_BUTTON, ycount, Jned.TALL_BUTTON
          - 2 * Jned.BORDER, Jned.SHORT_BUTTON_HT);
      tfGridDoubleLine.setVisible(false);
     gridDropPanel = new DropPanel(gridtoggle.getX(), gridtoggle.getY() + Jned.SHORT_BUTTON_HT, 3
        * Jned.BORDER + 4 * Jned.TALL_BUTTON, ycount + Jned.SHORT_BUTTON_HT + Jned.BORDER, gridlines);
     gridDropPanel.add(grsltxt);
     gridDropPanel.add(gridSelect);
     gridDropPanel.add(grsave);
     gridDropPanel.add(grdelete);
     gridDropPanel.add(grpst[0]);
     gridDropPanel.add(grpst[1]);
     gridDropPanel.add(grpst[2]);
     gridDropPanel.add(grspacing);
     gridDropPanel.add(grspcx);
     gridDropPanel.add(grspcxtxt);
     gridDropPanel.add(grspcy);
     gridDropPanel.add(grspcytxt);
     gridDropPanel.add(groffset);
     gridDropPanel.add(groffx);
     gridDropPanel.add(groffxtxt);
     gridDropPanel.add(groffy);
     gridDropPanel.add(groffytxt);
     gridDropPanel.add(grsymtxt);
     gridDropPanel.add(gridSymm);
     gridDropPanel.add(grsingle);
     gridDropPanel.add(singdub[0]);
     gridDropPanel.add(grdouble);
     gridDropPanel.add(singdub[1]);
     gridDropPanel.add(gronofftxt);
     gridDropPanel.add(gronoff);
     gridDropPanel.add(lGridDoubleLine);
     gridDropPanel.add(tfGridDoubleLine);
    gridOverlays = new Overlay[3];
    gridOverlays[0] = new Overlay(grspcxtxt, grspcytxt, groffxtxt, groffytxt, gridSymm,
        singdub[0], singdub[1], tfGridDoubleLine, gronoff, true, Jned.LVL_AREA_WD - 2 *
        Jned.LVL_SQUARE, Jned.LVL_AREA_HT - 2 * Jned.LVL_SQUARE, this);
    gridOverlays[1] = new Overlay(grspcxtxt, grspcytxt, groffxtxt, groffytxt, gridSymm,
        singdub[0], singdub[1], tfGridDoubleLine, gronoff, false, Jned.LVL_AREA_WD - 2 *
        Jned.LVL_SQUARE, Jned.LVL_AREA_HT - 2 * Jned.LVL_SQUARE, this);
    gridOverlays[2] = new Overlay(grspcxtxt, grspcytxt, groffxtxt, groffytxt, gridSymm,
        singdub[0], singdub[1], tfGridDoubleLine, gronoff, false, Jned.LVL_AREA_WD - 2 *
        Jned.LVL_SQUARE, Jned.LVL_AREA_HT - 2 * Jned.LVL_SQUARE, this);
      singdub[0].setPushed(true);
      gronoff.setPushed(true);
      grpst[0].setPushed(true);
     // Save dialog
     gridSaveDialog = new JDialog(frame, "Save Gridlines Preset");
     gridSaveDialog.getContentPane().setLayout(null);
     gridSaveDialog.getContentPane().setBackground(Colors.BG_COLOR);
     ycount = Jned.BORDER;
      JLabel grSaveConf = new JLabel("Type a name for this grid setting:");
       grSaveConf.setForeground(Color.BLACK);
       grSaveConf.setBounds(Jned.BORDER, ycount, Jned.CONFIRM_WINDOW - 2 * Jned.BORDER,
          Jned.SHORT_BUTTON_HT);
     gridSaveDialog.add(grSaveConf);
     ycount += Jned.BORDER + Jned.SHORT_BUTTON_HT;
      tfGridSaveName = new JTextField();
       tfGridSaveName.setFont(Jned.BOX_FONT);
       tfGridSaveName.setHorizontalAlignment(JTextField.LEFT);
       tfGridSaveName.setBounds(Jned.BORDER, ycount, Jned.CONFIRM_WINDOW - 2 * Jned.BORDER,
          Jned.SHORT_BUTTON_HT);
       tfGridSaveName.addActionListener(this);
     gridSaveDialog.add(tfGridSaveName);
     ycount += Jned.BORDER + Jned.SHORT_BUTTON_HT;
     Button grSaveSave = new Button(this, "grsavesave", -2, Jned.BORDER, ycount, 2 *
        Jned.TALL_BUTTON + Jned.BORDER, Jned.SHORT_BUTTON_HT, true, "Save");
     Button grSaveCancel = new Button(this, "grsavecancel", -2, Jned.CONFIRM_WINDOW - 2 *
        Jned.BORDER - 2 * Jned.TALL_BUTTON, ycount, 2 * Jned.TALL_BUTTON + Jned.BORDER,
        Jned.SHORT_BUTTON_HT, true, "Cancel");
     gridSaveDialog.add(grSaveSave);
     gridSaveDialog.add(grSaveCancel);
     gridSaveDialog.setLocationRelativeTo(null);
     gridSaveDialog.getContentPane().setPreferredSize(new Dimension(Jned.CONFIRM_WINDOW, ycount +
        Jned.BORDER + Jned.SHORT_BUTTON_HT));
     gridSaveDialog.pack();
     // Delete dialog
     gridDeleteDialog = new JDialog(frame, "Delete Gridlines Preset");
     gridDeleteDialog.getContentPane().setLayout(null);
     gridDeleteDialog.getContentPane().setBackground(Colors.BG_COLOR);
     ycount = Jned.BORDER;
      JLabel grDeleteConf = new JLabel("Are you sure you want to delete this grid setting?");
       grDeleteConf.setForeground(Color.BLACK);
       grDeleteConf.setBounds(Jned.BORDER, ycount, Jned.CONFIRM_WINDOW - 2 * Jned.BORDER,
          Jned.SHORT_BUTTON_HT);
     gridDeleteDialog.add(grDeleteConf);
     ycount += Jned.BORDER + Jned.SHORT_BUTTON_HT;
     Button grDeleteDelete = new Button(this, "grdeletedelete", -2, Jned.BORDER, ycount, 2 *
        Jned.TALL_BUTTON + Jned.BORDER, Jned.SHORT_BUTTON_HT, true, "Delete");
     Button grDeleteCancel = new Button(this, "grdeletecancel", -2, Jned.CONFIRM_WINDOW - 2 *
        Jned.BORDER - 2 * Jned.TALL_BUTTON, ycount, 2 * Jned.TALL_BUTTON + Jned.BORDER,
        Jned.SHORT_BUTTON_HT, true, "Cancel");
     gridDeleteDialog.add(grDeleteDelete);
     gridDeleteDialog.add(grDeleteCancel);
     gridDeleteDialog.setLocationRelativeTo(null);
     gridDeleteDialog.getContentPane().setPreferredSize(new Dimension(Jned.CONFIRM_WINDOW, ycount
        + Jned.BORDER + Jned.SHORT_BUTTON_HT));
     gridDeleteDialog.pack();
    overlayIndex = 0;
    add(gridDropPanel);
    buttons[73] = gridtoggle;
    add(gridlines);
    add(gridtoggle);
    
    gridSelect.setSelectedItem("classic l");
    loadGridLines("classic l");
    
    // Snapping
    ycount = Jned.BORDER;
    Button snaptoggle = new Button(this, "snaptoggle", -2, xcount, ycount, Jned.TALL_BUTTON * 2 +
        Jned.BORDER - Jned.DOWN_ARROW_WD, Jned.SHORT_BUTTON_HT, "Snapping");
    xcount += Jned.TALL_BUTTON * 2 + Jned.BORDER - Jned.DOWN_ARROW_WD;
    Button snapping = new Button(this, "snapping", ImageBank.BT_DOWN_ARROW, xcount, ycount,
        Jned.DOWN_ARROW_WD, Jned.SHORT_BUTTON_HT);
    xcount += Jned.DOWN_ARROW_WD + Jned.BORDER;
      snapSelect = new JComboBox<String>(config.getNames("snap", 1));
      snapSelect.setBounds(Jned.TALL_BUTTON + Jned.BORDER, ycount, Jned.BORDER + 3 *
          Jned.TALL_BUTTON, Jned.SHORT_BUTTON_HT);
      snapSelect.addActionListener(this);
      JLabel snsltxt = makeJLabel("Preset:", 0, ycount, Jned.TALL_BUTTON + Jned.BORDER - 4,
          Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
      Button snsave = new Button(this, "snsave", -2, Jned.BORDER, ycount, 2 * Jned.TALL_BUTTON,
          Jned.SHORT_BUTTON_HT, true, "Save");
      Button sndelete = new Button(this, "sndelete", -2, 2 * Jned.BORDER + 2 * Jned.TALL_BUTTON,
          ycount, 2 * Jned.TALL_BUTTON, Jned.SHORT_BUTTON_HT, true, "Delete");
      // Spacing
      ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
      JLabel snspacing = makeJLabel("Spacing:", Jned.BORDER, ycount, tbw + Jned.BORDER - 4,
          Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      JLabel snspcx = makeJLabel("x", 2 * Jned.BORDER + tbw, ycount, Jned.SHORT_BUTTON_HT / 2 - 4,
          Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      JTextField snspcxtxt = new JTextField();
      snspcxtxt.setFont(Jned.BOX_FONT);
      snspcxtxt.setHorizontalAlignment(JTextField.RIGHT);
      snspcxtxt.setBounds(2 * Jned.BORDER + tbw + Jned.SHORT_BUTTON_HT / 2, ycount, tbw + 1 -
          Jned.SHORT_BUTTON_HT / 2, Jned.SHORT_BUTTON_HT);
      JLabel snspcy = makeJLabel("y", 3 * Jned.BORDER + 2 * tbw + 1, ycount, Jned.SHORT_BUTTON_HT
          / 2 - 4, Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      JTextField snspcytxt = new JTextField();
      snspcytxt.setFont(Jned.BOX_FONT);
      snspcytxt.setHorizontalAlignment(JTextField.RIGHT);
      snspcytxt.setBounds(3 * Jned.BORDER + 2 * tbw + 1 + Jned.SHORT_BUTTON_HT / 2, ycount, tbw +
          1 - Jned.SHORT_BUTTON_HT / 2, Jned.SHORT_BUTTON_HT);
      // Offset
      ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
      JLabel snoffset = makeJLabel("Offset:", Jned.BORDER, ycount, tbw + Jned.BORDER - 4,
          Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      JLabel snoffx = makeJLabel("x", 2 * Jned.BORDER + tbw, ycount, Jned.SHORT_BUTTON_HT / 2 - 4,
          Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      JTextField snoffxtxt = new JTextField();
      snoffxtxt.setFont(Jned.BOX_FONT);
      snoffxtxt.setHorizontalAlignment(JTextField.RIGHT);
      snoffxtxt.setBounds(2 * Jned.BORDER + tbw + Jned.SHORT_BUTTON_HT / 2, ycount, tbw + 1 -
          Jned.SHORT_BUTTON_HT / 2, Jned.SHORT_BUTTON_HT);
      JLabel snoffy = makeJLabel("y", 3 * Jned.BORDER + 2 * tbw + 1, ycount, Jned.SHORT_BUTTON_HT
          / 2 - 4, Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      JTextField snoffytxt = new JTextField();
      snoffytxt.setFont(Jned.BOX_FONT);
      snoffytxt.setHorizontalAlignment(JTextField.RIGHT);
      snoffytxt.setBounds(3 * Jned.BORDER + 2 * tbw + 1 + Jned.SHORT_BUTTON_HT / 2, ycount, tbw +
          1 - Jned.SHORT_BUTTON_HT / 2, Jned.SHORT_BUTTON_HT);
      // Symmetry
      ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
      JComboBox<String> snapSymm = new JComboBox<String>(gridSymmetries);
      snapSymm.setBounds(tbw + 2 * Jned.BORDER, ycount, Jned.BORDER + 2 + 2 * tbw,
          Jned.SHORT_BUTTON_HT);
      JLabel snsymtxt = makeJLabel("Symmetry:", Jned.BORDER, ycount, tbw + Jned.BORDER - 4,
          Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      // Show/hide
      ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
      JLabel snshowtxt = makeJLabel("Show snap points:", Jned.BORDER, ycount, 4 * Jned.TALL_BUTTON
          + Jned.BORDER - Jned.SHORT_BUTTON_HT - 4, Jned.SHORT_BUTTON_HT, SwingConstants.RIGHT);
      Button snshow = new Button(this, "snshow", -2, 2 * Jned.BORDER + 4 * Jned.TALL_BUTTON -
          Jned.SHORT_BUTTON_HT, ycount, Jned.SHORT_BUTTON_HT, Jned.SHORT_BUTTON_HT);
     snapDropPanel = new DropPanel(snaptoggle.getX(), snaptoggle.getY() + Jned.SHORT_BUTTON_HT, 3
        * Jned.BORDER + 4 * Jned.TALL_BUTTON, ycount + Jned.SHORT_BUTTON_HT + Jned.BORDER,
        snapping);
     snapDropPanel.add(snsltxt);
     snapDropPanel.add(snapSelect);
     snapDropPanel.add(snsave);
     snapDropPanel.add(sndelete);
     snapDropPanel.add(snspacing);
     snapDropPanel.add(snspcx);
     snapDropPanel.add(snspcxtxt);
     snapDropPanel.add(snspcy);
     snapDropPanel.add(snspcytxt);
     snapDropPanel.add(snoffset);
     snapDropPanel.add(snoffx);
     snapDropPanel.add(snoffxtxt);
     snapDropPanel.add(snoffy);
     snapDropPanel.add(snoffytxt);
     snapDropPanel.add(snsymtxt);
     snapDropPanel.add(snapSymm);
     snapDropPanel.add(snshowtxt);
     snapDropPanel.add(snshow);
     buttons[79] = snshow;
    snapOverlay = new Overlay(snspcxtxt, snspcytxt, snoffxtxt, snoffytxt, snapSymm, null, null,
        null, null, true, Jned.LVL_AREA_WD, Jned.LVL_AREA_HT, this);
    // Save dialog
     snapSaveDialog = new JDialog(frame, "Save Snap Preset");
     snapSaveDialog.getContentPane().setLayout(null);
     snapSaveDialog.getContentPane().setBackground(Colors.BG_COLOR);
     ycount = Jned.BORDER;
      JLabel snSaveConf = new JLabel("Type a name for this snap setting:");
       snSaveConf.setForeground(Color.BLACK);
       snSaveConf.setBounds(Jned.BORDER, ycount, Jned.CONFIRM_WINDOW - 2 * Jned.BORDER,
          Jned.SHORT_BUTTON_HT);
     snapSaveDialog.add(snSaveConf);
     ycount += Jned.BORDER + Jned.SHORT_BUTTON_HT;
      tfSnapSaveName = new JTextField();
       tfSnapSaveName.setFont(Jned.BOX_FONT);
       tfSnapSaveName.setHorizontalAlignment(JTextField.LEFT);
       tfSnapSaveName.setBounds(Jned.BORDER, ycount, Jned.CONFIRM_WINDOW - 2 * Jned.BORDER,
          Jned.SHORT_BUTTON_HT);
       tfSnapSaveName.addActionListener(this);
     snapSaveDialog.add(tfSnapSaveName);
     ycount += Jned.BORDER + Jned.SHORT_BUTTON_HT;
     Button snSaveSave = new Button(this, "snsavesave", -2, Jned.BORDER, ycount, 2 *
        Jned.TALL_BUTTON + Jned.BORDER, Jned.SHORT_BUTTON_HT, true, "Save");
     Button snSaveCancel = new Button(this, "snsavecancel", -2, Jned.CONFIRM_WINDOW - 2 *
        Jned.BORDER - 2 * Jned.TALL_BUTTON, ycount, 2 * Jned.TALL_BUTTON + Jned.BORDER, Jned.SHORT_BUTTON_HT, true, "Cancel");
     snapSaveDialog.add(snSaveSave);
     snapSaveDialog.add(snSaveCancel);
     snapSaveDialog.setLocationRelativeTo(null);
     snapSaveDialog.getContentPane().setPreferredSize(new Dimension(Jned.CONFIRM_WINDOW, ycount +
        Jned.BORDER + Jned.SHORT_BUTTON_HT));
     snapSaveDialog.pack();
    // Delete dialog
     snapDeleteDialog = new JDialog(frame, "Delete Snap Preset");
     snapDeleteDialog.getContentPane().setLayout(null);
     snapDeleteDialog.getContentPane().setBackground(Colors.BG_COLOR);
     ycount = Jned.BORDER;
      JLabel snDeleteConf = new JLabel("Are you sure you want to delete this snap setting?");
       snDeleteConf.setForeground(Color.BLACK);
       snDeleteConf.setBounds(Jned.BORDER, ycount, Jned.CONFIRM_WINDOW - 2 * Jned.BORDER,
          Jned.SHORT_BUTTON_HT);
     snapDeleteDialog.add(snDeleteConf);
     ycount += Jned.BORDER + Jned.SHORT_BUTTON_HT;
     Button snDeleteDelete = new Button(this, "sndeletedelete", -2, Jned.BORDER, ycount, 2 *
        Jned.TALL_BUTTON + Jned.BORDER, Jned.SHORT_BUTTON_HT, true, "Delete");
     Button snDeleteCancel = new Button(this, "sndeletecancel", -2, Jned.CONFIRM_WINDOW - 2 *
        Jned.BORDER - 2 * Jned.TALL_BUTTON, ycount, 2 * Jned.TALL_BUTTON + Jned.BORDER,
        Jned.SHORT_BUTTON_HT, true, "Cancel");
     snapDeleteDialog.add(snDeleteDelete);
     snapDeleteDialog.add(snDeleteCancel);
     snapDeleteDialog.setLocationRelativeTo(null);
     snapDeleteDialog.getContentPane().setPreferredSize(new Dimension(Jned.CONFIRM_WINDOW, ycount
        + Jned.BORDER + Jned.SHORT_BUTTON_HT));
     snapDeleteDialog.pack();
    add(snapDropPanel);
    buttons[76] = snaptoggle;
    add(snapping);
    add(snaptoggle);
    
    snapSelect.setSelectedItem("classic x");
    loadSnapPoints("classic x");
    
    // Show/hide buttons for triggers and paths
    ycount = Jned.BORDER;
    Button trigger = new Button(this, "trigger", -2, xcount, ycount, 2 * Jned.TALL_BUTTON +
        Jned.BORDER, Jned.SHORT_BUTTON_HT, "Door triggers");
    xcount += 2 * Jned.TALL_BUTTON + 2 * Jned.BORDER;
    Button dpath = new Button(this, "dpath", -2, xcount, ycount, 2 * Jned.TALL_BUTTON +
        Jned.BORDER, Jned.SHORT_BUTTON_HT, "Drone paths");
    buttons[60] = trigger;
    buttons[63] = dpath;
    add(trigger);
    add(dpath);
    
    // Undo and redo buttons
    //xcount = Jned.LVL_AREA_WD - 2 * Jned.TALL_BUTTON; TASK delete this line once you're sure it works
    xcount = Jned.TOTAL_WD - 4 * (Jned.BORDER + Jned.TALL_BUTTON);
    Button undo = new Button(this, "undo", -2, xcount, ycount, 2 * Jned.TALL_BUTTON + Jned.BORDER,
        Jned.SHORT_BUTTON_HT, true, "Undo");
    xcount += 2 * Jned.TALL_BUTTON + 2 * Jned.BORDER;
    Button redo = new Button(this, "redo", -2, xcount, ycount, 2 * Jned.TALL_BUTTON + Jned.BORDER,
        Jned.SHORT_BUTTON_HT, true, "Redo"); 
    buttons[4] = undo;
    buttons[5] = redo;
    add(undo);
    add(redo);
    
    // SECOND ROW
    // Items button
    xcount = Jned.BORDER;
    ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
    Button items = new Button(this, "items", -2, xcount, ycount, 2 * Jned.TALL_BUTTON +
        Jned.BORDER, Jned.SHORT_BUTTON_HT, "ITEMS");
    buttons[2] = items;
    add(items);
    
    // Level area
    xcount += 2 * Jned.TALL_BUTTON + 2 * Jned.BORDER;
    lvl = new LevelArea(xcount, ycount, Jned.LVL_AREA_WD, Jned.LVL_AREA_HT, Jned.LVL_SQUARE, this,
        keys);
    lvl.setOverlay(0, gridOverlays[0]);
    lvl.setOverlay(1, gridOverlays[1]);
    lvl.setOverlay(2, gridOverlays[2]);
    lvl.setOverlay(3, snapOverlay);
    add(lvl);
    
    // THIRD ROW
    // Enemies button
    xcount = Jned.BORDER;
    ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
    Button enemies = new Button(this, "enemies", -2, xcount, ycount, 2 * Jned.TALL_BUTTON +
        Jned.BORDER, Jned.SHORT_BUTTON_HT, "ENEMIES");
    buttons[3] = enemies;
    add(enemies);
    
    // COLUMNS
    mbuttons = new MultiButton[20];
    
    // Left 0
    ycount += Jned.SHORT_BUTTON_HT + Jned.BORDER;
    Button tile45 = new Button(this, "45tile", ImageBank.BT_TILE45, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_HALF);
    Button gaussturret = new Button(this, "Gaussturret", ImageBank.BT_GAUSS, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button player = new Button(this, "Player", ImageBank.BT_PLAYER, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button[] left0 = {tile45, gaussturret, player};
    add(mbuttons[0] = new MultiButton(left0, xcount, ycount, Jned.TALL_BUTTON, Jned.TALL_BUTTON));
    buttons[11] = tile45;
    buttons[21] = player;
    buttons[31] = gaussturret;
    
    // Right 0
    xcount += Jned.TALL_BUTTON + Jned.BORDER;
    // Tile sub-menus:
    Button tile45Q = new Button(this, "45tileQ", ImageBank.BT_TILE45 + 6, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_HALF + 6);
    Button thintile63Q = new Button(this, "63thintileQ", ImageBank.BT_THIN63 + 6, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THICK63 + 6);
    Button thintile27Q = new Button(this, "27thintileQ", ImageBank.BT_THIN27 + 6, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THICK27 + 6);
    Button concavetileQ = new Button(this, "ConcavetileQ", ImageBank.BT_CONCAVE + 6, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_CONVEX + 6);
    Button halftileQ = new Button(this, "HalftileQ", ImageBank.BT_HALF + 6, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_TILE45 + 6);
    Button thicktile63Q = new Button(this, "63thicktileQ", ImageBank.BT_THICK63 + 6, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THIN63 + 6);
    Button thicktile27Q = new Button(this, "27thicktileQ", ImageBank.BT_THICK27 + 6, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THIN27 + 6);
    Button convextileQ = new Button(this, "ConvextileQ", ImageBank.BT_CONVEX + 6, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_CONCAVE + 6);
    // Enemies sub-menus:
    Button thwumpD = new Button(this, "ThwumpD", ImageBank.BT_THWUMP, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button zapdroneD = new Button(this, "ZapdroneD", ImageBank.BT_ZAP, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button seekerdroneD = new Button(this, "SeekerdroneD", ImageBank.BT_SEEKER, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button laserdroneD = new Button(this, "LaserdroneD", ImageBank.BT_LASER, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button chaingundroneD = new Button(this, "ChaingundroneD", ImageBank.BT_CHAINGUN, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    // Items sub-menus:
    Button onewayD = new Button(this, "OnewayD", ImageBank.BT_ONEWAY, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button normaldoorD = new Button(this, "NormaldoorD", ImageBank.BT_NDOOR, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button lockeddoorD = new Button(this, "LockeddoorD", ImageBank.BT_LDOOR, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button trapdoorD = new Button(this, "TrapdoorD", ImageBank.BT_TDOOR, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button launchpadD = new Button(this, "LaunchpadD", ImageBank.BT_LAUNCH, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button[] right0ar = {tile45Q, thintile63Q,thintile27Q, concavetileQ, halftileQ, thicktile63Q,
        thicktile27Q, convextileQ, null, null, null, null, null, null, thwumpD, zapdroneD,
        seekerdroneD, laserdroneD, chaingundroneD, null, null, null, null, onewayD, normaldoorD,
        lockeddoorD, trapdoorD, launchpadD};
    add(mbuttons[10] = new MultiButton(right0ar, xcount, ycount, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON));
    buttons[41] = buttons[45] = mbuttons[10];
    
    // Left 1
    ycount += Jned.TALL_BUTTON + Jned.BORDER;
    xcount = Jned.BORDER;
    Button thintile63 = new Button(this, "63thintile", ImageBank.BT_THIN63, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_THICK63);
    Button hominglauncher = new Button(this, "Hominglauncher", ImageBank.BT_HOMING, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button gold = new Button(this, "Gold", ImageBank.BT_GOLD, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button[] left1 = {thintile63, hominglauncher, gold};
    add(mbuttons[1] = new MultiButton(left1, xcount, ycount, Jned.TALL_BUTTON, Jned.TALL_BUTTON));
    buttons[12] = thintile63;
    buttons[22] = gold;
    buttons[32] = hominglauncher;
    
    // Right 1
    xcount += Jned.TALL_BUTTON + Jned.BORDER;
    // Tile sub-menus:
    Button tile45W = new Button(this, "45tileW", ImageBank.BT_TILE45, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_HALF);
    Button thintile63W = new Button(this, "63thintileW", ImageBank.BT_THIN63, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THICK63);
    Button thintile27W = new Button(this, "27thintileW", ImageBank.BT_THIN27, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THICK27);
    Button concavetileW = new Button(this, "ConcavetileW", ImageBank.BT_CONCAVE, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_CONVEX);
    Button halftileW = new Button(this, "HalftileW", ImageBank.BT_HALF, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_TILE45);
    Button thicktile63W = new Button(this, "63thicktileW", ImageBank.BT_THICK63, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THIN63);
    Button thicktile27W = new Button(this, "27thicktileW", ImageBank.BT_THICK27, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THIN27);
    Button convextileW = new Button(this, "ConvextileW", ImageBank.BT_CONVEX, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_CONCAVE);
    // Enemies sub-menus:
    Button thwumpS = new Button(this, "ThwumpS", ImageBank.BT_THWUMP + 2, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button zapdroneS = new Button(this, "ZapdroneS", ImageBank.BT_ZAP + 2, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button seekerdroneS = new Button(this, "SeekerdroneS", ImageBank.BT_SEEKER + 2, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button laserdroneS = new Button(this, "LaserdroneS", ImageBank.BT_LASER + 2, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button chaingundroneS = new Button(this, "ChaingundroneS", ImageBank.BT_CHAINGUN + 2, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    // Items sub-menus:
    Button onewayS = new Button(this, "OnewayS", ImageBank.BT_ONEWAY + 2, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button normaldoorS = new Button(this, "NormaldoorS", ImageBank.BT_NDOOR + 2, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button lockeddoorS = new Button(this, "LockeddoorS", ImageBank.BT_LDOOR + 2, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button trapdoorS = new Button(this, "TrapdoorS", ImageBank.BT_TDOOR + 2, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button launchpadDS = new Button(this, "LaunchpadDS", ImageBank.BT_LAUNCH + 2, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button[] right1ar = {tile45W, thintile63W,thintile27W, concavetileW, halftileW, thicktile63W,
        thicktile27W, convextileW, null, null, null, null, null, null, thwumpS, zapdroneS,
        seekerdroneS, laserdroneS, chaingundroneS, null, null, null, null, onewayS, normaldoorS, 
        lockeddoorS, trapdoorS, launchpadDS};
    add(mbuttons[11] = new MultiButton(right1ar, xcount, ycount, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON));
    buttons[42] = buttons[46] = mbuttons[11];
    
    // Left 2
    ycount += Jned.TALL_BUTTON + Jned.BORDER;
    xcount = Jned.BORDER;
    Button thintile27 = new Button(this, "27thintile", ImageBank.BT_THIN27, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_THICK27);
    Button mine = new Button(this, "Mine", ImageBank.BT_MINE, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button bounceblock = new Button(this, "Bounceblock", ImageBank.BT_BOUNCE, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button[] left2 = {thintile27, mine, bounceblock};
    add(mbuttons[2] = new MultiButton(left2, xcount, ycount, Jned.TALL_BUTTON, Jned.TALL_BUTTON));
    buttons[13] = thintile27;
    buttons[23] = bounceblock;
    buttons[33] = mine;
    
    // Right 2
    xcount += Jned.TALL_BUTTON + Jned.BORDER;
    // Tile sub-menus:
    Button tile45A = new Button(this, "45tileA", ImageBank.BT_TILE45 + 4, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_HALF + 4);
    Button thintile63A = new Button(this, "63thintileA", ImageBank.BT_THIN63 + 4, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THICK63 + 4);
    Button thintile27A = new Button(this, "27thintileA", ImageBank.BT_THIN27 + 4, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THICK27 + 4);
    Button concavetileA = new Button(this, "ConcavetileA", ImageBank.BT_CONCAVE + 4, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_CONVEX + 4);
    Button halftileA = new Button(this, "HalftileA", ImageBank.BT_HALF + 4, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_TILE45 + 4);
    Button thicktile63A = new Button(this, "63thicktileA", ImageBank.BT_THICK63 + 4, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THIN63 + 4);
    Button thicktile27A = new Button(this, "27thicktileA", ImageBank.BT_THICK27 + 4, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THIN27 + 4);
    Button convextileA = new Button(this, "ConvextileA", ImageBank.BT_CONVEX + 4, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_CONCAVE + 4);
    // Enemies sub-menus:
    Button thwumpA = new Button(this, "ThwumpA", ImageBank.BT_THWUMP + 4, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button zapdroneA = new Button(this, "ZapdroneA", ImageBank.BT_ZAP + 4, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button seekerdroneA = new Button(this, "SeekerdroneA", ImageBank.BT_SEEKER + 4, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button laserdroneA = new Button(this, "LaserdroneA", ImageBank.BT_LASER + 4, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button chaingundroneA = new Button(this, "ChaingundroneA", ImageBank.BT_CHAINGUN + 4, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    // Items sub-menus:
    Button onewayA = new Button(this, "OnewayA", ImageBank.BT_ONEWAY + 4, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button normaldoorA = new Button(this, "NormaldoorA", ImageBank.BT_NDOOR + 4, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button lockeddoorA = new Button(this, "LockeddoorA", ImageBank.BT_LDOOR + 4, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button trapdoorA = new Button(this, "TrapdoorA", ImageBank.BT_TDOOR + 4, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button launchpadS = new Button(this, "LaunchpadS", ImageBank.BT_LAUNCH + 4, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button[] right2ar = {tile45A, thintile63A, thintile27A, concavetileA, halftileA, thicktile63A,
        thicktile27A, convextileA, null, null, null, null, null, null, thwumpA, zapdroneA,
        seekerdroneA, laserdroneA, chaingundroneA, null, null, null, null, onewayA, normaldoorA,
        lockeddoorA, trapdoorA, launchpadS};
    add(mbuttons[12] = new MultiButton(right2ar, xcount, ycount, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON));
    buttons[43] = buttons[47] = mbuttons[12];
    
    // Left 3
    ycount += Jned.TALL_BUTTON + Jned.BORDER;
    xcount = Jned.BORDER;
    Button concavetile = new Button(this, "Concavetile", ImageBank.BT_CONCAVE, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_CONVEX);
    Button floorguard = new Button(this, "Floorguard", ImageBank.BT_FLOOR, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button exit = new Button(this, "Exitdoor", ImageBank.BT_EXIT, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button[] left3 = {concavetile, floorguard, exit};
    add(mbuttons[3] = new MultiButton(left3, xcount, ycount, Jned.TALL_BUTTON, Jned.TALL_BUTTON));
    buttons[14] = concavetile;
    buttons[24] = exit;
    buttons[34] = floorguard;
    
    // Right 3
    xcount += Jned.TALL_BUTTON + Jned.BORDER;
    // Tile sub-menus:
    Button tile45S = new Button(this, "45tileS", ImageBank.BT_TILE45 + 2, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_HALF + 2);
    Button thintile63S = new Button(this, "63thintileS", ImageBank.BT_THIN63 + 2, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THICK63 + 2);
    Button thintile27S = new Button(this, "27thintileS", ImageBank.BT_THIN27 + 2, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THICK27 + 2);
    Button concavetileS = new Button(this, "ConcavetileS", ImageBank.BT_CONCAVE + 2, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_CONVEX + 2);
    Button halftileS = new Button(this, "HalftileS", ImageBank.BT_HALF + 2, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_TILE45 + 2);
    Button thicktile63S = new Button(this, "63thicktileS", ImageBank.BT_THICK63 + 2, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THIN63 + 2);
    Button thicktile27S = new Button(this, "27thicktileS", ImageBank.BT_THICK27 + 2, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THIN27 + 2);
    Button convextileS = new Button(this, "ConvextileS", ImageBank.BT_CONVEX + 2, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_CONCAVE + 2);
    // Enemies sub-menus:
    Button thwumpW = new Button(this, "ThwumpW", ImageBank.BT_THWUMP + 6, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button zapdroneW = new Button(this, "ZapdroneW", ImageBank.BT_ZAP + 6, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button seekerdroneW = new Button(this, "WeekerdroneW", ImageBank.BT_SEEKER + 6, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button laserdroneW = new Button(this, "LaserdroneW", ImageBank.BT_LASER + 6, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button chaingundroneW = new Button(this, "ChaingundroneW", ImageBank.BT_CHAINGUN + 6, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    // Items sub-menus:
    Button onewayW = new Button(this, "OnewayW", ImageBank.BT_ONEWAY + 6, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button normaldoorW = new Button(this, "NormaldoorW", ImageBank.BT_NDOOR + 6, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button lockeddoorW = new Button(this, "LockeddoorW", ImageBank.BT_LDOOR + 6, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button trapdoorW = new Button(this, "TrapdoorW", ImageBank.BT_TDOOR + 6, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button launchpadAS = new Button(this, "LaunchpadAS", ImageBank.BT_LAUNCH + 6, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button[] right3ar = {tile45S, thintile63S, thintile27S, concavetileS, halftileS, thicktile63S,
        thicktile27S, convextileS, null, null, null, null, null, null, thwumpW, zapdroneW,
        seekerdroneW, laserdroneW, chaingundroneW, null, null, null, null, onewayW, normaldoorW,
        lockeddoorW, trapdoorW, launchpadAS};
    add(mbuttons[13] = new MultiButton(right3ar, xcount, ycount, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON));
    buttons[44] = buttons[48] = mbuttons[13];
    
    // Left 4
    ycount += Jned.TALL_BUTTON + Jned.BORDER;
    xcount = Jned.BORDER;
    Button halftile = new Button(this, "Halftile", ImageBank.BT_HALF, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_TILE45);
    Button thwump = new Button(this, "Thwump", ImageBank.BT_THWUMP, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button oneway = new Button(this, "Oneway", ImageBank.BT_ONEWAY + 6, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button[] left4 = {halftile, thwump, oneway};
    add(mbuttons[4] = new MultiButton(left4, xcount, ycount, Jned.TALL_BUTTON, Jned.TALL_BUTTON));
    buttons[15] = halftile;
    buttons[25] = oneway;
    buttons[35] = thwump;
    
    // Right 4
    xcount += Jned.TALL_BUTTON + Jned.BORDER;
    // Enemies sub-menus:
    Button zapdronesurfacecw = new Button(this, "Zapdronesurfacecw", ImageBank.BT_ZAP + 8, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button seekerdronesurfacecw = new Button(this, "Seekerdronesurfacecw", ImageBank.BT_SEEKER + 8,
        0, 0, Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button laserdronesurfacecw = new Button(this, "Laserdronesurfacecw", ImageBank.BT_LASER + 8, 0,
        0, Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button chaingundronesurfacecw = new Button(this, "Chaingundronesurfacecw",
        ImageBank.BT_CHAINGUN + 8, 0, 0, Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    // Items sub-menus:
    Button launchpadA = new Button(this, "LaunchpadA", ImageBank.BT_LAUNCH + 8, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button[] right4ar = {null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, zapdronesurfacecw, seekerdronesurfacecw, laserdronesurfacecw,
        chaingundronesurfacecw, null, null, null, null, null, null, null, null, launchpadA};
    add(mbuttons[14] = new MultiButton(right4ar, xcount, ycount, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON));
    buttons[53] = buttons[49] = mbuttons[14];
    
    // Left 5
    ycount += Jned.TALL_BUTTON + Jned.BORDER;
    xcount = Jned.BORDER;
    Button thicktile63 = new Button(this, "63thicktile", ImageBank.BT_THICK63, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THIN63);
    Button zapdrone = new Button(this, "Zapdrone", ImageBank.BT_ZAP, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button normaldoor = new Button(this, "Normaldoor", ImageBank.BT_NDOOR, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button[] left5 = {thicktile63, zapdrone, normaldoor};
    add(mbuttons[5] = new MultiButton(left5, xcount, ycount, Jned.TALL_BUTTON, Jned.TALL_BUTTON));
    buttons[16] = thicktile63;
    buttons[26] = normaldoor;
    buttons[36] = zapdrone;
    
    // Right 5
    xcount += Jned.TALL_BUTTON + Jned.BORDER;
    // Enemies sub-menus:
    Button zapdronesurfaceccw = new Button(this, "Zapdronesurfaceccw", ImageBank.BT_ZAP + 10, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button seekerdronesurfaceccw = new Button(this, "Seekerdronesurfaceccw", ImageBank.BT_SEEKER + 
        10, 0, 0, Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button laserdronesurfaceccw = new Button(this, "Laserdronesurfaceccw", ImageBank.BT_LASER + 10,
        0, 0, Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button chaingundronesurfaceccw = new Button(this, "Chaingundronesurfaceccw",
        ImageBank.BT_CHAINGUN + 10, 0, 0, Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    // Items sub-menus:
    Button launchpadAW = new Button(this, "LaunchpadAW", ImageBank.BT_LAUNCH + 10, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button[] right5ar = {null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, zapdronesurfaceccw, seekerdronesurfaceccw, laserdronesurfaceccw,
        chaingundronesurfaceccw, null, null, null, null, null, null, null, null, launchpadAW};
    add(mbuttons[15] = new MultiButton(right5ar, xcount, ycount, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON));
    buttons[54] = buttons[50] = mbuttons[15];
    
    // Left 6
    ycount += Jned.TALL_BUTTON + Jned.BORDER;
    xcount = Jned.BORDER;
    Button thicktile27 = new Button(this, "27thicktile", ImageBank.BT_THICK27, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON, ImageBank.BT_THIN27);
    Button seekerdrone = new Button(this, "Seekerdrone", ImageBank.BT_SEEKER, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button lockeddoor = new Button(this, "Lockeddoor", ImageBank.BT_LDOOR + 8, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button[] left6 = {thicktile27, seekerdrone, lockeddoor};
    add(mbuttons[6] = new MultiButton(left6, xcount, ycount, Jned.TALL_BUTTON, Jned.TALL_BUTTON));
    buttons[17] = thicktile27;
    buttons[27] = lockeddoor;
    buttons[37] = seekerdrone;
    
    // Right 6
    xcount += Jned.TALL_BUTTON + Jned.BORDER;
    // Enemies sub-menus:
    Button zapdronedumbcw = new Button(this, "Zapdronedumbcw", ImageBank.BT_ZAP + 12, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button seekerdronedumbcw = new Button(this, "Seekerdronedumbcw", ImageBank.BT_SEEKER + 12, 0,
        0, Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button laserdronedumbcw = new Button(this, "Laserdronedumbcw", ImageBank.BT_LASER + 12, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button chaingundronedumbcw = new Button(this, "Chaingundronedumbcw", ImageBank.BT_CHAINGUN +
        12, 0, 0, Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    // Items sub-menus:
    Button launchpadW = new Button(this, "LaunchpadW", ImageBank.BT_LAUNCH + 12, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button[] right6ar = {null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, zapdronedumbcw, seekerdronedumbcw, laserdronedumbcw, chaingundronedumbcw,
        null, null, null, null, null, null, null, null, launchpadW};
    add(mbuttons[16] = new MultiButton(right6ar, xcount, ycount, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON));
    buttons[55] = buttons[51] = mbuttons[16];
    
    // Left 7
    ycount += Jned.TALL_BUTTON + Jned.BORDER;
    xcount = Jned.BORDER;
    Button convextile = new Button(this, "Convextile", ImageBank.BT_CONVEX, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_CONCAVE);
    Button laserdrone = new Button(this, "Laserdrone", ImageBank.BT_LASER, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button trapdoor = new Button(this, "Trapdoor", ImageBank.BT_TDOOR + 8, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON);
    Button[] left7 = {convextile, laserdrone, trapdoor};
    add(mbuttons[7] = new MultiButton(left7, xcount, ycount, Jned.TALL_BUTTON, Jned.TALL_BUTTON));
    buttons[18] = convextile;
    buttons[28] = trapdoor;
    buttons[38] = laserdrone;
    
    // Right 7
    xcount += Jned.TALL_BUTTON + Jned.BORDER;
    // Enemies sub-menus:
    Button zapdronedumbccw = new Button(this, "Zapdronedumbccw", ImageBank.BT_ZAP + 14, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button seekerdronedumbccw = new Button(this, "Seekerdronedumbccw", ImageBank.BT_SEEKER + 14, 0,
        0, Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button laserdronedumbccw = new Button(this, "Laserdronedumbccw", ImageBank.BT_LASER + 14, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button chaingundronedumbccw = new Button(this, "Chaingundronedumbccw", ImageBank.BT_CHAINGUN +
        14, 0, 0, Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    // Items sub-menus:
    Button launchpadDW = new Button(this, "LaunchpadDW", ImageBank.BT_LAUNCH + 14, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button[] right7ar = {null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, zapdronedumbccw, seekerdronedumbccw, laserdronedumbccw,
        chaingundronedumbccw, null, null, null, null, null, null, null, null, launchpadDW};
    add(mbuttons[17] = new MultiButton(right7ar, xcount, ycount, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON));
    buttons[56] = buttons[52] = mbuttons[17];
    
    // Left 8
    ycount += Jned.TALL_BUTTON + Jned.BORDER;
    xcount = Jned.BORDER;
    Button erase = new Button(this, "Erase", ImageBank.BT_ERASE, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_FILL);
    Button chaingundrone = new Button(this, "Chaingundrone", ImageBank.BT_CHAINGUN, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button launchpad = new Button(this, "Launchpad", ImageBank.BT_LAUNCH + 12, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button[] left8 = {erase, chaingundrone, launchpad};
    add(mbuttons[8] = new MultiButton(left8, xcount, ycount, Jned.TALL_BUTTON, Jned.TALL_BUTTON));
    buttons[19] = erase;
    buttons[29] = launchpad;
    buttons[39] = chaingundrone;
    
    // Right 8
    xcount += Jned.TALL_BUTTON + Jned.BORDER;
    // Enemies sub-menus:
    Button zapdronealt = new Button(this, "Zapdronealt", ImageBank.BT_ZAP + 16, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button seekerdronealt = new Button(this, "Seekerdronealt", ImageBank.BT_SEEKER + 16, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button laserdronealt = new Button(this, "Laserdronealt", ImageBank.BT_LASER + 16, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button chaingundronealt = new Button(this, "Chaingundronealt", ImageBank.BT_CHAINGUN + 16, 0,
        0, Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button[] right8ar = {null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, zapdronealt, seekerdronealt, laserdronealt, chaingundronealt, null,
        null, null, null, null, null, null, null, null};
    add(mbuttons[18] = new MultiButton(right8ar, xcount, ycount, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON));
    buttons[57] = mbuttons[18];
    
    // Left 9
    ycount += Jned.TALL_BUTTON + Jned.BORDER;
    xcount = Jned.BORDER;
    Button fill = new Button(this, "Fill", ImageBank.BT_FILL, 0, 0, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON, ImageBank.BT_ERASE);
    Button[] left9 = {fill, null, null};
    add(mbuttons[9] = new MultiButton(left9, xcount, ycount, Jned.TALL_BUTTON, Jned.TALL_BUTTON));
    buttons[20] = fill;

    // Right 9
    xcount += Jned.TALL_BUTTON + Jned.BORDER;
    // Enemies sub-menus:
    Button zapdronerand = new Button(this, "Zapdronerand", ImageBank.BT_ZAP + 18, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button seekerdronerand = new Button(this, "Seekerdronerand", ImageBank.BT_SEEKER + 18, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button laserdronerand = new Button(this, "Laserdronerand", ImageBank.BT_LASER + 18, 0, 0,
        Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button chaingundronerand = new Button(this, "Chaingundronerand", ImageBank.BT_CHAINGUN + 18,
        0, 0, Jned.TALL_BUTTON, Jned.TALL_BUTTON);
    Button[] right9ar = {null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, zapdronerand, seekerdronerand, laserdronerand, chaingundronerand, null,
        null, null, null, null, null, null, null, null};
    add(mbuttons[19] = new MultiButton(right9ar, xcount, ycount, Jned.TALL_BUTTON,
        Jned.TALL_BUTTON));
    buttons[58] = mbuttons[19];
    
    ycount += Jned.TALL_BUTTON + Jned.BORDER;
    xcount = Jned.BORDER;

    // Button groups
    Button[][] sewbuttons = {
      {tiles, items, enemies},
          
      // Tile sub-menus
      {tile45A, tile45Q, tile45S, tile45W},
      {thintile63A, thintile63S, thintile63W, thintile63Q},
      {thintile27A, thintile27S, thintile27W, thintile27Q},
      {concavetileA, concavetileS, concavetileW, concavetileQ},
      {halftileA, halftileQ, halftileS, halftileW},
      {thicktile63A, thicktile63S, thicktile63W, thicktile63Q},
      {thicktile27A, thicktile27S, thicktile27W, thicktile27Q},
      {convextileA, convextileS, convextileW, convextileQ},
      
      // Enemies directional sub-menus
      {thwumpA, thwumpW, thwumpD, thwumpS},
      {zapdroneA, zapdroneW, zapdroneD, zapdroneS},
      {seekerdroneA, seekerdroneW, seekerdroneD, seekerdroneS},
      {laserdroneA, laserdroneW, laserdroneD, laserdroneS},
      {chaingundroneA, chaingundroneW, chaingundroneD, chaingundroneS},
                        
      // Objects sub-menus
      {onewayA, onewayW, onewayD, onewayS},
      {normaldoorA, normaldoorW, normaldoorD, normaldoorS},
      {lockeddoorA, lockeddoorW, lockeddoorD, lockeddoorS},
      {trapdoorA, trapdoorW, trapdoorD, trapdoorS},
      {launchpadA, launchpadAW, launchpadW, launchpadDW, launchpadD, launchpadDS, launchpadS,
          launchpadAS},
      
      // Left menus 
      {tile45, thintile63, thintile27, concavetile, halftile, thicktile63, thicktile27,
          convextile, erase, fill},
      {gaussturret, hominglauncher, mine, floorguard, thwump, zapdrone, seekerdrone, laserdrone,
          chaingundrone}, 
      {player, gold, bounceblock, exit, oneway, normaldoor, lockeddoor, trapdoor, launchpad},
          
      //Drone behavior sub-menus
      {zapdronedumbcw, zapdronedumbccw, zapdronesurfacecw, zapdronesurfaceccw, zapdronealt,
          zapdronerand},
      {seekerdronedumbcw, seekerdronedumbccw, seekerdronesurfacecw, seekerdronesurfaceccw,
          seekerdronealt, seekerdronerand},
      {laserdronedumbcw, laserdronedumbccw, laserdronesurfacecw, laserdronesurfaceccw,
          laserdronealt, laserdronerand},
      {chaingundronedumbcw, chaingundronedumbccw, chaingundronesurfacecw, chaingundronesurfaceccw,
          chaingundronealt, chaingundronerand}
    };
    for (int i = 0; i < sewbuttons.length; i++) {
      makeGroup(sewbuttons[i], i<19); // Menu array arranged so first 19 groups are radio-style
    }
    
    tiles.setPushed(true);
    
    widthWithoutTextBox = Jned.TOTAL_WD;
    heightWithoutTextBox = ycount;
    
    // END OF MAIN WINDOW
    
    // Text box
    tbox = new TextBox(this, 0, ycount, Jned.TOTAL_WD, Jned.TXT_ED_HT + 3 * Jned.BORDER +
        Jned.SHORT_BUTTON_HT);
    add(tbox.getPane());
    buttons[66] = tbox.tboxButtons[0];
    buttons[67] = tbox.tboxButtons[1];
    buttons[68] = tbox.tboxButtons[2];
    ycount += Jned.TXT_ED_HT + 3 * Jned.BORDER + Jned.SHORT_BUTTON_HT;
    
    setPreferredSize(new Dimension(Jned.TOTAL_WD, ycount));

    // Menu bar
    JMenuBar mbar = new JMenuBar();
    
     JMenu mFile = new JMenu("File");
     mFile.add(makeMenuItem("New", "action#88"));
     mFile.add(makeMenuItem("Open...", "action#84"));
     mFile.add(makeMenuItem("Save", "action#82"));
     mFile.add(makeMenuItem("Save As...", "action#83"));
     mFile.addSeparator();
     JMenuItem miExit = makeMenuItem("Exit", "Exit");
      // This item's keyboard shortcut text needs to be set manually, since the key command is
      // done by the environement and not Jned
      KeyStroke altf4 = KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK, true);
      miExit.setAccelerator(altf4);
      miExit.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(altf4, "none");
     mFile.add(miExit);
     
     JMenu mEdit = new JMenu("Edit");
     mEdit.add(makeMenuItem("Undo", "action#4"));
     mEdit.add(makeMenuItem("Redo", "action#5"));
     mEdit.addSeparator();
     mEdit.add(makeMenuItem("Cut", "action#6"));
     mEdit.add(makeMenuItem("Copy", "action#7"));
     mEdit.add(makeMenuItem("Paste", "action#8"));
     mEdit.add(makeMenuItem("Delete", "action#9"));
     mEdit.add(makeMenuItem("Select All", "action#89"));
     mEdit.addSeparator();
      JMenu mNudge = new JMenu("Nudge");
      mNudge.add(makeMenuItem("Right", "action#69"));
      mNudge.add(makeMenuItem("Down", "action#70"));
      mNudge.add(makeMenuItem("Left", "action#71"));
      mNudge.add(makeMenuItem("Up", "action#72"));
     mEdit.add(mNudge);
      JMenu mDirection = new JMenu("Direction");
      mDirection.add(makeMenuItem("Right", "action#45"));
      mDirection.add(makeMenuItem("Right/down", "action#49"));
      mDirection.add(makeMenuItem("Down", "action#46"));
      mDirection.add(makeMenuItem("Down/left", "action#50"));
      mDirection.add(makeMenuItem("Left", "action#47"));
      mDirection.add(makeMenuItem("Left/up", "action#51"));
      mDirection.add(makeMenuItem("Up", "action#48"));
      mDirection.add(makeMenuItem("Up/right", "action#52"));
     mEdit.add(mDirection);
      JMenu mBehavior = new JMenu("Behavior");
      mBehavior.add(makeMenuItem("Surface-follow clockwise", "action#53"));
      mBehavior.add(makeMenuItem("Surface-follow counter-clockwise", "action#54"));
      mBehavior.add(makeMenuItem("Dumb clockwise", "action#55"));
      mBehavior.add(makeMenuItem("Dumb counter-clockwise", "action#56"));
      mBehavior.add(makeMenuItem("Alternating", "action#57"));
      mBehavior.add(makeMenuItem("Quasi-random", "action#58"));
      mBehavior.add(makeMenuItem("No behavior/still", "action#59"));
     mEdit.add(mBehavior);
     mEdit.addSeparator();
     mEdit.add(makeMenuItem("Snapping", "action#78"));
      mSnapSettings = new JMenu("Snap setting");
      setSnapSettingMenu();
     mEdit.add(mSnapSettings);
     mEdit.addSeparator();
     mEdit.add(makeMenuItem("Keyboard Shortcuts...", "keyShortcuts"));
     
     JMenu mView = new JMenu("View");
     mView.add(makeMenuItem("Gridlines", "action#75"));
      mGridSettings = new JMenu("Gridline setting");
      setGridlineSettingMenu();
     mView.add(mGridSettings);
     mView.add(makeMenuItem("Snap points", "action#81"));
     
    mbar.add(mFile);
    mbar.add(mEdit);
    mbar.add(mView);
    frame.setJMenuBar(mbar);
    
    updateText(lvl.outputLevel());
    
    repaint();
  }
  // Constructor helper methods
  private JLabel makeJLabel(String txt, int xpos, int ypos, int wid, int hei, int align) {
    JLabel result = new JLabel(txt);
    result.setBounds(xpos, ypos, wid, hei);
    result.setFont(Jned.DEF_FONT);
    result.setForeground(Colors.TEXT_COLOR);
    result.setHorizontalAlignment(align);
    return result;
  }
  private JMenuItem makeMenuItem(String text, String command) {
    JMenuItem mi = new JMenuItem(text);
    mi.addActionListener(this);
    mi.setActionCommand(command);
    //Menu items that correspond to actions are linked to action #'s KeySetting object
    String[] multicmd = command.split("#");
    if (multicmd.length > 1) {
      if (multicmd[0].equals("action")) {
        try {
          KeySetting ks = keys.getKeySetting(Integer.parseInt(multicmd[1]));
          if (ks != null) ks.setSoulmate(mi);
        } catch (NumberFormatException e) {}
      }
    }
    return mi;
  }
  private void makeGroup(Button[] group, boolean isRadio) {
    for (int i = 0; i < group.length; i++) {
      if (isRadio) {
        group[i].setRadio(true);
      }
      for (int j = 0; j < group.length; j++) {
        if (i != j) {
          group[i].add(group[j]);
        }
      }
    }
  }
  
  private void setGridlineSettingMenu() {
    mGridSettings.removeAll();
    for (String nom : config.getNames("grid", 1)) {
      JMenuItem mi = new JMenuItem(nom);
      mGridSettings.add(nom);
    }
    // Following actions only work when in a separate loop, don't know why
    for (int i = 0; i < mGridSettings.getItemCount(); i++) {
      JMenuItem mi = mGridSettings.getItem(i);
      mi.addActionListener(this);
      try {
        int actionNumber = Integer.parseInt(config.getAttr2(mi.getText()));
        mi.setActionCommand("action#" + actionNumber);
        keys.getKeySetting(actionNumber).setSoulmate(mi);
      } catch (NumberFormatException e) {}
    }
  }
  private void setSnapSettingMenu() {
    mSnapSettings.removeAll();
    for (String nom : config.getNames("snap", 1)) {
      JMenuItem mi = new JMenuItem(nom);
      mSnapSettings.add(nom);
    }
    for (int i = 0; i < mSnapSettings.getItemCount(); i++) {
      JMenuItem mi = mSnapSettings.getItem(i);
      mi.addActionListener(this);
      try {
        int actionNumber = Integer.parseInt(config.getAttr2(mi.getText()));
        mi.setActionCommand("action#" + actionNumber);
        keys.getKeySetting(actionNumber).setSoulmate(mi);
      } catch (NumberFormatException e) {}
    }
  }
  
  public void saveAs(String name, String author, String genre) {
    setAttributes(name, author, genre);
    Nfile usrlvls = new Nfile(userlevels);
    usrlvls.writeNew(name, author, genre, tbox.getText());
    usrlvls.close();
    savedAs = true;
  }
  protected void setUserlevels(String usrlvls) {
    userlevels = usrlvls;
  }

  // TASK ship to LevelArea
  public void calculateDronePaths() {
    lvl.recalculateDronePaths();
  }

  //Finds an unused action number for grid/snap settings
  private int findNextActionNumber() {
    ArrayList<Integer> used = new ArrayList<Integer>();
    String[] gnames = config.getNames("grid", 1);
    String[] snames = config.getNames("snap", 1);
    for(String nom : gnames) {
      try {
        used.add(Integer.parseInt(config.getAttr2(nom)));
      } catch (NumberFormatException e) {
      }
    }
    for(String nom : snames) {
      try {
        used.add(Integer.parseInt(config.getAttr2(nom)));
      } catch (NumberFormatException e) {
      }
    }
    Collections.sort(used);
    for(int i = 0; i < used.size(); i++) {
      if(used.get(i) - 256 != i) return i + 256;
    }
    return used.size() + 256;
  }
  //Saves gridline data to config file
  public void saveGridLines(String name) {
    String data = "";
    for (int i = 0; i < 3; i++) {
      data += gridOverlays[i].saveData();
      if(i<2) data += "@";
    }
    config.writeNew(name, "grid", "" + findNextActionNumber(), data);
    gridSelect.addItem(name);
    gridSelect.setSelectedItem(name);
    keyShortcuts.addGridSnapSettings();
    setGridlineSettingMenu();
  }
  //Deletes a preset from the config file
  public void deleteGridLines(String name) {
    if(!name.split(" ")[0].equals("classic")) {
      config.delete(name);
      gridSelect.removeItem(name);
      keyShortcuts.addGridSnapSettings();
      setGridlineSettingMenu();
    }
  }
  //Loads gridline data from config file
  public void loadGridLines(String name) {
    String setting = config.getData(name);
    if(setting != null) {
      String[] overdat = setting.split("@"); //Change to make it load the data from file under name
      for (int i = 0; i < 3; i++) {
        gridOverlays[i].loadData(overdat[i]);
      }
    }
  }
  //Saves gridline data to config file
  public void saveSnapPoints(String name) {
    config.writeNew(name, "snap", "" + findNextActionNumber(), snapOverlay.saveData());
    snapSelect.addItem(name);
    snapSelect.setSelectedItem(name);
    keyShortcuts.addGridSnapSettings();
    setSnapSettingMenu();
  }
  //Deletes a preset from the config file
  public void deleteSnapPoints(String name) {
    if(!name.split(" ")[0].equals("classic")) {
      config.delete(name);
      snapSelect.removeItem(name);
      keyShortcuts.addGridSnapSettings();
      setSnapSettingMenu();
    }
  }
  //Loads gridline data from config file
  public void loadSnapPoints(String name) {
    String setting = config.getData(name);
    if(setting != null) snapOverlay.loadData(setting);
  }

  // Text box interaction
  /**
   * Splits the text box into a separate window.
   */
  public void popText() {
    remove(tbox.getPane());
    setPreferredSize(new Dimension(widthWithoutTextBox, heightWithoutTextBox));
    frame.pack();
    tbox.popOut();
    repaint();
  }
  
  /**
   * Joins the text box window with the Jned window, placing it on the bottom.
   */
  public void pushTextBelow() {
    tbox.popIn(true);
    setPreferredSize(new Dimension(widthWithoutTextBox, heightWithoutTextBox + tbox.getHeight()));
    add(tbox.getPane());
    frame.pack();
    repaint();
  }
  
  /**
   * Joins the text box window with the Jned window, placing it on the right side.
   */
  public void pushTextBeside() {
    tbox.popIn(false);
    setPreferredSize(new Dimension(widthWithoutTextBox + tbox.getWidth(), heightWithoutTextBox));
    add(tbox.getPane());
    frame.pack();
    repaint();
  }

  /**
   * Places a set of level data in the text box, updating the history if necessary.
   * @param text the string containing level data
   */
  public void updateText(String text) {
    if(!hist.current().equals(text)) {
      tbox.setText(text);
      hist.add(text);
    }
  }

  // TASK - ship to TextBox
  public void highlightItem(int index) {
    tbox.highlightItem(index);
  }
  public void highlightTile(int index) {
    tbox.highlightTile(index);
  }
  public void unHighlight() {
    tbox.unHighlight();
  }
  
  // Level IO & attribute manipulation
  /**
   * Loads level and attribute data to Jned.
   * @param name the string representing the level name attribute
   * @param author the string representing the author attribute
   * @param genre the string representing the genre attribute
   * @param data the string containing level data
   */
  public void loadLevel(String name, String author, String genre, String data) {
    setAttributes(name, author, genre);
    updateText(data);
    push("tboxlvl");
    hist.clear();
  }
  
  /**
   * Loads a blank, unsaved level to Jned.
   */
  public void newLevel() {
    setAttributes("", "", "");
    updateText(Jned.BLANK_LEVEL);
    push("tboxlvl");
    hist.clear();
    frame.setTitle("New level");
    savedAs = false;
  }
  
  /**
   * Loads a set of attributes to the current level.
   * @param name the string representing the level name attribute
   * @param author the string representing the author attribute
   * @param genre the string representing the genre attribute
   */
  public void setAttributes(String name, String author, String genre) {
    lvlName = name;
    lvlAuthor = author;
    lvlGenre = genre;
    frame.setTitle(name + " - " + author + " - " + genre);
  }
  
  /**
   * Loads a set of attributes to the current level.
   * @param attrs a string array containing name, author, and genre strings
   */
  public void setAttributes(String[] attrs) {
    if(attrs.length >= 3) {
      setAttributes(attrs[0], attrs[1], attrs[2]);
    }
  }
  
  /**
   * Returns the attribute data of the current level.
   * @return a string array containing name, author, and genre strings
   */
  public String[] getAttributes() {
    String[] res = new String[3];
    res[0] = lvlName;
    res[1] = lvlAuthor;
    res[2] = lvlGenre;
    return res;
  }

  
  /**
   * Performs an action by its action number. Most of the game actions correspond directly to
   * buttons on the screen. In these cases, this method simply pushes the associated button. In
   * other cases, this method will use mode and key press information to decide what button to
   * push (or unpush). Some actions are deferred directly to the push() method in either Jned or
   * LevelArea, as appropriate.
   * @param an the action number to perform
   */
  public void doActionNumber (int an) {
    // Grid and snap settings
    if (an >= 256) {
      String[] setting = config.getNames(("" + an), 2);
      if (setting.length > 0) {
        String type = config.getAttr1(setting[0]);
        if (type.equals("grid")) {
          gridSelect.setSelectedItem(setting[0]);
          loadGridLines(setting[0]);
        }
        if (type.equals("snap")) {
          snapSelect.setSelectedItem(setting[0]);
          loadSnapPoints(setting[0]);
        }
      }
      return;
    }
    
    // Filtering of mode-dependant actions:
    int md = lvl.getIntMode();
    // Drone behavior actions only matter in drone modes
    if (an >= 53 && an <= 59 && (md < LevelArea.ZAP || md >= LevelArea.CHAINGUN + 27)) {
      return;
    }
    if (lvl.getBoolMode()) { // Tile mode
      // Item/enemy actions only matter in item/enemy mode
      if (an >= 21 && an <= 40 || an >= 45 && an <=48 || an >= 53 && an <= 59 || an >= 69 && an
          <= 72) {
        return;
      }
    } else { // Items or Enemies modes
      // Tile actions only matter in tile mode
      if (an >= 11 && an <= 20 || an >= 41 && an <= 44) {
        return;
      }
      // Item/enemy actions should switch to item/enemy mode when in the other mode
      if (an >= 21 && an <= 30) {
        buttons[2].setPushed(true);
      }
      if (an >= 31 && an <= 40) {
        buttons[3].setPushed(true);
      }
      // Launchpad directions must be changed to right action numbers, which have another layout
      if (md >= LevelArea.LAUNCH && md <= LevelArea.LAUNCH + 7) {
        switch (an) {
          case 45: // Right (already correct)
            break;
          case 46: // Down
            an = 47;
            break;
          case 47: // Left
            an = 49;
            break;
          case 48: // Up
            an = 51;
            break;
          case 49: // Right-down
            an = 46;
            break;
          case 50: // Down-left
            an = 48;
            break;
          case 51: // Left-up
            an = 50;
            break;
          case 52: // Up-right (already correct)
            break;
          default:
        }
      }
    }
    
    // Action execution
    if (buttons[an] != null) {
      buttons[an].setPushed(true);
    } else {
      switch (an) {
        case 6: // Cut
          if (lvl.getBoolMode()) {
            lvl.push("cut");
          } else {
            lvl.push("itemCut");
          }
          break;
        case 7: // Copy
          if (lvl.getBoolMode()) {
            lvl.push("copy");
          } else {
            lvl.push("itemCopy");
          }
          break;
        case 8: // Paste
          if (lvl.getBoolMode()) {
            lvl.push("paste");
          } else {
            lvl.push("itemPaste");
          }
          break;
        case 9: // Delete
          pushDelete();
          break;
        case 10: // Selection mode
          lvl.setMode(-1);
          for (int i = 0; i <= 9; i++) {
            mbuttons[i].setPushed(false);
            mbuttons[i + 10].setIndex(8);
          }
          break;
        case 59: // No drone behavior
          for (int i = 53; i <= 58; i++) {
            buttons[i].setPushed(false);
          }
          switch (lvl.getType(lvl.getIntMode())) {
            case 5:
              lvl.setMode(LevelArea.ZAP + 7 * itemDirection / 2 + 6);
              break;
            case 6:
              lvl.setMode(LevelArea.SEEKER + 7 * itemDirection / 2 + 6);
              break;
            case 7:
              lvl.setMode(LevelArea.LASER + 7 * itemDirection / 2 + 6);
              break;
            case 8:
              lvl.setMode(LevelArea.CHAINGUN + 7 * itemDirection / 2 + 6);
              break;
            default:
          }
          droneBehavior = 6;
          break;
        case 62: // Toggle triggers
          if (!lvl.drawingTriggers()) {
            buttons[60].setPushed(true);
            break;
          }
          // fall through
        case 61: // Hide triggers
          buttons[60].setPushed(false);
          lvl.setDrawTriggers(false);
          break;
        case 65: // Toggle drone paths
          if (!lvl.drawingDronePaths()) {
            buttons[63].setPushed(true);
            break;
          }
          // fall through
        case 64: // Hide drone paths
          buttons[63].setPushed(false);
          lvl.setDrawDronePaths(false);
          break;
        case 69: // Nudge right
          lvl.push("nudgeRight");
          break;
        case 70: // Nudge down
          lvl.push("nudgeDown");
          break;
        case 71: // Nudge left
          lvl.push("nudgeLeft");
          break;
        case 72: // Nudge up
          lvl.push("nudgeUp");
          break;
        case 75: // Toggle gridlines
          if (!lvl.drawingGrid()) {
            buttons[73].setPushed(true);
            break;
          }
          // fall through
        case 74: //Hide gridlines
          buttons[73].setPushed(false);
          lvl.setDrawGrid(false);
          break;
        case 78: //Toggle snapping
          if (!lvl.isSnappingOn()) {
            buttons[76].setPushed(true);
            break;
          }
          // fall through
        case 77: // Snapping off
          buttons[76].setPushed(false);
          lvl.setSnapping(false);
          break;
        case 81: // Toggle snap points
          if (!lvl.drawingSnapPoints()) {
            buttons[79].setPushed(true);
            break;
          }
          // fall through
        case 80: // Hide snap points
          buttons[79].setPushed(false);
          lvl.setDrawSnapPoints(false);
          break;
        case 82: // Save
          push("save");
          break;
        case 83: // Save As
          push("saveAs");
          break;
        case 84: // Open
          push("open");
          break;
        case 85: // Pop text
          popText();
          break;
        case 86: // Push text below
          pushTextBelow();
          break;
        case 87: // Push text beside  
          pushTextBeside();
          break;
        case 88: // New
          push("new");
          break;
        case 89: // Select All
          lvl.selectAll();
        break;
        default:
          System.err.println("No action for number " + an);
      }
    }
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getSource().equals(tfGridSaveName)) {
      push("tfGridSaveName");
      return;
    }
    if (e.getSource().equals(tfSnapSaveName)) {
      push("tfSnapSaveName");
      return;
    }
    String comm = e.getActionCommand();
    if (comm.equals("comboBoxChanged")) {
      if (((JComboBox)e.getSource()).equals(gridSelect)) {
        loadGridLines((String)gridSelect.getSelectedItem());
      }
      if (((JComboBox)e.getSource()).equals(snapSelect)) {
        loadSnapPoints((String)snapSelect.getSelectedItem());
      }
    } else {
      push(comm);
    }
  }
  
  /**
   * Performs actions that should result from pushing the described button, or a hypothetical
   * button with the given action command. This method is intended as the last step in the process
   * of any button push/key press/action. It does not change the pushed state of any actual Button
   * objects.
   * <p>
   * Much of what happens in Jned routes through this method. Mode changing, undo/redo, gridline
   * and snap setting changes, opening and saving files, and some miscellaneous functions are all
   * accomplished via cases in this method. Pushing buttons, pressing keys, or selecting menu items
   * all trigger a call to this method with the appropriate action command text, directly or
   * indirectly.
   * @param button the string representing the action command for the desired action
   */
  public void push(String button) {
    switch (button) {
      case "tiles":
        switchLeftColumn(0);
        switchRightColumn(8);
        lvl.setMode(true);
        break;
      case "items":
        switchLeftColumn(2);
        switchRightColumn(8);
        lvl.setMode(false);
        break;
      case "enemies":
        switchLeftColumn(1);
        switchRightColumn(8);
        lvl.setMode(false);
        break;
      
      // TILES
      // 45 tile 
      case "45tile":
        switchRightColumn(0, tileDirection);
        break;
      case "45tileoff":
        lvl.setMode(-1);
        switchRightColumn(8);
        break;
      case "45tileQ":
        tileDirection = 0;
        lvl.setMode(2);
        break;
      case "45tileW":
        tileDirection = 1;
        lvl.setMode(3);
        break;
      case "45tileA":
        tileDirection = 2;
        lvl.setMode(5);
        break;
      case "45tileS":
        tileDirection = 3;
        lvl.setMode(4);
        break;
      
      // 63 thin tile
      case "63thintile":
        switchRightColumn(1, tileDirection);
        break;
      case "63thintileoff":
        lvl.setMode(-1);
        switchRightColumn(8);
        break;
      case "63thintileQ":
        tileDirection = 0;
        lvl.setMode(6);
        break;
      case "63thintileW":
        tileDirection = 1;
        lvl.setMode(7);
        break;
      case "63thintileA":
        tileDirection = 2;
        lvl.setMode(9);
        break;
      case "63thintileS":
        tileDirection = 3;
        lvl.setMode(8);
        break;
        
      // 27 thin tile
      case "27thintile":
        switchRightColumn(2, tileDirection);
        break;
      case "27thintileoff":
        lvl.setMode(-1);
        switchRightColumn(8);
        break;
      case "27thintileQ":
        tileDirection = 0;
        lvl.setMode(10);
        break;
      case "27thintileW":
        tileDirection = 1;
        lvl.setMode(11);
        break;
      case "27thintileA":
        tileDirection = 2;
        lvl.setMode(13);
        break;
      case "27thintileS":
        tileDirection = 3;
        lvl.setMode(12);
        break;
      
      // Concave tile
      case "Concavetile":
        switchRightColumn(3, tileDirection);
        break;
      case "Concavetileoff":
        lvl.setMode(-1);
        switchRightColumn(8);
        break;
      case "ConcavetileQ":
        tileDirection = 0;
        lvl.setMode(14);
        break;
      case "ConcavetileW":
        tileDirection = 1;
        lvl.setMode(15);
        break;
      case "ConcavetileA":
        tileDirection = 2;
        lvl.setMode(17);
        break;
      case "ConcavetileS":
        tileDirection = 3;
        lvl.setMode(16);
        break;
      
      // Half tile
      case "Halftile":
        switchRightColumn(4, tileDirection);
        break;
      case "Halftileoff":
        lvl.setMode(-1);
        switchRightColumn(8);
        break;
      case "HalftileQ":
        tileDirection = 0;
        lvl.setMode(18);
        break;
      case "HalftileW":
        tileDirection = 1;
        lvl.setMode(19);
        break;
      case "HalftileA":
        tileDirection = 2;
        lvl.setMode(21);
        break;
      case "HalftileS":
        tileDirection = 3;
        lvl.setMode(20);
        break;
      
      // 63 thick tile
      case "63thicktile":
        switchRightColumn(5, tileDirection);
        break;
      case "63thicktileoff":
        lvl.setMode(-1);
        switchRightColumn(8);
        break;
      case "63thicktileQ":
        tileDirection = 0;
        lvl.setMode(22);
        break;
      case "63thicktileW":
        tileDirection = 1;
        lvl.setMode(23);
        break;
      case "63thicktileA":
        tileDirection = 2;
        lvl.setMode(25);
        break;
      case "63thicktileS":
        tileDirection = 3;
        lvl.setMode(24);
        break;
      
      // 27 thick tile
      case "27thicktile":
        switchRightColumn(6, tileDirection);
        break;
      case "27thicktileoff":
        lvl.setMode(-1);
        switchRightColumn(8);
        break;
      case "27thicktileQ":
        tileDirection = 0;
        lvl.setMode(26);
        break;
      case "27thicktileW":
        tileDirection = 1;
        lvl.setMode(27);
        break;
      case "27thicktileA":
        tileDirection = 2;
        lvl.setMode(29);
        break;
      case "27thicktileS":
        tileDirection = 3;
        lvl.setMode(28);
        break;
      
      // Convex tile
      case "Convextile":
        switchRightColumn(7, tileDirection);
        break;
      case "Convextileoff":
        lvl.setMode(-1);
        switchRightColumn(8);
        break;
      case "ConvextileQ":
        tileDirection = 0;
        lvl.setMode(30);
        break;
      case "ConvextileW":
        tileDirection = 1;
        lvl.setMode(31);
        break;
      case "ConvextileA":
        tileDirection = 2;
        lvl.setMode(33);
        break;
      case "ConvextileS":
        tileDirection = 3;
        lvl.setMode(32);
        break;
      
      // Erase
      case "Erase":
        switchRightColumn(8);
        lvl.setMode(0);
        break;
      case "Eraseoff":
        lvl.setMode(-1);
        break;
      
      // Fill
      case "Fill":
        switchRightColumn(9);
        lvl.setMode(1);
        break;
      case "Filloff":
        lvl.setMode(-1);
        break;
        
      // ENEMIES
      // Gauss turret
      case "Gaussturret":
        switchRightColumn(10);
        lvl.setMode(LevelArea.GAUSS);
        break;
      case "Gaussturretoff":
        lvl.setMode(-1);
        break;
      
      // Homing launcher
      case "Hominglauncher":
        switchRightColumn(11);
        lvl.setMode(LevelArea.HOMING);
        break;
      case "Hominglauncheroff":
        lvl.setMode(-1);
        break;
      
      // Mine
      case "Mine":
        switchRightColumn(12);
        lvl.setMode(LevelArea.MINE);
        break;
      case "Mineoff":
        lvl.setMode(-1);
        break;
      
      // Floor guard
      case "Floorguard":
        switchRightColumn(13);
        lvl.setMode(LevelArea.FLOOR);
        break;
      case "Floorguardoff":
        lvl.setMode(-1);
        break;
 
      // Thwump
      case "Thwump":
        switchRightColumn(14, itemDirection / 2);
        break;
      case "Thwumpoff":
        switchRightColumn(8);
        lvl.setMode(-1);
        break;
      case "ThwumpA":
        lvl.setMode(LevelArea.THWUMP + 2);
        itemDirection = itemDirection % 2 + 4;
        break;
      case "ThwumpD":
        lvl.setMode(LevelArea.THWUMP);
        itemDirection = itemDirection % 2;
        break;
      case "ThwumpS":
        lvl.setMode(LevelArea.THWUMP + 1);
        itemDirection = itemDirection % 2 + 2;
        break;
      case "ThwumpW":
        lvl.setMode(LevelArea.THWUMP + 3);
        itemDirection = itemDirection % 2 + 6;
        break;
      
      // Zap drone
      case "Zapdrone":
        switchRightColumn(15, itemDirection / 2, 4 + droneBehavior);
        break;
      case "Zapdroneoff":
        switchRightColumn(8);
        lvl.setMode(-1);
        break;
      case "ZapdroneA":
        itemDirection = itemDirection % 2 + 4;
        lvl.setMode(LevelArea.ZAP + 14 + droneBehavior);
        break;
      case "ZapdroneD":
        itemDirection = itemDirection % 2;
        lvl.setMode(LevelArea.ZAP + droneBehavior);
        break;
      case "ZapdroneS":
        itemDirection = itemDirection % 2 + 2;
        lvl.setMode(LevelArea.ZAP + 7 + droneBehavior);
        break;
      case "ZapdroneW":
        itemDirection = itemDirection % 2 + 6;
        lvl.setMode(LevelArea.ZAP + 21 + droneBehavior);
        break;
      case "Zapdronedumbcw":
        droneBehavior=2;
        lvl.setMode(LevelArea.ZAP + 7 * itemDirection / 2 + 2);
        break;
      case "Zapdronedumbccw":
        droneBehavior=3;
        lvl.setMode(LevelArea.ZAP + 7 * itemDirection / 2 + 3);
        break;
      case "Zapdronesurfacecw":
        droneBehavior=0;
        lvl.setMode(LevelArea.ZAP + 7 * itemDirection / 2 + 0);
        break;
      case "Zapdronesurfaceccw":
        droneBehavior=1;
        lvl.setMode(LevelArea.ZAP + 7 * itemDirection / 2 + 1);
        break;
      case "Zapdronealt":
        droneBehavior=4;
        lvl.setMode(LevelArea.ZAP + 7 * itemDirection / 2 + 4);
        break;
      case "Zapdronerand":
        droneBehavior=5;
        lvl.setMode(LevelArea.ZAP + 7 * itemDirection / 2 + 5);
        break;
      case "Zapdronedumbcwoff":
        // fall through
      case "Zapdronedumbccwoff":
        // fall through
      case "Zapdronesurfacecwoff":
        // fall through
      case "Zapdronesurfaceccwoff":
        // fall through
      case "Zapdronealtoff":
        // fall through
      case "Zapdronerandoff":
        droneBehavior=6;
        lvl.setMode(LevelArea.ZAP + 7 * itemDirection / 2 + 6);
        break;
      
      // Seeker drone
      case "Seekerdrone":
        switchRightColumn(16, itemDirection / 2, 4 + droneBehavior);
        break;
      case "Seekerdroneoff":
        switchRightColumn(8);
        lvl.setMode(-1);
        break;
      case "SeekerdroneA":
        itemDirection = itemDirection % 2 + 4;
        lvl.setMode(LevelArea.SEEKER + 14 + droneBehavior);
        break;
      case "SeekerdroneD":
        itemDirection = itemDirection % 2;
        lvl.setMode(LevelArea.SEEKER + droneBehavior);
        break;
      case "SeekerdroneS":
        itemDirection = itemDirection % 2 + 2;
        lvl.setMode(LevelArea.SEEKER + 7 + droneBehavior);
        break;
      case "SeekerdroneW":
        itemDirection = itemDirection % 2 + 6;
        lvl.setMode(LevelArea.SEEKER + 21 + droneBehavior);
        break;
      case "Seekerdronedumbcw":
        droneBehavior=2;
        lvl.setMode(LevelArea.SEEKER + 7 * itemDirection / 2 + 2);
        break;
      case "Seekerdronedumbccw":
        droneBehavior=3;
        lvl.setMode(LevelArea.SEEKER + 7 * itemDirection / 2 + 3);
        break;
      case "Seekerdronesurfacecw":
        droneBehavior=0;
        lvl.setMode(LevelArea.SEEKER + 7 * itemDirection / 2 + 0);
        break;
      case "Seekerdronesurfaceccw":
        droneBehavior=1;
        lvl.setMode(LevelArea.SEEKER + 7 * itemDirection / 2 + 1);
        break;
      case "Seekerdronealt":
        droneBehavior=4;
        lvl.setMode(LevelArea.SEEKER + 7 * itemDirection / 2 + 4);
        break;
      case "Seekerdronerand":
        droneBehavior=5;
        lvl.setMode(LevelArea.SEEKER + 7 * itemDirection / 2 + 5);
        break;
      case "Seekerdronedumbcwoff":
        // fall through
      case "Seekerdronedumbccwoff":
        // fall through
      case "Seekerdronesurfacecwoff":
        // fall through
      case "Seekerdronesurfaceccwoff":
        // fall through
      case "Seekerdronealtoff":
        // fall through
      case "Seekerdronerandoff":
        droneBehavior=6;
        lvl.setMode(LevelArea.SEEKER + 7 * itemDirection / 2 + 6);
        break;

      // Laser drone
      case "Laserdrone":
        switchRightColumn(17, itemDirection / 2, 4 + droneBehavior);
        break;
      case "Laserdroneoff":
        switchRightColumn(8);
        lvl.setMode(-1);
        break;
      case "LaserdroneA":
        itemDirection = itemDirection % 2 + 4;
        lvl.setMode(LevelArea.LASER + 14 + droneBehavior);
        break;
      case "LaserdroneD":
        itemDirection = itemDirection % 2;
        lvl.setMode(LevelArea.LASER + droneBehavior);
        break;
      case "LaserdroneS":
        itemDirection = itemDirection % 2 + 2;
        lvl.setMode(LevelArea.LASER + 7 + droneBehavior);
        break;
      case "LaserdroneW":
        itemDirection = itemDirection % 2 + 6;
        lvl.setMode(LevelArea.LASER + 21 + droneBehavior);
        break;
      case "Laserdronedumbcw":
        droneBehavior=2;
        lvl.setMode(LevelArea.LASER + 7 * itemDirection / 2 + 2);
        break;
      case "Laserdronedumbccw":
        droneBehavior=3;
        lvl.setMode(LevelArea.LASER + 7 * itemDirection / 2 + 3);
        break;
      case "Laserdronesurfacecw":
        droneBehavior=0;
        lvl.setMode(LevelArea.LASER + 7 * itemDirection / 2 + 0);
        break;
      case "Laserdronesurfaceccw":
        droneBehavior=1;
        lvl.setMode(LevelArea.LASER + 7 * itemDirection / 2 + 1);
        break;
      case "Laserdronealt":
        droneBehavior=4;
        lvl.setMode(LevelArea.LASER + 7 * itemDirection / 2 + 4);
        break;
      case "Laserdronerand":
        droneBehavior=5;
        lvl.setMode(LevelArea.LASER + 7 * itemDirection / 2 + 5);
        break;
      case "Laserdronedumbcwoff":
        // fall through
      case "Laserdronedumbccwoff":
        // fall through
      case "Laserdronesurfacecwoff":
        // fall through
      case "Laserdronesurfaceccwoff":
        // fall through
      case "Laserdronealtoff":
        // fall through
      case "Laserdronerandoff":
        droneBehavior=6;
        lvl.setMode(LevelArea.LASER + 7 * itemDirection / 2 + 6);
        break;

      // Chaingun drone
      case "Chaingundrone":
        switchRightColumn(18, itemDirection / 2, 4 + droneBehavior);
        break;
      case "Chaingundroneoff":
        switchRightColumn(8);
        lvl.setMode(-1);
        break;
      case "ChaingundroneA":
        itemDirection = itemDirection % 2 + 4;
        lvl.setMode(LevelArea.CHAINGUN + 14 + droneBehavior);
        break;
      case "ChaingundroneD":
        itemDirection = itemDirection % 2;
        lvl.setMode(LevelArea.CHAINGUN + droneBehavior);
        break;
      case "ChaingundroneS":
        itemDirection = itemDirection % 2 + 2;
        lvl.setMode(LevelArea.CHAINGUN + 7 + droneBehavior);
        break;
      case "ChaingundroneW":
        itemDirection = itemDirection % 2 + 6;
        lvl.setMode(LevelArea.CHAINGUN + 21 + droneBehavior);
        break;
      case "Chaingundronedumbcw":
        droneBehavior=2;
        lvl.setMode(LevelArea.CHAINGUN + 7 * itemDirection / 2 + 2);
        break;
      case "Chaingundronedumbccw":
        droneBehavior=3;
        lvl.setMode(LevelArea.CHAINGUN + 7 * itemDirection / 2 + 3);
        break;
      case "Chaingundronesurfacecw":
        droneBehavior=0;
        lvl.setMode(LevelArea.CHAINGUN + 7 * itemDirection / 2 + 0);
        break;
      case "Chaingundronesurfaceccw":
        droneBehavior=1;
        lvl.setMode(LevelArea.CHAINGUN + 7 * itemDirection / 2 + 1);
        break;
      case "Chaingundronealt":
        droneBehavior=4;
        lvl.setMode(LevelArea.CHAINGUN + 7 * itemDirection / 2 + 4);
        break;
      case "Chaingundronerand":
        droneBehavior=5;
        lvl.setMode(LevelArea.CHAINGUN + 7 * itemDirection / 2 + 5);
        break;
      case "Chaingundronedumbcwoff":
        // fall through
      case "Chaingundronedumbccwoff":
        // fall through
      case "Chaingundronesurfacecwoff":
        // fall through
      case "Chaingundronesurfaceccwoff":
        // fall through
      case "Chaingundronealtoff":
        // fall through
      case "Chaingundronerandoff":
        droneBehavior=6;
        lvl.setMode(LevelArea.CHAINGUN + 7 * itemDirection / 2 + 6);
        break;
      
      // ITEMS
      // Player
      case "Player":
        switchRightColumn(19);
        lvl.setMode(LevelArea.PLAYER);
        break;
      case "Playeroff":
        lvl.setMode(-1);
        break;
      
      // Gold
      case "Gold":
        switchRightColumn(20);
        lvl.setMode(LevelArea.GOLD);
        break;
      case "Goldoff":
        lvl.setMode(-1);
        break;
        
      // Bounce block
      case "Bounceblock":
        switchRightColumn(21);
        lvl.setMode(LevelArea.BOUNCE);
        break;
      case "Bounceblockoff":
        lvl.setMode(-1);
        break;
      
      // Exit
      case "Exitdoor":
        switchRightColumn(22);
        lvl.setMode(LevelArea.EXIT);
        break;
      case "Exitdooroff":
        lvl.setMode(-1);
        break;
      
      // Oneway platform
      case "Oneway":
        switchRightColumn(23, itemDirection / 2);
        break;
      case "Onewayoff":
        switchRightColumn(8);
        lvl.setMode(-1);
        break;
      case "OnewayA":
        lvl.setMode(LevelArea.ONEWAY + 2);
        itemDirection = itemDirection % 2 + 4;
        break;
      case "OnewayD":
        lvl.setMode(LevelArea.ONEWAY);
        itemDirection = itemDirection % 2;
        break;
      case "OnewayS":
        lvl.setMode(LevelArea.ONEWAY + 1);
        itemDirection = itemDirection % 2 + 2;
        break;
      case "OnewayW":
        lvl.setMode(LevelArea.ONEWAY + 3);
        itemDirection = itemDirection % 2 + 6;
        break;
      
      // Normal door
      case "Normaldoor":
        switchRightColumn(24, itemDirection / 2);
        break;
      case "Normaldooroff":
        switchRightColumn(8);
        lvl.setMode(-1);
        break;
      case "NormaldoorA":
        lvl.setMode(LevelArea.NDOOR + 2);
        itemDirection = itemDirection % 2 + 4;
        break;
      case "NormaldoorD":
        lvl.setMode(LevelArea.NDOOR);
        itemDirection = itemDirection % 2;
        break;
      case "NormaldoorS":
        lvl.setMode(LevelArea.NDOOR + 1);
        itemDirection = itemDirection % 2 + 2;
        break;
      case "NormaldoorW":
        lvl.setMode(LevelArea.NDOOR + 3);
        itemDirection = itemDirection % 2 + 6;
        break;
      
      // Locked door
      case "Lockeddoor":
        switchRightColumn(25, itemDirection / 2);
        break;
      case "Lockeddooroff":
        switchRightColumn(8);
        lvl.setMode(-1);
        break;
      case "LockeddoorA":
        lvl.setMode(LevelArea.LDOOR + 2);
        itemDirection = itemDirection % 2 + 4;
        break;
      case "LockeddoorD":
        lvl.setMode(LevelArea.LDOOR);
        itemDirection = itemDirection % 2;
        break;
      case "LockeddoorS":
        lvl.setMode(LevelArea.LDOOR + 1);
        itemDirection = itemDirection % 2 + 2;
        break;
      case "LockeddoorW":
        lvl.setMode(LevelArea.LDOOR + 3);
        itemDirection = itemDirection % 2 + 6;
        break;
      
      // Trap door
      case "Trapdoor":
        switchRightColumn(26, itemDirection / 2);
        break;
      case "Trapdooroff":
        switchRightColumn(8);
        lvl.setMode(-1);
        break;
      case "TrapdoorA":
        lvl.setMode(LevelArea.TDOOR + 2);
        itemDirection = itemDirection % 2 + 4;
        break;
      case "TrapdoorD":
        lvl.setMode(LevelArea.TDOOR);
        itemDirection = itemDirection % 2;
        break;
      case "TrapdoorS":
        lvl.setMode(LevelArea.TDOOR + 1);
        itemDirection = itemDirection % 2 + 2;
        break;
      case "TrapdoorW":
        lvl.setMode(LevelArea.TDOOR + 3);
        itemDirection = itemDirection % 2 + 6;
        break;
      
      // Launchpad
      case "Launchpad":
        switchRightColumn(27, itemDirection);
        break;
      case "Launchpadoff":
        switchRightColumn(8);
        lvl.setMode(-1);
        break;
      case "LaunchpadA":
        lvl.setMode(LevelArea.LAUNCH + 4);
        itemDirection = 4;
        break;
      case "LaunchpadAS":
        lvl.setMode(LevelArea.LAUNCH + 3);
        itemDirection = 3;
        break;
      case "LaunchpadAW":
        lvl.setMode(LevelArea.LAUNCH + 5);
        itemDirection = 5;
        break;
      case "LaunchpadD":
        lvl.setMode(LevelArea.LAUNCH);
        itemDirection = 0;
        break;
      case "LaunchpadDS":
        lvl.setMode(LevelArea.LAUNCH + 1);
        itemDirection = 1;
        break;
      case "LaunchpadDW":
        lvl.setMode(LevelArea.LAUNCH + 7);
        itemDirection = 7;
        break;
      case "LaunchpadS":
        lvl.setMode(LevelArea.LAUNCH + 2);
        itemDirection = 2;
        break;
      case "LaunchpadW":
        lvl.setMode(LevelArea.LAUNCH + 6);
        itemDirection = 6;
        break;
      
      // Text box buttons
      case "cpylvl":
        tbox.copyToClipboard();
        break;
      case "pstlvl":
        tbox.pasteFromClipboard();
        break;
      case "tboxlvl":
        lvl.inputLevel(tbox.getText());
        break;
      case "undo":
        tbox.setText(hist.undo());
        lvl.inputLevel(tbox.getText());
        break;
      case "redo":
        tbox.setText(hist.redo());
        lvl.inputLevel(tbox.getText());
        break;
      case "Exit":
        System.exit(0);
        break;
      
      // Gridline panel buttons
      case "gridlines":
        gridDropPanel.setVisible(true);
        break;
      case "gridtoggle":
        lvl.setDrawGrid(true);
        break;
      case "gridtoggleoff":
        lvl.setDrawGrid(false);
        break;
      case "grsave":
        tfGridSaveName.setText("");
        gridSaveDialog.setVisible(true);
        break;
      case "grdelete":
        gridDeleteDialog.setVisible(true);
        break;
      case "grpri":
        gridOverlays[overlayIndex].setActive(false);
        overlayIndex = 0;
        gridOverlays[overlayIndex].setActive(true);
        break;
      case "grsec":
        gridOverlays[overlayIndex].setActive(false);
        overlayIndex = 1;
        gridOverlays[overlayIndex].setActive(true);
        break;
      case "grter":
        gridOverlays[overlayIndex].setActive(false);
        overlayIndex = 2;
        gridOverlays[overlayIndex].setActive(true);
        break;
      case "gridlinesoff":
        gridDropPanel.setVisible(false);
        break;
      case "grsinglebt":
        if (gridOverlays != null) {
          gridOverlays[overlayIndex].push("single");
          lGridDoubleLine.setVisible(false);
          tfGridDoubleLine.setVisible(false);
        }
        break;
      case "grdoublebt":
        if (gridOverlays != null) {
          gridOverlays[overlayIndex].push("double");
          lGridDoubleLine.setVisible(true);
          tfGridDoubleLine.setVisible(true);
        }
        break;
      case "gronoff":
        if(gridOverlays != null)
          gridOverlays[overlayIndex].push("on");
        break;
      case "gronoffoff":
        if(gridOverlays != null)
          gridOverlays[overlayIndex].push("off");
        break;
      case "tfGridSaveName":
        // fall through
      case "grsavesave":
        saveGridLines(tfGridSaveName.getText());
        gridSaveDialog.setVisible(false);
        break;
      case "grsavecancel":
        gridSaveDialog.setVisible(false);
        break;
      case "grdeletedelete":
        deleteGridLines((String)gridSelect.getSelectedItem());
        gridDeleteDialog.setVisible(false);
        break;
      case "grdeletecancel":
        gridDeleteDialog.setVisible(false);
        break;
      
      // Snap panel buttons
      case "snapping":
        snapDropPanel.setVisible(true);
        break;
      case "snaptoggle":
        lvl.setSnapping(true);
        break;
      case "snaptoggleoff":
        lvl.setSnapping(false);
        break;
      case "snsave":
        tfSnapSaveName.setText("");
        snapSaveDialog.setVisible(true);
        break;
      case "sndelete":
        snapDeleteDialog.setVisible(true);
        break;
      case "snappingoff":
        snapDropPanel.setVisible(false);
        break;
      case "snshow":
        lvl.setDrawSnapPoints(true);
        break;
      case "snshowoff":
        lvl.setDrawSnapPoints(false);
        break;
      case "tfSnapSaveName":
        // fall through
      case "snsavesave":
        saveSnapPoints(tfSnapSaveName.getText());
        snapSaveDialog.setVisible(false);
        break;
      case "snsavecancel":
        snapSaveDialog.setVisible(false);
        break;
      case "sndeletedelete":
        deleteSnapPoints((String)snapSelect.getSelectedItem());
        snapDeleteDialog.setVisible(false);
        break;
      case "sndeletecancel":
        snapDeleteDialog.setVisible(false);
        break;
      
      // Triggers and paths
      case "trigger":
        lvl.setDrawTriggers(true);
        break;
      case "triggeroff":
        lvl.setDrawTriggers(false);
        break;
      case "dpath":
        lvl.setDrawDronePaths(true);
        break;
      case "dpathoff":
        lvl.setDrawDronePaths(false);
        break;
        
      // Menu bar
      case "new":
        newLevel();
        break;
      case "save":
        if (savedAs) {
          Nfile usrlvls = new Nfile(userlevels);
          usrlvls.setData(tbox.getText(), lvlName);
          usrlvls.close();
        } else {
          fileChooser.open(true);
        }
        break;
      case "saveAs":
        fileChooser.open(true);
        break;
      case "open":
        fileChooser.open(false);
        break;
      case "keyShortcuts":
        keyShortcuts.open();
        break;
      
      // Other
      default:
        String[] multicmd = button.split("#");
        if (multicmd.length > 1) {
          switch (multicmd[0]) {
            case "action":
              try { // menu buttons linked to action numbers use this roundabout mechanism
                doActionNumber(Integer.parseInt(multicmd[1]));
              } catch (NumberFormatException e) {}
              break;
            case "gridlineSetting":
              gridSelect.setSelectedItem(multicmd[1]);
              break;
            case "snapSetting":
              snapSelect.setSelectedItem(multicmd[1]);
              break;
            default:
          }
        }
    }
    repaint();
  }
  // Column multibutton switching
  private void switchLeftColumn (int index) {
    for (int i = 0; i <= 9; i++) {
      mbuttons[i].setIndex(index);
    }
  }
  private void switchRightColumn (int index) {
     switchRightColumn(index, -1, -1);
  }
  private void switchRightColumn (int index, int pushedIndex) {
    switchRightColumn(index, pushedIndex, -1);
  }
  private void switchRightColumn (int index, int pushedIndex, int pushedIndex2) {
    for (int i = 0; i <= 9; i++) {
      mbuttons[i + 10].setIndex(index, i == pushedIndex || i == pushedIndex2);
    }
  }

  // TASK - verify usage
  public boolean drawImage(int index, int xor, int yor, Graphics g) {
    BufferedImage image = imgBank.get(index);
    if(image == null) return false;
    g.drawImage(image, xor + imgBank.getXoff(index), yor + imgBank.getYoff(index), null);
    return true;
  }
  
  public BufferedImage img(int index) { // TASK verify usage
    return imgBank.get(index);
  }
  
  public boolean isShiftPushed() {
    return keys.isShiftPushed();
  }
  
  // TASK - move all these operations inside the Jned constructor
  public static void main (String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Jned jned = new Jned(frame);
    frame.getContentPane().add(jned);
    frame.setResizable(false);
    frame.pack();
    frame.setVisible(true);
    jned.grabFocus();
  }

  public void pushDelete() {
    lvl.pushDelete();
    repaint();
  }

  public void mouseClicked(MouseEvent me) {}
  public void mousePressed(MouseEvent me) {}
  public void mouseReleased(MouseEvent me) {}
  public void mouseEntered(MouseEvent me) {
    grabFocus();
  }
  public void mouseExited(MouseEvent me) {}
}