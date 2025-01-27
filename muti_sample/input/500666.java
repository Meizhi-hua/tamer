class WriterToASCI extends Writer implements WriterChain
{
  private final OutputStream m_os;
  public WriterToASCI(OutputStream os)
  {
    m_os = os;
  }
  public void write(char chars[], int start, int length)
          throws java.io.IOException
  {
    int n = length+start;
    for (int i = start; i < n; i++)
    {
      m_os.write(chars[i]);
    }
  }
  public void write(int c) throws IOException
  {
    m_os.write(c);
  }
  public void write(String s) throws IOException
  {
    int n = s.length();
    for (int i = 0; i < n; i++)
    {
      m_os.write(s.charAt(i));
    }
  }
  public void flush() throws java.io.IOException
  {
    m_os.flush();
  }
  public void close() throws java.io.IOException
  {
    m_os.close();
  }
  public OutputStream getOutputStream()
  {
    return m_os;
  }
  public Writer getWriter()
  {
      return null;
  }
}
