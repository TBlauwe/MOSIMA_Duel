package MOSIMA_Duel.Utility;

import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    public static double generateRandomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static boolean dice(float proba) {
        return ThreadLocalRandom.current().nextFloat() <= proba;
    }
}
