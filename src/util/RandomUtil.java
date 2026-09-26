package util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Centralized random generation for all simulation timing and failure probabilities.
 * Uses ThreadLocalRandom for thread-safe concurrent performance.
 */
public final class RandomUtil {
    private RandomUtil() {}

    /**
     * Generate random wash duration: 4-6 seconds inclusive (4000 - 6000 ms).
     */
    public static int washDurationMillis() {
        return ThreadLocalRandom.current().nextInt(4000, 6000 + 1);
    }

    /**
     * Generate random dry duration: 3-5 seconds inclusive (3000 - 5000 ms).
     */
    public static int dryDurationMillis() {
        return ThreadLocalRandom.current().nextInt(3000, 5000 + 1);
    }

    /**
     * Generate random payment duration: 1-2 seconds inclusive (1000 - 2000 ms).
     */
    public static int paymentDurationMillis() {
        return ThreadLocalRandom.current().nextInt(1000, 2000 + 1);
    }

    /**
     * Generate random customer arrival gap: 0-3 seconds inclusive (0 - 3000 ms).
     */
    public static int arrivalGapMillis() {
        return ThreadLocalRandom.current().nextInt(0, 3000 + 1);
    }

    /**
     * Determine if a failure should occur with given percentage.
     * @param percent Probability in percent (e.g. 5 = 5% chance)
     * @return true if failure triggered, false otherwise
     */
    public static boolean shouldFail(int percent) {
        if (percent <= 0) return false;
        if (percent >= 100) return true;
        return ThreadLocalRandom.current().nextInt(100) < percent;
    }
}