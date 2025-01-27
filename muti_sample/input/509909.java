public class NodeSetDTM extends NodeVector
        implements  DTMIterator, 
        Cloneable
{
    static final long serialVersionUID = 7686480133331317070L;
  public NodeSetDTM(DTMManager dtmManager)
  {
    super();
    m_manager = dtmManager;
  }
  public NodeSetDTM(int blocksize, int dummy, DTMManager dtmManager)
  {
    super(blocksize);
    m_manager = dtmManager;
  }
  public NodeSetDTM(NodeSetDTM nodelist)
  {
    super();
    m_manager = nodelist.getDTMManager();
    m_root = nodelist.getRoot();
    addNodes((DTMIterator) nodelist);
  }
  public NodeSetDTM(DTMIterator ni)
  {
    super();
    m_manager = ni.getDTMManager();
    m_root = ni.getRoot();
    addNodes(ni);
  }
  public NodeSetDTM(NodeIterator iterator, XPathContext xctxt)
  {
    super();
    Node node;
    m_manager = xctxt.getDTMManager();
    while (null != (node = iterator.nextNode()))
    {
      int handle = xctxt.getDTMHandleFromNode(node);
      addNodeInDocOrder(handle, xctxt);
    }
  }
  public NodeSetDTM(NodeList nodeList, XPathContext xctxt)
  {
    super();
    m_manager = xctxt.getDTMManager();
    int n = nodeList.getLength();
    for (int i = 0; i < n; i++) 
    {
      Node node = nodeList.item(i);
      int handle = xctxt.getDTMHandleFromNode(node);
      addNode(handle); 
    } 
  }
  public NodeSetDTM(int node, DTMManager dtmManager)
  {
    super();
    m_manager = dtmManager;
    addNode(node);
  }
  public void setEnvironment(Object environment)
  {
  }
  public int getRoot()
  {
    if(DTM.NULL == m_root)
    {
      if(size() > 0)
        return item(0);
      else
        return DTM.NULL;
    }
    else
      return m_root;
  }
  public void setRoot(int context, Object environment)
  {
  }
  public Object clone() throws CloneNotSupportedException
  {
    NodeSetDTM clone = (NodeSetDTM) super.clone();
    return clone;
  }
  public DTMIterator cloneWithReset() throws CloneNotSupportedException
  {
    NodeSetDTM clone = (NodeSetDTM) clone();
    clone.reset();
    return clone;
  }
  public void reset()
  {
    m_next = 0;
  }
  public int getWhatToShow()
  {
    return DTMFilter.SHOW_ALL & ~DTMFilter.SHOW_ENTITY_REFERENCE;
  }
  public DTMFilter getFilter()
  {
    return null;
  }
  public boolean getExpandEntityReferences()
  {
    return true;
  }
  public DTM getDTM(int nodeHandle)
  {
    return m_manager.getDTM(nodeHandle);
  }
  DTMManager m_manager;
  public DTMManager getDTMManager()
  {
    return m_manager;
  }
  public int nextNode()
  {
    if ((m_next) < this.size())
    {
      int next = this.elementAt(m_next);
      m_next++;
      return next;
    }
    else
      return DTM.NULL;
  }
  public int previousNode()
  {
    if (!m_cacheNodes)
      throw new RuntimeException(
        XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_CANNOT_ITERATE, null)); 
    if ((m_next - 1) > 0)
    {
      m_next--;
      return this.elementAt(m_next);
    }
    else
      return DTM.NULL;
  }
  public void detach(){}
  public void allowDetachToRelease(boolean allowRelease)
  {
  }
  public boolean isFresh()
  {
    return (m_next == 0);
  }
  public void runTo(int index)
  {
    if (!m_cacheNodes)
      throw new RuntimeException(
        XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_CANNOT_INDEX, null)); 
    if ((index >= 0) && (m_next < m_firstFree))
      m_next = index;
    else
      m_next = m_firstFree - 1;
  }
  public int item(int index)
  {
    runTo(index);
    return this.elementAt(index);
  }
  public int getLength()
  {
    runTo(-1);
    return this.size();
  }
  public void addNode(int n)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    this.addElement(n);
  }
  public void insertNode(int n, int pos)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    insertElementAt(n, pos);
  }
  public void removeNode(int n)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    this.removeElement(n);
  }
  public void addNodes(DTMIterator iterator)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    if (null != iterator)  
    {
      int obj;
      while (DTM.NULL != (obj = iterator.nextNode()))
      {
        addElement(obj);
      }
    }
  }
  public void addNodesInDocOrder(DTMIterator iterator, XPathContext support)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    int node;
    while (DTM.NULL != (node = iterator.nextNode()))
    {
      addNodeInDocOrder(node, support);
    }
  }
  public int addNodeInDocOrder(int node, boolean test, XPathContext support)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    int insertIndex = -1;
    if (test)
    {
      int size = size(), i;
      for (i = size - 1; i >= 0; i--)
      {
        int child = elementAt(i);
        if (child == node)
        {
          i = -2;  
          break;
        }
        DTM dtm = support.getDTM(node);
        if (!dtm.isNodeAfter(node, child))
        {
          break;
        }
      }
      if (i != -2)
      {
        insertIndex = i + 1;
        insertElementAt(node, insertIndex);
      }
    }
    else
    {
      insertIndex = this.size();
      boolean foundit = false;
      for (int i = 0; i < insertIndex; i++)
      {
        if (i == node)
        {
          foundit = true;
          break;
        }
      }
      if (!foundit)
        addElement(node);
    }
    return insertIndex;
  }  
  public int addNodeInDocOrder(int node, XPathContext support)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    return addNodeInDocOrder(node, true, support);
  }  
  public int size()
  {
    return super.size();
  }
  public void addElement(int value)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    super.addElement(value);
  }
  public void insertElementAt(int value, int at)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    super.insertElementAt(value, at);
  }
  public void appendNodes(NodeVector nodes)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    super.appendNodes(nodes);
  }
  public void removeAllElements()
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    super.removeAllElements();
  }
  public boolean removeElement(int s)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    return super.removeElement(s);
  }
  public void removeElementAt(int i)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    super.removeElementAt(i);
  }
  public void setElementAt(int node, int index)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    super.setElementAt(node, index);
  }
  public void setItem(int node, int index)
  {
    if (!m_mutable)
      throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); 
    super.setElementAt(node, index);
  }
  public int elementAt(int i)
  {
    runTo(i);
    return super.elementAt(i);
  }
  public boolean contains(int s)
  {
    runTo(-1);
    return super.contains(s);
  }
  public int indexOf(int elem, int index)
  {
    runTo(-1);
    return super.indexOf(elem, index);
  }
  public int indexOf(int elem)
  {
    runTo(-1);
    return super.indexOf(elem);
  }
  transient protected int m_next = 0;
  public int getCurrentPos()
  {
    return m_next;
  }
  public void setCurrentPos(int i)
  {
    if (!m_cacheNodes)
      throw new RuntimeException(
        XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_CANNOT_INDEX, null)); 
    m_next = i;
  }
  public int getCurrentNode()
  {
    if (!m_cacheNodes)
      throw new RuntimeException(
        "This NodeSetDTM can not do indexing or counting functions!");
    int saved = m_next;
    int current = (m_next > 0) ? m_next-1 : m_next; 
    int n = (current < m_firstFree) ? elementAt(current) : DTM.NULL;
    m_next = saved; 
    return n;
  }
  transient protected boolean m_mutable = true;
  transient protected boolean m_cacheNodes = true;
  protected int m_root = DTM.NULL;
  public boolean getShouldCacheNodes()
  {
    return m_cacheNodes;
  }
  public void setShouldCacheNodes(boolean b)
  {
    if (!isFresh())
      throw new RuntimeException(
        XSLMessages.createXPATHMessage(XPATHErrorResources.ER_CANNOT_CALL_SETSHOULDCACHENODE, null)); 
    m_cacheNodes = b;
    m_mutable = true;
  }
  public boolean isMutable()
  {
    return m_mutable;
  }
  transient private int m_last = 0;
  public int getLast()
  {
    return m_last;
  }
  public void setLast(int last)
  {
    m_last = last;
  }
  public boolean isDocOrdered()
  {
    return true;
  }
  public int getAxis()
  {
    return -1;
  }
}
