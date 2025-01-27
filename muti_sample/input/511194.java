public class XSLTElementDef
{
  XSLTElementDef(){}
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject)
  {
    build(namespace, name, nameAlias, elements, attributes, contentHandler,
          classObject);
    if ( (null != namespace)
    &&  (namespace.equals(Constants.S_XSLNAMESPACEURL)
        || namespace.equals(Constants.S_BUILTIN_EXTENSIONS_URL)
        || namespace.equals(Constants.S_BUILTIN_OLD_EXTENSIONS_URL)))
    {
      schema.addAvailableElement(new QName(namespace, name));
      if(null != nameAlias)
        schema.addAvailableElement(new QName(namespace, nameAlias));
    } 
  }
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject, boolean has_required)
  {
		this.m_has_required = has_required;
    build(namespace, name, nameAlias, elements, attributes, contentHandler,
          classObject);
    if ( (null != namespace)
    &&  (namespace.equals(Constants.S_XSLNAMESPACEURL)
        || namespace.equals(Constants.S_BUILTIN_EXTENSIONS_URL)
        || namespace.equals(Constants.S_BUILTIN_OLD_EXTENSIONS_URL)))
    {
      schema.addAvailableElement(new QName(namespace, name));
      if(null != nameAlias)
        schema.addAvailableElement(new QName(namespace, nameAlias));
    } 
  }
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject, 
								 boolean has_required, boolean required)
  {
    this(schema, namespace, name,  nameAlias,
                 elements, attributes,
                 contentHandler, classObject, has_required);
		this.m_required = required;
  }
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject, 
								 boolean has_required, boolean required, int order, 
								 boolean multiAllowed)
  {
		this(schema, namespace, name,  nameAlias,
                 elements, attributes,
                 contentHandler, classObject, has_required, required);    
		this.m_order = order;
		this.m_multiAllowed = multiAllowed;
  }
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject, 
								 boolean has_required, boolean required, boolean has_order, int order, 
								 boolean multiAllowed)
  {
		this(schema, namespace, name,  nameAlias,
                 elements, attributes,
                 contentHandler, classObject, has_required, required);    
		this.m_order = order;
		this.m_multiAllowed = multiAllowed;
    this.m_isOrdered = has_order;		
  }
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject, 
								 boolean has_order, int order, boolean multiAllowed)
  {
    this(schema, namespace, name,  nameAlias,
                 elements, attributes,
                 contentHandler, classObject, 
								 order, multiAllowed);
		this.m_isOrdered = has_order;		
  }
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject, 
								 int order, boolean multiAllowed)
  {
    this(schema, namespace, name, nameAlias, elements, attributes, contentHandler,
          classObject);
    this.m_order = order;
		this.m_multiAllowed = multiAllowed;
  }
  XSLTElementDef(Class classObject, XSLTElementProcessor contentHandler,
                 int type)
  {
    this.m_classObject = classObject;
    this.m_type = type;
    setElementProcessor(contentHandler);
  }
  void build(String namespace, String name, String nameAlias,
             XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
             XSLTElementProcessor contentHandler, Class classObject)
  {
    this.m_namespace = namespace;
    this.m_name = name;
    this.m_nameAlias = nameAlias;
    this.m_elements = elements;
    this.m_attributes = attributes;
    setElementProcessor(contentHandler);
    this.m_classObject = classObject;
		if (hasRequired() && m_elements != null)
		{
			int n = m_elements.length;
			for (int i = 0; i < n; i++)
			{
				XSLTElementDef def = m_elements[i];
				if (def != null && def.getRequired())
				{
					if (m_requiredFound == null)			
						m_requiredFound = new Hashtable();
					m_requiredFound.put(def.getName(), "xsl:" +def.getName()); 
				}
			}
		}
  }
  private static boolean equalsMayBeNull(Object obj1, Object obj2)
  {
    return (obj2 == obj1)
           || ((null != obj1) && (null != obj2) && obj2.equals(obj1));
  }
  private static boolean equalsMayBeNullOrZeroLen(String s1, String s2)
  {
    int len1 = (s1 == null) ? 0 : s1.length();
    int len2 = (s2 == null) ? 0 : s2.length();
    return (len1 != len2) ? false 
						 : (len1 == 0) ? true 
								 : s1.equals(s2);
  }
  static final int T_ELEMENT = 1, T_PCDATA = 2, T_ANY = 3;
  private int m_type = T_ELEMENT;
  int getType()
  {
    return m_type;
  }
  void setType(int t)
  {
    m_type = t;
  }
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
  private String m_nameAlias;
  String getNameAlias()
  {
    return m_nameAlias;
  }
  private XSLTElementDef[] m_elements;
  public XSLTElementDef[] getElements()
  {
    return m_elements;
  }
  void setElements(XSLTElementDef[] defs)
  {
    m_elements = defs;
  }
  private boolean QNameEquals(String uri, String localName)
  {
    return (equalsMayBeNullOrZeroLen(m_namespace, uri)
            && (equalsMayBeNullOrZeroLen(m_name, localName)
                || equalsMayBeNullOrZeroLen(m_nameAlias, localName)));
  }
  XSLTElementProcessor getProcessorFor(String uri, String localName) 
	{
    XSLTElementProcessor elemDef = null;  
    if (null == m_elements)
      return null;
    int n = m_elements.length;
    int order = -1;
		boolean multiAllowed = true;
    for (int i = 0; i < n; i++)
    {
      XSLTElementDef def = m_elements[i];
      if (def.m_name.equals("*"))
      {
        if (!equalsMayBeNullOrZeroLen(uri, Constants.S_XSLNAMESPACEURL))
				{
          elemDef = def.m_elementProcessor;
				  order = def.getOrder();
					multiAllowed = def.getMultiAllowed();
				}
      }
			else if (def.QNameEquals(uri, localName))
			{	
				if (def.getRequired())
					this.setRequiredFound(def.getName(), true);
				order = def.getOrder();
				multiAllowed = def.getMultiAllowed();
				elemDef = def.m_elementProcessor;
				break;
			}
		}		
		if (elemDef != null && this.isOrdered())
		{			
			int lastOrder = getLastOrder();
			if (order > lastOrder)
				setLastOrder(order);
			else if (order == lastOrder && !multiAllowed)
			{
				return null;
			}
			else if (order < lastOrder && order > 0)
			{
				return null;
			}
		}
    return elemDef;
  }
  XSLTElementProcessor getProcessorForUnknown(String uri, String localName)
  {
    if (null == m_elements)
      return null;
    int n = m_elements.length;
    for (int i = 0; i < n; i++)
    {
      XSLTElementDef def = m_elements[i];
      if (def.m_name.equals("unknown") && uri.length() > 0)
      {
        return def.m_elementProcessor;
      }
    }
    return null;
  }
  private XSLTAttributeDef[] m_attributes;
  XSLTAttributeDef[] getAttributes()
  {
    return m_attributes;
  }
  XSLTAttributeDef getAttributeDef(String uri, String localName)
  {
    XSLTAttributeDef defaultDef = null;
    XSLTAttributeDef[] attrDefs = getAttributes();
    int nAttrDefs = attrDefs.length;
    for (int k = 0; k < nAttrDefs; k++)
    {
      XSLTAttributeDef attrDef = attrDefs[k];
      String uriDef = attrDef.getNamespace();
      String nameDef = attrDef.getName();
      if (nameDef.equals("*") && (equalsMayBeNullOrZeroLen(uri, uriDef) || 
          (uriDef != null && uriDef.equals("*") && uri!=null && uri.length() > 0 )))
      {
        return attrDef;
      }
      else if (nameDef.equals("*") && (uriDef == null))
      {
        defaultDef = attrDef;
      }
      else if (equalsMayBeNullOrZeroLen(uri, uriDef)
               && localName.equals(nameDef))
      {
        return attrDef;
      }
    }
    if (null == defaultDef)
    {
      if (uri.length() > 0 && !equalsMayBeNullOrZeroLen(uri, Constants.S_XSLNAMESPACEURL))
      {
        return XSLTAttributeDef.m_foreignAttr;
      }
    }
    return defaultDef;
  }
  private XSLTElementProcessor m_elementProcessor;
  public XSLTElementProcessor getElementProcessor()
  {
    return m_elementProcessor;
  }
  public void setElementProcessor(XSLTElementProcessor handler)
  {
    if (handler != null)
    {
      m_elementProcessor = handler;
      m_elementProcessor.setElemDef(this);
    }
  }
  private Class m_classObject;
  Class getClassObject()
  {
    return m_classObject;
  }
  private boolean m_has_required = false;
  boolean hasRequired()
  {
    return m_has_required;
  }
  private boolean m_required = false;
  boolean getRequired()
  {
    return m_required;
  }
	Hashtable m_requiredFound;
  void setRequiredFound(String elem, boolean found)
  {
   if (m_requiredFound.get(elem) != null) 
		 m_requiredFound.remove(elem);
  }
  boolean getRequiredFound()
  {
		if (m_requiredFound == null)
			return true;
    return m_requiredFound.isEmpty();
  }
  String getRequiredElem()
  {
		if (m_requiredFound == null)
			return null;
		Enumeration elems = m_requiredFound.elements();
		String s = "";
		boolean first = true;
		while (elems.hasMoreElements())
		{
			if (first)
				first = false;
			else
			 s = s + ", ";
			s = s + (String)elems.nextElement();
		}
    return s;
  }
	boolean m_isOrdered = false;	
  boolean isOrdered()
  {
			return m_isOrdered;
  }
  private int m_order = -1;
  int getOrder()
  {
    return m_order;
  }
  private int m_lastOrder = -1;
  int getLastOrder()
  {
    return m_lastOrder;
  }
  void setLastOrder(int order)
  {
    m_lastOrder = order ;
  }
  private boolean m_multiAllowed = true;
  boolean getMultiAllowed()
  {
    return m_multiAllowed;
  }
}
