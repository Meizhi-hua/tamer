public class TestSuperclass {
    enum ClassKind {
        CLASS("class"),
        INTERFACE("interface");
        ClassKind(String keyword) {
            this.keyword = keyword;
        }
        final String keyword;
    }
    enum GenericKind {
        NO(""),
        YES("<T>");
        GenericKind(String typarams) {
            this.typarams = typarams;
        }
        final String typarams;
    }
    enum SuperKind {
        NONE(null),
        SUPER("Super");
        SuperKind(String name) {
            this.name = name;
        }
        String extend() {
            return (name == null) ? "" : "extends " + name;
        }
        String decl(ClassKind ck) {
            return (name == null) ? "" : ck.keyword + " " + name + " { }";
        }
        final String name;
    }
    public static void main(String... args) throws Exception {
        JavaCompiler comp = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fm = comp.getStandardFileManager(null, null, null);
        int errors = 0;
        for (ClassKind ck: ClassKind.values()) {
            for (GenericKind gk: GenericKind.values()) {
                for (SuperKind sk: SuperKind.values()) {
                    errors += new TestSuperclass(ck, gk, sk).run(comp, fm);
                }
            }
        }
        if (errors > 0)
            throw new Exception(errors + " errors found");
    }
    final ClassKind ck;
    final GenericKind gk;
    final SuperKind sk;
    TestSuperclass(ClassKind ck, GenericKind gk, SuperKind sk) {
        this.ck = ck;
        this.gk = gk;
        this.sk = sk;
    }
    int run(JavaCompiler comp, StandardJavaFileManager fm) throws IOException {
        System.err.println("test: ck:" + ck + " gk:" + gk + " sk:" + sk);
        File testDir = new File(ck + "-" + gk + "-" + sk);
        testDir.mkdirs();
        fm.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(testDir));
        JavaSource js = new JavaSource();
        System.err.println(js.getCharContent(false));
        CompilationTask t = comp.getTask(null, fm, null, null, null, Arrays.asList(js));
        if (!t.call())
            throw new Error("compilation failed");
        File testClass = new File(testDir, "Test.class");
        String out = javap(testClass);
        String expect = js.source.replaceAll("(?s)^(.* Test[^{]+?) *\\{.*", "$1");
        String found = out.replaceAll("(?s).*\n(.* Test[^{]+?) *\\{.*", "$1");
        checkEqual("class signature", expect, found);
        return errors;
    }
    String javap(File file) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String[] args = { file.getPath() };
        int rc = com.sun.tools.javap.Main.run(args, pw);
        pw.close();
        String out = sw.toString();
        if (!out.isEmpty())
            System.err.println(out);
        if (rc != 0)
            throw new Error("javap failed: rc=" + rc);
        return out;
    }
    void checkEqual(String label, String expect, String found) {
        if (!expect.equals(found))
            error("Unexpected " + label + " found: '" + found + "', expected: '" + expect + "'");
    }
    void error(String msg) {
        System.err.println("Error: " + msg);
        errors++;
    }
    int errors;
    class JavaSource extends SimpleJavaFileObject {
        static final String template =
                  "#CK Test#GK #EK { }\n"
                + "#SK\n";
        final String source;
        public JavaSource() {
            super(URI.create("myfo:/Test.java"), JavaFileObject.Kind.SOURCE);
            source = template
                    .replace("#CK", ck.keyword)
                    .replace("#GK", gk.typarams)
                    .replace("#EK", sk.extend())
                    .replace("#SK", sk.decl(ck));
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }
}
