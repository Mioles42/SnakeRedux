/*
 * Miles Krusniak
 * SnakeMain.java (Snake)
 * 
 * SnakeMain is the entry point of the program
 * and serves only as a driver for SnakeGame, which
 * does everything.
 */

public class SnakeMain {
    public static void main(String[] args) {
        //Initialize and run a new game of Snake.
        SnakeGame game = new SnakeGame();
        game.run();
    }
}