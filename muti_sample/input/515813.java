public class FuncDocument extends Function2Args
{
    static final long serialVersionUID = 2483304325971281424L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    int context = xctxt.getCurrentNode();
    DTM dtm = xctxt.getDTM(context);
    int docContext = dtm.getDocumentRoot(context);
    XObject arg = (XObject) this.getArg0().execute(xctxt);
    String base = "";
    Expression arg1Expr = this.getArg1();
    if (null != arg1Expr)
    {
      XObject arg2 = arg1Expr.execute(xctxt);
      if (XObject.CLASS_NODESET == arg2.getType())
      {
        int baseNode = arg2.iter().nextNode();
        if (baseNode == DTM.NULL)
        {
          	warn(xctxt, XSLTErrorResources.WG_EMPTY_SECOND_ARG, null);
          	XNodeSet nodes = new XNodeSet(xctxt.getDTMManager());
   	        return nodes;
        } else{
	        DTM baseDTM = xctxt.getDTM(baseNode);
    	    base = baseDTM.getDocumentBaseURI();
        }
      }
      else
      {
        arg2.iter();
      }
    }
    else
    {
      assertion(null != xctxt.getNamespaceContext(), "Namespace context can not be null!");
      base = xctxt.getNamespaceContext().getBaseIdentifier();
    }
    XNodeSet nodes = new XNodeSet(xctxt.getDTMManager());
    NodeSetDTM mnl = nodes.mutableNodeset();
    DTMIterator iterator = (XObject.CLASS_NODESET == arg.getType())
                            ? arg.iter() : null;
    int pos = DTM.NULL;
    while ((null == iterator) || (DTM.NULL != (pos = iterator.nextNode())))
    {
      XMLString ref = (null != iterator)
                   ? xctxt.getDTM(pos).getStringValue(pos) : arg.xstr();
      if (null == arg1Expr && DTM.NULL != pos)
      {
        DTM baseDTM = xctxt.getDTM(pos);
        base = baseDTM.getDocumentBaseURI();
      }
      if (null == ref)
        continue;
      if (DTM.NULL == docContext)
      {
        error(xctxt, XSLTErrorResources.ER_NO_CONTEXT_OWNERDOC, null);  
      }
      int indexOfColon = ref.indexOf(':');
      int indexOfSlash = ref.indexOf('/');
      if ((indexOfColon != -1) && (indexOfSlash != -1)
              && (indexOfColon < indexOfSlash))
      {
        base = null;
      }
      int newDoc = getDoc(xctxt, context, ref.toString(), base);
      if (DTM.NULL != newDoc)
      {
        if (!mnl.contains(newDoc))
        {
          mnl.addElement(newDoc);
        }
      }
      if (null == iterator || newDoc == DTM.NULL)
        break;
    }
    return nodes;
  }
  int getDoc(XPathContext xctxt, int context, String uri, String base)
          throws javax.xml.transform.TransformerException
  {
    SourceTreeManager treeMgr = xctxt.getSourceTreeManager();
    Source source;
    int newDoc;
    try
    {
      source = treeMgr.resolveURI(base, uri, xctxt.getSAXLocator());
      newDoc = treeMgr.getNode(source);
    }
    catch (IOException ioe)
    {
      throw new TransformerException(ioe.getMessage(), 
        (SourceLocator)xctxt.getSAXLocator(), ioe);
    }
    catch(TransformerException te)
    {
      throw new TransformerException(te);
    }
    if (DTM.NULL != newDoc)
      return newDoc;
    if (uri.length() == 0)
    {
      uri = xctxt.getNamespaceContext().getBaseIdentifier();
      try
      {
        source = treeMgr.resolveURI(base, uri, xctxt.getSAXLocator());
      }
      catch (IOException ioe)
      {
        throw new TransformerException(ioe.getMessage(), 
          (SourceLocator)xctxt.getSAXLocator(), ioe);
      }
    }
    String diagnosticsString = null;
    try
    {
      if ((null != uri) && (uri.length() > 0))
      {
        newDoc = treeMgr.getSourceTree(source, xctxt.getSAXLocator(), xctxt);
      }
      else
        warn(xctxt, XSLTErrorResources.WG_CANNOT_MAKE_URL_FROM,
             new Object[]{ ((base == null) ? "" : base) + uri });  
    }
    catch (Throwable throwable)
    {
      newDoc = DTM.NULL;
      while (throwable
             instanceof org.apache.xml.utils.WrappedRuntimeException)
      {
        throwable =
          ((org.apache.xml.utils.WrappedRuntimeException) throwable).getException();
      }
      if ((throwable instanceof NullPointerException)
              || (throwable instanceof ClassCastException))
      {
        throw new org.apache.xml.utils.WrappedRuntimeException(
          (Exception) throwable);
      }
      StringWriter sw = new StringWriter();
      PrintWriter diagnosticsWriter = new PrintWriter(sw);
      if (throwable instanceof TransformerException)
      {
        TransformerException spe = (TransformerException) throwable;
        {
          Throwable e = spe;
          while (null != e)
          {
            if (null != e.getMessage())
            {
              diagnosticsWriter.println(" (" + e.getClass().getName() + "): "
                                        + e.getMessage());
            }
            if (e instanceof TransformerException)
            {
              TransformerException spe2 = (TransformerException) e;
              SourceLocator locator = spe2.getLocator();
              if ((null != locator) && (null != locator.getSystemId()))
                diagnosticsWriter.println("   ID: " + locator.getSystemId()
                                          + " Line #" + locator.getLineNumber()
                                          + " Column #"
                                          + locator.getColumnNumber());
              e = spe2.getException();
              if (e instanceof org.apache.xml.utils.WrappedRuntimeException)
                e = ((org.apache.xml.utils.WrappedRuntimeException) e).getException();
            }
            else
              e = null;
          }
        }
      }
      else
      {
        diagnosticsWriter.println(" (" + throwable.getClass().getName()
                                  + "): " + throwable.getMessage());
      }
      diagnosticsString = throwable.getMessage(); 
    }
    if (DTM.NULL == newDoc)
    {
      if (null != diagnosticsString)
      {
        warn(xctxt, XSLTErrorResources.WG_CANNOT_LOAD_REQUESTED_DOC,
             new Object[]{ diagnosticsString });  
      }
      else
        warn(xctxt, XSLTErrorResources.WG_CANNOT_LOAD_REQUESTED_DOC,
             new Object[]{
               uri == null
               ? ((base == null) ? "" : base) + uri : uri.toString() });  
    }
    else
    {
    }
    return newDoc;
  }
  public void error(XPathContext xctxt, String msg, Object args[])
          throws javax.xml.transform.TransformerException
  {
    String formattedMsg = XSLMessages.createMessage(msg, args);
    ErrorListener errHandler = xctxt.getErrorListener();
    TransformerException spe = new TransformerException(formattedMsg,
                              (SourceLocator)xctxt.getSAXLocator());
    if (null != errHandler)
      errHandler.error(spe);
    else
      System.out.println(formattedMsg);
  }
  public void warn(XPathContext xctxt, String msg, Object args[])
          throws javax.xml.transform.TransformerException
  {
    String formattedMsg = XSLMessages.createWarning(msg, args);
    ErrorListener errHandler = xctxt.getErrorListener();
    TransformerException spe = new TransformerException(formattedMsg,
                              (SourceLocator)xctxt.getSAXLocator());
    if (null != errHandler)
      errHandler.warning(spe);
    else
      System.out.println(formattedMsg);
  }
  public void checkNumberArgs(int argNum) throws WrongNumberArgsException
  {
    if ((argNum < 1) || (argNum > 2))
      reportWrongNumberArgs();
  }
  protected void reportWrongNumberArgs() throws WrongNumberArgsException {
      throw new WrongNumberArgsException(XSLMessages.createMessage(XSLTErrorResources.ER_ONE_OR_TWO, null)); 
  }
  public boolean isNodesetExpr()
  {
    return true;
  }
}
