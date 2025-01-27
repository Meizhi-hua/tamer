public class ExtensionsTable
{  
  public Hashtable m_extensionFunctionNamespaces = new Hashtable();
  private StylesheetRoot m_sroot;
  public ExtensionsTable(StylesheetRoot sroot)
    throws javax.xml.transform.TransformerException
  {
    m_sroot = sroot;
    Vector extensions = m_sroot.getExtensions();
    for (int i = 0; i < extensions.size(); i++)
    {
      ExtensionNamespaceSupport extNamespaceSpt = 
                 (ExtensionNamespaceSupport)extensions.get(i);
      ExtensionHandler extHandler = extNamespaceSpt.launch();
        if (extHandler != null)
          addExtensionNamespace(extNamespaceSpt.getNamespace(), extHandler);
      }
    }
  public ExtensionHandler get(String extns)
  {
    return (ExtensionHandler) m_extensionFunctionNamespaces.get(extns);
  }
  public void addExtensionNamespace(String uri, ExtensionHandler extNS)
  {
    m_extensionFunctionNamespaces.put(uri, extNS);
  }
  public boolean functionAvailable(String ns, String funcName)
          throws javax.xml.transform.TransformerException
  {
    boolean isAvailable = false;
    if (null != ns)
    {
      ExtensionHandler extNS = 
           (ExtensionHandler) m_extensionFunctionNamespaces.get(ns);
      if (extNS != null)
        isAvailable = extNS.isFunctionAvailable(funcName);
    }
    return isAvailable;
  }
  public boolean elementAvailable(String ns, String elemName)
          throws javax.xml.transform.TransformerException
  {
    boolean isAvailable = false;
    if (null != ns)
    {
      ExtensionHandler extNS = 
               (ExtensionHandler) m_extensionFunctionNamespaces.get(ns);
      if (extNS != null) 
        isAvailable = extNS.isElementAvailable(elemName);
    } 
    return isAvailable;        
  }  
  public Object extFunction(String ns, String funcName, 
                            Vector argVec, Object methodKey, 
                            ExpressionContext exprContext)
            throws javax.xml.transform.TransformerException
  {
    Object result = null;
    if (null != ns)
    {
      ExtensionHandler extNS =
        (ExtensionHandler) m_extensionFunctionNamespaces.get(ns);
      if (null != extNS)
      {
        try
        {
          result = extNS.callFunction(funcName, argVec, methodKey,
                                      exprContext);
        }
        catch (javax.xml.transform.TransformerException e)
        {
          throw e;
        }
        catch (Exception e)
        {
          throw new javax.xml.transform.TransformerException(e);
        }
      }
      else
      {
        throw new XPathProcessorException(XSLMessages.createMessage(XSLTErrorResources.ER_EXTENSION_FUNC_UNKNOWN, new Object[]{ns, funcName })); 
      }
    }
    return result;    
  }
  public Object extFunction(FuncExtFunction extFunction, Vector argVec, 
                            ExpressionContext exprContext)
         throws javax.xml.transform.TransformerException
  {
    Object result = null;
    String ns = extFunction.getNamespace();
    if (null != ns)
    {
      ExtensionHandler extNS =
        (ExtensionHandler) m_extensionFunctionNamespaces.get(ns);
      if (null != extNS)
      {
        try
        {
          result = extNS.callFunction(extFunction, argVec, exprContext);
        }
        catch (javax.xml.transform.TransformerException e)
        {
          throw e;
        }
        catch (Exception e)
        {
          throw new javax.xml.transform.TransformerException(e);
        }
      }
      else
      {
        throw new XPathProcessorException(XSLMessages.createMessage(XSLTErrorResources.ER_EXTENSION_FUNC_UNKNOWN, 
                                          new Object[]{ns, extFunction.getFunctionName()})); 
      }
    }
    return result;        
  }
}
