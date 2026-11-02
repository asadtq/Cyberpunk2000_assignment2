import java.awt.Point;

public class ReturnToColonyStrategy implements MovementStrategy {

    private static final float REINFORCE_AMOUNT = 0.2f;

    @Override

    public void move(Worker worker) {
        Point position = worker.getPosition();
        int colonyX = Main.WIDTH/2;
        int colonyY = Main.HEIGHT /2 ;
        int newX = position.x + Integer.compare(colonyX , position.x);
        int newY = position.y + Integer.compare(colonyY, position.y);
        worker.move(new Point(newX, newY));

        if (Main.pheromoneGrid[newX][newY] != null) {
            Main.pheromoneGrid[newX][newY].reinforce(REINFORCE_AMOUNT);
                System.out.println("Reinforced trail at " + newX + ", "+ newY);
        }
                if (newX == colonyX && newY == colonyY) {
                 worker.deliverFood();
                }
        }
    }




