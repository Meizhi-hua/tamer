public final class DOMReference extends DOMStructure
    implements Reference, DOMURIReference {
    private static boolean useC14N11 =
        AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                return Boolean.getBoolean
                    ("com.sun.org.apache.xml.internal.security.useC14N11");
            }
        });
    private static Logger log = Logger.getLogger("org.jcp.xml.dsig.internal.dom");
    private final DigestMethod digestMethod;
    private final String id;
    private final List transforms;
    private List allTransforms;
    private final Data appliedTransformData;
    private Attr here;
    private final String uri;
    private final String type;
    private byte[] digestValue;
    private byte[] calcDigestValue;
    private Element refElem;
    private boolean digested = false;
    private boolean validated = false;
    private boolean validationStatus;
    private Data derefData;
    private InputStream dis;
    private MessageDigest md;
    private Provider provider;
    public DOMReference(String uri, String type, DigestMethod dm,
        List transforms, String id, Provider provider) {
        this(uri, type, dm, null, null, transforms, id, null, provider);
    }
    public DOMReference(String uri, String type, DigestMethod dm,
        List appliedTransforms, Data result, List transforms, String id,
        Provider provider) {
        this(uri, type, dm, appliedTransforms,
             result, transforms, id, null, provider);
    }
    public DOMReference(String uri, String type, DigestMethod dm,
        List appliedTransforms, Data result, List transforms, String id,
        byte[] digestValue, Provider provider) {
        if (dm == null) {
            throw new NullPointerException("DigestMethod must be non-null");
        }
        this.allTransforms = new ArrayList();
        if (appliedTransforms != null) {
            List transformsCopy = new ArrayList(appliedTransforms);
            for (int i = 0, size = transformsCopy.size(); i < size; i++) {
                if (!(transformsCopy.get(i) instanceof Transform)) {
                    throw new ClassCastException
                        ("appliedTransforms["+i+"] is not a valid type");
                }
            }
            this.allTransforms = transformsCopy;
        }
        if (transforms == null) {
            this.transforms = Collections.EMPTY_LIST;
        } else {
            List transformsCopy = new ArrayList(transforms);
            for (int i = 0, size = transformsCopy.size(); i < size; i++) {
                if (!(transformsCopy.get(i) instanceof Transform)) {
                    throw new ClassCastException
                        ("transforms["+i+"] is not a valid type");
                }
            }
            this.transforms = transformsCopy;
            this.allTransforms.addAll(transformsCopy);
        }
        this.digestMethod = dm;
        this.uri = uri;
        if ((uri != null) && (!uri.equals(""))) {
            try {
                new URI(uri);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        this.type = type;
        this.id = id;
        if (digestValue != null) {
            this.digestValue = (byte[]) digestValue.clone();
            this.digested = true;
        }
        this.appliedTransformData = result;
        this.provider = provider;
    }
    public DOMReference(Element refElem, XMLCryptoContext context,
        Provider provider) throws MarshalException {
        Element nextSibling = DOMUtils.getFirstChildElement(refElem);
        List transforms = new ArrayList(5);
        if (nextSibling.getLocalName().equals("Transforms")) {
            Element transformElem = DOMUtils.getFirstChildElement(nextSibling);
            while (transformElem != null) {
                transforms.add
                    (new DOMTransform(transformElem, context, provider));
                transformElem = DOMUtils.getNextSiblingElement(transformElem);
            }
            nextSibling = DOMUtils.getNextSiblingElement(nextSibling);
        }
        Element dmElem = nextSibling;
        this.digestMethod = DOMDigestMethod.unmarshal(dmElem);
        try {
            Element dvElem = DOMUtils.getNextSiblingElement(dmElem);
            this.digestValue = Base64.decode(dvElem);
        } catch (Base64DecodingException bde) {
            throw new MarshalException(bde);
        }
        this.uri = DOMUtils.getAttributeValue(refElem, "URI");
        this.id = DOMUtils.getAttributeValue(refElem, "Id");
        this.type = DOMUtils.getAttributeValue(refElem, "Type");
        this.here = refElem.getAttributeNodeNS(null, "URI");
        this.refElem = refElem;
        this.transforms = transforms;
        this.allTransforms = transforms;
        this.appliedTransformData = null;
        this.provider = provider;
    }
    public DigestMethod getDigestMethod() {
        return digestMethod;
    }
    public String getId() {
        return id;
    }
    public String getURI() {
        return uri;
    }
    public String getType() {
        return type;
    }
    public List getTransforms() {
        return Collections.unmodifiableList(allTransforms);
    }
    public byte[] getDigestValue() {
        return (digestValue == null ? null : (byte[]) digestValue.clone());
    }
    public byte[] getCalculatedDigestValue() {
        return (calcDigestValue == null ? null
                : (byte[]) calcDigestValue.clone());
    }
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context)
        throws MarshalException {
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Marshalling Reference");
        }
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        refElem = DOMUtils.createElement
            (ownerDoc, "Reference", XMLSignature.XMLNS, dsPrefix);
        DOMUtils.setAttributeID(refElem, "Id", id);
        DOMUtils.setAttribute(refElem, "URI", uri);
        DOMUtils.setAttribute(refElem, "Type", type);
        if (!allTransforms.isEmpty()) {
            Element transformsElem = DOMUtils.createElement
                (ownerDoc, "Transforms", XMLSignature.XMLNS, dsPrefix);
            refElem.appendChild(transformsElem);
            for (int i = 0, size = allTransforms.size(); i < size; i++) {
                DOMStructure transform =
                    (DOMStructure) allTransforms.get(i);
                transform.marshal(transformsElem, dsPrefix, context);
            }
        }
        ((DOMDigestMethod) digestMethod).marshal(refElem, dsPrefix, context);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Adding digestValueElem");
        }
        Element digestValueElem = DOMUtils.createElement
            (ownerDoc, "DigestValue", XMLSignature.XMLNS, dsPrefix);
        if (digestValue != null) {
            digestValueElem.appendChild
                (ownerDoc.createTextNode(Base64.encode(digestValue)));
        }
        refElem.appendChild(digestValueElem);
        parent.appendChild(refElem);
        here = refElem.getAttributeNodeNS(null, "URI");
    }
    public void digest(XMLSignContext signContext)
        throws XMLSignatureException {
        Data data = null;
        if (appliedTransformData == null) {
            data = dereference(signContext);
        } else {
            data = appliedTransformData;
        }
        digestValue = transform(data, signContext);
        String encodedDV = Base64.encode(digestValue);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Reference object uri = " + uri);
        }
        Element digestElem = DOMUtils.getLastChildElement(refElem);
        if (digestElem == null) {
            throw new XMLSignatureException("DigestValue element expected");
        }
        DOMUtils.removeAllChildren(digestElem);
        digestElem.appendChild
            (refElem.getOwnerDocument().createTextNode(encodedDV));
        digested = true;
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Reference digesting completed");
        }
    }
    public boolean validate(XMLValidateContext validateContext)
        throws XMLSignatureException {
        if (validateContext == null) {
            throw new NullPointerException("validateContext cannot be null");
        }
        if (validated) {
            return validationStatus;
        }
        Data data = dereference(validateContext);
        calcDigestValue = transform(data, validateContext);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Expected digest: "
                + Base64.encode(digestValue));
            log.log(Level.FINE, "Actual digest: "
                + Base64.encode(calcDigestValue));
        }
        validationStatus = Arrays.equals(digestValue, calcDigestValue);
        validated = true;
        return validationStatus;
    }
    public Data getDereferencedData() {
        return derefData;
    }
    public InputStream getDigestInputStream() {
        return dis;
    }
    private Data dereference(XMLCryptoContext context)
        throws XMLSignatureException {
        Data data = null;
        URIDereferencer deref = context.getURIDereferencer();
        if (deref == null) {
            deref = DOMURIDereferencer.INSTANCE;
        }
        try {
            data = deref.dereference(this, context);
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "URIDereferencer class name: "
                    + deref.getClass().getName());
                log.log(Level.FINE, "Data class name: "
                    + data.getClass().getName());
            }
        } catch (URIReferenceException ure) {
            throw new XMLSignatureException(ure);
        }
        return data;
    }
    private byte[] transform(Data dereferencedData,
        XMLCryptoContext context) throws XMLSignatureException {
        if (md == null) {
            try {
                md = MessageDigest.getInstance
                    (((DOMDigestMethod) digestMethod).getMessageDigestAlgorithm());
            } catch (NoSuchAlgorithmException nsae) {
                throw new XMLSignatureException(nsae);
            }
        }
        md.reset();
        DigesterOutputStream dos;
        Boolean cache = (Boolean)
            context.getProperty("javax.xml.crypto.dsig.cacheReference");
        if (cache != null && cache.booleanValue() == true) {
            this.derefData = copyDerefData(dereferencedData);
            dos = new DigesterOutputStream(md, true);
        } else {
            dos = new DigesterOutputStream(md);
        }
        OutputStream os = new UnsyncBufferedOutputStream(dos);
        Data data = dereferencedData;
        for (int i = 0, size = transforms.size(); i < size; i++) {
            DOMTransform transform = (DOMTransform) transforms.get(i);
            try {
                if (i < size - 1) {
                    data = transform.transform(data, context);
                } else {
                    data = transform.transform(data, context, os);
                }
            } catch (TransformException te) {
                throw new XMLSignatureException(te);
            }
        }
        try {
            if (data != null) {
                XMLSignatureInput xi;
                boolean c14n11 = useC14N11;
                String c14nalg = CanonicalizationMethod.INCLUSIVE;
                if (context instanceof XMLSignContext) {
                    if (!c14n11) {
                        Boolean prop = (Boolean) context.getProperty
                            ("com.sun.org.apache.xml.internal.security.useC14N11");
                        c14n11 = (prop != null && prop.booleanValue() == true);
                        if (c14n11) {
                            c14nalg = "http:
                        }
                    } else {
                        c14nalg = "http:
                    }
                }
                if (data instanceof ApacheData) {
                    xi = ((ApacheData) data).getXMLSignatureInput();
                } else if (data instanceof OctetStreamData) {
                    xi = new XMLSignatureInput
                        (((OctetStreamData)data).getOctetStream());
                } else if (data instanceof NodeSetData) {
                    TransformService spi = null;
                    try {
                        spi = TransformService.getInstance(c14nalg, "DOM");
                    } catch (NoSuchAlgorithmException nsae) {
                        spi = TransformService.getInstance
                            (c14nalg, "DOM", provider);
                    }
                    data = spi.transform(data, context);
                    xi = new XMLSignatureInput
                        (((OctetStreamData)data).getOctetStream());
                } else {
                    throw new XMLSignatureException("unrecognized Data type");
                }
                if (context instanceof XMLSignContext && c14n11
                    && !xi.isOctetStream() && !xi.isOutputStreamSet()) {
                    DOMTransform t = new DOMTransform
                        (TransformService.getInstance(c14nalg, "DOM"));
                    Element transformsElem = null;
                    String dsPrefix = DOMUtils.getSignaturePrefix(context);
                    if (allTransforms.isEmpty()) {
                        transformsElem = DOMUtils.createElement(
                            refElem.getOwnerDocument(),
                            "Transforms", XMLSignature.XMLNS, dsPrefix);
                        refElem.insertBefore(transformsElem,
                            DOMUtils.getFirstChildElement(refElem));
                    } else {
                        transformsElem = DOMUtils.getFirstChildElement(refElem);
                    }
                    t.marshal(transformsElem, dsPrefix, (DOMCryptoContext) context);
                    allTransforms.add(t);
                    xi.updateOutputStream(os, true);
                } else {
                    xi.updateOutputStream(os);
                }
            }
            os.flush();
            if (cache != null && cache.booleanValue() == true) {
                this.dis = dos.getInputStream();
            }
            return dos.getDigestValue();
        } catch (Exception e) {
            throw new XMLSignatureException(e);
        }
    }
    public Node getHere() {
        return here;
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reference)) {
            return false;
        }
        Reference oref = (Reference) o;
        boolean idsEqual = (id == null ? oref.getId() == null :
            id.equals(oref.getId()));
        boolean urisEqual = (uri == null ? oref.getURI() == null :
            uri.equals(oref.getURI()));
        boolean typesEqual = (type == null ? oref.getType() == null :
            type.equals(oref.getType()));
        boolean digestValuesEqual =
            Arrays.equals(digestValue, oref.getDigestValue());
        return (digestMethod.equals(oref.getDigestMethod()) && idsEqual &&
            urisEqual && typesEqual && allTransforms.equals(oref.getTransforms()));
    }
    boolean isDigested() {
        return digested;
    }
    private static Data copyDerefData(Data dereferencedData) {
        if (dereferencedData instanceof ApacheData) {
            ApacheData ad = (ApacheData) dereferencedData;
            XMLSignatureInput xsi = ad.getXMLSignatureInput();
            if (xsi.isNodeSet()) {
                try {
                    final Set s = xsi.getNodeSet();
                    return new NodeSetData() {
                        public Iterator iterator() { return s.iterator(); }
                    };
                } catch (Exception e) {
                            log.log(Level.WARNING,
                        "cannot cache dereferenced data: " + e);
                    return null;
                }
            } else if (xsi.isElement()) {
                return new DOMSubTreeData
                    (xsi.getSubNode(), xsi.isExcludeComments());
            } else if (xsi.isOctetStream() || xsi.isByteArray()) {
                try {
                return new OctetStreamData
                  (xsi.getOctetStream(), xsi.getSourceURI(), xsi.getMIMEType());
                } catch (IOException ioe) {
                            log.log(Level.WARNING,
                        "cannot cache dereferenced data: " + ioe);
                    return null;
                }
            }
        }
        return dereferencedData;
    }
}
