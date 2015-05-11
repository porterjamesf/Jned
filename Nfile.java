import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 * A wrapper class for an n userlevels file, or a file formatted in the same way. In Jned, the
 * config file is formatted the same way as the n userlevels file, so that instances of this class
 * can be used to read and edit both.
 * <p>
 * The file must contain an ampersand (&) character to denote the beginning of entries (anything
 * prior to that is considered ignoreable). Entries must take the following form:
 * <p>
 * $name#attribute1#attribute2#data#
 * <p>
 * Any entry without all four # characters will be ignored, as will any text outside the $ and the
 * fourth #.
 * @author James Porter
 */
public class Nfile {
  public final int BUFFER_SIZE = 32;
  private Path path;
  private SeekableByteChannel bc;
  private ByteBuffer buf;
  private String line;
  private ArrayList<Integer[]> entryPositions;
  private ArrayList<String> entryNames;
  
  /**
   * Constructs a new Nfile using the file path given. If the path leads to an existing file, it
   * will be opened for reading/writing by this Nfile instance. If not, a new file will be created
   * and opened.
   * @param filePath the pathname for the file to wrap with this Nfile, as a String
   */
  public Nfile (String filePath) {
    path = Paths.get(filePath);
    if (Files.exists(path)) {
      try {
        bc = Files.newByteChannel(path, new OpenOption[] {StandardOpenOption.READ,
            StandardOpenOption.WRITE});
        buf = ByteBuffer.allocate(BUFFER_SIZE);
        scan();
      } catch (IOException e) {
        System.err.println("IO exception opening " + path + ": \n" + e);
        close();
      }
    } else {
      try {
        File lvlFile = new File(filePath);
        if (lvlFile.createNewFile()) {
          bc = Files.newByteChannel(path, new OpenOption[] {StandardOpenOption.READ,
              StandardOpenOption.WRITE});
          buf = ByteBuffer.allocate(BUFFER_SIZE);
          scan();
          write("Jned local user levels storage \n&\n", (int)bc.size(), (int)bc.size());
          scan();
        } else {
          System.err.println("Creation of " + path + " failed.");
        }
      } catch (IOException e) {
        System.err.println("IO exception creating " + path + ": \n" + e);
        close();
      }
    }
  }
  
  // Scans through the file and fills out internal arrays of data for entries
  private void scan() {
    try {
      entryPositions = new ArrayList<Integer[]>();
      entryNames = new ArrayList<String>();
      bc.position(0);
      buf.clear();
      //Scans the file and notes positions of all entries, creates array entries for them
      int begin;
      int end;
      int index;
      boolean started = false;
      // File is taken in 32 byte chunks
      while ((end = bc.read(buf)) > 0) {
        buf.rewind();
        line = Charset.forName("US-ASCII").decode(buf).toString().substring(0, end);
        buf.flip();
        
        begin = 0;
        while (begin < end) {
          // If the initial & has not been seen yet, it will be checked for exclusively
          if (!started) { //There may be an extended comment section at the beginning. Data only begins after '&', so that is looked for first
            index = line.substring(begin, end).indexOf('&');
            if(index >= 0) {
              started = true;
              begin += index + 1;
            } else {
              begin = end;
            }
          } else {
            // Once the & has been seen, positions of all $'s are recorded
            index = line.substring(begin, end).indexOf('$');
            if (index >= 0) {
              entryPositions.add(new Integer[5]);
              entryPositions.get(entryPositions.size() - 1)[0] = (int) bc.position() - end + index
                  + begin;
              entryNames.add("");
              begin += index + 1;
            } else {
              begin = end;
            }
          }
        }
      }
      
      // After entry start positions ($'s) are recorded, they are revisited and scanned 1 by 1
      for (int i = entryPositions.size() - 1; i >= 0; i--) {
        bc.position(entryPositions.get(i)[0]);
        
        boolean notdone = true;
        int hashCount = 0;
        int nextIndex;
        // For each entry, the file will be scanned until finding four #'s or another $
        while (notdone) {
          end = bc.read(buf);
          buf.rewind();
          line = Charset.forName("US-ASCII").decode(buf).toString();
          buf.flip();
          
          begin = 0;
          while (begin < end) {
            index = line.substring(begin, end).indexOf('#');
            nextIndex = line.substring(begin + 1, end).indexOf('$');
            if (0 < nextIndex && nextIndex < index) {
              System.err.println("Error reading file " + path + ":\nFound '$' before 4 '#'s");
              begin = end;
              notdone = false;
            } else {
              if (index >= 0) {
                hashCount++;
                entryPositions.get(i)[hashCount] = (int) bc.position() - end + index + begin;
                if (hashCount <= 3) {
                  begin += index + 1;
                } else {
                  begin = end;
                  notdone = false;
                }
              } else {
                begin = end;
              }
            }
          }
        }
        if (hashCount != 4) {
          System.err.println("Error reading file " + path + ":/nDidn't find 4 #'s");
          entryPositions.remove(i);
          entryNames.remove(i);
        }
      }
      
      for(int i = entryPositions.size() - 1; i >= 0; i--) {
        entryNames.set(i, getName(i));
      }
      buf.clear();
    } catch (IOException e) {
      System.err.println("Could not scan file: " + path);
    }
  }
  
