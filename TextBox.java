/*
TextBox.java
James Porter

The area representing the text bos at the bottom of the Jned application. Contains the level data formatted like the text box in n's editor.
*/

import javax.swing.*;
import javax.swing.text.Caret;
import java.util.ArrayList;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;

public class TextBox {
	private Jned mind;
	private JPanel pan;					//Main content panel
	private JFrame frext;				//Pop-out frame
	protected Buttonwog[] tboxButtons;	//The copy level, paste level, and load text buttons
	private JTextArea text;				//Text area
	private JScrollPane scrolp;			//Scroll pane: top level component
	private Clipboard clip;				//Link to system clipboard
	private Caret caret;				//The caret of the text area. Selection operations are done frequently enough to warrent a class variable for this.
	private int	blx,		//Coordinates of text box
				bly,		//when it is located below
				blw,		//the main window (x, y,
				blh,		//width, height)
				bsx,		//Coordinates of text box
				bsy,		//when it is located beside
				bsw,		//the main window (x, y,
				bsh,		//width, height)
				otw,		//Width and height of text box
				oth,		//when in its own frame
				status;		//0 for popped out, 1 for below main window, 2 for beside main window
	
	//Constructor
	public TextBox (Jned blown, int xorig, int yorig, int wid, int hei/*, LevelArea levar*/) {
		mind = blown;
		blx = xorig;
		bly = bsh = yorig;
		blw = otw = bsx = wid;
		blh = oth = hei;
		bsy = 0;
		bsw = 100;
		status = 1;
		
		pan = new JPanel();
		pan.setBounds(blx, bly, blw, blh);
		pan.setBackground(Jned.BG_COLOR);
		pan.setLayout(null);
		
		tboxButtons = new Buttonwog[3];
		int xcount = mind.BORDER, ycount = mind.BORDER;
		tboxButtons[0] = new Buttonwog(mind,"cpylvl",-2,xcount,ycount,mind.BORDER + 2*mind.TALL_BUTTON,mind.SHORT_BUTTON_HT,true,"Copy level");	//Copy level text to clipboard
		xcount += 2*mind.BORDER + 2*mind.TALL_BUTTON;
		tboxButtons[1] = new Buttonwog(mind,"pstlvl",-2,xcount,ycount,mind.BORDER + 2*mind.TALL_BUTTON,mind.SHORT_BUTTON_HT,true,"Paste level"); //Paste level text to clipboard
		xcount += 2*mind.BORDER + 2*mind.TALL_BUTTON;
		tboxButtons[2] = new Buttonwog(mind,"tboxlvl",-2,xcount,ycount,mind.BORDER + 2*mind.TALL_BUTTON,mind.SHORT_BUTTON_HT,true,"Load text"); //Set text area to level
		pan.add(tboxButtons[0]);
		pan.add(tboxButtons[1]);
		pan.add(tboxButtons[2]);
		ycount += mind.SHORT_BUTTON_HT + mind.BORDER;
		
		text = new JTextArea();
		text.setLineWrap(true);
		text.setFont(Jned.BOX_FONT);
		caret = text.getCaret();
		
		scrolp = new JScrollPane(text);
		scrolp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrolp.setBounds(mind.BORDER, ycount, wid - 2*mind.BORDER, hei - 3*mind.BORDER - mind.SHORT_BUTTON_HT);
		pan.add(scrolp);
		
		clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		frext = new JFrame("Jned text box");
		frext.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		//frext.getContentPane().add(pan);
		//frext.pack();
		frext.setVisible(false);
	}
	
	//Returns a link to the scrollpane, the top level JComponent of this object 
	public JPanel getPane() {
		return pan;
	}
	
	//Frame popping - only handles adding/removing from frame. Adding/removing from main window is handled by Jned 
	public void popOut() {
		pan.setBounds(0,0,otw,oth);
		pan.setPreferredSize(new Dimension(otw,oth));
		frext.getContentPane().add(pan);
		frext.pack();
		frext.setVisible(true);
		status = 0;
	}
	public void popIn(boolean isBelow) {
		frext.getContentPane().remove(pan);
		frext.setVisible(false);
		
		if(isBelow) {
			pan.setBounds(blx,bly,blw,blh);
			status = 1;
		} else {
			pan.setBounds(bsx,bsy,bsw,bsh);
			status = 2;
		}
	}
	
	//Dimensions accessors
	public int getHeight() {
		return (status==0?oth:(status==1?blh:bsh));
	}
	public int getWidth() {
		return (status==0?otw:(status==1?blw:bsw));
	}
	
	//Text accessor/mutator methods
	public String getText() {
		return text.getText();
	}
	public void setText(String st) {
		text.setText(st);
	}
	public void copyToClipboard() {
		clip.setContents(new StringSelection(text.getText()),null);
	}
	public String pasteFromClipboard() {
		String data = "";
		try {
			data = (String)clip.getData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			return "Couldn't copy text from clipboard.";
		}
		if(checkData(data)) {
			text.setText(data);
			return "Clipboard contents pasted to text box.";
		} else {
			return "Clipboard contents are not the right format for an n level.";
		}
	}
	
	//Checks a string to verify that it is the correct format for level data
	private boolean checkData(String data) {
		try {
			if(data.charAt(713) != '|') return false;
			//char[] lvldata = data.substring(0,713).toCharArray();
			String valid = "013254GFIH?>A@7698QPONKJMLCBED;:=<";
			for (int i = 0; i < 713; i++) {
				if(valid.indexOf(data.charAt(i))==-1) return false;
			}
			return true;
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}
	
	//Highlights an item or tile, given the starting index of its text
	public void highlightItem(int index) {
		int endpoint = text.getText().indexOf('!',index+714);
		if(endpoint==-1) endpoint = text.getText().length();
		caret.setDot(endpoint);
		caret.moveDot(index+714);
	}
	public void highlightTile(int index) {
		caret.setDot(index+1);
		caret.moveDot(index);
	}
	public void unHighlight() {
		caret.setDot(caret.getDot());
	}
}