public class XPathParser
{
	static public final String CONTINUE_AFTER_FATAL_ERROR="CONTINUE_AFTER_FATAL_ERROR";
  private OpMap m_ops;
  transient String m_token;
  transient char m_tokenChar = 0;
  int m_queueMark = 0;
  protected final static int FILTER_MATCH_FAILED     = 0;
  protected final static int FILTER_MATCH_PRIMARY    = 1;
  protected final static int FILTER_MATCH_PREDICATES = 2;
  public XPathParser(ErrorListener errorListener, javax.xml.transform.SourceLocator sourceLocator)
  {
    m_errorListener = errorListener;
    m_sourceLocator = sourceLocator;
  }
  PrefixResolver m_namespaceContext;
  public void initXPath(
          Compiler compiler, String expression, PrefixResolver namespaceContext)
            throws javax.xml.transform.TransformerException
  {
    m_ops = compiler;
    m_namespaceContext = namespaceContext;
    m_functionTable = compiler.getFunctionTable();
    Lexer lexer = new Lexer(compiler, namespaceContext, this);
    lexer.tokenize(expression);
    m_ops.setOp(0,OpCodes.OP_XPATH);
    m_ops.setOp(OpMap.MAPINDEX_LENGTH,2);
	try {
      nextToken();
      Expr();
      if (null != m_token)
      {
        String extraTokens = "";
        while (null != m_token)
        {
          extraTokens += "'" + m_token + "'";
          nextToken();
          if (null != m_token)
            extraTokens += ", ";
        }
        error(XPATHErrorResources.ER_EXTRA_ILLEGAL_TOKENS,
              new Object[]{ extraTokens });  
      }
    } 
    catch (org.apache.xpath.XPathProcessorException e)
    {
	  if(CONTINUE_AFTER_FATAL_ERROR.equals(e.getMessage()))
	  {
		initXPath(compiler, "/..",  namespaceContext);
	  }
	  else
		throw e;
    }
    compiler.shrink();
  }
  public void initMatchPattern(
          Compiler compiler, String expression, PrefixResolver namespaceContext)
            throws javax.xml.transform.TransformerException
  {
    m_ops = compiler;
    m_namespaceContext = namespaceContext;
    m_functionTable = compiler.getFunctionTable();
    Lexer lexer = new Lexer(compiler, namespaceContext, this);
    lexer.tokenize(expression);
    m_ops.setOp(0, OpCodes.OP_MATCHPATTERN);
    m_ops.setOp(OpMap.MAPINDEX_LENGTH, 2);
    nextToken();
    Pattern();
    if (null != m_token)
    {
      String extraTokens = "";
      while (null != m_token)
      {
        extraTokens += "'" + m_token + "'";
        nextToken();
        if (null != m_token)
          extraTokens += ", ";
      }
      error(XPATHErrorResources.ER_EXTRA_ILLEGAL_TOKENS,
            new Object[]{ extraTokens });  
    }
    m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), OpCodes.ENDOP);
    m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
    m_ops.shrink();
  }
  private ErrorListener m_errorListener;
  javax.xml.transform.SourceLocator m_sourceLocator;
  private FunctionTable m_functionTable;
  public void setErrorHandler(ErrorListener handler)
  {
    m_errorListener = handler;
  }
  public ErrorListener getErrorListener()
  {
    return m_errorListener;
  }
  final boolean tokenIs(String s)
  {
    return (m_token != null) ? (m_token.equals(s)) : (s == null);
  }
  final boolean tokenIs(char c)
  {
    return (m_token != null) ? (m_tokenChar == c) : false;
  }
  final boolean lookahead(char c, int n)
  {
    int pos = (m_queueMark + n);
    boolean b;
    if ((pos <= m_ops.getTokenQueueSize()) && (pos > 0)
            && (m_ops.getTokenQueueSize() != 0))
    {
      String tok = ((String) m_ops.m_tokenQueue.elementAt(pos - 1));
      b = (tok.length() == 1) ? (tok.charAt(0) == c) : false;
    }
    else
    {
      b = false;
    }
    return b;
  }
  private final boolean lookbehind(char c, int n)
  {
    boolean isToken;
    int lookBehindPos = m_queueMark - (n + 1);
    if (lookBehindPos >= 0)
    {
      String lookbehind = (String) m_ops.m_tokenQueue.elementAt(lookBehindPos);
      if (lookbehind.length() == 1)
      {
        char c0 = (lookbehind == null) ? '|' : lookbehind.charAt(0);
        isToken = (c0 == '|') ? false : (c0 == c);
      }
      else
      {
        isToken = false;
      }
    }
    else
    {
      isToken = false;
    }
    return isToken;
  }
  private final boolean lookbehindHasToken(int n)
  {
    boolean hasToken;
    if ((m_queueMark - n) > 0)
    {
      String lookbehind = (String) m_ops.m_tokenQueue.elementAt(m_queueMark - (n - 1));
      char c0 = (lookbehind == null) ? '|' : lookbehind.charAt(0);
      hasToken = (c0 == '|') ? false : true;
    }
    else
    {
      hasToken = false;
    }
    return hasToken;
  }
  private final boolean lookahead(String s, int n)
  {
    boolean isToken;
    if ((m_queueMark + n) <= m_ops.getTokenQueueSize())
    {
      String lookahead = (String) m_ops.m_tokenQueue.elementAt(m_queueMark + (n - 1));
      isToken = (lookahead != null) ? lookahead.equals(s) : (s == null);
    }
    else
    {
      isToken = (null == s);
    }
    return isToken;
  }
  private final void nextToken()
  {
    if (m_queueMark < m_ops.getTokenQueueSize())
    {
      m_token = (String) m_ops.m_tokenQueue.elementAt(m_queueMark++);
      m_tokenChar = m_token.charAt(0);
    }
    else
    {
      m_token = null;
      m_tokenChar = 0;
    }
  }
  private final String getTokenRelative(int i)
  {
    String tok;
    int relative = m_queueMark + i;
    if ((relative > 0) && (relative < m_ops.getTokenQueueSize()))
    {
      tok = (String) m_ops.m_tokenQueue.elementAt(relative);
    }
    else
    {
      tok = null;
    }
    return tok;
  }
  private final void prevToken()
  {
    if (m_queueMark > 0)
    {
      m_queueMark--;
      m_token = (String) m_ops.m_tokenQueue.elementAt(m_queueMark);
      m_tokenChar = m_token.charAt(0);
    }
    else
    {
      m_token = null;
      m_tokenChar = 0;
    }
  }
  private final void consumeExpected(String expected)
          throws javax.xml.transform.TransformerException
  {
    if (tokenIs(expected))
    {
      nextToken();
    }
    else
    {
      error(XPATHErrorResources.ER_EXPECTED_BUT_FOUND, new Object[]{ expected,
                                                                     m_token });  
		throw new XPathProcessorException(CONTINUE_AFTER_FATAL_ERROR);
	}
  }
  private final void consumeExpected(char expected)
          throws javax.xml.transform.TransformerException
  {
    if (tokenIs(expected))
    {
      nextToken();
    }
    else
    {
      error(XPATHErrorResources.ER_EXPECTED_BUT_FOUND,
            new Object[]{ String.valueOf(expected),
                          m_token });  
		throw new XPathProcessorException(CONTINUE_AFTER_FATAL_ERROR);
    }
  }
  void warn(String msg, Object[] args) throws TransformerException
  {
    String fmsg = XSLMessages.createXPATHWarning(msg, args);
    ErrorListener ehandler = this.getErrorListener();
    if (null != ehandler)
    {
      ehandler.warning(new TransformerException(fmsg, m_sourceLocator));
    }
    else
    {
      System.err.println(fmsg);
    }
  }
  private void assertion(boolean b, String msg)
  {
    if (!b)
    {
      String fMsg = XSLMessages.createXPATHMessage(
        XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION,
        new Object[]{ msg });
      throw new RuntimeException(fMsg);
    }
  }
  void error(String msg, Object[] args) throws TransformerException
  {
    String fmsg = XSLMessages.createXPATHMessage(msg, args);
    ErrorListener ehandler = this.getErrorListener();
    TransformerException te = new TransformerException(fmsg, m_sourceLocator);
    if (null != ehandler)
    {
      ehandler.fatalError(te);
    }
    else
    {
      throw te;
    }
  }
  void errorForDOM3(String msg, Object[] args) throws TransformerException
  {
	String fmsg = XSLMessages.createXPATHMessage(msg, args);
	ErrorListener ehandler = this.getErrorListener();
	TransformerException te = new XPathStylesheetDOM3Exception(fmsg, m_sourceLocator);
	if (null != ehandler)
	{
	  ehandler.fatalError(te);
	}
	else
	{
	  throw te;
	}
  }
  protected String dumpRemainingTokenQueue()
  {
    int q = m_queueMark;
    String returnMsg;
    if (q < m_ops.getTokenQueueSize())
    {
      String msg = "\n Remaining tokens: (";
      while (q < m_ops.getTokenQueueSize())
      {
        String t = (String) m_ops.m_tokenQueue.elementAt(q++);
        msg += (" '" + t + "'");
      }
      returnMsg = msg + ")";
    }
    else
    {
      returnMsg = "";
    }
    return returnMsg;
  }
  final int getFunctionToken(String key)
  {
    int tok;
    Object id;
    try
    {
      id = Keywords.lookupNodeTest(key);
      if (null == id) id = m_functionTable.getFunctionID(key);
      tok = ((Integer) id).intValue();
    }
    catch (NullPointerException npe)
    {
      tok = -1;
    }
    catch (ClassCastException cce)
    {
      tok = -1;
    }
    return tok;
  }
  void insertOp(int pos, int length, int op)
  {
    int totalLen = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    for (int i = totalLen - 1; i >= pos; i--)
    {
      m_ops.setOp(i + length, m_ops.getOp(i));
    }
    m_ops.setOp(pos,op);
    m_ops.setOp(OpMap.MAPINDEX_LENGTH,totalLen + length);
  }
  void appendOp(int length, int op)
  {
    int totalLen = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    m_ops.setOp(totalLen, op);
    m_ops.setOp(totalLen + OpMap.MAPINDEX_LENGTH, length);
    m_ops.setOp(OpMap.MAPINDEX_LENGTH, totalLen + length);
  }
  protected void Expr() throws javax.xml.transform.TransformerException
  {
    OrExpr();
  }
  protected void OrExpr() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    AndExpr();
    if ((null != m_token) && tokenIs("or"))
    {
      nextToken();
      insertOp(opPos, 2, OpCodes.OP_OR);
      OrExpr();
      m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
        m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
    }
  }
  protected void AndExpr() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    EqualityExpr(-1);
    if ((null != m_token) && tokenIs("and"))
    {
      nextToken();
      insertOp(opPos, 2, OpCodes.OP_AND);
      AndExpr();
      m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
        m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
    }
  }
  protected int EqualityExpr(int addPos) throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    if (-1 == addPos)
      addPos = opPos;
    RelationalExpr(-1);
    if (null != m_token)
    {
      if (tokenIs('!') && lookahead('=', 1))
      {
        nextToken();
        nextToken();
        insertOp(addPos, 2, OpCodes.OP_NOTEQUALS);
        int opPlusLeftHandLen = m_ops.getOp(OpMap.MAPINDEX_LENGTH) - addPos;
        addPos = EqualityExpr(addPos);
        m_ops.setOp(addPos + OpMap.MAPINDEX_LENGTH,
          m_ops.getOp(addPos + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
        addPos += 2;
      }
      else if (tokenIs('='))
      {
        nextToken();
        insertOp(addPos, 2, OpCodes.OP_EQUALS);
        int opPlusLeftHandLen = m_ops.getOp(OpMap.MAPINDEX_LENGTH) - addPos;
        addPos = EqualityExpr(addPos);
        m_ops.setOp(addPos + OpMap.MAPINDEX_LENGTH,
          m_ops.getOp(addPos + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
        addPos += 2;
      }
    }
    return addPos;
  }
  protected int RelationalExpr(int addPos) throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    if (-1 == addPos)
      addPos = opPos;
    AdditiveExpr(-1);
    if (null != m_token)
    {
      if (tokenIs('<'))
      {
        nextToken();
        if (tokenIs('='))
        {
          nextToken();
          insertOp(addPos, 2, OpCodes.OP_LTE);
        }
        else
        {
          insertOp(addPos, 2, OpCodes.OP_LT);
        }
        int opPlusLeftHandLen = m_ops.getOp(OpMap.MAPINDEX_LENGTH) - addPos;
        addPos = RelationalExpr(addPos);
        m_ops.setOp(addPos + OpMap.MAPINDEX_LENGTH, 
          m_ops.getOp(addPos + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
        addPos += 2;
      }
      else if (tokenIs('>'))
      {
        nextToken();
        if (tokenIs('='))
        {
          nextToken();
          insertOp(addPos, 2, OpCodes.OP_GTE);
        }
        else
        {
          insertOp(addPos, 2, OpCodes.OP_GT);
        }
        int opPlusLeftHandLen = m_ops.getOp(OpMap.MAPINDEX_LENGTH) - addPos;
        addPos = RelationalExpr(addPos);
        m_ops.setOp(addPos + OpMap.MAPINDEX_LENGTH,
          m_ops.getOp(addPos + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
        addPos += 2;
      }
    }
    return addPos;
  }
  protected int AdditiveExpr(int addPos) throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    if (-1 == addPos)
      addPos = opPos;
    MultiplicativeExpr(-1);
    if (null != m_token)
    {
      if (tokenIs('+'))
      {
        nextToken();
        insertOp(addPos, 2, OpCodes.OP_PLUS);
        int opPlusLeftHandLen = m_ops.getOp(OpMap.MAPINDEX_LENGTH) - addPos;
        addPos = AdditiveExpr(addPos);
        m_ops.setOp(addPos + OpMap.MAPINDEX_LENGTH,
          m_ops.getOp(addPos + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
        addPos += 2;
      }
      else if (tokenIs('-'))
      {
        nextToken();
        insertOp(addPos, 2, OpCodes.OP_MINUS);
        int opPlusLeftHandLen = m_ops.getOp(OpMap.MAPINDEX_LENGTH) - addPos;
        addPos = AdditiveExpr(addPos);
        m_ops.setOp(addPos + OpMap.MAPINDEX_LENGTH, 
          m_ops.getOp(addPos + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
        addPos += 2;
      }
    }
    return addPos;
  }
  protected int MultiplicativeExpr(int addPos) throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    if (-1 == addPos)
      addPos = opPos;
    UnaryExpr();
    if (null != m_token)
    {
      if (tokenIs('*'))
      {
        nextToken();
        insertOp(addPos, 2, OpCodes.OP_MULT);
        int opPlusLeftHandLen = m_ops.getOp(OpMap.MAPINDEX_LENGTH) - addPos;
        addPos = MultiplicativeExpr(addPos);
        m_ops.setOp(addPos + OpMap.MAPINDEX_LENGTH,
          m_ops.getOp(addPos + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
        addPos += 2;
      }
      else if (tokenIs("div"))
      {
        nextToken();
        insertOp(addPos, 2, OpCodes.OP_DIV);
        int opPlusLeftHandLen = m_ops.getOp(OpMap.MAPINDEX_LENGTH) - addPos;
        addPos = MultiplicativeExpr(addPos);
        m_ops.setOp(addPos + OpMap.MAPINDEX_LENGTH,
          m_ops.getOp(addPos + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
        addPos += 2;
      }
      else if (tokenIs("mod"))
      {
        nextToken();
        insertOp(addPos, 2, OpCodes.OP_MOD);
        int opPlusLeftHandLen = m_ops.getOp(OpMap.MAPINDEX_LENGTH) - addPos;
        addPos = MultiplicativeExpr(addPos);
        m_ops.setOp(addPos + OpMap.MAPINDEX_LENGTH,
          m_ops.getOp(addPos + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
        addPos += 2;
      }
      else if (tokenIs("quo"))
      {
        nextToken();
        insertOp(addPos, 2, OpCodes.OP_QUO);
        int opPlusLeftHandLen = m_ops.getOp(OpMap.MAPINDEX_LENGTH) - addPos;
        addPos = MultiplicativeExpr(addPos);
        m_ops.setOp(addPos + OpMap.MAPINDEX_LENGTH,
          m_ops.getOp(addPos + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
        addPos += 2;
      }
    }
    return addPos;
  }
  protected void UnaryExpr() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    boolean isNeg = false;
    if (m_tokenChar == '-')
    {
      nextToken();
      appendOp(2, OpCodes.OP_NEG);
      isNeg = true;
    }
    UnionExpr();
    if (isNeg)
      m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
        m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
  }
  protected void StringExpr() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    appendOp(2, OpCodes.OP_STRING);
    Expr();
    m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
      m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
  }
  protected void BooleanExpr() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    appendOp(2, OpCodes.OP_BOOL);
    Expr();
    int opLen = m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos;
    if (opLen == 2)
    {
      error(XPATHErrorResources.ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL, null);  
    }
    m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH, opLen);
  }
  protected void NumberExpr() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    appendOp(2, OpCodes.OP_NUMBER);
    Expr();
    m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
      m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
  }
  protected void UnionExpr() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    boolean continueOrLoop = true;
    boolean foundUnion = false;
    do
    {
      PathExpr();
      if (tokenIs('|'))
      {
        if (false == foundUnion)
        {
          foundUnion = true;
          insertOp(opPos, 2, OpCodes.OP_UNION);
        }
        nextToken();
      }
      else
      {
        break;
      }
    }
    while (continueOrLoop);
    m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
          m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
  }
  protected void PathExpr() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    int filterExprMatch = FilterExpr();
    if (filterExprMatch != FILTER_MATCH_FAILED)
    {
      boolean locationPathStarted = (filterExprMatch==FILTER_MATCH_PREDICATES);
      if (tokenIs('/'))
      {
        nextToken();
        if (!locationPathStarted)
        {
          insertOp(opPos, 2, OpCodes.OP_LOCATIONPATH);
          locationPathStarted = true;
        }
        if (!RelativeLocationPath())
        {
          error(XPATHErrorResources.ER_EXPECTED_REL_LOC_PATH, null);
        }
      }
      if (locationPathStarted)
      {
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), OpCodes.ENDOP);
        m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
        m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
          m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
      }
    }
    else
    {
      LocationPath();
    }
  }
  protected int FilterExpr() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    int filterMatch;
    if (PrimaryExpr())
    {
      if (tokenIs('['))
      {
        insertOp(opPos, 2, OpCodes.OP_LOCATIONPATH);
        while (tokenIs('['))
        {
          Predicate();
        }
        filterMatch = FILTER_MATCH_PREDICATES;
      }
      else
      {
        filterMatch = FILTER_MATCH_PRIMARY;
      }
    }
    else
    {
      filterMatch = FILTER_MATCH_FAILED;
    }
    return filterMatch;
  }
  protected boolean PrimaryExpr() throws javax.xml.transform.TransformerException
  {
    boolean matchFound;
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    if ((m_tokenChar == '\'') || (m_tokenChar == '"'))
    {
      appendOp(2, OpCodes.OP_LITERAL);
      Literal();
      m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH, 
        m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
      matchFound = true;
    }
    else if (m_tokenChar == '$')
    {
      nextToken();  
      appendOp(2, OpCodes.OP_VARIABLE);
      QName();
      m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
        m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
      matchFound = true;
    }
    else if (m_tokenChar == '(')
    {
      nextToken();
      appendOp(2, OpCodes.OP_GROUP);
      Expr();
      consumeExpected(')');
      m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
        m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
      matchFound = true;
    }
    else if ((null != m_token) && ((('.' == m_tokenChar) && (m_token.length() > 1) && Character.isDigit(
            m_token.charAt(1))) || Character.isDigit(m_tokenChar)))
    {
      appendOp(2, OpCodes.OP_NUMBERLIT);
      Number();
      m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
        m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
      matchFound = true;
    }
    else if (lookahead('(', 1) || (lookahead(':', 1) && lookahead('(', 3)))
    {
      matchFound = FunctionCall();
    }
    else
    {
      matchFound = false;
    }
    return matchFound;
  }
  protected void Argument() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    appendOp(2, OpCodes.OP_ARGUMENT);
    Expr();
    m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
      m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
  }
  protected boolean FunctionCall() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    if (lookahead(':', 1))
    {
      appendOp(4, OpCodes.OP_EXTFUNCTION);
      m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH + 1, m_queueMark - 1);
      nextToken();
      consumeExpected(':');
      m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH + 2, m_queueMark - 1);
      nextToken();
    }
    else
    {
      int funcTok = getFunctionToken(m_token);
      if (-1 == funcTok)
      {
        error(XPATHErrorResources.ER_COULDNOT_FIND_FUNCTION,
              new Object[]{ m_token });  
      }
      switch (funcTok)
      {
      case OpCodes.NODETYPE_PI :
      case OpCodes.NODETYPE_COMMENT :
      case OpCodes.NODETYPE_TEXT :
      case OpCodes.NODETYPE_NODE :
        return false;
      default :
        appendOp(3, OpCodes.OP_FUNCTION);
        m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH + 1, funcTok);
      }
      nextToken();
    }
    consumeExpected('(');
    while (!tokenIs(')') && m_token != null)
    {
      if (tokenIs(','))
      {
        error(XPATHErrorResources.ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG, null);  
      }
      Argument();
      if (!tokenIs(')'))
      {
        consumeExpected(',');
        if (tokenIs(')'))
        {
          error(XPATHErrorResources.ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG,
                null);  
        }
      }
    }
    consumeExpected(')');
    m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), OpCodes.ENDOP);
    m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
    m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH, 
      m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
    return true;
  }
  protected void LocationPath() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    appendOp(2, OpCodes.OP_LOCATIONPATH);
    boolean seenSlash = tokenIs('/');
    if (seenSlash)
    {
      appendOp(4, OpCodes.FROM_ROOT);
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH) - 2, 4);
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH) - 1, OpCodes.NODETYPE_ROOT);
      nextToken();
    } else if (m_token == null) {
      error(XPATHErrorResources.ER_EXPECTED_LOC_PATH_AT_END_EXPR, null);
    }
    if (m_token != null)
    {
      if (!RelativeLocationPath() && !seenSlash)
      {
        error(XPATHErrorResources.ER_EXPECTED_LOC_PATH, 
              new Object [] {m_token});
      }
    }
    m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), OpCodes.ENDOP);
    m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
    m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
      m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
  }
  protected boolean RelativeLocationPath()
               throws javax.xml.transform.TransformerException
  {
    if (!Step())
    {
      return false;
    }
    while (tokenIs('/'))
    {
      nextToken();
      if (!Step())
      {
        error(XPATHErrorResources.ER_EXPECTED_LOC_STEP, null);
      }
    }
    return true;
  }
  protected boolean Step() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    boolean doubleSlash = tokenIs('/');
    if (doubleSlash)
    {
      nextToken();
      appendOp(2, OpCodes.FROM_DESCENDANTS_OR_SELF);
      m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), OpCodes.NODETYPE_NODE);
      m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
      m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH + 1,
          m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
      m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
          m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
      opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    }
    if (tokenIs("."))
    {
      nextToken();
      if (tokenIs('['))
      {
        error(XPATHErrorResources.ER_PREDICATE_ILLEGAL_SYNTAX, null);  
      }
      appendOp(4, OpCodes.FROM_SELF);
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH) - 2,4);
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH) - 1, OpCodes.NODETYPE_NODE);
    }
    else if (tokenIs(".."))
    {
      nextToken();
      appendOp(4, OpCodes.FROM_PARENT);
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH) - 2,4);
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH) - 1, OpCodes.NODETYPE_NODE);
    }
    else if (tokenIs('*') || tokenIs('@') || tokenIs('_')
             || (m_token!= null && Character.isLetter(m_token.charAt(0))))
    {
      Basis();
      while (tokenIs('['))
      {
        Predicate();
      }
      m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
        m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos); 
    }
    else
    {
      if (doubleSlash)
      {
        error(XPATHErrorResources.ER_EXPECTED_LOC_STEP, null);
      }
      return false;
    }
    return true;
  }
  protected void Basis() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    int axesType;
    if (lookahead("::", 1))
    {
      axesType = AxisName();
      nextToken();
      nextToken();
    }
    else if (tokenIs('@'))
    {
      axesType = OpCodes.FROM_ATTRIBUTES;
      appendOp(2, axesType);
      nextToken();
    }
    else
    {
      axesType = OpCodes.FROM_CHILDREN;
      appendOp(2, axesType);
    }
    m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
    NodeTest(axesType);
    m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH + 1,
      m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
   }
  protected int AxisName() throws javax.xml.transform.TransformerException
  {
    Object val = Keywords.getAxisName(m_token);
    if (null == val)
    {
      error(XPATHErrorResources.ER_ILLEGAL_AXIS_NAME,
            new Object[]{ m_token });  
    }
    int axesType = ((Integer) val).intValue();
    appendOp(2, axesType);
    return axesType;
  }
  protected void NodeTest(int axesType) throws javax.xml.transform.TransformerException
  {
    if (lookahead('(', 1))
    {
      Object nodeTestOp = Keywords.getNodeType(m_token);
      if (null == nodeTestOp)
      {
        error(XPATHErrorResources.ER_UNKNOWN_NODETYPE,
              new Object[]{ m_token });  
      }
      else
      {
        nextToken();
        int nt = ((Integer) nodeTestOp).intValue();
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), nt);
        m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
        consumeExpected('(');
        if (OpCodes.NODETYPE_PI == nt)
        {
          if (!tokenIs(')'))
          {
            Literal();
          }
        }
        consumeExpected(')');
      }
    }
    else
    {
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), OpCodes.NODENAME);
      m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
      if (lookahead(':', 1))
      {
        if (tokenIs('*'))
        {
          m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), OpCodes.ELEMWILDCARD);
        }
        else
        {
          m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), m_queueMark - 1);
          if (!Character.isLetter(m_tokenChar) && !tokenIs('_'))
          {
            error(XPATHErrorResources.ER_EXPECTED_NODE_TEST, null);
          }
        }
        nextToken();
        consumeExpected(':');
      }
      else
      {
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), OpCodes.EMPTY);
      }
      m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
      if (tokenIs('*'))
      {
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), OpCodes.ELEMWILDCARD);
      }
      else
      {
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), m_queueMark - 1);
        if (!Character.isLetter(m_tokenChar) && !tokenIs('_'))
        {
          error(XPATHErrorResources.ER_EXPECTED_NODE_TEST, null);
        }
      }
      m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
      nextToken();
    }
  }
  protected void Predicate() throws javax.xml.transform.TransformerException
  {
    if (tokenIs('['))
    {
      nextToken();
      PredicateExpr();
      consumeExpected(']');
    }
  }
  protected void PredicateExpr() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    appendOp(2, OpCodes.OP_PREDICATE);
    Expr();
    m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), OpCodes.ENDOP);
    m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
    m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
      m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
  }
  protected void QName() throws javax.xml.transform.TransformerException
  {
    if(lookahead(':', 1))
    {
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), m_queueMark - 1);
      m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
      nextToken();
      consumeExpected(':');
    }
    else
    {
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), OpCodes.EMPTY);
      m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
    }
    m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), m_queueMark - 1);
    m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
    nextToken();
  }
  protected void NCName()
  {
    m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), m_queueMark - 1);
    m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
    nextToken();
  }
  protected void Literal() throws javax.xml.transform.TransformerException
  {
    int last = m_token.length() - 1;
    char c0 = m_tokenChar;
    char cX = m_token.charAt(last);
    if (((c0 == '\"') && (cX == '\"')) || ((c0 == '\'') && (cX == '\'')))
    {
      int tokenQueuePos = m_queueMark - 1;
      m_ops.m_tokenQueue.setElementAt(null,tokenQueuePos);
      Object obj = new XString(m_token.substring(1, last));
      m_ops.m_tokenQueue.setElementAt(obj,tokenQueuePos);
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), tokenQueuePos);
      m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
      nextToken();
    }
    else
    {
      error(XPATHErrorResources.ER_PATTERN_LITERAL_NEEDS_BE_QUOTED,
            new Object[]{ m_token });  
    }
  }
  protected void Number() throws javax.xml.transform.TransformerException
  {
    if (null != m_token)
    {
      double num;
      try
      {
      	if ((m_token.indexOf('e') > -1)||(m_token.indexOf('E') > -1))
      		throw new NumberFormatException();
        num = Double.valueOf(m_token).doubleValue();
      }
      catch (NumberFormatException nfe)
      {
        num = 0.0;  
        error(XPATHErrorResources.ER_COULDNOT_BE_FORMATTED_TO_NUMBER,
              new Object[]{ m_token });  
      }
      m_ops.m_tokenQueue.setElementAt(new XNumber(num),m_queueMark - 1);
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), m_queueMark - 1);
      m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
      nextToken();
    }
  }
  protected void Pattern() throws javax.xml.transform.TransformerException
  {
    while (true)
    {
      LocationPathPattern();
      if (tokenIs('|'))
      {
        nextToken();
      }
      else
      {
        break;
      }
    }
  }
  protected void LocationPathPattern() throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    final int RELATIVE_PATH_NOT_PERMITTED = 0;
    final int RELATIVE_PATH_PERMITTED     = 1;
    final int RELATIVE_PATH_REQUIRED      = 2;
    int relativePathStatus = RELATIVE_PATH_NOT_PERMITTED;
    appendOp(2, OpCodes.OP_LOCATIONPATHPATTERN);
    if (lookahead('(', 1)
            && (tokenIs(Keywords.FUNC_ID_STRING)
                || tokenIs(Keywords.FUNC_KEY_STRING)))
    {
      IdKeyPattern();
      if (tokenIs('/'))
      {
        nextToken();
        if (tokenIs('/'))
        {
          appendOp(4, OpCodes.MATCH_ANY_ANCESTOR);
          nextToken();
        }
        else
        {
          appendOp(4, OpCodes.MATCH_IMMEDIATE_ANCESTOR);
        }
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH) - 2, 4);
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH) - 1, OpCodes.NODETYPE_FUNCTEST);
        relativePathStatus = RELATIVE_PATH_REQUIRED;
      }
    }
    else if (tokenIs('/'))
    {
      if (lookahead('/', 1))
      {
        appendOp(4, OpCodes.MATCH_ANY_ANCESTOR);
        nextToken();
        relativePathStatus = RELATIVE_PATH_REQUIRED;
      }
      else
      {
        appendOp(4, OpCodes.FROM_ROOT);
        relativePathStatus = RELATIVE_PATH_PERMITTED;
      }
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH) - 2, 4);
      m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH) - 1, OpCodes.NODETYPE_ROOT);
      nextToken();
    }
    else
    {
      relativePathStatus = RELATIVE_PATH_REQUIRED;
    }
    if (relativePathStatus != RELATIVE_PATH_NOT_PERMITTED)
    {
      if (!tokenIs('|') && (null != m_token))
      {
        RelativePathPattern();
      }
      else if (relativePathStatus == RELATIVE_PATH_REQUIRED)
      {
        error(XPATHErrorResources.ER_EXPECTED_REL_PATH_PATTERN, null);
      }
    }
    m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH), OpCodes.ENDOP);
    m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
    m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
      m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
  }
  protected void IdKeyPattern() throws javax.xml.transform.TransformerException
  {
    FunctionCall();
  }
  protected void RelativePathPattern()
              throws javax.xml.transform.TransformerException
  {
    boolean trailingSlashConsumed = StepPattern(false);
    while (tokenIs('/'))
    {
      nextToken();
      trailingSlashConsumed = StepPattern(!trailingSlashConsumed);
    }
  }
  protected boolean StepPattern(boolean isLeadingSlashPermitted)
            throws javax.xml.transform.TransformerException
  {
    return AbbreviatedNodeTestStep(isLeadingSlashPermitted);
  }
  protected boolean AbbreviatedNodeTestStep(boolean isLeadingSlashPermitted)
            throws javax.xml.transform.TransformerException
  {
    int opPos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
    int axesType;
    int matchTypePos = -1;
    if (tokenIs('@'))
    {
      axesType = OpCodes.MATCH_ATTRIBUTE;
      appendOp(2, axesType);
      nextToken();
    }
    else if (this.lookahead("::", 1))
    {
      if (tokenIs("attribute"))
      {
        axesType = OpCodes.MATCH_ATTRIBUTE;
        appendOp(2, axesType);
      }
      else if (tokenIs("child"))
      {
        matchTypePos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        axesType = OpCodes.MATCH_IMMEDIATE_ANCESTOR;
        appendOp(2, axesType);
      }
      else
      {
        axesType = -1;
        this.error(XPATHErrorResources.ER_AXES_NOT_ALLOWED,
                   new Object[]{ this.m_token });
      }
      nextToken();
      nextToken();
    }
    else if (tokenIs('/'))
    {
      if (!isLeadingSlashPermitted)
      {
        error(XPATHErrorResources.ER_EXPECTED_STEP_PATTERN, null);
      }
      axesType = OpCodes.MATCH_ANY_ANCESTOR;
      appendOp(2, axesType);
      nextToken();
    }
    else
    {
      matchTypePos = m_ops.getOp(OpMap.MAPINDEX_LENGTH);
      axesType = OpCodes.MATCH_IMMEDIATE_ANCESTOR;
      appendOp(2, axesType);
    }
    m_ops.setOp(OpMap.MAPINDEX_LENGTH, m_ops.getOp(OpMap.MAPINDEX_LENGTH) + 1);
    NodeTest(axesType);
    m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH + 1,
      m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
    while (tokenIs('['))
    {
      Predicate();
    }
    boolean trailingSlashConsumed;
    if ((matchTypePos > -1) && tokenIs('/') && lookahead('/', 1))
    {
      m_ops.setOp(matchTypePos, OpCodes.MATCH_ANY_ANCESTOR);
      nextToken();
      trailingSlashConsumed = true;
    }
    else
    {
      trailingSlashConsumed = false;
    }
    m_ops.setOp(opPos + OpMap.MAPINDEX_LENGTH,
      m_ops.getOp(OpMap.MAPINDEX_LENGTH) - opPos);
    return trailingSlashConsumed;
  }
}
