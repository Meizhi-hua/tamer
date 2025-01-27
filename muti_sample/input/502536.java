public class TransformerIdentityImpl extends Transformer
        implements TransformerHandler, DeclHandler
{
  public TransformerIdentityImpl(boolean isSecureProcessing)
  {
    m_outputFormat = new OutputProperties(Method.XML);
    m_isSecureProcessing = isSecureProcessing;
  }
  public TransformerIdentityImpl()
  {
    this(false);
  }
  public void setResult(Result result) throws IllegalArgumentException
  {
    if(null == result)
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_RESULT_NULL, null)); 
    m_result = result;
  }
  public void setSystemId(String systemID)
  {
    m_systemID = systemID;
  }
  public String getSystemId()
  {
    return m_systemID;
  }
  public Transformer getTransformer()
  {
    return this;
  }
  public void reset()
  {
    m_flushedStartDoc = false;
    m_foundFirstElement = false;
    m_outputStream = null;
    clearParameters();
    m_result = null;
    m_resultContentHandler = null;
    m_resultDeclHandler = null;
    m_resultDTDHandler = null;
    m_resultLexicalHandler = null;
    m_serializer = null;
    m_systemID = null;
    m_URIResolver = null;
    m_outputFormat = new OutputProperties(Method.XML);
  }
  private void createResultContentHandler(Result outputTarget)
          throws TransformerException
  {
    if (outputTarget instanceof SAXResult)
    {
      SAXResult saxResult = (SAXResult) outputTarget;
      m_resultContentHandler = saxResult.getHandler();
      m_resultLexicalHandler = saxResult.getLexicalHandler();
      if (m_resultContentHandler instanceof Serializer)
      {
        m_serializer = (Serializer) m_resultContentHandler;
      }
    }
    else if (outputTarget instanceof DOMResult)
    {
      DOMResult domResult = (DOMResult) outputTarget;
      Node outputNode = domResult.getNode();
      Node nextSibling = domResult.getNextSibling();
      Document doc;
      short type;
      if (null != outputNode)
      {
        type = outputNode.getNodeType();
        doc = (Node.DOCUMENT_NODE == type)
              ? (Document) outputNode : outputNode.getOwnerDocument();
      }
      else
      {
        try
        {
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          dbf.setNamespaceAware(true);
          if (m_isSecureProcessing)
          {
            try
            {
              dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            }
            catch (ParserConfigurationException pce) {}
          }
          DocumentBuilder db = dbf.newDocumentBuilder();
          doc = db.newDocument();
        }
        catch (ParserConfigurationException pce)
        {
          throw new TransformerException(pce);
        }
        outputNode = doc;
        type = outputNode.getNodeType();
        ((DOMResult) outputTarget).setNode(outputNode);
      }
      DOMBuilder domBuilder =
        (Node.DOCUMENT_FRAGMENT_NODE == type)
        ? new DOMBuilder(doc, (DocumentFragment) outputNode)
        : new DOMBuilder(doc, outputNode);
      if (nextSibling != null)
        domBuilder.setNextSibling(nextSibling);
      m_resultContentHandler = domBuilder;
      m_resultLexicalHandler = domBuilder;
    }
    else if (outputTarget instanceof StreamResult)
    {
      StreamResult sresult = (StreamResult) outputTarget;
      try
      {
        Serializer serializer =
          SerializerFactory.getSerializer(m_outputFormat.getProperties());
        m_serializer = serializer;
        if (null != sresult.getWriter())
          serializer.setWriter(sresult.getWriter());
        else if (null != sresult.getOutputStream())
          serializer.setOutputStream(sresult.getOutputStream());
        else if (null != sresult.getSystemId())
        {
          String fileURL = sresult.getSystemId();
          if (fileURL.startsWith("file:
            if (fileURL.substring(8).indexOf(":") >0) {
              fileURL = fileURL.substring(8);
            } else  {
              fileURL = fileURL.substring(7);
            }
          } else if (fileURL.startsWith("file:/")) {
            if (fileURL.substring(6).indexOf(":") >0) {
              fileURL = fileURL.substring(6);
            } else {
              fileURL = fileURL.substring(5);
            }
          }
          m_outputStream = new java.io.FileOutputStream(fileURL);
          serializer.setOutputStream(m_outputStream);
        }
        else
          throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_OUTPUT_SPECIFIED, null)); 
        m_resultContentHandler = serializer.asContentHandler();
      }
      catch (IOException ioe)
      {
        throw new TransformerException(ioe);
      }
    }
    else
    {
      throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_TRANSFORM_TO_RESULT_TYPE, new Object[]{outputTarget.getClass().getName()})); 
    }
    if (m_resultContentHandler instanceof DTDHandler)
      m_resultDTDHandler = (DTDHandler) m_resultContentHandler;
    if (m_resultContentHandler instanceof DeclHandler)
      m_resultDeclHandler = (DeclHandler) m_resultContentHandler;
    if (m_resultContentHandler instanceof LexicalHandler)
      m_resultLexicalHandler = (LexicalHandler) m_resultContentHandler;
  }
  public void transform(Source source, Result outputTarget)
          throws TransformerException
  {
    createResultContentHandler(outputTarget);
    if ((source instanceof StreamSource && source.getSystemId()==null &&
       ((StreamSource)source).getInputStream()==null &&
       ((StreamSource)source).getReader()==null)||
       (source instanceof SAXSource &&
       ((SAXSource)source).getInputSource()==null &&
       ((SAXSource)source).getXMLReader()==null )||
       (source instanceof DOMSource && ((DOMSource)source).getNode()==null)){
      try {
        DocumentBuilderFactory builderF = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderF.newDocumentBuilder();
        String systemID = source.getSystemId();
        source = new DOMSource(builder.newDocument());
        if (systemID != null) {
          source.setSystemId(systemID);
        }
      } catch (ParserConfigurationException e){
        throw new TransformerException(e.getMessage());
      }           
    }
    try
    {
      if (source instanceof DOMSource)
      {
        DOMSource dsource = (DOMSource) source;
        m_systemID = dsource.getSystemId();
        Node dNode = dsource.getNode();
        if (null != dNode)
        {
          try
          {
            if(dNode.getNodeType() == Node.ATTRIBUTE_NODE)
              this.startDocument();
            try
            {
              if(dNode.getNodeType() == Node.ATTRIBUTE_NODE)
              {
                String data = dNode.getNodeValue();
                char[] chars = data.toCharArray();
                characters(chars, 0, chars.length);
              }
              else
              { 
                org.apache.xml.serializer.TreeWalker walker;
                walker = new org.apache.xml.serializer.TreeWalker(this, m_systemID);
                walker.traverse(dNode);
              }
            }
            finally
            {
              if(dNode.getNodeType() == Node.ATTRIBUTE_NODE)
                this.endDocument();
            }
          }
          catch (SAXException se)
          {
            throw new TransformerException(se);
          }
          return;
        }
        else
        {
          String messageStr = XSLMessages.createMessage(
            XSLTErrorResources.ER_ILLEGAL_DOMSOURCE_INPUT, null);
          throw new IllegalArgumentException(messageStr);
        }
      }
      InputSource xmlSource = SAXSource.sourceToInputSource(source);
      if (null == xmlSource)
      {
        throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_TRANSFORM_SOURCE_TYPE, new Object[]{source.getClass().getName()})); 
      }
      if (null != xmlSource.getSystemId())
        m_systemID = xmlSource.getSystemId();
      XMLReader reader = null;
      boolean managedReader = false;
      try
      {
        if (source instanceof SAXSource) {
          reader = ((SAXSource) source).getXMLReader();
        }
        if (null == reader) {
          try {
            reader = XMLReaderManager.getInstance().getXMLReader();
            managedReader = true;
          } catch (SAXException se) {
            throw new TransformerException(se);
          }
        } else {
          try {
            reader.setFeature("http:
                              true);
          } catch (org.xml.sax.SAXException se) {
          }
        }
        ContentHandler inputHandler = this;
        reader.setContentHandler(inputHandler);
        if (inputHandler instanceof org.xml.sax.DTDHandler)
          reader.setDTDHandler((org.xml.sax.DTDHandler) inputHandler);
        try
        {
          if (inputHandler instanceof org.xml.sax.ext.LexicalHandler)
            reader.setProperty("http:
                               inputHandler);
          if (inputHandler instanceof org.xml.sax.ext.DeclHandler)
            reader.setProperty(
              "http:
              inputHandler);
        }
        catch (org.xml.sax.SAXException se){}
        try
        {
          if (inputHandler instanceof org.xml.sax.ext.LexicalHandler)
            reader.setProperty("http:
                               inputHandler);
          if (inputHandler instanceof org.xml.sax.ext.DeclHandler)
            reader.setProperty("http:
                               inputHandler);
        }
        catch (org.xml.sax.SAXNotRecognizedException snre){}
        reader.parse(xmlSource);
      }
      catch (org.apache.xml.utils.WrappedRuntimeException wre)
      {
        Throwable throwable = wre.getException();
        while (throwable
               instanceof org.apache.xml.utils.WrappedRuntimeException)
        {
          throwable =
            ((org.apache.xml.utils.WrappedRuntimeException) throwable).getException();
        }
        throw new TransformerException(wre.getException());
      }
      catch (org.xml.sax.SAXException se)
      {
        throw new TransformerException(se);
      }
      catch (IOException ioe)
      {
        throw new TransformerException(ioe);
      } finally {
        if (managedReader) {
          XMLReaderManager.getInstance().releaseXMLReader(reader);
        }
      }
    }
    finally
    {
      if(null != m_outputStream)
      {
        try
        {
          m_outputStream.close();
        }
        catch(IOException ioe){}
        m_outputStream = null;
      }
    }
  }
  public void setParameter(String name, Object value)
  {
    if (value == null) {
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_SET_PARAM_VALUE, new Object[]{name}));
    }
    if (null == m_params)
    {
      m_params = new Hashtable();
    }
    m_params.put(name, value);
  }
  public Object getParameter(String name)
  {
    if (null == m_params)
      return null;
    return m_params.get(name);
  }
  public void clearParameters()
  {
    if (null == m_params)
      return;
    m_params.clear();
  }
  public void setURIResolver(URIResolver resolver)
  {
    m_URIResolver = resolver;
  }
  public URIResolver getURIResolver()
  {
    return m_URIResolver;
  }
  public void setOutputProperties(Properties oformat)
          throws IllegalArgumentException
  {
    if (null != oformat)
    {
      String method = (String) oformat.get(OutputKeys.METHOD);
      if (null != method)
        m_outputFormat = new OutputProperties(method);
      else
        m_outputFormat = new OutputProperties();
      m_outputFormat.copyFrom(oformat);
    }
    else {
      m_outputFormat = null;
    }
  }
  public Properties getOutputProperties()
  {
    return (Properties) m_outputFormat.getProperties().clone();
  }
  public void setOutputProperty(String name, String value)
          throws IllegalArgumentException
  {
    if (!OutputProperties.isLegalPropertyKey(name))
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{name})); 
    m_outputFormat.setProperty(name, value);
  }
  public String getOutputProperty(String name) throws IllegalArgumentException
  {
    String value = null;
    OutputProperties props = m_outputFormat;
    value = props.getProperty(name);
    if (null == value)
    {
      if (!OutputProperties.isLegalPropertyKey(name))
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{name})); 
    }
    return value;
  }
  public void setErrorListener(ErrorListener listener)
          throws IllegalArgumentException
  {
      if (listener == null)
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_NULL_ERROR_HANDLER, null));
      else
        m_errorListener = listener;
  }
  public ErrorListener getErrorListener()
  {
    return m_errorListener;
  }
  public void notationDecl(String name, String publicId, String systemId)
          throws SAXException
  {
    if (null != m_resultDTDHandler)
      m_resultDTDHandler.notationDecl(name, publicId, systemId);
  }
  public void unparsedEntityDecl(
          String name, String publicId, String systemId, String notationName)
            throws SAXException
  {
    if (null != m_resultDTDHandler)
      m_resultDTDHandler.unparsedEntityDecl(name, publicId, systemId,
                                            notationName);
  }
  public void setDocumentLocator(Locator locator)
  {
    try
    {
      if (null == m_resultContentHandler)
        createResultContentHandler(m_result);
    }
    catch (TransformerException te)
    {
      throw new org.apache.xml.utils.WrappedRuntimeException(te);
    }
    m_resultContentHandler.setDocumentLocator(locator);
  }
  public void startDocument() throws SAXException
  {
    try
    {
      if (null == m_resultContentHandler)
        createResultContentHandler(m_result);
    }
    catch (TransformerException te)
    {
      throw new SAXException(te.getMessage(), te);
    }
    m_flushedStartDoc = false;
    m_foundFirstElement = false;
  }
  boolean m_flushedStartDoc = false;
  protected final void flushStartDoc()
     throws SAXException
  {
    if(!m_flushedStartDoc)
    {
      if (m_resultContentHandler == null)
      {
        try
        {
          createResultContentHandler(m_result);
        }
        catch(TransformerException te)
        {
            throw new SAXException(te);
        }
      }
      m_resultContentHandler.startDocument();
      m_flushedStartDoc = true;
    }
  }
  public void endDocument() throws SAXException
  {
    flushStartDoc();
    m_resultContentHandler.endDocument();
  }
  public void startPrefixMapping(String prefix, String uri)
          throws SAXException
  {
    flushStartDoc();
    m_resultContentHandler.startPrefixMapping(prefix, uri);
  }
  public void endPrefixMapping(String prefix) throws SAXException
  {
    flushStartDoc();
    m_resultContentHandler.endPrefixMapping(prefix);
  }
  public void startElement(
          String uri, String localName, String qName, Attributes attributes)
            throws SAXException
  {
    if (!m_foundFirstElement && null != m_serializer)
    {
      m_foundFirstElement = true;
      Serializer newSerializer;
      try
      {
        newSerializer = SerializerSwitcher.switchSerializerIfHTML(uri,
                localName, m_outputFormat.getProperties(), m_serializer);
      }
      catch (TransformerException te)
      {
        throw new SAXException(te);
      }
      if (newSerializer != m_serializer)
      {
        try
        {
          m_resultContentHandler = newSerializer.asContentHandler();
        }
        catch (IOException ioe)  
        {
          throw new SAXException(ioe);
        }
        if (m_resultContentHandler instanceof DTDHandler)
          m_resultDTDHandler = (DTDHandler) m_resultContentHandler;
        if (m_resultContentHandler instanceof LexicalHandler)
          m_resultLexicalHandler = (LexicalHandler) m_resultContentHandler;
        m_serializer = newSerializer;
      }
    }
    flushStartDoc();
    m_resultContentHandler.startElement(uri, localName, qName, attributes);
  }
  public void endElement(String uri, String localName, String qName)
          throws SAXException
  {
    m_resultContentHandler.endElement(uri, localName, qName);
  }
  public void characters(char ch[], int start, int length) throws SAXException
  {
    flushStartDoc();
    m_resultContentHandler.characters(ch, start, length);
  }
  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException
  {
    m_resultContentHandler.ignorableWhitespace(ch, start, length);
  }
  public void processingInstruction(String target, String data)
          throws SAXException
  {
    flushStartDoc();
    m_resultContentHandler.processingInstruction(target, data);
  }
  public void skippedEntity(String name) throws SAXException
  {
    flushStartDoc();
    m_resultContentHandler.skippedEntity(name);
  }
  public void startDTD(String name, String publicId, String systemId)
          throws SAXException
  {
    flushStartDoc();
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.startDTD(name, publicId, systemId);
  }
  public void endDTD() throws SAXException
  {
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.endDTD();
  }
  public void startEntity(String name) throws SAXException
  {
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.startEntity(name);
  }
  public void endEntity(String name) throws SAXException
  {
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.endEntity(name);
  }
  public void startCDATA() throws SAXException
  {
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.startCDATA();
  }
  public void endCDATA() throws SAXException
  {
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.endCDATA();
  }
  public void comment(char ch[], int start, int length) throws SAXException
  {
    flushStartDoc();
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.comment(ch, start, length);
  }
    public void elementDecl (String name, String model)
        throws SAXException
    {
                        if (null != m_resultDeclHandler)
                                m_resultDeclHandler.elementDecl(name, model);
    }
    public void attributeDecl (String eName,
                                        String aName,
                                        String type,
                                        String valueDefault,
                                        String value)
        throws SAXException
    {
      if (null != m_resultDeclHandler)
                                m_resultDeclHandler.attributeDecl(eName, aName, type, valueDefault, value);
    }
    public void internalEntityDecl (String name, String value)
        throws SAXException
    {
      if (null != m_resultDeclHandler)
                                m_resultDeclHandler.internalEntityDecl(name, value); 
    }
    public void externalEntityDecl (String name, String publicId,
                                             String systemId)
        throws SAXException
    {
      if (null != m_resultDeclHandler)
                                m_resultDeclHandler.externalEntityDecl(name, publicId, systemId);
    }
  private java.io.FileOutputStream m_outputStream = null;
  private ContentHandler m_resultContentHandler;
  private LexicalHandler m_resultLexicalHandler;
  private DTDHandler m_resultDTDHandler;
  private DeclHandler m_resultDeclHandler;
  private Serializer m_serializer;
  private Result m_result;
  private String m_systemID;
  private Hashtable m_params;
  private ErrorListener m_errorListener =
    new org.apache.xml.utils.DefaultErrorHandler(false);
  URIResolver m_URIResolver;
  private OutputProperties m_outputFormat;
  boolean m_foundFirstElement;
  private boolean m_isSecureProcessing = false;
}
