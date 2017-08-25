/*
 * Miles Krusniak
 * SnakeGame.java (Snake)
 * 
 * SnakeGame keeps track of everything that happens within the
 * actual game - the movement of the snake, placement of items, etc.
 * 
 * Item - an object on the board, may it be the wall, part of the snake,
 * food for the snake, or empty space.
 * Pickup [item] - an object that may be "consumed" by the snake, producing
 * positive or negative effects.
 * Chaos mode - a style of Snake where pickup items randomly appear and disappear, as
 * opposed to the way in which one pickup would appear only after the one before it
 * was consumed by the snake. In my program, chaos mode may be toggled.
 */

import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;
import java.io.*;

public class SnakeGame {
    /*
     * Representation of the  Snake game.
     * Extends: Nothing
     * Implements: Nothing
     */
    
    //SnakeObject variables
    SnakeWindow window;
    SnakeObject snake;
    
    //Board and items. The board is the actual representation of the playing space
    //(as opposed to the grid, which is the visual representation) and it contains
    //arbitrary ints corresponding to items. All of the following constants refer to
    //the arbitrary values given to the items.
    private int[][] board;
    final private static int WALL_ITEM = 1;
    final private static int NO_ITEM = 0;
    final private static int SNAKE_ITEM = -1;
    final private static int SNAKEHEAD_ITEM = -2;
    //Pickup items
    final private static int FOOD_ITEM = 2;
    final private static int SPEED_ITEM = 3;
    final private static int SLOW_ITEM = 4;
    final private static int POINTS_ITEM = 5;
    final private static int GROWTH_ITEM = 6;
    final private static int LOUSY_ITEM = 7;
    final private static int AWESOME_ITEM = 8;
    final private static int DEATH_ITEM = 9;
    
    
    //Following are (mostly) booleans used for options. They aren't private because
    //SnakeWindow uses them (in the menu bar)
    //Option booleans and an int used in chaos mode.
    boolean wormInsteadOfSnake = false; //Makes the snake brown
    boolean chaosMode = false; //Turns on chaos mode
    private int pickupsOnBoard = 0;
    
    //Item-enable booleans. These correspond with the JCheckBoxMenuItems in the SnakeWindow
    //and as such they may be toggled. All are enabled by default.
    boolean speedItemEnabled = true;
    boolean slowItemEnabled = true;
    boolean pointsItemEnabled = true;
    boolean growthItemEnabled = true;
    boolean lousyItemEnabled = true;
    boolean awesomeItemEnabled = true;
    boolean deathItemEnabled = true;
    
    //Statistic variables with mostly arbitrary start values
    int speed = 200;
    int points = 0;
    
    //Timing variables
    long oldTime = System.currentTimeMillis();
    long newTime;
    
    //High score feature
    static SnakeScore[] highScores;
    SnakeScore currentScore = new SnakeScore(0);
    
    //A constant for grid size. If you change it, you will wind up with a completely working grid of that size.
    final private int SIZE = 30;
    
