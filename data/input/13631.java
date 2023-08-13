class A<T> {
    class B<U> {}
}
class Test<T> {
    static class Inner1<U> {
        void bar(U[] array1) { return;}
    }
    class Inner2<V> {
        List<?> foo2(List<? extends V> t) {
            return null;
        }
    }
    static <S extends Object & Comparable<? super S> > S max(Collection<? extends S> coll) {
        return null;
    }
    List<? extends T> foo(List<? super T> t) {
        return null;
    }
}
public class StringsAndBounds {
    public void f(A<String>.B<Integer> x) {
    }
    public <T>  void g(T a) {return ;}
    static void scanner(Class clazz) {
        System.out.println("\n\nScanning " + clazz.getName());
        for(Class c: clazz.getDeclaredClasses()) {
            scanner(c);
        }
        for(Method m: clazz.getDeclaredMethods()) {
            System.out.println("\nMethod:\t" + m.toString()); 
            System.out.println("\tReturn Type: " + m.getGenericReturnType().toString() );
            for(Type p: m.getGenericParameterTypes()) {
                if (p instanceof WildcardType) { 
                    Type[] upperBounds = ((WildcardType)p).getUpperBounds();
                    if (upperBounds.length < 1 ||
                        upperBounds[0] == null)
                        throw new RuntimeException("Malformed upper bounds: " + p);
                }
                System.out.println("\tParameter: " + p.toString());
            }
        }
    }
    public static void main(String[] argv) throws Exception {
        scanner(StringsAndBounds.class);
        scanner(A.B.class);
        scanner(Test.class);
    }
}
