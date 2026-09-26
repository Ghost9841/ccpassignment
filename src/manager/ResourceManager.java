package manager;

import facility.LaundryFacility;
import facility.WashingMachine;
import facility.Dryer;
import facility.PaymentKiosk;
import event.SimulationBus;
import event.MachineType;
import util.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * Thread-safe manager for all shared laundromat resources (washers, dryers, kiosks).
 * Uses fair Counting Semaphores for strict FIFO access and ConcurrentLinkedQueues
 * for physical machine allocation.
 */
public class ResourceManager {
    private final LaundryFacility facility;
    private final StatisticsManager stats;
    private final SimulationBus eventBus;

    private Semaphore washerSemaphore;
    private Semaphore dryerSemaphore;
    private Semaphore kioskSemaphore;

    private ConcurrentLinkedQueue<WashingMachine> availableWashers;
    private ConcurrentLinkedQueue<Dryer> availableDryers;
    private ConcurrentLinkedQueue<PaymentKiosk> availableKiosks;

    // Bonus Congestion Chaos Scenario flags
    private volatile boolean chaosMode = false;
    private volatile boolean ownerIntervened = false;

    public ResourceManager(LaundryFacility facility, StatisticsManager stats, 
                           SimulationBus eventBus) {
        this.facility = facility;
        this.stats = stats;
        this.eventBus = eventBus;
        initResources();
    }

    private void initResources() {
        // Fair semaphores ensure First-Come-First-Served (FIFO) ordering for waiting threads
        this.washerSemaphore = new Semaphore(LaundryFacility.WASHER_COUNT, true);
        this.dryerSemaphore = new Semaphore(LaundryFacility.DRYER_COUNT, true);
        this.kioskSemaphore = new Semaphore(LaundryFacility.KIOSK_COUNT, true);

        this.availableWashers = new ConcurrentLinkedQueue<>(facility.getWashers());
        this.availableDryers = new ConcurrentLinkedQueue<>(facility.getDryers());
        this.availableKiosks = new ConcurrentLinkedQueue<>(facility.getKiosks());
    }

    public void reset() {
        this.chaosMode = false;
        this.ownerIntervened = false;
        initResources();
        if (eventBus != null) {
            for (WashingMachine w : facility.getWashers()) {
                eventBus.machineIdle(MachineType.WASHER, w.getId());
            }
            for (Dryer d : facility.getDryers()) {
                eventBus.machineIdle(MachineType.DRYER, d.getId());
            }
            for (PaymentKiosk k : facility.getKiosks()) {
                eventBus.machineIdle(MachineType.KIOSK, k.getId());
            }
            eventBus.queueUpdated(MachineType.WASHER, 0);
            eventBus.queueUpdated(MachineType.DRYER, 0);
            eventBus.queueUpdated(MachineType.KIOSK, 0);
        }
    }

    public void setChaosMode(boolean enabled) {
        this.chaosMode = enabled;
        this.ownerIntervened = false;
        if (enabled) {
            Logger.log("[CHAOS SCENARIO ACTIVATED] Payment kiosks will malfunction today! Chaos in the laundromat...");
        }
    }

    public boolean isChaosModeActive() {
        return chaosMode && !ownerIntervened;
    }

    // --- Washing Machine Allocation ---

    public WashingMachine acquireWasher(int customerId) throws InterruptedException {
        int queueLength = washerSemaphore.getQueueLength() + 1;
        if (eventBus != null) {
            eventBus.queueUpdated(MachineType.WASHER, queueLength);
        }

        washerSemaphore.acquire();

        if (eventBus != null) {
            eventBus.queueUpdated(MachineType.WASHER, washerSemaphore.getQueueLength());
        }

        WashingMachine washer = availableWashers.poll();
        if (washer == null) {
            washerSemaphore.release();
            throw new IllegalStateException("No washer available despite semaphore permit acquisition");
        }

        stats.washerAcquired();
        return washer;
    }

