public class NSInfo
{
  public NSInfo(boolean hasProcessedNS, boolean hasXMLNSAttrs)
  {
    m_hasProcessedNS = hasProcessedNS;
    m_hasXMLNSAttrs = hasXMLNSAttrs;
    m_namespace = null;
    m_ancestorHasXMLNSAttrs = ANCESTORXMLNSUNPROCESSED;
  }
  public NSInfo(boolean hasProcessedNS, boolean hasXMLNSAttrs,
                int ancestorHasXMLNSAttrs)
  {
    m_hasProcessedNS = hasProcessedNS;
    m_hasXMLNSAttrs = hasXMLNSAttrs;
    m_ancestorHasXMLNSAttrs = ancestorHasXMLNSAttrs;
    m_namespace = null;
  }
  public NSInfo(String namespace, boolean hasXMLNSAttrs)
  {
    m_hasProcessedNS = true;
    m_hasXMLNSAttrs = hasXMLNSAttrs;
    m_namespace = namespace;
    m_ancestorHasXMLNSAttrs = ANCESTORXMLNSUNPROCESSED;
  }
  public String m_namespace;
  public boolean m_hasXMLNSAttrs;
  public boolean m_hasProcessedNS;
  public int m_ancestorHasXMLNSAttrs;
  public static final int ANCESTORXMLNSUNPROCESSED = 0;
  public static final int ANCESTORHASXMLNS = 1;
  public static final int ANCESTORNOXMLNS = 2;
}
