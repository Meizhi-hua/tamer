public final class BoolStack implements Cloneable
{
  private boolean m_values[];
  private int m_allocatedSize;
  private int m_index;
  public BoolStack()
  {
    this(32);
  }
  public BoolStack(int size)
  {
    m_allocatedSize = size;
    m_values = new boolean[size];
    m_index = -1;
  }
  public final int size()
  {
    return m_index + 1;
  }
  public final void clear()
  {
  	m_index = -1;
  }
  public final boolean push(boolean val)
  {
    if (m_index == m_allocatedSize - 1)
      grow();
    return (m_values[++m_index] = val);
  }
  public final boolean pop()
  {
    return m_values[m_index--];
  }
  public final boolean popAndTop()
  {
    m_index--;
    return (m_index >= 0) ? m_values[m_index] : false;
  }
  public final void setTop(boolean b)
  {
    m_values[m_index] = b;
  }
  public final boolean peek()
  {
    return m_values[m_index];
  }
  public final boolean peekOrFalse()
  {
    return (m_index > -1) ? m_values[m_index] : false;
  }
  public final boolean peekOrTrue()
  {
    return (m_index > -1) ? m_values[m_index] : true;
  }
  public boolean isEmpty()
  {
    return (m_index == -1);
  }
  private void grow()
  {
    m_allocatedSize *= 2;
    boolean newVector[] = new boolean[m_allocatedSize];
    System.arraycopy(m_values, 0, newVector, 0, m_index + 1);
    m_values = newVector;
  }
  public Object clone() 
    throws CloneNotSupportedException
  {
    return super.clone();
  }
}
