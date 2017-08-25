/*
 * Miles Krusniak
 * SnakeWindow.java (Snake)
 * 
 * This represents a window for the game of Snake to be displayed on. It's
 * part of the larger Snake project. I will be using absolute positioning 
 * instead of a layout manager so that I can get the grid where I want it.
 * 
 * Style note: When I'm extending another class, I tend to forget that I get
 * all of its methods, so I wonder where the methods are coming from. As a
 * result I like to say "this.method()" in these cases (instead of just method()).
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SnakeWindow extends JFrame {
    /*
     * Represents a window containing the game of Snake.
     * Extends: JFrame, since this is a window.
     * Implements: Nothing
     */
    
    //Game status variables.     
    //Not private because they are accessed,
    //edited, etc. by SnakeGame, and it's impractical to make accessor/mutator methods
    boolean gameHasStarted = false; //Whether
    boolean paused = false;
    boolean waitingForName = false;
    
    //SnakeObjects.
    private SnakeListener listener; //the listener to apply to the window components
    private SnakeGame game; //the game from which this particular instance is executing from
    
    //Components
    //Most of these speak for themselves. Buttons aren't private in this case
    //because we need to change their text in some cases.
    private Box[][] grid; //Grid of special JPanels to draw on (display the game)
    JButton start; //Start (and Reset) button
    JButton pause; //Pause (and Resume) button
    private JButton scores;
    private JTextArea info; //Text below the grid
    private JLabel title; //Simply says "Snake" above the grid
    private JTextArea controls; //Last-minute addition: Controls displayed under buttons
    private JSeparator line; //Separator between buttons and grid
    private JMenuBar menuBar; //Menu bar at top
    private JMenu optionMenu; //'Option' menu setting
    private JMenu itemMenu; //'Item' menu setting.
    
    //CheckBoxMenuItems: The first group in 'Item', the second group in 'Option'
    JCheckBoxMenuItem speedItem, slowItem, growthItem, pointsItem, lousyItem, awesomeItem, deathItem;
    JCheckBoxMenuItem chaosMode, wormInsteadOfSnake;
    
    //High scores window and components
    JDialog scoresWindow;
    JTextArea scoresText;
    JButton scoresClose;
    JTextField scoresField;
    
    //Sizing variables, for absolute positioning of components
    private int gridX, gridY; //Will be calculated as size in pixels of grid, length and width
    private final int BOX_SIZE = 10; //Pixel size of each box in the grid
    private final int GRID_LOC_Y = 40, GRID_LOC_X = 20; //Grid top-right-corner location
    
    public SnakeWindow(SnakeGame game, int gridSizeX, int gridSizeY) {
        /*
         * Constructor for SnakeWindow object, which is a kind of JFrame
         * with a grid on the left and buttons on the right.
         * 
         * Parameters: 
         * game - the SnakeGame the window should execute with
         * gridSizeX - number of boxes of grid length
         * gridSizeY - number of boxes of grid width
         * Returns: a constructed SnakeWindow
         * 
         * The number-of-boxes-in-the-grid variables are mostly unused, but available - the
         * grid may be resized but only from within the source.
         */
        
        //Step 1: Deal with SnakeObjects
        this.game = game;
        listener = new SnakeListener();
        
        //Step 2: Get some more sizing variables ready
        gridX = (BOX_SIZE * gridSizeX) + 2 * GRID_LOC_X; //Total size of the grid: 2 times the offset plus
        gridY = (BOX_SIZE * gridSizeY) + 2 * GRID_LOC_Y; //the grid size times the number of boxes
        
        //Step 3: Some general window settings
        this.setTitle("Snake"); //Window name
        this.setFocusable(true); //Must be true to recieve key information
        this.setLayout(null); //I don't want to use a layout manager please!
        this.setSize(gridX + 160, gridY + 70); //
        this.setResizable(false);
        
        //Step 4: Initialize lots of components
        //Methods I use:
        //setSize() - set the size of the component
        //setLocation() - set the location, since I am using absoluts positioning
        //setFont() - for the ones with text, set it to Serif font at particular size
        //add() - puts the specified component in the window
        
        line = new JSeparator(SwingConstants.VERTICAL);
        line.setSize(5, gridY);
        line.setLocation(gridX, 0);
        this.add(line);
        
        start = new JButton("Start");
        start.setSize(100, 40);
        start.setLocation(gridX + 30, GRID_LOC_Y);
        start.setFont(new Font("Serif", Font.PLAIN, 16));
        this.add(start);
        
        pause = new JButton("Pause");
        pause.setSize(100, 40);
        pause.setLocation(gridX + 30, GRID_LOC_Y + 50);
        pause.setFont(new Font("Serif", Font.PLAIN, 16));
        this.add(pause);
        
        scores = new JButton("High Scores");
        scores.setSize(120, 25);
        scores.setLocation(gridX + 20, GRID_LOC_Y + 290);
        scores.setFont(new Font("Serif", Font.PLAIN, 16));
        scores.addActionListener(listener);
        this.add(scores);
        
        info = new JTextArea("!!!If this is showing up, something is wrong!!!");
        info.setOpaque(false);
        info.setSize(gridX, 50);
        info.setFont(new Font("Serif", Font.PLAIN, 14));
        info.setLocation(GRID_LOC_X, gridY - 40);
        this.add(info);
        
        controls = new JTextArea("→ - Right\n← - Left\n↑ - Up\n↓ - Down\n\nZ - Start/Reset\nX - Pause/Resume\nC - Chaos mode");
        controls.setSize(110, 160);
        controls.setFont(new Font("Serif", Font.PLAIN, 14));
        controls.setLocation(gridX + 30, GRID_LOC_Y + 110);
        controls.setEditable(false);
        controls.setFocusable(false);
        controls.setOpaque(false);
        this.add(controls);
        
        title = new JLabel("Snake");
        title.setSize(gridX, 20);
        title.setFont(new Font("Serif", Font.PLAIN, 20));
        title.setLocation(GRID_LOC_X, 10);
        this.add(title);
        
        //Step 5: Get some listeners going
        //All of the buttons are KeyListeners along with the window itself 
        //because sometimes the buttons keep the focus when they shouldn't.
        this.addWindowListener(listener); //Window events - closing, mainly
        this.addKeyListener(listener); //Key events - keyboard input
        start.addKeyListener(listener);
        pause.addKeyListener(listener);
        scores.addKeyListener(listener);
        start.addActionListener(listener); //Action events - pushing buttons
        pause.addActionListener(listener);
        scores.addActionListener(listener);
        
        
        //Step 6: Deal with the menu. I've done this in a separate method setUpMenu()
        //for the ease of reading.
        setUpMenu();
        
        //Step 7: High scores
        scoresWindow = new JDialog(this);
        
        //Step 7a: Some general window settings
        scoresWindow.setTitle("Snake: High Scores"); //Window name
        scoresWindow.setFocusable(true); //Must be true to recieve key information
        scoresWindow.setLayout(null); //I don't want to use a layout manager please!
        scoresWindow.setLocation(this.getSize().width / 2, this.getSize().height / 4);
        scoresWindow.setSize(300, 300);
        scoresWindow.setResizable(false);
        scoresWindow.addWindowListener(listener);
        
        //Step 7b: Text box
        scoresText = new JTextArea("");
        scoresText.setSize(290, 120);
        scoresText.setFont(new Font("Serif", Font.PLAIN, 14));
        scoresText.setLocation(5, 10);
        scoresText.setEditable(false);
        scoresText.setFocusable(false);
        scoresText.setOpaque(false);
        scoresWindow.add(scoresText);
        
        //Step 7c: Close button
        scoresClose = new JButton("Return to game");
        scoresClose.setSize(220, 20);
        scoresClose.setLocation(40, 240);
        scoresClose.setFont(new Font("Serif", Font.PLAIN, 16));
        scoresClose.addActionListener(listener);
        scoresWindow.add(scoresClose);
        
        //Step 7d: Name entry
        scoresField = new JTextField();
        scoresField.setSize(100, 20);
        scoresField.setLocation(100, 140);
        scoresField.addActionListener(listener);
        scoresField.setVisible(false);
        scoresWindow.add(scoresField);
        
        //Step 8: Deal with the grid. It've also done this in a separate method, createGrid().
        createGrid(gridSizeX, gridSizeY);
        setVisible(true);
    }
    private void setUpMenu() {
        /*
         * Sets up the menu components of the window. This keeps 
         * the construction of the window a bit more organized.
         * Since there are so many menu items, I did consider using an array,
         * but the fact remains that there are enough arbitrary values (i.e. tooltip
         * text, name, etc) that I would rather deal with them one by one.
         * 
         * Parameters: None
         * Returns: Nothing
         */
        
        //6a: Set up menu heirarchy
        menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        
        optionMenu = new JMenu("Option");
        menuBar.add(optionMenu);
        
        itemMenu = new JMenu("Item");
        menuBar.add(itemMenu);
        
        //6b: Initialize the menu options for both menus in the menu bar.
        speedItem = new JCheckBoxMenuItem("Speed item");
        slowItem = new JCheckBoxMenuItem("Slow item");
        growthItem = new JCheckBoxMenuItem("Growth item");
        pointsItem = new JCheckBoxMenuItem("Points item");
        lousyItem = new JCheckBoxMenuItem("Loss item");
        awesomeItem = new JCheckBoxMenuItem("Boost item");
        deathItem = new JCheckBoxMenuItem("Death item");
        chaosMode = new JCheckBoxMenuItem("Chaos mode");
        wormInsteadOfSnake = new JCheckBoxMenuItem("Worm instead of snake");
        
        //6c: Add all of the menu options to the menu they belong to.
        optionMenu.add(chaosMode);
        optionMenu.add(wormInsteadOfSnake);
        itemMenu.add(speedItem);
        itemMenu.add(slowItem);
        itemMenu.add(growthItem);
        itemMenu.add(pointsItem);
        itemMenu.add(lousyItem);
        itemMenu.add(awesomeItem);
        itemMenu.add(deathItem);
        
        //6d: Listen to the menu options
        speedItem.addActionListener(listener);
        slowItem.addActionListener(listener);
        growthItem.addActionListener(listener);
        pointsItem.addActionListener(listener);
        lousyItem.addActionListener(listener);
        awesomeItem.addActionListener(listener);
        deathItem.addActionListener(listener);
        chaosMode.addActionListener(listener);
        wormInsteadOfSnake.addActionListener(listener);
        
        //6e: Set default selection values for the options corresponding
        //to the default boolean values in the SnakeGame
        speedItem.setSelected(true);
        slowItem.setSelected(true);
        growthItem.setSelected(true);
        pointsItem.setSelected(true);
        lousyItem.setSelected(true);
        awesomeItem.setSelected(true);
        deathItem.setSelected(true);
        chaosMode.setSelected(false);
        wormInsteadOfSnake.setSelected(false);
        
        //6f: Set tooltips for each menu option.
        speedItem.setToolTipText("Cyan: Speeds the snake up");
        slowItem.setToolTipText("Magenta: Slows the snake down");
        growthItem.setToolTipText("Green: Adds six segments to the snake");
        pointsItem.setToolTipText("Brown: Worth lots of points");
        lousyItem.setToolTipText("Dark gray: Slows snake down, worth negative points");         
        awesomeItem.setToolTipText("White: Speeds snake up, worth lots of points");
        deathItem.setToolTipText("Red: Kills the snake, chaos mode only");
        chaosMode.setToolTipText("Items appear and disappear randomly");
        wormInsteadOfSnake.setToolTipText("Change the snake to a worm");
    }
    private void createGrid(int sizeX, int sizeY) {
        /*
         * Uses a double loop to create a grid out of boxes.
         * Each box is added individually to the window (they aren't a
         * part of a bigger component or anything).
         * 
         * Parameters:
         * sizeX - number of boxes of grid length, unimplemented in the actual game
         * sizeY - number of boxes of grid width, unimplemented in the actual game
         * Returns: Nothing
         */
        
        grid = new Box[sizeX][sizeY]; //initialize the 2D array
        //Double loop to access every item in the grid
        for(int i = 0; i < sizeX; i++) {
            for(int j = 0; j < sizeY; j++) {
                //For each item in the grid...
                grid[i][j] = new Box(BOX_SIZE, BOX_SIZE); //Initialize it as a square box
                grid[i][j].setBackground(Color.BLUE); //Make it blue by default (debugging)
                grid[i][j].setLocation(i * BOX_SIZE + GRID_LOC_X, j * BOX_SIZE + GRID_LOC_Y); //Set it to a location relative to the rest of the grid
                grid[i][j].setSize(BOX_SIZE, BOX_SIZE); //Make it the right size
                this.add(grid[i][j]); //Add it to the window
            }
        }
    }
    public void setBoxColor(int x, int y, Color color) {
        /*
         * Changes the color of a single box in the grid at (x, y)
         * 
         * Parameters:
         * x - x value of grid location to change
         * y - y value of grid location to change
         * color - the color to change it to
         * Returns: Nothing
         */
        
        grid[x][y].setBackground(color);
        
    }
    public void setInfo(String text) {
        /*
         * Set the text of the label below the grid.
         * 
         * Parameters: text - the text to change it to
         * Returns: Nothing
         */
        info.setText(text);
        
    }
    public void setHighScores(SnakeScore[] scores) {
        String newText = "";
        for(int i = 0; i < scores.length; i++) {
            if(scores[i].playerName == null) newText += (i + 1) + ": ----------\n";
            else newText += (i + 1) + ": " + scores[i].playerName + " [Points: " + scores[i].points + "  Size: " + scores[i].size + ((scores[i].chaos)? "  *chaos" : "") + "]\n";
        }
        scoresText.setText(newText);
    }
    public void getPlayerName() {
        scoresField.setVisible(true);
        scoresField.setFocusable(true);
        scoresWindow.setVisible(true);
        scoresText.setText(scoresText.getText() + "\nHigh score!\nEnter your name:");
        scoresWindow.requestFocus();
        scoresWindow.setModal(true);
        waitingForName = true;
    }
    
    private class SnakeListener implements
    KeyListener, ActionListener, WindowListener {
        /*
         * An internal class that is meant to deal with the actions fired by the components
         * of SnakeWindow. It knows everything that SnakeWindow does, so all it does is implement the
         * listeners (instead of implementing them directly from SnakeWindow, which is a bit more confusing)
         * 
         * Extends: Nothing (not directly, but it is an internal class of SnakeWindow, if that counts)
         * Implements: KeyListener, ActionListener, WindowListener
         */
        
        String keysPressed = "";
        
        public SnakeListener() {
            //Constructor for SnakeListener. SnakeListener has no instance data, so this method is empty.
        }
        
        /*
         * KeyListener methods
         * Meant to deal with keyboard input. Right now the controls are the arrow keys.
         * Only KeyPressed does anything (it calls SnakeGame to deal with the key) because
         * the other methods are pretty much unnecessary.
         * 
         * Parameters: e - the KeyEvent that was fired
         * Returns: Nothing
         */
        public void keyPressed(KeyEvent e) {
            game.dealWithKey(e.getKeyCode());
        }
        public void keyReleased(KeyEvent e) {
        }
        public void keyTyped(KeyEvent e) {
            keysPressed += e.getKeyChar();
            if(keysPressed.contains("4242424242")) {
                game.doEasterEgg(1);
                keysPressed = "";
            }
        }
        
        /*
         * ActionListener methods
         * Depending on the origin of the ActionEvent, this single method (actionPerformed) deals
         * with it in defferent ways, usually involving changing the state variables.
         * 
         * Parameters: e - the ActionEvent that was fired
         * Returns: Nothing
         */
        public void actionPerformed(ActionEvent e) {
            
            //Deal with start button
            //Start button overrides pause - pressing it always cuases the game to unpause
            if(e.getSource() == start) {
                if(gameHasStarted) {
                    //If the button was on "Reset" game (button puched midgame)
                    // the game is in state unstarted and unpaused
                    gameHasStarted = false;
                    start.setText("Start");
                    paused = false;
                    pause.setText("Pause");
                } else {
                    //Assert: The button was on "Start" (button pushed pregame)
                    //Game is in state started and unpaused
                    gameHasStarted = true;
                    start.setText("Reset");
                    paused = false;
                    pause.setText("Pause");
                }
            }
            
            //Deal with pause button
            if(e.getSource() == pause) {
                if(paused){
                    //If the game is already pause, then unpause
                    paused = false;
                    pause.setText("Pause");
                } else {
                    //Assert: Game is not in pause (so we should pause it)
                    paused = true;
                    pause.setText("Resume");
                }
            }
            
            //Deal with high scores button
            if(e.getSource() == scores) {
                scoresWindow.setVisible(true);
                //Pause the game just in case
                paused = true;
                pause.setText("Resume");
            }
            
            //Deal with high scores close button, doesn't work when waiting for a name
            if(e.getSource() == scoresClose) {
                if(waitingForName) {
                    scoresWindow.setVisible(true);
                    scoresClose.setText("Please enter a name!");
                } else {
                    scoresClose.setText("Return to game");
                    scoresWindow.setVisible(false);
                }
            }
            
            //Deal with scores name input
            if(e.getSource() == scoresField) {
                if(scoresField.getText().length() <= 10) {
                    game.currentScore.playerName = scoresField.getText();
                } else game.currentScore.playerName = scoresField.getText().substring(0, 9);
                waitingForName = false;
                scoresField.setVisible(false);
                scoresWindow.setModal(false);
            }
            
            //Deal with menu options
            //These variables are booleans that are held within the SnakeGame
            //They can be marked on or off depending on input
            if(e.getSource() == speedItem) game.speedItemEnabled = speedItem.isSelected();
            if(e.getSource() == slowItem) game.slowItemEnabled = slowItem.isSelected();
            if(e.getSource() == pointsItem) game.pointsItemEnabled = pointsItem.isSelected();
            if(e.getSource() == growthItem) game.growthItemEnabled = growthItem.isSelected();
            if(e.getSource() == lousyItem) game.lousyItemEnabled = lousyItem.isSelected();
            if(e.getSource() == awesomeItem) game.awesomeItemEnabled = awesomeItem.isSelected();
            if(e.getSource() == deathItem) game.deathItemEnabled = deathItem.isSelected();
            if(e.getSource() == chaosMode) game.chaosMode = chaosMode.isSelected();
            if(e.getSource() == wormInsteadOfSnake) game.wormInsteadOfSnake = wormInsteadOfSnake.isSelected();
        }
        
        /*
         * WindowListener methods
         * If the game is closed, then end the program
         * If the game is minimized, pause it
         * 
         * Parameters: e - the WindowEvent that was fired
         * Returns: Nothing
         */
        public void windowDeactivated(WindowEvent e){
        }
        public void windowActivated(WindowEvent e){
        }
        public void windowDeiconified(WindowEvent e){
        }
        public void windowIconified(WindowEvent e){
            paused = true;
            pause.setText("Resume");
        }
        public void windowClosed(WindowEvent e){
        }
        public void windowOpened(WindowEvent e){
        }
        public void windowClosing(WindowEvent e){
            if(e.getSource() == scoresWindow) {
                game.currentScore.playerName = scoresField.getText();
                waitingForName = false;
                scoresField.setVisible(false);
                scoresWindow.setModal(false);
            } else {
                gameHasStarted = false;
                game.saveHighScores();
                System.exit(0);
            }
        }
    }
}