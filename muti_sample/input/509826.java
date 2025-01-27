public class PKIXCertPathValidatorSpi extends CertPathValidatorSpi
{
    private static final String CERTIFICATE_POLICIES = X509Extensions.CertificatePolicies.getId();
    private static final String POLICY_MAPPINGS = X509Extensions.PolicyMappings.getId();
    private static final String INHIBIT_ANY_POLICY = X509Extensions.InhibitAnyPolicy.getId();
    private static final String ISSUING_DISTRIBUTION_POINT = X509Extensions.IssuingDistributionPoint.getId();
    private static final String DELTA_CRL_INDICATOR = X509Extensions.DeltaCRLIndicator.getId();
    private static final String POLICY_CONSTRAINTS = X509Extensions.PolicyConstraints.getId();
    private static final String BASIC_CONSTRAINTS = X509Extensions.BasicConstraints.getId();
    private static final String SUBJECT_ALTERNATIVE_NAME = X509Extensions.SubjectAlternativeName.getId();
    private static final String NAME_CONSTRAINTS = X509Extensions.NameConstraints.getId();
    private static final String KEY_USAGE = X509Extensions.KeyUsage.getId();
    private static final String CRL_NUMBER = X509Extensions.CRLNumber.getId();
    private static final String ANY_POLICY = "2.5.29.32.0";
    private static final int    KEY_CERT_SIGN = 5;
    private static final int    CRL_SIGN = 6;
    private static final String[] crlReasons = new String[] {
                                        "unspecified",
                                        "keyCompromise",
                                        "cACompromise",
                                        "affiliationChanged",
                                        "superseded",
                                        "cessationOfOperation",
                                        "certificateHold",
                                        "unknown",
                                        "removeFromCRL",
                                        "privilegeWithdrawn",
                                        "aACompromise" };
    private static final Set<BigInteger> SERIAL_BLACKLIST = new HashSet<BigInteger>(Arrays.asList(
        new BigInteger(1, new byte[] {(byte)0x07,(byte)0x7a,(byte)0x59,(byte)0xbc,(byte)0xd5,(byte)0x34,(byte)0x59,(byte)0x60,(byte)0x1c,(byte)0xa6,(byte)0x90,(byte)0x72,(byte)0x67,(byte)0xa6,(byte)0xdd,(byte)0x1c}),
        new BigInteger(1, new byte[] {(byte)0x04,(byte)0x7e,(byte)0xcb,(byte)0xe9,(byte)0xfc,(byte)0xa5,(byte)0x5f,(byte)0x7b,(byte)0xd0,(byte)0x9e,(byte)0xae,(byte)0x36,(byte)0xe1,(byte)0x0c,(byte)0xae,(byte)0x1e}),
        new BigInteger(1, new byte[] {(byte)0xd8,(byte)0xf3,(byte)0x5f,(byte)0x4e,(byte)0xb7,(byte)0x87,(byte)0x2b,(byte)0x2d,(byte)0xab,(byte)0x06,(byte)0x92,(byte)0xe3,(byte)0x15,(byte)0x38,(byte)0x2f,(byte)0xb0}),
        new BigInteger(1, new byte[] {(byte)0xb0,(byte)0xb7,(byte)0x13,(byte)0x3e,(byte)0xd0,(byte)0x96,(byte)0xf9,(byte)0xb5,(byte)0x6f,(byte)0xae,(byte)0x91,(byte)0xc8,(byte)0x74,(byte)0xbd,(byte)0x3a,(byte)0xc0}),
        new BigInteger(1, new byte[] {(byte)0x92,(byte)0x39,(byte)0xd5,(byte)0x34,(byte)0x8f,(byte)0x40,(byte)0xd1,(byte)0x69,(byte)0x5a,(byte)0x74,(byte)0x54,(byte)0x70,(byte)0xe1,(byte)0xf2,(byte)0x3f,(byte)0x43}),
        new BigInteger(1, new byte[] {(byte)0xe9,(byte)0x02,(byte)0x8b,(byte)0x95,(byte)0x78,(byte)0xe4,(byte)0x15,(byte)0xdc,(byte)0x1a,(byte)0x71,(byte)0x0a,(byte)0x2b,(byte)0x88,(byte)0x15,(byte)0x44,(byte)0x47}),
        new BigInteger(1, new byte[] {(byte)0xd7,(byte)0x55,(byte)0x8f,(byte)0xda,(byte)0xf5,(byte)0xf1,(byte)0x10,(byte)0x5b,(byte)0xb2,(byte)0x13,(byte)0x28,(byte)0x2b,(byte)0x70,(byte)0x77,(byte)0x29,(byte)0xa3}),
        new BigInteger(1, new byte[] {(byte)0xf5,(byte)0xc8,(byte)0x6a,(byte)0xf3,(byte)0x61,(byte)0x62,(byte)0xf1,(byte)0x3a,(byte)0x64,(byte)0xf5,(byte)0x4f,(byte)0x6d,(byte)0xc9,(byte)0x58,(byte)0x7c,(byte)0x06}),
        new BigInteger(1, new byte[] {(byte)0x39,(byte)0x2a,(byte)0x43,(byte)0x4f,(byte)0x0e,(byte)0x07,(byte)0xdf,(byte)0x1f,(byte)0x8a,(byte)0xa3,(byte)0x05,(byte)0xde,(byte)0x34,(byte)0xe0,(byte)0xc2,(byte)0x29}),
        new BigInteger(1, new byte[] {(byte)0x3e,(byte)0x75,(byte)0xce,(byte)0xd4,(byte)0x6b,(byte)0x69,(byte)0x30,(byte)0x21,(byte)0x21,(byte)0x88,(byte)0x30,(byte)0xae,(byte)0x86,(byte)0xa8,(byte)0x2a,(byte)0x71})
    ));
    private static final byte[][] PUBLIC_KEY_SHA1_BLACKLIST = {
        {(byte)0x41, (byte)0x0f, (byte)0x36, (byte)0x36, (byte)0x32, (byte)0x58, (byte)0xf3, (byte)0x0b, (byte)0x34, (byte)0x7d,
         (byte)0x12, (byte)0xce, (byte)0x48, (byte)0x63, (byte)0xe4, (byte)0x33, (byte)0x43, (byte)0x78, (byte)0x06, (byte)0xa8},
        {(byte)0xba, (byte)0x3e, (byte)0x7b, (byte)0xd3, (byte)0x8c, (byte)0xd7, (byte)0xe1, (byte)0xe6, (byte)0xb9, (byte)0xcd,
         (byte)0x4c, (byte)0x21, (byte)0x99, (byte)0x62, (byte)0xe5, (byte)0x9d, (byte)0x7a, (byte)0x2f, (byte)0x4e, (byte)0x37},
        {(byte)0xe2, (byte)0x3b, (byte)0x8d, (byte)0x10, (byte)0x5f, (byte)0x87, (byte)0x71, (byte)0x0a, (byte)0x68, (byte)0xd9,
         (byte)0x24, (byte)0x80, (byte)0x50, (byte)0xeb, (byte)0xef, (byte)0xc6, (byte)0x27, (byte)0xbe, (byte)0x4c, (byte)0xa6},
        {(byte)0x7b, (byte)0x2e, (byte)0x16, (byte)0xbc, (byte)0x39, (byte)0xbc, (byte)0xd7, (byte)0x2b, (byte)0x45, (byte)0x6e,
         (byte)0x9f, (byte)0x05, (byte)0x5d, (byte)0x1d, (byte)0xe6, (byte)0x15, (byte)0xb7, (byte)0x49, (byte)0x45, (byte)0xdb},
        {(byte)0xe8, (byte)0xf9, (byte)0x12, (byte)0x00, (byte)0xc6, (byte)0x5c, (byte)0xee, (byte)0x16, (byte)0xe0, (byte)0x39,
         (byte)0xb9, (byte)0xf8, (byte)0x83, (byte)0x84, (byte)0x16, (byte)0x61, (byte)0x63, (byte)0x5f, (byte)0x81, (byte)0xc5}
    };
    private static boolean isPublicKeyBlackListed(PublicKey publicKey) {
        byte[] encoded = publicKey.getEncoded();
        Digest digest = OpenSSLMessageDigest.getInstance("SHA1");
        digest.update(encoded, 0, encoded.length);
        byte[] out = new byte[digest.getDigestSize()];
        digest.doFinal(out, 0);
        for (byte[] sha1 : PUBLIC_KEY_SHA1_BLACKLIST) {
            if (Arrays.equals(out, sha1)) {
                return true;
            }
        }
        return false;
    }
    public CertPathValidatorResult engineValidate(
        CertPath certPath,
        CertPathParameters params)
        throws CertPathValidatorException, InvalidAlgorithmParameterException
    {
        if (!(params instanceof PKIXParameters))
        {
            throw new InvalidAlgorithmParameterException("params must be a PKIXParameters instance");
        }
        PKIXParameters paramsPKIX = (PKIXParameters)params;
        if (paramsPKIX.getTrustAnchors() == null)
        {
            throw new InvalidAlgorithmParameterException("trustAnchors is null, this is not allowed for path validation");
        }
        List    certs = certPath.getCertificates();
        int     n = certs.size();
        if (certs.isEmpty())
        {
            throw new CertPathValidatorException("CertPath is empty", null, certPath, 0);
        }
        {
            X509Certificate cert = (X509Certificate) certs.get(0);
            if (cert != null) {
                BigInteger serial = cert.getSerialNumber();
                if (serial != null && SERIAL_BLACKLIST.contains(serial)) {
                    String message = "Certificate revocation of serial 0x" + serial.toString(16);
                    System.out.println(message);
                    AnnotatedException e = new AnnotatedException(message);
                    throw new CertPathValidatorException(e.getMessage(), e, certPath, 0);
                }
            }
        }
        Date validDate = CertPathValidatorUtilities.getValidDate(paramsPKIX);
        Set userInitialPolicySet = paramsPKIX.getInitialPolicies();
        X509Certificate lastCert = (X509Certificate)certs.get(certs.size() - 1);
        TrustAnchor trust = CertPathValidatorUtilities.findTrustAnchor(lastCert,
                certPath, certs.size() - 1, paramsPKIX);
        if (trust == null)
        {
            throw new CertPathValidatorException("TrustAnchor for CertPath not found.", null, certPath, -1);
        }
        Iterator certIter;
        int index = 0;
        int i;
        List     []  policyNodes = new ArrayList[n + 1];
        for (int j = 0; j < policyNodes.length; j++)
        {
            policyNodes[j] = new ArrayList();
        }
        Set policySet = new HashSet();
        policySet.add(ANY_POLICY);
        PKIXPolicyNode  validPolicyTree = new PKIXPolicyNode(new ArrayList(), 0, policySet, null, new HashSet(), ANY_POLICY, false);
        policyNodes[0].add(validPolicyTree);
        Set     permittedSubtreesDN = new HashSet();
        Set     permittedSubtreesEmail = new HashSet();
        Set     permittedSubtreesIP = new HashSet();
        Set     excludedSubtreesDN = new HashSet();
        Set     excludedSubtreesEmail = new HashSet();
        Set     excludedSubtreesIP = new HashSet();
        int explicitPolicy;
        Set acceptablePolicies = null;
        if (paramsPKIX.isExplicitPolicyRequired())
        {
            explicitPolicy = 0;
        }
        else
        {
            explicitPolicy = n + 1;
        }
        int inhibitAnyPolicy;
        if (paramsPKIX.isAnyPolicyInhibited())
        {
            inhibitAnyPolicy = 0;
        }
        else
        {
            inhibitAnyPolicy = n + 1;
        }
        int policyMapping;
        if (paramsPKIX.isPolicyMappingInhibited())
        {
            policyMapping = 0;
        }
        else
        {
            policyMapping = n + 1;
        }
        PublicKey workingPublicKey;
        X500Principal workingIssuerName;
        X509Certificate sign = trust.getTrustedCert();
        boolean trustAnchorInChain = false;
        try
        {
            if (sign != null)
            {
                workingIssuerName = CertPathValidatorUtilities.getSubjectPrincipal(sign);
                workingPublicKey = sign.getPublicKey();
                try {
                    byte[] trustBytes = sign.getEncoded();
                    byte[] certBytes = lastCert.getEncoded();
                    trustAnchorInChain = Arrays.equals(trustBytes, certBytes);
                } catch(Exception e) {
                }
            }
            else
            {
                workingIssuerName = new X500Principal(trust.getCAName());
                workingPublicKey = trust.getCAPublicKey();
            }
        }
        catch (IllegalArgumentException ex)
        {
            throw new CertPathValidatorException("TrustAnchor subjectDN: " + ex.toString());
        }
        AlgorithmIdentifier workingAlgId = CertPathValidatorUtilities.getAlgorithmIdentifier(workingPublicKey);
        DERObjectIdentifier workingPublicKeyAlgorithm = workingAlgId.getObjectId();
        DEREncodable        workingPublicKeyParameters = workingAlgId.getParameters();
        int maxPathLength = n;
        Iterator tmpIter;
        int tmpInt;
        if (paramsPKIX.getTargetCertConstraints() != null
            && !paramsPKIX.getTargetCertConstraints().match((X509Certificate)certs.get(0)))
        {
            throw new CertPathValidatorException("target certificate in certpath does not match targetcertconstraints", null, certPath, 0);
        }
        List  pathCheckers = paramsPKIX.getCertPathCheckers();
        certIter = pathCheckers.iterator();
        while (certIter.hasNext())
        {
            ((PKIXCertPathChecker)certIter.next()).init(false);
        }
        X509Certificate cert = null;
        for (index = certs.size() - 1; index >= 0 ; index--)
        {
            if (isPublicKeyBlackListed(workingPublicKey)) {
                 String message = "Certificate revocation of public key " + workingPublicKey;
                 System.out.println(message);
                 AnnotatedException e = new AnnotatedException(message);
                 throw new CertPathValidatorException(e.getMessage(), e, certPath, index);
            }
            try
            {
                i = n - index;
                cert = (X509Certificate)certs.get(index);
                try
                {
                    if (!(i == 1 && trustAnchorInChain)) 
                    {
                        cert.verify(workingPublicKey, "BC");
                    }
                }
                catch (GeneralSecurityException e)
                {
                    throw new CertPathValidatorException("Could not validate certificate signature.", e, certPath, index);
                }
                try
                {
                    cert.checkValidity(validDate);
                }
                catch (CertificateExpiredException e)
                {
                    throw new CertPathValidatorException("Could not validate certificate: " + e.getMessage(), e, certPath, index);
                }
                catch (CertificateNotYetValidException e)
                {
                    throw new CertPathValidatorException("Could not validate certificate: " + e.getMessage(), e, certPath, index);
                }
                if (paramsPKIX.isRevocationEnabled())
                {
                    checkCRLs(paramsPKIX, cert, validDate, sign, workingPublicKey);
                }
                if (!CertPathValidatorUtilities.getEncodedIssuerPrincipal(cert).equals(workingIssuerName))
                {
                    throw new CertPathValidatorException(
                                "IssuerName(" + CertPathValidatorUtilities.getEncodedIssuerPrincipal(cert) +
                                ") does not match SubjectName(" + workingIssuerName +
                                ") of signing certificate", null, certPath, index);
                }
                if (!(CertPathValidatorUtilities.isSelfIssued(cert) && (i < n)))
                {
                    X500Principal principal = CertPathValidatorUtilities.getSubjectPrincipal(cert);
                    ASN1InputStream aIn = new ASN1InputStream(principal.getEncoded());
                    ASN1Sequence    dns;
                    try
                    {
                        dns = (ASN1Sequence)aIn.readObject();
                    }
                    catch (IOException e)
                    {
                        throw new CertPathValidatorException("exception extracting subject name when checking subtrees");
                    }
                    CertPathValidatorUtilities.checkPermittedDN(permittedSubtreesDN, dns);
                    CertPathValidatorUtilities.checkExcludedDN(excludedSubtreesDN, dns);
                    ASN1Sequence   altName = (ASN1Sequence)CertPathValidatorUtilities.getExtensionValue(cert, SUBJECT_ALTERNATIVE_NAME);
                    if (altName != null)
                    {
                        for (int j = 0; j < altName.size(); j++)
                        {
                            ASN1TaggedObject o = (ASN1TaggedObject)altName.getObjectAt(j);
                            switch(o.getTagNo())
                            {
                            case 1:
                                String email = DERIA5String.getInstance(o, true).getString();
                                CertPathValidatorUtilities.checkPermittedEmail(permittedSubtreesEmail, email);
                                CertPathValidatorUtilities.checkExcludedEmail(excludedSubtreesEmail, email);
                                break;
                            case 4:
                                ASN1Sequence altDN = ASN1Sequence.getInstance(o, true);
                                CertPathValidatorUtilities.checkPermittedDN(permittedSubtreesDN, altDN);
                                CertPathValidatorUtilities.checkExcludedDN(excludedSubtreesDN, altDN);
                                break;
                            case 7:
                                byte[] ip = ASN1OctetString.getInstance(o, true).getOctets();
                                CertPathValidatorUtilities.checkPermittedIP(permittedSubtreesIP, ip);
                                CertPathValidatorUtilities.checkExcludedIP(excludedSubtreesIP, ip);
                            }
                        }
                    }
                }
                ASN1Sequence   certPolicies = (ASN1Sequence)CertPathValidatorUtilities.getExtensionValue(cert, CERTIFICATE_POLICIES);
                if (certPolicies != null && validPolicyTree != null)
                {
                    Enumeration e = certPolicies.getObjects();
                    Set         pols = new HashSet();
                    while (e.hasMoreElements())
                    {
                        PolicyInformation   pInfo = PolicyInformation.getInstance(e.nextElement());
                        DERObjectIdentifier pOid = pInfo.getPolicyIdentifier();
                        pols.add(pOid.getId());
                        if (!ANY_POLICY.equals(pOid.getId()))
                        {
                            Set pq = CertPathValidatorUtilities.getQualifierSet(pInfo.getPolicyQualifiers());
                            boolean match = CertPathValidatorUtilities.processCertD1i(i, policyNodes, pOid, pq);
                            if (!match)
                            {
                                CertPathValidatorUtilities.processCertD1ii(i, policyNodes, pOid, pq);
                            }
                        }
                    }
                    if (acceptablePolicies == null || acceptablePolicies.contains(ANY_POLICY))
                    {
                        acceptablePolicies = pols;
                    }
                    else
                    {
                        Iterator    it = acceptablePolicies.iterator();
                        Set         t1 = new HashSet();
                        while (it.hasNext())
                        {
                            Object  o = it.next();
                            if (pols.contains(o))
                            {
                                t1.add(o);
                            }
                        }
                        acceptablePolicies = t1;
                    }
                    if ((inhibitAnyPolicy > 0) || ((i < n) && CertPathValidatorUtilities.isSelfIssued(cert)))
                    {
                        e = certPolicies.getObjects();
                        while (e.hasMoreElements())
                        {
                            PolicyInformation   pInfo = PolicyInformation.getInstance(e.nextElement());
                            if (ANY_POLICY.equals(pInfo.getPolicyIdentifier().getId()))
                            {
                                Set    _apq   = CertPathValidatorUtilities.getQualifierSet(pInfo.getPolicyQualifiers());
                                List      _nodes = policyNodes[i - 1];
                                for (int k = 0; k < _nodes.size(); k++)
                                {
                                    PKIXPolicyNode _node = (PKIXPolicyNode)_nodes.get(k);
                                    Iterator _policySetIter = _node.getExpectedPolicies().iterator();
                                    while (_policySetIter.hasNext())
                                    {
                                        Object _tmp = _policySetIter.next();
                                        String _policy;
                                        if (_tmp instanceof String)
                                        {
                                            _policy = (String)_tmp;
                                        }
                                        else if (_tmp instanceof DERObjectIdentifier)
                                        {
                                            _policy = ((DERObjectIdentifier)_tmp).getId();
                                        }
                                        else
                                        {
                                            continue;
                                        }
                                        boolean  _found        = false;
                                        Iterator _childrenIter = _node.getChildren();
                                        while (_childrenIter.hasNext())
                                        {
                                            PKIXPolicyNode _child = (PKIXPolicyNode)_childrenIter.next();
                                            if (_policy.equals(_child.getValidPolicy()))
                                            {
                                                _found = true;
                                            }
                                        }
                                        if (!_found)
                                        {
                                            Set _newChildExpectedPolicies = new HashSet();
                                            _newChildExpectedPolicies.add(_policy);
                                            PKIXPolicyNode _newChild = new PKIXPolicyNode(new ArrayList(),
                                                                                          i,
                                                                                          _newChildExpectedPolicies,
                                                                                          _node,
                                                                                          _apq,
                                                                                          _policy,
                                                                                          false);
                                            _node.addChild(_newChild);
                                            policyNodes[i].add(_newChild);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                    for (int j = (i - 1); j >= 0; j--)
                    {
                        List      nodes = policyNodes[j];
                        for (int k = 0; k < nodes.size(); k++)
                        {
                            PKIXPolicyNode node = (PKIXPolicyNode)nodes.get(k);
                            if (!node.hasChildren())
                            {
                                validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, node);
                                if (validPolicyTree == null)
                                {
                                    break;
                                }
                            }
                        }
                    }
                    Set criticalExtensionOids = cert.getCriticalExtensionOIDs();
                    if (criticalExtensionOids != null)
                    {
                        boolean critical = criticalExtensionOids.contains(CERTIFICATE_POLICIES);
                        List      nodes = policyNodes[i];
                        for (int j = 0; j < nodes.size(); j++)
                        {
                            PKIXPolicyNode node = (PKIXPolicyNode)nodes.get(j);
                            node.setCritical(critical);
                        }
                    }
                }
                if (certPolicies == null)
                {
                    validPolicyTree = null;
                }
                if (explicitPolicy <= 0 && validPolicyTree == null)
                {
                    throw new CertPathValidatorException("No valid policy tree found when one expected.");
                }
                if (i != n) 
                {
                    if (cert != null && cert.getVersion() == 1)
                    {
                        if (!(i == 1 && trustAnchorInChain)) 
                        {
                            throw new CertPathValidatorException(
                                "Version 1 certs can't be used as intermediate certificates");
                        }
                    }
                    DERObject   pm = CertPathValidatorUtilities.getExtensionValue(cert, POLICY_MAPPINGS);
                    if (pm != null)
                    {
                        ASN1Sequence mappings = (ASN1Sequence)pm;
                        for (int j = 0; j < mappings.size(); j++)
                        {
                            ASN1Sequence    mapping = (ASN1Sequence)mappings.getObjectAt(j);
                            DERObjectIdentifier issuerDomainPolicy = (DERObjectIdentifier)mapping.getObjectAt(0);
                            DERObjectIdentifier subjectDomainPolicy = (DERObjectIdentifier)mapping.getObjectAt(1);
                            if (ANY_POLICY.equals(issuerDomainPolicy.getId()))
                            {
                                throw new CertPathValidatorException("IssuerDomainPolicy is anyPolicy");
                            }
                            if (ANY_POLICY.equals(subjectDomainPolicy.getId()))
                            {
                                throw new CertPathValidatorException("SubjectDomainPolicy is anyPolicy");
                            }
                        }
                    }
                    if (pm != null)
                    {
                        ASN1Sequence mappings = (ASN1Sequence)pm;
                        Map m_idp = new HashMap();
                        Set s_idp = new HashSet();
                        for (int j = 0; j < mappings.size(); j++)
                        {
                            ASN1Sequence mapping = (ASN1Sequence)mappings.getObjectAt(j);
                            String id_p = ((DERObjectIdentifier)mapping.getObjectAt(0)).getId();
                            String sd_p = ((DERObjectIdentifier)mapping.getObjectAt(1)).getId();
                            Set tmp;
                            if (!m_idp.containsKey(id_p))
                            {
                                tmp = new HashSet();
                                tmp.add(sd_p);
                                m_idp.put(id_p, tmp);
                                s_idp.add(id_p);
                            }
                            else
                            {
                                tmp = (Set)m_idp.get(id_p);
                                tmp.add(sd_p);
                            }
                        }
                        Iterator it_idp = s_idp.iterator();
                        while (it_idp.hasNext())
                        {
                            String id_p = (String)it_idp.next();
                            if (policyMapping > 0)
                            {
                                boolean idp_found = false;
                                Iterator nodes_i = policyNodes[i].iterator();
                                while (nodes_i.hasNext())
                                {
                                    PKIXPolicyNode node = (PKIXPolicyNode)nodes_i.next();
                                    if (node.getValidPolicy().equals(id_p))
                                    {
                                        idp_found = true;
                                        node.expectedPolicies = (Set)m_idp.get(id_p);
                                        break;
                                    }
                                }
                                if (!idp_found)
                                {
                                    nodes_i = policyNodes[i].iterator();
                                    while (nodes_i.hasNext())
                                    {
                                        PKIXPolicyNode node = (PKIXPolicyNode)nodes_i.next();
                                        if (ANY_POLICY.equals(node.getValidPolicy()))
                                        {
                                            Set pq = null;
                                            ASN1Sequence policies = (ASN1Sequence)CertPathValidatorUtilities.getExtensionValue(
                                                    cert, CERTIFICATE_POLICIES);
                                            Enumeration e = policies.getObjects();
                                            while (e.hasMoreElements())
                                            {
                                                PolicyInformation pinfo = PolicyInformation.getInstance(e.nextElement());
                                                if (ANY_POLICY.equals(pinfo.getPolicyIdentifier().getId()))
                                                {
                                                    pq = CertPathValidatorUtilities.getQualifierSet(pinfo.getPolicyQualifiers());
                                                    break;
                                                }
                                            }
                                            boolean ci = false;
                                            if (cert.getCriticalExtensionOIDs() != null)
                                            {
                                                ci = cert.getCriticalExtensionOIDs().contains(CERTIFICATE_POLICIES);
                                            }
                                            PKIXPolicyNode p_node = (PKIXPolicyNode)node.getParent();
                                            if (ANY_POLICY.equals(p_node.getValidPolicy()))
                                            {
                                                PKIXPolicyNode c_node = new PKIXPolicyNode(
                                                        new ArrayList(), i,
                                                        (Set)m_idp.get(id_p),
                                                        p_node, pq, id_p, ci);
                                                p_node.addChild(c_node);
                                                policyNodes[i].add(c_node);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            else if (policyMapping <= 0)
                            {
                                Iterator nodes_i = policyNodes[i].iterator();
                                while (nodes_i.hasNext())
                                {
                                    PKIXPolicyNode node = (PKIXPolicyNode)nodes_i.next();
                                    if (node.getValidPolicy().equals(id_p))
                                    {
                                        PKIXPolicyNode p_node = (PKIXPolicyNode)node.getParent();
                                        p_node.removeChild(node);
                                        nodes_i.remove();
                                        for (int k = (i - 1); k >= 0; k--)
                                        {
                                            List nodes = policyNodes[k];
                                            for (int l = 0; l < nodes.size(); l++)
                                            {
                                                PKIXPolicyNode node2 = (PKIXPolicyNode)nodes.get(l);
                                                if (!node2.hasChildren())
                                                {
                                                    validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, node2);
                                                    if (validPolicyTree == null)
                                                    {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ASN1Sequence ncSeq = (ASN1Sequence)CertPathValidatorUtilities.getExtensionValue(cert, NAME_CONSTRAINTS);
                    if (ncSeq != null)
                    {
                        NameConstraints nc = new NameConstraints(ncSeq);
                        ASN1Sequence permitted = nc.getPermittedSubtrees();
                        if (permitted != null)
                        {
                            Enumeration e = permitted.getObjects();
                            while (e.hasMoreElements())
                            {
                                GeneralSubtree  subtree = GeneralSubtree.getInstance(e.nextElement());
                                GeneralName     base = subtree.getBase();
                                switch(base.getTagNo())
                                {
                                    case 1:
                                        permittedSubtreesEmail = CertPathValidatorUtilities.intersectEmail(permittedSubtreesEmail, DERIA5String.getInstance(base.getName()).getString());
                                        break;
                                    case 4:
                                        permittedSubtreesDN = CertPathValidatorUtilities.intersectDN(permittedSubtreesDN, (ASN1Sequence)base.getName());
                                        break;
                                    case 7:
                                        permittedSubtreesIP = CertPathValidatorUtilities.intersectIP(permittedSubtreesIP, ASN1OctetString.getInstance(base.getName()).getOctets());
                                        break;
                                }
                            }
                        }
                        ASN1Sequence excluded = nc.getExcludedSubtrees();
                        if (excluded != null)
                        {
                            Enumeration e = excluded.getObjects();
                            while (e.hasMoreElements())
                            {
                                GeneralSubtree  subtree = GeneralSubtree.getInstance(e.nextElement());
                                GeneralName     base = subtree.getBase();
                                switch(base.getTagNo())
                                {
                                case 1:
                                    excludedSubtreesEmail = CertPathValidatorUtilities.unionEmail(excludedSubtreesEmail, DERIA5String.getInstance(base.getName()).getString());
                                    break;
                                case 4:
                                    excludedSubtreesDN = CertPathValidatorUtilities.unionDN(excludedSubtreesDN, (ASN1Sequence)base.getName());
                                    break;
                                case 7:
                                    excludedSubtreesIP = CertPathValidatorUtilities.unionIP(excludedSubtreesIP, ASN1OctetString.getInstance(base.getName()).getOctets());
                                    break;
                                }
                            }
                        }
                    }
                    if (!CertPathValidatorUtilities.isSelfIssued(cert))
                    {
                        if (explicitPolicy != 0)
                        {
                            explicitPolicy--;
                        }
                        if (policyMapping != 0)
                        {
                            policyMapping--;
                        }
                        if (inhibitAnyPolicy != 0)
                        {
                            inhibitAnyPolicy--;
                        }
                    }
                    ASN1Sequence pc = (ASN1Sequence)CertPathValidatorUtilities.getExtensionValue(cert, POLICY_CONSTRAINTS);
                    if (pc != null)
                    {
                        Enumeration policyConstraints = pc.getObjects();
                        while (policyConstraints.hasMoreElements())
                        {
                            ASN1TaggedObject    constraint = (ASN1TaggedObject)policyConstraints.nextElement();
                            switch (constraint.getTagNo())
                            {
                            case 0:
                                tmpInt = DERInteger.getInstance(constraint).getValue().intValue();
                                if (tmpInt < explicitPolicy)
                                {
                                    explicitPolicy = tmpInt;
                                }
                                break;
                            case 1:
                                tmpInt = DERInteger.getInstance(constraint).getValue().intValue();
                                if (tmpInt < policyMapping)
                                {
                                    policyMapping = tmpInt;
                                }
                            break;
                            }
                        }
                    }
                    DERInteger iap = (DERInteger)CertPathValidatorUtilities.getExtensionValue(cert, INHIBIT_ANY_POLICY);
                    if (iap != null)
                    {
                        int _inhibitAnyPolicy = iap.getValue().intValue();
                        if (_inhibitAnyPolicy < inhibitAnyPolicy)
                        {
                            inhibitAnyPolicy = _inhibitAnyPolicy;
                        }
                    }
                    BasicConstraints    bc = BasicConstraints.getInstance(
                            CertPathValidatorUtilities.getExtensionValue(cert, BASIC_CONSTRAINTS));
                    if (bc != null)
                    {
                        if (!(bc.isCA()))
                        {
                            throw new CertPathValidatorException("Not a CA certificate");
                        }
                    }
                    else
                    {
                        if (!(i == 1 && trustAnchorInChain)) 
                        {
                            throw new CertPathValidatorException("Intermediate certificate lacks BasicConstraints");
                        }
                    }
                    if (!CertPathValidatorUtilities.isSelfIssued(cert))
                    {
                        if (maxPathLength <= 0)
                        {
                            throw new CertPathValidatorException("Max path length not greater than zero");
                        }
                        maxPathLength--;
                    }
                    if (bc != null)
                    {
                        BigInteger          _pathLengthConstraint = bc.getPathLenConstraint();
                        if (_pathLengthConstraint != null)
                        {
                            int _plc = _pathLengthConstraint.intValue();
                            if (_plc < maxPathLength)
                            {
                                maxPathLength = _plc;
                            }
                        }
                    }
                    boolean[] _usage = cert.getKeyUsage();
                    if ((_usage != null) && !_usage[5])
                    {
                        throw new CertPathValidatorException(
                                    "Issuer certificate keyusage extension is critical an does not permit key signing.\n",
                                    null, certPath, index);
                    }
                    if (cert.getCriticalExtensionOIDs() != null)
                    {
                        Set criticalExtensions = new HashSet(cert.getCriticalExtensionOIDs());
                        criticalExtensions.remove(KEY_USAGE);
                        criticalExtensions.remove(CERTIFICATE_POLICIES);
                        criticalExtensions.remove(POLICY_MAPPINGS);
                        criticalExtensions.remove(INHIBIT_ANY_POLICY);
                        criticalExtensions.remove(ISSUING_DISTRIBUTION_POINT);
                        criticalExtensions.remove(DELTA_CRL_INDICATOR);
                        criticalExtensions.remove(POLICY_CONSTRAINTS);
                        criticalExtensions.remove(BASIC_CONSTRAINTS);
                        criticalExtensions.remove(SUBJECT_ALTERNATIVE_NAME);
                        criticalExtensions.remove(NAME_CONSTRAINTS);
                        tmpIter = pathCheckers.iterator();
                        while (tmpIter.hasNext())
                        {
                            try
                            {
                                ((PKIXCertPathChecker)tmpIter.next()).check(cert, criticalExtensions);
                            }
                            catch (CertPathValidatorException e)
                            {
                                throw new CertPathValidatorException(e.getMessage(), e.getCause(), certPath, index);
                            }
                        }
                        if (!criticalExtensions.isEmpty())
                        {
                            throw new CertPathValidatorException(
                                "Certificate has unsupported critical extension", null, certPath, index);
                        }
                    }
                }
                sign = cert;
                workingPublicKey = sign.getPublicKey();
                try
                {
                    workingIssuerName = CertPathValidatorUtilities.getSubjectPrincipal(sign);
                }
                catch (IllegalArgumentException ex)
                {
                    throw new CertPathValidatorException(sign.getSubjectDN().getName() + " :" + ex.toString());
                }
                workingAlgId = CertPathValidatorUtilities.getAlgorithmIdentifier(workingPublicKey);
                workingPublicKeyAlgorithm = workingAlgId.getObjectId();
                workingPublicKeyParameters = workingAlgId.getParameters();
            }
            catch (AnnotatedException e)
            {
                throw new CertPathValidatorException(e.getMessage(), e.getUnderlyingException(), certPath, index);
            }
        }
        if (!CertPathValidatorUtilities.isSelfIssued(cert) && (explicitPolicy != 0))
        {
            explicitPolicy--;
        }
        try
        {
            ASN1Sequence pc = (ASN1Sequence)CertPathValidatorUtilities.getExtensionValue(cert, POLICY_CONSTRAINTS);
            if (pc != null)
            {
                Enumeration policyConstraints = pc.getObjects();
                while (policyConstraints.hasMoreElements())
                {
                    ASN1TaggedObject    constraint = (ASN1TaggedObject)policyConstraints.nextElement();
                    switch (constraint.getTagNo())
                    {
                    case 0:
                        tmpInt = DERInteger.getInstance(constraint).getValue().intValue();
                        if (tmpInt == 0)
                        {
                            explicitPolicy = 0;
                        }
                        break;
                    }
                }
            }
        }
        catch (AnnotatedException e)
        {
            throw new CertPathValidatorException(e.getMessage(), e.getUnderlyingException(), certPath, index);
        }
        Set criticalExtensions = cert.getCriticalExtensionOIDs();
        if (criticalExtensions != null)
        {
            criticalExtensions = new HashSet(criticalExtensions);
            criticalExtensions.remove(KEY_USAGE);
            criticalExtensions.remove(CERTIFICATE_POLICIES);
            criticalExtensions.remove(POLICY_MAPPINGS);
            criticalExtensions.remove(INHIBIT_ANY_POLICY);
            criticalExtensions.remove(ISSUING_DISTRIBUTION_POINT);
            criticalExtensions.remove(DELTA_CRL_INDICATOR);
            criticalExtensions.remove(POLICY_CONSTRAINTS);
            criticalExtensions.remove(BASIC_CONSTRAINTS);
            criticalExtensions.remove(SUBJECT_ALTERNATIVE_NAME);
            criticalExtensions.remove(NAME_CONSTRAINTS);
        }
        else
        {
            criticalExtensions = new HashSet();
        }
        tmpIter = pathCheckers.iterator();
        while (tmpIter.hasNext())
        {
            try
            {
                ((PKIXCertPathChecker)tmpIter.next()).check(cert, criticalExtensions);
            }
            catch (CertPathValidatorException e)
            {
                throw new CertPathValidatorException(e.getMessage(), e.getCause(), certPath, index);
            }
        }
        if (!criticalExtensions.isEmpty())
        {
            throw new CertPathValidatorException(
                "Certificate has unsupported critical extension", null, certPath, index);
        }
        PKIXPolicyNode intersection;
        if (validPolicyTree == null)
        { 
            if (paramsPKIX.isExplicitPolicyRequired())
            {
                throw new CertPathValidatorException("Explicit policy requested but none available.");
            }
            intersection = null;
        }
        else if (CertPathValidatorUtilities.isAnyPolicy(userInitialPolicySet)) 
        {
            if (paramsPKIX.isExplicitPolicyRequired())
            {
                if (acceptablePolicies.isEmpty())
                {
                    throw new CertPathValidatorException("Explicit policy requested but none available.");
                }
                else
                {
                    Set _validPolicyNodeSet = new HashSet();
                    for (int j = 0; j < policyNodes.length; j++)
                    {
                        List      _nodeDepth = policyNodes[j];
                        for (int k = 0; k < _nodeDepth.size(); k++)
                        {
                            PKIXPolicyNode _node = (PKIXPolicyNode)_nodeDepth.get(k);
                            if (ANY_POLICY.equals(_node.getValidPolicy()))
                            {
                                Iterator _iter = _node.getChildren();
                                while (_iter.hasNext())
                                {
                                    _validPolicyNodeSet.add(_iter.next());
                                }
                            }
                        }
                    }
                    Iterator _vpnsIter = _validPolicyNodeSet.iterator();
                    while (_vpnsIter.hasNext())
                    {
                        PKIXPolicyNode _node = (PKIXPolicyNode)_vpnsIter.next();
                        String _validPolicy = _node.getValidPolicy();
                        if (!acceptablePolicies.contains(_validPolicy))
                        {
                        }
                    }
                    if (validPolicyTree != null)
                    {
                        for (int j = (n - 1); j >= 0; j--)
                        {
                            List      nodes = policyNodes[j];
                            for (int k = 0; k < nodes.size(); k++)
                            {
                                PKIXPolicyNode node = (PKIXPolicyNode)nodes.get(k);
                                if (!node.hasChildren())
                                {
                                    validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, node);
                                }
                            }
                        }
                    }
                }
            }
            intersection = validPolicyTree;
        }
        else
        {
            Set _validPolicyNodeSet = new HashSet();
            for (int j = 0; j < policyNodes.length; j++)
            {
                List      _nodeDepth = policyNodes[j];
                for (int k = 0; k < _nodeDepth.size(); k++)
                {
                    PKIXPolicyNode _node = (PKIXPolicyNode)_nodeDepth.get(k);
                    if (ANY_POLICY.equals(_node.getValidPolicy()))
                    {
                        Iterator _iter = _node.getChildren();
                        while (_iter.hasNext())
                        {
                            PKIXPolicyNode _c_node = (PKIXPolicyNode)_iter.next();
                            if (!ANY_POLICY.equals(_c_node.getValidPolicy()))
                            {
                                _validPolicyNodeSet.add(_c_node);
                            }
                        }
                    }
                }
            }
            Iterator _vpnsIter = _validPolicyNodeSet.iterator();
            while (_vpnsIter.hasNext())
            {
                PKIXPolicyNode _node = (PKIXPolicyNode)_vpnsIter.next();
                String _validPolicy = _node.getValidPolicy();
                if (!userInitialPolicySet.contains(_validPolicy))
                {
                    validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, _node);
                }
            }
            if (validPolicyTree != null)
            {
                for (int j = (n - 1); j >= 0; j--)
                {
                    List      nodes = policyNodes[j];
                    for (int k = 0; k < nodes.size(); k++)
                    {
                        PKIXPolicyNode node = (PKIXPolicyNode)nodes.get(k);
                        if (!node.hasChildren())
                        {
                            validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, node);
                        }
                    }
                }
            }
            intersection = validPolicyTree;
        }
        if ((explicitPolicy > 0) || (intersection != null))
        {
            return new PKIXCertPathValidatorResult(trust, intersection, workingPublicKey);
        }
        throw new CertPathValidatorException("Path processing failed on policy.", null, certPath, index);
    }
    private void checkCRLs(PKIXParameters paramsPKIX, X509Certificate cert, Date validDate, X509Certificate sign, PublicKey workingPublicKey) 
        throws AnnotatedException
    {
        X509CRLSelector crlselect;
        crlselect = new X509CRLSelector();
        try
        {
            crlselect.addIssuerName(CertPathValidatorUtilities.getEncodedIssuerPrincipal(cert).getEncoded());
        }
        catch (IOException e)
        {
            throw new AnnotatedException("Cannot extract issuer from certificate: " + e, e);
        }
        crlselect.setCertificateChecking(cert);
        Iterator crl_iter = CertPathValidatorUtilities.findCRLs(crlselect, paramsPKIX.getCertStores()).iterator();
        boolean validCrlFound = false;
        X509CRLEntry crl_entry;
        while (crl_iter.hasNext())
        {
            X509CRL crl = (X509CRL)crl_iter.next();
            if (cert.getNotAfter().after(crl.getThisUpdate()))
            {
                if (crl.getNextUpdate() == null
                    || validDate.before(crl.getNextUpdate())) 
                {
                    validCrlFound = true;
                }
                if (sign != null)
                {
                    boolean[] keyusage = sign.getKeyUsage();
                    if (keyusage != null
                        && (keyusage.length < 7 || !keyusage[CRL_SIGN]))
                    {
                        throw new AnnotatedException(
                            "Issuer certificate keyusage extension does not permit crl signing.\n" + sign);
                    }
                }
                try
                {
                    crl.verify(workingPublicKey, "BC");
                }
                catch (Exception e)
                {
                    throw new AnnotatedException("can't verify CRL: " + e, e);
                }
                crl_entry = crl.getRevokedCertificate(cert.getSerialNumber());
                if (crl_entry != null
                    && !validDate.before(crl_entry.getRevocationDate()))
                {
                    String reason = null;
                    if (crl_entry.hasExtensions())
                    {
                        DEREnumerated reasonCode = DEREnumerated.getInstance(CertPathValidatorUtilities.getExtensionValue(crl_entry, X509Extensions.ReasonCode.getId()));
                        if (reasonCode != null)
                        {
                            reason = crlReasons[reasonCode.getValue().intValue()];
                        }
                    }
                    String message = "Certificate revocation after " + crl_entry.getRevocationDate();
                    if (reason != null)
                    {
                        message += ", reason: " + reason;
                    }
                    throw new AnnotatedException(message);
                }
                DERObject idp = CertPathValidatorUtilities.getExtensionValue(crl, ISSUING_DISTRIBUTION_POINT);
                DERObject dci = CertPathValidatorUtilities.getExtensionValue(crl, DELTA_CRL_INDICATOR);
                if (dci != null)
                {
                    X509CRLSelector baseSelect = new X509CRLSelector();
                    try
                    {
                        baseSelect.addIssuerName(CertPathValidatorUtilities.getIssuerPrincipal(crl).getEncoded());
                    }
                    catch (IOException e)
                    {
                        throw new AnnotatedException("can't extract issuer from certificate: " + e, e);
                    }
                    baseSelect.setMinCRLNumber(((DERInteger)dci).getPositiveValue());
                    baseSelect.setMaxCRLNumber(((DERInteger)CertPathValidatorUtilities.getExtensionValue(crl, CRL_NUMBER)).getPositiveValue().subtract(BigInteger.valueOf(1)));
                    boolean  foundBase = false;
                    Iterator it  = CertPathValidatorUtilities.findCRLs(baseSelect, paramsPKIX.getCertStores()).iterator();
                    while (it.hasNext())
                    {
                        X509CRL base = (X509CRL)it.next();
                        DERObject baseIdp = CertPathValidatorUtilities.getExtensionValue(base, ISSUING_DISTRIBUTION_POINT);
                        if (idp == null)
                        {
                            if (baseIdp == null)
                            {
                                foundBase = true;
                                break;
                            }
                        }
                        else
                        {
                            if (idp.equals(baseIdp))
                            {
                                foundBase = true;
                                break;
                            }
                        }
                    }
                    if (!foundBase)
                    {
                        throw new AnnotatedException("No base CRL for delta CRL");
                    }
                }
                if (idp != null)
                {
                    IssuingDistributionPoint    p = IssuingDistributionPoint.getInstance(idp);
                    BasicConstraints    bc = BasicConstraints.getInstance(CertPathValidatorUtilities.getExtensionValue(cert, BASIC_CONSTRAINTS));
                    if (p.onlyContainsUserCerts() && (bc != null && bc.isCA()))
                    {
                        throw new AnnotatedException("CA Cert CRL only contains user certificates");
                    }
                    if (p.onlyContainsCACerts() && (bc == null || !bc.isCA()))
                    {
                        throw new AnnotatedException("End CRL only contains CA certificates");
                    }
                    if (p.onlyContainsAttributeCerts())
                    {
                        throw new AnnotatedException("onlyContainsAttributeCerts boolean is asserted");
                    }
                }
            }
        }
        if (!validCrlFound)
        {
            throw new AnnotatedException("no valid CRL found");
        }
    }
}
