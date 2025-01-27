public class MockXmlNode implements Node {
    MockNodeList mNodeList;
    private String mLocalName;
    private String mNamespace;
    private short mNodeType;
    private MockXmlNode mParent;
    private MockXmlNode mPreviousSibling;
    private MockXmlNode mNextSibling;
    private String mAttrValue;
    private MockNamedNodeMap mAttributes;
    private HashMap<String, String> mNsMap = null;
    public MockXmlNode(String namespace, String localName, short node_type,
            MockXmlNode[] children) {
        mLocalName = localName;
        mNamespace = namespace;
        mNodeType = node_type;
        mNodeList = new MockNodeList(children);
        fixNavigation();
    }
    public MockXmlNode(String namespace, String localName, String value) {
        mLocalName = localName;
        mNamespace = namespace;
        mAttrValue = value;
        mNodeType = Node.ATTRIBUTE_NODE;
        mNodeList = new MockNodeList(new MockXmlNode[0]);
        fixNavigation();
    }
    private void fixNavigation() {
        MockXmlNode prev = null;
        for (MockXmlNode n : mNodeList.getArrayList()) {
            n.mParent = this;
            n.mPreviousSibling = prev;
            if (prev != null) {
                prev.mNextSibling = n;
            }
            n.fixNavigation();
            prev = n;
        }
    }
    public void addAttributes(String namespaceURI, String localName, String value) {
        if (mAttributes == null) {
            mAttributes = new MockNamedNodeMap();
        }
        MockXmlNode node = mAttributes.addAttribute(namespaceURI, localName, value);
        node.mParent = this;
    }
    public void setPrefix(String namespace, String prefix) {
        if (mNsMap == null) {
            mNsMap = new HashMap<String, String>();
        }
        mNsMap.put(namespace, prefix);
    }
    public String getPrefix(String namespace) {
        if (mNsMap != null) {
            return mNsMap.get(namespace);
        }
        return mParent.getPrefix(namespace);
    }
    public Node appendChild(Node newChild) throws DOMException {
        mNodeList.getArrayList().add((MockXmlNode) newChild);
        return newChild;
    }
    public NamedNodeMap getAttributes() {
        return mAttributes;
    }
    public NodeList getChildNodes() {
        return mNodeList;
    }
    public Node getFirstChild() {
        if (mNodeList.getLength() > 0) {
            return mNodeList.item(0);
        }
        return null;
    }
    public Node getLastChild() {
        if (mNodeList.getLength() > 0) {
            return mNodeList.item(mNodeList.getLength() - 1);
        }
        return null;
    }
    public Node getNextSibling() {
        return mNextSibling;
    }
    public String getNodeName() {
        return mLocalName;
    }
    public String getLocalName() {
        return mLocalName;
    }
    public short getNodeType() {
        return mNodeType;
    }
    public Node getParentNode() {
        return mParent;
    }
    public Node getPreviousSibling() {
        return mPreviousSibling;
    }
    public boolean hasChildNodes() {
        return mNodeList.getLength() > 0;
    }
    public boolean hasAttributes() {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public boolean isSameNode(Node other) {
        return this == other;
    }
    public String getNodeValue() throws DOMException {
        return mAttrValue;
    }
    public String getPrefix() {
        return getPrefix(getNamespaceURI());
    }
    public String getNamespaceURI() {
        return mNamespace;
    }
    public Node cloneNode(boolean deep) {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public short compareDocumentPosition(Node other) throws DOMException {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public String getBaseURI() {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public Object getFeature(String feature, String version) {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public Document getOwnerDocument() {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public String getTextContent() throws DOMException {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public Object getUserData(String key) {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public Node insertBefore(Node newChild, Node refChild)
            throws DOMException {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public boolean isDefaultNamespace(String namespaceURI) {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public boolean isEqualNode(Node arg) {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public boolean isSupported(String feature, String version) {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public String lookupNamespaceURI(String prefix) {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public String lookupPrefix(String namespaceURI) {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public void normalize() {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public Node removeChild(Node oldChild) throws DOMException {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public Node replaceChild(Node newChild, Node oldChild)
            throws DOMException {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public void setNodeValue(String nodeValue) throws DOMException {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public void setPrefix(String prefix) throws DOMException {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public void setTextContent(String textContent) throws DOMException {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public Object setUserData(String key, Object data,
            UserDataHandler handler) {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
}
