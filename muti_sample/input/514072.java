public class TypeOfBiometricData  
    extends ASN1Encodable
    implements ASN1Choice
{
    public static final int PICTURE                     = 0;
    public static final int HANDWRITTEN_SIGNATURE       = 1;
    DEREncodable      obj;
    public static TypeOfBiometricData getInstance(Object obj)
    {
        if (obj == null || obj instanceof TypeOfBiometricData)
        {
            return (TypeOfBiometricData)obj;
        }
        if (obj instanceof DERInteger)
        {
            DERInteger predefinedBiometricTypeObj = DERInteger.getInstance(obj);
            int  predefinedBiometricType = predefinedBiometricTypeObj.getValue().intValue();
            return new TypeOfBiometricData(predefinedBiometricType);
        }
        else if (obj instanceof DERObjectIdentifier)
        {
            DERObjectIdentifier BiometricDataID = DERObjectIdentifier.getInstance(obj);
            return new TypeOfBiometricData(BiometricDataID);
        }
        throw new IllegalArgumentException("unknown object in getInstance");
    }
    public TypeOfBiometricData(int predefinedBiometricType)
    {
        if (predefinedBiometricType == PICTURE || predefinedBiometricType == HANDWRITTEN_SIGNATURE)
        {
                obj = new DERInteger(predefinedBiometricType);
        }
        else
        {
            throw new IllegalArgumentException("unknow PredefinedBiometricType : " + predefinedBiometricType);
        }        
    }
    public TypeOfBiometricData(DERObjectIdentifier BiometricDataID)
    {
        obj = BiometricDataID;
    }
    public boolean isPredefined()
    {
        return obj instanceof DERInteger;
    }
    public int getPredefinedBiometricType()
    {
        return ((DERInteger)obj).getValue().intValue();
    }
    public DERObjectIdentifier getBiometricDataOid()
    {
        return (DERObjectIdentifier)obj;
    }
    public DERObject toASN1Object() 
    {        
        return obj.getDERObject();
    }
}
