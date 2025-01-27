public class Krb5InitCredential
    extends KerberosTicket
    implements Krb5CredElement {
    private static final long serialVersionUID = 7723415700837898232L;
    private Krb5NameElement name;
    private Credentials krb5Credentials;
    private Krb5InitCredential(Krb5NameElement name,
                               byte[] asn1Encoding,
                               KerberosPrincipal client,
                               KerberosPrincipal server,
                               byte[] sessionKey,
                               int keyType,
                               boolean[] flags,
                               Date authTime,
                               Date startTime,
                               Date endTime,
                               Date renewTill,
                               InetAddress[] clientAddresses)
                               throws GSSException {
        super(asn1Encoding,
              client,
              server,
              sessionKey,
              keyType,
              flags,
              authTime,
              startTime,
              endTime,
              renewTill,
              clientAddresses);
        this.name = name;
        try {
            krb5Credentials = new Credentials(asn1Encoding,
                                              client.getName(),
                                              server.getName(),
                                              sessionKey,
                                              keyType,
                                              flags,
                                              authTime,
                                              startTime,
                                              endTime,
                                              renewTill,
                                              clientAddresses);
        } catch (KrbException e) {
            throw new GSSException(GSSException.NO_CRED, -1,
                                   e.getMessage());
        } catch (IOException e) {
            throw new GSSException(GSSException.NO_CRED, -1,
                                   e.getMessage());
        }
    }
    private Krb5InitCredential(Krb5NameElement name,
                               Credentials delegatedCred,
                               byte[] asn1Encoding,
                               KerberosPrincipal client,
                               KerberosPrincipal server,
                               byte[] sessionKey,
                               int keyType,
                               boolean[] flags,
                               Date authTime,
                               Date startTime,
                               Date endTime,
                               Date renewTill,
                               InetAddress[] clientAddresses)
                               throws GSSException {
        super(asn1Encoding,
              client,
              server,
              sessionKey,
              keyType,
              flags,
              authTime,
              startTime,
              endTime,
              renewTill,
              clientAddresses);
        this.name = name;
        this.krb5Credentials = delegatedCred;
    }
    static Krb5InitCredential getInstance(GSSCaller caller, Krb5NameElement name,
                                   int initLifetime)
        throws GSSException {
        KerberosTicket tgt = getTgt(caller, name, initLifetime);
        if (tgt == null)
            throw new GSSException(GSSException.NO_CRED, -1,
                                   "Failed to find any Kerberos tgt");
        if (name == null) {
            String fullName = tgt.getClient().getName();
            name = Krb5NameElement.getInstance(fullName,
                                       Krb5MechFactory.NT_GSS_KRB5_PRINCIPAL);
        }
        return new Krb5InitCredential(name,
                                      tgt.getEncoded(),
                                      tgt.getClient(),
                                      tgt.getServer(),
                                      tgt.getSessionKey().getEncoded(),
                                      tgt.getSessionKeyType(),
                                      tgt.getFlags(),
                                      tgt.getAuthTime(),
                                      tgt.getStartTime(),
                                      tgt.getEndTime(),
                                      tgt.getRenewTill(),
                                      tgt.getClientAddresses());
    }
    static Krb5InitCredential getInstance(Krb5NameElement name,
                                   Credentials delegatedCred)
        throws GSSException {
        EncryptionKey sessionKey = delegatedCred.getSessionKey();
        PrincipalName cPrinc = delegatedCred.getClient();
        PrincipalName sPrinc = delegatedCred.getServer();
        KerberosPrincipal client = null;
        KerberosPrincipal server = null;
        Krb5NameElement credName = null;
        if (cPrinc != null) {
            String fullName = cPrinc.getName();
            credName = Krb5NameElement.getInstance(fullName,
                               Krb5MechFactory.NT_GSS_KRB5_PRINCIPAL);
            client =  new KerberosPrincipal(fullName);
        }
        if (sPrinc != null) {
            server =
                new KerberosPrincipal(sPrinc.getName(),
                                        KerberosPrincipal.KRB_NT_SRV_INST);
        }
        return new Krb5InitCredential(credName,
                                      delegatedCred,
                                      delegatedCred.getEncoded(),
                                      client,
                                      server,
                                      sessionKey.getBytes(),
                                      sessionKey.getEType(),
                                      delegatedCred.getFlags(),
                                      delegatedCred.getAuthTime(),
                                      delegatedCred.getStartTime(),
                                      delegatedCred.getEndTime(),
                                      delegatedCred.getRenewTill(),
                                      delegatedCred.getClientAddresses());
    }
    public final GSSNameSpi getName() throws GSSException {
        return name;
    }
    public int getInitLifetime() throws GSSException {
        int retVal = 0;
        retVal = (int)(getEndTime().getTime()
                       - (new Date().getTime()));
        return retVal/1000;
    }
    public int getAcceptLifetime() throws GSSException {
        return 0;
    }
    public boolean isInitiatorCredential() throws GSSException {
        return true;
    }
    public boolean isAcceptorCredential() throws GSSException {
        return false;
    }
    public final Oid getMechanism() {
        return Krb5MechFactory.GSS_KRB5_MECH_OID;
    }
    public final java.security.Provider getProvider() {
        return Krb5MechFactory.PROVIDER;
    }
    Credentials getKrb5Credentials() {
        return krb5Credentials;
    }
    public void dispose() throws GSSException {
        try {
            destroy();
        } catch (javax.security.auth.DestroyFailedException e) {
            GSSException gssException =
                new GSSException(GSSException.FAILURE, -1,
                 "Could not destroy credentials - " + e.getMessage());
            gssException.initCause(e);
        }
    }
    private static KerberosTicket getTgt(GSSCaller caller, Krb5NameElement name,
                                                 int initLifetime)
        throws GSSException {
        String realm = null;
        final String clientPrincipal, tgsPrincipal = null;
        if (name != null) {
            clientPrincipal = (name.getKrb5PrincipalName()).getName();
            realm = (name.getKrb5PrincipalName()).getRealmAsString();
        } else {
            clientPrincipal = null;
            try {
                Config config = Config.getInstance();
                realm = config.getDefaultRealm();
            } catch (KrbException e) {
                GSSException ge =
                        new GSSException(GSSException.NO_CRED, -1,
                            "Attempt to obtain INITIATE credentials failed!" +
                            " (" + e.getMessage() + ")");
                ge.initCause(e);
                throw ge;
            }
        }
        final AccessControlContext acc = AccessController.getContext();
        try {
            final GSSCaller realCaller = (caller == GSSCaller.CALLER_UNKNOWN)
                                   ? GSSCaller.CALLER_INITIATE
                                   : caller;
            return AccessController.doPrivileged(
                new PrivilegedExceptionAction<KerberosTicket>() {
                public KerberosTicket run() throws Exception {
                    return Krb5Util.getTicket(
                        realCaller,
                        clientPrincipal, tgsPrincipal, acc);
                        }});
        } catch (PrivilegedActionException e) {
            GSSException ge =
                new GSSException(GSSException.NO_CRED, -1,
                    "Attempt to obtain new INITIATE credentials failed!" +
                    " (" + e.getMessage() + ")");
            ge.initCause(e.getException());
            throw ge;
        }
    }
}
