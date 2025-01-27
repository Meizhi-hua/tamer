class CharacterCategory {
    static final String[] categoryNames = {
        "Ll",        
        "Lu",        
        "Lt",        
        "Lo",        
        "Lm",        
        "Nd",        
        "Nl",        
        "No",        
        "Ps",        
        "Pe",        
        "Pi",        
        "Pf",        
        "Pd",        
        "Pc",        
        "Po",        
        "Sc",        
        "Sm",        
        "So",         
        "Mn",        
        "Mc",        
        "Me",        
        "Zl",        
        "Zp",        
        "Zs",        
        "Cc",        
        "Cf",        
        "--",        
    };
    private static int[][] categoryMap;
    static void makeCategoryMap(String filename) {
        specfile = filename;
        generateNewData();
        categoryMap = new int[categoryNames.length-1][];
        for (int i = 0; i < categoryNames.length-1; i++) {
            int len = newListCount[BMP][i] + newListCount[nonBMP][i];
            categoryMap[i] = new int[len];
            System.arraycopy(newList[i], 0, categoryMap[i], 0, len);
        }
    }
    static int[] getCategoryMap(int category) {
        return categoryMap[category];
    }
    public static void main(String[] args) {
        processArgs(args);
        generateNewData();
        if (!oldDatafile.equals("")) {
            generateOldData();
            generateOldDatafile();
        }
         showSummary();
        generateTestProgram();
    }
    private static String specfile = "UnicodeData.txt";
    private static String outputDir = "";
    private static String oldDatafile = "";
    private static void processArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg =args[i];
            if (arg.equals("-spec")) {
                specfile = args[++i];
            } else if (arg.equals("-old")) {
                oldDatafile = args[++i];
            } else if (arg.equals("-o")) {
                outputDir = args[++i];
            } else {
                System.err.println("Usage: java CharacterCategory [-spec specfile]");
                System.exit(1);
            }
        }
    }
    private static void showSummary() {
        int oldSum = 0;
        int newSum = 0;
        int oldSuppSum = 0;
        int newSuppSum = 0;
        for (int i = 0; i < categoryNames.length-1; i++) {
            int newNum = newListCount[BMP][i] + newListCount[nonBMP][i];
            if (oldTotalCount[i] != newNum) {
                System.err.println("Error: The number of generated data is different between the new approach and the old approach.");
            }
            if (oldListCount[SURROGATE][i] != newListCount[nonBMP][i]) {
                System.err.println("Error: The number of generated supplementarycharacters is different between the new approach and the old approach.");
            }
            System.out.println("    " + categoryNames[i] + ": " +
                               oldTotalCount[i] +
                               "(" + oldListCount[BEFORE][i] +
                               " + " + oldListCount[SURROGATE][i] +
                               " + " + oldListCount[AFTER][i] + ")" +
                               " --- " + newNum +
                               "(" + newListCount[BMP][i] +
                               " + " + newListCount[nonBMP][i] + ")");
            oldSum += oldListCount[BEFORE][i] * 2 +
                      oldListCount[SURROGATE][i] * 4 +
                      oldListCount[AFTER][i] * 2;
            newSum += newNum * 4 ;
            oldSuppSum += oldListCount[SURROGATE][i] * 4;
            newSuppSum += newListCount[nonBMP][i] * 4;
        }
        System.out.println("\nTotal buffer sizes are:\n    " +
                           oldSum + "bytes(Including " + oldSuppSum +
                           "bytes for supplementary characters)\n    " +
                           newSum + "bytes(Including " + newSuppSum +
                           "bytes for supplementary characters)");
        if (!ignoredOld.toString().equals(ignoredNew.toString())) {
            System.err.println("Ignored categories: Error: List mismatch: " +
                                ignoredOld + " vs. " + ignoredNew);
        } else {
            System.out.println("\nIgnored categories: " + ignoredOld);
            System.out.println("Please confirm that they aren't used in BreakIteratorRules.");
        }
    }
    private static final int HighSurrogate_CodeUnit_Start = 0xD800;
    private static final int LowSurrogate_CodeUnit_Start  = 0xDC00;
    private static final int Supplementary_CodePoint_Start    = 0x10000;
    private static StringBuffer ignoredOld = new StringBuffer();
    private static int[] oldTotalCount = new int[categoryNames.length];
    private static int[][] oldListCount = new int[3][categoryNames.length];
    private static int[][] oldListLen = new int[3][categoryNames.length];
    private static StringBuffer[][] oldList = new StringBuffer[3][categoryNames.length];
    private static final int BEFORE = 0;
    private static final int SURROGATE = 1;
    private static final int AFTER = 2;
    private static void generateOldData() {
        for (int i = 0; i<categoryNames.length; i++) {
            for (int j = BEFORE; j <= AFTER; j++) {
                oldListCount[j][i] = 0;
                oldList[j][i] = new StringBuffer();
                oldListLen[j][i] = 17;
            }
        }
        storeOldData();
        if (oldTotalCount[categoryNames.length-1] != 1) {
            System.err.println("This should not happen. Unicode data which belongs to an undefined category exists");
            System.exit(1);
        }
    }
    private static void storeOldData() {
        try {
            FileReader fin = new FileReader(specfile);
            BufferedReader bin = new BufferedReader(fin);
            String prevCode = "????";
            String line;
            int prevIndex = categoryNames.length - 1;
            int prevCodeValue = -1;
            int curCodeValue = 0;
            boolean setFirst = false;
            while ((line = bin.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(line, ";");
                String code = st.nextToken();
                char c = code.charAt(0);
                if (c == '#' || c == '/') {
                    continue;
                }
                int i = Integer.valueOf(code, 16).intValue();
                String characterName = st.nextToken();
                String category = st.nextToken();
                int index;
                for (index = 0; index < categoryNames.length; index++) {
                    if (category.equals(categoryNames[index])) {
                        break;
                    }
                }
                if (index != categoryNames.length) {
                    curCodeValue = Integer.parseInt(code, 16);
                    if (prevIndex != index) {
                        appendOldChar(prevIndex, prevCodeValue, prevCode);
                        appendOldChar(index, curCodeValue, code);
                        prevIndex = index;
                    } else if (prevCodeValue != curCodeValue - 1) {
                        if (setFirst && characterName.endsWith(" Last>")) {
                            setFirst = false;
                        } else {
                            appendOldChar(prevIndex, prevCodeValue, prevCode);
                            appendOldChar(index, curCodeValue, code);
                        }
                    }
                    prevCodeValue = curCodeValue;
                    prevCode = code;
                    if (characterName.endsWith(" First>")) {
                        setFirst = true;
                    }
                } else {
                    if (ignoredOld.indexOf(category) == -1) {
                        ignoredOld.append(category);
                        ignoredOld.append(' ');
                    }
                }
            }
            appendOldChar(prevIndex, prevCodeValue, prevCode);
            bin.close();
            fin.close();
        }
        catch (Exception e) {
            throw new InternalError(e.toString());
        }
    }
    private static void appendOldChar(int index, int code, String s) {
        int range;
        if (code < HighSurrogate_CodeUnit_Start) {
            range = BEFORE;
        } else if (code < Supplementary_CodePoint_Start) {
            range = AFTER;
        } else {
            range = SURROGATE;
        }
        if (oldListLen[range][index] > 64) {
            oldList[range][index].append("\"\n                + \"");
            oldListLen[range][index] = 19;
        }
        if (code == 0x22 || code == 0x5c) {
            oldList[range][index].append('\\');
            oldList[range][index].append((char)code);
            oldListLen[range][index] += 2;
        } else if (code > 0x20 && code < 0x7F) {
            oldList[range][index].append((char)code);
            oldListLen[range][index] ++;
        } else {
            if (range == SURROGATE) {
                oldList[range][index].append(toCodeUnit(code));
                oldListLen[range][index] += 12;
            } else {
                oldList[range][index].append("\\u");
                oldList[range][index].append(s);
                oldListLen[range][index] += 6;
            }
        }
        oldListCount[range][index] ++;
        oldTotalCount[index]++;
    }
    private static String toCodeUnit(int i) {
        StringBuffer sb = new StringBuffer();
        sb.append("\\u");
        sb.append(Integer.toString((i - Supplementary_CodePoint_Start) / 0x400 + HighSurrogate_CodeUnit_Start, 16).toUpperCase());
        sb.append("\\u");
        sb.append(Integer.toString(i % 0x400 + LowSurrogate_CodeUnit_Start, 16).toUpperCase());
        return sb.toString();
    }
    private static int toCodePoint(String s) {
        char c1 = s.charAt(0);
        if (s.length() == 1 || !Character.isHighSurrogate(c1)) {
            return (int)c1;
        } else {
            char c2 = s.charAt(1);
            if (s.length() != 2 || !Character.isLowSurrogate(c2)) {
                return -1;
            }
            return Character.toCodePoint(c1, c2);
        }
    }
    private static StringBuffer ignoredNew = new StringBuffer();
    private static int[] newTotalCount = new int[categoryNames.length];
    private static int[][] newListCount = new int[2][categoryNames.length];
    private static int[][] newList = new int[categoryNames.length][];
    private static final int BMP = 0;
    private static final int nonBMP = 1;
    private static void generateNewData() {
        for (int i = 0; i<categoryNames.length; i++) {
            newList[i] = new int[10];
        }
        storeNewData();
        if (newListCount[BMP][categoryNames.length-1] != 1) {
            System.err.println("This should not happen. Unicode data which belongs to an undefined category exists");
            System.exit(1);
        }
    }
    private static void storeNewData() {
        try {
            FileReader fin = new FileReader(specfile);
            BufferedReader bin = new BufferedReader(fin);
            String line;
            int prevIndex = categoryNames.length - 1;
            int prevCodeValue = -1;
            int curCodeValue = 0;
            boolean setFirst = false;
            while ((line = bin.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(line, ";");
                String code = st.nextToken();
                char c = code.charAt(0);
                if (c == '#' || c == '/') {
                    continue;
                }
                int i = Integer.valueOf(code, 16).intValue();
                String characterName = st.nextToken();
                String category = st.nextToken();
                int index;
                for (index = 0; index < categoryNames.length; index++) {
                    if (category.equals(categoryNames[index])) {
                        break;
                    }
                }
                if (index != categoryNames.length) {
                    curCodeValue = Integer.parseInt(code, 16);
                    if (prevIndex == index) {
                        if (setFirst) {
                            if (characterName.endsWith(" Last>")) {
                                setFirst = false;
                            } else {
                                System.err.println("*** Error 1 at " + code);
                            }
                        } else {
                            if (characterName.endsWith(" First>")) {
                                setFirst = true;
                            } else if (characterName.endsWith(" Last>")) {
                                System.err.println("*** Error 2 at " + code);
                            } else {
                                if (prevCodeValue != curCodeValue - 1) {
                                    appendNewChar(prevIndex, prevCodeValue);
                                    appendNewChar(index, curCodeValue);
                                }
                            }
                        }
                    } else {
                        if (setFirst) {
                            System.err.println("*** Error 3 at " + code);
                        } else if (characterName.endsWith(" First>")) {
                            setFirst = true;
                        } else if (characterName.endsWith(" Last>")) {
                            System.err.println("*** Error 4 at " + code);
                        }
                        appendNewChar(prevIndex, prevCodeValue);
                        appendNewChar(index, curCodeValue);
                        prevIndex = index;
                    }
                    prevCodeValue = curCodeValue;
                } else {
                    if (ignoredNew.indexOf(category) == -1) {
                        ignoredNew.append(category);
                        ignoredNew.append(' ');
                    }
                }
            }
            appendNewChar(prevIndex, prevCodeValue);
            bin.close();
            fin.close();
        }
        catch (Exception e) {
            System.err.println("Error occurred on accessing " + specfile);
            e.printStackTrace();
            System.exit(1);
        }
    }
    private static void appendNewChar(int index, int code) {
        int bufLen = newList[index].length;
        if (newTotalCount[index] == bufLen) {
            int[] tmpBuf = new int[bufLen + 10];
            System.arraycopy(newList[index], 0, tmpBuf, 0, bufLen);
            newList[index] = tmpBuf;
        }
        newList[index][newTotalCount[index]++] = code;
        if (code < 0x10000) {
            newListCount[BMP][index]++;
        } else {
            newListCount[nonBMP][index]++;
        }
    }
    private static void generateOldDatafile() {
        try {
            FileWriter fout = new FileWriter(oldDatafile);
            BufferedWriter bout = new BufferedWriter(fout);
            bout.write("\n    
            for (int i = 0; i < categoryNames.length - 1; i++) {
                if (oldTotalCount[i] != 0) {
                    bout.write("        { \"" + categoryNames[i] + "\",");
                    if (oldListCount[BEFORE][i] != 0) {
                        bout.write(" \"");
                        bout.write(oldList[BEFORE][i].toString() + "\"\n");
                    }
                    if (oldListCount[AFTER][i] != 0) {
                        if (oldListCount[BEFORE][i] != 0) {
                            bout.write("                + \"");
                        } else {
                            bout.write(" \"");
                        }
                        bout.write(oldList[AFTER][i].toString() + "\"\n");
                    }
                    if (oldListCount[SURROGATE][i] != 0) {
                        if (oldListCount[BEFORE][i] != 0 || oldListCount[AFTER][i] != 0) {
                            bout.write("                + \"");
                        } else {
                            bout.write(" \"");
                        }
                        bout.write(oldList[SURROGATE][i].toString() + "\"\n");
                    }
                    bout.write("        },\n");
                }
            }
            bout.write("    };\n\n");
            bout.close();
            fout.close();
        }
        catch (Exception e) {
            System.err.println("Error occurred on accessing " + oldDatafile);
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("\n" + oldDatafile + " has been generated.");
    }
    private static final String outfile = "CharacterCategoryTest.java";
    private static void generateTestProgram() {
        try {
            FileWriter fout = new FileWriter(outfile);
            BufferedWriter bout = new BufferedWriter(fout);
            bout.write(collationMethod);
            bout.write("\n    
            bout.write("    private static final String[] categoryNames = {");
            for (int i = 0; i < categoryNames.length - 1; i++) {
                if (i % 10 == 0) {
                    bout.write("\n        ");
                }
                bout.write("\"" + categoryNames[i] + "\", ");
            }
            bout.write("\n    };\n\n");
            bout.write("    private static final int[][] categoryMap = {\n");
            for (int i = 0; i < categoryNames.length - 1; i++) {
                StringBuffer sb = new StringBuffer("        { ");
                for (int j = 0; j < newTotalCount[i]; j++) {
                    if (j % 8 == 0) {
                        sb.append("\n        ");
                    }
                    sb.append(" 0x");
                    sb.append(Integer.toString(newList[i][j], 16).toUpperCase());
                    sb.append(',');
                }
                sb.append("\n        },\n");
                bout.write(sb.toString());
            }
            bout.write("    };\n");
            bout.write("\n}\n");
            bout.close();
            fout.close();
        }
        catch (Exception e) {
            System.err.println("Error occurred on accessing " + outfile);
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("\n" + outfile + " has been generated.");
    }
    static String collationMethod =
"public class CharacterCategoryTest {\n\n" +
"    static final int SIZE = 0x110000;\n" +
"    static final String[] category = {\n" +
"       \"Cn\", \"Lu\", \"Ll\", \"Lt\", \"Lm\", \"Lo\", \"Mn\", \"Me\",\n" +
"       \"Mc\", \"Nd\", \"Nl\", \"No\", \"Zs\", \"Zl\", \"Zp\", \"Cc\",\n" +
"       \"Cf\", \"\",   \"Co\", \"Cs\", \"Pd\", \"Ps\", \"Pe\", \"Pc\",\n" +
"       \"Po\", \"Sm\", \"Sc\", \"Sk\", \"So\", \"Pi\", \"Pf\"\n" +
"    };\n\n" +
"    public static void main(String[] args) {\n" +
"        boolean err = false;\n" +
"        byte[] b = new byte[SIZE];\n" +
"        for (int i = 0; i < SIZE; i++) {\n" +
"            b[i] = 0;\n" +
"        }\n" +
"        for (int i = 0; i < categoryMap.length; i++) {\n" +
"            byte categoryNum = 0;\n" +
"            String categoryName = categoryNames[i];\n" +
"            for (int j = 0; j < category.length; j++) {\n" +
"                if (categoryName.equals(category[j])) {\n" +
"                    categoryNum = (byte)j;\n" +
"                    break;\n" +
"                }\n" +
"            }\n" +
"            int[] values = categoryMap[i];\n" +
"            for (int j = 0; j < values.length;) {\n" +
"                int firstChar = values[j++];\n" +
"                int lastChar = values[j++];\n" +
"                for (int k = firstChar; k <= lastChar; k++) {\n" +
"                    b[k] = categoryNum;\n" +
"                }\n" +
"            }\n" +
"        }\n" +
"        for (int i = 0; i < SIZE; i++) {\n" +
"            int characterType = Character.getType(i);\n" +
"            if (b[i] != characterType) {\n" +
"                \n" +
"                if (characterType == Character.PRIVATE_USE ||\n" +
"                    characterType == Character.SURROGATE ||\n" +
"                    characterType == Character.MODIFIER_SYMBOL) {\n" +
"                    continue;\n" +
"                }\n" +
"                err = true;\n" +
"                System.err.println(\"Category conflict for a character(0x\" +\n" +
"                                   Integer.toHexString(i) +\n" +
"                                   \"). CharSet.categoryMap:\" +\n" +
"                                   category[b[i]] +\n" +
"                                   \"  Character.getType():\" +\n" +
"                                   category[characterType]);\n" +
"            }\n" +
"        }\n\n" +
"        if (err) {\n" +
"            throw new RuntimeException(\"Conflict occurred between Charset.categoryMap and Character.getType()\");\n" +
"        }\n" +
"    }\n";
}
