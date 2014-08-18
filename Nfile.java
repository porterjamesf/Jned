/*
Nfile.java
James Porter	01/05/2013

An object that pairs with a file (such as the config file or the n userlevels file) and provides functionality for reading and writing entries
from that file.

The file must have an ampersand '&' at some point prior to all data entries. Anything before that is ignored. After that, each entry must use
the syntax of:
$name#attribute#attribute#data#
Anything that is outside of a '$' and the fourth '#' after it will be ignored. Any entry lacking four '#'s before the next '$' will be ignored.
*/

import java.util.ArrayList;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.File;
import java.awt.Rectangle;
import java.nio.charset.Charset;

public class Nfile {
	public final int BUFFER_SIZE = 32;
	private Path path;
	private SeekableByteChannel bc;
	private ByteBuffer buf;
	private String line;
	private ArrayList<Integer[]> pentries;	//Byte positions of the $ and # characters defining each entry
	private ArrayList<String> sentries;		//Each entry's name, as a String. Taken from between the $ and the first #
	
	//Constructor
	public Nfile (String spath) {
		path = Paths.get(spath);
		if(Files.exists(path)) {
			try {
				bc = Files.newByteChannel(path, new OpenOption[] {StandardOpenOption.READ, StandardOpenOption.WRITE});
				buf = ByteBuffer.allocate(BUFFER_SIZE);
				scan();
			} catch (IOException ioe) {
				System.out.println("IO exception opening " + path);
				close();
			}
		} else {
			//System.out.println("Could not find file: " + path + "\nCreating new file.");
			try {
				File lvlFile = new File(spath);
				if(lvlFile.createNewFile()) {
					bc = Files.newByteChannel(path, new OpenOption[] {StandardOpenOption.READ, StandardOpenOption.WRITE});
					buf = ByteBuffer.allocate(BUFFER_SIZE);
					scan();	//The first scan initializes values so that the write operation will work
					write("Jned local user levels storage \n&\n",(int)bc.size(),(int)bc.size());
					scan(); //The second scan re-initializes with the ampersand in place, so that subsequent writing of level entries will work properly
				} else {
					System.out.println("Creation of " + path + " failed.");
				}
			} catch (IOException ioe) {
				System.out.println("IO exception creating " + path + ": \n" + ioe);
				close();
			}
		}
	}
	//Scans through the file and fills out internal arrays of data for entries
	private void scan() {
		try {
			pentries = new ArrayList<Integer[]>();
			sentries = new ArrayList<String>();
			bc.position(0);
			buf.clear();
			//Scans the file and notes positions of all entries, creates array entries for them
			int beg, end, ind;
			boolean started = false;
			while((end = bc.read(buf)) > 0) {//Loops through sections of the file, 32 bytes at a time
				//Puts this section of bytes into a string variable to work with
				buf.rewind();
				line = Charset.forName("US-ASCII").decode(buf).toString().substring(0,end);
				buf.flip();
				
				beg = 0;
				while(beg<end) { //Each run through the loop looks for a character, either & or $. If it finds one, it loops again,
									//only looking at a substring starting after that character.
					if(!started) { //There may be an extended comment section at the beginning. Data only begins after '&', so that is looked for first
						ind = line.substring(beg,end).indexOf('&');
						if(ind >= 0) {
							//When '&' is found, subsequent loops will look for $'s.
							started = true;
							beg += ind+1;
						} else {
							beg = end;
						}
					} else {
						ind = line.substring(beg,end).indexOf('$');
						if(ind >= 0) {
							//When '$' is found, entries are created in pentries and sentries, and the position is marked in slot 0 of pentries.
							pentries.add(new Integer[5]);
							pentries.get(pentries.size()-1)[0] = (int)bc.position()-end+ind+beg;
							sentries.add("");
							beg += ind+1;
						} else {
							beg = end;
						}
					}
				}
			}
			//Goes to each entry and fills out information for it
			for (int i = pentries.size()-1; i >= 0; i--) {
				bc.position(pentries.get(i)[0]); //Begin at the previously marked position of the $
				
				boolean notdone = true;
				int hct = 0, nind;
				while(notdone) { //Loops until finding 4 #'s or another $
					end = bc.read(buf);
					//Puts this section of bytes into a string variable to work with
					buf.rewind();
					line = Charset.forName("US-ASCII").decode(buf).toString();
					buf.flip();
					
					beg = 0;
					while(beg<end) {
						ind = line.substring(beg,end).indexOf('#');
						nind = line.substring(beg+1,end).indexOf('$');
						if(0<nind && nind<ind) {
							System.out.println("Found $ before 4 #'s");
							beg = end;
							notdone = false;
						} else {
							if(ind >= 0) {
								hct++;
								switch (hct) {
									case 1:
									case 2:
									case 3: //For any of the first three sections, the position is saved in pentries and the loop continues
										pentries.get(i)[hct] = (int)bc.position()-end+ind+beg;
										beg += ind+1;
									break;
									case 4:
										//For the fourth one, the position is saved and this entry is completed
										pentries.get(i)[4] = (int)bc.position()-end+ind+beg;
										beg = end;
										notdone = false;
									break;
									default: break;
								}
							} else {
								beg = end;
							}
						}
					}
				}
				//Removes any entries that didn't have the proper number of #'s
				if(hct != 4) {
					System.out.println("Didn't find 4 #'s");
					pentries.remove(i);
					sentries.remove(i);
				}
			}
			//Fills out names
			for(int i = pentries.size()-1; i >= 0; i--) {
				sentries.set(i,getName(i));
			}
			buf.clear();
		} catch (IOException ioe) {
			System.out.println("Could not scan config file.");
		};
	}
	
