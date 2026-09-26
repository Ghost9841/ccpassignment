import customer.Customer;
import event.SimulationBus;
import facility.LaundryFacility;
import gui.LaundryFrame;
import manager.ResourceManager;
import manager.StatisticsManager;
import util.Logger;
import util.RandomUtil;
import util.TimeUtil;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main Controller & Entry Point for the Smart Laundry Facility Simulation.
 * Coordinates multi-threaded customer arrivals, shared resources, GUI observer,
 * and graceful completion tracking.
 */
public class Main {
    public static final int TOTAL_CUSTOMERS = 50;

    private static LaundryFacility facility;
    private static StatisticsManager stats;
    private static SimulationBus eventBus;
    private static ResourceManager resourceManager;
    private static LaundryFrame gui;
    private static volatile boolean simulationActive = false;

    public static void main(String[] args) {
        // Set cross-platform or system Look and Feel for clean modern styling
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        Logger.log("=== Initializing Smart Laundry Facility Simulation ===");

        // 1. Initialize shared domain models
        facility = new LaundryFacility();
        stats = new StatisticsManager();
        eventBus = new SimulationBus();
        resourceManager = new ResourceManager(facility, stats, eventBus);

        // 2. Launch GUI safely on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            gui = new LaundryFrame(stats, resourceManager);
            eventBus.addListener(gui);

            // Wire UI buttons to trigger simulations
            gui.setSimulationTriggers(
                    () -> runSimulationAsync(false), // Normal 50-customer simulation
                    () -> runSimulationAsync(true)   // Bonus Congestion Chaos scenario
            );

            // Automatically launch normal simulation on program start
            runSimulationAsync(false);
        });
    }

    /**
     * Executes the simulation on a dedicated background supervisor thread
     * so that the Swing EDT remains completely responsive.
     */
    private static synchronized void runSimulationAsync(boolean chaosMode) {
        if (simulationActive) {
            Logger.log("Simulation is already currently in progress.");
            return;
        }
        simulationActive = true;

        Thread supervisor = new Thread(() -> {
            try {
                executeSimulation(chaosMode);
            } finally {
                simulationActive = false;
            }
        }, "SimulationSupervisor");
        supervisor.start();
    }

    /**
     * Core simulation workflow:
     * Resets metrics, starts clock, dispatches 50 customer threads,
     * and awaits full completion.
     */
    private static void executeSimulation(boolean chaosMode) {
        Logger.log("\n=======================================================");
        Logger.log(chaosMode ? ">>> STARTING BONUS CONGESTION CHAOS SIMULATION <<<"
                             : ">>> STARTING NORMAL 50-CUSTOMER SIMULATION <<<");
        Logger.log("=======================================================");

        // Reset state for new run
        stats.reset();
        resourceManager.reset();
        resourceManager.setChaosMode(chaosMode);

        gui.startSimulationClock();

        ExecutorService executor = Executors.newCachedThreadPool();
        long simStartTime = System.currentTimeMillis();

        Logger.log("Entry gate opened: 50 customers will arrive sequentially (0-3s interval)...");

        // Customer arrival loop: exactly 50 customers
        for (int i = 1; i <= TOTAL_CUSTOMERS; i++) {
            Customer customer = new Customer(i, resourceManager, stats, eventBus);
            executor.execute(customer);

            // Random arrival interval between 0 and 3 seconds
            try {
                int gap = RandomUtil.arrivalGapMillis();
                Thread.sleep(gap);
            } catch (InterruptedException e) {
                Logger.log("Customer arrival loop interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }

        Logger.log("All " + TOTAL_CUSTOMERS + " customers have arrived at the facility entrance.");
        Logger.log("Awaiting all customers to complete wash, dry, and payment stages...");

        // Graceful shutdown: cease accepting new arrivals and await in-flight customers
        executor.shutdown();
        try {
            // Generous timeout allowing retries to succeed cleanly
            if (!executor.awaitTermination(300, TimeUnit.SECONDS)) {
                Logger.log("Forcing shutdown after timeout...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Logger.log("Supervisor thread interrupted during awaitTermination");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        long totalSimDuration = System.currentTimeMillis() - simStartTime;

        Logger.log("\n=======================================================");
        Logger.log(">>> ALL CUSTOMERS PROCESSED - SIMULATION COMPLETE <<<");
        Logger.log("Total Execution Time: " + TimeUtil.formatDuration(totalSimDuration));
        Logger.log("=======================================================");

        // Print final statistics to console & GUI log
        stats.printReport();

        // Notify GUI of final state
        gui.simulationComplete();
    }
}