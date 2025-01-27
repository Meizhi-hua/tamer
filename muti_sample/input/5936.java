public final class ObjectIdImpl implements ObjectId
{
    private byte[] id;
    public boolean equals( Object obj )
    {
        if (!(obj instanceof ObjectIdImpl))
            return false ;
        ObjectIdImpl other = (ObjectIdImpl)obj ;
        return Arrays.equals( this.id, other.id ) ;
    }
    public int hashCode()
    {
        int result = 17 ;
        for (int ctr=0; ctr<id.length; ctr++)
            result = 37*result + id[ctr] ;
        return result ;
    }
    public ObjectIdImpl( byte[] id )
    {
        this.id = id ;
    }
    public byte[] getId()
    {
        return id ;
    }
    public void write( OutputStream os )
    {
        os.write_long( id.length ) ;
        os.write_octet_array( id, 0, id.length ) ;
    }
}
