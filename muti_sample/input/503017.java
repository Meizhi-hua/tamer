public class DTMManagerDefault extends DTMManager
{
  private static final boolean DUMPTREE = false;
  private static final boolean DEBUG = false;
  protected DTM m_dtms[] = new DTM[256];
  int m_dtm_offsets[] = new int[256];
  protected XMLReaderManager m_readerManager = null;
  protected DefaultHandler m_defaultHandler = new DefaultHandler();
  synchronized public void addDTM(DTM dtm, int id) {	addDTM(dtm,id,0); }
  synchronized public void addDTM(DTM dtm, int id, int offset)
  {
		if(id>=IDENT_MAX_DTMS)
		{
	    throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_NO_DTMIDS_AVAIL, null)); 
		}
		int oldlen=m_dtms.length;
		if(oldlen<=id)
		{
			int newlen=Math.min((id+256),IDENT_MAX_DTMS);
			DTM new_m_dtms[] = new DTM[newlen];
			System.arraycopy(m_dtms,0,new_m_dtms,0,oldlen);
			m_dtms=new_m_dtms;
			int new_m_dtm_offsets[] = new int[newlen];
			System.arraycopy(m_dtm_offsets,0,new_m_dtm_offsets,0,oldlen);
			m_dtm_offsets=new_m_dtm_offsets;
		}
    m_dtms[id] = dtm;
		m_dtm_offsets[id]=offset;
    dtm.documentRegistration();
  }
  synchronized public int getFirstFreeDTMID()
  {
    int n = m_dtms.length;
    for (int i = 1; i < n; i++)
    {
      if(null == m_dtms[i])
      {
        return i;
      }
    }
		return n; 
  }
  private ExpandedNameTable m_expandedNameTable =
    new ExpandedNameTable();
  public DTMManagerDefault(){}
  synchronized public DTM getDTM(Source source, boolean unique,
                                 DTMWSFilter whiteSpaceFilter,
                                 boolean incremental, boolean doIndexing)
  {
    if(DEBUG && null != source)
      System.out.println("Starting "+
                         (unique ? "UNIQUE" : "shared")+
                         " source: "+source.getSystemId()
                         );
    XMLStringFactory xstringFactory = m_xsf;
    int dtmPos = getFirstFreeDTMID();
    int documentID = dtmPos << IDENT_DTM_NODE_BITS;
    if ((null != source) && source instanceof DOMSource)
    {
      DOM2DTM dtm = new DOM2DTM(this, (DOMSource) source, documentID,
                                whiteSpaceFilter, xstringFactory, doIndexing);
      addDTM(dtm, dtmPos, 0);
      return dtm;
    }
    else
    {
      boolean isSAXSource = (null != source)
        ? (source instanceof SAXSource) : true;
      boolean isStreamSource = (null != source)
        ? (source instanceof StreamSource) : false;
      if (isSAXSource || isStreamSource) {
        XMLReader reader = null;
        SAX2DTM dtm;
        try {
          InputSource xmlSource;
          if (null == source) {
            xmlSource = null;
          } else {
            reader = getXMLReader(source);
            xmlSource = SAXSource.sourceToInputSource(source);
            String urlOfSource = xmlSource.getSystemId();
            if (null != urlOfSource) {
              try {
                urlOfSource = SystemIDResolver.getAbsoluteURI(urlOfSource);
              } catch (Exception e) {
                System.err.println("Can not absolutize URL: " + urlOfSource);
              }
              xmlSource.setSystemId(urlOfSource);
            }
          }
          if (source==null && unique && !incremental && !doIndexing) {
            dtm = new SAX2RTFDTM(this, source, documentID, whiteSpaceFilter,
                                 xstringFactory, doIndexing);
          }
          else {
            dtm = new SAX2DTM(this, source, documentID, whiteSpaceFilter,
                              xstringFactory, doIndexing);
          }
          addDTM(dtm, dtmPos, 0);
          boolean haveXercesParser =
                     (null != reader)
                     && (reader.getClass()
                               .getName()
                               .equals("org.apache.xerces.parsers.SAXParser") );
          if (haveXercesParser) {
            incremental = true;  
          }
          if (m_incremental && incremental
               ) {
            IncrementalSAXSource coParser=null;
            if (haveXercesParser) {
              try {
                coParser =(IncrementalSAXSource)
                  Class.forName("org.apache.xml.dtm.ref.IncrementalSAXSource_Xerces").newInstance();  
              }  catch( Exception ex ) {
                ex.printStackTrace();
                coParser=null;
              }
            }
            if (coParser==null ) {
              if (null == reader) {
                coParser = new IncrementalSAXSource_Filter();
              } else {
                IncrementalSAXSource_Filter filter =
                         new IncrementalSAXSource_Filter();
                filter.setXMLReader(reader);
                coParser=filter;
              }
            }
            dtm.setIncrementalSAXSource(coParser);
            if (null == xmlSource) {
              return dtm;
            }
            if (null == reader.getErrorHandler()) {
              reader.setErrorHandler(dtm);
            }
            reader.setDTDHandler(dtm);
            try {
              coParser.startParse(xmlSource);
            } catch (RuntimeException re) {
              dtm.clearCoRoutine();
              throw re;
            } catch (Exception e) {
              dtm.clearCoRoutine();
              throw new org.apache.xml.utils.WrappedRuntimeException(e);
            }
          } else {
            if (null == reader) {
              return dtm;
            }
            reader.setContentHandler(dtm);
            reader.setDTDHandler(dtm);
            if (null == reader.getErrorHandler()) {
              reader.setErrorHandler(dtm);
            }
            try {
              reader.setProperty(
                               "http:
                               dtm);
            } catch (SAXNotRecognizedException e){}
              catch (SAXNotSupportedException e){}
            try {
              reader.parse(xmlSource);
            } catch (RuntimeException re) {
              dtm.clearCoRoutine();
              throw re;
            } catch (Exception e) {
              dtm.clearCoRoutine();
              throw new org.apache.xml.utils.WrappedRuntimeException(e);
            }
          }
          if (DUMPTREE) {
            System.out.println("Dumping SAX2DOM");
            dtm.dumpDTM(System.err);
          }
          return dtm;
        } finally {
          if (reader != null && !(m_incremental && incremental)) {
            reader.setContentHandler(m_defaultHandler);
            reader.setDTDHandler(m_defaultHandler);
            reader.setErrorHandler(m_defaultHandler);
            try {
              reader.setProperty("http:
            }
            catch (Exception e) {}
          }
          releaseXMLReader(reader);
        }
      } else {
        throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_NOT_SUPPORTED, new Object[]{source})); 
      }
    }
  }
  synchronized public int getDTMHandleFromNode(org.w3c.dom.Node node)
  {
    if(null == node)
      throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_NODE_NON_NULL, null)); 
    if (node instanceof org.apache.xml.dtm.ref.DTMNodeProxy)
      return ((org.apache.xml.dtm.ref.DTMNodeProxy) node).getDTMNodeNumber();
    else
    {
			int max = m_dtms.length;
      for(int i = 0; i < max; i++)
        {
          DTM thisDTM=m_dtms[i];
          if((null != thisDTM) && thisDTM instanceof DOM2DTM)
          {
            int handle=((DOM2DTM)thisDTM).getHandleOfNode(node);
            if(handle!=DTM.NULL) return handle;
          }
         }
      Node root = node;
      Node p = (root.getNodeType() == Node.ATTRIBUTE_NODE) ? ((org.w3c.dom.Attr)root).getOwnerElement() : root.getParentNode();
      for (; p != null; p = p.getParentNode())
      {
        root = p;
      }
      DOM2DTM dtm = (DOM2DTM) getDTM(new javax.xml.transform.dom.DOMSource(root),
																		 false, null, true, true);
      int handle;
      if(node instanceof org.apache.xml.dtm.ref.dom2dtm.DOM2DTMdefaultNamespaceDeclarationNode)
      {
				handle=dtm.getHandleOfNode(((org.w3c.dom.Attr)node).getOwnerElement());
				handle=dtm.getAttributeNode(handle,node.getNamespaceURI(),node.getLocalName());
      }
      else
				handle = ((DOM2DTM)dtm).getHandleOfNode(node);
      if(DTM.NULL == handle)
        throw new RuntimeException(XMLMessages.createXMLMessage(XMLErrorResources.ER_COULD_NOT_RESOLVE_NODE, null)); 
      return handle;
    }
  }
  synchronized public XMLReader getXMLReader(Source inputSource)
  {
    try
    {
      XMLReader reader = (inputSource instanceof SAXSource)
                         ? ((SAXSource) inputSource).getXMLReader() : null;
      if (null == reader) {
        if (m_readerManager == null) {
            m_readerManager = XMLReaderManager.getInstance();
        }
        reader = m_readerManager.getXMLReader();
      }
      return reader;
    } catch (SAXException se) {
      throw new DTMException(se.getMessage(), se);
    }
  }
  synchronized public void releaseXMLReader(XMLReader reader) {
    if (m_readerManager != null) {
      m_readerManager.releaseXMLReader(reader);
    }
  }
  synchronized public DTM getDTM(int nodeHandle)
  {
    try
    {
      return m_dtms[nodeHandle >>> IDENT_DTM_NODE_BITS];
    }
    catch(java.lang.ArrayIndexOutOfBoundsException e)
    {
      if(nodeHandle==DTM.NULL)
				return null;		
      else
				throw e;		
    }    
  }
  synchronized public int getDTMIdentity(DTM dtm)
  {
	if(dtm instanceof DTMDefaultBase)
	{
		DTMDefaultBase dtmdb=(DTMDefaultBase)dtm;
		if(dtmdb.getManager()==this)
			return dtmdb.getDTMIDs().elementAt(0);
		else
			return -1;
	}
    int n = m_dtms.length;
    for (int i = 0; i < n; i++)
    {
      DTM tdtm = m_dtms[i];
      if (tdtm == dtm && m_dtm_offsets[i]==0)
        return i << IDENT_DTM_NODE_BITS;
    }
    return -1;
  }
  synchronized public boolean release(DTM dtm, boolean shouldHardDelete)
  {
    if(DEBUG)
    {
      System.out.println("Releasing "+
			 (shouldHardDelete ? "HARD" : "soft")+
			 " dtm="+
			 dtm.getDocumentBaseURI()
			 );
    }
    if (dtm instanceof SAX2DTM)
    {
      ((SAX2DTM) dtm).clearCoRoutine();
    }
		if(dtm instanceof DTMDefaultBase)
		{
			org.apache.xml.utils.SuballocatedIntVector ids=((DTMDefaultBase)dtm).getDTMIDs();
			for(int i=ids.size()-1;i>=0;--i)
				m_dtms[ids.elementAt(i)>>>DTMManager.IDENT_DTM_NODE_BITS]=null;
		}
		else
		{
			int i = getDTMIdentity(dtm);
		    if (i >= 0)
			{
				m_dtms[i >>> DTMManager.IDENT_DTM_NODE_BITS] = null;
			}
		}
    dtm.documentRelease();
    return true;
  }
  synchronized public DTM createDocumentFragment()
  {
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.newDocument();
      Node df = doc.createDocumentFragment();
      return getDTM(new DOMSource(df), true, null, false, false);
    }
    catch (Exception e)
    {
      throw new DTMException(e);
    }
  }
  synchronized public DTMIterator createDTMIterator(int whatToShow, DTMFilter filter,
                                       boolean entityReferenceExpansion)
  {
    return null;
  }
  synchronized public DTMIterator createDTMIterator(String xpathString,
                                       PrefixResolver presolver)
  {
    return null;
  }
  synchronized public DTMIterator createDTMIterator(int node)
  {
    return null;
  }
  synchronized public DTMIterator createDTMIterator(Object xpathCompiler, int pos)
  {
    return null;
  }
  public ExpandedNameTable getExpandedNameTable(DTM dtm)
  {
    return m_expandedNameTable;
  }
}
