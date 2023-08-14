public class Test_if_ltz extends DxTestCase {
    public void testN1() {
        T_if_ltz_1 t = new T_if_ltz_1();
        assertEquals(1234, t.run(5));
    }
    public void testN2() {
        T_if_ltz_1 t = new T_if_ltz_1();
        assertEquals(1, t.run(-5));
    }
    public void testN3() {
        T_if_ltz_2 t = new T_if_ltz_2();
        assertEquals(1234, t.run(1.123f));
    }
    public void testB1() {
        T_if_ltz_1 t = new T_if_ltz_1();
        assertEquals(1234, t.run(Integer.MAX_VALUE));
    }
    public void testB2() {
        T_if_ltz_1 t = new T_if_ltz_1();
        assertEquals(1, t.run(Integer.MIN_VALUE));
    }
    public void testB3() {
        T_if_ltz_1 t = new T_if_ltz_1();
        assertEquals(1234, t.run(0));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.if_ltz.d.T_if_ltz_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.if_ltz.d.T_if_ltz_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.if_ltz.d.T_if_ltz_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.if_ltz.d.T_if_ltz_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.if_ltz.d.T_if_ltz_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.if_ltz.d.T_if_ltz_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.if_ltz.d.T_if_ltz_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}