@TestTargetClass(TypeVariable.class) 
public class TypeVariableTest extends GenericReflectionTestsBase {
    static class A<T>{}
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Tests getGenericDeclaration of a type variable on a class.",
        method = "getGenericDeclaration",
        args = {}
    )
    public void testSimpleTypeVariableOnClass(){
        Class<? extends A> clazz = A.class;
        TypeVariable[] typeParameters = clazz.getTypeParameters();
        assertLenghtOne(typeParameters);
        TypeVariable<Class> typeVariable = typeParameters[0];
        assertEquals(clazz, typeVariable.getGenericDeclaration());
        assertEquals("T", typeVariable.getName());
        Type[] bounds = typeVariable.getBounds();
        assertLenghtOne(bounds);
        assertEquals(Object.class, bounds[0]);
    }
    static class B{
        <T> void b(){};
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Tests getGenericDeclaration of a type variable on a method.",
        method = "getGenericDeclaration",
        args = {}
    )
    public void testSimpleTypeVariableOnMethod() throws Exception{
        Class<? extends B> clazz = B.class;
        Method method = clazz.getDeclaredMethod("b");
        TypeVariable<Method>[] typeParameters = method.getTypeParameters();
        assertLenghtOne(typeParameters);
        TypeVariable<Method> typeVariable = typeParameters[0];
        assertEquals(method, typeVariable.getGenericDeclaration());
        assertEquals("T", typeVariable.getName());
        Type[] bounds = typeVariable.getBounds();
        assertLenghtOne(bounds);
        assertEquals(Object.class, bounds[0]);
    }
    static class C {
        <T>C(){}
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Interaction test.",
            method = "getGenericDeclaration",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Interaction test.",
            method = "getBounds",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Interaction test.",
            method = "getName",
            args = {}
        )
    })
    public void testSimpleTypeVariableOnConstructor() throws Exception{
        Class<? extends C> clazz = C.class;
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        TypeVariable<?>[] typeParameters = constructor.getTypeParameters();
        assertLenghtOne(typeParameters); 
        TypeVariable<?> typeVariable = typeParameters[0];
        assertEquals(constructor, typeVariable.getGenericDeclaration());
        assertEquals("T", typeVariable.getName());
        Type[] bounds = typeVariable.getBounds();
        assertLenghtOne(bounds);
        assertEquals(Object.class, bounds[0]);
    }
    static class D<Q,R,S>{}
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Interaction test.",
        method = "getGenericDeclaration",
        args = {}
    )
    public void testMultipleTypeVariablesOnClass() throws Exception {
        Class<? extends D> clazz = D.class;
        TypeVariable<?>[] typeParameters = clazz.getTypeParameters();
        assertEquals(3, typeParameters.length);
        assertEquals("Q", typeParameters[0].getName());
        assertEquals(clazz, typeParameters[0].getGenericDeclaration());
        assertEquals("R", typeParameters[1].getName());
        assertEquals(clazz, typeParameters[1].getGenericDeclaration());
        assertEquals("S", typeParameters[2].getName());
        assertEquals(clazz, typeParameters[2].getGenericDeclaration());
    }
    static class E {
        <Q,R,S> void e(){}
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public void testMultipleTypeVariablesOnMethod() throws Exception {
        Class<? extends E> clazz = E.class;
        Method method = clazz.getDeclaredMethod("e");
        TypeVariable<?>[] typeParameters = method.getTypeParameters();
        assertEquals(3, typeParameters.length);
        assertEquals("Q", typeParameters[0].getName());
        assertEquals(method, typeParameters[0].getGenericDeclaration());
        assertEquals("R", typeParameters[1].getName());
        assertEquals(method, typeParameters[1].getGenericDeclaration());
        assertEquals("S", typeParameters[2].getName());
        assertEquals(method, typeParameters[2].getGenericDeclaration());
    }
    static class F {
        <Q,R,S> F(){}
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public void testMultipleTypeVariablesOnConstructor() throws Exception {
        Class<? extends F> clazz = F.class;
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        TypeVariable<?>[] typeParameters = constructor.getTypeParameters();
        assertEquals(3, typeParameters.length);
        assertEquals("Q", typeParameters[0].getName());
        assertEquals(constructor, typeParameters[0].getGenericDeclaration());
        assertEquals("R", typeParameters[1].getName());
        assertEquals(constructor, typeParameters[1].getGenericDeclaration());
        assertEquals("S", typeParameters[2].getName());
        assertEquals(constructor, typeParameters[2].getGenericDeclaration());
    }
    static class G <T extends Number>{}
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Interaction test, Missing tests for TypeNotPresentException, MalformedParametrizedTypeException",
        method = "getBounds",
        args = {}
    )
    public void testSingleBound() throws Exception {
        Class<? extends G> clazz = G.class;
        TypeVariable[] typeParameters = clazz.getTypeParameters();
        TypeVariable<Class> typeVariable = typeParameters[0];
        Type[] bounds = typeVariable.getBounds();
        assertLenghtOne(bounds);
        assertEquals(Number.class, bounds[0]);
    }
    static class H <T extends Number & Serializable >{}
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Interaction test., Missing tests for TypeNotPresentException, MalformedParametrizedTypeException",
        method = "getBounds",
        args = {}
    )
    public void testMultipleBound() throws Exception {
        Class<? extends H> clazz = H.class;
        TypeVariable[] typeParameters = clazz.getTypeParameters();
        TypeVariable<Class> typeVariable = typeParameters[0];
        Type[] bounds = typeVariable.getBounds();
        assertEquals(2, bounds.length);
        assertEquals(Number.class, bounds[0]);
        assertEquals(Serializable.class, bounds[1]);
    }
}
