/*
FileChooser.java

James Porter

The dialog used for opening and saving files
*/

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.filechooser.*;

public class FileChooser extends JPanel implements DocumentListener {
	public static Font	LEVEL_FONT = new Font(Font.MONOSPACED,Font.PLAIN,10);	//Font of text in level selection buttons
	public static int	WINDOW_WIDTH = 640,
						BORDER = 4,
						ROW_HEIGHT = 24,
						BUTTON_WIDTH = 64,
						LVLFL_WIDTH = 96,
						SCROLLPANE_HEIGHT = 256,
						OVERWRITE_WIDTH = 320;
	private static double	LEVEL = 0.07,		//Percentage of width for labels in LevelButton
							AUTHOR = 0.45,
							GENRE = 0.75;
	private Jned master;
	private Nfile config;
	private String userlevels;
	private JDialog frame,
					overwrite;
	private JFileChooser fchooser;
	private JLabel 	fileName,
					lblName, lblAuthor, lblGenre;
	private JPanel levels;
	private JViewport view;
	private LevelButton[] lvlButtons;
	private Buttonwog dothething, cancel;
	private JTextField lvlName, lvlAuthor, lvlGenre;
	private int saveY, openY, savePlus,		//Y values kept for the purpose of switching between open/save modes
				lvlc, authc, genc,			//Calculated values corresponding to the static LEVEL/AUTHOR/GENRE numbers above
				listSize,					//Number of levels in list
				selection,					//Selected level number
				calcedWidth,				//Width of level buttons
				viewHeight;					//Height of the scrollpane's view, used in math for moving around viewport when levels are selected
	private boolean isSaving;				//Whether or not the dialog is in save mode (true = save, false = open)
	
