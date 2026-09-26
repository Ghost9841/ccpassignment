package facility;

/**
 * Represents a payment kiosk in the laundry facility.
 */
public class PaymentKiosk {
    private final int id;

    public PaymentKiosk(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Kiosk-" + id;
    }
}