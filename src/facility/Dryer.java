package facility;

import event.MachineType;
import event.SimulationBus;
import util.Logger;
import util.RandomUtil;
import util.TimeUtil;

/**
 * Represents a physical dryer in the laundromat.
 * Simulates a 3-5 second drying cycle (no failure).
 */
public class Dryer {
    private final int id;

    public Dryer(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * Executes a drying cycle for the specified customer.
     *
     * @param customerId ID of customer using this dryer
     * @param eventBus event bus to broadcast status updates to GUI
     * @throws InterruptedException if thread is interrupted while running
     */
    public void dry(int customerId, SimulationBus eventBus) throws InterruptedException {
        int durationMs = RandomUtil.dryDurationMillis();

        Logger.log(String.format("[DRYING STARTED] Customer-%d started drying on Dryer-%d (Cycle: %s)",
                customerId, id, TimeUtil.formatDuration(durationMs)));

        if (eventBus != null) {
            eventBus.machineBusy(MachineType.DRYER, id, customerId, durationMs);
        }

        Thread.sleep(durationMs);

        Logger.log(String.format("[DRYING COMPLETED] Customer-%d finished drying on Dryer-%d",
                customerId, id));
    }

    @Override
    public String toString() {
        return "Dryer-" + id;
    }
}