    public void releaseWasher(WashingMachine washer) {
        if (washer != null) {
            availableWashers.offer(washer);
            stats.washerReleased();
            if (eventBus != null) {
                eventBus.machineIdle(MachineType.WASHER, washer.getId());
            }
            washerSemaphore.release();
            if (eventBus != null) {
                eventBus.queueUpdated(MachineType.WASHER, washerSemaphore.getQueueLength());
            }
        }
    }

    // --- Dryer Allocation ---

    public Dryer acquireDryer(int customerId) throws InterruptedException {
        int queueLength = dryerSemaphore.getQueueLength() + 1;
        if (eventBus != null) {
            eventBus.queueUpdated(MachineType.DRYER, queueLength);
        }

        dryerSemaphore.acquire();

        if (eventBus != null) {
            eventBus.queueUpdated(MachineType.DRYER, dryerSemaphore.getQueueLength());
        }

        Dryer dryer = availableDryers.poll();
        if (dryer == null) {
            dryerSemaphore.release();
            throw new IllegalStateException("No dryer available despite semaphore permit acquisition");
        }

        stats.dryerAcquired();
        return dryer;
    }

    public void releaseDryer(Dryer dryer) {
        if (dryer != null) {
            availableDryers.offer(dryer);
            stats.dryerReleased();
            if (eventBus != null) {
                eventBus.machineIdle(MachineType.DRYER, dryer.getId());
            }
            dryerSemaphore.release();
            if (eventBus != null) {
                eventBus.queueUpdated(MachineType.DRYER, dryerSemaphore.getQueueLength());
            }
        }
    }

    // --- Payment Kiosk Allocation ---

    public PaymentKiosk acquireKiosk(int customerId) throws InterruptedException {
        int queueLength = kioskSemaphore.getQueueLength() + 1;
        if (eventBus != null) {
            eventBus.queueUpdated(MachineType.KIOSK, queueLength);
        }

        // Bonus Congestion check: If queue hits 30 in chaos mode
        if (chaosMode && !ownerIntervened && queueLength >= 30) {
            ownerIntervened = true;
            String alert = "CONGESTION ALERT: 30 customers waiting at Payment! Owner called in to resolve crisis and fix kiosks!";
            Logger.log("\n=======================================================");
            Logger.log("[CONGESTION CHAOS ALERT] " + alert);
            Logger.log("=======================================================\n");
            if (eventBus != null) {
                eventBus.congestionAlert(alert, queueLength);
            }
        }

        kioskSemaphore.acquire();

        if (eventBus != null) {
            eventBus.queueUpdated(MachineType.KIOSK, kioskSemaphore.getQueueLength());
        }

        PaymentKiosk kiosk = availableKiosks.poll();
        if (kiosk == null) {
            kioskSemaphore.release();
            throw new IllegalStateException("No kiosk available despite semaphore permit acquisition");
        }

        stats.kioskAcquired();
        return kiosk;
    }

    public void releaseKiosk(PaymentKiosk kiosk) {
        if (kiosk != null) {
            availableKiosks.offer(kiosk);
            stats.kioskReleased();
            if (eventBus != null) {
                eventBus.machineIdle(MachineType.KIOSK, kiosk.getId());
            }
            kioskSemaphore.release();
            if (eventBus != null) {
                eventBus.queueUpdated(MachineType.KIOSK, kioskSemaphore.getQueueLength());
            }
        }
    }

    // Queue query methods
    public int getAvailableWashers() { return availableWashers.size(); }
    public int getAvailableDryers() { return availableDryers.size(); }
    public int getAvailableKiosks() { return availableKiosks.size(); }
    public int getWaitingWashers() { return washerSemaphore.getQueueLength(); }
    public int getWaitingDryers() { return dryerSemaphore.getQueueLength(); }
    public int getWaitingKiosks() { return kioskSemaphore.getQueueLength(); }
}