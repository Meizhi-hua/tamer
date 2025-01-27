public class NodeVector implements Serializable, Cloneable
{
    static final long serialVersionUID = -713473092200731870L;
  private int m_blocksize;
  private int m_map[];
  protected int m_firstFree = 0;
  private int m_mapSize;  
  public NodeVector()
  {
    m_blocksize = 32;
    m_mapSize = 0;
  }
  public NodeVector(int blocksize)
  {
    m_blocksize = blocksize;
    m_mapSize = 0;
  }
  public Object clone() throws CloneNotSupportedException
  {
    NodeVector clone = (NodeVector) super.clone();
    if ((null != this.m_map) && (this.m_map == clone.m_map))
    {
      clone.m_map = new int[this.m_map.length];
      System.arraycopy(this.m_map, 0, clone.m_map, 0, this.m_map.length);
    }
    return clone;
  }
  public int size()
  {
    return m_firstFree;
  }
  public void addElement(int value)
  {
    if ((m_firstFree + 1) >= m_mapSize)
    {
      if (null == m_map)
      {
        m_map = new int[m_blocksize];
        m_mapSize = m_blocksize;
      }
      else
      {
        m_mapSize += m_blocksize;
        int newMap[] = new int[m_mapSize];
        System.arraycopy(m_map, 0, newMap, 0, m_firstFree + 1);
        m_map = newMap;
      }
    }
    m_map[m_firstFree] = value;
    m_firstFree++;
  }
  public final void push(int value)
  {
    int ff = m_firstFree;
    if ((ff + 1) >= m_mapSize)
    {
      if (null == m_map)
      {
        m_map = new int[m_blocksize];
        m_mapSize = m_blocksize;
      }
      else
      {
        m_mapSize += m_blocksize;
        int newMap[] = new int[m_mapSize];
        System.arraycopy(m_map, 0, newMap, 0, ff + 1);
        m_map = newMap;
      }
    }
    m_map[ff] = value;
    ff++;
    m_firstFree = ff;
  }
  public final int pop()
  {
    m_firstFree--;
    int n = m_map[m_firstFree];
    m_map[m_firstFree] = DTM.NULL;
    return n;
  }
  public final int popAndTop()
  {
    m_firstFree--;
    m_map[m_firstFree] = DTM.NULL;
    return (m_firstFree == 0) ? DTM.NULL : m_map[m_firstFree - 1];
  }
  public final void popQuick()
  {
    m_firstFree--;
    m_map[m_firstFree] = DTM.NULL;
  }
  public final int peepOrNull()
  {
    return ((null != m_map) && (m_firstFree > 0))
           ? m_map[m_firstFree - 1] : DTM.NULL;
  }
  public final void pushPair(int v1, int v2)
  {
    if (null == m_map)
    {
      m_map = new int[m_blocksize];
      m_mapSize = m_blocksize;
    }
    else
    {
      if ((m_firstFree + 2) >= m_mapSize)
      {
        m_mapSize += m_blocksize;
        int newMap[] = new int[m_mapSize];
        System.arraycopy(m_map, 0, newMap, 0, m_firstFree);
        m_map = newMap;
      }
    }
    m_map[m_firstFree] = v1;
    m_map[m_firstFree + 1] = v2;
    m_firstFree += 2;
  }
  public final void popPair()
  {
    m_firstFree -= 2;
    m_map[m_firstFree] = DTM.NULL;
    m_map[m_firstFree + 1] = DTM.NULL;
  }
  public final void setTail(int n)
  {
    m_map[m_firstFree - 1] = n;
  }
  public final void setTailSub1(int n)
  {
    m_map[m_firstFree - 2] = n;
  }
  public final int peepTail()
  {
    return m_map[m_firstFree - 1];
  }
  public final int peepTailSub1()
  {
    return m_map[m_firstFree - 2];
  }
  public void insertInOrder(int value)
  {
    for (int i = 0; i < m_firstFree; i++)
    {
      if (value < m_map[i])
      {
        insertElementAt(value, i);
        return;
      }
    }
    addElement(value);
  }
  public void insertElementAt(int value, int at)
  {
    if (null == m_map)
    {
      m_map = new int[m_blocksize];
      m_mapSize = m_blocksize;
    }
    else if ((m_firstFree + 1) >= m_mapSize)
    {
      m_mapSize += m_blocksize;
      int newMap[] = new int[m_mapSize];
      System.arraycopy(m_map, 0, newMap, 0, m_firstFree + 1);
      m_map = newMap;
    }
    if (at <= (m_firstFree - 1))
    {
      System.arraycopy(m_map, at, m_map, at + 1, m_firstFree - at);
    }
    m_map[at] = value;
    m_firstFree++;
  }
  public void appendNodes(NodeVector nodes)
  {
    int nNodes = nodes.size();
    if (null == m_map)
    {
      m_mapSize = nNodes + m_blocksize;
      m_map = new int[m_mapSize];
    }
    else if ((m_firstFree + nNodes) >= m_mapSize)
    {
      m_mapSize += (nNodes + m_blocksize);
      int newMap[] = new int[m_mapSize];
      System.arraycopy(m_map, 0, newMap, 0, m_firstFree + nNodes);
      m_map = newMap;
    }
    System.arraycopy(nodes.m_map, 0, m_map, m_firstFree, nNodes);
    m_firstFree += nNodes;
  }
  public void removeAllElements()
  {
    if (null == m_map)
      return;
    for (int i = 0; i < m_firstFree; i++)
    {
      m_map[i] = DTM.NULL;
    }
    m_firstFree = 0;
  }
  public void RemoveAllNoClear()
  {
    if (null == m_map)
      return;
    m_firstFree = 0;
  }
  public boolean removeElement(int s)
  {
    if (null == m_map)
      return false;
    for (int i = 0; i < m_firstFree; i++)
    {
      int node = m_map[i];
      if (node == s)
      {
        if (i > m_firstFree)
          System.arraycopy(m_map, i + 1, m_map, i - 1, m_firstFree - i);
        else
          m_map[i] = DTM.NULL;
        m_firstFree--;
        return true;
      }
    }
    return false;
  }
  public void removeElementAt(int i)
  {
    if (null == m_map)
      return;
    if (i > m_firstFree)
      System.arraycopy(m_map, i + 1, m_map, i - 1, m_firstFree - i);
    else
      m_map[i] = DTM.NULL;
  }
  public void setElementAt(int node, int index)
  {
    if (null == m_map)
    {
      m_map = new int[m_blocksize];
      m_mapSize = m_blocksize;
    }
    if(index == -1)
    	addElement(node);
    m_map[index] = node;
  }
  public int elementAt(int i)
  {
    if (null == m_map)
      return DTM.NULL;
    return m_map[i];
  }
  public boolean contains(int s)
  {
    if (null == m_map)
      return false;
    for (int i = 0; i < m_firstFree; i++)
    {
      int node = m_map[i];
      if (node == s)
        return true;
    }
    return false;
  }
  public int indexOf(int elem, int index)
  {
    if (null == m_map)
      return -1;
    for (int i = index; i < m_firstFree; i++)
    {
      int node = m_map[i];
      if (node == elem)
        return i;
    }
    return -1;
  }
  public int indexOf(int elem)
  {
    if (null == m_map)
      return -1;
    for (int i = 0; i < m_firstFree; i++)
    {
      int node = m_map[i];
      if (node == elem)
        return i;
    }
    return -1;
  }
  public void sort(int a[], int lo0, int hi0) throws Exception
  {
    int lo = lo0;
    int hi = hi0;
    if (lo >= hi)
    {
      return;
    }
    else if (lo == hi - 1)
    {
      if (a[lo] > a[hi])
      {
        int T = a[lo];
        a[lo] = a[hi];
        a[hi] = T;
      }
      return;
    }
    int pivot = a[(lo + hi) / 2];
    a[(lo + hi) / 2] = a[hi];
    a[hi] = pivot;
    while (lo < hi)
    {
      while (a[lo] <= pivot && lo < hi)
      {
        lo++;
      }
      while (pivot <= a[hi] && lo < hi)
      {
        hi--;
      }
      if (lo < hi)
      {
        int T = a[lo];
        a[lo] = a[hi];
        a[hi] = T;
      }
    }
    a[hi0] = a[hi];
    a[hi] = pivot;
    sort(a, lo0, lo - 1);
    sort(a, hi + 1, hi0);
  }
  public void sort() throws Exception
  {
    sort(m_map, 0, m_firstFree - 1);
  }
}
