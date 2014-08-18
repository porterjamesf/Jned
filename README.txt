__________________________________________
  _____         _____ ___
      | |\    | |     |  \
      | | \   | |     |   \
      | |  \  | |--   |   |
  \   | |   \ | |     |   /
   \_/  |    \| |____ |__/   v1.0

A Java-based n level editor
Created by James Porter and Matt Hulsey
__________________________________________

Table of contents:
    1: Setting up and running the application
    2: Editing a level
	a: Tiles
	b: Items and enemies
	c: Gridlines and snapping
	d: Triggers and paths
	e: Undo/redo
    3: Saving/loading a level
	a: Textbox
	b: Files
    4: Keyboard shortcuts
__________________________________________

1 - SETTING UP AND RUNNING THE APPLICATION

Setting up Jned is a breeze. Just download, unzip, and it's ready to go. To run,
you'll need to have Java installed on your computer. If you don't have the Java
Runtime Environment (JRE) installed, you can download it from the Oracle website:

http://www.oracle.com/technetwork/java/javase/downloads/index.html

Jned was created using version 7, so any Java version 7 or up should be sufficient.

If you are unfamiliar with running a java application, the next step may not be
obvious. Open the command prompt or terminal and navigate to the folder containing
Jned. Then, type the command:

java Jned

and hit enter. It should start up. If you get an error telling you the command "java"
is unrecognized, you either haven't installed the JRE, or you need to tell your
computer where to look for the definition of the command "java". This is done using a
path variable. You can find instructions for this at:

http://docs.oracle.com/javase/tutorial/essential/environment/paths.html
__________________________________________

2 - Editing a level

When Jned opens, you immediately see the main level editing area. You start out with
a blank level.


2a - Tiles

To work with tiles, make sure the "TILES" button (top-left) is selected. You can hit
any of the tile buttons down the left-hand column to start placing that kind of tile.
Each has a sub-menu of buttons to select direction.

The standard set of keyboard commands from Ned should work here as well, only they
select the mode of what tile to place upon a click, rather than directly placing the
tile. Holding shift will switch to the tile's complement, just like in Ned.

If you have no tile button selected, you can do a few more things. Clicking and drag-
ing will expand the selection box. Now when you select a tile and add, it fills in a
much larger area. You can use this to empty/fill large areas (or the whole level) by
adding the Erase or Fill tiles.

With no tile button selected, try right-clicking. This gives you a drop-down menu
that lets you cut, copy and paste sections of the tileset. Pasting will either crop
or repeat the copied tileset to fit the size of the selection box when pasting. This
allows you to copy a small pattern, increase the selection box to the whole level,
then fill it with that pattern, for example.


2b - Items and enemies

Clicking on the "ITEMS" or "ENEMIES" buttons (top left, below "TILES") puts you into
object-editing mode. Just like the tiles, you can add objects by selecting a button
from the left-hand column.

Objects that have direction will have a sub-menu just to the right. Drones also have 
a set of six sub-menu buttons to choose their behavior. Directional items must always
have a direction selected, but you can deselect all drone behaviors if you want to.
This allows you to place drones that will simply stand still.

If you have no item/enemy button selected, you can do other things. Clicking on an
existing object will select it. Clicking and dragging lets you move it wherever you
like. You can drag a box to select multiple objects at once, or ctrl-click on
individual objects to add them (or remove them) from the selection. The Delete key
is reserved for TILES mode, as in Ned, but you can push Backspace to delete selected
objects.

If you right-click on an object, you'll get a drop-down menu for it. This allows you
to do a variety of things, such as changing direction or behavior, nuding the
position (which you can also do with the arrow keys), or cut/copy/paste operations.
If you have multiple objects selected, you'll see options for all of them.

If you right click on a player object when there are multiple player objects on the
level, you may see the option "Set to Active Player". N only allows one of the player
objects to be the one you control when you play the level. This option allows you to
freely select which one it is.

Launchpads have their own special options. You can manipulate not only how strong
they are (the power), but also the precise direction they will fling you in a full
360 degree range. Or, you can set the direction to one of the 8 defaults.


