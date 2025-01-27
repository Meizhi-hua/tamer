public class AttList implements Attributes
{
  NamedNodeMap m_attrs;
  int m_lastIndex;
  DOMHelper m_dh;
  public AttList(NamedNodeMap attrs, DOMHelper dh)
  {
    m_attrs = attrs;
    m_lastIndex = m_attrs.getLength() - 1;
    m_dh = dh;
  }
  public int getLength()
  {
    return m_attrs.getLength();
  }
  public String getURI(int index)
  {
    String ns = m_dh.getNamespaceOfNode(((Attr) m_attrs.item(index)));
    if(null == ns)
      ns = "";
    return ns;
  }
  public String getLocalName(int index)
  {
    return m_dh.getLocalNameOfNode(((Attr) m_attrs.item(index)));
  }
  public String getQName(int i)
  {
    return ((Attr) m_attrs.item(i)).getName();
  }
  public String getType(int i)
  {
    return "CDATA";  
  }
  public String getValue(int i)
  {
    return ((Attr) m_attrs.item(i)).getValue();
  }
  public String getType(String name)
  {
    return "CDATA";  
  }
  public String getType(String uri, String localName)
  {
    return "CDATA";  
  }
  public String getValue(String name)
  {
    Attr attr = ((Attr) m_attrs.getNamedItem(name));
    return (null != attr) 
          ? attr.getValue() : null;
  }
  public String getValue(String uri, String localName)
  {
		Node a=m_attrs.getNamedItemNS(uri,localName);
		return (a==null) ? null : a.getNodeValue();
  }
  public int getIndex(String uri, String localPart)
  {
    for(int i=m_attrs.getLength()-1;i>=0;--i)
    {
      Node a=m_attrs.item(i);
      String u=a.getNamespaceURI();
      if( (u==null ? uri==null : u.equals(uri))
	  &&
	  a.getLocalName().equals(localPart) )
	return i;
    }
    return -1;
  }
  public int getIndex(String qName)
  {
    for(int i=m_attrs.getLength()-1;i>=0;--i)
    {
      Node a=m_attrs.item(i);
      if(a.getNodeName().equals(qName) )
	return i;
    }
    return -1;
  }
}