	//Reads the bytes in between two specific locations in the file
	private String read(int start, int end) {
		try {
			bc.position(start);
			int remaining = end-start;
			String result = "";
			while (remaining > 0) { //Loops until the buffer contains the last part of this segment
				bc.read(buf);			
				//Puts this section of bytes into a string variable to work with
				buf.rewind();
				line = Charset.forName("US-ASCII").decode(buf).toString();
				buf.flip();
				
				//Adds this section to the total string
				result += line.substring(0,Math.min(BUFFER_SIZE,remaining));
				remaining -= BUFFER_SIZE;
			}
			
			buf.clear();
			return result;
		} catch (IOException ioe) {
			return null;
		}
	}
	//Writes a string in place of whatever is between two specific locations in the file
	private void write(String str, int start, int end) {
		try {
			String temp = read(end,(int)bc.size()); //Stores all of file that comes after this edit, to be appended later
			bc.position(start);
			bc.truncate(start); //Chops off file at start of this edit
			
			//Writes the string to the file
			byte[] source = str.getBytes();
			int remaining = source.length, off = 0;
			while (remaining > 0) {
				//Puts bytes from the string into the buffer
				buf.clear();
				buf.put(source, off, Math.min(BUFFER_SIZE,remaining));
				buf.flip();
				
				//write buffer to file
				bc.write(buf);
				remaining -= BUFFER_SIZE;
				off += BUFFER_SIZE;
			}
			
			//Writes the chopped off file end to the file
			source = temp.getBytes();
			remaining = source.length;
			off = 0;
			while (remaining > 0) {
				//Puts bytes from the string into the buffer
				buf.clear();
				buf.put(source, off, Math.min(BUFFER_SIZE,remaining));
				buf.flip();
				
				//write buffer to file
				bc.write(buf);
				remaining -= BUFFER_SIZE;
				off += BUFFER_SIZE;
			}
			
			//Adjusts indices in pentries to new positions
			int amount = str.length() + start - end;
			for(Integer[] par : pentries) {
				for(int i = 0; i < 5; i++) {
					if(par[i] > start) par[i] += amount;
				}
			}
			
			buf.clear();
		} catch (IOException ioe) {
			System.out.println("Error writing '" + str + "'to file.");
		}
	}
	public String getPath () {
		return path.toString();
	}
	
