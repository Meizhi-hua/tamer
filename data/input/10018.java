public class RC2PermCheck {
    public static void main(String[] args) throws Exception {
        Provider p = Security.getProvider("SunJCE");
        System.out.println("Testing provider " + p.getName() + "...");
        if (Cipher.getMaxAllowedKeyLength("DES") == Integer.MAX_VALUE) {
            System.out.println("Skip this test due to unlimited version");
            return;
        }
        String algo = "RC2";
        Cipher c = Cipher.getInstance(algo + "/CBC/PKCS5Padding", p);
        SecretKeySpec key = new SecretKeySpec(new byte[16], "RC2");
        SecureRandom srand = new SecureRandom();
        int numOfTests = 6;
        boolean result = true;
        for (int i = 0; i < numOfTests; i++) {
            try {
                switch (i) {
                case 0:
                    c.init(Cipher.ENCRYPT_MODE, key);
                    break;
                case 1:
                    c.init(Cipher.ENCRYPT_MODE, key, srand);
                    break;
                case 2:
                    c.init(Cipher.ENCRYPT_MODE, key,
                           (AlgorithmParameters) null);
                    break;
                case 3:
                    c.init(Cipher.ENCRYPT_MODE, key,
                           (AlgorithmParameters) null, srand);
                    break;
                case 4:
                    c.init(Cipher.ENCRYPT_MODE, key,
                           (AlgorithmParameterSpec) null);
                    break;
                case 5:
                    c.init(Cipher.ENCRYPT_MODE, key,
                           (AlgorithmParameterSpec) null, srand);
                    break;
                }
            } catch (Exception ex) {
                result = false;
                System.out.println("Test#1." + i + " failed!");
                ex.printStackTrace();
                continue;
            }
        }
        RC2ParameterSpec paramSpec = new RC2ParameterSpec(128, new byte[8]);
        AlgorithmParameters param = AlgorithmParameters.getInstance(algo, p);
        param.init(paramSpec);
        numOfTests = 4;
        for (int i = 0; i < numOfTests; i++) {
            try {
                switch (i) {
                case 0:
                    c.init(Cipher.ENCRYPT_MODE, key, paramSpec);
                    break;
                case 1:
                    c.init(Cipher.ENCRYPT_MODE, key, paramSpec, srand);
                    break;
                case 2:
                    c.init(Cipher.ENCRYPT_MODE, key, param);
                    break;
                case 3:
                    c.init(Cipher.ENCRYPT_MODE, key, param, srand);
                    break;
                }
            } catch (Exception ex) {
                result = false;
                System.out.println("Test#2." + i + " failed!");
                ex.printStackTrace();
            }
        }
        paramSpec = new RC2ParameterSpec(256, new byte[8]);
        param = AlgorithmParameters.getInstance(algo);
        param.init(paramSpec);
        for (int i = 0; i < numOfTests; i++) {
            try {
                switch (i) {
                case 0:
                    c.init(Cipher.ENCRYPT_MODE, key, paramSpec);
                    result = false;
                    System.out.println("Test#3." + i + " failed!");
                    break;
                case 1:
                    c.init(Cipher.ENCRYPT_MODE, key, paramSpec, srand);
                    result = false;
                    System.out.println("Test#3." + i + " failed!");
                    break;
                case 2:
                    c.init(Cipher.ENCRYPT_MODE, key, param);
                    result = false;
                    System.out.println("Test#3." + i + " failed!");
                    break;
                case 3:
                    c.init(Cipher.ENCRYPT_MODE, key, param, srand);
                    result = false;
                    System.out.println("Test#3." + i + " failed!");
                    break;
                }
            } catch (InvalidAlgorithmParameterException iape) {
                continue;
            }
        }
        if (result) {
            System.out.println("All tests passed!");
        } else {
            throw new Exception("One or more test failed!");
        }
    }
}
