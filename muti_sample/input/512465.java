public class SAX2DTM2 extends SAX2DTM
{
  public final class ChildrenIterator extends InternalAxisIteratorBase
  {
    public DTMAxisIterator setStartNode(int node)
    {
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;
        _currentNode = (node == DTM.NULL) ? DTM.NULL
                                          : _firstch2(makeNodeIdentity(node));
        return resetPosition();
      }
      return this;
    }
    public int next()
    {
      if (_currentNode != NULL) {
        int node = _currentNode;
        _currentNode = _nextsib2(node);
        return returnNode(makeNodeHandle(node));
      }
      return END;
    }
  }  
  public final class ParentIterator extends InternalAxisIteratorBase
  {
    private int _nodeType = DTM.NULL;
    public DTMAxisIterator setStartNode(int node)
    {
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;
        if (node != DTM.NULL)
          _currentNode = _parent2(makeNodeIdentity(node));
        else
          _currentNode = DTM.NULL;
        return resetPosition();
      }
      return this;
    }
    public DTMAxisIterator setNodeType(final int type)
    {
      _nodeType = type;
      return this;
    }
    public int next()
    {
      int result = _currentNode;
      if (result == END)
        return DTM.NULL;
      if (_nodeType == NULL) {
        _currentNode = END;
        return returnNode(makeNodeHandle(result));
      }
      else if (_nodeType >= DTM.NTYPES) {
        if (_nodeType == _exptype2(result)) {
          _currentNode = END;
	  return returnNode(makeNodeHandle(result));
        }
      }
      else {
        if (_nodeType == _type2(result)) {
	  _currentNode = END;
	  return returnNode(makeNodeHandle(result));
        }
      }
      return DTM.NULL;
    }
  }  
  public final class TypedChildrenIterator extends InternalAxisIteratorBase
  {
    private final int _nodeType;
    public TypedChildrenIterator(int nodeType)
    {
      _nodeType = nodeType;
    }
    public DTMAxisIterator setStartNode(int node)
    {
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;
        _currentNode = (node == DTM.NULL)
                                   ? DTM.NULL
                                   : _firstch2(makeNodeIdentity(_startNode));
        return resetPosition();
      }
      return this;
    }
    public int next()
    {
      int node = _currentNode;
      if (node == DTM.NULL)
        return DTM.NULL;
      final int nodeType = _nodeType;
      if (nodeType != DTM.ELEMENT_NODE) {
        while (node != DTM.NULL && _exptype2(node) != nodeType) {
          node = _nextsib2(node);
        }
      }
      else {
      	int eType;
      	while (node != DTM.NULL) {
      	  eType = _exptype2(node);
      	  if (eType >= DTM.NTYPES)
      	    break;
      	  else
      	    node = _nextsib2(node);
      	}
      }
      if (node == DTM.NULL) {
        _currentNode = DTM.NULL;
        return DTM.NULL;
      } else {
        _currentNode = _nextsib2(node);
        return returnNode(makeNodeHandle(node));
      }
    }
    public int getNodeByPosition(int position)
    {
      if (position <= 0)
        return DTM.NULL;
      int node = _currentNode;
      int pos = 0;
      final int nodeType = _nodeType;
      if (nodeType != DTM.ELEMENT_NODE) {
        while (node != DTM.NULL) {
          if (_exptype2(node) == nodeType) {
            pos++;
            if (pos == position)
              return makeNodeHandle(node);
          }
          node = _nextsib2(node);
        }
        return NULL;
      }
      else {
      	while (node != DTM.NULL) {
      	  if (_exptype2(node) >= DTM.NTYPES) {
      	    pos++;
      	    if (pos == position)
      	      return makeNodeHandle(node);
      	  }
      	  node = _nextsib2(node);
      	}
      	return NULL;
      }
    }
  }  
  public class TypedRootIterator extends RootIterator
  {
    private final int _nodeType;
    public TypedRootIterator(int nodeType)
    {
      super();
      _nodeType = nodeType;
    }
    public int next()
    {
      if(_startNode == _currentNode)
        return NULL;
      final int node = _startNode;
      int expType = _exptype2(makeNodeIdentity(node));
      _currentNode = node;
      if (_nodeType >= DTM.NTYPES) {
        if (_nodeType == expType) {
          return returnNode(node);
        }
      }
      else {
        if (expType < DTM.NTYPES) {
          if (expType == _nodeType) {
            return returnNode(node);
          }
        }
        else {
          if (m_extendedTypes[expType].getNodeType() == _nodeType) {
            return returnNode(node);
          }
        }
      }
      return NULL;
    }
  }  
  public class FollowingSiblingIterator extends InternalAxisIteratorBase
  {
    public DTMAxisIterator setStartNode(int node)
    {
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;
        _currentNode = makeNodeIdentity(node);
        return resetPosition();
      }
      return this;
    }
    public int next()
    {
      _currentNode = (_currentNode == DTM.NULL) ? DTM.NULL
                                                : _nextsib2(_currentNode);
      return returnNode(makeNodeHandle(_currentNode));
    }
  }  
  public final class TypedFollowingSiblingIterator
          extends FollowingSiblingIterator
  {
    private final int _nodeType;
    public TypedFollowingSiblingIterator(int type)
    {
      _nodeType = type;
    }
    public int next()
    {
      if (_currentNode == DTM.NULL) {
        return DTM.NULL;
      }
      int node = _currentNode;
      final int nodeType = _nodeType;
      if (nodeType != DTM.ELEMENT_NODE) {
        while ((node = _nextsib2(node)) != DTM.NULL && _exptype2(node) != nodeType) {}
      }
      else {
        while ((node = _nextsib2(node)) != DTM.NULL && _exptype2(node) < DTM.NTYPES) {}
      }
      _currentNode = node;
      return (node == DTM.NULL)
                      ? DTM.NULL
                      : returnNode(makeNodeHandle(node));
    }
  }  
  public final class AttributeIterator extends InternalAxisIteratorBase
  {
    public DTMAxisIterator setStartNode(int node)
    {
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;
        _currentNode = getFirstAttributeIdentity(makeNodeIdentity(node));
        return resetPosition();
      }
      return this;
    }
    public int next()
    {
      final int node = _currentNode;
      if (node != NULL) {
        _currentNode = getNextAttributeIdentity(node);
        return returnNode(makeNodeHandle(node));
      }
      return NULL;
    }
  }  
  public final class TypedAttributeIterator extends InternalAxisIteratorBase
  {
    private final int _nodeType;
    public TypedAttributeIterator(int nodeType)
    {
      _nodeType = nodeType;
    }
    public DTMAxisIterator setStartNode(int node)
    {
      if (_isRestartable)
      {
        _startNode = node;
        _currentNode = getTypedAttribute(node, _nodeType);
        return resetPosition();
      }
      return this;
    }
    public int next()
    {
      final int node = _currentNode;
      _currentNode = NULL;
      return returnNode(node);
    }
  }  
  public class PrecedingSiblingIterator extends InternalAxisIteratorBase
  {
    protected int _startNodeID;
    public boolean isReverse()
    {
      return true;
    }
    public DTMAxisIterator setStartNode(int node)
    {
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;
        node = _startNodeID = makeNodeIdentity(node);
        if(node == NULL)
        {
          _currentNode = node;
          return resetPosition();
        }
        int type = _type2(node);
        if(ExpandedNameTable.ATTRIBUTE == type
           || ExpandedNameTable.NAMESPACE == type )
        {
          _currentNode = node;
        }
        else
        {
          _currentNode = _parent2(node);
          if(NULL!=_currentNode)
            _currentNode = _firstch2(_currentNode);
          else
            _currentNode = node;
        }
        return resetPosition();
      }
      return this;
    }
    public int next()
    {
      if (_currentNode == _startNodeID || _currentNode == DTM.NULL)
      {
        return NULL;
      }
      else
      {
        final int node = _currentNode;
        _currentNode = _nextsib2(node);
        return returnNode(makeNodeHandle(node));
      }
    }
  }  
  public final class TypedPrecedingSiblingIterator
          extends PrecedingSiblingIterator
  {
    private final int _nodeType;
    public TypedPrecedingSiblingIterator(int type)
    {
      _nodeType = type;
    }
    public int next()
    {
      int node = _currentNode;
      final int nodeType = _nodeType;
      final int startNodeID = _startNodeID;
      if (nodeType != DTM.ELEMENT_NODE) {
        while (node != NULL && node != startNodeID && _exptype2(node) != nodeType) {
          node = _nextsib2(node);
        }
      }
      else {
        while (node != NULL && node != startNodeID && _exptype2(node) < DTM.NTYPES) {
          node = _nextsib2(node);
        }
      }
      if (node == DTM.NULL || node == startNodeID) {
        _currentNode = NULL;
        return NULL;
      }
      else {
        _currentNode = _nextsib2(node);
        return returnNode(makeNodeHandle(node));
      }
    }
    public int getLast()
    {
      if (_last != -1)
        return _last;
      setMark();
      int node = _currentNode;
      final int nodeType = _nodeType;
      final int startNodeID = _startNodeID;
      int last = 0;
      if (nodeType != DTM.ELEMENT_NODE) {
        while (node != NULL && node != startNodeID) {
          if (_exptype2(node) == nodeType) {
            last++;
          }
          node = _nextsib2(node);
        }
      }
      else {
        while (node != NULL && node != startNodeID) {
          if (_exptype2(node) >= DTM.NTYPES) {
            last++;
          }
          node = _nextsib2(node);
        }
      }
      gotoMark();
      return (_last = last);
    }
  }  
  public class PrecedingIterator extends InternalAxisIteratorBase
  {
    private final int _maxAncestors = 8;
    protected int[] _stack = new int[_maxAncestors];
    protected int _sp, _oldsp;
    protected int _markedsp, _markedNode, _markedDescendant;
    public boolean isReverse()
    {
      return true;
    }
    public DTMAxisIterator cloneIterator()
    {
      _isRestartable = false;
      try
      {
        final PrecedingIterator clone = (PrecedingIterator) super.clone();
        final int[] stackCopy = new int[_stack.length];
        System.arraycopy(_stack, 0, stackCopy, 0, _stack.length);
        clone._stack = stackCopy;
        return clone;
      }
      catch (CloneNotSupportedException e)
      {
        throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ITERATOR_CLONE_NOT_SUPPORTED, null)); 
      }
    }
    public DTMAxisIterator setStartNode(int node)
    {
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        node = makeNodeIdentity(node);
        int parent, index;
       if (_type2(node) == DTM.ATTRIBUTE_NODE)
         node = _parent2(node);
        _startNode = node;
        _stack[index = 0] = node;
       	parent=node;
	while ((parent = _parent2(parent)) != NULL)
	{
	  if (++index == _stack.length)
	  {
	    final int[] stack = new int[index*2];
	    System.arraycopy(_stack, 0, stack, 0, index);
	    _stack = stack;
	  }
	  _stack[index] = parent;
        }
        if(index>0)
	  --index; 
        _currentNode=_stack[index]; 
        _oldsp = _sp = index;
        return resetPosition();
      }
      return this;
    }
    public int next()
    {
   	for(++_currentNode; _sp>=0; ++_currentNode)
   	{
   	  if(_currentNode < _stack[_sp])
   	  {
   	    int type = _type2(_currentNode);
   	    if(type != ATTRIBUTE_NODE && type != NAMESPACE_NODE)
   	      return returnNode(makeNodeHandle(_currentNode));
   	  }
   	  else
   	    --_sp;
   	}
   	return NULL;
    }
    public DTMAxisIterator reset()
    {
      _sp = _oldsp;
      return resetPosition();
    }
    public void setMark() {
        _markedsp = _sp;
        _markedNode = _currentNode;
        _markedDescendant = _stack[0];
    }
    public void gotoMark() {
        _sp = _markedsp;
        _currentNode = _markedNode;
    }
  }  
  public final class TypedPrecedingIterator extends PrecedingIterator
  {
    private final int _nodeType;
    public TypedPrecedingIterator(int type)
    {
      _nodeType = type;
    }
    public int next()
    {
      int node = _currentNode;
      final int nodeType = _nodeType;
      if (nodeType >= DTM.NTYPES) {
        while (true) {
          node++;
          if (_sp < 0) {
            node = NULL;
            break;
          }
          else if (node >= _stack[_sp]) {
            if (--_sp < 0) {
              node = NULL;
              break;
            }
          }
          else if (_exptype2(node) == nodeType) {
            break;
          }
        }
      }
      else {
        int expType;
        while (true) {
          node++;
          if (_sp < 0) {
            node = NULL;
            break;
          }
          else if (node >= _stack[_sp]) {
            if (--_sp < 0) {
              node = NULL;
              break;
            }
          }
          else {
            expType = _exptype2(node);
            if (expType < DTM.NTYPES) {
              if (expType == nodeType) {
                break;
              }
            }
            else {
              if (m_extendedTypes[expType].getNodeType() == nodeType) {
                break;
              }
            }
          }
        }
      }
      _currentNode = node;
      return (node == NULL) ? NULL : returnNode(makeNodeHandle(node));
    }
  }  
  public class FollowingIterator extends InternalAxisIteratorBase
  {
    public FollowingIterator()
    {
    }
    public DTMAxisIterator setStartNode(int node)
    {
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;
        node = makeNodeIdentity(node);
        int first;
        int type = _type2(node);
        if ((DTM.ATTRIBUTE_NODE == type) || (DTM.NAMESPACE_NODE == type))
        {
          node = _parent2(node);
          first = _firstch2(node);
          if (NULL != first) {
            _currentNode = makeNodeHandle(first);
            return resetPosition();
          }
        }
        do
        {
          first = _nextsib2(node);
          if (NULL == first)
            node = _parent2(node);
        }
        while (NULL == first && NULL != node);
        _currentNode = makeNodeHandle(first);
        return resetPosition();
      }
      return this;
    }
    public int next()
    {
      int node = _currentNode;
      int current = makeNodeIdentity(node);
      while (true)
      {
        current++;
        int type = _type2(current);
        if (NULL == type) {
          _currentNode = NULL;
          return returnNode(node);
        }
        if (ATTRIBUTE_NODE == type || NAMESPACE_NODE == type)
          continue;
        _currentNode = makeNodeHandle(current);
        return returnNode(node);
      }
    }
  }  
  public final class TypedFollowingIterator extends FollowingIterator
  {
    private final int _nodeType;
    public TypedFollowingIterator(int type)
    {
      _nodeType = type;
    }
    public int next()
    {
      int current;
      int node;
      int type;
      final int nodeType = _nodeType;
      int currentNodeID = makeNodeIdentity(_currentNode);
      if (nodeType >= DTM.NTYPES) {
        do {
          node = currentNodeID;
	  current = node;
          do {
            current++;
            type = _type2(current);
          }
          while (type != NULL && (ATTRIBUTE_NODE == type || NAMESPACE_NODE == type));
          currentNodeID = (type != NULL) ? current : NULL;
        }
        while (node != DTM.NULL && _exptype2(node) != nodeType);
      }
      else {
        do {
          node = currentNodeID;
	  current = node;
          do {
            current++;
            type = _type2(current);
          }
          while (type != NULL && (ATTRIBUTE_NODE == type || NAMESPACE_NODE == type));
          currentNodeID = (type != NULL) ? current : NULL;
        }
        while (node != DTM.NULL
               && (_exptype2(node) != nodeType && _type2(node) != nodeType));
      }
      _currentNode = makeNodeHandle(currentNodeID);
      return (node == DTM.NULL ? DTM.NULL :returnNode(makeNodeHandle(node)));
    }
  }  
  public class AncestorIterator extends InternalAxisIteratorBase
  {
    private static final int m_blocksize = 32;
    int[] m_ancestors = new int[m_blocksize];
    int m_size = 0;
    int m_ancestorsPos;
    int m_markedPos;
    int m_realStartNode;
    public int getStartNode()
    {
      return m_realStartNode;
    }
    public final boolean isReverse()
    {
      return true;
    }
    public DTMAxisIterator cloneIterator()
    {
      _isRestartable = false;  
      try
      {
        final AncestorIterator clone = (AncestorIterator) super.clone();
        clone._startNode = _startNode;
        return clone;
      }
      catch (CloneNotSupportedException e)
      {
        throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ITERATOR_CLONE_NOT_SUPPORTED, null)); 
      }
    }
    public DTMAxisIterator setStartNode(int node)
    {
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      m_realStartNode = node;
      if (_isRestartable)
      {
        int nodeID = makeNodeIdentity(node);
        m_size = 0;
        if (nodeID == DTM.NULL) {
          _currentNode = DTM.NULL;
          m_ancestorsPos = 0;
          return this;
        }
        if (!_includeSelf) {
          nodeID = _parent2(nodeID);
          node = makeNodeHandle(nodeID);
        }
        _startNode = node;
        while (nodeID != END) {
          if (m_size >= m_ancestors.length)
          {
            int[] newAncestors = new int[m_size * 2];
            System.arraycopy(m_ancestors, 0, newAncestors, 0, m_ancestors.length);
            m_ancestors = newAncestors;
          }
          m_ancestors[m_size++] = node;
          nodeID = _parent2(nodeID);
          node = makeNodeHandle(nodeID);
        }
        m_ancestorsPos = m_size - 1;
        _currentNode = (m_ancestorsPos>=0)
                               ? m_ancestors[m_ancestorsPos]
                               : DTM.NULL;
        return resetPosition();
      }
      return this;
    }
    public DTMAxisIterator reset()
    {
      m_ancestorsPos = m_size - 1;
      _currentNode = (m_ancestorsPos >= 0) ? m_ancestors[m_ancestorsPos]
                                         : DTM.NULL;
      return resetPosition();
    }
    public int next()
    {
      int next = _currentNode;
      int pos = --m_ancestorsPos;
      _currentNode = (pos >= 0) ? m_ancestors[m_ancestorsPos]
                                : DTM.NULL;
      return returnNode(next);
    }
    public void setMark() {
        m_markedPos = m_ancestorsPos;
    }
    public void gotoMark() {
        m_ancestorsPos = m_markedPos;
        _currentNode = m_ancestorsPos>=0 ? m_ancestors[m_ancestorsPos]
                                         : DTM.NULL;
    }
  }  
  public final class TypedAncestorIterator extends AncestorIterator
  {
    private final int _nodeType;
    public TypedAncestorIterator(int type)
    {
      _nodeType = type;
    }
    public DTMAxisIterator setStartNode(int node)
    {
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      m_realStartNode = node;
      if (_isRestartable)
      {
        int nodeID = makeNodeIdentity(node);
        m_size = 0;
        if (nodeID == DTM.NULL) {
          _currentNode = DTM.NULL;
          m_ancestorsPos = 0;
          return this;
        }
        final int nodeType = _nodeType;
        if (!_includeSelf) {
          nodeID = _parent2(nodeID);
          node = makeNodeHandle(nodeID);
        }
        _startNode = node;
        if (nodeType >= DTM.NTYPES) {
          while (nodeID != END) {
            int eType = _exptype2(nodeID);
            if (eType == nodeType) {
              if (m_size >= m_ancestors.length)
              {
              	int[] newAncestors = new int[m_size * 2];
              	System.arraycopy(m_ancestors, 0, newAncestors, 0, m_ancestors.length);
              	m_ancestors = newAncestors;
              }
              m_ancestors[m_size++] = makeNodeHandle(nodeID);
            }
            nodeID = _parent2(nodeID);
          }
        }
        else {
          while (nodeID != END) {
            int eType = _exptype2(nodeID);
            if ((eType < DTM.NTYPES && eType == nodeType)
                || (eType >= DTM.NTYPES
                    && m_extendedTypes[eType].getNodeType() == nodeType)) {
              if (m_size >= m_ancestors.length)
              {
              	int[] newAncestors = new int[m_size * 2];
              	System.arraycopy(m_ancestors, 0, newAncestors, 0, m_ancestors.length);
              	m_ancestors = newAncestors;
              }
              m_ancestors[m_size++] = makeNodeHandle(nodeID);
            }
            nodeID = _parent2(nodeID);
          }
        }
        m_ancestorsPos = m_size - 1;
        _currentNode = (m_ancestorsPos>=0)
                               ? m_ancestors[m_ancestorsPos]
                               : DTM.NULL;
        return resetPosition();
      }
      return this;
    }
    public int getNodeByPosition(int position)
    {
      if (position > 0 && position <= m_size) {
        return m_ancestors[position-1];
      }
      else
        return DTM.NULL;
    }
    public int getLast() {
      return m_size;
    }
  }  
  public class DescendantIterator extends InternalAxisIteratorBase
  {
    public DTMAxisIterator setStartNode(int node)
    {
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        node = makeNodeIdentity(node);
        _startNode = node;
        if (_includeSelf)
          node--;
        _currentNode = node;
        return resetPosition();
      }
      return this;
    }
    protected final boolean isDescendant(int identity)
    {
      return (_parent2(identity) >= _startNode) || (_startNode == identity);
    }
    public int next()
    {
      final int startNode = _startNode;
      if (startNode == NULL) {
        return NULL;
      }
      if (_includeSelf && (_currentNode + 1) == startNode)
          return returnNode(makeNodeHandle(++_currentNode)); 
      int node = _currentNode;
      int type;
      if (startNode == ROOTNODE) {
        int eType;
        do {
          node++;
          eType = _exptype2(node);
          if (NULL == eType) {
            _currentNode = NULL;
            return END;
          }
        } while (eType == TEXT_NODE
                 || (type = m_extendedTypes[eType].getNodeType()) == ATTRIBUTE_NODE
                 || type == NAMESPACE_NODE);
      }
      else {
        do {
          node++;
          type = _type2(node);
          if (NULL == type ||!isDescendant(node)) {
            _currentNode = NULL;
            return END;
          }
        } while(ATTRIBUTE_NODE == type || TEXT_NODE == type
                 || NAMESPACE_NODE == type);
      }
      _currentNode = node;
      return returnNode(makeNodeHandle(node));  
    }
  public DTMAxisIterator reset()
  {
    final boolean temp = _isRestartable;
    _isRestartable = true;
    setStartNode(makeNodeHandle(_startNode));
    _isRestartable = temp;
    return this;
  }
  }  
  public final class TypedDescendantIterator extends DescendantIterator
  {
    private final int _nodeType;
    public TypedDescendantIterator(int nodeType)
    {
      _nodeType = nodeType;
    }
    public int next()
    {
      final int startNode = _startNode;
      if (_startNode == NULL) {
        return NULL;
      }
      int node = _currentNode;
      int expType;
      final int nodeType = _nodeType;
      if (nodeType != DTM.ELEMENT_NODE)
      {
        do
        {
          node++;
	  expType = _exptype2(node);
          if (NULL == expType || _parent2(node) < startNode && startNode != node) {
            _currentNode = NULL;
            return END;
          }
        }
        while (expType != nodeType);
      }
      else if (startNode == DTMDefaultBase.ROOTNODE)
      {
	do
	{
	  node++;
	  expType = _exptype2(node);
	  if (NULL == expType) {
	    _currentNode = NULL;
	    return END;
	  }
	} while (expType < DTM.NTYPES
	        || m_extendedTypes[expType].getNodeType() != DTM.ELEMENT_NODE);
      }
      else
      {
        do
        {
          node++;
	  expType = _exptype2(node);
          if (NULL == expType || _parent2(node) < startNode && startNode != node) {
            _currentNode = NULL;
            return END;
          }
        }
        while (expType < DTM.NTYPES
	       || m_extendedTypes[expType].getNodeType() != DTM.ELEMENT_NODE);
      }
      _currentNode = node;
      return returnNode(makeNodeHandle(node));
    }
  }  
  public final class TypedSingletonIterator extends SingletonIterator
  {
    private final int _nodeType;
    public TypedSingletonIterator(int nodeType)
    {
      _nodeType = nodeType;
    }
    public int next()
    {
      final int result = _currentNode;
      if (result == END)
        return DTM.NULL;
      _currentNode = END;
      if (_nodeType >= DTM.NTYPES) {
        if (_exptype2(makeNodeIdentity(result)) == _nodeType) {
          return returnNode(result);
        }
      }
      else {
        if (_type2(makeNodeIdentity(result)) == _nodeType) {
          return returnNode(result);
        }
      }
      return NULL;
    }
  }  
  private int[] m_exptype_map0;
  private int[] m_nextsib_map0;
  private int[] m_firstch_map0;
  private int[] m_parent_map0;
  private int[][] m_exptype_map;
  private int[][] m_nextsib_map;
  private int[][] m_firstch_map;
  private int[][] m_parent_map;
  protected ExtendedType[] m_extendedTypes;
  protected Vector m_values;
  private int m_valueIndex = 0;
  private int m_maxNodeIndex;
  protected int m_SHIFT;
  protected int m_MASK;
  protected int m_blocksize;
  protected final static int TEXT_LENGTH_BITS = 10;
  protected final static int TEXT_OFFSET_BITS = 21;
  protected final static int TEXT_LENGTH_MAX = (1<<TEXT_LENGTH_BITS) - 1;
  protected final static int TEXT_OFFSET_MAX = (1<<TEXT_OFFSET_BITS) - 1;
  protected boolean m_buildIdIndex = true;
  private static final String EMPTY_STR = "";
  private static final XMLString EMPTY_XML_STR = new XMLStringDefault("");
  public SAX2DTM2(DTMManager mgr, Source source, int dtmIdentity,
                 DTMWSFilter whiteSpaceFilter,
                 XMLStringFactory xstringfactory,
                 boolean doIndexing)
  {
    this(mgr, source, dtmIdentity, whiteSpaceFilter,
          xstringfactory, doIndexing, DEFAULT_BLOCKSIZE, true, true, false);
  }
  public SAX2DTM2(DTMManager mgr, Source source, int dtmIdentity,
                 DTMWSFilter whiteSpaceFilter,
                 XMLStringFactory xstringfactory,
                 boolean doIndexing,
                 int blocksize,
                 boolean usePrevsib,
                 boolean buildIdIndex,
                 boolean newNameTable)
  {
    super(mgr, source, dtmIdentity, whiteSpaceFilter,
          xstringfactory, doIndexing, blocksize, usePrevsib, newNameTable);
    int shift;
    for(shift=0; (blocksize>>>=1) != 0; ++shift);
    m_blocksize = 1<<shift;
    m_SHIFT = shift;
    m_MASK = m_blocksize - 1;
    m_buildIdIndex = buildIdIndex;
    m_values = new Vector(32, 512);
    m_maxNodeIndex = 1 << DTMManager.IDENT_DTM_NODE_BITS;
    m_exptype_map0 = m_exptype.getMap0();
    m_nextsib_map0 = m_nextsib.getMap0();
    m_firstch_map0 = m_firstch.getMap0();
    m_parent_map0  = m_parent.getMap0();
  }
  public final int _exptype(int identity)
  {
    return m_exptype.elementAt(identity);
  }
  public final int _exptype2(int identity)
  {
    if (identity < m_blocksize)
      return m_exptype_map0[identity];
    else
      return m_exptype_map[identity>>>m_SHIFT][identity&m_MASK];
  }
  public final int _nextsib2(int identity)
  {
    if (identity < m_blocksize)
      return m_nextsib_map0[identity];
    else
      return m_nextsib_map[identity>>>m_SHIFT][identity&m_MASK];
  }
  public final int _firstch2(int identity)
  {
    if (identity < m_blocksize)
      return m_firstch_map0[identity];
    else
      return m_firstch_map[identity>>>m_SHIFT][identity&m_MASK];
  }
  public final int _parent2(int identity)
  {
    if (identity < m_blocksize)
      return m_parent_map0[identity];
    else
      return m_parent_map[identity>>>m_SHIFT][identity&m_MASK];
  }
  public final int _type2(int identity)
  {
    int eType;
    if (identity < m_blocksize)
      eType = m_exptype_map0[identity];
    else
      eType = m_exptype_map[identity>>>m_SHIFT][identity&m_MASK];
    if (NULL != eType)
      return m_extendedTypes[eType].getNodeType();
    else
      return NULL;
  }
  public final int getExpandedTypeID2(int nodeHandle)
  {
    int nodeID = makeNodeIdentity(nodeHandle);
    if (nodeID != NULL) {
      if (nodeID < m_blocksize)
        return m_exptype_map0[nodeID];
      else
        return m_exptype_map[nodeID>>>m_SHIFT][nodeID&m_MASK];
    }
    else
      return NULL;
  }
  public final int _exptype2Type(int exptype)
  {
    if (NULL != exptype)
      return m_extendedTypes[exptype].getNodeType();
    else
      return NULL;
  }
  public int getIdForNamespace(String uri)
  {
     int index = m_values.indexOf(uri);
     if (index < 0)
     {
       m_values.addElement(uri);
       return m_valueIndex++;
     }
     else
       return index;
  }
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
  {
    charactersFlush();
    int exName = m_expandedNameTable.getExpandedTypeID(uri, localName, DTM.ELEMENT_NODE);
    int prefixIndex = (qName.length() != localName.length())
                      ? m_valuesOrPrefixes.stringToIndex(qName) : 0;
    int elemNode = addNode(DTM.ELEMENT_NODE, exName,
                           m_parents.peek(), m_previous, prefixIndex, true);
    if(m_indexing)
      indexNode(exName, elemNode);
    m_parents.push(elemNode);
    int startDecls = m_contextIndexes.peek();
    int nDecls = m_prefixMappings.size();
    String prefix;
    if(!m_pastFirstElement)
    {
      prefix="xml";
      String declURL = "http:
      exName = m_expandedNameTable.getExpandedTypeID(null, prefix, DTM.NAMESPACE_NODE);
      m_values.addElement(declURL);
      int val = m_valueIndex++;
      addNode(DTM.NAMESPACE_NODE, exName, elemNode,
                     DTM.NULL, val, false);
      m_pastFirstElement=true;
    }
    for (int i = startDecls; i < nDecls; i += 2)
    {
      prefix = (String) m_prefixMappings.elementAt(i);
      if (prefix == null)
        continue;
      String declURL = (String) m_prefixMappings.elementAt(i + 1);
      exName = m_expandedNameTable.getExpandedTypeID(null, prefix, DTM.NAMESPACE_NODE);
      m_values.addElement(declURL);
      int val = m_valueIndex++;
      addNode(DTM.NAMESPACE_NODE, exName, elemNode, DTM.NULL, val, false);
    }
    int n = attributes.getLength();
    for (int i = 0; i < n; i++)
    {
      String attrUri = attributes.getURI(i);
      String attrQName = attributes.getQName(i);
      String valString = attributes.getValue(i);
      int nodeType;
      String attrLocalName = attributes.getLocalName(i);
      if ((null != attrQName)
              && (attrQName.equals("xmlns")
                  || attrQName.startsWith("xmlns:")))
      {
        prefix = getPrefix(attrQName, attrUri);
        if (declAlreadyDeclared(prefix))
          continue;  
        nodeType = DTM.NAMESPACE_NODE;
      }
      else
      {
        nodeType = DTM.ATTRIBUTE_NODE;
        if (m_buildIdIndex && attributes.getType(i).equalsIgnoreCase("ID"))
          setIDAttribute(valString, elemNode);
      }
      if(null == valString)
        valString = "";
      m_values.addElement(valString);
      int val = m_valueIndex++;
      if (attrLocalName.length() != attrQName.length())
      {
        prefixIndex = m_valuesOrPrefixes.stringToIndex(attrQName);
        int dataIndex = m_data.size();
        m_data.addElement(prefixIndex);
        m_data.addElement(val);
        val = -dataIndex;
      }
      exName = m_expandedNameTable.getExpandedTypeID(attrUri, attrLocalName, nodeType);
      addNode(nodeType, exName, elemNode, DTM.NULL, val,
                     false);
    }
    if (null != m_wsfilter)
    {
      short wsv = m_wsfilter.getShouldStripSpace(makeNodeHandle(elemNode), this);
      boolean shouldStrip = (DTMWSFilter.INHERIT == wsv)
                            ? getShouldStripWhitespace()
                            : (DTMWSFilter.STRIP == wsv);
      pushShouldStripWhitespace(shouldStrip);
    }
    m_previous = DTM.NULL;
    m_contextIndexes.push(m_prefixMappings.size());  
  }
  public void endElement(String uri, String localName, String qName)
          throws SAXException
  {
    charactersFlush();
    m_contextIndexes.quickPop(1);
    int topContextIndex = m_contextIndexes.peek();
    if (topContextIndex != m_prefixMappings.size()) {
      m_prefixMappings.setSize(topContextIndex);
    }
    m_previous = m_parents.pop();
    popShouldStripWhitespace();
  }
  public void comment(char ch[], int start, int length) throws SAXException
  {
    if (m_insideDTD)      
      return;
    charactersFlush();
    m_values.addElement(new String(ch, start, length));
    int dataIndex = m_valueIndex++;
    m_previous = addNode(DTM.COMMENT_NODE, DTM.COMMENT_NODE,
                         m_parents.peek(), m_previous, dataIndex, false);
  }
  public void startDocument() throws SAXException
  {
    int doc = addNode(DTM.DOCUMENT_NODE,
                      DTM.DOCUMENT_NODE,
                      DTM.NULL, DTM.NULL, 0, true);
    m_parents.push(doc);
    m_previous = DTM.NULL;
    m_contextIndexes.push(m_prefixMappings.size());  
  }
  public void endDocument() throws SAXException
  {
    super.endDocument();
    m_exptype.addElement(NULL);
    m_parent.addElement(NULL);
    m_nextsib.addElement(NULL);
    m_firstch.addElement(NULL);
    m_extendedTypes = m_expandedNameTable.getExtendedTypes();
    m_exptype_map = m_exptype.getMap();
    m_nextsib_map = m_nextsib.getMap();
    m_firstch_map = m_firstch.getMap();
    m_parent_map  = m_parent.getMap();
  }
  protected final int addNode(int type, int expandedTypeID,
                        int parentIndex, int previousSibling,
                        int dataOrPrefix, boolean canHaveFirstChild)
  {
    int nodeIndex = m_size++;
    if (nodeIndex == m_maxNodeIndex)
    {
      addNewDTMID(nodeIndex);
      m_maxNodeIndex += (1 << DTMManager.IDENT_DTM_NODE_BITS);
    }
    m_firstch.addElement(DTM.NULL);
    m_nextsib.addElement(DTM.NULL);
    m_parent.addElement(parentIndex);
    m_exptype.addElement(expandedTypeID);
    m_dataOrQName.addElement(dataOrPrefix);
    if (m_prevsib != null) {
      m_prevsib.addElement(previousSibling);
    }
    if (m_locator != null && m_useSourceLocationProperty) {
      setSourceLocation();
    }
    switch(type)
    {
    case DTM.NAMESPACE_NODE:
      declareNamespaceInContext(parentIndex,nodeIndex);
      break;
    case DTM.ATTRIBUTE_NODE:
      break;
    default:
      if (DTM.NULL != previousSibling) {
        m_nextsib.setElementAt(nodeIndex,previousSibling);
      }
      else if (DTM.NULL != parentIndex) {
        m_firstch.setElementAt(nodeIndex,parentIndex);
      }
      break;
    }
    return nodeIndex;
  }
  protected final void charactersFlush()
  {
    if (m_textPendingStart >= 0)  
    {
      int length = m_chars.size() - m_textPendingStart;
      boolean doStrip = false;
      if (getShouldStripWhitespace())
      {
        doStrip = m_chars.isWhitespace(m_textPendingStart, length);
      }
      if (doStrip) {
        m_chars.setLength(m_textPendingStart);  
      } else {
        if (length > 0) {
          if (length <= TEXT_LENGTH_MAX
                  && m_textPendingStart <= TEXT_OFFSET_MAX) {
            m_previous = addNode(m_coalescedTextType, DTM.TEXT_NODE,
                             m_parents.peek(), m_previous,
                             length + (m_textPendingStart << TEXT_LENGTH_BITS),
                             false);
          } else {
            int dataIndex = m_data.size();
            m_previous = addNode(m_coalescedTextType, DTM.TEXT_NODE,
                               m_parents.peek(), m_previous, -dataIndex, false);
            m_data.addElement(m_textPendingStart);
            m_data.addElement(length);
          }
        }
      }
      m_textPendingStart = -1;
      m_textType = m_coalescedTextType = DTM.TEXT_NODE;
    }
  }
  public void processingInstruction(String target, String data)
	  throws SAXException
  {
    charactersFlush();
    int dataIndex = m_data.size();
    m_previous = addNode(DTM.PROCESSING_INSTRUCTION_NODE,
			 DTM.PROCESSING_INSTRUCTION_NODE,
			 m_parents.peek(), m_previous,
			 -dataIndex, false);
    m_data.addElement(m_valuesOrPrefixes.stringToIndex(target));
    m_values.addElement(data);
    m_data.addElement(m_valueIndex++);
  }
  public final int getFirstAttribute(int nodeHandle)
  {
    int nodeID = makeNodeIdentity(nodeHandle);
    if (nodeID == DTM.NULL)
      return DTM.NULL;
    int type = _type2(nodeID);
    if (DTM.ELEMENT_NODE == type)
    {
      while (true)
      {
        nodeID++;
	type = _type2(nodeID);
	if (type == DTM.ATTRIBUTE_NODE)
	{
	  return makeNodeHandle(nodeID);
	}
	else if (DTM.NAMESPACE_NODE != type)
	{
	  break;
	}
      }
    }
    return DTM.NULL;
  }
  protected int getFirstAttributeIdentity(int identity) {
    if (identity == NULL) {
        return NULL;
    }
    int type = _type2(identity);
    if (DTM.ELEMENT_NODE == type)
    {
      while (true)
      {
        identity++;
        type = _type2(identity);
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
  protected int getNextAttributeIdentity(int identity) {
    while (true) {
      identity++;
      int type = _type2(identity);
      if (type == DTM.ATTRIBUTE_NODE) {
        return identity;
      } else if (type != DTM.NAMESPACE_NODE) {
        break;
      }
    }
    return DTM.NULL;
  }
  protected final int getTypedAttribute(int nodeHandle, int attType)
  {
    int nodeID = makeNodeIdentity(nodeHandle);
    if (nodeID == DTM.NULL)
      return DTM.NULL;
    int type = _type2(nodeID);
    if (DTM.ELEMENT_NODE == type)
    {
      int expType;
      while (true)
      {
	nodeID++;
	expType = _exptype2(nodeID);
	if (expType != DTM.NULL)
	  type = m_extendedTypes[expType].getNodeType();
	else
	  return DTM.NULL;
	if (type == DTM.ATTRIBUTE_NODE)
	{
	  if (expType == attType) return makeNodeHandle(nodeID);
	}
	else if (DTM.NAMESPACE_NODE != type)
	{
	  break;
	}
      }
    }
    return DTM.NULL;
  }
  public String getLocalName(int nodeHandle)
  {
    int expType = _exptype(makeNodeIdentity(nodeHandle));
    if (expType == DTM.PROCESSING_INSTRUCTION_NODE)
    {
      int dataIndex = _dataOrQName(makeNodeIdentity(nodeHandle));
      dataIndex = m_data.elementAt(-dataIndex);
      return m_valuesOrPrefixes.indexToString(dataIndex);
    }
    else
      return m_expandedNameTable.getLocalName(expType);
  }
  public final String getNodeNameX(int nodeHandle)
  {
    int nodeID = makeNodeIdentity(nodeHandle);
    int eType = _exptype2(nodeID);
    if (eType == DTM.PROCESSING_INSTRUCTION_NODE)
    {
      int dataIndex = _dataOrQName(nodeID);
      dataIndex = m_data.elementAt(-dataIndex);
      return m_valuesOrPrefixes.indexToString(dataIndex);
    }
    final ExtendedType extType = m_extendedTypes[eType];
    if (extType.getNamespace().length() == 0)
    {
      return extType.getLocalName();
    }
    else
    {
      int qnameIndex = m_dataOrQName.elementAt(nodeID);
      if (qnameIndex == 0)
        return extType.getLocalName();
      if (qnameIndex < 0)
      {
	qnameIndex = -qnameIndex;
	qnameIndex = m_data.elementAt(qnameIndex);
      }
      return m_valuesOrPrefixes.indexToString(qnameIndex);
    }
  }
  public String getNodeName(int nodeHandle)
  {
    int nodeID = makeNodeIdentity(nodeHandle);
    int eType = _exptype2(nodeID);
    final ExtendedType extType = m_extendedTypes[eType];
    if (extType.getNamespace().length() == 0)
    {
      int type = extType.getNodeType();
      String localName = extType.getLocalName();
      if (type == DTM.NAMESPACE_NODE)
      {
	if (localName.length() == 0)
	  return "xmlns";
	else
	  return "xmlns:" + localName;
      }
      else if (type == DTM.PROCESSING_INSTRUCTION_NODE)
      {
	int dataIndex = _dataOrQName(nodeID);
	dataIndex = m_data.elementAt(-dataIndex);
	return m_valuesOrPrefixes.indexToString(dataIndex);
      }
      else if (localName.length() == 0)
      {
        return getFixedNames(type);
      }
      else
	return localName;
    }
    else
    {
      int qnameIndex = m_dataOrQName.elementAt(nodeID);
      if (qnameIndex == 0)
        return extType.getLocalName();
      if (qnameIndex < 0)
      {
	qnameIndex = -qnameIndex;
	qnameIndex = m_data.elementAt(qnameIndex);
      }
      return m_valuesOrPrefixes.indexToString(qnameIndex);
    }
  }
  public XMLString getStringValue(int nodeHandle)
  {
    int identity = makeNodeIdentity(nodeHandle);
    if (identity == DTM.NULL)
      return EMPTY_XML_STR;
    int type= _type2(identity);
    if (type == DTM.ELEMENT_NODE || type == DTM.DOCUMENT_NODE)
    {
      int startNode = identity;
      identity = _firstch2(identity);
      if (DTM.NULL != identity)
      {
	int offset = -1;
	int length = 0;
	do
	{
	  type = _exptype2(identity);
	  if (type == DTM.TEXT_NODE || type == DTM.CDATA_SECTION_NODE)
	  {
	    int dataIndex = m_dataOrQName.elementAt(identity);
	    if (dataIndex >= 0)
	    {
	      if (-1 == offset)
	      {
                offset = dataIndex >>> TEXT_LENGTH_BITS;
	      }
	      length += dataIndex & TEXT_LENGTH_MAX;
	    }
	    else
	    {
	      if (-1 == offset)
	      {
                offset = m_data.elementAt(-dataIndex);
	      }
	      length += m_data.elementAt(-dataIndex + 1);
	    }
	  }
	  identity++;
	} while (_parent2(identity) >= startNode);
	if (length > 0)
	{
	  if (m_xstrf != null)
	    return m_xstrf.newstr(m_chars, offset, length);
	  else
	    return new XMLStringDefault(m_chars.getString(offset, length));
	}
	else
	  return EMPTY_XML_STR;
      }
      else
        return EMPTY_XML_STR;
    }
    else if (DTM.TEXT_NODE == type || DTM.CDATA_SECTION_NODE == type)
    {
      int dataIndex = m_dataOrQName.elementAt(identity);
      if (dataIndex >= 0)
      {
      	if (m_xstrf != null)
      	  return m_xstrf.newstr(m_chars, dataIndex >>> TEXT_LENGTH_BITS,
      	                 dataIndex & TEXT_LENGTH_MAX);
      	else
      	  return new XMLStringDefault(m_chars.getString(dataIndex >>> TEXT_LENGTH_BITS,
      	                              dataIndex & TEXT_LENGTH_MAX));
      }
      else
      {
        if (m_xstrf != null)
          return m_xstrf.newstr(m_chars, m_data.elementAt(-dataIndex),
                                m_data.elementAt(-dataIndex+1));
        else
          return new XMLStringDefault(m_chars.getString(m_data.elementAt(-dataIndex),
                                   m_data.elementAt(-dataIndex+1)));
      }
    }
    else
    {
      int dataIndex = m_dataOrQName.elementAt(identity);
      if (dataIndex < 0)
      {
        dataIndex = -dataIndex;
        dataIndex = m_data.elementAt(dataIndex + 1);
      }
      if (m_xstrf != null)
        return m_xstrf.newstr((String)m_values.elementAt(dataIndex));
      else
        return new XMLStringDefault((String)m_values.elementAt(dataIndex));
    }
  }
  public final String getStringValueX(final int nodeHandle)
  {
    int identity = makeNodeIdentity(nodeHandle);
    if (identity == DTM.NULL)
      return EMPTY_STR;
    int type= _type2(identity);
    if (type == DTM.ELEMENT_NODE || type == DTM.DOCUMENT_NODE)
    {
      int startNode = identity;
      identity = _firstch2(identity);
      if (DTM.NULL != identity)
      {
	int offset = -1;
	int length = 0;
	do
	{
	  type = _exptype2(identity);
	  if (type == DTM.TEXT_NODE || type == DTM.CDATA_SECTION_NODE)
	  {
	    int dataIndex = m_dataOrQName.elementAt(identity);
	    if (dataIndex >= 0)
	    {
	      if (-1 == offset)
	      {
                offset = dataIndex >>> TEXT_LENGTH_BITS;
	      }
	      length += dataIndex & TEXT_LENGTH_MAX;
	    }
	    else
	    {
	      if (-1 == offset)
	      {
                offset = m_data.elementAt(-dataIndex);
	      }
	      length += m_data.elementAt(-dataIndex + 1);
	    }
	  }
	  identity++;
	} while (_parent2(identity) >= startNode);
	if (length > 0)
	{
	  return m_chars.getString(offset, length);
	}
	else
	  return EMPTY_STR;
      }
      else
        return EMPTY_STR;
    }
    else if (DTM.TEXT_NODE == type || DTM.CDATA_SECTION_NODE == type)
    {
      int dataIndex = m_dataOrQName.elementAt(identity);
      if (dataIndex >= 0)
      {
      	return m_chars.getString(dataIndex >>> TEXT_LENGTH_BITS,
      	                          dataIndex & TEXT_LENGTH_MAX);
      }
      else
      {
        return m_chars.getString(m_data.elementAt(-dataIndex),
                                  m_data.elementAt(-dataIndex+1));
      }
    }
    else
    {
      int dataIndex = m_dataOrQName.elementAt(identity);
      if (dataIndex < 0)
      {
        dataIndex = -dataIndex;
        dataIndex = m_data.elementAt(dataIndex + 1);
      }
      return (String)m_values.elementAt(dataIndex);
    }
  }
  public String getStringValue()
  {
    int child = _firstch2(ROOTNODE);
    if (child == DTM.NULL) return EMPTY_STR;
    if ((_exptype2(child) == DTM.TEXT_NODE) && (_nextsib2(child) == DTM.NULL))
    {
      int dataIndex = m_dataOrQName.elementAt(child);
      if (dataIndex >= 0)
        return m_chars.getString(dataIndex >>> TEXT_LENGTH_BITS, dataIndex & TEXT_LENGTH_MAX);
      else
        return m_chars.getString(m_data.elementAt(-dataIndex),
                                  m_data.elementAt(-dataIndex + 1));
    }
    else
      return getStringValueX(getDocument());
  }
  public final void dispatchCharactersEvents(int nodeHandle, ContentHandler ch,
                                             boolean normalize)
          throws SAXException
  {
    int identity = makeNodeIdentity(nodeHandle);
    if (identity == DTM.NULL)
      return;
    int type = _type2(identity);
    if (type == DTM.ELEMENT_NODE || type == DTM.DOCUMENT_NODE)
    {
      int startNode = identity;
      identity = _firstch2(identity);
      if (DTM.NULL != identity)
      {
	int offset = -1;
	int length = 0;
	do
	{
	  type = _exptype2(identity);
	  if (type == DTM.TEXT_NODE || type == DTM.CDATA_SECTION_NODE)
	  {
	    int dataIndex = m_dataOrQName.elementAt(identity);
	    if (dataIndex >= 0)
	    {
	      if (-1 == offset)
	      {
                offset = dataIndex >>> TEXT_LENGTH_BITS;
	      }
	      length += dataIndex & TEXT_LENGTH_MAX;
	    }
	    else
	    {
	      if (-1 == offset)
	      {
                offset = m_data.elementAt(-dataIndex);
	      }
	      length += m_data.elementAt(-dataIndex + 1);
	    }
	  }
	  identity++;
	} while (_parent2(identity) >= startNode);
	if (length > 0)
	{
          if(normalize)
            m_chars.sendNormalizedSAXcharacters(ch, offset, length);
          else
	    m_chars.sendSAXcharacters(ch, offset, length);
	}
      }
    }
    else if (DTM.TEXT_NODE == type || DTM.CDATA_SECTION_NODE == type)
    {
      int dataIndex = m_dataOrQName.elementAt(identity);
      if (dataIndex >= 0)
      {
      	if (normalize)
      	  m_chars.sendNormalizedSAXcharacters(ch, dataIndex >>> TEXT_LENGTH_BITS,
      	                                      dataIndex & TEXT_LENGTH_MAX);
      	else
      	  m_chars.sendSAXcharacters(ch, dataIndex >>> TEXT_LENGTH_BITS,
      	                            dataIndex & TEXT_LENGTH_MAX);
      }
      else
      {
        if (normalize)
          m_chars.sendNormalizedSAXcharacters(ch, m_data.elementAt(-dataIndex),
                                              m_data.elementAt(-dataIndex+1));
        else
          m_chars.sendSAXcharacters(ch, m_data.elementAt(-dataIndex),
                                    m_data.elementAt(-dataIndex+1));
      }
    }
    else
    {
      int dataIndex = m_dataOrQName.elementAt(identity);
      if (dataIndex < 0)
      {
        dataIndex = -dataIndex;
        dataIndex = m_data.elementAt(dataIndex + 1);
      }
      String str = (String)m_values.elementAt(dataIndex);
      if(normalize)
        FastStringBuffer.sendNormalizedSAXcharacters(str.toCharArray(),
                                                     0, str.length(), ch);
      else
        ch.characters(str.toCharArray(), 0, str.length());
    }
  }
  public String getNodeValue(int nodeHandle)
  {
    int identity = makeNodeIdentity(nodeHandle);
    int type = _type2(identity);
    if (type == DTM.TEXT_NODE || type == DTM.CDATA_SECTION_NODE)
    {
      int dataIndex = _dataOrQName(identity);
      if (dataIndex > 0)
      {
      	return m_chars.getString(dataIndex >>> TEXT_LENGTH_BITS,
      	                          dataIndex & TEXT_LENGTH_MAX);
      }
      else
      {
        return m_chars.getString(m_data.elementAt(-dataIndex),
                                  m_data.elementAt(-dataIndex+1));
      }
    }
    else if (DTM.ELEMENT_NODE == type || DTM.DOCUMENT_FRAGMENT_NODE == type
             || DTM.DOCUMENT_NODE == type)
    {
      return null;
    }
    else
    {
      int dataIndex = m_dataOrQName.elementAt(identity);
      if (dataIndex < 0)
      {
        dataIndex = -dataIndex;
        dataIndex = m_data.elementAt(dataIndex + 1);
      }
      return (String)m_values.elementAt(dataIndex);
    }
  }
    protected final void copyTextNode(final int nodeID, SerializationHandler handler)
        throws SAXException
    {
        if (nodeID != DTM.NULL) {
      	    int dataIndex = m_dataOrQName.elementAt(nodeID);
            if (dataIndex >= 0) {
                m_chars.sendSAXcharacters(handler,
                                          dataIndex >>> TEXT_LENGTH_BITS,
                                          dataIndex & TEXT_LENGTH_MAX);
            } else {
                m_chars.sendSAXcharacters(handler, m_data.elementAt(-dataIndex),
                                          m_data.elementAt(-dataIndex+1));
            }
        }
    }
    protected final String copyElement(int nodeID, int exptype,
                               SerializationHandler handler)
        throws SAXException
    {
        final ExtendedType extType = m_extendedTypes[exptype];
        String uri = extType.getNamespace();
        String name = extType.getLocalName();
        if (uri.length() == 0) {
            handler.startElement(name);
            return name;
        }
        else {
            int qnameIndex = m_dataOrQName.elementAt(nodeID);
            if (qnameIndex == 0) {
                handler.startElement(name);
                handler.namespaceAfterStartElement(EMPTY_STR, uri);
                return name;
            }
            if (qnameIndex < 0) {
    	        qnameIndex = -qnameIndex;
    	        qnameIndex = m_data.elementAt(qnameIndex);
            }
            String qName = m_valuesOrPrefixes.indexToString(qnameIndex);
            handler.startElement(qName);
            int prefixIndex = qName.indexOf(':');
            String prefix;
            if (prefixIndex > 0) {
                prefix = qName.substring(0, prefixIndex);
            }
            else {
                prefix = null;
            }
            handler.namespaceAfterStartElement(prefix, uri);
            return qName;
        }
    }
    protected final void copyNS(final int nodeID, SerializationHandler handler, boolean inScope)
        throws SAXException
    {
        if (m_namespaceDeclSetElements != null &&
            m_namespaceDeclSetElements.size() == 1 &&
            m_namespaceDeclSets != null &&
            ((SuballocatedIntVector)m_namespaceDeclSets.elementAt(0))
            .size() == 1)
            return;
        SuballocatedIntVector nsContext = null;
        int nextNSNode;
        if (inScope) {
            nsContext = findNamespaceContext(nodeID);
            if (nsContext == null || nsContext.size() < 1)
                return;
            else
                nextNSNode = makeNodeIdentity(nsContext.elementAt(0));
        }
        else
            nextNSNode = getNextNamespaceNode2(nodeID);
        int nsIndex = 1;
        while (nextNSNode != DTM.NULL) {
            int eType = _exptype2(nextNSNode);
            String nodeName = m_extendedTypes[eType].getLocalName();
            int dataIndex = m_dataOrQName.elementAt(nextNSNode);
            if (dataIndex < 0) {
                dataIndex = -dataIndex;
                dataIndex = m_data.elementAt(dataIndex + 1);
            }
            String nodeValue = (String)m_values.elementAt(dataIndex);
            handler.namespaceAfterStartElement(nodeName, nodeValue);
            if (inScope) {
                if (nsIndex < nsContext.size()) {
                    nextNSNode = makeNodeIdentity(nsContext.elementAt(nsIndex));
                    nsIndex++;
                }
                else
                    return;
            }
            else
                nextNSNode = getNextNamespaceNode2(nextNSNode);
        }
    }
    protected final int getNextNamespaceNode2(int baseID) {
        int type;
        while ((type = _type2(++baseID)) == DTM.ATTRIBUTE_NODE);
        if (type == DTM.NAMESPACE_NODE)
            return baseID;
        else
            return NULL;
    }
    protected final void copyAttributes(final int nodeID, SerializationHandler handler)
        throws SAXException{
       for(int current = getFirstAttributeIdentity(nodeID); current != DTM.NULL; current = getNextAttributeIdentity(current)){
            int eType = _exptype2(current);
            copyAttribute(current, eType, handler);
       }
    }
    protected final void copyAttribute(int nodeID, int exptype,
        SerializationHandler handler)
        throws SAXException
    {
        final ExtendedType extType = m_extendedTypes[exptype];
        final String uri = extType.getNamespace();
        final String localName = extType.getLocalName();
        String prefix = null;
        String qname = null;
        int dataIndex = _dataOrQName(nodeID);
        int valueIndex = dataIndex;
            if (dataIndex <= 0) {
                int prefixIndex = m_data.elementAt(-dataIndex);
                valueIndex = m_data.elementAt(-dataIndex+1);
                qname = m_valuesOrPrefixes.indexToString(prefixIndex);
                int colonIndex = qname.indexOf(':');
                if (colonIndex > 0) {
                    prefix = qname.substring(0, colonIndex);
                }
            }
            if (uri.length() != 0) {
                handler.namespaceAfterStartElement(prefix, uri);
            }
        String nodeName = (prefix != null) ? qname : localName;
        String nodeValue = (String)m_values.elementAt(valueIndex);
        handler.addAttribute(nodeName, nodeValue);
    }
}
