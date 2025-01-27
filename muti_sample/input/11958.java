public class T6406771 extends AbstractProcessor {
    String[] tests = {
        "line:24",
        "line:25",
        "line:26", "line:26",
      "col:7", "col:16", "col:26",                 
        "col:9",        "col:25",       "col:41",  
                   "col:20",              "col:43" 
    };
    public static void main(String[] args) {
        String self = T6406771.class.getName();
        String testSrc = System.getProperty("test.src");
        String testClasses = System.getProperty("test.classes");
        JavacTool tool = JavacTool.create();
        StandardJavaFileManager fm = tool.getStandardFileManager(null, null, null);
        JavaFileObject f = fm.getJavaFileObjectsFromFiles(Arrays.asList(new File(testSrc, self+".java"))).iterator().next();
        List<String> opts = Arrays.asList("-d", ".", "-processorpath", testClasses, "-processor", self, "-proc:only");
        JavacTask task = tool.getTask(null, fm, null, opts, null, Arrays.asList(f));
        if (!task.call())
            throw new AssertionError("failed");
    }
    public boolean process(Set<? extends TypeElement> elems, RoundEnvironment rEnv) {
        final String LINE = "line" + ':';   
        final String COLUMN  = "col" + ':';
        final Messager messager = processingEnv.getMessager();
        final Trees trees = Trees.instance(processingEnv);
        TreeScanner<Void,LineMap> s = new  TreeScanner<Void,LineMap>() {
            public Void visitLiteral(LiteralTree tree, LineMap lineMap) {
                if (tree.getKind() == Tree.Kind.STRING_LITERAL) {
                    String s = (String) tree.getValue();
                    int pos = ((JCTree) tree).pos; 
                    String prefix;
                    long found;
                    if (s.startsWith(LINE)) {
                        prefix = LINE;
                        found = lineMap.getLineNumber(pos);
                    }
                    else if (s.startsWith(COLUMN)) {
                        prefix = COLUMN;
                        found = lineMap.getColumnNumber(pos);
                    }
                    else
                        return null;
                    int expect = Integer.parseInt(s.substring(prefix.length()));
                    if (expect != found) {
                        messager.printMessage(Diagnostic.Kind.ERROR,
                                              "Error: " + prefix + " pos=" + pos
                                              + " expect=" + expect + " found=" + found);
                    }
                }
                return null;
            }
        };
        for (Element e: rEnv.getRootElements()) {
            System.err.println(e);
            Tree t = trees.getTree(e);
            TreePath p = trees.getPath(e);
            s.scan(p.getLeaf(), p.getCompilationUnit().getLineMap());
        }
        return true;
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