2c - Gridlines and snapping

Like Ned, Jned can display gridlines to help you organize your level. It also has a
snap-to feature to help you line up objects just right. These can be turned on and
off with the "Gridlines" and "Snapping" buttons at the top, just to the right of the
"TILES" button. 

To the right of each of those buttons is a little down arrow. This gives you access
to the settings for each. You can choose from a menu of existing presets (it comes
stocked with all the familiar gridline and snap settings from Ned), or start making
your own. 

Gridline settings consist of three levels (primary, secondary, and tertiary), all of
which are optional with the on/off button. Each level has the following parameters:

    Spacing  - How far apart each gridline is, for both vertical lines (x) and
                horizontal lines (y)
    Offset   - The displacement of the lines, to precisely control how they line up
    Symmetry - Whether the lines should be mirrored accross the center in either
                direction (left/right or top/bottom), or both (quadrants)

There are also buttons labeled Single and Double. Most lines are single lines, but
you can set it to Double to draw each gridline twice. The Spacing parameter set how
far apart the two lines are.

Snap settings are just like gridline settings, only there is only one level. Instead
of the parameters controlling lines, they control snap points that appear where those
lines would intersect. You can also display the snap points if you like.

The keyboard commands from Ned work here too, by default, with one difference: you
have to turn gridlines or snapping on for any of them to work. You can do this with
'v' and 'z', the commands that, in Ned, were used for 'no gridlines' and 'no snap',
respectively. Also, you can use '.' to show/hide the snap points.


2d - Triggers and paths

Jned can display the connections between doors and their triggers (or buttons), just
as in Ned. There's a button to turn them on/off at the top, just to the right of the 
"Gridlines" and "Snapping" buttons. Or, toggle them with the 'r' key.

Jned also can show you the paths that drones will follow. The button is right next
to "Door triggers", or toggle it with 'y'. This is handy for making sure your drones
go just where you want them. Each behavior is programmed in, and the "quasi-random"
behavior simply displays a branching path, showing all the possibilities. Drone paths
are accurate for drones that begin inside of tiles as well.


2e - Undo/redo

One very handy feature of Jned is its editing history. The Undo/Redo buttons in the
upper right (or ctrl-Z and ctrl-Y) give you access to a full history extending back
to when you opened the program or loaded the level.
__________________________________________

3 - Saving/loading a level

Unfortunately, Jned can't let you play your level directly. But getting your level to
and from N is pretty straightforward.


3a - Textbox

