public class StylesheetPIHandler extends DefaultHandler
{
  String m_baseID;
  String m_media;
  String m_title;
  String m_charset;
  Vector m_stylesheets = new Vector();
  URIResolver m_uriResolver;
  public void setURIResolver(URIResolver resolver)
  {
    m_uriResolver = resolver;
  }
  public URIResolver getURIResolver()
  {
    return m_uriResolver;
  }
  public StylesheetPIHandler(String baseID, String media, String title,
                             String charset)
  {
    m_baseID = baseID;
    m_media = media;
    m_title = title;
    m_charset = charset;
  }
  public Source getAssociatedStylesheet()
  {
    int sz = m_stylesheets.size();
    if (sz > 0)
    {
      Source source = (Source) m_stylesheets.elementAt(sz-1);
      return source;      
    }
    else
      return null;
  }
  public void processingInstruction(String target, String data)
          throws org.xml.sax.SAXException
  {
    if (target.equals("xml-stylesheet"))
    {
      String href = null;  
      String type = null;  
      String title = null;  
      String media = null;  
      String charset = null;  
      boolean alternate = false;  
      StringTokenizer tokenizer = new StringTokenizer(data, " \t=\n", true);
      boolean lookedAhead = false; 
      Source source = null;
      String token = "";
      while (tokenizer.hasMoreTokens())
      {        
        if (!lookedAhead)
          token = tokenizer.nextToken();
        else
          lookedAhead = false;
        if (tokenizer.hasMoreTokens() && 
               (token.equals(" ") || token.equals("\t") || token.equals("=")))
          continue;
        String name = token;  
        if (name.equals("type"))
        { 
          token = tokenizer.nextToken();
          while (tokenizer.hasMoreTokens() && 
               (token.equals(" " ) || token.equals("\t") || token.equals("=")))
            token = tokenizer.nextToken();
          type = token.substring(1, token.length() - 1);
        }
        else if (name.equals("href"))
        {
          token = tokenizer.nextToken();
          while (tokenizer.hasMoreTokens() && 
               (token.equals(" " ) || token.equals("\t") || token.equals("=")))
            token = tokenizer.nextToken();
          href = token;
          if (tokenizer.hasMoreTokens())
          {
            token = tokenizer.nextToken();
            while ( token.equals("=") && tokenizer.hasMoreTokens())
            {  
              href = href + token + tokenizer.nextToken();
              if (tokenizer.hasMoreTokens())
              {  
                token = tokenizer.nextToken();
                lookedAhead = true;
              }
              else
              {
                break;
              }
            }
          }
          href = href.substring(1, href.length() - 1);
          try
          { 
            if (m_uriResolver != null) 
            {
              source = m_uriResolver.resolve(href, m_baseID);
            } 
           else 
            {
              href = SystemIDResolver.getAbsoluteURI(href, m_baseID);
              source = new SAXSource(new InputSource(href));
            }            
          }
          catch(TransformerException te)
          {
            throw new org.xml.sax.SAXException(te);
          }
        }
        else if (name.equals("title"))
        {
          token = tokenizer.nextToken();
          while (tokenizer.hasMoreTokens() && 
               (token.equals(" " ) || token.equals("\t") || token.equals("=")))
            token = tokenizer.nextToken();
          title = token.substring(1, token.length() - 1);
        }
        else if (name.equals("media"))
        {
          token = tokenizer.nextToken();
          while (tokenizer.hasMoreTokens() && 
               (token.equals(" " ) || token.equals("\t") || token.equals("=")))
            token = tokenizer.nextToken();
          media = token.substring(1, token.length() - 1);
        }
        else if (name.equals("charset"))
        {
          token = tokenizer.nextToken();
          while (tokenizer.hasMoreTokens() && 
              (token.equals(" " ) || token.equals("\t") || token.equals("=")))
            token = tokenizer.nextToken();
          charset = token.substring(1, token.length() - 1);
        }
        else if (name.equals("alternate"))
        {
          token = tokenizer.nextToken();
          while (tokenizer.hasMoreTokens() && 
               (token.equals(" " ) || token.equals("\t") || token.equals("=")))
            token = tokenizer.nextToken();
          alternate = token.substring(1, token.length()
                                             - 1).equals("yes");
        }
      }
      if ((null != type) 
          && (type.equals("text/xsl") || type.equals("text/xml") || type.equals("application/xml+xslt"))  
          && (null != href))
      {
        if (null != m_media)
        {
          if (null != media)
          {
            if (!media.equals(m_media))
              return;
          }
          else
            return;
        }
        if (null != m_charset)
        {
          if (null != charset)
          {
            if (!charset.equals(m_charset))
              return;
          }
          else
            return;
        }
        if (null != m_title)
        {
          if (null != title)
          {
            if (!title.equals(m_title))
              return;
          }
          else
            return;
        }
        m_stylesheets.addElement(source);
      }
    }
  }
  public void startElement(
          String namespaceURI, String localName, String qName, Attributes atts)
            throws org.xml.sax.SAXException
  {
    throw new StopParseException();
  }
   public void setBaseId(String baseId) {
       m_baseID = baseId;
   }
   public String  getBaseId() {
       return m_baseID ;
   }
}
