class AssignableFrom implements Predicate<TestMethod> {
    private final Class root;
    AssignableFrom(Class root) {
        this.root = root;
    }
    public boolean apply(TestMethod testMethod) {
        return root.isAssignableFrom(testMethod.getEnclosingClass());
    }
}