	public FileChooser (Jned lord, JFrame fred, Nfile configurator, String usrlvls) {
		master = lord;
		config = configurator;
		selection = listSize = -1;
		
		userlevels = usrlvls;
		
		fchooser = new JFileChooser(".\\");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("text files", "txt");
		fchooser.setFileFilter(filter);
		
		frame = new JDialog(fred);
		frame.getContentPane().add(this);
		setBackground(Jned.BG_COLOR);
		setLayout(null);
		
		//Levels file
		int ycount = FileChooser.BORDER;
		add(makeJLabel("Levels file:",FileChooser.BORDER,ycount,FileChooser.LVLFL_WIDTH,FileChooser.ROW_HEIGHT,1));
		fileName = makeJLabel(userlevels,FileChooser.LVLFL_WIDTH + 2*FileChooser.BORDER,ycount,FileChooser.WINDOW_WIDTH-4*FileChooser.BORDER-FileChooser.BUTTON_WIDTH-FileChooser.LVLFL_WIDTH,FileChooser.ROW_HEIGHT,-1);
		add(fileName);
		add(new Buttonwog(master,"fileChooser fileChange",-2,FileChooser.WINDOW_WIDTH-FileChooser.BORDER-FileChooser.BUTTON_WIDTH,ycount,FileChooser.BUTTON_WIDTH,FileChooser.ROW_HEIGHT,true,"Change"));
		ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;
		
		//Some math
		calcedWidth = FileChooser.WINDOW_WIDTH - FileChooser.BORDER*4 - 17; //Width of the level buttons (scrollbar takes up 17)
		lvlc = (int)(FileChooser.LEVEL * calcedWidth);
		authc = (int)(FileChooser.AUTHOR * calcedWidth);
		genc = (int)(FileChooser.GENRE * calcedWidth);
		
		//Column label buttons
		add(new Buttonwog(master,"fileChooser lvlName",-2,lvlc + FileChooser.BORDER*2,ycount,FileChooser.BUTTON_WIDTH*2,FileChooser.ROW_HEIGHT,true,"Level Name"));
		add(new Buttonwog(master,"fileChooser lvlAuthor",-2,authc + FileChooser.BORDER*2,ycount,FileChooser.BUTTON_WIDTH,FileChooser.ROW_HEIGHT,true,"Author"));
		add(new Buttonwog(master,"fileChooser lvlGenre",-2,genc + FileChooser.BORDER*2,ycount,FileChooser.BUTTON_WIDTH,FileChooser.ROW_HEIGHT,true,"Type"));
		ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;
		
		//Scrollpane and level list panel
		levels = new JPanel();
		levels.setBackground(Jned.BG_COLOR);
		levels.setPreferredSize(new Dimension(FileChooser.WINDOW_WIDTH - 17,512));
		levels.setLayout(null);
		JScrollPane sp = new JScrollPane(levels);
		view = sp.getViewport();
		sp.setBounds(FileChooser.BORDER,ycount,FileChooser.WINDOW_WIDTH-2*FileChooser.BORDER, FileChooser.SCROLLPANE_HEIGHT);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(sp);
		ycount += FileChooser.BORDER + FileChooser.SCROLLPANE_HEIGHT;
		
		openY = ycount; //This is the y value to set the open/cancel buttons to when in open mode
		
		//Textfields and labels for save mode
		int authwidth = 45, genwidth = 35; //Widths of the labels "Author:" and "Genre:". Will be subject to tweaking when tested on different systems
		lblName = makeJLabel("Name:",FileChooser.BORDER,ycount,lvlc,FileChooser.ROW_HEIGHT,1);
		lvlName = new JTextField();
		lvlName.getDocument().addDocumentListener(this);
		lvlName.setFont(Jned.BOX_FONT);
		lvlName.setHorizontalAlignment(JTextField.LEFT);
		lvlName.setBounds(lvlc+FileChooser.BORDER*2,ycount,authc-lvlc-authwidth-2*FileChooser.BORDER, FileChooser.ROW_HEIGHT);
		lblAuthor = makeJLabel("Author:",authc+FileChooser.BORDER-authwidth,ycount,authwidth,FileChooser.ROW_HEIGHT,1);
		lvlAuthor = new JTextField();
		lvlAuthor.getDocument().addDocumentListener(this);
		lvlAuthor.setFont(Jned.BOX_FONT);
		lvlAuthor.setHorizontalAlignment(JTextField.LEFT);
		lvlAuthor.setBounds(authc+FileChooser.BORDER*2,ycount,genc-authc-genwidth-2*FileChooser.BORDER, FileChooser.ROW_HEIGHT);
		lblGenre = makeJLabel("Type:",genc+FileChooser.BORDER-genwidth,ycount,genwidth,FileChooser.ROW_HEIGHT,1);
		lvlGenre = new JTextField();
		lvlGenre.getDocument().addDocumentListener(this);
		lvlGenre.setFont(Jned.BOX_FONT);
		lvlGenre.setHorizontalAlignment(JTextField.LEFT);
		lvlGenre.setBounds(genc+FileChooser.BORDER*2,ycount,calcedWidth-genc, FileChooser.ROW_HEIGHT);
		ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;		
		
		saveY = ycount; //This is the y value to set the save/cancel buttons to when in save mode, or the window height to in open mode
		
		//Open/Save and Cancel buttons
		dothething = new Buttonwog(master,"fileChooser dothething",-2,FileChooser.BORDER,ycount,FileChooser.BUTTON_WIDTH,FileChooser.ROW_HEIGHT,true,"");
		add(dothething);
		cancel = new Buttonwog(master,"fileChooser cancel",-2,FileChooser.WINDOW_WIDTH-FileChooser.BORDER-FileChooser.BUTTON_WIDTH,ycount,FileChooser.BUTTON_WIDTH,FileChooser.ROW_HEIGHT,true,"Cancel");
		add(cancel);
		ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;
		
		savePlus = ycount; //This is the y value to set the window height to in save mode
		
		setPreferredSize(new Dimension(FileChooser.WINDOW_WIDTH,ycount));
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.pack();
		
		viewHeight = view.getExtentSize().height;
		
		
		//Overwrite dialog
		overwrite = new JDialog(fred, "Overwrite?", true);
		overwrite.getContentPane().setBackground(Jned.BG_COLOR);
		overwrite.setLayout(null);
		
		ycount = FileChooser.BORDER;
		overwrite.add(makeJLabel("Are you sure you want to overwrite this level?",FileChooser.BORDER,ycount,FileChooser.OVERWRITE_WIDTH-2*FileChooser.BORDER,FileChooser.ROW_HEIGHT,0));
		ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;
		overwrite.add(new Buttonwog(master,"fileChooser overwriteOK",-2,FileChooser.BORDER,ycount,FileChooser.BUTTON_WIDTH,FileChooser.ROW_HEIGHT,true,"OK"));
		overwrite.add(new Buttonwog(master,"fileChooser overwriteCancel",-2,FileChooser.OVERWRITE_WIDTH-2*FileChooser.BORDER-FileChooser.BUTTON_WIDTH,ycount,FileChooser.BUTTON_WIDTH,FileChooser.ROW_HEIGHT,true,"Cancel"));
		ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;
		
		overwrite.setLocationRelativeTo(null);
		overwrite.getContentPane().setPreferredSize(new Dimension(FileChooser.OVERWRITE_WIDTH,ycount));
		overwrite.pack();
	}
	//Helper method for JLabels
	private JLabel makeJLabel(String txt, int xpos, int ypos, int wid, int hei, int align) {
		JLabel lbl = new JLabel(txt,(align==-1?SwingConstants.LEFT:(align==1?SwingConstants.RIGHT:SwingConstants.CENTER)));
		lbl.setBounds(xpos, ypos, wid, hei);
		lbl.setForeground(Color.BLACK);
		return lbl;
	}
	
