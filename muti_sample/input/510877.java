public class CountersTable extends Hashtable
{
    static final long serialVersionUID = 2159100770924179875L;
  public CountersTable(){}
  Vector getCounters(ElemNumber numberElem)
  {
    Vector counters = (Vector) this.get(numberElem);
    return (null == counters) ? putElemNumber(numberElem) : counters;
  }
  Vector putElemNumber(ElemNumber numberElem)
  {
    Vector counters = new Vector();
    this.put(numberElem, counters);
    return counters;
  }
  transient private NodeSetDTM m_newFound;
  void appendBtoFList(NodeSetDTM flist, NodeSetDTM blist)
  {
    int n = blist.size();
    for (int i = (n - 1); i >= 0; i--)
    {
      flist.addElement(blist.item(i));
    }
  }
  transient int m_countersMade = 0;
  public int countNode(XPathContext support, ElemNumber numberElem, int node)
          throws TransformerException
  {
    int count = 0;
    Vector counters = getCounters(numberElem);
    int nCounters = counters.size();
    int target = numberElem.getTargetNode(support, node);
    if (DTM.NULL != target)
    {
      for (int i = 0; i < nCounters; i++)
      {
        Counter counter = (Counter) counters.elementAt(i);
        count = counter.getPreviouslyCounted(support, target);
        if (count > 0)
          return count;
      }
      count = 0;
      if (m_newFound == null)
        m_newFound = new NodeSetDTM(support.getDTMManager());
      for (; DTM.NULL != target;
              target = numberElem.getPreviousNode(support, target))
      {
        if (0 != count)
        {
          for (int i = 0; i < nCounters; i++)
          {
            Counter counter = (Counter) counters.elementAt(i);
            int cacheLen = counter.m_countNodes.size();
            if ((cacheLen > 0)
                    && (counter.m_countNodes.elementAt(cacheLen
                                                      - 1) == target))
            {
              count += (cacheLen + counter.m_countNodesStartCount);
              if (cacheLen > 0)
                appendBtoFList(counter.m_countNodes, m_newFound);
              m_newFound.removeAllElements();
              return count;
            }
          }
        }
        m_newFound.addElement(target);
        count++;
      }
      Counter counter = new Counter(numberElem, new NodeSetDTM(support.getDTMManager()));
      m_countersMade++;  
      appendBtoFList(counter.m_countNodes, m_newFound);
      m_newFound.removeAllElements();
      counters.addElement(counter);
    }
    return count;
  }
}
