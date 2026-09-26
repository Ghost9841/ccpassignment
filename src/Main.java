import customer.Customer;
import event.SimulationBus;
import facility.LaundryFacility;
import gui.LaundryFrame;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import manager.ResourceManager;
import manager.StatisticsManager;
import util.Logger;
import util.RandomUtil;
import util.TimeUtil;

/**
 * Main entry point for the Smart Laundry Facility Simulation.
 * Creates and wires all components, manages the simulation lifecycle.
 */
public class Main {
    // 50 customers total - matching assignment requirements
    private static final int TOTAL_CUSTOMERS = 50;
    // Run simulation for approximately 60 seconds
    private static final long SIMULATION_DURATION_MS = 60000;
    // Arrival gap: 0-3 seconds as specified
    private static final int MAX_ARRIVAL_GAP_MS = 3000;

    public static void main(String[] args) {
        Logger.log("=== Smart Laundry Facility Simulation Starting ===");
        
        // 1. Create shared objects
        LaundryFacility facility = new LaundryFacility();
        StatisticsManager stats = new StatisticsManager();
        SimulationBus eventBus = new SimulationBus();
        ResourceManager resourceManager = new ResourceManager(facility, stats, eventBus);
        
        // 2. Build and show GUI on EDT
        LaundryFrame gui = new LaundryFrame(stats, resourceManager);
        eventBus.addListener(gui);
        
        // 3. Create executor for customer threads
        ExecutorService executor = Executors.newCachedThreadPool();
        
        // 4. Start simulation with arrival loop
        long startTime = System.currentTimeMillis();
        int customerCount = 0;
        
        Logger.log("Starting customer arrivals...");
        
        // Time-driven approach: run for ~60 seconds or until 50 customers
        while (customerCount < TOTAL_CUSTOMERS && 
               (System.currentTimeMillis() - startTime) < SIMULATION_DURATION_MS) {
            
            // Create and submit customer
            int customerId = ++customerCount;
            Customer customer = new Customer(customerId, resourceManager, stats, eventBus);
            executor.execute(customer);
            
            // Random arrival gap: 0-3 seconds
            try {
                int gap = RandomUtil.arrivalGapMillis();
                Thread.sleep(gap);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        Logger.log("All " + customerCount + " customers have been created/arrived");
        
        // 5. Graceful shutdown
        executor.shutdown();
        try {
            // Wait for all customers to complete (with timeout)
            if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // 6. Print final statistics
        long totalTime = System.currentTimeMillis() - startTime;
        Logger.log("\n=== SIMULATION COMPLETE ===");
        Logger.log("Total runtime: " + TimeUtil.formatDuration(totalTime));
        stats.printReport();
        
        // 7. Update GUI with final stats
        gui.simulationComplete();
        
        Logger.log("=== Program Exiting ===");
    }
}