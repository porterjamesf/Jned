import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JPanel;

/**
 * A drop-down JPanel extension that lays over Jned. DropPanels will remain visible until the mouse
 * exits the panel, and then disappear. Each DropPanel has an associated Button in charge of
 * dropping the panel; it will be unpushed when the DropPanel disappears.
 * @author James Porter
 */
public class DropPanel extends JPanel implements MouseListener {
  private Button master;
  
  /**
   * Constructs a new DropPanel with the given dimensions and associated Button.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param width the width
   * @param height the height
   * @param button the associated Button that turns this DropPanel on or off
   */
  public DropPanel(int x, int y, int width, int heigth, Button button) {
    super();
    setBackground(Colors.DROP_COLOR);
    setLayout(null);
    setBounds(x, y, width, heigth);
    
    master = button;
    setVisible(false);
    addMouseListener(this);
  }
  
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(Colors.DROP_BORDER);
    g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
  }
  
  public void mouseExited (MouseEvent me) {
    // Without this check, the mouse would exit every time it went over any component added to
    // the DropPanel
    if (!(new Rectangle(0, 0, getWidth(), getHeight())).contains(me.getPoint())) {
      master.setPushed(false);
      master.repaint();
      setVisible(false);
    }
  }
  
  public void mouseClicked (MouseEvent me) {}
  public void mouseEntered (MouseEvent me) {}
  public void mousePressed (MouseEvent me) {}
  public void mouseReleased(MouseEvent me) {}
}