public class SourceTreeManager
{
  private Vector m_sourceTree = new Vector();
  public void reset()
  {
    m_sourceTree = new Vector();
  }
  URIResolver m_uriResolver;
  public void setURIResolver(URIResolver resolver)
  {
    m_uriResolver = resolver;
  }
  public URIResolver getURIResolver()
  {
    return m_uriResolver;
  }
  public String findURIFromDoc(int owner)
  {
    int n = m_sourceTree.size();
    for (int i = 0; i < n; i++)
    {
      SourceTree sTree = (SourceTree) m_sourceTree.elementAt(i);
      if (owner == sTree.m_root)
        return sTree.m_url;
    }
    return null;
  }
  public Source resolveURI(
          String base, String urlString, SourceLocator locator)
            throws TransformerException, IOException
  {
    Source source = null;
    if (null != m_uriResolver)
    {
      source = m_uriResolver.resolve(urlString, base);
    }
    if (null == source)
    {
      String uri = SystemIDResolver.getAbsoluteURI(urlString, base);
      source = new StreamSource(uri);
    }
    return source;
  }
  public void removeDocumentFromCache(int n)
  {
    if(DTM.NULL ==n)
      return;
    for(int i=m_sourceTree.size()-1;i>=0;--i)
    {
      SourceTree st=(SourceTree)m_sourceTree.elementAt(i);
      if(st!=null && st.m_root==n)
      {
	m_sourceTree.removeElementAt(i);
	return;
      }
    }
  }
  public void putDocumentInCache(int n, Source source)
  {
    int cachedNode = getNode(source);
    if (DTM.NULL != cachedNode)
    {
      if (!(cachedNode == n))
        throw new RuntimeException(
          "Programmer's Error!  "
          + "putDocumentInCache found reparse of doc: "
          + source.getSystemId());
      return;
    }
    if (null != source.getSystemId())
    {
      m_sourceTree.addElement(new SourceTree(n, source.getSystemId()));
    }
  }
  public int getNode(Source source)
  {
    String url = source.getSystemId();
    if (null == url)
      return DTM.NULL;
    int n = m_sourceTree.size();
    for (int i = 0; i < n; i++)
    {
      SourceTree sTree = (SourceTree) m_sourceTree.elementAt(i);
      if (url.equals(sTree.m_url))
        return sTree.m_root;
    }
    return DTM.NULL;
  }
  public int getSourceTree(
          String base, String urlString, SourceLocator locator, XPathContext xctxt)
            throws TransformerException
  {
    try
    {
      Source source = this.resolveURI(base, urlString, locator);
      return getSourceTree(source, locator, xctxt);
    }
    catch (IOException ioe)
    {
      throw new TransformerException(ioe.getMessage(), locator, ioe);
    }
  }
  public int getSourceTree(Source source, SourceLocator locator, XPathContext xctxt)
          throws TransformerException
  {
    int n = getNode(source);
    if (DTM.NULL != n)
      return n;
    n = parseToNode(source, locator, xctxt);
    if (DTM.NULL != n)
      putDocumentInCache(n, source);
    return n;
  }
  public int parseToNode(Source source, SourceLocator locator, XPathContext xctxt)
          throws TransformerException
  {
    try
    {      
      Object xowner = xctxt.getOwnerObject();
      DTM dtm;
      if(null != xowner && xowner instanceof org.apache.xml.dtm.DTMWSFilter)
      {
        dtm = xctxt.getDTM(source, false, 
                          (org.apache.xml.dtm.DTMWSFilter)xowner, false, true);
      }
      else
      {
        dtm = xctxt.getDTM(source, false, null, false, true);
      }
      return dtm.getDocument();
    }
    catch (Exception e)
    {
      throw new TransformerException(e.getMessage(), locator, e);
    }
  }
  public static XMLReader getXMLReader(Source inputSource, SourceLocator locator)
          throws TransformerException
  {
    try
    {
      XMLReader reader = (inputSource instanceof SAXSource)
                         ? ((SAXSource) inputSource).getXMLReader() : null;
      if (null == reader)
      {
        try {
          javax.xml.parsers.SAXParserFactory factory=
              javax.xml.parsers.SAXParserFactory.newInstance();
          factory.setNamespaceAware( true );
          javax.xml.parsers.SAXParser jaxpParser=
              factory.newSAXParser();
          reader=jaxpParser.getXMLReader();
        } catch( javax.xml.parsers.ParserConfigurationException ex ) {
          throw new org.xml.sax.SAXException( ex );
        } catch( javax.xml.parsers.FactoryConfigurationError ex1 ) {
            throw new org.xml.sax.SAXException( ex1.toString() );
        } catch( NoSuchMethodError ex2 ) {
        }
        catch (AbstractMethodError ame){}
        if(null == reader)
          reader = XMLReaderFactory.createXMLReader();
      }
      try
      {
        reader.setFeature("http:
                          true);
      }
      catch (org.xml.sax.SAXException se)
      {
      }
      return reader;
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new TransformerException(se.getMessage(), locator, se);
    }
  }
}
