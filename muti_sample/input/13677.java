public class TestKeyFactory extends PKCS11Test {
    private static void testKey(Key key1, Key key2) throws Exception {
        if (key2.getAlgorithm().equals("EC") == false) {
            throw new Exception("Algorithm not EC");
        }
        if (key1 instanceof PublicKey) {
            if (key2.getFormat().equals("X.509") == false) {
                throw new Exception("Format not X.509");
            }
        } else if (key1 instanceof PrivateKey) {
            if (key2.getFormat().equals("PKCS#8") == false) {
                throw new Exception("Format not PKCS#8");
            }
        }
        if (key1.equals(key2) == false) {
            System.out.println("key1: " + key1);
            System.out.println("key2: " + key2);
            System.out.println("enc1: " + toString(key1.getEncoded()));
            System.out.println("enc2: " + toString(key2.getEncoded()));
            throw new Exception("Keys not equal");
        }
        if (Arrays.equals(key1.getEncoded(), key2.getEncoded()) == false) {
            throw new Exception("Encodings not equal");
        }
    }
    private static void testPublic(KeyFactory kf, PublicKey key) throws Exception {
        System.out.println("Testing public key...");
        PublicKey key2 = (PublicKey)kf.translateKey(key);
        KeySpec keySpec = kf.getKeySpec(key, ECPublicKeySpec.class);
        PublicKey key3 = kf.generatePublic(keySpec);
        KeySpec x509Spec = kf.getKeySpec(key, X509EncodedKeySpec.class);
        PublicKey key4 = kf.generatePublic(x509Spec);
        KeySpec x509Spec2 = new X509EncodedKeySpec(key.getEncoded());
        PublicKey key5 = kf.generatePublic(x509Spec2);
        testKey(key, key);
        testKey(key, key2);
        testKey(key, key3);
        testKey(key, key4);
        testKey(key, key5);
    }
    private static void testPrivate(KeyFactory kf, PrivateKey key) throws Exception {
        System.out.println("Testing private key...");
        PrivateKey key2 = (PrivateKey)kf.translateKey(key);
        KeySpec keySpec = kf.getKeySpec(key, ECPrivateKeySpec.class);
        PrivateKey key3 = kf.generatePrivate(keySpec);
        KeySpec pkcs8Spec = kf.getKeySpec(key, PKCS8EncodedKeySpec.class);
        PrivateKey key4 = kf.generatePrivate(pkcs8Spec);
        KeySpec pkcs8Spec2 = new PKCS8EncodedKeySpec(key.getEncoded());
        PrivateKey key5 = kf.generatePrivate(pkcs8Spec2);
        testKey(key, key);
        testKey(key, key2);
        testKey(key, key3);
        testKey(key, key4);
        testKey(key, key5);
    }
    private static void test(KeyFactory kf, Key key) throws Exception {
        if (key.getAlgorithm().equals("EC") == false) {
            throw new Exception("Not an EC key");
        }
        if (key instanceof PublicKey) {
            testPublic(kf, (PublicKey)key);
        } else if (key instanceof PrivateKey) {
            testPrivate(kf, (PrivateKey)key);
        }
    }
    public static void main(String[] args) throws Exception {
        main(new TestKeyFactory());
    }
    public void main(Provider p) throws Exception {
        if (p.getService("KeyFactory", "EC") == null) {
            System.out.println("Provider does not support EC, skipping");
            return;
        }
        int[] keyLengths = {192, 163, 521, 409};
        KeyFactory kf = KeyFactory.getInstance("EC", p);
        for (int len : keyLengths) {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", p);
            kpg.initialize(len);
            KeyPair kp = kpg.generateKeyPair();
            test(kf, kp.getPrivate());
            test(kf, kp.getPublic());
        }
    }
}
