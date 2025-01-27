public class DBCS {
    static Pattern mPattern = Pattern.compile("(?:0x)?(\\p{XDigit}++)\\s++(?:0x)?(\\p{XDigit}++)(?:\\s++#.*)?");
    public static void genClass(String args[]) throws Exception {
        Scanner s = new Scanner(new File(args[0], args[2]));
        while (s.hasNextLine()) {
            String line = s.nextLine();
            if (line.startsWith("#") || line.length() == 0)
                continue;
            String[] fields = line.split("\\s+");
            if (fields.length < 10) {
                System.err.println("Misconfiged sbcs line <" + line + ">?");
                continue;
            }
            String clzName = fields[0];
            String csName  = fields[1];
            String hisName = ("null".equals(fields[2]))?null:fields[2];
            String type = fields[3].toUpperCase();
            if ("BASIC".equals(type))
                type = "";
            else
                type = "_" + type;
            String pkgName  = fields[4];
            boolean isASCII = Boolean.valueOf(fields[5]);
            int    b1Min = toInteger(fields[6]);
            int    b1Max = toInteger(fields[7]);
            int    b2Min    = toInteger(fields[8]);
            int    b2Max    = toInteger(fields[9]);
            System.out.printf("%s,%s,%s,%b,%s%n", clzName, csName, hisName, isASCII, pkgName);
            genClass0(args[0], args[1], "DoubleByte-X.java.template",
                    clzName, csName, hisName, pkgName,
                    isASCII, type,
                    b1Min, b1Max, b2Min, b2Max);
        }
    }
    static int toInteger(String s) {
        if (s.startsWith("0x") || s.startsWith("0X"))
            return Integer.valueOf(s.substring(2), 16);
        else
            return Integer.valueOf(s);
    }
    private static void genClass0(String srcDir, String dstDir, String template,
                                  String clzName,
                                  String csName,
                                  String hisName,
                                  String pkgName,
                                  boolean isASCII,
                                  String type,
                                  int b1Min, int b1Max,
                                  int b2Min, int b2Max)
        throws Exception
    {
        StringBuilder b2cSB = new StringBuilder();
        StringBuilder b2cNRSB = new StringBuilder();
        StringBuilder c2bNRSB = new StringBuilder();
        char[] db = new char[0x10000];
        char[] c2bIndex = new char[0x100];
        int c2bOff = 0x100;    
        Arrays.fill(db, UNMAPPABLE_DECODING);
        Arrays.fill(c2bIndex, UNMAPPABLE_DECODING);
        char[] b2cIndex = new char[0x100];
        Arrays.fill(b2cIndex, UNMAPPABLE_DECODING);
        FileInputStream in = new FileInputStream(new File(srcDir, clzName + ".map"));
        Parser p = new Parser(in, mPattern);
        Entry  e = null;
        while ((e = p.next()) != null) {
            db[e.bs] = (char)e.cp;
            if (e.bs > 0x100 &&    
                b2cIndex[e.bs>>8] == UNMAPPABLE_DECODING) {
                b2cIndex[e.bs>>8] = 1;
            }
            if (c2bIndex[e.cp>>8] == UNMAPPABLE_DECODING) {
                c2bOff += 0x100;
                c2bIndex[e.cp>>8] = 1;
            }
        }
        Output out = new Output(new Formatter(b2cSB));
        out.format("%n    static final String b2cSBStr =%n");
        out.format(db, 0x00, 0x100,  ";");
        out.format("%n        static final String[] b2cStr = {%n");
        for (int i = 0; i < 0x100; i++) {
            if (b2cIndex[i] == UNMAPPABLE_DECODING) {
                out.format("            null,%n");  
            } else {
                out.format(db, i, b2Min, b2Max, ",");
            }
        }
        out.format("        };%n");
        out.close();
        File f = new File(srcDir, clzName + ".nr");
        if (f.exists()) {
            StringBuilder sb = new StringBuilder();
            in = new FileInputStream(f);
            p = new Parser(in, mPattern);
            e = null;
            while ((e = p.next()) != null) {
                sb.append((char)e.bs);
                sb.append((char)e.cp);
            }
            char[] nr = sb.toString().toCharArray();
            out = new Output(new Formatter(b2cNRSB));
            out.format("String b2cNR =%n");
            out.format(nr, 0, nr.length,  ";");
            out.close();
        } else {
            b2cNRSB.append("String b2cNR = null;");
        }
        f = new File(srcDir, clzName + ".c2b");
        if (f.exists()) {
            StringBuilder sb = new StringBuilder();
            in = new FileInputStream(f);
            p = new Parser(in, mPattern);
            e = null;
            while ((e = p.next()) != null) {
                if (c2bIndex[e.cp>>8] == UNMAPPABLE_DECODING) {
                    c2bOff += 0x100;
                    c2bIndex[e.cp>>8] = 1;
                }
                sb.append((char)e.bs);
                sb.append((char)e.cp);
            }
            char[] nr = sb.toString().toCharArray();
            out = new Output(new Formatter(c2bNRSB));
            out.format("String c2bNR =%n");
            out.format(nr, 0, nr.length,  ";");
            out.close();
        } else {
            c2bNRSB.append("String c2bNR = null;");
        }
        String b2c = b2cSB.toString();
        String b2cNR = b2cNRSB.toString();
        String c2bNR = c2bNRSB.toString();
        Scanner s = new Scanner(new File(srcDir, template));
        PrintStream ops = new PrintStream(new FileOutputStream(
                             new File(dstDir, clzName + ".java")));
        if (hisName == null)
            hisName = "";
        while (s.hasNextLine()) {
            String line = s.nextLine();
            if (line.indexOf("$") == -1) {
                ops.println(line);
                continue;
            }
            line = line.replace("$PACKAGE$" , pkgName)
                       .replace("$IMPLEMENTS$", (hisName == null)?
                                "" : "implements HistoricallyNamedCharset")
                       .replace("$NAME_CLZ$", clzName)
                       .replace("$NAME_ALIASES$",
                                "sun.nio.cs".equals(pkgName) ?
                                "StandardCharsets.aliases_" + clzName :
                                "ExtendedCharsets.aliasesFor(\"" + csName + "\")")
                       .replace("$NAME_CS$" , csName)
                       .replace("$CONTAINS$",
                                "MS932".equals(clzName)?
                                "return ((cs.name().equals(\"US-ASCII\")) || (cs instanceof JIS_X_0201) || (cs instanceof " + clzName + "));":
                                (isASCII ?
                                 "return ((cs.name().equals(\"US-ASCII\")) || (cs instanceof " + clzName + "));":
                                 "return (cs instanceof " + clzName + ");"))
                       .replace("$HISTORICALNAME$",
                                (hisName == null)? "" :
                                "    public String historicalName() { return \"" + hisName + "\"; }")
                       .replace("$DECTYPE$", type)
                       .replace("$ENCTYPE$", type)
                       .replace("$B1MIN$"   , "0x" + Integer.toString(b1Min, 16))
                       .replace("$B1MAX$"   , "0x" + Integer.toString(b1Max, 16))
                       .replace("$B2MIN$"   , "0x" + Integer.toString(b2Min, 16))
                       .replace("$B2MAX$"   , "0x" + Integer.toString(b2Max, 16))
                       .replace("$B2C$", b2c)
                       .replace("$C2BLENGTH$", "0x" + Integer.toString(c2bOff, 16))
                       .replace("$NONROUNDTRIP_B2C$", b2cNR)
                       .replace("$NONROUNDTRIP_C2B$", c2bNR);
            ops.println(line);
        }
        ops.close();
    }
}
