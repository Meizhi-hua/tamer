public final class ToXMLSAXHandler extends ToSAXHandler
{
    protected boolean m_escapeSetting = true;
    public ToXMLSAXHandler()
    {
        m_prefixMap = new NamespaceMappings();
        initCDATA();
    }
    public Properties getOutputFormat()
    {
        return null;
    }
    public OutputStream getOutputStream()
    {
        return null;
    }
    public Writer getWriter()
    {
        return null;
    }
    public void indent(int n) throws SAXException
    {
    }
    public void serialize(Node node) throws IOException
    {
    }
    public boolean setEscaping(boolean escape) throws SAXException
    {
        boolean oldEscapeSetting = m_escapeSetting;
        m_escapeSetting = escape;
        if (escape) {
            processingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING, "");
        } else {
            processingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, "");
        }
        return oldEscapeSetting;
    }
    public void setOutputFormat(Properties format)
    {
    }
    public void setOutputStream(OutputStream output)
    {
    }
    public void setWriter(Writer writer)
    {
    }
    public void attributeDecl(
        String arg0,
        String arg1,
        String arg2,
        String arg3,
        String arg4)
        throws SAXException
    {
    }
    public void elementDecl(String arg0, String arg1) throws SAXException
    {
    }
    public void externalEntityDecl(String arg0, String arg1, String arg2)
        throws SAXException
    {
    }
    public void internalEntityDecl(String arg0, String arg1)
        throws SAXException
    {
    }
    public void endDocument() throws SAXException
    {
        flushPending();
        m_saxHandler.endDocument();
        if (m_tracer != null)
            super.fireEndDoc();
    }
    protected void closeStartTag() throws SAXException
    {
        m_elemContext.m_startTagOpen = false;
        final String localName = getLocalName(m_elemContext.m_elementName);
        final String uri = getNamespaceURI(m_elemContext.m_elementName, true);
        if (m_needToCallStartDocument)
        {
            startDocumentInternal();
        }
        m_saxHandler.startElement(uri, localName, m_elemContext.m_elementName, m_attributes);
        m_attributes.clear();
        if(m_state != null)
          m_state.setCurrentNode(null);
    }
    public void closeCDATA() throws SAXException
    {
        if (m_lexHandler != null && m_cdataTagOpen) {
            m_lexHandler.endCDATA();
        }
        m_cdataTagOpen = false;        
    }
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException
    {
        flushPending();
        if (namespaceURI == null)
        {
            if (m_elemContext.m_elementURI != null)
                namespaceURI = m_elemContext.m_elementURI;
            else
                namespaceURI = getNamespaceURI(qName, true);
        }
        if (localName == null)
        {
            if (m_elemContext.m_elementLocalName != null)
                localName = m_elemContext.m_elementLocalName;
            else
                localName = getLocalName(qName);
        }
        m_saxHandler.endElement(namespaceURI, localName, qName);
        if (m_tracer != null)
            super.fireEndElem(qName);       
        m_prefixMap.popNamespaces(m_elemContext.m_currentElemDepth,
            m_saxHandler);
        m_elemContext = m_elemContext.m_prev;
    }
    public void endPrefixMapping(String prefix) throws SAXException
    {
         return;
    }
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
        throws SAXException
    {
        m_saxHandler.ignorableWhitespace(arg0,arg1,arg2);
    }
    public void setDocumentLocator(Locator arg0)
    {
        m_saxHandler.setDocumentLocator(arg0);
    }
    public void skippedEntity(String arg0) throws SAXException
    {
        m_saxHandler.skippedEntity(arg0);
    }
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException
    {
       startPrefixMapping(prefix, uri, true);
    }
    public boolean startPrefixMapping(
        String prefix,
        String uri,
        boolean shouldFlush)
        throws org.xml.sax.SAXException
    {
        boolean pushed;
        int pushDepth;
        if (shouldFlush)
        {
            flushPending();
            pushDepth = m_elemContext.m_currentElemDepth + 1;
        }
        else
        {
            pushDepth = m_elemContext.m_currentElemDepth;
        }
        pushed = m_prefixMap.pushNamespace(prefix, uri, pushDepth);
        if (pushed)
        {
            m_saxHandler.startPrefixMapping(prefix,uri);
            if (getShouldOutputNSAttr()) 
            {
	              String name;
  	            if (EMPTYSTRING.equals(prefix))
  	            {
  	                name = "xmlns";
  	                addAttributeAlways(XMLNS_URI, name, name,"CDATA",uri, false);
  	            }
  	            else 
                {
  	                if (!EMPTYSTRING.equals(uri)) 
  	                {                             
  	                    name = "xmlns:" + prefix;
  	                    addAttributeAlways(XMLNS_URI, prefix, name,"CDATA",uri, false );
  	                }
  	            }
            }
        }
        return pushed;
    }
    public void comment(char[] arg0, int arg1, int arg2) throws SAXException
    {
        flushPending();
        if (m_lexHandler != null)
            m_lexHandler.comment(arg0, arg1, arg2);
        if (m_tracer != null)            
            super.fireCommentEvent(arg0, arg1, arg2);
    }
    public void endCDATA() throws SAXException
    {
    }
    public void endDTD() throws SAXException
    {
        if (m_lexHandler != null)
            m_lexHandler.endDTD();
    }
    public void startEntity(String arg0) throws SAXException
    {
        if (m_lexHandler != null)
            m_lexHandler.startEntity(arg0);
    }
    public void characters(String chars) throws SAXException
    {
        final int length = chars.length();
        if (length > m_charsBuff.length)
        {
            m_charsBuff = new char[length*2 + 1];
        }
        chars.getChars(0, length, m_charsBuff, 0);
        this.characters(m_charsBuff, 0, length); 
    }
    public ToXMLSAXHandler(ContentHandler handler, String encoding)
    {
        super(handler, encoding);
        initCDATA();
        m_prefixMap = new NamespaceMappings();
    }
    public ToXMLSAXHandler(
        ContentHandler handler,
        LexicalHandler lex,
        String encoding)
    {
        super(handler, lex, encoding);
        initCDATA();
        m_prefixMap = new NamespaceMappings();
    }
    public void startElement(
    String elementNamespaceURI,
    String elementLocalName,
    String elementName) throws SAXException
    {
        startElement(
            elementNamespaceURI,elementLocalName,elementName, null);
    }
    public void startElement(String elementName) throws SAXException
    {
        startElement(null, null, elementName, null);
    }
    public void characters(char[] ch, int off, int len) throws SAXException
    {
        if (m_needToCallStartDocument)
        {
            startDocumentInternal();
            m_needToCallStartDocument = false;
        }
        if (m_elemContext.m_startTagOpen)
        {
            closeStartTag();
            m_elemContext.m_startTagOpen = false;
        }
        if (m_elemContext.m_isCdataSection && !m_cdataTagOpen
        && m_lexHandler != null) 
        {
            m_lexHandler.startCDATA();
            m_cdataTagOpen = true;
        }
        m_saxHandler.characters(ch, off, len);
        if (m_tracer != null)
            fireCharEvent(ch, off, len);
    }
    public void endElement(String elemName) throws SAXException
    {
        endElement(null, null, elemName);
    }    
    public void namespaceAfterStartElement(
        final String prefix,
        final String uri)
        throws SAXException
    {
        startPrefixMapping(prefix,uri,false);
    }
    public void processingInstruction(String target, String data)
        throws SAXException
    {
        flushPending();
        m_saxHandler.processingInstruction(target, data);
        if (m_tracer != null)
            super.fireEscapingEvent(target, data);
    }
    protected boolean popNamespace(String prefix)
    {
        try
        {
            if (m_prefixMap.popNamespace(prefix))
            {
                m_saxHandler.endPrefixMapping(prefix);
                return true;
            }
        }
        catch (SAXException e)
        {
        }
        return false;
    }
    public void startCDATA() throws SAXException
    {
        if (!m_cdataTagOpen ) 
        {
            flushPending();
            if (m_lexHandler != null) {
                m_lexHandler.startCDATA();
                m_cdataTagOpen = true;     
            }              
        }        
    }
    public void startElement(
    String namespaceURI,
    String localName,
    String name,
    Attributes atts)
        throws SAXException
    {
        flushPending();
        super.startElement(namespaceURI, localName, name, atts);
         if (m_needToOutputDocTypeDecl)
         {
             String doctypeSystem = getDoctypeSystem();
             if (doctypeSystem != null && m_lexHandler != null)
             {
                 String doctypePublic = getDoctypePublic();
                 if (doctypeSystem != null)
                     m_lexHandler.startDTD(
                         name,
                         doctypePublic,
                         doctypeSystem);
             }
             m_needToOutputDocTypeDecl = false;
         }
        m_elemContext = m_elemContext.push(namespaceURI, localName, name);
        if (namespaceURI != null)
            ensurePrefixIsDeclared(namespaceURI, name);
        if (atts != null)
            addAttributes(atts);
        m_elemContext.m_isCdataSection = isCdataSection();
    }
    private void ensurePrefixIsDeclared(String ns, String rawName)
        throws org.xml.sax.SAXException
    {
        if (ns != null && ns.length() > 0)
        {
            int index;
            final boolean no_prefix = ((index = rawName.indexOf(":")) < 0);
            String prefix = (no_prefix) ? "" : rawName.substring(0, index);
            if (null != prefix)
            {
                String foundURI = m_prefixMap.lookupNamespace(prefix);
                if ((null == foundURI) || !foundURI.equals(ns))
                {
                    this.startPrefixMapping(prefix, ns, false);
                    if (getShouldOutputNSAttr()) {
                        this.addAttributeAlways(
                            "http:
                            no_prefix ? "xmlns" : prefix,  
                            no_prefix ? "xmlns" : ("xmlns:"+ prefix), 
                            "CDATA",
                            ns,
                            false);
                    }
                }
            }
        }
    }
    public void addAttribute(
        String uri,
        String localName,
        String rawName,
        String type,
        String value,
        boolean XSLAttribute)
        throws SAXException
    {      
        if (m_elemContext.m_startTagOpen)
        {
            ensurePrefixIsDeclared(uri, rawName);
            addAttributeAlways(uri, localName, rawName, type, value, false);
        }
    } 
    public boolean reset()
    {
        boolean wasReset = false;
        if (super.reset())
        {
            resetToXMLSAXHandler();
            wasReset = true;
        }
        return wasReset;
    }
    private void resetToXMLSAXHandler()
    {
        this.m_escapeSetting = true;
    }  
}
