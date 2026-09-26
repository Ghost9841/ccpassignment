package customer;

import manager.ResourceManager;
import manager.StatisticsManager;
import event.SimulationBus;
import event.MachineType;
import facility.WashingMachine;
import facility.Dryer;
import facility.PaymentKiosk;
import exception.MachineFailureException;
import exception.PaymentFailureException;
import util.Logger;
import util.RandomUtil;
import util.TimeUtil;

/**
 * Represents a single customer as a runnable thread.
 * Each customer goes through: Wash -> Dry -> Pay lifecycle.
 */
public class Customer implements Runnable {
    private final int id;
    private final ResourceManager resourceManager;
    private final StatisticsManager stats;
    private final SimulationBus eventBus;
    private final long arrivalTime;
    private static final int RETRY_DELAY_MS = 2000;

    public Customer(int id, ResourceManager resourceManager, 
                    StatisticsManager stats, SimulationBus eventBus) {
        this.id = id;
        this.resourceManager = resourceManager;
        this.stats = stats;
        this.eventBus = eventBus;
        this.arrivalTime = System.currentTimeMillis();
        stats.recordArrival();
        Thread.currentThread().setName("Customer-" + id);
    }

    @Override
    public void run() {
        Logger.log("Customer-" + id + " arrived at " + TimeUtil.formatTime(arrivalTime));
        
        try {
            // 1. WASH stage (with retry on failure)
            wash();
            
            // 2. DRY stage (no failure)
            dry();
            
            // 3. PAY stage (with retry on failure)
            pay();
            
            // 4. Complete
            long completionTime = System.currentTimeMillis();
            long totalTime = completionTime - arrivalTime;
            stats.recordCompletion(totalTime);
            
            Logger.log("Customer-" + id + " completed! Total time: " + 
                      TimeUtil.formatDuration(totalTime));
            
        } catch (InterruptedException e) {
            Logger.log("Customer-" + id + " was interrupted");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Washing stage with retry on failure.
     */
    private void wash() throws InterruptedException {
        boolean success = false;
        WashingMachine washer = null;
        
        while (!success) {
            try {
                // Acquire washer (blocks if none available)
                washer = resourceManager.acquireWasher();
                
                // Perform wash
                int durationMs = RandomUtil.washDurationMillis();
                Logger.log("Customer-" + id + " started washing on " + 
                          washer.getClass().getSimpleName() + "-" + washer.getId() + 
                          " for " + TimeUtil.formatDuration(durationMs));
                
                // Simulate washing
                try {
                    Thread.sleep(durationMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
                
                // Check for random failure (5% chance)
                if (RandomUtil.shouldFail(5)) {
                    Logger.log("Customer-" + id + " washing FAILED on " + 
                              washer.getClass().getSimpleName() + "-" + washer.getId());
                    stats.recordWasherFailure();
                    eventBus.machineFailed(MachineType.WASHER, washer.getId(), id);
                    throw new MachineFailureException("Washing machine failed");
                }
                
                // Success!
                Logger.log("Customer-" + id + " completed washing on " + 
                          washer.getClass().getSimpleName() + "-" + washer.getId());
                success = true;
                
            } catch (MachineFailureException e) {
                // Failure occurred, retry
                Logger.log("Customer-" + id + " will retry washing after " + 
                          RETRY_DELAY_MS + "ms");
                Thread.sleep(RETRY_DELAY_MS);
            } finally {
                // Always release the washer
                if (washer != null) {
                    resourceManager.releaseWasher(washer);
                    eventBus.machineIdle(MachineType.WASHER, washer.getId());
                }
            }
        }
    }

    /**
     * Drying stage (no failure).
     */
    private void dry() throws InterruptedException {
        Dryer dryer = null;
        
        try {
            // Acquire dryer (blocks if none available)
            dryer = resourceManager.acquireDryer();
            
            // Perform drying
            int durationMs = RandomUtil.dryDurationMillis();
            Logger.log("Customer-" + id + " started drying on " + 
                      dryer.getClass().getSimpleName() + "-" + dryer.getId() + 
                      " for " + TimeUtil.formatDuration(durationMs));
            
            // Simulate drying
            Thread.sleep(durationMs);
            
            Logger.log("Customer-" + id + " completed drying on " + 
                      dryer.getClass().getSimpleName() + "-" + dryer.getId());
            
        } finally {
            // Always release the dryer
            if (dryer != null) {
                resourceManager.releaseDryer(dryer);
                eventBus.machineIdle(MachineType.DRYER, dryer.getId());
            }
        }
    }

    /**
     * Payment stage with retry on failure.
     */
    private void pay() throws InterruptedException {
        boolean success = false;
        PaymentKiosk kiosk = null;
        
        while (!success) {
            try {
                // Acquire kiosk (blocks if none available)
                kiosk = resourceManager.acquireKiosk();
                
                // Process payment
                int durationMs = RandomUtil.paymentDurationMillis();
                Logger.log("Customer-" + id + " started payment at " + 
                          kiosk.getClass().getSimpleName() + "-" + kiosk.getId() + 
                          " for " + TimeUtil.formatDuration(durationMs));
                
                // Simulate payment
                try {
                    Thread.sleep(durationMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
                
                // Check for random failure (5% chance)
                if (RandomUtil.shouldFail(5)) {
                    Logger.log("Customer-" + id + " payment FAILED at " + 
                              kiosk.getClass().getSimpleName() + "-" + kiosk.getId());
                    stats.recordPaymentFailure();
                    eventBus.machineFailed(MachineType.KIOSK, kiosk.getId(), id);
                    throw new PaymentFailureException("Payment kiosk failed");
                }
                
                // Success!
                Logger.log("Customer-" + id + " completed payment at " + 
                          kiosk.getClass().getSimpleName() + "-" + kiosk.getId());
                success = true;
                
            } catch (PaymentFailureException e) {
                // Failure occurred, retry
                Logger.log("Customer-" + id + " will retry payment after " + 
                          RETRY_DELAY_MS + "ms");
                Thread.sleep(RETRY_DELAY_MS);
            } finally {
                // Always release the kiosk
                if (kiosk != null) {
                    resourceManager.releaseKiosk(kiosk);
                    eventBus.machineIdle(MachineType.KIOSK, kiosk.getId());
                }
            }
        }
    }

    public int getId() {
        return id;
    }
}