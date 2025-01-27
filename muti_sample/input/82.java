public class TestPos {
    static final String errCode = "compiler.err.cannot.create.array.with.type.arguments";
    static final String expected =
        String.format("%s%n%s%n",
                      "compiler.err.cannot.create.array.with.type.arguments @ 33",
                      "begin=28, end=50 : new Object[0],T,e,s,t");
    public static void main(String... args) throws IOException {
        final boolean[] sawError = { false };
        final StringBuilder log = new StringBuilder();
        class MyFileObject extends SimpleJavaFileObject {
            MyFileObject() {
                super(URI.create("myfo:
            }
            @Override
            public String getCharContent(boolean ignoreEncodingErrors) {
                return "class Test { { Object[] o = new <T,e,s,t>Object[0]; } }";
            }
        }
        class Scanner extends TreeScanner<Void,Trees> {
            CompilationUnitTree toplevel = null;
            @Override
            public Void visitCompilationUnit(CompilationUnitTree node, Trees trees) {
                toplevel = node;
                return super.visitCompilationUnit(node, trees);
            }
            @Override
            public Void visitErroneous(ErroneousTree node, Trees trees) {
                sawError[0] = true;
                long startPos = trees.getSourcePositions().getStartPosition(toplevel, node);
                long endPos = trees.getSourcePositions().getEndPosition(toplevel, node);
                log.append(String.format("begin=%s, end=%s : %s%n",
                                         startPos,
                                         endPos,
                                         node.getErrorTrees()));
                if (startPos != 28)
                    error("Start pos for %s is incorrect (%s)!", node, startPos);
                if (endPos != 50)
                    error("End pos for %s is incorrect (%s)!", node, endPos);
                return null;
            }
        }
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        List<JavaFileObject> compilationUnits =
                Collections.<JavaFileObject>singletonList(new MyFileObject());
        DiagnosticListener<JavaFileObject> dl = new DiagnosticListener<JavaFileObject>() {
            public void report(Diagnostic<? extends JavaFileObject> diag) {
                log.append(String.format("%s @ %s%n", diag.getCode(), diag.getPosition()));
                if (!diag.getCode().equals(errCode))
                    error("unexpected error");
                if (diag.getPosition() != 33)
                    error("Error pos for %s is incorrect (%s)!",
                          diag.getCode(), diag.getPosition());
                sawError[0] = true;
            }
        };
        JavacTask task = (JavacTask)javac.getTask(null, null, dl, null, null,
                                                  compilationUnits);
        Trees trees = Trees.instance(task);
        Iterable<? extends Tree> toplevels = task.parse();
        if (!sawError[0])
            error("No parse error detected");
        sawError[0] = false;
        new Scanner().scan(toplevels, trees);
        if (!sawError[0])
            error("No error tree detected");
        if (!log.toString().equals(expected))
            error("Unexpected log message: %n%s%n", log);
        System.out.print(log);
        System.out.flush();
    }
    static void error(String format, Object... args) {
        throw new AssertionError(String.format(format, args));
    }
}
