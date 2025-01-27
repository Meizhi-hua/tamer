public class NodeSorter
{
  XPathContext m_execContext;
  Vector m_keys;  
  public NodeSorter(XPathContext p)
  {
    m_execContext = p;
  }
  public void sort(DTMIterator v, Vector keys, XPathContext support)
          throws javax.xml.transform.TransformerException
  {
    m_keys = keys;
    int n = v.getLength();
    Vector nodes = new Vector();
    for (int i = 0; i < n; i++)
    {
      NodeCompareElem elem = new NodeCompareElem(v.item(i));
      nodes.addElement(elem);
    }
    Vector scratchVector = new Vector();
    mergesort(nodes, scratchVector, 0, n - 1, support);
    for (int i = 0; i < n; i++)
    {
      v.setItem(((NodeCompareElem) nodes.elementAt(i)).m_node, i);
    }
    v.setCurrentPos(0);
  }
  int compare(
          NodeCompareElem n1, NodeCompareElem n2, int kIndex, XPathContext support)
            throws TransformerException
  {
    int result = 0;
    NodeSortKey k = (NodeSortKey) m_keys.elementAt(kIndex);
    if (k.m_treatAsNumbers)
    {
      double n1Num, n2Num;
      if (kIndex == 0)
      {
        n1Num = ((Double) n1.m_key1Value).doubleValue();
        n2Num = ((Double) n2.m_key1Value).doubleValue();
      }
      else if (kIndex == 1)
      {
        n1Num = ((Double) n1.m_key2Value).doubleValue();
        n2Num = ((Double) n2.m_key2Value).doubleValue();
      }
      else
      {
        XObject r1 = k.m_selectPat.execute(m_execContext, n1.m_node,
                                           k.m_namespaceContext);
        XObject r2 = k.m_selectPat.execute(m_execContext, n2.m_node,
                                           k.m_namespaceContext);
        n1Num = r1.num();
        n2Num = r2.num();
      }
      if ((n1Num == n2Num) && ((kIndex + 1) < m_keys.size()))
      {
        result = compare(n1, n2, kIndex + 1, support);
      }
      else
      {
        double diff;
        if (Double.isNaN(n1Num))
        {
          if (Double.isNaN(n2Num))
            diff = 0.0;
          else
            diff = -1;
        }
        else if (Double.isNaN(n2Num))
           diff = 1;
        else
          diff = n1Num - n2Num;
        result = (int) ((diff < 0.0)
                        ? (k.m_descending ? 1 : -1)
                        : (diff > 0.0) ? (k.m_descending ? -1 : 1) : 0);
      }
    }  
    else
    {
      CollationKey n1String, n2String;
      if (kIndex == 0)
      {
        n1String = (CollationKey) n1.m_key1Value;
        n2String = (CollationKey) n2.m_key1Value;
      }
      else if (kIndex == 1)
      {
        n1String = (CollationKey) n1.m_key2Value;
        n2String = (CollationKey) n2.m_key2Value;
      }
      else
      {
        XObject r1 = k.m_selectPat.execute(m_execContext, n1.m_node,
                                           k.m_namespaceContext);
        XObject r2 = k.m_selectPat.execute(m_execContext, n2.m_node,
                                           k.m_namespaceContext);
        n1String = k.m_col.getCollationKey(r1.str());
        n2String = k.m_col.getCollationKey(r2.str());
      }
      result = n1String.compareTo(n2String);
      if (k.m_caseOrderUpper)
      {
        String tempN1 = n1String.getSourceString().toLowerCase();
        String tempN2 = n2String.getSourceString().toLowerCase();
        if (tempN1.equals(tempN2))
        {
          result = result == 0 ? 0 : -result;
        }
      }
      if (k.m_descending)
      {
        result = -result;
      }
    }  
    if (0 == result)
    {
      if ((kIndex + 1) < m_keys.size())
      {
        result = compare(n1, n2, kIndex + 1, support);
      }
    }
    if (0 == result)
    {
      DTM dtm = support.getDTM(n1.m_node); 
      result = dtm.isNodeAfter(n1.m_node, n2.m_node) ? -1 : 1;
    }
    return result;
  }
  void mergesort(Vector a, Vector b, int l, int r, XPathContext support)
          throws TransformerException
  {
    if ((r - l) > 0)
    {
      int m = (r + l) / 2;
      mergesort(a, b, l, m, support);
      mergesort(a, b, m + 1, r, support);
      int i, j, k;
      for (i = m; i >= l; i--)
      {
        if (i >= b.size())
          b.insertElementAt(a.elementAt(i), i);
        else
          b.setElementAt(a.elementAt(i), i);
      }
      i = l;
      for (j = (m + 1); j <= r; j++)
      {
        if (r + m + 1 - j >= b.size())
          b.insertElementAt(a.elementAt(j), r + m + 1 - j);
        else
          b.setElementAt(a.elementAt(j), r + m + 1 - j);
      }
      j = r;
      int compVal;
      for (k = l; k <= r; k++)
      {
        if (i == j)
          compVal = -1;
        else
          compVal = compare((NodeCompareElem) b.elementAt(i),
                            (NodeCompareElem) b.elementAt(j), 0, support);
        if (compVal < 0)
        {
          a.setElementAt(b.elementAt(i), k);
          i++;
        }
        else if (compVal > 0)
        {
          a.setElementAt(b.elementAt(j), k);
          j--;
        }
      }
    }
  }
  class NodeCompareElem
  {
    int m_node;
    int maxkey = 2;
    Object m_key1Value;
    Object m_key2Value;
    NodeCompareElem(int node) throws javax.xml.transform.TransformerException
    {
      m_node = node;
      if (!m_keys.isEmpty())
      {
        NodeSortKey k1 = (NodeSortKey) m_keys.elementAt(0);
        XObject r = k1.m_selectPat.execute(m_execContext, node,
                                           k1.m_namespaceContext);
        double d;
        if (k1.m_treatAsNumbers)
        {
          d = r.num();
          m_key1Value = new Double(d);
        }
        else
        {
          m_key1Value = k1.m_col.getCollationKey(r.str());
        }
        if (r.getType() == XObject.CLASS_NODESET)
        {
          DTMIterator ni = ((XNodeSet)r).iterRaw();
          int current = ni.getCurrentNode();
          if(DTM.NULL == current)
            current = ni.nextNode();
        }
        if (m_keys.size() > 1)
        {
          NodeSortKey k2 = (NodeSortKey) m_keys.elementAt(1);
          XObject r2 = k2.m_selectPat.execute(m_execContext, node,
                                              k2.m_namespaceContext);
          if (k2.m_treatAsNumbers) {
            d = r2.num();
            m_key2Value = new Double(d);
          } else {
            m_key2Value = k2.m_col.getCollationKey(r2.str());
          }
        }
      }  
    }
  }  
}
