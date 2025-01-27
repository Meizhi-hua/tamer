public class FuncConcat extends FunctionMultiArgs
{
    static final long serialVersionUID = 1737228885202314413L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    StringBuffer sb = new StringBuffer();
    sb.append(m_arg0.execute(xctxt).str());
    sb.append(m_arg1.execute(xctxt).str());
    if (null != m_arg2)
      sb.append(m_arg2.execute(xctxt).str());
    if (null != m_args)
    {
      for (int i = 0; i < m_args.length; i++)
      {
        sb.append(m_args[i].execute(xctxt).str());
      }
    }
    return new XString(sb.toString());
  }
  public void checkNumberArgs(int argNum) throws WrongNumberArgsException
  {
    if (argNum < 2)
      reportWrongNumberArgs();
  }
  protected void reportWrongNumberArgs() throws WrongNumberArgsException {
      throw new WrongNumberArgsException(XSLMessages.createXPATHMessage("gtone", null));
  }
}
