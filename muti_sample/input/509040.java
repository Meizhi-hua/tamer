public class ElemExtensionCall extends ElemLiteralResult
{
    static final long serialVersionUID = 3171339708500216920L;
  String m_extns;
  String m_lang;
  String m_srcURL;
  String m_scriptSrc;
  ElemExtensionDecl m_decl = null;
  public int getXSLToken()
  {
    return Constants.ELEMNAME_EXTENSIONCALL;
  }
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    m_extns = this.getNamespace();   
    m_decl = getElemExtensionDecl(sroot, m_extns);
    if (m_decl == null)
      sroot.getExtensionNamespacesManager().registerExtension(m_extns);
  }
  private ElemExtensionDecl getElemExtensionDecl(StylesheetRoot stylesheet,
          String namespace)
  {
    ElemExtensionDecl decl = null;
    int n = stylesheet.getGlobalImportCount();
    for (int i = 0; i < n; i++)
    {
      Stylesheet imported = stylesheet.getGlobalImport(i);
      for (ElemTemplateElement child = imported.getFirstChildElem();
              child != null; child = child.getNextSiblingElem())
      {
        if (Constants.ELEMNAME_EXTENSIONDECL == child.getXSLToken())
        {
          decl = (ElemExtensionDecl) child;
          String prefix = decl.getPrefix();
          String declNamespace = child.getNamespaceForPrefix(prefix);
          if (namespace.equals(declNamespace))
          {
            return decl;
          }
        }
      }
    }
    return null;
  }
  private void executeFallbacks(
          TransformerImpl transformer)
            throws TransformerException
  {
    for (ElemTemplateElement child = m_firstChild; child != null;
             child = child.m_nextSibling)
    {
      if (child.getXSLToken() == Constants.ELEMNAME_FALLBACK)
      {
        try
        {
          transformer.pushElemTemplateElement(child);
          ((ElemFallback) child).executeFallback(transformer);
        }
        finally
        {
          transformer.popElemTemplateElement();
        }
      }
    }
  }
  private boolean hasFallbackChildren()
  {
    for (ElemTemplateElement child = m_firstChild; child != null;
             child = child.m_nextSibling)
    {
      if (child.getXSLToken() == Constants.ELEMNAME_FALLBACK)
        return true;
    }
    return false;
  }
  public void execute(TransformerImpl transformer)
            throws TransformerException
  {
    if (transformer.getStylesheet().isSecureProcessing())
      throw new TransformerException(
        XSLMessages.createMessage(
          XSLTErrorResources.ER_EXTENSION_ELEMENT_NOT_ALLOWED_IN_SECURE_PROCESSING,
          new Object[] {getRawName()}));
    try
    {
      transformer.getResultTreeHandler().flushPending();
      ExtensionsTable etable = transformer.getExtensionsTable();
      ExtensionHandler nsh = etable.get(m_extns);
      if (null == nsh)
      {
        if (hasFallbackChildren())
        {
          executeFallbacks(transformer);
        }
        else
        {
	  TransformerException te = new TransformerException(XSLMessages.createMessage(
	  	XSLTErrorResources.ER_CALL_TO_EXT_FAILED, new Object[]{getNodeName()}));
	  transformer.getErrorListener().fatalError(te);
        }
        return;
      }
      try
      {
        nsh.processElement(this.getLocalName(), this, transformer,
                           getStylesheet(), this);
      }
      catch (Exception e)
      {
	if (hasFallbackChildren())
	  executeFallbacks(transformer);
	else
	{
          if(e instanceof TransformerException)
          {
            TransformerException te = (TransformerException)e;
            if(null == te.getLocator())
              te.setLocator(this);
            transformer.getErrorListener().fatalError(te);            
          }
          else if (e instanceof RuntimeException)
          {
            transformer.getErrorListener().fatalError(new TransformerException(e));
          }
          else
          {
            transformer.getErrorListener().warning(new TransformerException(e));
          }
        }
      }
    }
    catch(TransformerException e)
    {
      transformer.getErrorListener().fatalError(e);
    }
    catch(SAXException se) {
      throw new TransformerException(se);
    }
  }
  public String getAttribute(
          String rawName, org.w3c.dom.Node sourceNode, TransformerImpl transformer)
            throws TransformerException
  {
    AVT avt = getLiteralResultAttribute(rawName);
    if ((null != avt) && avt.getRawName().equals(rawName))
    {
      XPathContext xctxt = transformer.getXPathContext();
      return avt.evaluate(xctxt, 
            xctxt.getDTMHandleFromNode(sourceNode), 
            this);
    }
    return null;
  }
  protected boolean accept(XSLTVisitor visitor)
  {
  	return visitor.visitExtensionElement(this);
  }
}
