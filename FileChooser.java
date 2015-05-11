import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

/**
 * The window that displays all the files in the userlevels file linked up with Jned and allows you
 * to open or save them.
 * @author James Porter
 */
public class FileChooser extends JPanel implements ActionListener, DocumentListener {
  public static Font LEVEL_FONT = new Font(Font.MONOSPACED,Font.PLAIN, 10);
  
  // GUI spacing constants
  public static int WINDOW_WIDTH = 640;
  public static int BORDER = 4;
  public static int ROW_HEIGHT = 24;
  public static int BUTTON_WIDTH = 64;
  public static int LVLFL_WIDTH = 96;
  public static int SCROLLPANE_HEIGHT = 256;
  public static int OVERWRITE_WIDTH = 320;
  
  private Jned jned;
  private Nfile config;
  private String userlevels;
  private JLabel lFileName;
  
  private JDialog mainDialog;
  private JDialog overwriteDialog;
  private JFileChooser fileChooser;
  
  private JLabel lName;
  private JLabel lAuthor;
  private JLabel lGenre;
  
  // Percentage of width for LevelButton labels
  private static double BUTTON_LEVEL_WEIGHT = 0.07;
  private static double BUTTON_AUTHOR_WEIGHT = 0.45;
  private static double BUTTON_GENRE_WEIGHT = 0.75;
  
  // Level buttons
  private int levelButtonWidth;
  private int buttonLevelWidth;
  private int buttonAuthorWidth;
  private int buttonGenreWidth;
  private JPanel levelsPanel;
  private JViewport viewPort;
  private int viewPortHeight;
  private int listSize;
  private LevelButton[] levelButtons;
  private int selectedLevel;
  
  // Window bottom/mode switching variables
  private boolean isSaving; 
  private int openButtonsYValue;
  private JTextField tfName;
  private JTextField tfAuthor;
  private JTextField tfGenre;
  private int saveButtonsYValue;
  private Button doTheThing;
  private Button cancel;
  private int saveWindowHeight;
  
