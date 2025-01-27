public class GSSCredentialImpl implements GSSCredential {
    private GSSManagerImpl gssManager = null;
    private boolean destroyed = false;
    private Hashtable<SearchKey, GSSCredentialSpi> hashtable = null;
    private GSSCredentialSpi tempCred = null;
    GSSCredentialImpl(GSSManagerImpl gssManager, int usage)
        throws GSSException {
        this(gssManager, null, GSSCredential.DEFAULT_LIFETIME,
             (Oid[]) null, usage);
    }
    GSSCredentialImpl(GSSManagerImpl gssManager, GSSName name,
                             int lifetime, Oid mech, int usage)
        throws GSSException {
        if (mech == null) mech = ProviderList.DEFAULT_MECH_OID;
        init(gssManager);
        add(name, lifetime, lifetime, mech, usage);
    }
    GSSCredentialImpl(GSSManagerImpl gssManager, GSSName name,
                      int lifetime, Oid mechs[], int usage)
        throws GSSException {
        init(gssManager);
        boolean defaultList = false;
        if (mechs == null) {
            mechs = gssManager.getMechs();
            defaultList = true;
        }
        for (int i = 0; i < mechs.length; i++) {
            try {
                add(name, lifetime, lifetime, mechs[i], usage);
            } catch (GSSException e) {
                if (defaultList) {
                    GSSUtil.debug("Ignore " + e + " while acquring cred for "
                        + mechs[i]);
                } else throw e; 
            }
        }
        if ((hashtable.size() == 0) || (usage != getUsage()))
            throw new GSSException(GSSException.NO_CRED);
    }
    public GSSCredentialImpl(GSSManagerImpl gssManager,
                      GSSCredentialSpi mechElement) throws GSSException {
        init(gssManager);
        int usage = GSSCredential.ACCEPT_ONLY;
        if (mechElement.isInitiatorCredential()) {
            if (mechElement.isAcceptorCredential()) {
                usage = GSSCredential.INITIATE_AND_ACCEPT;
            } else {
                usage = GSSCredential.INITIATE_ONLY;
            }
        }
        SearchKey key = new SearchKey(mechElement.getMechanism(),
                                        usage);
        tempCred = mechElement;
        hashtable.put(key, tempCred);
    }
    void init(GSSManagerImpl gssManager) {
        this.gssManager = gssManager;
        hashtable = new Hashtable<SearchKey, GSSCredentialSpi>(
                                                gssManager.getMechs().length);
    }
    public void dispose() throws GSSException {
        if (!destroyed) {
            GSSCredentialSpi element;
            Enumeration<GSSCredentialSpi> values = hashtable.elements();
            while (values.hasMoreElements()) {
                element = values.nextElement();
                element.dispose();
            }
            destroyed = true;
        }
    }
    public GSSName getName() throws GSSException {
        if (destroyed) {
            throw new IllegalStateException("This credential is " +
                                        "no longer valid");
        }
        return GSSNameImpl.wrapElement(gssManager, tempCred.getName());
    }
    public GSSName getName(Oid mech) throws GSSException {
        if (destroyed) {
            throw new IllegalStateException("This credential is " +
                                        "no longer valid");
        }
        SearchKey key = null;
        GSSCredentialSpi element = null;
        if (mech == null) mech = ProviderList.DEFAULT_MECH_OID;
        key = new SearchKey(mech, GSSCredential.INITIATE_ONLY);
        element = hashtable.get(key);
        if (element == null) {
            key = new SearchKey(mech, GSSCredential.ACCEPT_ONLY);
            element = hashtable.get(key);
        }
        if (element == null) {
            key = new SearchKey(mech, GSSCredential.INITIATE_AND_ACCEPT);
            element = hashtable.get(key);
        }
        if (element == null) {
            throw new GSSExceptionImpl(GSSException.BAD_MECH, mech);
        }
        return GSSNameImpl.wrapElement(gssManager, element.getName());
    }
    public int getRemainingLifetime() throws GSSException {
        if (destroyed) {
            throw new IllegalStateException("This credential is " +
                                        "no longer valid");
        }
        SearchKey tempKey;
        GSSCredentialSpi tempCred;
        int tempLife = 0, tempInitLife = 0, tempAcceptLife = 0;
        int min = INDEFINITE_LIFETIME;
        for (Enumeration<SearchKey> e = hashtable.keys();
                                        e.hasMoreElements(); ) {
            tempKey = e.nextElement();
            tempCred = hashtable.get(tempKey);
            if (tempKey.getUsage() == INITIATE_ONLY)
                tempLife = tempCred.getInitLifetime();
            else if (tempKey.getUsage() == ACCEPT_ONLY)
                tempLife = tempCred.getAcceptLifetime();
            else {
                tempInitLife = tempCred.getInitLifetime();
                tempAcceptLife = tempCred.getAcceptLifetime();
                tempLife = (tempInitLife < tempAcceptLife ?
                            tempInitLife:
                            tempAcceptLife);
            }
            if (min > tempLife)
                min = tempLife;
        }
        return min;
    }
    public int getRemainingInitLifetime(Oid mech) throws GSSException {
        if (destroyed) {
            throw new IllegalStateException("This credential is " +
                                        "no longer valid");
        }
        GSSCredentialSpi element = null;
        SearchKey key = null;
        boolean found = false;
        int max = 0;
        if (mech == null) mech = ProviderList.DEFAULT_MECH_OID;
        key = new SearchKey(mech, GSSCredential.INITIATE_ONLY);
        element = hashtable.get(key);
        if (element != null) {
            found = true;
            if (max < element.getInitLifetime())
                max = element.getInitLifetime();
        }
        key = new SearchKey(mech, GSSCredential.INITIATE_AND_ACCEPT);
        element = hashtable.get(key);
        if (element != null) {
            found = true;
            if (max < element.getInitLifetime())
                max = element.getInitLifetime();
        }
        if (!found) {
            throw new GSSExceptionImpl(GSSException.BAD_MECH, mech);
        }
        return max;
    }
    public int getRemainingAcceptLifetime(Oid mech) throws GSSException {
        if (destroyed) {
            throw new IllegalStateException("This credential is " +
                                        "no longer valid");
        }
        GSSCredentialSpi element = null;
        SearchKey key = null;
        boolean found = false;
        int max = 0;
        if (mech == null) mech = ProviderList.DEFAULT_MECH_OID;
        key = new SearchKey(mech, GSSCredential.ACCEPT_ONLY);
        element = hashtable.get(key);
        if (element != null) {
            found = true;
            if (max < element.getAcceptLifetime())
                max = element.getAcceptLifetime();
        }
        key = new SearchKey(mech, GSSCredential.INITIATE_AND_ACCEPT);
        element = hashtable.get(key);
        if (element != null) {
            found = true;
            if (max < element.getAcceptLifetime())
                max = element.getAcceptLifetime();
        }
        if (!found) {
            throw new GSSExceptionImpl(GSSException.BAD_MECH, mech);
        }
        return max;
    }
    public int getUsage() throws GSSException {
        if (destroyed) {
            throw new IllegalStateException("This credential is " +
                                        "no longer valid");
        }
        SearchKey tempKey;
        boolean initiate = false;
        boolean accept = false;
        for (Enumeration<SearchKey> e = hashtable.keys();
                                        e.hasMoreElements(); ) {
            tempKey = e.nextElement();
            if (tempKey.getUsage() == INITIATE_ONLY)
                initiate = true;
            else if (tempKey.getUsage() == ACCEPT_ONLY)
                accept = true;
            else
                return INITIATE_AND_ACCEPT;
        }
        if (initiate) {
            if (accept)
                return INITIATE_AND_ACCEPT;
            else
                return INITIATE_ONLY;
        } else
            return ACCEPT_ONLY;
    }
    public int getUsage(Oid mech) throws GSSException {
        if (destroyed) {
            throw new IllegalStateException("This credential is " +
                                        "no longer valid");
        }
        GSSCredentialSpi element = null;
        SearchKey key = null;
        boolean initiate = false;
        boolean accept = false;
        if (mech == null) mech = ProviderList.DEFAULT_MECH_OID;
        key = new SearchKey(mech, GSSCredential.INITIATE_ONLY);
        element = hashtable.get(key);
        if (element != null) {
            initiate = true;
        }
        key = new SearchKey(mech, GSSCredential.ACCEPT_ONLY);
        element = hashtable.get(key);
        if (element != null) {
            accept = true;
        }
        key = new SearchKey(mech, GSSCredential.INITIATE_AND_ACCEPT);
        element = hashtable.get(key);
        if (element != null) {
            initiate = true;
            accept = true;
        }
        if (initiate && accept)
            return GSSCredential.INITIATE_AND_ACCEPT;
        else if (initiate)
            return GSSCredential.INITIATE_ONLY;
        else if (accept)
            return GSSCredential.ACCEPT_ONLY;
        else {
            throw new GSSExceptionImpl(GSSException.BAD_MECH, mech);
        }
    }
    public Oid[] getMechs() throws GSSException {
        if (destroyed) {
            throw new IllegalStateException("This credential is " +
                                        "no longer valid");
        }
        Vector<Oid> result = new Vector<Oid>(hashtable.size());
        for (Enumeration<SearchKey> e = hashtable.keys();
                                                e.hasMoreElements(); ) {
            SearchKey tempKey = e.nextElement();
            result.addElement(tempKey.getMech());
        }
        return result.toArray(new Oid[0]);
    }
    public void add(GSSName name, int initLifetime, int acceptLifetime,
                    Oid mech, int usage) throws GSSException {
        if (destroyed) {
            throw new IllegalStateException("This credential is " +
                                        "no longer valid");
        }
        if (mech == null) mech = ProviderList.DEFAULT_MECH_OID;
        SearchKey key = new SearchKey(mech, usage);
        if (hashtable.containsKey(key)) {
            throw new GSSExceptionImpl(GSSException.DUPLICATE_ELEMENT,
                                       "Duplicate element found: " +
                                       getElementStr(mech, usage));
        }
        GSSNameSpi nameElement = (name == null ? null :
                                  ((GSSNameImpl)name).getElement(mech));
        tempCred = gssManager.getCredentialElement(nameElement,
                                                   initLifetime,
                                                   acceptLifetime,
                                                   mech,
                                                   usage);
        if (tempCred != null) {
            if (usage == GSSCredential.INITIATE_AND_ACCEPT &&
                (!tempCred.isAcceptorCredential() ||
                !tempCred.isInitiatorCredential())) {
                int currentUsage;
                int desiredUsage;
                if (!tempCred.isInitiatorCredential()) {
                    currentUsage = GSSCredential.ACCEPT_ONLY;
                    desiredUsage = GSSCredential.INITIATE_ONLY;
                } else {
                    currentUsage = GSSCredential.INITIATE_ONLY;
                    desiredUsage = GSSCredential.ACCEPT_ONLY;
                }
                key = new SearchKey(mech, currentUsage);
                hashtable.put(key, tempCred);
                tempCred = gssManager.getCredentialElement(nameElement,
                                                        initLifetime,
                                                        acceptLifetime,
                                                        mech,
                                                        desiredUsage);
                key = new SearchKey(mech, desiredUsage);
                hashtable.put(key, tempCred);
            } else {
                hashtable.put(key, tempCred);
            }
        }
    }
    public boolean equals(Object another) {
        if (destroyed) {
            throw new IllegalStateException("This credential is " +
                                        "no longer valid");
        }
        if (this == another) {
            return true;
        }
        if (!(another instanceof GSSCredentialImpl)) {
            return false;
        }
        return false;
    }
    public int hashCode() {
        if (destroyed) {
            throw new IllegalStateException("This credential is " +
                                        "no longer valid");
        }
        return 1;
    }
    public GSSCredentialSpi getElement(Oid mechOid, boolean initiate)
        throws GSSException {
        if (destroyed) {
            throw new IllegalStateException("This credential is " +
                                        "no longer valid");
        }
        SearchKey key;
        GSSCredentialSpi element;
        if (mechOid == null) {
            mechOid = ProviderList.DEFAULT_MECH_OID;
            key = new SearchKey(mechOid,
                                 initiate? INITIATE_ONLY : ACCEPT_ONLY);
            element = hashtable.get(key);
            if (element == null) {
                key = new SearchKey(mechOid, INITIATE_AND_ACCEPT);
                element = hashtable.get(key);
                if (element == null) {
                    Object[] elements = hashtable.entrySet().toArray();
                    for (int i = 0; i < elements.length; i++) {
                        element = (GSSCredentialSpi)
                            ((Map.Entry)elements[i]).getValue();
                        if (element.isInitiatorCredential() == initiate)
                            break;
                    } 
                }
            }
        } else {
            if (initiate)
                key = new SearchKey(mechOid, INITIATE_ONLY);
            else
                key = new SearchKey(mechOid, ACCEPT_ONLY);
            element = hashtable.get(key);
            if (element == null) {
                key = new SearchKey(mechOid, INITIATE_AND_ACCEPT);
                element = hashtable.get(key);
            }
        }
        if (element == null)
            throw new GSSExceptionImpl(GSSException.NO_CRED,
                                       "No credential found for: " +
                                       mechOid + getElementStr(mechOid,
                                       initiate? INITIATE_ONLY : ACCEPT_ONLY));
        return element;
    }
    Set<GSSCredentialSpi> getElements() {
        HashSet<GSSCredentialSpi> retVal =
                        new HashSet<GSSCredentialSpi>(hashtable.size());
        Enumeration<GSSCredentialSpi> values = hashtable.elements();
        while (values.hasMoreElements()) {
            GSSCredentialSpi o = values.nextElement();
            retVal.add(o);
        }
        return retVal;
    }
    private static String getElementStr(Oid mechOid, int usage) {
        String displayString = mechOid.toString();
        if (usage == GSSCredential.INITIATE_ONLY) {
            displayString =
                displayString.concat(" usage: Initiate");
        } else if (usage == GSSCredential.ACCEPT_ONLY) {
            displayString =
                displayString.concat(" usage: Accept");
        } else {
            displayString =
                displayString.concat(" usage: Initiate and Accept");
        }
        return displayString;
    }
    public String toString() {
        if (destroyed) {
            throw new IllegalStateException("This credential is " +
                                        "no longer valid");
        }
        GSSCredentialSpi element = null;
        StringBuffer buffer = new StringBuffer("[GSSCredential: ");
        Object[] elements = hashtable.entrySet().toArray();
        for (int i = 0; i < elements.length; i++) {
            try {
                buffer.append('\n');
                element = (GSSCredentialSpi)
                    ((Map.Entry)elements[i]).getValue();
                buffer.append(element.getName());
                buffer.append(' ');
                buffer.append(element.getMechanism());
                buffer.append(element.isInitiatorCredential() ?
                              " Initiate" : "");
                buffer.append(element.isAcceptorCredential() ?
                              " Accept" : "");
                buffer.append(" [");
                buffer.append(element.toString());
                buffer.append(']');
            } catch (GSSException e) {
            }
        }
        buffer.append(']');
        return buffer.toString();
    }
    static class SearchKey {
        private Oid mechOid = null;
        private int usage = GSSCredential.INITIATE_AND_ACCEPT;
        public SearchKey(Oid mechOid, int usage) {
            this.mechOid = mechOid;
            this.usage = usage;
        }
        public Oid getMech() {
            return mechOid;
        }
        public int getUsage() {
            return usage;
        }
        public boolean equals(Object other) {
            if (! (other instanceof SearchKey))
                return false;
            SearchKey that = (SearchKey) other;
            return ((this.mechOid.equals(that.mechOid)) &&
                    (this.usage == that.usage));
        }
        public int hashCode() {
            return mechOid.hashCode();
        }
    }
}
