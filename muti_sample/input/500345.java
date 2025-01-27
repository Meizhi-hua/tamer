public class SAXSourceLocator extends LocatorImpl
        implements SourceLocator, Serializable
{
    static final long serialVersionUID = 3181680946321164112L;
  Locator m_locator;
  public SAXSourceLocator(){}
  public SAXSourceLocator(Locator locator)
  {
    m_locator = locator;
    this.setColumnNumber(locator.getColumnNumber());
    this.setLineNumber(locator.getLineNumber());
    this.setPublicId(locator.getPublicId());
    this.setSystemId(locator.getSystemId());
  }
  public SAXSourceLocator(javax.xml.transform.SourceLocator locator)
  {
    m_locator = null;
    this.setColumnNumber(locator.getColumnNumber());
    this.setLineNumber(locator.getLineNumber());
    this.setPublicId(locator.getPublicId());
    this.setSystemId(locator.getSystemId());
  }
  public SAXSourceLocator(SAXParseException spe)
  {
    this.setLineNumber( spe.getLineNumber() );
    this.setColumnNumber( spe.getColumnNumber() );
    this.setPublicId( spe.getPublicId() );
    this.setSystemId( spe.getSystemId() );
  }
  public String getPublicId()
  {
    return (null == m_locator) ? super.getPublicId() : m_locator.getPublicId();
  }
  public String getSystemId()
  {
    return (null == m_locator) ? super.getSystemId() : m_locator.getSystemId();
  }
  public int getLineNumber()
  {
    return (null == m_locator) ? super.getLineNumber() : m_locator.getLineNumber();
  }
  public int getColumnNumber()
  {
    return (null == m_locator) ? super.getColumnNumber() : m_locator.getColumnNumber();
  }
}
