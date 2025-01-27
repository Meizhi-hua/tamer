public class XMLCharacterRecognizer
{
  public static boolean isWhiteSpace(char ch)
  {
    return (ch == 0x20) || (ch == 0x09) || (ch == 0xD) || (ch == 0xA);
  }
  public static boolean isWhiteSpace(char ch[], int start, int length)
  {
    int end = start + length;
    for (int s = start; s < end; s++)
    {
      if (!isWhiteSpace(ch[s]))
        return false;
    }
    return true;
  }
  public static boolean isWhiteSpace(StringBuffer buf)
  {
    int n = buf.length();
    for (int i = 0; i < n; i++)
    {
      if (!isWhiteSpace(buf.charAt(i)))
        return false;
    }
    return true;
  }
  public static boolean isWhiteSpace(String s)
  {
    if(null != s)
    {
      int n = s.length();
      for (int i = 0; i < n; i++)
      {
        if (!isWhiteSpace(s.charAt(i)))
          return false;
      }
    }
    return true;
  }
}
