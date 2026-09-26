package event;

/**
 * Observer interface for real-time simulation events.
 * Implemented by GUI components to decouple simulation logic from Swing UI.
 */
public interface SimulationListener {
    /**
     * Fired when a machine is acquired and begins processing a customer.
     */
    void onMachineBusy(MachineType type, int machineId, int customerId, int durationMs);

    /**
     * Fired when a customer completes their cycle and releases the machine.
     */
    void onMachineIdle(MachineType type, int machineId);

    /**
     * Fired when a machine encounters a mid-cycle failure.
     */
    void onMachineFailed(MachineType type, int machineId, int customerId);

    /**
     * Fired when the waiting queue for a resource type changes.
     */
    void onQueueLengthChanged(MachineType type, int queueLength);

    /**
     * Fired when congestion chaos triggers (e.g. bonus scenario where payment queue >= 30).
     */
    void onCongestionAlert(String message, int queueLength);

    /**
     * Fired when a new customer arrives at the facility.
     */
    void onCustomerArrival(int customerId, int totalArrived);

    /**
     * Fired when a customer finishes all stages and departs.
     */
    void onCustomerCompleted(int customerId, int totalServed, long durationMs);
}