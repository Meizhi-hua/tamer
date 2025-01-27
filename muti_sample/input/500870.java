public class XStringForChars extends XString
{
    static final long serialVersionUID = -2235248887220850467L;
  int m_start;
  int m_length;
  protected String m_strCache = null;
  public XStringForChars(char[] val, int start, int length)
  {
    super(val);
    m_start = start;
    m_length = length;
    if(null == val)
      throw new IllegalArgumentException(
                          XSLMessages.createXPATHMessage(XPATHErrorResources.ER_FASTSTRINGBUFFER_CANNOT_BE_NULL, null)); 
  }
  private XStringForChars(String val)
  {
    super(val);
    throw new IllegalArgumentException(
                      XSLMessages.createXPATHMessage(XPATHErrorResources.ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING, null)); 
  }
  public FastStringBuffer fsb()
  {
    throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS, null)); 
  }
  public void appendToFsb(org.apache.xml.utils.FastStringBuffer fsb)
  {
    fsb.append((char[])m_obj, m_start, m_length);
  }
  public boolean hasString()
  {
    return (null != m_strCache);
  }
  public String str()
  {
    if(null == m_strCache)
      m_strCache = new String((char[])m_obj, m_start, m_length);
    return m_strCache;
  }
  public Object object()
  {
    return str();
  }
  public void dispatchCharactersEvents(org.xml.sax.ContentHandler ch)
      throws org.xml.sax.SAXException
  {
    ch.characters((char[])m_obj, m_start, m_length);
  }
  public void dispatchAsComment(org.xml.sax.ext.LexicalHandler lh)
      throws org.xml.sax.SAXException
  {
    lh.comment((char[])m_obj, m_start, m_length);
  }
  public int length()
  {
    return m_length;
  }
  public char charAt(int index)
  {
    return ((char[])m_obj)[index+m_start];
  }
  public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin)
  {
    System.arraycopy((char[])m_obj, m_start+srcBegin, dst, dstBegin, srcEnd);
  }
}
