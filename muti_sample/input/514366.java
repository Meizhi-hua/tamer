@TestTargetClass(Method.class)
public class GenericReflectionCornerCases extends GenericReflectionTestsBase {
    static class Pair<T, S> {}
    static class WildcardEquality<T> {
        void wildcardEquality(Pair<? extends T, ? extends T> param) {}
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Stress test.",
            method = "getTypeParameters",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Stress test.",
            method = "getGenericParameterTypes",
            args = {}
        )
    })
    @SuppressWarnings("unchecked")
    public void testWildcardEquality() throws Exception {
        Class<? extends WildcardEquality> clazz = WildcardEquality.class;
        Method method = clazz.getDeclaredMethod("wildcardEquality", Pair.class);
        TypeVariable<?>[] typeParameters = clazz.getTypeParameters();
        assertLenghtOne(typeParameters);
        TypeVariable<?> typeParameter = typeParameters[0];
        Type[] parameterTypes = method.getGenericParameterTypes();
        assertLenghtOne(parameterTypes);
        Type parameter = parameterTypes[0];
        assertInstanceOf(ParameterizedType.class, parameter);
        ParameterizedType paramType = (ParameterizedType) parameter;
        Type[] actualTypeArguments = paramType.getActualTypeArguments();
        assertEquals(2, actualTypeArguments.length);
        Type firstArgument = actualTypeArguments[0];
        assertInstanceOf(WildcardType.class, firstArgument);
        WildcardType firstWildcardArgument = (WildcardType) firstArgument;
        Type secondArgument = actualTypeArguments[1];
        assertInstanceOf(WildcardType.class, secondArgument);
        WildcardType secondWildcardArgument = (WildcardType) secondArgument;
        assertEquals(firstWildcardArgument, secondWildcardArgument);
        Type[] firstWildcardArgumentUpperBounds = firstWildcardArgument.getUpperBounds();
        assertLenghtOne(firstWildcardArgumentUpperBounds);
        Type firstWildcardArgumentUpperBoundsType = firstWildcardArgumentUpperBounds[0];
        Type[] secondWildcardArgumentUpperBounds = secondWildcardArgument.getUpperBounds();
        assertLenghtOne(secondWildcardArgumentUpperBounds);
        Type secondWildcardArgumentUpperBoundsType = secondWildcardArgumentUpperBounds[0];
        assertEquals(firstWildcardArgumentUpperBoundsType, secondWildcardArgumentUpperBoundsType);
        assertEquals(typeParameter, firstWildcardArgumentUpperBoundsType);
        assertEquals(typeParameter, secondWildcardArgumentUpperBoundsType);
    }
    static class WildcardUnEquality<T> {
        void wildcardUnEquality(Pair<? extends T, ? super T> param) {}
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Stress test.",
            method = "getTypeParameters",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Stress test.",
            method = "getGenericParameterTypes",
            args = {}
        )
    })
    @SuppressWarnings("unchecked")
    public void testWildcardUnEquality() throws Exception {
        Class<? extends WildcardUnEquality> clazz = WildcardUnEquality.class;
        Method method = clazz.getDeclaredMethod("wildcardUnEquality", Pair.class);
        TypeVariable<?>[] typeParameters = clazz.getTypeParameters();
        assertLenghtOne(typeParameters);
        TypeVariable<?> typeParameter = typeParameters[0];
        Type[] parameterTypes = method.getGenericParameterTypes();
        assertLenghtOne(parameterTypes);
        Type parameter = parameterTypes[0];
        assertInstanceOf(ParameterizedType.class, parameter);
        ParameterizedType paramType = (ParameterizedType) parameter;
        Type[] actualTypeArguments = paramType.getActualTypeArguments();
        assertEquals(2, actualTypeArguments.length);
        Type firstArgument = actualTypeArguments[0];
        assertInstanceOf(WildcardType.class, firstArgument);
        WildcardType firstWildcardArgument = (WildcardType) firstArgument;
        Type secondArgument = actualTypeArguments[1];
        assertInstanceOf(WildcardType.class, secondArgument);
        WildcardType secondWildcardArgument = (WildcardType) secondArgument;
        assertNotEquals(firstWildcardArgument, secondWildcardArgument);
        Type[] firstWildcardArgumentUpperBounds = firstWildcardArgument.getUpperBounds();
        assertLenghtOne(firstWildcardArgumentUpperBounds);
        Type firstWildcardArgumentUpperBoundsType = firstWildcardArgumentUpperBounds[0];
        Type[] secondWildcardArgumentLowerBounds = secondWildcardArgument.getLowerBounds();
        assertLenghtOne(secondWildcardArgumentLowerBounds);
        Type secondWildcardArgumentLoweroundsType = secondWildcardArgumentLowerBounds[0];
        assertEquals(firstWildcardArgumentUpperBoundsType, secondWildcardArgumentLoweroundsType);
        assertEquals(typeParameter, firstWildcardArgumentUpperBoundsType);
        assertEquals(typeParameter, secondWildcardArgumentLoweroundsType);
    }
    static class MultipleBoundedWildcardUnEquality<T extends Object & Comparable<MultipleBoundedWildcardUnEquality<T>>> {
        void multipleBoundedWildcardUnEquality(Pair<? extends T, ? super T> param) {}
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Stress test.",
            method = "getTypeParameters",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Stress test.",
            method = "getGenericParameterTypes",
            args = {}
        )
    })
    @SuppressWarnings("unchecked")
    @KnownFailure("Fails in CTS but passes under run-core-tests")
    public void testMultipleBoundedWildcardUnEquality() throws Exception {
        Class<? extends MultipleBoundedWildcardUnEquality> clazz = MultipleBoundedWildcardUnEquality.class;
        Method method = clazz.getDeclaredMethod("multipleBoundedWildcardUnEquality", Pair.class);
        TypeVariable<?>[] typeParameters = clazz.getTypeParameters();
        assertLenghtOne(typeParameters);
        TypeVariable<?> typeParameter = typeParameters[0];
        Type[] typeParameterBounds = typeParameter.getBounds();
        assertEquals(2, typeParameterBounds.length);
        assertEquals(Object.class, typeParameterBounds[0]);
        assertInstanceOf(ParameterizedType.class, typeParameterBounds[1]);
        ParameterizedType parameterizedType = (ParameterizedType) typeParameterBounds[1];
        assertEquals(Comparable.class, parameterizedType.getRawType());
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        assertLenghtOne(typeArguments);
        assertInstanceOf(ParameterizedType.class, typeArguments[0]);
        ParameterizedType type = (ParameterizedType) typeArguments[0];
        assertEquals(typeParameter, type.getActualTypeArguments()[0]);
        assertEquals(MultipleBoundedWildcardUnEquality.class, type.getRawType());
        Type[] parameterTypes = method.getGenericParameterTypes();
        assertLenghtOne(parameterTypes);
        Type parameter = parameterTypes[0];
        assertInstanceOf(ParameterizedType.class, parameter);
        ParameterizedType paramType = (ParameterizedType) parameter;
        Type[] actualTypeArguments = paramType.getActualTypeArguments();
        assertEquals(2, actualTypeArguments.length);
        Type firstArgument = actualTypeArguments[0];
        assertInstanceOf(WildcardType.class, firstArgument);
        WildcardType firstWildcardArgument = (WildcardType) firstArgument;
        Type secondArgument = actualTypeArguments[1];
        assertInstanceOf(WildcardType.class, secondArgument);
        WildcardType secondWildcardArgument = (WildcardType) secondArgument;
        assertNotEquals(firstWildcardArgument, secondWildcardArgument);
        Type[] firstWildcardArgumentUpperBounds = firstWildcardArgument.getUpperBounds();
        assertLenghtOne(firstWildcardArgumentUpperBounds);
        Type firstWildcardArgumentUpperBoundsType = firstWildcardArgumentUpperBounds[0];
        Type[] secondWildcardArgumentLowerBounds = secondWildcardArgument.getLowerBounds();
        assertLenghtOne(secondWildcardArgumentLowerBounds);
        Type secondWildcardArgumentLoweroundsType = secondWildcardArgumentLowerBounds[0];
        assertEquals(firstWildcardArgumentUpperBoundsType, secondWildcardArgumentLoweroundsType);
    }
    static class MultipleBoundedWildcardEquality<T extends Object & Comparable<MultipleBoundedWildcardEquality<T>>> {
        void multipleBoundedWildcardEquality(Pair<? extends T, ? extends T> param) {}
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Stress test.",
            method = "getTypeParameters",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Stress test.",
            method = "getGenericParameterTypes",
            args = {}
        )
    })
    @SuppressWarnings("unchecked")
    @KnownFailure("Fails in CTS but passes under run-core-tests")
    public void testMultipleBoundedWildcard() throws Exception {
        Class<? extends MultipleBoundedWildcardEquality> clazz = MultipleBoundedWildcardEquality.class;
        Method method = clazz.getDeclaredMethod("multipleBoundedWildcardEquality", Pair.class);
        TypeVariable<?>[] typeParameters = clazz.getTypeParameters();
        assertLenghtOne(typeParameters);
        TypeVariable<?> typeParameter = typeParameters[0];
        Type[] typeParameterBounds = typeParameter.getBounds();
        assertEquals(2, typeParameterBounds.length);
        assertEquals(Object.class, typeParameterBounds[0]);
        assertInstanceOf(ParameterizedType.class, typeParameterBounds[1]);
        ParameterizedType parameterizedType = (ParameterizedType) typeParameterBounds[1];
        assertEquals(Comparable.class, parameterizedType.getRawType());
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        assertLenghtOne(typeArguments);
        assertInstanceOf(ParameterizedType.class, typeArguments[0]);
        ParameterizedType type = (ParameterizedType) typeArguments[0];
        assertEquals(typeParameter, type.getActualTypeArguments()[0]);
        assertEquals(MultipleBoundedWildcardEquality.class, type.getRawType());
        Type[] parameterTypes = method.getGenericParameterTypes();
        assertLenghtOne(parameterTypes);
        Type parameter = parameterTypes[0];
        assertInstanceOf(ParameterizedType.class, parameter);
        ParameterizedType paramType = (ParameterizedType) parameter;
        Type[] actualTypeArguments = paramType.getActualTypeArguments();
        assertEquals(2, actualTypeArguments.length);
        Type firstArgument = actualTypeArguments[0];
        assertInstanceOf(WildcardType.class, firstArgument);
        WildcardType firstWildcardArgument = (WildcardType) firstArgument;
        Type secondArgument = actualTypeArguments[1];
        assertInstanceOf(WildcardType.class, secondArgument);
        WildcardType secondWildcardArgument = (WildcardType) secondArgument;
        assertEquals(firstWildcardArgument, secondWildcardArgument);
        Type[] firstWildcardArgumentUpperBounds = firstWildcardArgument.getUpperBounds();
        assertLenghtOne(firstWildcardArgumentUpperBounds);
        Type firstWildcardArgumentUpperBoundsType = firstWildcardArgumentUpperBounds[0];
        Type[] secondWildcardArgumentUpperBounds = secondWildcardArgument.getUpperBounds();
        assertLenghtOne(secondWildcardArgumentUpperBounds);
        Type secondWildcardArgumentLoweroundsType = secondWildcardArgumentUpperBounds[0];
        assertEquals(firstWildcardArgumentUpperBoundsType, secondWildcardArgumentLoweroundsType);
    }
}
