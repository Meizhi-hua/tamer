public class XUnresolvedVariable extends XObject
{  
    static final long serialVersionUID = -256779804767950188L;
  transient private int m_context;
  transient private TransformerImpl m_transformer;
  transient private int m_varStackPos = -1;
  transient private int m_varStackContext;
  private boolean m_isGlobal;
  transient private boolean m_doneEval = true;
  public XUnresolvedVariable(ElemVariable obj, int sourceNode, 
                             TransformerImpl transformer,
                             int varStackPos, int varStackContext,
                             boolean isGlobal)
  {
    super(obj);
    m_context = sourceNode;
    m_transformer = transformer;
    m_varStackPos = varStackPos;
    m_varStackContext = varStackContext;
    m_isGlobal = isGlobal;
  }
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    if (!m_doneEval) 
    {
      this.m_transformer.getMsgMgr().error      
        (xctxt.getSAXLocator(), XSLTErrorResources.ER_REFERENCING_ITSELF, 
          new Object[]{((ElemVariable)this.object()).getName().getLocalName()}); 
    }
    VariableStack vars = xctxt.getVarStack();
    int currentFrame = vars.getStackFrame();
    ElemVariable velem = (ElemVariable)m_obj;
    try
    {
      m_doneEval = false;
      if(-1 != velem.m_frameSize)
      	vars.link(velem.m_frameSize);
      XObject var = velem.getValue(m_transformer, m_context);
      m_doneEval = true;
      return var;
    }
    finally
    {
      if(-1 != velem.m_frameSize)
	  	vars.unlink(currentFrame);
    }
  }
  public void setVarStackPos(int top)
  {
    m_varStackPos = top;
  }
  public void setVarStackContext(int bottom)
  {
    m_varStackContext = bottom;
  }
  public int getType()
  {
    return CLASS_UNRESOLVEDVARIABLE;
  }
  public String getTypeString()
  {
    return "XUnresolvedVariable (" + object().getClass().getName() + ")";
  }
}
