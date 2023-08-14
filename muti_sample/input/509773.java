public class Test_ladd extends DxTestCase {
    public void testN1() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(99999999l, t.run(12345678l, 87654321l));
    }
    public void testN2() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(87654321l, t.run(0l, 87654321l));
    }
    public void testN3() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(-12345678l, t.run(-12345678l, 0l));
    }
    public void testB1() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(9223372036854775807L, t.run(0l, Long.MAX_VALUE));
    }
    public void testB2() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(-9223372036854775808L, t.run(0l, Long.MIN_VALUE));
    }
    public void testB3() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(0l, t.run(0l, 0l));
    }
    public void testB4() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(-2, t.run(Long.MAX_VALUE, Long.MAX_VALUE));
    }
    public void testB5() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(-1l, t.run(Long.MAX_VALUE, Long.MIN_VALUE));
    }
    public void testB6() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(0l, t.run(Long.MIN_VALUE, Long.MIN_VALUE));
    }
    public void testB7() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(-9223372036854775807l, t.run(Long.MIN_VALUE, 1l));
    }
    public void testB8() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(-9223372036854775808l, t.run(Long.MAX_VALUE, 1l));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ladd.jm.T_ladd_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ladd.jm.T_ladd_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.ladd.jm.T_ladd_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.ladd.jm.T_ladd_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.ladd.jm.T_ladd_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}