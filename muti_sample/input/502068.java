public class MsgMgr
{
  public MsgMgr(TransformerImpl transformer)
  {
    m_transformer = transformer;
  }
  private TransformerImpl m_transformer;
  public void message(SourceLocator srcLctr, String msg, boolean terminate) throws TransformerException
  {
    ErrorListener errHandler = m_transformer.getErrorListener();
    if (null != errHandler)
    {
      errHandler.warning(new TransformerException(msg, srcLctr));
    }
    else
    {
      if (terminate)
        throw new TransformerException(msg, srcLctr);
      else
        System.out.println(msg);
    }
  }
  public void warn(SourceLocator srcLctr, String msg) throws TransformerException
  {
    warn(srcLctr, null, null, msg, null);
  }
  public void warn(SourceLocator srcLctr, String msg, Object[] args) throws TransformerException
  {
    warn(srcLctr, null, null, msg, args);
  }
  public void warn(SourceLocator srcLctr, Node styleNode, Node sourceNode, String msg)
          throws TransformerException
  {
    warn(srcLctr, styleNode, sourceNode, msg, null);
  }
  public void warn(SourceLocator srcLctr, Node styleNode, Node sourceNode, String msg, Object args[])
          throws TransformerException
  {
    String formattedMsg = XSLMessages.createWarning(msg, args);
    ErrorListener errHandler = m_transformer.getErrorListener();
    if (null != errHandler)
      errHandler.warning(new TransformerException(formattedMsg, srcLctr));
    else
      System.out.println(formattedMsg);
  }
  public void error(SourceLocator srcLctr, String msg) throws TransformerException
  {
    error(srcLctr, null, null, msg, null);
  }
  public void error(SourceLocator srcLctr, String msg, Object[] args) throws TransformerException
  {
    error(srcLctr, null, null, msg, args);
  }
  public void error(SourceLocator srcLctr, String msg, Exception e) throws TransformerException
  {
    error(srcLctr, msg, null, e);
  }
  public void error(SourceLocator srcLctr, String msg, Object args[], Exception e) throws TransformerException
  {
    String formattedMsg = XSLMessages.createMessage(msg, args);
    ErrorListener errHandler = m_transformer.getErrorListener();
    if (null != errHandler)
      errHandler.fatalError(new TransformerException(formattedMsg, srcLctr));
    else
      throw new TransformerException(formattedMsg, srcLctr);
  }
  public void error(SourceLocator srcLctr, Node styleNode, Node sourceNode, String msg)
          throws TransformerException
  {
    error(srcLctr, styleNode, sourceNode, msg, null);
  }
  public void error(SourceLocator srcLctr, Node styleNode, Node sourceNode, String msg, Object args[])
          throws TransformerException
  {
    String formattedMsg = XSLMessages.createMessage(msg, args);
    ErrorListener errHandler = m_transformer.getErrorListener();
    if (null != errHandler)
      errHandler.fatalError(new TransformerException(formattedMsg, srcLctr));
    else
      throw new TransformerException(formattedMsg, srcLctr);
  }
}
