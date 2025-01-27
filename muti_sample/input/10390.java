public class IIOComparator {
    private static final DecimalFormat decimalFormat =
        new DecimalFormat("0.0");
    private static final String[] methodKeys = new String[] {
        "IIO-Core", "IIO-Ext", "Toolkit", "JPEGCodec", "GdkPixBuf"
    };
    private static final Hashtable allResults = new Hashtable();
    private static boolean wikiStyle;
    private static void printIIOTable(String resultsFile) {
        try {
            J2DAnalyzer.readResults(resultsFile);
        } catch (Exception e) {
            System.err.println("Error reading results file: " +
                               e.getMessage());
            return;
        }
        Vector results = J2DAnalyzer.results;
        int numsets = results.size();
        ResultSetHolder base = (ResultSetHolder)results.elementAt(0);
        Enumeration basekeys = base.getKeyEnumeration();
        String[] keys = toSortedArray(basekeys, false);
        for (int k = 0; k < keys.length; k++) {
            String key = keys[k];
            ResultHolder rh = base.getResultByKey(key);
            double score = rh.getScore();
            Hashtable opts = rh.getOptions();
            String imgsize = (String)opts.get("imageio.opts.size");
            String content = (String)opts.get("imageio.opts.content");
            String testname = "size=" + imgsize + ",content=" + content;
            String format = null;
            String method = null;
            String name = rh.getName();
            if (name.equals("imageio.input.image.imageio.reader.tests.read")) {
                format = (String)opts.get("imageio.input.image.imageio.opts.format");
                String type = format.substring(0, format.indexOf('-'));
                format = format.substring(format.indexOf('-')+1);
                if (format.equals("jpeg")) {
                    format = "jpg";
                }
                method = "IIO-" + (type.equals("core") ? "Core" : "Ext");
            } else if (name.equals("imageio.input.image.toolkit.tests.createImage")) {
                format = (String)opts.get("imageio.input.image.toolkit.opts.format");
                method = "Toolkit";
            } else if (name.equals("imageio.input.image.toolkit.tests.gdkLoadImage")) {
                format = (String)opts.get("imageio.input.image.toolkit.opts.format");
                method = "GdkPixBuf";
            } else if (name.equals("imageio.input.image.jpegcodec.tests.decodeAsBufferedImage")) {
                format = "jpg";
                method = "JPEGCodec";
            } else {
                System.err.println("skipping unrecognized key: " + name);
                continue;
            }
            Hashtable fmtResults = (Hashtable)allResults.get(format);
            if (fmtResults == null) {
                fmtResults = new Hashtable();
                allResults.put(format, fmtResults);
            }
            Hashtable testResults = (Hashtable)fmtResults.get(testname);
            if (testResults == null) {
                testResults = new Hashtable();
                fmtResults.put(testname, testResults);
            }
            testResults.put(method, new Double(score));
        }
        if (wikiStyle) {
            printWikiTable();
        } else {
            printHtmlTable();
        }
    }
    private static void printWikiTable() {
        Enumeration allKeys = allResults.keys();
        while (allKeys.hasMoreElements()) {
            String format = (String)allKeys.nextElement();
            System.out.println("---+++ " + format.toUpperCase());
            Hashtable fmtResults = (Hashtable)allResults.get(format);
            Enumeration testKeys = fmtResults.keys();
            String[] tests = toSortedArray(testKeys, true);
            Hashtable testResults = (Hashtable)fmtResults.get(tests[0]);
            String[] methods = new String[testResults.keySet().size()];
            for (int k = 0, i = 0; i < methodKeys.length; i++) {
                if (testResults.containsKey(methodKeys[i])) {
                    methods[k++] = methodKeys[i];
                }
            }
            System.out.print("| |");
            for (int i = 0; i < methods.length; i++) {
                System.out.print(" *" + methods[i] + "* |");
                if (i > 0) {
                    System.out.print(" *%* |");
                }
            }
            System.out.println("");
            for (int i = 0; i < tests.length; i++) {
                String testname = tests[i];
                testResults = (Hashtable)fmtResults.get(testname);
                System.out.print("| " + testname + " |");
                double baseres = 0.0;
                for (int j = 0; j < methods.length; j++) {
                    Double result = (Double)testResults.get(methods[j]);
                    double res = result.doubleValue();
                    System.out.print("   " +
                                     decimalFormat.format(res) +
                                     " | ");
                    if (j == 0) {
                        baseres = res;
                    } else {
                        double diff = ((res - baseres) / baseres) * 100.0;
                        System.out.print("   "+
                                         decimalFormat.format(diff) +
                                         " |");
                    }
                }
                System.out.println("");
            }
            System.out.println("");
        }
    }
    private static void printHtmlTable() {
        System.out.println("<html><body>\n");
        Enumeration allKeys = allResults.keys();
        while (allKeys.hasMoreElements()) {
            String format = (String)allKeys.nextElement();
            System.out.println("<h3>" + format.toUpperCase() + "</h3>");
            System.out.println("<table border=\"1\">");
            Hashtable fmtResults = (Hashtable)allResults.get(format);
            Enumeration testKeys = fmtResults.keys();
            String[] tests = toSortedArray(testKeys, true);
            Hashtable testResults = (Hashtable)fmtResults.get(tests[0]);
            String[] methods = new String[testResults.keySet().size()];
            for (int k = 0, i = 0; i < methodKeys.length; i++) {
                if (testResults.containsKey(methodKeys[i])) {
                    methods[k++] = methodKeys[i];
                }
            }
            System.out.print("<tr><td></td>");
            for (int i = 0; i < methods.length; i++) {
                printHtmlCell("<b>"+methods[i]+"</b>", "#99CCCC", "center");
                if (i > 0) {
                    printHtmlCell("<b>%</b>", "#99CCCC", "center");
                }
            }
            System.out.println("</tr>");
            for (int i = 0; i < tests.length; i++) {
                String rowcolor = (i % 2 == 0) ? "#FFFFCC" : "#FFFFFF";
                String testname = tests[i];
                testResults = (Hashtable)fmtResults.get(testname);
                System.out.print("<tr>");
                printHtmlCell(testname, rowcolor, "left");
                double baseres = 0.0;
                for (int j = 0; j < methods.length; j++) {
                    Double result = (Double)testResults.get(methods[j]);
                    double res = result.doubleValue();
                    printHtmlCell(decimalFormat.format(res),
                                  rowcolor, "right");
                    if (j == 0) {
                        baseres = res;
                    } else {
                        double diff = ((res - baseres) / baseres) * 100.0;
                        String cellcolor;
                        if (Math.abs(diff) <= 5.0) {
                            cellcolor = "#CFCFFF";
                        } else if (diff < -5.0) {
                            cellcolor = "#CFFFCF";
                        } else {
                            cellcolor = "#FFCFCF";
                        }
                        String difftext = decimalFormat.format(diff);
                        if (diff > 0.0) {
                            difftext = "+" + difftext;
                        }
                        printHtmlCell(difftext, cellcolor, "right");
                        System.out.println("");
                    }
                }
                System.out.println("</tr>");
            }
            System.out.println("</table><br>\n");
        }
        System.out.println("</body></html>");
    }
    private static void printHtmlCell(String s, String color, String align) {
        System.out.print("<td bgcolor=\"" + color +
                         "\" align=\"" + align + "\">" + s +
                         "</td>");
    }
    private static String[] toSortedArray(Enumeration e, boolean special) {
        Vector keylist = new Vector();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            keylist.add(key);
        }
        String keys[] = new String[keylist.size()];
        keylist.copyInto(keys);
        if (special) {
            sort2(keys);
        } else {
            sort(keys);
        }
        return keys;
    }
    public static void sort(String strs[]) {
        for (int i = 1; i < strs.length; i++) {
            for (int j = i; j > 0; j--) {
                if (strs[j].compareTo(strs[j-1]) >= 0) {
                    break;
                }
                String tmp = strs[j-1];
                strs[j-1] = strs[j];
                strs[j] = tmp;
            }
        }
    }
    public static void sort2(String strs[]) {
        for (int i = 1; i < strs.length; i++) {
            for (int j = i; j > 0; j--) {
                if (compare(strs[j-1], strs[j])) {
                    break;
                }
                String tmp = strs[j-1];
                strs[j-1] = strs[j];
                strs[j] = tmp;
            }
        }
    }
    private static int magic(String s) {
        if (s.endsWith("random")) {
            return 3;
        } else if (s.endsWith("photo")) {
            return 2;
        } else if (s.endsWith("vector")) {
            return 1;
        } else {
            return 0;
        }
    }
    private static boolean compare(String s1, String s2) {
        String sizestr1 = s1.substring(s1.indexOf('=')+1, s1.indexOf(','));
        String sizestr2 = s2.substring(s2.indexOf('=')+1, s2.indexOf(','));
        int size1 = Integer.parseInt(sizestr1);
        int size2 = Integer.parseInt(sizestr2);
        if (size1 == size2) {
            return (magic(s1) < magic(s2));
        } else {
            return (size1 < size2);
        }
    }
    private static void printUsage() {
        System.out.println("java -cp J2DAnalyzer.jar " +
                           IIOComparator.class.getName() +
                           " [-wiki] <resultfile>");
    }
    public static void main(String[] args) {
        if (args.length == 2) {
            if (args[0].equals("-wiki")) {
                wikiStyle = true;
                printIIOTable(args[1]);
            } else {
                printUsage();
            }
        } else if (args.length == 1) {
            printIIOTable(args[0]);
        } else {
            printUsage();
        }
    }
}
