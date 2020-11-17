package com.cordovapluginfastcam.math;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class MathUtil {
    /**
     * Dynamically determines the machine parameters of the floating-point arithmetic.
     */
    private static class FPU {
        int RADIX = 2;
        int DIGITS = 53;
        int FLOAT_DIGITS = 24;
        int ROUND_STYLE = 2;
        int MACHEP = -52;
        int FLOAT_MACHEP = -23;
        int NEGEP = -53;
        int FLOAT_NEGEP = -24;
        float FLOAT_EPSILON = (float) Math.pow(2.0, FLOAT_MACHEP);
        double EPSILON = Math.pow(2.0, MACHEP);

        FPU() {
            double beta, betain, betah, a, b, ZERO, ONE, TWO, temp, tempa, temp1;
            int i, itemp;

            ONE = (double) 1;
            TWO = ONE + ONE;
            ZERO = ONE - ONE;

            a = ONE;
            temp1 = ONE;
            while (temp1 - ONE == ZERO) {
                a = a + a;
                temp = a + ONE;
                temp1 = temp - a;
            }
            b = ONE;
            itemp = 0;
            while (itemp == 0) {
                b = b + b;
                temp = a + b;
                itemp = (int) (temp - a);
            }
            RADIX = itemp;
            beta = (double) RADIX;

            DIGITS = 0;
            b = ONE;
            temp1 = ONE;
            while (temp1 - ONE == ZERO) {
                DIGITS = DIGITS + 1;
                b = b * beta;
                temp = b + ONE;
                temp1 = temp - b;
            }
            ROUND_STYLE = 0;
            betah = beta / TWO;
            temp = a + betah;
            if (temp - a != ZERO) {
                ROUND_STYLE = 1;
            }
            tempa = a + beta;
            temp = tempa + betah;
            if ((ROUND_STYLE == 0) && (temp - tempa != ZERO)) {
                ROUND_STYLE = 2;
            }

            NEGEP = DIGITS + 3;
            betain = ONE / beta;
            a = ONE;
            for (i = 0; i < NEGEP; i++) {
                a = a * betain;
            }
            b = a;
            temp = ONE - a;
            while (temp - ONE == ZERO) {
                a = a * beta;
                NEGEP = NEGEP - 1;
                temp = ONE - a;
            }
            NEGEP = -(NEGEP);

            MACHEP = -(DIGITS) - 3;
            a = b;
            temp = ONE + a;
            while (temp - ONE == ZERO) {
                a = a * beta;
                MACHEP = MACHEP + 1;
                temp = ONE + a;
            }
            EPSILON = a;
        }
    }

    /**
     * Dynamically determines the machine parameters of the floating-point arithmetic.
     */
    private static final FPU fpu = new FPU();

    /**
     * The machine precision for the double type, which is the difference between 1
     * and the smallest value greater than 1 that is representable for the double type.
     */
    public static final double EPSILON = fpu.EPSILON;
    /**
     * The machine precision for the float type, which is the difference between 1
     * and the smallest value greater than 1 that is representable for the float type.
     */
    public static final float FLOAT_EPSILON = fpu.FLOAT_EPSILON;

    /** Tests if a floating number is zero. */
    public static boolean isZero(float x) {
        return isZero(x, FLOAT_EPSILON);
    }

    /** Tests if a floating number is zero with given epsilon. */
    public static boolean isZero(float x, float epsilon) {
        return abs(x) < epsilon;
    }

    /** Tests if a floating number is zero. */
    public static boolean isZero(double x) {
        return isZero(x, EPSILON);
    }

    /** Tests if a floating number is zero with given epsilon. */
    public static boolean isZero(double x, double epsilon) {
        return abs(x) < epsilon;
    }

    /**
     * The Euclidean distance.
     */
    public static double distance(int[] x, int[] y) {
        return sqrt(squaredDistance(x, y));
    }

    /**
     * The Euclidean distance.
     */
    public static double distance(float[] x, float[] y) {
        return sqrt(squaredDistance(x, y));
    }

    /**
     * The Euclidean distance.
     */
    public static double distance(double[] x, double[] y) {
        return sqrt(squaredDistance(x, y));
    }

    /**
     * The squared Euclidean distance.
     */
    public static double squaredDistance(int[] x, int[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Input vector sizes are different.");
        }

        switch (x.length) {
            case 2: {
                int d0 = x[0] - y[0];
                int d1 = x[1] - y[1];
                return d0 * d0 + d1 * d1;
            }

            case 3: {
                int d0 = x[0] - y[0];
                int d1 = x[1] - y[1];
                int d2 = x[2] - y[2];
                return d0 * d0 + d1 * d1 + d2 * d2;
            }

            case 4: {
                int d0 = x[0] - y[0];
                int d1 = x[1] - y[1];
                int d2 = x[2] - y[2];
                int d3 = x[3] - y[3];
                return d0 * d0 + d1 * d1 + d2 * d2 + d3 * d3;
            }
        }

        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            int d = x[i] - y[i];
            sum += d * d;
        }

        return sum;
    }

    /**
     * The squared Euclidean distance.
     */
    public static double squaredDistance(float[] x, float[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Input vector sizes are different.");
        }

        switch (x.length) {
            case 2: {
                double d0 = (double) x[0] - (double) y[0];
                double d1 = (double) x[1] - (double) y[1];
                return d0 * d0 + d1 * d1;
            }

            case 3: {
                double d0 = (double) x[0] - (double) y[0];
                double d1 = (double) x[1] - (double) y[1];
                double d2 = (double) x[2] - (double) y[2];
                return d0 * d0 + d1 * d1 + d2 * d2;
            }

            case 4: {
                double d0 = (double) x[0] - (double) y[0];
                double d1 = (double) x[1] - (double) y[1];
                double d2 = (double) x[2] - (double) y[2];
                double d3 = (double) x[3] - (double) y[3];
                return d0 * d0 + d1 * d1 + d2 * d2 + d3 * d3;
            }
        }

        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            // covert x and y for better precision
            double d = (double) x[i] - (double) y[i];
            sum += d * d;
        }

        return sum;
    }

    /**
     * The squared Euclidean distance.
     */
    public static double squaredDistance(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Input vector sizes are different.");
        }

        switch (x.length) {
            case 2: {
                double d0 = x[0] - y[0];
                double d1 = x[1] - y[1];
                return d0 * d0 + d1 * d1;
            }

            case 3: {
                double d0 = x[0] - y[0];
                double d1 = x[1] - y[1];
                double d2 = x[2] - y[2];
                return d0 * d0 + d1 * d1 + d2 * d2;
            }

            case 4: {
                double d0 = x[0] - y[0];
                double d1 = x[1] - y[1];
                double d2 = x[2] - y[2];
                double d3 = x[3] - y[3];
                return d0 * d0 + d1 * d1 + d2 * d2 + d3 * d3;
            }
        }

        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            double d = x[i] - y[i];
            sum += d * d;
        }

        return sum;
    }
}
