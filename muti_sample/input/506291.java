public class XalanTransformState
    implements TransformState {
    Node m_node = null;
    ElemTemplateElement m_currentElement = null;
    ElemTemplate m_currentTemplate = null;
    ElemTemplate m_matchedTemplate = null;
    int m_currentNodeHandle = DTM.NULL;
    Node m_currentNode = null;
    int m_matchedNode = DTM.NULL;
    DTMIterator m_contextNodeList = null;
    boolean m_elemPending = false;    
    TransformerImpl m_transformer = null;
    public void setCurrentNode(Node n) {
        m_node = n;
    }
    public void resetState(Transformer transformer) {
        if ((transformer != null) && (transformer instanceof TransformerImpl)) {
           m_transformer = (TransformerImpl)transformer;
           m_currentElement = m_transformer.getCurrentElement();
           m_currentTemplate = m_transformer.getCurrentTemplate();
           m_matchedTemplate = m_transformer.getMatchedTemplate();
           int currentNodeHandle = m_transformer.getCurrentNode();
           DTM dtm = m_transformer.getXPathContext().getDTM(currentNodeHandle);
           m_currentNode = dtm.getNode(currentNodeHandle);
           m_matchedNode = m_transformer.getMatchedNode();
           m_contextNodeList = m_transformer.getContextNodeList();    
        }       
    }
    public ElemTemplateElement getCurrentElement() {
      if (m_elemPending)
         return m_currentElement;
      else
         return m_transformer.getCurrentElement();
    }
    public Node getCurrentNode() {
      if (m_currentNode != null) {
         return m_currentNode;
      } else {
         DTM dtm = m_transformer.getXPathContext().getDTM(m_transformer.getCurrentNode());
         return dtm.getNode(m_transformer.getCurrentNode());
      }
    }
    public ElemTemplate getCurrentTemplate() {
       if (m_elemPending)
         return m_currentTemplate;
       else
         return m_transformer.getCurrentTemplate();
    }
    public ElemTemplate getMatchedTemplate() {
      if (m_elemPending)
         return m_matchedTemplate;
      else
         return m_transformer.getMatchedTemplate();
    }
    public Node getMatchedNode() {
       if (m_elemPending) {
         DTM dtm = m_transformer.getXPathContext().getDTM(m_matchedNode);
         return dtm.getNode(m_matchedNode);
       } else {
         DTM dtm = m_transformer.getXPathContext().getDTM(m_transformer.getMatchedNode());
         return dtm.getNode(m_transformer.getMatchedNode());
       }
    }
    public NodeIterator getContextNodeList() {
      if (m_elemPending) {
          return new org.apache.xml.dtm.ref.DTMNodeIterator(m_contextNodeList);
      } else {
          return new org.apache.xml.dtm.ref.DTMNodeIterator(m_transformer.getContextNodeList());
      }
    }
    public Transformer getTransformer() {
        return m_transformer;
    }
}
