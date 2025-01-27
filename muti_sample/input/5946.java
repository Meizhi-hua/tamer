public final class NativeGSSFactory implements MechanismFactory {
    GSSLibStub cStub = null;
    private final GSSCaller caller;
    private GSSCredElement getCredFromSubject(GSSNameElement name,
                                              boolean initiate)
        throws GSSException {
        Oid mech = cStub.getMech();
        Vector<GSSCredElement> creds = GSSUtil.searchSubject
            (name, mech, initiate, GSSCredElement.class);
        if (creds != null && creds.isEmpty()) {
            if (GSSUtil.useSubjectCredsOnly(caller)) {
                throw new GSSException(GSSException.NO_CRED);
            }
        }
        GSSCredElement result = ((creds == null || creds.isEmpty()) ?
                                 null : creds.firstElement());
        if (result != null) {
            result.doServicePermCheck();
        }
        return result;
    }
    public NativeGSSFactory(GSSCaller caller) {
        this.caller = caller;
    }
    public void setMech(Oid mech) throws GSSException {
        cStub = GSSLibStub.getInstance(mech);
    }
    public GSSNameSpi getNameElement(String nameStr, Oid nameType)
        throws GSSException {
        try {
            byte[] nameBytes =
                (nameStr == null ? null : nameStr.getBytes("UTF-8"));
            return new GSSNameElement(nameBytes, nameType, cStub);
        } catch (UnsupportedEncodingException uee) {
            throw new GSSExceptionImpl(GSSException.FAILURE, uee);
        }
    }
    public GSSNameSpi getNameElement(byte[] name, Oid nameType)
        throws GSSException {
        return new GSSNameElement(name, nameType, cStub);
    }
    public GSSCredentialSpi getCredentialElement(GSSNameSpi name,
                                                 int initLifetime,
                                                 int acceptLifetime,
                                                 int usage)
        throws GSSException {
        GSSNameElement nname = null;
        if (name != null && !(name instanceof GSSNameElement)) {
            nname = (GSSNameElement)
                getNameElement(name.toString(), name.getStringNameType());
        } else nname = (GSSNameElement) name;
        if (usage == GSSCredential.INITIATE_AND_ACCEPT) {
            usage = GSSCredential.INITIATE_ONLY;
        }
        GSSCredElement credElement =
            getCredFromSubject((GSSNameElement) nname,
                               (usage == GSSCredential.INITIATE_ONLY));
        if (credElement == null) {
            if (usage == GSSCredential.INITIATE_ONLY) {
                credElement = new GSSCredElement(nname, initLifetime,
                                                 usage, cStub);
            } else if (usage == GSSCredential.ACCEPT_ONLY) {
                if (nname == null) {
                    nname = GSSNameElement.DEF_ACCEPTOR;
                }
                credElement = new GSSCredElement(nname, acceptLifetime,
                                                 usage, cStub);
            } else {
                throw new GSSException(GSSException.FAILURE, -1,
                                       "Unknown usage mode requested");
            }
        }
        return credElement;
    }
    public GSSContextSpi getMechanismContext(GSSNameSpi peer,
                                             GSSCredentialSpi myCred,
                                             int lifetime)
        throws GSSException {
        if (peer == null) {
            throw new GSSException(GSSException.BAD_NAME);
        } else if (!(peer instanceof GSSNameElement)) {
            peer = (GSSNameElement)
                getNameElement(peer.toString(), peer.getStringNameType());
        }
        if (myCred == null) {
            myCred = getCredFromSubject(null, true);
        } else if (!(myCred instanceof GSSCredElement)) {
            throw new GSSException(GSSException.NO_CRED);
        }
        return new NativeGSSContext((GSSNameElement) peer,
                                     (GSSCredElement) myCred,
                                     lifetime, cStub);
    }
    public GSSContextSpi getMechanismContext(GSSCredentialSpi myCred)
        throws GSSException {
        if (myCred == null) {
            myCred = getCredFromSubject(null, false);
        } else if (!(myCred instanceof GSSCredElement)) {
            throw new GSSException(GSSException.NO_CRED);
        }
        return new NativeGSSContext((GSSCredElement) myCred, cStub);
    }
    public GSSContextSpi getMechanismContext(byte[] exportedContext)
        throws GSSException {
        return cStub.importContext(exportedContext);
    }
    public final Oid getMechanismOid() {
        return cStub.getMech();
    }
    public Provider getProvider() {
        return SunNativeProvider.INSTANCE;
    }
    public Oid[] getNameTypes() throws GSSException {
        return cStub.inquireNamesForMech();
    }
}
