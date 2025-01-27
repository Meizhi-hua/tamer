public class StringVector implements java.io.Serializable
{
    static final long serialVersionUID = 4995234972032919748L;
  protected int m_blocksize;
  protected String m_map[];
  protected int m_firstFree = 0;
  protected int m_mapSize;
  public StringVector()
  {
    m_blocksize = 8;
    m_mapSize = m_blocksize;
    m_map = new String[m_blocksize];
  }
  public StringVector(int blocksize)
  {
    m_blocksize = blocksize;
    m_mapSize = blocksize;
    m_map = new String[blocksize];
  }
  public int getLength()
  {
    return m_firstFree;
  }
  public final int size()
  {
    return m_firstFree;
  }
  public final void addElement(String value)
  {
    if ((m_firstFree + 1) >= m_mapSize)
    {
      m_mapSize += m_blocksize;
      String newMap[] = new String[m_mapSize];
      System.arraycopy(m_map, 0, newMap, 0, m_firstFree + 1);
      m_map = newMap;
    }
    m_map[m_firstFree] = value;
    m_firstFree++;
  }
  public final String elementAt(int i)
  {
    return m_map[i];
  }
  public final boolean contains(String s)
  {
    if (null == s)
      return false;
    for (int i = 0; i < m_firstFree; i++)
    {
      if (m_map[i].equals(s))
        return true;
    }
    return false;
  }
  public final boolean containsIgnoreCase(String s)
  {
    if (null == s)
      return false;
    for (int i = 0; i < m_firstFree; i++)
    {
      if (m_map[i].equalsIgnoreCase(s))
        return true;
    }
    return false;
  }
  public final void push(String s)
  {
    if ((m_firstFree + 1) >= m_mapSize)
    {
      m_mapSize += m_blocksize;
      String newMap[] = new String[m_mapSize];
      System.arraycopy(m_map, 0, newMap, 0, m_firstFree + 1);
      m_map = newMap;
    }
    m_map[m_firstFree] = s;
    m_firstFree++;
  }
  public final String pop()
  {
    if (m_firstFree <= 0)
      return null;
    m_firstFree--;
    String s = m_map[m_firstFree];
    m_map[m_firstFree] = null;
    return s;
  }
  public final String peek()
  {
    return (m_firstFree <= 0) ? null : m_map[m_firstFree - 1];
  }
}
