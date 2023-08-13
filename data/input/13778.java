public final class StatusLoopDependency {
    static String selfSignedCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICPjCCAaegAwIBAgIBADANBgkqhkiG9w0BAQQFADAfMQswCQYDVQQGEwJVUzEQ\n" +
        "MA4GA1UEChMHRXhhbXBsZTAeFw0wOTA2MjgxMzMyMThaFw0zMDA2MDgxMzMyMTha\n" +
        "MB8xCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdFeGFtcGxlMIGfMA0GCSqGSIb3DQEB\n" +
        "AQUAA4GNADCBiQKBgQDInJhXi0655bPXAVkz1n5I6fAcZejzPnOPuwq3hU3OxFw8\n" +
        "81Uf6o9oKI1h4w4XAD8u1cUNOgiX+wPwojronlp68bIfO6FVhNf287pLtLhNJo+7\n" +
        "m6Qxw3ymFvEKy+PVj20CHSggdKHxUa4MBZBmHMFNBuxfYmjwzn+yTMmCCXOvSwID\n" +
        "AQABo4GJMIGGMB0GA1UdDgQWBBSQ52Dpau+gtL+Kc31dusYnKj16ZTBHBgNVHSME\n" +
        "QDA+gBSQ52Dpau+gtL+Kc31dusYnKj16ZaEjpCEwHzELMAkGA1UEBhMCVVMxEDAO\n" +
        "BgNVBAoTB0V4YW1wbGWCAQAwDwYDVR0TAQH/BAUwAwEB/zALBgNVHQ8EBAMCAQYw\n" +
        "DQYJKoZIhvcNAQEEBQADgYEAjBt6ea65HCqbGsS2rs/HhlGusYXtThRVC5vwXSey\n" +
        "ZFYwSgukuq1KDzckqZFu1meNImEwdZjwxdN0e2p/nVREPC42rZliSj6V1ThayKXj\n" +
        "DWEZW1U5aR8T+3NYfDrdKcJGx4Hzfz0qKz1j4ssV1M9ptJxYYv4y2Da+592IN1S9\n" +
        "v/E=\n" +
        "-----END CERTIFICATE-----";
    static String subCaCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICUDCCAbmgAwIBAgIBAzANBgkqhkiG9w0BAQQFADAfMQswCQYDVQQGEwJVUzEQ\n" +
        "MA4GA1UEChMHRXhhbXBsZTAeFw0wOTA2MjgxMzMyMjRaFw0yOTAzMTUxMzMyMjRa\n" +
        "MDExCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdFeGFtcGxlMRAwDgYDVQQLEwdDbGFz\n" +
        "cy0xMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDPFv24SK78VI0gWlyIrq/X\n" +
        "srl1431K5hJJxMYZtaQunyPmrYg3oI9KvKFykxnR0N4XDPaIi75p9dXGppVu80BA\n" +
        "+csvIPBwlBQoNmKDQWTziDOqfK4tE+IMuL/Y7pxnH6CDMY7VGpvatty2zcmH+m/v\n" +
        "E/n+HPyeELJQT2rT/3T+7wIDAQABo4GJMIGGMB0GA1UdDgQWBBRidC8Dt3dBzYES\n" +
        "KpR2tR560sZ0+zBHBgNVHSMEQDA+gBSQ52Dpau+gtL+Kc31dusYnKj16ZaEjpCEw\n" +
        "HzELMAkGA1UEBhMCVVMxEDAOBgNVBAoTB0V4YW1wbGWCAQAwDwYDVR0TAQH/BAUw\n" +
        "AwEB/zALBgNVHQ8EBAMCAQYwDQYJKoZIhvcNAQEEBQADgYEAMeMKqrMr5d3eTQsv\n" +
        "MYOD15Dl3THQGLAa4ad5Eyq5/1eUeEOpztzCgDfi0iPD8YCubIEVasBTSqTiGXqb\n" +
        "RpGuPHOwwfWvHrTeHSludiFBAUiKj7aEV+oQa0FBn4U4TT8HA62HQ93FhzTDI3jP\n" +
        "iil34GktVl6gfMKGzUEW/Dh8OM4=\n" +
        "-----END CERTIFICATE-----";
    static String topCrlIssuerCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICPjCCAaegAwIBAgIBAjANBgkqhkiG9w0BAQQFADAfMQswCQYDVQQGEwJVUzEQ\n" +
        "MA4GA1UEChMHRXhhbXBsZTAeFw0wOTA2MjgxMzMyMjNaFw0yOTAzMTUxMzMyMjNa\n" +
        "MB8xCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdFeGFtcGxlMIGfMA0GCSqGSIb3DQEB\n" +
        "AQUAA4GNADCBiQKBgQC99u93trf+WmpfiqunJy/P31ej1l4rESxft2JSGNjKuLFN\n" +
        "/BO3SAugGJSkCARAwXjB0c8eeXhXWhVVWdNpbKepRJTxrjDfnFIavLgtUvmFwn/3\n" +
        "hPXe+RQeA8+AJ99Y+o+10kY8JAZLa2j93C2FdmwOjUbo8aIz85yhbiV1tEDjLwID\n" +
        "AQABo4GJMIGGMB0GA1UdDgQWBBSyFyA3XWLbdL6W6hksmBn7RKsQmDBHBgNVHSME\n" +
        "QDA+gBSQ52Dpau+gtL+Kc31dusYnKj16ZaEjpCEwHzELMAkGA1UEBhMCVVMxEDAO\n" +
        "BgNVBAoTB0V4YW1wbGWCAQAwDwYDVR0TAQH/BAUwAwEB/zALBgNVHQ8EBAMCAQYw\n" +
        "DQYJKoZIhvcNAQEEBQADgYEAHTm8aRTeakgCfEBCgSWK9wvMW1c18ANGMm8OFDBk\n" +
        "xabVy9BT0MVFHlaneh89oIxTZN0FMTpg21GZMAvIzhEt7DGdO7HLsW7JniN7/OZ0\n" +
        "rACmpK5frmZrLS03zUm8c+rTbazNfYLoZVG3/mDZbKIi+4y8IGnFcgLVsHsYoBNP\n" +
        "G0c=\n" +
        "-----END CERTIFICATE-----";
    static String subCrlIssuerCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICUDCCAbmgAwIBAgIBBDANBgkqhkiG9w0BAQQFADAfMQswCQYDVQQGEwJVUzEQ\n" +
        "MA4GA1UEChMHRXhhbXBsZTAeFw0wOTA2MjgxMzMyMjdaFw0yOTAzMTUxMzMyMjda\n" +
        "MDExCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdFeGFtcGxlMRAwDgYDVQQLEwdDbGFz\n" +
        "cy0xMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC+8AcLJtGAVUWvv3ifcyQw\n" +
        "OGqwzcPrBw/XCs6vTMlcdtFzcH1M+Z3/QHN9+5VT1gqeTIZ+b8g9005Og3XKy/HX\n" +
        "obXZeLv20VZsr+jm52ySghEYOVCTJ9OyFOAp5adp6nf0cA66Feh3LsmVhpTEcDOG\n" +
        "GnyntQm0DBYxRoOT/GBlvQIDAQABo4GJMIGGMB0GA1UdDgQWBBSRWhMuZLQoHSDN\n" +
        "xhxr+vdDmfAY8jBHBgNVHSMEQDA+gBSQ52Dpau+gtL+Kc31dusYnKj16ZaEjpCEw\n" +
        "HzELMAkGA1UEBhMCVVMxEDAOBgNVBAoTB0V4YW1wbGWCAQAwDwYDVR0TAQH/BAUw\n" +
        "AwEB/zALBgNVHQ8EBAMCAQYwDQYJKoZIhvcNAQEEBQADgYEAMIDZLdOLFiPyS1bh\n" +
        "Ch4eUYHT+K1WG93skbga3kVYg3GSe+gctwkKwKK13bwfi8zc7wwz6MtmQwEYhppc\n" +
        "pKKKEwi5QirBCP54rihLCvRQaj6ZqUJ6VP+zPAqHYMDbzlBbHtVF/1lQUP30I6SV\n" +
        "Fu987DvLmZ2GuQA9FKJsnlD9pbU=\n" +
        "-----END CERTIFICATE-----";
    static String targetCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICNzCCAaCgAwIBAgIBAjANBgkqhkiG9w0BAQQFADAxMQswCQYDVQQGEwJVUzEQ\n" +
        "MA4GA1UEChMHRXhhbXBsZTEQMA4GA1UECxMHQ2xhc3MtMTAeFw0wOTA2MjgxMzMy\n" +
        "MzBaFw0yOTAzMTUxMzMyMzBaMEExCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdFeGFt\n" +
        "cGxlMRAwDgYDVQQLEwdDbGFzcy0xMQ4wDAYDVQQDEwVBbGljZTCBnzANBgkqhkiG\n" +
        "9w0BAQEFAAOBjQAwgYkCgYEA7wnsvR4XEOfVznf40l8ClLod+7L0y2/+smVV+GM/\n" +
        "T1/QF/stajAJxXNy08gK00WKZ6ruTHhR9vh/Z6+EQM2RZDCpU0A7LPa3kLE/XTmS\n" +
        "1MLDu8ntkdlpURpvhdDWem+rl2HU5oZgzV8Jkcov9vXuSjqEDfr45FlPuV40T8+7\n" +
        "cxsCAwEAAaNPME0wCwYDVR0PBAQDAgPoMB0GA1UdDgQWBBSBwsAhi6Z1kriOs3ty\n" +
        "uSIujv9a3DAfBgNVHSMEGDAWgBRidC8Dt3dBzYESKpR2tR560sZ0+zANBgkqhkiG\n" +
        "9w0BAQQFAAOBgQDEiBqd5AMy2SQopFaS3dYkzj8MHlwtbCSoNVYkOfDnewcatrbk\n" +
        "yFcp6FX++PMdOQFHWvvnDdkCUAzZQp8kCkF9tGLVLBtOK7XxQ1us1LZym7kOPzsd\n" +
        "G93Dcf0U1JRO77juc61Br5paAy8Bok18Y/MeG7uKgB2MAEJYKhGKbCrfMw==\n" +
        "-----END CERTIFICATE-----";
    static String topCrlStr =
        "-----BEGIN X509 CRL-----\n" +
        "MIIBGzCBhQIBATANBgkqhkiG9w0BAQQFADAfMQswCQYDVQQGEwJVUzEQMA4GA1UE\n" +
        "ChMHRXhhbXBsZRcNMDkwNjI4MTMzMjM4WhcNMjgwODI3MTMzMjM4WjAiMCACAQUX\n" +
        "DTA5MDYyODEzMzIzN1owDDAKBgNVHRUEAwoBBKAOMAwwCgYDVR0UBAMCAQEwDQYJ\n" +
        "KoZIhvcNAQEEBQADgYEAVUIeu2x7ZwsliafoCBOg+u8Q4S/VFfTe/SQnRyTM3/V1\n" +
        "v+Vn5Acc7eo8Rh4AHcnFFbLNk38n6lllov/CaVR0IPZ6hnrNHVa7VYkNlRAwV2aN\n" +
        "GUUhkMMOLVLnN25UOrN9J637SHmRE6pB+TRMaEQ73V7UNlWxuSMK4KofWen0A34=\n" +
        "-----END X509 CRL-----";
    static String subCrlStr =
        "-----BEGIN X509 CRL-----\n" +
        "MIIBLTCBlwIBATANBgkqhkiG9w0BAQQFADAxMQswCQYDVQQGEwJVUzEQMA4GA1UE\n" +
        "ChMHRXhhbXBsZTEQMA4GA1UECxMHQ2xhc3MtMRcNMDkwNjI4MTMzMjQzWhcNMjgw\n" +
        "ODI3MTMzMjQzWjAiMCACAQQXDTA5MDYyODEzMzIzOFowDDAKBgNVHRUEAwoBBKAO\n" +
        "MAwwCgYDVR0UBAMCAQEwDQYJKoZIhvcNAQEEBQADgYEACQZEf6ydb3fKTMPJ8DBO\n" +
        "oo630MsrT3P0x0AC4+aQOueCBaGpNqW/H379uZxXAad7yr+aXUBwaeBMYVKUbwOe\n" +
        "5TrN5QWPe2eCkU+MSQvh1SHASDDMH4jhWFMRdO3aPMDKKPlO/Q3s0G72eD7Zo5dr\n" +
        "N9AvUXxGxU4DruoJuFPcrCI=\n" +
        "-----END X509 CRL-----";
    private static Set<TrustAnchor> generateTrustAnchors()
            throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream is =
                    new ByteArrayInputStream(selfSignedCertStr.getBytes());
        Certificate selfSignedCert = cf.generateCertificate(is);
        TrustAnchor anchor =
            new TrustAnchor((X509Certificate)selfSignedCert, null);
        return Collections.singleton(anchor);
    }
    private static CertStore generateCertificateStore() throws Exception {
        Collection entries = new HashSet();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream is;
        is = new ByteArrayInputStream(targetCertStr.getBytes());
        Certificate cert = cf.generateCertificate(is);
        entries.add(cert);
        is = new ByteArrayInputStream(subCaCertStr.getBytes());
        cert = cf.generateCertificate(is);
        entries.add(cert);
        is = new ByteArrayInputStream(selfSignedCertStr.getBytes());
        cert = cf.generateCertificate(is);
        entries.add(cert);
        is = new ByteArrayInputStream(topCrlIssuerCertStr.getBytes());
        cert = cf.generateCertificate(is);
        entries.add(cert);
        is = new ByteArrayInputStream(subCrlIssuerCertStr.getBytes());
        cert = cf.generateCertificate(is);
        entries.add(cert);
        is = new ByteArrayInputStream(topCrlStr.getBytes());
        Collection mixes = cf.generateCRLs(is);
        entries.addAll(mixes);
        is = new ByteArrayInputStream(subCrlStr.getBytes());
        mixes = cf.generateCRLs(is);
        entries.addAll(mixes);
        return CertStore.getInstance("Collection",
                            new CollectionCertStoreParameters(entries));
    }
    private static X509CertSelector generateSelector(String name)
                throws Exception {
        X509CertSelector selector = new X509CertSelector();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream is = null;
        if (name.equals("subca")) {
            is = new ByteArrayInputStream(subCaCertStr.getBytes());
        } else if (name.equals("subci")) {
            is = new ByteArrayInputStream(subCrlIssuerCertStr.getBytes());
        } else {
            is = new ByteArrayInputStream(targetCertStr.getBytes());
        }
        X509Certificate target = (X509Certificate)cf.generateCertificate(is);
        byte[] extVal = target.getExtensionValue("2.5.29.14");
        if (extVal != null) {
            DerInputStream in = new DerInputStream(extVal);
            byte[] subjectKID = in.getOctetString();
            selector.setSubjectKeyIdentifier(subjectKID);
        } else {
            throw new Exception("unexpected certificate: no SKID extension");
        }
        return selector;
    }
    private static boolean match(String name, Certificate cert)
                throws Exception {
        X509CertSelector selector = new X509CertSelector();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream is = null;
        if (name.equals("subca")) {
            is = new ByteArrayInputStream(subCaCertStr.getBytes());
        } else if (name.equals("subci")) {
            is = new ByteArrayInputStream(subCrlIssuerCertStr.getBytes());
        } else {
            is = new ByteArrayInputStream(targetCertStr.getBytes());
        }
        X509Certificate target = (X509Certificate)cf.generateCertificate(is);
        return target.equals(cert);
    }
    public static void main(String[] args) throws Exception {
        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
        X509CertSelector selector = generateSelector(args[0]);
        Set<TrustAnchor> anchors = generateTrustAnchors();
        CertStore certs = generateCertificateStore();
        PKIXBuilderParameters params =
                new PKIXBuilderParameters(anchors, selector);
        params.addCertStore(certs);
        params.setRevocationEnabled(true);
        params.setDate(new Date(109, 7, 1));   
        Security.setProperty("ocsp.enable", "false");
        System.setProperty("com.sun.security.enableCRLDP", "true");
        PKIXCertPathBuilderResult result =
                (PKIXCertPathBuilderResult)builder.build(params);
        if (!match(args[0], result.getCertPath().getCertificates().get(0))) {
            throw new Exception("unexpected certificate");
        }
    }
}
