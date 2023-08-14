public class JavacTrees extends Trees {
    private Resolve resolve;
    private Enter enter;
    private Log log;
    private MemberEnter memberEnter;
    private Attr attr;
    private TreeMaker treeMaker;
    private JavacElements elements;
    private JavacTaskImpl javacTaskImpl;
    public static JavacTrees instance(JavaCompiler.CompilationTask task) {
        if (!(task instanceof JavacTaskImpl))
            throw new IllegalArgumentException();
        return instance(((JavacTaskImpl)task).getContext());
    }
    public static JavacTrees instance(ProcessingEnvironment env) {
        if (!(env instanceof JavacProcessingEnvironment))
            throw new IllegalArgumentException();
        return instance(((JavacProcessingEnvironment)env).getContext());
    }
    public static JavacTrees instance(Context context) {
        JavacTrees instance = context.get(JavacTrees.class);
        if (instance == null)
            instance = new JavacTrees(context);
        return instance;
    }
    private JavacTrees(Context context) {
        context.put(JavacTrees.class, this);
        init(context);
    }
    public void updateContext(Context context) {
        init(context);
    }
    private void init(Context context) {
        attr = Attr.instance(context);
        enter = Enter.instance(context);
        elements = JavacElements.instance(context);
        log = Log.instance(context);
        resolve = Resolve.instance(context);
        treeMaker = TreeMaker.instance(context);
        memberEnter = MemberEnter.instance(context);
        javacTaskImpl = context.get(JavacTaskImpl.class);
    }
    public SourcePositions getSourcePositions() {
        return new SourcePositions() {
                public long getStartPosition(CompilationUnitTree file, Tree tree) {
                    return TreeInfo.getStartPos((JCTree) tree);
                }
                public long getEndPosition(CompilationUnitTree file, Tree tree) {
                    Map<JCTree,Integer> endPositions = ((JCCompilationUnit) file).endPositions;
                    return TreeInfo.getEndPos((JCTree) tree, endPositions);
                }
            };
    }
    public JCClassDecl getTree(TypeElement element) {
        return (JCClassDecl) getTree((Element) element);
    }
    public JCMethodDecl getTree(ExecutableElement method) {
        return (JCMethodDecl) getTree((Element) method);
    }
    public JCTree getTree(Element element) {
        Symbol symbol = (Symbol) element;
        TypeSymbol enclosing = symbol.enclClass();
        Env<AttrContext> env = enter.getEnv(enclosing);
        if (env == null)
            return null;
        JCClassDecl classNode = env.enclClass;
        if (classNode != null) {
            if (TreeInfo.symbolFor(classNode) == element)
                return classNode;
            for (JCTree node : classNode.getMembers())
                if (TreeInfo.symbolFor(node) == element)
                    return node;
        }
        return null;
    }
    public JCTree getTree(Element e, AnnotationMirror a) {
        return getTree(e, a, null);
    }
    public JCTree getTree(Element e, AnnotationMirror a, AnnotationValue v) {
        Pair<JCTree, JCCompilationUnit> treeTopLevel = elements.getTreeAndTopLevel(e, a, v);
        if (treeTopLevel == null)
            return null;
        return treeTopLevel.fst;
    }
    public TreePath getPath(CompilationUnitTree unit, Tree node) {
        return TreePath.getPath(unit, node);
    }
    public TreePath getPath(Element e) {
        return getPath(e, null, null);
    }
    public TreePath getPath(Element e, AnnotationMirror a) {
        return getPath(e, a, null);
    }
    public TreePath getPath(Element e, AnnotationMirror a, AnnotationValue v) {
        final Pair<JCTree, JCCompilationUnit> treeTopLevel = elements.getTreeAndTopLevel(e, a, v);
        if (treeTopLevel == null)
            return null;
        return TreePath.getPath(treeTopLevel.snd, treeTopLevel.fst);
    }
    public Element getElement(TreePath path) {
        JCTree tree = (JCTree) path.getLeaf();
        Symbol sym = TreeInfo.symbolFor(tree);
        if (sym == null && TreeInfo.isDeclaration(tree)) {
            for (TreePath p = path; p != null; p = p.getParentPath()) {
                JCTree t = (JCTree) p.getLeaf();
                if (t.getTag() == JCTree.CLASSDEF) {
                    JCClassDecl ct = (JCClassDecl) t;
                    if (ct.sym != null) {
                        if ((ct.sym.flags_field & Flags.UNATTRIBUTED) != 0) {
                            attr.attribClass(ct.pos(), ct.sym);
                            sym = TreeInfo.symbolFor(tree);
                        }
                        break;
                    }
                }
            }
        }
        return sym;
    }
    public TypeMirror getTypeMirror(TreePath path) {
        Tree t = path.getLeaf();
        return ((JCTree)t).type;
    }
    public JavacScope getScope(TreePath path) {
        return new JavacScope(getAttrContext(path));
    }
    public String getDocComment(TreePath path) {
        CompilationUnitTree t = path.getCompilationUnit();
        if (t instanceof JCTree.JCCompilationUnit) {
            JCCompilationUnit cu = (JCCompilationUnit) t;
            if (cu.docComments != null) {
                return cu.docComments.get(path.getLeaf());
            }
        }
        return null;
    }
    public boolean isAccessible(Scope scope, TypeElement type) {
        if (scope instanceof JavacScope && type instanceof ClassSymbol) {
            Env<AttrContext> env = ((JavacScope) scope).env;
            return resolve.isAccessible(env, (ClassSymbol)type, true);
        } else
            return false;
    }
    public boolean isAccessible(Scope scope, Element member, DeclaredType type) {
        if (scope instanceof JavacScope
                && member instanceof Symbol
                && type instanceof com.sun.tools.javac.code.Type) {
            Env<AttrContext> env = ((JavacScope) scope).env;
            return resolve.isAccessible(env, (com.sun.tools.javac.code.Type)type, (Symbol)member, true);
        } else
            return false;
    }
    private Env<AttrContext> getAttrContext(TreePath path) {
        if (!(path.getLeaf() instanceof JCTree))  
            throw new IllegalArgumentException();
        if (javacTaskImpl != null) {
            try {
                javacTaskImpl.enter(null);
            } catch (IOException e) {
                throw new Error("unexpected error while entering symbols: " + e);
            }
        }
        JCCompilationUnit unit = (JCCompilationUnit) path.getCompilationUnit();
        Copier copier = new Copier(treeMaker.forToplevel(unit));
        Env<AttrContext> env = null;
        JCMethodDecl method = null;
        JCVariableDecl field = null;
        List<Tree> l = List.nil();
        TreePath p = path;
        while (p != null) {
            l = l.prepend(p.getLeaf());
            p = p.getParentPath();
        }
        for ( ; l.nonEmpty(); l = l.tail) {
            Tree tree = l.head;
            switch (tree.getKind()) {
                case COMPILATION_UNIT:
                    env = enter.getTopLevelEnv((JCCompilationUnit)tree);
                    break;
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    env = enter.getClassEnv(((JCClassDecl)tree).sym);
                    break;
                case METHOD:
                    method = (JCMethodDecl)tree;
                    break;
                case VARIABLE:
                    field = (JCVariableDecl)tree;
                    break;
                case BLOCK: {
                    if (method != null)
                        env = memberEnter.getMethodEnv(method, env);
                    JCTree body = copier.copy((JCTree)tree, (JCTree) path.getLeaf());
                    env = attribStatToTree(body, env, copier.leafCopy);
                    return env;
                }
                default:
                    if (field != null && field.getInitializer() == tree) {
                        env = memberEnter.getInitEnv(field, env);
                        JCExpression expr = copier.copy((JCExpression)tree, (JCTree) path.getLeaf());
                        env = attribExprToTree(expr, env, copier.leafCopy);
                        return env;
                    }
            }
        }
        return field != null ? memberEnter.getInitEnv(field, env) : env;
    }
    private Env<AttrContext> attribStatToTree(JCTree stat, Env<AttrContext>env, JCTree tree) {
        JavaFileObject prev = log.useSource(env.toplevel.sourcefile);
        try {
            return attr.attribStatToTree(stat, env, tree);
        } finally {
            log.useSource(prev);
        }
    }
    private Env<AttrContext> attribExprToTree(JCExpression expr, Env<AttrContext>env, JCTree tree) {
        JavaFileObject prev = log.useSource(env.toplevel.sourcefile);
        try {
            return attr.attribExprToTree(expr, env, tree);
        } finally {
            log.useSource(prev);
        }
    }
    static class Copier extends TreeCopier<JCTree> {
        JCTree leafCopy = null;
        Copier(TreeMaker M) {
            super(M);
        }
        @Override
        public <T extends JCTree> T copy(T t, JCTree leaf) {
            T t2 = super.copy(t, leaf);
            if (t == leaf)
                leafCopy = t2;
            return t2;
        }
    }
    public TypeMirror getOriginalType(javax.lang.model.type.ErrorType errorType) {
        if (errorType instanceof com.sun.tools.javac.code.Type.ErrorType) {
            return ((com.sun.tools.javac.code.Type.ErrorType)errorType).getOriginalType();
        }
        return com.sun.tools.javac.code.Type.noType;
    }
    public void printMessage(Diagnostic.Kind kind, CharSequence msg,
            com.sun.source.tree.Tree t,
            com.sun.source.tree.CompilationUnitTree root) {
        JavaFileObject oldSource = null;
        JavaFileObject newSource = null;
        JCDiagnostic.DiagnosticPosition pos = null;
        newSource = root.getSourceFile();
        if (newSource != null) {
            oldSource = log.useSource(newSource);
            pos = ((JCTree) t).pos();
        }
        try {
            switch (kind) {
            case ERROR:
                boolean prev = log.multipleErrors;
                try {
                    log.error(pos, "proc.messager", msg.toString());
                } finally {
                    log.multipleErrors = prev;
                }
                break;
            case WARNING:
                log.warning(pos, "proc.messager", msg.toString());
                break;
            case MANDATORY_WARNING:
                log.mandatoryWarning(pos, "proc.messager", msg.toString());
                break;
            default:
                log.note(pos, "proc.messager", msg.toString());
            }
        } finally {
            if (oldSource != null)
                log.useSource(oldSource);
        }
    }
    @Override
    public TypeMirror getLub(CatchTree tree) {
        JCCatch ct = (JCCatch) tree;
        JCVariableDecl v = ct.param;
        if (v.type != null && v.type.getKind() == TypeKind.UNION) {
            UnionClassType ut = (UnionClassType) v.type;
            return ut.getLub();
        } else {
            return v.type;
        }
    }
}