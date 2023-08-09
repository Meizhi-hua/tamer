public class TestUTF_32 {
    private static void testDecode(String charset,
                                   String expected,
                                   byte[] input)
        throws Exception
    {
        String out = new String(input, charset);
        if (!out.equals(expected)) {
            failureReport (out, expected);
            throw new Exception("UTF_32 Decoding test failed: " + charset);
        }
    }
    private static void testEncode(String charset,
                                   String input,
                                   byte[] expected)
        throws Exception
    {
        byte[] testBytes = input.getBytes(charset);
        for (int i = 0; i< expected.length; i++)
            if (testBytes[i] != expected[i])
                throw new Exception("UTF_32 Encoding test failed: [" + i + "]"+ charset);
    }
    private static void warn(String s) {
        System.err.println("FAILED Test UTF-32:" +
                            s) ;
    }
    private static void failureReport(String testStr,
                                      String expected) {
        System.err.println ("Expected Characters:");
        for (int i = 0; i < expected.length() ; i++) {
            warn("expected char[" + i + "] : " +
                  Integer.toHexString((int)expected.charAt(i)) +
                 "obtained char[" + i + "] : " +
                  Integer.toHexString((int)testStr.charAt(i)));
        }
    }
    private static void writeInt(OutputStream os, int i, boolean isBig)
        throws Exception
    {
        if (isBig) {
            os.write((i>>24) & 0xff);
            os.write((i>>16) & 0xff);
            os.write((i>>8) & 0xff);
            os.write(i & 0xff);
        } else {
            os.write(i & 0xff);
            os.write((i>>8) & 0xff);
            os.write((i>>16) & 0xff);
            os.write((i>>24) & 0xff);
        }
    }
    private static byte[] getBytes(boolean doBOM, boolean isBig)
        throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024*1024);
        if (doBOM)
           writeInt(baos, 0xfeff, isBig);
        for (int i = 0; i < 0xffff; i++) {
            if (i < Character.MIN_SURROGATE ||
                i > Character.MAX_SURROGATE)
            writeInt(baos, i, isBig);
        }
        for (int i = 0x10000; i < 0x1ffff; i++) {
            writeInt(baos, i, isBig);
        }
        for (int i = 0x100000; i < 0x10ffff; i++) {
            writeInt(baos, i, isBig);
        }
        byte[] bb = baos.toByteArray();
        baos.close();
        return bb;
    }
    public static void main (String[] args) throws Exception {
        byte[] bb;
        String s;
        bb = getBytes(false, true);
        s = new String(bb, "UTF-32");
        testDecode("UTF_32", s, bb);
        testEncode("UTF_32", s, bb);
        bb = getBytes(true, false);
        s = new String(bb, "UTF-32");
        bb = getBytes(false, true);
        testDecode("UTF_32", s, bb);
        testEncode("UTF_32", s, bb);
        bb = getBytes(false, true);
        s = new String(bb, "UTF-32BE");
        testDecode("UTF_32BE", s, bb);
        testEncode("UTF_32BE", s, bb);
        bb = getBytes(false, false);
        s = new String(bb, "UTF-32LE");
        testDecode("UTF_32LE", s, bb);
        testEncode("UTF_32LE", s, bb);
        bb = getBytes(true, true);
        s = new String(bb, "UTF-32BE-BOM");
        testDecode("UTF_32BE_BOM", s, bb);
        testEncode("UTF_32BE_BOM", s, bb);
        bb = getBytes(true, false);
        s = new String(bb, "UTF-32LE-BOM");
        testDecode("UTF_32LE_BOM", s, bb);
        testEncode("UTF_32LE_BOM", s, bb);
        s = "\u4e00\ufffd\u4e01";
        bb = new byte[] {
            (byte)0x00,(byte)0x00,(byte)0x4e,(byte)0x00,
            (byte)0xff,(byte)0xfe,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x4e,(byte)0x01
        };
        if (!s.equals(new String(bb, "UTF_32")) ||
            !s.equals(new String(bb, "UTF_32BE")) ||
            !s.equals(new String(bb, "UTF_32BE_BOM")))
            throw new Exception("UTF_32 Decoding test failed: ");
        bb = new byte[] {
            (byte)0xff,(byte)0xfe,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x4e,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0xfe,(byte)0xff,
            (byte)0x01,(byte)0x4e,(byte)0x00,(byte)0x00
        };
        if (!s.equals(new String(bb, "UTF_32")) ||
            !s.equals(new String(bb, "UTF_32LE")) ||
            !s.equals(new String(bb, "UTF_32LE_BOM")))
            throw new Exception("UTF_32 Decoding test failed: ");
        if (CoderResult.OVERFLOW !=
            Charset.forName("UTF_32")
            .newDecoder()
            .decode((ByteBuffer)(ByteBuffer.allocate(4)
                                 .put(new byte[]
                                      {(byte)0,(byte)1, (byte)0,(byte)01})
                                 .flip()),
                    CharBuffer.allocate(1),
                    true)) {
            throw new Exception ("Test UTF-32 Overflow test failed");
        }
        System.err.println ("OVERALL PASS OF UTF-32 Test");
    }
}
