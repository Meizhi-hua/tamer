public class InvokeGenericTest {
    static int verbosity = 0;
    static {
        String vstr = System.getProperty("test.java.lang.invoke.InvokeGenericTest.verbosity");
        if (vstr != null)  verbosity = Integer.parseInt(vstr);
    }
    @Test
    public void testFirst() throws Throwable {
        verbosity += 9; try {
        } finally { printCounts(); verbosity -= 9; }
    }
    public InvokeGenericTest() {
    }
    @Before
    public void checkImplementedPlatform() {
        boolean platformOK = false;
        Properties properties = System.getProperties();
        String vers = properties.getProperty("java.vm.version");
        String name = properties.getProperty("java.vm.name");
        String arch = properties.getProperty("os.arch");
        if ((arch.equals("amd64") || arch.equals("i386") || arch.equals("x86") ||
             arch.equals("sparc") || arch.equals("sparcv9")) &&
            (name.contains("Client") || name.contains("Server"))
            ) {
            platformOK = true;
        } else {
            System.err.println("Skipping tests for unsupported platform: "+Arrays.asList(vers, name, arch));
        }
        assumeTrue(platformOK);
    }
    String testName;
    static int allPosTests, allNegTests;
    int posTests, negTests;
    @After
    public void printCounts() {
        if (verbosity >= 2 && (posTests | negTests) != 0) {
            System.out.println();
            if (posTests != 0)  System.out.println("=== "+testName+": "+posTests+" positive test cases run");
            if (negTests != 0)  System.out.println("=== "+testName+": "+negTests+" negative test cases run");
            allPosTests += posTests;
            allNegTests += negTests;
            posTests = negTests = 0;
        }
    }
    void countTest(boolean positive) {
        if (positive) ++posTests;
        else          ++negTests;
    }
    void countTest() { countTest(true); }
    void startTest(String name) {
        if (testName != null)  printCounts();
        if (verbosity >= 1)
            System.out.println("["+name+"]");
        posTests = negTests = 0;
        testName = name;
    }
    @BeforeClass
    public static void setUpClass() throws Exception {
        calledLog.clear();
        calledLog.add(null);
        nextArgVal = INITIAL_ARG_VAL;
    }
    @AfterClass
    public static void tearDownClass() throws Exception {
        int posTests = allPosTests, negTests = allNegTests;
        if (verbosity >= 2 && (posTests | negTests) != 0) {
            System.out.println();
            if (posTests != 0)  System.out.println("=== "+posTests+" total positive test cases");
            if (negTests != 0)  System.out.println("=== "+negTests+" total negative test cases");
        }
    }
    static List<Object> calledLog = new ArrayList<Object>();
    static Object logEntry(String name, Object... args) {
        return Arrays.asList(name, Arrays.asList(args));
    }
    static Object called(String name, Object... args) {
        Object entry = logEntry(name, args);
        calledLog.add(entry);
        return entry;
    }
    static void assertCalled(String name, Object... args) {
        Object expected = logEntry(name, args);
        Object actual   = calledLog.get(calledLog.size() - 1);
        if (expected.equals(actual) && verbosity < 9)  return;
        System.out.println("assertCalled "+name+":");
        System.out.println("expected:   "+expected);
        System.out.println("actual:     "+actual);
        System.out.println("ex. types:  "+getClasses(expected));
        System.out.println("act. types: "+getClasses(actual));
        assertEquals("previous method call", expected, actual);
    }
    static void printCalled(MethodHandle target, String name, Object... args) {
        if (verbosity >= 3)
            System.out.println("calling MH="+target+" to "+name+Arrays.toString(args));
    }
    static Object castToWrapper(Object value, Class<?> dst) {
        Object wrap = null;
        if (value instanceof Number)
            wrap = castToWrapperOrNull(((Number)value).longValue(), dst);
        if (value instanceof Character)
            wrap = castToWrapperOrNull((char)(Character)value, dst);
        if (wrap != null)  return wrap;
        return dst.cast(value);
    }
    static Object castToWrapperOrNull(long value, Class<?> dst) {
        if (dst == int.class || dst == Integer.class)
            return (int)(value);
        if (dst == long.class || dst == Long.class)
            return (long)(value);
        if (dst == char.class || dst == Character.class)
            return (char)(value);
        if (dst == short.class || dst == Short.class)
            return (short)(value);
        if (dst == float.class || dst == Float.class)
            return (float)(value);
        if (dst == double.class || dst == Double.class)
            return (double)(value);
        if (dst == byte.class || dst == Byte.class)
            return (byte)(value);
        if (dst == boolean.class || dst == boolean.class)
            return ((value % 29) & 1) == 0;
        return null;
    }
    static final int ONE_MILLION = (1000*1000),  
                     TEN_BILLION = (10*1000*1000*1000),  
                     INITIAL_ARG_VAL = ONE_MILLION << 1;  
    static long nextArgVal;
    static long nextArg(boolean moreBits) {
        long val = nextArgVal++;
        long sign = -(val & 1); 
        val >>= 1;
        if (moreBits)
            val += (val % ONE_MILLION) * TEN_BILLION;
        return val ^ sign;
    }
    static int nextArg() {
        return (int) nextArg(false);
    }
    static long nextArg(Class<?> kind) {
        if (kind == long.class   || kind == Long.class ||
            kind == double.class || kind == Double.class)
            return nextArg(true);
        return (long) nextArg();
    }
    static Object randomArg(Class<?> param) {
        Object wrap = castToWrapperOrNull(nextArg(param), param);
        if (wrap != null) {
            return wrap;
        }
        if (param.isInterface()) {
            for (Class<?> c : param.getClasses()) {
                if (param.isAssignableFrom(c) && !c.isInterface())
                    { param = c; break; }
            }
        }
        if (param.isInterface() || param.isAssignableFrom(String.class))
            return "#"+nextArg();
        else
            try {
                return param.newInstance();
            } catch (InstantiationException ex) {
            } catch (IllegalAccessException ex) {
            }
        return null;  
    }
    static Object[] randomArgs(Class<?>... params) {
        Object[] args = new Object[params.length];
        for (int i = 0; i < args.length; i++)
            args[i] = randomArg(params[i]);
        return args;
    }
    static Object[] randomArgs(int nargs, Class<?> param) {
        Object[] args = new Object[nargs];
        for (int i = 0; i < args.length; i++)
            args[i] = randomArg(param);
        return args;
    }
    static final Object ANON_OBJ = new Object();
    static Object zeroArg(Class<?> param) {
        Object x = castToWrapperOrNull(0L, param);
        if (x != null)  return x;
        if (param.isInterface() || param.isAssignableFrom(String.class))  return "\"\"";
        if (param == Object.class)  return ANON_OBJ;
        if (param.getComponentType() != null)  return Array.newInstance(param.getComponentType(), 0);
        return null;
    }
    static Object[] zeroArgs(Class<?>... params) {
        Object[] args = new Object[params.length];
        for (int i = 0; i < args.length; i++)
            args[i] = zeroArg(params[i]);
        return args;
    }
    static Object[] zeroArgs(List<Class<?>> params) {
        return zeroArgs(params.toArray(new Class<?>[0]));
    }
    static <T, E extends T> T[] array(Class<T[]> atype, E... a) {
        return Arrays.copyOf(a, a.length, atype);
    }
    static <T> T[] cat(T[] a, T... b) {
        int alen = a.length, blen = b.length;
        if (blen == 0)  return a;
        T[] c = Arrays.copyOf(a, alen + blen);
        System.arraycopy(b, 0, c, alen, blen);
        return c;
    }
    static Integer[] boxAll(int... vx) {
        Integer[] res = new Integer[vx.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = vx[i];
        }
        return res;
    }
    static Object getClasses(Object x) {
        if (x == null)  return x;
        if (x instanceof String)  return x;  
        if (x instanceof List) {
            Object[] xa = ((List)x).toArray();
            for (int i = 0; i < xa.length; i++)
                xa[i] = getClasses(xa[i]);
            return Arrays.asList(xa);
        }
        return x.getClass().getSimpleName();
    }
    static MethodHandle changeArgTypes(MethodHandle target, Class<?> argType) {
        return changeArgTypes(target, 0, 999, argType);
    }
    static MethodHandle changeArgTypes(MethodHandle target,
            int beg, int end, Class<?> argType) {
        MethodType targetType = target.type();
        end = Math.min(end, targetType.parameterCount());
        ArrayList<Class<?>> argTypes = new ArrayList<Class<?>>(targetType.parameterList());
        Collections.fill(argTypes.subList(beg, end), argType);
        MethodType ttype2 = MethodType.methodType(targetType.returnType(), argTypes);
        return target.asType(ttype2);
    }
    static final Lookup LOOKUP = MethodHandles.lookup();
    Map<List<Class<?>>, MethodHandle> CALLABLES = new HashMap<List<Class<?>>, MethodHandle>();
    MethodHandle callable(List<Class<?>> params) {
        MethodHandle mh = CALLABLES.get(params);
        if (mh == null) {
            mh = collector_MH.asType(methodType(Object.class, params));
            CALLABLES.put(params, mh);
        }
        return mh;
    }
    MethodHandle callable(Class<?>... params) {
        return callable(Arrays.asList(params));
    }
    private static Object collector(Object... args) {
        return Arrays.asList(args);
    }
    private static final MethodHandle collector_MH;
    static {
        try {
            collector_MH
                = LOOKUP.findStatic(LOOKUP.lookupClass(),
                                    "collector",
                                    methodType(Object.class, Object[].class));
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
    @Test
    public void testSimple() throws Throwable {
        startTest("testSimple");
        countTest();
        String[] args = { "one", "two" };
        MethodHandle mh = callable(Object.class, String.class);
        Object res; List resl;
        res = resl = (List) mh.invoke((String)args[0], (Object)args[1]);
        assertEquals(Arrays.asList(args), res);
    }
    @Test
    public void testSimplePrims() throws Throwable {
        startTest("testSimplePrims");
        countTest();
        int[] args = { 1, 2 };
        MethodHandle mh = callable(Object.class, Object.class);
        Object res; List resl;
        res = resl = (List) mh.invoke(args[0], args[1]);
        assertEquals(Arrays.toString(args), res.toString());
    }
    @Test
    public void testAlternateName() throws Throwable {
        startTest("testAlternateName");
        countTest();
        String[] args = { "one", "two" };
        MethodHandle mh = callable(Object.class, String.class);
        Object res; List resl;
        res = resl = (List) mh.invoke((String)args[0], (Object)args[1]);
        assertEquals(Arrays.asList(args), res);
    }
    @Test
    public void testWrongArgumentCount() throws Throwable {
        startTest("testWrongArgumentCount");
        for (int i = 0; i <= 10; i++) {
            testWrongArgumentCount(Collections.<Class<?>>nCopies(i, Integer.class));
            if (i <= 4) {
                testWrongArgumentCount(Collections.<Class<?>>nCopies(i, int.class));
                testWrongArgumentCount(Collections.<Class<?>>nCopies(i, long.class));
            }
        }
    }
    public void testWrongArgumentCount(List<Class<?>> params) throws Throwable {
        int max = params.size();
        for (int i = 0; i < max; i++) {
            List<Class<?>> params2 = params.subList(0, i);
            for (int k = 0; k <= 2; k++) {
                if (k == 1)  params  = methodType(Object.class,  params).generic().parameterList();
                if (k == 2)  params2 = methodType(Object.class, params2).generic().parameterList();
                testWrongArgumentCount(params, params2);
                testWrongArgumentCount(params2, params);
            }
        }
    }
    public void testWrongArgumentCount(List<Class<?>> expect, List<Class<?>> observe) throws Throwable {
        countTest(false);
        if (expect.equals(observe))
            assert(false);
        MethodHandle target = callable(expect);
        Object[] args = zeroArgs(observe);
        Object junk;
        try {
            switch (args.length) {
            case 0:
                junk = target.invoke(); break;
            case 1:
                junk = target.invoke(args[0]); break;
            case 2:
                junk = target.invoke(args[0], args[1]); break;
            case 3:
                junk = target.invoke(args[0], args[1], args[2]); break;
            case 4:
                junk = target.invoke(args[0], args[1], args[2], args[3]); break;
            default:
                junk = target.invokeWithArguments(args); break;
            }
        } catch (WrongMethodTypeException ex) {
            return;
        } catch (Exception ex) {
            throw new RuntimeException("wrong exception calling "+target+" on "+Arrays.asList(args), ex);
        }
        throw new RuntimeException("bad success calling "+target+" on "+Arrays.asList(args));
    }
    static List<MethodType> allMethodTypes(int minargc, int maxargc, Class<?>... types) {
        ArrayList<MethodType> result = new ArrayList<MethodType>();
        if (types.length > 0) {
            ArrayList<MethodType> argcTypes = new ArrayList<MethodType>();
            for (Class<?> rtype : types) {
                argcTypes.add(MethodType.methodType(rtype));
            }
            if (types[0] == void.class)
                types = Arrays.copyOfRange(types, 1, types.length);
            for (int argc = 0; argc <= maxargc; argc++) {
                if (argc >= minargc)
                    result.addAll(argcTypes);
                if (argc >= maxargc)
                    break;
                ArrayList<MethodType> prevTypes = argcTypes;
                argcTypes = new ArrayList<MethodType>();
                for (MethodType prevType : prevTypes) {
                    for (Class<?> ptype : types) {
                        argcTypes.add(prevType.insertParameterTypes(argc, ptype));
                    }
                }
            }
        }
        return Collections.unmodifiableList(result);
    }
    static List<MethodType> allMethodTypes(int argc, Class<?>... types) {
        return allMethodTypes(argc, argc, types);
    }
    MethodHandle toString_MH;
    @Test
    public void testReferenceConversions() throws Throwable {
        startTest("testReferenceConversions");
        toString_MH = LOOKUP.
            findVirtual(Object.class, "toString", MethodType.methodType(String.class));
        Object[] args = { "one", "two" };
        for (MethodType type : allMethodTypes(2, Object.class, String.class, CharSequence.class)) {
            testReferenceConversions(type, args);
        }
    }
    public void testReferenceConversions(MethodType type, Object... args) throws Throwable {
        countTest();
        int nargs = args.length;
        List<Object> argList = Arrays.asList(args);
        String expectString = argList.toString();
        if (verbosity > 3)  System.out.println("target type: "+type+expectString);
        MethodHandle mh = callable(type.parameterList());
        mh = MethodHandles.filterReturnValue(mh, toString_MH);
        mh = mh.asType(type);
        Object res = null;
        if (nargs == 2) {
            res = mh.invoke((Object)args[0], (Object)args[1]);
            assertEquals(expectString, res);
            res = mh.invoke((String)args[0], (Object)args[1]);
            assertEquals(expectString, res);
            res = mh.invoke((Object)args[0], (String)args[1]);
            assertEquals(expectString, res);
            res = mh.invoke((String)args[0], (String)args[1]);
            assertEquals(expectString, res);
            res = mh.invoke((String)args[0], (CharSequence)args[1]);
            assertEquals(expectString, res);
            res = mh.invoke((CharSequence)args[0], (Object)args[1]);
            assertEquals(expectString, res);
            res = (String) mh.invoke((Object)args[0], (Object)args[1]);
            assertEquals(expectString, res);
            res = (String) mh.invoke((String)args[0], (Object)args[1]);
            assertEquals(expectString, res);
            res = (CharSequence) mh.invoke((String)args[0], (Object)args[1]);
            assertEquals(expectString, res);
        } else {
            assert(false);  
        }
    }
    @Test
    public void testBoxConversions() throws Throwable {
        startTest("testBoxConversions");
        countTest();
        Object[] args = { 1, 2 };
        MethodHandle mh = callable(Object.class, int.class);
        Object res; List resl; int resi;
        res = resl = (List) mh.invoke((int)args[0], (Object)args[1]);
        assertEquals(Arrays.asList(args), res);
        mh = MethodHandles.identity(int.class);
        mh = MethodHandles.dropArguments(mh, 1, int.class);
        res = resi = (int) mh.invoke((Object) args[0], (Object) args[1]);
        assertEquals(args[0], res);
        res = resi = (int) mh.invoke((int) args[0], (Object) args[1]);
        assertEquals(args[0], res);
    }
}
