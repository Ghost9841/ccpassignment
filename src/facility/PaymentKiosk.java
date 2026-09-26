package facility;

import event.MachineType;
import event.SimulationBus;
import exception.PaymentFailureException;
import util.Logger;
import util.RandomUtil;
import util.TimeUtil;

/**
 * Represents a self-service payment kiosk.
 * Simulates 1-2 second payment processing with a 5% chance of failure (or chaos failure).
 */
public class PaymentKiosk {
    private final int id;

    public PaymentKiosk(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * Processes payment for a customer.
     *
     * @param customerId ID of customer making payment
     * @param eventBus event bus to broadcast status updates to GUI
     * @param forceChaosFailure if true, kiosk is down as part of the bonus congested scenario
     * @throws PaymentFailureException if kiosk fails to process payment
     * @throws InterruptedException if thread is interrupted
     */
    public void processPayment(int customerId, SimulationBus eventBus, boolean forceChaosFailure)
            throws PaymentFailureException, InterruptedException {
        int durationMs = RandomUtil.paymentDurationMillis();

        Logger.log(String.format("[PAYMENT STARTED] Customer-%d started payment at Kiosk-%d (Duration: %s)",
                customerId, id, TimeUtil.formatDuration(durationMs)));

        if (eventBus != null) {
            eventBus.machineBusy(MachineType.KIOSK, id, customerId, durationMs);
        }

        Thread.sleep(durationMs);

        boolean willFail = forceChaosFailure || RandomUtil.shouldFail(5); // 5% normal failure or chaos down
        if (willFail) {
            Logger.log(String.format("[FAILURE] Kiosk-%d payment failed for Customer-%d!",
                    id, customerId));

            if (eventBus != null) {
                eventBus.machineFailed(MachineType.KIOSK, id, customerId);
            }
            throw new PaymentFailureException("Payment kiosk " + id + " failed");
        } else {
            Logger.log(String.format("[PAYMENT COMPLETED] Customer-%d completed payment at Kiosk-%d",
                    customerId, id));
        }
    }

    @Override
    public String toString() {
        return "Kiosk-" + id;
    }
}