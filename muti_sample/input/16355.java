public class TooManyFooTest {
    private static final Logger LOG =
            Logger.getLogger(TooManyFooTest.class.getName());
    public static class NumberHolder {
        public Integer getNumber() { return 0;}
        public void setNumber(Integer n) {};
    }
    public static class MyNumberHolder extends NumberHolder {
    }
    public interface Parent1 {
        public int foo(); 
        public Integer barfoo(); 
        public Long    foobar(); 
        public Number  toofoo(); 
        public Object toofoofoo(); 
        public NumberHolder toobarbar(); 
    }
    public interface Parent2 {
        public int foo(); 
        public Number barfoo();
        public Number foobar();
        public Object toofoo();
        public NumberHolder  toofoofoo();
        public Object toobarbar();
    }
    public interface ChildMBean extends Parent1, Parent2 {
        public Long foobar();
        public Long toofoo();
    }
    public interface ChildMXBean extends Parent1, Parent2 {
        public Long foobar();
        public Long toofoo();
    }
    public interface ChildMixMXBean extends ChildMBean, ChildMXBean {
    }
    public static class Child implements ChildMBean {
        public int foo() {return 0;}
        public Long foobar() {return 0L;}
        public Long toofoo() {return 0L;}
        public Integer barfoo() {return 0;}
        public MyNumberHolder toofoofoo() { return null;}
        public MyNumberHolder toobarbar() { return null;}
    }
    public static class ChildMix implements ChildMXBean {
        public int foo() {return 0;}
        public Long foobar() {return 0L;}
        public Long toofoo() {return 0L;}
        public Integer barfoo() {return 0;}
        public MyNumberHolder toofoofoo() { return null;}
        public MyNumberHolder toobarbar() { return null;}
    }
    public static class ChildMixMix extends Child implements ChildMixMXBean {
    }
    public TooManyFooTest() {
    }
    private static final int OPCOUNT;
    private static final Map<String,String> EXPECTED_TYPES;
    private static final String[][] type_array = {
        { "foo", int.class.getName() },
        { "foobar", Long.class.getName()},
        { "toofoo", Long.class.getName()},
        { "barfoo", Integer.class.getName()},
        { "toofoofoo", NumberHolder.class.getName()},
        { "toobarbar", NumberHolder.class.getName()},
    };
    static {
        try {
            final Set<String> declared = new HashSet<String>();
            for (Method m:Child.class.getDeclaredMethods()) {
                declared.add(m.getName()+Arrays.asList(m.getParameterTypes()));
            }
            final Set<String> exposed = new HashSet<String>();
            for (Method m:ChildMBean.class.getMethods()) {
                exposed.add(m.getName()+Arrays.asList(m.getParameterTypes()));
            }
            declared.retainAll(exposed);
            OPCOUNT = declared.size();
            EXPECTED_TYPES = new HashMap<String,String>();
            for (String[] st:type_array) {
                EXPECTED_TYPES.put(st[0],st[1]);
            }
        } catch (Exception x) {
            throw new ExceptionInInitializerError(x);
        }
    }
    private static void test(Object child, String name, boolean mxbean)
        throws Exception {
        final ObjectName childName =
                new ObjectName("test:type=Child,name="+name);
        final MBeanServer server =
                ManagementFactory.getPlatformMBeanServer();
        server.registerMBean(child,childName);
        try {
            final MBeanInfo info = server.getMBeanInfo(childName);
            System.out.println(name+": " + info.getDescriptor());
            final int len = info.getOperations().length;
            if (len == OPCOUNT) {
                System.out.println(name+": OK, only "+OPCOUNT+
                        " operations here...");
            } else {
                final String qual = (len>OPCOUNT)?"many":"few";
                System.err.println(name+": Too "+qual+" foos! Found "+
                        len+", expected "+OPCOUNT);
                for (MBeanOperationInfo op : info.getOperations()) {
                    System.err.println("public "+op.getReturnType()+" "+
                            op.getName()+"();");
                }
                throw new RuntimeException("Too " + qual +
                        " foos for "+name);
            }
            final Descriptor d = info.getDescriptor();
            final String mxstr = String.valueOf(d.getFieldValue("mxbean"));
            final boolean mxb =
                    (mxstr==null)?false:Boolean.valueOf(mxstr).booleanValue();
            System.out.println(name+": mxbean="+mxb);
            if (mxbean && !mxb)
                throw new AssertionError("MXBean is not OpenMBean?");
            for (MBeanOperationInfo mboi : info.getOperations()) {
                if (mxbean && !mboi.getName().equals("foo")) {
                    if (!(mboi instanceof OpenMBeanOperationInfo))
                        throw new AssertionError("Operation "+mboi.getName()+
                                "() is not Open?");
                }
                final String exp = EXPECTED_TYPES.get(mboi.getName());
                String type = (String)mboi.getDescriptor().
                            getFieldValue("originalType");
                if (type == null) type = mboi.getReturnType();
                if (type.equals(exp)) continue;
                System.err.println("Bad return type for "+
                        mboi.getName()+"! Found "+type+
                        ", expected "+exp);
                throw new RuntimeException("Bad return type for "+
                        mboi.getName());
            }
        } finally {
            server.unregisterMBean(childName);
        }
    }
    public static void main(String[] args) throws Exception {
        final Child child = new Child();
        test(child,"Child[MBean]",false);
        final ChildMix childx = new ChildMix();
        test(childx,"ChildMix[MXBean]",true);
        final ChildMixMix childmx = new ChildMixMix();
        test(childmx,"ChildMixMix[MXBean]",false);
        final StandardMBean schild = new StandardMBean(child,ChildMBean.class);
        test(schild,"Child[StandarMBean(Child)]",false);
        final StandardMBean schildx =
                new StandardMBean(childx,ChildMXBean.class,true);
        test(schildx,"ChildMix[StandarMXBean(ChildMix)]",true);
        final StandardMBean schildmx =
                new StandardMBean(childmx,ChildMixMXBean.class,true);
        test(schildmx,"ChildMixMix[StandarMXBean(ChildMixMix)]",true);
    }
}
