public class CertStoreCollectionSpi extends CertStoreSpi
{
    private CollectionCertStoreParameters params;
    public CertStoreCollectionSpi(CertStoreParameters params)
        throws InvalidAlgorithmParameterException
    {
        super(params);
        if (!(params instanceof CollectionCertStoreParameters))
        {
            throw new InvalidAlgorithmParameterException("org.bouncycastle.jce.provider.CertStoreCollectionSpi: parameter must be a CollectionCertStoreParameters object\n" +  params.toString());
        }
        this.params = (CollectionCertStoreParameters)params;
    }
    public Collection engineGetCertificates(
        CertSelector selector)
        throws CertStoreException 
    {
        Set         col = new HashSet();
        Iterator    iter = params.getCollection().iterator();
        if (selector == null)
        {
            while (iter.hasNext())
            {
                Object obj = iter.next();
                if (obj instanceof Certificate)
                {
                    col.add(obj);
                }
            }
        }
        else
        {
            while (iter.hasNext())
            {
                Object obj = iter.next();
                if ((obj instanceof Certificate) && selector.match((Certificate)obj))
                {
                    col.add(obj);
                }
            }
        }
        return col;
    }
    public Collection engineGetCRLs(
        CRLSelector selector)
        throws CertStoreException 
    {
        Set         col = new HashSet();
        Iterator    iter = params.getCollection().iterator();
        if (selector == null)
        {
            while (iter.hasNext())
            {
                Object obj = iter.next();
                if (obj instanceof CRL)
                {
                    col.add(obj);
                }
            }
        }
        else
        {
            while (iter.hasNext())
            {
                Object obj = iter.next();
                if ((obj instanceof CRL) && selector.match((CRL)obj))
                {
                    col.add(obj);
                }
            }
        }
        return col;
    }    
}
