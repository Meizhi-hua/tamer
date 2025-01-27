class ProcessorKey extends XSLTElementProcessor
{
    static final long serialVersionUID = 4285205417566822979L;
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    KeyDeclaration kd = new KeyDeclaration(handler.getStylesheet(), handler.nextUid());
    kd.setDOMBackPointer(handler.getOriginatingNode());
    kd.setLocaterInfo(handler.getLocator());
    setPropertiesFromAttributes(handler, rawName, attributes, kd);
    handler.getStylesheet().setKey(kd);
  }
  void setPropertiesFromAttributes(
          StylesheetHandler handler, String rawName, Attributes attributes, 
          org.apache.xalan.templates.ElemTemplateElement target)
            throws org.xml.sax.SAXException
  {
    XSLTElementDef def = getElemDef();
    List processedDefs = new ArrayList();
    int nAttrs = attributes.getLength();
    for (int i = 0; i < nAttrs; i++)
    {
      String attrUri = attributes.getURI(i);
      String attrLocalName = attributes.getLocalName(i);
      XSLTAttributeDef attrDef = def.getAttributeDef(attrUri, attrLocalName);
      if (null == attrDef)
      {
        handler.error(attributes.getQName(i)
                      + "attribute is not allowed on the " + rawName
                      + " element!", null);
      }
      else
      {
        String valueString = attributes.getValue(i);
        if (valueString.indexOf(org.apache.xpath.compiler.Keywords.FUNC_KEY_STRING
                                + "(") >= 0)
          handler.error(
            XSLMessages.createMessage(
            XSLTErrorResources.ER_INVALID_KEY_CALL, null), null);
        processedDefs.add(attrDef);
        attrDef.setAttrValue(handler, attrUri, attrLocalName,
                             attributes.getQName(i), attributes.getValue(i),
                             target);
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
        if (!processedDefs.contains(attrDef))
          handler.error(
            XSLMessages.createMessage(
              XSLTErrorResources.ER_REQUIRES_ATTRIB, new Object[]{ rawName,
                                                                   attrDef.getName() }), null);
      }
    }
  }
}
