public class T6404194 {
    public static void main(String... args) throws IOException {
        class MyFileObject extends SimpleJavaFileObject {
            MyFileObject() {
                super(URI.create("myfo:
            }
            @Override
            public String getCharContent(boolean ignoreEncodingErrors) {
                return "@SuppressWarning(\"foo\") @Deprecated class Test { Test() { } }";
            }
        }
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        List<JavaFileObject> compilationUnits =
                Collections.<JavaFileObject>singletonList(new MyFileObject());
        JavacTask task = (JavacTask)javac.getTask(null, null, null, null, null, compilationUnits);
        Trees trees = Trees.instance(task);
        CompilationUnitTree toplevel = task.parse().iterator().next();
        ClassTree classTree = (ClassTree)toplevel.getTypeDecls().get(0);
        List<? extends Tree> annotations = classTree.getModifiers().getAnnotations();
        Tree tree1 = annotations.get(0);
        Tree tree2 = annotations.get(1);
        long pos = trees.getSourcePositions().getStartPosition(toplevel, tree1);
        if (pos != 0)
            throw new AssertionError(String.format("Start pos for %s is incorrect (%s)!",
                                                   tree1, pos));
        pos = trees.getSourcePositions().getEndPosition(toplevel, tree1);
        if (pos != 23)
            throw new AssertionError(String.format("End pos for %s is incorrect (%s)!",
                                                   tree1, pos));
        pos = trees.getSourcePositions().getStartPosition(toplevel, tree2);
        if (pos != 24)
            throw new AssertionError(String.format("Start pos for %s is incorrect (%s)!",
                                                   tree2, pos));
        pos = trees.getSourcePositions().getEndPosition(toplevel, tree2);
        if (pos != 35)
            throw new AssertionError(String.format("End pos for %s is incorrect (%s)!",
                                                   tree2, pos));
    }
}
