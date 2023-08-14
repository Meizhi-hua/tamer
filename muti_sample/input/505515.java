public class FsUtils {
    private static final String LOGTAG = "FsUtils";
    static final String HTTP_TESTS_PREFIX = "/sdcard/android/layout_tests/http/tests/";
    static final String HTTPS_TESTS_PREFIX = "/sdcard/android/layout_tests/http/tests/ssl/";
    static final String HTTP_LOCAL_TESTS_PREFIX = "/sdcard/android/layout_tests/http/tests/local/";
    static final String HTTP_MEDIA_TESTS_PREFIX = "/sdcard/android/layout_tests/http/tests/media/";
    static final String HTTP_WML_TESTS_PREFIX = "/sdcard/android/layout_tests/http/tests/wml/";
    private FsUtils() {
    }
    public static void findLayoutTestsRecursively(BufferedOutputStream bos,
            String dir, boolean ignoreResultsInDir) throws IOException {
        Log.v(LOGTAG, "Searching tests under " + dir);
        File d = new File(dir);
        if (!d.isDirectory()) {
            throw new AssertionError("A directory expected, but got " + dir);
        }
        ignoreResultsInDir |= FileFilter.ignoreResult(dir);
        String[] files = d.list();
        for (int i = 0; i < files.length; i++) {
            String s = dir + "/" + files[i];
            File f = new File(s);
            if (f.isDirectory()) {
                if (!FileFilter.isNonTestDir(s)) {
                    Log.v(LOGTAG, "Recursing on " + s);
                    findLayoutTestsRecursively(bos, s, ignoreResultsInDir);
                }
                continue;
            }
            if (FileFilter.ignoreTest(s)) {
                Log.v(LOGTAG, "Ignoring: " + s);
                continue;
            }
            if ((s.toLowerCase().endsWith(".html") || s.toLowerCase().endsWith(".xml"))
                    && !s.endsWith("TEMPLATE.html")) {
                Log.v(LOGTAG, "Recording " + s);
                bos.write(s.getBytes());
                if (ignoreResultsInDir || FileFilter.ignoreResult(s)) {
                    bos.write((" IGNORE_RESULT").getBytes());
                }
                bos.write('\n');
            }
        }
    }
    public static void updateTestStatus(String statusFile, String s) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(statusFile));
            bos.write(s.getBytes());
            bos.close();
        } catch (Exception e) {
            Log.e(LOGTAG, "Cannot update file " + statusFile);
        }
    }
    public static String readTestStatus(String statusFile) {
        String status = null;
        File testStatusFile = new File(statusFile);
        if(testStatusFile.exists()) {
            try {
                BufferedReader inReader = new BufferedReader(
                        new FileReader(testStatusFile));
                status = inReader.readLine();
                inReader.close();
            } catch (IOException e) {
                Log.e(LOGTAG, "Error reading test status.", e);
            }
        }
        return status;
    }
    public static String getTestUrl(String path) {
        String url = null;
        if (!path.startsWith(HTTP_TESTS_PREFIX)) {
            url = "file:
        } else {
            ForwardService.getForwardService().startForwardService();
            if (path.startsWith(HTTPS_TESTS_PREFIX)) {
                url = "https:
            } else if (!path.startsWith(HTTP_LOCAL_TESTS_PREFIX)
                    && !path.startsWith(HTTP_MEDIA_TESTS_PREFIX)
                    && !path.startsWith(HTTP_WML_TESTS_PREFIX)) {
                url = "http:
            } else {
                url = "file:
            }
        }
        return url;
    }
    public static boolean diffIgnoreSpaces(String file1, String file2)  throws IOException {
        BufferedReader br1 = new BufferedReader(new FileReader(file1));
        BufferedReader br2 = new BufferedReader(new FileReader(file2));
        boolean same = true;
        Pattern trailingSpace = Pattern.compile("\\s+$");
        while(true) {
            String line1 = br1.readLine();
            String line2 = br2.readLine();
            if (line1 == null && line2 == null)
                break;
            if (line1 != null) {
                line1 = trailingSpace.matcher(line1).replaceAll("");
            } else {
                line1 = "";
            }
            if (line2 != null) {
                line2 = trailingSpace.matcher(line2).replaceAll("");
            } else {
                line2 = "";
            }
            if(!line1.equals(line2)) {
                same = false;
                break;
            }
        }
        br1.close();
        br2.close();
        return same;
    }
    public static boolean isTestPageUrl(String url) {
        int qmPostion = url.indexOf('?');
        int slashPostion = url.lastIndexOf('/');
        if (slashPostion < qmPostion) {
            String fileName = url.substring(slashPostion + 1, qmPostion);
            if ("index.html".equals(fileName)) {
                return true;
            }
        }
        return false;
    }
    public static String getLastSegmentInPath(String path) {
        int endPos = path.lastIndexOf('/');
        path = path.substring(0, endPos);
        endPos = path.lastIndexOf('/');
        return path.substring(endPos + 1);
    }
    public static void writeDrawTime(String fileName, String url, long[] times) {
        StringBuffer lineBuffer = new StringBuffer();
        lineBuffer.append(getLastSegmentInPath(url));
        for (long time : times) {
            lineBuffer.append('\t');
            lineBuffer.append(time);
        }
        lineBuffer.append('\n');
        String line = lineBuffer.toString();
        Log.v(LOGTAG, "logging draw times: " + line);
        try {
            FileWriter fw = new FileWriter(fileName, true);
            fw.write(line);
            fw.close();
        } catch (IOException ioe) {
            Log.e(LOGTAG, "Failed to log draw times", ioe);
        }
    }
}
