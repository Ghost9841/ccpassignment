package facility;

/**
 * Represents a dryer in the laundry facility.
 */
public class Dryer {
    private final int id;

    public Dryer(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Dryer-" + id;
    }
}