  /**
   * Constructs a new FileChooser.
   * @param jned a reference to the associated Jned instance
   * @param frame the JFrame that owns this FileChooser's dialog windows
   * @param config the Nfile wrapping the Jned config file
   * @param userlevels a String of the filepath to the userlevels file to read/write levels
   */
  public FileChooser (Jned jned, JFrame frame, Nfile config, String userlevels) {
    this.jned = jned;
    this.config = config;
    this.userlevels = userlevels;
    
    selectedLevel = -1;
    listSize = -1;
    
    fileChooser = new JFileChooser(".\\");
    FileNameExtensionFilter filter = new FileNameExtensionFilter("text files", "txt");
    fileChooser.setFileFilter(filter);
    
    mainDialog = new JDialog(frame);
    mainDialog.getContentPane().add(this);
    setBackground(Colors.BG_COLOR);
    setLayout(null);
    
    // Levels file
    int ycount = FileChooser.BORDER;
    add(makeJLabel("Levels file:", FileChooser.BORDER, ycount, FileChooser.LVLFL_WIDTH,
        FileChooser.ROW_HEIGHT, 1));
    lFileName = makeJLabel(userlevels, FileChooser.LVLFL_WIDTH + 2 * FileChooser.BORDER,
        ycount, FileChooser.WINDOW_WIDTH - 4 * FileChooser.BORDER - FileChooser.BUTTON_WIDTH
        - FileChooser.LVLFL_WIDTH, FileChooser.ROW_HEIGHT, -1);
    add(lFileName);
    add(new Button(this, "fileChange", -2, FileChooser.WINDOW_WIDTH - FileChooser.BORDER -
        FileChooser.BUTTON_WIDTH, ycount, FileChooser.BUTTON_WIDTH, FileChooser.ROW_HEIGHT,
        true, "Change"));
    ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;
    
    levelButtonWidth = FileChooser.WINDOW_WIDTH - FileChooser.BORDER * 4 - 17; // the 17 is
    // for the width of the scrollbar
    buttonLevelWidth = (int) (FileChooser.BUTTON_LEVEL_WEIGHT * levelButtonWidth);
    buttonAuthorWidth = (int) (FileChooser.BUTTON_AUTHOR_WEIGHT * levelButtonWidth);
    buttonGenreWidth = (int) (FileChooser.BUTTON_GENRE_WEIGHT * levelButtonWidth);
    
    // Column label buttons
    add(new Button(this, "lvlName", -2, buttonLevelWidth + FileChooser.BORDER * 2, ycount,
        FileChooser.BUTTON_WIDTH * 2, FileChooser.ROW_HEIGHT, true, "Level Name"));
    add(new Button(this, "lvlAuthor", -2, buttonAuthorWidth + FileChooser.BORDER * 2, ycount,
        FileChooser.BUTTON_WIDTH, FileChooser.ROW_HEIGHT, true, "Author"));
    add(new Button(this, "lvlGenre", -2, buttonGenreWidth + FileChooser.BORDER * 2, ycount,
        FileChooser.BUTTON_WIDTH, FileChooser.ROW_HEIGHT, true, "Type"));
    
    // Level buttons
    ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;
    levelsPanel = new JPanel();
    levelsPanel.setBackground(Colors.BG_COLOR);
    levelsPanel.setPreferredSize(new Dimension(FileChooser.WINDOW_WIDTH - 17, 512));
    levelsPanel.setLayout(null);
    JScrollPane sp = new JScrollPane(levelsPanel);
    viewPort = sp.getViewport();
    sp.setBounds(FileChooser.BORDER, ycount, FileChooser.WINDOW_WIDTH - 2 *
        FileChooser.BORDER, FileChooser.SCROLLPANE_HEIGHT);
    sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    add(sp);
    
    ycount += FileChooser.BORDER + FileChooser.SCROLLPANE_HEIGHT;
    openButtonsYValue = ycount;
    
    // Textfields and labels for save mode
    int authwidth = 45; // TASK - find better way to get width of these labels
    int genwidth = 35;
    lName = makeJLabel("Name:", FileChooser.BORDER, ycount, buttonLevelWidth,
        FileChooser.ROW_HEIGHT, 1);
    tfName = new JTextField();
    tfName.getDocument().addDocumentListener(this);
    tfName.setFont(Jned.BOX_FONT);
    tfName.setHorizontalAlignment(JTextField.LEFT);
    tfName.setBounds(buttonLevelWidth + FileChooser.BORDER * 2, ycount, buttonAuthorWidth -
        buttonLevelWidth - authwidth - 2 * FileChooser.BORDER, FileChooser.ROW_HEIGHT);
    lAuthor = makeJLabel("Author:", buttonAuthorWidth + FileChooser.BORDER - authwidth,
        ycount, authwidth, FileChooser.ROW_HEIGHT, 1);
    tfAuthor = new JTextField();
    tfAuthor.getDocument().addDocumentListener(this);
    tfAuthor.setFont(Jned.BOX_FONT);
    tfAuthor.setHorizontalAlignment(JTextField.LEFT);
    tfAuthor.setBounds(buttonAuthorWidth + FileChooser.BORDER * 2, ycount, buttonGenreWidth -
        buttonAuthorWidth - genwidth - 2 * FileChooser.BORDER, FileChooser.ROW_HEIGHT);
    lGenre = makeJLabel("Type:", buttonGenreWidth + FileChooser.BORDER - genwidth, ycount,
        genwidth, FileChooser.ROW_HEIGHT, 1);
    tfGenre = new JTextField();
    tfGenre.getDocument().addDocumentListener(this);
    tfGenre.setFont(Jned.BOX_FONT);
    tfGenre.setHorizontalAlignment(JTextField.LEFT);
    tfGenre.setBounds(buttonGenreWidth + FileChooser.BORDER * 2, ycount, levelButtonWidth -
        buttonGenreWidth, FileChooser.ROW_HEIGHT);
  
    ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;
    saveButtonsYValue = ycount;
    
    // Open/Save and Cancel buttons
    doTheThing = new Button(this, "doTheThing", -2, FileChooser.BORDER, ycount,
        FileChooser.BUTTON_WIDTH, FileChooser.ROW_HEIGHT, true, "");
    add(doTheThing);
    cancel = new Button(this, "cancel", -2, FileChooser.WINDOW_WIDTH - FileChooser.BORDER -
        FileChooser.BUTTON_WIDTH, ycount, FileChooser.BUTTON_WIDTH, FileChooser.ROW_HEIGHT,
        true, "Cancel");
    add(cancel);
    
    ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;
    saveWindowHeight = ycount;
    
    setPreferredSize(new Dimension(FileChooser.WINDOW_WIDTH, ycount));
    mainDialog.setLocationRelativeTo(null);
    mainDialog.setResizable(false);
    mainDialog.pack();
    
    // Overwrite dialog
    overwriteDialog = new JDialog(frame, "Overwrite?", true);
    overwriteDialog.getContentPane().setBackground(Colors.BG_COLOR);
    overwriteDialog.setLayout(null);
    
    ycount = FileChooser.BORDER;
    overwriteDialog.add(makeJLabel("Are you sure you want to overwrite this level?",
        FileChooser.BORDER, ycount, FileChooser.OVERWRITE_WIDTH - 2 * FileChooser.BORDER,
        FileChooser.ROW_HEIGHT, 0));
    ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;
    overwriteDialog.add(new Button(this, "overwriteOK", -2, FileChooser.BORDER, ycount,
        FileChooser.BUTTON_WIDTH, FileChooser.ROW_HEIGHT, true, "OK"));
    overwriteDialog.add(new Button(this, "overwriteCancel", -2, FileChooser.OVERWRITE_WIDTH
        - 2 * FileChooser.BORDER - FileChooser.BUTTON_WIDTH, ycount,
        FileChooser.BUTTON_WIDTH, FileChooser.ROW_HEIGHT, true, "Cancel"));
    ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;
    
    overwriteDialog.setLocationRelativeTo(null);
    overwriteDialog.getContentPane().setPreferredSize(new Dimension(
        FileChooser.OVERWRITE_WIDTH, ycount));
    overwriteDialog.pack();
  }
  
