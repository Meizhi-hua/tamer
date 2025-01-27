public class ClonerToResultTree
{
  public static void cloneToResultTree(int node, int nodeType, DTM dtm, 
                                             SerializationHandler rth,
                                             boolean shouldCloneAttributes)
    throws TransformerException
  {
    try
    {
      switch (nodeType)
      {
      case DTM.TEXT_NODE :
        dtm.dispatchCharactersEvents(node, rth, false);
        break;
      case DTM.DOCUMENT_FRAGMENT_NODE :
      case DTM.DOCUMENT_NODE :
        break;
      case DTM.ELEMENT_NODE :
        {
          String ns = dtm.getNamespaceURI(node);
          if (ns==null) ns="";
          String localName = dtm.getLocalName(node);
          rth.startElement(ns, localName, dtm.getNodeNameX(node));
          if (shouldCloneAttributes)
          {
            SerializerUtils.addAttributes(rth, node);
            SerializerUtils.processNSDecls(rth, node, nodeType, dtm);
          }
        }
        break;
      case DTM.CDATA_SECTION_NODE :
        rth.startCDATA();          
        dtm.dispatchCharactersEvents(node, rth, false);
        rth.endCDATA();
        break;
      case DTM.ATTRIBUTE_NODE :
        SerializerUtils.addAttribute(rth, node);
        break;
			case DTM.NAMESPACE_NODE:
  			    SerializerUtils.processNSDecls(rth,node,DTM.NAMESPACE_NODE,dtm);
				break;
      case DTM.COMMENT_NODE :
        XMLString xstr = dtm.getStringValue (node);
        xstr.dispatchAsComment(rth);
        break;
      case DTM.ENTITY_REFERENCE_NODE :
        rth.entityReference(dtm.getNodeNameX(node));
        break;
      case DTM.PROCESSING_INSTRUCTION_NODE :
        {
          rth.processingInstruction(dtm.getNodeNameX(node), 
                                      dtm.getNodeValue(node));
        }
        break;
      default :
        throw new  TransformerException(
                         "Can't clone node: "+dtm.getNodeName(node));
      }
    }
    catch(org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
  }  
}
