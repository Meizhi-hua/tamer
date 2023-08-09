public class MXBeanAnnotationTest {
    @MXBean
    public static interface Empty {}
    public static class EmptyImpl implements Empty {}
    @MXBean(false)
    public static interface NotMXBean {}
    public static class NotImpl implements NotMXBean {}
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("a:b=c");
        try {
            mbs.registerMBean(new EmptyImpl(), on);
            boolean ok = checkMXBean(mbs.getMBeanInfo(on), true,
                                     "empty MXBean interface");
            mbs.unregisterMBean(on);
            if (ok)
                System.out.println("OK: empty MXBean interface");
        } catch (Exception e) {
            failure = "MXBean with empty interface got exception: " + e;
            System.out.println("FAILED: " + failure);
            e.printStackTrace(System.out);
        }
        try {
            mbs.registerMBean(new NotImpl(), on);
            failure = "Registered a non-Standard MBean with @MXBean(false)";
            System.out.println("FAILED: " + failure);
        } catch (NotCompliantMBeanException e) {
            System.out.println("OK: non-Standard MBean with @MXBean(false) " +
                               "rejected");
        }
        if (failure == null)
            System.out.println("TEST PASSED");
        else
            throw new Exception("TEST FAILED: " + failure);
    }
    private static boolean checkMXBean(MBeanInfo mbi, boolean expected,
                                       String what) {
        Descriptor d = mbi.getDescriptor();
        String mxbean = (String) d.getFieldValue("mxbean");
        boolean is = (mxbean != null && mxbean.equals("true"));
        if (is == expected)
            return true;
        else {
            failure = "MBean should " + (expected ? "" : "not ") +
                "have mxbean=true: " + d;
            return false;
        }
    }
    private static String failure;
}
