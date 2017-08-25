/*
 * Miles Krusniak
 * SnakeObject.java (Snake)
 * 
 * SnakeObject represents the snake in its namesake game. Mainly it
 * contains the location (in board boxes) of the snake's parts and
 * the snake's direction.
 */

import java.awt.*;

public class SnakeObject {
    /*
     * Representation of the snake in the game - its location, size, and direction.
     * 
     * Extends: Nothing
     * Implements: Nothing
     */
    
    //Directional constants
    final static int LEFT = 0;
    final static int RIGHT = 2;
    final static int UP = 4;
    final static int DOWN = 3;
    
    //Instance variables, they are self-explanatory
    private Point[] location;
    private int size;
    private int direction;
    
    public SnakeObject() {
        /*
         * Constructor for SnakeObjects.
         * 
         * Parameters: None
         * Returns: Constructed SnakeObject
         */
        
        //The snake always starts in the same position, facing the same direction.
        size = 3;
        direction = RIGHT;
        location = new Point[size];
        
        location[0] = new Point(4, 4);
        location[1] = new Point(3, 4);
        location[2] = new Point(2, 4);
    }
    
    /*
     * 
     * Following are "behavior" methods.
     * 
     */
    public void extend(int extendBy) {
        /*
         * Extends the snake object by extendBy units.
         * 
         * Parameters: extendBy - the number of units to extend the snake by.
         * Returns: Nothing
         */
        size += extendBy;
        Point[] newLocation = new Point[size];
        
        for(int i = 0; i < location.length; i++) {
            newLocation[i] = location[i];
            //This will leave two null spaces at the end of the new array.
            //The worm will "extend" from these boxes as it moves.
        }
        location = newLocation;
    }
    public void move() {
        /*
         * Moves the snake in the direction specified by the instance variable of the same name.
         * 
         * Parameters: None
         * Returns: Nothing
         */
        for(int i = location.length - 2; i >= 0; i--) {
            location[i + 1] = location[i];
            //This will leave the frontmost point, the one at
            //index 0, as a copy that can be written over.
            //The last point will be consumed - the snake has moved off that tile
        }
        
        //Depending on direction, the "head" of the snake will extend in a certain direction.
        if(direction == LEFT) location[0] = new Point(location[1].x - 1, location[1].y);
        if(direction == RIGHT) location[0] = new Point(location[1].x + 1, location[1].y);
        if(direction == UP) location[0] = new Point(location[1].x, location[1].y - 1);
        if(direction == DOWN) location[0] = new Point(location[1].x, location[1].y + 1);
    }
    
    /*
     * 
     * Following are mutator and accessor methods.
     * 
     */
    public void changeDirection(int direction) {
        //Meant to be used with directional constants in this class
        this.direction = direction;
    }
    public void changeSize(int increment) {
        //A hard change for the size, not used in this program
        size += increment;
    }
    public Point[] getLocation() {
        //Returns: The location instance variable of this SnakeObject.
        return location;
    }
    public int getDirection() {
        //Returns: The direction instance variable of this SnakeObject.
        return direction;
    }
    public int getSize() {
        return size;
    }
}