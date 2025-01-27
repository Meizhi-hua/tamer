public abstract class Expression implements java.io.Serializable, ExpressionNode, XPathVisitable
{
    static final long serialVersionUID = 565665869777906902L;
  private ExpressionNode m_parent;
  public boolean canTraverseOutsideSubtree()
  {
    return false;
  }
  public XObject execute(XPathContext xctxt, int currentNode)
          throws javax.xml.transform.TransformerException
  {
    return execute(xctxt);
  }
  public XObject execute(
          XPathContext xctxt, int currentNode, DTM dtm, int expType)
            throws javax.xml.transform.TransformerException
  {
    return execute(xctxt);
  }
  public abstract XObject execute(XPathContext xctxt)
    throws javax.xml.transform.TransformerException;
  public XObject execute(XPathContext xctxt, boolean destructiveOK)
    throws javax.xml.transform.TransformerException
  {
  	return execute(xctxt);
  }
  public double num(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    return execute(xctxt).num();
  }
  public boolean bool(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    return execute(xctxt).bool();
  }
  public XMLString xstr(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    return execute(xctxt).xstr();
  }
  public boolean isNodesetExpr()
  {
    return false;
  }
  public int asNode(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
  	DTMIterator iter = execute(xctxt).iter();
    return iter.nextNode();
  }
  public DTMIterator asIterator(XPathContext xctxt, int contextNode)
          throws javax.xml.transform.TransformerException
  {
    try
    {
      xctxt.pushCurrentNodeAndExpression(contextNode, contextNode);
      return execute(xctxt).iter();
    }
    finally
    {
      xctxt.popCurrentNodeAndExpression();
    }
  }
  public DTMIterator asIteratorRaw(XPathContext xctxt, int contextNode)
          throws javax.xml.transform.TransformerException
  {
    try
    {
      xctxt.pushCurrentNodeAndExpression(contextNode, contextNode);
      XNodeSet nodeset = (XNodeSet)execute(xctxt);
      return nodeset.iterRaw();
    }
    finally
    {
      xctxt.popCurrentNodeAndExpression();
    }
  }
  public void executeCharsToContentHandler(
          XPathContext xctxt, ContentHandler handler)
            throws javax.xml.transform.TransformerException,
                   org.xml.sax.SAXException
  {
    XObject obj = execute(xctxt);
    obj.dispatchCharactersEvents(handler);
    obj.detach();
  }
  public boolean isStableNumber()
  {
    return false;
  }
  public abstract void fixupVariables(java.util.Vector vars, int globalsSize);
  public abstract boolean deepEquals(Expression expr);
  protected final boolean isSameClass(Expression expr)
  {
  	if(null == expr)
  	  return false;
  	return (getClass() == expr.getClass());
  }
  public void warn(XPathContext xctxt, String msg, Object[] args)
          throws javax.xml.transform.TransformerException
  {
    java.lang.String fmsg = XSLMessages.createXPATHWarning(msg, args);
    if (null != xctxt)
    {
      ErrorListener eh = xctxt.getErrorListener();
      eh.warning(new TransformerException(fmsg, xctxt.getSAXLocator()));
    }
  }
  public void assertion(boolean b, java.lang.String msg)
  {
    if (!b)
    {
      java.lang.String fMsg = XSLMessages.createXPATHMessage(
        XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION,
        new Object[]{ msg });
      throw new RuntimeException(fMsg);
    }
  }
  public void error(XPathContext xctxt, String msg, Object[] args)
          throws javax.xml.transform.TransformerException
  {
    java.lang.String fmsg = XSLMessages.createXPATHMessage(msg, args);
    if (null != xctxt)
    {
      ErrorListener eh = xctxt.getErrorListener();
      TransformerException te = new TransformerException(fmsg, this);
      eh.fatalError(te);
    }
  }
  public ExpressionNode getExpressionOwner()
  {
  	ExpressionNode parent = exprGetParent();
  	while((null != parent) && (parent instanceof Expression))
  		parent = parent.exprGetParent();
  	return parent;
  }
  public void exprSetParent(ExpressionNode n)
  {
  	assertion(n != this, "Can not parent an expression to itself!");
  	m_parent = n;
  }
  public ExpressionNode exprGetParent()
  {
  	return m_parent;
  }
  public void exprAddChild(ExpressionNode n, int i)
  {
  	assertion(false, "exprAddChild method not implemented!");
  }
  public ExpressionNode exprGetChild(int i)
  {
  	return null;
  }
  public int exprGetNumChildren()
  {
  	return 0;
  }
  public String getPublicId()
  {
  	if(null == m_parent)
  	  return null;
  	return m_parent.getPublicId();
  }
  public String getSystemId()
  {
  	if(null == m_parent)
  	  return null;
  	return m_parent.getSystemId();
  }
  public int getLineNumber()
  {
  	if(null == m_parent)
  	  return 0;
  	return m_parent.getLineNumber();
  }
  public int getColumnNumber()
  {
  	if(null == m_parent)
  	  return 0;
  	return m_parent.getColumnNumber();
  }
}
