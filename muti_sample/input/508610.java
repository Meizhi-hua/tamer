@TestTargetClass(BigDecimal.class)
public class BigDecimalConvertTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValueNeg() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        double result = -1.2380964839238476E53;
        assertEquals("incorrect value", result, aNumber.doubleValue(), 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValuePos() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        double result = 1.2380964839238476E53;
        assertEquals("incorrect value", result, aNumber.doubleValue(), 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValuePosInfinity() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+400";
        BigDecimal aNumber = new BigDecimal(a);
        double result = Double.POSITIVE_INFINITY;
        assertEquals("incorrect value", result, aNumber.doubleValue(), 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValueNegInfinity() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+400";
        BigDecimal aNumber = new BigDecimal(a);
        double result = Double.NEGATIVE_INFINITY;
        assertEquals("incorrect value", result, aNumber.doubleValue(), 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValueMinusZero() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E-400";
        BigDecimal aNumber = new BigDecimal(a);
        long minusZero = -9223372036854775808L;
        double result = aNumber.doubleValue();
        assertTrue("incorrect value", Double.doubleToLongBits(result) == minusZero);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for doubleValue method.",
        method = "doubleValue",
        args = {}
    )
    public void testDoubleValuePlusZero() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E-400";
        BigDecimal aNumber = new BigDecimal(a);
        long zero = 0;
        double result = aNumber.doubleValue();
        assertTrue("incorrect value", Double.doubleToLongBits(result) == zero);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueNeg() {
        String a = "-1238096483923847.6356789029578E+21";
        BigDecimal aNumber = new BigDecimal(a);
        float result = -1.2380965E36F;
        assertTrue("incorrect value", aNumber.floatValue() == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValuePos() {
        String a = "1238096483923847.6356789029578E+21";
        BigDecimal aNumber = new BigDecimal(a);
        float result = 1.2380965E36F;
        assertTrue("incorrect value", aNumber.floatValue() == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValuePosInfinity() {
        String a = "123809648373567356745735.6356789787678287E+200";
        BigDecimal aNumber = new BigDecimal(a);
        float result = Float.POSITIVE_INFINITY;
        assertTrue("incorrect value", aNumber.floatValue() == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueNegInfinity() {
        String a = "-123809648392384755735.63567887678287E+200";
        BigDecimal aNumber = new BigDecimal(a);
        float result = Float.NEGATIVE_INFINITY;
        assertTrue("incorrect value", aNumber.floatValue() == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValueMinusZero() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E-400";
        BigDecimal aNumber = new BigDecimal(a);
        int minusZero = -2147483648;
        float result = aNumber.floatValue();
        assertTrue("incorrect value", Float.floatToIntBits(result) == minusZero);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for floatValue method.",
        method = "floatValue",
        args = {}
    )
    public void testFloatValuePlusZero() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E-400";
        BigDecimal aNumber = new BigDecimal(a);
        int zero = 0;
        float result = aNumber.floatValue();
        assertTrue("incorrect value", Float.floatToIntBits(result) == zero);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValue method.",
        method = "intValue",
        args = {}
    )
    public void testIntValueNeg() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        int result = 218520473;
        assertTrue("incorrect value", aNumber.intValue() == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValue method.",
        method = "intValue",
        args = {}
    )
    public void testIntValuePos() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        int result = -218520473;
        assertTrue("incorrect value", aNumber.intValue() == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for longValue method",
        method = "longValue",
        args = {}
    )
    public void testLongValueNeg() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        long result = -1246043477766677607L;
        assertTrue("incorrect value", aNumber.longValue() == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for longValue method",
        method = "longValue",
        args = {}
    )
    public void testLongValuePos() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        long result = 1246043477766677607L;
        assertTrue("incorrect value", aNumber.longValue() == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "ArithmeticException checking missed",
        method = "scaleByPowerOfTen",
        args = {int.class}
    )
    public void testScaleByPowerOfTen1() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 13;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal result = aNumber.scaleByPowerOfTen(10);
        String res = "1231212478987482988429808779810457634781384756794.987";
        int resScale = 3;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "ArithmeticException checking missed",
        method = "scaleByPowerOfTen",
        args = {int.class}
    )
    public void testScaleByPowerOfTen2() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = -13;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal result = aNumber.scaleByPowerOfTen(10);
        String res = "1.231212478987482988429808779810457634781384756794987E+74";
        int resScale = -23;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toBigInteger method",
        method = "toBigInteger",
        args = {}
    )
    public void testToBigIntegerPos1() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigInteger bNumber = new BigInteger("123809648392384754573567356745735635678902957849027687");
        BigDecimal aNumber = new BigDecimal(a);
        BigInteger result = aNumber.toBigInteger();
        assertTrue("incorrect value", result.equals(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toBigInteger method",
        method = "toBigInteger",
        args = {}
    )
    public void testToBigIntegerPos2() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+15";
        BigInteger bNumber = new BigInteger("123809648392384754573567356745735635678902957849");
        BigDecimal aNumber = new BigDecimal(a);
        BigInteger result = aNumber.toBigInteger();
        assertTrue("incorrect value", result.equals(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toBigInteger method",
        method = "toBigInteger",
        args = {}
    )
    public void testToBigIntegerPos3() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+45";
        BigInteger bNumber = new BigInteger("123809648392384754573567356745735635678902957849027687876782870000000000000000");
        BigDecimal aNumber = new BigDecimal(a);
        BigInteger result = aNumber.toBigInteger();
        assertTrue("incorrect value", result.equals(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toBigInteger method",
        method = "toBigInteger",
        args = {}
    )
    public void testToBigIntegerNeg1() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigInteger bNumber = new BigInteger("-123809648392384754573567356745735635678902957849027687");
        BigDecimal aNumber = new BigDecimal(a);
        BigInteger result = aNumber.toBigInteger();
        assertTrue("incorrect value", result.equals(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toBigInteger method",
        method = "toBigInteger",
        args = {}
    )
    public void testToBigIntegerNeg2() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+15";
        BigInteger bNumber = new BigInteger("-123809648392384754573567356745735635678902957849");
        BigDecimal aNumber = new BigDecimal(a);
        BigInteger result = aNumber.toBigInteger();
        assertTrue("incorrect value", result.equals(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toBigInteger method",
        method = "toBigInteger",
        args = {}
    )
    public void testToBigIntegerNeg3() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+45";
        BigInteger bNumber = new BigInteger("-123809648392384754573567356745735635678902957849027687876782870000000000000000");
        BigDecimal aNumber = new BigDecimal(a);
        BigInteger result = aNumber.toBigInteger();
        assertTrue("incorrect value", result.equals(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toBigInteger method",
        method = "toBigInteger",
        args = {}
    )
    public void testToBigIntegerZero() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E-500";
        BigInteger bNumber = new BigInteger("0");
        BigDecimal aNumber = new BigDecimal(a);
        BigInteger result = aNumber.toBigInteger();
        assertTrue("incorrect value", result.equals(bNumber));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toBigIntegerExact method",
        method = "toBigIntegerExact",
        args = {}
    )
    public void testToBigIntegerExact1() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+45";
        BigDecimal aNumber = new BigDecimal(a);
        String res = "-123809648392384754573567356745735635678902957849027687876782870000000000000000";
        BigInteger result = aNumber.toBigIntegerExact();
        assertEquals("incorrect value", res, result.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toBigIntegerExact method",
        method = "toBigIntegerExact",
        args = {}
    )
    public void testToBigIntegerExactException() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E-10";
        BigDecimal aNumber = new BigDecimal(a);
        try {
            aNumber.toBigIntegerExact();
            fail("java.lang.ArithmeticException has not been thrown");
        } catch (java.lang.ArithmeticException e) {
            return;
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toEngineeringString method",
        method = "toEngineeringString",
        args = {}
    )
    public void testToEngineeringStringPos() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E-501";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "123.80964839238475457356735674573563567890295784902768787678287E-471";
        assertEquals("incorrect value", result, aNumber.toEngineeringString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toEngineeringString method",
        method = "toEngineeringString",
        args = {}
    )
    public void testToEngineeringStringNeg() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E-501";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "-123.80964839238475457356735674573563567890295784902768787678287E-471";
        assertEquals("incorrect value", result, aNumber.toEngineeringString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toEngineeringString method",
        method = "toEngineeringString",
        args = {}
    )
    public void testToEngineeringStringZeroPosExponent() {
        String a = "0.0E+16";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "0E+15";
        assertEquals("incorrect value", result, aNumber.toEngineeringString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toEngineeringString method",
        method = "toEngineeringString",
        args = {}
    )
    public void testToEngineeringStringZeroNegExponent() {
        String a = "0.0E-16";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "0.00E-15";
        assertEquals("incorrect value", result, aNumber.toEngineeringString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toPlainString method",
        method = "toPlainString",
        args = {}
    )
    public void testToPlainStringNegNegExp() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E-100";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "-0.000000000000000000000000000000000000000000000000000000000000000000012380964839238475457356735674573563567890295784902768787678287";
        assertTrue("incorrect value", aNumber.toPlainString().equals(result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toPlainString method",
        method = "toPlainString",
        args = {}
    )
    public void testToPlainStringNegPosExp() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E100";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "-1238096483923847545735673567457356356789029578490276878767828700000000000000000000000000000000000000000000000000000000000000000000000";
        assertTrue("incorrect value", aNumber.toPlainString().equals(result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toPlainString method",
        method = "toPlainString",
        args = {}
    )
    public void testToPlainStringPosNegExp() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E-100";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "0.000000000000000000000000000000000000000000000000000000000000000000012380964839238475457356735674573563567890295784902768787678287";
        assertTrue("incorrect value", aNumber.toPlainString().equals(result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toPlainString method",
        method = "toPlainString",
        args = {}
    )
    public void testToPlainStringPosPosExp() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+100";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "1238096483923847545735673567457356356789029578490276878767828700000000000000000000000000000000000000000000000000000000000000000000000";
        assertTrue("incorrect value", aNumber.toPlainString().equals(result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {}
    )
    public void testToStringZeroScale() {
        String a = "-123809648392384754573567356745735635678902957849027687876782870";
        BigDecimal aNumber = new BigDecimal(new BigInteger(a));
        String result = "-123809648392384754573567356745735635678902957849027687876782870";
        assertTrue("incorrect value", aNumber.toString().equals(result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {}
    )
    public void testToStringPos() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E-500";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "1.2380964839238475457356735674573563567890295784902768787678287E-468";
        assertTrue("incorrect value", aNumber.toString().equals(result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for toString method",
        method = "toString",
        args = {}
    )
    public void testToStringNeg() {
        String a = "-123.4564563673567380964839238475457356735674573563567890295784902768787678287E-5";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "-0.001234564563673567380964839238475457356735674573563567890295784902768787678287";
        assertTrue("incorrect value", aNumber.toString().equals(result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf(long) method",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfPosZeroScale() {
        long a = 98374823947823578L;
        BigDecimal aNumber = BigDecimal.valueOf(a);
        String result = "98374823947823578";
        assertTrue("incorrect value", aNumber.toString().equals(result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf(long) method",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfNegZeroScale() {
        long a = -98374823947823578L;
        BigDecimal aNumber = BigDecimal.valueOf(a);
        String result = "-98374823947823578";
        assertTrue("incorrect value", aNumber.toString().equals(result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf(long) method",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfNegScalePos() {
        long a = -98374823947823578L;
        int scale = 12;
        BigDecimal aNumber = BigDecimal.valueOf(a, scale);
        String result = "-98374.823947823578";
        assertTrue("incorrect value", aNumber.toString().equals(result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf(long) method",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfNegScaleNeg() {
        long a = -98374823947823578L;
        int scale = -12;
        BigDecimal aNumber = BigDecimal.valueOf(a, scale);
        String result = "-9.8374823947823578E+28";
        assertTrue("incorrect value", aNumber.toString().equals(result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf(long) method",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfPosScalePos() {
        long a = 98374823947823578L;
        int scale = 12;
        BigDecimal aNumber = BigDecimal.valueOf(a, scale);
        String result = "98374.823947823578";
        assertTrue("incorrect value", aNumber.toString().equals(result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf(long) method",
        method = "valueOf",
        args = {long.class}
    )
    public void testValueOfPosScaleNeg() {
        long a = 98374823947823578L;
        int scale = -12;
        BigDecimal aNumber = BigDecimal.valueOf(a, scale);
        String result = "9.8374823947823578E+28";
        assertTrue("incorrect value", aNumber.toString().equals(result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf(double) method",
        method = "valueOf",
        args = {double.class}
    )
    public void testValueOfDoubleNeg() {
        double a = -65678765876567576.98788767;
        BigDecimal result = BigDecimal.valueOf(a);
        String res = "-65678765876567576";
        int resScale = 0;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf(double) method",
        method = "valueOf",
        args = {double.class}
    )
    public void testValueOfDoublePos1() {
        double a = 65678765876567576.98788767;
        BigDecimal result = BigDecimal.valueOf(a);
        String res = "65678765876567576";
        int resScale = 0;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf(double) method",
        method = "valueOf",
        args = {double.class}
    )
    public void testValueOfDoublePos2() {
        double a = 12321237576.98788767;
        BigDecimal result = BigDecimal.valueOf(a);
        String res = "12321237576.987888";
        int resScale = 6;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf(double) method",
        method = "valueOf",
        args = {double.class}
    )
    public void testValueOfDoublePos3() {
        double a = 12321237576.9878838;
        BigDecimal result = BigDecimal.valueOf(a);
        String res = "12321237576.987885";
        int resScale = 6;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for valueOf(double) method",
        method = "valueOf",
        args = {double.class}
    )
    public void testValueOfDoubleNaN() {
        double a = Double.NaN;
        try {
            BigDecimal.valueOf(a);
            fail("NumberFormatException has not been thrown for Double.NaN");
        } catch (NumberFormatException e) {
            return;
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValueExact method",
        method = "intValueExact",
        args = {}
    )
    public void test_IntValueExactNeg() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        try {
            aNumber.intValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling intValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValueExact method",
        method = "intValueExact",
        args = {}
    )
    public void test_IntValueExactPos() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        try {
            aNumber.intValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling intValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValueExact method",
        method = "intValueExact",
        args = {}
    )
    public void test_IntValueExactFloatNeg() {
        BigDecimal aNumber = new BigDecimal("-2147483647.999");
        try {
            aNumber.intValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling intValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValueExact method",
        method = "intValueExact",
        args = {}
    )
    public void test_IntValueExactFloatPos() {
        float a = 2147483646.99999F;
        BigDecimal aNumber = new BigDecimal(a);
        try {
            aNumber.intValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling intValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValueExact method",
        method = "intValueExact",
        args = {}
    )
    public void test_IntValueExactLongPos() {
        long a = 2147483647L;
        BigDecimal aNumber = new BigDecimal(a);
        int iNumber = aNumber.intValueExact();
        assertTrue("incorrect value", iNumber == a);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for intValueExact method",
        method = "intValueExact",
        args = {}
    )
    public void test_IntValueExactLongNeg() {
        long a = -2147483648L;
        BigDecimal aNumber = new BigDecimal(a);
        int iNumber = aNumber.intValueExact();
        assertTrue("incorrect value", iNumber == a);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "ArithmeticException checked",
        method = "longValueExact",
        args = {}
    )
    public void test_LongValueExactNeg() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        try {
            aNumber.longValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling longValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "ArithmeticException checked",
        method = "longValueExact",
        args = {}
    )
    public void test_LongValueExactPos() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        try {
            aNumber.longValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling longValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "ArithmeticException checked",
        method = "longValueExact",
        args = {}
    )
    public void test_LongValueExactFloatNeg() {
        BigDecimal aNumber = new BigDecimal("-9223372036854775807.99999");
        try {
            aNumber.longValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling longValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "ArithmeticException checked",
        method = "longValueExact",
        args = {}
    )
    public void test_LongValueExactFloatPos() {
        float a = 9223372036854775806.99999F;
        BigDecimal aNumber = new BigDecimal(a);
        try {
            aNumber.longValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling longValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for byteValueExact method",
        method = "byteValueExact",
        args = {}
    )
    public void test_ByteValueExactPos() {
        int i = 127;
        BigDecimal bdNumber = new BigDecimal(i);
        byte bNumber = bdNumber.byteValueExact();
        assertTrue("incorrect byteValueExact", i == bNumber);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for byteValueExact method",
        method = "byteValueExact",
        args = {}
    )
    public void test_ByteValueExactNeg() {
        String sNumber = "-127.56789";
        int iNumber = -128;
        int iPresition = 3;
        MathContext mc = new MathContext(iPresition, RoundingMode.UP);
        BigDecimal bdNumber = new BigDecimal(sNumber, mc);
        byte bNumber = bdNumber.byteValueExact();
        assertTrue("incorrect byteValueExact", iNumber == bNumber);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for byteValueExact method",
        method = "byteValueExact",
        args = {}
    )
    public void test_ByteValueExactCharZero() {
        char[] cNumber = {
                '-', '0', '.', '0'
        };
        int iNumber = 0;
        int iPresition = 5;
        MathContext mc = new MathContext(iPresition, RoundingMode.HALF_DOWN);
        BigDecimal bdNumber = new BigDecimal(cNumber, mc);
        byte bNumber = bdNumber.byteValueExact();
        assertTrue("incorrect byteValueExact", iNumber == bNumber);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for byteValueExact method",
        method = "byteValueExact",
        args = {}
    )
    public void test_ByteValueExactStringZero() {
        String sNumber = "00000000000000";
        int iNumber = 0;
        int iPresition = 0;
        MathContext mc = new MathContext(iPresition, RoundingMode.HALF_UP);
        BigDecimal bdNumber = new BigDecimal(sNumber, mc);
        byte bNumber = bdNumber.byteValueExact();
        assertTrue("incorrect byteValueExact", iNumber == bNumber);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for byteValueExact method",
        method = "byteValueExact",
        args = {}
    )
    public void test_ByteValueExactDoubleMax() {
        double dNumber = Double.MAX_VALUE;
        BigDecimal bdNumber = new BigDecimal(dNumber);
        try {
            bdNumber.byteValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling byteValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for byteValueExact method",
        method = "byteValueExact",
        args = {}
    )
    public void test_ByteValueExactDoubleMin() {
        double dNumber = Double.MIN_VALUE;
        BigDecimal bdNumber = new BigDecimal(dNumber);
        try {
            bdNumber.byteValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling byteValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for byteValueExact method",
        method = "byteValueExact",
        args = {}
    )
    public void test_ByteValueExactFloatPos() {
        float fNumber = 123.5445F;
        BigDecimal bdNumber = new BigDecimal(fNumber);
        try {
            bdNumber.byteValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling byteValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for byteValueExact method",
        method = "byteValueExact",
        args = {}
    )
    public void test_ByteValueExactFloatNeg() {
        float fNumber = -12.987654321F;
        BigDecimal bdNumber = new BigDecimal(fNumber);
        try {
            bdNumber.byteValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling byteValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for byteValueExact method",
        method = "byteValueExact",
        args = {}
    )
    public void test_ByteValueExactDouble() {
        double dNumber = 123.0000D;
        BigDecimal bdNumber = new BigDecimal(dNumber);
        byte bNumber = bdNumber.byteValueExact();
        assertTrue("incorrect byteValueExact", dNumber == bNumber);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for byteValueExact method",
        method = "byteValueExact",
        args = {}
    )
    public void test_ByteValueExactLongMin() {
        long lNumber = Long.MIN_VALUE;
        BigDecimal bdNumber = new BigDecimal(lNumber);
        try {
            bdNumber.byteValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling byteValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for byteValueExact method",
        method = "byteValueExact",
        args = {}
    )
    public void test_ByteValueExactIntMax() {
        int iNumber = Integer.MAX_VALUE;
        BigDecimal bdNumber = new BigDecimal(iNumber);
        try {
            bdNumber.byteValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling byteValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "byteValue",
        args = {}
    )
    public void test_ByteValuePos() {
        int i = 127;
        BigDecimal bdNumber = new BigDecimal(i);
        byte bNumber = bdNumber.byteValue();
        assertTrue("incorrect byteValue", i == bNumber);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "byteValue",
        args = {}
    )
    public void test_ByteValueNeg() {
        String sNumber = "-127.56789";
        int iNumber = -128;
        int iPresition = 3;
        MathContext mc = new MathContext(iPresition, RoundingMode.UP);
        BigDecimal bdNumber = new BigDecimal(sNumber, mc);
        byte bNumber = bdNumber.byteValue();
        assertTrue("incorrect byteValueExact", iNumber == bNumber);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "byteValue",
        args = {}
    )
    public void test_ByteValueCharZero() {
        char[] cNumber = {
                '-', '0', '.', '0'
        };
        int iNumber = 0;
        int iPresition = 0;
        MathContext mc = new MathContext(iPresition, RoundingMode.HALF_UP);
        BigDecimal bdNumber = new BigDecimal(cNumber, mc);
        byte bNumber = bdNumber.byteValue();
        assertTrue("incorrect byteValue", iNumber == bNumber);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "byteValue",
        args = {}
    )
    public void test_ByteValueStringZero() {
        String sNumber = "00000";
        int iNumber = 0;
        int iPresition = 0;
        MathContext mc = new MathContext(iPresition, RoundingMode.HALF_UP);
        BigDecimal bdNumber = new BigDecimal(sNumber, mc);
        byte bNumber = bdNumber.byteValue();
        assertTrue("incorrect byteValue", iNumber == bNumber);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "byteValue",
        args = {}
    )
    public void test_ByteValueDoubleMax() {
        double dNumber = Double.MAX_VALUE;
        BigDecimal bdNumber = new BigDecimal(dNumber);
        int result = 0;
        byte bNumber = bdNumber.byteValue();
        assertTrue("incorrect byteValue", bNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "byteValue",
        args = {}
    )
    public void test_ByteValueDoubleMin() {
        double dNumber = Double.MIN_VALUE;
        BigDecimal bdNumber = new BigDecimal(dNumber);
        int result = 0;
        byte bNumber = bdNumber.byteValue();
        assertTrue("incorrect byteValue", bNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "byteValue",
        args = {}
    )
    public void test_ByteValueFloatNeg() {
        float fNumber = -12.987654321F;
        byte bValue = -12;
        BigDecimal bdNumber = new BigDecimal(fNumber);
        byte bNumber = bdNumber.byteValue();
        assertTrue("incorrect byteValue", bNumber == bValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "byteValue",
        args = {}
    )
    public void test_ByteValueDouble() {
        double dNumber = 123.0000D;
        BigDecimal bdNumber = new BigDecimal(dNumber);
        byte bNumber = bdNumber.byteValue();
        assertTrue("incorrect byteValue", dNumber == bNumber);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "byteValue",
        args = {}
    )
    public void test_ByteValueLongMin() {
        long lNumber = Long.MIN_VALUE;
        int result = 0;
        BigDecimal bdNumber = new BigDecimal(lNumber);
        byte bNumber = bdNumber.byteValue();
        assertTrue("incorrect byteValue", bNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "byteValue",
        args = {}
    )
    public void test_ByteValueIntMin() {
        int iNumber = Integer.MIN_VALUE;
        int result = 0;
        BigDecimal bdNumber = new BigDecimal(iNumber);
        byte bNumber = bdNumber.byteValue();
        assertTrue("incorrect byteValue", bNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "byteValue",
        args = {}
    )
    public void test_ByteValueIntMax() {
        int iNumber = Integer.MAX_VALUE;
        int result = -1;
        BigDecimal bdNumber = new BigDecimal(iNumber);
        byte bNumber = bdNumber.byteValue();
        assertTrue("incorrect byteValue", bNumber == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "shortValue",
        args = {}
    )
    public void test_ShortValueNeg() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        int result = 23449;
        assertTrue("incorrect value", aNumber.shortValue() == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "shortValue",
        args = {}
    )
    public void test_ShortValuePos() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        int result = -23449;
        assertTrue("incorrect value", aNumber.shortValue() == result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shortValueExact method",
        method = "shortValueExact",
        args = {}
    )
    public void test_ShortValueExactNeg() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        try {
            aNumber.shortValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling intValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shortValueExact method",
        method = "shortValueExact",
        args = {}
    )
    public void test_ShortValueExactPos() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        try {
            aNumber.shortValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling intValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shortValueExact method",
        method = "shortValueExact",
        args = {}
    )
    public void test_ShortValueExactFloatNeg() {
        BigDecimal aNumber = new BigDecimal("-32766.99999");
        try {
            aNumber.shortValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling intValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shortValueExact method",
        method = "shortValueExact",
        args = {}
    )
    public void test_ShortValueExactFloatPos() {
        float a = 32767.99999F;
        BigDecimal aNumber = new BigDecimal(a);
        try {
            aNumber.shortValueExact();
            fail("java.lang.ArithmeticException isn't thrown after calling intValueExact");
        } catch (java.lang.ArithmeticException ae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shortValueExact method",
        method = "shortValueExact",
        args = {}
    )
    public void test_ShortValueExactLongPos() {
        long a = 12345L;
        BigDecimal aNumber = new BigDecimal(a);
        short shNumber = aNumber.shortValueExact();
        assertTrue("incorrect value", shNumber == a);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for shortValueExact method",
        method = "shortValueExact",
        args = {}
    )
    public void test_ShortValueExactLongNeg() {
        long a = -12345L;
        BigDecimal aNumber = new BigDecimal(a);
        int iNumber = aNumber.shortValueExact();
        assertTrue("incorrect value", iNumber == a);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for stripTrailingZeros method",
        method = "stripTrailingZeros",
        args = {}
    )
    public void test_stripTrailingZerosZeros() {
        BigDecimal bdNumber = new BigDecimal("0000000");
        BigDecimal result = bdNumber.stripTrailingZeros();
        assertEquals("incorrect value", result.unscaledValue(), bdNumber.unscaledValue());
        assertTrue("incorrect value", result.scale() == 0);
        bdNumber = new BigDecimal(0);
        result = bdNumber.stripTrailingZeros();
        assertEquals("incorrect value", result.unscaledValue(), bdNumber.unscaledValue());
        assertTrue("incorrect value", result.scale() == 0);
        bdNumber = new BigDecimal(0.000000);
        result = bdNumber.stripTrailingZeros();
        assertEquals("incorrect value", result.unscaledValue(), bdNumber.unscaledValue());
        assertTrue("incorrect value", result.scale() == 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for stripTrailingZeros method",
        method = "stripTrailingZeros",
        args = {}
    )
    public void test_stripTrailingZeros() {
        String s = "00000000100000000100000000.000000000100000000";
        int iScale = 10;
        BigDecimal bdValue = new BigDecimal("1000000001000000000000000001");
        BigDecimal bdNumber = new BigDecimal(s);
        BigDecimal bdResult = bdNumber.stripTrailingZeros();
        assertEquals("incorrect value", bdResult.unscaledValue(), bdValue.unscaledValue());
        assertTrue("incorrect value", bdResult.scale() == iScale);
        s = "1000.0";
        iScale = -3;
        BigDecimal bd = new BigDecimal("1");
        bdNumber = new BigDecimal(s);
        bdResult = bdNumber.stripTrailingZeros();
        assertEquals("incorrect value", bdResult.unscaledValue(), bd.unscaledValue());
        assertTrue("incorrect value", bdResult.scale() == iScale);
    }
}
