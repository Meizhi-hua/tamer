public class OpMap
{
  protected String m_currentPattern;
  public String toString()
  {
    return m_currentPattern;
  }
  public String getPatternString()
  {
    return m_currentPattern;
  }
  static final int MAXTOKENQUEUESIZE = 500;
  static final int BLOCKTOKENQUEUESIZE = 500;
  ObjectVector m_tokenQueue = new ObjectVector(MAXTOKENQUEUESIZE, BLOCKTOKENQUEUESIZE);
  public ObjectVector getTokenQueue()
  {
    return m_tokenQueue;
  }
  public Object getToken(int pos)
  {
    return m_tokenQueue.elementAt(pos);
  }
  public int getTokenQueueSize()
  {
    return m_tokenQueue.size();
  }
  OpMapVector m_opMap = null;
  public OpMapVector getOpMap()
  {
    return m_opMap;
  }
  public static final int MAPINDEX_LENGTH = 1;
  void shrink()
  {
    int n = m_opMap.elementAt(MAPINDEX_LENGTH);
    m_opMap.setToSize(n + 4);
    m_opMap.setElementAt(0,n);
    m_opMap.setElementAt(0,n+1);
    m_opMap.setElementAt(0,n+2);
    n = m_tokenQueue.size();
    m_tokenQueue.setToSize(n + 4);
    m_tokenQueue.setElementAt(null,n);
    m_tokenQueue.setElementAt(null,n + 1);
    m_tokenQueue.setElementAt(null,n + 2);
  }
  public int getOp(int opPos)
  {
    return m_opMap.elementAt(opPos);
  }
  public void setOp(int opPos, int value)
  {
     m_opMap.setElementAt(value,opPos);
  }
  public int getNextOpPos(int opPos)
  {
    return opPos + m_opMap.elementAt(opPos + 1);
  }
  public int getNextStepPos(int opPos)
  {
    int stepType = getOp(opPos);
    if ((stepType >= OpCodes.AXES_START_TYPES)
            && (stepType <= OpCodes.AXES_END_TYPES))
    {
      return getNextOpPos(opPos);
    }
    else if ((stepType >= OpCodes.FIRST_NODESET_OP)
             && (stepType <= OpCodes.LAST_NODESET_OP))
    {
      int newOpPos = getNextOpPos(opPos);
      while (OpCodes.OP_PREDICATE == getOp(newOpPos))
      {
        newOpPos = getNextOpPos(newOpPos);
      }
      stepType = getOp(newOpPos);
      if (!((stepType >= OpCodes.AXES_START_TYPES)
            && (stepType <= OpCodes.AXES_END_TYPES)))
      {
        return OpCodes.ENDOP;
      }
      return newOpPos;
    }
    else
    {
      throw new RuntimeException(
        XSLMessages.createXPATHMessage(XPATHErrorResources.ER_UNKNOWN_STEP, new Object[]{String.valueOf(stepType)})); 
    }
  }
  public static int getNextOpPos(int[] opMap, int opPos)
  {
    return opPos + opMap[opPos + 1];
  }
  public int getFirstPredicateOpPos(int opPos)
     throws javax.xml.transform.TransformerException
  {
    int stepType = m_opMap.elementAt(opPos);
    if ((stepType >= OpCodes.AXES_START_TYPES)
            && (stepType <= OpCodes.AXES_END_TYPES))
    {
      return opPos + m_opMap.elementAt(opPos + 2);
    }
    else if ((stepType >= OpCodes.FIRST_NODESET_OP)
             && (stepType <= OpCodes.LAST_NODESET_OP))
    {
      return opPos + m_opMap.elementAt(opPos + 1);
    }
    else if(-2 == stepType)
    {
      return -2;
    }
    else
    {
      error(org.apache.xpath.res.XPATHErrorResources.ER_UNKNOWN_OPCODE,
            new Object[]{ String.valueOf(stepType) });  
      return -1;
    }
  }
  public void error(String msg, Object[] args) throws javax.xml.transform.TransformerException
  {
    java.lang.String fmsg = org.apache.xalan.res.XSLMessages.createXPATHMessage(msg, args);
    throw new javax.xml.transform.TransformerException(fmsg);
  }
  public static int getFirstChildPos(int opPos)
  {
    return opPos + 2;
  }
  public int getArgLength(int opPos)
  {
    return m_opMap.elementAt(opPos + MAPINDEX_LENGTH);
  }
  public int getArgLengthOfStep(int opPos)
  {
    return m_opMap.elementAt(opPos + MAPINDEX_LENGTH + 1) - 3;
  }
  public static int getFirstChildPosOfStep(int opPos)
  {
    return opPos + 3;
  }
  public int getStepTestType(int opPosOfStep)
  {
    return m_opMap.elementAt(opPosOfStep + 3);  
  }
  public String getStepNS(int opPosOfStep)
  {
    int argLenOfStep = getArgLengthOfStep(opPosOfStep);
    if (argLenOfStep == 3)
    {
      int index = m_opMap.elementAt(opPosOfStep + 4);
      if (index >= 0)
        return (String) m_tokenQueue.elementAt(index);
      else if (OpCodes.ELEMWILDCARD == index)
        return NodeTest.WILD;
      else
        return null;
    }
    else
      return null;
  }
  public String getStepLocalName(int opPosOfStep)
  {
    int argLenOfStep = getArgLengthOfStep(opPosOfStep);
    int index;
    switch (argLenOfStep)
    {
    case 0 :
      index = OpCodes.EMPTY;
      break;
    case 1 :
      index = OpCodes.ELEMWILDCARD;
      break;
    case 2 :
      index = m_opMap.elementAt(opPosOfStep + 4);
      break;
    case 3 :
      index = m_opMap.elementAt(opPosOfStep + 5);
      break;
    default :
      index = OpCodes.EMPTY;
      break;  
    }
    if (index >= 0)
      return (String) m_tokenQueue.elementAt(index).toString();
    else if (OpCodes.ELEMWILDCARD == index)
      return NodeTest.WILD;
    else
      return null;
  }
}
