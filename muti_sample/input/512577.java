public class TransformerHandlerImpl
        implements EntityResolver, DTDHandler, ContentHandler, ErrorHandler,
                   LexicalHandler, TransformerHandler, DeclHandler
{
    private final boolean m_optimizer;
    private final boolean m_incremental;
    private final boolean m_source_location;
  private boolean m_insideParse = false;
  public TransformerHandlerImpl(TransformerImpl transformer,
                                boolean doFragment, String baseSystemID)
  {
    super();
    m_transformer = transformer;
    m_baseSystemID = baseSystemID;
    XPathContext xctxt = transformer.getXPathContext();
    DTM dtm = xctxt.getDTM(null, true, transformer, true, true);
    m_dtm = dtm;
    dtm.setDocumentBaseURI(baseSystemID);
    m_contentHandler = dtm.getContentHandler();
    m_dtdHandler = dtm.getDTDHandler();
    m_entityResolver = dtm.getEntityResolver();
    m_errorHandler = dtm.getErrorHandler();
    m_lexicalHandler = dtm.getLexicalHandler();
    m_incremental = transformer.getIncremental();
    m_optimizer = transformer.getOptimize();
    m_source_location = transformer.getSource_location();
  }
  protected void clearCoRoutine()
  {
    clearCoRoutine(null);
  }
  protected void clearCoRoutine(SAXException ex)
  {
    if(null != ex)
      m_transformer.setExceptionThrown(ex);
    if(m_dtm instanceof SAX2DTM)
    {
      if(DEBUG)
        System.err.println("In clearCoRoutine...");
      try
      {
        SAX2DTM sax2dtm = ((SAX2DTM)m_dtm);          
        if(null != m_contentHandler 
           && m_contentHandler instanceof IncrementalSAXSource_Filter)
        {
          IncrementalSAXSource_Filter sp =
            (IncrementalSAXSource_Filter)m_contentHandler;
          sp.deliverMoreNodes(false);
        }
        sax2dtm.clearCoRoutine(true);
        m_contentHandler = null;
        m_dtdHandler = null;
        m_entityResolver = null;
        m_errorHandler = null;
        m_lexicalHandler = null;
      }
      catch(Throwable throwable)
      {
        throwable.printStackTrace();
      }
      if(DEBUG)
        System.err.println("...exiting clearCoRoutine");
    }
  }
  public void setResult(Result result) throws IllegalArgumentException
  {
    if (null == result)
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_RESULT_NULL, null)); 
    try
    {
        SerializationHandler xoh = 
            m_transformer.createSerializationHandler(result);
        m_transformer.setSerializationHandler(xoh);
    }
    catch (javax.xml.transform.TransformerException te)
    {
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_RESULT_COULD_NOT_BE_SET, null)); 
    }
    m_result = result;
  }
  public void setSystemId(String systemID)
  {
    m_baseSystemID = systemID;
    m_dtm.setDocumentBaseURI(systemID);
  }
  public String getSystemId()
  {
    return m_baseSystemID;
  }
  public Transformer getTransformer()
  {
    return m_transformer;
  }
  public InputSource resolveEntity(String publicId, String systemId)
          throws SAXException, IOException
  {
    if (m_entityResolver != null)
    {
      return m_entityResolver.resolveEntity(publicId, systemId);
    }
    else
    {
      return null;
    }
  }
  public void notationDecl(String name, String publicId, String systemId)
          throws SAXException
  {
    if (m_dtdHandler != null)
    {
      m_dtdHandler.notationDecl(name, publicId, systemId);
    }
  }
  public void unparsedEntityDecl(
          String name, String publicId, String systemId, String notationName)
            throws SAXException
  {
    if (m_dtdHandler != null)
    {
      m_dtdHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
    }
  }
  public void setDocumentLocator(Locator locator)
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#setDocumentLocator: "
                         + locator.getSystemId());
    this.m_locator = locator;
    if(null == m_baseSystemID)
    {
      setSystemId(locator.getSystemId());
    }
    if (m_contentHandler != null)
    {
      m_contentHandler.setDocumentLocator(locator);
    }
  }
  public void startDocument() throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#startDocument");
    m_insideParse = true;
    if (m_contentHandler != null)
    {
      if(m_incremental)
      {
        m_transformer.setSourceTreeDocForThread(m_dtm.getDocument());
        int cpriority = Thread.currentThread().getPriority();
        m_transformer.runTransformThread( cpriority );
      }
      m_contentHandler.startDocument();
   }
  }
  public void endDocument() throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#endDocument");
    m_insideParse = false;
    if (m_contentHandler != null)
    {
      m_contentHandler.endDocument();
    }
    if(m_incremental)
    {
      m_transformer.waitTransformThread();
    }
    else
    {
      m_transformer.setSourceTreeDocForThread(m_dtm.getDocument());
      m_transformer.run();
    }
  }
  public void startPrefixMapping(String prefix, String uri)
          throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#startPrefixMapping: "
                         + prefix + ", " + uri);
    if (m_contentHandler != null)
    {
      m_contentHandler.startPrefixMapping(prefix, uri);
    }
  }
  public void endPrefixMapping(String prefix) throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#endPrefixMapping: "
                         + prefix);
    if (m_contentHandler != null)
    {
      m_contentHandler.endPrefixMapping(prefix);
    }
  }
  public void startElement(
          String uri, String localName, String qName, Attributes atts)
            throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#startElement: " + qName);
    if (m_contentHandler != null)
    {
      m_contentHandler.startElement(uri, localName, qName, atts);
    }
  }
  public void endElement(String uri, String localName, String qName)
          throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#endElement: " + qName);
    if (m_contentHandler != null)
    {
      m_contentHandler.endElement(uri, localName, qName);
    }
  }
  public void characters(char ch[], int start, int length) throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#characters: " + start + ", "
                         + length);
    if (m_contentHandler != null)
    {
      m_contentHandler.characters(ch, start, length);
    }
  }
  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#ignorableWhitespace: "
                         + start + ", " + length);
    if (m_contentHandler != null)
    {
      m_contentHandler.ignorableWhitespace(ch, start, length);
    }
  }
  public void processingInstruction(String target, String data)
          throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#processingInstruction: "
                         + target + ", " + data);
    if (m_contentHandler != null)
    {
      m_contentHandler.processingInstruction(target, data);
    }
  }
  public void skippedEntity(String name) throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#skippedEntity: " + name);
    if (m_contentHandler != null)
    {
      m_contentHandler.skippedEntity(name);
    }
  }
  public void warning(SAXParseException e) throws SAXException
  {
    javax.xml.transform.ErrorListener errorListener = m_transformer.getErrorListener();
    if(errorListener instanceof ErrorHandler)
    {
      ((ErrorHandler)errorListener).warning(e);
    }
    else
    {
      try
      {
        errorListener.warning(new javax.xml.transform.TransformerException(e));
      }
      catch(javax.xml.transform.TransformerException te)
      {
        throw e;
      }
    }
  }
  public void error(SAXParseException e) throws SAXException
  {
    javax.xml.transform.ErrorListener errorListener = m_transformer.getErrorListener();
    if(errorListener instanceof ErrorHandler)
    {
      ((ErrorHandler)errorListener).error(e);
      if(null != m_errorHandler)
        m_errorHandler.error(e); 
    }
    else
    {
      try
      {
        errorListener.error(new javax.xml.transform.TransformerException(e));
        if(null != m_errorHandler)
          m_errorHandler.error(e); 
      }
      catch(javax.xml.transform.TransformerException te)
      {
        throw e;
      }
    }
  }
  public void fatalError(SAXParseException e) throws SAXException
  {
    if(null != m_errorHandler)
    {
      try
      {
        m_errorHandler.fatalError(e);
      }
      catch(SAXParseException se)
      {
      }
    }
    javax.xml.transform.ErrorListener errorListener = m_transformer.getErrorListener();
    if(errorListener instanceof ErrorHandler)
    {
      ((ErrorHandler)errorListener).fatalError(e);
      if(null != m_errorHandler)
        m_errorHandler.fatalError(e); 
    }
    else
    {
      try
      {
        errorListener.fatalError(new javax.xml.transform.TransformerException(e));
        if(null != m_errorHandler)
          m_errorHandler.fatalError(e); 
      }
      catch(javax.xml.transform.TransformerException te)
      {
        throw e;
      }
    }
  }
  public void startDTD(String name, String publicId, String systemId)
          throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#startDTD: " + name + ", "
                         + publicId + ", " + systemId);
    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.startDTD(name, publicId, systemId);
    }
  }
  public void endDTD() throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#endDTD");
    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.endDTD();
    }
  }
  public void startEntity(String name) throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#startEntity: " + name);
    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.startEntity(name);
    }
  }
  public void endEntity(String name) throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#endEntity: " + name);
    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.endEntity(name);
    }
  }
  public void startCDATA() throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#startCDATA");
    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.startCDATA();
    }
  }
  public void endCDATA() throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#endCDATA");
    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.endCDATA();
    }
  }
  public void comment(char ch[], int start, int length) throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#comment: " + start + ", "
                         + length);
    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.comment(ch, start, length);
    }
  }
  public void elementDecl(String name, String model) throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#elementDecl: " + name + ", "
                         + model);
    if (null != m_declHandler)
    {
      m_declHandler.elementDecl(name, model);
    }
  }
  public void attributeDecl(
          String eName, String aName, String type, String valueDefault, String value)
            throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#attributeDecl: " + eName
                         + ", " + aName + ", etc...");
    if (null != m_declHandler)
    {
      m_declHandler.attributeDecl(eName, aName, type, valueDefault, value);
    }
  }
  public void internalEntityDecl(String name, String value)
          throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#internalEntityDecl: " + name
                         + ", " + value);
    if (null != m_declHandler)
    {
      m_declHandler.internalEntityDecl(name, value);
    }
  }
  public void externalEntityDecl(
          String name, String publicId, String systemId) throws SAXException
  {
    if (DEBUG)
      System.out.println("TransformerHandlerImpl#externalEntityDecl: " + name
                         + ", " + publicId + ", " + systemId);
    if (null != m_declHandler)
    {
      m_declHandler.externalEntityDecl(name, publicId, systemId);
    }
  }
  private static boolean DEBUG = false;
  private TransformerImpl m_transformer;
  private String m_baseSystemID;
  private Result m_result = null;
  private Locator m_locator = null;
  private EntityResolver m_entityResolver = null;
  private DTDHandler m_dtdHandler = null;
  private ContentHandler m_contentHandler = null;
  private ErrorHandler m_errorHandler = null;
  private LexicalHandler m_lexicalHandler = null;
  private DeclHandler m_declHandler = null;
  DTM m_dtm;
}