    public SnakeGame() {
        /*
         * Constructor for SnakeGames. Pretty simple, because run() is what does all the work.
         * 
         * Parameters: None
         * Returns: Constructed SnakeGame
         */
        window = new SnakeWindow(this, SIZE, SIZE);
        board = new int[SIZE][SIZE];
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(new File("com\\miolean\\snake\\scores.mf")));
            highScores = (SnakeScore[]) stream.readObject();
        }
        catch(IOException e) {
            highScores = new SnakeScore[3];
            for(int i = 0; i < highScores.length; i++) highScores[i] = new SnakeScore(0);
        }
        catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * 
     * run() method (category by itself)
     * 
     */
    public void run() {
        /*
         * run() does the brunt of the work in the program. It has quite a few variables by itself.
         * It deals with the actual gametime, controlling the other methods in this class.
         * Why run()? I would usually call it playGame() but I was experimenting with Threads. Now, SnakeGame
         * could implement Runnable if I wanted it to.
         * 
         * Parameters: None
         * Returns: Nothing
         */
        
        //Item variables
        int itemAtHead;
        final int MAX_PICKUPS = 25;
        
        window.setHighScores(highScores);
        
        //The following loop will exit only when the JVM does
        while(true) {
            //Reset all of the variables, including the snake, and wait for the player to start
            window.setInfo("Press start to begin.");
            snake = new SnakeObject();
            currentScore = new SnakeScore(0);
            resetBoard();
            applyBoard(false);
            points = 0;
            speed = 200;
            board[5][5] = FOOD_ITEM; //This is always the first pickup
            pickupsOnBoard = 0;
            
            
            //The following loop is in place as long as the game is in "Started" state
            //This means that the start button has been pressed
            while(window.gameHasStarted) {
                //Keep up with the time and display.
                if(chaosMode) window.setInfo("Movement speed: " + (500 - speed) + "        " + points + " points" + "         Size: " +  snake.getSize() + "\nHigh score: "
                    + highScores[0].playerName + " [" + highScores[0].points + " points]  |  Chaos Mode");
                else window.setInfo("Movement speed: " + (500 - speed) + "        " + points + " points" + "         Size: " +  snake.getSize() + "\nHigh score: "
                    + highScores[0].playerName + " [" + highScores[0].points + " points]");
                newTime = System.currentTimeMillis();
                
                //The following loop is in place while the game is paused.
                if(window.paused) {
                    window.setInfo("Game is paused. Press 'resume' to resume.");
                    while(window.paused) {
                        oldTime = newTime; //Bide time on pause
                        newTime = System.currentTimeMillis();
                    }
                }
                
                //If it's time to move... (one tick has passed)
                if(newTime - oldTime > speed) {
                    //Update the time, move the snake, and check the item that the snake just ran over.
                    //These are things we must do every tick.
                    oldTime = newTime;
                    snake.move();
                    itemAtHead = checkItemAtHead();
                    
                    //If the snake ran into something undesirable (the wall, the death pickup, or itself)
                    if(itemAtHead == WALL_ITEM || itemAtHead == SNAKE_ITEM || itemAtHead == DEATH_ITEM) {
                        
                        
                        window.setInfo("You died! Press 'reset'.        Score: " + points + "  Size: " + snake.getSize());
                        currentScore.points = points;
                        currentScore.speed = 500 - speed;
                        currentScore.size = snake.getSize();
                        currentScore.chaos = chaosMode;
                        
                        if(changeHighScores() != 0) {
                            currentScore.playerName = "*YOU*";
                            window.setHighScores(highScores);
                            
                            window.getPlayerName();
                            while(window.waitingForName) applyBoard(true);
                            if(currentScore.playerName.equals("")) currentScore.playerName = "Nobody";
                        }
                        window.scoresField.setText("");
                        window.setHighScores(highScores);
                        
                        while(window.gameHasStarted) {
                            //No choice but to press reset. Keep track of changes in the board (ie snake to worm)
                            applyBoard(true);
                        }
                    } else if(itemAtHead != NO_ITEM) {
                        //assert: itemAtHead is a pickup item that will not kill the snake.
                        //It can't be the head, since it can't be exactly where it was before,
                        //and it can't be anything else, we've already filtered them out.
                        
                        //Do something depending on the type of pickup.
                        //These values are mostly arbitrary based on what impact I want
                        //each pickup to have.
                        //We did just consume one pickup, so decrement the counter.
                        pickupsOnBoard--;
                        if(itemAtHead == FOOD_ITEM) {
                            points += 4;
                            speed -= 10;
                            snake.extend(3);
                        }
                        if(itemAtHead == SPEED_ITEM) {
                            points += 5;
                            speed -= 40;
                            snake.extend(2);
                        }
                        if(itemAtHead == SLOW_ITEM) {
                            points += 5;
                            speed += 40;
                            snake.extend(2);
                        }
                        if(itemAtHead == GROWTH_ITEM) {
                            points += 7;
                            speed -= 10;
                            snake.extend(7);
                        }
                        if(itemAtHead == POINTS_ITEM) {
                            points += 20;
                            speed -= 5;
                            snake.extend(2);
                        }
                        if(itemAtHead == LOUSY_ITEM) {
                            points -= 15;
                            speed += 40;
                            snake.extend(1);
                        }
                        if(itemAtHead == AWESOME_ITEM) {
                            points += 20;
                            speed -= 30;
                            snake.extend(4);
                        }
                        
                        //Two ifs: Add a pickup (assuming we haven't reached the max, which occurs in chaos mode) and if the speed is too fast/slow, reset it to the max/min.
                        if(! (chaosMode && pickupsOnBoard >= MAX_PICKUPS)) addPickup(false);
                        if(speed < 50) speed = 50; //set minimum
                        if(speed > 499) speed = 499; //set maximum
                        
                    } else if(chaosMode && pickupsOnBoard < MAX_PICKUPS) {
                        //If the game is in chaos mode, another item may show up or decay regardless of whether we consume them or not
                        //as long as we haven't reached the max.. About 1/20 chance.
                        addPickup(true);
                        removePickup();
                        applyBoard(false);
                    } else applyBoard(false); //Something we must do anyway
                }
            }
        }
    }
    
    /*
     * 
     * Board operations to deal with it and the grid.
     * 
     */
    public void updateBoard() {
        /*
         * Apply the snake to the board (the snake has probably moved)
         * 
         * Parameters: None
         * Returns: Nothing
         */
        
        //Uses a algorithm just like resetBoard(), but preserving the pickups. It serves to remove
        //the snake items from the board before we reapply it.
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[i].length; j++) {
                if(i == 0 || i == board.length - 1 || j == 0 || j == board[i].length - 1)  board[i][j] = WALL_ITEM;
                else if(board[i][j] >= FOOD_ITEM) { //All pickup items are > FOOD_ITEM, so we can use that
                } else board[i][j] = NO_ITEM;
            }
        }
        
        //Apply snake to board. The snake is a collection of points, so we can take each of these and
        //apply a snake item to the corresponding point on the board.
        Point[] snakeLocation = snake.getLocation();
        for(int i = 0; i < snakeLocation.length && snakeLocation[i] != null; i++) {
            board[snakeLocation[i].x - 1][snakeLocation[i].y - 1] = SNAKE_ITEM;
        }
        //Make the front one a snakehead item
        board[snakeLocation[0].x - 1][snakeLocation[0].y - 1] = SNAKEHEAD_ITEM;
    }
    public void applyBoard(boolean dead) {
        /*
         * Uses a double loop to traverse the entire board, setting the colors of each corresponding box
         * in the grid as we go.
         * 
         * Parameters: dead - whether the snake should show as red (because it is dead).
         * Returns: Nothing
         */
        updateBoard(); //Update board before showing it
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[i].length; j++) {
                //General items
                if(board[i][j] == NO_ITEM) window.setBoxColor(i, j, Color.BLUE);
                if(board[i][j] == WALL_ITEM) window.setBoxColor(i, j, Color.BLACK);
                //Snake (or worm) items
                if(board[i][j] == SNAKEHEAD_ITEM && dead) window.setBoxColor(i, j, new Color(200, 0, 0));
                else if(board[i][j] == SNAKEHEAD_ITEM && wormInsteadOfSnake) window.setBoxColor(i, j, new Color(50, 50, 0));
                else if(board[i][j] == SNAKEHEAD_ITEM) window.setBoxColor(i, j, new Color(0, 180, 0));
                if(board[i][j] == SNAKE_ITEM && dead) window.setBoxColor(i, j, Color.RED);
                else if(board[i][j] == SNAKE_ITEM && wormInsteadOfSnake) window.setBoxColor(i, j, new Color(100, 100, 50));
                else if(board[i][j] == SNAKE_ITEM) window.setBoxColor(i, j, Color.GREEN);
                //Pickup items
                if(board[i][j] == FOOD_ITEM) window.setBoxColor(i, j, Color.ORANGE);
                if(board[i][j] == SPEED_ITEM) window.setBoxColor(i, j, Color.CYAN);
                if(board[i][j] == SLOW_ITEM) window.setBoxColor(i, j, Color.MAGENTA);
                if(board[i][j] == GROWTH_ITEM) window.setBoxColor(i, j, new Color(50, 255, 100));
                if(board[i][j] == POINTS_ITEM) window.setBoxColor(i, j, new Color(200, 120, 50));
                if(board[i][j] == LOUSY_ITEM) window.setBoxColor(i, j, Color.DARK_GRAY);
                if(board[i][j] == AWESOME_ITEM) window.setBoxColor(i, j, Color.WHITE);
                if(board[i][j] == DEATH_ITEM) window.setBoxColor(i, j, Color.RED);
            }
        }
    }
    public void resetBoard() {
        /*
         * Removes the snake and pickup items from the board - a blank board.
         */
        
        //Double loop - touch each item in the board
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[i].length; j++) {
                //If the item is at the edge of the board, then it should be a wall; otherwise, it should be blank
                if(i == 0 || i == board.length - 1 || j == 0 || j == board[i].length - 1) {
                    board[i][j] = WALL_ITEM;
                } else board[i][j] = NO_ITEM;
            }
        }
    }
    
    /*
     * 
     * Keep-up methods to be used mid-game.
     * 
     */
    public int checkItemAtHead() {
        /*
         * After the snake has been moved, we'll call this to check what item is at
         * the front point of the snake - the item that the snake just ran into. (it doesn't
         * work if we call it after updating the board - we must do this right after moving the
         * snake, otherwise the head will overwrite the item in front of it)
         * 
         * Parameters: None
         * Returns: The item that the snake is about to run into
         */
        return board[snake.getLocation()[0].x - 1][snake.getLocation()[0].y - 1];
        
    }
    public void addPickup(boolean mayFail) {
        /*
         * addPickup() adds a pickup item to a random spot on the board that isn't already occupied.
         * The single parameter is used in chaos mode, because in that mode, new pickups may show up randomly,
         * and in this case we definitely don't want it to always work.
         * 
         * Parameters: mayFail - whether the method should have only a chance of success.
         * Returns: Nothing
         */
        
        //Variables
        Random generator = new Random();
        int newX, newY, randomItem; 
        //newX and newY will be the coordinates of the new pickup
        //randomItem will be a random number that will correspond to a pickup item.
        
        //Choose a location for a new item that isn't occupied
        newX = generator.nextInt(SIZE);
        newY = generator.nextInt(SIZE);
        while(board[newX][newY] != NO_ITEM) {
            //Keep trying if we didn't get an unoccupied space
            newX = generator.nextInt(SIZE);
            newY = generator.nextInt(SIZE);
        }
        
        //Choose a random number. If we can fail, let it be out of the range of acceptable numbers.
        if(! mayFail)  randomItem = generator.nextInt(100) + 1;
        else  randomItem = generator.nextInt(2000) + 1;
        
        //Based on that number, choose a pickup to place
        if(chaosMode) {
            if(randomItem >= 1 && randomItem < 40) board[newX][newY] = FOOD_ITEM;
            else if(randomItem >= 40 && randomItem < 50 && deathItemEnabled) board[newX][newY] = DEATH_ITEM;
            else if(randomItem >= 50 && randomItem < 60 && speedItemEnabled) board[newX][newY] = SPEED_ITEM;
            else if(randomItem >= 60 && randomItem < 70 && slowItemEnabled) board[newX][newY] = SLOW_ITEM;
            else if(randomItem >= 70 && randomItem < 80 && growthItemEnabled) board[newX][newY] = GROWTH_ITEM;
            else if(randomItem >= 80 && randomItem < 90 && pointsItemEnabled) board[newX][newY] = POINTS_ITEM;
            else if(randomItem >= 90 && randomItem < 95 && lousyItemEnabled) board[newX][newY] = LOUSY_ITEM;
            else if(randomItem >= 95 && randomItem <= 100 && awesomeItemEnabled) board[newX][newY] = AWESOME_ITEM;
            else if(randomItem > 100) board[newX][newY] = NO_ITEM; //chaos: if the number is not in the realm of possibility, failure has occured
            else board[newX][newY] = FOOD_ITEM; //if a disabled pickup is chosen, this is the default
        } else {
            if(randomItem >= 1 && randomItem < 50) board[newX][newY] = FOOD_ITEM;
            else if(randomItem >= 50 && randomItem < 60 && speedItemEnabled) board[newX][newY] = SPEED_ITEM;
            else if(randomItem >= 60 && randomItem < 70 && slowItemEnabled) board[newX][newY] = SLOW_ITEM;
            else if(randomItem >= 70 && randomItem < 80 && growthItemEnabled) board[newX][newY] = GROWTH_ITEM;
            else if(randomItem >= 80 && randomItem < 90 && pointsItemEnabled) board[newX][newY] = POINTS_ITEM;
            else if(randomItem >= 90 && randomItem < 95 && lousyItemEnabled) board[newX][newY] = LOUSY_ITEM;
            else if(randomItem >= 95 && randomItem <= 100 && awesomeItemEnabled) board[newX][newY] = AWESOME_ITEM;
            else board[newX][newY] = FOOD_ITEM; //if a disabled pickup is chosen, this is the default
        }
        
        //Assuming we didn't fail, increment the counter of items and update the board
        if(board[newX][newY] != NO_ITEM) pickupsOnBoard++;
        applyBoard(false);
    }
    public void removePickup() {
        /*
         * Has a random chance of removing a pickup from the board. Unlike addPickup(), we don't
         * need to choose whether it fails, because it is only used in the chaos mode case where it may fail.
         * (it's used only once, and in that context we do not want it to always succeed)
         * 
         * Parameters: None
         * Returns: Nothing
         */
        int destroyX, destroyY;
        Random generator = new Random();
        //destroyX and destroyY will be the coordinates of the pickup to destroy
        
        //If we don't have enough pickups on the board to spare, then skip it
        if(pickupsOnBoard <= 1) return;
        
        //Otherwise, given a 1/20 chance...
        if(generator.nextInt(20) == 10) {
            //Find a pickup to destroy
            destroyX = generator.nextInt(SIZE);
            destroyY = generator.nextInt(SIZE);
            while(!(board[destroyX][destroyY] >= FOOD_ITEM)) {
                //If we didn't hit a pickup, keep trying
                destroyX = generator.nextInt(SIZE);
                destroyY = generator.nextInt(SIZE);
            }
            //Destroy it, decrement the pickup counter
            board[destroyX][destroyY] = NO_ITEM;
            pickupsOnBoard--;
        }
        //Redraw the board
        applyBoard(false);
    }
    public void dealWithKey(int keyCode) {
        /*
         * Interrupts the normal flow of execution because a button has been pressed.
         * This method decides whether the key pressed was one that does something, and if so, does it.
         * 
         * Parameters: keyCode - the code passed to the method that corresponds to a key
         * Returns: Nothing
         */
        
        //Arrow keys
        if(keyCode == KeyEvent.VK_UP && snake.getDirection() != snake.DOWN) snake.changeDirection(snake.UP);
        if(keyCode == KeyEvent.VK_DOWN && snake.getDirection() != snake.UP) snake.changeDirection(snake.DOWN);
        if(keyCode == KeyEvent.VK_LEFT && snake.getDirection() != snake.RIGHT) snake.changeDirection(snake.LEFT);
        if(keyCode == KeyEvent.VK_RIGHT && snake.getDirection() != snake.LEFT) snake.changeDirection(snake.RIGHT);
        
        //Shortcut keys
        if(keyCode == KeyEvent.VK_Z) {
            if(window.gameHasStarted) {
                // the game is in state unstarted and unpaused
                window.gameHasStarted = false;
                window.start.setText("Start");
                window.paused = false;
                window.pause.setText("Pause");
            } else {
                //Game is in state started and unpaused
                window.gameHasStarted = true;
                window.start.setText("Reset");
                window.paused = false;
                window.pause.setText("Pause");
            }
        }
        if(keyCode == KeyEvent.VK_X) {
            if(window.paused){
                //If the game is already paused, then unpause
                window.paused = false;
                window.pause.setText("Pause");
            } else {
                //Assert: Game is not in pause (so we should pause it)
                window.paused = true;
                window.pause.setText("Resume");
            }
        }
        if(keyCode == KeyEvent.VK_C) {
            if(chaosMode) chaosMode = false;
            else chaosMode = true;
            window.chaosMode.setSelected(chaosMode);
        }
    }
    private int changeHighScores() {
        int i;
        for(i = highScores.length - 1; i >= 0 && highScores[i].points < currentScore.points; i--) {
        }
        i++;
        if(i == highScores.length) return 0;
        
        //assert: i will be the score one below ours if it exists (we will take its place)
        for(int j = highScores.length - 1; j > i; j--) {
            highScores[j] = highScores[j - 1];
        }
        highScores[i] = currentScore;
        return i + 1; //Place number in human counting
    }
    public void saveHighScores() {
        ObjectOutputStream stream;
        try {
            stream = new ObjectOutputStream(new FileOutputStream(new File("com\\miolean\\snake\\scores.mf")));
            stream.writeObject(highScores);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void doEasterEgg(int num) {
        if(num == 1 && window.paused) {
            points += 42;
            window.setInfo("Programmed by Miles Krusniak");
        }
    }
}