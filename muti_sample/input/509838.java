@TestTargetClass(BigInteger.class)
public class BigIntegerOperateBitsTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for bitCount method.",
        method = "bitCount",
        args = {}
    )
    public void testBitCountZero() {
        BigInteger aNumber = new BigInteger("0");
        assertEquals(0, aNumber.bitCount());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for bitCount method.",
        method = "bitCount",
        args = {}
    )
    public void testBitCountNeg() {
        BigInteger aNumber = new BigInteger("-12378634756382937873487638746283767238657872368748726875");
        assertEquals(87, aNumber.bitCount());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for bitCount method.",
        method = "bitCount",
        args = {}
    )
    public void testBitCountPos() {
        BigInteger aNumber = new BigInteger("12378634756343564757582937873487638746283767238657872368748726875");
        assertEquals(107, aNumber.bitCount());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for bitLength method.",
        method = "bitLength",
        args = {}
    )
    public void testBitLengthZero() {
        BigInteger aNumber = new BigInteger("0");
        assertEquals(0, aNumber.bitLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for bitLength method.",
        method = "bitLength",
        args = {}
    )
    public void testBitLengthPositive1() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertEquals(108, aNumber.bitLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for bitLength method.",
        method = "bitLength",
        args = {}
    )
    public void testBitLengthPositive2() {
        byte aBytes[] = {-128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertEquals(96, aNumber.bitLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for bitLength method.",
        method = "bitLength",
        args = {}
    )
    public void testBitLengthPositive3() {
        byte aBytes[] = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertEquals(81, aNumber.bitLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for bitLength method.",
        method = "bitLength",
        args = {}
    )
    public void testBitLengthNegative1() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = -1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertEquals(108, aNumber.bitLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for bitLength method.",
        method = "bitLength",
        args = {}
    )
    public void testBitLengthNegative2() {
        byte aBytes[] = {-128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertEquals(96, aNumber.bitLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for bitLength method.",
        method = "bitLength",
        args = {}
    )
    public void testBitLengthNegative3() {
        byte aBytes[] = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int aSign = -1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertEquals(80, aNumber.bitLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitException() {
        byte aBytes[] = {-1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = -7;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        try {
            aNumber.clearBit(number);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
            assertEquals("Improper exception message", "Negative bit address", e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitZero() {
        byte aBytes[] = {0};
        int aSign = 0;
        int number = 0;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitZeroOutside1() {
        byte aBytes[] = {0};
        int aSign = 0;
        int number = 95;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitNegativeInside1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 15;
        byte rBytes[] = {-2, 127, -57, -101, 1, 75, -90, -46, -92, -4, 14, 92, -26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitNegativeInside2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 44;
        byte rBytes[] = {-2, 127, -57, -101, 1, 75, -90, -62, -92, -4, 14, -36, -26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitNegativeInside3() {
        String as = "-18446744073709551615";
        int number = 2;
        BigInteger aNumber = new BigInteger(as);
        BigInteger result = aNumber.clearBit(number);
        assertEquals(as, result.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitNegativeInside4() {
        String as = "-4294967295";
        String res = "-4294967296";
        int number = 0;
        BigInteger aNumber = new BigInteger(as);
        BigInteger result = aNumber.clearBit(number);
        assertEquals(res, result.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitNegativeInside5() {
        String as = "-18446744073709551615";
        String res = "-18446744073709551616";
        int number = 0;
        BigInteger aNumber = new BigInteger(as);
        BigInteger result = aNumber.clearBit(number);
        assertEquals(res, result.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitNegativeOutside1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 150;
        byte rBytes[] = {-65, -1, -1, -1, -1, -1, -2, 127, -57, -101, 1, 75, -90, -46, -92, -4, 14, -36, -26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitNegativeOutside2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 165;
        byte rBytes[] = {-33, -1, -1, -1, -1, -1, -1, -1, -2, 127, -57, -101, 1, 75, -90, -46, -92, -4, 14, -36, -26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitPositiveInside1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 20;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -31, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitPositiveInside2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 17;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitPositiveInside3() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 45;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 89, 13, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitPositiveInside4 () {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 50;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitPositiveInside5 () {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 63;
        byte rBytes[] = {1, -128, 56, 100, -2, 52, 89, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitPositiveOutside1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 150;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitPositiveOutside2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 191;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for clearBit method.",
        method = "clearBit",
        args = {int.class}
    )
    public void testClearBitTopNegative() {
        byte aBytes[] = {1, -128, 56, 100, -15, 35, 26};
        int aSign = -1;
        int number = 63;
        byte rBytes[] = {-1, 127, -2, 127, -57, -101, 14, -36, -26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.clearBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitException() {
        byte aBytes[] = {-1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = -7;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        try {
            aNumber.flipBit(number);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
            assertEquals("Improper exception message", "Negative bit address", e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitZero() {
        byte aBytes[] = {0};
        int aSign = 0;
        int number = 0;
        byte rBytes[] = {1};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.flipBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitZeroOutside1() {
        byte aBytes[] = {0};
        int aSign = 0;
        int number = 62;
        byte rBytes[] = {64, 0, 0, 0, 0, 0, 0, 0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.flipBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue("incorrect value", resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitZeroOutside2() {
        byte aBytes[] = {0};
        int aSign = 0;
        int number = 63;
        byte rBytes[] = {0, -128, 0, 0, 0, 0, 0, 0, 0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.flipBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue("incorrect value", resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitLeftmostNegative() {
        byte aBytes[] = {1, -128, 56, 100, -15, 35, 26};
        int aSign = -1;
        int number = 48;
        byte rBytes[] = {-1, 127, -57, -101, 14, -36, -26, 49};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.flipBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitLeftmostPositive() {
        byte aBytes[] = {1, -128, 56, 100, -15, 35, 26};
        int aSign = 1;
        int number = 48;
        byte rBytes[] = {0, -128, 56, 100, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.flipBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitNegativeInside1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 15;
        byte rBytes[] = {-2, 127, -57, -101, 1, 75, -90, -46, -92, -4, 14, 92, -26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.flipBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitNegativeInside2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 45;
        byte rBytes[] = {-2, 127, -57, -101, 1, 75, -90, -14, -92, -4, 14, -36, -26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.flipBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitNegativeInside3() {
        String as = "-18446744073709551615";
        String res = "-18446744073709551611";
        int number = 2;
        BigInteger aNumber = new BigInteger(as);
        BigInteger result = aNumber.flipBit(number);
        assertEquals(res, result.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitNegativeInside4() {
        String as = "-4294967295";
        String res = "-4294967296";
        int number = 0;
        BigInteger aNumber = new BigInteger(as);
        BigInteger result = aNumber.flipBit(number);
        assertEquals(res, result.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitNegativeInside5() {
        String as = "-18446744073709551615";
        String res = "-18446744073709551616";
        int number = 0;
        BigInteger aNumber = new BigInteger(as);
        BigInteger result = aNumber.flipBit(number);
        assertEquals(res, result.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitNegativeOutside1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 150;
        byte rBytes[] = {-65, -1, -1, -1, -1, -1, -2, 127, -57, -101, 1, 75, -90, -46, -92, -4, 14, -36, -26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.flipBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitNegativeOutside2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 191;
        byte rBytes[] = {-1, 127, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, 127, -57, -101, 1, 75, -90, -46, -92, -4, 14, -36, -26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.flipBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitPositiveInside1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 15;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, -93, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.flipBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitPositiveInside2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 45;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 89, 13, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.flipBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitPositiveOutside1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 150;
        byte rBytes[] = {64, 0, 0, 0, 0, 0, 1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.flipBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for flipBit method.",
        method = "flipBit",
        args = {int.class}
    )
    public void testFlipBitPositiveOutside2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 191;
        byte rBytes[] = {0, -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.flipBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitException() {
        byte aBytes[] = {-1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = -7;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        try {
            aNumber.setBit(number);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
            assertEquals("Improper exception message", "Negative bit address", e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitZero() {
        byte aBytes[] = {0};
        int aSign = 0;
        int number = 0;
        byte rBytes[] = {1};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitZeroOutside1() {
        byte aBytes[] = {0};
        int aSign = 0;
        int number = 95;
        byte rBytes[] = {0, -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitPositiveInside1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 20;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitPositiveInside2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 17;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -13, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitPositiveInside3() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 45;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitPositiveInside4 () {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 50;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 93, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitPositiveOutside1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 150;
        byte rBytes[] = {64, 0, 0, 0, 0, 0, 1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitPositiveOutside2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 223;
        byte rBytes[] = {0, -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitTopPositive() {
        byte aBytes[] = {1, -128, 56, 100, -15, 35, 26};
        int aSign = 1;
        int number = 63;
        byte rBytes[] = {0, -128, 1, -128, 56, 100, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitLeftmostNegative() {
        byte aBytes[] = {1, -128, 56, 100, -15, 35, 26};
        int aSign = -1;
        int number = 48;
        byte rBytes[] = {-1, 127, -57, -101, 14, -36, -26, 49};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitNegativeInside1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 15;
        byte rBytes[] = {-2, 127, -57, -101, 1, 75, -90, -46, -92, -4, 14, -36, -26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitNegativeInside2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 44;
        byte rBytes[] = {-2, 127, -57, -101, 1, 75, -90, -46, -92, -4, 14, -36, -26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitNegativeInside3() {
        String as = "-18446744073709551615";
        String res = "-18446744073709551611";
        int number = 2;
        BigInteger aNumber = new BigInteger(as);
        BigInteger result = aNumber.setBit(number);
        assertEquals(res, result.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitNegativeInside4() {
        String as = "-4294967295";
        int number = 0;
        BigInteger aNumber = new BigInteger(as);
        BigInteger result = aNumber.setBit(number);
        assertEquals(as, result.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitNegativeInside5() {
        String as = "-18446744073709551615";
        int number = 0;
        BigInteger aNumber = new BigInteger(as);
        BigInteger result = aNumber.setBit(number);
        assertEquals(as, result.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitNegativeOutside1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 150;
        byte rBytes[] = {-2, 127, -57, -101, 1, 75, -90, -46, -92, -4, 14, -36, -26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitNegativeOutside2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 191;
        byte rBytes[] = {-2, 127, -57, -101, 1, 75, -90, -46, -92, -4, 14, -36, -26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.setBit(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setBit method.",
        method = "setBit",
        args = {int.class}
    )
    public void testSetBitBug1331() {
        BigInteger result = BigInteger.valueOf(0L).setBit(191);
        assertEquals("incorrect value", "3138550867693340381917894711603833208051177722232017256448", result.toString());
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftLeft method.",
        method = "shiftLeft",
        args = {int.class}
    )
    public void testShiftLeft1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 0;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftLeft(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftLeft method.",
        method = "shiftLeft",
        args = {int.class}
    )
    public void testShiftLeft2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = -27;
        byte rBytes[] = {48, 7, 12, -97, -42, -117, 37, -85, 96};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftLeft(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftLeft method.",
        method = "shiftLeft",
        args = {int.class}
    )
    public void testShiftLeft3() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 27;
        byte rBytes[] = {12, 1, -61, 39, -11, -94, -55, 106, -40, 31, -119, 24, -48, 0, 0, 0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftLeft(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftLeft method.",
        method = "shiftLeft",
        args = {int.class}
    )
    public void testShiftLeft4() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 45;
        byte rBytes[] = {48, 7, 12, -97, -42, -117, 37, -85, 96, 126, 36, 99, 64, 0, 0, 0, 0, 0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftLeft(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftLeft method.",
        method = "shiftLeft",
        args = {int.class}
    )
    public void testShiftLeft5() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 45;
        byte rBytes[] = {-49, -8, -13, 96, 41, 116, -38, 84, -97, -127, -37, -100, -64, 0, 0, 0, 0, 0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftLeft(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "shiftRight",
        args = {int.class}
    )
    public void testShiftRight1() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 0;
        byte rBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftRight(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "shiftRight",
        args = {int.class}
    )
    public void testShiftRight2() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = -27;
        byte rBytes[] = {12, 1, -61, 39, -11, -94, -55, 106, -40, 31, -119, 24, -48, 0, 0, 0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftRight(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "shiftRight",
        args = {int.class}
    )
    public void testShiftRight3() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 27;
        byte rBytes[] = {48, 7, 12, -97, -42, -117, 37, -85, 96};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftRight(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "shiftRight",
        args = {int.class}
    )
    public void testShiftRight4() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 45;
        byte rBytes[] = {12, 1, -61, 39, -11, -94, -55};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftRight(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "shiftRight",
        args = {int.class}
    )
    public void testShiftRight5() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 300;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftRight(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "shiftRight",
        args = {int.class}
    )
    public void testShiftRightNegNonZeroesMul32() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 1, 0, 0, 0, 0, 0, 0, 0};
        int aSign = -1;
        int number = 64;
        byte rBytes[] = {-2, 127, -57, -101, 1, 75, -90, -46, -92};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftRight(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "shiftRight",
        args = {int.class}
    )
    public void testShiftRightNegNonZeroes() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 0, 0, 0, 0, 0, 0, 0, 0};
        int aSign = -1;
        int number = 68;
        byte rBytes[] = {-25, -4, 121, -80, 20, -70, 109, 42};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftRight(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "shiftRight",
        args = {int.class}
    )
    public void testShiftRightNegZeroes() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int aSign = -1;
        int number = 68;
        byte rBytes[] = {-25, -4, 121, -80, 20, -70, 109, 48};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftRight(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "shiftRight",
        args = {int.class}
    )
    public void testShiftRightNegZeroesMul32() {
        byte aBytes[] = {1, -128, 56, 100, -2, -76, 89, 45, 91, 0, 0, 0, 0, 0, 0, 0, 0};
        int aSign = -1;
        int number = 64;
        byte rBytes[] = {-2, 127, -57, -101, 1, 75, -90, -46, -91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.shiftRight(number);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "testBit",
        args = {int.class}
    )
    public void testTestBitException() {
        byte aBytes[] = {-1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = -7;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        try {
            aNumber.testBit(number);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
            assertEquals("Improper exception message", "Negative bit address", e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "testBit",
        args = {int.class}
    )
    public void testTestBitPositive1() {
        byte aBytes[] = {-1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 7;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertTrue(!aNumber.testBit(number));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "testBit",
        args = {int.class}
    )
    public void testTestBitPositive2() {
        byte aBytes[] = {-1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 45;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertTrue(aNumber.testBit(number));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "testBit",
        args = {int.class}
    )
    public void testTestBitPositive3() {
        byte aBytes[] = {-1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = 1;
        int number = 300;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertTrue(!aNumber.testBit(number));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "testBit",
        args = {int.class}
    )
    public void testTestBitNegative1() {
        byte aBytes[] = {-1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 7;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertTrue(aNumber.testBit(number));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "testBit",
        args = {int.class}
    )
    public void testTestBitNegative2() {
        byte aBytes[] = {-1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 45;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertTrue(!aNumber.testBit(number));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shiftRight method.",
        method = "testBit",
        args = {int.class}
    )
    public void testTestBitNegative3() {
        byte aBytes[] = {-1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26};
        int aSign = -1;
        int number = 300;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertTrue(aNumber.testBit(number));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getLowestSetBit method.",
        method = "getLowestSetBit",
        args = {}
    )
    public void test_getLowestSetBitNeg() {
        byte aBytes[] = {
                -1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26
        };
        int aSign = -1;
        int iNumber = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        int result = aNumber.getLowestSetBit();
        assertTrue("incorrect value", result == iNumber);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getLowestSetBit method.",
        method = "getLowestSetBit",
        args = {}
    )
    public void test_getLowestSetBitPos() {
        byte aBytes[] = {
                -1, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26
        };
        int aSign = 1;
        int iNumber = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        int result = aNumber.getLowestSetBit();
        assertTrue("incorrect value", result == iNumber);
        byte[] aBytes_ = {
                127, 0, 3
        };
        iNumber = 0;
        aNumber = new BigInteger(aSign, aBytes_);
        result = aNumber.getLowestSetBit();
        assertTrue("incorrect value", result == iNumber);
        byte[] aBytes__ = {
                -128, 0, 0
        };
        iNumber = 23;
        aNumber = new BigInteger(aSign, aBytes__);
        result = aNumber.getLowestSetBit();
        assertTrue("incorrect value", result == iNumber);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getLowestSetBit method.",
        method = "getLowestSetBit",
        args = {}
    )
    public void test_getLowestSetBitZero() {
        byte[] aBytes = {
            0
        };
        int aSign = 0;
        int iNumber = -1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        int result = aNumber.getLowestSetBit();
        assertTrue("incorrect value", result == iNumber);
        byte[] aBytes_ = {
                0, 0, 0
        };
        iNumber = -1;
        aNumber = new BigInteger(aSign, aBytes_);
        result = aNumber.getLowestSetBit();
        assertTrue("incorrect value", result == iNumber);
    }
}
