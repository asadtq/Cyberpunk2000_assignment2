import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;


public class Main {
//program settings
static final int WIDTH = 20;
static final int HEIGHT = 20;
static final int CELL_SIZE = 30;
static final int TOTAL_FOOD = 10;

//respawn rate
static int respawnTimer = 0 ;
static final int RESPAWN_RATE = 50;// so every 50 ticks
static int scoutMoveTimer = 0 ;
static int workerMoveTimer = 0;
static final int SCOUT_MOVE_RATE = 2; // move evry 3 ticks
static final int WORKER_MOVE_RATE = 1;

//queen
static final int workerSPAWN_RATE = 45; // slightly slower than scouts
static final int MAX_WORKERS = 5;


static Queen queen;
static Spider spider;
static int spiderMoveTimer = 0;
static final int SPIDER_MOVE_RATE = 3; // spider moves every 3 ticks
static List<Scout> scouts = new ArrayList<>();
static List<Worker> workers = new ArrayList<>();


enum CellType {
    EMPTY , FOOD , WALL , COLONY , QUEEN
}

static boolean stopGame = false;

static void update() {
    if (stopGame ) return; 

    respawnTimer++;
    if (respawnTimer >= RESPAWN_RATE ){
        respawnFood();
        respawnTimer = 0 ;
    }

   
    try {
        queen.gettingOld(); 
    } catch (QueenDeadException e ) {
        System.out.println("GAME OVER: " + e.getMessage( ) );
        stopGame = true;
        return; 
    }

    scoutMoveTimer++;
    if (scoutMoveTimer >= SCOUT_MOVE_RATE ) {
        moveScouts();
        scoutMoveTimer = 0;
    }

    workerMoveTimer++;
    if (workerMoveTimer >= WORKER_MOVE_RATE ) {
        moveWorkers();
        workerMoveTimer = 0;
    }

    checkPanic();

    // spider moves slower than the ants
    boolean moveSpiderNow = false;
    if (spiderMoveTimer >= SPIDER_MOVE_RATE ) {
        moveSpiderNow = true;
        spiderMoveTimer = 0;
    }
    spider.act(scouts, workers, moveSpiderNow );

    // anything the spider ate gets removed from the colony (lambda: remove every ant that is not alive )
    scouts.removeIf(a -> !a.isAlive( ) );
    workers.removeIf(a -> !a.isAlive( ) );
    

    // PARALLEL STREAM
    IntStream.range(0, WIDTH ).parallel().forEach(x -> {
        for (int y = 0; y < HEIGHT; y++ ) {
            if (pheromoneGrid[x ][y ] != null ) {
                pheromoneGrid[x ][y ].decay();
                if (pheromoneGrid[x ][y ].getStrength() < 0.05f ) {
                    pheromoneGrid[x ][y ] = null; // Clear whan faded
                }

            }

            // danger pheromone fades slower (0.5% per tick) so ants avoid the spot for ~1 minute
            if (dangerGrid[x ][y ] != null ) {
                dangerGrid[x ][y ].decay(0.995f );
                if (dangerGrid[x ][y ].getStrength() < 0.05f ) {
                    dangerGrid[x ][y ] = null;
                }
            }
        }
    } );
}



// if no ant comes home for x ticks the queen assumes they died
static int noScoutTimer = 0;
static int noWorkerTimer = 0;
static final int PANIC_LIMIT = 300;

// move every scout. A scout that found food goes home, the others wander
static void moveScouts() {
    for (int i = 0; i < scouts.size(); i++ ) {
        Scout scouterAnt = scouts.get(i );

        // scout is running away from the danger, thats all it does this tick
        if (scouterAnt.isFleeing( ) ) {
            scouterAnt.flee();
            continue;
        }

        // remember where the scout was so we know which way it came from
        int oldX = scouterAnt.getPosition().x;
        int oldY = scouterAnt.getPosition().y;

        if (scouterAnt.isPathfinding( ) ) {
            scouterAnt.returnToColony(WIDTH, HEIGHT );

            // check if the scout got home
            if (scouterAnt.getPosition().x == WIDTH / 2 && scouterAnt.getPosition().y == HEIGHT / 2 ) {
                noScoutTimer = 0; // a scout came home, reset emergency timer

                // one new worker evry time a scout gets back
                Worker newWorkerAnt = queen.prodWorker();
                if (newWorkerAnt != null ) {
                    workers.add(newWorkerAnt );
                }
            }
        } else {
            scouterAnt.wander(WIDTH, HEIGHT );
        }

        // if the scout just stepped into the danger square, make it run the other way
        scouterAnt.checkForDanger(oldX, oldY );
    }
}


// move every worker
static void moveWorkers() {
    for (int j = 0; j < workers.size(); j++ ) {
        Worker workerAnt = workers.get(j );

        // worker is running away from the danger, thats all it does this tick
        if (workerAnt.isFleeing( ) ) {
            workerAnt.flee();
            continue;
        }

        // remember where the worker was so we know which way it came from
        int oldX = workerAnt.getPosition().x;
        int oldY = workerAnt.getPosition().y;

        if (workerAnt.isReportingDeath( ) ) {
            // run straight home to tell the queen
            int rdx = Integer.compare(WIDTH / 2, workerAnt.getPosition().x );
            int rdy = Integer.compare(HEIGHT / 2, workerAnt.getPosition().y );
            workerAnt.getPosition().translate(rdx, rdy );

            if (workerAnt.getPosition().x == WIDTH / 2 && workerAnt.getPosition().y == HEIGHT / 2 ) {
                workerAnt.setReportingDeath(false );
                System.out.println("Queen: a worker died, we need more!" );
                Worker replacement = queen.prodWorker();
                if (replacement != null ) {
                    workers.add(replacement );
                }
            }
            continue; 
        }

        if (workerAnt.isCarryingFood( ) ) {
            // walk back to the colony
            int dx = Integer.compare(WIDTH / 2, workerAnt.getPosition().x );
            int dy = Integer.compare(HEIGHT / 2, workerAnt.getPosition().y );
            workerAnt.getPosition().translate(dx, dy );

            if (workerAnt.getPosition().x == WIDTH / 2 && workerAnt.getPosition().y == HEIGHT / 2 ) {
                noWorkerTimer = 0; // worker made it home, reset the emergency timer
                try {
                    workerAnt.dropFood();
                    queen.feedQueen(1 ); // queen gets some stamina back for food
                } catch (Exception e ) {
                    System.out.println("worker had trouble dropping food - " + e.getMessage( ) );
                }
            }

        } else {
            workerAnt.followTrail();

            int wx = workerAnt.getPosition().x;
            int wy = workerAnt.getPosition().y;
            if (grid[wx ][wy ] == CellType.FOOD ) {
                workerAnt.pickUpFood(1 );
                grid[wx ][wy ] = CellType.EMPTY;
            }
        }

        // if the worker just stepped into danger square, make it flee
        workerAnt.checkForDanger(oldX, oldY );
    }
}

// the queen doesnt magically know an ant died, if nobody comes home for a while she makes a new one
static void checkPanic() {
    noScoutTimer++;
    noWorkerTimer++;

    if (noScoutTimer > PANIC_LIMIT ) {
        System.out.println("Queen: no scouts have come home, must have died. Making a new one." );
        Scout backupScout = queen.prodScout();
        if (backupScout != null ) {
            scouts.add(backupScout );
        }
        noScoutTimer = 0; // reset so she doesnt spam a new scout every tick
    }

    if (noWorkerTimer > PANIC_LIMIT ) {
        System.out.println("Queen: no workers have come home, must have died. Making a new one." );
        Worker backupWorker = queen.prodWorker();
        if (backupWorker != null ) {
            workers.add(backupWorker );
        }
        noWorkerTimer = 0;
    }
}


// STREAM
static int countFood() {
    return (int) Arrays.stream(grid )
                       .flatMap(row -> Arrays.stream(row ) )
                       .filter(cell -> cell == CellType.FOOD )
                       .count();
}

// ant list
static List<Ant> getAllAnts() {
    List<Ant> allAnts = new ArrayList<>();
    allAnts.addAll(scouts );
    allAnts.addAll(workers );
    return allAnts;
}


static void drawStats(Graphics g ) {
    List<Ant> allAnts = getAllAnts();

    // COLLECTOR 1: groupingBy, group the ants by what class they are and count each group
    Map<String, Long> countByType = allAnts.stream().collect(Collectors.groupingBy(a -> a.getClass().getSimpleName(), Collectors.counting() ) );

    // COLLECTOR 2: partitioningBy, split the ants into true (fleeing) and false (not fleeing) and count each
    Map<Boolean, Long> fleeingCount = allAnts.stream().collect(Collectors.partitioningBy(a -> a.isFleeing(), Collectors.counting() ) );

    // STREAM: how many cells still have a food trail on them
    long trailCells = Arrays.stream(pheromoneGrid ).flatMap(row -> Arrays.stream(row ) ).filter(p -> p != null ).count();

    g.setFont(new Font("Arial", Font.BOLD, 12 ) );
    g.setColor(Color.WHITE );
    g.drawString("Scouts: " + countByType.getOrDefault("Scout", 0L )
            + "  Workers: " + countByType.getOrDefault("Worker", 0L )
            + "  Panicking: " + fleeingCount.get(true ), 5, 15 );
    g.drawString("Trail cells: " + trailCells + "  Food: " + countFood()
            + "  Queen stamina: " + queen.getStamina(), 5, 30 );
}


static Random random = new Random() ;
static void respawnFood() { 
    if (countFood() >= TOTAL_FOOD ) return; // food spawn max

int foodPlaced = 0;
while (foodPlaced < 3 ){
    int x = random.nextInt(WIDTH );
    int y = random.nextInt(HEIGHT );
    if (grid[x ][y ] == CellType.EMPTY ) {
        grid[x ][y ] = CellType.FOOD;
        foodPlaced++;
    
        }

    }
}



static CellType[][] grid = new CellType[WIDTH ][HEIGHT ];
@SuppressWarnings("unchecked")
static Pheromone<Float>[][] pheromoneGrid = new Pheromone[WIDTH ][HEIGHT ];

// second pheromone layer: left behind when the spider kills an ant, ants avoid it
@SuppressWarnings("unchecked")
static Pheromone<Float>[][] dangerGrid = new Pheromone[WIDTH ][HEIGHT ];

public static void main(String[] args ) {
    
    initGrid();
    JFrame frame = new JFrame("Ant Colony" );

    JPanel panel = new JPanel() {
        @Override 
        protected void paintComponent(Graphics g ) {
            super.paintComponent(g );
            drawGrid(g );
            drawStats(g );
            if (stopGame ) {
                drawGameOverText(g );
            }
        }
    };

    panel.setPreferredSize(new Dimension(WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE ) );
    frame.add(panel );
    frame.pack();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
    frame.setVisible(true );

    Timer timer = new Timer(100, e -> {
        update();
        panel.repaint();
} );
        timer.start();
}



static void drawGrid(Graphics g ) {
    for (int x = 0; x < WIDTH; x++ ){
        for (int y = 0 ; y < HEIGHT; y++ ){
            switch (grid[x ][y ] ){
                case EMPTY:     g.setColor(Color.BLACK ); break;
                case FOOD:      g.setColor(Color.YELLOW ); break;
                case WALL:      g.setColor(Color.RED );  break;
                //case COLONY:    g.setColor(Color.MAGENTA);  break;
                case QUEEN:     g.setColor(Color.PINK ); break;
                default:        g.setColor(Color.BLACK );    break;
                
            }
            
            g.fillRect(x * CELL_SIZE,y* CELL_SIZE , CELL_SIZE, CELL_SIZE );
            
            // check if pheromon is there
            if (pheromoneGrid[x ][y ] != null ) {
                if (grid[x ][y ] == CellType.EMPTY ) { // only draw on empty spaces
                    float s = pheromoneGrid[x ][y ].getStrength();
                    
                    // set transparency
                    int trans = (int)(s * 180 );
                    if (trans > 255 ) {
                        trans = 255;
                    }
                    if (trans < 0 ) {
                        trans = 0;
                    }
                    
                    Color pheroColor = new Color(0, 255, 255, trans );
                    g.setColor(pheroColor );
                    g.fillRect(x * CELL_SIZE, y *CELL_SIZE, CELL_SIZE , CELL_SIZE );
                }
            }

            // draw danger pheromone
            if (dangerGrid[x ][y ] != null ) {
                int dangerAlpha = (int)(dangerGrid[x ][y ].getStrength() * 160 );
                if (dangerAlpha > 255 ) {
                    dangerAlpha = 255;
                }
                if (dangerAlpha < 0 ) {
                    dangerAlpha = 0;
                }
                g.setColor(new Color(255, 140, 0, dangerAlpha ) );
                g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE );
            }

            g.setColor(Color.DARK_GRAY );
            g.drawRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE );
        }
    }

    g.setColor(Color.GREEN );
    for (Scout scout : scouts ) {
        Point p = scout.getPosition();
        g.fillOval(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE , CELL_SIZE );
    }

    g.setColor(Color.BLUE );
    for (Scout scout : scouts ) {
        Point p = scout.getPosition();
        g.fillOval(p.x * CELL_SIZE + 5, p.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10 );
    }

    // draw workers
    g.setColor(Color.RED );
    for (Worker worker : workers ) {
        Point p = worker.getPosition();
        g.fillOval(p.x * CELL_SIZE + 5, p.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10 );
    }

    // draw spider 
    Point sp = spider.getPosition();
    g.setColor(Color.MAGENTA );
    g.fillOval(sp.x * CELL_SIZE + 2, sp.y * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4 );
    g.setColor(Color.WHITE );
    g.fillOval(sp.x * CELL_SIZE + 8, sp.y * CELL_SIZE + 9, 5, 5 );
    g.fillOval(sp.x * CELL_SIZE + 17, sp.y * CELL_SIZE + 9, 5, 5 );
}



