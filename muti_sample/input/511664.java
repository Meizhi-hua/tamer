public class ContextMatchStepPattern extends StepPattern
{
    static final long serialVersionUID = -1888092779313211942L;
  public ContextMatchStepPattern(int axis, int paxis)
  {
    super(DTMFilter.SHOW_ALL, axis, paxis);
  }
  public XObject execute(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    if (xctxt.getIteratorRoot() == xctxt.getCurrentNode())
      return getStaticScore();
    else
      return this.SCORE_NONE;
  }
  public XObject executeRelativePathPattern(
          XPathContext xctxt, StepPattern prevStep)
            throws javax.xml.transform.TransformerException
  {
    XObject score = NodeTest.SCORE_NONE;
    int context = xctxt.getCurrentNode();
    DTM dtm = xctxt.getDTM(context);
    if (null != dtm)
    {
      int predContext = xctxt.getCurrentNode();
      DTMAxisTraverser traverser;
      int axis = m_axis;
      boolean needToTraverseAttrs = WalkerFactory.isDownwardAxisOfMany(axis);
      boolean iterRootIsAttr = (dtm.getNodeType(xctxt.getIteratorRoot()) 
                                 == DTM.ATTRIBUTE_NODE);
      if((Axis.PRECEDING == axis) && iterRootIsAttr)
      {
        axis = Axis.PRECEDINGANDANCESTOR;
      }
      traverser = dtm.getAxisTraverser(axis);
      for (int relative = traverser.first(context); DTM.NULL != relative;
              relative = traverser.next(context, relative))
      {
        try
        {
          xctxt.pushCurrentNode(relative);
          score = execute(xctxt);
          if (score != NodeTest.SCORE_NONE)
          {
	      if (executePredicates(xctxt, dtm, context))
		  return score;
	      score = NodeTest.SCORE_NONE;
          }
          if(needToTraverseAttrs && iterRootIsAttr
             && (DTM.ELEMENT_NODE == dtm.getNodeType(relative)))
          {
            int xaxis = Axis.ATTRIBUTE;
            for (int i = 0; i < 2; i++) 
            {            
              DTMAxisTraverser atraverser = dtm.getAxisTraverser(xaxis);
              for (int arelative = atraverser.first(relative); 
                      DTM.NULL != arelative;
                      arelative = atraverser.next(relative, arelative))
              {
                try
                {
                  xctxt.pushCurrentNode(arelative);
                  score = execute(xctxt);
                  if (score != NodeTest.SCORE_NONE)
                  {
                    if (score != NodeTest.SCORE_NONE)
                      return score;
                  }
                }
                finally
                {
                  xctxt.popCurrentNode();
                }
              }
              xaxis = Axis.NAMESPACE;
            }
          }
        }
        finally
        {
          xctxt.popCurrentNode();
        }
      }
    }
    return score;
  }
}
