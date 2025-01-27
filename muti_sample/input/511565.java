public class StylesheetComposed extends Stylesheet
{
    static final long serialVersionUID = -3444072247410233923L;
  public StylesheetComposed(Stylesheet parent)
  {
    super(parent);
  }
  public boolean isAggregatedType()
  {
    return true;
  }
  public void recompose(Vector recomposableElements) throws TransformerException
  {
    int n = getIncludeCountComposed();
    for (int i = -1; i < n; i++)
    {
      Stylesheet included = getIncludeComposed(i);
      int s = included.getOutputCount();
      for (int j = 0; j < s; j++)
      {
        recomposableElements.addElement(included.getOutput(j));
      }
      s = included.getAttributeSetCount();
      for (int j = 0; j < s; j++)
      {
        recomposableElements.addElement(included.getAttributeSet(j));
      }
      s = included.getDecimalFormatCount();
      for (int j = 0; j < s; j++)
      {
        recomposableElements.addElement(included.getDecimalFormat(j));
      }
      s = included.getKeyCount();
      for (int j = 0; j < s; j++)
      {
        recomposableElements.addElement(included.getKey(j));
      }
      s = included.getNamespaceAliasCount();
      for (int j = 0; j < s; j++)
      {
        recomposableElements.addElement(included.getNamespaceAlias(j));
      }
      s = included.getTemplateCount();
      for (int j = 0; j < s; j++)
      {
        recomposableElements.addElement(included.getTemplate(j));
      }
      s = included.getVariableOrParamCount();
      for (int j = 0; j < s; j++)
      {
        recomposableElements.addElement(included.getVariableOrParam(j));
      }
      s = included.getStripSpaceCount();
      for (int j = 0; j < s; j++)
      {
        recomposableElements.addElement(included.getStripSpace(j));
      }
      s = included.getPreserveSpaceCount();
      for (int j = 0; j < s; j++)
      {
        recomposableElements.addElement(included.getPreserveSpace(j));
      }
    }
  }
  private int m_importNumber = -1;
  private int m_importCountComposed;
  private int m_endImportCountComposed;
  void recomposeImports()
  {
    m_importNumber = getStylesheetRoot().getImportNumber(this);
    StylesheetRoot root = getStylesheetRoot();
    int globalImportCount = root.getGlobalImportCount();
    m_importCountComposed = (globalImportCount - m_importNumber) - 1;
    int count = getImportCount();
    if ( count > 0)
    {
      m_endImportCountComposed += count;
      while (count > 0)
        m_endImportCountComposed += this.getImport(--count).getEndImportCountComposed();
    }
    count = getIncludeCountComposed();
    while (count>0)
    {
      int imports = getIncludeComposed(--count).getImportCount();
      m_endImportCountComposed += imports;
      while (imports > 0)
        m_endImportCountComposed +=getIncludeComposed(count).getImport(--imports).getEndImportCountComposed();
    }                                                            
  }
  public StylesheetComposed getImportComposed(int i)
          throws ArrayIndexOutOfBoundsException
  {
    StylesheetRoot root = getStylesheetRoot();
    return root.getGlobalImport(1 + m_importNumber + i);
  }
  public int getImportCountComposed()
  {
    return m_importCountComposed;
  }
  public int getEndImportCountComposed()
  {
    return m_endImportCountComposed;
  }
  private transient Vector m_includesComposed;
  void recomposeIncludes(Stylesheet including)
  {
    int n = including.getIncludeCount();
    if (n > 0)
    {
      if (null == m_includesComposed)
        m_includesComposed = new Vector();
      for (int i = 0; i < n; i++)
      {
        Stylesheet included = including.getInclude(i);
        m_includesComposed.addElement(included);
        recomposeIncludes(included);
      }
    }
  }
  public Stylesheet getIncludeComposed(int i)
          throws ArrayIndexOutOfBoundsException
  {
    if (-1 == i)
      return this;
    if (null == m_includesComposed)
      throw new ArrayIndexOutOfBoundsException();
    return (Stylesheet) m_includesComposed.elementAt(i);
  }
  public int getIncludeCountComposed()
  {
    return (null != m_includesComposed) ? m_includesComposed.size() : 0;
  }
  public void recomposeTemplates(boolean flushFirst) throws TransformerException
  {
  }
}
