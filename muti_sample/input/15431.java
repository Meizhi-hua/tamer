public class GenericStringTest {
    public static void main(String argv[]) throws Exception{
        int failures = 0;
        List<Class<?>> classList = new LinkedList<Class<?>>();
        classList.add(TestClass1.class);
        classList.add(TestClass2.class);
        for(Class<?> clazz: classList)
            for(Constructor<?> ctor: clazz.getDeclaredConstructors()) {
                ExpectedGenericString egs = ctor.getAnnotation(ExpectedGenericString.class);
                String actual = ctor.toGenericString();
                System.out.println(actual);
                if (! egs.value().equals(actual)) {
                    failures++;
                    System.err.printf("ERROR: Expected generic string ''%s''; got ''%s''.\n",
                                      egs.value(), actual);
                }
                if (ctor.isAnnotationPresent(ExpectedString.class)) {
                    ExpectedString es = ctor.getAnnotation(ExpectedString.class);
                    String result = ctor.toString();
                    if (! es.value().equals(result)) {
                        failures++;
                        System.err.printf("ERROR: Expected ''%s''; got ''%s''.\n",
                                          es.value(), result);
                    }
                }
            }
        if (failures > 0) {
            System.err.println("Test failed.");
            throw new RuntimeException();
        }
    }
}
class TestClass1 {
    @ExpectedGenericString(
   "TestClass1(int,double)")
    TestClass1(int x, double y) {}
    @ExpectedGenericString(
   "private TestClass1(int,int)")
    private TestClass1(int x, int param2) {}
    @ExpectedGenericString(
   "private TestClass1(java.lang.Object) throws java.lang.RuntimeException")
    private TestClass1(Object o) throws RuntimeException {}
    @ExpectedGenericString(
   "protected <S,T> TestClass1(S,T) throws java.lang.Exception")
    protected <S, T> TestClass1(S s, T t) throws Exception{}
    @ExpectedGenericString(
   "TestClass1(java.lang.Object...)")
    @ExpectedString(
   "TestClass1(java.lang.Object[])")
    TestClass1(Object... o){}
}
class TestClass2<E> {
    @ExpectedGenericString(
   "public <T> TestClass2(E,T)")
    public <T> TestClass2(E e, T t) {}
}
@Retention(RetentionPolicy.RUNTIME)
@interface ExpectedGenericString {
    String value();
}
@Retention(RetentionPolicy.RUNTIME)
@interface ExpectedString {
    String value();
}
