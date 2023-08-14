public class TestMethod {
    private final String enclosingClassname;
    private final String testMethodName;
    private final Class<? extends TestCase> enclosingClass;
    public TestMethod(Method method, Class<? extends TestCase> enclosingClass) {
        this(method.getName(), enclosingClass);
    }
    public TestMethod(String methodName, Class<? extends TestCase> enclosingClass) {
        this.enclosingClass = enclosingClass;
        this.enclosingClassname = enclosingClass.getName();
        this.testMethodName = methodName;
    }
    public TestMethod(TestCase testCase) {
        this(testCase.getName(), testCase.getClass());
    }
    public String getName() {
        return testMethodName;
    }
    public String getEnclosingClassname() {
        return enclosingClassname;
    }
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        try {
            return getEnclosingClass().getMethod(getName()).getAnnotation(annotationClass);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
    @SuppressWarnings("unchecked")
    public Class<? extends TestCase> getEnclosingClass() {
        return enclosingClass;
    }
    public TestCase createTest()
            throws InvocationTargetException, IllegalAccessException, InstantiationException {
        return instantiateTest(enclosingClass, testMethodName);
    }
    @SuppressWarnings("unchecked")
    private TestCase instantiateTest(Class testCaseClass, String testName)
            throws InvocationTargetException, IllegalAccessException, InstantiationException {
        Constructor[] constructors = testCaseClass.getConstructors();
        if (constructors.length == 0) {
            return instantiateTest(testCaseClass.getSuperclass(), testName);
        } else {
            for (Constructor constructor : constructors) {
                Class[] params = constructor.getParameterTypes();
                if (noargsConstructor(params)) {
                    TestCase test = ((Constructor<? extends TestCase>) constructor).newInstance();
                    test.setName(testName);
                    return test;
                } else if (singleStringConstructor(params)) {
                    return ((Constructor<? extends TestCase>) constructor)
                            .newInstance(testName);
                }
            }
        }
        throw new RuntimeException("Unable to locate a constructor for "
                + testCaseClass.getName());
    }
    private boolean singleStringConstructor(Class[] params) {
        return (params.length == 1) && (params[0].equals(String.class));
    }
    private boolean noargsConstructor(Class[] params) {
        return params.length == 0;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestMethod that = (TestMethod) o;
        if (enclosingClassname != null
                ? !enclosingClassname.equals(that.enclosingClassname)
                : that.enclosingClassname != null) {
            return false;
        }
        if (testMethodName != null
                ? !testMethodName.equals(that.testMethodName)
                : that.testMethodName != null) {
            return false;
        }
        return true;
    }
    @Override
    public int hashCode() {
        int result;
        result = (enclosingClassname != null ? enclosingClassname.hashCode() : 0);
        result = 31 * result + (testMethodName != null ? testMethodName.hashCode() : 0);
        return result;
    }
    @Override
    public String toString() {
        return enclosingClassname + "." + testMethodName;
    }
}