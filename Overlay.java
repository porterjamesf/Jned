/*
Overlay.java
James Porter	01/05/2013

An overlay of lines or dots used in Jned for the grid and/or the snap points
*/

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;

public class Overlay implements ActionListener, MouseListener {
	JTextField[]	fields;		//0 - Spacing between columns
								//1 - Spacing between rows
								//2 - Offset from left
								//3 - Offset from top
								//4 - Spacing between two lines for double-line mode
	JComboBox		symm;		//Combobox for selecting type of symmetry (none, right/left, top/bottom, quadrants)
	Buttonwog		single,		//Links to buttons
					duble,
					onoff;
	private boolean	isSingle,	//True for single line mode, false for double-line mode
					isOn,		//True: Paint these lines/dots  False: do not
					active;		//When false, ignores all changes to buttons
	private int[]	values;		//Int values in the 5 fields, as well as the index to the combo box in values[5]
	private ArrayList<Integer>	xpoints,	//The actual coordinates for lines/dots of this overlay
								ypoints;
	private int		width,		//Dimensions of window the overlay goes over
					height;
	private Jned mind;
					
	//Constructor
	public Overlay (JTextField xspc, JTextField yspc, JTextField xoff, JTextField yoff, JComboBox sym, Buttonwog sing, Buttonwog dub, JTextField dspc, Buttonwog on, boolean act, int wid, int hei, Jned blown) {
		fields = new JTextField[5];
		values = new int[6];
		fields[0] = xspc;
		fields[1] = yspc;
		fields[2] = xoff;
		fields[3] = yoff;
		symm = sym;
		symm.addActionListener(this);
		single = sing;
		duble = dub;
		fields[4] = dspc;
		onoff = on;
		active = act;
		values[0] = values[1] = values[2] = values[3] = values[4] = values[5] = 0;
		isSingle = true;
		isOn = true;
		mind = blown;
		
		for(int i = 0; i < 5; i++) {
			if(fields[i] != null) {
				fields[i].addActionListener(this);
				fields[i].addMouseListener(this);
			}
		}
		
		width = wid;
		height = hei;
		xpoints = new ArrayList<Integer>();
		ypoints = new ArrayList<Integer>();
	}
	
	//Updates the text fields and buttons to internally stored values
	public void update() {
		for (int i = 0; i < 5; i++) {
			if(fields[i] != null) fields[i].setText("" + values[i]);
		}
		symm.setSelectedIndex(values[5]);
		if(isSingle) {
			if(single != null) {
				single.push();
				duble.unpush();
			}
		} else {
			if(single != null) {
				duble.push();
				single.unpush();
			}
		}
		if(onoff != null) {
			if(isOn) {
				onoff.push();
			} else {
				onoff.unpush();
			}
		}
	}
	
	//Loads internal values from a specifically formatted data string
	public void loadData(String data) {
		String[] bits = data.split(";");
		try {
			for(int i = 0; i < 6; i++) {
				values[i] = Integer.parseInt(bits[i]);
			}
			isSingle = Boolean.parseBoolean(bits[6]);
			isOn = Boolean.parseBoolean(bits[7]);
			recalculate();
			if(active) update();
		} catch (NumberFormatException nfe) {}
	}
	
	//Exports internal values as a specifically formatted data string
	public String saveData() {
		String result = "";
		for(int i = 0; i < 6; i++) {
			result += values[i] + ";";
		}
		result += isSingle + ";" + isOn;
		return result;
	}
	
	//Sets the overlay to active/inactive
	public void setActive (boolean act) {
		if(act && !active) update();
		active = act;
	}
	
	//Method invoked by Jned when relevant button is pushed
	public void push(String command) {
		if(active) {
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
				default: break;
			}
			recalculate();
		}
	}
	
	//Listener for text field entries and combo box
	public void actionPerformed(ActionEvent ae) {
		if(active) {
			if(ae.getActionCommand().equals("comboBoxChanged")) {
				values[5] = symm.getSelectedIndex();
				recalculate();
			} else {
				JTextField temp = (JTextField)ae.getSource();
				temp.getCaret().setVisible(false);
				for(int i = 0; i < 5; i++) {
					if(temp.equals(fields[i])) {
						format(i,temp.getText());
					}
				}
			}
		}
	}

	//Formats entered text into an integer
	private void format(int index, String str) {
		try {
			int temp = Integer.parseInt(str);
			values[index] = temp;
			recalculate();
		} catch (Exception e) {}
		fields[index].setText("" + values[index]);		
	}
	
	//Calculates the indices for lines/dots
	private void recalculate() {
		xpoints = new ArrayList<Integer>();
		if(values[0]!=0) {
			boolean flip = (values[5]%2==1);
			for(int i = values[2] % values[0]; i <= width/(flip?2:1); i += values[0]) {
				xpoints.add(i);
				if(flip) xpoints.add(width - i);
				if(!isSingle) {
					int dline = i + values[4];
					if(dline <= width/(flip?2:1)) {
						xpoints.add(dline);
						if(flip) xpoints.add(width - dline);
					}
				}
			}
		}
		Collections.sort(xpoints);
		ypoints = new ArrayList<Integer>();
		if(values[1]!=0) {
			boolean flip = (values[5]>1);
			for(int j = values[3] % values[1]; j <= height/(flip?2:1); j += values[1]) {
				ypoints.add(j);
				if(flip) ypoints.add(height - j);
				if(!isSingle) {
					int dline = j + values[4];
					if(dline <= height/(flip?2:1)) {
						ypoints.add(dline);
						if(flip) ypoints.add(height - dline);
					}
				}
			}
		}
		Collections.sort(ypoints);
		mind.repaint();
	}
	
	//Listeners for mouse events on the text fields
	public void mouseClicked(MouseEvent me) {
		if(active) {
			JTextField temp = (JTextField)me.getSource();
			temp.selectAll();
			temp.getCaret().setVisible(true);
		}
	}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mousePressed(MouseEvent me) {}
	public void mouseReleased(MouseEvent me) {}
	
	//Accessors
	public boolean isOn() {
		return isOn;
	}
	
	//Returns arrays of integers representing this overlay's coordinates for lines/dots. Returns null if overlay is turned off
	public ArrayList<Integer> getPoints(boolean isX) {
		if(isX) {
			return xpoints;
		} else {
			return ypoints;
		}
	}
	
	//Returns the nearest point to a given coordinate
	public int snapCoord(int coord, boolean isX) {
		ArrayList<Integer> scratch = (isX?xpoints:ypoints);
		int ind = Collections.binarySearch(scratch,coord); 			//Finds nearest snap point
		if(ind < 0) ind = -ind - 1;									//Corrects for the binarySearch method's output format when an exact value is not found
		try {
			if(coord-scratch.get(ind-1)<scratch.get(ind)-coord) {	//Figures out which neighboring snap point is closer
				return scratch.get(ind-1);							//  and returns that value
			} else {
				return scratch.get(ind);
			}
		} catch (IndexOutOfBoundsException ioobe) {return coord;}	//If the mouse goes outside the level area, the given coordinate is returned
	}
}