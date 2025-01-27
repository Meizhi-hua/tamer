final class ChunkedIntArray
{
  final int slotsize=4; 
  static final int lowbits=10; 
  static final int chunkalloc=1<<lowbits;
  static final int lowmask=chunkalloc-1;
  ChunksVector chunks=new ChunksVector();
  final int fastArray[] = new int[chunkalloc];
  int lastUsed=0;
  ChunkedIntArray(int slotsize)
  {
    if(this.slotsize<slotsize)
      throw new ArrayIndexOutOfBoundsException(XMLMessages.createXMLMessage(XMLErrorResources.ER_CHUNKEDINTARRAY_NOT_SUPPORTED, new Object[]{Integer.toString(slotsize)})); 
    else if (this.slotsize>slotsize)
      System.out.println("*****WARNING: ChunkedIntArray("+slotsize+") wasting "+(this.slotsize-slotsize)+" words per slot");
    chunks.addElement(fastArray);
  }
  int appendSlot(int w0, int w1, int w2, int w3)
  {
    {
      final int slotsize=4;
      int newoffset = (lastUsed+1)*slotsize;
      int chunkpos = newoffset >> lowbits;
      int slotpos = (newoffset & lowmask);
      if (chunkpos > chunks.size() - 1)
        chunks.addElement(new int[chunkalloc]);
      int[] chunk = chunks.elementAt(chunkpos);
      chunk[slotpos] = w0;
      chunk[slotpos+1] = w1;
      chunk[slotpos+2] = w2;
      chunk[slotpos+3] = w3;
      return ++lastUsed;
    }
  }
  int readEntry(int position, int offset) throws ArrayIndexOutOfBoundsException
  {
    {
      if (offset>=slotsize)
        throw new ArrayIndexOutOfBoundsException(XMLMessages.createXMLMessage(XMLErrorResources.ER_OFFSET_BIGGER_THAN_SLOT, null)); 
      position*=slotsize;
      int chunkpos = position >> lowbits;
      int slotpos = position & lowmask;
      int[] chunk = chunks.elementAt(chunkpos);
      return chunk[slotpos + offset];
    }
  }
  int specialFind(int startPos, int position)
  {
          int ancestor = startPos;
          while(ancestor > 0)
          {
                ancestor*=slotsize;
                int chunkpos = ancestor >> lowbits;
                int slotpos = ancestor & lowmask;
                int[] chunk = chunks.elementAt(chunkpos);
                ancestor = chunk[slotpos + 1];
                if(ancestor == position)
                         break;
          }
          if (ancestor <= 0) 
          {
                  return position;
          }
          return -1;
  }
  int slotsUsed()
  {
    return lastUsed;
  }
  void discardLast()
  {
    --lastUsed;
  }
  void writeEntry(int position, int offset, int value) throws ArrayIndexOutOfBoundsException
  {
    {
      if (offset >= slotsize)
        throw new ArrayIndexOutOfBoundsException(XMLMessages.createXMLMessage(XMLErrorResources.ER_OFFSET_BIGGER_THAN_SLOT, null)); 
      position*=slotsize;
      int chunkpos = position >> lowbits;
      int slotpos = position & lowmask;
      int[] chunk = chunks.elementAt(chunkpos);
      chunk[slotpos + offset] = value; 
    }
  }
  void writeSlot(int position, int w0, int w1, int w2, int w3)
  {
      position *= slotsize;
      int chunkpos = position >> lowbits;
      int slotpos = (position & lowmask);
    if (chunkpos > chunks.size() - 1)
      chunks.addElement(new int[chunkalloc]);
    int[] chunk = chunks.elementAt(chunkpos);
    chunk[slotpos] = w0;
    chunk[slotpos + 1] = w1;
    chunk[slotpos + 2] = w2;
    chunk[slotpos + 3] = w3;
  }
  void readSlot(int position, int[] buffer)
  {
    {
      position *= slotsize;
      int chunkpos = position >> lowbits;
      int slotpos = (position & lowmask);
      if (chunkpos > chunks.size() - 1)
        chunks.addElement(new int[chunkalloc]);
      int[] chunk = chunks.elementAt(chunkpos);
      System.arraycopy(chunk,slotpos,buffer,0,slotsize);
    }
  }
  class ChunksVector
  {
    final int BLOCKSIZE = 64;
    int[] m_map[] = new int[BLOCKSIZE][];
    int m_mapSize = BLOCKSIZE;
    int pos = 0;
    ChunksVector()
    {
    }
    final int size()
    {
      return pos;
    }
    void addElement(int[] value)
    {
      if(pos >= m_mapSize)
      {
        int orgMapSize = m_mapSize;
        while(pos >= m_mapSize)
          m_mapSize+=BLOCKSIZE;
        int[] newMap[] = new int[m_mapSize][];
        System.arraycopy(m_map, 0, newMap, 0, orgMapSize);
        m_map = newMap;
      }
      m_map[pos] = value;
      pos++;
    }
    final int[] elementAt(int pos)
    {
      return m_map[pos];
    }
  }
}
