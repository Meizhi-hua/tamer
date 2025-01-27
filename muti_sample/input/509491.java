public class IntStack extends IntVector
{
  public IntStack()
  {
    super();
  }
  public IntStack(int blocksize)
  {
    super(blocksize);
  }
  public IntStack (IntStack v)
  {
  	super(v);
  }
  public int push(int i)
  {
    if ((m_firstFree + 1) >= m_mapSize)
    {
      m_mapSize += m_blocksize;
      int newMap[] = new int[m_mapSize];
      System.arraycopy(m_map, 0, newMap, 0, m_firstFree + 1);
      m_map = newMap;
    }
    m_map[m_firstFree] = i;
    m_firstFree++;
    return i;
  }
  public final int pop()
  {
    return m_map[--m_firstFree];
  }
  public final void quickPop(int n)
  {
    m_firstFree -= n;
  }
  public final int peek()
  {
    try {
      return m_map[m_firstFree - 1];
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      throw new EmptyStackException();
    }
  }
  public int peek(int n)
  {
    try {
      return m_map[m_firstFree-(1+n)];
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      throw new EmptyStackException();
    }
  }
  public void setTop(int val)
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
  public int search(int o)
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
  	return (IntStack) super.clone();
  }
}
