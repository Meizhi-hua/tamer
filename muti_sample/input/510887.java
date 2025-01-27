class ProcessorOutputElem extends XSLTElementProcessor
{
    static final long serialVersionUID = 3513742319582547590L;
  private OutputProperties m_outputProperties;
  public void setCdataSectionElements(java.util.Vector newValue)
  {
    m_outputProperties.setQNameProperties(OutputKeys.CDATA_SECTION_ELEMENTS, newValue);
  }
  public void setDoctypePublic(String newValue)
  {
    m_outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC, newValue);
  }
  public void setDoctypeSystem(String newValue)
  {
    m_outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM, newValue);
  }
  public void setEncoding(String newValue)
  {
    m_outputProperties.setProperty(OutputKeys.ENCODING, newValue);
  }
  public void setIndent(boolean newValue)
  {
    m_outputProperties.setBooleanProperty(OutputKeys.INDENT, newValue);
  }
  public void setMediaType(String newValue)
  {
    m_outputProperties.setProperty(OutputKeys.MEDIA_TYPE, newValue);
  }
  public void setMethod(org.apache.xml.utils.QName newValue)
  {
    m_outputProperties.setQNameProperty(OutputKeys.METHOD, newValue);
  }
  public void setOmitXmlDeclaration(boolean newValue)
  {
    m_outputProperties.setBooleanProperty(OutputKeys.OMIT_XML_DECLARATION, newValue);
  }
  public void setStandalone(boolean newValue)
  {
    m_outputProperties.setBooleanProperty(OutputKeys.STANDALONE, newValue);
  }
  public void setVersion(String newValue)
  {
    m_outputProperties.setProperty(OutputKeys.VERSION, newValue);
  }
  public void setForeignAttr(String attrUri, String attrLocalName, String attrRawName, String attrValue)
  {
    QName key = new QName(attrUri, attrLocalName);
    m_outputProperties.setProperty(key, attrValue);
  }
  public void addLiteralResultAttribute(String attrUri, String attrLocalName, String attrRawName, String attrValue)
  {
    QName key = new QName(attrUri, attrLocalName);
    m_outputProperties.setProperty(key, attrValue);
  }
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    m_outputProperties = new OutputProperties();
    m_outputProperties.setDOMBackPointer(handler.getOriginatingNode());
    m_outputProperties.setLocaterInfo(handler.getLocator());
    m_outputProperties.setUid(handler.nextUid());
    setPropertiesFromAttributes(handler, rawName, attributes, this);
    String entitiesFileName =
      (String) m_outputProperties.getProperties().get(OutputPropertiesFactory.S_KEY_ENTITIES);
    if (null != entitiesFileName)
    {
      try
      {
        String absURL = SystemIDResolver.getAbsoluteURI(entitiesFileName,
                    handler.getBaseIdentifier());
        m_outputProperties.getProperties().put(OutputPropertiesFactory.S_KEY_ENTITIES, absURL);
      }
      catch(TransformerException te)
      {
        handler.error(te.getMessage(), te);
      }
    }
    handler.getStylesheet().setOutput(m_outputProperties);
    ElemTemplateElement parent = handler.getElemTemplateElement();
    parent.appendChild(m_outputProperties);
    m_outputProperties = null;
  }
}
