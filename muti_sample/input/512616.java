public class ChildTestIterator extends BasicTestIterator
{
    static final long serialVersionUID = -7936835957960705722L;
  transient protected DTMAxisTraverser m_traverser;
  ChildTestIterator(Compiler compiler, int opPos, int analysis)
          throws javax.xml.transform.TransformerException
  {
    super(compiler, opPos, analysis);
  }
  public ChildTestIterator(DTMAxisTraverser traverser)
  {
    super(null);
    m_traverser = traverser;
  }
  protected int getNextNode()
  {                     
    if(true )
    {
      m_lastFetched = (DTM.NULL == m_lastFetched)
                   ? m_traverser.first(m_context)
                   : m_traverser.next(m_context, m_lastFetched);
    }
    return m_lastFetched;
  }
  public DTMIterator cloneWithReset() throws CloneNotSupportedException
  {
    ChildTestIterator clone = (ChildTestIterator) super.cloneWithReset();
    clone.m_traverser = m_traverser;
    return clone;
  }
  public void setRoot(int context, Object environment)
  {
    super.setRoot(context, environment);
    m_traverser = m_cdtm.getAxisTraverser(Axis.CHILD);
  }
  public int getAxis()
  {
    return org.apache.xml.dtm.Axis.CHILD;
  }
  public void detach()
  {   
    if(m_allowDetach)
    {
      m_traverser = null;
      super.detach();
    }
  }
}