	//Shows the dialog, adjusting for either saving or opening
	public void open(boolean isSave) {
		isSaving = isSave;
		select(-1);
		frame.setTitle(isSaving?"Save level":"Open level");
		dothething.setText(isSaving?"Save":"Open");
		remove(lblName);
		remove(lvlName);
		remove(lblAuthor);
		remove(lvlAuthor);
		remove(lblGenre);
		remove(lvlGenre);
		if(isSaving) {
			add(lblName);
			add(lvlName);
			add(lblAuthor);
			add(lvlAuthor);
			add(lblGenre);
			add(lvlGenre);
			String [] atts = master.getAttributes();
			lvlName.setText(atts[0]);
			lvlAuthor.setText(atts[1]);
			lvlGenre.setText(atts[2]);
			dothething.setLocation(FileChooser.BORDER,saveY);
			dothething.enableButton();
			cancel.setLocation(FileChooser.WINDOW_WIDTH-FileChooser.BORDER-FileChooser.BUTTON_WIDTH,saveY);
			setPreferredSize(new Dimension(FileChooser.WINDOW_WIDTH,savePlus));
		} else {
			dothething.setLocation(FileChooser.BORDER,openY);
			cancel.setLocation(FileChooser.WINDOW_WIDTH-FileChooser.BORDER-FileChooser.BUTTON_WIDTH,openY);
			setPreferredSize(new Dimension(FileChooser.WINDOW_WIDTH,saveY));
		}
		populateList();
		frame.pack();
		frame.setVisible(true);
	}
	
	//Creates the list of levels. Each one is a a button
	private void populateList() {
		Nfile usrlvls = new Nfile(userlevels);
		String[] names = usrlvls.getNames();
		listSize = names.length;
		int ycount = FileChooser.BORDER;
		levels.removeAll();
		lvlButtons = new LevelButton[listSize];
		for(int i = 0; i < listSize; i++) {
			lvlButtons[i] = new LevelButton(master,i,names[i],usrlvls.getAttr1(i),usrlvls.getAttr2(i),FileChooser.BORDER,ycount,calcedWidth,FileChooser.ROW_HEIGHT);
			levels.add(lvlButtons[i]);
			ycount += FileChooser.BORDER + FileChooser.ROW_HEIGHT;
		}
		levels.setPreferredSize(new Dimension(FileChooser.WINDOW_WIDTH - 17,ycount));
		usrlvls.close();
	}
	
