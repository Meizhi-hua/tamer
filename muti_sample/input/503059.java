public class Test_lload_1 extends DxTestCase {
    public void testN1() {
        assertEquals(1234567890123l, T_lload_1_1.run());
    }
    public void testN2() {
        assertTrue(T_lload_1_6.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lload_1.jm.T_lload_1_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lload_1.jm.T_lload_1_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lload_1.jm.T_lload_1_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lload_1.jm.T_lload_1_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
