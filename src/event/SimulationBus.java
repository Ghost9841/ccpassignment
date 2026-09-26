package event;

import util.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple event bus for simulation events.
 * Thread-safe using CopyOnWriteArrayList.
 */
public class SimulationBus {
    private final List<SimulationListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(SimulationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SimulationListener listener) {
        listeners.remove(listener);
    }

    public void machineBusy(MachineType type, int machineId, String customerName) {
        for (SimulationListener listener : listeners) {
            try {
                listener.onMachineBusy(type, machineId, customerName);
            } catch (Exception e) {
                Logger.log("Error in listener: " + e.getMessage());
            }
        }
    }

    public void machineIdle(MachineType type, int machineId) {
        for (SimulationListener listener : listeners) {
            try {
                listener.onMachineIdle(type, machineId);
            } catch (Exception e) {
                Logger.log("Error in listener: " + e.getMessage());
            }
        }
    }

    public void machineFailed(MachineType type, int machineId, int customerId) {
        for (SimulationListener listener : listeners) {
            try {
                listener.onMachineFailed(type, machineId, customerId);
            } catch (Exception e) {
                Logger.log("Error in listener: " + e.getMessage());
            }
        }
    }
}