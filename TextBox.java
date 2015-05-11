import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Caret;

/**
 * The text box containing the level data in Jned.
 * @author James Porter
 */
public class TextBox {
  private Jned jned;
  private JPanel textBoxPanel;
  private JFrame textBoxFrame;
  protected Button[] tboxButtons;
  private JTextArea text;
  private Clipboard clip;
  private Caret caret;
  
  private int belowX;
  private int belowY;
  private int belowWidth;
  private int belowHeight;
  private int besideX;
  private int besideY;
  private int besideWidth;
  private int besideHeight;
  private int poppedOutWidth;
  private int poppedOutHeight;
  
  private int status; // 0 for popped out, 1 for below main window, 2 for beside main window
  
  /**
   * Constructs a new TextBox with the given position and dimensions.
   * @param jned a reference to the associated Jned instance
   * @param x the x position
   * @param y the y position
   * @param width the width
   * @param height the height
   */
  public TextBox (Jned jned, int x, int y, int width, int height) {
    this.jned = jned;
    
    belowX = x;
    belowY = y;
    besideHeight = y;
    belowWidth = width;
    poppedOutWidth = width;
    besideX = width;
    belowHeight = height;
    poppedOutHeight = height;
    besideY = 0;
    besideWidth = 100;
    
    status = 1;
    
    textBoxPanel = new JPanel();
    textBoxPanel.setBounds(belowX, belowY, belowWidth, belowHeight);
    textBoxPanel.setBackground(Colors.BG_COLOR);
    textBoxPanel.setLayout(null);
    
    tboxButtons = new Button[3];
    int xcount = jned.BORDER;
    int ycount = jned.BORDER;
    tboxButtons[0] = new Button(jned, "cpylvl", -2, xcount, ycount, jned.BORDER + 2 *
        jned.TALL_BUTTON, jned.SHORT_BUTTON_HT, true, "Copy level");
    xcount += 2 * jned.BORDER + 2 * jned.TALL_BUTTON;
    tboxButtons[1] = new Button(jned, "pstlvl", -2, xcount, ycount, jned.BORDER + 2 *
        jned.TALL_BUTTON, jned.SHORT_BUTTON_HT, true, "Paste level");
    xcount += 2 * jned.BORDER + 2 * jned.TALL_BUTTON;
    tboxButtons[2] = new Button(jned, "tboxlvl", -2, xcount, ycount, jned.BORDER + 2 *
        jned.TALL_BUTTON, jned.SHORT_BUTTON_HT, true, "Load text");
    textBoxPanel.add(tboxButtons[0]);
    textBoxPanel.add(tboxButtons[1]);
    textBoxPanel.add(tboxButtons[2]);
    ycount += jned.SHORT_BUTTON_HT + jned.BORDER;
    
    text = new JTextArea();
    text.setLineWrap(true);
    text.setFont(Jned.BOX_FONT);
    caret = text.getCaret();
    
    JScrollPane scrollPane = new JScrollPane(text);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setBounds(jned.BORDER, ycount, width - 2 * jned.BORDER, height - 3 * jned.BORDER -
        jned.SHORT_BUTTON_HT);
    textBoxPanel.add(scrollPane);
    
    clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    textBoxFrame = new JFrame("Jned text box");
    textBoxFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    textBoxFrame.setVisible(false);
  }
  
  /**
   * Returns the top level panel of this TextBox.
   * @return the JPanel containing all content of this TextBox
   */
  public JPanel getPane() {
    return textBoxPanel;
  }
  
  //Frame popping - only handles adding/removing from frame. Adding/removing from main window is handled by Jned 
  /**
   * Places TextBox in its own frame and displays it. This method will not remove TextBox from any
   * other frames; that must be handled separately.
   */
  public void popOut() {
    textBoxPanel.setBounds(0, 0, poppedOutWidth, poppedOutHeight);
    textBoxPanel.setPreferredSize(new Dimension(poppedOutWidth, poppedOutHeight));
    textBoxFrame.getContentPane().add(textBoxPanel);
    textBoxFrame.pack();
    textBoxFrame.setVisible(true);
    status = 0;
  }
  
  /**
   * Removes TextBox from its own frame and resizes it to be placed in Jned either below or beside
   * the other content. This method will not add TextBox to any other frames; that must be handled
   * separately.
   * @param isBelow true to size this TextArea to go below Jned, false to go beside it
   */
  public void popIn(boolean isBelow) {
    textBoxFrame.getContentPane().remove(textBoxPanel);
    textBoxFrame.setVisible(false);
    
    if (isBelow) {
      textBoxPanel.setBounds(belowX, belowY, belowWidth, belowHeight);
      status = 1;
    } else {
      textBoxPanel.setBounds(besideX, besideY, besideWidth, besideHeight);
      status = 2;
    }
  }
  
  /**
   * Returns the present width of this TextBox;
   * @return the width of the TextBox content
   */
  public int getWidth() {
    return (status == 0 ? poppedOutWidth : (status == 1 ? belowWidth : besideWidth));
  }
  
  /**
   * Returns the present height of this TextBox;
   * @return the height of the TextBox content
   */
  public int getHeight() {
    return (status == 0 ? poppedOutHeight : (status == 1 ? belowHeight : besideHeight));
  }
  
  /**
   * Returns the text box contents.
   * @return String of the text in this TextBox
   */
  public String getText() {
    return text.getText();
  }
  
  /**
   * Sets the text box contents.
   * @param levelText the String of text to place in this TextBox
   */
  public void setText(String levelText) {
    text.setText(levelText);
  }
  
  /**
   * Copies the contents of this TextBox to the system clipboard.
   */
  public void copyToClipboard() {
    clip.setContents(new StringSelection(text.getText()), null);
  }
  
  /**
   * Pastes the contents of the system clipboard to this TextBox, if they are in the right format.
   * @return a String stating the success or failure of pasting
   */
  public String pasteFromClipboard() {
    String data = "";
    try {
      data = (String) clip.getData(DataFlavor.stringFlavor);
    } catch (Exception e) {
      return "Couldn't copy text from clipboard.";
    }
    if (checkData(data)) {
      text.setText(data);
      return "Clipboard contents pasted to text box.";
    } else {
      return "Clipboard contents are not the right format for an n level.";
    }
  }
  
  // Checks a string to verify that it is the correct format for level data
  private boolean checkData(String data) {
    try {
      if (data.charAt(713) != '|') {
        return false;
      }
      String valid = "013254GFIH?>A@7698QPONKJMLCBED;:=<";
      for (int i = 0; i < 713; i++) {
        if (valid.indexOf(data.charAt(i)) == -1) {
          return false;
        }
      }
      return true;
    } catch (IndexOutOfBoundsException e) {
      return false;
    }
  }
  
  /**
   * Highlights an item, given the starting index of its text.
   * @param index the index where the item's text begins, starting after the '|'
   */
  public void highlightItem(int index) {
    int endpoint = text.getText().indexOf('!', index + 714);
    if (endpoint == -1) {
      endpoint = text.getText().length();
    }
    caret.setDot(endpoint);
    caret.moveDot(index + 714);
  }
  
  /**
   * Highlights a tile, given the index of its text.
   * @param index the index of the tile's character
   */
  public void highlightTile(int index) {
    caret.setDot(index + 1);
    caret.moveDot(index);
  }
  
  /**
   * Removes all highlighting.
   */
  public void unHighlight() {
    caret.setDot(caret.getDot());
  }
}