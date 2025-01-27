class MethodExitReturnValuesTarg {
    static URL[] urls = new URL[1];
    public static byte      byteValue = 89;
    public static char      charValue = 'x';
    public static double    doubleValue = 2.2;
    public static float     floatValue = 3.3f;
    public static int       intValue = 1;
    public static long      longValue = Long.MAX_VALUE;
    public static short     shortValue = 8;
    public static boolean   booleanValue = false;
    public static Class       classValue = Object.class;
    public static ClassLoader classLoaderValue;
    {
        try {
            urls[0] = new URL("hi there");
        } catch (java.net.MalformedURLException ee) {
        }
        classLoaderValue = new URLClassLoader(urls);
    }
    public static Thread      threadValue;
    public static ThreadGroup threadGroupValue;
    public static String      stringValue = "abc";
    public static int[]       intArrayValue = new int[] {1, 2, 3};
    public static MethodExitReturnValuesTarg  objectValue =
        new MethodExitReturnValuesTarg();
    public String ivar = stringValue;
    public static byte[]    arrByte      = new byte[]    {byteValue};
    public static char[]    arrChar      = new char[]    {charValue};
    public static double[]  arrDouble    = new double[]  {doubleValue};
    public static float[]   arrFloat     = new float[]   {floatValue};
    public static int[]     arrInt       = new int[]     {intValue};
    public static long[]    arrLong      = new long[]    {longValue};
    public static short[]   arrShort     = new short[]   {shortValue};
    public static boolean[] arrBoolean   = new boolean[] {booleanValue};
    public static Object[]  arrObject    = new Object[]  {objectValue};
    public static String s_show(String p1) { return p1;}
    public static byte s_bytef()      { return byteValue; }
    public static char s_charf()      { return charValue; }
    public static double s_doublef()  { return doubleValue; }
    public static float s_floatf()    { return floatValue; }
    public static int s_intf()        { return intValue; }
    public static long s_longf()      { return longValue; }
    public static short s_shortf()    { return shortValue; }
    public static boolean s_booleanf(){ return booleanValue; }
    public static String s_stringf()  { return stringValue; }
    public static Class s_classf()    { return classValue; }
    public static ClassLoader s_classLoaderf()
                                      { return classLoaderValue; }
    public static Thread s_threadf()  { threadValue = Thread.currentThread();
                                        return threadValue; }
    public static ThreadGroup s_threadGroupf()
                                      { threadGroupValue = threadValue.getThreadGroup();
                                        return threadGroupValue; }
    public static int[] s_intArrayf() { return intArrayValue; }
    public static Object s_nullObjectf() { return null; }
    public static Object s_objectf()  { return objectValue; }
    public static void s_voidf()      {}
    public byte i_bytef()            { return byteValue; }
    public char i_charf()            { return charValue; }
    public double i_doublef()        { return doubleValue; }
    public float i_floatf()          { return floatValue; }
    public int i_intf()              { return intValue; }
    public long i_longf()            { return longValue; }
    public short i_shortf()          { return shortValue; }
    public boolean i_booleanf()      { return booleanValue; }
    public String i_stringf()        { return stringValue; }
    public Class i_classf()          { return classValue; }
    public ClassLoader i_classLoaderf()
                                     { return classLoaderValue; }
    public Thread i_threadf()        { return threadValue; }
    public ThreadGroup i_threadGroupf()
                                     { return threadGroupValue; }
    public int[] i_intArrayf()       { return intArrayValue; }
    public Object i_nullObjectf()    { return null; }
    public Object i_objectf()        { return objectValue; }
    public void i_voidf()            {}
    static void doit(MethodExitReturnValuesTarg xx) {
        s_show("==========  Testing static methods ================");
        s_bytef();
        s_charf();
        s_doublef();
        s_floatf();
        s_intf();
        s_longf();
        s_shortf();
        s_booleanf();
        s_stringf();
        s_classf();
        s_classLoaderf();
        s_threadf();
        s_threadGroupf();
        s_intArrayf();
        s_nullObjectf();
        s_objectf();
        s_voidf();
        s_show("==========  Testing instance methods ================");
        xx.i_bytef();
        xx.i_charf();
        xx.i_doublef();
        xx.i_floatf();
        xx.i_intf();
        xx.i_longf();
        xx.i_shortf();
        xx.i_booleanf();
        xx.i_stringf();
        xx.i_intArrayf();
        xx.i_classf();
        xx.i_classLoaderf();
        xx.i_threadf();
        xx.i_threadGroupf();
        xx.i_nullObjectf();
        xx.i_objectf();
        xx.i_voidf();
        s_show("==========  Testing native methods ================");
        StrictMath.sin(doubleValue);
        Array.getByte(arrByte, 0);
        Array.getChar(arrChar, 0);
        Array.getDouble(arrDouble, 0);
        Array.getFloat(arrFloat, 0);
        Array.getInt(arrInt, 0);
        Array.getLong(arrLong, 0);
        Array.getShort(arrShort, 0);
        Array.getBoolean(arrBoolean, 0);
        Array.get(arrObject, 0);
        stringValue.intern();
    }
    public static void main(String[] args) {
        MethodExitReturnValuesTarg xx =
            new MethodExitReturnValuesTarg();
        doit(xx);
    }
}
public class MethodExitReturnValuesTest extends TestScaffold {
    private String[] excludes = {
        "javax.*",
        "sun.*",
        "com.sun.*"};
    static VirtualMachineManager vmm ;
    ClassType targetClass;
    Field theValueField;
    static int successes = 0;
    static final int expectedSuccesses = 44;  
    MethodExitReturnValuesTest(String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        MethodExitReturnValuesTest meee = new MethodExitReturnValuesTest(args);
        vmm = Bootstrap.virtualMachineManager();
        meee.startTests();
    }
    void ckByteValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("byteValue");
        ByteValue theValue = (ByteValue)targetClass.getValue(theValueField);
        byte vv = theValue.value();
        byte rv = ((ByteValue)retValue).value();
        if (vv != rv) {
            failure("failure: byte: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: byte " + rv);
            successes++;
        }
    }
    void ckCharValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("charValue");
        CharValue theValue = (CharValue)targetClass.getValue(theValueField);
        char vv = theValue.value();
        char rv = ((CharValue)retValue).value();
        if (vv != rv) {
            failure("failure: char: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: char " + rv);
            successes++;
        }
    }
    void ckDoubleValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("doubleValue");
        DoubleValue theValue = (DoubleValue)targetClass.getValue(theValueField);
        double vv = theValue.value();
        double rv = ((DoubleValue)retValue).value();
        if (vv != rv) {
            failure("failure: double: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: double " + rv);
            successes++;
        }
    }
    void ckFloatValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("floatValue");
        FloatValue theValue = (FloatValue)targetClass.getValue(theValueField);
        float vv = theValue.value();
        float rv = ((FloatValue)retValue).value();
        if (vv != rv) {
            failure("failure: float: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: float " + rv);
            successes++;
        }
    }
    void ckIntValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("intValue");
        IntegerValue theValue = (IntegerValue)targetClass.getValue(theValueField);
        int vv = theValue.value();
        int rv = ((IntegerValue)retValue).value();
        if (vv != rv) {
            failure("failure: int: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: int " + rv);
            successes++;
        }
    }
    void ckLongValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("longValue");
        LongValue theValue = (LongValue)targetClass.getValue(theValueField);
        long vv = theValue.value();
        long rv = ((LongValue)retValue).value();
        if (vv != rv) {
            failure("failure: long: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: long " + rv);
            successes++;
        }
    }
    void ckShortValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("shortValue");
        ShortValue theValue = (ShortValue)targetClass.getValue(theValueField);
        short vv = theValue.value();
        short rv = ((ShortValue)retValue).value();
        if (vv != rv) {
            failure("failure: short: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: short " + rv);
            successes++;
        }
    }
    void ckBooleanValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("booleanValue");
        BooleanValue theValue = (BooleanValue)targetClass.getValue(theValueField);
        boolean vv = theValue.value();
        boolean rv = ((BooleanValue)retValue).value();
        if (vv != rv) {
            failure("failure: boolean: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: boolean " + rv);
            successes++;
        }
    }
    void ckStringValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("stringValue");
        StringReference theValue = (StringReference)targetClass.getValue(theValueField);
        String vv = theValue.value();
        String rv = ((StringReference)retValue).value();
        if (vv != rv) {
            failure("failure: String: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: String: " + rv);
            successes++;
        }
    }
    void ckClassValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("classValue");
        ClassObjectReference vv = (ClassObjectReference)targetClass.
            getValue(theValueField);
        ClassObjectReference rv = (ClassObjectReference)retValue;
        if (vv != rv) {
            failure("failure: Class: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: Class: " + rv);
            successes++;
        }
    }
    void ckClassLoaderValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("classLoaderValue");
        ClassLoaderReference vv = (ClassLoaderReference)targetClass.
            getValue(theValueField);
        ClassLoaderReference rv = (ClassLoaderReference)retValue;
        if (vv != rv) {
            failure("failure: ClassLoader: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: ClassLoader: " + rv);
            successes++;
        }
    }
    void ckThreadValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("threadValue");
        ThreadReference vv = (ThreadReference)targetClass.
            getValue(theValueField);
        ThreadReference rv = (ThreadReference)retValue;
        if (vv != rv) {
            failure("failure: Thread: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: Thread: " + rv);
            successes++;
        }
    }
    void ckThreadGroupValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("threadGroupValue");
        ThreadGroupReference vv = (ThreadGroupReference)targetClass.
            getValue(theValueField);
        ThreadGroupReference rv = (ThreadGroupReference)retValue;
        if (vv != rv) {
            failure("failure: ThreadgGroup: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: ThreadGroup: " + rv);
            successes++;
        }
    }
    void ckArrayValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("intArrayValue");
        ArrayReference theValue = (ArrayReference)targetClass.getValue(theValueField);
        IntegerValue theElem2 = (IntegerValue)theValue.getValue(2);
        ArrayReference theRetValue = (ArrayReference)retValue;
        IntegerValue retElem2 = (IntegerValue)theRetValue.getValue(2);
        int vv = theElem2.value();
        int rv = retElem2.value();
        if (vv != rv) {
            failure("failure: in[2]: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: int[2]: " + rv);
            successes++;
        }
    }
    void ckNullObjectValue(Value retValue) {
        if (retValue != null) {
            failure("failure: NullObject: expected " + null + ", got " + retValue);
        } else {
            System.out.println("Passed: NullObject: " + retValue);
            successes++;
        }
    }
    void ckObjectValue(Value retValue) {
        Field theValueField = targetClass.fieldByName("stringValue");
        StringReference theValue = (StringReference)targetClass.getValue(theValueField);
        Field theIVarField = targetClass.fieldByName("ivar");
        ObjectReference theRetValue = (ObjectReference)retValue;
        StringReference theRetValField = (StringReference)theRetValue.getValue(theIVarField);
        String vv = theValue.value();
        String rv = theRetValField.value();
        if (vv != rv) {
            failure("failure: Object: expected " + vv + ", got " + rv);
       } else {
            System.out.println("Passed: Object: " + rv);
            successes++;
        }
    }
    void ckVoidValue(Value retValue) {
        System.out.println("Passed: Void");
        successes++;
    }
    void ckSinValue(Value retValue) {
        double rv = ((DoubleValue)retValue).value();
        double vv = StrictMath.sin(MethodExitReturnValuesTarg.doubleValue);
        if (rv != vv) {
            failure("failure: sin: expected " + vv + ", got " + rv);
        } else {
            System.out.println("Passed: sin " + rv);
            successes++;
        }
    }
    public void methodExited(MethodExitEvent event) {
        String origMethodName = event.method().name();
        if (vmm.majorInterfaceVersion() >= 1 &&
            vmm.minorInterfaceVersion() >= 6 &&
            vm().canGetMethodReturnValues()) {
            Value retValue = event.returnValue();
            if ("sin".equals(origMethodName)) {
                ckSinValue(retValue);
                return;
            }
            if (!origMethodName.startsWith("s_") &&
                !origMethodName.startsWith("i_")) {
                if ("getByte".equals(origMethodName))         ckByteValue(retValue);
                else if ("getChar".equals(origMethodName))    ckCharValue(retValue);
                else if ("getDouble".equals(origMethodName))  ckDoubleValue(retValue);
                else if ("getFloat".equals(origMethodName))   ckFloatValue(retValue);
                else if ("getInt".equals(origMethodName))     ckIntValue(retValue);
                else if ("getLong".equals(origMethodName))    ckLongValue(retValue);
                else if ("getShort".equals(origMethodName))   ckShortValue(retValue);
                else if ("getBoolean".equals(origMethodName)) ckBooleanValue(retValue);
                else if ("getObject".equals(origMethodName))  ckObjectValue(retValue);
                else if ("intern".equals(origMethodName))     ckStringValue(retValue);
                return;
            }
            String methodName = origMethodName.substring(2);
            if ("show".equals(methodName)) {
                System.out.println(retValue);
                return;
            }
            if ("bytef".equals(methodName))             ckByteValue(retValue);
            else if ("charf".equals(methodName))        ckCharValue(retValue);
            else if ("doublef".equals(methodName))      ckDoubleValue(retValue);
            else if ("floatf".equals(methodName))       ckFloatValue(retValue);
            else if ("intf".equals(methodName))         ckIntValue(retValue);
            else if ("longf".equals(methodName))        ckLongValue(retValue);
            else if ("shortf".equals(methodName))       ckShortValue(retValue);
            else if ("booleanf".equals(methodName))     ckBooleanValue(retValue);
            else if ("stringf".equals(methodName))      ckStringValue(retValue);
            else if ("classf".equals(methodName))       ckClassValue(retValue);
            else if ("classLoaderf".equals(methodName)) ckClassLoaderValue(retValue);
            else if ("threadf".equals(methodName))      ckThreadValue(retValue);
            else if ("threadGroupf".equals(methodName)) ckThreadGroupValue(retValue);
            else if ("intArrayf".equals(methodName))    ckArrayValue(retValue);
            else if ("nullObjectf".equals(methodName))  ckNullObjectValue(retValue);
            else if ("objectf".equals(methodName))      ckObjectValue(retValue);
            else if ("voidf".equals(methodName))        ckVoidValue(retValue);
            else {
                failure("failure: Unknown methodName: " + origMethodName);
            }
        } else {
            System.out.println("Return Value not available for method: " + origMethodName);
        }
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("MethodExitReturnValuesTarg");
        targetClass = (ClassType)bpe.location().declaringType();
        mainThread = bpe.thread();
        theValueField = targetClass.fieldByName("theValue");
        MethodExitRequest exitRequest =
            eventRequestManager().createMethodExitRequest();
        for (int i=0; i<excludes.length; ++i) {
            exitRequest.addClassExclusionFilter(excludes[i]);
        }
        int sessionSuspendPolicy = EventRequest.SUSPEND_ALL;
        exitRequest.setSuspendPolicy(sessionSuspendPolicy);
        exitRequest.enable();
        listenUntilVMDisconnect();
        if (successes != expectedSuccesses) {
            failure("failure: Expected " + expectedSuccesses + ", but got " + successes);
        }
        System.out.println("All done, " + successes + " passed");
        if (!testFailed) {
            System.out.println();
            System.out.println("MethodExitReturnValuesTest: passed");
        } else {
            System.out.println();
            System.out.println("MethodExitReturnValuesTest: failed");
            throw new Exception("MethodExitReturnValuesTest: failed");
        }
    }
}
