public class Test_d2l extends DxTestCase {
    public void testN1() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(2l, t.run(2.9999999d));
    }
    public void testN2() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(1l, t.run(1d));
    }
    public void testN3() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(-1l, t.run(-1d));
    }
    public void testB1() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(Long.MAX_VALUE, t.run(Double.MAX_VALUE));
    }
    public void testB2() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(0, t.run(Double.MIN_VALUE));
    }
    public void testB3() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(0l, t.run(-0));
    }
    public void testB4() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(0l, t.run(Double.NaN));
    }
    public void testB5() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(Long.MAX_VALUE, t.run(Double.POSITIVE_INFINITY));
    }
    public void testB6() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(Long.MIN_VALUE, t.run(Double.NEGATIVE_INFINITY));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.d2l.jm.T_d2l_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.d2l.jm.T_d2l_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.d2l.jm.T_d2l_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.d2l.jm.T_d2l_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
