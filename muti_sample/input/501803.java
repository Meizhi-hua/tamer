public class ElemParam extends ElemVariable
{
    static final long serialVersionUID = -1131781475589006431L;
  int m_qnameID;
  public ElemParam(){}
  public int getXSLToken()
  {
    return Constants.ELEMNAME_PARAMVARIABLE;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_PARAMVARIABLE_STRING;
  }
  public ElemParam(ElemParam param) throws TransformerException
  {
    super(param);
  }
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    m_qnameID = sroot.getComposeState().getQNameID(m_qname);
    int parentToken = m_parentNode.getXSLToken();
    if (parentToken == Constants.ELEMNAME_TEMPLATE
        || parentToken == Constants.EXSLT_ELEMNAME_FUNCTION)
      ((ElemTemplate)m_parentNode).m_inArgsSize++;
  }
  public void execute(TransformerImpl transformer) throws TransformerException
  {
    VariableStack vars = transformer.getXPathContext().getVarStack();
    if(!vars.isLocalSet(m_index))
    {
      int sourceNode = transformer.getXPathContext().getCurrentNode();
      XObject var = getValue(transformer, sourceNode);
      transformer.getXPathContext().getVarStack().setLocalVariable(m_index, var);
    }
  }
}
