/*
Pushable.java
James Porter	01/05/2013

Interface for buttons and multibuttons, which each have a push method. Useful for action commands that work the same on any of the buttons in a multibutton
*/

public interface Pushable {
	public void push();
	public void unpush();
}