  private JLabel makeJLabel(String txt, int x, int y, int width, int height, int align) {
    JLabel lbl = new JLabel(txt, (align == -1 ? SwingConstants.LEFT : (align == 1 ?
        SwingConstants.RIGHT : SwingConstants.CENTER)));
    lbl.setBounds(x, y, width, height);
    lbl.setForeground(Color.BLACK);
    return lbl;
  }
  
  /**
   * Opens the file saving dialog, setting it to either save a level or open a level.
   * @param isSaving true to save a level, false to open a level
   */
  public void open(boolean isSaving) {
    this.isSaving = isSaving;
    select(-1);
    mainDialog.setTitle(isSaving ? "Save level" : "Open level");
    doTheThing.setLabel(isSaving ? "Save" : "Open");
    
    remove(lName);
    remove(tfName);
    remove(lAuthor);
    remove(tfAuthor);
    remove(lGenre);
    remove(tfGenre);
    if (isSaving) {
      add(lName);
      add(tfName);
      add(lAuthor);
      add(tfAuthor);
      add(lGenre);
      add(tfGenre);
      String[] atts = jned.getAttributes();
      tfName.setText(atts[0]);
      tfAuthor.setText(atts[1]);
      tfGenre.setText(atts[2]);
      doTheThing.setLocation(FileChooser.BORDER, saveButtonsYValue);
      doTheThing.setEnabled(true);
      cancel.setLocation(FileChooser.WINDOW_WIDTH - FileChooser.BORDER -
          FileChooser.BUTTON_WIDTH, saveButtonsYValue);
      setPreferredSize(new Dimension(FileChooser.WINDOW_WIDTH, saveWindowHeight));
    } else {
      doTheThing.setLocation(FileChooser.BORDER, openButtonsYValue);
      cancel.setLocation(FileChooser.WINDOW_WIDTH - FileChooser.BORDER -
          FileChooser.BUTTON_WIDTH, openButtonsYValue);
      setPreferredSize(new Dimension(FileChooser.WINDOW_WIDTH, saveButtonsYValue));
    }
    
    populateList();
    mainDialog.pack();
    mainDialog.setVisible(true);
  }
  
  private void populateList() {
    Nfile usrlvls = new Nfile(userlevels);
    String[] names = usrlvls.getNames();
    listSize = names.length;
    levelsPanel.removeAll();
    levelButtons = new LevelButton[listSize];
    int ycount = FileChooser.BORDER;
    for (int i = 0; i < listSize; i++) {
      levelButtons[i] = new LevelButton(this, i, names[i], usrlvls.getAttr1(i),
          usrlvls.getAttr2(i), FileChooser.BORDER, ycount, levelButtonWidth,
          FileChooser.ROW_HEIGHT);
      levelsPanel.add(levelButtons[i]);
      ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;
    }
    levelsPanel.setPreferredSize(new Dimension(FileChooser.WINDOW_WIDTH - 17, ycount));
    usrlvls.close();
    viewPortHeight = viewPort.getExtentSize().height;
  }
  
  public void actionPerformed(ActionEvent e) {
    push(e.getActionCommand());
  }
  
