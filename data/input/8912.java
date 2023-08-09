public class TrailingSlash {
    static PrintStream log = System.err;
    static int failures = 0;
    static void check(String what, String fns,
                      boolean expected, boolean threw)
    {
        if (expected == threw) {
            log.println("    FAIL: new " + what + "(\"" + fns + "\") "
                        + (expected ? "failed" : "succeeded"));
            failures++;
        }
    }
    static void go(String fns, boolean fis, boolean raf, boolean fos)
        throws IOException
    {
        boolean threw;
        threw = false;
        try {
            new FileInputStream(fns).close();
            log.println("    FileInputStream okay");
        } catch (IOException x) {
            log.println("    FileInputStream: " + x);
            threw = true;
        }
        check("FileInputStream", fns, fis, threw);
        threw = false;
        try {
            new RandomAccessFile(fns, "r").close();
            log.println("    RandomAccessFile okay");
        } catch (IOException x) {
            log.println("    RandomAccessFile: " + x);
            threw = true;
        }
        check("RandomAccessFile", fns, raf, threw);
        threw = false;
        try {
            new FileOutputStream(fns).close();
            log.println("    FileOutputStream okay");
        } catch (IOException x) {
            log.println("    FileOutputStream: " + x);
            threw = true;
        }
        check("FileOutputStream", fns, fos, threw);
    }
    static void go(String fn, String fns) throws Exception {
        log.println("Test case: " + fns);
        File f = new File(fn);
        f.delete();
        if (f.exists())
            throw new Exception("Can't delete " + f);
        log.println("  " + fn + " does not exist");
        go(fns, false, false, true);
        f.delete();
        f.mkdir();
        log.println("  " + fn + " is a directory");
        go(fns, false, false, false);
        f.delete();
        f.createNewFile();
        log.println("  " + fn + " is a file");
        go(fns, true, true, true);
    }
    public static void main(String[] args) throws Exception {
        if (File.separatorChar != '/') {
            return;
        }
        go("xyzzy", "xyzzy");
        go("xyzzy", "xyzzy/");
        go("xyzzy", "xyzzy
        if (failures > 0)
            throw new Exception(failures + " failures");
    }
}
