public class MethodNode extends AbstractNode {
    private static class MethodNodeChildren extends Children.Keys {
        private InputMethod method;
        private InputGraph graph;
        private String bciString;
        public MethodNodeChildren(InputMethod method, InputGraph graph, String bciString) {
            this.method = method;
            this.bciString = bciString;
            this.graph = graph;
        }
        protected Node[] createNodes(Object object) {
            assert object instanceof InputBytecode;
            InputBytecode bc = (InputBytecode) object;
            if (bc.getInlined() == null) {
                return new Node[]{new BytecodeNode(bc, graph, bciString)};
            } else {
                return new Node[]{new BytecodeNode(bc, graph, bciString), new MethodNode(bc.getInlined(), graph, bc.getBci() + " " + bciString)};
            }
        }
        @Override
        public void addNotify() {
            if (method != null) {
                setKeys(method.getBytecodes());
            }
        }
        public void setMethod(InputMethod method, InputGraph graph) {
            this.method = method;
            this.graph = graph;
            addNotify();
        }
    }
    public MethodNode(InputMethod method, InputGraph graph, String bciString) {
        super((method != null && method.getBytecodes().size() == 0) ? Children.LEAF : new MethodNodeChildren(method, graph, bciString));
        if (method != null) {
            this.setDisplayName(method.getName());
        }
    }
    @Override
    public Image getIcon(int i) {
        return Utilities.loadImage("com/sun/hotspot/igv/bytecodes/images/method.gif");
    }
    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
    public void update(InputGraph graph, InputMethod method) {
        ((MethodNodeChildren) this.getChildren()).setMethod(method, graph);
        if (method != null) {
            this.setDisplayName(method.getName());
        }
    }
}