import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Spider hunts the ants. It extends Ant like Scout and Worker do.
// When it kills an ant it leaves danger pheromone that the other ants avoid.
class Spider extends Ant {
    static final int SIGHT = 3;          // how many cells away she can see an ant
    static final int DIGEST_MOVES = 15;  // after eating she wanders for this many moves
    static final int SAFE_ZONE = 3;      // spider cant go this close to colony

    private Random random;
    private int digestTimer;

    public Spider(int health, int stamina ) {
        super(health, stamina );
        this.random = new Random();
        this.position = new Point(2, 2 ); // far corner away from colony
        this.digestTimer = 0;
    }

    // called every tick from Main.update
    // she only moves when moveNow is true, but she can bite every tick
    public void act(List<Scout> scouts, List<Worker> workers, boolean moveNow ) {
        if (moveNow ) {
            if (digestTimer > 0 ) {
                digestTimer--;
                wander();
            } else {
                huntStep(scouts, workers );
            }
        }

        // only bites if she isnt full
        if (digestTimer == 0 ) {
            bite(scouts, workers );
        }
    }

    // put scouts and workers in one list
    private List<Ant> getAllAnts(List<Scout> scouts, List<Worker> workers ) {
        List<Ant> allAnts = new ArrayList<>();
        allAnts.addAll(scouts );
        allAnts.addAll(workers );
        return allAnts;
    }

    // walk towards the closest ant it sees
    private void huntStep(List<Scout> scouts, List<Worker> workers ) {
        Ant closest = null;
        double closestDistance = 999;

        for (Ant ant : getAllAnts(scouts, workers ) ) {
            if (!ant.isAlive( ) ) {
                continue;
            }
            double distance = ant.getPosition().distance(position );
            if (distance <= SIGHT && distance < closestDistance ) {
                closest = ant;
                closestDistance = distance;
            }
        }

        if (closest == null ) {
            wander();
        } else {
            Point target = closest.getPosition();
            int dx = Integer.compare(target.x, position.x );
            int dy = Integer.compare(target.y, position.y );
            tryMove(dx, dy );
        }
    }

    // eat the first ant standing on the same cell as the spider
    private void bite(List<Scout> scouts, List<Worker> workers ) {
        for (Ant ant : getAllAnts(scouts, workers ) ) {
            if (ant.isAlive() && ant.getPosition().equals(position ) ) {
                ant.takeDamage(ant.getHealth( ) ); // kill ant
                markDanger(position.x, position.y );
                digestTimer = DIGEST_MOVES;
                break; // only eat one at a time
            }
        }
    }

    private void wander() {
        int dx = random.nextInt(3 ) - 1;
        int dy = random.nextInt(3 ) - 1;
        tryMove(dx, dy );
    }

    private void tryMove(int dx, int dy ) {
        int newX = position.x + dx;
        int newY = position.y + dy;

        // stay on the map
        if (newX < 0 || newX >= Main.WIDTH || newY < 0 || newY >= Main.HEIGHT ) return;

        // stay out of the colony so she cant camp where ants spawn
        int distFromColonyX = Math.abs(newX - Main.WIDTH / 2 );
        int distFromColonyY = Math.abs(newY - Main.HEIGHT / 2 );
        if (distFromColonyX <= SAFE_ZONE && distFromColonyY <= SAFE_ZONE ) return;

        position.setLocation(newX, newY );
    }

    // 3x3 square of danger, strongest in middle
    private void markDanger(int centerX, int centerY ) {
        for (int dx = -1; dx <= 1; dx++ ) {
            for (int dy = -1; dy <= 1; dy++ ) {
                int x = centerX + dx;
                int y = centerY + dy;

                if (x < 0 || x >= Main.WIDTH || y < 0 || y >= Main.HEIGHT ) continue;

                float strength = 0.6f;
                if (dx == 0 && dy == 0 ) {
                    strength = 1.0f;
                }
                Main.dangerGrid[x ][y ] = new Pheromone<>(new Point(x, y ), strength );
            }
        }
    }
}