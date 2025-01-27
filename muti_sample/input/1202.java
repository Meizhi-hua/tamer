public class T6725036 {
    public static void main(String... args) throws Exception {
        new T6725036().run();
    }
    void run() throws Exception {
        RelativeFile TEST_ENTRY_NAME = new RelativeFile("java/lang/String.class");
        File f = new File(System.getProperty("java.home"));
        if (!f.getName().equals("jre"))
            f = new File(f, "jre");
        File rt_jar = new File(new File(f, "lib"), "rt.jar");
        JarFile j = new JarFile(rt_jar);
        JarEntry je = j.getJarEntry(TEST_ENTRY_NAME.getPath());
        long jarEntryTime = je.getTime();
        ZipFileIndexCache zfic = ZipFileIndexCache.getSharedInstance();
        ZipFileIndex zfi = zfic.getZipFileIndex(rt_jar, null, false, null, false);
        long zfiTime = zfi.getLastModified(TEST_ENTRY_NAME);
        check(je, jarEntryTime, zfi + ":" + TEST_ENTRY_NAME.getPath(), zfiTime);
        Context context = new Context();
        JavacFileManager fm = new JavacFileManager(context, false, null);
        ZipFileIndexArchive zfia = new ZipFileIndexArchive(fm, zfi);
        JavaFileObject jfo =
            zfia.getFileObject(TEST_ENTRY_NAME.dirname(),
                                   TEST_ENTRY_NAME.basename());
        long jfoTime = jfo.getLastModified();
        check(je, jarEntryTime, jfo, jfoTime);
        if (errors > 0)
            throw new Exception(errors + " occurred");
    }
    void check(Object ref, long refTime, Object test, long testTime) {
        if (refTime == testTime)
            return;
        System.err.println("Error: ");
        System.err.println("Expected: " + getText(ref, refTime));
        System.err.println("   Found: " + getText(test, testTime));
        errors++;
    }
    String getText(Object x, long t) {
        return String.format("%14d", t) + " (" + new Date(t) + ") from " + x;
    }
    int errors;
}
