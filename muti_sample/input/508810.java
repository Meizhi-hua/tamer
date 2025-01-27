@TestTargetClass(BigInteger.class)
public class BigIntegerAndTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testZeroPos() {
        byte aBytes[] = {0};
        byte bBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        int aSign = 0;
        int bSign = 1;        
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testZeroNeg() {
        byte aBytes[] = {0};
        byte bBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        int aSign = 0;
        int bSign = -1;        
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testPosZero() {
        byte aBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        byte bBytes[] = {0};
        int aSign = 1;
        int bSign = 0;        
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testNegPos() {
        byte aBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        byte bBytes[] = {0};
        int aSign = -1;
        int bSign = 0;        
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testZeroZero() {
        byte aBytes[] = {0};
        byte bBytes[] = {0};
        int aSign = 0;
        int bSign = 0;        
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testZeroOne() {
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger bNumber = BigInteger.ONE;
        BigInteger result = aNumber.and(bNumber);
        assertTrue(result.equals(BigInteger.ZERO));
        assertEquals("incorrect sign", 0, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testOneOne() {
        BigInteger aNumber = BigInteger.ONE;
        BigInteger bNumber = BigInteger.ONE;
        BigInteger result = aNumber.and(bNumber);
        assertTrue(result.equals(BigInteger.ONE));
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testPosPosSameLength() {
        byte aBytes[] = {-128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117};
        byte bBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {0, -128, 56, 100, 4, 4, 17, 37, 16, 1, 64, 1, 10, 3};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testPosPosFirstLonger() {
        byte aBytes[] = {-128, 9, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117, 23, 87, -25, -75};
        byte bBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {0, -2, -76, 88, 44, 1, 2, 17, 35, 16, 9, 2, 5, 6, 21};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testPosPosFirstShorter() {
        byte aBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        byte bBytes[] = {-128, 9, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117, 23, 87, -25, -75};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {0, -2, -76, 88, 44, 1, 2, 17, 35, 16, 9, 2, 5, 6, 21};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testNegNegSameLength() {
        byte aBytes[] = {-128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117};
        byte bBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {-1, 1, 2, 3, 3, 0, 65, -96, -48, -124, -60, 12, -40, -31, 97};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testNegNegFirstLonger() {
        byte aBytes[] = {-128, 9, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117, 23, 87, -25, -75};
        byte bBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {-1, 127, -10, -57, -101, 1, 2, 2, 2, -96, -16, 8, -40, -59, 68, -88, -88, 16, 73};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testNegNegFirstShorter() {
        byte aBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        byte bBytes[] = {-128, 9, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117, 23, 87, -25, -75};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {-1, 127, -10, -57, -101, 1, 2, 2, 2, -96, -16, 8, -40, -59, 68, -88, -88, 16, 73};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testPosNegSameLength() {
        byte aBytes[] = {-128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117};
        byte bBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {0, -6, -80, 72, 8, 75, 2, -79, 34, 16, -119};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testNegPosSameLength() {
        byte aBytes[] = {-128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117};
        byte bBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {0, -2, 125, -60, -104, 1, 10, 6, 2, 32, 56, 2, 4, 4, 21};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testNegPosFirstLonger() {
        byte aBytes[] = {-128, 9, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117, 23, 87, -25, -75};
        byte bBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {73, -92, -48, 4, 12, 6, 4, 32, 48, 64, 0, 8, 3};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testNegPosFirstShorter() {
        byte aBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        byte bBytes[] = {-128, 9, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117, 23, 87, -25, -75};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {0, -128, 9, 56, 100, 0, 0, 1, 1, 90, 1, -32, 0, 10, -126, 21, 82, -31, -95};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testPosNegFirstLonger() {
        byte aBytes[] = {-128, 9, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117, 23, 87, -25, -75};
        byte bBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {0, -128, 9, 56, 100, 0, 0, 1, 1, 90, 1, -32, 0, 10, -126, 21, 82, -31, -95};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testPosNegFirstShorter() {
        byte aBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        byte bBytes[] = {-128, 9, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117, 23, 87, -25, -75};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {73, -92, -48, 4, 12, 6, 4, 32, 48, 64, 0, 8, 3};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testSpecialCase1() {
        byte aBytes[] = {-1, -1, -1, -1};
        byte bBytes[] = {5, -4, -3, -2};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {-1, 0, 0, 0, 0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for and method.",
        method = "and",
        args = {java.math.BigInteger.class}
    )
    public void testSpecialCase2() {
        byte aBytes[] = {-51};
        byte bBytes[] = {-52, -51, -50, -49, -48};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {0, -52, -51, -50, -49, 16};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.and(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
}
