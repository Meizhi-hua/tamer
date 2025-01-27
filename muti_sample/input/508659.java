public class AxesWalker extends PredicatedNodeTest
        implements Cloneable, PathComponent, ExpressionOwner
{
    static final long serialVersionUID = -2966031951306601247L;
  public AxesWalker(LocPathIterator locPathIterator, int axis)
  {
    super( locPathIterator );
    m_axis = axis;
  }
  public final WalkingIterator wi()
  {
    return (WalkingIterator)m_lpi;
  }
  public void init(Compiler compiler, int opPos, int stepType)
          throws javax.xml.transform.TransformerException
  {
    initPredicateInfo(compiler, opPos);
  }
  public Object clone() throws CloneNotSupportedException
  {
    AxesWalker clone = (AxesWalker) super.clone();
    return clone;
  }
  AxesWalker cloneDeep(WalkingIterator cloneOwner, Vector cloneList)
     throws CloneNotSupportedException
  {
    AxesWalker clone = findClone(this, cloneList);
    if(null != clone)
      return clone;
    clone = (AxesWalker)this.clone();
    clone.setLocPathIterator(cloneOwner);
    if(null != cloneList)
    {
      cloneList.addElement(this);
      cloneList.addElement(clone);
    }
    if(wi().m_lastUsedWalker == this)
      cloneOwner.m_lastUsedWalker = clone;
    if(null != m_nextWalker)
      clone.m_nextWalker = m_nextWalker.cloneDeep(cloneOwner, cloneList);
    if(null != cloneList)
    {
      if(null != m_prevWalker)
        clone.m_prevWalker = m_prevWalker.cloneDeep(cloneOwner, cloneList);
    }
    else
    {
      if(null != m_nextWalker)
        clone.m_nextWalker.m_prevWalker = clone;
    }
    return clone;
  }
  static AxesWalker findClone(AxesWalker key, Vector cloneList)
  {
    if(null != cloneList)
    {
      int n = cloneList.size();
      for (int i = 0; i < n; i+=2) 
      {
        if(key == cloneList.elementAt(i))
          return (AxesWalker)cloneList.elementAt(i+1);
      }
    }
    return null;    
  }
  public void detach()
  { 
  	m_currentNode = DTM.NULL;
  	m_dtm = null;
  	m_traverser = null;
  	m_isFresh = true;
  	m_root = DTM.NULL;
  }
  public int getRoot()
  {
    return m_root;
  }
  public int getAnalysisBits()
  {
  	int axis = getAxis();
  	int bit = WalkerFactory.getAnalysisBitFromAxes(axis);
  	return bit;
  }
  public void setRoot(int root)
  {
    XPathContext xctxt = wi().getXPathContext();
    m_dtm = xctxt.getDTM(root);
    m_traverser = m_dtm.getAxisTraverser(m_axis);
    m_isFresh = true;
    m_foundLast = false;
    m_root = root;
    m_currentNode = root;
    if (DTM.NULL == root)
    {
      throw new RuntimeException(
        XSLMessages.createXPATHMessage(XPATHErrorResources.ER_SETTING_WALKER_ROOT_TO_NULL, null)); 
    }
    resetProximityPositions();
  }
  public final int getCurrentNode()
  {
    return m_currentNode;
  }
  public void setNextWalker(AxesWalker walker)
  {
    m_nextWalker = walker;
  }
  public AxesWalker getNextWalker()
  {
    return m_nextWalker;
  }
  public void setPrevWalker(AxesWalker walker)
  {
    m_prevWalker = walker;
  }
  public AxesWalker getPrevWalker()
  {
    return m_prevWalker;
  }
  private int returnNextNode(int n)
  {
    return n;
  }
  protected int getNextNode()
  {
    if (m_foundLast)
      return DTM.NULL;
    if (m_isFresh)
    {
      m_currentNode = m_traverser.first(m_root);
      m_isFresh = false;
    }
    else if(DTM.NULL != m_currentNode) 
    {
      m_currentNode = m_traverser.next(m_root, m_currentNode);
    }
    if (DTM.NULL == m_currentNode)
      this.m_foundLast = true;
    return m_currentNode;
  }
  public int nextNode()
  {
    int nextNode = DTM.NULL;
    AxesWalker walker = wi().getLastUsedWalker();
    while (true)
    {
      if (null == walker)
        break;
      nextNode = walker.getNextNode();
      if (DTM.NULL == nextNode)
      {
        walker = walker.m_prevWalker;
      }
      else
      {
        if (walker.acceptNode(nextNode) != DTMIterator.FILTER_ACCEPT)
        {
          continue;
        }
        if (null == walker.m_nextWalker)
        {
          wi().setLastUsedWalker(walker);
          break;
        }
        else
        {
          AxesWalker prev = walker;
          walker = walker.m_nextWalker;
          walker.setRoot(nextNode);
          walker.m_prevWalker = prev;
          continue;
        }
      }  
    }  
    return nextNode;
  }
  public int getLastPos(XPathContext xctxt)
  {
    int pos = getProximityPosition();
    AxesWalker walker;
    try
    {
      walker = (AxesWalker) clone();
    }
    catch (CloneNotSupportedException cnse)
    {
      return -1;
    }
    walker.setPredicateCount(m_predicateIndex);
    walker.setNextWalker(null);
    walker.setPrevWalker(null);
    WalkingIterator lpi = wi();
    AxesWalker savedWalker = lpi.getLastUsedWalker();
    try
    {
      lpi.setLastUsedWalker(walker);
      int next;
      while (DTM.NULL != (next = walker.nextNode()))
      {
        pos++;
      }
    }
    finally
    {
      lpi.setLastUsedWalker(savedWalker);
    }
    return pos;
  }
  private DTM m_dtm;
  public void setDefaultDTM(DTM dtm)
  {
    m_dtm = dtm;
  }
  public DTM getDTM(int node)
  {
    return wi().getXPathContext().getDTM(node);
  }
  public boolean isDocOrdered()
  {
    return true;
  }
  public int getAxis()
  {
    return m_axis;
  }
  public void callVisitors(ExpressionOwner owner, XPathVisitor visitor)
  {
  	if(visitor.visitStep(owner, this))
  	{
  		callPredicateVisitors(visitor);
  		if(null != m_nextWalker)
  		{
  			m_nextWalker.callVisitors(this, visitor);
  		}
  	}
  }
  public Expression getExpression()
  {
    return m_nextWalker;
  }
  public void setExpression(Expression exp)
  {
  	exp.exprSetParent(this);
  	m_nextWalker = (AxesWalker)exp;
  }
    public boolean deepEquals(Expression expr)
    {
      if (!super.deepEquals(expr))
                return false;
      AxesWalker walker = (AxesWalker)expr;
      if(this.m_axis != walker.m_axis)
      	return false;
      return true;
    }
  transient int m_root = DTM.NULL;
  private transient int m_currentNode = DTM.NULL;
  transient boolean m_isFresh;
  protected AxesWalker m_nextWalker;
  AxesWalker m_prevWalker;
  protected int m_axis = -1;
  protected DTMAxisTraverser m_traverser; 
}
