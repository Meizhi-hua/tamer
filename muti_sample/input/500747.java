public class Test_t482_14 extends DxTestCase {
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.verify.t482_14.jm.T_t482_14_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.verify.t482_14.jm.T_t482_14_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}