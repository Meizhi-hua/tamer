public class JAXPVariableStack extends VariableStack {
    private final XPathVariableResolver resolver;
    public JAXPVariableStack(XPathVariableResolver resolver) {
        super(2);
        this.resolver = resolver;
    }
    public XObject getVariableOrParam(XPathContext xctxt, QName qname)
        throws TransformerException,IllegalArgumentException {
        if ( qname == null ) {
            String fmsg = XSLMessages.createXPATHMessage( 
                XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                new Object[] {"Variable qname"} );
            throw new IllegalArgumentException( fmsg );
        }
	javax.xml.namespace.QName name =
	    new javax.xml.namespace.QName(
                qname.getNamespace(),
                qname.getLocalPart());
        Object varValue = resolver.resolveVariable( name );		
        if ( varValue == null ) {
            String fmsg = XSLMessages.createXPATHMessage( 
                XPATHErrorResources.ER_RESOLVE_VARIABLE_RETURNS_NULL,
                new Object[] { name.toString()} );
            throw new TransformerException( fmsg );
        } 
        return XObject.create( varValue, xctxt );
    }
}
