public class SignUsingNONEwithRSA {
    private static final List<byte[]> precomputedHashes = Arrays.asList(
        new byte[] {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10,
            0x11, 0x12, 0x13, 0x14, 0x15, 0x16
        },
        new byte[] {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10,
            0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x20
        },
        new byte[] {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10,
            0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x20,
            0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x30,
            0x31, 0x32, 0x33, 0x34, 0x35, 0x36
        },
        new byte[] {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10,
            0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x20,
            0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x30,
            0x31, 0x32
        },
        new byte[] {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10,
            0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x20,
            0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x30,
            0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40,
            0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48
        },
        new byte[] {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10,
            0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x20,
            0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x30,
            0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40,
            0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x50,
            0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x60,
            0x61, 0x62, 0x63, 0x64
        });
    private static List<byte[]> generatedSignatures = new ArrayList<>();
    public static void main(String[] args) throws Exception {
        Provider[] providers = Security.getProviders("Signature.NONEwithRSA");
        if (providers == null) {
            System.out.println("No JCE providers support the " +
                "'Signature.NONEwithRSA' algorithm");
            System.out.println("Skipping this test...");
            return;
        } else {
            System.out.println("The following JCE providers support the " +
                "'Signature.NONEwithRSA' algorithm: ");
            for (Provider provider : providers) {
                System.out.println("    " + provider.getName());
            }
        }
        System.out.println("-------------------------------------------------");
        KeyPair keys = getKeysFromKeyStore();
        signAllUsing("SunMSCAPI", keys.getPrivate());
        System.out.println("-------------------------------------------------");
        verifyAllUsing("SunMSCAPI", keys.getPublic());
        System.out.println("-------------------------------------------------");
        verifyAllUsing("SunJCE", keys.getPublic());
        System.out.println("-------------------------------------------------");
        keys = generateKeys();
        signAllUsing("SunJCE", keys.getPrivate());
        System.out.println("-------------------------------------------------");
        verifyAllUsing("SunMSCAPI", keys.getPublic());
        System.out.println("-------------------------------------------------");
    }
    private static KeyPair getKeysFromKeyStore() throws Exception {
        KeyStore ks = KeyStore.getInstance("Windows-MY", "SunMSCAPI");
        ks.load(null, null);
        System.out.println("Loaded keystore: Windows-MY");
        Enumeration e = ks.aliases();
        PrivateKey privateKey = null;
        PublicKey publicKey = null;
        while (e.hasMoreElements()) {
            String alias = (String) e.nextElement();
            if (alias.equals("6578658")) {
                System.out.println("Loaded entry: " + alias);
                privateKey = (PrivateKey) ks.getKey(alias, null);
                publicKey = (PublicKey) ks.getCertificate(alias).getPublicKey();
            }
        }
        if (privateKey == null || publicKey == null) {
            throw new Exception("Cannot load the keys need to run this test");
        }
        return new KeyPair(publicKey, privateKey);
    }
    private static KeyPair generateKeys() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024, null);
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();
        if (privateKey == null || publicKey == null) {
            throw new Exception("Cannot load the keys need to run this test");
        }
        return new KeyPair(publicKey, privateKey);
    }
    private static void signAllUsing(String providerName, PrivateKey privateKey)
            throws Exception {
        Signature sig1 = Signature.getInstance("NONEwithRSA", providerName);
        if (sig1 == null) {
            throw new Exception("'NONEwithRSA' is not supported");
        }
        if (sig1.getProvider() != null) {
            System.out.println("Using NONEwithRSA signer from the " +
                sig1.getProvider().getName() + " JCE provider");
        } else {
            System.out.println(
                "Using NONEwithRSA signer from the internal JCE provider");
        }
        System.out.println("Using key: " + privateKey);
        generatedSignatures.clear();
        for (byte[] hash : precomputedHashes) {
            sig1.initSign(privateKey);
            sig1.update(hash);
            try {
                byte [] sigBytes = sig1.sign();
                System.out.println("\nGenerated RSA signature over a " +
                    hash.length + "-byte hash (signature length: " +
                    sigBytes.length * 8 + " bits)");
                System.out.println(String.format("0x%0" +
                    (sigBytes.length * 2) + "x",
                    new java.math.BigInteger(1, sigBytes)));
                generatedSignatures.add(sigBytes);
            } catch (SignatureException se) {
                System.out.println("Error generating RSA signature: " + se);
            }
        }
    }
    private static void verifyAllUsing(String providerName, PublicKey publicKey)
            throws Exception {
        Signature sig1 = Signature.getInstance("NONEwithRSA", providerName);
        if (sig1.getProvider() != null) {
            System.out.println("\nUsing NONEwithRSA verifier from the " +
                sig1.getProvider().getName() + " JCE provider");
        } else {
            System.out.println(
                "\nUsing NONEwithRSA verifier from the internal JCE provider");
        }
        System.out.println("Using key: " + publicKey);
        int i = 0;
        for (byte[] hash : precomputedHashes) {
            byte[] sigBytes = generatedSignatures.get(i++);
            System.out.println("\nVerifying RSA Signature over a " +
                hash.length + "-byte hash (signature length: " +
                sigBytes.length * 8 + " bits)");
            System.out.println(String.format("0x%0" +
                (sigBytes.length * 2) + "x",
                new java.math.BigInteger(1, sigBytes)));
            sig1.initVerify(publicKey);
            sig1.update(hash);
            if (sig1.verify(sigBytes)) {
                System.out.println("Verify PASSED");
            } else {
                throw new Exception("Verify FAILED");
            }
        }
    }
}
