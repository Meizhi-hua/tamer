public class TreeWalker2Result extends DTMTreeWalker
{
  TransformerImpl m_transformer;
  SerializationHandler m_handler;
  int m_startNode;
  public TreeWalker2Result(TransformerImpl transformer,
                           SerializationHandler handler)
  {
    super(handler, null);
    m_transformer = transformer;
    m_handler = handler;
  }
  public void traverse(int pos) throws org.xml.sax.SAXException
  {
    m_dtm = m_transformer.getXPathContext().getDTM(pos);
    m_startNode = pos;
    super.traverse(pos);
  }
  protected void endNode(int node) throws org.xml.sax.SAXException
  {
    super.endNode(node);
    if(DTM.ELEMENT_NODE == m_dtm.getNodeType(node))
    {
      m_transformer.getXPathContext().popCurrentNode();
    }
  }
  protected void startNode(int node) throws org.xml.sax.SAXException
  {
    XPathContext xcntxt = m_transformer.getXPathContext();
    try
    {
      if (DTM.ELEMENT_NODE == m_dtm.getNodeType(node))
      {
        xcntxt.pushCurrentNode(node);                   
        if(m_startNode != node)
        {
          super.startNode(node);
        }
        else
        {
          String elemName = m_dtm.getNodeName(node);
          String localName = m_dtm.getLocalName(node);
          String namespace = m_dtm.getNamespaceURI(node);
          m_handler.startElement(namespace, localName, elemName);
          boolean hasNSDecls = false;
          DTM dtm = m_dtm;
          for (int ns = dtm.getFirstNamespaceNode(node, true); 
               DTM.NULL != ns; ns = dtm.getNextNamespaceNode(node, ns, true))
          {
            SerializerUtils.ensureNamespaceDeclDeclared(m_handler,dtm, ns);
          }
          for (int attr = dtm.getFirstAttribute(node); 
               DTM.NULL != attr; attr = dtm.getNextAttribute(attr))
          {
            SerializerUtils.addAttribute(m_handler, attr);
          }
        }
      }
      else
      {
        xcntxt.pushCurrentNode(node);
        super.startNode(node);
        xcntxt.popCurrentNode();
      }
    }
    catch(javax.xml.transform.TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
}
