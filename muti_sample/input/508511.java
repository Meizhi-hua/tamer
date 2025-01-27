public class SerializerUtils
{
    public static void addAttribute(SerializationHandler handler, int attr)
        throws TransformerException
    {
        TransformerImpl transformer =
            (TransformerImpl) handler.getTransformer();
        DTM dtm = transformer.getXPathContext().getDTM(attr);
        if (SerializerUtils.isDefinedNSDecl(handler, attr, dtm))
            return;
        String ns = dtm.getNamespaceURI(attr);
        if (ns == null)
            ns = "";
        try
        {
            handler.addAttribute(
                ns,
                dtm.getLocalName(attr),
                dtm.getNodeName(attr),
                "CDATA",
                dtm.getNodeValue(attr), false);
        }
        catch (SAXException e)
        {
        }
    } 
    public static void addAttributes(SerializationHandler handler, int src)
        throws TransformerException
    {
        TransformerImpl transformer =
            (TransformerImpl) handler.getTransformer();
        DTM dtm = transformer.getXPathContext().getDTM(src);
        for (int node = dtm.getFirstAttribute(src);
            DTM.NULL != node;
            node = dtm.getNextAttribute(node))
        {
            addAttribute(handler, node);
        }
    }
    public static void outputResultTreeFragment(
        SerializationHandler handler,
        XObject obj,
        XPathContext support)
        throws org.xml.sax.SAXException
    {
        int doc = obj.rtf();
        DTM dtm = support.getDTM(doc);
        if (null != dtm)
        {
            for (int n = dtm.getFirstChild(doc);
                DTM.NULL != n;
                n = dtm.getNextSibling(n))
            {
                handler.flushPending();
                if (dtm.getNodeType(n) == DTM.ELEMENT_NODE
                        && dtm.getNamespaceURI(n) == null)
                    handler.startPrefixMapping("", "");
                dtm.dispatchToEvents(n, handler);
            }
        }
    }
    public static void processNSDecls(
        SerializationHandler handler,
        int src,
        int type,
        DTM dtm)
        throws TransformerException
    {
        try
        {
            if (type == DTM.ELEMENT_NODE)
            {
                for (int namespace = dtm.getFirstNamespaceNode(src, true);
                    DTM.NULL != namespace;
                    namespace = dtm.getNextNamespaceNode(src, namespace, true))
                {
                    String prefix = dtm.getNodeNameX(namespace);
                    String desturi = handler.getNamespaceURIFromPrefix(prefix);
                    String srcURI = dtm.getNodeValue(namespace);
                    if (!srcURI.equalsIgnoreCase(desturi))
                    {
                        handler.startPrefixMapping(prefix, srcURI, false);
                    }
                }
            }
            else if (type == DTM.NAMESPACE_NODE)
            {
                String prefix = dtm.getNodeNameX(src);
                String desturi = handler.getNamespaceURIFromPrefix(prefix);
                String srcURI = dtm.getNodeValue(src);
                if (!srcURI.equalsIgnoreCase(desturi))
                {
                    handler.startPrefixMapping(prefix, srcURI, false);
                }
            }
        }
        catch (org.xml.sax.SAXException se)
        {
            throw new TransformerException(se);
        }
    }
    public static boolean isDefinedNSDecl(
        SerializationHandler serializer,
        int attr,
        DTM dtm)
    {
        if (DTM.NAMESPACE_NODE == dtm.getNodeType(attr))
        {
            String prefix = dtm.getNodeNameX(attr);
            String uri = serializer.getNamespaceURIFromPrefix(prefix);
            if ((null != uri) && uri.equals(dtm.getStringValue(attr)))
                return true;
        }
        return false;
    }
    public static void ensureNamespaceDeclDeclared(
        SerializationHandler handler,
        DTM dtm,
        int namespace)
        throws org.xml.sax.SAXException
    {
        String uri = dtm.getNodeValue(namespace);
        String prefix = dtm.getNodeNameX(namespace);
        if ((uri != null && uri.length() > 0) && (null != prefix))
        {
            String foundURI;
            NamespaceMappings ns = handler.getNamespaceMappings();
            if (ns != null)
            {
                foundURI = ns.lookupNamespace(prefix);
                if ((null == foundURI) || !foundURI.equals(uri))
                {
                    handler.startPrefixMapping(prefix, uri, false);
                }
            }
        }
    }
}
