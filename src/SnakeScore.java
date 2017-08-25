import java.io.*;
public class SnakeScore
implements Serializable {
    
    int points;
    int size;
    int speed;
    boolean chaos;
    String playerName;
    
    public SnakeScore(int points) {
        this.points = points;
        this.playerName = "Nobody";
    }
}