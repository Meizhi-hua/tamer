public class Test_fstore_1 extends DxTestCase {
    public void testN1() {
        assertEquals(2f, T_fstore_1_1.run());
    }
    public void testN2() {
        assertTrue(T_fstore_1_5.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fstore_1.jm.T_fstore_1_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.fstore_1.jm.T_fstore_1_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.fstore_1.jm.T_fstore_1_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
