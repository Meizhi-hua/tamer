public class Test_dadd extends DxTestCase {
    public void testN1() {
        T_dadd_1 t = new T_dadd_1();
        assertEquals(5.84d, t.run(2.7d, 3.14d));
    }
    public void testN2() {
        T_dadd_1 t = new T_dadd_1();
        assertEquals(-3.14d, t.run(0, -3.14d));
    }
    public void testN3() {
        T_dadd_1 t = new T_dadd_1();
        assertEquals(-5.84d, t.run(-3.14d, -2.7d));
    }
    public void testB1() {
        T_dadd_1 t = new T_dadd_1();
        assertEquals(Double.NaN, t.run(Double.MAX_VALUE, Double.NaN));
    }
    public void testB2() {
        T_dadd_1 t = new T_dadd_1();
        assertEquals(Double.NaN, t.run(Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY));
    }
    public void testB3() {
        T_dadd_1 t = new T_dadd_1();
        assertEquals(Double.POSITIVE_INFINITY, t.run(Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY));
    }
    public void testB4() {
        T_dadd_1 t = new T_dadd_1();
        assertEquals(Double.POSITIVE_INFINITY, t.run(Double.POSITIVE_INFINITY,
                -2.7d));
    }
    public void testB5() {
        T_dadd_1 t = new T_dadd_1();
        assertEquals(+0d, t.run(+0d, -0d));
    }
    public void testB6() {
        T_dadd_1 t = new T_dadd_1();
        assertEquals(-0d, t.run(-0d, -0d));
    }
    public void testB7() {
        T_dadd_1 t = new T_dadd_1();
        assertEquals(+0d, t.run(-2.7d, 2.7d));
    }
    public void testB8() {
        T_dadd_1 t = new T_dadd_1();
        assertEquals(Double.POSITIVE_INFINITY, t.run(Double.MAX_VALUE,
                Double.MAX_VALUE));
    }
    public void testB9() {
        T_dadd_1 t = new T_dadd_1();
        assertEquals(0d, t.run(Double.MIN_VALUE, -4.9E-324));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dadd.jm.T_dadd_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.dadd.jm.T_dadd_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.dadd.jm.T_dadd_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.dadd.jm.T_dadd_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
