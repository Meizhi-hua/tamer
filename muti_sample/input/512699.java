public class LDSSecurityObject 
    extends ASN1Encodable 
    implements ICAOObjectIdentifiers    
{
    public static final int ub_DataGroups = 16;
    DERInteger version = new DERInteger(0);
    AlgorithmIdentifier digestAlgorithmIdentifier; 
    DataGroupHash[] datagroupHash;            
    public static LDSSecurityObject getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof LDSSecurityObject)
        {
            return (LDSSecurityObject)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new LDSSecurityObject(ASN1Sequence.getInstance(obj));            
        }
        throw new IllegalArgumentException("unknown object in getInstance");
    }    
    public LDSSecurityObject(
        ASN1Sequence seq)
    {
        if (seq == null || seq.size() == 0)
        {
            throw new IllegalArgumentException("null or empty sequence passed.");
        }
        Enumeration e = seq.getObjects();
        version = DERInteger.getInstance(e.nextElement());
        digestAlgorithmIdentifier = AlgorithmIdentifier.getInstance(e.nextElement());
        ASN1Sequence datagroupHashSeq = ASN1Sequence.getInstance(e.nextElement());
        checkDatagroupHashSeqSize(datagroupHashSeq.size());        
        datagroupHash = new DataGroupHash[datagroupHashSeq.size()];
        for (int i= 0; i< datagroupHashSeq.size();i++)
        {
            datagroupHash[i] = DataGroupHash.getInstance(datagroupHashSeq.getObjectAt(i));
        } 
    }
    public LDSSecurityObject(
        AlgorithmIdentifier digestAlgorithmIdentifier, 
        DataGroupHash[]       datagroupHash)
    {
        this.digestAlgorithmIdentifier = digestAlgorithmIdentifier;
        this.datagroupHash = datagroupHash;
        checkDatagroupHashSeqSize(datagroupHash.length);                      
    }    
    private void checkDatagroupHashSeqSize(int size)
    {
        if ((size < 2) || (size > ub_DataGroups))
        {
               throw new IllegalArgumentException("wrong size in DataGroupHashValues : not in (2.."+ ub_DataGroups +")");
        }
    }  
    public AlgorithmIdentifier getDigestAlgorithmIdentifier()
    {
        return digestAlgorithmIdentifier;
    }
    public DataGroupHash[] getDatagroupHash()
    {
        return datagroupHash;
    }
    public DERObject toASN1Object() 
    {
        ASN1EncodableVector seq = new ASN1EncodableVector();
        seq.add(version);
        seq.add(digestAlgorithmIdentifier);
        ASN1EncodableVector seqname = new ASN1EncodableVector();
        for (int i = 0; i < datagroupHash.length; i++) 
        {
            seqname.add(datagroupHash[i]);
        }            
        seq.add(new DERSequence(seqname));                   
        return new DERSequence(seq);
    }          
}
