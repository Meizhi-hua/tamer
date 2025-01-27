public class OutputProperties extends ElemTemplateElement
        implements Cloneable
{
    static final long serialVersionUID = -6975274363881785488L;
  public OutputProperties()
  {
    this(org.apache.xml.serializer.Method.XML);
  }
  public OutputProperties(Properties defaults)
  {
    m_properties = new Properties(defaults);
  }
  public OutputProperties(String method)
  {
    m_properties = new Properties(
        OutputPropertiesFactory.getDefaultMethodProperties(method));
  }
  public Object clone()
  {
    try
    {
      OutputProperties cloned = (OutputProperties) super.clone();
      cloned.m_properties = (Properties) cloned.m_properties.clone();
      return cloned;
    }
    catch (CloneNotSupportedException e)
    {
      return null;
    }
  }
  public void setProperty(QName key, String value)
  {
    setProperty(key.toNamespacedString(), value);
  }
  public void setProperty(String key, String value)
  {
    if(key.equals(OutputKeys.METHOD))
    {
      setMethodDefaults(value);
    }
    if (key.startsWith(OutputPropertiesFactory.S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL))
      key = OutputPropertiesFactory.S_BUILTIN_EXTENSIONS_UNIVERSAL
         + key.substring(OutputPropertiesFactory.S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL_LEN);
    m_properties.put(key, value);
  }
  public String getProperty(QName key)
  {
    return m_properties.getProperty(key.toNamespacedString());
  }
  public String getProperty(String key) 
  {
    if (key.startsWith(OutputPropertiesFactory.S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL))
      key = OutputPropertiesFactory.S_BUILTIN_EXTENSIONS_UNIVERSAL 
        + key.substring(OutputPropertiesFactory.S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL_LEN);
    return m_properties.getProperty(key);
  }
  public void setBooleanProperty(QName key, boolean value)
  {
    m_properties.put(key.toNamespacedString(), value ? "yes" : "no");
  }
  public void setBooleanProperty(String key, boolean value)
  {
    m_properties.put(key, value ? "yes" : "no");
  }
  public boolean getBooleanProperty(QName key)
  {
    return getBooleanProperty(key.toNamespacedString());
  }
  public boolean getBooleanProperty(String key)
  {
    return OutputPropertyUtils.getBooleanProperty(key, m_properties);
  }
  public void setIntProperty(QName key, int value)
  {
    setIntProperty(key.toNamespacedString(), value);
  }
  public void setIntProperty(String key, int value)
  {
    m_properties.put(key, Integer.toString(value));
  }
  public int getIntProperty(QName key)
  {
    return getIntProperty(key.toNamespacedString());
  }
  public int getIntProperty(String key)
  {
    return OutputPropertyUtils.getIntProperty(key, m_properties);
  }
  public void setQNameProperty(QName key, QName value)
  {
    setQNameProperty(key.toNamespacedString(), value);
  }
  public void setMethodDefaults(String method)
  {
        String defaultMethod = m_properties.getProperty(OutputKeys.METHOD);
        if((null == defaultMethod) || !defaultMethod.equals(method)
         || defaultMethod.equals("xml")
         )
        {
            Properties savedProps = m_properties;
            Properties newDefaults = 
                OutputPropertiesFactory.getDefaultMethodProperties(method);
            m_properties = new Properties(newDefaults);
            copyFrom(savedProps, false);
        }
  }
  public void setQNameProperty(String key, QName value)
  {
    setProperty(key, value.toNamespacedString());
  }
  public QName getQNameProperty(QName key)
  {
    return getQNameProperty(key.toNamespacedString());
  }
  public QName getQNameProperty(String key)
  {
    return getQNameProperty(key, m_properties);
  }
  public static QName getQNameProperty(String key, Properties props)
  {
    String s = props.getProperty(key);
    if (null != s)
      return QName.getQNameFromString(s);
    else
      return null;
  }
  public void setQNameProperties(QName key, Vector v)
  {
    setQNameProperties(key.toNamespacedString(), v);
  }
  public void setQNameProperties(String key, Vector v)
  {
    int s = v.size();
    FastStringBuffer fsb = new FastStringBuffer(9,9);
    for (int i = 0; i < s; i++)
    {
      QName qname = (QName) v.elementAt(i);
      fsb.append(qname.toNamespacedString());
      if (i < s-1) 
        fsb.append(' ');
    }
    m_properties.put(key, fsb.toString());
  }
  public Vector getQNameProperties(QName key)
  {
    return getQNameProperties(key.toNamespacedString());
  }
  public Vector getQNameProperties(String key)
  {
    return getQNameProperties(key, m_properties);
  }
  public static Vector getQNameProperties(String key, Properties props)
  {
    String s = props.getProperty(key);
    if (null != s)
    {
      Vector v = new Vector();
      int l = s.length();
      boolean inCurly = false;
      FastStringBuffer buf = new FastStringBuffer();
      for (int i = 0; i < l; i++)
      {
        char c = s.charAt(i);
        if (Character.isWhitespace(c))
        {
          if (!inCurly)
          {
            if (buf.length() > 0)
            {
              QName qname = QName.getQNameFromString(buf.toString());
              v.addElement(qname);
              buf.reset();
            }
            continue;
          }
        }
        else if ('{' == c)
          inCurly = true;
        else if ('}' == c)
          inCurly = false;
        buf.append(c);
      }
      if (buf.length() > 0)
      {
        QName qname = QName.getQNameFromString(buf.toString());
        v.addElement(qname);
        buf.reset();
      }
      return v;
    }
    else
      return null;
  }
  public void recompose(StylesheetRoot root)
    throws TransformerException
  {
    root.recomposeOutput(this);
  }
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
  }
  public Properties getProperties()
  {
    return m_properties;
  }
  public void copyFrom(Properties src)
  {
    copyFrom(src, true);
  }
  public void copyFrom(Properties src, boolean shouldResetDefaults)
  {
    Enumeration keys = src.keys();
    while (keys.hasMoreElements())
    {
      String key = (String) keys.nextElement();
      if (!isLegalPropertyKey(key))
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{key})); 
      Object oldValue = m_properties.get(key);
      if (null == oldValue)
      {
        String val = (String) src.get(key);
        if(shouldResetDefaults && key.equals(OutputKeys.METHOD))
        {
          setMethodDefaults(val);
        }
        m_properties.put(key, val);
      }
      else if (key.equals(OutputKeys.CDATA_SECTION_ELEMENTS))
      {
        m_properties.put(key, (String) oldValue + " " + (String) src.get(key));
      }
    }
  }
  public void copyFrom(OutputProperties opsrc)
    throws TransformerException
  {
    copyFrom(opsrc.getProperties());
  }
  public static boolean isLegalPropertyKey(String key)
  {
    return (key.equals(OutputKeys.CDATA_SECTION_ELEMENTS)
            || key.equals(OutputKeys.DOCTYPE_PUBLIC)
            || key.equals(OutputKeys.DOCTYPE_SYSTEM)
            || key.equals(OutputKeys.ENCODING)
            || key.equals(OutputKeys.INDENT)
            || key.equals(OutputKeys.MEDIA_TYPE)
            || key.equals(OutputKeys.METHOD)
            || key.equals(OutputKeys.OMIT_XML_DECLARATION)
            || key.equals(OutputKeys.STANDALONE)
            || key.equals(OutputKeys.VERSION)
            || (key.length() > 0) 
                  && (key.charAt(0) == '{') 
                  && (key.lastIndexOf('{') == 0)
                  && (key.indexOf('}') > 0)
                  && (key.lastIndexOf('}') == key.indexOf('}')));
  }
  private Properties m_properties = null;
    static public Properties getDefaultMethodProperties(String method)
    {
        return org.apache.xml.serializer.OutputPropertiesFactory.getDefaultMethodProperties(method);
    }
}
