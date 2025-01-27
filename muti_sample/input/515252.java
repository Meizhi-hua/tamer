public class FastStringBuffer
{
  static final int DEBUG_FORCE_INIT_BITS=0;
    static final boolean DEBUG_FORCE_FIXED_CHUNKSIZE=true;
	public static final int SUPPRESS_LEADING_WS=0x01;
	public static final int SUPPRESS_TRAILING_WS=0x02;
	public static final int SUPPRESS_BOTH
		= SUPPRESS_LEADING_WS | SUPPRESS_TRAILING_WS;
	private static final int CARRY_WS=0x04;
  int m_chunkBits = 15;
  int m_maxChunkBits = 15;
  int m_rebundleBits = 2;
  int m_chunkSize;  
  int m_chunkMask;  
  char[][] m_array;
  int m_lastChunk = 0;
  int m_firstFree = 0;
  FastStringBuffer m_innerFSB = null;
  public FastStringBuffer(int initChunkBits, int maxChunkBits,
                          int rebundleBits)
  {
    if(DEBUG_FORCE_INIT_BITS!=0) initChunkBits=DEBUG_FORCE_INIT_BITS;
    if(DEBUG_FORCE_FIXED_CHUNKSIZE) maxChunkBits=initChunkBits;
    m_array = new char[16][];
    if (initChunkBits > maxChunkBits)
      initChunkBits = maxChunkBits;
    m_chunkBits = initChunkBits;
    m_maxChunkBits = maxChunkBits;
    m_rebundleBits = rebundleBits;
    m_chunkSize = 1 << (initChunkBits);
    m_chunkMask = m_chunkSize - 1;
    m_array[0] = new char[m_chunkSize];
  }
  public FastStringBuffer(int initChunkBits, int maxChunkBits)
  {
    this(initChunkBits, maxChunkBits, 2);
  }
  public FastStringBuffer(int initChunkBits)
  {
    this(initChunkBits, 15, 2);
  }
  public FastStringBuffer()
  {
    this(10, 15, 2);
  }
  public final int size()
  {
    return (m_lastChunk << m_chunkBits) + m_firstFree;
  }
  public final int length()
  {
    return (m_lastChunk << m_chunkBits) + m_firstFree;
  }
  public final void reset()
  {
    m_lastChunk = 0;
    m_firstFree = 0;
    FastStringBuffer innermost = this;
    while (innermost.m_innerFSB != null)
    {
      innermost = innermost.m_innerFSB;
    }
    m_chunkBits = innermost.m_chunkBits;
    m_chunkSize = innermost.m_chunkSize;
    m_chunkMask = innermost.m_chunkMask;
    m_innerFSB = null;
    m_array = new char[16][0];
    m_array[0] = new char[m_chunkSize];
  }
  public final void setLength(int l)
  {
    m_lastChunk = l >>> m_chunkBits;
    if (m_lastChunk == 0 && m_innerFSB != null)
    {
      m_innerFSB.setLength(l, this);
    }
    else
    {
      m_firstFree = l & m_chunkMask;
      if(m_firstFree==0 && m_lastChunk>0)
      {
      	--m_lastChunk;
      	m_firstFree=m_chunkSize;
      }
    }
  }
  private final void setLength(int l, FastStringBuffer rootFSB)
  {
    m_lastChunk = l >>> m_chunkBits;
    if (m_lastChunk == 0 && m_innerFSB != null)
    {
      m_innerFSB.setLength(l, rootFSB);
    }
    else
    {
      rootFSB.m_chunkBits = m_chunkBits;
      rootFSB.m_maxChunkBits = m_maxChunkBits;
      rootFSB.m_rebundleBits = m_rebundleBits;
      rootFSB.m_chunkSize = m_chunkSize;
      rootFSB.m_chunkMask = m_chunkMask;
      rootFSB.m_array = m_array;
      rootFSB.m_innerFSB = m_innerFSB;
      rootFSB.m_lastChunk = m_lastChunk;
      rootFSB.m_firstFree = l & m_chunkMask;
    }
  }
  public final String toString()
  {
    int length = (m_lastChunk << m_chunkBits) + m_firstFree;
    return getString(new StringBuffer(length), 0, 0, length).toString();
  }
  public final void append(char value)
  {
    char[] chunk;
    if (m_firstFree < m_chunkSize)  
      chunk = m_array[m_lastChunk];
    else
    {
      int i = m_array.length;
      if (m_lastChunk + 1 == i)
      {
        char[][] newarray = new char[i + 16][];
        System.arraycopy(m_array, 0, newarray, 0, i);
        m_array = newarray;
      }
      chunk = m_array[++m_lastChunk];
      if (chunk == null)
      {
        if (m_lastChunk == 1 << m_rebundleBits
                && m_chunkBits < m_maxChunkBits)
        {
          m_innerFSB = new FastStringBuffer(this);
        }
        chunk = m_array[m_lastChunk] = new char[m_chunkSize];
      }
      m_firstFree = 0;
    }
    chunk[m_firstFree++] = value;
  }
  public final void append(String value)
  {
    if (value == null) 
      return;
    int strlen = value.length();
    if (0 == strlen)
      return;
    int copyfrom = 0;
    char[] chunk = m_array[m_lastChunk];
    int available = m_chunkSize - m_firstFree;
    while (strlen > 0)
    {
      if (available > strlen)
        available = strlen;
      value.getChars(copyfrom, copyfrom + available, m_array[m_lastChunk],
                     m_firstFree);
      strlen -= available;
      copyfrom += available;
      if (strlen > 0)
      {
        int i = m_array.length;
        if (m_lastChunk + 1 == i)
        {
          char[][] newarray = new char[i + 16][];
          System.arraycopy(m_array, 0, newarray, 0, i);
          m_array = newarray;
        }
        chunk = m_array[++m_lastChunk];
        if (chunk == null)
        {
          if (m_lastChunk == 1 << m_rebundleBits
                  && m_chunkBits < m_maxChunkBits)
          {
            m_innerFSB = new FastStringBuffer(this);
          }
          chunk = m_array[m_lastChunk] = new char[m_chunkSize];
        }
        available = m_chunkSize;
        m_firstFree = 0;
      }
    }
    m_firstFree += available;
  }
  public final void append(StringBuffer value)
  {
    if (value == null) 
      return;
    int strlen = value.length();
    if (0 == strlen)
      return;
    int copyfrom = 0;
    char[] chunk = m_array[m_lastChunk];
    int available = m_chunkSize - m_firstFree;
    while (strlen > 0)
    {
      if (available > strlen)
        available = strlen;
      value.getChars(copyfrom, copyfrom + available, m_array[m_lastChunk],
                     m_firstFree);
      strlen -= available;
      copyfrom += available;
      if (strlen > 0)
      {
        int i = m_array.length;
        if (m_lastChunk + 1 == i)
        {
          char[][] newarray = new char[i + 16][];
          System.arraycopy(m_array, 0, newarray, 0, i);
          m_array = newarray;
        }
        chunk = m_array[++m_lastChunk];
        if (chunk == null)
        {
          if (m_lastChunk == 1 << m_rebundleBits
                  && m_chunkBits < m_maxChunkBits)
          {
            m_innerFSB = new FastStringBuffer(this);
          }
          chunk = m_array[m_lastChunk] = new char[m_chunkSize];
        }
        available = m_chunkSize;
        m_firstFree = 0;
      }
    }
    m_firstFree += available;
  }
  public final void append(char[] chars, int start, int length)
  {
    int strlen = length;
    if (0 == strlen)
      return;
    int copyfrom = start;
    char[] chunk = m_array[m_lastChunk];
    int available = m_chunkSize - m_firstFree;
    while (strlen > 0)
    {
      if (available > strlen)
        available = strlen;
      System.arraycopy(chars, copyfrom, m_array[m_lastChunk], m_firstFree,
                       available);
      strlen -= available;
      copyfrom += available;
      if (strlen > 0)
      {
        int i = m_array.length;
        if (m_lastChunk + 1 == i)
        {
          char[][] newarray = new char[i + 16][];
          System.arraycopy(m_array, 0, newarray, 0, i);
          m_array = newarray;
        }
        chunk = m_array[++m_lastChunk];
        if (chunk == null)
        {
          if (m_lastChunk == 1 << m_rebundleBits
                  && m_chunkBits < m_maxChunkBits)
          {
            m_innerFSB = new FastStringBuffer(this);
          }
          chunk = m_array[m_lastChunk] = new char[m_chunkSize];
        }
        available = m_chunkSize;
        m_firstFree = 0;
      }
    }
    m_firstFree += available;
  }
  public final void append(FastStringBuffer value)
  {
    if (value == null) 
      return;
    int strlen = value.length();
    if (0 == strlen)
      return;
    int copyfrom = 0;
    char[] chunk = m_array[m_lastChunk];
    int available = m_chunkSize - m_firstFree;
    while (strlen > 0)
    {
      if (available > strlen)
        available = strlen;
      int sourcechunk = (copyfrom + value.m_chunkSize - 1)
                        >>> value.m_chunkBits;
      int sourcecolumn = copyfrom & value.m_chunkMask;
      int runlength = value.m_chunkSize - sourcecolumn;
      if (runlength > available)
        runlength = available;
      System.arraycopy(value.m_array[sourcechunk], sourcecolumn,
                       m_array[m_lastChunk], m_firstFree, runlength);
      if (runlength != available)
        System.arraycopy(value.m_array[sourcechunk + 1], 0,
                         m_array[m_lastChunk], m_firstFree + runlength,
                         available - runlength);
      strlen -= available;
      copyfrom += available;
      if (strlen > 0)
      {
        int i = m_array.length;
        if (m_lastChunk + 1 == i)
        {
          char[][] newarray = new char[i + 16][];
          System.arraycopy(m_array, 0, newarray, 0, i);
          m_array = newarray;
        }
        chunk = m_array[++m_lastChunk];
        if (chunk == null)
        {
          if (m_lastChunk == 1 << m_rebundleBits
                  && m_chunkBits < m_maxChunkBits)
          {
            m_innerFSB = new FastStringBuffer(this);
          }
          chunk = m_array[m_lastChunk] = new char[m_chunkSize];
        }
        available = m_chunkSize;
        m_firstFree = 0;
      }
    }
    m_firstFree += available;
  }
  public boolean isWhitespace(int start, int length)
  {
    int sourcechunk = start >>> m_chunkBits;
    int sourcecolumn = start & m_chunkMask;
    int available = m_chunkSize - sourcecolumn;
    boolean chunkOK;
    while (length > 0)
    {
      int runlength = (length <= available) ? length : available;
      if (sourcechunk == 0 && m_innerFSB != null)
        chunkOK = m_innerFSB.isWhitespace(sourcecolumn, runlength);
      else
        chunkOK = org.apache.xml.utils.XMLCharacterRecognizer.isWhiteSpace(
          m_array[sourcechunk], sourcecolumn, runlength);
      if (!chunkOK)
        return false;
      length -= runlength;
      ++sourcechunk;
      sourcecolumn = 0;
      available = m_chunkSize;
    }
    return true;
  }
  public String getString(int start, int length)
  {
    int startColumn = start & m_chunkMask;
    int startChunk = start >>> m_chunkBits;
    if (startColumn + length < m_chunkMask && m_innerFSB == null) {
      return getOneChunkString(startChunk, startColumn, length);
    }
    return getString(new StringBuffer(length), startChunk, startColumn,
                     length).toString();
  }
  protected String getOneChunkString(int startChunk, int startColumn,
                                     int length) {
    return new String(m_array[startChunk], startColumn, length);
  }
  StringBuffer getString(StringBuffer sb, int start, int length)
  {
    return getString(sb, start >>> m_chunkBits, start & m_chunkMask, length);
  }
  StringBuffer getString(StringBuffer sb, int startChunk, int startColumn,
                         int length)
  {
    int stop = (startChunk << m_chunkBits) + startColumn + length;
    int stopChunk = stop >>> m_chunkBits;
    int stopColumn = stop & m_chunkMask;
    for (int i = startChunk; i < stopChunk; ++i)
    {
      if (i == 0 && m_innerFSB != null)
        m_innerFSB.getString(sb, startColumn, m_chunkSize - startColumn);
      else
        sb.append(m_array[i], startColumn, m_chunkSize - startColumn);
      startColumn = 0;  
    }
    if (stopChunk == 0 && m_innerFSB != null)
      m_innerFSB.getString(sb, startColumn, stopColumn - startColumn);
    else if (stopColumn > startColumn)
      sb.append(m_array[stopChunk], startColumn, stopColumn - startColumn);
    return sb;
  }
  public char charAt(int pos)
  {
    int startChunk = pos >>> m_chunkBits;
    if (startChunk == 0 && m_innerFSB != null)
      return m_innerFSB.charAt(pos & m_chunkMask);
    else
      return m_array[startChunk][pos & m_chunkMask];
  }
  public void sendSAXcharacters(
          org.xml.sax.ContentHandler ch, int start, int length)
            throws org.xml.sax.SAXException
  {
    int startChunk = start >>> m_chunkBits;
    int startColumn = start & m_chunkMask;
    if (startColumn + length < m_chunkMask && m_innerFSB == null) {
        ch.characters(m_array[startChunk], startColumn, length);
        return;
    }
    int stop = start + length;
    int stopChunk = stop >>> m_chunkBits;
    int stopColumn = stop & m_chunkMask;
    for (int i = startChunk; i < stopChunk; ++i)
    {
      if (i == 0 && m_innerFSB != null)
        m_innerFSB.sendSAXcharacters(ch, startColumn,
                                     m_chunkSize - startColumn);
      else
        ch.characters(m_array[i], startColumn, m_chunkSize - startColumn);
      startColumn = 0;  
    }
    if (stopChunk == 0 && m_innerFSB != null)
      m_innerFSB.sendSAXcharacters(ch, startColumn, stopColumn - startColumn);
    else if (stopColumn > startColumn)
    {
      ch.characters(m_array[stopChunk], startColumn,
                    stopColumn - startColumn);
    }
  }
  public int sendNormalizedSAXcharacters(
          org.xml.sax.ContentHandler ch, int start, int length)
            throws org.xml.sax.SAXException
  {
	int stateForNextChunk=SUPPRESS_LEADING_WS;
    int stop = start + length;
    int startChunk = start >>> m_chunkBits;
    int startColumn = start & m_chunkMask;
    int stopChunk = stop >>> m_chunkBits;
    int stopColumn = stop & m_chunkMask;
    for (int i = startChunk; i < stopChunk; ++i)
    {
      if (i == 0 && m_innerFSB != null)
				stateForNextChunk=
        m_innerFSB.sendNormalizedSAXcharacters(ch, startColumn,
                                     m_chunkSize - startColumn);
      else
				stateForNextChunk=
        sendNormalizedSAXcharacters(m_array[i], startColumn, 
                                    m_chunkSize - startColumn, 
																		ch,stateForNextChunk);
      startColumn = 0;  
    }
    if (stopChunk == 0 && m_innerFSB != null)
			stateForNextChunk= 
      m_innerFSB.sendNormalizedSAXcharacters(ch, startColumn, stopColumn - startColumn);
    else if (stopColumn > startColumn)
    {
			stateForNextChunk= 
      sendNormalizedSAXcharacters(m_array[stopChunk], 
																	startColumn, stopColumn - startColumn,
																	ch, stateForNextChunk | SUPPRESS_TRAILING_WS);
    }
		return stateForNextChunk;
  }
  static final char[] SINGLE_SPACE = {' '};
  static int sendNormalizedSAXcharacters(char ch[], 
             int start, int length, 
             org.xml.sax.ContentHandler handler,
						 int edgeTreatmentFlags)
          throws org.xml.sax.SAXException
  {
     boolean processingLeadingWhitespace =
                       ((edgeTreatmentFlags & SUPPRESS_LEADING_WS) != 0);
     boolean seenWhitespace = ((edgeTreatmentFlags & CARRY_WS) != 0);
     int currPos = start;
     int limit = start+length;
     if (processingLeadingWhitespace) {
         for (; currPos < limit
                && XMLCharacterRecognizer.isWhiteSpace(ch[currPos]);
              currPos++) { }
         if (currPos == limit) {
             return edgeTreatmentFlags;
         }
     }
     while (currPos < limit) {
         int startNonWhitespace = currPos;
         for (; currPos < limit
                && !XMLCharacterRecognizer.isWhiteSpace(ch[currPos]);
              currPos++) { }
         if (startNonWhitespace != currPos) {
             if (seenWhitespace) {
                 handler.characters(SINGLE_SPACE, 0, 1);
                 seenWhitespace = false;
             }
             handler.characters(ch, startNonWhitespace,
                                currPos - startNonWhitespace);
         }
         int startWhitespace = currPos;
         for (; currPos < limit
                && XMLCharacterRecognizer.isWhiteSpace(ch[currPos]);
              currPos++) { }
         if (startWhitespace != currPos) {
             seenWhitespace = true;
         }
     }
     return (seenWhitespace ? CARRY_WS : 0)
            | (edgeTreatmentFlags & SUPPRESS_TRAILING_WS);
  }
  public static void sendNormalizedSAXcharacters(char ch[], 
             int start, int length, 
             org.xml.sax.ContentHandler handler)
          throws org.xml.sax.SAXException
  {
		sendNormalizedSAXcharacters(ch, start, length, 
             handler, SUPPRESS_BOTH);
	}
  public void sendSAXComment(
          org.xml.sax.ext.LexicalHandler ch, int start, int length)
            throws org.xml.sax.SAXException
  {
    String comment = getString(start, length);
    ch.comment(comment.toCharArray(), 0, length);
  }
  private void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin)
  {
  }
  private FastStringBuffer(FastStringBuffer source)
  {
    m_chunkBits = source.m_chunkBits;
    m_maxChunkBits = source.m_maxChunkBits;
    m_rebundleBits = source.m_rebundleBits;
    m_chunkSize = source.m_chunkSize;
    m_chunkMask = source.m_chunkMask;
    m_array = source.m_array;
    m_innerFSB = source.m_innerFSB;
    m_lastChunk = source.m_lastChunk - 1;
    m_firstFree = source.m_chunkSize;
    source.m_array = new char[16][];
    source.m_innerFSB = this;
    source.m_lastChunk = 1;
    source.m_firstFree = 0;
    source.m_chunkBits += m_rebundleBits;
    source.m_chunkSize = 1 << (source.m_chunkBits);
    source.m_chunkMask = source.m_chunkSize - 1;
  }
}
