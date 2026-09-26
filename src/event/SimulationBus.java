package event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe event bus for publishing simulation events to observers (GUI).
 * Keeps simulation domain models decoupled from Swing GUI code.
 */
public class SimulationBus {
    private final List<SimulationListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(SimulationListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(SimulationListener listener) {
        listeners.remove(listener);
    }

    public void machineBusy(MachineType type, int machineId, int customerId, int durationMs) {
        for (SimulationListener listener : listeners) {
            try {
                listener.onMachineBusy(type, machineId, customerId, durationMs);
            } catch (Exception ignored) {}
        }
    }

    public void machineIdle(MachineType type, int machineId) {
        for (SimulationListener listener : listeners) {
            try {
                listener.onMachineIdle(type, machineId);
            } catch (Exception ignored) {}
        }
    }

    public void machineFailed(MachineType type, int machineId, int customerId) {
        for (SimulationListener listener : listeners) {
            try {
                listener.onMachineFailed(type, machineId, customerId);
            } catch (Exception ignored) {}
        }
    }

    public void queueUpdated(MachineType type, int queueLength) {
        for (SimulationListener listener : listeners) {
            try {
                listener.onQueueLengthChanged(type, queueLength);
            } catch (Exception ignored) {}
        }
    }

    public void congestionAlert(String message, int queueLength) {
        for (SimulationListener listener : listeners) {
            try {
                listener.onCongestionAlert(message, queueLength);
            } catch (Exception ignored) {}
        }
    }

    public void customerArrived(int customerId, int totalArrived) {
        for (SimulationListener listener : listeners) {
            try {
                listener.onCustomerArrival(customerId, totalArrived);
            } catch (Exception ignored) {}
        }
    }

    public void customerCompleted(int customerId, int totalServed, long durationMs) {
        for (SimulationListener listener : listeners) {
            try {
                listener.onCustomerCompleted(customerId, totalServed, durationMs);
            } catch (Exception ignored) {}
        }
    }
}