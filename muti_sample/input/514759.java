@TestTargetClass(BigInteger.class)
public class BigIntegerConvertTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValueZero() {
        String a = "0";
        double result = 0.0;
        double aNumber = new BigInteger(a).doubleValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValuePositive1() {
        String a = "27467238945";
        double result = 2.7467238945E10;
        double aNumber = new BigInteger(a).doubleValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValuePositive2() {
        String a = "2746723894572364578265426346273456972";
        double result = 2.7467238945723645E36;
        double aNumber = new BigInteger(a).doubleValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValueNegative1() {
        String a = "-27467238945";
        double result = -2.7467238945E10;
        double aNumber = new BigInteger(a).doubleValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValueNegative2() {
        String a = "-2746723894572364578265426346273456972";
        double result = -2.7467238945723645E36;
        double aNumber = new BigInteger(a).doubleValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValuePosRounded1() {
        byte[] a = {-128, 1, 2, 3, 4, 5, 60, 23, 1, -3, -5};
        int aSign = 1;
        double result = 1.54747264387948E26;
        double aNumber = new BigInteger(aSign, a).doubleValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValuePosRounded2() {
        byte[] a = {-128, 1, 2, 3, 4, 5, 36, 23, 1, -3, -5};
        int aSign = 1;
        double result = 1.547472643879479E26;
        double aNumber = new BigInteger(aSign, a).doubleValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValuePosNotRounded() {
        byte[] a = {-128, 1, 2, 3, 4, 5, -128, 23, 1, -3, -5};
        int aSign = 1;
        double result = 1.5474726438794828E26;
        double aNumber = new BigInteger(aSign, a).doubleValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValueNegRounded1() {
        byte[] a = {-128, 1, 2, 3, 4, 5, 60, 23, 1, -3, -5};
        int aSign = -1;
        double result = -1.54747264387948E26;
        double aNumber = new BigInteger(aSign, a).doubleValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValueNegRounded2() {
        byte[] a = {-128, 1, 2, 3, 4, 5, 36, 23, 1, -3, -5};
        int aSign = -1;
        double result = -1.547472643879479E26;
        double aNumber = new BigInteger(aSign, a).doubleValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValueNegNotRounded() {
        byte[] a = {-128, 1, 2, 3, 4, 5, -128, 23, 1, -3, -5};
        int aSign = -1;
        double result = -1.5474726438794828E26;
        double aNumber = new BigInteger(aSign, a).doubleValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValuePosMaxValue() {
        byte[] a = {0, -1, -1, -1, -1, -1, -1, -8, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
           };
        int aSign = 1;
        double aNumber = new BigInteger(aSign, a).doubleValue();
        assertTrue(aNumber == Double.MAX_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValueNegMaxValue() {
        byte[] a = {0, -1, -1, -1, -1, -1, -1, -8, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
           };
        int aSign = -1;
        double aNumber = new BigInteger(aSign, a).doubleValue();
        assertTrue(aNumber == -Double.MAX_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValuePositiveInfinity1() {
        byte[] a = {-1, -1, -1, -1, -1, -1, -1, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 
           };
        int aSign = 1;
        double aNumber = new BigInteger(aSign, a).doubleValue();
        assertTrue(aNumber == Double.POSITIVE_INFINITY);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValuePositiveInfinity2() {
        String a = "2746723894572364578265426346273456972283746872364768676747462342342342342342342342323423423423423423426767456345745293762384756238475634563456845634568934568347586346578648576478568456457634875673845678456786587345873645767456834756745763457863485768475678465783456702897830296720476846578634576384567845678346573465786457863";
        double aNumber = new BigInteger(a).doubleValue();
        assertTrue(aNumber == Double.POSITIVE_INFINITY);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValueNegativeInfinity1() {
        String a = "-2746723894572364578265426346273456972283746872364768676747462342342342342342342342323423423423423423426767456345745293762384756238475634563456845634568934568347586346578648576478568456457634875673845678456786587345873645767456834756745763457863485768475678465783456702897830296720476846578634576384567845678346573465786457863";
        double aNumber = new BigInteger(a).doubleValue();
        assertTrue(aNumber == Double.NEGATIVE_INFINITY);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValueNegativeInfinity2() {
        byte[] a = {-1, -1, -1, -1, -1, -1, -1, -8, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 
           };
        int aSign = -1;
        double aNumber = new BigInteger(aSign, a).doubleValue();
        assertTrue(aNumber == Double.NEGATIVE_INFINITY);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValuePosMantissaIsZero() {
        byte[] a = {-128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 
           };
        int aSign = 1;
        double result = 8.98846567431158E307;
        double aNumber = new BigInteger(aSign, a).doubleValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValueNegMantissaIsZero() {
        byte[] a = {-128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 
           };
        int aSign = -1;
        double aNumber = new BigInteger(aSign, a).doubleValue();
        assertTrue(aNumber == -8.98846567431158E307);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueZero() {
        String a = "0";
        float result = 0.0f;
        float aNumber = new BigInteger(a).floatValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValuePositive1() {
        String a = "27467238";
        float result = 2.7467238E7f;
        float aNumber = new BigInteger(a).floatValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValuePositive2() {
        String a = "27467238945723645782";
        float result = 2.7467239E19f;
        float aNumber = new BigInteger(a).floatValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueNegative1() {
        String a = "-27467238";
        float result = -2.7467238E7f;
        float aNumber = new BigInteger(a).floatValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueNegative2() {
        String a = "-27467238945723645782";
        float result = -2.7467239E19f;
        float aNumber = new BigInteger(a).floatValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValuePosRounded1() {
        byte[] a = {-128, 1, -1, -4, 4, 5, 60, 23, 1, -3, -5};
        int aSign = 1;
        float result = 1.5475195E26f;
        float aNumber = new BigInteger(aSign, a).floatValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValuePosRounded2() {
        byte[] a = {-128, 1, 2, -128, 4, 5, 60, 23, 1, -3, -5};
        int aSign = 1;
        float result = 1.5474728E26f;
        float aNumber = new BigInteger(aSign, a).floatValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValuePosNotRounded() {
        byte[] a = {-128, 1, 2, 3, 4, 5, 60, 23, 1, -3, -5};
        int aSign = 1;
        float result = 1.5474726E26f;
        float aNumber = new BigInteger(aSign, a).floatValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueNegRounded1() {
        byte[] a = {-128, 1, -1, -4, 4, 5, 60, 23, 1, -3, -5};
        int aSign = -1;
        float result = -1.5475195E26f;
        float aNumber = new BigInteger(aSign, a).floatValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueNegRounded2() {
        byte[] a = {-128, 1, 2, -128, 4, 5, 60, 23, 1, -3, -5};
        int aSign = -1;
        float result = -1.5474728E26f;
        float aNumber = new BigInteger(aSign, a).floatValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueNegNotRounded() {
        byte[] a = {-128, 1, 2, 3, 4, 5, 60, 23, 1, -3, -5};
        int aSign = -1;
        float result = -1.5474726E26f;
        float aNumber = new BigInteger(aSign, a).floatValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValuePosMaxValue() {
        byte[] a = {0, -1, -1, -1, 0, -1, -1, -8, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        int aSign = 1;
        float aNumber = new BigInteger(aSign, a).floatValue();
        assertTrue(aNumber == Float.MAX_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueNegMaxValue() {
        byte[] a = {0, -1, -1, -1, 0, -1, -1, -8, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        int aSign = -1;
        float aNumber = new BigInteger(aSign, a).floatValue();
        assertTrue(aNumber == -Float.MAX_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValuePositiveInfinity1() {
        byte[] a = {0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        int aSign = 1;
        float aNumber = new BigInteger(aSign, a).floatValue();
        assertTrue(aNumber == Float.POSITIVE_INFINITY);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValuePositiveInfinity2() {
        String a = "2746723894572364578265426346273456972283746872364768676747462342342342342342342342323423423423423423426767456345745293762384756238475634563456845634568934568347586346578648576478568456457634875673845678456786587345873645767456834756745763457863485768475678465783456702897830296720476846578634576384567845678346573465786457863";
        float aNumber = new BigInteger(a).floatValue();
        assertTrue(aNumber == Float.POSITIVE_INFINITY);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueNegativeInfinity1() {
        String a = "-2746723894572364578265426346273456972283746872364768676747462342342342342342342342323423423423423423426767456345745293762384756238475634563456845634568934568347586346578648576478568456457634875673845678456786587345873645767456834756745763457863485768475678465783456702897830296720476846578634576384567845678346573465786457863";
        float aNumber = new BigInteger(a).floatValue();
        assertTrue(aNumber == Float.NEGATIVE_INFINITY);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueNegativeInfinity2() {
        byte[] a = {0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        int aSign = -1;
        float aNumber = new BigInteger(aSign, a).floatValue();
        assertTrue(aNumber == Float.NEGATIVE_INFINITY);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValuePosMantissaIsZero() {
        byte[] a = {-128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int aSign = 1;
        float result = 1.7014118E38f;
        float aNumber = new BigInteger(aSign, a).floatValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueNegMantissaIsZero() {
        byte[] a = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int aSign = -1;
        float aNumber = new BigInteger(aSign, a).floatValue();
        assertTrue(aNumber == Float.NEGATIVE_INFINITY);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueBug2482() {
        String a = "2147483649";
        float result = 2.14748365E9f;
        float aNumber = new BigInteger(a).floatValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValue method.",
        method = "intValue",
        args = {}
    )
    public void testIntValuePositive1() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3};
        int resInt = 1496144643;
        int aNumber = new BigInteger(aBytes).intValue();
        assertTrue(aNumber == resInt);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValue method.",
        method = "intValue",
        args = {}
    )
    public void testIntValuePositive2() {
        byte aBytes[] = {12, 56, 100};
        int resInt = 800868;
        int aNumber = new BigInteger(aBytes).intValue();
        assertTrue(aNumber == resInt);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValue method.",
        method = "intValue",
        args = {}
    )
    public void testIntValuePositive3() {
        byte aBytes[] = {56, 13, 78, -12, -5, 56, 100};
        int sign = 1;
        int resInt = -184862620;
        int aNumber = new BigInteger(sign, aBytes).intValue();
        assertTrue(aNumber == resInt);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValue method.",
        method = "intValue",
        args = {}
    )
    public void testIntValueNegative1() {
        byte aBytes[] = {12, 56, 100, -2, -76, -128, 45, 91, 3};
        int sign = -1;
        int resInt = 2144511229;
        int aNumber = new BigInteger(sign, aBytes).intValue();
        assertTrue(aNumber == resInt);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValue method.",
        method = "intValue",
        args = {}
    )
    public void testIntValueNegative2() {
        byte aBytes[] = {-12, 56, 100};
        int result = -771996;
        int aNumber = new BigInteger(aBytes).intValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValue method.",
        method = "intValue",
        args = {}
    )
    public void testIntValueNegative3() {
        byte aBytes[] = {12, 56, 100, -2, -76, 127, 45, 91, 3};
        int sign = -1;
        int resInt = -2133678851;
        int aNumber = new BigInteger(sign, aBytes).intValue();
        assertTrue(aNumber == resInt);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for longValue method.",
        method = "longValue",
        args = {}
    )
    public void testLongValuePositive1() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, 120, -34, -12, 45, 98};
        long result = 3268209772258930018L;
        long aNumber = new BigInteger(aBytes).longValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for longValue method.",
        method = "longValue",
        args = {}
    )
    public void testLongValuePositive2() {
        byte aBytes[] = {12, 56, 100, 18, -105, 34, -18, 45};
        long result = 880563758158769709L;
        long aNumber = new BigInteger(aBytes).longValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for longValue method.",
        method = "longValue",
        args = {}
    )
    public void testLongValueNegative1() {
        byte aBytes[] = {12, -1, 100, -2, -76, -128, 45, 91, 3};
        long result = -43630045168837885L;
        long aNumber = new BigInteger(aBytes).longValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for longValue method.",
        method = "longValue",
        args = {}
    )
    public void testLongValueNegative2() {
        byte aBytes[] = {-12, 56, 100, 45, -101, 45, 98};
        long result = -3315696807498398L;
        long aNumber = new BigInteger(aBytes).longValue();
        assertTrue(aNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf method.",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfIntegerMax() {
        long longVal = Integer.MAX_VALUE;
        BigInteger aNumber = BigInteger.valueOf(longVal);
        byte rBytes[] = {127, -1, -1, -1};
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf method.",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfIntegerMin() {
        long longVal = Integer.MIN_VALUE;
        BigInteger aNumber = BigInteger.valueOf(longVal);
        byte rBytes[] = {-128, 0, 0, 0};
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf method.",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfLongMax() {
        long longVal = Long.MAX_VALUE;
        BigInteger aNumber = BigInteger.valueOf(longVal);
        byte rBytes[] = {127, -1, -1, -1, -1, -1, -1, -1};
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf method.",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfLongMin() {
        long longVal = Long.MIN_VALUE;
        BigInteger aNumber = BigInteger.valueOf(longVal);
        byte rBytes[] = {-128, 0, 0, 0, 0, 0, 0, 0};
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf method.",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfLongPositive1() {
        long longVal = 268209772258930018L;
        BigInteger aNumber = BigInteger.valueOf(longVal);
        byte rBytes[] = {3, -72, -33, 93, -24, -56, 45, 98};
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf method.",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfLongPositive2() {
        long longVal = 58930018L;
        BigInteger aNumber = BigInteger.valueOf(longVal);
        byte rBytes[] = {3, -125, 51, 98};
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, aNumber.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf method.",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfLongNegative1() {
        long longVal = -268209772258930018L;
        BigInteger aNumber = BigInteger.valueOf(longVal);
        byte rBytes[] = {-4, 71, 32, -94, 23, 55, -46, -98};
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf method.",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfLongNegative2() {
        long longVal = -58930018L;
        BigInteger aNumber = BigInteger.valueOf(longVal);
        byte rBytes[] = {-4, 124, -52, -98};
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, aNumber.signum());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf method.",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfLongZero() {
        long longVal = 0L;
        BigInteger aNumber = BigInteger.valueOf(longVal);
        byte rBytes[] = {0};
        byte resBytes[] = new byte[rBytes.length];
        resBytes = aNumber.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, aNumber.signum());
    }
}
