// FACTORY PATTERN
// every ant gets made here, instead of calling "new Scout()" / "new Worker()" all over the code.
// to add another ant we only add one more case in here
public class AntFactory {

    static Ant create(AntType type) {
        switch (type) {
            case SCOUT:
                return new Scout(100, 100);
            case WORKER:
                return new Worker(100, 100);
            case SPIDER:
                return new Spider(100, 100);
            default:
                return null; 
        }
    }
}