@TestTargetClass(X509CRL.class)
public class X509CRLTest extends TestCase {
    private X509CRL tbt_crl;
    String certificate = "-----BEGIN CERTIFICATE-----\n"
        + "MIICZTCCAdICBQL3AAC2MA0GCSqGSIb3DQEBAgUAMF8xCzAJBgNVBAYTAlVTMSAw\n"
        + "HgYDVQQKExdSU0EgRGF0YSBTZWN1cml0eSwgSW5jLjEuMCwGA1UECxMlU2VjdXJl\n"
        + "IFNlcnZlciBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTAeFw05NzAyMjAwMDAwMDBa\n"
        + "Fw05ODAyMjAyMzU5NTlaMIGWMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZv\n"
        + "cm5pYTESMBAGA1UEBxMJUGFsbyBBbHRvMR8wHQYDVQQKExZTdW4gTWljcm9zeXN0\n"
        + "ZW1zLCBJbmMuMSEwHwYDVQQLExhUZXN0IGFuZCBFdmFsdWF0aW9uIE9ubHkxGjAY\n"
        + "BgNVBAMTEWFyZ29uLmVuZy5zdW4uY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCB\n"
        + "iQKBgQCofmdY+PiUWN01FOzEewf+GaG+lFf132UpzATmYJkA4AEA/juW7jSi+LJk\n"
        + "wJKi5GO4RyZoyimAL/5yIWDV6l1KlvxyKslr0REhMBaD/3Z3EsLTTEf5gVrQS6sT\n"
        + "WMoSZAyzB39kFfsB6oUXNtV8+UKKxSxKbxvhQn267PeCz5VX2QIDAQABMA0GCSqG\n"
        + "SIb3DQEBAgUAA34AXl3at6luiV/7I9MN5CXYoPJYI8Bcdc1hBagJvTMcmlqL2uOZ\n"
        + "H9T5hNMEL9Tk6aI7yZPXcw/xI2K6pOR/FrMp0UwJmdxX7ljV6ZtUZf7pY492UqwC\n"
        + "1777XQ9UEZyrKJvF5ntleeO0ayBqLGVKCWzWZX9YsXCpv47FNLZbupE=\n"
        + "-----END CERTIFICATE-----\n";
    ByteArrayInputStream certArray = new ByteArrayInputStream(certificate
        .getBytes());
    private class TBTCRL extends X509CRL {
        public String toString() {
            return null;
        }
        public boolean isRevoked(Certificate cert) {
            return true;
        }
        public Set<String> getNonCriticalExtensionOIDs() {
            return null;
        }
        public Set<String> getCriticalExtensionOIDs() {
            return null;
        }
        public byte[] getExtensionValue(String oid) {
            return null;
        }
        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }
        public byte[] getEncoded() {
            return null;
        }
        public void verify(PublicKey key)
                 throws CRLException, NoSuchAlgorithmException,
                        InvalidKeyException, NoSuchProviderException,
                        SignatureException
        {
        }
        public void verify(PublicKey key, String sigProvider)
                 throws CRLException, NoSuchAlgorithmException,
                        InvalidKeyException, NoSuchProviderException,
                        SignatureException
        {
        }
        public int getVersion() {
            return 2;
        }
        public Principal getIssuerDN() {
            return null;
        }
        public Date getThisUpdate() {
            return null;
        }
        public Date getNextUpdate() {
            return null;
        }
        public X509CRLEntry getRevokedCertificate(BigInteger serialNumber) {
            return null;
        }
        public Set<X509CRLEntry> getRevokedCertificates() {
            return null;
        }
        public byte[] getTBSCertList() {
            return null;
        }
        public byte[] getSignature() {
            return null;
        }
        public String getSigAlgName() {
            return null;
        }
        public String getSigAlgOID() {
            return null;
        }
        public byte[] getSigAlgParams() {
            return null;
        }
    }
    public X509CRLTest() {
    }
    public void setUp() {
        tbt_crl = new TBTCRL() {
            public byte[] getEncoded() {
                return new byte[] {1, 2, 3};
            }
        };
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getType",
        args = {}
    )
    public void testGetType() {
        assertEquals("The type of X509CRL should be X.509",
                                            tbt_crl.getType(), "X.509");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        TBTCRL tbt_crl_1 = new TBTCRL() {
            public byte[] getEncoded() {
                return new byte[] {1, 2, 3};
            }
        };
        TBTCRL tbt_crl_2 = new TBTCRL() {
            public byte[] getEncoded() {
                return new byte[] {1, 2, 3};
            }
        };
        TBTCRL tbt_crl_3 = new TBTCRL() {
            public byte[] getEncoded() {
                return new byte[] {3, 2, 1};
            }
        };
        assertTrue("The equivalence relation should be reflexive.",
                                                    tbt_crl.equals(tbt_crl));
        assertEquals("The CRLs with equal encoded form should be equal",
                                                    tbt_crl, tbt_crl_1);
        assertTrue("The equivalence relation should be symmetric.",
                                                    tbt_crl_1.equals(tbt_crl));
        assertEquals("The CRLs with equal encoded form should be equal",
                                                    tbt_crl_1, tbt_crl_2);
        assertTrue("The equivalence relation should be transitive.",
                                                    tbt_crl.equals(tbt_crl_2));
        assertFalse("Should not be equal to null object.",
                                                    tbt_crl.equals(null));
        assertFalse("The CRLs with differing encoded form should not be equal",
                                                    tbt_crl.equals(tbt_crl_3));
        assertFalse("The CRL should not be equals to the object which is not "
                    + "an instance of X509CRL", tbt_crl.equals(new Object()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        TBTCRL tbt_crl_1 = new TBTCRL() {
            public byte[] getEncoded() {
                return new byte[] {1, 2, 3};
            }
        };
        assertTrue("Equal objects should have the same hash codes.",
                                    tbt_crl.hashCode() == tbt_crl_1.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIssuerX500Principal",
        args = {}
    )
    public void testGetIssuerX500Principal() {
        TBTCRL crl = new TBTCRL() {
            public byte[] getEncoded() {
                return TestUtils.getX509CRL_v1();
            }
        };
        assertEquals(new X500Principal("CN=Z"), crl.getIssuerX500Principal());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getRevokedCertificate",
        args = {java.security.cert.X509Certificate.class}
    )
    @AndroidOnly("Test filed on RI: getRevokedCertificate throws " +
            "RuntimeException.")
    public void testGetRevokedCertificate() {
        try {
            tbt_crl.getRevokedCertificate((X509Certificate) null);
            fail("NullPointerException should be thrown "
                        + "in the case of null input data.");
        } catch (NullPointerException e) {
        }
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(certArray);
            tbt_crl.getRevokedCertificate(cert);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getEncoded",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getIssuerDN",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getNextUpdate",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getRevokedCertificate",
            args = {java.math.BigInteger.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getRevokedCertificates",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSigAlgName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSigAlgOID",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSigAlgParams",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSignature",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getTBSCertList",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getThisUpdate",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getVersion",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "verify",
            args = {java.security.PublicKey.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "verify",
            args = {java.security.PublicKey.class, java.lang.String.class}
        )
    })
    public void testAbstractMethods() {
        TBTCRL crl = new TBTCRL() {
            public byte[] getEncoded() {
                return TestUtils.getX509CRL_v1();
            }
        };
        try {
            crl.getEncoded();
            crl.getIssuerDN();
            crl.getNextUpdate();
            crl.getRevokedCertificate(BigInteger.ONE);
            crl.getRevokedCertificates();
            crl.getSigAlgName();
            crl.getSigAlgOID();
            crl.getSigAlgParams();
            crl.getSignature();
            crl.getTBSCertList();
            crl.getThisUpdate();
            crl.getVersion();
            crl.verify(null);
            crl.verify(null, "test");
        } catch (Exception e) {
            fail("Unexpected exception for constructor");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "X509CRL",
        args = {}
    )
    public void testX509CRL() {
        try {
            TBTCRL crl = new TBTCRL();
            assertTrue(crl instanceof X509CRL);
        } catch (Exception e) {
            fail("Unexpected exception for constructor");
        }
    }
    public static Test suite() {
        return new TestSuite(X509CRLTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
