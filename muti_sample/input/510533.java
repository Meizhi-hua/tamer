public class FuncUnparsedEntityURI extends FunctionOneArg
{
    static final long serialVersionUID = 845309759097448178L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    String name = m_arg0.execute(xctxt).str();
    int context = xctxt.getCurrentNode();
    DTM dtm = xctxt.getDTM(context);
    int doc = dtm.getDocument();
    String uri = dtm.getUnparsedEntityURI(name);
    return new XString(uri);
  }
}
