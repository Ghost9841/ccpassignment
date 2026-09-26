package util;

/**
 * Time formatting and conversion helpers for logs and GUI.
 */
public final class TimeUtil {
    private TimeUtil() {}

    /**
     * Format a duration in milliseconds to MM:SS or seconds string.
     */
    public static String formatDuration(long millis) {
        if (millis < 0) millis = 0;
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        
        if (minutes > 0) {
            return String.format("%02d:%02d", minutes, seconds);
        }
        return String.format("%.1fs", millis / 1000.0);
    }

    /**
     * Format a clock duration to mm:ss format.
     */
    public static String formatClock(long millis) {
        if (millis < 0) millis = 0;
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Format a timestamp into mm:ss.SSS.
     */
    public static String formatTime(long timestamp) {
        long seconds = (timestamp / 1000) % 60;
        long minutes = (timestamp / 1000 / 60) % 60;
        long millis = timestamp % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }
}