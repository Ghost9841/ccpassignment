package manager;

import util.Logger;
import util.TimeUtil;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe statistics collector for the simulation.
 * Uses atomic variables to ensure safe concurrent updates across customer threads.
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
    private final AtomicInteger maxConcurrentKiosks = new AtomicInteger(0);
    
    // Failure counters
    private final AtomicInteger washerFailures = new AtomicInteger(0);
    private final AtomicInteger paymentFailures = new AtomicInteger(0);

    /**
     * Record a customer arrival at the entrance gate.
     */
    public void recordArrival() {
        customersArrived.incrementAndGet();
    }

    /**
     * Record a customer completion with total turnaround time.
     */
    public void recordCompletion(long timeMs) {
        customersServed.incrementAndGet();
        totalCompletionTimeMs.addAndGet(timeMs);
    }

    /**
     * Record that a washer was acquired (update current and peak).
     */
    public void washerAcquired() {
        int current = currentWashers.incrementAndGet();
        updateMax(maxConcurrentWashers, current);
    }

    /**
     * Record that a washer was released.
     */
    public void washerReleased() {
        currentWashers.decrementAndGet();
    }

    /**
     * Record that a dryer was acquired (update current and peak).
     */
    public void dryerAcquired() {
        int current = currentDryers.incrementAndGet();
        updateMax(maxConcurrentDryers, current);
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
        int current = currentKiosks.incrementAndGet();
        updateMax(maxConcurrentKiosks, current);
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
     * Update running maximum using thread-safe compare-and-set loop.
     */
    private void updateMax(AtomicInteger maxHolder, int newValue) {
        while (true) {
            int currentMax = maxHolder.get();
            if (newValue <= currentMax) {
                break;
            }
            if (maxHolder.compareAndSet(currentMax, newValue)) {
                break;
            }
        }
    }

    // Reset statistics for new runs
    public void reset() {
        customersArrived.set(0);
        customersServed.set(0);
        totalCompletionTimeMs.set(0);
        currentWashers.set(0);
        maxConcurrentWashers.set(0);
        currentDryers.set(0);
        maxConcurrentDryers.set(0);
        currentKiosks.set(0);
        maxConcurrentKiosks.set(0);
        washerFailures.set(0);
        paymentFailures.set(0);
    }

    // Getters for live statistics
    public int getCustomersArrived() { return customersArrived.get(); }
    public int getCustomersServed() { return customersServed.get(); }
    public int getCurrentWashers() { return currentWashers.get(); }
    public int getMaxConcurrentWashers() { return maxConcurrentWashers.get(); }
    public int getCurrentDryers() { return currentDryers.get(); }
    public int getMaxConcurrentDryers() { return maxConcurrentDryers.get(); }
    public int getCurrentKiosks() { return currentKiosks.get(); }
    public int getMaxConcurrentKiosks() { return maxConcurrentKiosks.get(); }
    public int getWasherFailures() { return washerFailures.get(); }
    public int getPaymentFailures() { return paymentFailures.get(); }
    
    public double getAverageCompletionTimeMs() {
        int served = customersServed.get();
        return served == 0 ? 0 : (double) totalCompletionTimeMs.get() / served;
    }

    /**
     * Print final statistics report in console and logs.
     */
    public void printReport() {
        Logger.log("\n==================================================");
        Logger.log("           FINAL SIMULATION STATISTICS REPORT      ");
        Logger.log("==================================================");
        Logger.log(String.format("Customers Arrived           : %d", getCustomersArrived()));
        Logger.log(String.format("Customers Served            : %d", getCustomersServed()));
        Logger.log(String.format("Completion Rate             : %.1f%%", 
                getCustomersArrived() == 0 ? 0.0 : (getCustomersServed() * 100.0 / getCustomersArrived())));
        Logger.log(String.format("Average Turnaround Time     : %s", 
                TimeUtil.formatDuration((long) getAverageCompletionTimeMs())));
        Logger.log(String.format("Max Concurrent Washers Used : %d / 6", getMaxConcurrentWashers()));
        Logger.log(String.format("Max Concurrent Dryers Used  : %d / 4", getMaxConcurrentDryers()));
        Logger.log(String.format("Max Concurrent Kiosks Used  : %d / 2", getMaxConcurrentKiosks()));
        Logger.log(String.format("Total Washer Failures       : %d", getWasherFailures()));
        Logger.log(String.format("Total Payment Failures      : %d", getPaymentFailures()));
        Logger.log("==================================================\n");
    }
}