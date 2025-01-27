class OCSPChecker extends PKIXCertPathChecker {
    static final String OCSP_ENABLE_PROP = "ocsp.enable";
    static final String OCSP_URL_PROP = "ocsp.responderURL";
    static final String OCSP_CERT_SUBJECT_PROP =
        "ocsp.responderCertSubjectName";
    static final String OCSP_CERT_ISSUER_PROP = "ocsp.responderCertIssuerName";
    static final String OCSP_CERT_NUMBER_PROP =
        "ocsp.responderCertSerialNumber";
    private static final String HEX_DIGITS = "0123456789ABCDEFabcdef";
    private static final Debug DEBUG = Debug.getInstance("certpath");
    private static final boolean dump = false;
    private int remainingCerts;
    private X509Certificate[] certs;
    private CertPath cp;
    private PKIXParameters pkixParams;
    private boolean onlyEECert = false;
    OCSPChecker(CertPath certPath, PKIXParameters pkixParams)
        throws CertPathValidatorException {
        this(certPath, pkixParams, false);
    }
    OCSPChecker(CertPath certPath, PKIXParameters pkixParams, boolean onlyEECert)
        throws CertPathValidatorException {
        this.cp = certPath;
        this.pkixParams = pkixParams;
        this.onlyEECert = onlyEECert;
        List<? extends Certificate> tmp = cp.getCertificates();
        certs = tmp.toArray(new X509Certificate[tmp.size()]);
        init(false);
    }
    @Override
    public void init(boolean forward) throws CertPathValidatorException {
        if (!forward) {
            remainingCerts = certs.length + 1;
        } else {
            throw new CertPathValidatorException(
                "Forward checking not supported");
        }
    }
    @Override public boolean isForwardCheckingSupported() {
        return false;
    }
    @Override public Set<String> getSupportedExtensions() {
        return Collections.<String>emptySet();
    }
    @Override
    public void check(Certificate cert, Collection<String> unresolvedCritExts)
        throws CertPathValidatorException {
        remainingCerts--;
        X509CertImpl currCertImpl = null;
        try {
            currCertImpl = X509CertImpl.toImpl((X509Certificate)cert);
        } catch (CertificateException ce) {
            throw new CertPathValidatorException(ce);
        }
        if (onlyEECert && currCertImpl.getBasicConstraints() != -1) {
            if (DEBUG != null) {
                DEBUG.println("Skipping revocation check, not end entity cert");
            }
            return;
        }
        String[] properties = getOCSPProperties();
        URI uri = getOCSPServerURI(currCertImpl, properties[0]);
        X500Principal responderSubjectName = null;
        X500Principal responderIssuerName = null;
        BigInteger responderSerialNumber = null;
        if (properties[1] != null) {
            responderSubjectName = new X500Principal(properties[1]);
        } else if (properties[2] != null && properties[3] != null) {
            responderIssuerName = new X500Principal(properties[2]);
            String value = stripOutSeparators(properties[3]);
            responderSerialNumber = new BigInteger(value, 16);
        } else if (properties[2] != null || properties[3] != null) {
            throw new CertPathValidatorException(
                "Must specify both ocsp.responderCertIssuerName and " +
                "ocsp.responderCertSerialNumber properties");
        }
        boolean seekResponderCert = false;
        if (responderSubjectName != null || responderIssuerName != null) {
            seekResponderCert = true;
        }
        X509Certificate issuerCert = null;
        boolean seekIssuerCert = true;
        X509Certificate responderCert = null;
        if (remainingCerts < certs.length) {
            issuerCert = certs[remainingCerts];
            seekIssuerCert = false; 
            if (!seekResponderCert) {
                responderCert = issuerCert;
                if (DEBUG != null) {
                    DEBUG.println("Responder's certificate is the same " +
                        "as the issuer of the certificate being validated");
                }
            }
        }
        if (seekIssuerCert || seekResponderCert) {
            if (DEBUG != null && seekResponderCert) {
                DEBUG.println("Searching trust anchors for responder's " +
                    "certificate");
            }
            Iterator<TrustAnchor> anchors
                = pkixParams.getTrustAnchors().iterator();
            if (!anchors.hasNext()) {
                throw new CertPathValidatorException(
                    "Must specify at least one trust anchor");
            }
            X500Principal certIssuerName =
                currCertImpl.getIssuerX500Principal();
            while (anchors.hasNext() && (seekIssuerCert || seekResponderCert)) {
                TrustAnchor anchor = anchors.next();
                X509Certificate anchorCert = anchor.getTrustedCert();
                X500Principal anchorSubjectName =
                    anchorCert.getSubjectX500Principal();
                if (dump) {
                    System.out.println("Issuer DN is " + certIssuerName);
                    System.out.println("Subject DN is " + anchorSubjectName);
                }
                if (seekIssuerCert &&
                    certIssuerName.equals(anchorSubjectName)) {
                    issuerCert = anchorCert;
                    seekIssuerCert = false; 
                    if (!seekResponderCert && responderCert == null) {
                        responderCert = anchorCert;
                        if (DEBUG != null) {
                            DEBUG.println("Responder's certificate is the" +
                                " same as the issuer of the certificate " +
                                "being validated");
                        }
                    }
                }
                if (seekResponderCert) {
                    if ((responderSubjectName != null &&
                         responderSubjectName.equals(anchorSubjectName)) ||
                        (responderIssuerName != null &&
                         responderSerialNumber != null &&
                         responderIssuerName.equals(
                         anchorCert.getIssuerX500Principal()) &&
                         responderSerialNumber.equals(
                         anchorCert.getSerialNumber()))) {
                        responderCert = anchorCert;
                        seekResponderCert = false; 
                    }
                }
            }
            if (issuerCert == null) {
                throw new CertPathValidatorException(
                    "No trusted certificate for " + currCertImpl.getIssuerDN());
            }
            if (seekResponderCert) {
                if (DEBUG != null) {
                    DEBUG.println("Searching cert stores for responder's " +
                        "certificate");
                }
                X509CertSelector filter = null;
                if (responderSubjectName != null) {
                    filter = new X509CertSelector();
                    filter.setSubject(responderSubjectName);
                } else if (responderIssuerName != null &&
                    responderSerialNumber != null) {
                    filter = new X509CertSelector();
                    filter.setIssuer(responderIssuerName);
                    filter.setSerialNumber(responderSerialNumber);
                }
                if (filter != null) {
                    List<CertStore> certStores = pkixParams.getCertStores();
                    for (CertStore certStore : certStores) {
                        Iterator i = null;
                        try {
                            i = certStore.getCertificates(filter).iterator();
                        } catch (CertStoreException cse) {
                            if (DEBUG != null) {
                                DEBUG.println("CertStore exception:" + cse);
                            }
                            continue;
                        }
                        if (i.hasNext()) {
                            responderCert = (X509Certificate) i.next();
                            seekResponderCert = false; 
                            break;
                        }
                    }
                }
            }
        }
        if (seekResponderCert) {
            throw new CertPathValidatorException(
                "Cannot find the responder's certificate " +
                "(set using the OCSP security properties).");
        }
        CertId certId = null;
        OCSPResponse response = null;
        try {
            certId = new CertId
                (issuerCert, currCertImpl.getSerialNumberObject());
            response = OCSP.check(Collections.singletonList(certId), uri,
                responderCert, pkixParams.getDate());
        } catch (Exception e) {
            if (e instanceof CertPathValidatorException) {
                throw (CertPathValidatorException) e;
            } else {
                throw new CertPathValidatorException(e);
            }
        }
        RevocationStatus rs = (RevocationStatus) response.getSingleResponse(certId);
        RevocationStatus.CertStatus certStatus = rs.getCertStatus();
        if (certStatus == RevocationStatus.CertStatus.REVOKED) {
            Throwable t = new CertificateRevokedException(
                rs.getRevocationTime(), rs.getRevocationReason(),
                responderCert.getSubjectX500Principal(),
                rs.getSingleExtensions());
            throw new CertPathValidatorException(t.getMessage(), t,
                null, -1, BasicReason.REVOKED);
        } else if (certStatus == RevocationStatus.CertStatus.UNKNOWN) {
            throw new CertPathValidatorException(
                "Certificate's revocation status is unknown", null, cp,
                remainingCerts, BasicReason.UNDETERMINED_REVOCATION_STATUS);
        }
    }
    private static URI getOCSPServerURI(X509CertImpl currCertImpl,
        String responderURL) throws CertPathValidatorException {
        if (responderURL != null) {
            try {
                return new URI(responderURL);
            } catch (URISyntaxException e) {
                throw new CertPathValidatorException(e);
            }
        }
        AuthorityInfoAccessExtension aia =
            currCertImpl.getAuthorityInfoAccessExtension();
        if (aia == null) {
            throw new CertPathValidatorException(
                "Must specify the location of an OCSP Responder");
        }
        List<AccessDescription> descriptions = aia.getAccessDescriptions();
        for (AccessDescription description : descriptions) {
            if (description.getAccessMethod().equals(
                AccessDescription.Ad_OCSP_Id)) {
                GeneralName generalName = description.getAccessLocation();
                if (generalName.getType() == GeneralNameInterface.NAME_URI) {
                    URIName uri = (URIName) generalName.getName();
                    return uri.getURI();
                }
            }
        }
        throw new CertPathValidatorException(
            "Cannot find the location of the OCSP Responder");
    }
    private static String[] getOCSPProperties() {
        final String[] properties = new String[4];
        AccessController.doPrivileged(
            new PrivilegedAction<Void>() {
                public Void run() {
                    properties[0] = Security.getProperty(OCSP_URL_PROP);
                    properties[1] =
                        Security.getProperty(OCSP_CERT_SUBJECT_PROP);
                    properties[2] =
                        Security.getProperty(OCSP_CERT_ISSUER_PROP);
                    properties[3] =
                        Security.getProperty(OCSP_CERT_NUMBER_PROP);
                    return null;
                }
            });
        return properties;
    }
    private static String stripOutSeparators(String value) {
        char[] chars = value.toCharArray();
        StringBuilder hexNumber = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (HEX_DIGITS.indexOf(chars[i]) != -1) {
                hexNumber.append(chars[i]);
            }
        }
        return hexNumber.toString();
    }
}
