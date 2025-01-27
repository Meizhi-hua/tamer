public class XSLTAttributeDef
{
   static final int FATAL = 0;
   static final int ERROR = 1;
   static final int WARNING = 2;
  XSLTAttributeDef(String namespace, String name, int type, boolean required, boolean supportsAVT, int errorType)
  {
    this.m_namespace = namespace;
    this.m_name = name;
    this.m_type = type;
    this.m_required = required;
    this.m_supportsAVT = supportsAVT;
    this.m_errorType = errorType;
  }
  XSLTAttributeDef(String namespace, String name, int type, boolean supportsAVT, int errorType, String defaultVal)
  {
    this.m_namespace = namespace;
    this.m_name = name;
    this.m_type = type;
    this.m_required = false;
    this.m_supportsAVT = supportsAVT;  
    this.m_errorType = errorType;      
    this.m_default = defaultVal;
   }
  XSLTAttributeDef(String namespace, String name, boolean required, boolean supportsAVT, 
                    boolean prefixedQNameValAllowed, int errorType, String k1, int v1, String k2, int v2)
  {
    this.m_namespace = namespace;
    this.m_name = name;
	this.m_type = prefixedQNameValAllowed ? this.T_ENUM_OR_PQNAME : this.T_ENUM;    
    this.m_required = required;
    this.m_supportsAVT = supportsAVT;    
    this.m_errorType = errorType;    
    m_enums = new StringToIntTable(2);
    m_enums.put(k1, v1);
    m_enums.put(k2, v2);
  }
  XSLTAttributeDef(String namespace, String name, boolean required, boolean supportsAVT,
                    boolean prefixedQNameValAllowed, int errorType, String k1, int v1, String k2, int v2, String k3, int v3)
  {
    this.m_namespace = namespace;
    this.m_name = name;
	this.m_type = prefixedQNameValAllowed ? this.T_ENUM_OR_PQNAME : this.T_ENUM;    
    this.m_required = required;
    this.m_supportsAVT = supportsAVT; 
    this.m_errorType = errorType;      
    m_enums = new StringToIntTable(3);
    m_enums.put(k1, v1);
    m_enums.put(k2, v2);
    m_enums.put(k3, v3);
  }
  XSLTAttributeDef(String namespace, String name, boolean required, boolean supportsAVT,
                   boolean prefixedQNameValAllowed, int errorType, String k1, int v1, String k2, int v2, 
                   String k3, int v3, String k4, int v4)
  {
    this.m_namespace = namespace;
    this.m_name = name;
	this.m_type = prefixedQNameValAllowed ? this.T_ENUM_OR_PQNAME : this.T_ENUM;    
    this.m_required = required;
    this.m_supportsAVT = supportsAVT;      
    this.m_errorType = errorType; 
    m_enums = new StringToIntTable(4);
    m_enums.put(k1, v1);
    m_enums.put(k2, v2);
    m_enums.put(k3, v3);
    m_enums.put(k4, v4);
  }
  static final int T_CDATA = 1,
  T_URL = 2,
  T_AVT = 3,  
  T_PATTERN = 4,
  T_EXPR = 5,
  T_CHAR = 6,
  T_NUMBER = 7,
  T_YESNO = 8,
  T_QNAME = 9,
  T_QNAMES = 10,
  T_ENUM = 11,
  T_SIMPLEPATTERNLIST = 12,
  T_NMTOKEN = 13,
  T_STRINGLIST = 14,
  T_PREFIX_URLLIST = 15,
  T_ENUM_OR_PQNAME = 16,
  T_NCNAME = 17,
  T_AVT_QNAME = 18,
  T_QNAMES_RESOLVE_NULL = 19,
  T_PREFIXLIST = 20;
  static final XSLTAttributeDef m_foreignAttr = new XSLTAttributeDef("*", "*",
                                            XSLTAttributeDef.T_CDATA,false, false, WARNING);
  static final String S_FOREIGNATTR_SETTER = "setForeignAttr";
  private String m_namespace;
  String getNamespace()
  {
    return m_namespace;
  }
  private String m_name;
  String getName()
  {
    return m_name;
  }
  private int m_type;
  int getType()
  {
    return m_type;
  }
  private StringToIntTable m_enums;
  private int getEnum(String key)
  {
    return m_enums.get(key);
  }
  private String[] getEnumNames()
  {
    return m_enums.keys();
  }
  private String m_default;
  String getDefault()
  {
    return m_default;
  }
  void setDefault(String def)
  {
    m_default = def;
  }
  private boolean m_required;
  boolean getRequired()
  {
    return m_required;
  }
  private boolean m_supportsAVT;
  boolean getSupportsAVT()
  {
    return m_supportsAVT;
  }
  int m_errorType = this.WARNING;
  int getErrorType()
  {
    return m_errorType;
  }
  String m_setterString = null;
  public String getSetterMethodName()
  {
    if (null == m_setterString)
    {
      if (m_foreignAttr == this)
      {
        return S_FOREIGNATTR_SETTER;
      }
      else if (m_name.equals("*"))
      {
        m_setterString = "addLiteralResultAttribute";
        return m_setterString;
      }
      StringBuffer outBuf = new StringBuffer();
      outBuf.append("set");
      if ((m_namespace != null)
              && m_namespace.equals(Constants.S_XMLNAMESPACEURI))
      {
        outBuf.append("Xml");
      }
      int n = m_name.length();
      for (int i = 0; i < n; i++)
      {
        char c = m_name.charAt(i);
        if ('-' == c)
        {
          i++;
          c = m_name.charAt(i);
          c = Character.toUpperCase(c);
        }
        else if (0 == i)
        {
          c = Character.toUpperCase(c);
        }
        outBuf.append(c);
      }
      m_setterString = outBuf.toString();
    }
    return m_setterString;
  }
  AVT processAVT(
          StylesheetHandler handler, String uri, String name, String rawName, String value,
          ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {
    try
    {
      AVT avt = new AVT(handler, uri, name, rawName, value, owner);
      return avt;
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
  Object processCDATA(StylesheetHandler handler, String uri, String name,
                      String rawName, String value, ElemTemplateElement owner)
                      throws org.xml.sax.SAXException
  {
  	if (getSupportsAVT()) {
	    try
	    {
	      AVT avt = new AVT(handler, uri, name, rawName, value, owner);
	      return avt;
	    }
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    }  		
  	} else {  	  	
	    return value;
  	}
  }
  Object processCHAR(
          StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {
	if (getSupportsAVT()) {
	    try
	    {
	      AVT avt = new AVT(handler, uri, name, rawName, value, owner);
		  if ((avt.isSimple()) && (value.length() != 1)) {
		  	handleError(handler, XSLTErrorResources.INVALID_TCHAR, new Object[] {name, value},null);
            return null;
		  }	
	      return avt;
	    }
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    }
	} else {    
	    if (value.length() != 1)
	    {
            handleError(handler, XSLTErrorResources.INVALID_TCHAR, new Object[] {name, value},null);
            return null;
	    }
	    return new Character(value.charAt(0));
	}
  }
  Object processENUM(StylesheetHandler handler, String uri, String name,
                     String rawName, String value, ElemTemplateElement owner)
                     throws org.xml.sax.SAXException
  {
	AVT avt = null;
	if (getSupportsAVT()) {
	    try
	    {
	      avt = new AVT(handler, uri, name, rawName, value, owner);
	      if (!avt.isSimple()) return avt;
	    }
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    }
	}    
    int retVal = this.getEnum(value);
	if (retVal == StringToIntTable.INVALID_KEY) 
    {
       StringBuffer enumNamesList = getListOfEnums();
       handleError(handler, XSLTErrorResources.INVALID_ENUM,new Object[]{name, value, enumNamesList.toString() },null);
       return null;
    }
	if (getSupportsAVT()) return avt;
	else return new Integer(retVal);	
  }
  Object processENUM_OR_PQNAME(StylesheetHandler handler, String uri, String name,
                     String rawName, String value, ElemTemplateElement owner)
                     throws org.xml.sax.SAXException
  {
	Object objToReturn = null;
	if (getSupportsAVT()) {
	    try
	    {
	      AVT avt = new AVT(handler, uri, name, rawName, value, owner);
	      if (!avt.isSimple()) return avt;
	      else objToReturn = avt;
	    }  
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    }
	}    
  	int key = this.getEnum(value);
    if (key != StringToIntTable.INVALID_KEY) 
    {
        if (objToReturn == null) objToReturn = new Integer(key);
    }
    else
    {
        try 
        {
			QName qname = new QName(value, handler, true);
            if (objToReturn == null) objToReturn = qname;	
			if (qname.getPrefix() == null) {
	           StringBuffer enumNamesList = getListOfEnums();
 	           enumNamesList.append(" <qname-but-not-ncname>");
               handleError(handler,XSLTErrorResources.INVALID_ENUM,new Object[]{name, value, enumNamesList.toString() },null); 
               return null;
	        }            
        }
        catch (IllegalArgumentException ie) 
        {
           StringBuffer enumNamesList = getListOfEnums();
           enumNamesList.append(" <qname-but-not-ncname>");
           handleError(handler,XSLTErrorResources.INVALID_ENUM,new Object[]{name, value, enumNamesList.toString() },ie); 
           return null;
        }
        catch (RuntimeException re)
        {
           StringBuffer enumNamesList = getListOfEnums();
           enumNamesList.append(" <qname-but-not-ncname>");
           handleError(handler,XSLTErrorResources.INVALID_ENUM,new Object[]{name, value, enumNamesList.toString() },re); 
           return null;
        }    
  	}
  	return objToReturn;
  }
  Object processEXPR(
          StylesheetHandler handler, String uri, String name, String rawName, String value,
          ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {
    try
    {
      XPath expr = handler.createXPath(value, owner);
      return expr;
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
  Object processNMTOKEN(StylesheetHandler handler, String uri, String name,
                        String rawName, String value, ElemTemplateElement owner)
             throws org.xml.sax.SAXException
  {
  	if (getSupportsAVT()) {
	    try
	    {
	      AVT avt = new AVT(handler, uri, name, rawName, value, owner);
		  if ((avt.isSimple()) && (!XML11Char.isXML11ValidNmtoken(value))) {
            handleError(handler,XSLTErrorResources.INVALID_NMTOKEN, new Object[] {name,value},null);
            return null;
		  }	
	      return avt;
	    }
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    }  		
  	} else {
  		if (!XML11Char.isXML11ValidNmtoken(value)) {
            handleError(handler,XSLTErrorResources.INVALID_NMTOKEN, new Object[] {name,value},null);
            return null;
  		}
  	}	  			
    return value;
  }
  Object processPATTERN(
          StylesheetHandler handler, String uri, String name, String rawName, String value,
          ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {
    try
    {
      XPath pattern = handler.createMatchPatternXPath(value, owner);
      return pattern;
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
  Object processNUMBER(
          StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {
	if (getSupportsAVT()) 
	{
		Double val;
		AVT avt = null;
	    try
	    {
	      avt = new AVT(handler, uri, name, rawName, value, owner);
	      if (avt.isSimple()) 
	      {
	      	val = Double.valueOf(value);
	      }
	    }
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    } 
	    catch (NumberFormatException nfe)
	    {
	     	handleError(handler,XSLTErrorResources.INVALID_NUMBER, new Object[] {name, value}, nfe);
            return null;
	    }
	    return avt;
	} 
	else
    {
	    try
	    {
	      return Double.valueOf(value);
	    }
	    catch (NumberFormatException nfe)
	    {
            handleError(handler,XSLTErrorResources.INVALID_NUMBER, new Object[] {name, value}, nfe);
            return null;
	    }
    }    
  }
  Object processQNAME(
          StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {
     try 
        {	
   	      QName qname = new QName(value, handler, true);
          return qname;
        }
        catch (IllegalArgumentException ie)
        {
            handleError(handler,XSLTErrorResources.INVALID_QNAME, new Object[] {name, value},ie);
            return null;
        }
        catch (RuntimeException re) {
            handleError(handler,XSLTErrorResources.INVALID_QNAME, new Object[] {name, value},re);
            return null;
        }
  	}
  Object processAVT_QNAME(
          StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {
       AVT avt = null;
       try
       {
          avt = new AVT(handler, uri, name, rawName, value, owner);
          if (avt.isSimple())
          {
             int indexOfNSSep = value.indexOf(':');
             if (indexOfNSSep >= 0) 
             {   
                  String prefix = value.substring(0, indexOfNSSep);
                  if (!XML11Char.isXML11ValidNCName(prefix))
                  {
                     handleError(handler,XSLTErrorResources.INVALID_QNAME,new Object[]{name,value },null);
                     return null;
                  }
             }
             String localName =  (indexOfNSSep < 0)
                 ? value : value.substring(indexOfNSSep + 1); 
             if ((localName == null) || (localName.length() == 0) ||
                 (!XML11Char.isXML11ValidNCName(localName)))
             {    
                     handleError(handler,XSLTErrorResources.INVALID_QNAME,new Object[]{name,value },null );
                     return null;
             }
          }  
        }
        catch (TransformerException te)
        {
          throw new org.xml.sax.SAXException(te);
        } 
    return avt;
 }
  Object processNCNAME(
          StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {
    if (getSupportsAVT()) 
    {
        AVT avt = null;
        try
        {
          avt = new AVT(handler, uri, name, rawName, value, owner);
          if ((avt.isSimple()) &&  (!XML11Char.isXML11ValidNCName(value))) 
          {
             handleError(handler,XSLTErrorResources.INVALID_NCNAME,new Object[] {name,value},null);
             return null;
          }      
          return avt;
        }
        catch (TransformerException te)
        {
          throw new org.xml.sax.SAXException(te);
        } 
    } else {
        if (!XML11Char.isXML11ValidNCName(value)) 
        {
            handleError(handler,XSLTErrorResources.INVALID_NCNAME,new Object[] {name,value},null);
            return null;
        }
        return value;
    }
 }
  Vector processQNAMES(
          StylesheetHandler handler, String uri, String name, String rawName, String value)
            throws org.xml.sax.SAXException
  {
    StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
    int nQNames = tokenizer.countTokens();
    Vector qnames = new Vector(nQNames);
    for (int i = 0; i < nQNames; i++)
    {
      qnames.addElement(new QName(tokenizer.nextToken(), handler));
    }
    return qnames;
  }
  final Vector processQNAMESRNU(StylesheetHandler handler, String uri,
    String name, String rawName, String value)
    throws org.xml.sax.SAXException
  {
    StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
    int nQNames = tokenizer.countTokens();
    Vector qnames = new Vector(nQNames);
    String defaultURI = handler.getNamespaceForPrefix("");
    for (int i = 0; i < nQNames; i++)
    {
      String tok = tokenizer.nextToken();
      if (tok.indexOf(':') == -1) {
        qnames.addElement(new QName(defaultURI,tok));
      } else {
        qnames.addElement(new QName(tok, handler));
      }
    }
    return qnames;
  }
  Vector processSIMPLEPATTERNLIST(
          StylesheetHandler handler, String uri, String name, String rawName, String value,
          ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {
    try
    {
      StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
      int nPatterns = tokenizer.countTokens();
      Vector patterns = new Vector(nPatterns);
      for (int i = 0; i < nPatterns; i++)
      {
        XPath pattern =
          handler.createMatchPatternXPath(tokenizer.nextToken(), owner);
        patterns.addElement(pattern);
      }
      return patterns;
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
  StringVector processSTRINGLIST(StylesheetHandler handler, String uri,
                                 String name, String rawName, String value)
  {
    StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
    int nStrings = tokenizer.countTokens();
    StringVector strings = new StringVector(nStrings);
    for (int i = 0; i < nStrings; i++)
    {
      strings.addElement(tokenizer.nextToken());
    }
    return strings;
  }
  StringVector processPREFIX_URLLIST(
          StylesheetHandler handler, String uri, String name, String rawName, String value)
            throws org.xml.sax.SAXException
  {
    StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
    int nStrings = tokenizer.countTokens();
    StringVector strings = new StringVector(nStrings);
    for (int i = 0; i < nStrings; i++)
    {
      String prefix = tokenizer.nextToken();
      String url = handler.getNamespaceForPrefix(prefix);
      if (url != null)
        strings.addElement(url);
      else
        throw new org.xml.sax.SAXException(XSLMessages.createMessage(XSLTErrorResources.ER_CANT_RESOLVE_NSPREFIX, new Object[] {prefix}));
    }
    return strings;
  }
   StringVector processPREFIX_LIST(
           StylesheetHandler handler, String uri, String name, 
           String rawName, String value) throws org.xml.sax.SAXException
   {
     StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
     int nStrings = tokenizer.countTokens();
     StringVector strings = new StringVector(nStrings);
     for (int i = 0; i < nStrings; i++)
     {
       String prefix = tokenizer.nextToken();
       String url = handler.getNamespaceForPrefix(prefix);
       if (prefix.equals(Constants.ATTRVAL_DEFAULT_PREFIX) || url != null)
         strings.addElement(prefix);
       else
         throw new org.xml.sax.SAXException(
              XSLMessages.createMessage(
                   XSLTErrorResources.ER_CANT_RESOLVE_NSPREFIX, 
                   new Object[] {prefix}));
     }
     return strings;
   }
  Object processURL(
          StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {
    if (getSupportsAVT()) {
	    try
	    {
	      AVT avt = new AVT(handler, uri, name, rawName, value, owner);
	      return avt;
	    }
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    }  		
     } else {
	    return value;
    }
  }
  private Boolean processYESNO(
          StylesheetHandler handler, String uri, String name, String rawName, String value)
            throws org.xml.sax.SAXException
  {
    if (!(value.equals("yes") || value.equals("no")))
    {
      handleError(handler, XSLTErrorResources.INVALID_BOOLEAN, new Object[] {name,value}, null);
      return null;
   }
     return new Boolean(value.equals("yes") ? true : false);
  }
  Object processValue(
          StylesheetHandler handler, String uri, String name, String rawName, String value,
          ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {
    int type = getType();
    Object processedValue = null;
    switch (type)
    {
    case T_AVT :
      processedValue = processAVT(handler, uri, name, rawName, value, owner);
      break;
    case T_CDATA :
      processedValue = processCDATA(handler, uri, name, rawName, value, owner);
      break;
    case T_CHAR :
      processedValue = processCHAR(handler, uri, name, rawName, value, owner);
      break;
    case T_ENUM :
      processedValue = processENUM(handler, uri, name, rawName, value, owner);
      break;
    case T_EXPR :
      processedValue = processEXPR(handler, uri, name, rawName, value, owner);
      break;
    case T_NMTOKEN :
      processedValue = processNMTOKEN(handler, uri, name, rawName, value, owner);
      break;
    case T_PATTERN :
      processedValue = processPATTERN(handler, uri, name, rawName, value, owner);
      break;
    case T_NUMBER :
      processedValue = processNUMBER(handler, uri, name, rawName, value, owner);
      break;
    case T_QNAME :
      processedValue = processQNAME(handler, uri, name, rawName, value, owner);
      break;
    case T_QNAMES :
      processedValue = processQNAMES(handler, uri, name, rawName, value);
      break;
	case T_QNAMES_RESOLVE_NULL:
      processedValue = processQNAMESRNU(handler, uri, name, rawName, value);
      break;
    case T_SIMPLEPATTERNLIST :
      processedValue = processSIMPLEPATTERNLIST(handler, uri, name, rawName,
                                                value, owner);
      break;
    case T_URL :
      processedValue = processURL(handler, uri, name, rawName, value, owner);
      break;
    case T_YESNO :
      processedValue = processYESNO(handler, uri, name, rawName, value);
      break;
    case T_STRINGLIST :
      processedValue = processSTRINGLIST(handler, uri, name, rawName, value);
      break;
    case T_PREFIX_URLLIST :
      processedValue = processPREFIX_URLLIST(handler, uri, name, rawName,
                                             value);
      break;
    case T_ENUM_OR_PQNAME :
    	processedValue = processENUM_OR_PQNAME(handler, uri, name, rawName, value, owner);
    	break;
    case T_NCNAME :
        processedValue = processNCNAME(handler, uri, name, rawName, value, owner);
        break;
    case T_AVT_QNAME :
        processedValue = processAVT_QNAME(handler, uri, name, rawName, value, owner);
        break;
    case T_PREFIXLIST :
      processedValue = processPREFIX_LIST(handler, uri, name, rawName,
                                             value);
      break;
    default :
    }
    return processedValue;
  }
  void setDefAttrValue(StylesheetHandler handler, ElemTemplateElement elem)
          throws org.xml.sax.SAXException
  {
    setAttrValue(handler, this.getNamespace(), this.getName(),
                 this.getName(), this.getDefault(), elem);
  }
  private Class getPrimativeClass(Object obj)
  {
    if (obj instanceof XPath)
      return XPath.class;
    Class cl = obj.getClass();
    if (cl == Double.class)
    {
      cl = double.class;
    }
    if (cl == Float.class)
    {
      cl = float.class;
    }
    else if (cl == Boolean.class)
    {
      cl = boolean.class;
    }
    else if (cl == Byte.class)
    {
      cl = byte.class;
    }
    else if (cl == Character.class)
    {
      cl = char.class;
    }
    else if (cl == Short.class)
    {
      cl = short.class;
    }
    else if (cl == Integer.class)
    {
      cl = int.class;
    }
    else if (cl == Long.class)
    {
      cl = long.class;
    }
    return cl;
  }
  private StringBuffer getListOfEnums() 
  {
     StringBuffer enumNamesList = new StringBuffer();            
     String [] enumValues = this.getEnumNames();
     for (int i = 0; i < enumValues.length; i++)
     {
        if (i > 0)
        {
           enumNamesList.append(' ');
        }
        enumNamesList.append(enumValues[i]);
    }        
    return enumNamesList;
  }
  boolean setAttrValue(
          StylesheetHandler handler, String attrUri, String attrLocalName, 
          String attrRawName, String attrValue, ElemTemplateElement elem)
            throws org.xml.sax.SAXException
  {
    if(attrRawName.equals("xmlns") || attrRawName.startsWith("xmlns:"))
      return true;
    String setterString = getSetterMethodName();
    if (null != setterString)
    {
      try
      {
        Method meth;
        Object[] args;
        if(setterString.equals(S_FOREIGNATTR_SETTER))
        {
          if( attrUri==null) attrUri="";
          Class sclass = attrUri.getClass();
          Class[] argTypes = new Class[]{ sclass, sclass,
                                      sclass, sclass };
          meth = elem.getClass().getMethod(setterString, argTypes);
          args = new Object[]{ attrUri, attrLocalName,
                                      attrRawName, attrValue };
        }
        else
        {
          Object value = processValue(handler, attrUri, attrLocalName,
                                      attrRawName, attrValue, elem);
          if (null == value) return false;
          Class[] argTypes = new Class[]{ getPrimativeClass(value) };
          try
          {
            meth = elem.getClass().getMethod(setterString, argTypes);
          }
          catch (NoSuchMethodException nsme)
          {
            Class cl = ((Object) value).getClass();
            argTypes[0] = cl;
            meth = elem.getClass().getMethod(setterString, argTypes);
          }
          args = new Object[]{ value };
        }
        meth.invoke(elem, args);
      }
      catch (NoSuchMethodException nsme)
      {
        if (!setterString.equals(S_FOREIGNATTR_SETTER)) 
        {
          handler.error(XSLTErrorResources.ER_FAILED_CALLING_METHOD, new Object[]{setterString}, nsme);
          return false;
        }
      }
      catch (IllegalAccessException iae)
      {
        handler.error(XSLTErrorResources.ER_FAILED_CALLING_METHOD, new Object[]{setterString}, iae);
        return false;
      }
      catch (InvocationTargetException nsme)
      {
        handleError(handler, XSLTErrorResources.WG_ILLEGAL_ATTRIBUTE_VALUE,
            new Object[]{ Constants.ATTRNAME_NAME, getName()}, nsme);
        return false;
      }
    }
    return true;
  }
  private void handleError(StylesheetHandler handler, String msg, Object [] args, Exception exc) throws org.xml.sax.SAXException
  {
    switch (getErrorType()) 
    {
        case (FATAL):
        case (ERROR):
                handler.error(msg, args, exc);          
                break;
        case (WARNING):
                handler.warn(msg, args);       
        default: break;
    }
  }
}
