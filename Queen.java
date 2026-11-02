import java.util.ArrayList;
import java.util.List;


public class Queen extends Ant{
    static final int WORKER_COST = 10 ;
    static final int SCOUT_COST = 15;
    List <Worker> workers = new ArrayList<>();
    List<Scout> scouts = new ArrayList<>();
    int nextAntId = 1;
    int eat ;
    static int noScoutTimer = 0;
    static int noWorkerTimer = 0;
    static final int PANIC_LIMIT = 300; // ticks before she assumes something died

    public Queen(int health , int stamina) {
        super(health,stamina);
    }
    

    void setEat(int eat) { this.eat = eat; }
    int getEat() { return eat ; }

      public Worker prodWorker() {
        if (stamina < WORKER_COST) {
            System.out.println("Queen: not enough energy to spawn a worker. Stamina: " + stamina);
            return null;
        }
        stamina -= WORKER_COST;
        Worker worker = new Worker(100, 100);
        workers.add(worker);
        System.out.println("Queen spawned " + worker + ". Energy left: " + stamina);

        return worker;
    }

    public Scout prodScout() {
        if (stamina < SCOUT_COST) {
            System.out.println("Queen: Not enough energy for a scout. Stamina: " + stamina);
            return null;
        }
        stamina -= SCOUT_COST;
        Scout scout = new Scout(100, 100);
        scouts.add(scout);
        System.out.println("Queen: thinks that ant died, sending emergency scout. Remaining stamina: " + stamina);
        return scout;
    }

    public void gettingOld() throws QueenDeadException {
        stamina -= 1;
        if (stamina <= 0) {
            System.out.println("Queen: Colony collapsed, queen dead.");
            throw new QueenDeadException("Queen is dead.");
        } else {
            System.out.println("Queen: Getting older... current stamina: " + stamina);
        }
    }
    public void feedQueen(int foodAmount) {
        eat = eat + foodAmount;
        int staminaBack = foodAmount * 5; // just made this number up, seemed to work ok
        stamina = stamina + staminaBack;
        System.out.println("Queen: ate the food. Stamina; " + stamina);
    }

    // THIS IS THE QUEEN BRAIN
    // moving ants around, spawning new ones, and panic-spawning if nobody comes home
    public  static void queenBrain(Queen theQueen, List<Scout> scoutList, List<Worker> workerList, Main.CellType[][] grid, int width, int height, boolean moveScouts, boolean moveWorkers) throws QueenDeadException { 
        // queen slowly gets old every tick, loses a bit of stamina
        theQueen.gettingOld();

        // ---------------------------------------------- SCOUTS 
        if (moveScouts) {
            for (int i = 0; i < scoutList.size(); i++) {
                Scout scouterAnt = scoutList.get(i);

                if (scouterAnt.isPathfinding()) {

                    scouterAnt.returnToColony(width, height);

                    // check of ants returned
                    if (scouterAnt.getPosition().x == width / 2) {
                        if (scouterAnt.getPosition().y == height / 2) { 
                            noScoutTimer = 0; // a scout came home, reset the emergency timer

                            // the more scouts that come home, the more workers get sent
                            // so just spawn one worker every single time a scout gets back

                            Worker newworkerAnt = theQueen.prodWorker();
                            if (newworkerAnt != null) {
                                workerList.add(newworkerAnt);
                            }
                        }
                    }

                } else {
                    scouterAnt.wander(width, height);
                }
            }
        }

        // ---------------------------------------------- WORKERS 
        if (moveWorkers) {
            for (int j = 0; j < workerList.size(); j++) {
                Worker workerAnt = workerList.get(j);

                if (workerAnt.isCarryingFood()) {
                    // move the worker back to the colony
                    int dx = Integer.compare(width / 2, workerAnt.getPosition().x);
                    int dy = Integer.compare(height / 2, workerAnt.getPosition().y);
                    workerAnt.getPosition().translate(dx, dy);

                    if (workerAnt.getPosition().x == width / 2 && workerAnt.getPosition().y == height / 2) {
                        noWorkerTimer = 0; // worker made it home, reset the emergency timer
                        try {
                            workerAnt.dropFood();
                            theQueen.feedQueen(1); // queen gets some stamina back for the food
                        } catch (Exception e) {
                            System.out.println("Queen brain: worker had trouble dropping food - " + e.getMessage());
                        }
                    }

                } else {
                    workerAnt.followTrail();

                     // grab food if the worker is on a food tile
                    int wx = workerAnt.getPosition().x;
                    int wy =workerAnt.getPosition().y;
                    if (grid [wx][wy] == Main.CellType.FOOD) {
                        workerAnt.pickUpFood(1);
                        grid [wx][wy] = Main.CellType.EMPTY;
                    }
                }
            }
        }

        // ---------------------------------------------- PANIC CHECKS
        // queen doesnt magically know an ant died, she just waits a while, and if no ant comes home, she assumes they died and makes a new one
        noScoutTimer++;
        noWorkerTimer++;

        if (noScoutTimer > PANIC_LIMIT) {
            System.out.println("Queen: no scouts have come home, must have died. Making a new one.");

            // new scout
            Scout backupScout = theQueen.prodScout();
            if (backupScout != null) {
            
                scoutList.add(backupScout);
            }
            noScoutTimer = 0; // reset so she doesnt spam a new scout every tick after this
        }

        if (noWorkerTimer > PANIC_LIMIT) {
            System.out.println("Queen: no workers have come home, must have died. Making a new one.");

            // new scout
            Worker backupWorker = theQueen.prodWorker();
            if (backupWorker != null) {
                workerList.add(backupWorker);
            }
            noWorkerTimer = 0;
        }
    }

}
