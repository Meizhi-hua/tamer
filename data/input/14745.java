public class FloatDoubleValueTests {
    private static final long two2the24 = 1L<<23;
    private static final long two2the53 = 1L<<52;
    private static final long maxFltLong = (long)(Integer.MAX_VALUE & ~(0xff));
    private static final long maxDblLong = Long.MAX_VALUE & ~(0x7ffL);
    static void testDoubleValue0(long i, BigDecimal bd) {
        if (bd.doubleValue() != i ||
            bd.longValue()   != i)
            throw new RuntimeException("Unexpected equality failure for " +
                                       i + "\t" + bd);
    }
    static void testFloatValue0(long i, BigDecimal bd) {
        if (bd.floatValue() != i ||
            bd.longValue()   != i)
            throw new RuntimeException("Unexpected equality failure for " +
                                       i + "\t" + bd);
    }
    static void checkFloat(BigDecimal bd, float f) {
        float fbd = bd.floatValue();
        if (f != fbd ) {
            String message = String.format("Bad conversion:"+
                                           "got %g (%a)\texpected %g (%a)",
                                           f, f, fbd, fbd);
            throw new RuntimeException(message);
        }
    }
    static void checkDouble(BigDecimal bd, double d) {
        double dbd = bd.doubleValue();
        if (d != dbd ) {
            String message = String.format("Bad conversion:"+
                                           "got %g (%a)\texpected %g (%a)",
                                           d, d, dbd, dbd);
            throw new RuntimeException(message);
        }
    }
    static void testFloatDoubleValue() {
        long longValues[] = {
            0,
            1,
            2,
            two2the24-1,
            two2the24,
            two2the24+1,
            maxFltLong-1,
            maxFltLong,
            maxFltLong+1,
        };
        for(long i : longValues) {
            BigDecimal bd1 = new BigDecimal(i);
            BigDecimal bd2 = new BigDecimal(-i);
            testDoubleValue0( i, bd1);
            testDoubleValue0(-i, bd2);
            testFloatValue0( i, bd1);
            testFloatValue0(-i, bd2);
        }
    }
    static void testDoubleValue() {
        long longValues[] = {
            Integer.MAX_VALUE-1,
            Integer.MAX_VALUE,
            (long)Integer.MAX_VALUE+1,
            two2the53-1,
            two2the53,
            two2the53+1,
            maxDblLong,
        };
        for(long i : longValues) {
            BigDecimal bd1 = new BigDecimal(i);
            BigDecimal bd2 = new BigDecimal(-i);
            testDoubleValue0( i, bd1);
            testDoubleValue0(-i, bd2);
            checkFloat(bd1, (float)i);
            checkFloat(bd2, -(float)i);
        }
        for(long i = maxDblLong; i < Long.MAX_VALUE; i++) {
            BigDecimal bd1 = new BigDecimal(i);
            BigDecimal bd2 = new BigDecimal(-i);
            checkDouble(bd1, (double)i);
            checkDouble(bd2, -(double)i);
            checkFloat(bd1, (float)i);
            checkFloat(bd2, -(float)i);
        }
        checkDouble(new BigDecimal(Long.MIN_VALUE), (double)Long.MIN_VALUE);
        checkDouble(new BigDecimal(Long.MAX_VALUE), (double)Long.MAX_VALUE);
    }
    static void testFloatValue() {
        for(long i = maxFltLong; i <= Integer.MAX_VALUE; i++) {
            BigDecimal bd1 = new BigDecimal(i);
            BigDecimal bd2 = new BigDecimal(-i);
            checkFloat(bd1, (float)i);
            checkFloat(bd2, -(float)i);
            testDoubleValue0( i, bd1);
            testDoubleValue0(-i, bd2);
        }
    }
    static void testFloatValue1() {
        checkFloat(new BigDecimal("85070591730234615847396907784232501249"), 8.507059e+37f);
        checkFloat(new BigDecimal("7784232501249e12"), 7.7842326e24f);
        checkFloat(new BigDecimal("907784232501249e-12"),907.78424f);
        checkFloat(new BigDecimal("7784e8"),7.7839997e11f);
        checkFloat(new BigDecimal("9077e-8"),9.077e-5f);
    }
    static void testDoubleValue1() {
        checkDouble(new BigDecimal("85070591730234615847396907784232501249"), 8.507059173023462e37);
        checkDouble(new BigDecimal("7784232501249e12"), 7.784232501249e24);
        checkDouble(new BigDecimal("907784232501249e-12"), 907.784232501249);
        checkDouble(new BigDecimal("7784e8"), 7.784e11);
        checkDouble(new BigDecimal("9077e-8"), 9.077e-5);
    }
    public static void main(String[] args) throws Exception {
        testFloatDoubleValue();
        testDoubleValue();
        testFloatValue();
        testFloatValue1();
        testDoubleValue1();
    }
}
