public class Test_xor_int_2addr extends DxTestCase {
    public void testN1() {
         T_xor_int_2addr_1 t = new T_xor_int_2addr_1();
         assertEquals(7, t.run(15, 8));
    }
    public void testN2() {
         T_xor_int_2addr_1 t = new T_xor_int_2addr_1();
         assertEquals(9, t.run(0xfffffff8, 0xfffffff1));
    }
    public void testN3() {
         T_xor_int_2addr_1 t = new T_xor_int_2addr_1();
         assertEquals(0xFFFF3501, t.run(0xcafe, -1));
    }
    public void testN4() {
        T_xor_int_2addr_4 t = new  T_xor_int_2addr_4();
        try {
            t.run(1, 3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_xor_int_2addr_1 t = new T_xor_int_2addr_1();
        assertEquals(-1, t.run(0, -1));
    }
    public void testB2() {
        T_xor_int_2addr_1 t = new T_xor_int_2addr_1();
        assertEquals(0xffffffff, t.run(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.xor_int_2addr.d.T_xor_int_2addr_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.xor_int_2addr.d.T_xor_int_2addr_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.xor_int_2addr.d.T_xor_int_2addr_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
