public class ElemSort extends ElemTemplateElement
{
    static final long serialVersionUID = -4991510257335851938L;
  private XPath m_selectExpression = null;
  public void setSelect(XPath v)
  {
    if (v.getPatternString().indexOf("{") < 0)
      m_selectExpression = v;
    else
      error(XSLTErrorResources.ER_NO_CURLYBRACE, null);
  }
  public XPath getSelect()
  {
    return m_selectExpression;
  }
  private AVT m_lang_avt = null;
  public void setLang(AVT v)
  {
    m_lang_avt = v;
  }
  public AVT getLang()
  {
    return m_lang_avt;
  }
  private AVT m_dataType_avt = null;
  public void setDataType(AVT v)
  {
    m_dataType_avt = v;
  }
  public AVT getDataType()
  {
    return m_dataType_avt;
  }
  private AVT m_order_avt = null;
  public void setOrder(AVT v)
  {
    m_order_avt = v;
  }
  public AVT getOrder()
  {
    return m_order_avt;
  }
  private AVT m_caseorder_avt = null;
  public void setCaseOrder(AVT v)
  {
    m_caseorder_avt = v;
  }
  public AVT getCaseOrder()
  {
    return m_caseorder_avt;
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_SORT;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_SORT_STRING;
  }
  public Node appendChild(Node newChild) throws DOMException
  {
    error(XSLTErrorResources.ER_CANNOT_ADD,
          new Object[]{ newChild.getNodeName(),
                        this.getNodeName() });  
    return null;
  }
  public void compose(StylesheetRoot sroot) 
    throws javax.xml.transform.TransformerException
  {
    super.compose(sroot);
    StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    java.util.Vector vnames = cstate.getVariableNames();
    if(null != m_caseorder_avt)
      m_caseorder_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_dataType_avt)
      m_dataType_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_lang_avt)
      m_lang_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_order_avt)
      m_order_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_selectExpression)
      m_selectExpression.fixupVariables(vnames, cstate.getGlobalsSize());
  }
}
