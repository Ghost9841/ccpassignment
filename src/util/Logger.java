package util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Centralized thread-safe logging utility.
 */
public final class Logger {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss.SSS");
    private static StringBuilder logBuffer = new StringBuilder();

    private Logger() {}

    /**
     * Log a message with timestamp and thread name.
     * Thread-safe through synchronized method.
     */
    public static synchronized void log(String message) {
        String timestamp = SDF.format(new Date());
        String threadName = Thread.currentThread().getName();
        String logLine = String.format("[%s][%s] %s", timestamp, threadName, message);
        
        System.out.println(logLine);
        logBuffer.append(logLine).append("\n");
        
        // Keep buffer manageable
        if (logBuffer.length() > 100000) {
            logBuffer = new StringBuilder(logBuffer.substring(50000));
        }
    }

    /**
     * Get the complete log history.
     */
    public static synchronized String getLogHistory() {
        return logBuffer.toString();
    }

    /**
     * Clear the log buffer.
     */
    public static synchronized void clearLog() {
        logBuffer = new StringBuilder();
    }
}