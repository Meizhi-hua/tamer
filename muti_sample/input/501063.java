public abstract class ToSAXHandler extends SerializerBase 
{
    public ToSAXHandler()
    {
    }
    public ToSAXHandler(
        ContentHandler hdlr,
        LexicalHandler lex,
        String encoding)
    {
        setContentHandler(hdlr);
        setLexHandler(lex);
        setEncoding(encoding);
    }
    public ToSAXHandler(ContentHandler handler, String encoding)
    {
        setContentHandler(handler);
        setEncoding(encoding);
    }
    protected ContentHandler m_saxHandler;
    protected LexicalHandler m_lexHandler;
    private boolean m_shouldGenerateNSAttribute = true;
    protected TransformStateSetter m_state = null;
    protected void startDocumentInternal() throws SAXException
    {
        if (m_needToCallStartDocument)  
        {
            super.startDocumentInternal();
            m_saxHandler.startDocument();
            m_needToCallStartDocument = false;
        }
    }
    public void startDTD(String arg0, String arg1, String arg2)
        throws SAXException
    {
    }
    public void characters(String characters) throws SAXException
    {
        final int len = characters.length();
        if (len > m_charsBuff.length)
        {
           m_charsBuff = new char[len*2 + 1];             
        }
        characters.getChars(0,len, m_charsBuff, 0);   
        characters(m_charsBuff, 0, len);
    }
    public void comment(String comment) throws SAXException
    {
        flushPending();
        if (m_lexHandler != null)
        {
            final int len = comment.length();
            if (len > m_charsBuff.length)
            {
               m_charsBuff = new char[len*2 + 1];              
            }
            comment.getChars(0,len, m_charsBuff, 0);            
            m_lexHandler.comment(m_charsBuff, 0, len);
            if (m_tracer != null)
                super.fireCommentEvent(m_charsBuff, 0, len);
        }
    }
    public void processingInstruction(String target, String data)
        throws SAXException
    {
    }
    protected void closeStartTag() throws SAXException
    {
    }
    protected void closeCDATA() throws SAXException
    {
    }
    public void startElement(
        String arg0,
        String arg1,
        String arg2,
        Attributes arg3)
        throws SAXException
    {
        if (m_state != null) {
            m_state.resetState(getTransformer());
        }
        if (m_tracer != null)
            super.fireStartElem(arg2);
    }
    public void setLexHandler(LexicalHandler _lexHandler)
    {
        this.m_lexHandler = _lexHandler;
    }
    public void setContentHandler(ContentHandler _saxHandler)
    {
        this.m_saxHandler = _saxHandler;
        if (m_lexHandler == null && _saxHandler instanceof LexicalHandler)
        {
            m_lexHandler = (LexicalHandler) _saxHandler;
        }
    }
    public void setCdataSectionElements(Vector URI_and_localNames)
    {
    }
    public void setShouldOutputNSAttr(boolean doOutputNSAttr)
    {
        m_shouldGenerateNSAttribute = doOutputNSAttr;
    }
    boolean getShouldOutputNSAttr()
    {
        return m_shouldGenerateNSAttribute;
    }
    public void flushPending() throws SAXException
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
            if (m_cdataTagOpen)
            {
                closeCDATA();
                m_cdataTagOpen = false;
            }
    }
    public void setTransformState(TransformStateSetter ts) {
        this.m_state = ts;
    }
    public void startElement(String uri, String localName, String qName)
        throws SAXException {
        if (m_state != null) {
            m_state.resetState(getTransformer());
        }
        if (m_tracer != null)
            super.fireStartElem(qName);         
    }
    public void startElement(String qName) throws SAXException {
        if (m_state != null) {
            m_state.resetState(getTransformer());
        }        
        if (m_tracer != null)
            super.fireStartElem(qName);              
    }
    public void characters(org.w3c.dom.Node node)
        throws org.xml.sax.SAXException
    {
        if (m_state != null)
        {
            m_state.setCurrentNode(node);
        }
        String data = node.getNodeValue();
        if (data != null) {
            this.characters(data);
        }
    }    
    public void fatalError(SAXParseException exc) throws SAXException {
        super.fatalError(exc);
        m_needToCallStartDocument = false;
        if (m_saxHandler instanceof ErrorHandler) {
            ((ErrorHandler)m_saxHandler).fatalError(exc);            
        }
    }
    public void error(SAXParseException exc) throws SAXException {
        super.error(exc);
        if (m_saxHandler instanceof ErrorHandler)
            ((ErrorHandler)m_saxHandler).error(exc);        
    }
    public void warning(SAXParseException exc) throws SAXException {
        super.warning(exc);
        if (m_saxHandler instanceof ErrorHandler)
            ((ErrorHandler)m_saxHandler).warning(exc);        
    }
    public boolean reset()
    {
        boolean wasReset = false;
        if (super.reset())
        {
            resetToSAXHandler();
            wasReset = true;
        }
        return wasReset;
    }
    private void resetToSAXHandler()
    {
        this.m_lexHandler = null;
        this.m_saxHandler = null;
        this.m_state = null;
        this.m_shouldGenerateNSAttribute = false;
    }  
    public void addUniqueAttribute(String qName, String value, int flags)
        throws SAXException
    {
        addAttribute(qName, value); 
    }
}
