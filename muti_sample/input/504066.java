public class DOMHelper
{
  public static Document createDocument(boolean isSecureProcessing)
  {
    try
    {
      DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
      dfactory.setNamespaceAware(true);
      DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
      Document outNode = docBuilder.newDocument();
      return outNode;
    }
    catch (ParserConfigurationException pce)
    {
      throw new RuntimeException(
        XMLMessages.createXMLMessage(
          XMLErrorResources.ER_CREATEDOCUMENT_NOT_SUPPORTED, null));  
    }
  }
  public static Document createDocument()
  {
    return createDocument(false);
  }
  public boolean shouldStripSourceNode(Node textNode)
          throws javax.xml.transform.TransformerException
  {
    return false;
  }
  public String getUniqueID(Node node)
  {
    return "N" + Integer.toHexString(node.hashCode()).toUpperCase();
  }
  public static boolean isNodeAfter(Node node1, Node node2)
  {
    if (node1 == node2 || isNodeTheSame(node1, node2))
      return true;
    boolean isNodeAfter = true;
    Node parent1 = getParentOfNode(node1);
    Node parent2 = getParentOfNode(node2);          
    if (parent1 == parent2 || isNodeTheSame(parent1, parent2))  
    {
      if (null != parent1)
        isNodeAfter = isNodeAfterSibling(parent1, node1, node2);
      else
      {
      }
    }
    else
    {
      int nParents1 = 2, nParents2 = 2;  
      while (parent1 != null)
      {
        nParents1++;
        parent1 = getParentOfNode(parent1);
      }
      while (parent2 != null)
      {
        nParents2++;
        parent2 = getParentOfNode(parent2);
      }
      Node startNode1 = node1, startNode2 = node2;
      if (nParents1 < nParents2)
      {
        int adjust = nParents2 - nParents1;
        for (int i = 0; i < adjust; i++)
        {
          startNode2 = getParentOfNode(startNode2);
        }
      }
      else if (nParents1 > nParents2)
      {
        int adjust = nParents1 - nParents2;
        for (int i = 0; i < adjust; i++)
        {
          startNode1 = getParentOfNode(startNode1);
        }
      }
      Node prevChild1 = null, prevChild2 = null;  
      while (null != startNode1)
      {
        if (startNode1 == startNode2 || isNodeTheSame(startNode1, startNode2))  
        {
          if (null == prevChild1)  
          {
            isNodeAfter = (nParents1 < nParents2) ? true : false;
            break;  
          }
          else 
          {
            isNodeAfter = isNodeAfterSibling(startNode1, prevChild1,
                                             prevChild2);
            break;  
          }
        }  
        prevChild1 = startNode1;
        startNode1 = getParentOfNode(startNode1);
        prevChild2 = startNode2;
        startNode2 = getParentOfNode(startNode2);
      }  
    }  
    return isNodeAfter;
  }  
  public static boolean isNodeTheSame(Node node1, Node node2)
  {
    if (node1 instanceof DTMNodeProxy && node2 instanceof DTMNodeProxy)
      return ((DTMNodeProxy)node1).equals((DTMNodeProxy)node2);
    else
      return (node1 == node2);
  }
  private static boolean isNodeAfterSibling(Node parent, Node child1,
                                            Node child2)
  {
    boolean isNodeAfterSibling = false;
    short child1type = child1.getNodeType();
    short child2type = child2.getNodeType();
    if ((Node.ATTRIBUTE_NODE != child1type)
            && (Node.ATTRIBUTE_NODE == child2type))
    {
      isNodeAfterSibling = false;
    }
    else if ((Node.ATTRIBUTE_NODE == child1type)
             && (Node.ATTRIBUTE_NODE != child2type))
    {
      isNodeAfterSibling = true;
    }
    else if (Node.ATTRIBUTE_NODE == child1type)
    {
      NamedNodeMap children = parent.getAttributes();
      int nNodes = children.getLength();
      boolean found1 = false, found2 = false;
      for (int i = 0; i < nNodes; i++)
      {
        Node child = children.item(i);
        if (child1 == child || isNodeTheSame(child1, child))
        {
          if (found2)
          {
            isNodeAfterSibling = false;
            break;
          }
          found1 = true;
        }
        else if (child2 == child || isNodeTheSame(child2, child))
        {
          if (found1)
          {
            isNodeAfterSibling = true;
            break;
          }
          found2 = true;
        }
      }
    }
    else
    {
      Node child = parent.getFirstChild();
      boolean found1 = false, found2 = false;
      while (null != child)
      {
        if (child1 == child || isNodeTheSame(child1, child))
        {
          if (found2)
          {
            isNodeAfterSibling = false;
            break;
          }
          found1 = true;
        }
        else if (child2 == child || isNodeTheSame(child2, child))
        {
          if (found1)
          {
            isNodeAfterSibling = true;
            break;
          }
          found2 = true;
        }
        child = child.getNextSibling();
      }
    }
    return isNodeAfterSibling;
  }  
  public short getLevel(Node n)
  {
    short level = 1;
    while (null != (n = getParentOfNode(n)))
    {
      level++;
    }
    return level;
  }
  public String getNamespaceForPrefix(String prefix, Element namespaceContext)
  {
    int type;
    Node parent = namespaceContext;
    String namespace = null;
    if (prefix.equals("xml"))
    {
      namespace = QName.S_XMLNAMESPACEURI; 
    }
        else if(prefix.equals("xmlns"))
    {
      namespace = "http:
    }
    else
    {
          String declname=(prefix=="")
                        ? "xmlns"
                        : "xmlns:"+prefix;
      while ((null != parent) && (null == namespace)
             && (((type = parent.getNodeType()) == Node.ELEMENT_NODE)
                 || (type == Node.ENTITY_REFERENCE_NODE)))
      {
        if (type == Node.ELEMENT_NODE)
        {
                        Attr attr=((Element)parent).getAttributeNode(declname);
                        if(attr!=null)
                        {
                namespace = attr.getNodeValue();
                break;
                        }
                }
        parent = getParentOfNode(parent);
      }
    }
    return namespace;
  }
  Hashtable m_NSInfos = new Hashtable();
  protected static final NSInfo m_NSInfoUnProcWithXMLNS = new NSInfo(false,
                                                            true);
  protected static final NSInfo m_NSInfoUnProcWithoutXMLNS = new NSInfo(false,
                                                               false);
  protected static final NSInfo m_NSInfoUnProcNoAncestorXMLNS =
    new NSInfo(false, false, NSInfo.ANCESTORNOXMLNS);
  protected static final NSInfo m_NSInfoNullWithXMLNS = new NSInfo(true,
                                                          true);
  protected static final NSInfo m_NSInfoNullWithoutXMLNS = new NSInfo(true,
                                                             false);
  protected static final NSInfo m_NSInfoNullNoAncestorXMLNS =
    new NSInfo(true, false, NSInfo.ANCESTORNOXMLNS);
  protected Vector m_candidateNoAncestorXMLNS = new Vector();
  public String getNamespaceOfNode(Node n)
  {
    String namespaceOfPrefix;
    boolean hasProcessedNS;
    NSInfo nsInfo;
    short ntype = n.getNodeType();
    if (Node.ATTRIBUTE_NODE != ntype)
    {
      Object nsObj = m_NSInfos.get(n);  
      nsInfo = (nsObj == null) ? null : (NSInfo) nsObj;
      hasProcessedNS = (nsInfo == null) ? false : nsInfo.m_hasProcessedNS;
    }
    else
    {
      hasProcessedNS = false;
      nsInfo = null;
    }
    if (hasProcessedNS)
    {
      namespaceOfPrefix = nsInfo.m_namespace;
    }
    else
    {
      namespaceOfPrefix = null;
      String nodeName = n.getNodeName();
      int indexOfNSSep = nodeName.indexOf(':');
      String prefix;
      if (Node.ATTRIBUTE_NODE == ntype)
      {
        if (indexOfNSSep > 0)
        {
          prefix = nodeName.substring(0, indexOfNSSep);
        }
        else
        {
          return namespaceOfPrefix;
        }
      }
      else
      {
        prefix = (indexOfNSSep >= 0)
                 ? nodeName.substring(0, indexOfNSSep) : "";
      }
      boolean ancestorsHaveXMLNS = false;
      boolean nHasXMLNS = false;
      if (prefix.equals("xml"))
      {
        namespaceOfPrefix = QName.S_XMLNAMESPACEURI;
      }
      else
      {
        int parentType;
        Node parent = n;
        while ((null != parent) && (null == namespaceOfPrefix))
        {
          if ((null != nsInfo)
                  && (nsInfo.m_ancestorHasXMLNSAttrs
                      == NSInfo.ANCESTORNOXMLNS))
          {
            break;
          }
          parentType = parent.getNodeType();
          if ((null == nsInfo) || nsInfo.m_hasXMLNSAttrs)
          {
            boolean elementHasXMLNS = false;
            if (parentType == Node.ELEMENT_NODE)
            {
              NamedNodeMap nnm = parent.getAttributes();
              for (int i = 0; i < nnm.getLength(); i++)
              {
                Node attr = nnm.item(i);
                String aname = attr.getNodeName();
                if (aname.charAt(0) == 'x')
                {
                  boolean isPrefix = aname.startsWith("xmlns:");
                  if (aname.equals("xmlns") || isPrefix)
                  {
                    if (n == parent)
                      nHasXMLNS = true;
                    elementHasXMLNS = true;
                    ancestorsHaveXMLNS = true;
                    String p = isPrefix ? aname.substring(6) : "";
                    if (p.equals(prefix))
                    {
                      namespaceOfPrefix = attr.getNodeValue();
                      break;
                    }
                  }
                }
              }
            }
            if ((Node.ATTRIBUTE_NODE != parentType) && (null == nsInfo)
                    && (n != parent))
            {
              nsInfo = elementHasXMLNS
                       ? m_NSInfoUnProcWithXMLNS : m_NSInfoUnProcWithoutXMLNS;
              m_NSInfos.put(parent, nsInfo);
            }
          }
          if (Node.ATTRIBUTE_NODE == parentType)
          {
            parent = getParentOfNode(parent);
          }
          else
          {
            m_candidateNoAncestorXMLNS.addElement(parent);
            m_candidateNoAncestorXMLNS.addElement(nsInfo);
            parent = parent.getParentNode();
          }
          if (null != parent)
          {
            Object nsObj = m_NSInfos.get(parent);  
            nsInfo = (nsObj == null) ? null : (NSInfo) nsObj;
          }
        }
        int nCandidates = m_candidateNoAncestorXMLNS.size();
        if (nCandidates > 0)
        {
          if ((false == ancestorsHaveXMLNS) && (null == parent))
          {
            for (int i = 0; i < nCandidates; i += 2)
            {
              Object candidateInfo = m_candidateNoAncestorXMLNS.elementAt(i
                                       + 1);
              if (candidateInfo == m_NSInfoUnProcWithoutXMLNS)
              {
                m_NSInfos.put(m_candidateNoAncestorXMLNS.elementAt(i),
                              m_NSInfoUnProcNoAncestorXMLNS);
              }
              else if (candidateInfo == m_NSInfoNullWithoutXMLNS)
              {
                m_NSInfos.put(m_candidateNoAncestorXMLNS.elementAt(i),
                              m_NSInfoNullNoAncestorXMLNS);
              }
            }
          }
          m_candidateNoAncestorXMLNS.removeAllElements();
        }
      }
      if (Node.ATTRIBUTE_NODE != ntype)
      {
        if (null == namespaceOfPrefix)
        {
          if (ancestorsHaveXMLNS)
          {
            if (nHasXMLNS)
              m_NSInfos.put(n, m_NSInfoNullWithXMLNS);
            else
              m_NSInfos.put(n, m_NSInfoNullWithoutXMLNS);
          }
          else
          {
            m_NSInfos.put(n, m_NSInfoNullNoAncestorXMLNS);
          }
        }
        else
        {
          m_NSInfos.put(n, new NSInfo(namespaceOfPrefix, nHasXMLNS));
        }
      }
    }
    return namespaceOfPrefix;
  }
  public String getLocalNameOfNode(Node n)
  {
    String qname = n.getNodeName();
    int index = qname.indexOf(':');
    return (index < 0) ? qname : qname.substring(index + 1);
  }
  public String getExpandedElementName(Element elem)
  {
    String namespace = getNamespaceOfNode(elem);
    return (null != namespace)
           ? namespace + ":" + getLocalNameOfNode(elem)
           : getLocalNameOfNode(elem);
  }
  public String getExpandedAttributeName(Attr attr)
  {
    String namespace = getNamespaceOfNode(attr);
    return (null != namespace)
           ? namespace + ":" + getLocalNameOfNode(attr)
           : getLocalNameOfNode(attr);
  }
  public boolean isIgnorableWhitespace(Text node)
  {
    boolean isIgnorable = false;  
    return isIgnorable;
  }
  public Node getRoot(Node node)
  {
    Node root = null;
    while (node != null)
    {
      root = node;
      node = getParentOfNode(node);
    }
    return root;
  }
  public Node getRootNode(Node n)
  {
    int nt = n.getNodeType();
    return ( (Node.DOCUMENT_NODE == nt) || (Node.DOCUMENT_FRAGMENT_NODE == nt) ) 
           ? n : n.getOwnerDocument();
  }
  public boolean isNamespaceNode(Node n)
  {
    if (Node.ATTRIBUTE_NODE == n.getNodeType())
    {
      String attrName = n.getNodeName();
      return (attrName.startsWith("xmlns:") || attrName.equals("xmlns"));
    }
    return false;
  }
  public static Node getParentOfNode(Node node) throws RuntimeException
  {
    Node parent;
    short nodeType = node.getNodeType();
    if (Node.ATTRIBUTE_NODE == nodeType)
    {
      Document doc = node.getOwnerDocument();
          DOMImplementation impl=doc.getImplementation();
          if(impl!=null && impl.hasFeature("Core","2.0"))
          {
                  parent=((Attr)node).getOwnerElement();
                  return parent;
          }
      Element rootElem = doc.getDocumentElement();
      if (null == rootElem)
      {
        throw new RuntimeException(
          XMLMessages.createXMLMessage(
            XMLErrorResources.ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT,
            null));  
      }
      parent = locateAttrParent(rootElem, node);
        }
    else
    {
      parent = node.getParentNode();
    }
    return parent;
  }
  public Element getElementByID(String id, Document doc)
  {
    return null;
  }
  public String getUnparsedEntityURI(String name, Document doc)
  {
    String url = "";
    DocumentType doctype = doc.getDoctype();
    if (null != doctype)
    {
      NamedNodeMap entities = doctype.getEntities();
      if(null == entities)
        return url;
      Entity entity = (Entity) entities.getNamedItem(name);
      if(null == entity)
        return url;
      String notationName = entity.getNotationName();
      if (null != notationName)  
      {
        url = entity.getSystemId();
        if (null == url)
        {
          url = entity.getPublicId();
        }
        else
        {
        }        
      }
    }
    return url;
  }
  private static Node locateAttrParent(Element elem, Node attr)
  {
    Node parent = null;
        Attr check=elem.getAttributeNode(attr.getNodeName());
        if(check==attr)
                parent = elem;
    if (null == parent)
    {
      for (Node node = elem.getFirstChild(); null != node;
              node = node.getNextSibling())
      {
        if (Node.ELEMENT_NODE == node.getNodeType())
        {
          parent = locateAttrParent((Element) node, attr);
          if (null != parent)
            break;
        }
      }
    }
    return parent;
  }
  protected Document m_DOMFactory = null;
  public void setDOMFactory(Document domFactory)
  {
    this.m_DOMFactory = domFactory;
  }
  public Document getDOMFactory()
  {
    if (null == this.m_DOMFactory)
    {
      this.m_DOMFactory = createDocument();
    }
    return this.m_DOMFactory;
  }
  public static String getNodeData(Node node)
  {
    FastStringBuffer buf = StringBufferPool.get();
    String s;
    try
    {
      getNodeData(node, buf);
      s = (buf.length() > 0) ? buf.toString() : "";
    }
    finally
    {
      StringBufferPool.free(buf);
    }
    return s;
  }
  public static void getNodeData(Node node, FastStringBuffer buf)
  {
    switch (node.getNodeType())
    {
    case Node.DOCUMENT_FRAGMENT_NODE :
    case Node.DOCUMENT_NODE :
    case Node.ELEMENT_NODE :
    {
      for (Node child = node.getFirstChild(); null != child;
              child = child.getNextSibling())
      {
        getNodeData(child, buf);
      }
    }
    break;
    case Node.TEXT_NODE :
    case Node.CDATA_SECTION_NODE :
      buf.append(node.getNodeValue());
      break;
    case Node.ATTRIBUTE_NODE :
      buf.append(node.getNodeValue());
      break;
    case Node.PROCESSING_INSTRUCTION_NODE :
      break;
    default :
      break;
    }
  }
}
