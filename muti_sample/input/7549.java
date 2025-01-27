public class TestResolveIdent {
    @SuppressWarnings("deprecation")
    static Class<?> getDeprecatedClass() {
        return java.io.StringBufferInputStream.class;
    }
    public static void main(String[] args) throws IOException {
        javax.tools.JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        JavacTaskImpl task = (JavacTaskImpl)tool.getTask(null, null, null, null, null, null);
        JavaCompiler compiler = JavaCompiler.instance(task.getContext());
        System.out.println(compiler.resolveIdent(getDeprecatedClass().getCanonicalName()));
    }
}
