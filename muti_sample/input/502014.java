public class Test_rem_long extends DxTestCase {
    public void testN1() {
        T_rem_long_1 t = new T_rem_long_1();
        assertEquals(2000000000l, t.run(10000000000l, 4000000000l));
    }
    public void testN2() {
        T_rem_long_1 t = new T_rem_long_1();
        assertEquals(123l, t.run(1234567890123l, 123456789l));
    }
    public void testN3() {
        T_rem_long_1 t = new T_rem_long_1();
        assertEquals(0l, t.run(0l, 1234567890123l));
    }
    public void testN4() {
        T_rem_long_1 t = new T_rem_long_1();
        assertEquals(-2000000000l, t.run(-10000000000l, 4000000000l));
    }
    public void testN5() {
        T_rem_long_1 t = new T_rem_long_1();
        assertEquals(2000000000l, t.run(10000000000l, -4000000000l));
    }
    public void testN6() {
        T_rem_long_1 t = new T_rem_long_1();
        assertEquals(-2000000000l, t.run(-10000000000l, -4000000000l));
    }
    public void testN7() {
        T_rem_long_3 t = new T_rem_long_3();
        try {
            t.run(500000l, 1.05d);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_rem_long_1 t = new T_rem_long_1();
        assertEquals(0l, t.run(Long.MIN_VALUE, -1l));
    }
    public void testB2() {
        T_rem_long_1 t = new T_rem_long_1();
        assertEquals(0l, t.run(Long.MIN_VALUE, 1l));
    }
    public void testB3() {
        T_rem_long_1 t = new T_rem_long_1();
        assertEquals(0l, t.run(Long.MAX_VALUE, 1l));
    }
    public void testB4() {
        T_rem_long_1 t = new T_rem_long_1();
        assertEquals(-1l, t.run(Long.MIN_VALUE, Long.MAX_VALUE));
    }
    public void testB5() {
        T_rem_long_1 t = new T_rem_long_1();
        assertEquals(1l, t.run(1l, Long.MAX_VALUE));
    }
    public void testB6() {
        T_rem_long_1 t = new T_rem_long_1();
        assertEquals(1l, t.run(1l, Long.MIN_VALUE));
    }
    public void testE1() {
        T_rem_long_1 t = new T_rem_long_1();
        try {
            t.run(1234567890123l, 0l);
            fail("expected ArithmeticException");
        } catch (ArithmeticException ae) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.rem_long.d.T_rem_long_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.rem_long.d.T_rem_long_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.rem_long.d.T_rem_long_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.rem_long.d.T_rem_long_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
