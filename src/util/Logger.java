package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Centralized thread-safe logging utility for the simulation.
 * Outputs formatted logs with timestamps and thread names to console
 * and forwards them in real-time to registered GUI listeners.
 */
public final class Logger {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final StringBuilder logBuffer = new StringBuilder();
    private static final List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

    private Logger() {}

    /**
     * Register a listener (e.g. GUI log viewer) to receive log lines.
     */
    public static void addListener(Consumer<String> listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregister a log listener.
     */
    public static void removeListener(Consumer<String> listener) {
        listeners.remove(listener);
    }

    /**
     * Thread-safe log method with timestamp and current thread name.
     */
    public static synchronized void log(String message) {
        String timestamp = SDF.format(new Date());
        String threadName = Thread.currentThread().getName();
        String logLine = String.format("[%s][%s] %s", timestamp, threadName, message);

        // 1. Output to console
        System.out.println(logLine);

        // 2. Buffer in memory
        logBuffer.append(logLine).append("\n");
        if (logBuffer.length() > 200000) {
            logBuffer.delete(0, 100000);
        }

        // 3. Dispatch to GUI listeners
        for (Consumer<String> listener : listeners) {
            try {
                listener.accept(logLine);
            } catch (Exception ignored) {
                // Safeguard against any faulty listener
            }
        }
    }

    /**
     * Get the full buffered log history.
     */
    public static synchronized String getLogHistory() {
        return logBuffer.toString();
    }

    /**
     * Clear the log buffer.
     */
    public static synchronized void clearLog() {
        logBuffer.setLength(0);
    }
}