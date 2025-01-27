public class DecimalFormatProperties extends ElemTemplateElement
{
    static final long serialVersionUID = -6559409339256269446L;
  DecimalFormatSymbols m_dfs;
  public DecimalFormatProperties(int docOrderNumber)
  {
    m_dfs = new java.text.DecimalFormatSymbols();
    m_dfs.setInfinity(Constants.ATTRVAL_INFINITY);
    m_dfs.setNaN(Constants.ATTRVAL_NAN);
    m_docOrderNumber = docOrderNumber;
  }
  public DecimalFormatSymbols getDecimalFormatSymbols()
  {
    return m_dfs;
  }
  private QName m_qname = null;
  public void setName(QName qname)
  {
    m_qname = qname;
  }
  public QName getName()
  {
    if (m_qname == null)
      return new QName("");
    else
      return m_qname;
  }
  public void setDecimalSeparator(char ds)
  {
    m_dfs.setDecimalSeparator(ds);
  }
  public char getDecimalSeparator()
  {
    return m_dfs.getDecimalSeparator();
  }
  public void setGroupingSeparator(char gs)
  {
    m_dfs.setGroupingSeparator(gs);
  }
  public char getGroupingSeparator()
  {
    return m_dfs.getGroupingSeparator();
  }
  public void setInfinity(String inf)
  {
    m_dfs.setInfinity(inf);
  }
  public String getInfinity()
  {
    return m_dfs.getInfinity();
  }
  public void setMinusSign(char v)
  {
    m_dfs.setMinusSign(v);
  }
  public char getMinusSign()
  {
    return m_dfs.getMinusSign();
  }
  public void setNaN(String v)
  {
    m_dfs.setNaN(v);
  }
  public String getNaN()
  {
    return m_dfs.getNaN();
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_DECIMALFORMAT_STRING;
  }
  public void setPercent(char v)
  {
    m_dfs.setPercent(v);
  }
  public char getPercent()
  {
    return m_dfs.getPercent();
  }
  public void setPerMille(char v)
  {
    m_dfs.setPerMill(v);
  }
  public char getPerMille()
  {
    return m_dfs.getPerMill();
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_DECIMALFORMAT;
  }
  public void setZeroDigit(char v)
  {
    m_dfs.setZeroDigit(v);
  }
  public char getZeroDigit()
  {
    return m_dfs.getZeroDigit();
  }
  public void setDigit(char v)
  {
    m_dfs.setDigit(v);
  }
  public char getDigit()
  {
    return m_dfs.getDigit();
  }
  public void setPatternSeparator(char v)
  {
    m_dfs.setPatternSeparator(v);
  }
  public char getPatternSeparator()
  {
    return m_dfs.getPatternSeparator();
  }
  public void recompose(StylesheetRoot root)
  {
    root.recomposeDecimalFormats(this);
  }
}
