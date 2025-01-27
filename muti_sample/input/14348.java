public class TestProvider extends RMIClassLoaderSpi {
    public static final Method loadClassMethod;
    public static final Method loadProxyClassMethod;
    public static final Method getClassLoaderMethod;
    public static final Method getClassAnnotationMethod;
    static {
        try {
            loadClassMethod = RMIClassLoaderSpi.class.getMethod(
                "loadClass", new Class[] {
                    String.class, String.class, ClassLoader.class
                });
            loadProxyClassMethod = RMIClassLoaderSpi.class.getMethod(
                "loadProxyClass", new Class[] {
                    String.class, String[].class, ClassLoader.class
                });
            getClassLoaderMethod = RMIClassLoaderSpi.class.getMethod(
                "getClassLoader", new Class[] { String.class });
            getClassAnnotationMethod = RMIClassLoaderSpi.class.getMethod(
                "getClassAnnotation", new Class[] { Class.class });
        } catch (NoSuchMethodException e) {
            Error error = new NoSuchMethodError();
            error.initCause(e);
            throw error;
        }
    }
    public static final Class loadClassReturn =
        (new Object() { }).getClass();
    public static final Class loadProxyClassReturn =
        (new Object() { }).getClass();
    public static final ClassLoader getClassLoaderReturn =
        URLClassLoader.newInstance(new URL[0]);
    public static final String getClassAnnotationReturn = new String();
    public static List invocations =
        Collections.synchronizedList(new ArrayList(1));
    public TestProvider() {
        System.err.println("TestProvider()");
    }
    public Class loadClass(String codebase, String name,
                           ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException
    {
        invocations.add(new Invocation(loadClassMethod,
            new Object[] { codebase, name, defaultLoader }));
        return loadClassReturn;
    }
    public Class loadProxyClass(String codebase, String[] interfaces,
                                ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException
    {
        invocations.add(new Invocation(loadProxyClassMethod,
            new Object[] { codebase, interfaces, defaultLoader }));
        return loadProxyClassReturn;
    }
    public ClassLoader getClassLoader(String codebase)
        throws MalformedURLException
    {
        invocations.add(new Invocation(
            getClassLoaderMethod, new Object[] { codebase }));
        return getClassLoaderReturn;
    }
    public String getClassAnnotation(Class<?> cl) {
        invocations.add(new Invocation(
            getClassAnnotationMethod, new Object[] { cl }));
        return getClassAnnotationReturn;
    }
    public static class Invocation {
        public Method method;
        public Object[] args;
        public Invocation(Method method, Object[] args) {
            this.method = method;
            this.args = (Object[]) args.clone();
        }
    }
    public static void exerciseTestProvider(Object loadClassReturn,
                                            Object loadProxyClassReturn,
                                            Object getClassLoaderReturn,
                                            Object getClassAnnotationReturn,
                                            List invocationQueue)
        throws MalformedURLException, ClassNotFoundException
    {
        URL codebaseURL = new URL("http:
        String codebase = codebaseURL.toString();
        String classname = "Foo";
        ClassLoader defaultLoader = URLClassLoader.newInstance(new URL[0]);
        String[] interfaces = new String[] { "Bar", "Baz" };
        Class dummyClass = (new Object() { }).getClass();
        TestLibrary.suggestSecurityManager(null);
        String testcase;
        Object ret;
        testcase = "RMIClassLoader.loadClass(String)";
        System.err.println("testing " + testcase);
        ret = RMIClassLoader.loadClass(classname);
        if (ret != loadClassReturn) {
             throw new RuntimeException("TEST FAILED: " +
                testcase + " returned " + ret);
        }
        verifyOneInvocation(TestProvider.loadClassMethod,
            new Object[] { null, classname, null }, invocationQueue);
        testcase = "RMIClassLoader.loadClass(URL,String)";
        System.err.println("testing " + testcase);
        ret = RMIClassLoader.loadClass(codebaseURL, classname);
        if (ret != loadClassReturn) {
             throw new RuntimeException("TEST FAILED: " +
                testcase + " returned " + ret);
        }
        verifyOneInvocation(TestProvider.loadClassMethod,
            new Object[] { codebase, classname, null }, invocationQueue);
        testcase = "RMIClassLoader.loadClass(String,String)";
        System.err.println("testing " + testcase);
        ret = RMIClassLoader.loadClass(codebase, classname);
        if (ret != loadClassReturn) {
             throw new RuntimeException("TEST FAILED: " +
                testcase + " returned " + ret);
        }
        verifyOneInvocation(TestProvider.loadClassMethod,
            new Object[] { codebase, classname, null }, invocationQueue);
        testcase = "RMIClassLoader.loadClass(String,String,ClassLoader";
        System.err.println("testing " + testcase);
        ret = RMIClassLoader.loadClass(codebase, classname, defaultLoader);
        if (ret != loadClassReturn) {
             throw new RuntimeException("TEST FAILED: " +
                testcase + " returned " + ret);
        }
        verifyOneInvocation(TestProvider.loadClassMethod,
            new Object[] { codebase, classname, defaultLoader },
            invocationQueue);
        testcase =
            "RMIClassLoader.loadProxyClass(String,String[],ClassLoader)";
        System.err.println("testing " + testcase);
        ret = RMIClassLoader.loadProxyClass(codebase, interfaces,
                                            defaultLoader);
        if (ret != loadProxyClassReturn) {
            throw new RuntimeException("TEST FAILED: " +
                testcase + " returned " + ret);
        }
        verifyOneInvocation(TestProvider.loadProxyClassMethod,
            new Object[] { codebase, interfaces, defaultLoader },
            invocationQueue);
        testcase = "RMIClassLoader.getClassLoader(String)";
        System.err.println("testing " + testcase);
        ret = RMIClassLoader.getClassLoader(codebase);
        if (ret != getClassLoaderReturn) {
            throw new RuntimeException("TEST FAILED: " +
                testcase + " returned " + ret);
        }
        verifyOneInvocation(TestProvider.getClassLoaderMethod,
            new Object[] { codebase }, invocationQueue);
        testcase = "RMIClassLoader.getClassAnnotation(Class)";
        System.err.println("testing " + testcase);
        ret = RMIClassLoader.getClassAnnotation(dummyClass);
        if (ret != getClassAnnotationReturn) {
            throw new RuntimeException("TEST FAILED: " +
                testcase + " returned " + ret);
        }
        verifyOneInvocation(TestProvider.getClassAnnotationMethod,
            new Object[] { dummyClass }, invocationQueue);
        System.err.println("TEST PASSED");
    }
    private static void verifyOneInvocation(Method method, Object[] args,
                                            List invocationQueue)
    {
        TestProvider.Invocation inv =
            (TestProvider.Invocation) invocationQueue.remove(0);
        if (!method.equals(inv.method)) {
            throw new RuntimeException(
                "unexpected provider method invoked: expected " + method +
                ", detected " + inv.method);
        }
        List expectedArgs = Arrays.asList(args);
        List detectedArgs = Arrays.asList(inv.args);
        if (!expectedArgs.equals(detectedArgs)) {
            throw new RuntimeException("TEST FAILED: " +
                "unexpected provider method invocation arguments: " +
                "expected " + expectedArgs + ", detected " + detectedArgs);
        }
        if (!invocationQueue.isEmpty()) {
            inv = (TestProvider.Invocation)
                invocationQueue.remove(0);
            throw new RuntimeException("TEST FAILED: " +
                "unexpected provider invocation: " + inv.method + " " +
                Arrays.asList(inv.args));
        }
    }
}
