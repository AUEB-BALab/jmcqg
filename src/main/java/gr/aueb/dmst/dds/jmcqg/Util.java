package gr.aueb.dmst.dds.jmcqg;

import java.util.Random;

public class Util {
    private static Random random = new Random();

    /** Seed the used random number generator */
    public static void seed(int seed) {
        random = new Random(seed);
    }

    /** @return a random integer between from (inclusive) and to (exclusive) */
    public static int randomInt(int from, int to) {
        return from + random.nextInt(to - from);
    }
}