  // Returns a String of the text between two byte positions in file
  private String read(int start, int end) {
    try {
      bc.position(start);
      int remaining = end - start;
      String result = "";
      while (remaining > 0) {
        bc.read(buf);
        buf.rewind();
        line = Charset.forName("US-ASCII").decode(buf).toString();
        buf.flip();
        
        result += line.substring(0, Math.min(BUFFER_SIZE, remaining));
        remaining -= BUFFER_SIZE;
      }
      
      buf.clear();
      return result;
    } catch (IOException e) {
      return null;
    }
  }
  
  // Writes a String between two byte positions in file, replacing anything that was there
  private void write(String string, int start, int end) {
    try {
      // The write is accomplished by truncating the file at the start of the edit and storing the
      // entirety of the file after the edit in temp.
      String temp = read(end, (int) bc.size());
      bc.position(start);
      bc.truncate(start);
      
      byte[] source = string.getBytes();
      int remaining = source.length;
      int off = 0;
      while (remaining > 0) {
        // Puts bytes from the string into the buffer
        buf.clear();
        buf.put(source, off, Math.min(BUFFER_SIZE, remaining));
        buf.flip();
        
        // Writes buffer to file
        bc.write(buf);
        remaining -= BUFFER_SIZE;
        off += BUFFER_SIZE;
      }
      
      // Writes temp to the file
      source = temp.getBytes();
      remaining = source.length;
      off = 0;
      while (remaining > 0) {
        // Puts bytes from the string into the buffer
        buf.clear();
        buf.put(source, off, Math.min(BUFFER_SIZE, remaining));
        buf.flip();
        
        // Writes buffer to file
        bc.write(buf);
        remaining -= BUFFER_SIZE;
        off += BUFFER_SIZE;
      }
      
      // Adjusts indices in entryPositions to new positions
      int amount = string.length() + start - end;
      for (Integer[] entry : entryPositions) {
        for (int i = 0; i < 5; i++) {
          if (entry[i] > start) {
            entry[i] += amount;
          }
        }
      }
      
      buf.clear();
    } catch (IOException e) {
      System.err.println("Error writing '" + string + "'to file " + path);
    }
  }
  
  /**
   * Returns the filepath for this Nfile.
   * @return filepath, as a String
   */
  public String getPath () {
    return path.toString();
  }
  
  /**
   * Returns the name of the entry at the given index.
   * @param index the index of desired entry
   * @return entry name, or null if index is invalid
   */
  public String getName (int index) {
    if (index < entryPositions.size() && index >= 0) {
      return read(entryPositions.get(index)[0] + 1, entryPositions.get(index)[1]);
    } else {
      return null;
    }
  }
  
  /**
   * Returns the first attribute of the entry at the given index.
   * @param index the index of desired entry
   * @return first attribute, or null if index is invalid
   */
  public String getAttr1 (int index) {
    if (index < entryPositions.size() && index >= 0) {
      return read(entryPositions.get(index)[1] + 1, entryPositions.get(index)[2]);
    } else {
      return null;
    }
  }
  
  /**
   * Returns the second attribute of the entry at the given index.
   * @param index the index of desired entry
   * @return second attribute, or null if index is invalid
   */
  public String getAttr2 (int index) {
    if (index < entryPositions.size() && index >= 0) {
      return read(entryPositions.get(index)[2] + 1, entryPositions.get(index)[3]);
    } else {
      return null;
    }
  }
  
  /**
   * Returns the data of the entry at the given index.
   * @param index the index of desired entry
   * @return data String, or null if index is invalid
   */
  public String getData (int index) {
    if (index < entryPositions.size() && index >= 0) {
      return read(entryPositions.get(index)[3] + 1, entryPositions.get(index)[4]);
    } else {
      return null;
    }
  }
  
  /**
   * Returns the first attribute of the entry with the given name.
   * @param name the name of desired entry, as a String
   * @return first attribute, or null if name matches no entry
   */
  public String getAttr1 (String name) {
    return getAttr1(entryNames.indexOf(name));
  }
  
  /**
   * Returns the second attribute of the entry with the given name.
   * @param name the name of desired entry, as a String
   * @return second attribute, or null if name matches no entry
   */
  public String getAttr2 (String name) {
    return getAttr2(entryNames.indexOf(name));
  }
  
