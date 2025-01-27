public abstract class XMLStringFactory
{
  public abstract XMLString newstr(String string);
  public abstract XMLString newstr(FastStringBuffer string, int start, 
                                   int length);
  public abstract XMLString newstr(char[] string, int start, 
                                   int length);
  public abstract XMLString emptystr();
}