Jned comes with a fully accessable text box built in, showing you live changes to the
text code defining your level. The simplest way to test your level is to copy the
level data in the text box and paste it into Ned. If you're familiar with using Ned,
you're no doubt an old hat at this, but for those new to the process, just follow
these steps:

    1 - Copy the text in the text box. You can do this by either selecting it all and
        hitting ctrl-c, or by pressing the "Copy level" button above the text box (or
        just pressing the 'Home' key).
    2 - In n, press the '~' key (or '\') to get to Ned.
    3 - Click inside the "level data" box and hit ctrl-A to select it all.
    4 - Hit ctrl-V to paste your level data in the box. Click somewhere outside the
        box to make sure you won't type anything in it by hitting a key.
    5 - Hit 'L' to load the level data. You should now see your level in Ned. You can
    6 - Hit 'P' to go into play-testing mode. In this mode, use Caps Lock to start or
        stop. If you die, you can respawn by pointing the mouse where you wan to go
        and hitting Enter. To reset, hit '~' or '\' again and then hit 'R'.

That will let you get a level from Jned to Ned, but what about the reverse? If you'd
like to take a level in Ned (or any other source, e.g. NUMA or the userlevels file)
and move it over to Jned, you can do this:

    1 - From the main screen of Ned (the debug menu), hit 'S' to copy the level data
        to the clipboard. Or, for another source, select all the level data and hit
        ctrl-C.
    2 - In Jned, push the "Paste level" button above the text box, or hit just hit
        'Page Down'. This will paste the level data to Jned's textbox, but only if it
        is the correct format for an n level.
    3 - Push the "Load text" button, or hit 'Page Up'. This will load all the level
        data from the text box, just like hitting 'L' in Ned.

This should make it fast and easy to bring up n levels in Jned.

You can also edit levels by manipulating the code directly in the text box. Every
change to the level by ordinary means updates the text box, so if you change some-
thing directly, remember to push "Load text" or hit 'Page Up' before doing anything.
This method of editing is not recommended unless you know what you're doing!


3b - Files

If all of that seems like a huge headache, don't worry. Jned provides a much more
user-friendly way to load and save your level files without having to even look at
the text box. 

All n levels are stored as text in a plain text file. N comes with a special file for
exactly this purpose, called "userlevels.txt". You should be able to find it in your
n directory. This is what n reads to generate the list of user-created levels inside
the game, and let you play them freely.

You can, if you like, store the text of your levels in any text file you want. You
just won't be able to load it up right in n without using the steps outlined in the
above section.

Jned lets you do either of these options easily. When you are ready to save your
level, click on the "File" menu at the top and select "Save" or "Save As". This will
open up the file choosing window. Initially, this won't show any levels. By default,
Jned simply makes a blank text file in its directory to store user-made levels. You
can fill out the information for your level and save it here if you just want to
store your work in progress.

If, however, you'd like to play it, then you should find that n userlevels file. At
the top of the window you can see the path of the leves file that is currently shown.
Any levels stored in that text file will appear listed in the middle. Click on the
"Change" button to bring up a dialog window with which you can navigate to and select
the "userlevels.txt" file in your n directory. Push "Open" to bring up the levels.

Now, you should see all the user levels in that file listed for you. Fill out your
level's information and hit "Save" to add it to the list. In n, go into userlevels.
N will take a moment to parse the userlevels file, and then show you the list. You
should be able to find your level in there and play it.

Note that you can click on any level present and its information will be filled out
in the fields. If you save that, it will write over the selected level. If you make
any changes to the fields, however, it will simply write a new level and leave the
old one.

After that, you can simply make changes in Jned, save them, and then play it right in
n. Note that n needs to parse the userlevels data over again every time something
changes, so whenever to save a new change in Jned, make sure to go to the main menu
in n and back to userlevels. Otherwise, the new change won't show up when you play
your level.

You can also load any level from a text file through the same window by selecting
"File", then "Open". If you want to load levels from another file, just hit "Change"
and find it. Jned will remember the last text file you were looking at for levels and
go there first each time you bring up the file choosing window.
__________________________________________

4 - Keyboard shortcuts

Jned comes with a range of customizable keyboard commands to shortcut most of its
functionality. By default, these are set to match or at least come as close as
possible to the old keyboard commands of Ned, to which many of you are probably
intimately familiar.

However, you are under no obligation to stick to what we thought would be the best
configuration. Every keyboard shortcut is fully customizable. To change them, click
on the "Edit" menu and select "Keyboard Shortcuts". This will bring up a window that
lists every keyboard shortcut and provides buttons to change them.

At the top of this list a Preset selection box. Jned comes stocked with two options.
The first, called "default", is the set of commands that we thought most closely
matched Ned. The second, called "custom", is whatever configuration you have changed
things too, but not yet saved under it's own name.

Jned remembers all changes you make in this custom preset, so that they'll stick
around even if you close the application. But if you change them some more, or load
another preset, the old values will be lost. To keep them, you can save any
configuration you like, and it will thereafter appear as an option in the selection
box.

If you click on the "Change" button for any command, you'll be presented with a
small window. This shows a list of keys for that command, though it's usually just a
list of one. You can add any number of keys for the same command, or remove existing
ones. And, of course, you can simply change one to another. You'll be warned if you
try to add or change to a key that is already used for something else. If you accept
it anyway, it will be removed from whatever other command it was set to before.

If you find yourself running low on keys to use, remember that you can add on the
ctrl, alt, or shift keys to anything, in any combination, allowing for eight
different versions of each key.