public class DTMDocumentImpl
implements DTM, org.xml.sax.ContentHandler, org.xml.sax.ext.LexicalHandler
{
        protected static final byte DOCHANDLE_SHIFT = 22;
        protected static final int NODEHANDLE_MASK = (1 << (DOCHANDLE_SHIFT + 1)) - 1;
        protected static final int DOCHANDLE_MASK = -1 - NODEHANDLE_MASK;
        int m_docHandle = NULL;		 
        int m_docElement = NULL;	 
        int currentParent = 0;			
        int previousSibling = 0;		
        protected int m_currentNode = -1;		
        private boolean previousSiblingWasParent = false;
        int gotslot[] = new int[4];
        private boolean done = false;
        boolean m_isError = false;
        private final boolean DEBUG = false;
        protected String m_documentBaseURI;
  private IncrementalSAXSource m_incrSAXSource=null;
        ChunkedIntArray nodes = new ChunkedIntArray(4);
        private FastStringBuffer m_char = new FastStringBuffer();
        private int m_char_current_start=0;
        private DTMStringPool m_localNames = new DTMStringPool();
        private DTMStringPool m_nsNames = new DTMStringPool();
        private DTMStringPool m_prefixNames = new DTMStringPool();
        private ExpandedNameTable m_expandedNames=
                new ExpandedNameTable();
        private XMLStringFactory m_xsf;
        public DTMDocumentImpl(DTMManager mgr, int documentNumber,
                               DTMWSFilter whiteSpaceFilter,
                               XMLStringFactory xstringfactory){
                initDocument(documentNumber);	 
                m_xsf = xstringfactory;
        }
  public void setIncrementalSAXSource(IncrementalSAXSource source)
  {
    m_incrSAXSource=source;
    source.setContentHandler(this);
    source.setLexicalHandler(this);
  }
        private final int appendNode(int w0, int w1, int w2, int w3)
        {
                int slotnumber = nodes.appendSlot(w0, w1, w2, w3);
                if (DEBUG) System.out.println(slotnumber+": "+w0+" "+w1+" "+w2+" "+w3);
                if (previousSiblingWasParent)
                        nodes.writeEntry(previousSibling,2,slotnumber);
                previousSiblingWasParent = false;	
                return slotnumber;
        }
        public void setFeature(String featureId, boolean state) {};
        public void setLocalNameTable(DTMStringPool poolRef) {
                m_localNames = poolRef;
        }
        public DTMStringPool getLocalNameTable() {
                 return m_localNames;
         }
        public void setNsNameTable(DTMStringPool poolRef) {
                m_nsNames = poolRef;
        }
        public DTMStringPool getNsNameTable() {
                 return m_nsNames;
         }
        public void setPrefixNameTable(DTMStringPool poolRef) {
                m_prefixNames = poolRef;
        }
        public DTMStringPool getPrefixNameTable() {
                return m_prefixNames;
        }
         void setContentBuffer(FastStringBuffer buffer) {
                 m_char = buffer;
         }
         FastStringBuffer getContentBuffer() {
                 return m_char;
         }
  public org.xml.sax.ContentHandler getContentHandler()
  {
    if (m_incrSAXSource instanceof IncrementalSAXSource_Filter)
      return (ContentHandler) m_incrSAXSource;
    else
      return this;
  }
  public LexicalHandler getLexicalHandler()
  {
    if (m_incrSAXSource instanceof IncrementalSAXSource_Filter)
      return (LexicalHandler) m_incrSAXSource;
    else
      return this;
  }
  public org.xml.sax.EntityResolver getEntityResolver()
  {
    return null;
  }
  public org.xml.sax.DTDHandler getDTDHandler()
  {
    return null;
  }
  public org.xml.sax.ErrorHandler getErrorHandler()
  {
    return null;
  }
  public org.xml.sax.ext.DeclHandler getDeclHandler()
  {
    return null;
  }
  public boolean needsTwoThreads()
  {
    return null!=m_incrSAXSource;
  }
  public void characters(char[] ch, int start, int length)
       throws org.xml.sax.SAXException
  {
    m_char.append(ch,start,length);
  }
  private void processAccumulatedText()
  {
    int len=m_char.length();
    if(len!=m_char_current_start)
      {
        appendTextChild(m_char_current_start,len-m_char_current_start);
        m_char_current_start=len;
      }
  }
  public void endDocument()
       throws org.xml.sax.SAXException
  {
    appendEndDocument();
  }
  public void endElement(java.lang.String namespaceURI, java.lang.String localName,
      java.lang.String qName)
       throws org.xml.sax.SAXException
  {
    processAccumulatedText();
    appendEndElement();
  }
  public void endPrefixMapping(java.lang.String prefix)
       throws org.xml.sax.SAXException
  {
  }
  public void ignorableWhitespace(char[] ch, int start, int length)
       throws org.xml.sax.SAXException
  {
  }
  public void processingInstruction(java.lang.String target, java.lang.String data)
       throws org.xml.sax.SAXException
  {
    processAccumulatedText();
  }
  public void setDocumentLocator(Locator locator)
  {
  }
  public void skippedEntity(java.lang.String name)
       throws org.xml.sax.SAXException
  {
    processAccumulatedText();
  }
  public void startDocument()
       throws org.xml.sax.SAXException
  {
    appendStartDocument();
  }
  public void startElement(java.lang.String namespaceURI, java.lang.String localName,
      java.lang.String qName, Attributes atts)
       throws org.xml.sax.SAXException
  {
    processAccumulatedText();
    String prefix=null;
    int colon=qName.indexOf(':');
    if(colon>0)
      prefix=qName.substring(0,colon);
    System.out.println("Prefix="+prefix+" index="+m_prefixNames.stringToIndex(prefix));
    appendStartElement(m_nsNames.stringToIndex(namespaceURI),
                     m_localNames.stringToIndex(localName),
                     m_prefixNames.stringToIndex(prefix)); 
    int nAtts=(atts==null) ? 0 : atts.getLength();
    for(int i=nAtts-1;i>=0;--i)
      {
        qName=atts.getQName(i);
        if(qName.startsWith("xmlns:") || "xmlns".equals(qName))
          {
            prefix=null;
            colon=qName.indexOf(':');
            if(colon>0)
              {
                prefix=qName.substring(0,colon);
              }
            else
              {
                prefix=null; 
              }
            appendNSDeclaration(
                                    m_prefixNames.stringToIndex(prefix),
                                    m_nsNames.stringToIndex(atts.getValue(i)),
                                    atts.getType(i).equalsIgnoreCase("ID"));
          }
      }
    for(int i=nAtts-1;i>=0;--i)
      {
        qName=atts.getQName(i);
        if(!(qName.startsWith("xmlns:") || "xmlns".equals(qName)))
          {
            prefix=null;
            colon=qName.indexOf(':');
            if(colon>0)
              {
                prefix=qName.substring(0,colon);
                localName=qName.substring(colon+1);
              }
            else
              {
                prefix=""; 
                localName=qName;
              }
            m_char.append(atts.getValue(i)); 
            int contentEnd=m_char.length();
            if(!("xmlns".equals(prefix) || "xmlns".equals(qName)))
              appendAttribute(m_nsNames.stringToIndex(atts.getURI(i)),
                                  m_localNames.stringToIndex(localName),
                                  m_prefixNames.stringToIndex(prefix),
                                  atts.getType(i).equalsIgnoreCase("ID"),
                                  m_char_current_start, contentEnd-m_char_current_start);
            m_char_current_start=contentEnd;
          }
      }
  }
  public void startPrefixMapping(java.lang.String prefix, java.lang.String uri)
       throws org.xml.sax.SAXException
  {
  }
  public void comment(char[] ch, int start, int length)
       throws org.xml.sax.SAXException
  {
    processAccumulatedText();
    m_char.append(ch,start,length); 
    appendComment(m_char_current_start,length);
    m_char_current_start+=length;
  }
  public void endCDATA()
       throws org.xml.sax.SAXException
  {
  }
  public void endDTD()
       throws org.xml.sax.SAXException
  {
  }
  public void endEntity(java.lang.String name)
       throws org.xml.sax.SAXException
  {
  }
  public void startCDATA()
       throws org.xml.sax.SAXException
  {
  }
  public void startDTD(java.lang.String name, java.lang.String publicId,
      java.lang.String systemId)
       throws org.xml.sax.SAXException
  {
  }
  public void startEntity(java.lang.String name)
       throws org.xml.sax.SAXException
  {
  }
        final void initDocument(int documentNumber)
        {
                m_docHandle = documentNumber<<DOCHANDLE_SHIFT;
                nodes.writeSlot(0,DOCUMENT_NODE,-1,-1,0);
                done = false;
        }
        public boolean hasChildNodes(int nodeHandle) {
                return(getFirstChild(nodeHandle) != NULL);
        }
        public int getFirstChild(int nodeHandle) {
                nodeHandle &= NODEHANDLE_MASK;
                nodes.readSlot(nodeHandle, gotslot);
                short type = (short) (gotslot[0] & 0xFFFF);
                if ((type == ELEMENT_NODE) || (type == DOCUMENT_NODE) ||
                                (type == ENTITY_REFERENCE_NODE)) {
                        int kid = nodeHandle + 1;
                        nodes.readSlot(kid, gotslot);
                        while (ATTRIBUTE_NODE == (gotslot[0] & 0xFFFF)) {
                                kid = gotslot[2];
                                if (kid == NULL) return NULL;
                                nodes.readSlot(kid, gotslot);
                        }
                        if (gotslot[1] == nodeHandle)
                        {
                          int firstChild = kid | m_docHandle;
                          return firstChild;
                        }
                }
                return NULL;
        }
        public int getLastChild(int nodeHandle) {
                nodeHandle &= NODEHANDLE_MASK;
                int lastChild = NULL;
                for (int nextkid = getFirstChild(nodeHandle); nextkid != NULL;
                                nextkid = getNextSibling(nextkid)) {
                        lastChild = nextkid;
                }
                return lastChild | m_docHandle;
        }
        public int getAttributeNode(int nodeHandle, String namespaceURI, String name) {
                int nsIndex = m_nsNames.stringToIndex(namespaceURI),
                                                                        nameIndex = m_localNames.stringToIndex(name);
                nodeHandle &= NODEHANDLE_MASK;
                nodes.readSlot(nodeHandle, gotslot);
                short type = (short) (gotslot[0] & 0xFFFF);
                if (type == ELEMENT_NODE)
                        nodeHandle++;
                while (type == ATTRIBUTE_NODE) {
                        if ((nsIndex == (gotslot[0] << 16)) && (gotslot[3] == nameIndex))
                                return nodeHandle | m_docHandle;
                        nodeHandle = gotslot[2];
                        nodes.readSlot(nodeHandle, gotslot);
                }
                return NULL;
        }
        public int getFirstAttribute(int nodeHandle) {
                nodeHandle &= NODEHANDLE_MASK;
                if (ELEMENT_NODE != (nodes.readEntry(nodeHandle, 0) & 0xFFFF))
                        return NULL;
                nodeHandle++;
                return(ATTRIBUTE_NODE == (nodes.readEntry(nodeHandle, 0) & 0xFFFF)) ?
                nodeHandle | m_docHandle : NULL;
        }
        public int getFirstNamespaceNode(int nodeHandle, boolean inScope) {
                return NULL;
        }
        public int getNextSibling(int nodeHandle) {
                nodeHandle &= NODEHANDLE_MASK;
                if (nodeHandle == 0)
                        return NULL;
                short type = (short) (nodes.readEntry(nodeHandle, 0) & 0xFFFF);
                if ((type == ELEMENT_NODE) || (type == ATTRIBUTE_NODE) ||
                                (type == ENTITY_REFERENCE_NODE)) {
                        int nextSib = nodes.readEntry(nodeHandle, 2);
                        if (nextSib == NULL)
                                return NULL;
                        if (nextSib != 0)
                                return (m_docHandle | nextSib);
                }
                int thisParent = nodes.readEntry(nodeHandle, 1);
                if (nodes.readEntry(++nodeHandle, 1) == thisParent)
                        return (m_docHandle | nodeHandle);
                return NULL;
        }
        public int getPreviousSibling(int nodeHandle) {
                nodeHandle &= NODEHANDLE_MASK;
                if (nodeHandle == 0)
                        return NULL;
                int parent = nodes.readEntry(nodeHandle, 1);
                int kid = NULL;
                for (int nextkid = getFirstChild(parent); nextkid != nodeHandle;
                                nextkid = getNextSibling(nextkid)) {
                        kid = nextkid;
                }
                return kid | m_docHandle;
        }
        public int getNextAttribute(int nodeHandle) {
                nodeHandle &= NODEHANDLE_MASK;
                nodes.readSlot(nodeHandle, gotslot);
                short type = (short) (gotslot[0] & 0xFFFF);
                if (type == ELEMENT_NODE) {
                        return getFirstAttribute(nodeHandle);
                } else if (type == ATTRIBUTE_NODE) {
                        if (gotslot[2] != NULL)
                                return (m_docHandle | gotslot[2]);
                }
                return NULL;
        }
        public int getNextNamespaceNode(int baseHandle,int namespaceHandle, boolean inScope) {
                return NULL;
        }
        public int getNextDescendant(int subtreeRootHandle, int nodeHandle) {
                subtreeRootHandle &= NODEHANDLE_MASK;
                nodeHandle &= NODEHANDLE_MASK;
                if (nodeHandle == 0)
                        return NULL;
                while (!m_isError) {
                        if (done && (nodeHandle > nodes.slotsUsed()))
                                break;
                        if (nodeHandle > subtreeRootHandle) {
                                nodes.readSlot(nodeHandle+1, gotslot);
                                if (gotslot[2] != 0) {
                                        short type = (short) (gotslot[0] & 0xFFFF);
                                        if (type == ATTRIBUTE_NODE) {
                                                nodeHandle +=2;
                                        } else {
                                                int nextParentPos = gotslot[1];
                                                if (nextParentPos >= subtreeRootHandle)
                                                        return (m_docHandle | (nodeHandle+1));
                                                else
                                                        break;
                                        }
                                } else if (!done) {
                                } else
                                        break;
                        } else {
                                nodeHandle++;
                        }
                }
                return NULL;
        }
        public int getNextFollowing(int axisContextHandle, int nodeHandle) {
                return NULL;
        }
        public int getNextPreceding(int axisContextHandle, int nodeHandle) {
                nodeHandle &= NODEHANDLE_MASK;
                while (nodeHandle > 1) {
                        nodeHandle--;
                        if (ATTRIBUTE_NODE == (nodes.readEntry(nodeHandle, 0) & 0xFFFF))
                                continue;
                        return (m_docHandle | nodes.specialFind(axisContextHandle, nodeHandle));
                }
                return NULL;
        }
        public int getParent(int nodeHandle) {
                return (m_docHandle | nodes.readEntry(nodeHandle, 1));
        }
        public int getDocumentRoot() {
                return (m_docHandle | m_docElement);
        }
        public int getDocument() {
                return m_docHandle;
        }
        public int getOwnerDocument(int nodeHandle) {
                if ((nodeHandle & NODEHANDLE_MASK) == 0)
                        return NULL;
                return (nodeHandle & DOCHANDLE_MASK);
        }
        public int getDocumentRoot(int nodeHandle) {
                if ((nodeHandle & NODEHANDLE_MASK) == 0)
                        return NULL;
                return (nodeHandle & DOCHANDLE_MASK);
        }
        public XMLString getStringValue(int nodeHandle) {
        nodes.readSlot(nodeHandle, gotslot);
        int nodetype=gotslot[0] & 0xFF;
        String value=null;
        switch (nodetype) {
        case TEXT_NODE:
        case COMMENT_NODE:
        case CDATA_SECTION_NODE:
                value= m_char.getString(gotslot[2], gotslot[3]);
                break;
        case PROCESSING_INSTRUCTION_NODE:
        case ATTRIBUTE_NODE:
        case ELEMENT_NODE:
        case ENTITY_REFERENCE_NODE:
        default:
                break;
        }
        return m_xsf.newstr( value );
        }
        public int getStringValueChunkCount(int nodeHandle)
        {
                return 0;
        }
        public char[] getStringValueChunk(int nodeHandle, int chunkIndex,
                                                                                                                                                int[] startAndLen) {return new char[0];}
        public int getExpandedTypeID(int nodeHandle) {
           nodes.readSlot(nodeHandle, gotslot);
           String qName = m_localNames.indexToString(gotslot[3]);
           int colonpos = qName.indexOf(":");
           String localName = qName.substring(colonpos+1);
           String namespace = m_nsNames.indexToString(gotslot[0] << 16);
           String expandedName = namespace + ":" + localName;
           int expandedNameID = m_nsNames.stringToIndex(expandedName);
        return expandedNameID;
        }
        public int getExpandedTypeID(String namespace, String localName, int type) {
           String expandedName = namespace + ":" + localName;
           int expandedNameID = m_nsNames.stringToIndex(expandedName);
           return expandedNameID;
        }
        public String getLocalNameFromExpandedNameID(int ExpandedNameID) {
           String expandedName = m_localNames.indexToString(ExpandedNameID);
           int colonpos = expandedName.indexOf(":");
           String localName = expandedName.substring(colonpos+1);
           return localName;
        }
        public String getNamespaceFromExpandedNameID(int ExpandedNameID) {
           String expandedName = m_localNames.indexToString(ExpandedNameID);
           int colonpos = expandedName.indexOf(":");
           String nsName = expandedName.substring(0, colonpos);
        return nsName;
        }
        private static final String[] fixednames=
        {
                null,null,							
                null,"#text",						
                "#cdata_section",null,	
                null,null,							
                "#comment","#document",	
                null,"#document-fragment", 
                null};									
        public String getNodeName(int nodeHandle) {
                nodes.readSlot(nodeHandle, gotslot);
                short type = (short) (gotslot[0] & 0xFFFF);
                String name = fixednames[type];
                if (null == name) {
                  int i=gotslot[3];
                  System.out.println("got i="+i+" "+(i>>16)+"/"+(i&0xffff));
                  name=m_localNames.indexToString(i & 0xFFFF);
                  String prefix=m_prefixNames.indexToString(i >>16);
                  if(prefix!=null && prefix.length()>0)
                    name=prefix+":"+name;
                }
                return name;
        }
        public String getNodeNameX(int nodeHandle) {return null;}
        public String getLocalName(int nodeHandle) {
                nodes.readSlot(nodeHandle, gotslot);
                short type = (short) (gotslot[0] & 0xFFFF);
                String name = "";
                if ((type==ELEMENT_NODE) || (type==ATTRIBUTE_NODE)) {
                  int i=gotslot[3];
                  name=m_localNames.indexToString(i & 0xFFFF);
                  if(name==null) name="";
                }
                return name;
        }
        public String getPrefix(int nodeHandle) {
                nodes.readSlot(nodeHandle, gotslot);
                short type = (short) (gotslot[0] & 0xFFFF);
                String name = "";
                if((type==ELEMENT_NODE) || (type==ATTRIBUTE_NODE)) {
                  int i=gotslot[3];
                  name=m_prefixNames.indexToString(i >>16);
                  if(name==null) name="";
                }
                return name;
        }
        public String getNamespaceURI(int nodeHandle) {return null;}
        public String getNodeValue(int nodeHandle)
        {
                nodes.readSlot(nodeHandle, gotslot);
                int nodetype=gotslot[0] & 0xFF;		
                String value=null;
                switch (nodetype) {			
                case ATTRIBUTE_NODE:
                        nodes.readSlot(nodeHandle+1, gotslot);
                case TEXT_NODE:
                case COMMENT_NODE:
                case CDATA_SECTION_NODE:
                        value=m_char.getString(gotslot[2], gotslot[3]);		
                        break;
                case PROCESSING_INSTRUCTION_NODE:
                case ELEMENT_NODE:
                case ENTITY_REFERENCE_NODE:
                default:
                        break;
                }
                return value;
        }
        public short getNodeType(int nodeHandle) {
                return(short) (nodes.readEntry(nodeHandle, 0) & 0xFFFF);
        }
        public short getLevel(int nodeHandle) {
                short count = 0;
                while (nodeHandle != 0) {
                        count++;
                        nodeHandle = nodes.readEntry(nodeHandle, 1);
                }
                return count;
        }
        public boolean isSupported(String feature, String version) {return false;}
        public String getDocumentBaseURI()
        {
          return m_documentBaseURI;
        }
        public void setDocumentBaseURI(String baseURI)
        {
          m_documentBaseURI = baseURI;
        }
        public String getDocumentSystemIdentifier(int nodeHandle) {return null;}
        public String getDocumentEncoding(int nodeHandle) {return null;}
        public String getDocumentStandalone(int nodeHandle) {return null;}
        public String getDocumentVersion(int documentHandle) {return null;}
        public boolean getDocumentAllDeclarationsProcessed() {return false;}
        public String getDocumentTypeDeclarationSystemIdentifier() {return null;}
        public String getDocumentTypeDeclarationPublicIdentifier() {return null;}
        public int getElementById(String elementId) {return 0;}
        public String getUnparsedEntityURI(String name) {return null;}
        public boolean supportsPreStripping() {return false;}
        public boolean isNodeAfter(int nodeHandle1, int nodeHandle2) {return false;}
        public boolean isCharacterElementContentWhitespace(int nodeHandle) {return false;}
        public boolean isDocumentAllDeclarationsProcessed(int documentHandle) {return false;}
        public boolean isAttributeSpecified(int attributeHandle) {return false;}
        public void dispatchCharactersEvents(
                                                                                                                                                        int nodeHandle, org.xml.sax.ContentHandler ch, boolean normalize)
        throws org.xml.sax.SAXException {}
        public void dispatchToEvents(int nodeHandle, org.xml.sax.ContentHandler ch)
        throws org.xml.sax.SAXException {}
        public org.w3c.dom.Node getNode(int nodeHandle)
        {
          return null;
        }
        public void appendChild(int newChild, boolean clone, boolean cloneDepth) {
                boolean sameDoc = ((newChild & DOCHANDLE_MASK) == m_docHandle);
                if (clone || !sameDoc) {
                } else {
                }
        }
        public void appendTextChild(String str) {
        }
  void appendTextChild(int m_char_current_start,int contentLength)
  {
    int w0 = TEXT_NODE;
    int w1 = currentParent;
    int w2 = m_char_current_start;
    int w3 = contentLength;
    int ourslot = appendNode(w0, w1, w2, w3);
    previousSibling = ourslot;
  }
  void appendComment(int m_char_current_start,int contentLength)
  {
    int w0 = COMMENT_NODE;
    int w1 = currentParent;
    int w2 = m_char_current_start;
    int w3 = contentLength;
    int ourslot = appendNode(w0, w1, w2, w3);
    previousSibling = ourslot;
  }
  void appendStartElement(int namespaceIndex,int localNameIndex, int prefixIndex)
  {
                int w0 = (namespaceIndex << 16) | ELEMENT_NODE;
                int w1 = currentParent;
                int w2 = 0;
                int w3 = localNameIndex | prefixIndex<<16;
                System.out.println("set w3="+w3+" "+(w3>>16)+"/"+(w3&0xffff));
                int ourslot = appendNode(w0, w1, w2, w3);
                currentParent = ourslot;
                previousSibling = 0;
                if (m_docElement == NULL)
                        m_docElement = ourslot;
  }
  void appendNSDeclaration(int prefixIndex, int namespaceIndex,
                           boolean isID)
  {
    final int namespaceForNamespaces=m_nsNames.stringToIndex("http:
    int w0 = NAMESPACE_NODE | (m_nsNames.stringToIndex("http:
    int w1 = currentParent;
    int w2 = 0;
    int w3 = namespaceIndex;
    int ourslot = appendNode(w0, w1, w2, w3);
    previousSibling = ourslot;	
    previousSiblingWasParent = false;
    return ;
  }
  void appendAttribute(int namespaceIndex, int localNameIndex, int prefixIndex,
                       boolean isID,
                       int m_char_current_start, int contentLength)
  {
    int w0 = ATTRIBUTE_NODE | namespaceIndex<<16;
    int w1 = currentParent;
    int w2 = 0;
    int w3 = localNameIndex | prefixIndex<<16;
    System.out.println("set w3="+w3+" "+(w3>>16)+"/"+(w3&0xffff));
    int ourslot = appendNode(w0, w1, w2, w3);
    previousSibling = ourslot;	
    w0 = TEXT_NODE;
    w1 = ourslot;
    w2 = m_char_current_start;
    w3 = contentLength;
    appendNode(w0, w1, w2, w3);
    previousSiblingWasParent = true;
    return ;
  }
  public DTMAxisTraverser getAxisTraverser(final int axis)
  {
    return null;
  }
  public DTMAxisIterator getAxisIterator(final int axis)
  {
    return null;
  }
  public DTMAxisIterator getTypedAxisIterator(final int axis, final int type)
  {
    return null;
  }
  void appendEndElement()
  {
    if (previousSiblingWasParent)
      nodes.writeEntry(previousSibling, 2, NULL);
    previousSibling = currentParent;
    nodes.readSlot(currentParent, gotslot);
    currentParent = gotslot[1] & 0xFFFF;
    previousSiblingWasParent = true;
  }
  void appendStartDocument()
  {
    m_docElement = NULL;	 
    initDocument(0);
  }
  void appendEndDocument()
  {
    done = true;
  }
  public void setProperty(String property, Object value)
  {
  }
  public SourceLocator getSourceLocatorFor(int node)
  {
    return null;
  }
   public void documentRegistration()
   {
   }
   public void documentRelease()
   {
   }
   public void migrateTo(DTMManager manager)
   {
   }
}
