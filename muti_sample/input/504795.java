public class ObjectStack extends ObjectVector
{
  public ObjectStack()
  {
    super();
  }
  public ObjectStack(int blocksize)
  {
    super(blocksize);
  }
  public ObjectStack (ObjectStack v)
  {
  	super(v);
  }
  public Object push(Object i)
  {
    if ((m_firstFree + 1) >= m_mapSize)
    {
      m_mapSize += m_blocksize;
      Object newMap[] = new Object[m_mapSize];
      System.arraycopy(m_map, 0, newMap, 0, m_firstFree + 1);
      m_map = newMap;
    }
    m_map[m_firstFree] = i;
    m_firstFree++;
    return i;
  }
  public Object pop()
  {
    Object val = m_map[--m_firstFree];
    m_map[m_firstFree] = null;
    return val;
  }
  public void quickPop(int n)
  {
    m_firstFree -= n;
  }
  public Object peek()
  {
    try {
      return m_map[m_firstFree - 1];
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      throw new EmptyStackException();
    }
  }
  public Object peek(int n)
  {
    try {
      return m_map[m_firstFree-(1+n)];
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      throw new EmptyStackException();
    }
  }
  public void setTop(Object val)
  {
    try {
      m_map[m_firstFree - 1] = val;
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      throw new EmptyStackException();
    }
  }
  public boolean empty()
  {
    return m_firstFree == 0;
  }
  public int search(Object o)
  {
    int i = lastIndexOf(o);
    if (i >= 0)
    {
      return size() - i;
    }
    return -1;
  }
  public Object clone()
    throws CloneNotSupportedException
  {
  	return (ObjectStack) super.clone();
  }  
}
