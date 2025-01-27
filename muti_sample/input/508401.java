public class Test_rem_int extends DxTestCase {
    public void testN1() {
        T_rem_int_1 t = new T_rem_int_1();
        assertEquals(0, t.run(8, 4));
    }
    public void testN2() {
        T_rem_int_1 t = new T_rem_int_1();
        assertEquals(3, t.run(1073741823, 4));
    }
    public void testN3() {
        T_rem_int_1 t = new T_rem_int_1();
        assertEquals(0, t.run(0, 4));
    }
    public void testN4() {
        T_rem_int_1 t = new T_rem_int_1();
        assertEquals(-1, t.run(-10, 3));
    }
    public void testN5() {
        T_rem_int_1 t = new T_rem_int_1();
        assertEquals(1, t.run(1073741824, -3));
    }
    public void testN6() {
        T_rem_int_1 t = new T_rem_int_1();
        assertEquals(-697, t.run(-17895697, -3000));
    }
    public void testN7() {
        T_rem_int_6 t = new T_rem_int_6();
        try {
            t.run(3.14f, 15);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_rem_int_1 t = new T_rem_int_1();
        assertEquals(0, t.run(Integer.MIN_VALUE, -1));
    }
    public void testB2() {
        T_rem_int_1 t = new T_rem_int_1();
        assertEquals(0, t.run(Integer.MIN_VALUE, 1));
    }
    public void testB3() {
        T_rem_int_1 t = new T_rem_int_1();
        assertEquals(0, t.run(Integer.MAX_VALUE, 1));
    }
    public void testB4() {
        T_rem_int_1 t = new T_rem_int_1();
        assertEquals(-1, t.run(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }
    public void testB5() {
        T_rem_int_1 t = new T_rem_int_1();
        assertEquals(1, t.run(1, Integer.MAX_VALUE));
    }
    public void testB6() {
        T_rem_int_1 t = new T_rem_int_1();
        assertEquals(1, t.run(1, Integer.MIN_VALUE));
    }
    public void testE1() {
        T_rem_int_1 t = new T_rem_int_1();
        try {
            t.run(1, 0);
            fail("expected ArithmeticException");
        } catch (ArithmeticException ae) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.rem_int.d.T_rem_int_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.rem_int.d.T_rem_int_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.rem_int.d.T_rem_int_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.rem_int.d.T_rem_int_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
