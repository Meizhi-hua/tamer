public class TestISO2022CNDecoder
{
    private static String encodingName = "ISO2022CN";
    private static boolean decodeTest (
        byte encoded[],
        char decoded[],
        String label)
    {
        boolean retval = true;
        int i = 0;
        try {
            ByteArrayInputStream in;
            InputStreamReader reader;
            in = new ByteArrayInputStream(encoded);
            reader = new InputStreamReader(in, encodingName);
            for (i = 0; i < decoded.length; i++) {
                int c = reader.read();
                if (c != decoded[i]) {
                    System.err.print(label + ": read failed, char " + i);
                    System.err.print(" ... expected 0x"
                            + Integer.toHexString(decoded[i]));
                    if (c == -1)
                        System.err.println(", got EOF");
                    else
                        System.err.println(", got 0x"
                            + Integer.toHexString(c));
                    retval = false;
                    if (c == -1)
                        return retval;
                }
            }
            int testChar;
            if ((testChar = reader.read()) != -1) {
                System.err.println(label + ": read failed, no EOF");
                System.err.println("testChar is " +
                        Integer.toHexString((int)testChar));
                return false;
            }
            String decodedString = new String(encoded, "ISO2022CN");
            for (i = 0; i < decodedString.length(); i++) {
                if (decodedString.charAt(i) != decoded[i])
                    System.err.println(label + ": read failed, char " + i);
            }
            CharsetDecoder dec = Charset.forName("ISO2022CN")
                .newDecoder()
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .onMalformedInput(CodingErrorAction.REPLACE);
            ByteBuffer bb = ByteBuffer.allocateDirect(encoded.length).put(encoded);
            bb.flip();
            CharBuffer cb = ByteBuffer.allocateDirect(2*encoded.length*(int)dec.maxCharsPerByte())
                                      .asCharBuffer();
            if (bb.hasArray() || cb.hasArray()) {
                System.err.println(label + ": directBuffer failed, ");
                return false;
            }
            if (!dec.decode(bb, cb, true).isUnderflow()) {
                System.err.println(label + ": decoder's decode() failed!");
                return false;
            }
            cb.flip();
            for (i = 0; i < cb.limit(); i++) {
                if (cb.get() != decoded[i])
                    System.err.println(label + ": decoder failed, char " + i);
            }
        } catch (Exception e) {
            System.err.println(label + ": failed "
                + "(i = " + i + "), "
                + e.getClass().getName()
                + ", " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return retval;
    }
    private static boolean equal(CoderResult a, CoderResult b) {
        return (a == CoderResult.OVERFLOW && b == CoderResult.OVERFLOW) ||
            (a == CoderResult.UNDERFLOW && b == CoderResult.UNDERFLOW) ||
            ((a.isError() == b.isError()) &&
             (a.isMalformed() == b.isMalformed()) &&
             (a.isUnmappable() == b.isUnmappable()) &&
             (a.length() == b.length()));
    }
    private static boolean decodeResultTest (byte encoded[],
                                             CoderResult expected,
                                             String label) {
        CharsetDecoder dec = Charset.forName("ISO2022CN").newDecoder();
        ByteBuffer bb = ByteBuffer.wrap(encoded);
        CharBuffer cb = CharBuffer.allocate(encoded.length*(int)dec.maxCharsPerByte());
        CoderResult result = dec.decode(bb, cb, true);
        if (!equal(result, expected)) {
            System.err.println(label + ": decoder's decode() failed!");
            return false;
        }
        bb = ByteBuffer.allocateDirect(encoded.length).put(encoded);
        bb.flip();
        cb = ByteBuffer.allocateDirect(2*encoded.length*(int)dec.maxCharsPerByte())
            .asCharBuffer();
        if (bb.hasArray() || cb.hasArray()) {
            System.err.println(label + ": directBuffer failed, ");
            return false;
        }
        result = dec.reset().decode(bb, cb, true);
        if (!equal(result, expected)) {
            System.err.println(label + ": decoder's decode() - direct failed!");
            return false;
        }
        return true;
    }
    private static boolean negative (byte encoded [], String label)
    {
        try {
            ByteArrayInputStream in;
            InputStreamReader reader;
            int c;
            in = new ByteArrayInputStream(encoded);
            reader = new InputStreamReader(in, encodingName);
            c = reader.read();
            System.err.print (label + ": read failed, ");
            if (c == -1)
                System.err.println("reported EOF");
            else
                System.err.println("returned char 0x"
                    + Integer.toHexString(c)
                    + ", expected exception");
            return false;
        } catch (CharConversionException e) {
            return true;
        } catch (Throwable t) {
            System.err.println(label + ": failed, threw "
                + t.getClass().getName()
                + ", " + t.getMessage());
        }
        return false;
    }
    private static boolean decodeTest6392664 () {
        try {
            CharsetDecoder dec = Charset.forName("ISO-2022-CN-GB").newDecoder();
            dec.decode(ByteBuffer.wrap(new byte[] {(byte)0x0e, (byte)0x42, (byte)0x43 }));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private static byte test0_bytes[] = {
        (byte)0x00,
        (byte)0x01, (byte)0x02, (byte)0x03,
        (byte)0x0E, (byte)0x21, (byte)0x2f,
        (byte)0x0E, (byte)0xDD, (byte)0x9f
    };
    private static char test0_chars[] = {
        0x0000,
        0x0001, 0x0002, 0x0003,
        0x2019,
        0xFFFD
    };
    private static byte test1_bytes[] = {
        (byte)0x1b, (byte)0x24, (byte)0x29, (byte)0x41, (byte)0x21,
        (byte)0x2f };
    private static char test1_chars[] = {
        0x21, 0x2f
    };
    private static byte test2_bytes[] = {
        (byte)0x0e,
        (byte)0x1b, (byte)0x24, (byte)0x29, (byte)0x41,
        (byte)0x21, (byte)0x2f };
    private static char test2_chars[] = {
        0x2019
    };
    private static byte test3_bytes[] = {
        (byte)0x1b, (byte)0x24, (byte)0x29, (byte)0x41,
        (byte)0x0e,
        (byte)0x21, (byte)0x2f };
    private static byte test3a_bytes[] = {
        (byte)0x1b, (byte)0x24, (byte)0x41,
        (byte)0x0e,
        (byte)0x21, (byte)0x2f };
    private static char test3_chars[] = {
        0x2019
    };
    private static byte test4_bytes[] = {
        (byte)0x1b, (byte)0x24, (byte)0x29, (byte)0x41,
        (byte)0x0f,
        (byte)0x21, (byte)0x2f };
    private static char test4_chars[] = {
        0x21, 0x2f
    };
    private static byte test5_bytes[] = {
        (byte)0x1b, (byte)0x24, (byte)0x29, (byte)0x41,
        (byte)0x0e, (byte)0x21, (byte)0x2e,
        (byte)0x0f, (byte)0x21, (byte)0x2f };
    private static char test5_chars[] = {
        0x2018, 0x21, 0x2f
    };
    private static byte test6_bytes[] = {
        (byte)0x1b, (byte)0x24, (byte)0x29, (byte)0x41,
        (byte)0x0e, (byte)0x21, (byte)0x2e,
        (byte)0x21, (byte)0x2f };
    private static char test6_chars[] = {
        0x2018, 0x2019
    };
    private static byte test7_bytes[] = {
        (byte)0x1b, (byte)0x24, (byte)0x29, (byte)'G',
        (byte)0x0e, (byte)0x21, (byte)0x2e,
        (byte)0x21, (byte)0x2f };
    private static char test7_chars[] = {
        0xFE50, 0xFE51
    };
    private static byte test8_bytes[] = {
        (byte)0x1b, (byte)0x24, (byte)0x29, (byte)'G',
        (byte)0x0e, (byte)0x21, (byte)0x2e,
        (byte)0x0f, (byte)0x21, (byte)0x2f };
    private static char test8_chars[] = {
        0xFE50, 0x21, 0x2f
    };
    private static byte test9_bytes[] = {
        (byte)0x1b, (byte)0x24, (byte)0x2a, (byte)'H',
        (byte)0x1b, (byte)0x4e,
        (byte)0x21, (byte)0x2f };
    private static char test9_chars[] = {
        0x4e0e
    };
    private static byte test10_bytes[] = {
        (byte)0x1b, (byte)0x24, (byte)'+', (byte)'I',
        (byte)0x1b, (byte)0x4f,
        (byte)0x21, (byte)0x2f };
    private static char test10_chars[] = {
        0x51e2
    };
    private static byte test11_bytes[] = {
        (byte)0x1b, (byte)0x24, (byte)0x29, (byte)0x41, 
        (byte)0x0e,                                     
        (byte)0x21, (byte)0x2e,                         
        (byte)0x1b, (byte)0x24, (byte)0x2a, (byte)'H',  
        (byte)0x1b, (byte)0x4e,                         
        (byte)0x21, (byte)0x2f,                         
        (byte)0x21, (byte)0x2f                          
    };
    private static char test11_chars[] = {
        0x2018,
        0x4e0e,
        0x2019
    };
    private static byte test12_bytes[] = {
        (byte)0x1b, (byte)0x24, (byte)0x29, (byte)0x41, 
        (byte)0x0e,                                     
        (byte)0x21, (byte)0x2e,                         
        (byte)0x1b, (byte)0x24, (byte)'+', (byte)'I',  
        (byte)0x1b, (byte)0x4f,                         
        (byte)0x21, (byte)0x2f,                         
        (byte)0x21, (byte)0x2f                          
    };
    private static char test12_chars[] = {
        0x2018,
        0x51e2,
        0x2019
    };
    private static byte test13_bytes[] = {
        (byte)0x0f0,   
    };
    private static char test13_chars[] = {
        0x00f0,
    };
    private static byte test14_bytes[] = {
        (byte)0x0E, (byte)0x21, (byte)0x2f,
        (byte)0x0E, (byte)0xDD, (byte)0x9f
    };
    private static CoderResult test14_result = CoderResult.unmappableForLength(2);
    private static byte test15_bytes[] = {
        (byte)0x1b, (byte)0x4f,      
        (byte)0x20, (byte)0x2f,      
    };
    private static  CoderResult test15_result = CoderResult.unmappableForLength(4);
    private static boolean encodeTest6730652 () throws Exception {
        String strCNSP3 = "\u4e28\u4e36\u4e3f\u4e85\u4e05\u4e04\u5369\u53b6\u4e2a\u4e87\u4e49\u51e2\u56b8\u56b9\u56c4\u8053\u92b0";
        return strCNSP3.equals(new String(strCNSP3.getBytes("x-ISO-2022-CN-CNS"), "x-ISO-2022-CN-CNS"));
    }
    public static void main (String argv []) throws Exception
    {
        boolean pass = true;
        System.out.println ("");
        System.out.println ("------ checking ISO2022CN decoder -----");
        pass &= decodeTest6392664();
        try {
            new InputStreamReader (System.in, "ISO2022CN");
        } catch (Exception e) {
            encodingName = "ISO2022CN";
            System.out.println ("... requires nonstandard encoding name "
                    + encodingName);
            pass &= false;
        }
        pass &= decodeTest(test0_bytes, test0_chars, "first batch");
        pass &= decodeTest(test1_bytes, test1_chars, "escapes1");
        pass &= decodeTest(test2_bytes, test2_chars, "escapes2");
        pass &= decodeTest(test3_bytes, test3_chars, "escapes3");
        pass &= decodeTest(test3a_bytes, test3_chars, "escapes3a");
        pass &= decodeTest(test4_bytes, test4_chars, "escapes4");
        pass &= decodeTest(test5_bytes, test5_chars, "escapes5");
        pass &= decodeTest(test6_bytes, test6_chars, "escapes6");
        pass &= decodeTest(test7_bytes, test7_chars, "escapes7");
        pass &= decodeTest(test8_bytes, test8_chars, "escapes8");
        pass &= decodeTest(test9_bytes, test9_chars, "escapes9");
        pass &= decodeTest(test10_bytes, test10_chars, "escapes10");
        pass &= decodeTest(test11_bytes, test11_chars, "escapes11");
        pass &= decodeTest(test12_bytes, test12_chars, "escapes12");
        pass &= decodeTest(test13_bytes, test13_chars, "escapes13");
        pass &= decodeResultTest(test14_bytes, test14_result, "escapes14");
        pass &= decodeResultTest(test15_bytes, test15_result, "escapes15");
        pass &= encodeTest6730652 ();
        if (! pass) {
            throw new Exception("FAIL -- incorrect ISO-2022-CN");
        }
    }
}
