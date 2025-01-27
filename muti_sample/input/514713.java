public class ElemCopyOf extends ElemTemplateElement
{
    static final long serialVersionUID = -7433828829497411127L;
  public XPath m_selectExpression = null;
  public void setSelect(XPath expr)
  {
    m_selectExpression = expr;
  }
  public XPath getSelect()
  {
    return m_selectExpression;
  }
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    m_selectExpression.fixupVariables(cstate.getVariableNames(), cstate.getGlobalsSize());
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_COPY_OF;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_COPY_OF_STRING;
  }
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
    try
    {
      XPathContext xctxt = transformer.getXPathContext();
      int sourceNode = xctxt.getCurrentNode();
      XObject value = m_selectExpression.execute(xctxt, sourceNode, this);
      SerializationHandler handler = transformer.getSerializationHandler();
      if (null != value)
                        {
        int type = value.getType();
        String s;
        switch (type)
        {
        case XObject.CLASS_BOOLEAN :
        case XObject.CLASS_NUMBER :
        case XObject.CLASS_STRING :
          s = value.str();
          handler.characters(s.toCharArray(), 0, s.length());
          break;
        case XObject.CLASS_NODESET :
          DTMIterator nl = value.iter();
          DTMTreeWalker tw = new TreeWalker2Result(transformer, handler);
          int pos;
          while (DTM.NULL != (pos = nl.nextNode()))
          {
            DTM dtm = xctxt.getDTMManager().getDTM(pos);
            short t = dtm.getNodeType(pos);
            if (t == DTM.DOCUMENT_NODE)
            {
              for (int child = dtm.getFirstChild(pos); child != DTM.NULL;
                   child = dtm.getNextSibling(child))
              {
                tw.traverse(child);
              }
            }
            else if (t == DTM.ATTRIBUTE_NODE)
            {
              SerializerUtils.addAttribute(handler, pos);
            }
            else
            {
              tw.traverse(pos);
            }
          }
          break;
        case XObject.CLASS_RTREEFRAG :
          SerializerUtils.outputResultTreeFragment(
            handler, value, transformer.getXPathContext());
          break;
        default :
          s = value.str();
          handler.characters(s.toCharArray(), 0, s.length());
          break;
        }
      }
    }
    catch(org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
  }
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {
    error(XSLTErrorResources.ER_CANNOT_ADD,
          new Object[]{ newChild.getNodeName(),
                        this.getNodeName() });  
    return null;
  }
  protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
  {
  	if(callAttrs)
  		m_selectExpression.getExpression().callVisitors(m_selectExpression, visitor);
    super.callChildVisitors(visitor, callAttrs);
  }
}
