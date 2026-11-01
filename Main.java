import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.swing.*;


public class Main {

    static final int WIDTH = 20;
    static final int HEIGHT = 20;
    static final int CELL_SIZE = 30;
    static final int TOTAL_FOOD = 10;
    static int respawnTimer = 0 ;
    static int scoutspawnTimer = 0 ;
    static final int scoutSPAWN_RATE = 30; 
    static final int MAX_SCOUTS = 2 ;
    static final int RESPAWN_RATE = 50;// so every 50 ticks
    static int scoutMoveTimer = 0 ;
    static final int SCOUT_MOVE_RATE = 2; // move every 3 ticks
    static int workerMoveTimer = 0 ;
    static final int WORKER_MOVE_RATE = 4;
    static final int MAX_WORKERS = 5;
    static int workerSpawnTimer = 0;
    static final int WORKER_SPAWN_RATE = 40;
    static final boolean DEBUG = true;

    static Queen queen;
    static List<Scout> scouts = new ArrayList<>();
    static List<Worker> workers = new ArrayList<>();



    enum CellType {
        EMPTY , FOOD , WALL , COLONY , QUEEN
    }


   ///  Timer for food respawn

   static void update() {
        respawnTimer++;
        if (respawnTimer >= RESPAWN_RATE){
            respawnFood();
            respawnTimer = 0 ;
        }

        // counting pheromones to debug an error with workers not appearing

        int pheromoneCount = 0;
        for(int x = 0; x < WIDTH; x++){
            for ( int y = 0; y <HEIGHT; y++){
                if(pheromoneGrid[x][y] != null)
                    pheromoneCount++;
            }
        }
        if (DEBUG) {
            System.out.println("Pheromones: " + pheromoneCount);
            System.out.println("Avg pheromone strength: " + averagePheromoneStrength());
        }
        // debug for workers update
        if (DEBUG) {
        for (Worker worker : workers) {
            Point p = worker.getPosition();
            System.out.println("Worker at: " + p.x + ", " + p.y);
          
        }
    }


        scoutspawnTimer++;
        if(scoutspawnTimer >= scoutSPAWN_RATE && scouts.size() < MAX_SCOUTS) {
            Scout scout = queen.prodScout();
            if (scout != null) {
                scouts.add(scout);
                scoutspawnTimer = 0;
            }
        }

        scoutspawnTimer++;
        if(scoutspawnTimer >= WORKER_SPAWN_RATE && workers.size() < MAX_WORKERS) {
            Worker worker = queen.prodWorker();
            if (worker != null) {
                workers.add(worker);
                scoutspawnTimer = 0;
            }
        }


        
        scoutMoveTimer++;
        if (scoutMoveTimer >= SCOUT_MOVE_RATE) {
            for (Scout scout : scouts) {
                if (scout.isPathfinding()) {
                    scout.returnToColony(WIDTH, HEIGHT);
                }else {
                    scout.wander(WIDTH, HEIGHT);
                }
            }
            scoutMoveTimer = 0 ;
        }

        workerMoveTimer++;
        if (workerMoveTimer >= WORKER_MOVE_RATE) {
           for (Worker worker : workers) {
            if( worker.isCarryingFood()) {
                worker.returnToColony();
            } else {
                Point p = worker.getPosition();
                worker.detectPheromones(p.x, p.y);
                worker.followTrail();
           }
    }
    workerMoveTimer = 0;
    }


    // Pheromone needs to decay otherwise ants keep going to same place

    for(int x = 0; x < WIDTH ; x++) {
        for(int y = 0; y < HEIGHT; y++) {
            if(pheromoneGrid[x][y] != null){
                pheromoneGrid[x][y].decay();
                if(pheromoneGrid[x][y].getStrength().floatValue() < 0.01f){
                    pheromoneGrid[x][y] = null;
                }
            }
        }
    }

   
}


    static int countFood() {
        int count = 0;
        for (int x = 0; x < WIDTH ; x++) {
            for (int y = 0 ; y < HEIGHT; y++) {
                if (grid[x][y] == CellType.FOOD) count++ ;
            }
        }
        return count;
    }

