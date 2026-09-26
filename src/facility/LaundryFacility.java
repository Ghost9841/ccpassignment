package facility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container for all physical machines in the laundry facility.
 */
public class LaundryFacility {
    public static final int WASHER_COUNT = 6;
    public static final int DRYER_COUNT = 4;
    public static final int KIOSK_COUNT = 2;

    private final List<WashingMachine> washers;
    private final List<Dryer> dryers;
    private final List<PaymentKiosk> kiosks;

    public LaundryFacility() {
        // Create washing machines
        washers = new ArrayList<>();
        for (int i = 1; i <= WASHER_COUNT; i++) {
            washers.add(new WashingMachine(i));
        }

        // Create dryers
        dryers = new ArrayList<>();
        for (int i = 1; i <= DRYER_COUNT; i++) {
            dryers.add(new Dryer(i));
        }

        // Create payment kiosks
        kiosks = new ArrayList<>();
        for (int i = 1; i <= KIOSK_COUNT; i++) {
            kiosks.add(new PaymentKiosk(i));
        }
    }

    public List<WashingMachine> getWashers() {
        return Collections.unmodifiableList(washers);
    }

    public List<Dryer> getDryers() {
        return Collections.unmodifiableList(dryers);
    }

    public List<PaymentKiosk> getKiosks() {
        return Collections.unmodifiableList(kiosks);
    }
}