public class KeyToolTest {
    String out;
    String err;
    String ex;
    String lastInput = "", lastCommand = "";
    private static final boolean debug =
        System.getProperty("debug") != null;
    static final String NSS_P11_ARG =
            "-keystore NONE -storetype PKCS11 -providerName SunPKCS11-nss -providerClass sun.security.pkcs11.SunPKCS11 -providerArg p11-nss.txt ";
    static final String NSS_SRC_P11_ARG =
            "-srckeystore NONE -srcstoretype PKCS11 -srcproviderName SunPKCS11-nss -providerClass sun.security.pkcs11.SunPKCS11 -providerArg p11-nss.txt ";
    static final String NZZ_P11_ARG =
            "-keystore NONE -storetype PKCS11 -providerName SunPKCS11-nzz -providerClass sun.security.pkcs11.SunPKCS11 -providerArg p11-nzz.txt ";
    static final String NZZ_SRC_P11_ARG =
            "-srckeystore NONE -srcstoretype PKCS11 -srcproviderName SunPKCS11-nzz -providerClass sun.security.pkcs11.SunPKCS11 -providerArg p11-nzz.txt ";
    static final String SUN_P11_ARG = "-keystore NONE -storetype PKCS11 ";
    static final String SUN_SRC_P11_ARG = "-srckeystore NONE -srcstoretype PKCS11 ";
    String p11Arg, srcP11Arg;
    KeyToolTest() {
        Locale.setDefault(Locale.US);
    }
    void remove(String filename) {
        if (debug) {
            System.err.println("Removing " + filename);
        }
        new File(filename).delete();
        if (new File(filename).exists()) {
            throw new RuntimeException("Error deleting " + filename);
        }
    }
    void test(String input, String cmd) throws Exception {
        lastInput = input;
        lastCommand = cmd;
        HumanInputStream in = new HumanInputStream(input+"X");
        test(in, cmd);
        if(in.read() != 'X' || in.read() != -1)
            throw new Exception("Input not consumed exactly");
    }
    void test(InputStream in, String cmd) throws Exception {
        if (debug) {
            System.err.println(cmd);
        } else {
            System.err.print(".");
        }
        PrintStream p1 = System.out;
        PrintStream p2 = System.err;
        InputStream i1 = System.in;
        ByteArrayOutputStream b1 = new ByteArrayOutputStream();
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        try {
            System.setIn(in);
            System.setOut(new PrintStream(b1));
            System.setErr(new PrintStream(b2));
            KeyTool.main(("-debug "+cmd).split("\\s+"));
        } finally {
            out = b1.toString();
            err = b2.toString();
            ex = out;   
            System.setIn(i1);
            System.setOut(p1);
            System.setErr(p2);
        }
    }
    void testOK(String input, String cmd) throws Exception {
        try {
            test(input, cmd);
        } catch(Exception e) {
            afterFail(input, cmd, "OK");
            throw e;
        }
    }
    void testFail(String input, String cmd) throws Exception {
        boolean ok;
        try {
            test(input, cmd);
            ok = true;
        } catch(Exception e) {
            if (e instanceof MissingResourceException) {
                ok = true;
            } else {
                ok = false;
            }
        }
        if(ok) {
            afterFail(input, cmd, "FAIL");
            throw new RuntimeException();
        }
    }
    void testOK(InputStream is, String cmd) throws Exception {
        try {
            test(is, cmd);
        } catch(Exception e) {
            afterFail("", cmd, "OK");
            throw e;
        }
    }
    void testFail(InputStream is, String cmd) throws Exception {
        boolean ok;
        try {
            test(is, cmd);
            ok = true;
        } catch(Exception e) {
            ok = false;
        }
        if(ok) {
            afterFail("", cmd, "FAIL");
            throw new RuntimeException();
        }
    }
    void testAnyway(String input, String cmd) {
        try {
            test(input, cmd);
        } catch(Exception e) {
            ;
        }
    }
    void afterFail(String input, String cmd, String should) {
        System.err.println("\nTest fails for the command ---\n" +
                "keytool " + cmd + "\nOr its debug version ---\n" +
                "keytool -debug " + cmd);
        System.err.println("The command result should be " + should +
                ", but it's not. Try run the command manually and type" +
                " these input into it: ");
        char[] inputChars = input.toCharArray();
        for (int i=0; i<inputChars.length; i++) {
            char ch = inputChars[i];
            if (ch == '\n') System.err.print("ENTER ");
            else if (ch == ' ') System.err.print("SPACE ");
            else System.err.print(ch + " ");
        }
        System.err.println("");
        System.err.println("ERR is:\n"+err);
        System.err.println("OUT is:\n"+out);
    }
    void assertTrue(boolean bool, String msg) {
        if (debug) {
            System.err.println("If not " + bool + ", " + msg);
        } else {
            System.err.print("v");
        }
        if(!bool) {
            afterFail(lastInput, lastCommand, "TRUE");
                System.err.println(msg);
            throw new RuntimeException(msg);
        }
    }
    void assertTrue(boolean bool) {
        assertTrue(bool, "well...");
    }
    KeyStore loadStore(String file, String pass, String type) throws Exception {
        KeyStore ks = KeyStore.getInstance(type);
        FileInputStream is = null;
        if (file != null && !file.equals("NONE")) {
            is = new FileInputStream(file);
        }
        ks.load(is, pass.toCharArray());
        is.close();
        return ks;
    }
    void testAll() throws Exception {
        KeyStore ks;
        remove("x.jks");
        remove("x.jceks");
        remove("x.p12");
        remove("x2.jceks");
        remove("x2.jks");
        remove("x.jks.p1.cert");
        remove("x.jks");
        remove("x.jks.p1.cert");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -alias p1 -dname CN=olala");
        testOK("", "-keystore x.jks -storepass changeit -exportcert -alias p1 -file x.jks.p1.cert");
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.getKey("p1", "changeit".toCharArray()) != null,
            "key not DSA");
        assertTrue(new File("x.jks.p1.cert").exists(), "p1 export err");
        testOK("", "-keystore x.jks -storepass changeit -delete -alias p1");
        testOK("y\n", "-keystore x.jks -storepass changeit -importcert -alias c1 -file x.jks.p1.cert");  
        testOK("", "-keystore x.jks -storepass changeit -importcert -alias c2 -file x.jks.p1.cert -noprompt"); 
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.getCertificate("c1") != null, "import c1 err");
        byte[] encoded = ks.getCertificate("c1").getEncoded();
        X509CertImpl certImpl = new X509CertImpl(encoded);
        assertTrue(certImpl.getVersion() == 3, "Version is not 3");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -alias p1 -dname CN=olala");
        testOK("changeit\n", "-keystore x.jks -changealias -alias p1 -destalias p11");
        testOK("changeit\n", "-keystore x.jks -changealias -alias c1 -destalias c11");
        testOK("changeit\n\n", "-keystore x.jks -keyclone -alias p11 -destalias p111"); 
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(!ks.containsAlias("p1"), "there is no p1");
        assertTrue(!ks.containsAlias("c1"), "there is no c1");
        assertTrue(ks.containsAlias("p11"), "there is p11");
        assertTrue(ks.containsAlias("c11"), "there is c11");
        assertTrue(ks.containsAlias("p111"), "there is p111");
        remove("x.jceks");
        testOK("changeit\nchangeit\n\n", "-keystore x.jceks -storetype JCEKS -genseckey -alias s1"); 
        testFail("changeit\n\n", "-keystore x.jceks -storetype JCEKS -genseckey -alias s11 -keysize 128"); 
        testOK("changeit\n\n", "-keystore x.jceks -storetype JCEKS -genseckey -keyalg DESede -alias s2"); 
        testFail("changeit\n\n", "-keystore x.jceks -storetype AES -genseckey -keyalg Rijndael -alias s3"); 
        testOK("changeit\n\n", "-keystore x.jceks -storetype JCEKS -genseckey -keyalg AES -alias s3 -keysize 128");
        testOK("\n", "-keystore x.jceks -storetype JCEKS -storepass changeit -genseckey -alias s4"); 
        testOK("keypass\nkeypass\n", "-keystore x.jceks -storetype JCEKS -storepass changeit -genseckey -alias s5"); 
        testOK("bad\n\bad\nkeypass\nkeypass\n", "-keystore x.jceks -storetype JCEKS -storepass changeit -genseckey -alias s6"); 
        testFail("bad\n\bad\nbad\n", "-keystore x.jceks -storetype JCEKS -storepass changeit -genseckey -alias s7"); 
        testFail("bad\n\bad\nbad\nkeypass\n", "-keystore x.jceks -storetype JCEKS -storepass changeit -genseckey -alias s7"); 
        ks = loadStore("x.jceks", "changeit", "JCEKS");
        assertTrue(ks.getKey("s1", "changeit".toCharArray()).getAlgorithm().equalsIgnoreCase("DES"), "s1 is DES");
        assertTrue(ks.getKey("s1", "changeit".toCharArray()).getEncoded().length == 8,  "DES is 56");
        assertTrue(ks.getKey("s2", "changeit".toCharArray()).getEncoded().length == 24,  "DESede is 168");
        assertTrue(ks.getKey("s2", "changeit".toCharArray()).getAlgorithm().equalsIgnoreCase("DESede"), "s2 is DESede");
        assertTrue(ks.getKey("s3", "changeit".toCharArray()).getAlgorithm().equalsIgnoreCase("AES"), "s3 is AES");
        assertTrue(ks.getKey("s4", "changeit".toCharArray()).getAlgorithm().equalsIgnoreCase("DES"), "s4 is DES");
        assertTrue(ks.getKey("s5", "keypass".toCharArray()).getAlgorithm().equalsIgnoreCase("DES"), "s5 is DES");
        assertTrue(ks.getKey("s6", "keypass".toCharArray()).getAlgorithm().equalsIgnoreCase("DES"), "s6 is DES");
        assertTrue(!ks.containsAlias("s7"), "s7 not created");
        remove("x.jks");
        remove("x.jceks");
        testOK("changeit\nchangeit\n\n", "-keystore x.jceks -storetype JCEKS -genkeypair -alias p1 -dname CN=Olala"); 
        testOK("", "-keystore x.jceks -storetype JCEKS -storepass changeit -importcert -alias c1 -file x.jks.p1.cert -noprompt"); 
        ks = loadStore("x.jceks", "changeit", "JCEKS");
        assertTrue(ks.size() == 2, "2 entries in JCEKS");
        testFail("changeit\nchangeit\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -destalias pp");
        testFail("changeit\nchangeit\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -srckeypass changeit");
        testFail("changeit\nchangeit\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -destkeypass changeit");
        testOK("changeit\nchangeit\nchangeit\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS");
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.size() == 2, "2 entries in JKS");
        testOK("changeit\nchangeit\ny\ny\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS");
        ks = loadStore("x.jks", "changeit", "JKS");
        testOK("changeit\nchangeit\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -noprompt");
        assertTrue(err.indexOf("Warning") != -1, "noprompt will warn");
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.size() == 2, "2 entries in JKS");
        testOK("changeit\nchangeit\n\ns1\n\ns2\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS");
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.size() == 4, "4 entries in JKS");
        remove("x.jks");
        testOK("changeit\nchangeit\nchangeit\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -srcalias p1"); 
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.size() == 1, "1 entries in JKS");
        testOK("changeit\nchangeit\ny\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -srcalias p1"); 
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.size() == 1, "1 entries in JKS");
        testOK("changeit\nchangeit\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -srcalias p1 -noprompt"); 
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.size() == 1, "1 entries in JKS");
        testOK("changeit\nchangeit\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -srcalias p1 -destalias p2"); 
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.size() == 2, "2 entries in JKS");
        testOK("changeit\nchangeit\n\nnewalias\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -srcalias p1"); 
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.size() == 3, "3 entries in JKS");
        remove("x.jks");
        testOK("changeit\nkeypass\nkeypass\n", "-keystore x.jceks -storetype JCEKS -genkeypair -alias p2 -dname CN=Olala"); 
        testOK("changeit\nchangeit\nchangeit\nkeypass\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -srcalias p2"); 
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.size() == 1, "1 entries in JKS");
        testOK("changeit\nchangeit\nkeypass\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -srcalias p2 -destalias p3 -destkeypass keypass2"); 
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.size() == 2, "2 entries in JKS");
        assertTrue(ks.getKey("p2", "keypass".toCharArray()) != null, "p2 has old password");
        assertTrue(ks.getKey("p3", "keypass2".toCharArray()) != null, "p3 has new password");
        remove("x.jks");
        testOK("changeit\nchangeit\nchangeit\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -srcalias c1"); 
        testOK("changeit\n\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -srcalias c1 -destalias c2");   
        assertTrue(err.indexOf("WARNING") != -1, "But will warn");
        testOK("changeit\n\ny\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -srcalias c1 -destalias c2");   
        testOK("changeit\n\n\nc3\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -srcalias c1 -destalias c2");   
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.size() == 3, "3 entries in JKS"); 
        remove("x.jks");
        testOK("changeit\n\n", "-keystore x.jceks -storetype JCEKS -genseckey -alias s1"); 
        testOK("changeit\n\n", "-keystore x.jceks -storetype JCEKS -genseckey -alias s2"); 
        testOK("changeit\n", "-keystore x.jceks -storetype JCEKS -delete -alias p2"); 
        ks = loadStore("x.jceks", "changeit", "JCEKS");
        assertTrue(ks.size() == 4, "4 entries in JCEKS");       
        testOK("changeit\nchangeit\nchangeit\n", "-importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS -srcalias s1"); 
        assertTrue(err.indexOf("not imported") != -1, "Not imported");
        assertTrue(err.indexOf("Cannot store non-PrivateKeys") != -1, "Not imported");
        remove("x.jks");
        testOK("\n\n", "-srcstorepass changeit -deststorepass changeit -importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS"); 
        assertTrue(err.indexOf("s1 not") != -1, "s1 not");
        assertTrue(err.indexOf("s2 not") != -1, "s2 not");
        assertTrue(err.indexOf("c1 success") != -1, "c1 success");
        assertTrue(err.indexOf("p1 success") != -1, "p1 success");
        testOK("yes\n", "-srcstorepass changeit -deststorepass changeit -importkeystore -srckeystore x.jceks -srcstoretype JCEKS -destkeystore x.jks -deststoretype JKS"); 
        remove("x.jks");
        testFail("changeit\nchangeit\n", "-keystore x.jks -genkeypair -alias p1 -dname CN=olala"); 
        remove("x.jks");
        testOK("changeit\nchangeit\n\n", "-keystore x.jks -genkeypair -alias p1 -dname CN=olala"); 
        remove("x.p12");
        testOK("", "-keystore x.p12 -storetype PKCS12 -storepass changeit -genkeypair -alias p0 -dname CN=olala"); 
        testOK("changeit\n", "-keystore x.p12 -storetype PKCS12 -genkeypair -alias p1 -dname CN=olala");
        testOK("changeit\n", "-keystore x.p12 -keypass changeit -storetype PKCS12 -genkeypair -alias p3 -dname CN=olala"); 
        assertTrue(err.indexOf("Warning") == -1, "PKCS12 silent when keypass == storepass");
        testOK("changeit\n", "-keystore x.p12 -keypass another -storetype PKCS12 -genkeypair -alias p2 -dname CN=olala"); 
        assertTrue(err.indexOf("Warning") != -1, "PKCS12 warning when keypass != storepass");
        testFail("", "-keystore x.p12 -storepass changeit -storetype PKCS12 -keypasswd -new changeit -alias p3"); 
        testOK("", "-keystore x.p12 -storepass changeit -storetype PKCS12 -changealias -alias p3 -destalias p33");
        testOK("", "-keystore x.p12 -storepass changeit -storetype PKCS12 -keyclone -alias p33 -destalias p3");
        remove("x.p12");
        testOK("", "-keystore x.p12 -storetype PKCS12 -storepass changeit -genkeypair -alias p0 -dname CN=olala"); 
        testOK("", "-storepass changeit -keystore x.p12 -storetype PKCS12 -genkeypair -alias p1 -dname CN=olala");
        testOK("", "-storepass changeit -keystore x.p12 -keypass changeit -storetype PKCS12 -genkeypair -alias p3 -dname CN=olala"); 
        assertTrue(err.indexOf("Warning") == -1, "PKCS12 silent when keypass == storepass");
        testOK("", "-storepass changeit -keystore x.p12 -keypass another -storetype PKCS12 -genkeypair -alias p2 -dname CN=olala"); 
        assertTrue(err.indexOf("Warning") != -1, "PKCS12 warning when keypass != storepass");
        remove("x.jks");
        remove("x.jceks");
        remove("x.p12");
        remove("x2.jceks");
        remove("x2.jks");
        remove("x.jks.p1.cert");
    }
    void testPKCS11() throws Exception {
        KeyStore ks;
        testAnyway("", p11Arg + "-storepass test12 -delete -alias p1");
        testAnyway("", p11Arg + "-storepass test12 -delete -alias p2");
        testAnyway("", p11Arg + "-storepass test12 -delete -alias p3");
        testAnyway("", p11Arg + "-storepass test12 -delete -alias nss");
        testOK("", p11Arg + "-storepass test12 -list");
        assertTrue(out.indexOf("Your keystore contains 0 entries") != -1, "*** MAKE SURE YOU HAVE NO ENTRIES IN YOUR PKCS11 KEYSTORE BEFORE THIS TEST ***");
        testOK("", p11Arg + "-storepass test12 -genkeypair -alias p1 -dname CN=olala");
        testOK("test12\n", p11Arg + "-genkeypair -alias p2 -dname CN=olala2");
        testFail("test12\n", p11Arg + "-keypass test12 -genkeypair -alias p3 -dname CN=olala3"); 
        testFail("test12\n", p11Arg + "-keypass nonsense -genkeypair -alias p3 -dname CN=olala3"); 
        testOK("", p11Arg + "-storepass test12 -list");
        assertTrue(out.indexOf("Your keystore contains 2 entries") != -1, "2 entries in p11");
        testOK("test12\n", p11Arg + "-alias p1 -changealias -destalias p3");
        testOK("", p11Arg + "-storepass test12 -list -alias p3");
        testFail("", p11Arg + "-storepass test12 -list -alias p1");
        testOK("test12\n", p11Arg + "-alias p3 -keyclone -destalias p1");
        testFail("", p11Arg + "-storepass test12 -list -alias p3");   
        testOK("", p11Arg + "-storepass test12 -list -alias p1");
        testFail("test12\n", p11Arg + "-alias p1 -keypasswd -new another"); 
        testOK("", p11Arg + "-storepass test12 -list");
        assertTrue(out.indexOf("Your keystore contains 2 entries") != -1, "2 entries in p11");
        testOK("", p11Arg + "-storepass test12 -delete -alias p1");
        testOK("", p11Arg + "-storepass test12 -delete -alias p2");
        testOK("", p11Arg + "-storepass test12 -list");
        assertTrue(out.indexOf("Your keystore contains 0 entries") != -1, "*** MAKE SURE YOU HAVE NO ENTRIES IN YOUR PKCS11 KEYSTORE BEFORE THIS TEST ***");
    }
    void testPKCS11ImportKeyStore() throws Exception {
        KeyStore ks;
        testOK("", p11Arg + "-storepass test12 -genkeypair -alias p1 -dname CN=olala");
        testOK("test12\n", p11Arg + "-genkeypair -alias p2 -dname CN=olala2");
        remove("x.jks");
        testOK("changeit\nchangeit\ntest12\n", srcP11Arg + "-importkeystore -destkeystore x.jks -deststoretype JKS -srcalias p1");
        assertTrue(err.indexOf("not imported") != -1, "cannot import key without destkeypass");
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(!ks.containsAlias("p1"), "p1 is not imported");
        testOK("changeit\ntest12\n", srcP11Arg + "-importkeystore -destkeystore x.jks -deststoretype JKS -srcalias p1 -destkeypass changeit");
        testOK("changeit\ntest12\n", srcP11Arg + "-importkeystore -destkeystore x.jks -deststoretype JKS -srcalias p2 -destkeypass changeit");
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.containsAlias("p1"), "p1 is imported");
        assertTrue(ks.containsAlias("p2"), "p2 is imported");
        testOK("", p11Arg + "-storepass test12 -delete -alias p1");
        testOK("", p11Arg + "-storepass test12 -delete -alias p2");
        testOK("test12\nchangeit\n", p11Arg + "-importkeystore -srckeystore x.jks -srcstoretype JKS");
        testOK("", p11Arg + "-storepass test12 -list -alias p1");
        testOK("", p11Arg + "-storepass test12 -list -alias p2");
        testOK("", p11Arg + "-storepass test12 -list");
        assertTrue(out.indexOf("Your keystore contains 2 entries") != -1, "2 entries in p11");
        testOK("", p11Arg + "-storepass test12 -delete -alias p1");
        testOK("", p11Arg + "-storepass test12 -delete -alias p2");
        testOK("", p11Arg + "-storepass test12 -list");
        assertTrue(out.indexOf("Your keystore contains 0 entries") != -1, "empty p11");
        remove("x.jks");
    }
    void sqeTest() throws Exception {
        FileOutputStream fos = new FileOutputStream("badkeystore");
        for (int i=0; i<100; i++) {
            fos.write(i);
        }
        fos.close();
        sqeCsrTest();
        sqePrintcertTest();
        sqeDeleteTest();
        sqeExportTest();
        sqeGenkeyTest();
        sqeImportTest();
        sqeKeyclonetest();
        sqeKeypasswdTest();
        sqeListTest();
        sqeSelfCertTest();
        sqeStorepassTest();
        remove("badkeystore");
    }
    void sqeImportTest() throws Exception {
        KeyStore ks;
        remove("x.jks");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala");
        testOK("", "-keystore x.jks -storepass changeit -exportcert -file x.jks.p1.cert");
         testOK("", "-keystore x.jks -storepass changeit -delete -alias mykey");
        testOK("", "-keystore x.jks -storepass changeit -importcert -file x.jks.p1.cert -noprompt");
         testOK("", "-keystore x.jks -storepass changeit -delete -alias mykey");
        testOK("yes\n", "-keystore x.jks -storepass changeit -importcert -file x.jks.p1.cert");
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(ks.containsAlias("mykey"), "imported");
         testOK("", "-keystore x.jks -storepass changeit -delete -alias mykey");
        testOK("\n", "-keystore x.jks -storepass changeit -importcert -file x.jks.p1.cert");
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(!ks.containsAlias("mykey"), "imported");
        testOK("no\n", "-keystore x.jks -storepass changeit -importcert -file x.jks.p1.cert");
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(!ks.containsAlias("mykey"), "imported");
        testFail("no\n", "-keystore x.jks -storepass changeit -importcert -file nonexist");
        testFail("no\n", "-keystore x.jks -storepass changeit -importcert -file x.jks");
        remove("x.jks");
    }
    void sqeKeyclonetest() throws Exception {
        remove("x.jks");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -new newpass -keyclone -dest p0"); 
        testOK("\n", "-keystore x.jks -storepass changeit -keypass changeit -keyclone -dest p1"); 
        testOK("\n", "-keystore x.jks -storepass changeit -keyclone -dest p2");
        testFail("\n", "-keystore x.jks -storepass changeit -keyclone -dest p2");
        testFail("\n", "-keystore x.jks -storepass changeit -keyclone -dest p3 -alias noexist");
        testOK("", "-keystore x.jks -storepass changeit -exportcert -file x.jks.p1.cert");
        testOK("", "-keystore x.jks -storepass changeit -delete -alias mykey");
        testOK("", "-keystore x.jks -storepass changeit -importcert -file x.jks.p1.cert -noprompt");
        testFail("", "-keystore x.jks -storepass changeit -keypass changeit -new newpass -keyclone -dest p0"); 
        remove("x.jks");
    }
    void sqeKeypasswdTest() throws Exception {
        remove("x.jks");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -keypasswd -new newpass");
         testOK("", "-keystore x.jks -storepass changeit -keypass newpass -keypasswd -new changeit");
        testOK("newpass\nnewpass\n", "-keystore x.jks -storepass changeit -keypass changeit -keypasswd");
         testOK("", "-keystore x.jks -storepass changeit -keypass newpass -keypasswd -new changeit");
        testOK("new\nnew\nnewpass\nnewpass\n", "-keystore x.jks -storepass changeit -keypass changeit -keypasswd");
         testOK("", "-keystore x.jks -storepass changeit -keypass newpass -keypasswd -new changeit");
        testOK("", "-keystore x.jks -storepass changeit -keypasswd -new newpass");
         testOK("", "-keystore x.jks -storepass changeit -keypass newpass -keypasswd -new changeit");
        testOK("changeit\n", "-keystore x.jks -keypasswd -new newpass");
         testOK("", "-keystore x.jks -storepass changeit -keypass newpass -keypasswd -new changeit");
        testFail("", "-keystore x.jks -storepass badpass -keypass changeit -keypasswd -new newpass");
        testFail("", "-keystore x.jks -storepass changeit -keypass bad -keypasswd -new newpass");
        testOK("", "-keystore x.jks -storepass changeit -exportcert -file x.jks.p1.cert");
        testOK("", "-keystore x.jks -storepass changeit -delete -alias mykey");
        testOK("", "-keystore x.jks -storepass changeit -importcert -file x.jks.p1.cert -noprompt");
        testFail("", "-keystore x.jks -storepass changeit -keypass changeit -keypasswd -new newpass");
        testOK("", "-keystore x.jks -storepass changeit -delete -alias mykey");
        testOK("", "-keystore x.jks -storepass changeit -keypass keypass -genkeypair -dname CN=olala");
        testFail("", "-keystore x.jks -storepass changeit -keypasswd -new newpass");
        testOK("keypass\n", "-keystore x.jks -storepass changeit -keypasswd -new newpass");
        remove("x.jks");
    }
    void sqeListTest() throws Exception {
        remove("x.jks");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala");
        testOK("", "-keystore x.jks -storepass changeit -list");
        testOK("", "-keystore x.jks -storepass changeit -list -alias mykey");
        testFail("", "-keystore x.jks -storepass changeit -list -alias notexist");
        testFail("", "-keystore x.jks -storepass badpass -list -alias mykey");
        testOK("", "-keystore x.jks -storepass changeit -keypass badpass -list -alias mykey");  
        testOK("\n", "-keystore x.jks -list");
        assertTrue(err.indexOf("WARNING") != -1, "no storepass");
        testOK("changeit\n", "-keystore x.jks -list");
        assertTrue(err.indexOf("WARNING") == -1, "has storepass");
        testFail("badpass\n", "-keystore x.jks -list");
        testFail("", "-keystore aa\\bb
        testFail("", "-keystore nonexisting -storepass changeit -list");
        testFail("", "-keystore badkeystore -storepass changeit -list");
        remove("x.jks");
    }
    void sqeSelfCertTest() throws Exception {
        remove("x.jks");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala");
        testOK("", "-keystore x.jks -storepass changeit -selfcert");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -selfcert");
        testFail("", "-keystore x.jks -storepass changeit -keypass changeit -selfcert -alias nonexisting"); 
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -selfcert -dname CN=NewName");
        testFail("", "-keystore x.jks -storepass changeit -keypass changeit -selfcert -sigalg MD5withRSA"); 
        testFail("", "-keystore x.jks -storepass wrong -keypass changeit -selfcert"); 
        testFail("", "-keystore x.jks -storepass changeit -keypass wrong -selfcert"); 
        testFail("", "-keystore nonexist -storepass changeit -keypass changeit -selfcert");
        testFail("", "-keystore aa
        remove("x.jks");
        testOK("", "-keystore x.jks -storepass changeit -keypass keypass -genkeypair -dname CN=olala");
        testFail("", "-keystore x.jks -storepass changeit -selfcert");
        testOK("keypass\n", "-keystore x.jks -storepass changeit -selfcert");
        testOK("", "-keystore x.jks -storepass changeit -exportcert -file x.jks.p1.cert");
        testOK("", "-keystore x.jks -storepass changeit -delete -alias mykey");
        testOK("", "-keystore x.jks -storepass changeit -importcert -file x.jks.p1.cert -noprompt");
        testFail("", "-keystore x.jks -storepass changeit -selfcert");  
        remove("x.jks");
    }
    void sqeStorepassTest() throws Exception {
        remove("x.jks");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala");
        testOK("", "-storepasswd -keystore x.jks -storepass changeit -new newstore"); 
         testOK("", "-storepasswd -keystore x.jks -storepass newstore -new changeit");
        testOK("changeit\nnewstore\nnewstore\n", "-storepasswd -keystore x.jks"); 
         testOK("", "-storepasswd -keystore x.jks -storepass newstore -new changeit");
        testOK("changeit\n", "-storepasswd -keystore x.jks -new newstore"); 
         testOK("", "-storepasswd -keystore x.jks -storepass newstore -new changeit");
        testOK("newstore\nnewstore\n", "-storepasswd -keystore x.jks -storepass changeit"); 
         testOK("", "-storepasswd -keystore x.jks -storepass newstore -new changeit");
        testOK("new\nnew\nnewstore\nnewstore\n", "-storepasswd -keystore x.jks -storepass changeit"); 
         testOK("", "-storepasswd -keystore x.jks -storepass newstore -new changeit");
        testFail("", "-storepasswd -keystore x.jks -storepass badold -new newstore"); 
        testFail("", "-storepasswd -keystore x.jks -storepass changeit -new new"); 
        testFail("", "-storepasswd -keystore nonexist -storepass changeit -new newstore"); 
        testFail("", "-storepasswd -keystore badkeystore -storepass changeit -new newstore"); 
        testFail("", "-storepasswd -keystore aa\\bb
        remove("x.jks");
    }
    void sqeGenkeyTest() throws Exception {
        remove("x.jks");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala");
        testFail("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -alias newentry");
        testFail("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -alias newentry");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -keyalg DSA -alias n1");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -keyalg RSA -alias n2");
        testFail("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -keyalg NoSuchAlg -alias n3");
        testFail("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -keysize 56 -alias n4");
        testFail("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -keysize 999 -alias n5");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -keysize 512 -alias n6");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -keysize 1024 -alias n7");
        testFail("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -sigalg NoSuchAlg -alias n8");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -keyalg RSA -sigalg MD2withRSA -alias n9");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -keyalg RSA -sigalg MD5withRSA -alias n10");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -keyalg RSA -sigalg SHA1withRSA -alias n11");
        testFail("", "-keystore aa\\bb
        testFail("", "-keystore badkeystore -storepass changeit -keypass changeit -genkeypair -dname CN=olala -alias n14");
        testFail("", "-keystore x.jks -storepass badpass -keypass changeit -genkeypair -dname CN=olala -alias n16");
        testFail("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CNN=olala -alias n17");
        remove("x.jks");
    }
    void sqeExportTest() throws Exception {
        remove("x.jks");
        testFail("", "-keystore x.jks -storepass changeit -export -file mykey.cert -alias mykey"); 
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala");
        testOK("", "-keystore x.jks -storepass changeit -export -file mykey.cert -alias mykey");
        testOK("", "-keystore x.jks -storepass changeit -delete -alias mykey");
        testOK("", "-keystore x.jks -storepass changeit -import -file mykey.cert -noprompt -alias c1");
        testOK("", "-keystore x.jks -storepass changeit -export -file mykey.cert2 -alias c1");
        testFail("", "-keystore aa\\bb
        testFail("", "-keystore nonexistkeystore -storepass changeit -export -file mykey.cert2 -alias c1");
        testFail("", "-keystore badkeystore -storepass changeit -export -file mykey.cert2 -alias c1");
        testFail("", "-keystore x.jks -storepass badpass -export -file mykey.cert2 -alias c1");
        remove("mykey.cert");
        remove("mykey.cert2");
        remove("x.jks");
    }
    void sqeDeleteTest() throws Exception {
        remove("x.jks");
        testFail("", "-keystore x.jks -storepass changeit -delete -alias mykey"); 
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala");
        testOK("", "-keystore x.jks -storepass changeit -delete -alias mykey");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala");
        testFail("", "-keystore aa\\bb
        testFail("", "-keystore nonexistkeystore -storepass changeit -delete -alias mykey"); 
        testFail("", "-keystore badkeystore -storepass changeit -delete -alias mykey"); 
        testFail("", "-keystore x.jks -storepass xxxxxxxx -delete -alias mykey"); 
        remove("x.jks");
    }
    void sqeCsrTest() throws Exception {
        remove("x.jks");
        remove("x.jks.p1.cert");
        remove("csr1");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala");
        testOK("", "-keystore x.jks -storepass changeit -certreq -file csr1 -alias mykey");
        testOK("", "-keystore x.jks -storepass changeit -certreq -file csr1");
        testOK("", "-keystore x.jks -storepass changeit -certreq -file csr1 -sigalg SHA1withDSA");
        testFail("", "-keystore x.jks -storepass changeit -certreq -file csr1 -sigalg MD5withRSA"); 
        testFail("", "-keystore x.jks -storepass badstorepass -certreq -file csr1"); 
        testOK("changeit\n", "-keystore x.jks -certreq -file csr1"); 
        testFail("\n", "-keystore x.jks -certreq -file csr1"); 
        testFail("", "-keystore x.jks -storepass changeit -keypass badkeypass -certreq -file csr1"); 
        testFail("", "-keystore x.jks -storepass changeit -certreq -file aa\\bb
        testFail("", "-keystore noexistks -storepass changeit -certreq -file csr1"); 
        testOK("", "-keystore x.jks -storepass changeit -delete -alias mykey");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala -keyalg RSA");
        testOK("", "-keystore x.jks -storepass changeit -certreq -file csr1 -alias mykey");
        testOK("", "-keystore x.jks -storepass changeit -certreq -file csr1");
        testFail("", "-keystore x.jks -storepass changeit -certreq -file csr1 -sigalg SHA1withDSA"); 
        testOK("", "-keystore x.jks -storepass changeit -certreq -file csr1 -sigalg MD5withRSA");
        testOK("", "-keystore x.jks -storepass changeit -exportcert -file x.jks.p1.cert");
        testOK("", "-keystore x.jks -storepass changeit -delete -alias mykey");
        testOK("", "-keystore x.jks -storepass changeit -importcert -file x.jks.p1.cert -noprompt");
        testFail("", "-keystore x.jks -storepass changeit -certreq -file csr1 -alias mykey");
        testFail("", "-keystore x.jks -storepass changeit -certreq -file csr1");
        remove("x.jks");
        remove("x.jks.p1.cert");
        remove("csr1");
    }
    void sqePrintcertTest() throws Exception {
        remove("x.jks");
        remove("mykey.cert");
        testOK("", "-keystore x.jks -storepass changeit -keypass changeit -genkeypair -dname CN=olala");
        testOK("", "-keystore x.jks -storepass changeit -export -file mykey.cert -alias mykey");
        testFail("", "-printcert -file badkeystore");
        testFail("", "-printcert -file a/b/c/d");
        testOK("", "-printcert -file mykey.cert");
        FileInputStream fin = new FileInputStream("mykey.cert");
        testOK(fin, "-printcert");
        fin.close();
        remove("x.jks");
        remove("mykey.cert");
    }
    void v3extTest(String keyAlg) throws Exception {
        KeyStore ks;
        remove("x.jks");
        String simple = "-keystore x.jks -storepass changeit -keypass changeit -noprompt -keyalg " + keyAlg + " ";
        String pre = simple + "-genkeypair -dname CN=Olala -alias ";
        testOK("", pre + "o1");
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(((X509Certificate)ks.getCertificate("o1")).getVersion() == 3);
        assertTrue(((X509CertImpl)ks.getCertificate("o1")).getSubjectKeyIdentifierExtension() != null);
        testOK("", pre + "b1 -ext BC:critical");
        testOK("", pre + "b2 -ext BC");
        testOK("", pre + "b3 -ext bc");
        testOK("", pre + "b4 -ext BasicConstraints");
        testOK("", pre + "b5 -ext basicconstraints");
        testOK("", pre + "b6 -ext BC=ca:true,pathlen:12");
        testOK("", pre + "b7 -ext BC=ca:false");
        testOK("", pre + "b8 -ext BC:critical=ca:false");
        testOK("", pre + "b9 -ext BC=12");
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(((X509CertImpl)ks.getCertificate("b1")).getBasicConstraintsExtension().isCritical());
        assertTrue(!((X509CertImpl)ks.getCertificate("b2")).getBasicConstraintsExtension().isCritical());
        assertTrue(((X509CertImpl)ks.getCertificate("b8")).getBasicConstraintsExtension().isCritical());
        assertTrue(((X509Certificate)ks.getCertificate("b1")).getBasicConstraints() == Integer.MAX_VALUE);
        assertTrue(((X509Certificate)ks.getCertificate("b2")).getBasicConstraints() == Integer.MAX_VALUE);
        assertTrue(((X509Certificate)ks.getCertificate("b3")).getBasicConstraints() == Integer.MAX_VALUE);
        assertTrue(((X509Certificate)ks.getCertificate("b4")).getBasicConstraints() == Integer.MAX_VALUE);
        assertTrue(((X509Certificate)ks.getCertificate("b5")).getBasicConstraints() == Integer.MAX_VALUE);
        assertTrue(((X509Certificate)ks.getCertificate("b6")).getBasicConstraints() == 12);
        assertTrue(((X509Certificate)ks.getCertificate("b7")).getBasicConstraints() == -1);
        assertTrue(((X509Certificate)ks.getCertificate("b9")).getBasicConstraints() == 12);
        testOK("", pre + "ku1 -ext KeyUsage:critical=digitalsignature");
        testOK("", pre + "ku2 -ext KU=digitalSignature");
        testOK("", pre + "ku3 -ext KU=ds");
        testOK("", pre + "ku4 -ext KU=dig");
        testFail("", pre + "ku5 -ext KU=d");    
        testFail("", pre + "ku6 -ext KU=cs");   
        testOK("", pre + "ku11 -ext KU=nr");
        testFail("", pre + "ku12 -ext KU=ke");  
        testOK("", pre + "ku12 -ext KU=keyE");
        testFail("", pre + "ku13 -ext KU=de");  
        testOK("", pre + "ku13 -ext KU=dataE");
        testOK("", pre + "ku14 -ext KU=ka");
        testOK("", pre + "ku15 -ext KU=kcs");
        testOK("", pre + "ku16 -ext KU=crls");
        testOK("", pre + "ku17 -ext KU=eo");
        testOK("", pre + "ku18 -ext KU=do");
        testOK("", pre + "ku19 -ext KU=cc");
        testOK("", pre + "ku017 -ext KU=ds,cc,eo");
        testOK("", pre + "ku135 -ext KU=nr,dataEncipherment,keyCertSign");
        testOK("", pre + "ku246 -ext KU=keyEnc,cRL,keyA");
        testOK("", pre + "ku1234 -ext KU=ka,da,keyE,nonR");
        ks = loadStore("x.jks", "changeit", "JKS");
        class CheckKU {
            void check(KeyStore ks, String alias, int... pos) throws Exception {
                System.err.print("x");
                boolean[] bs = ((X509Certificate)ks.getCertificate(alias)).getKeyUsage();
                bs = Arrays.copyOf(bs, 9);
                for (int i=0; i<bs.length; i++) {
                    boolean found = false;
                    for (int p: pos) {
                        if (p == i) found = true;
                    }
                    if (!found ^ bs[i]) {
                    } else {
                        throw new RuntimeException("KU not match at " + i +
                                ": " + found + " vs " + bs[i]);
                    }
                }
            }
        }
        CheckKU c = new CheckKU();
        assertTrue(((X509CertImpl)ks.getCertificate("ku1")).getExtension(PKIXExtensions.KeyUsage_Id).isCritical());
        assertTrue(!((X509CertImpl)ks.getCertificate("ku2")).getExtension(PKIXExtensions.KeyUsage_Id).isCritical());
        c.check(ks, "ku1", 0);
        c.check(ks, "ku2", 0);
        c.check(ks, "ku3", 0);
        c.check(ks, "ku4", 0);
        c.check(ks, "ku11", 1);
        c.check(ks, "ku12", 2);
        c.check(ks, "ku13", 3);
        c.check(ks, "ku14", 4);
        c.check(ks, "ku15", 5);
        c.check(ks, "ku16", 6);
        c.check(ks, "ku17", 7);
        c.check(ks, "ku18", 8);
        c.check(ks, "ku19", 1);
        c.check(ks, "ku11", 1);
        c.check(ks, "ku11", 1);
        c.check(ks, "ku11", 1);
        c.check(ks, "ku017", 0, 1, 7);
        c.check(ks, "ku135", 1, 3, 5);
        c.check(ks, "ku246", 6, 2, 4);
        c.check(ks, "ku1234", 1, 2, 3, 4);
        testOK("", pre + "eku1 -ext EKU:critical=sa");
        testOK("", pre + "eku2 -ext ExtendedKeyUsage=ca");
        testOK("", pre + "eku3 -ext EKU=cs");
        testOK("", pre + "eku4 -ext EKU=ep");
        testOK("", pre + "eku8 -ext EKU=ts");
        testFail("", pre + "eku9 -ext EKU=os");
        testOK("", pre + "eku9 -ext EKU=ocsps");
        testOK("", pre + "eku10 -ext EKU=any");
        testOK("", pre + "eku11 -ext EKU=1.2.3.4,1.3.5.7,ep");
        testFail("", pre + "eku12 -ext EKU=c");
        testFail("", pre + "eku12 -ext EKU=nothing");
        ks = loadStore("x.jks", "changeit", "JKS");
        class CheckEKU {
            void check(KeyStore ks, String alias, String... pos) throws Exception {
                System.err.print("x");
                List<String> bs = ((X509Certificate)ks.getCertificate(alias)).getExtendedKeyUsage();
                int found = 0;
                for (String p: pos) {
                    if (bs.contains(p)) {
                        found++;
                    } else {
                        throw new RuntimeException("EKU: not included " + p);
                    }
                }
                if (found != bs.size()) {
                    throw new RuntimeException("EKU: more items than expected");
                }
            }
        }
        CheckEKU cx = new CheckEKU();
        assertTrue(((X509CertImpl)ks.getCertificate("eku1")).getExtension(PKIXExtensions.ExtendedKeyUsage_Id).isCritical());
        assertTrue(!((X509CertImpl)ks.getCertificate("eku2")).getExtension(PKIXExtensions.ExtendedKeyUsage_Id).isCritical());
        cx.check(ks, "eku1", "1.3.6.1.5.5.7.3.1");
        cx.check(ks, "eku2", "1.3.6.1.5.5.7.3.2");
        cx.check(ks, "eku3", "1.3.6.1.5.5.7.3.3");
        cx.check(ks, "eku4", "1.3.6.1.5.5.7.3.4");
        cx.check(ks, "eku8", "1.3.6.1.5.5.7.3.8");
        cx.check(ks, "eku9", "1.3.6.1.5.5.7.3.9");
        cx.check(ks, "eku10", "2.5.29.37.0");
        cx.check(ks, "eku11", "1.3.6.1.5.5.7.3.4", "1.2.3.4", "1.3.5.7");
        testOK("", pre+"san1 -ext san:critical=email:me@me.org");
        testOK("", pre+"san2 -ext san=uri:http:
        testOK("", pre+"san3 -ext san=dns:me.org");
        testOK("", pre+"san4 -ext san=ip:192.168.0.1");
        testOK("", pre+"san5 -ext san=oid:1.2.3.4");
        testOK("", pre+"san235 -ext san=uri:http:
        ks = loadStore("x.jks", "changeit", "JKS");
        class CheckSAN {
            void check(KeyStore ks, String alias, int type, Object... items) throws Exception {
                int pos = 0;
                System.err.print("x");
                Object[] names = null;
                if (type == 0) names = ((X509Certificate)ks.getCertificate(alias)).getSubjectAlternativeNames().toArray();
                else names = ((X509Certificate)ks.getCertificate(alias)).getIssuerAlternativeNames().toArray();
                Arrays.sort(names, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        int i1 = (Integer)((List)o1).get(0);
                        int i2 = (Integer)((List)o2).get(0);
                        return i1 - i2;
                    }
                });
                for (Object o: names) {
                    List l = (List)o;
                    for (Object o2: l) {
                        if (!items[pos++].equals(o2)) {
                            throw new RuntimeException("Not equals at " + pos
                                    + ": " + items[pos-1] + " vs " + o2);
                        }
                    }
                }
                if (pos != items.length) {
                    throw new RuntimeException("Extra items, pos is " + pos);
                }
            }
        }
        CheckSAN csan = new CheckSAN();
        assertTrue(((X509CertImpl)ks.getCertificate("san1")).getSubjectAlternativeNameExtension().isCritical());
        assertTrue(!((X509CertImpl)ks.getCertificate("san2")).getSubjectAlternativeNameExtension().isCritical());
        csan.check(ks, "san1", 0, 1, "me@me.org");
        csan.check(ks, "san2", 0, 6, "http:
        csan.check(ks, "san3", 0, 2, "me.org");
        csan.check(ks, "san4", 0, 7, "192.168.0.1");
        csan.check(ks, "san5", 0, 8, "1.2.3.4");
        csan.check(ks, "san235", 0, 2, "me.org", 6, "http:
        testOK("", pre+"ian1 -ext ian:critical=email:me@me.org");
        testOK("", pre+"ian2 -ext ian=uri:http:
        testOK("", pre+"ian3 -ext ian=dns:me.org");
        testOK("", pre+"ian4 -ext ian=ip:192.168.0.1");
        testOK("", pre+"ian5 -ext ian=oid:1.2.3.4");
        testOK("", pre+"ian235 -ext ian=uri:http:
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(((X509CertImpl)ks.getCertificate("ian1")).getIssuerAlternativeNameExtension().isCritical());
        assertTrue(!((X509CertImpl)ks.getCertificate("ian2")).getIssuerAlternativeNameExtension().isCritical());
        csan.check(ks, "ian1", 1, 1, "me@me.org");
        csan.check(ks, "ian2", 1, 6, "http:
        csan.check(ks, "ian3", 1, 2, "me.org");
        csan.check(ks, "ian4", 1, 7, "192.168.0.1");
        csan.check(ks, "ian5", 1, 8, "1.2.3.4");
        csan.check(ks, "ian235", 1, 2, "me.org", 6, "http:
        testOK("", pre+"sia1 -ext sia=care:uri:ldap:
        testOK("", pre+"sia2 -ext sia=ts:email:ts@ca.com");
        testFail("SIA never critical", pre+"sia3 -ext sia:critical=ts:email:ts@ca.com");
        ks = loadStore("x.jks", "changeit", "JKS");
        class CheckSia {
            void check(KeyStore ks, String alias, int type, Object... items) throws Exception {
                int pos = 0;
                System.err.print("x");
                AccessDescription[] ads = null;
                if (type == 0) {
                    SubjectInfoAccessExtension siae = (SubjectInfoAccessExtension)((X509CertImpl)ks.getCertificate(alias)).getExtension(PKIXExtensions.SubjectInfoAccess_Id);
                    ads = siae.getAccessDescriptions().toArray(new AccessDescription[0]);
                } else {
                    AuthorityInfoAccessExtension aiae = (AuthorityInfoAccessExtension)((X509CertImpl)ks.getCertificate(alias)).getExtension(PKIXExtensions.AuthInfoAccess_Id);
                    ads = aiae.getAccessDescriptions().toArray(new AccessDescription[0]);
                }
                Arrays.sort(ads, new Comparator<AccessDescription>() {
                    @Override
                    public int compare(AccessDescription o1, AccessDescription o2) {
                        return o1.getAccessMethod().toString().compareTo(o2.getAccessMethod().toString());
                    }
                });
                for (AccessDescription ad: ads) {
                    if (!ad.getAccessMethod().equals(items[pos++]) ||
                            !new Integer(ad.getAccessLocation().getType()).equals(items[pos++])) {
                        throw new RuntimeException("Not same type at " + pos);
                    }
                    String name = null;
                    switch (ad.getAccessLocation().getType()) {
                        case 1:
                            name = ((RFC822Name)ad.getAccessLocation().getName()).getName();
                            break;
                        case 6:
                            name = ((URIName)ad.getAccessLocation().getName()).getURI().toString();
                            break;
                        default:
                            throw new RuntimeException("Not implemented: " + ad);
                    }
                    if (!name.equals(items[pos++])) {
                        throw new Exception("Name not same for " + ad + " at pos " + pos);
                    }
                }
            }
        }
        CheckSia csia = new CheckSia();
        assertTrue(!((X509CertImpl)ks.getCertificate("sia1")).getExtension(PKIXExtensions.SubjectInfoAccess_Id).isCritical());
        csia.check(ks, "sia1", 0, AccessDescription.Ad_CAREPOSITORY_Id, 6, "ldap:
        csia.check(ks, "sia2", 0, AccessDescription.Ad_TIMESTAMPING_Id, 1, "ts@ca.com");
        testOK("", pre+"aia1 -ext aia=cai:uri:ldap:
        testOK("", pre+"aia2 -ext aia=ocsp:email:ocsp@ca.com");
        testFail("AIA never critical", pre+"aia3 -ext aia:critical=ts:email:ts@ca.com");
        ks = loadStore("x.jks", "changeit", "JKS");
        assertTrue(!((X509CertImpl)ks.getCertificate("aia1")).getExtension(PKIXExtensions.AuthInfoAccess_Id).isCritical());
        csia.check(ks, "aia1", 1, AccessDescription.Ad_CAISSUERS_Id, 6, "ldap:
        csia.check(ks, "aia2", 1, AccessDescription.Ad_OCSP_Id, 1, "ocsp@ca.com");
        testOK("", pre+"oid1 -ext 1.2.3:critical=0102");
        testOK("", pre+"oid2 -ext 1.2.3");
        testOK("", pre+"oid12 -ext 1.2.3 -ext 1.2.4=01:02:03");
        ks = loadStore("x.jks", "changeit", "JKS");
        class CheckOid {
            void check(KeyStore ks, String alias, String oid, byte[] value) throws Exception {
                int pos = 0;
                System.err.print("x");
                Extension ex = ((X509CertImpl)ks.getCertificate(alias)).getExtension(new ObjectIdentifier(oid));
                if (!Arrays.equals(value, ex.getValue())) {
                    throw new RuntimeException("Not same content in " + alias + " for " + oid);
                }
            }
        }
        CheckOid coid = new CheckOid();
        assertTrue(((X509CertImpl)ks.getCertificate("oid1")).getExtension(new ObjectIdentifier("1.2.3")).isCritical());
        assertTrue(!((X509CertImpl)ks.getCertificate("oid2")).getExtension(new ObjectIdentifier("1.2.3")).isCritical());
        coid.check(ks, "oid1", "1.2.3", new byte[]{1,2});
        coid.check(ks, "oid2", "1.2.3", new byte[]{});
        coid.check(ks, "oid12", "1.2.3", new byte[]{});
        coid.check(ks, "oid12", "1.2.4", new byte[]{1,2,3});
        testOK("", pre+"ca");
        testOK("", pre+"a");
        testOK("", simple+"-alias a -certreq " +
                "-ext BC=1 -ext KU=crl " +
                "-ext 1.2.3=01 -ext 1.2.4:critical=0102 -ext 1.2.5=010203 " +
                "-rfc -file test.req");
        testOK("", "-printcertreq -file test.req");
        testOK("", simple+"-gencert -alias ca -infile test.req -ext " +
                "honored=all,-KU,1.2.3:critical,1.2.4:non-critical " +
                "-ext BC=2 -ext 2.3.4=01020304 " +
                "-debug -rfc -outfile test.cert");
        testOK("", simple+"-importcert -file test.cert -alias a");
        ks = loadStore("x.jks", "changeit", "JKS");
        X509CertImpl a = (X509CertImpl)ks.getCertificate("a");
        assertTrue(a.getAuthorityKeyIdentifierExtension() != null);
        assertTrue(a.getSubjectKeyIdentifierExtension() != null);
        assertTrue(a.getKeyUsage() == null);
        assertTrue(a.getExtension(new ObjectIdentifier("1.2.3")).isCritical());
        assertTrue(!a.getExtension(new ObjectIdentifier("1.2.4")).isCritical());
        assertTrue(!a.getExtension(new ObjectIdentifier("1.2.5")).isCritical());
        assertTrue(a.getExtensionValue("1.2.3").length == 3);
        assertTrue(a.getExtensionValue("1.2.4").length == 4);
        assertTrue(a.getExtensionValue("1.2.5").length == 5);
        assertTrue(a.getBasicConstraints() == 2);
        assertTrue(!a.getExtension(new ObjectIdentifier("2.3.4")).isCritical());
        assertTrue(a.getExtensionValue("2.3.4").length == 6);
        remove("x.jks");
        remove("test.req");
        remove("test.cert");
    }
    void i18nTest() throws Exception {
        remove("x.jks");
        testOK("", "-help");
        testOK("a\npassword\npassword\nMe\nHere\nNow\nPlace\nPlace\nUS\nyes\n\n", "-genkey -v -keysize 512 -keystore x.jks");
        testOK("", "-list -v -storepass password -keystore x.jks");
        testFail("a\n", "-list -v -keystore x.jks");
        assertTrue(ex.indexOf("password was incorrect") != -1);
        testFail("password\n", "-genkey -v -keysize 512 -keystore x.jks");
        assertTrue(ex.indexOf("alias <mykey> already exists") != -1);
        testOK("\n\n\n\n\n\nyes\n\n", "-genkey -v -keysize 512 -alias mykey2 -storepass password -keystore x.jks");
        testOK("password\n", "-list -v -keystore x.jks");
        testFail("a\naaaaaa\nbbbbbb\na\n", "-keypasswd -v -alias mykey2 -storepass password -keystore x.jks");
        assertTrue(ex.indexOf("Too many failures - try later") != -1);
        testOK("aaaaaa\naaaaaa\n", "-keypasswd -v -alias mykey2 -storepass password -keystore x.jks");
        testOK("", "-selfcert -v -alias mykey -storepass password -keystore x.jks");
        testOK("", "-list -v -storepass password -keystore x.jks");
        remove("cert");
        testOK("", "-export -v -alias mykey -file cert -storepass password -keystore x.jks");
        testFail("", "-import -v -file cert -storepass password -keystore x.jks");
        assertTrue(ex.indexOf("Certificate reply and certificate in keystore are identical") != -1);
        testOK("", "-printcert -file cert -keystore x.jks");
        remove("cert");
        testOK("", "-list -storepass password -provider sun.security.provider.Sun -keystore x.jks");
        testFail("", "-storepasswd -storepass password -new abc");
        assertTrue(ex.indexOf("New password must be at least 6 characters") != -1);
        testFail("", "-storepasswd -storetype PKCS11 -keystore NONE");
        assertTrue(ex.indexOf("UnsupportedOperationException") != -1);
        testFail("", "-keypasswd -storetype PKCS11 -keystore NONE");
        assertTrue(ex.indexOf("UnsupportedOperationException") != -1);
        testFail("", "-list -protected -storepass password -keystore x.jks");
        assertTrue(ex.indexOf("if -protected is specified, then") != -1);
        testFail("", "-keypasswd -protected -keypass password -keystore x.jks");
        assertTrue(ex.indexOf("if -protected is specified, then") != -1);
        testFail("", "-keypasswd -protected -new password -keystore x.jks");
        assertTrue(ex.indexOf("if -protected is specified, then") != -1);
        remove("x.jks");
    }
    void i18nPKCS11Test() throws Exception {
        testOK("", p11Arg + "-storepass test12 -genkey -alias genkey -dname cn=genkey -keysize 512 -keyalg rsa");
        testOK("", p11Arg + "-storepass test12 -list");
        testOK("", p11Arg + "-storepass test12 -list -alias genkey");
        testOK("", p11Arg + "-storepass test12 -certreq -alias genkey -file genkey.certreq");
        testOK("", p11Arg + "-storepass test12 -export -alias genkey -file genkey.cert");
        testOK("", "-printcert -file genkey.cert");
        testOK("", p11Arg + "-storepass test12 -selfcert -alias genkey -dname cn=selfCert");
        testOK("", p11Arg + "-storepass test12 -list -alias genkey -v");
        assertTrue(out.indexOf("Owner: CN=selfCert") != -1);
        testOK("", p11Arg + "-storepass test12 -delete -alias genkey");
        testOK("", p11Arg + "-storepass test12 -list");
        assertTrue(out.indexOf("Your keystore contains 0 entries") != -1);
        remove("genkey.cert");
        remove("genkey.certreq");
    }
    void sszzTest() throws Exception {
        testAnyway("", NSS_P11_ARG+"-delete -alias nss -storepass test12");
        testAnyway("", NZZ_P11_ARG+"-delete -alias nss -storepass test12");
        testOK("", NSS_P11_ARG+"-genkeypair -dname CN=NSS -alias nss -storepass test12");
        testOK("", NSS_SRC_P11_ARG + NZZ_P11_ARG +
                "-importkeystore -srcstorepass test12 -deststorepass test12");
        testAnyway("", NSS_P11_ARG+"-delete -alias nss -storepass test12");
        testAnyway("", NZZ_P11_ARG+"-delete -alias nss -storepass test12");
    }
    public static void main(String[] args) throws Exception {
        HumanInputStream.test();
        KeyToolTest t = new KeyToolTest();
        if (System.getProperty("file") != null) {
            t.sqeTest();
            t.testAll();
            t.i18nTest();
            t.v3extTest("RSA");
            t.v3extTest("DSA");
            boolean testEC = true;
            try {
                KeyPairGenerator.getInstance("EC");
            } catch (NoSuchAlgorithmException nae) {
                testEC = false;
            }
            if (testEC) t.v3extTest("EC");
        }
        if (System.getProperty("nss") != null) {
            t.srcP11Arg = NSS_SRC_P11_ARG;
            t.p11Arg = NSS_P11_ARG;
            t.testPKCS11();
            t.i18nPKCS11Test();
        }
        if (System.getProperty("solaris") != null) {
            t.srcP11Arg = SUN_SRC_P11_ARG;
            t.p11Arg = SUN_P11_ARG;
            t.testPKCS11();
            t.testPKCS11ImportKeyStore();
            t.i18nPKCS11Test();
        }
        System.out.println("Test pass!!!");
    }
}
class TestException extends Exception {
    public TestException(String e) {
        super(e);
    }
}
class HumanInputStream extends InputStream {
    byte[] src;
    int pos;
    int length;
    boolean inLine;
    int stopIt;
    public HumanInputStream(String input) {
        src = input.getBytes();
        pos = 0;
        length = src.length;
        stopIt = 0;
        inLine = false;
    }
    @Override public int read() throws IOException {
        int re;
        if(pos < length) {
            re = src[pos];
            if(inLine) {
                if(stopIt > 0) {
                    stopIt--;
                    re = -1;
                } else {
                    if(re == '\n') {
                        stopIt = 2;
                    }
                    pos++;
                }
            } else {
                pos++;
            }
        } else {
            re = -1;
        }
        return re;
    }
    @Override public int read(byte[] buffer, int offset, int len) {
        inLine = true;
        try {
            int re = super.read(buffer, offset, len);
            return re;
        } catch(Exception e) {
            throw new RuntimeException("HumanInputStream error");
        } finally {
            inLine = false;
        }
    }
    @Override public int available() {
        if(pos < length) return 1;
        return 0;
    }
    static void assertTrue(boolean bool) {
        if(!bool)
            throw new RuntimeException();
    }
    public static void test() throws Exception {
        class Tester {
            HumanInputStream is;
            BufferedReader reader;
            Tester(String s) {
                is = new HumanInputStream(s);
                reader = new BufferedReader(new InputStreamReader(is));
            }
            void testStreamReadOnce(int expection) throws Exception {
                assertTrue(is.read() == expection);
            }
            void testStreamReadMany(String expection) throws Exception {
                char[] keys = expection.toCharArray();
                for(int i=0; i<keys.length; i++) {
                    assertTrue(is.read() == keys[i]);
                }
            }
            void testReaderReadline(String expection) throws Exception {
                String s = new BufferedReader(new InputStreamReader(is)).readLine();
                if(s == null) assertTrue(expection == null);
                else assertTrue(s.equals(expection));
            }
            void testReaderReadline2(String expection) throws Exception  {
                String s = reader.readLine();
                if(s == null) assertTrue(expection == null);
                else assertTrue(s.equals(expection));
            }
        }
        Tester test;
        test = new Tester("111\n222\n\n444\n\n");
        test.testReaderReadline("111");
        test.testReaderReadline("222");
        test.testReaderReadline("");
        test.testReaderReadline("444");
        test.testReaderReadline("");
        test.testReaderReadline(null);
        test = new Tester("111\n222\n\n444\n\n");
        test.testReaderReadline2("111");
        test.testReaderReadline2("222");
        test.testReaderReadline2("");
        test.testReaderReadline2("444");
        test.testReaderReadline2("");
        test.testReaderReadline2(null);
        test = new Tester("111\n222\n\n444\n\n");
        test.testReaderReadline2("111");
        test.testReaderReadline("222");
        test.testReaderReadline2("");
        test.testReaderReadline2("444");
        test.testReaderReadline("");
        test.testReaderReadline2(null);
        test = new Tester("1\n2");
        test.testStreamReadMany("1\n2");
        test.testStreamReadOnce(-1);
        test = new Tester("12\n234");
        test.testStreamReadOnce('1');
        test.testReaderReadline("2");
        test.testStreamReadOnce('2');
        test.testReaderReadline2("34");
        test.testReaderReadline2(null);
        test = new Tester("changeit\n");
        test.testStreamReadMany("changeit\n");
        test.testReaderReadline(null);
        test = new Tester("changeit\nName\nCountry\nYes\n");
        test.testStreamReadMany("changeit\n");
        test.testReaderReadline("Name");
        test.testReaderReadline("Country");
        test.testReaderReadline("Yes");
        test.testReaderReadline(null);
        test = new Tester("Me\nHere\n");
        test.testReaderReadline2("Me");
        test.testReaderReadline2("Here");
    }
}
