public class CCacheOutputStream extends KrbDataOutputStream implements FileCCacheConstants {
    public CCacheOutputStream(OutputStream os) {
        super(os);
    }
    public void writeHeader(PrincipalName p, int version) throws IOException {
        write((version & 0xff00) >> 8);
        write(version & 0x00ff);
        p.writePrincipal(this);
    }
    public void addCreds(Credentials creds) throws IOException, Asn1Exception {
        creds.cname.writePrincipal(this);
        creds.sname.writePrincipal(this);
        creds.key.writeKey(this);
        write32((int)(creds.authtime.getTime()/1000));
        if (creds.starttime != null)
            write32((int)(creds.starttime.getTime()/1000));
        else write32(0);
        write32((int)(creds.endtime.getTime()/1000));
        if (creds.renewTill != null)
            write32((int)(creds.renewTill.getTime()/1000));
        else write32(0);
        if (creds.isEncInSKey) {
            write8(1);
        }
        else write8(0);
        writeFlags(creds.flags);
        if (creds.caddr == null)
            write32(0);
        else
            creds.caddr.writeAddrs(this);
        if (creds.authorizationData == null) {
            write32(0);
        }
        else
            creds.authorizationData.writeAuth(this);
        writeTicket(creds.ticket);
        writeTicket(creds.secondTicket);
    }
    void writeTicket(Ticket t) throws IOException, Asn1Exception {
        if (t == null) {
            write32(0);
        }
        else {
            byte[] bytes = t.asn1Encode();
            write32(bytes.length);
            write(bytes, 0, bytes.length);
        }
    }
    void writeFlags(TicketFlags flags) throws IOException {
        int tFlags = 0;
        boolean[] f = flags.toBooleanArray();
        if (f[1] == true) {
            tFlags |= TKT_FLG_FORWARDABLE;
        }
        if (f[2] == true) {
            tFlags |= TKT_FLG_FORWARDED;
        }
        if (f[3] == true) {
            tFlags |= TKT_FLG_PROXIABLE;
        }
        if (f[4] == true) {
            tFlags |= TKT_FLG_PROXY;
        }
        if (f[5] == true) {
            tFlags |= TKT_FLG_MAY_POSTDATE;
        }
        if (f[6] == true) {
            tFlags |= TKT_FLG_POSTDATED;
        }
        if (f[7] == true) {
            tFlags |= TKT_FLG_INVALID;
        }
        if (f[8] == true) {
            tFlags |= TKT_FLG_RENEWABLE;
        }
        if (f[9] == true) {
            tFlags |= TKT_FLG_INITIAL;
        }
        if (f[10] == true) {
            tFlags |= TKT_FLG_PRE_AUTH;
        }
        if (f[11] == true) {
            tFlags |= TKT_FLG_HW_AUTH;
        }
        write32(tFlags);
    }
}
