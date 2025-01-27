public class T6468404 extends ToolTester {
    void test(String... args) {
        System.err.println("Compiling with sources:");
        task = tool.getTask(
                null, fm, null, null,
                null, Collections.singleton(new DummyFO("C")));
        task.setProcessors(Collections.singleton(new P()));
        if (!task.call())
            throw new AssertionError();
        System.err.println("Compiling with binaries w/o -g:");
        task = tool.getTask(
                null, fm, null, null,
                null, Collections.singleton(new DummyFO("Dummy")));
        task.setProcessors(Collections.singleton(new P()));
        if (!task.call())
            throw new AssertionError();
        task = tool.getTask(
                null, fm, null,
                Arrays.asList("-g"),
                null, Collections.singleton(new DummyFO("C")));
        if (!task.call())
            throw new AssertionError();
        System.err.println("Compiling with binaries w/ -g:");
        task = tool.getTask(
                null, fm, null, null,
                null, Collections.singleton(new DummyFO("Dummy")));
        task.setProcessors(Collections.singleton(new P()));
        if (!task.call())
            throw new AssertionError();
    }
    public static void main(String... args) {
        new T6468404().test(args);
    }
}
class DummyFO extends SimpleJavaFileObject {
    String n;
    public DummyFO(String n) {
        super(URI.create("nowhere:/" + n + ".java"), JavaFileObject.Kind.SOURCE);
        this.n = n;
    }
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return "public class " + n + " {" + n + "(java.util.List<String> l) {}}";
    }
}
@SupportedAnnotationTypes("*")
class P extends AbstractProcessor {
    boolean ran = false;
    Elements elements;
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elements = processingEnv.getElementUtils();
    }
    ExecutableElement getFirstMethodIn(String name) {
        return (ExecutableElement)elements.getTypeElement(name).getEnclosedElements().get(0);
    }
    boolean isParameterized(TypeMirror type) {
        return !((DeclaredType)type).getTypeArguments().isEmpty();
    }
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!ran) {
            ran = true;
            ExecutableElement m = getFirstMethodIn("C");
            System.err.println("method: " + m);
            TypeMirror type = (DeclaredType)m.getParameters().get(0).asType();
            System.err.println("parameters[0]: " + type);
            if (!isParameterized(type))
                throw new AssertionError(type);
            type = ((ExecutableType)m.asType()).getParameterTypes().get(0);
            System.err.println("parameterTypes[0]: " + type);
            if (!isParameterized(type))
                throw new AssertionError(type);
            System.err.println();
        }
        return true;
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
