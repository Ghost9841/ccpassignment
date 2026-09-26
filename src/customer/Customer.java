package customer;

import manager.ResourceManager;
import manager.StatisticsManager;
import event.SimulationBus;
import facility.WashingMachine;
import facility.Dryer;
import facility.PaymentKiosk;
import exception.MachineFailureException;
import exception.PaymentFailureException;
import util.Logger;
import util.TimeUtil;

/**
 * Represents an individual customer as a concurrent worker thread.
 * Coordinates the sequential lifecycle: Entry -> Wash -> Dry -> Pay -> Exit.
 * Employs robust retry loops with guaranteed resource release in finally blocks.
 */
public class Customer implements Runnable {
    private final int id;
    private final ResourceManager resourceManager;
    private final StatisticsManager stats;
    private final SimulationBus eventBus;
    private final long arrivalTime;
    private static final int RETRY_DELAY_MS = 2000; // 2 seconds retry as specified in requirements

    public Customer(int id, ResourceManager resourceManager, 
                    StatisticsManager stats, SimulationBus eventBus) {
        this.id = id;
        this.resourceManager = resourceManager;
        this.stats = stats;
        this.eventBus = eventBus;
        this.arrivalTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        // Assign clear thread name for logging & thread tracking
        Thread.currentThread().setName("CustomerThread-" + id);
        
        // 1. Entrance gate arrival
        stats.recordArrival();
        int arrivedCount = stats.getCustomersArrived();
        Logger.log(String.format("[ENTRY GATE] Customer-%d entered the laundromat at %s (Total Arrived: %d)",
                id, TimeUtil.formatTime(arrivalTime), arrivedCount));
        
        if (eventBus != null) {
            eventBus.customerArrived(id, arrivedCount);
        }

        try {
            // Stage 1: Washing Stage (4-6s, 5% mid-cycle failure with retry)
            wash();

            // Stage 2: Drying Stage (3-5s, strict wait for available dryer)
            dry();

            // Stage 3: Payment Stage (1-2s, 5% failure or chaos scenario with 2s retry)
            pay();

            // Stage 4: Exit Gate Departure
            long departureTime = System.currentTimeMillis();
            long totalDurationMs = departureTime - arrivalTime;
            stats.recordCompletion(totalDurationMs);
            int servedCount = stats.getCustomersServed();

            Logger.log(String.format("[EXIT GATE] Customer-%d completed all stages and departed. Turnaround time: %s (Total Served: %d)",
                    id, TimeUtil.formatDuration(totalDurationMs), servedCount));

            if (eventBus != null) {
                eventBus.customerCompleted(id, servedCount, totalDurationMs);
            }

        } catch (InterruptedException e) {
            Logger.log(String.format("[CUSTOMER INTERRUPTED] Customer-%d execution interrupted", id));
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Washing stage: blocks until washer acquired, runs wash cycle.
     * If failure occurs, washer is immediately released in finally block,
     * thread waits RETRY_DELAY_MS, and re-acquires a washer until successful.
     */
    private void wash() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            WashingMachine washer = null;
            try {
                washer = resourceManager.acquireWasher(id);
                washer.wash(id, eventBus);
                return; // Successfully finished wash cycle
            } catch (MachineFailureException e) {
                stats.recordWasherFailure();
                Logger.log(String.format("Customer-%d: Washer failure encountered. Washer released. Waiting %s before re-acquiring...",
                        id, TimeUtil.formatDuration(RETRY_DELAY_MS)));
            } finally {
                if (washer != null) {
                    resourceManager.releaseWasher(washer);
                }
            }
            // Machine is released; wait retry delay before next attempt
            Thread.sleep(RETRY_DELAY_MS);
        }
        throw new InterruptedException("Customer-" + id + " interrupted during wash retry loop");
    }

    /**
     * Drying stage: blocks until dryer acquired, runs dry cycle, safely releases dryer.
     */
    private void dry() throws InterruptedException {
        Dryer dryer = null;
        try {
            dryer = resourceManager.acquireDryer(id);
            dryer.dry(id, eventBus);
        } finally {
            if (dryer != null) {
                resourceManager.releaseDryer(dryer);
            }
        }
    }

    /**
     * Payment stage: blocks until kiosk acquired, processes payment.
     * If kiosk fails (or chaos down), kiosk is immediately released in finally block,
     * thread waits RETRY_DELAY_MS (2s), and retries until payment succeeds.
     */
    private void pay() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            PaymentKiosk kiosk = null;
            try {
                kiosk = resourceManager.acquireKiosk(id);
                boolean forceChaosFail = resourceManager.isChaosModeActive();
                kiosk.processPayment(id, eventBus, forceChaosFail);
                return; // Successfully completed payment
            } catch (PaymentFailureException e) {
                stats.recordPaymentFailure();
                Logger.log(String.format("Customer-%d: Payment kiosk failed. Kiosk released. Waiting %s before retrying...",
                        id, TimeUtil.formatDuration(RETRY_DELAY_MS)));
            } finally {
                if (kiosk != null) {
                    resourceManager.releaseKiosk(kiosk);
                }
            }
            // Kiosk is released; wait 2s retry delay before next attempt
            Thread.sleep(RETRY_DELAY_MS);
        }
        throw new InterruptedException("Customer-" + id + " interrupted during payment retry loop");
    }

    public int getId() {
        return id;
    }
}