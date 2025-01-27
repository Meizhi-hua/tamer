public class DTMTreeWalker
{
  private ContentHandler m_contentHandler = null;
  protected DTM m_dtm;
  public void setDTM(DTM dtm)
  {
    m_dtm = dtm;
  }
  public ContentHandler getcontentHandler()
  {
    return m_contentHandler;
  }
  public void setcontentHandler(ContentHandler ch)
  {
    m_contentHandler = ch;
  }
  public DTMTreeWalker()
  {
  }
  public DTMTreeWalker(ContentHandler contentHandler, DTM dtm)
  {
    this.m_contentHandler = contentHandler;
    m_dtm = dtm;
  }
  public void traverse(int pos) throws org.xml.sax.SAXException
  {
    int top = pos;		
    while (DTM.NULL != pos)
    {
      startNode(pos);
      int nextNode = m_dtm.getFirstChild(pos);
      while (DTM.NULL == nextNode)
      {
        endNode(pos);
        if (top == pos)
          break;
        nextNode = m_dtm.getNextSibling(pos);
        if (DTM.NULL == nextNode)
        {
          pos = m_dtm.getParent(pos);
          if ((DTM.NULL == pos) || (top == pos))
          {
            if (DTM.NULL != pos)
              endNode(pos);
            nextNode = DTM.NULL;
            break;
          }
        }
      }
      pos = nextNode;
    }
  }
  public void traverse(int pos, int top) throws org.xml.sax.SAXException
  {
    while (DTM.NULL != pos)
    {
      startNode(pos);
      int nextNode = m_dtm.getFirstChild(pos);
      while (DTM.NULL == nextNode)
      {
        endNode(pos);
        if ((DTM.NULL != top) && top == pos)
          break;
        nextNode = m_dtm.getNextSibling(pos);
        if (DTM.NULL == nextNode)
        {
          pos = m_dtm.getParent(pos);
          if ((DTM.NULL == pos) || ((DTM.NULL != top) && (top == pos)))
          {
            nextNode = DTM.NULL;
            break;
          }
        }
      }
      pos = nextNode;
    }
  }
  boolean nextIsRaw = false;
  private final void dispatachChars(int node)
     throws org.xml.sax.SAXException
  {
    m_dtm.dispatchCharactersEvents(node, m_contentHandler, false);
  }
  protected void startNode(int node) throws org.xml.sax.SAXException
  {
    if (m_contentHandler instanceof NodeConsumer)
    {
    }
    switch (m_dtm.getNodeType(node))
    {
    case DTM.COMMENT_NODE :
    {
      XMLString data = m_dtm.getStringValue(node);
      if (m_contentHandler instanceof LexicalHandler)
      {
        LexicalHandler lh = ((LexicalHandler) this.m_contentHandler);
        data.dispatchAsComment(lh);
      }
    }
    break;
    case DTM.DOCUMENT_FRAGMENT_NODE :
      break;
    case DTM.DOCUMENT_NODE :
      this.m_contentHandler.startDocument();
      break;
    case DTM.ELEMENT_NODE :
      DTM dtm = m_dtm;           
      for (int nsn = dtm.getFirstNamespaceNode(node, true); DTM.NULL != nsn;
           nsn = dtm.getNextNamespaceNode(node, nsn, true))
      {
        String prefix = dtm.getNodeNameX(nsn);
        this.m_contentHandler.startPrefixMapping(prefix, dtm.getNodeValue(nsn));
      }
      String ns = dtm.getNamespaceURI(node);
      if(null == ns)
        ns = "";
      org.xml.sax.helpers.AttributesImpl attrs = 
                            new org.xml.sax.helpers.AttributesImpl();
      for (int i = dtm.getFirstAttribute(node); 
           i != DTM.NULL; 
           i = dtm.getNextAttribute(i)) 
      {
        attrs.addAttribute(dtm.getNamespaceURI(i), 
                           dtm.getLocalName(i), 
                           dtm.getNodeName(i), 
                           "CDATA", 
                           dtm.getNodeValue(i));
      }
      this.m_contentHandler.startElement(ns,
                                         m_dtm.getLocalName(node),
                                         m_dtm.getNodeName(node),
                                         attrs);
      break;
    case DTM.PROCESSING_INSTRUCTION_NODE :
    {
      String name = m_dtm.getNodeName(node);
      if (name.equals("xslt-next-is-raw"))
      {
        nextIsRaw = true;
      }
      else
      {
        this.m_contentHandler.processingInstruction(name,
                                                    m_dtm.getNodeValue(node));
      }
    }
    break;
    case DTM.CDATA_SECTION_NODE :
    {
      boolean isLexH = (m_contentHandler instanceof LexicalHandler);
      LexicalHandler lh = isLexH
                          ? ((LexicalHandler) this.m_contentHandler) : null;
      if (isLexH)
      {
        lh.startCDATA();
      }
      dispatachChars(node);
      {
        if (isLexH)
        {
          lh.endCDATA();
        }
      }
    }
    break;
    case DTM.TEXT_NODE :
    {
      if (nextIsRaw)
      {
        nextIsRaw = false;
        m_contentHandler.processingInstruction(javax.xml.transform.Result.PI_DISABLE_OUTPUT_ESCAPING, "");
        dispatachChars(node);
        m_contentHandler.processingInstruction(javax.xml.transform.Result.PI_ENABLE_OUTPUT_ESCAPING, "");
      }
      else
      {
        dispatachChars(node);
      }
    }
    break;
    case DTM.ENTITY_REFERENCE_NODE :
    {
      if (m_contentHandler instanceof LexicalHandler)
      {
        ((LexicalHandler) this.m_contentHandler).startEntity(
          m_dtm.getNodeName(node));
      }
      else
      {
      }
    }
    break;
    default :
    }
  }
  protected void endNode(int node) throws org.xml.sax.SAXException
  {
    switch (m_dtm.getNodeType(node))
    {
    case DTM.DOCUMENT_NODE :
      this.m_contentHandler.endDocument();
      break;
    case DTM.ELEMENT_NODE :
      String ns = m_dtm.getNamespaceURI(node);
      if(null == ns)
        ns = "";
      this.m_contentHandler.endElement(ns,
                                         m_dtm.getLocalName(node),
                                         m_dtm.getNodeName(node));
      for (int nsn = m_dtm.getFirstNamespaceNode(node, true); DTM.NULL != nsn;
           nsn = m_dtm.getNextNamespaceNode(node, nsn, true))
      {
        String prefix = m_dtm.getNodeNameX(nsn);
        this.m_contentHandler.endPrefixMapping(prefix);
      }
      break;
    case DTM.CDATA_SECTION_NODE :
      break;
    case DTM.ENTITY_REFERENCE_NODE :
    {
      if (m_contentHandler instanceof LexicalHandler)
      {
        LexicalHandler lh = ((LexicalHandler) this.m_contentHandler);
        lh.endEntity(m_dtm.getNodeName(node));
      }
    }
    break;
    default :
    }
  }
}  
