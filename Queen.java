import java.util.ArrayList;
import java.util.List;


public class Queen extends Ant{
    static final int WORKER_COST = 10 ;
    static final int SCOUT_COST = 15;
    List <Worker> workers = new ArrayList<>();
    List<Scout> scouts = new ArrayList<>();
    int nextAntId = 1;
    int eat ;

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
        Worker worker = (Worker) AntFactory.create(AntType.WORKER);
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
        Scout scout = (Scout) AntFactory.create(AntType.SCOUT);
        scouts.add(scout);
        System.out.println("Queen: thinks that ant died, sending emergency scout. Remaining stamina: " + stamina);
        return scout;
    }

    // queen only ages every AGING_RATE ticks (was every single tick, which killed the colony in ~10 seconds
    // before workers could deliver food; set to 1 for the original behaviour)
    static final int AGING_RATE = 3;
    int ageTimer = 0;

    public void gettingOld() throws QueenDeadException {
        ageTimer++;
        if (ageTimer < AGING_RATE) return;
        ageTimer = 0;

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

}