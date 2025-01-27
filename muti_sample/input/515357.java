public abstract class LocPathIterator extends PredicatedNodeTest
        implements Cloneable, DTMIterator, java.io.Serializable, PathComponent
{
    static final long serialVersionUID = -4602476357268405754L;
  protected LocPathIterator()
  {
  }
  protected LocPathIterator(PrefixResolver nscontext)
  {
    setLocPathIterator(this);
    m_prefixResolver = nscontext;
  }
  protected LocPathIterator(Compiler compiler, int opPos, int analysis)
          throws javax.xml.transform.TransformerException
  {
    this(compiler, opPos, analysis, true);
  }
  protected LocPathIterator(
          Compiler compiler, int opPos, int analysis, boolean shouldLoadWalkers)
            throws javax.xml.transform.TransformerException
  {
    setLocPathIterator(this);
  }
  public int getAnalysisBits()
  {
  	int axis = getAxis();
  	int bit = WalkerFactory.getAnalysisBitFromAxes(axis);
  	return bit;
  }
  private void readObject(java.io.ObjectInputStream stream)
          throws java.io.IOException, javax.xml.transform.TransformerException
  {
    try
    {
      stream.defaultReadObject();
      m_clones =  new IteratorPool(this);
    }
    catch (ClassNotFoundException cnfe)
    {
      throw new javax.xml.transform.TransformerException(cnfe);
    }
  }
  public void setEnvironment(Object environment)
  {
  }
  public DTM getDTM(int nodeHandle)
  {
    return m_execContext.getDTM(nodeHandle);
  }
  public DTMManager getDTMManager()
  {
    return m_execContext.getDTMManager();
  }
  public XObject execute(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    XNodeSet iter = new XNodeSet((LocPathIterator)m_clones.getInstance());
    iter.setRoot(xctxt.getCurrentNode(), xctxt);
    return iter;
  }
  public void executeCharsToContentHandler(
          XPathContext xctxt, org.xml.sax.ContentHandler handler)
            throws javax.xml.transform.TransformerException,
                   org.xml.sax.SAXException
  {
    LocPathIterator clone = (LocPathIterator)m_clones.getInstance();
    int current = xctxt.getCurrentNode();
    clone.setRoot(current, xctxt);
    int node = clone.nextNode();
    DTM dtm = clone.getDTM(node);
    clone.detach();
    if(node != DTM.NULL)
    {
      dtm.dispatchCharactersEvents(node, handler, false);
    }
  }
  public DTMIterator asIterator(
          XPathContext xctxt, int contextNode)
            throws javax.xml.transform.TransformerException
  {
    XNodeSet iter = new XNodeSet((LocPathIterator)m_clones.getInstance());
    iter.setRoot(contextNode, xctxt);
    return iter;
  }
  public boolean isNodesetExpr()
  {
    return true;
  }
  public int asNode(XPathContext xctxt)
    throws javax.xml.transform.TransformerException
  {
    DTMIterator iter = (DTMIterator)m_clones.getInstance();
    int current = xctxt.getCurrentNode();
    iter.setRoot(current, xctxt);
    int next = iter.nextNode();
    iter.detach();
    return next;
  }
  public boolean bool(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    return (asNode(xctxt) != DTM.NULL);
  }
  public void setIsTopLevel(boolean b)
  {
    m_isTopLevel = b;
  }
  public boolean getIsTopLevel()
  {
    return m_isTopLevel;
  }
  public void setRoot(int context, Object environment)
  {
    m_context = context;
    XPathContext xctxt = (XPathContext)environment;
    m_execContext = xctxt;
    m_cdtm = xctxt.getDTM(context);
    m_currentContextNode = context; 
    if(null == m_prefixResolver)
    	m_prefixResolver = xctxt.getNamespaceContext();
    m_lastFetched = DTM.NULL;
    m_foundLast = false;
    m_pos = 0;
    m_length = -1;
    if (m_isTopLevel)
      this.m_stackFrame = xctxt.getVarStack().getStackFrame();
  }
  protected void setNextPosition(int next)
  {
    assertion(false, "setNextPosition not supported in this iterator!");
  }
  public final int getCurrentPos()
  {
    return m_pos;
  }
  public void setShouldCacheNodes(boolean b)
  {
    assertion(false, "setShouldCacheNodes not supported by this iterater!");
  }
  public boolean isMutable()
  {
    return false;
  }
  public void setCurrentPos(int i)
  {
  	assertion(false, "setCurrentPos not supported by this iterator!");
  }
  public void incrementCurrentPos()
  {
  	m_pos++;
  }
  public int size()
  {
	assertion(false, "size() not supported by this iterator!");
	return 0;
  }
  public int item(int index)
  {
	assertion(false, "item(int index) not supported by this iterator!");
	return 0;
  }
  public void setItem(int node, int index)
  {
	assertion(false, "setItem not supported by this iterator!");
  }
  public int getLength()
  {      
  	boolean isPredicateTest = (this == m_execContext.getSubContextList());
  	int predCount = getPredicateCount();
    if (-1 != m_length && isPredicateTest && m_predicateIndex < 1)
  		return m_length;
    if (m_foundLast)
  		return m_pos;
    int pos = (m_predicateIndex >= 0) ? getProximityPosition() : m_pos;
    LocPathIterator clone;
    try
    {
      clone = (LocPathIterator) clone();        
    }
    catch (CloneNotSupportedException cnse)
    {
      return -1;
    }
    if (predCount > 0 && isPredicateTest)
    {
      clone.m_predCount = m_predicateIndex;
    }
    int next;
    while (DTM.NULL != (next = clone.nextNode()))
    {
      pos++;
    }
    if (isPredicateTest && m_predicateIndex < 1)
      m_length = pos;
    return pos;
  }
  public boolean isFresh()
  {
    return (m_pos == 0);
  }
  public int previousNode()
  {
    throw new RuntimeException(
      XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_CANNOT_ITERATE, null)); 
  }
  public int getWhatToShow()
  {
    return DTMFilter.SHOW_ALL & ~DTMFilter.SHOW_ENTITY_REFERENCE;
  }
  public DTMFilter getFilter()
  {
    return null;
  }
  public int getRoot()
  {
    return m_context;
  }
  public boolean getExpandEntityReferences()
  {
    return true;
  }
  protected boolean m_allowDetach = true;
  public void allowDetachToRelease(boolean allowRelease)
  {
    m_allowDetach = allowRelease;
  }
  public void detach()
  {    
    if(m_allowDetach)
    {
      m_execContext = null;
      m_cdtm = null;
      m_length = -1;
      m_pos = 0;
      m_lastFetched = DTM.NULL;
      m_context = DTM.NULL;
      m_currentContextNode = DTM.NULL;
      m_clones.freeInstance(this);
    }
  }
  public void reset()
  {
  	assertion(false, "This iterator can not reset!");
  }
  public DTMIterator cloneWithReset() throws CloneNotSupportedException
  {
    LocPathIterator clone;
    clone = (LocPathIterator)m_clones.getInstanceOrThrow();
    clone.m_execContext = m_execContext;
    clone.m_cdtm = m_cdtm;
    clone.m_context = m_context;
    clone.m_currentContextNode = m_currentContextNode;
    clone.m_stackFrame = m_stackFrame;
    return clone;
  }
  public abstract int nextNode();
  protected int returnNextNode(int nextNode)
  {
    if (DTM.NULL != nextNode)
    {
      m_pos++;
    }
    m_lastFetched = nextNode;
    if (DTM.NULL == nextNode)
      m_foundLast = true;
    return nextNode;
  }
  public int getCurrentNode()
  {
    return m_lastFetched;
  }
  public void runTo(int index)
  {
    if (m_foundLast || ((index >= 0) && (index <= getCurrentPos())))
      return;
    int n;
    if (-1 == index)
    {
      while (DTM.NULL != (n = nextNode()));
    }
    else
    {
      while (DTM.NULL != (n = nextNode()))
      {
        if (getCurrentPos() >= index)
          break;
      }
    }
  }
  public final boolean getFoundLast()
  {
    return m_foundLast;
  }
  public final XPathContext getXPathContext()
  {
    return m_execContext;
  }
  public final int getContext()
  {
    return m_context;
  }
  public final int getCurrentContextNode()
  {
    return m_currentContextNode;
  }
  public final void setCurrentContextNode(int n)
  {
    m_currentContextNode = n;
  }
  public final PrefixResolver getPrefixResolver()
  {
  	if(null == m_prefixResolver)
  	{
    	m_prefixResolver = (PrefixResolver)getExpressionOwner();
  	}
    return m_prefixResolver;
  }
  public void callVisitors(ExpressionOwner owner, XPathVisitor visitor)
  {
  	 	if(visitor.visitLocationPath(owner, this))
  	 	{
  	 		visitor.visitStep(owner, this);
  	 		callPredicateVisitors(visitor);
  	 	}
  }  
  transient protected IteratorPool m_clones = new IteratorPool(this);
  transient protected DTM m_cdtm;
  transient int m_stackFrame = -1;
  private boolean m_isTopLevel = false;
  transient public int m_lastFetched = DTM.NULL;
  transient protected int m_context = DTM.NULL;
  transient protected int m_currentContextNode = DTM.NULL;
  transient protected int m_pos = 0;
  transient protected int m_length = -1;
  private PrefixResolver m_prefixResolver;
  transient protected XPathContext m_execContext;
  public boolean isDocOrdered()
  {
    return true;
  }
  public int getAxis()
  {
    return -1;
  }
  public int getLastPos(XPathContext xctxt)
  {
    return getLength();
  }
}
