public class StringTypes {
    private static String s = null;
    private static String fileName = "StringTypes.bin";
    public static void main(String[] args) throws Exception {
        s = new String("This is just a test!");
        byte[] asciiBytes = s.getBytes("ASCII");
        byte[] utf8Bytes = s.getBytes("UTF8");
        byte[] iso8859_1Bytes = s.getBytes("ISO-8859-1");
        byte[] unicodeBytes = s.getBytes("UnicodeBigUnmarked");
        byte[] unicodeBytes2 = getBytes(s);
        if (!equalBytes(unicodeBytes, unicodeBytes2))
          throw new Exception ("Problem with unicode encoder being used.");
        FileOutputStream fout = new FileOutputStream(fileName);
        DerOutputStream derOut = new DerOutputStream();
        System.out.println("Writing Java string out as various DER" +
                           " encoded Strings now...");
        derOut.putUTF8String(s);
        derOut.putPrintableString(s);
        derOut.putIA5String(s);
        derOut.putT61String(s);
        derOut.putBMPString(s);
        derOut.derEncode(fout);
        fout.close();
        FileInputStream fis = new FileInputStream(fileName);
        byte[] data = new byte[fis.available()];
        fis.read(data);
        DerInputStream derIn = new DerInputStream(data);
        fis.close();
        System.out.println("\nReading Strings back as DerValue's...\n");
        DerValue der;
        der = derIn.getDerValue();
        verifyDER("UTF8", der, DerValue.tag_UTF8String, utf8Bytes);
        der = derIn.getDerValue();
        verifyDER("Printable", der, DerValue.tag_PrintableString, asciiBytes);
        der = derIn.getDerValue();
        verifyDER("IA5", der, DerValue.tag_IA5String, asciiBytes);
        der = derIn.getDerValue();
        verifyDER("T61", der, DerValue.tag_T61String, iso8859_1Bytes);
        der = derIn.getDerValue();
        verifyDER("BMP", der, DerValue.tag_BMPString, unicodeBytes);
        if (derIn.available() > 0)
          throw new Exception("DerInputStream has extra data!");
        derIn.reset();
        System.out.println("Reading Strings back as Strings...\n");
        verifyString("UTF8", derIn.getUTF8String());
        verifyString("Printable", derIn.getPrintableString());
        verifyString("IA5", derIn.getIA5String());
        verifyString("T61", derIn.getT61String());
        verifyString("BMP", derIn.getBMPString());
    }
    private static byte[] getBytes(String s) {
        int len = s.length();
        byte[] retVal = new byte[len*2]; 
        for (int i = 0, j = 0; i < len; i++, j+=2) {
            retVal[j]   = (byte) (s.charAt(i)>>8);
            retVal[j+1] = (byte) (s.charAt(i));
        }
        return retVal;
    }
    private static boolean equalBytes(byte[] a, byte[] b) {
        int len1 = a.length;
        int len2 = b.length;
        if (len1 != len2)
            return false;
        for (int i = 0; i < len1 && i < len2; i++)
            if (a[i] != b[i])
                return false;
        return true;
    }
    private static void verifyDER(String type,
                                  DerValue der, byte tag,
                                  byte[] data) throws Exception {
        if (der.tag != tag)
            throw new Exception("Problem with tag for " + type);
        if (!equalBytes(der.data.toByteArray(), data))
            throw new Exception("Problem with data for " + type);
        System.out.println(type + " checks out OK");
        System.out.println("Calling toString on it: " + der.toString() + "\n");
    }
    private static void verifyString(String type, String str)
        throws Exception {
        if (!s.equals(str))
            throw new Exception("Problem with string " + type);
        System.out.println(type + "String checks out OK\n");
    }
}
