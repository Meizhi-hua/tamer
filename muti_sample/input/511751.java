public class ProcessorCharacters extends XSLTElementProcessor
{
    static final long serialVersionUID = 8632900007814162650L;
  public void startNonText(StylesheetHandler handler) throws org.xml.sax.SAXException
  {
    if (this == handler.getCurrentProcessor())
    {
      handler.popProcessor();
    }
    int nChars = m_accumulator.length();
    if ((nChars > 0)
            && ((null != m_xslTextElement)
                ||!XMLCharacterRecognizer.isWhiteSpace(m_accumulator)) 
                || handler.isSpacePreserve())
    {
      ElemTextLiteral elem = new ElemTextLiteral();
      elem.setDOMBackPointer(m_firstBackPointer);
      elem.setLocaterInfo(handler.getLocator());
      try
      {
        elem.setPrefixes(handler.getNamespaceSupport());
      }
      catch(TransformerException te)
      {
        throw new org.xml.sax.SAXException(te);
      }
      boolean doe = (null != m_xslTextElement)
                    ? m_xslTextElement.getDisableOutputEscaping() : false;
      elem.setDisableOutputEscaping(doe);
      elem.setPreserveSpace(true);
      char[] chars = new char[nChars];
      m_accumulator.getChars(0, nChars, chars, 0);
      elem.setChars(chars);
      ElemTemplateElement parent = handler.getElemTemplateElement();
      parent.appendChild(elem);
    }
    m_accumulator.setLength(0);
    m_firstBackPointer = null;
  }
  protected Node m_firstBackPointer = null;
  public void characters(
          StylesheetHandler handler, char ch[], int start, int length)
            throws org.xml.sax.SAXException
  {
    m_accumulator.append(ch, start, length);
    if(null == m_firstBackPointer)
      m_firstBackPointer = handler.getOriginatingNode();
    if (this != handler.getCurrentProcessor())
      handler.pushProcessor(this);
  }
  public void endElement(
          StylesheetHandler handler, String uri, String localName, String rawName)
            throws org.xml.sax.SAXException
  {
    startNonText(handler);
    handler.getCurrentProcessor().endElement(handler, uri, localName,
                                             rawName);
    handler.popProcessor();
  }
  private StringBuffer m_accumulator = new StringBuffer();
  private ElemText m_xslTextElement;
  void setXslTextElement(ElemText xslTextElement)
  {
    m_xslTextElement = xslTextElement;
  }
}
