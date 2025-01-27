public class ExtensionNamespaceSupport
{
  String m_namespace = null;
  String m_handlerClass = null;
  Class [] m_sig = null;  
  Object [] m_args = null;
  public ExtensionNamespaceSupport(String namespace, 
                                   String handlerClass, 
                                   Object[] constructorArgs)
  {
    m_namespace = namespace;
    m_handlerClass = handlerClass;
    m_args = constructorArgs;
    m_sig = new Class[m_args.length];
    for (int i = 0; i < m_args.length; i++)
    {
      if (m_args[i] != null)
        m_sig[i] = m_args[i].getClass();
      else 
      {
        m_sig = null;
        break;
      }
    }
  }
  public String getNamespace()
  {
    return m_namespace;
  }
  public ExtensionHandler launch()
    throws TransformerException
  {
    ExtensionHandler handler = null;
    try
    {
      Class cl = ExtensionHandler.getClassForName(m_handlerClass);
      Constructor con = null;
      if (m_sig != null)
        con = cl.getConstructor(m_sig);
      else 
      {
        Constructor[] cons = cl.getConstructors();
        for (int i = 0; i < cons.length; i ++)
        {
          if (cons[i].getParameterTypes().length == m_args.length)
          {
            con = cons[i];
            break;
          }
        }
      }
      if (con != null)
        handler = (ExtensionHandler)con.newInstance(m_args);
      else
        throw new TransformerException("ExtensionHandler constructor not found");
    }
    catch (Exception e)
    {
      throw new TransformerException(e);
    }
    return handler;
  }
}
