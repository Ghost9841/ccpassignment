package event;

/**
 * Enumeration representing the three types of shared physical resources
 * in the smart laundry facility simulation.
 */
public enum MachineType {
    WASHER("Washing Machine", "WASHER"),
    DRYER("Dryer", "DRYER"),
    KIOSK("Payment Kiosk", "KIOSK");

    private final String displayName;
    private final String shortName;

    MachineType(String displayName, String shortName) {
        this.displayName = displayName;
        this.shortName = shortName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortName() {
        return shortName;
    }
}