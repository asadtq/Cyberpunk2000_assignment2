import java.awt.Point;
import java.util.Random;

public class FollowTrailStrategy implements MovementStrategy {

    private final Random random = new Random();

    @Override 
    public void move(Worker worker) {
        Point position = worker.getPosition();
        worker.detectPheromones(position.x, position.y);

        int colonyX = Main.WIDTH/2;
        int colonyY = Main.HEIGHT/2;
        double currentDist = position.distance(colonyX, colonyY);

        Pheromone<Float> strongest = null;
        for (Pheromone<Float> p : worker.trail) {
            if(p.getPosition().equals(position)) continue;
            if(p.getPosition().distance(colonyX, colonyY) <= currentDist) continue;
            if (strongest == null || p.getStrength() > strongest.getStrength()) { 
                strongest = p;
            }
        }

        int newX;
        int newY;
            if (strongest!= null) {
                Point target = strongest.getPosition();
                newX = position.x + Integer.compare(target.x, position.x);
                newY = position.y + Integer.compare(target.y , position.y );

            }else {
                newX = position.x+random.nextInt(3) -1;
                newY = position.y + random.nextInt(3) -1;
            }

            if(newX<0 || newX >=Main.WIDTH || newY < 0|| newY >=Main.HEIGHT) return;

            worker.move(new Point(newX, newY));
            System.out.println("Worker moving to: " + newX + ", "+ newY);
            checkForFood(worker, newX, newY);
        
    }

    private void checkForFood(Worker worker , int x, int y) {
        if (Main.grid[x][y] == Main.CellType.FOOD) {
            Main.grid[x][y] = Main.CellType.EMPTY;
            Main.pheromoneGrid[x][y] = null;
            worker.pickUpFood(1);
            System.out.println("Worker picked up food! carryingFood = "+ worker.isCarryingFood());
        }
        
    }

    
}
