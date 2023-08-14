public class TestEmptyZip {
    public static void realMain(String[] args) throws Throwable {
        String zipName = "foo.zip";
        File f = new File(System.getProperty("test.scratch", "."), zipName);
        if (f.exists() && !f.delete()) {
            throw new Exception("failed to delete " + zipName);
        }
        f.createNewFile();
        try {
            checkCannotRead(f);
            OutputStream out = new FileOutputStream(f);
            try {
                out.write("class Foo { }".getBytes());
            } finally {
                out.close();
            }
            checkCannotRead(f);
        } finally {
            f.delete();
        }
        write(f);
        readFile(f);
        readStream(f);
        f.delete();
    }
    static void checkCannotRead(File f) throws IOException {
        try {
            new ZipFile(f).close();
            fail();
        } catch (ZipException ze) {
            if (f.length() == 0) {
                check(ze.getMessage().contains("zip file is empty"));
            } else {
                pass();
            }
        }
        try (FileInputStream fis = new FileInputStream(f);
             ZipInputStream zis = new ZipInputStream(fis))
        {
            ZipEntry ze = zis.getNextEntry();
            check(ze == null);
        } catch (IOException ex) {
            unexpected(ex);
        }
    }
    static void write(File f) throws Exception {
        try (FileOutputStream fis = new FileOutputStream(f);
             ZipOutputStream zos = new ZipOutputStream(fis))
        {
            zos.finish();
            pass();
        } catch (Exception ex) {
            unexpected(ex);
        }
    }
    static void readFile(File f) throws Exception {
        try (ZipFile zf = new ZipFile(f)) {
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                fail();
            }
            pass();
        } catch (Exception ex) {
            unexpected(ex);
        }
    }
    static void readStream(File f) throws Exception {
        try (FileInputStream fis = new FileInputStream(f);
             ZipInputStream zis = new ZipInputStream(fis))
        {
            ZipEntry ze = zis.getNextEntry();
            check(ze == null);
            byte[] buf = new byte[1024];
            check(zis.read(buf, 0, 1024) == -1);
        }
    }
    static volatile int passed = 0, failed = 0;
    static boolean pass() {passed++; return true;}
    static boolean fail() {failed++; Thread.dumpStack(); return false;}
    static boolean fail(String msg) {System.out.println(msg); return fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static boolean check(boolean cond) {if (cond) pass(); else fail(); return cond;}
    static boolean equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) return pass();
        else return fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.println("\nPassed = " + passed + " failed = " + failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
