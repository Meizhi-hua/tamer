public class Test_if_gt extends DxTestCase {
    public void testN1() {
        T_if_gt_1 t = new T_if_gt_1();
        assertEquals(1234, t.run(5, 6));
    }
    public void testN2() {
        T_if_gt_1 t = new T_if_gt_1();
        assertEquals(1234, t.run(0x0f0e0d0c, 0x0f0e0d0c));
    }
    public void testN3() {
        T_if_gt_1 t = new T_if_gt_1();
        assertEquals(1, t.run(5, -5));
    }
    public void testN4() {
        T_if_gt_3 t = new T_if_gt_3();
        assertEquals(1, t.run(1f, 1));
    }
    public void testB1() {
        T_if_gt_1 t = new T_if_gt_1();
        assertEquals(1234, t.run(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
    public void testB2() {
        T_if_gt_1 t = new T_if_gt_1();
        assertEquals(1234, t.run(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }
    public void testB3() {
        T_if_gt_1 t = new T_if_gt_1();
        assertEquals(1, t.run(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }
    public void testB4() {
        T_if_gt_1 t = new T_if_gt_1();
        assertEquals(1, t.run(0, Integer.MIN_VALUE));
    }
    public void testB5() {
        T_if_gt_1 t = new T_if_gt_1();
        assertEquals(1234, t.run(0, 0));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.if_gt.d.T_if_gt_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.if_gt.d.T_if_gt_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.if_gt.d.T_if_gt_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.if_gt.d.T_if_gt_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.if_gt.d.T_if_gt_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.if_gt.d.T_if_gt_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.if_gt.d.T_if_gt_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
