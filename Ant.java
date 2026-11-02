import java.awt.Point;

public class Ant implements AntBehaviour {

         int health;
         int stamina;
        boolean alive ;
        Point position;
        // static final cost ;

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
        public void takeDamage(int amount) {
                this.health -= amount;
                if(this.health <= 0) die();
        }



}

