public class Test_if_eqz extends DxTestCase {
    public void testN1() {
        T_if_eqz_1 t = new T_if_eqz_1();
        assertEquals(1234, t.run(5));
        assertEquals(1234, t.run(-5));
    }
    public void testN2() {
        T_if_eqz_2 t = new T_if_eqz_2();
        String str = "abc";
        assertEquals(1234, t.run(str));
    }
    public void testN3() {
        T_if_eqz_3 t = new T_if_eqz_3();
        assertEquals(1234, t.run(3.123f));
    }
    public void testB1() {
        T_if_eqz_1 t = new T_if_eqz_1();
        assertEquals(1234, t.run(Integer.MAX_VALUE));
    }
    public void testB2() {
        T_if_eqz_1 t = new T_if_eqz_1();
        assertEquals(1234, t.run(Integer.MIN_VALUE));
    }
    public void testB3() {
        T_if_eqz_3 t = new T_if_eqz_3();
        assertEquals(1234, t.run(Float.MAX_VALUE));
    }
    public void testB4() {
        T_if_eqz_3 t = new T_if_eqz_3();
        assertEquals(1234, t.run(Float.MIN_VALUE));
    }
    public void testB5() {
        T_if_eqz_1 t = new T_if_eqz_1();
        assertEquals(1, t.run(0));
    }
    public void testB6() {
        T_if_eqz_4 t = new T_if_eqz_4();
        assertEquals(1, t.run(null));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.if_eqz.d.T_if_eqz_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.if_eqz.d.T_if_eqz_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.if_eqz.d.T_if_eqz_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.if_eqz.d.T_if_eqz_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.if_eqz.d.T_if_eqz_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.if_eqz.d.T_if_eqz_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
