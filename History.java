/*
History.java
James Porter 03/20/2013

History list for the Jned application. Keeps a list of previous states of the n level, stored as text files
using the n editor format.
*/

import java.util.ArrayList;

public class History {
	private ArrayList<String> list;
	private int index;
	
	//Constructor
	public History () {
		list = new ArrayList<String>();
		index = -1;
	}
	
	public void add(String text) {
		if(index < list.size() - 1) {
			for (int i = list.size() - 1; i > index; i--) {
				list.remove(i);
			}
		}
		list.add(text);
		index++;
	}
	
	public String current() {
		if (index==-1) return "";
		return list.get(index);
	}
	
	public String undo() {
		if(index < 0) return null;
		if(index == 0) return list.get(0);
		return list.get(--index);
	}
	
	public String redo() {
		if(index < 0) return null;
		if(index == list.size() - 1) return list.get(index);
		return list.get(++index);
	}
	
	public void clear() {
		ArrayList<String> nlist = new ArrayList<String>();
		nlist.add(list.get(index));
		index = 0;
		list = nlist;
	}
}