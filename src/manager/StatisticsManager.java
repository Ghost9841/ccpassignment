package manager;

import util.Logger;
import util.TimeUtil;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Thread-safe statistics collector for the simulation.
 * Uses atomic variables to ensure safe concurrent updates.
 */
public class StatisticsManager {
    // Basic counters
    private final AtomicInteger customersArrived = new AtomicInteger(0);
    private final AtomicInteger customersServed = new AtomicInteger(0);
    private final AtomicLong totalCompletionTimeMs = new AtomicLong(0);
    
    // Resource usage counters
    private final AtomicInteger currentWashers = new AtomicInteger(0);
    private final AtomicInteger maxConcurrentWashers = new AtomicInteger(0);
    private final AtomicInteger currentDryers = new AtomicInteger(0);
    private final AtomicInteger maxConcurrentDryers = new AtomicInteger(0);
    private final AtomicInteger currentKiosks = new AtomicInteger(0);
    
    // Failure counters
    private final AtomicInteger washerFailures = new AtomicInteger(0);
    private final AtomicInteger paymentFailures = new AtomicInteger(0);

    /**
     * Record a customer arrival.
     */
    public void recordArrival() {
        customersArrived.incrementAndGet();
    }

    /**
     * Record a customer completion with total time.
     */
    public void recordCompletion(long timeMs) {
        customersServed.incrementAndGet();
        totalCompletionTimeMs.addAndGet(timeMs);
    }

    /**
     * Record that a washer was acquired (update current and max).
     */
    public void washerAcquired() {
        int current = currentWashers.incrementAndGet();
        updateMax(currentWashers, maxConcurrentWashers, current);
    }

    /**
     * Record that a washer was released.
     */
    public void washerReleased() {
        currentWashers.decrementAndGet();
    }

    /**
     * Record that a dryer was acquired (update current and max).
     */
    public void dryerAcquired() {
        int current = currentDryers.incrementAndGet();
        updateMax(currentDryers, maxConcurrentDryers, current);
    }

    /**
     * Record that a dryer was released.
     */
    public void dryerReleased() {
        currentDryers.decrementAndGet();
    }

    /**
     * Record that a kiosk was acquired.
     */
    public void kioskAcquired() {
        currentKiosks.incrementAndGet();
    }

    /**
     * Record that a kiosk was released.
     */
    public void kioskReleased() {
        currentKiosks.decrementAndGet();
    }

    /**
     * Record a washing machine failure.
     */
    public void recordWasherFailure() {
        washerFailures.incrementAndGet();
    }

    /**
     * Record a payment kiosk failure.
     */
    public void recordPaymentFailure() {
        paymentFailures.incrementAndGet();
    }

    /**
     * Update running maximum using compare-and-set loop.
     */
    private void updateMax(AtomicInteger current, AtomicInteger max, int newValue) {
        // Use a CAS loop for thread-safe max update
        while (true) {
            int currentMax = max.get();
            if (newValue <= currentMax) {
                break;
            }
            if (max.compareAndSet(currentMax, newValue)) {
                break;
            }
        }
    }

    // Getters
    public int getCustomersArrived() { return customersArrived.get(); }
    public int getCustomersServed() { return customersServed.get(); }
    public int getCurrentWashers() { return currentWashers.get(); }
    public int getMaxConcurrentWashers() { return maxConcurrentWashers.get(); }
    public int getCurrentDryers() { return currentDryers.get(); }
    public int getMaxConcurrentDryers() { return maxConcurrentDryers.get(); }
    public int getCurrentKiosks() { return currentKiosks.get(); }
    public int getWasherFailures() { return washerFailures.get(); }
    public int getPaymentFailures() { return paymentFailures.get(); }
    
    public double getAverageCompletionTimeMs() {
        int served = customersServed.get();
        return served == 0 ? 0 : (double) totalCompletionTimeMs.get() / served;
    }

    /**
     * Print final statistics report.
     */
    public void printReport() {
        Logger.log("\n=== FINAL STATISTICS REPORT ===");
        Logger.log("Customers Arrived: " + getCustomersArrived());
        Logger.log("Customers Served: " + getCustomersServed());
        Logger.log("Average Completion Time: " + 
                  TimeUtil.formatDuration((long) getAverageCompletionTimeMs()));
        Logger.log("Max Concurrent Washers: " + getMaxConcurrentWashers() + "/6");
        Logger.log("Max Concurrent Dryers: " + getMaxConcurrentDryers() + "/4");
        Logger.log("Washer Failures: " + getWasherFailures());
        Logger.log("Payment Failures: " + getPaymentFailures());
        Logger.log("================================\n");
    }
}