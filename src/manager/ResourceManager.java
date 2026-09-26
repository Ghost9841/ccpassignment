package manager;

import facility.LaundryFacility;
import facility.WashingMachine;
import facility.Dryer;
import facility.PaymentKiosk;
import event.SimulationBus;
import event.MachineType;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * Manages all shared resources (washers, dryers, kiosks) with thread-safe access.
 * Uses semaphores to limit concurrent access and queues for specific machine tracking.
 */
public class ResourceManager {
    private final Semaphore washerSemaphore;
    private final Semaphore dryerSemaphore;
    private final Semaphore kioskSemaphore;

    private final ConcurrentLinkedQueue<WashingMachine> availableWashers;
    private final ConcurrentLinkedQueue<Dryer> availableDryers;
    private final ConcurrentLinkedQueue<PaymentKiosk> availableKiosks;

    private final StatisticsManager stats;
    private final SimulationBus eventBus;

    public ResourceManager(LaundryFacility facility, StatisticsManager stats, 
                           SimulationBus eventBus) {
        this.stats = stats;
        this.eventBus = eventBus;

        // Initialize semaphores with resource counts (fair = true for FIFO)
        this.washerSemaphore = new Semaphore(LaundryFacility.WASHER_COUNT, true);
        this.dryerSemaphore = new Semaphore(LaundryFacility.DRYER_COUNT, true);
        this.kioskSemaphore = new Semaphore(LaundryFacility.KIOSK_COUNT, true);

        // Initialize available queues with all machines
        this.availableWashers = new ConcurrentLinkedQueue<>(facility.getWashers());
        this.availableDryers = new ConcurrentLinkedQueue<>(facility.getDryers());
        this.availableKiosks = new ConcurrentLinkedQueue<>(facility.getKiosks());
    }

    /**
     * Acquire a washing machine - blocks if none available.
     */
    public WashingMachine acquireWasher() throws InterruptedException {
        washerSemaphore.acquire();
        WashingMachine washer = availableWashers.poll();
        if (washer == null) {
            // Should never happen if semaphore is correct
            washerSemaphore.release();
            throw new IllegalStateException("No washer available despite semaphore permit");
        }
        stats.washerAcquired();
        eventBus.machineBusy(MachineType.WASHER, washer.getId(), 
                            Thread.currentThread().getName());
        return washer;
    }

    /**
     * Release a washing machine back to the pool.
     */
    public void releaseWasher(WashingMachine washer) {
        if (washer != null) {
            availableWashers.offer(washer);
            stats.washerReleased();
            washerSemaphore.release();
        }
    }

    /**
     * Acquire a dryer - blocks if none available.
     */
    public Dryer acquireDryer() throws InterruptedException {
        dryerSemaphore.acquire();
        Dryer dryer = availableDryers.poll();
        if (dryer == null) {
            dryerSemaphore.release();
            throw new IllegalStateException("No dryer available despite semaphore permit");
        }
        stats.dryerAcquired();
        eventBus.machineBusy(MachineType.DRYER, dryer.getId(), 
                            Thread.currentThread().getName());
        return dryer;
    }

    /**
     * Release a dryer back to the pool.
     */
    public void releaseDryer(Dryer dryer) {
        if (dryer != null) {
            availableDryers.offer(dryer);
            stats.dryerReleased();
            dryerSemaphore.release();
        }
    }

    /**
     * Acquire a payment kiosk - blocks if none available.
     */
    public PaymentKiosk acquireKiosk() throws InterruptedException {
        kioskSemaphore.acquire();
        PaymentKiosk kiosk = availableKiosks.poll();
        if (kiosk == null) {
            kioskSemaphore.release();
            throw new IllegalStateException("No kiosk available despite semaphore permit");
        }
        stats.kioskAcquired();
        eventBus.machineBusy(MachineType.KIOSK, kiosk.getId(), 
                            Thread.currentThread().getName());
        return kiosk;
    }

    /**
     * Release a payment kiosk back to the pool.
     */
    public void releaseKiosk(PaymentKiosk kiosk) {
        if (kiosk != null) {
            availableKiosks.offer(kiosk);
            stats.kioskReleased();
            kioskSemaphore.release();
        }
    }

    // Getters for GUI
    public int getAvailableWashers() { return availableWashers.size(); }
    public int getAvailableDryers() { return availableDryers.size(); }
    public int getAvailableKiosks() { return availableKiosks.size(); }
    public int getWaitingWashers() { return washerSemaphore.getQueueLength(); }
    public int getWaitingDryers() { return dryerSemaphore.getQueueLength(); }
    public int getWaitingKiosks() { return kioskSemaphore.getQueueLength(); }
}