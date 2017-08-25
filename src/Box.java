/*
 * Miles Krusniak
 * Box.java (Snake)
 * 
 * A Box is a JPanel of a certain size with border lines. In Snake I use
 * it to make the grid.
 */

import java.awt.*;
import javax.swing.*;

public class Box extends JPanel {
    /*
     * A JPanel with border lines.
     * 
     * Extends: JPanel
     * Implements: Nothing
     */
    
    //Variables corresponding to the Box's dimensions. I
    //use square boxes in this program but I also figured that 
    //I might reuse this class with rectangular boxes.
    int sideX;
    int sideY;
    
    public Box(int x, int y) {
        /*
         * Constructor for Boxes.
         * 
         * Parameters:
         * x - x dimension of the Box
         * y - y dimension of the Box
         * Returns: A constucted Box.
         */
        
        sideX = x;
        sideY = y;
    }
    public void paintComponent(Graphics g) {
        /*
         * This method is what lets us change how the panel is displayed
         * on the screen. We are intercepting JPanel'spaintComponent
         * to draw our own shape on the panel. We will be drawing
         * border lines around the panel.
         * 
         * Parameters: g - the Graphics object we are drawing when we draw the Box.
         * Returns: Nothing
         */
        super.paintComponent(g);
        
        //Draw the border
        //Horizontal lines
        g.drawLine(0, 0, sideX, 0);
        g.drawLine(0, sideY, sideX, sideY);
        
        //Vertical lines
        g.drawLine(0, 0, 0, sideY);
        g.drawLine(sideX, 0, sideX, sideY);
    }
}