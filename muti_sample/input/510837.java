public class LocatorImpl implements Locator
{
    public LocatorImpl ()
    {
    }
    public LocatorImpl (Locator locator)
    {
    setPublicId(locator.getPublicId());
    setSystemId(locator.getSystemId());
    setLineNumber(locator.getLineNumber());
    setColumnNumber(locator.getColumnNumber());
    }
    public String getPublicId ()
    {
    return publicId;
    }
    public String getSystemId ()
    {
    return systemId;
    }
    public int getLineNumber ()
    {
    return lineNumber;
    }
    public int getColumnNumber ()
    {
    return columnNumber;
    }
    public void setPublicId (String publicId)
    {
    this.publicId = publicId;
    }
    public void setSystemId (String systemId)
    {
    this.systemId = systemId;
    }
    public void setLineNumber (int lineNumber)
    {
    this.lineNumber = lineNumber;
    }
    public void setColumnNumber (int columnNumber)
    {
    this.columnNumber = columnNumber;
    }
    private String publicId;
    private String systemId;
    private int lineNumber;
    private int columnNumber;
}
