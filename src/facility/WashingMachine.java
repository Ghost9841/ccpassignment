package facility;

/**
 * Represents a washing machine in the laundry facility.
 */
public class WashingMachine {
    private final int id;

    public WashingMachine(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Washer-" + id;
    }
}