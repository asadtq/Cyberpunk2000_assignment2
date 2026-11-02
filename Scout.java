import java.awt.Point;
import java.util.Random;

     class Scout extends Ant { 
        boolean pathfinding;
        private Point position;
        private Random random;

        public Scout(int health , int stamina) {
            super(health, stamina);
            this.position = new Point(10,10);
            this.random = new Random();
            this.pathfinding = false;
        }

        public Point getPosition() {
            return position;
        }

        public void setPathfinding(boolean pathfinding_set) {
            this.pathfinding = pathfinding_set;
        }

        public boolean isPathfinding() {
            return pathfinding;
        }

    //movement
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

        // Scout need to mark food location with pheromone
        if (Main.grid[newX][newY] == Main.CellType.FOOD) {
            Main.pheromoneGrid[newX][newY] = leavePheromone();
            pathfinding = true; // heading back to colony
        }
       
    }

    public void returnToColony(int gridWidth , int gridHeight) {
        int colonyX = Main.WIDTH / 2;
        int colonyY = Main.HEIGHT / 2;

        int dx = Integer.compare(colonyX , position.x); 
        int dy = Integer.compare(colonyY, position.y); 

        int newX = position.x + dx;
        int newY = position.y + dy;

        position.setLocation(newX, newY);

        //leave pheromon t on the way back
        Main.pheromoneGrid[newX][newY] = leavePheromone();

        if (newX == colonyX && newY == colonyY) {
            pathfinding = false;
        }

        
    }

    /**
        drop pheremone at scount position
        retrun object with float strength
     */
    public Pheromone<Float> leavePheromone() {
        return new Pheromone<>(new Point(this.position), 1.0f);
    }


} 

    
    

