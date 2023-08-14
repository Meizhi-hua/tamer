public class NIOJISAutoDetectTest {
    private static int failures = 0;
    private static void fail(String failureMsg) {
        System.out.println(failureMsg);
        failures++;
    }
    private static void check(boolean cond, String msg) {
        if (!cond) {
            fail("test failed: " + msg);
            new Exception().printStackTrace();
        }
    }
    private static String SJISName() throws Exception {
        return detectingCharset(new byte[] {(byte)0xbb, (byte)0xdd,
                                            (byte)0xcf, (byte)0xb2});
    }
    private static String EUCJName() throws Exception {
        return detectingCharset(new byte[] {(byte)0xa4, (byte)0xd2,
                                            (byte)0xa4, (byte)0xe9});
    }
    private static String detectingCharset(byte[] bytes) throws Exception {
        CharsetDecoder cd = Charset.forName("JISAutodetect").newDecoder();
        check(cd.isAutoDetecting(), "isAutodecting()");
        check(! cd.isCharsetDetected(), "isCharsetDetected");
        cd.decode(ByteBuffer.wrap(new byte[] {(byte)'A'}));
        check(! cd.isCharsetDetected(), "isCharsetDetected");
        try {
            cd.detectedCharset();
            fail("no IllegalStateException");
        } catch (IllegalStateException e) {}
        cd.decode(ByteBuffer.wrap(bytes));
        check(cd.isCharsetDetected(), "isCharsetDetected");
        Charset cs = cd.detectedCharset();
        check(cs != null, "cs != null");
        check(! cs.newDecoder().isAutoDetecting(), "isAutodetecting()");
        return cs.name();
    }
    public static void main(String[] argv) throws Exception {
        out.println(new String(new byte[] {0x61}, "JISAutoDetect"));
        {
            byte[] bytes = "ABCD\n".getBytes();
            ByteArrayInputStream bais = new  ByteArrayInputStream(bytes);
            InputStreamReader isr = new InputStreamReader(bais, "JISAutoDetect");
            BufferedReader reader = new BufferedReader(isr);
            check (reader.readLine().equals("ABCD"), "first read gets text");
            check (reader.readLine() == null, "second read gets null");
        }
        String SJIS = SJISName();
        String EUCJ = EUCJName();
        out.printf("SJIS charset is %s%n", SJIS);
        out.printf("EUCJ charset is %s%n", EUCJ);
        int cnt2022 = 0;
        int cnteucj = 0;
        int cntsjis = 0;
        int cntBAD  = 0;
        for (char c = '\u0000'; c < '\uffff'; c++) {
            if (c == '\u001b' || 
                c == '\u2014')   
                continue;
            String s = new String (new char[] {c});
            byte[] beucj = s.getBytes(EUCJ);
            String seucj = new String(beucj, EUCJ);
            if (seucj.equals(s)) {
                cnteucj++;
                String sauto = new String(beucj, "JISAutoDetect");
                if (! sauto.equals(seucj)) {
                    cntBAD++;
                    String ssjis = new String(beucj, SJIS);
                    if (! sauto.equals(ssjis)) {
                        fail("Autodetection agrees with neither EUC nor SJIS");
                    }
                }
            } else
                continue; 
            byte[] b2022 = s.getBytes("ISO-2022-JP");
            if (new String(b2022, "ISO-2022-JP").equals(s)) {
                cnt2022++;
                check(new String(b2022,"JISAutoDetect").equals(s),
                      "ISO2022 autodetection");
            }
            byte[] bsjis = s.getBytes(SJIS);
            if (new String(bsjis, SJIS).equals(s)) {
                cntsjis++;
                check(new String(bsjis,"JISAutoDetect").equals(s),
                      "SJIS autodetection");
            }
        }
        out.printf("There are %d ISO-2022-JP-encodable characters.%n", cnt2022);
        out.printf("There are %d SJIS-encodable characters.%n",        cntsjis);
        out.printf("There are %d EUC-JP-encodable characters.%n",      cnteucj);
        out.printf("There are %d characters that are " +
                   "misdetected as SJIS after being EUC-encoded.%n", cntBAD);
        test("ISO-2022-JP", new byte[] {'A', 'B', 'C'});
        test("EUC-JP",      new byte[] {'A', 'B', 'C'});
        test("SJIS",        new byte[] {'A', 'B', 'C'});
        test("SJIS",
             new byte[] { 'C', 'o', 'p',  'y',  'r', 'i', 'g',  'h', 't',
                          ' ', (byte)0xa9, ' ', '1', '9', '9',  '8' });
        test("SJIS",
             new byte[] { (byte)0xbb, (byte)0xdd, (byte)0xcf, (byte)0xb2,
                          (byte)0xb8, (byte)0xdb, (byte)0xbc, (byte)0xbd,
                          (byte)0xc3, (byte)0xd1, (byte)0xbd, (byte)0xde,
                          (byte)0x82, (byte)0xc5, (byte)0x82, (byte)0xb7 });
        test("EUC-JP",
             new byte[] { (byte)0xa4, (byte)0xd2, (byte)0xa4, (byte)0xe9,
                          (byte)0xa4, (byte)0xac, (byte)0xa4, (byte)0xca });
        test("SJIS",
             new byte[] { (byte)0xbb, (byte)0xdd, (byte)0xcf, (byte)0xb2,
                          (byte)0xb8, (byte)0xdb, (byte)0xbc, (byte)0xbd,
                          (byte)0xc3, (byte)0xd1, (byte)0xbd, (byte)0xde});
        test("SJIS",
             new byte[] { (byte)0xbb, (byte)0xdd, (byte)0xcf, (byte)0xb2,
                          (byte)0xb8, (byte)0xdb, (byte)0xbc, (byte)0xbd,
                          (byte)0xc3, (byte)0xd1, (byte)0xbd });
        test("SJIS",
             new byte[] { (byte)0x8f, (byte)0xa1, (byte)0xaa });
        test("EUC-JP",
             new byte[] { (byte)0x8f, (byte)0xc5, (byte)0xe0, (byte)0x20});
        test("EUC-JP",
             new byte[] { (byte)0xbb, (byte)0xdd, (byte)0xcf, (byte)0xb2,
                          (byte)0xb8, (byte)0xdb, (byte)0xbc, (byte)0xbd,
                          (byte)0xc3, (byte)0xd1, (byte)0xbd, (byte)0xde,
                          (byte)0xa4, (byte)0xc7, (byte)0xa4, (byte)0xb9 });
        test("ISO-2022-JP",
             new byte[] { 0x1b, '$', 'B', '#', '4', '$', '5', 0x1b, '(', 'B' });
        {
            CharsetDecoder dc = Charset.forName("x-JISAutoDetect").newDecoder();
            ByteBuffer bb = ByteBuffer.allocate(128);
            CharBuffer cb = CharBuffer.allocate(128);
            bb.put((byte)'A').put((byte)0x8f);
            bb.flip();
            CoderResult res = dc.decode(bb,cb,false);
            check(res.isUnderflow(), "isUnderflow");
            check(bb.position() == 1, "bb.position()");
            check(cb.position() == 1, "cb.position()");
            res = dc.decode(bb,cb,false);
            check(res.isUnderflow(), "isUnderflow");
            check(bb.position() == 1, "bb.position()");
            check(cb.position() == 1, "cb.position()");
            bb.compact();
            bb.put((byte)0xa1);
            bb.flip();
            res = dc.decode(bb,cb,true);
            check(res.isUnderflow(), "isUnderflow");
            check(bb.position() == 2, "bb.position()");
            check(cb.position() == 2, "cb.position()");
        }
        if (failures > 0)
            throw new RuntimeException(failures + " tests failed");
    }
    static void checkCoderResult(CoderResult result) {
        check(result.isUnderflow(),
              "Unexpected coder result: " + result);
    }
    static void test(String expectedCharset, byte[] input) throws Exception {
        Charset cs = Charset.forName("x-JISAutoDetect");
        CharsetDecoder autoDetect = cs.newDecoder();
        Charset cs2 = Charset.forName(expectedCharset);
        CharsetDecoder decoder = cs2.newDecoder();
        ByteBuffer bb = ByteBuffer.allocate(128);
        CharBuffer charOutput = CharBuffer.allocate(128);
        CharBuffer charExpected = CharBuffer.allocate(128);
        bb.put(input);
        bb.flip();
        bb.mark();
        CoderResult result = autoDetect.decode(bb, charOutput, true);
        checkCoderResult(result);
        charOutput.flip();
        String actual = charOutput.toString();
        bb.reset();
        result = decoder.decode(bb, charExpected, true);
        checkCoderResult(result);
        charExpected.flip();
        String expected = charExpected.toString();
        check(actual.equals(expected),
              String.format("actual=%s expected=%s", actual, expected));
    }
}
