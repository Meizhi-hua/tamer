public class Function2Args extends FunctionOneArg
{
    static final long serialVersionUID = 5574294996842710641L;
  Expression m_arg1;
  public Expression getArg1()
  {
    return m_arg1;
  }
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
    super.fixupVariables(vars, globalsSize);
    if(null != m_arg1)
      m_arg1.fixupVariables(vars, globalsSize);
  }
  public void setArg(Expression arg, int argNum)
          throws WrongNumberArgsException
  {
    if (argNum == 0)
      super.setArg(arg, argNum);
    else if (1 == argNum)
    {
      m_arg1 = arg;
      arg.exprSetParent(this);
    }
    else
		  reportWrongNumberArgs();
  }
  public void checkNumberArgs(int argNum) throws WrongNumberArgsException
  {
    if (argNum != 2)
      reportWrongNumberArgs();
  }
  protected void reportWrongNumberArgs() throws WrongNumberArgsException {
      throw new WrongNumberArgsException(XSLMessages.createXPATHMessage("two", null));
  }
   public boolean canTraverseOutsideSubtree()
   {
    return super.canTraverseOutsideSubtree() 
    ? true : m_arg1.canTraverseOutsideSubtree();
   }
  class Arg1Owner implements ExpressionOwner
  {
    public Expression getExpression()
    {
      return m_arg1;
    }
    public void setExpression(Expression exp)
    {
    	exp.exprSetParent(Function2Args.this);
    	m_arg1 = exp;
    }
  }
  public void callArgVisitors(XPathVisitor visitor)
  {
  	super.callArgVisitors(visitor);
  	if(null != m_arg1)
  		m_arg1.callVisitors(new Arg1Owner(), visitor);
  }
  public boolean deepEquals(Expression expr)
  {
  	if(!super.deepEquals(expr))
  		return false;
  	if(null != m_arg1)
  	{
  		if(null == ((Function2Args)expr).m_arg1)
  			return false;
  		if(!m_arg1.deepEquals(((Function2Args)expr).m_arg1))
  			return false;
  	}
  	else if(null != ((Function2Args)expr).m_arg1)
  		return false;
  	return true;
  }
}