  /**
   * Returns the data of the entry with the given name.
   * @param name the name of desired entry, as a String
   * @return data String, or null if name matches no entry
   */
  public String getData (String name) {
    getNames(); // TASK - try removing this & test
    return getData(entryNames.indexOf(name));
  }
  
  /**
   * Returns a list of names of all entries.
   * @return String[] containing entry names in indexed order
   */
  public String[] getNames() {
    String[] result = new String[entryNames.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = entryNames.get(i);
    }
    return result;
  }
  
  /**
   * Returns a list of names of all entries that match the provided argument in their specified
   * attribute.
   * @param attributeValue the String to match against entries' attributes
   * @param attributeIndex 1 to match entries' first attribute, 2 to match second
   * @return String[] containing names of entries with successful matches
   */
  public String[] getNames(String attributeValue, int attributeIndex) {
    if (attributeIndex < 1 || attributeIndex > 2) {
      return null;
    }
    ArrayList<String> resultList = new ArrayList<String>();
    for (int i = 0; i < entryNames.size(); i++) {
      if ((attributeIndex == 1 ? getAttr1(i) : getAttr2(i)).equals(attributeValue)) {
        resultList.add(entryNames.get(i));
      }
    }    
    String[] result = new String[resultList.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = resultList.get(i);
    }
    return result;
  }
  
  /**
   * Overwrites the name of the entry with the specified index.
   * @param newName the name to write, as a String
   * @param index index of the entry to write to
   */
  public void setName (String newName, int index) {
    if (index < entryNames.size() && index >= 0) {
      write(newName, entryPositions.get(index)[0] + 1, entryPositions.get(index)[1]);
    }
  }
  
  /**
   * Overwrites the first attribute of the entry with the specified index.
   * @param value the new value to write, as a String
   * @param index index of the entry to write to
   */
  public void setAttr1 (String value, int index) {
    if(index < entryNames.size() && index >= 0) {
      write(value, entryPositions.get(index)[1] + 1, entryPositions.get(index)[2]);
    }
  }
  
  /**
   * Overwrites the second attribute of the entry with the specified index.
   * @param value the new value to write, as a String
   * @param index index of the entry to write to
   */
  public void setAttr2 (String value, int index) {
    if(index < entryNames.size() && index >= 0) {
      write(value, entryPositions.get(index)[2] + 1, entryPositions.get(index)[3]);
    }
  }
  
  /**
   * Overwrites the data of the entry with the specified index.
   * @param data the new data String to write
   * @param index index of the entry to write to
   */
  public void setData (String data, int index) {
    if(index < entryNames.size() && index >= 0) {
      write(data, entryPositions.get(index)[3] + 1, entryPositions.get(index)[4]);
    }
  }
  
  /**
   * Overwrites the name of the entry with the given name.
   * @param newName the name to write, as a String
   * @param oldName the former name of desired entry
   */
  public void setName (String newname, String oldName) {
    setName(newname, entryNames.indexOf(oldName));
  }
  
  /**
   * Overwrites the first attribute of the entry with the given name.
   * @param value the new value to write, as a String
   * @param name the name of desired entry, as a String
   */
  public void setAttr1 (String value, String name) {
    setAttr1(value, entryNames.indexOf(name));
  }
  
  /**
   * Overwrites the second attribute of the entry with the given name.
   * @param value the new value to write, as a String
   * @param name the name of desired entry, as a String
   */
  public void setAttr2 (String value, String name) {
    setAttr2(value, entryNames.indexOf(name));
  }
  
  /**
   * Overwrites the data of the entry with the given name.
   * @param data the new data String to write
   * @param name the name of desired entry, as a String
   */
  public void setData (String data, String name) {
    setData(data, entryNames.indexOf(name));
  }
  
  /**
   * Writes a new entry into the file.
   * @param name the name of the new entry, as a String
   * @param attr1 the value of the first attribute, as a String
   * @param attr2 the value of the second attribute, as a String
   * @param data the data String of the new entry
   */
  public void writeNew (String name, String attr1, String attr2, String data) {
    try {
      String entry = "\n$" + name + "#" + attr1 + "#" + attr2 + "#" + data + "#";
      write(entry, (int) bc.size(), (int) bc.size());
      scan();
    } catch (IOException e) {
      System.err.println("Error writing '" + name + "' to file " + path);
    }
  }
  
  /**
   * Deletes the entry with the given name from the file.
   * @param name the name of the entry to delete, as a String
   */
  public void delete (String name) {
    int ind = entryNames.indexOf(name);
    if (ind > 0) {
      int start = entryPositions.get(ind)[0];
      if (read(start - 1, start).equals("\n")) {
        start--;
      }
      write("", start, entryPositions.get(ind)[4] + 1);
    }
    scan();
  }
  
  /**
   * Closes file resources.
   */
  public void close() {
    try {
      if (bc != null) {
        bc.close();
      }
    } catch (IOException e) {}
  }
}