  /**
   * Performs actions appropriate for a button push with the given action command.
   * @param command the action command for desired button push
   */
  public void push(String command) {
    try {
      int buttonNumber = Integer.parseInt(command);
      if (isSaving) {
        tfName.setText(levelButtons[buttonNumber].name);
        tfAuthor.setText(levelButtons[buttonNumber].author);
        tfGenre.setText(levelButtons[buttonNumber].genre);
      } else {
        select(buttonNumber);
      }
    } catch (NumberFormatException e) {
      switch (command) {
        case "doTheThing":
          if (isSaving) {
            if (selectedLevel >= 0) {
              overwriteDialog.setVisible(true);
            } else {
              save();
            }
          } else {
            Nfile usrlvls = new Nfile(userlevels);
            jned.loadLevel(levelButtons[selectedLevel].name,
                levelButtons[selectedLevel].author, levelButtons[selectedLevel].genre,
                usrlvls.getData(levelButtons[selectedLevel].name));
            usrlvls.close();
            jned.savedAs = true;
          }
          // fall through
        case "cancel":
          mainDialog.setVisible(false);
          break;
        case "fileChange":
          int result = fileChooser.showOpenDialog(mainDialog);
          if (result == JFileChooser.APPROVE_OPTION) {
            userlevels = fileChooser.getSelectedFile().getPath();
            if (userlevels.indexOf(".txt") == -1) {
              userlevels += ".txt";
            }
            jned.setUserlevels(userlevels);
            lFileName.setText(userlevels);
            populateList();
            config.setData(userlevels, "fpath");
          }
          break;
        case "lvlName": // TASK - program these buttons to sort list
        
          break;
        case "lvlAuthor":
        
          break;
        case "lvlGenre":
        
          break;
        case "overwriteOK":
          save();
          // fall through
        case "overwriteCancel":
          overwriteDialog.setVisible(false);
          break;
        default:
          System.err.println("Unrecognized command");
      }
    }
  }
  
  public void insertUpdate(DocumentEvent e) {
    checkFields();
  }
  
  public void removeUpdate(DocumentEvent e) {
    checkFields();
  }
  
  // Checks the name, author, and genre fields against the list to see if they all match one
  // of the levels. Selects any match, selects none if there is no match.
  private void checkFields() {
    boolean notMatched = true; // TASK - try a simple select(-1) instead of notMatched
    for (int i = 0; i < listSize; i++) {
      if (levelButtons[i].name.equals(tfName.getText())) {
        if (levelButtons[i].author.equals(tfAuthor.getText())) {
          if (levelButtons[i].genre.equals(tfGenre.getText())) {
            select(i);
            notMatched = false;
          }
        }
      }
    }
    if (notMatched) {
      select(-1);
    }
  }
  
  /**
   * Saves the level, using a call to Jned.
   */
  public void save() {
    if (selectedLevel > 0) {
      jned.setAttributes(tfName.getText(), tfAuthor.getText(), tfGenre.getText());
      jned.savedAs = true;
      jned.push("save");
    } else {
      jned.saveAs(tfName.getText(), tfAuthor.getText(), tfGenre.getText());
      populateList();
    }
  }
  
  // Controls selection highlighting
  private void select(int number) {
    if (selectedLevel >= 0) {
      levelButtons[selectedLevel].selected = false;
      levelButtons[selectedLevel].repaint();
    }
    if (number < 0 || number >= listSize || selectedLevel == number) {
      selectedLevel = -1;
      if(!isSaving) {
        doTheThing.setEnabled(false);
      }
    } else {
      selectedLevel = number;
      levelButtons[selectedLevel].selected = true;
      levelButtons[selectedLevel].repaint();
      if(!isSaving) {
        doTheThing.setEnabled(true);
      }
      
      // Viewport adjustment
      int selectionY = (FileChooser.BORDER + FileChooser.ROW_HEIGHT) * selectedLevel;
      int viewPortDifference = viewPort.getViewPosition().y - selectionY;
      if (viewPortDifference > 0) {
        viewPort.setViewPosition(new Point(0, viewPort.getViewPosition().y -
            viewPortDifference));
      } else {
        int viewPortLowerDifference = selectionY + FileChooser.BORDER * 2 +
            FileChooser.ROW_HEIGHT - viewPort.getViewPosition().y - viewPortHeight;
        if (viewPortLowerDifference > 0) {
          viewPort.setViewPosition(new Point(0, viewPort.getViewPosition().y +
              viewPortLowerDifference));
        }
      }
    }
  }
  
  public void changedUpdate(DocumentEvent e) {}
  
  private class LevelButton extends Button {
    protected String name;
    protected String author;
    protected String genre;
    private int index;
    protected boolean selected;

    public LevelButton(ActionListener listener, int ind, String levelName, String levelAuthor,
        String levelGenre, int x, int y, int width, int height) {
      super(listener, "" + ind, -2, x, y, width, height, true, "");
      name = levelName;
      author = levelAuthor;
      genre = levelGenre;
      index = ind;
      selected = false;
    }
    
    public void paintText(Graphics g) {
      g.setFont(FileChooser.LEVEL_FONT);
      g.drawString("" + index, FileChooser.BORDER, getHeight() / 2 + Jned.DEF_FONT_YOFF);
      g.drawString(name, buttonLevelWidth, getHeight() / 2 + Jned.DEF_FONT_YOFF);
      g.drawString(author, buttonAuthorWidth, getHeight() / 2 + Jned.DEF_FONT_YOFF);
      g.drawString(genre, buttonGenreWidth, getHeight() / 2 + Jned.DEF_FONT_YOFF);
      
      if (selected) {
        g.setColor(Colors.BUTTON_SEL);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
      }
    }
  }
}