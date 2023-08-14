public class CeilAndFloorTests {
    private static int testCeilCase(double input, double expected) {
        int failures = 0;
        failures += Tests.test("Math.ceil",  input, Math.ceil(input),   expected);
        failures += Tests.test("StrictMath.ceil",  input, StrictMath.ceil(input), expected);
        return failures;
    }
    private static int testFloorCase(double input, double expected) {
        int failures = 0;
        failures += Tests.test("Math.floor",  input, Math.floor(input),   expected);
        failures += Tests.test("StrictMath.floor",  input, StrictMath.floor(input), expected);
        return failures;
    }
    private static int nearIntegerTests() {
        int failures = 0;
        double [] fixedPoints = {
            -0.0,
             0.0,
            -1.0,
             1.0,
            -0x1.0p52,
             0x1.0p52,
            -Double.MAX_VALUE,
             Double.MAX_VALUE,
             Double.NEGATIVE_INFINITY,
             Double.POSITIVE_INFINITY,
             Double.NaN,
        };
        for(double fixedPoint : fixedPoints) {
            failures += testCeilCase(fixedPoint, fixedPoint);
            failures += testFloorCase(fixedPoint, fixedPoint);
        }
        for(int i = Double.MIN_EXPONENT; i <= Double.MAX_EXPONENT; i++) {
            double powerOfTwo   = Math.scalb(1.0, i);
            double neighborDown = FpUtils.nextDown(powerOfTwo);
            double neighborUp   = Math.nextUp(powerOfTwo);
            if (i < 0) {
                failures += testCeilCase( powerOfTwo,  1.0);
                failures += testCeilCase(-powerOfTwo, -0.0);
                failures += testFloorCase( powerOfTwo,  0.0);
                failures += testFloorCase(-powerOfTwo, -1.0);
                failures += testCeilCase( neighborDown, 1.0);
                failures += testCeilCase(-neighborDown, -0.0);
                failures += testFloorCase( neighborUp,  0.0);
                failures += testFloorCase(-neighborUp, -1.0);
            } else {
                failures += testCeilCase(powerOfTwo, powerOfTwo);
                failures += testFloorCase(powerOfTwo, powerOfTwo);
                if (neighborDown==Math.rint(neighborDown)) {
                    failures += testCeilCase( neighborDown,  neighborDown);
                    failures += testCeilCase(-neighborDown, -neighborDown);
                    failures += testFloorCase( neighborDown, neighborDown);
                    failures += testFloorCase(-neighborDown,-neighborDown);
                } else {
                    failures += testCeilCase( neighborDown, powerOfTwo);
                    failures += testFloorCase(-neighborDown, -powerOfTwo);
                }
                if (neighborUp==Math.rint(neighborUp)) {
                    failures += testCeilCase(neighborUp, neighborUp);
                    failures += testCeilCase(-neighborUp, -neighborUp);
                    failures += testFloorCase(neighborUp, neighborUp);
                    failures += testFloorCase(-neighborUp, -neighborUp);
                } else {
                    failures += testFloorCase(neighborUp, powerOfTwo);
                    failures += testCeilCase(-neighborUp, -powerOfTwo);
                }
            }
        }
        for(int i = -(0x10000); i <= 0x10000; i++) {
            double d = (double) i;
            double neighborDown = FpUtils.nextDown(d);
            double neighborUp   = Math.nextUp(d);
            failures += testCeilCase( d, d);
            failures += testCeilCase(-d, -d);
            failures += testFloorCase( d, d);
            failures += testFloorCase(-d, -d);
            if (Math.abs(d) > 1.0) {
                failures += testCeilCase( neighborDown, d);
                failures += testCeilCase(-neighborDown, -d+1);
                failures += testFloorCase( neighborUp, d);
                failures += testFloorCase(-neighborUp, -d-1);
            }
        }
        return failures;
    }
    public static int roundingTests() {
        int failures = 0;
        double [][] testCases = {
            { Double.MIN_VALUE,                           1.0},
            {-Double.MIN_VALUE,                          -0.0},
            { FpUtils.nextDown(DoubleConsts.MIN_NORMAL),  1.0},
            {-FpUtils.nextDown(DoubleConsts.MIN_NORMAL), -0.0},
            { DoubleConsts.MIN_NORMAL,                    1.0},
            {-DoubleConsts.MIN_NORMAL,                   -0.0},
            { 0.1,                                        1.0},
            {-0.1,                                       -0.0},
            { 0.5,                                        1.0},
            {-0.5,                                       -0.0},
            { 1.5,                                        2.0},
            {-1.5,                                       -1.0},
            { 2.5,                                        3.0},
            {-2.5,                                       -2.0},
            { FpUtils.nextDown(1.0),                      1.0},
            { FpUtils.nextDown(-1.0),                    -1.0},
            { Math.nextUp(1.0),                           2.0},
            { Math.nextUp(-1.0),                         -0.0},
            { 0x1.0p51,                                 0x1.0p51},
            {-0x1.0p51,                                -0x1.0p51},
            { FpUtils.nextDown(0x1.0p51),               0x1.0p51},
            {-Math.nextUp(0x1.0p51),                   -0x1.0p51},
            { Math.nextUp(0x1.0p51),                    0x1.0p51+1},
            {-FpUtils.nextDown(0x1.0p51),              -0x1.0p51+1},
            { FpUtils.nextDown(0x1.0p52),               0x1.0p52},
            {-Math.nextUp(0x1.0p52),                   -0x1.0p52-1.0},
            { Math.nextUp(0x1.0p52),                    0x1.0p52+1.0},
            {-FpUtils.nextDown(0x1.0p52),              -0x1.0p52+1.0},
        };
        for(double[] testCase : testCases) {
            failures += testCeilCase(testCase[0], testCase[1]);
            failures += testFloorCase(-testCase[0], -testCase[1]);
        }
        return failures;
    }
    public static void main(String... args) {
        int failures = 0;
        failures += nearIntegerTests();
        failures += roundingTests();
        if (failures > 0) {
            System.err.println("Testing {Math, StrictMath}.ceil incurred "
                               + failures + " failures.");
            throw new RuntimeException();
        }
    }
}