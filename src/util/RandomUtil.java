package util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Centralized random generation for all simulation timing and failures.
 * Uses ThreadLocalRandom for thread-safe random generation.
 */
public final class RandomUtil {
    private RandomUtil() {}

    /**
     * Generate random wash duration: 4-6 seconds.
     */
    public static int washDurationMillis() {
        return ThreadLocalRandom.current().nextInt(4000, 6000 + 1);
    }

    /**
     * Generate random dry duration: 3-5 seconds.
     */
    public static int dryDurationMillis() {
        return ThreadLocalRandom.current().nextInt(3000, 5000 + 1);
    }

    /**
     * Generate random payment duration: 1-2 seconds.
     */
    public static int paymentDurationMillis() {
        return ThreadLocalRandom.current().nextInt(1000, 2000 + 1);
    }

    /**
     * Generate random arrival gap: 0-3 seconds.
     */
    public static int arrivalGapMillis() {
        return ThreadLocalRandom.current().nextInt(0, 3000 + 1);
    }

    /**
     * Determine if a failure should occur with given percentage.
     * @param percent Chance of failure (e.g., 5 = 5% chance)
     */
    public static boolean shouldFail(int percent) {
        return ThreadLocalRandom.current().nextInt(100) < percent;
    }
}