	//Interface methods
	public void push(String com) {
		try {
			int buttonNumber = Integer.parseInt(com);
			if(isSaving) {
				lvlName.setText(lvlButtons[buttonNumber].name);
				lvlAuthor.setText(lvlButtons[buttonNumber].author);
				lvlGenre.setText(lvlButtons[buttonNumber].genre);
			} else {
				select(buttonNumber);
			}
		} catch (NumberFormatException ex) {
			switch (com) {
				case "dothething":
					if(isSaving) {
						if(selection >= 0) {
							overwrite.setVisible(true);
						} else {
							save();
						}
					} else {
						Nfile usrlvls = new Nfile(userlevels);
						master.loadLevel(lvlButtons[selection].name,lvlButtons[selection].author,lvlButtons[selection].genre,usrlvls.getData(lvlButtons[selection].name));
						//master.updateText(usrlvls.getData(lvlButtons[selection].name)); //Places the selected level's data into the text box
						usrlvls.close();
						//master.push("tboxlvl");											//Loads the text box data to the level area, just as if you'd pushed 'Load text'
						//master.setAttributes(lvlButtons[selection].name,lvlButtons[selection].author,lvlButtons[selection].genre);
						master.savedAs = true;
					}
					//Intentional continuation into next case
				case "cancel":
					frame.setVisible(false);
					break;
				case "fileChange":
					int result = fchooser.showOpenDialog(frame);
					if(result == JFileChooser.APPROVE_OPTION) {
						userlevels = fchooser.getSelectedFile().getPath();
						if(userlevels.indexOf(".txt")==-1) userlevels += ".txt";
						master.setUserlevels(userlevels);
						fileName.setText(userlevels);
						populateList();
						config.setData(userlevels,"fpath");
					}
					break;
				case "lvlName":
				
					break;
				case "lvlAuthor":
				
					break;
				case "lvlGenre":
				
					break;
				case "overwriteOK":
					save();
					//Intentional continuation into next case
				case "overwriteCancel":
					overwrite.setVisible(false);
					break;
				default:
					System.out.println("Unrecognized command");
					break;
			}
		}
	}
	public void insertUpdate(DocumentEvent e) {
		checkFields();
	}
	public void removeUpdate(DocumentEvent e) {
		checkFields();
	}
	public void changedUpdate(DocumentEvent e) {}
	//Checks the name, author, and genre fields against the list to see if they all match one of the levels. Selects any match, selects none if there is no match.
	private void checkFields() {
		boolean notMatched = true;
		for(int i = 0; i < listSize; i++) {
			if(lvlButtons[i].name.equals(lvlName.getText())) {
				if(lvlButtons[i].author.equals(lvlAuthor.getText())) {
					if(lvlButtons[i].genre.equals(lvlGenre.getText())) {
						//Full match
						select(i);
						notMatched = false;
					}
				}
			}
		}
		if(notMatched) {
			select(-1);
		}
	}
	
	//Saves the level
	public void save() {
		if(selection > 0) { //Overwriting
			master.setAttributes(lvlName.getText(),lvlAuthor.getText(),lvlGenre.getText());
			master.savedAs = true;
			master.push("save");
		} else {
			master.saveAs(lvlName.getText(),lvlAuthor.getText(),lvlGenre.getText());
			populateList();
		}
	}
	
	//Highlights one of the buttons
	private void select(int number) {
		if(selection >= 0) {
			lvlButtons[selection].selected = false;
			lvlButtons[selection].repaint();
		}
		if(number < 0 || number >= listSize || selection == number) {
			selection = -1;
			if(!isSaving) dothething.disableButton();
		} else {
			selection = number;
			lvlButtons[selection].selected = true;
			lvlButtons[selection].repaint();
			if(!isSaving) dothething.enableButton();
			
			//Viewport adjustment
			int calcy = (FileChooser.BORDER + FileChooser.ROW_HEIGHT)*selection;
			int	diffTop = view.getViewPosition().y - calcy;
			if(diffTop > 0) {
				view.setViewPosition(new Point(0,view.getViewPosition().y - diffTop));
			} else {
				int diffBottom = calcy + FileChooser.BORDER*2 + FileChooser.ROW_HEIGHT - view.getViewPosition().y - viewHeight;
				if(diffBottom > 0) {
					view.setViewPosition(new Point(0,view.getViewPosition().y + diffBottom));
				}
			}
		}
	}
	
	private class LevelButton extends Buttonwog {
		protected String	name,
							author,
							genre;
		private int			number;
		protected boolean 	selected = false;
	

		public LevelButton(Jned mind, int num, String nom, String auth, String gen, int xpos, int ypos, int width, int height) {
			super(mind,"fileChooser " + num,-2,xpos,ypos,width,height,true,"");
			name = nom;
			author = auth;
			genre = gen;
			number = num;
		}
		
		public void paintText(Graphics g) {
			g.setFont(FileChooser.LEVEL_FONT);
			g.drawString("" + number,FileChooser.BORDER,getHeight()/2+Jned.DEF_FONT_YOFF);
			g.drawString(name,lvlc,getHeight()/2+Jned.DEF_FONT_YOFF);
			g.drawString(author,authc,getHeight()/2+Jned.DEF_FONT_YOFF);
			g.drawString(genre,genc,getHeight()/2+Jned.DEF_FONT_YOFF);
			
			//Tacked-on drawing of border for selected level
			if(selected) {
				g.setColor(Jned.BUTTON_SEL);
				g.drawRect(0, 0, getWidth()-1, getHeight()-1);
			}
		}
	}
}