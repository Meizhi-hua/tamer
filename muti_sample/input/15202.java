public class TestClientCodeWrapper extends JavacTestingAbstractProcessor {
    public static void main(String... args) throws Exception {
        new TestClientCodeWrapper().run();
    }
    void run() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        defaultFileManager = compiler.getStandardFileManager(null, null, null);
        for (Method m: getMethodsExcept(JavaFileManager.class, "close", "getJavaFileForInput")) {
            test(m);
        }
        for (Method m: getMethodsExcept(FileObject.class, "delete")) {
            test(m);
        }
        for (Method m: getMethods(JavaFileObject.class)) {
            test(m);
        }
        for (Method m: getMethodsExcept(Processor.class, "getCompletions")) {
            test(m);
        }
        for (Method m: DiagnosticListener.class.getDeclaredMethods()) {
            test(m);
        }
        for (Method m: TaskListener.class.getDeclaredMethods()) {
            test(m);
        }
        if (errors > 0)
            throw new Exception(errors + " errors occurred");
    }
    Set<Method> getMethods(Class<?> clazz) {
        return getMethodsExcept(clazz, new String[0]);
    }
    Set<Method> getMethodsExcept(Class<?> clazz, String... exclude) {
        Set<Method> methods = new TreeSet<Method>(new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                return m1.toString().compareTo(m2.toString());
            }
        });
        Set<String> e = new HashSet<String>(Arrays.asList(exclude));
        for (Method m: clazz.getDeclaredMethods()) {
            if (!e.contains(m.getName()))
                methods.add(m);
        }
        return methods;
    }
    void test(Method m) throws Exception {
        testNum++;
        File extDirs = new File("empty-extdirs");
        extDirs.mkdirs();
        File testClasses = new File("test" + testNum);
        testClasses.mkdirs();
        defaultFileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(testClasses));
        System.err.println("test " + testNum + ": "
                + m.getDeclaringClass().getSimpleName() + "." + m.getName());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        List<String> javacOptions = Arrays.asList(
                "-extdirs", extDirs.getPath(), 
                "-processor", TestClientCodeWrapper.class.getName()
                );
        List<String> classes = Collections.emptyList();
        JavacTool tool = JavacTool.create();
        try {
            JavacTask task = tool.getTask(pw,
                    getFileManager(m, defaultFileManager),
                    getDiagnosticListener(m, pw),
                    javacOptions,
                    classes,
                    getCompilationUnits(m));
            if (isDeclaredIn(m, Processor.class))
                task.setProcessors(getProcessors(m));
            if (isDeclaredIn(m, TaskListener.class))
                task.setTaskListener(getTaskListener(m, pw));
            boolean ok = task.call();
            error("compilation " + (ok ? "succeeded" : "failed") + " unexpectedly");
        } catch (RuntimeException e) {
            System.err.println("caught " + e);
            if (e.getClass() == RuntimeException.class) {
                Throwable cause = e.getCause();
                if (cause instanceof UserError) {
                    String expect = m.getName();
                    String found = cause.getMessage();
                    checkEqual("exception messaqe", expect, found);
                } else {
                    cause.printStackTrace(System.err);
                    error("Unexpected exception: " + cause);
                }
            } else {
                e.printStackTrace(System.err);
                error("Unexpected exception: " + e);
            }
        }
        pw.close();
        String out = sw.toString();
        System.err.println(out);
    }
    JavaFileManager getFileManager(Method m, JavaFileManager defaultFileManager) {
        return isDeclaredIn(m, JavaFileManager.class, FileObject.class, JavaFileObject.class)
                ? new UserFileManager(m, defaultFileManager)
                : defaultFileManager;
    }
    DiagnosticListener<JavaFileObject> getDiagnosticListener(Method m, PrintWriter out) {
        return isDeclaredIn(m, DiagnosticListener.class)
                ? new UserDiagnosticListener(m, out)
                : null;
    }
    Iterable<? extends JavaFileObject> getCompilationUnits(Method m) {
        File testSrc = new File(System.getProperty("test.src"));
        File thisSrc = new File(testSrc, TestClientCodeWrapper.class.getName() + ".java");
        Iterable<? extends JavaFileObject> files = defaultFileManager.getJavaFileObjects(thisSrc);
        if (isDeclaredIn(m, FileObject.class, JavaFileObject.class))
            return Arrays.asList(new UserFileObject(m, files.iterator().next()));
        else
            return files;
    }
    Iterable<? extends Processor> getProcessors(Method m) {
        return Arrays.asList(new UserProcessor(m));
    }
    TaskListener getTaskListener(Method m, PrintWriter out) {
        return new UserTaskListener(m, out);
    }
    <T> void checkEqual(String label, T expect, T found) {
        if (!expect.equals(found))
            error("Unexpected value for " + label + ": " + found + "; expected: " + expect);
    }
    void error(String msg) {
        System.err.println("Error: " + msg);
        errors++;
    }
    static boolean isDeclaredIn(Method m, Class<?>... classes) {
        Class<?> dc = m.getDeclaringClass();
        for (Class<?> c: classes) {
            if (c == dc) return true;
        }
        return false;
    }
    static void throwUserExceptionIfNeeded(Method m, String name) {
        if (m != null && m.getName().equals(name))
            throw new UserError(name);
    }
    StandardJavaFileManager defaultFileManager;
    int testNum;
    int errors;
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean firstRound = false;
        for (Element e: roundEnv.getRootElements()) {
            if (e.getSimpleName().contentEquals(TestClientCodeWrapper.class.getSimpleName()))
                firstRound = true;
        }
        if (firstRound) {
            try {
                FileObject f1 = filer.getResource(StandardLocation.CLASS_PATH, "",
                    TestClientCodeWrapper.class.getName() + ".java");
                f1.openInputStream().close();
                f1.openReader(false).close();
                FileObject f2 = filer.createResource(
                        StandardLocation.CLASS_OUTPUT, "", "f2.txt", (Element[]) null);
                f2.openOutputStream().close();
                FileObject f3 = filer.createResource(
                        StandardLocation.CLASS_OUTPUT, "", "f3.txt", (Element[]) null);
                f3.openWriter().close();
                JavaFileObject f4 = filer.createSourceFile("f4", (Element[]) null);
                f4.openWriter().close();
                f4.getNestingKind();
                f4.getAccessLevel();
                messager.printMessage(Diagnostic.Kind.NOTE, "informational note",
                        roundEnv.getRootElements().iterator().next());
            } catch (IOException e) {
                throw new UserError(e);
            }
        }
        return true;
    }
    static class UserError extends Error {
        private static final long serialVersionUID = 1L;
        UserError(String msg) {
            super(msg);
        }
        UserError(Throwable t) {
            super(t);
        }
    }
    static class UserFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        Method fileManagerMethod;
        Method fileObjectMethod;
        UserFileManager(Method m, JavaFileManager delegate) {
            super(delegate);
            if (isDeclaredIn(m, JavaFileManager.class)) {
                fileManagerMethod = m;
            } else if (isDeclaredIn(m, FileObject.class, JavaFileObject.class)) {
                fileObjectMethod = m;
            } else
                assert false;
        }
        @Override
        public ClassLoader getClassLoader(Location location) {
            throwUserExceptionIfNeeded(fileManagerMethod, "getClassLoader");
            return super.getClassLoader(location);
        }
        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
            throwUserExceptionIfNeeded(fileManagerMethod, "list");
            return wrap(super.list(location, packageName, kinds, recurse));
        }
        @Override
        public String inferBinaryName(Location location, JavaFileObject file) {
            throwUserExceptionIfNeeded(fileManagerMethod, "inferBinaryName");
            return super.inferBinaryName(location, unwrap(file));
        }
        @Override
        public boolean isSameFile(FileObject a, FileObject b) {
            throwUserExceptionIfNeeded(fileManagerMethod, "isSameFile");
            return super.isSameFile(unwrap(a), unwrap(b));
        }
        @Override
        public boolean handleOption(String current, Iterator<String> remaining) {
            throwUserExceptionIfNeeded(fileManagerMethod, "handleOption");
            return super.handleOption(current, remaining);
        }
        @Override
        public boolean hasLocation(Location location) {
            throwUserExceptionIfNeeded(fileManagerMethod, "hasLocation");
            return super.hasLocation(location);
        }
        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
            throwUserExceptionIfNeeded(fileManagerMethod, "getJavaFileForInput");
            return wrap(super.getJavaFileForInput(location, className, kind));
        }
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
            throwUserExceptionIfNeeded(fileManagerMethod, "getJavaFileForOutput");
            return wrap(super.getJavaFileForOutput(location, className, kind, sibling));
        }
        @Override
        public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
            throwUserExceptionIfNeeded(fileManagerMethod, "getFileForInput");
            return wrap(super.getFileForInput(location, packageName, relativeName));
        }
        @Override
        public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
            throwUserExceptionIfNeeded(fileManagerMethod, "getFileForOutput");
            return wrap(super.getFileForOutput(location, packageName, relativeName, sibling));
        }
        @Override
        public void flush() throws IOException {
            throwUserExceptionIfNeeded(fileManagerMethod, "flush");
            super.flush();
        }
        @Override
        public void close() throws IOException {
            throwUserExceptionIfNeeded(fileManagerMethod, "close");
            super.close();
        }
        @Override
        public int isSupportedOption(String option) {
            throwUserExceptionIfNeeded(fileManagerMethod, "isSupportedOption");
            return super.isSupportedOption(option);
        }
        public FileObject wrap(FileObject fo) {
            if (fileObjectMethod == null)
                return fo;
            return new UserFileObject(fileObjectMethod, (JavaFileObject)fo);
        }
        FileObject unwrap(FileObject fo) {
            if (fo instanceof UserFileObject)
                return ((UserFileObject) fo).unwrap();
            else
                return fo;
        }
        public JavaFileObject wrap(JavaFileObject fo) {
            if (fileObjectMethod == null)
                return fo;
            return new UserFileObject(fileObjectMethod, fo);
        }
        public Iterable<JavaFileObject> wrap(Iterable<? extends JavaFileObject> list) {
            List<JavaFileObject> wrapped = new ArrayList<JavaFileObject>();
            for (JavaFileObject fo : list)
                wrapped.add(wrap(fo));
            return Collections.unmodifiableList(wrapped);
        }
        JavaFileObject unwrap(JavaFileObject fo) {
            if (fo instanceof UserFileObject)
                return ((UserFileObject) fo).unwrap();
            else
                return fo;
        }
    }
    static class UserFileObject extends ForwardingJavaFileObject<JavaFileObject> {
        Method method;
        UserFileObject(Method m, JavaFileObject delegate) {
            super(delegate);
            assert isDeclaredIn(m, FileObject.class, JavaFileObject.class);
            this.method = m;
        }
        JavaFileObject unwrap() {
            return fileObject;
        }
        @Override
        public Kind getKind() {
            throwUserExceptionIfNeeded(method, "getKind");
            return super.getKind();
        }
        @Override
        public boolean isNameCompatible(String simpleName, Kind kind) {
            throwUserExceptionIfNeeded(method, "isNameCompatible");
            return super.isNameCompatible(simpleName, kind);
        }
        @Override
        public NestingKind getNestingKind() {
            throwUserExceptionIfNeeded(method, "getNestingKind");
            return super.getNestingKind();
        }
        @Override
        public Modifier getAccessLevel() {
            throwUserExceptionIfNeeded(method, "getAccessLevel");
            return super.getAccessLevel();
        }
        @Override
        public URI toUri() {
            throwUserExceptionIfNeeded(method, "toUri");
            return super.toUri();
        }
        @Override
        public String getName() {
            throwUserExceptionIfNeeded(method, "getName");
            return super.getName();
        }
        @Override
        public InputStream openInputStream() throws IOException {
            throwUserExceptionIfNeeded(method, "openInputStream");
            return super.openInputStream();
        }
        @Override
        public OutputStream openOutputStream() throws IOException {
            throwUserExceptionIfNeeded(method, "openOutputStream");
            return super.openOutputStream();
        }
        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            throwUserExceptionIfNeeded(method, "openReader");
            return super.openReader(ignoreEncodingErrors);
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            throwUserExceptionIfNeeded(method, "getCharContent");
            return super.getCharContent(ignoreEncodingErrors);
        }
        @Override
        public Writer openWriter() throws IOException {
            throwUserExceptionIfNeeded(method, "openWriter");
            return super.openWriter();
        }
        @Override
        public long getLastModified() {
            throwUserExceptionIfNeeded(method, "getLastModified");
            return super.getLastModified();
        }
        @Override
        public boolean delete() {
            throwUserExceptionIfNeeded(method, "delete");
            return super.delete();
        }
    }
    static class UserProcessor extends JavacTestingAbstractProcessor {
        Method method;
        UserProcessor(Method m) {
            assert isDeclaredIn(m, Processor.class);
            method = m;
        }
        @Override
        public Set<String> getSupportedOptions() {
            throwUserExceptionIfNeeded(method, "getSupportedOptions");
            return super.getSupportedOptions();
        }
        @Override
        public Set<String> getSupportedAnnotationTypes() {
            throwUserExceptionIfNeeded(method, "getSupportedAnnotationTypes");
            return super.getSupportedAnnotationTypes();
        }
        @Override
        public SourceVersion getSupportedSourceVersion() {
            throwUserExceptionIfNeeded(method, "getSupportedSourceVersion");
            return super.getSupportedSourceVersion();
        }
        @Override
        public void init(ProcessingEnvironment processingEnv) {
            throwUserExceptionIfNeeded(method, "init");
            super.init(processingEnv);
        }
        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            throwUserExceptionIfNeeded(method, "process");
            return true;
        }
        @Override
        public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
            throwUserExceptionIfNeeded(method, "getCompletions");
            return super.getCompletions(element, annotation, member, userText);
        }
    }
    static class UserDiagnosticListener implements DiagnosticListener<JavaFileObject> {
        Method method;
        PrintWriter out;
        UserDiagnosticListener(Method m, PrintWriter out) {
            assert isDeclaredIn(m, DiagnosticListener.class);
            this.method = m;
            this.out = out;
        }
        @Override
        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
            throwUserExceptionIfNeeded(method, "report");
            out.println("report: " + diagnostic);
        }
    }
    static class UserTaskListener implements TaskListener {
        Method method;
        PrintWriter out;
        UserTaskListener(Method m, PrintWriter out) {
            assert isDeclaredIn(m, TaskListener.class);
            this.method = m;
            this.out = out;
        }
        @Override
        public void started(TaskEvent e) {
            throwUserExceptionIfNeeded(method, "started");
            out.println("started: " + e);
        }
        @Override
        public void finished(TaskEvent e) {
            throwUserExceptionIfNeeded(method, "finished");
            out.println("finished: " + e);
        }
    }
}
