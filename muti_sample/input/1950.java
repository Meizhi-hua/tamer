public class ExitValue
{
    public static String join(String separator, String[] elts) {
        String result = elts[0];
        for (int i = 1; i < elts.length; ++i)
            result = result + separator + elts[i];
        return result;
    }
    public static void checkExitValue(String[] commandArgs,
                                      int expectedExitValue)
        throws Exception
    {
        if (! (new File(commandArgs[0]).exists()))
            return;
        System.out.println("Running command: " + join(" ", commandArgs));
        Process proc = Runtime.getRuntime().exec(commandArgs);
        int val;
        byte[] buf = new byte[4096];
        int n = proc.getErrorStream().read(buf);
        if (n > 0)
            throw new Exception
                ("Unexpected stderr: "
                 + new String(buf, 0, n, "ASCII"));
        if ((val = proc.waitFor()) != expectedExitValue)
            throw new Exception
                ("waitFor() returned unexpected value " + val);
        if ((val = proc.exitValue()) != expectedExitValue)
            throw new Exception
                ("exitValue() returned unexpected value " + val);
    }
    public static void checkPosixShellExitValue(String posixShellProgram,
                                                int expectedExitValue)
        throws Exception
    {
        checkExitValue(new String[] { "/bin/sh", "-c", posixShellProgram },
                       expectedExitValue);
    }
    final static int EXIT_CODE = 5;
    public static void main(String[] args) throws Exception {
        String java = join(File.separator, new String []
            { System.getProperty("java.home"), "bin", "java" });
        checkExitValue(new String[]
            { java,
              "-classpath", System.getProperty("test.classes", "."),
              "ExitValue$Run", String.valueOf(EXIT_CODE)
            }, EXIT_CODE);
        checkExitValue(new String[] { "/bin/true" }, 0);
        checkPosixShellExitValue("exit", 0);
        checkPosixShellExitValue("exit 7", 7);
        if (new File("/bin/kill").exists()) {
            int sigoffset =
                System.getProperty("os.name").equals("SunOS") ? 0 : 128;
            checkPosixShellExitValue("/bin/kill -9 $$", sigoffset+9);
        }
    }
    public static class Run {
        public static void main (String[] argv) {
            System.exit(Integer.parseInt(argv[0]));
        }
    }
}
