public class Counter
{
  static final int MAXCOUNTNODES = 500;
  int m_countNodesStartCount = 0;
  NodeSetDTM m_countNodes;
  int m_fromNode = DTM.NULL;
  ElemNumber m_numberElem;
  int m_countResult;
  Counter(ElemNumber numberElem, NodeSetDTM countNodes) throws TransformerException
  {
    m_countNodes = countNodes;
    m_numberElem = numberElem;
  }
  int getPreviouslyCounted(XPathContext support, int node)
  {
    int n = m_countNodes.size();
    m_countResult = 0;
    for (int i = n - 1; i >= 0; i--)
    {
      int countedNode = m_countNodes.elementAt(i);
      if (node == countedNode)
      {
        m_countResult = i + 1 + m_countNodesStartCount;
        break;
      }
      DTM dtm = support.getDTM(countedNode);
      if (dtm.isNodeAfter(countedNode, node))
        break;
    }
    return m_countResult;
  }
  int getLast()
  {
    int size = m_countNodes.size();
    return (size > 0) ? m_countNodes.elementAt(size - 1) : DTM.NULL;
  }
}