	public String getName (int index) {
		if(index<pentries.size() && index >= 0) {
			return read(pentries.get(index)[0]+1,pentries.get(index)[1]);
		} else {
			return null;
		}
	}
	public String getAttr1 (int index) {
		if(index<pentries.size() && index >= 0) {
			return read(pentries.get(index)[1]+1,pentries.get(index)[2]);
		} else {
			return null;
		}
	}
	public String getAttr2 (int index) {
		if(index<pentries.size() && index >= 0) {
			return read(pentries.get(index)[2]+1,pentries.get(index)[3]);
		} else {
			return null;
		}
	}
	public String getData (int index) {
		if(index<pentries.size() && index >= 0) {
			return read(pentries.get(index)[3]+1,pentries.get(index)[4]);
		} else {
			return null;
		}
	}
	public String getAttr1 (String name) {
		return getAttr1(sentries.indexOf(name));
	}
	public String getAttr2 (String name) {
		return getAttr2(sentries.indexOf(name));
	}
	public String getData (String name) {
		getNames();																													//Why is this here? Try removing it later
		return getData(sentries.indexOf(name));
	}
	//Returns the list of all entry names 
	public String[] getNames() {
		String[] result = new String[sentries.size()];
		for(int i = 0; i < result.length; i++) {
			result[i] = sentries.get(i);
		}
		return result;
	}
	//Returns a list of all entry names that match a certain attribute (1 or 2)
	public String[] getNames(String att, int attrInd) {
		if(attrInd < 1 || attrInd > 2) return null;
		ArrayList<String> res = new ArrayList<String>();
		for(int i = 0; i < sentries.size(); i++) {
			if((attrInd==1?getAttr1(i):getAttr2(i)).equals(att)) {
				res.add(sentries.get(i));
			}
		}		
		String[] result = new String[res.size()];
		for(int i = 0; i < result.length; i++) {
			result[i] = res.get(i);
		}
		return result;
	}
	
	//Writing of names, attributes, and data
	public void setName (String newname, int index) {
		if(index<sentries.size() && index >= 0) {
			write(newname, pentries.get(index)[0]+1,pentries.get(index)[1]);
		}
	}
	public void setAttr1 (String info, int index) {
		if(index<sentries.size() && index >= 0) {
			write(info, pentries.get(index)[1]+1,pentries.get(index)[2]);
		}
	}
	public void setAttr2 (String info, int index) {
		if(index<sentries.size() && index >= 0) {
			write(info, pentries.get(index)[2]+1,pentries.get(index)[3]);
		}
	}
	public void setData (String info, int index) {
		if(index<sentries.size() && index >= 0) {
			write(info, pentries.get(index)[3]+1,pentries.get(index)[4]);
		}
	}
	public void setName (String newname, String oldname) {
		setName(newname, sentries.indexOf(oldname));
	}
	public void setAttr1 (String info, String name) {
		setAttr1(info, sentries.indexOf(name));
	}
	public void setAttr2 (String info, String name) {
		setAttr2(info, sentries.indexOf(name));
	}
	public void setData (String info, String name) {
		setData(info, sentries.indexOf(name));
	}
	public void writeNew (String name, String attr1, String attr2, String data) {
		try {
			String entry = "\n$" + name + "#" + attr1 + "#" + attr2 + "#" + data + "#";
			write(entry,(int)bc.size(),(int)bc.size());
			scan();
		} catch (IOException ioe) {
			System.out.println("Error writing '" + name + "'to file.");
		}
	}
	//Removes an entry from the file
	public void delete (String name) {
		int ind = sentries.indexOf(name);
		if (ind > 0) {
			int start = pentries.get(ind)[0];
			if(read(start-1,start).equals("\n")) start--;
			write("",start,pentries.get(ind)[4]+1);
		}
		scan();
	}
	
	//Closes resources
	public void close() {
		try {if(bc != null) bc.close();} catch (IOException ioe) {}
	}
	
	//Test method
	public static void main(String[] args) {
		Nfile nf = new Nfile("ftxt.txt");
		nf.close();
	}
}