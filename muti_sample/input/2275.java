public class BlowfishTestVector {
    private static final byte[] TEST_KEY_1 = new byte[] {
        (byte)0x1c, (byte)0x58, (byte)0x7f, (byte)0x1c,
        (byte)0x13, (byte)0x92, (byte)0x4f, (byte)0xef
    };
    private static final byte[] TV_P1 = new byte[] {
        (byte)0x30, (byte)0x55, (byte)0x32, (byte)0x28,
        (byte)0x6d, (byte)0x6f, (byte)0x29, (byte)0x5a
    };
    private static final byte[] TV_C1 = new byte[] {
        (byte)0x55, (byte)0xcb, (byte)0x37, (byte)0x74,
        (byte)0xd1, (byte)0x3e, (byte)0xf2, (byte)0x01
    };
    private static final String S_TEST_KEY_2 = "Who is John Galt?";
    private static final byte[] TV_P2 = new byte[] {
        (byte)0xfe, (byte)0xdc, (byte)0xba, (byte)0x98,
        (byte)0x76, (byte)0x54, (byte)0x32, (byte)0x10
    };
    private static final byte[] TV_C2 = new byte[] {
        (byte)0xcc, (byte)0x91, (byte)0x73, (byte)0x2b,
        (byte)0x80, (byte)0x22, (byte)0xf6, (byte)0x84
    };
    public static void main(String[] argv) throws Exception {
        Provider p = new com.sun.crypto.provider.SunJCE();
        Security.addProvider(p);
        String transformation = "Blowfish/ECB/NoPadding";
        Cipher cipher = Cipher.getInstance(transformation);
        int MAX_KEY_SIZE = Cipher.getMaxAllowedKeyLength(transformation);
        if (TEST_KEY_1.length*8 <= MAX_KEY_SIZE) {
            SecretKey sKey = new SecretKeySpec(TEST_KEY_1, "Blowfish");
            try {
                cipher.init(Cipher.ENCRYPT_MODE, sKey);
                byte[] c1 = cipher.doFinal(TV_P1);
                if (!Arrays.equals(c1, TV_C1))
                    throw new Exception("Encryption (Test vector 1) failed");
                cipher.init(Cipher.DECRYPT_MODE, sKey);
                byte[] p1 = cipher.doFinal(c1);
                if (!Arrays.equals(p1, TV_P1))
                    throw new Exception("Decryption (Test vector 1) failed");
            } catch (SecurityException se) {
                TestUtil.handleSE(se);
            }
        }
        byte[] testKey2 = S_TEST_KEY_2.getBytes();
        if (testKey2.length*8 <= MAX_KEY_SIZE) {
            SecretKey sKey = new SecretKeySpec(testKey2, "Blowfish");
            try {
                cipher.init(Cipher.ENCRYPT_MODE, sKey);
                byte[] c2 = cipher.doFinal(TV_P2);
                if (!Arrays.equals(c2, TV_C2))
                    throw new Exception("Encryption (Test vector 2) failed");
                cipher.init(Cipher.DECRYPT_MODE, sKey);
                byte[] p2 = cipher.doFinal(c2);
                if (!Arrays.equals(p2, TV_P2))
                    throw new Exception("Decryption (Test vector 2) failed");
            } catch (SecurityException se) {
                TestUtil.handleSE(se);
            }
        }
        System.out.println("Test passed");
    }
    static private void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                            '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }
    static private String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
             byte2hex(block[i], buf);
             if (i < len-1) {
                 buf.append(":");
             }
        }
        return buf.toString();
    }
}
