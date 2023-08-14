public class Test_div_double_2addr extends DxTestCase {
    public void testN1() {
        T_div_double_2addr_1 t = new T_div_double_2addr_1();
        assertEquals(0.8598726114649682d, t.run(2.7d, 3.14d));
    }
    public void testN2() {
        T_div_double_2addr_1 t = new T_div_double_2addr_1();
        assertEquals(0d, t.run(0, 3.14d));
    }
    public void testN3() {
        T_div_double_2addr_1 t = new T_div_double_2addr_1();
        assertEquals(-1.162962962962963d, t.run(-3.14d, 2.7d));
    }
    public void testN4() {
        T_div_double_2addr_1 t = new T_div_double_2addr_1();
        assertEquals(-1.162962962962963d, t.run(-3.14d, 2.7d));
    }
    public void testN5() {
        T_div_double_2addr_3 t = new T_div_double_2addr_3();
        try {
            t.run(3.14d, 12345l);
        } catch (Throwable e) {
        }
    }    
    public void testB2() {
        T_div_double_2addr_1 t = new T_div_double_2addr_1();
        assertEquals(Double.NaN, t.run(Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY));
    }
    public void testB3() {
        T_div_double_2addr_1 t = new T_div_double_2addr_1();
        assertEquals(Double.NEGATIVE_INFINITY, t.run(Double.POSITIVE_INFINITY,
                -2.7d));
    }
    public void testB4() {
        T_div_double_2addr_1 t = new T_div_double_2addr_1();
        assertEquals(0d, t.run(-2.7d, Double.NEGATIVE_INFINITY));
    }
    public void testB5() {
        T_div_double_2addr_1 t = new T_div_double_2addr_1();
        assertEquals(Double.NaN, t.run(0, 0));
    }
    public void testB6() {
        T_div_double_2addr_1 t = new T_div_double_2addr_1();
        assertEquals(-0d, t.run(0, -2.7d));
    }
    public void testB7() {
        T_div_double_2addr_1 t = new T_div_double_2addr_1();
        assertEquals(Double.NEGATIVE_INFINITY, t.run(-2.7d, 0));
    }
    public void testB8() {
        T_div_double_2addr_1 t = new T_div_double_2addr_1();
        assertEquals(Double.POSITIVE_INFINITY, t.run(1, Double.MIN_VALUE));
    }
    public void testB9() {
        T_div_double_2addr_1 t = new T_div_double_2addr_1();
        assertEquals(Double.NEGATIVE_INFINITY, t.run(Double.MAX_VALUE, -1E-9f));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.div_double_2addr.d.T_div_double_2addr_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.div_double_2addr.d.T_div_double_2addr_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.div_double_2addr.d.T_div_double_2addr_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.div_double_2addr.d.T_div_double_2addr_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}