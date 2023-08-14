public class T6879371 {
    public static void main(String[] args) throws Exception {
        new T6879371().run();
    }
    public void run() throws Exception {
        File classDir = new File("classes");
        classDir.mkdir();
        String className = "Test";
        File javaFile = writeTestFile(className);
        compileTestFile(classDir, javaFile);
        test(classDir, className, false);
        test(classDir, className, true);
    }
    void test(File classDir, String className, boolean useJavaUtilZip) throws Exception {
        String prev = System.getProperty("useJavaUtilZip");
        setProperty("useJavaUtilZip", (useJavaUtilZip ? "true" : null));
        try {
            File zipFile = zip(classDir, new File(classDir + ".zip"));
            javap("-classpath", zipFile.getPath(), className);
            if (!zipFile.delete())
                throw new Exception("failed to delete " + zipFile);
        } finally {
            setProperty("useJavaUtilZip", prev);
        }
    }
    File writeTestFile(String name) throws IOException {
        File f = new File(name + ".java");
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        out.println("class " + name + " { }");
        out.close();
        return f;
    }
    void compileTestFile(File classDir, File file) {
        int rc = com.sun.tools.javac.Main.compile(
           new String[] { "-d", classDir.getPath(), file.getPath() });
        if (rc != 0)
            throw new Error("compilation failed. rc=" + rc);
    }
    File zip(File dir, File zipFile) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
        for (File file: dir.listFiles()) {
            if (file.isFile()) {
                byte[] data = new byte[(int) file.length()];
                DataInputStream in = new DataInputStream(new FileInputStream(file));
                in.readFully(data);
                in.close();
                zipOut.putNextEntry(new ZipEntry(file.getName()));
                zipOut.write(data, 0, data.length);
                zipOut.closeEntry();
            }
        }
        zipOut.close();
        return zipFile;
    }
    String javap(String... args) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        int rc = com.sun.tools.javap.Main.run(args, out);
        if (rc != 0)
            throw new Error("javap failed. rc=" + rc);
        out.close();
        return sw.toString();
    }
    void setProperty(String key, String value) {
        if (value != null)
            System.setProperty(key, value);
        else
            System.getProperties().remove(key);
    }
}