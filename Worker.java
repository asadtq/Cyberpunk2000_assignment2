import java.awt.Point;
import java.util.ArrayList;
import java.util.List;



class Worker extends Ant{
    /*1. Worker needs to detect pheromones nearby
    2. Follow the strongest pheromone trail
    3. Pick up food when it reaches the source
    4. Carry food back to colony  */
    boolean followingTrail;
    boolean carryingFood;
    int foodCarried;
  
  

    /*The following refers to list of pheromones where strength is measured as a Float .
    So each pheromone in the trail has a position and a float strength value like 0.8f
    */
    
    List<Pheromone<Float>> trail = new ArrayList<>();

    private final MovementStrategy followTrailStrategy = new FollowTrailStrategy();
    private final MovementStrategy returnToColonyStrategy = new ReturnToColonyStrategy();
    private MovementStrategy strategy = followTrailStrategy;
   
    
   

    public Worker (int health, int stamina) {
       super(health,stamina);
       this.position = new Point(Main.WIDTH / 2, Main.HEIGHT /2);
       this.followingTrail = false;
       this.carryingFood = false;
       this.foodCarried = 0 ;
    }

    public void setStrategy(MovementStrategy strategy) {
        this.strategy = strategy;
    }

    public void update() {
        strategy.move(this);
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
        setStrategy(returnToColonyStrategy);
    }

    public void deliverFood(){
        carryingFood = false;
        foodCarried = 0;
        setStrategy(followTrailStrategy);
        System.out.println("Worker delivered food to colony!");
    }

    //Connecting to the generics Pheromone<T>
    //Worker checks which pheromone in the list is the strongest
    //and moves towards it .

    



 
    



}


 