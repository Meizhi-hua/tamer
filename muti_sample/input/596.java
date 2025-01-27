public class XTree extends JTree {
    private static final List<String> orderedKeyPropertyList =
            new ArrayList<String>();
    static {
        String keyPropertyList =
                System.getProperty("com.sun.tools.jconsole.mbeans.keyPropertyList");
        if (keyPropertyList == null) {
            orderedKeyPropertyList.add("type");
            orderedKeyPropertyList.add("j2eeType");
        } else {
            StringTokenizer st = new StringTokenizer(keyPropertyList, ",");
            while (st.hasMoreTokens()) {
                orderedKeyPropertyList.add(st.nextToken());
            }
        }
    }
    private MBeansTab mbeansTab;
    private Map<String, DefaultMutableTreeNode> nodes =
            new HashMap<String, DefaultMutableTreeNode>();
    public XTree(MBeansTab mbeansTab) {
        this(new DefaultMutableTreeNode("MBeanTreeRootNode"), mbeansTab);
    }
    public XTree(TreeNode root, MBeansTab mbeansTab) {
        super(root, true);
        this.mbeansTab = mbeansTab;
        setRootVisible(false);
        setShowsRootHandles(true);
        ToolTipManager.sharedInstance().registerComponent(this);
    }
    private synchronized void removeChildNode(DefaultMutableTreeNode child) {
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        model.removeNodeFromParent(child);
    }
    private synchronized void addChildNode(
            DefaultMutableTreeNode parent,
            DefaultMutableTreeNode child,
            int index) {
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        model.insertNodeInto(child, parent, index);
    }
    private synchronized void addChildNode(
            DefaultMutableTreeNode parent, DefaultMutableTreeNode child) {
        int childCount = parent.getChildCount();
        if (childCount == 0) {
            addChildNode(parent, child, 0);
            return;
        }
        if (child instanceof ComparableDefaultMutableTreeNode) {
            ComparableDefaultMutableTreeNode comparableChild =
                    (ComparableDefaultMutableTreeNode) child;
            for (int i = childCount - 1; i >= 0; i--) {
                DefaultMutableTreeNode brother =
                        (DefaultMutableTreeNode) parent.getChildAt(i);
                if ((i <= 2 && isMetadataNode(brother)) ||
                        comparableChild.compareTo(brother) >= 0) {
                    addChildNode(parent, child, i + 1);
                    return;
                }
            }
            addChildNode(parent, child, 0);
            return;
        }
        addChildNode(parent, child, childCount);
    }
    @Override
    public synchronized void removeAll() {
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        root.removeAllChildren();
        model.nodeStructureChanged(root);
        nodes.clear();
    }
    public synchronized void removeMBeanFromView(ObjectName mbean) {
        DefaultMutableTreeNode node = null;
        Dn dn = new Dn(mbean);
        if (dn.getTokenCount() > 0) {
            DefaultTreeModel model = (DefaultTreeModel) getModel();
            Token token = dn.getToken(0);
            String hashKey = dn.getHashKey(token);
            node = nodes.get(hashKey);
            if ((node != null) && (!node.isRoot())) {
                if (hasNonMetadataNodes(node)) {
                    removeMetadataNodes(node);
                    String label = token.getValue();
                    XNodeInfo userObject = new XNodeInfo(
                            Type.NONMBEAN, label,
                            label, token.getTokenValue());
                    changeNodeValue(node, userObject);
                } else {
                    DefaultMutableTreeNode parent =
                            (DefaultMutableTreeNode) node.getParent();
                    model.removeNodeFromParent(node);
                    nodes.remove(hashKey);
                    removeParentFromView(dn, 1, parent);
                }
            }
        }
    }
    private boolean hasNonMetadataNodes(DefaultMutableTreeNode node) {
        for (Enumeration e = node.children(); e.hasMoreElements();) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
            Object uo = n.getUserObject();
            if (uo instanceof XNodeInfo) {
                switch (((XNodeInfo) uo).getType()) {
                    case ATTRIBUTES:
                    case NOTIFICATIONS:
                    case OPERATIONS:
                        break;
                    default:
                        return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }
    public boolean hasMetadataNodes(DefaultMutableTreeNode node) {
        for (Enumeration e = node.children(); e.hasMoreElements();) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
            Object uo = n.getUserObject();
            if (uo instanceof XNodeInfo) {
                switch (((XNodeInfo) uo).getType()) {
                    case ATTRIBUTES:
                    case NOTIFICATIONS:
                    case OPERATIONS:
                        return true;
                    default:
                        break;
                }
            } else {
                return false;
            }
        }
        return false;
    }
    public boolean isMetadataNode(DefaultMutableTreeNode node) {
        Object uo = node.getUserObject();
        if (uo instanceof XNodeInfo) {
            switch (((XNodeInfo) uo).getType()) {
                case ATTRIBUTES:
                case NOTIFICATIONS:
                case OPERATIONS:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }
    private void removeMetadataNodes(DefaultMutableTreeNode node) {
        Set<DefaultMutableTreeNode> metadataNodes =
                new HashSet<DefaultMutableTreeNode>();
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        for (Enumeration e = node.children(); e.hasMoreElements();) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
            Object uo = n.getUserObject();
            if (uo instanceof XNodeInfo) {
                switch (((XNodeInfo) uo).getType()) {
                    case ATTRIBUTES:
                    case NOTIFICATIONS:
                    case OPERATIONS:
                        metadataNodes.add(n);
                        break;
                    default:
                        break;
                }
            }
        }
        for (DefaultMutableTreeNode n : metadataNodes) {
            model.removeNodeFromParent(n);
        }
    }
    private DefaultMutableTreeNode removeParentFromView(
            Dn dn, int index, DefaultMutableTreeNode node) {
        if ((!node.isRoot()) && node.isLeaf() &&
                (!(((XNodeInfo) node.getUserObject()).getType().equals(Type.MBEAN)))) {
            DefaultMutableTreeNode parent =
                    (DefaultMutableTreeNode) node.getParent();
            removeChildNode(node);
            String hashKey = dn.getHashKey(dn.getToken(index));
            nodes.remove(hashKey);
            removeParentFromView(dn, index + 1, parent);
        }
        return node;
    }
    public synchronized void addMBeansToView(Set<ObjectName> mbeans) {
        Set<Dn> dns = new TreeSet<Dn>();
        for (ObjectName mbean : mbeans) {
            Dn dn = new Dn(mbean);
            dns.add(dn);
        }
        for (Dn dn : dns) {
            ObjectName mbean = dn.getObjectName();
            XMBean xmbean = new XMBean(mbean, mbeansTab);
            addMBeanToView(mbean, xmbean, dn);
        }
    }
    public synchronized void addMBeanToView(ObjectName mbean) {
        XMBean xmbean = new XMBean(mbean, mbeansTab);
        Dn dn = new Dn(mbean);
        addMBeanToView(mbean, xmbean, dn);
    }
    private synchronized void addMBeanToView(
            ObjectName mbean, XMBean xmbean, Dn dn) {
        DefaultMutableTreeNode childNode = null;
        DefaultMutableTreeNode parentNode = null;
        Token token = dn.getToken(0);
        String hashKey = dn.getHashKey(token);
        if (nodes.containsKey(hashKey)) {
            childNode = nodes.get(hashKey);
            Object data = createNodeValue(xmbean, token);
            String label = data.toString();
            XNodeInfo userObject =
                    new XNodeInfo(Type.MBEAN, data, label, mbean.toString());
            changeNodeValue(childNode, userObject);
            return;
        }
        childNode = createDnNode(dn, token, xmbean);
        nodes.put(hashKey, childNode);
        for (int i = 1; i < dn.getTokenCount(); i++) {
            token = dn.getToken(i);
            hashKey = dn.getHashKey(token);
            if (nodes.containsKey(hashKey)) {
                parentNode = nodes.get(hashKey);
                addChildNode(parentNode, childNode);
                return;
            } else {
                if ("domain".equals(token.getTokenType())) {
                    parentNode = createDomainNode(dn, token);
                    DefaultMutableTreeNode root =
                            (DefaultMutableTreeNode) getModel().getRoot();
                    addChildNode(root, parentNode);
                } else {
                    parentNode = createSubDnNode(dn, token);
                }
                nodes.put(hashKey, parentNode);
                addChildNode(parentNode, childNode);
            }
            childNode = parentNode;
        }
    }
    private synchronized void changeNodeValue(
            DefaultMutableTreeNode node, XNodeInfo nodeValue) {
        if (node instanceof ComparableDefaultMutableTreeNode) {
            DefaultMutableTreeNode clone =
                    (DefaultMutableTreeNode) node.clone();
            clone.setUserObject(nodeValue);
            if (((ComparableDefaultMutableTreeNode) node).compareTo(clone) == 0) {
                node.setUserObject(nodeValue);
                DefaultTreeModel model = (DefaultTreeModel) getModel();
                model.nodeChanged(node);
            } else {
                DefaultMutableTreeNode parent =
                        (DefaultMutableTreeNode) node.getParent();
                removeChildNode(node);
                node.setUserObject(nodeValue);
                addChildNode(parent, node);
            }
        } else {
            node.setUserObject(nodeValue);
            DefaultTreeModel model = (DefaultTreeModel) getModel();
            model.nodeChanged(node);
        }
        if (nodeValue.getType().equals(Type.MBEAN)) {
            removeMetadataNodes(node);
            TreeNode[] treeNodes = node.getPath();
            TreePath path = new TreePath(treeNodes);
            if (isExpanded(path)) {
                addMetadataNodes(node);
            }
        }
        if (node == getLastSelectedPathComponent()) {
            TreePath selectionPath = getSelectionPath();
            clearSelection();
            setSelectionPath(selectionPath);
        }
    }
    private DefaultMutableTreeNode createDomainNode(Dn dn, Token token) {
        DefaultMutableTreeNode node = new ComparableDefaultMutableTreeNode();
        String label = dn.getDomain();
        XNodeInfo userObject =
                new XNodeInfo(Type.NONMBEAN, label, label, label);
        node.setUserObject(userObject);
        return node;
    }
    private DefaultMutableTreeNode createDnNode(
            Dn dn, Token token, XMBean xmbean) {
        DefaultMutableTreeNode node = new ComparableDefaultMutableTreeNode();
        Object data = createNodeValue(xmbean, token);
        String label = data.toString();
        XNodeInfo userObject = new XNodeInfo(Type.MBEAN, data, label,
                xmbean.getObjectName().toString());
        node.setUserObject(userObject);
        return node;
    }
    private DefaultMutableTreeNode createSubDnNode(Dn dn, Token token) {
        DefaultMutableTreeNode node = new ComparableDefaultMutableTreeNode();
        String label = isKeyValueView() ? token.getTokenValue() : token.getValue();
        XNodeInfo userObject =
                new XNodeInfo(Type.NONMBEAN, label, label, token.getTokenValue());
        node.setUserObject(userObject);
        return node;
    }
    private Object createNodeValue(XMBean xmbean, Token token) {
        String label = isKeyValueView() ? token.getTokenValue() : token.getValue();
        xmbean.setText(label);
        return xmbean;
    }
    private static Map<String, String> extractKeyValuePairs(
            String props, ObjectName mbean) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        int eq = props.indexOf("=");
        while (eq != -1) {
            String key = props.substring(0, eq);
            String value = mbean.getKeyProperty(key);
            map.put(key, value);
            props = props.substring(key.length() + 1 + value.length());
            if (props.startsWith(",")) {
                props = props.substring(1);
            }
            eq = props.indexOf("=");
        }
        return map;
    }
    private static String getKeyPropertyListString(ObjectName mbean) {
        String props = mbean.getKeyPropertyListString();
        Map<String, String> map = extractKeyValuePairs(props, mbean);
        StringBuilder sb = new StringBuilder();
        for (String key : orderedKeyPropertyList) {
            if (map.containsKey(key)) {
                sb.append(key + "=" + map.get(key) + ",");
                map.remove(key);
            }
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue() + ",");
        }
        String orderedKeyPropertyListString = sb.toString();
        orderedKeyPropertyListString = orderedKeyPropertyListString.substring(
                0, orderedKeyPropertyListString.length() - 1);
        return orderedKeyPropertyListString;
    }
    public void addMetadataNodes(DefaultMutableTreeNode node) {
        XMBean mbean = (XMBean) ((XNodeInfo) node.getUserObject()).getData();
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        MBeanInfoNodesSwingWorker sw =
                new MBeanInfoNodesSwingWorker(model, node, mbean);
        if (sw != null) {
            sw.execute();
        }
    }
    private static class MBeanInfoNodesSwingWorker
            extends SwingWorker<Object[], Void> {
        private final DefaultTreeModel model;
        private final DefaultMutableTreeNode node;
        private final XMBean mbean;
        public MBeanInfoNodesSwingWorker(
                DefaultTreeModel model,
                DefaultMutableTreeNode node,
                XMBean mbean) {
            this.model = model;
            this.node = node;
            this.mbean = mbean;
        }
        @Override
        public Object[] doInBackground() throws InstanceNotFoundException,
                IntrospectionException, ReflectionException, IOException {
            Object result[] = new Object[2];
            result[0] = mbean.getMBeanInfo();
            result[1] = mbean.isBroadcaster();
            return result;
        }
        @Override
        protected void done() {
            try {
                Object result[] = get();
                MBeanInfo mbeanInfo = (MBeanInfo) result[0];
                Boolean isBroadcaster = (Boolean) result[1];
                if (mbeanInfo != null) {
                    addMBeanInfoNodes(model, node, mbean, mbeanInfo, isBroadcaster);
                }
            } catch (Exception e) {
                Throwable t = Utils.getActualException(e);
                if (JConsole.isDebug()) {
                    t.printStackTrace();
                }
            }
        }
        private void addMBeanInfoNodes(
                DefaultTreeModel tree, DefaultMutableTreeNode node,
                XMBean mbean, MBeanInfo mbeanInfo, Boolean isBroadcaster) {
            MBeanAttributeInfo[] ai = mbeanInfo.getAttributes();
            MBeanOperationInfo[] oi = mbeanInfo.getOperations();
            MBeanNotificationInfo[] ni = mbeanInfo.getNotifications();
            int childIndex = 0;
            if (ai != null && ai.length > 0) {
                DefaultMutableTreeNode attributes = new DefaultMutableTreeNode();
                XNodeInfo attributesUO = new XNodeInfo(Type.ATTRIBUTES, mbean,
                        Resources.getText("Attributes"), null);
                attributes.setUserObject(attributesUO);
                node.insert(attributes, childIndex++);
                for (MBeanAttributeInfo mbai : ai) {
                    DefaultMutableTreeNode attribute = new DefaultMutableTreeNode();
                    XNodeInfo attributeUO = new XNodeInfo(Type.ATTRIBUTE,
                            new Object[]{mbean, mbai}, mbai.getName(), null);
                    attribute.setUserObject(attributeUO);
                    attribute.setAllowsChildren(false);
                    attributes.add(attribute);
                }
            }
            if (oi != null && oi.length > 0) {
                DefaultMutableTreeNode operations = new DefaultMutableTreeNode();
                XNodeInfo operationsUO = new XNodeInfo(Type.OPERATIONS, mbean,
                        Resources.getText("Operations"), null);
                operations.setUserObject(operationsUO);
                node.insert(operations, childIndex++);
                for (MBeanOperationInfo mboi : oi) {
                    StringBuilder sb = new StringBuilder();
                    for (MBeanParameterInfo mbpi : mboi.getSignature()) {
                        sb.append(mbpi.getType() + ",");
                    }
                    String signature = sb.toString();
                    if (signature.length() > 0) {
                        signature = signature.substring(0, signature.length() - 1);
                    }
                    String toolTipText = mboi.getName() + "(" + signature + ")";
                    DefaultMutableTreeNode operation = new DefaultMutableTreeNode();
                    XNodeInfo operationUO = new XNodeInfo(Type.OPERATION,
                            new Object[]{mbean, mboi}, mboi.getName(), toolTipText);
                    operation.setUserObject(operationUO);
                    operation.setAllowsChildren(false);
                    operations.add(operation);
                }
            }
            if (isBroadcaster != null && isBroadcaster.booleanValue()) {
                DefaultMutableTreeNode notifications = new DefaultMutableTreeNode();
                XNodeInfo notificationsUO = new XNodeInfo(Type.NOTIFICATIONS, mbean,
                        Resources.getText("Notifications"), null);
                notifications.setUserObject(notificationsUO);
                node.insert(notifications, childIndex++);
                if (ni != null && ni.length > 0) {
                    for (MBeanNotificationInfo mbni : ni) {
                        DefaultMutableTreeNode notification =
                                new DefaultMutableTreeNode();
                        XNodeInfo notificationUO = new XNodeInfo(Type.NOTIFICATION,
                                mbni, mbni.getName(), null);
                        notification.setUserObject(notificationUO);
                        notification.setAllowsChildren(false);
                        notifications.add(notification);
                    }
                }
            }
            model.reload(node);
        }
    }
    private static boolean treeView;
    private static boolean treeViewInit = false;
    private static boolean isTreeView() {
        if (!treeViewInit) {
            treeView = getTreeViewValue();
            treeViewInit = true;
        }
        return treeView;
    }
    private static boolean getTreeViewValue() {
        String tv = System.getProperty("treeView");
        return ((tv == null) ? true : !(tv.equals("false")));
    }
    private boolean keyValueView = Boolean.getBoolean("keyValueView");
    private boolean isKeyValueView() {
        return keyValueView;
    }
    private static class ComparableDefaultMutableTreeNode
            extends DefaultMutableTreeNode
            implements Comparable<DefaultMutableTreeNode> {
        public int compareTo(DefaultMutableTreeNode node) {
            return (this.toString().compareTo(node.toString()));
        }
    }
    private static class Dn implements Comparable<Dn> {
        private ObjectName mbean;
        private String domain;
        private String keyPropertyList;
        private String hashDn;
        private List<Token> tokens = new ArrayList<Token>();
        public Dn(ObjectName mbean) {
            this.mbean = mbean;
            this.domain = mbean.getDomain();
            this.keyPropertyList = getKeyPropertyListString(mbean);
            if (isTreeView()) {
                Map<String, String> map =
                        extractKeyValuePairs(keyPropertyList, mbean);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    tokens.add(new Token("key", entry.getKey() + "=" + entry.getValue()));
                }
            } else {
                tokens.add(new Token("key", "properties=" + keyPropertyList));
            }
            tokens.add(0, new Token("domain", "domain=" + domain));
            Collections.reverse(tokens);
            computeHashDn();
        }
        public ObjectName getObjectName() {
            return mbean;
        }
        public String getDomain() {
            return domain;
        }
        public String getKeyPropertyList() {
            return keyPropertyList;
        }
        public Token getToken(int index) {
            return tokens.get(index);
        }
        public int getTokenCount() {
            return tokens.size();
        }
        public String getHashDn() {
            return hashDn;
        }
        public String getHashKey(Token token) {
            final int begin = hashDn.indexOf(token.getTokenValue());
            return hashDn.substring(begin, hashDn.length());
        }
        private void computeHashDn() {
            if (tokens.isEmpty()) {
                return;
            }
            final StringBuilder hdn = new StringBuilder();
            for (int i = 0; i < tokens.size(); i++) {
                hdn.append(tokens.get(i).getTokenValue());
                hdn.append(",");
            }
            hashDn = hdn.substring(0, hdn.length() - 1);
        }
        @Override
        public String toString() {
            return domain + ":" + keyPropertyList;
        }
        public int compareTo(Dn dn) {
            return this.toString().compareTo(dn.toString());
        }
    }
    private static class Token {
        private String tokenType;
        private String tokenValue;
        private String key;
        private String value;
        public Token(String tokenType, String tokenValue) {
            this.tokenType = tokenType;
            this.tokenValue = tokenValue;
            buildKeyValue();
        }
        public String getTokenType() {
            return tokenType;
        }
        public String getTokenValue() {
            return tokenValue;
        }
        public String getKey() {
            return key;
        }
        public String getValue() {
            return value;
        }
        private void buildKeyValue() {
            int index = tokenValue.indexOf("=");
            if (index < 0) {
                key = tokenValue;
                value = tokenValue;
            } else {
                key = tokenValue.substring(0, index);
                value = tokenValue.substring(index + 1, tokenValue.length());
            }
        }
    }
}
