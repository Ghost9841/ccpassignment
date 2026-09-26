package util;

/**
 * Time formatting and conversion utilities.
 */
public final class TimeUtil {
    private TimeUtil() {}

    /**
     * Format a duration in milliseconds to HH:MM:SS format.
     */
    public static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        }
        return seconds + "s";
    }

    /**
     * Format a timestamp to HH:mm:ss.SSS.
     */
    public static String formatTime(long timestamp) {
        long seconds = (timestamp / 1000) % 60;
        long minutes = (timestamp / 1000 / 60) % 60;
        long millis = timestamp % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }
}