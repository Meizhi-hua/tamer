public class ORBInitRefResolverImpl implements Resolver {
    Operation urlHandler ;
    java.util.Map orbInitRefTable ;
    public ORBInitRefResolverImpl( Operation urlHandler, StringPair[] initRefs )
    {
        this.urlHandler = urlHandler ;
        orbInitRefTable = new java.util.HashMap() ;
        for( int i = 0; i < initRefs.length ; i++ ) {
            StringPair sp = initRefs[i] ;
            orbInitRefTable.put( sp.getFirst(), sp.getSecond() ) ;
        }
    }
    public org.omg.CORBA.Object resolve( String ident )
    {
        String url = (String)orbInitRefTable.get( ident ) ;
        if (url == null)
            return null ;
        org.omg.CORBA.Object result =
            (org.omg.CORBA.Object)urlHandler.operate( url ) ;
        return result ;
    }
    public java.util.Set list()
    {
        return orbInitRefTable.keySet() ;
    }
}
