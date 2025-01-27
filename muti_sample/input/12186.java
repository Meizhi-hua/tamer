public class DOMURIDereferencer implements URIDereferencer {
    static final URIDereferencer INSTANCE = new DOMURIDereferencer();
    private DOMURIDereferencer() {
        Init.init();
    }
    public Data dereference(URIReference uriRef, XMLCryptoContext context)
        throws URIReferenceException {
        if (uriRef == null) {
            throw new NullPointerException("uriRef cannot be null");
        }
        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }
        DOMURIReference domRef = (DOMURIReference) uriRef;
        Attr uriAttr = (Attr) domRef.getHere();
        String uri = uriRef.getURI();
        DOMCryptoContext dcc = (DOMCryptoContext) context;
        if (uri != null && uri.length() != 0 && uri.charAt(0) == '#') {
            String id = uri.substring(1);
            if (id.startsWith("xpointer(id(")) {
                int i1 = id.indexOf('\'');
                int i2 = id.indexOf('\'', i1+1);
                id = id.substring(i1+1, i2);
            }
            Node referencedElem = dcc.getElementById(id);
            if (referencedElem != null) {
                IdResolver.registerElementById((Element) referencedElem, id);
            }
        }
        try {
            String baseURI = context.getBaseURI();
            ResourceResolver apacheResolver =
                ResourceResolver.getInstance(uriAttr, baseURI);
            XMLSignatureInput in = apacheResolver.resolve(uriAttr, baseURI);
            if (in.isOctetStream()) {
                return new ApacheOctetStreamData(in);
            } else {
                return new ApacheNodeSetData(in);
            }
        } catch (Exception e) {
            throw new URIReferenceException(e);
        }
    }
}
