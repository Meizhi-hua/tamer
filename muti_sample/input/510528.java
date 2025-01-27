public class Arg
{
  private QName m_qname;
  public final QName getQName()
  {
    return m_qname;
  }
  public final void setQName(QName name)
  {
    m_qname = name;
  }
  private XObject m_val;
  public final XObject getVal()
  {
    return m_val;
  }
  public final void setVal(XObject val)
  {
    m_val = val;
  }
  public void detach()
  {
    if(null != m_val)
    {
      m_val.allowDetachToRelease(true);
      m_val.detach();
    }
  }
  private String m_expression;
  public String getExpression()
  {
    return m_expression;
  }
  public void setExpression(String expr)
  {
    m_expression = expr;
  }
  private boolean m_isFromWithParam;
   public boolean isFromWithParam()
   {
    return m_isFromWithParam;
   }
  private boolean m_isVisible;
   public boolean isVisible()
   {
    return m_isVisible;
   }
   public void setIsVisible(boolean b)
   {
    m_isVisible = b;
   }
  public Arg()
  {
    m_qname = new QName("");
    ;  
    m_val = null;
    m_expression = null;
    m_isVisible = true;
    m_isFromWithParam = false;
  }
  public Arg(QName qname, String expression, boolean isFromWithParam)
  {
    m_qname = qname;
    m_val = null;
    m_expression = expression;
    m_isFromWithParam = isFromWithParam;
    m_isVisible = !isFromWithParam;
  }
  public Arg(QName qname, XObject val)
  {
    m_qname = qname;
    m_val = val;
    m_isVisible = true;
    m_isFromWithParam = false;
    m_expression = null;
  }
  public boolean equals(Object obj) 
  {
    if(obj instanceof QName)
    {
      return m_qname.equals(obj);
    }
    else
      return super.equals(obj);
  }
  public Arg(QName qname, XObject val, boolean isFromWithParam)
  {
    m_qname = qname;
    m_val = val;
    m_isFromWithParam = isFromWithParam;
    m_isVisible = !isFromWithParam;
    m_expression = null;
  }
}
