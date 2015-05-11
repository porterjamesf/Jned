import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * Representation of a set of grid lines or snap points in Jned. An Overlay contains all of the
 * setting information controlling its spacing and symmetry and translates them into actual x and y
 * coordinates at which to draw lines (or points at the intersections).
 * @author James Porter
 */
public class Overlay implements ActionListener, MouseListener {
  private JTextField[] spacingTextFields;
  private JComboBox symmetryComboBox;
  private Button[] buttons;
  private boolean isSingle;
  private boolean isOn;
  private boolean isActive;
  private int[] values;
  private ArrayList<Integer> xpoints;
  private ArrayList<Integer> ypoints;
  private int width;
  private int height;
  private Jned jned;
  
  /**
   * Constructs a new Overlay with the given controlling input JComponents and dimensions.
   * @param xSpacing JTextField controlling horizontal line spacing amount
   * @param ySpacing JTextField controlling vertical line spacing amount
   * @param xOffset JTextField controlling horizontal line offset amount
   * @param yOffset JTextField controlling vertical line offset amount
   * @param symmetry JComboBox controlling selection of symmetry mode
   * @param singleLine Button that sets this Overlay to single lines
   * @param doubleLine Button that sets this Overlay to doubled lines
   * @param doubleSpacing JTextField controlling spacing between line doubles
   * @param onOff Button that turns Overlay on and off
   * @param isActive true to set Overlay to active immediately, false to set in inactive
   * @param width the width of area this Overlay will cover
   * @param height the heigth of area this Overlay will cover
   * @param jned reference to enclosing Jned instance
   */
  public Overlay (JTextField xSpacing, JTextField ySpacing, JTextField xOffset, JTextField yOffset,
      JComboBox symmetry, Button singleLine, Button doubleLine, JTextField doubleSpacing,
      Button onOff, boolean isActive, int width, int heigth, Jned jned) {
    spacingTextFields = new JTextField[5];
    spacingTextFields[0] = xSpacing;
    spacingTextFields[1] = ySpacing;
    spacingTextFields[2] = xOffset;
    spacingTextFields[3] = yOffset;
    symmetryComboBox = symmetry;
    buttons = new Button[3];
    buttons[0] = singleLine;
    buttons[1] = doubleLine;
    spacingTextFields[4] = doubleSpacing;
    buttons[2] = onOff;
    this.isActive = isActive;
    this.width = width;
    this.height = heigth;
    this.jned = jned;
    
    values = new int[6];
    values[0] = 0;
    values[1] = 0;
    values[2] = 0;
    values[3] = 0;
    values[4] = 0;
    values[5] = 0;
    
    isSingle = true;
    isOn = true;
    
    symmetryComboBox.addActionListener(this);
    for (int i = 0; i < 5; i++) {
      if (spacingTextFields[i] != null) {
        spacingTextFields[i].addActionListener(this);
        spacingTextFields[i].addMouseListener(this);
      }
    }
    
    xpoints = new ArrayList<Integer>();
    ypoints = new ArrayList<Integer>();
  }
  
  /**
   * Sets all of the controlling JComponents and Buttons to show the values stored within this
   * Overlay.
   */
  public void update() {
    for (int i = 0; i < 5; i++) {
      if (spacingTextFields[i] != null) {
        spacingTextFields[i].setText("" + values[i]);
      }
    }
    symmetryComboBox.setSelectedIndex(values[5]);
    if (isSingle) {
      if (buttons[0] != null) {
        buttons[0].setPushed(true);
        buttons[1].setPushed(false);
      }
    } else {
      if (buttons[0] != null) {
        buttons[1].setPushed(true);
        buttons[0].setPushed(false);
      }
    }
    if (buttons[2] != null) {
      if (isOn) {
        buttons[2].setPushed(true);
      } else {
        buttons[2].setPushed(false);
      }
    }
  }
  
  /**
   * Inputs settings from a correctly formatted String.
   * @param data String of setting data
   */
  public void loadData(String data) {
    String[] bits = data.split(";");
    try {
      for (int i = 0; i < 6; i++) {
        values[i] = Integer.parseInt(bits[i]);
      }
      isSingle = Boolean.parseBoolean(bits[6]);
      isOn = Boolean.parseBoolean(bits[7]);
      recalculate();
      if (isActive) {
        update();
      }
    } catch (NumberFormatException e) {}
  }
  
  /**
   * Outputs settings into a formatted String for saving.
   * @return String of setting data
   */
  public String saveData() {
    String result = "";
    for(int i = 0; i < 6; i++) {
      result += values[i] + ";";
    }
    result += isSingle + ";" + isOn;
    return result;
  }
  
  /**
   * Returns whether this overly is active or inactive
   * @return true when active, false when inactive
   */
  public boolean isActive () {
    return isActive;
  }
  
