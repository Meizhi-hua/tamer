@TestTargetClass(BigInteger.class)
public class BigIntegerMultiplyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for multiply method.",
        method = "multiply",
        args = {java.math.BigInteger.class}
    )
    public void testCase1() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {10, 40, 100, -55, 96, 51, 76, 40, -45, 85, 105, 4, 28, -86, -117, -52, 100, 120, 90};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.multiply(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for multiply method.",
        method = "multiply",
        args = {java.math.BigInteger.class}
    )
    public void testCase2() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {-11, -41, -101, 54, -97, -52, -77, -41, 44, -86, -106, -5, -29, 85, 116, 51, -101, -121, -90};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.multiply(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for multiply method.",
        method = "multiply",
        args = {java.math.BigInteger.class}
    )
    public void testCase3() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 1, 2, 3, 4, 5};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {10, 40, 100, -55, 96, 51, 76, 40, -45, 85, 115, 44, -127, 
                         115, -21, -62, -15, 85, 64, -87, -2, -36, -36, -106};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.multiply(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for multiply method.",
        method = "multiply",
        args = {java.math.BigInteger.class}
    )
    public void testCase4() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 1, 2, 3, 4, 5};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {10, 40, 100, -55, 96, 51, 76, 40, -45, 85, 115, 44, -127, 
                         115, -21, -62, -15, 85, 64, -87, -2, -36, -36, -106};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.multiply(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for multiply method.",
        method = "multiply",
        args = {java.math.BigInteger.class}
    )
    public void testCase5() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 1, 2, 3, 4, 5};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {-11, -41, -101, 54, -97, -52, -77, -41, 44, -86, -116, -45, 126,
                         -116, 20, 61, 14, -86, -65, 86, 1, 35, 35, 106};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.multiply(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for multiply method.",
        method = "multiply",
        args = {java.math.BigInteger.class}
    )
    public void testCase6() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 1, 2, 3, 4, 5};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {-11, -41, -101, 54, -97, -52, -77, -41, 44, -86, -116, -45, 126,
                         -116, 20, 61, 14, -86, -65, 86, 1, 35, 35, 106};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.multiply(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for multiply method.",
        method = "multiply",
        args = {java.math.BigInteger.class}
    )
    public void testCase7() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 1, 2, 3, 4, 5};
        byte bBytes[] = {0};
        int aSign = 1;
        int bSign = 0;        
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.multiply(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for multiply method.",
        method = "multiply",
        args = {java.math.BigInteger.class}
    )
    public void testCase8() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 1, 2, 3, 4, 5};
        int aSign = 1;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ZERO;
        BigInteger result = aNumber.multiply(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for multiply method.",
        method = "multiply",
        args = {java.math.BigInteger.class}
    )
    public void testCase9() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 1, 2, 3, 4, 5};
        int aSign = 1;
        byte rBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 1, 2, 3, 4, 5};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ONE;
        BigInteger result = aNumber.multiply(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for multiply method.",
        method = "multiply",
        args = {java.math.BigInteger.class}
    )
    public void testCase10() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 1, 2, 3, 4, 5};
        int aSign = -1;
        byte rBytes[] = {-2, -3, -4, -5, -6, -7, -8, -2, -3, -4, -2, -3, -4, -5, -5};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ONE;
        BigInteger result = aNumber.multiply(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for multiply method.",
        method = "multiply",
        args = {java.math.BigInteger.class}
    )
    public void testIntbyInt1() {
        byte aBytes[] = {10, 20, 30, 40};
        byte bBytes[] = {1, 2, 3, 4};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {-11, -41, -101, 55, 5, 15, 96};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.multiply(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for multiply method.",
        method = "multiply",
        args = {java.math.BigInteger.class}
    )
    public void testIntbyInt2() {
        byte aBytes[] = {-1, -1, -1, -1};
        byte bBytes[] = {-1, -1, -1, -1};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {0, -1, -1, -1, -2, 0, 0, 0, 1};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.multiply(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for pow method.",
        method = "pow",
        args = {int.class}
    )
    public void testPowException() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        int aSign = 1;
        int exp = -5;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        try {
            aNumber.pow(exp);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
            assertEquals("Improper exception message", "Negative exponent", e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for pow method.",
        method = "pow",
        args = {int.class}
    )
    public void testPowNegativeNumToOddExp() {
        byte aBytes[] = {50, -26, 90, 69, 120, 32, 63, -103, -14, 35};
        int aSign = -1;
        int exp = 5;
        byte rBytes[] = {-21, -94, -42, -15, -127, 113, -50, -88, 115, -35, 3,
            59, -92, 111, -75, 103, -42, 41, 34, -114, 99, -32, 105, -59, 127,
            45, 108, 74, -93, 105, 33, 12, -5, -20, 17, -21, -119, -127, -115,
            27, -122, 26, -67, 109, -125, 16, 91, -70, 109};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.pow(exp);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for pow method.",
        method = "pow",
        args = {int.class}
    )
    public void testPowNegativeNumToEvenExp() {
        byte aBytes[] = {50, -26, 90, 69, 120, 32, 63, -103, -14, 35};
        int aSign = -1;
        int exp = 4;
        byte rBytes[] = {102, 107, -122, -43, -52, -20, -27, 25, -9, 88, -13,
            75, 78, 81, -33, -77, 39, 27, -37, 106, 121, -73, 108, -47, -101,
            80, -25, 71, 13, 94, -7, -33, 1, -17, -65, -70, -61, -3, -47};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.pow(exp);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for pow method.",
        method = "pow",
        args = {int.class}
    )
    public void testPowNegativeNumToZeroExp() {
        byte aBytes[] = {50, -26, 90, 69, 120, 32, 63, -103, -14, 35};
        int aSign = -1;
        int exp = 0;
        byte rBytes[] = {1};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.pow(exp);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for pow method.",
        method = "pow",
        args = {int.class}
    )
    public void testPowPositiveNum() {
        byte aBytes[] = {50, -26, 90, 69, 120, 32, 63, -103, -14, 35};
        int aSign = 1;
        int exp = 5;
        byte rBytes[] = {20, 93, 41, 14, 126, -114, 49, 87, -116, 34, -4, -60,
            91, -112, 74, -104, 41, -42, -35, 113, -100, 31, -106, 58, -128,
            -46, -109, -75, 92, -106, -34, -13, 4, 19, -18, 20, 118, 126, 114,
            -28, 121, -27, 66, -110, 124, -17, -92, 69, -109};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.pow(exp);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for pow method.",
        method = "pow",
        args = {int.class}
    )
    public void testPowPositiveNumToZeroExp() {
        byte aBytes[] = {50, -26, 90, 69, 120, 32, 63, -103, -14, 35};
        int aSign = 1;
        int exp = 0;
        byte rBytes[] = {1};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.pow(exp);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
}
