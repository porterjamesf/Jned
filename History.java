import java.util.ArrayList;

/**
 * A backlog of changes to the level in Jned. Each time the level is edited, the new state is
 * output as a String in n level code, which is appended to History's list. Old versions can
 * be easily returned to by simply moving through the list and loading the level code.
 * @author James Porter
 */
public class History {
  private ArrayList<String> list;
  private int index;
  
  /**
   * Constructs a new History.
   */
  public History () {
    list = new ArrayList<String>();
    index = -1;
  }
  
  /**
   * Adds a new version of the level to the front of the list.
   * @param levelCode String version of level, in n level code
   */
  public void add(String levelCode) {
    if (index < list.size() - 1) {
      for (int i = list.size() - 1; i > index; i--) {
        list.remove(i);
      }
    }
    list.add(levelCode);
    index++;
  }
  
  /**
   * Returns the level at the current position of the list.
   * @return String version of current level, in n level code. Returns a blank string if no
   * level has been added.
   */
  public String current() {
    if (index == -1) {
      return "";
    }
    return list.get(index);
  }
  
  /**
   * Moves one place backward in the list and returns the new current level.
   * @return String version of new current level, after moving back one place. Returns null
   * if no level has been added.
   */
  public String undo() {
    if (index < 0) {
      return null;
    }
    if (index == 0) {
      return list.get(0);
    }
    return list.get(--index);
  }
  
  /**
   * Moves one place forward in the list, if possible, and returns the new current level.
   * @return String version of new current level, after moving forward one place. Returns null if
   * no level has been added.
   */
  public String redo() {
    if (index < 0) {
      return null;
    }
    if (index == list.size() - 1) {
      return list.get(index);
    }
    return list.get(++index);
  }
  
  /**
   * Clears the history, erasing the list and adding only the current level.
   */
  public void clear() {
    ArrayList<String> newList = new ArrayList<String>();
    newList.add(list.get(index));
    index = 0;
    list = newList;
  }
}