  /**
   * Sets this Overlay to active or inactive
   * @param isActive true to set Overly to active, false to set it to inactive
   */
  public void setActive (boolean isActive) {
    if(isActive && !this.isActive) {
      update();
    }
    this.isActive = isActive;
  }
  
  /**
   * Returns whether or not this Overlay is turned on.
   * @return true when Overlay is on, false when it is off
   */
  public boolean isOn() {
    return isOn;
  }
  
  /**
   * Performs setting changes that should occur when Button with the given action command is
   * pushed.
   */
  public void push(String command) {
    if (isActive) {
      switch (command) {
        case "single":
          isSingle = true;
          break;
        case "double":
          isSingle = false;
          break;
        case "on":
          isOn = true;
          break;
        case "off":
          isOn = false;
          break;
        default:
      }
      recalculate();
    }
  }
  
  public void actionPerformed(ActionEvent e) {
    if (isActive) {
      if( e.getActionCommand().equals("comboBoxChanged")) {
        values[5] = symmetryComboBox.getSelectedIndex();
        recalculate();
      } else {
        JTextField temp = (JTextField) e.getSource();
        temp.getCaret().setVisible(false);
        for (int i = 0; i < 5; i++) {
          if (temp.equals(spacingTextFields[i])) {
            format(i, temp.getText());
          }
        }
      }
    }
  }

  // Formats text into an integer
  private void format(int index, String str) {
    try {
      int temp = Integer.parseInt(str);
      values[index] = temp;
      recalculate();
    } catch (NumberFormatException e) {}
    spacingTextFields[index].setText("" + values[index]);    
  }
  
  // Calculates line/point coordinates based on settings
  private void recalculate() {
    xpoints = new ArrayList<Integer>();
    if (values[0] != 0) {
      boolean flip = (values[5] % 2 == 1);
      for (int i = values[2] % values[0]; i <= width / (flip ? 2 : 1); i += values[0]) {
        xpoints.add(i);
        if (flip) {
          xpoints.add(width - i);
        }
        if (!isSingle) {
          int lineDouble = i + values[4];
          if (lineDouble <= width / (flip ? 2 : 1)) {
            xpoints.add(lineDouble);
            if (flip) {
              xpoints.add(width - lineDouble);
            }
          }
        }
      }
      Collections.sort(xpoints);
    }
      
    ypoints = new ArrayList<Integer>();
    if (values[1] != 0) {
      boolean flip = (values[5] > 1);
      for (int j = values[3] % values[1]; j <= height / (flip ? 2 : 1); j += values[1]) {
        ypoints.add(j);
        if (flip) {
          ypoints.add(height - j);
        }
        if (!isSingle) {
          int lineDouble = j + values[4];
          if (lineDouble <= height / (flip ? 2 : 1)) {
            ypoints.add(lineDouble);
            if (flip) {
              ypoints.add(height - lineDouble);
            }
          }
        }
      }
      Collections.sort(ypoints);
    }
    
    jned.repaint();
  }
  
  public void mouseClicked(MouseEvent me) {
    if (isActive) {
      JTextField temp = (JTextField) me.getSource();
      temp.selectAll();
      temp.getCaret().setVisible(true);
    }
  }
    
  /**
   * Returns an ArrayList of the calculated coordinates for lines or points along the given
   * dimension.
   * @param isX true to retrieve the x coordinates, false to retrieve the y coordinates
   * @return ArrayList<Integer> of the desired coordinates
   */
  public ArrayList<Integer> getPoints(boolean isX) {
    if(isX) {
      return xpoints;
    } else {
      return ypoints;
    }
  }
  
  /**
   * Returns the coordinate closest in proximity to the given coordinate along the given dimension.
   * @param coordinate the original coordinate
   * @param isX true to snap to the closest x coordinate, false to snap to the closest y coordinate
   * @return the Overlay coordinate closest to the original coordinate along specified dimension
   */
  public int snapCoord(int coordinate, boolean isX) {
    ArrayList<Integer> scratch = (isX ? xpoints : ypoints);
    int ind = Collections.binarySearch(scratch, coordinate);
    if(ind < 0) {
      ind = -ind - 1;
    }
    try {
      if (coordinate - scratch.get(ind - 1) < scratch.get(ind) - coordinate) {
        return scratch.get(ind - 1);
      } else {
        return scratch.get(ind);
      }
    } catch (IndexOutOfBoundsException ioobe) {
      // Coordinates outside this Overlay's perimeter are not snapped
      return coordinate;
    }
  }
  
  public void mouseEntered(MouseEvent me) {}
  public void mouseExited(MouseEvent me) {}
  public void mousePressed(MouseEvent me) {}
  public void mouseReleased(MouseEvent me) {}
}