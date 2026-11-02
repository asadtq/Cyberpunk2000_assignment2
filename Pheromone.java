import java.awt.Point;
// Generic Class

//bound type parameter to Number
public class Pheromone<T extends Number> {
    Point position;
    T strength;

    public Pheromone(Point position, T strength) {
        this.position = position;
        this.strength = strength;

    }

    public T getStrength() { return strength; }
    public Point getPosition() { return position; }
    
public void decay() {
    decay(0.99f); // normal food trails fade at 1% per tick
}

/*Needed as the T cast cant be fully verified at compile
 time due to type erasure*/
// caller picks the rate, so danger pheromone can linger longer than food trails
@SuppressWarnings("unchecked")
public void decay(float rate) {
    float current = ((Number) strength).floatValue();
    float decayed = current * rate;
    this.strength = (T) Float.valueOf(decayed);
}

}