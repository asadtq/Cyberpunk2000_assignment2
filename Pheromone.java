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
    
@SuppressWarnings("unchecked")    
/*Needed as the T cast cant be fully verified at compile
 time due to type erasure*/

public void decay() {

    // pheromone fades over time 
    //1. Gets the current strength as a float
    //2. Multiplies by 0.99f
    //3. Updates the strength
    // Cast the result back

float current = ((Number) strength).floatValue();
float decayed = current * 0.99f;
    this.strength = (T) Float.valueOf(decayed);

}
    
}
