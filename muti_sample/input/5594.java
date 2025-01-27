public class KDCRep {
    public Realm crealm;
    public PrincipalName cname;
    public Ticket ticket;
    public EncryptedData encPart;
    public EncKDCRepPart encKDCRepPart; 
    private int pvno;
    private int msgType;
    public PAData[] pAData = null; 
    private boolean DEBUG = Krb5.DEBUG;
    public KDCRep(
            PAData[] new_pAData,
            Realm new_crealm,
            PrincipalName new_cname,
            Ticket new_ticket,
            EncryptedData new_encPart,
            int req_type) throws IOException {
        pvno = Krb5.PVNO;
        msgType = req_type;
        if (new_pAData != null) {
            pAData = new PAData[new_pAData.length];
            for (int i = 0; i < new_pAData.length; i++) {
                if (new_pAData[i] == null) {
                    throw new IOException("Cannot create a KDCRep");
                } else {
                    pAData[i] = (PAData) new_pAData[i].clone();
                }
            }
        }
        crealm = new_crealm;
        cname = new_cname;
        ticket = new_ticket;
        encPart = new_encPart;
    }
    public KDCRep() {
    }
    public KDCRep(byte[] data, int req_type) throws Asn1Exception,
            KrbApErrException, RealmException, IOException {
        init(new DerValue(data), req_type);
    }
    public KDCRep(DerValue encoding, int req_type) throws Asn1Exception,
            RealmException, KrbApErrException, IOException {
        init(encoding, req_type);
    }
    protected void init(DerValue encoding, int req_type)
            throws Asn1Exception, RealmException, IOException,
            KrbApErrException {
        DerValue der, subDer;
        if ((encoding.getTag() & 0x1F) != req_type) {
            if (DEBUG) {
                System.out.println(">>> KDCRep: init() " +
                        "encoding tag is " +
                        encoding.getTag() +
                        " req type is " + req_type);
            }
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if (der.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        subDer = der.getData().getDerValue();
        if ((subDer.getTag() & 0x1F) == 0x00) {
            pvno = subDer.getData().getBigInteger().intValue();
            if (pvno != Krb5.PVNO) {
                throw new KrbApErrException(Krb5.KRB_AP_ERR_BADVERSION);
            }
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        subDer = der.getData().getDerValue();
        if ((subDer.getTag() & 0x1F) == 0x01) {
            msgType = subDer.getData().getBigInteger().intValue();
            if (msgType != req_type) {
                throw new KrbApErrException(Krb5.KRB_AP_ERR_MSG_TYPE);
            }
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        if ((der.getData().peekByte() & 0x1F) == 0x02) {
            subDer = der.getData().getDerValue();
            DerValue[] padata = subDer.getData().getSequence(1);
            pAData = new PAData[padata.length];
            for (int i = 0; i < padata.length; i++) {
                pAData[i] = new PAData(padata[i]);
            }
        } else {
            pAData = null;
        }
        crealm = Realm.parse(der.getData(), (byte) 0x03, false);
        cname = PrincipalName.parse(der.getData(), (byte) 0x04, false);
        ticket = Ticket.parse(der.getData(), (byte) 0x05, false);
        encPart = EncryptedData.parse(der.getData(), (byte) 0x06, false);
        if (der.getData().available() > 0) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();
        temp.putInteger(BigInteger.valueOf(pvno));
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x00), temp);
        temp = new DerOutputStream();
        temp.putInteger(BigInteger.valueOf(msgType));
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x01), temp);
        if (pAData != null && pAData.length > 0) {
            DerOutputStream padata_stream = new DerOutputStream();
            for (int i = 0; i < pAData.length; i++) {
                padata_stream.write(pAData[i].asn1Encode());
            }
            temp = new DerOutputStream();
            temp.write(DerValue.tag_SequenceOf, padata_stream);
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x02), temp);
        }
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x03), crealm.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x04), cname.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x05), ticket.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x06), encPart.asn1Encode());
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        return temp.toByteArray();
    }
}
