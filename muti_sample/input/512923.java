public class XSLTElementProcessor extends ElemTemplateElement
{
    static final long serialVersionUID = 5597421564955304421L;
  XSLTElementProcessor(){}
	private IntStack m_savedLastOrder;
  private XSLTElementDef m_elemDef;
  XSLTElementDef getElemDef()
  {
    return m_elemDef;
  }
  void setElemDef(XSLTElementDef def)
  {
    m_elemDef = def;
  }
  public InputSource resolveEntity(
          StylesheetHandler handler, String publicId, String systemId)
            throws org.xml.sax.SAXException
  {
    return null;
  }
  public void notationDecl(StylesheetHandler handler, String name,
                           String publicId, String systemId)
  {
  }
  public void unparsedEntityDecl(StylesheetHandler handler, String name,
                                 String publicId, String systemId,
                                 String notationName)
  {
  }
  public void startNonText(StylesheetHandler handler) throws org.xml.sax.SAXException
  {
  }
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    if (m_savedLastOrder == null)
				m_savedLastOrder = new IntStack();
			m_savedLastOrder.push(getElemDef().getLastOrder());
			getElemDef().setLastOrder(-1);
  }
  public void endElement(
          StylesheetHandler handler, String uri, String localName, String rawName)
            throws org.xml.sax.SAXException
  {
		if (m_savedLastOrder != null && !m_savedLastOrder.empty())
			getElemDef().setLastOrder(m_savedLastOrder.pop());
		if (!getElemDef().getRequiredFound())
			handler.error(XSLTErrorResources.ER_REQUIRED_ELEM_NOT_FOUND, new Object[]{getElemDef().getRequiredElem()}, null);
  }
  public void characters(
          StylesheetHandler handler, char ch[], int start, int length)
            throws org.xml.sax.SAXException
  {
    handler.error(XSLTErrorResources.ER_CHARS_NOT_ALLOWED, null, null);
  }
  public void ignorableWhitespace(
          StylesheetHandler handler, char ch[], int start, int length)
            throws org.xml.sax.SAXException
  {
  }
  public void processingInstruction(
          StylesheetHandler handler, String target, String data)
            throws org.xml.sax.SAXException
  {
  }
  public void skippedEntity(StylesheetHandler handler, String name)
          throws org.xml.sax.SAXException
  {
  }
  void setPropertiesFromAttributes(
          StylesheetHandler handler, String rawName, Attributes attributes, 
          ElemTemplateElement target)
            throws org.xml.sax.SAXException
  {
    setPropertiesFromAttributes(handler, rawName, attributes, target, true);
  }
  Attributes setPropertiesFromAttributes(
          StylesheetHandler handler, String rawName, Attributes attributes, 
          ElemTemplateElement target, boolean throwError)
            throws org.xml.sax.SAXException
  {
    XSLTElementDef def = getElemDef();
    AttributesImpl undefines = null;
    boolean isCompatibleMode = ((null != handler.getStylesheet() 
                                 && handler.getStylesheet().getCompatibleMode())
                                || !throwError);
    if (isCompatibleMode)
      undefines = new AttributesImpl();
    List processedDefs = new ArrayList();
    List errorDefs = new ArrayList();    
    int nAttrs = attributes.getLength();
    for (int i = 0; i < nAttrs; i++)
    {
      String attrUri = attributes.getURI(i);
      if((null != attrUri) && (attrUri.length() == 0)
                           && (attributes.getQName(i).startsWith("xmlns:") || 
                               attributes.getQName(i).equals("xmlns")))
      {
        attrUri = org.apache.xalan.templates.Constants.S_XMLNAMESPACEURI;
      }
      String attrLocalName = attributes.getLocalName(i);
      XSLTAttributeDef attrDef = def.getAttributeDef(attrUri, attrLocalName);
      if (null == attrDef)
      {
        if (!isCompatibleMode)
        {
          handler.error(XSLTErrorResources.ER_ATTR_NOT_ALLOWED, new Object[]{attributes.getQName(i), rawName}, null);
        }
        else
        {
          undefines.addAttribute(attrUri, attrLocalName,
                                 attributes.getQName(i),
                                 attributes.getType(i),
                                 attributes.getValue(i));
        }
      }
      else
      {
        boolean success = attrDef.setAttrValue(handler, attrUri, attrLocalName,
                             attributes.getQName(i), attributes.getValue(i),
                             target);
        if (success)
            processedDefs.add(attrDef);
        else
            errorDefs.add(attrDef);
      }
    }
    XSLTAttributeDef[] attrDefs = def.getAttributes();
    int nAttrDefs = attrDefs.length;
    for (int i = 0; i < nAttrDefs; i++)
    {
      XSLTAttributeDef attrDef = attrDefs[i];
      String defVal = attrDef.getDefault();
      if (null != defVal)
      {
        if (!processedDefs.contains(attrDef))
        {
          attrDef.setDefAttrValue(handler, target);
        }
      }
      if (attrDef.getRequired())
      {
        if ((!processedDefs.contains(attrDef)) && (!errorDefs.contains(attrDef)))
          handler.error(
            XSLMessages.createMessage(
              XSLTErrorResources.ER_REQUIRES_ATTRIB, new Object[]{ rawName,
                                                                   attrDef.getName() }), null);
      }
    }
    return undefines;
  }
}
