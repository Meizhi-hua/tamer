public class SuballocatedIntVector
{
  protected int m_blocksize;
  protected int m_SHIFT, m_MASK;
  protected static final int NUMBLOCKS_DEFAULT = 32;
  protected int m_numblocks = NUMBLOCKS_DEFAULT;
  protected int m_map[][];
  protected int m_firstFree = 0;
  protected int m_map0[];
  protected int m_buildCache[];
  protected int m_buildCacheStartIndex;
  public SuballocatedIntVector()
  {
    this(2048);
  }
  public SuballocatedIntVector(int blocksize, int numblocks)
  {
    for(m_SHIFT=0;0!=(blocksize>>>=1);++m_SHIFT)
      ;
    m_blocksize=1<<m_SHIFT;
    m_MASK=m_blocksize-1;
    m_numblocks = numblocks;
    m_map0=new int[m_blocksize];
    m_map = new int[numblocks][];
    m_map[0]=m_map0;
    m_buildCache = m_map0;
    m_buildCacheStartIndex = 0;
  }
  public SuballocatedIntVector(int blocksize)
  {
    this(blocksize, NUMBLOCKS_DEFAULT);
  }
  public int size()
  {
    return m_firstFree;
  }
  public void setSize(int sz)
  {
    if(m_firstFree>sz) 
      m_firstFree = sz;
  }
  public  void addElement(int value)
  {
    int indexRelativeToCache = m_firstFree - m_buildCacheStartIndex;
    if(indexRelativeToCache >= 0 && indexRelativeToCache < m_blocksize) {
      m_buildCache[indexRelativeToCache]=value;
      ++m_firstFree;
    } else {
      int index=m_firstFree>>>m_SHIFT;
      int offset=m_firstFree&m_MASK;
      if(index>=m_map.length)
      {
	int newsize=index+m_numblocks;
	int[][] newMap=new int[newsize][];
	System.arraycopy(m_map, 0, newMap, 0, m_map.length);
	m_map=newMap;
      }
      int[] block=m_map[index];
      if(null==block)
	block=m_map[index]=new int[m_blocksize];
      block[offset]=value;
      m_buildCache = block;
      m_buildCacheStartIndex = m_firstFree-offset;
      ++m_firstFree;
    }
  }
  private  void addElements(int value, int numberOfElements)
  {
    if(m_firstFree+numberOfElements<m_blocksize)
      for (int i = 0; i < numberOfElements; i++) 
      {
        m_map0[m_firstFree++]=value;
      }
    else
    {
      int index=m_firstFree>>>m_SHIFT;
      int offset=m_firstFree&m_MASK;
      m_firstFree+=numberOfElements;
      while( numberOfElements>0)
      {
        if(index>=m_map.length)
        {
          int newsize=index+m_numblocks;
          int[][] newMap=new int[newsize][];
          System.arraycopy(m_map, 0, newMap, 0, m_map.length);
          m_map=newMap;
        }
        int[] block=m_map[index];
        if(null==block)
          block=m_map[index]=new int[m_blocksize];
        int copied=(m_blocksize-offset < numberOfElements)
          ? m_blocksize-offset : numberOfElements;
        numberOfElements-=copied;
        while(copied-- > 0)
          block[offset++]=value;
        ++index;offset=0;
      }
    }
  }
  private  void addElements(int numberOfElements)
  {
    int newlen=m_firstFree+numberOfElements;
    if(newlen>m_blocksize)
    {
      int index=m_firstFree>>>m_SHIFT;
      int newindex=(m_firstFree+numberOfElements)>>>m_SHIFT;
      for(int i=index+1;i<=newindex;++i)
        m_map[i]=new int[m_blocksize];
    }
    m_firstFree=newlen;
  }
  private  void insertElementAt(int value, int at)
  {
    if(at==m_firstFree)
      addElement(value);
    else if (at>m_firstFree)
    {
      int index=at>>>m_SHIFT;
      if(index>=m_map.length)
      {
        int newsize=index+m_numblocks;
        int[][] newMap=new int[newsize][];
        System.arraycopy(m_map, 0, newMap, 0, m_map.length);
        m_map=newMap;
      }
      int[] block=m_map[index];
      if(null==block)
        block=m_map[index]=new int[m_blocksize];
      int offset=at&m_MASK;
          block[offset]=value;
          m_firstFree=offset+1;
        }
    else
    {
      int index=at>>>m_SHIFT;
      int maxindex=m_firstFree>>>m_SHIFT; 
      ++m_firstFree;
      int offset=at&m_MASK;
      int push;
      while(index<=maxindex)
      {
        int copylen=m_blocksize-offset-1;
        int[] block=m_map[index];
        if(null==block)
        {
          push=0;
          block=m_map[index]=new int[m_blocksize];
        }
        else
        {
          push=block[m_blocksize-1];
          System.arraycopy(block, offset , block, offset+1, copylen);
        }
        block[offset]=value;
        value=push;
        offset=0;
        ++index;
      }
    }
  }
  public void removeAllElements()
  {
    m_firstFree = 0;
    m_buildCache = m_map0;
    m_buildCacheStartIndex = 0;
  }
  private  boolean removeElement(int s)
  {
    int at=indexOf(s,0);
    if(at<0)
      return false;
    removeElementAt(at);
    return true;
  }
  private  void removeElementAt(int at)
  {
    if(at<m_firstFree)
    {
      int index=at>>>m_SHIFT;
      int maxindex=m_firstFree>>>m_SHIFT;
      int offset=at&m_MASK;
      while(index<=maxindex)
      {
        int copylen=m_blocksize-offset-1;
        int[] block=m_map[index];
        if(null==block)
          block=m_map[index]=new int[m_blocksize];
        else
          System.arraycopy(block, offset+1, block, offset, copylen);
        if(index<maxindex)
        {
          int[] next=m_map[index+1];
          if(next!=null)
            block[m_blocksize-1]=(next!=null) ? next[0] : 0;
        }
        else
          block[m_blocksize-1]=0;
        offset=0;
        ++index;
      }
    }
    --m_firstFree;
  }
  public void setElementAt(int value, int at)
  {
    if(at<m_blocksize)
      m_map0[at]=value;
    else
    {
      int index=at>>>m_SHIFT;
      int offset=at&m_MASK;
      if(index>=m_map.length)
      {
	int newsize=index+m_numblocks;
	int[][] newMap=new int[newsize][];
	System.arraycopy(m_map, 0, newMap, 0, m_map.length);
	m_map=newMap;
      }
      int[] block=m_map[index];
      if(null==block)
	block=m_map[index]=new int[m_blocksize];
      block[offset]=value;
    }
    if(at>=m_firstFree)
      m_firstFree=at+1;
  }
  public int elementAt(int i)
  {
    if(i<m_blocksize)
      return m_map0[i];
    return m_map[i>>>m_SHIFT][i&m_MASK];
  }
  private  boolean contains(int s)
  {
    return (indexOf(s,0) >= 0);
  }
  public int indexOf(int elem, int index)
  {
        if(index>=m_firstFree)
                return -1;
    int bindex=index>>>m_SHIFT;
    int boffset=index&m_MASK;
    int maxindex=m_firstFree>>>m_SHIFT;
    int[] block;
    for(;bindex<maxindex;++bindex)
    {
      block=m_map[bindex];
      if(block!=null)
        for(int offset=boffset;offset<m_blocksize;++offset)
          if(block[offset]==elem)
            return offset+bindex*m_blocksize;
      boffset=0; 
    }
    int maxoffset=m_firstFree&m_MASK;
    block=m_map[maxindex];
    for(int offset=boffset;offset<maxoffset;++offset)
      if(block[offset]==elem)
        return offset+maxindex*m_blocksize;
    return -1;    
  }
  public int indexOf(int elem)
  {
    return indexOf(elem,0);
  }
  private  int lastIndexOf(int elem)
  {
    int boffset=m_firstFree&m_MASK;
    for(int index=m_firstFree>>>m_SHIFT;
        index>=0;
        --index)
    {
      int[] block=m_map[index];
      if(block!=null)
        for(int offset=boffset; offset>=0; --offset)
          if(block[offset]==elem)
            return offset+index*m_blocksize;
      boffset=0; 
    }
    return -1;
  }
  public final int[] getMap0()
  {
    return m_map0;
  }
  public final int[][] getMap()
  {
    return m_map;
  }
}
