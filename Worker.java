import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



class Worker extends Ant{
    /*1. Worker needs to detect pheromones nearby
    2. Follow the strongest pheromone trail
    3. Pick up food when it reaches the source
    4. Carry food back to colony  */
    boolean followingTrail;
    boolean carryingFood;
    int foodCarried;
    private static final float REINFORCE_AMOUNT = 0.2f;
    private Random random = new Random();

    /*The following refers to list of pheromones where strength is measured as a Float .
    So each pheromone in the trail has a position and a float strength value like 0.8f
    */
    
    List<Pheromone<Float>> trail = new ArrayList<>();
   
    
   

    public Worker (int health, int stamina) {
       super(health,stamina);
       this.position = new Point(Main.WIDTH / 2, Main.HEIGHT /2);
       this.followingTrail = false;
       this.carryingFood = false;
       this.foodCarried = 0 ;
    }




// Should scan the surrounding 8 cells for pheromones 
// Use loop to look through neighbouring cells and add pheromones from the trail list
// loop through pheromoneGrid

    public void detectPheromones(int x, int y) { 
    trail.clear() ;

    for (int dx = -1; dx <= 1; dx++) {
        for (int dy = -1; dy <= 1; dy++) {
            int nx = x+dx;
            int ny = y+dy;

            if(nx>=0    &&  nx<Main.WIDTH   &&  ny>=0   &&ny<Main.HEIGHT){
                if(Main.pheromoneGrid[nx][ny] != null){
                    trail.add(Main.pheromoneGrid[nx][ny]);
                }
            }
        }
    }
    }

    public void setFollowingTrail(boolean followingTrail) {
        this.followingTrail = followingTrail;
    }

    public boolean isFollowingTrail() { return followingTrail;}

    public void setCarryingFood(boolean carryingFood) {
        this.carryingFood = carryingFood;
    }

    public boolean isCarryingFood () { return carryingFood;} 

    public int getFoodCarried() { return foodCarried; }

    public void pickUpFood(int amount) {
        this.foodCarried = amount;
        this.carryingFood = true;
    }


    //Connecting to the generics Pheromone<T>
    //Worker checks which pheromone in the list is the strongest
    //and moves towards it .

    public void followTrail(){

    Pheromone<Float> strongest = null;
    int colonyX = Main.WIDTH /2 ;
    int colonyY = Main.HEIGHT /2 ;

    for (Pheromone<Float> p : trail) {
        if (p.getPosition().equals(position)) continue;


    double currentDist = position.distance(colonyX, colonyY);
    double pDist = p.getPosition().distance(colonyX, colonyY);
    if(pDist <= currentDist) continue;


    //Once worker reaches a pheromone position it should ignore that pheromone and look for the next one. We need 
    // to find the strongest pheromone that is not at the worker's current position
     //   if(p.getPosition().equals(position)) continue;

        if (strongest == null || p.getStrength() > strongest.getStrength()) {
            strongest = p;
        }
    }
    


    if(strongest != null) {
       moveTowards(strongest.getPosition()); //use the method
       //when no pheromone trail is left nearby , workers should find another path randomly
    } else {
       
        int dx = random.nextInt(3) -1;
        int dy = random.nextInt(3) -1;
        int newX = position.x+dx;
        int newY = position.y+dy;
                if(newX >= 0 && newX < Main.WIDTH && newY >= 0 && newY < Main.HEIGHT) {
                    position = new Point(newX , newY);
                    if (Main.grid[newX][newY] == Main.CellType.FOOD) {
                        Main.grid[newX][newY] = Main.CellType.EMPTY;
                        Main.pheromoneGrid[newX][newY] = null;
                        pickUpFood(1);

                    }
                }

              
    }

}

public void returnToColony() {
    int colonyX = Main.WIDTH/2;
    int colonyY = Main.HEIGHT/2;
    int dx = Integer.compare(colonyX , position.x);
    int dy = Integer.compare(colonyY, position.y);
    int newX = position.x + dx;
    int newY = position.y + dy;
    position = new Point(newX, newY);

    if (Main.pheromoneGrid[newX][newY] != null ){ 
        Main.pheromoneGrid[newX][newY].reinforce(REINFORCE_AMOUNT);
        System.out.println("Reinforced trail at " + newX + ", " + newY);
    }


    if ( newX == colonyX && newY == colonyY) {
        carryingFood = false;
        foodCarried = 0;
        System.out.println("Worker delivered food to colony!");
    }
} 

 public void moveTowards(Point target) {

    try { 
        if (target == null){ 
             throw new InvalidPositionException("Worker cannot move to a null position!" );
        }
        
        int dx = Integer.compare(target.x , position.x);
        int dy = Integer.compare(target.y, position.y);
        int newX = position.x + dx;
        int newY = position.y + dy;

        
            if (newX >= 0 && newX < Main.WIDTH && newY >= 0 && newY < Main.HEIGHT) {
                this.position = new Point(newX, newY);
                System.out.println("Worker moving to: " + newX +" ," + newY);

                if(Main.grid[newX][newY] == Main.CellType.FOOD) {
                    Main.grid[newX][newY] = Main.CellType.EMPTY;
                    Main.pheromoneGrid[newX][newY] = null;
                    pickUpFood(1);
                    //debug line 
                    System.out.println("Worker picked up food! carryingFood = " +carryingFood);
                }
            }
        } catch (InvalidPositionException e) { System.out.println("Movement error: " + e.getMessage());
      }
            
    }
    



}


 