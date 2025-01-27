public class ProcessorTemplateElem extends XSLTElementProcessor
{
    static final long serialVersionUID = 8344994001943407235L;
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    super.startElement(handler, uri, localName, rawName, attributes);
    try
    {
      XSLTElementDef def = getElemDef();
      Class classObject = def.getClassObject();
      ElemTemplateElement elem = null;
      try
      {
        elem = (ElemTemplateElement) classObject.newInstance();
        elem.setDOMBackPointer(handler.getOriginatingNode());
        elem.setLocaterInfo(handler.getLocator());
        elem.setPrefixes(handler.getNamespaceSupport());
      }
      catch (InstantiationException ie)
      {
        handler.error(XSLTErrorResources.ER_FAILED_CREATING_ELEMTMPL, null, ie);
      }
      catch (IllegalAccessException iae)
      {
        handler.error(XSLTErrorResources.ER_FAILED_CREATING_ELEMTMPL, null, iae);
      }
      setPropertiesFromAttributes(handler, rawName, attributes, elem);
      appendAndPush(handler, elem);
    }
    catch(TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
  protected void appendAndPush(
          StylesheetHandler handler, ElemTemplateElement elem)
            throws org.xml.sax.SAXException
  {
    ElemTemplateElement parent = handler.getElemTemplateElement();
    if(null != parent)  
    {
      parent.appendChild(elem);
      handler.pushElemTemplateElement(elem);
    }
  }
  public void endElement(
          StylesheetHandler handler, String uri, String localName, String rawName)
            throws org.xml.sax.SAXException
  {
    super.endElement(handler, uri, localName, rawName);
    handler.popElemTemplateElement().setEndLocaterInfo(handler.getLocator());
  }
}
