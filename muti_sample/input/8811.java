public abstract class ObjectKeyTemplateBase implements ObjectKeyTemplate
{
    public static final String JIDL_ORB_ID = "" ;
    private static final String[] JIDL_OAID_STRINGS = { "TransientObjectAdapter" } ;
    public static final ObjectAdapterId JIDL_OAID = new ObjectAdapterIdArray( JIDL_OAID_STRINGS ) ;
    private ORB orb ;
    protected IORSystemException wrapper ;
    private ORBVersion version ;
    private int magic ;
    private int scid ;
    private int serverid ;
    private String orbid ;
    private ObjectAdapterId oaid ;
    private byte[] adapterId ;
    public byte[] getAdapterId()
    {
        return (byte[])(adapterId.clone()) ;
    }
    private byte[] computeAdapterId()
    {
        ByteBuffer buff = new ByteBuffer() ;
        buff.append( getServerId() ) ;
        buff.append( orbid ) ;
        buff.append( oaid.getNumLevels() ) ;
        Iterator iter = oaid.iterator() ;
        while (iter.hasNext()) {
            String comp = (String)(iter.next()) ;
            buff.append( comp ) ;
        }
        buff.trimToSize() ;
        return buff.toArray() ;
    }
    public ObjectKeyTemplateBase( ORB orb, int magic, int scid, int serverid,
        String orbid, ObjectAdapterId oaid )
    {
        this.orb = orb ;
        this.wrapper = IORSystemException.get( orb,
            CORBALogDomains.OA_IOR ) ;
        this.magic = magic ;
        this.scid = scid ;
        this.serverid = serverid ;
        this.orbid = orbid ;
        this.oaid = oaid ;
        adapterId = computeAdapterId() ;
    }
    public boolean equals( Object obj )
    {
        if (!(obj instanceof ObjectKeyTemplateBase))
            return false ;
        ObjectKeyTemplateBase other = (ObjectKeyTemplateBase)obj ;
        return (magic == other.magic) && (scid == other.scid) &&
            (serverid == other.serverid) && (version.equals( other.version ) &&
            orbid.equals( other.orbid ) && oaid.equals( other.oaid )) ;
    }
    public int hashCode()
    {
        int result = 17 ;
        result = 37*result + magic ;
        result = 37*result + scid ;
        result = 37*result + serverid ;
        result = 37*result + version.hashCode() ;
        result = 37*result + orbid.hashCode() ;
        result = 37*result + oaid.hashCode() ;
        return result ;
    }
    public int getSubcontractId()
    {
        return scid ;
    }
    public int getServerId()
    {
        return serverid ;
    }
    public String getORBId()
    {
        return orbid ;
    }
    public ObjectAdapterId getObjectAdapterId()
    {
        return oaid ;
    }
    public void write(ObjectId objectId, OutputStream os)
    {
        writeTemplate( os ) ;
        objectId.write( os ) ;
    }
    public void write( OutputStream os )
    {
        writeTemplate( os ) ;
    }
    abstract protected void writeTemplate( OutputStream os ) ;
    protected int getMagic()
    {
        return magic ;
    }
    public void setORBVersion( ORBVersion version )
    {
        this.version = version ;
    }
    public ORBVersion getORBVersion()
    {
        return version ;
    }
    protected byte[] readObjectKey( InputStream is )
    {
        int len = is.read_long() ;
        byte[] result = new byte[len] ;
        is.read_octet_array( result, 0, len ) ;
        return result ;
    }
    public CorbaServerRequestDispatcher getServerRequestDispatcher( ORB orb, ObjectId id )
    {
        return orb.getRequestDispatcherRegistry().getServerRequestDispatcher( scid ) ;
    }
}
