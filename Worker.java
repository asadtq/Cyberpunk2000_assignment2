import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Worker extends Ant{
    private Random random;
    /*1. Worker needs to detect pheromones nearby
    2. Follow the strongest pheromone trail
    3. Pick up food when it reaches the source
    4. Carry food back to colony  */
    boolean followingTrail;
    boolean carryingFood;
    int foodCarried;

    // true when this worker saw the spider kill an ant and is running home to tell the queen
    boolean reportingDeath = false;

    int lastX = -1;
    int lastY = -1;

    // running away from danger (the spider's 3x3 patch)
    boolean fleeing = false;
    int fleeTimer = 0;
    int fleeDx = 0;
    int fleeDy = 0;
    static final int FLEE_TICKS = 20; // workers move every tick and a tick is 100ms, so 20 = 2 seconds
    
    //pheremone trail lists
    List<Pheromone<Float>> trail = new ArrayList<>();
    

    public Worker (int health, int stamina) {
       super(health,stamina);
       this.random = new Random();
       this.position = new Point(10, 10); // Spawns the worker at the center colony
       this.followingTrail = false;
       this.carryingFood = false;
       this.foodCarried = 0;
    }

    public void setFollowingTrail(boolean followingTrail) {
        this.followingTrail = followingTrail;
    }
    public boolean isFollowingTrail() { return followingTrail;}
    public void setReportingDeath(boolean reportingDeath) { this.reportingDeath = reportingDeath; }
    public boolean isReportingDeath() { return reportingDeath; }
    public void setCarryingFood(boolean carryingFood) {
        this.carryingFood = carryingFood;
    }
    public boolean isCarryingFood () { return carryingFood;} 
    public int getFoodCarried() { return foodCarried; }
    public void pickUpFood(int amount) {
        this.foodCarried = amount;
        this.carryingFood = true;
    }
    public void followTrail() { 
        int cx = position.x;
        int cy = position.y;

        int nextX = cx;
        int nextY = cy;
        float best = 999.0f; // look for lowest

        // 8 directions, scouts walk diagonally so the trail has diagonal steps in it
        int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}, {1,1}, {1,-1}, {-1,1}, {-1,-1}};
        
        for (int i = 0; i < dirs.length; i++) {
            try {
                int checkX = cx + dirs[i][0];
                int checkY = cy + dirs[i][1];
                //if this tile isnt the same as the last one we stood on. 
                if (checkX == lastX && checkY == lastY) {
                    continue; 
                }
                
                if (Main.pheromoneGrid[checkX][checkY] != null) {
                    //generic type requirement
                    float str = ((Number) Main.pheromoneGrid[checkX][checkY].getStrength()).floatValue();
                    if (str < best && str > 0.01f) {
                        best = str;
                        nextX = checkX;
                        nextY = checkY;
                    }
                }
            } 
            catch (ArrayIndexOutOfBoundsException e) {
                // try/catch requirement
                //System.out.println("worker at edge of map");
            }
        }
        
        // anti-stuck code
        if (nextX == cx && nextY == cy) {
            wander(20,20);
            // wander already moved us, dont overwrite it with the old position below
            this.lastX = cx;
            this.lastY = cy;
            return;
        }
        
        // last moven spot
        this.lastX = cx;
        this.lastY = cy;
        //set
        this.position.setLocation(nextX, nextY);
    }

  public void wander(int gridWidth, int gridHeight) {
        // Pick a random direction (-1, 0, or 1) for x and y
        int dx = random.nextInt(3) - 1;
        int dy = random.nextInt(3) - 1;

        int newX = position.x + dx;
        int newY = position.y + dy;

        //Boundary check for wander 
        if (newX < 0 || newX >= gridWidth || newY < 0 || newY >= gridHeight) return;
        
        // new pos
        this.position.setLocation(newX, newY);
  }
       

public boolean isFleeing() { return fleeing; }

    // called after the worker moves. oldX and oldY are where it was before the move
    public void checkForDanger(int oldX, int oldY) {
        if (fleeing) return; // already running, dont restart the timer

        if (Main.dangerGrid[position.x][position.y] != null) {
            fleeing = true;
            fleeTimer = FLEE_TICKS;

            // opposite direction = back the way it came from
            fleeDx = oldX - position.x;
            fleeDy = oldY - position.y;

            // if it didnt actually move, just pick a random way to run
            if (fleeDx == 0 && fleeDy == 0) {
                fleeDx = random.nextInt(3) - 1;
                fleeDy = random.nextInt(3) - 1;
            }
        }
    }

    // run in a straight line in the opposite direction away from the danger until the timer runs out
    public void flee() {
        int newX = position.x + (fleeDx *-1);
        int newY = position.y + (fleeDy *-1);

        // only move if its still on the map (otherwise it just stops at the edge)
        if (newX >= 0 && newX < Main.WIDTH && newY >= 0 && newY < Main.HEIGHT) {
            this.position.setLocation(newX, newY);
        }

        fleeTimer--;
        if (fleeTimer <= 0) {
            fleeing = false;
            // forget the old trail position so it can pick the trail up again
            lastX = -1;
            lastY = -1;
        }
    }

public void dropFood() throws Exception {
        if (this.carryingFood == false) {
            throw new Exception("ant doesnt have food to drop");
        } else {
            this.carryingFood = false;
            this.foodCarried = 0;
            
            // Queen +50 stamina
            Main.queen.setStamina(Main.queen.getStamina() + 50); 
        }
    }
    
}