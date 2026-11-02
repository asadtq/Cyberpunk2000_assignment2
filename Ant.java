import java.awt.Point;
import java.util.Random;

public class Ant implements AntBehaviour {

         int health;
         int stamina;
        boolean alive ;
        Point position;
        // static final cost ;

        // running away from danger (the spiders 3x3 patch). Scout and Worker both use this
        boolean fleeing = false;
        int fleeTimer = 0;
        int fleeDx = 0;
        int fleeDy = 0;
        static final int FLEE_TICKS = 20; // number of moves it runs for
        Random fleeRandom = new Random();

        //constructor 
        public Ant(int health , int stamina) {
                this.health = health;
                this.stamina = stamina;
                this.alive = true;
        }

        public void setHealth(int health) {this.health = health;}
        public int getHealth() { return health;}
        public void setStamina(int stamina) {this.stamina = stamina;}
        public int getStamina() {return stamina;}
        public void die() {alive = false ; health = 0;}
        public boolean isAlive() { return alive;}
        public void move(Point destination) { this.position = destination;}
        public Point getPosition() { return position;}
        public boolean isFleeing() { return fleeing; }

        // called after the ant moves. oldX and oldY is where it was before the move
        public void checkForDanger(int oldX, int oldY) {
                if (fleeing) return; // already running, dont restart the timer

                Point p = getPosition();
                if (Main.dangerGrid[p.x][p.y] != null) {
                        fleeing = true;
                        fleeTimer = FLEE_TICKS;

                        // run back the way it came from
                        fleeDx = oldX - p.x;
                        fleeDy = oldY - p.y;

                        // if it didnt actually move, pick a random way to run
                        if (fleeDx == 0 && fleeDy == 0) {
                                fleeDx = fleeRandom.nextInt(3) - 1;
                                fleeDy = fleeRandom.nextInt(3) - 1;
                        }
                }
        }

        // run in a straight line until the timer runs out
        public void flee() {
                Point p = getPosition();
                int newX = p.x + fleeDx;
                int newY = p.y + fleeDy;

                // only move if its still on the map (otherwise it stops at the edge)
                if (newX >= 0 && newX < Main.WIDTH && newY >= 0 && newY < Main.HEIGHT) {
                        p.setLocation(newX, newY);
                }

                fleeTimer--;
                if (fleeTimer <= 0) {
                        fleeing = false;
                }
        }

        public void takeDamage(int amount) {
                this.health -= amount;
                if(this.health <= 0) die();
        }



}