static void initGrid ( ) {

    //empty cell
    for (int x = 0; x < WIDTH; x++ ) {
        for (int y = 0; y < HEIGHT; y++ ) {
            grid[x ][y ] = CellType.EMPTY;
        }
    }
    // add Queen
    queen = new Queen(100,100 );
    grid[WIDTH/2 ][HEIGHT /2 ] =CellType.QUEEN;
    // add Scouts
    scouts.add((Scout ) AntFactory.create(AntType.SCOUT ) );
    scouts.add((Scout ) AntFactory.create(AntType.SCOUT ) );
    scouts.add((Scout ) AntFactory.create(AntType.SCOUT ) );

    // add Spider
    spider = (Spider ) AntFactory.create(AntType.SPIDER );

    // scatter food 
    Random random = new Random();
    int foodPlaced = 0;
    while (foodPlaced < 10 ){
        int x = random.nextInt(WIDTH );
        int y = random.nextInt(HEIGHT );
        if (grid[x ][y ] == CellType.EMPTY ) {
            grid[x ][y ] = CellType.FOOD;
            foodPlaced++;
        }
    }
}


// simulation stop text draw
static void drawGameOverText(Graphics g ) {
    String msg = "QUEEN DEAD";
    g.setFont(new Font("Arial", Font.BOLD, 67 ) );
    FontMetrics fm = g.getFontMetrics();
    int textWidth = fm.stringWidth(msg );
    int x = (WIDTH * CELL_SIZE - textWidth ) / 2;
    int y = (HEIGHT * CELL_SIZE ) / 2;
    g.setColor(Color.RED );
    g.drawString(msg, x, y );
}


}