    static double averagePheromoneStrength() {
        return Arrays.stream(pheromoneGrid)
        .flatMap(Arrays::stream)
        .filter(Objects::nonNull)
        //lamba p -> something; where p is Pheromone<Float> 
        // Turns each pheromone into a number 
        .mapToDouble(p -> p.getStrength().doubleValue())
        .average()
        .orElse(0.0);

    }
    

    static Random random = new Random() ;

    static void respawnFood() { 
        if (countFood() >= TOTAL_FOOD) return; // stop food from spawning if it exceeds counter

    int foodPlaced = 0;
    while (foodPlaced < 3){
        int x = random.nextInt(WIDTH);
        int y = random.nextInt(HEIGHT);
        if (grid[x][y] == CellType.EMPTY) {
            grid[x][y] = CellType.FOOD;
            foodPlaced++;
        
            }

        }
    }


    /* Food collection
    
    List <Int foodCollection
        food.stream()
        .m(food - > food.split(""))
        .flatMap(Array::stream)
        .distinct()
        .collect(toList());
        */


    static CellType[][] grid = new CellType[WIDTH][HEIGHT];
    @SuppressWarnings("unchecked")
    static Pheromone<Float>[][] pheromoneGrid = new Pheromone[WIDTH][HEIGHT];

    public static void main(String[] args) {
        
        initGrid();
        JFrame frame = new JFrame("Ant Colony");

        JPanel panel = new JPanel() {
            @Override 
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
            }
        };

        


    

        panel.setPreferredSize(new Dimension(WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE));
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Timer timer = new Timer(100, e -> {
            update();
            panel.repaint();
    });
            timer.start();
}

    

    static void drawGrid(Graphics g) {



        for (int x = 0; x < WIDTH; x++){
            for (int y = 0 ; y < HEIGHT; y++){
                switch (grid[x][y]){
                    case EMPTY:     g.setColor(Color.BLACK); break;
                    case FOOD:      g.setColor(Color.YELLOW); break;
                    case WALL:      g.setColor(Color.RED);  break;
                    //case COLONY:    g.setColor(Color.MAGENTA);  break;
                    case QUEEN:     g.setColor(Color.PINK); break;
                    default:        g.setColor(Color.BLACK);    break;
                    
                }
                g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                 g.drawRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
               
        }

        // Need to draw pheromone trails as they are not visible

        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT; y++){
                if(pheromoneGrid[x][y] != null){
                    float strength = pheromoneGrid[x][y].getStrength().floatValue();
                    float alpha = Math.max(0f, Math.min(strength, 1f));
                    g.setColor(new Color(1f, 1f, 1f, alpha));
                    g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        g.setColor(Color.GREEN);
            for (Scout scout : scouts) {
        Point p = scout.getPosition();
        g.fillOval(p.x * CELL_SIZE + 5, p.y * CELL_SIZE +5 , CELL_SIZE -10, CELL_SIZE -10);
    }


   

    g.setColor(Color.BLUE);
    for (Worker worker : workers) {
    Point p = worker.getPosition();
    g.fillRect(p.x * CELL_SIZE + 5, p.y *CELL_SIZE +5, CELL_SIZE -10, CELL_SIZE-10);
    }

 }
 
 

 static void initGrid () {
    // set every cell to Empty 
    // place COLONY in the middle 
    // scatter food around the map
    // place Queen

    for (int x = 0; x < WIDTH; x++) {
        for (int y = 0; y < HEIGHT; y++) {
            grid[x][y] = CellType.EMPTY;
        }
    }
// Spawn Queen
    queen = new Queen(100,100);
    grid[WIDTH / 2][HEIGHT / 2] = CellType.QUEEN;

    //place colony in the middle
   // grid[WIDTH / 2][HEIGHT / 2] = CellType.COLONY;

    scouts.add(new Scout(100, 100));
    scouts.add(new Scout(100, 100));
    scouts.add(new Scout(100, 100));

    workers.add(new Worker(100, 100));
    workers.add(new Worker(100, 100));

 

    // scatter food 
    Random random = new Random();
    int foodPlaced = 0;
    while (foodPlaced < 10){
        int x = random.nextInt(WIDTH);
        int y = random.nextInt(HEIGHT);
        if (grid[x][y] == CellType.EMPTY) {
            grid[x][y] = CellType.FOOD;
            foodPlaced++;
        }
    }
 }

}