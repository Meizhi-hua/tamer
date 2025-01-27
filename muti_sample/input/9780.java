public class TruncateHMAC {
    private final static String DIR = System.getProperty("test.src", ".");
    private static DocumentBuilderFactory dbf = null;
    private static boolean atLeastOneFailed = false;
    public static void main(String[] args) throws Exception {
        Init.init();
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        validate("signature-enveloping-hmac-sha1-trunclen-0-attack.xml", false);
        validate("signature-enveloping-hmac-sha1-trunclen-8-attack.xml", false);
        validate("signature-enveloping-hmac-sha1.xml", true);
        generate_hmac_sha1_40();
        if (atLeastOneFailed) {
            throw new Exception
                ("At least one signature did not validate as expected");
        }
    }
    private static void validate(String data, boolean pass) throws Exception {
        System.out.println("Validating " + data);
        File file = new File(DIR, data);
        Document doc = dbf.newDocumentBuilder().parse(file);
        NodeList nl =
            doc.getElementsByTagNameNS(Constants.SignatureSpecNS, "Signature");
        if (nl.getLength() == 0) {
            throw new Exception("Couldn't find signature Element");
        }
        Element sigElement = (Element) nl.item(0);
        XMLSignature signature = new XMLSignature
            (sigElement, file.toURI().toString());
        SecretKey sk = signature.createSecretKey("secret".getBytes("ASCII"));
        try {
            System.out.println
                ("Validation status: " + signature.checkSignatureValue(sk));
            if (!pass) {
                System.out.println("FAILED");
                atLeastOneFailed = true;
            } else {
                System.out.println("PASSED");
            }
        } catch (XMLSignatureException xse) {
            System.out.println(xse.getMessage());
            if (!pass) {
                System.out.println("PASSED");
            } else {
                System.out.println("FAILED");
            }
        }
    }
    private static void generate_hmac_sha1_40() throws Exception {
        System.out.println("Generating ");
        Document doc = dbf.newDocumentBuilder().newDocument();
        XMLSignature sig = new XMLSignature
            (doc, null, XMLSignature.ALGO_ID_MAC_HMAC_SHA1, 40,
             Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
        try {
            sig.sign(getSecretKey("secret".getBytes("ASCII")));
            System.out.println("FAILED");
            atLeastOneFailed = true;
        } catch (XMLSignatureException xse) {
            System.out.println(xse.getMessage());
            System.out.println("PASSED");
        }
    }
    private static SecretKey getSecretKey(final byte[] secret) {
        return new SecretKey() {
            public String getFormat()   { return "RAW"; }
            public byte[] getEncoded()  { return secret; }
            public String getAlgorithm(){ return "SECRET"; }
        };
    }
}
