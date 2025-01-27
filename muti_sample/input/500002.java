public class HasAnnotation implements Predicate<TestMethod> {
    private Predicate<TestMethod> hasMethodOrClassAnnotation;
    public HasAnnotation(Class<? extends Annotation> annotationClass) {
        this.hasMethodOrClassAnnotation = or(
                new HasMethodAnnotation(annotationClass),
                new HasClassAnnotation(annotationClass));
    }
    public boolean apply(TestMethod testMethod) {
        return hasMethodOrClassAnnotation.apply(testMethod);
    }
}
