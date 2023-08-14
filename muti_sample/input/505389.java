public class Test_const_wide_32 extends DxTestCase {
    public void testN1() {
        T_const_wide_32_1 t = new T_const_wide_32_1();
         long a = 10000000l;
         long b = 10000000l;
        assertEquals(a + b, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.const_wide_32.d.T_const_wide_32_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.const_wide_32.d.T_const_wide_32_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}