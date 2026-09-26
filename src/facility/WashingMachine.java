package facility;

import event.MachineType;
import event.SimulationBus;
import exception.MachineFailureException;
import util.Logger;
import util.RandomUtil;
import util.TimeUtil;

/**
 * Represents a physical washing machine in the laundromat.
 * Simulates a 4-6 second wash cycle with a 5% chance of mid-cycle failure.
 */
public class WashingMachine {
    private final int id;

    public WashingMachine(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * Executes a washing cycle for the specified customer.
     * 
     * @param customerId ID of customer using this machine
     * @param eventBus event bus to broadcast status updates to GUI
     * @throws MachineFailureException if machine suffers a mid-cycle failure
     * @throws InterruptedException if thread is interrupted while running
     */
    public void wash(int customerId, SimulationBus eventBus)
            throws MachineFailureException, InterruptedException {
        int durationMs = RandomUtil.washDurationMillis();
        boolean willFail = RandomUtil.shouldFail(5); // 5% chance of failure

        Logger.log(String.format("[WASHING STARTED] Customer-%d started wash on Washer-%d (Cycle: %s)",
                customerId, id, TimeUtil.formatDuration(durationMs)));

        if (eventBus != null) {
            eventBus.machineBusy(MachineType.WASHER, id, customerId, durationMs);
        }

        if (willFail) {
            // Fails roughly midway through cycle
            int failDelay = durationMs / 2;
            Thread.sleep(failDelay);

            Logger.log(String.format("[FAILURE] Washer-%d encountered mid-cycle mechanical failure while washing for Customer-%d!",
                    id, customerId));

            if (eventBus != null) {
                eventBus.machineFailed(MachineType.WASHER, id, customerId);
            }
            throw new MachineFailureException("Washer-" + id + " failed mid-cycle");
        } else {
            // Normal full cycle
            Thread.sleep(durationMs);
            Logger.log(String.format("[WASHING COMPLETED] Customer-%d finished washing on Washer-%d",
                    customerId, id));
        }
    }

    @Override
    public String toString() {
        return "Washer-" + id;
    }
}