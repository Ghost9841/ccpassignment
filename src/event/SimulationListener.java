package event;

public interface SimulationListener {
    void onMachineBusy(MachineType type, int machineId, String customerName);
    void onMachineIdle(MachineType type, int machineId);
    void onMachineFailed(MachineType type, int machineId, int customerId);
}