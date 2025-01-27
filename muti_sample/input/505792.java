public abstract class DTMDefaultBase implements DTM
{
    static final boolean JJK_DEBUG=false;
  public static final int ROOTNODE = 0;
  protected int m_size = 0;
  protected SuballocatedIntVector m_exptype;
  protected SuballocatedIntVector m_firstch;
  protected SuballocatedIntVector m_nextsib;
  protected SuballocatedIntVector m_prevsib;
  protected SuballocatedIntVector m_parent;
  protected Vector m_namespaceDeclSets = null;
  protected SuballocatedIntVector m_namespaceDeclSetElements = null;
  protected int[][][] m_elemIndexes;
  public static final int DEFAULT_BLOCKSIZE = 512;  
  public static final int DEFAULT_NUMBLOCKS = 32;
  public static final int DEFAULT_NUMBLOCKS_SMALL = 4;
  protected static final int NOTPROCESSED = DTM.NULL - 1;
  public DTMManager m_mgr;
  protected DTMManagerDefault m_mgrDefault=null;
  protected SuballocatedIntVector m_dtmIdent;
  protected String m_documentBaseURI;
  protected DTMWSFilter m_wsfilter;
  protected boolean m_shouldStripWS = false;
  protected BoolStack m_shouldStripWhitespaceStack;
  protected XMLStringFactory m_xstrf;
  protected ExpandedNameTable m_expandedNameTable;
  protected boolean m_indexing;
  public DTMDefaultBase(DTMManager mgr, Source source, int dtmIdentity,
  			DTMWSFilter whiteSpaceFilter,
  			XMLStringFactory xstringfactory, boolean doIndexing)
  {
    this(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory,
         doIndexing, DEFAULT_BLOCKSIZE, true, false);
  }
  public DTMDefaultBase(DTMManager mgr, Source source, int dtmIdentity,
                        DTMWSFilter whiteSpaceFilter,
                        XMLStringFactory xstringfactory, boolean doIndexing,
                        int blocksize, boolean usePrevsib,
                        boolean newNameTable)
  {    
    int numblocks;    
    if (blocksize <= 64)
    {
      numblocks = DEFAULT_NUMBLOCKS_SMALL;
      m_dtmIdent= new SuballocatedIntVector(4, 1);
    }
    else
    {
      numblocks = DEFAULT_NUMBLOCKS;
      m_dtmIdent= new SuballocatedIntVector(32);
    }
    m_exptype = new SuballocatedIntVector(blocksize, numblocks);
    m_firstch = new SuballocatedIntVector(blocksize, numblocks);
    m_nextsib = new SuballocatedIntVector(blocksize, numblocks);
    m_parent  = new SuballocatedIntVector(blocksize, numblocks);
    if (usePrevsib)
      m_prevsib = new SuballocatedIntVector(blocksize, numblocks);
    m_mgr = mgr;
    if(mgr instanceof DTMManagerDefault)
      m_mgrDefault=(DTMManagerDefault)mgr;
    m_documentBaseURI = (null != source) ? source.getSystemId() : null;
    m_dtmIdent.setElementAt(dtmIdentity,0);
    m_wsfilter = whiteSpaceFilter;
    m_xstrf = xstringfactory;
    m_indexing = doIndexing;
    if (doIndexing)
    {
      m_expandedNameTable = new ExpandedNameTable();
    }
    else
    {
      m_expandedNameTable = m_mgrDefault.getExpandedNameTable(this);
    }
    if (null != whiteSpaceFilter)
    {
      m_shouldStripWhitespaceStack = new BoolStack();
      pushShouldStripWhitespace(false);
    }
  }
  protected void ensureSizeOfIndex(int namespaceID, int LocalNameID)
  {
    if (null == m_elemIndexes)
    {
      m_elemIndexes = new int[namespaceID + 20][][];
    }
    else if (m_elemIndexes.length <= namespaceID)
    {
      int[][][] indexes = m_elemIndexes;
      m_elemIndexes = new int[namespaceID + 20][][];
      System.arraycopy(indexes, 0, m_elemIndexes, 0, indexes.length);
    }
    int[][] localNameIndex = m_elemIndexes[namespaceID];
    if (null == localNameIndex)
    {
      localNameIndex = new int[LocalNameID + 100][];
      m_elemIndexes[namespaceID] = localNameIndex;
    }
    else if (localNameIndex.length <= LocalNameID)
    {
      int[][] indexes = localNameIndex;
      localNameIndex = new int[LocalNameID + 100][];
      System.arraycopy(indexes, 0, localNameIndex, 0, indexes.length);
      m_elemIndexes[namespaceID] = localNameIndex;
    }
    int[] elemHandles = localNameIndex[LocalNameID];
    if (null == elemHandles)
    {
      elemHandles = new int[128];
      localNameIndex[LocalNameID] = elemHandles;
      elemHandles[0] = 1;
    }
    else if (elemHandles.length <= elemHandles[0] + 1)
    {
      int[] indexes = elemHandles;
      elemHandles = new int[elemHandles[0] + 1024];
      System.arraycopy(indexes, 0, elemHandles, 0, indexes.length);
      localNameIndex[LocalNameID] = elemHandles;
    }
  }
  protected void indexNode(int expandedTypeID, int identity)
  {
    ExpandedNameTable ent = m_expandedNameTable;
    short type = ent.getType(expandedTypeID);
    if (DTM.ELEMENT_NODE == type)
    {
      int namespaceID = ent.getNamespaceID(expandedTypeID);
      int localNameID = ent.getLocalNameID(expandedTypeID);
      ensureSizeOfIndex(namespaceID, localNameID);
      int[] index = m_elemIndexes[namespaceID][localNameID];
      index[index[0]] = identity;
      index[0]++;
    }
  }
  protected int findGTE(int[] list, int start, int len, int value)
  {
    int low = start;
    int high = start + (len - 1);
    int end = high;
    while (low <= high)
    {
      int mid = (low + high) / 2;
      int c = list[mid];
      if (c > value)
        high = mid - 1;
      else if (c < value)
        low = mid + 1;
      else
        return mid;
    }
    return (low <= end && list[low] > value) ? low : -1;
  }
  int findElementFromIndex(int nsIndex, int lnIndex, int firstPotential)
  {
    int[][][] indexes = m_elemIndexes;
    if (null != indexes && nsIndex < indexes.length)
    {
      int[][] lnIndexs = indexes[nsIndex];
      if (null != lnIndexs && lnIndex < lnIndexs.length)
      {
        int[] elems = lnIndexs[lnIndex];
        if (null != elems)
        {
          int pos = findGTE(elems, 1, elems[0], firstPotential);
          if (pos > -1)
          {
            return elems[pos];
          }
        }
      }
    }
    return NOTPROCESSED;
  }
  protected abstract int getNextNodeIdentity(int identity);
  protected abstract boolean nextNode();
  protected abstract int getNumberOfNodes();
  protected DTMAxisTraverser[] m_traversers;
  protected short _type(int identity)
  {
    int info = _exptype(identity);
    if (NULL != info)
      return m_expandedNameTable.getType(info);
    else
      return NULL;
  }
  protected int _exptype(int identity)
  {
  	if (identity == DTM.NULL)
  	return NULL;
    while (identity>=m_size)
    {
      if (!nextNode() && identity >= m_size)
        return NULL;
    }
    return m_exptype.elementAt(identity);
  }
  protected int _level(int identity)
  {
    while (identity>=m_size)
    {
      boolean isMore = nextNode();
      if (!isMore && identity >= m_size)
        return NULL;
    }
    int i=0;
    while(NULL != (identity=_parent(identity)))
      ++i;
    return i;
  }
  protected int _firstch(int identity)
  {
    int info = (identity >= m_size) ? NOTPROCESSED : m_firstch.elementAt(identity);
    while (info == NOTPROCESSED)
    {
      boolean isMore = nextNode();
      if (identity >= m_size &&!isMore)
        return NULL;
      else
      {
        info = m_firstch.elementAt(identity);
        if(info == NOTPROCESSED && !isMore)
          return NULL;
      }
    }
    return info;
  }
  protected int _nextsib(int identity)
  {
    int info = (identity >= m_size) ? NOTPROCESSED : m_nextsib.elementAt(identity);
    while (info == NOTPROCESSED)
    {
      boolean isMore = nextNode();
      if (identity >= m_size &&!isMore)
        return NULL;
      else
      {
        info = m_nextsib.elementAt(identity);
        if(info == NOTPROCESSED && !isMore)
          return NULL;
      }
    }
    return info;
  }
  protected int _prevsib(int identity)
  {
    if (identity < m_size)
      return m_prevsib.elementAt(identity);
    while (true)
    {
      boolean isMore = nextNode();
      if (identity >= m_size && !isMore)
        return NULL;
      else if (identity < m_size)
        return m_prevsib.elementAt(identity);
    }
  }
  protected int _parent(int identity)
  {
    if (identity < m_size)
      return m_parent.elementAt(identity);
    while (true)
    {
      boolean isMore = nextNode();
      if (identity >= m_size && !isMore)
        return NULL;
      else if (identity < m_size)
        return m_parent.elementAt(identity);
    }
  }
  public void dumpDTM(OutputStream os)
  {
    try
    {
      if(os==null)
      {
	      File f = new File("DTMDump"+((Object)this).hashCode()+".txt");
 	      System.err.println("Dumping... "+f.getAbsolutePath());
 	      os=new FileOutputStream(f);
      }
      PrintStream ps = new PrintStream(os);
      while (nextNode()){}
      int nRecords = m_size;
      ps.println("Total nodes: " + nRecords);
      for (int index = 0; index < nRecords; ++index)
      {
      	int i=makeNodeHandle(index);
        ps.println("=========== index=" + index + " handle=" + i + " ===========");
        ps.println("NodeName: " + getNodeName(i));
        ps.println("NodeNameX: " + getNodeNameX(i));
        ps.println("LocalName: " + getLocalName(i));
        ps.println("NamespaceURI: " + getNamespaceURI(i));
        ps.println("Prefix: " + getPrefix(i));
        int exTypeID = _exptype(index);
        ps.println("Expanded Type ID: "
                           + Integer.toHexString(exTypeID));
        int type = _type(index);
        String typestring;
        switch (type)
        {
        case DTM.ATTRIBUTE_NODE :
          typestring = "ATTRIBUTE_NODE";
          break;
        case DTM.CDATA_SECTION_NODE :
          typestring = "CDATA_SECTION_NODE";
          break;
        case DTM.COMMENT_NODE :
          typestring = "COMMENT_NODE";
          break;
        case DTM.DOCUMENT_FRAGMENT_NODE :
          typestring = "DOCUMENT_FRAGMENT_NODE";
          break;
        case DTM.DOCUMENT_NODE :
          typestring = "DOCUMENT_NODE";
          break;
        case DTM.DOCUMENT_TYPE_NODE :
          typestring = "DOCUMENT_NODE";
          break;
        case DTM.ELEMENT_NODE :
          typestring = "ELEMENT_NODE";
          break;
        case DTM.ENTITY_NODE :
          typestring = "ENTITY_NODE";
          break;
        case DTM.ENTITY_REFERENCE_NODE :
          typestring = "ENTITY_REFERENCE_NODE";
          break;
        case DTM.NAMESPACE_NODE :
          typestring = "NAMESPACE_NODE";
          break;
        case DTM.NOTATION_NODE :
          typestring = "NOTATION_NODE";
          break;
        case DTM.NULL :
          typestring = "NULL";
          break;
        case DTM.PROCESSING_INSTRUCTION_NODE :
          typestring = "PROCESSING_INSTRUCTION_NODE";
          break;
        case DTM.TEXT_NODE :
          typestring = "TEXT_NODE";
          break;
        default :
          typestring = "Unknown!";
          break;
        }
        ps.println("Type: " + typestring);
        int firstChild = _firstch(index);
        if (DTM.NULL == firstChild)
          ps.println("First child: DTM.NULL");
        else if (NOTPROCESSED == firstChild)
          ps.println("First child: NOTPROCESSED");
        else
          ps.println("First child: " + firstChild);
        if (m_prevsib != null)
        {
          int prevSibling = _prevsib(index);
          if (DTM.NULL == prevSibling)
            ps.println("Prev sibling: DTM.NULL");
          else if (NOTPROCESSED == prevSibling)
            ps.println("Prev sibling: NOTPROCESSED");
          else
            ps.println("Prev sibling: " + prevSibling);
        }
        int nextSibling = _nextsib(index);
        if (DTM.NULL == nextSibling)
          ps.println("Next sibling: DTM.NULL");
        else if (NOTPROCESSED == nextSibling)
          ps.println("Next sibling: NOTPROCESSED");
        else
          ps.println("Next sibling: " + nextSibling);
        int parent = _parent(index);
        if (DTM.NULL == parent)
          ps.println("Parent: DTM.NULL");
        else if (NOTPROCESSED == parent)
          ps.println("Parent: NOTPROCESSED");
        else
          ps.println("Parent: " + parent);
        int level = _level(index);
        ps.println("Level: " + level);
        ps.println("Node Value: " + getNodeValue(i));
        ps.println("String Value: " + getStringValue(i));
      }
    }
    catch(IOException ioe)
    {
      ioe.printStackTrace(System.err);
        throw new RuntimeException(ioe.getMessage());
    }
  }
  public String dumpNode(int nodeHandle)
  {	  
	  if(nodeHandle==DTM.NULL)
		  return "[null]";
        String typestring;
        switch (getNodeType(nodeHandle))
        {
        case DTM.ATTRIBUTE_NODE :
          typestring = "ATTR";
          break;
        case DTM.CDATA_SECTION_NODE :
          typestring = "CDATA";
          break;
        case DTM.COMMENT_NODE :
          typestring = "COMMENT";
          break;
        case DTM.DOCUMENT_FRAGMENT_NODE :
          typestring = "DOC_FRAG";
          break;
        case DTM.DOCUMENT_NODE :
          typestring = "DOC";
          break;
        case DTM.DOCUMENT_TYPE_NODE :
          typestring = "DOC_TYPE";
          break;
        case DTM.ELEMENT_NODE :
          typestring = "ELEMENT";
          break;
        case DTM.ENTITY_NODE :
          typestring = "ENTITY";
          break;
        case DTM.ENTITY_REFERENCE_NODE :
          typestring = "ENT_REF";
          break;
        case DTM.NAMESPACE_NODE :
          typestring = "NAMESPACE";
          break;
        case DTM.NOTATION_NODE :
          typestring = "NOTATION";
          break;
        case DTM.NULL :
          typestring = "null";
          break;
        case DTM.PROCESSING_INSTRUCTION_NODE :
          typestring = "PI";
          break;
        case DTM.TEXT_NODE :
          typestring = "TEXT";
          break;
        default :
          typestring = "Unknown!";
          break;
        }
      StringBuffer sb=new StringBuffer();
	  sb.append("["+nodeHandle+": "+typestring+
				"(0x"+Integer.toHexString(getExpandedTypeID(nodeHandle))+") "+
				getNodeNameX(nodeHandle)+" {"+getNamespaceURI(nodeHandle)+"}"+
				"=\""+ getNodeValue(nodeHandle)+"\"]");
	  return sb.toString();
  }
  public void setFeature(String featureId, boolean state){}
  public boolean hasChildNodes(int nodeHandle)
  {
    int identity = makeNodeIdentity(nodeHandle);
    int firstChild = _firstch(identity);
    return firstChild != DTM.NULL;
  }
  final public int makeNodeHandle(int nodeIdentity)
  {
    if(NULL==nodeIdentity) return NULL;
    if(JJK_DEBUG && nodeIdentity>DTMManager.IDENT_NODE_DEFAULT)
      System.err.println("GONK! (only useful in limited situations)");
    return m_dtmIdent.elementAt(nodeIdentity >>> DTMManager.IDENT_DTM_NODE_BITS)
      + (nodeIdentity & DTMManager.IDENT_NODE_DEFAULT) ;											
  }
  final public int makeNodeIdentity(int nodeHandle)
  {
    if(NULL==nodeHandle) return NULL;
    if(m_mgrDefault!=null)
    {
      int whichDTMindex=nodeHandle>>>DTMManager.IDENT_DTM_NODE_BITS;
      if(m_mgrDefault.m_dtms[whichDTMindex]!=this)
	return NULL;
      else
	return
	  m_mgrDefault.m_dtm_offsets[whichDTMindex]
	  | (nodeHandle & DTMManager.IDENT_NODE_DEFAULT);
    }
    int whichDTMid=m_dtmIdent.indexOf(nodeHandle & DTMManager.IDENT_DTM_DEFAULT);
    return (whichDTMid==NULL) 
      ? NULL
      : (whichDTMid << DTMManager.IDENT_DTM_NODE_BITS)
      + (nodeHandle & DTMManager.IDENT_NODE_DEFAULT);
  }
  public int getFirstChild(int nodeHandle)
  {
    int identity = makeNodeIdentity(nodeHandle);
    int firstChild = _firstch(identity);
    return makeNodeHandle(firstChild);
  }
  public int getTypedFirstChild(int nodeHandle, int nodeType)
  {
    int firstChild, eType;
    if (nodeType < DTM.NTYPES) {
      for (firstChild = _firstch(makeNodeIdentity(nodeHandle));
           firstChild != DTM.NULL;
           firstChild = _nextsib(firstChild)) {
        eType = _exptype(firstChild);
        if (eType == nodeType
               || (eType >= DTM.NTYPES
                      && m_expandedNameTable.getType(eType) == nodeType)) {
          return makeNodeHandle(firstChild);
        }
      }
    } else {
      for (firstChild = _firstch(makeNodeIdentity(nodeHandle));
           firstChild != DTM.NULL;
           firstChild = _nextsib(firstChild)) {
        if (_exptype(firstChild) == nodeType) {
          return makeNodeHandle(firstChild);
        }
      }
    }
    return DTM.NULL;
  }
  public int getLastChild(int nodeHandle)
  {
    int identity = makeNodeIdentity(nodeHandle);
    int child = _firstch(identity);
    int lastChild = DTM.NULL;
    while (child != DTM.NULL)
    {
      lastChild = child;
      child = _nextsib(child);
    }
    return makeNodeHandle(lastChild);
  }
  public abstract int getAttributeNode(int nodeHandle, String namespaceURI,
                                       String name);
  public int getFirstAttribute(int nodeHandle)
  {
    int nodeID = makeNodeIdentity(nodeHandle);
    return makeNodeHandle(getFirstAttributeIdentity(nodeID));
  }
  protected int getFirstAttributeIdentity(int identity) {
    int type = _type(identity);
    if (DTM.ELEMENT_NODE == type)
    {
      while (DTM.NULL != (identity = getNextNodeIdentity(identity)))
      {
        type = _type(identity);
        if (type == DTM.ATTRIBUTE_NODE)
        {
          return identity;
        }
        else if (DTM.NAMESPACE_NODE != type)
        {
          break;
        }
      }
    }
    return DTM.NULL;
  }
  protected int getTypedAttribute(int nodeHandle, int attType) {
    int type = getNodeType(nodeHandle);
    if (DTM.ELEMENT_NODE == type) {
      int identity = makeNodeIdentity(nodeHandle);
      while (DTM.NULL != (identity = getNextNodeIdentity(identity)))
      {
        type = _type(identity);
        if (type == DTM.ATTRIBUTE_NODE)
        {
          if (_exptype(identity) == attType) return makeNodeHandle(identity);
        }
        else if (DTM.NAMESPACE_NODE != type)
        {
          break;
        }
      }
    }
    return DTM.NULL;
  }
  public int getNextSibling(int nodeHandle)
  {
  	if (nodeHandle == DTM.NULL)
  	return DTM.NULL;
    return makeNodeHandle(_nextsib(makeNodeIdentity(nodeHandle)));
  }
  public int getTypedNextSibling(int nodeHandle, int nodeType)
  {
  	if (nodeHandle == DTM.NULL)
  	return DTM.NULL;
  	int node = makeNodeIdentity(nodeHandle);
  	int eType;
  	while ((node = _nextsib(node)) != DTM.NULL && 
  	((eType = _exptype(node)) != nodeType && 
  	m_expandedNameTable.getType(eType)!= nodeType)); 
    return (node == DTM.NULL ? DTM.NULL : makeNodeHandle(node));
  }
  public int getPreviousSibling(int nodeHandle)
  {
    if (nodeHandle == DTM.NULL)
      return DTM.NULL;
    if (m_prevsib != null)
      return makeNodeHandle(_prevsib(makeNodeIdentity(nodeHandle)));
    else
    {
      int nodeID = makeNodeIdentity(nodeHandle);
      int parent = _parent(nodeID);
      int node = _firstch(parent);
      int result = DTM.NULL;
      while (node != nodeID)
      {
        result = node;
        node = _nextsib(node);
      }
      return makeNodeHandle(result);
    }
  }
  public int getNextAttribute(int nodeHandle) {
    int nodeID = makeNodeIdentity(nodeHandle);
    if (_type(nodeID) == DTM.ATTRIBUTE_NODE) {
      return makeNodeHandle(getNextAttributeIdentity(nodeID));
    }
    return DTM.NULL;
  }
  protected int getNextAttributeIdentity(int identity) {
    while (DTM.NULL != (identity = getNextNodeIdentity(identity))) {
      int type = _type(identity);
      if (type == DTM.ATTRIBUTE_NODE) {
        return identity;
      } else if (type != DTM.NAMESPACE_NODE) {
        break;
      }
    }
    return DTM.NULL;
  }
  private Vector m_namespaceLists = null;  
  protected void declareNamespaceInContext(int elementNodeIndex,int namespaceNodeIndex)
  {
    SuballocatedIntVector nsList=null;
    if(m_namespaceDeclSets==null)
      {
        m_namespaceDeclSetElements=new SuballocatedIntVector(32);
        m_namespaceDeclSetElements.addElement(elementNodeIndex);
        m_namespaceDeclSets=new Vector();
        nsList=new SuballocatedIntVector(32);
        m_namespaceDeclSets.addElement(nsList);
      }
    else
      {
        int last=m_namespaceDeclSetElements.size()-1;
        if(last>=0 && elementNodeIndex==m_namespaceDeclSetElements.elementAt(last))
          {
            nsList=(SuballocatedIntVector)m_namespaceDeclSets.elementAt(last);
          }
      }
    if(nsList==null)
      {
        m_namespaceDeclSetElements.addElement(elementNodeIndex);
        SuballocatedIntVector inherited =
                                findNamespaceContext(_parent(elementNodeIndex));
        if (inherited!=null) {
            int isize=inherited.size();
            nsList=new SuballocatedIntVector(Math.max(Math.min(isize+16,2048),
                                                      32));
            for(int i=0;i<isize;++i)
              {
                nsList.addElement(inherited.elementAt(i));
              }
        } else {
            nsList=new SuballocatedIntVector(32);
        }
        m_namespaceDeclSets.addElement(nsList);
      }
    int newEType=_exptype(namespaceNodeIndex);
    for(int i=nsList.size()-1;i>=0;--i)
      {
        if(newEType==getExpandedTypeID(nsList.elementAt(i)))
          {
            nsList.setElementAt(makeNodeHandle(namespaceNodeIndex),i);
            return;
          }
      }
    nsList.addElement(makeNodeHandle(namespaceNodeIndex));
  }
  protected SuballocatedIntVector findNamespaceContext(int elementNodeIndex)
  {
    if (null!=m_namespaceDeclSetElements)
      {
        int wouldBeAt=findInSortedSuballocatedIntVector(m_namespaceDeclSetElements,
                                            elementNodeIndex);
        if(wouldBeAt>=0) 
          return (SuballocatedIntVector) m_namespaceDeclSets.elementAt(wouldBeAt);
        if(wouldBeAt == -1) 
          return null; 
        wouldBeAt=-1-wouldBeAt;
        int candidate=m_namespaceDeclSetElements.elementAt(-- wouldBeAt);
        int ancestor=_parent(elementNodeIndex);
        if (wouldBeAt == 0 && candidate < ancestor) {
          int rootHandle = getDocumentRoot(makeNodeHandle(elementNodeIndex));
          int rootID = makeNodeIdentity(rootHandle);
          int uppermostNSCandidateID;
          if (getNodeType(rootHandle) == DTM.DOCUMENT_NODE) {
            int ch = _firstch(rootID);
            uppermostNSCandidateID = (ch != DTM.NULL) ? ch : rootID;
          } else {
            uppermostNSCandidateID = rootID;
          }
          if (candidate == uppermostNSCandidateID) {
            return (SuballocatedIntVector)m_namespaceDeclSets.elementAt(wouldBeAt);
          }
        }
        while(wouldBeAt>=0 && ancestor>0)
          {
            if (candidate==ancestor) {
                return (SuballocatedIntVector)m_namespaceDeclSets.elementAt(wouldBeAt);
            } else if (candidate<ancestor) {
                do {
                  ancestor=_parent(ancestor);
                } while (candidate < ancestor);
            } else if(wouldBeAt > 0){
              candidate=m_namespaceDeclSetElements.elementAt(--wouldBeAt);
            }
            else
            	break;
          }
      }
    return null; 
  }
  protected int findInSortedSuballocatedIntVector(SuballocatedIntVector vector, int lookfor)
  {
    int i = 0;
    if(vector != null) {
      int first = 0;
      int last  = vector.size() - 1;
      while (first <= last) {
        i = (first + last) / 2;
        int test = lookfor-vector.elementAt(i);
        if(test == 0) {
          return i; 
        }
        else if (test < 0) {
          last = i - 1; 
        }
        else {
          first = i + 1; 
        }
      }
      if (first > i) {
        i = first; 
      }
    }
    return -1 - i; 
  }
  public int getFirstNamespaceNode(int nodeHandle, boolean inScope)
  {
        if(inScope)
        {
            int identity = makeNodeIdentity(nodeHandle);
            if (_type(identity) == DTM.ELEMENT_NODE)
            {
              SuballocatedIntVector nsContext=findNamespaceContext(identity);
              if(nsContext==null || nsContext.size()<1)
                return NULL;
              return nsContext.elementAt(0);
            }
            else
              return NULL;
          }
        else
          {
            int identity = makeNodeIdentity(nodeHandle);
            if (_type(identity) == DTM.ELEMENT_NODE)
            {
              while (DTM.NULL != (identity = getNextNodeIdentity(identity)))
              {
                int type = _type(identity);
                if (type == DTM.NAMESPACE_NODE)
                    return makeNodeHandle(identity);
                else if (DTM.ATTRIBUTE_NODE != type)
                    break;
              }
              return NULL;
            }
            else
              return NULL;
          }
  }
  public int getNextNamespaceNode(int baseHandle, int nodeHandle,
                                  boolean inScope)
  {
        if(inScope)
          {
                SuballocatedIntVector nsContext=findNamespaceContext(makeNodeIdentity(baseHandle));
            if(nsContext==null)
              return NULL;
            int i=1 + nsContext.indexOf(nodeHandle);
            if(i<=0 || i==nsContext.size())
              return NULL;
            return nsContext.elementAt(i);
          }
        else
          {
            int identity = makeNodeIdentity(nodeHandle);
            while (DTM.NULL != (identity = getNextNodeIdentity(identity)))
              {
                int type = _type(identity);
                if (type == DTM.NAMESPACE_NODE)
                  {
                    return makeNodeHandle(identity);
                  }
                else if (type != DTM.ATTRIBUTE_NODE)
                  {
                    break;
                  }
              }
          }
     return DTM.NULL;
  }
  public int getParent(int nodeHandle)
  {
    int identity = makeNodeIdentity(nodeHandle);
    if (identity > 0)
      return makeNodeHandle(_parent(identity));
    else
      return DTM.NULL;
  }
  public int getDocument()
  {
    return m_dtmIdent.elementAt(0); 
  }
  public int getOwnerDocument(int nodeHandle)
  {
    if (DTM.DOCUMENT_NODE == getNodeType(nodeHandle))
  	    return DTM.NULL;
    return getDocumentRoot(nodeHandle);
  }
  public int getDocumentRoot(int nodeHandle)
  {
    return getManager().getDTM(nodeHandle).getDocument();
  }
  public abstract XMLString getStringValue(int nodeHandle);
  public int getStringValueChunkCount(int nodeHandle)
  {
    error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED, null));
    return 0;
  }
  public char[] getStringValueChunk(int nodeHandle, int chunkIndex,
                                    int[] startAndLen)
  {
    error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED, null));
    return null;
  }
  public int getExpandedTypeID(int nodeHandle)
  {
    int id=makeNodeIdentity(nodeHandle);
    if(id==NULL)
      return NULL;
    return _exptype(id);
  }
  public int getExpandedTypeID(String namespace, String localName, int type)
  {
    ExpandedNameTable ent = m_expandedNameTable;
    return ent.getExpandedTypeID(namespace, localName, type);
  }
  public String getLocalNameFromExpandedNameID(int expandedNameID)
  {
    return m_expandedNameTable.getLocalName(expandedNameID);
  }
  public String getNamespaceFromExpandedNameID(int expandedNameID)
  {
    return m_expandedNameTable.getNamespace(expandedNameID);
  }
  public int getNamespaceType(final int nodeHandle)
  {
    int identity = makeNodeIdentity(nodeHandle);
    int expandedNameID = _exptype(identity);
    return m_expandedNameTable.getNamespaceID(expandedNameID);
  }
  public abstract String getNodeName(int nodeHandle);
  public String getNodeNameX(int nodeHandle)
  {
    error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED, null));
    return null;
  }
  public abstract String getLocalName(int nodeHandle);
  public abstract String getPrefix(int nodeHandle);
  public abstract String getNamespaceURI(int nodeHandle);
  public abstract String getNodeValue(int nodeHandle);
  public short getNodeType(int nodeHandle)
  {
  	if (nodeHandle == DTM.NULL)
  	return DTM.NULL;
    return m_expandedNameTable.getType(_exptype(makeNodeIdentity(nodeHandle)));
  }
  public short getLevel(int nodeHandle)
  {
    int identity = makeNodeIdentity(nodeHandle);
    return (short) (_level(identity) + 1);
  }
  public int getNodeIdent(int nodeHandle)
  {
      return makeNodeIdentity(nodeHandle); 
  }
  public int getNodeHandle(int nodeId)
  {
      return makeNodeHandle(nodeId);
  }
  public boolean isSupported(String feature, String version)
  {
    return false;
  }
  public String getDocumentBaseURI()
  {
    return m_documentBaseURI;
  }
  public void setDocumentBaseURI(String baseURI)
  {
    m_documentBaseURI = baseURI;
  }
  public String getDocumentSystemIdentifier(int nodeHandle)
  {
    return m_documentBaseURI;
  }
  public String getDocumentEncoding(int nodeHandle)
  {
    return "UTF-8";
  }
  public String getDocumentStandalone(int nodeHandle)
  {
    return null;
  }
  public String getDocumentVersion(int documentHandle)
  {
    return null;
  }
  public boolean getDocumentAllDeclarationsProcessed()
  {
    return true;
  }
  public abstract String getDocumentTypeDeclarationSystemIdentifier();
  public abstract String getDocumentTypeDeclarationPublicIdentifier();
  public abstract int getElementById(String elementId);
  public abstract String getUnparsedEntityURI(String name);
  public boolean supportsPreStripping()
  {
    return true;
  }
  public boolean isNodeAfter(int nodeHandle1, int nodeHandle2)
  {
    int index1 = makeNodeIdentity(nodeHandle1);
    int index2 = makeNodeIdentity(nodeHandle2);
    return index1!=NULL && index2!=NULL && index1 <= index2;
  }
  public boolean isCharacterElementContentWhitespace(int nodeHandle)
  {
    return false;
  }
  public boolean isDocumentAllDeclarationsProcessed(int documentHandle)
  {
    return true;
  }
  public abstract boolean isAttributeSpecified(int attributeHandle);
  public abstract void dispatchCharactersEvents(
    int nodeHandle, org.xml.sax.ContentHandler ch, boolean normalize)
      throws org.xml.sax.SAXException;
  public abstract void dispatchToEvents(
    int nodeHandle, org.xml.sax.ContentHandler ch)
      throws org.xml.sax.SAXException;
  public org.w3c.dom.Node getNode(int nodeHandle)
  {
    return new DTMNodeProxy(this, nodeHandle);
  }
  public void appendChild(int newChild, boolean clone, boolean cloneDepth)
  {
    error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED, null));
  }
  public void appendTextChild(String str)
  {
    error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED, null));
  }
  protected void error(String msg)
  {
    throw new DTMException(msg);
  }
  protected boolean getShouldStripWhitespace()
  {
    return m_shouldStripWS;
  }
  protected void pushShouldStripWhitespace(boolean shouldStrip)
  {
    m_shouldStripWS = shouldStrip;
    if (null != m_shouldStripWhitespaceStack)
      m_shouldStripWhitespaceStack.push(shouldStrip);
  }
  protected void popShouldStripWhitespace()
  {
    if (null != m_shouldStripWhitespaceStack)
      m_shouldStripWS = m_shouldStripWhitespaceStack.popAndTop();
  }
  protected void setShouldStripWhitespace(boolean shouldStrip)
  {
    m_shouldStripWS = shouldStrip;
    if (null != m_shouldStripWhitespaceStack)
      m_shouldStripWhitespaceStack.setTop(shouldStrip);
  }
   public void documentRegistration()
   {
   }
   public void documentRelease()
   {
   }
   public void migrateTo(DTMManager mgr)
   {
     m_mgr = mgr;
     if(mgr instanceof DTMManagerDefault)
       m_mgrDefault=(DTMManagerDefault)mgr;     
   }      
	 public DTMManager getManager()
	 {
		 return m_mgr;
	 }
	 public SuballocatedIntVector getDTMIDs()
	 {
		 if(m_mgr==null) return null;
		 return m_dtmIdent;
	 }
}
