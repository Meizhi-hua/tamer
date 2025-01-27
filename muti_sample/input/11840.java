public class TreeMaker implements JCTree.Factory {
    protected static final Context.Key<TreeMaker> treeMakerKey =
        new Context.Key<TreeMaker>();
    public static TreeMaker instance(Context context) {
        TreeMaker instance = context.get(treeMakerKey);
        if (instance == null)
            instance = new TreeMaker(context);
        return instance;
    }
    public int pos = Position.NOPOS;
    public JCCompilationUnit toplevel;
    Names names;
    Types types;
    Symtab syms;
    protected TreeMaker(Context context) {
        context.put(treeMakerKey, this);
        this.pos = Position.NOPOS;
        this.toplevel = null;
        this.names = Names.instance(context);
        this.syms = Symtab.instance(context);
        this.types = Types.instance(context);
    }
    TreeMaker(JCCompilationUnit toplevel, Names names, Types types, Symtab syms) {
        this.pos = Position.FIRSTPOS;
        this.toplevel = toplevel;
        this.names = names;
        this.types = types;
        this.syms = syms;
    }
    public TreeMaker forToplevel(JCCompilationUnit toplevel) {
        return new TreeMaker(toplevel, names, types, syms);
    }
    public TreeMaker at(int pos) {
        this.pos = pos;
        return this;
    }
    public TreeMaker at(DiagnosticPosition pos) {
        this.pos = (pos == null ? Position.NOPOS : pos.getStartPosition());
        return this;
    }
    public JCCompilationUnit TopLevel(List<JCAnnotation> packageAnnotations,
                                      JCExpression pid,
                                      List<JCTree> defs) {
        Assert.checkNonNull(packageAnnotations);
        for (JCTree node : defs)
            Assert.check(node instanceof JCClassDecl
                || node instanceof JCImport
                || node instanceof JCSkip
                || node instanceof JCErroneous
                || (node instanceof JCExpressionStatement
                    && ((JCExpressionStatement)node).expr instanceof JCErroneous),
                node.getClass().getSimpleName());
        JCCompilationUnit tree = new JCCompilationUnit(packageAnnotations, pid, defs,
                                     null, null, null, null);
        tree.pos = pos;
        return tree;
    }
    public JCImport Import(JCTree qualid, boolean importStatic) {
        JCImport tree = new JCImport(qualid, importStatic);
        tree.pos = pos;
        return tree;
    }
    public JCClassDecl ClassDef(JCModifiers mods,
                                Name name,
                                List<JCTypeParameter> typarams,
                                JCExpression extending,
                                List<JCExpression> implementing,
                                List<JCTree> defs)
    {
        JCClassDecl tree = new JCClassDecl(mods,
                                     name,
                                     typarams,
                                     extending,
                                     implementing,
                                     defs,
                                     null);
        tree.pos = pos;
        return tree;
    }
    public JCMethodDecl MethodDef(JCModifiers mods,
                               Name name,
                               JCExpression restype,
                               List<JCTypeParameter> typarams,
                               List<JCVariableDecl> params,
                               List<JCExpression> thrown,
                               JCBlock body,
                               JCExpression defaultValue) {
        JCMethodDecl tree = new JCMethodDecl(mods,
                                       name,
                                       restype,
                                       typarams,
                                       params,
                                       thrown,
                                       body,
                                       defaultValue,
                                       null);
        tree.pos = pos;
        return tree;
    }
    public JCVariableDecl VarDef(JCModifiers mods, Name name, JCExpression vartype, JCExpression init) {
        JCVariableDecl tree = new JCVariableDecl(mods, name, vartype, init, null);
        tree.pos = pos;
        return tree;
    }
    public JCSkip Skip() {
        JCSkip tree = new JCSkip();
        tree.pos = pos;
        return tree;
    }
    public JCBlock Block(long flags, List<JCStatement> stats) {
        JCBlock tree = new JCBlock(flags, stats);
        tree.pos = pos;
        return tree;
    }
    public JCDoWhileLoop DoLoop(JCStatement body, JCExpression cond) {
        JCDoWhileLoop tree = new JCDoWhileLoop(body, cond);
        tree.pos = pos;
        return tree;
    }
    public JCWhileLoop WhileLoop(JCExpression cond, JCStatement body) {
        JCWhileLoop tree = new JCWhileLoop(cond, body);
        tree.pos = pos;
        return tree;
    }
    public JCForLoop ForLoop(List<JCStatement> init,
                           JCExpression cond,
                           List<JCExpressionStatement> step,
                           JCStatement body)
    {
        JCForLoop tree = new JCForLoop(init, cond, step, body);
        tree.pos = pos;
        return tree;
    }
    public JCEnhancedForLoop ForeachLoop(JCVariableDecl var, JCExpression expr, JCStatement body) {
        JCEnhancedForLoop tree = new JCEnhancedForLoop(var, expr, body);
        tree.pos = pos;
        return tree;
    }
    public JCLabeledStatement Labelled(Name label, JCStatement body) {
        JCLabeledStatement tree = new JCLabeledStatement(label, body);
        tree.pos = pos;
        return tree;
    }
    public JCSwitch Switch(JCExpression selector, List<JCCase> cases) {
        JCSwitch tree = new JCSwitch(selector, cases);
        tree.pos = pos;
        return tree;
    }
    public JCCase Case(JCExpression pat, List<JCStatement> stats) {
        JCCase tree = new JCCase(pat, stats);
        tree.pos = pos;
        return tree;
    }
    public JCSynchronized Synchronized(JCExpression lock, JCBlock body) {
        JCSynchronized tree = new JCSynchronized(lock, body);
        tree.pos = pos;
        return tree;
    }
    public JCTry Try(JCBlock body, List<JCCatch> catchers, JCBlock finalizer) {
        return Try(List.<JCTree>nil(), body, catchers, finalizer);
    }
    public JCTry Try(List<JCTree> resources,
                     JCBlock body,
                     List<JCCatch> catchers,
                     JCBlock finalizer) {
        JCTry tree = new JCTry(resources, body, catchers, finalizer);
        tree.pos = pos;
        return tree;
    }
    public JCCatch Catch(JCVariableDecl param, JCBlock body) {
        JCCatch tree = new JCCatch(param, body);
        tree.pos = pos;
        return tree;
    }
    public JCConditional Conditional(JCExpression cond,
                                   JCExpression thenpart,
                                   JCExpression elsepart)
    {
        JCConditional tree = new JCConditional(cond, thenpart, elsepart);
        tree.pos = pos;
        return tree;
    }
    public JCIf If(JCExpression cond, JCStatement thenpart, JCStatement elsepart) {
        JCIf tree = new JCIf(cond, thenpart, elsepart);
        tree.pos = pos;
        return tree;
    }
    public JCExpressionStatement Exec(JCExpression expr) {
        JCExpressionStatement tree = new JCExpressionStatement(expr);
        tree.pos = pos;
        return tree;
    }
    public JCBreak Break(Name label) {
        JCBreak tree = new JCBreak(label, null);
        tree.pos = pos;
        return tree;
    }
    public JCContinue Continue(Name label) {
        JCContinue tree = new JCContinue(label, null);
        tree.pos = pos;
        return tree;
    }
    public JCReturn Return(JCExpression expr) {
        JCReturn tree = new JCReturn(expr);
        tree.pos = pos;
        return tree;
    }
    public JCThrow Throw(JCTree expr) {
        JCThrow tree = new JCThrow(expr);
        tree.pos = pos;
        return tree;
    }
    public JCAssert Assert(JCExpression cond, JCExpression detail) {
        JCAssert tree = new JCAssert(cond, detail);
        tree.pos = pos;
        return tree;
    }
    public JCMethodInvocation Apply(List<JCExpression> typeargs,
                       JCExpression fn,
                       List<JCExpression> args)
    {
        JCMethodInvocation tree = new JCMethodInvocation(typeargs, fn, args);
        tree.pos = pos;
        return tree;
    }
    public JCNewClass NewClass(JCExpression encl,
                             List<JCExpression> typeargs,
                             JCExpression clazz,
                             List<JCExpression> args,
                             JCClassDecl def)
    {
        JCNewClass tree = new JCNewClass(encl, typeargs, clazz, args, def);
        tree.pos = pos;
        return tree;
    }
    public JCNewArray NewArray(JCExpression elemtype,
                             List<JCExpression> dims,
                             List<JCExpression> elems)
    {
        JCNewArray tree = new JCNewArray(elemtype, dims, elems);
        tree.pos = pos;
        return tree;
    }
    public JCParens Parens(JCExpression expr) {
        JCParens tree = new JCParens(expr);
        tree.pos = pos;
        return tree;
    }
    public JCAssign Assign(JCExpression lhs, JCExpression rhs) {
        JCAssign tree = new JCAssign(lhs, rhs);
        tree.pos = pos;
        return tree;
    }
    public JCAssignOp Assignop(int opcode, JCTree lhs, JCTree rhs) {
        JCAssignOp tree = new JCAssignOp(opcode, lhs, rhs, null);
        tree.pos = pos;
        return tree;
    }
    public JCUnary Unary(int opcode, JCExpression arg) {
        JCUnary tree = new JCUnary(opcode, arg);
        tree.pos = pos;
        return tree;
    }
    public JCBinary Binary(int opcode, JCExpression lhs, JCExpression rhs) {
        JCBinary tree = new JCBinary(opcode, lhs, rhs, null);
        tree.pos = pos;
        return tree;
    }
    public JCTypeCast TypeCast(JCTree clazz, JCExpression expr) {
        JCTypeCast tree = new JCTypeCast(clazz, expr);
        tree.pos = pos;
        return tree;
    }
    public JCInstanceOf TypeTest(JCExpression expr, JCTree clazz) {
        JCInstanceOf tree = new JCInstanceOf(expr, clazz);
        tree.pos = pos;
        return tree;
    }
    public JCArrayAccess Indexed(JCExpression indexed, JCExpression index) {
        JCArrayAccess tree = new JCArrayAccess(indexed, index);
        tree.pos = pos;
        return tree;
    }
    public JCFieldAccess Select(JCExpression selected, Name selector) {
        JCFieldAccess tree = new JCFieldAccess(selected, selector, null);
        tree.pos = pos;
        return tree;
    }
    public JCIdent Ident(Name name) {
        JCIdent tree = new JCIdent(name, null);
        tree.pos = pos;
        return tree;
    }
    public JCLiteral Literal(int tag, Object value) {
        JCLiteral tree = new JCLiteral(tag, value);
        tree.pos = pos;
        return tree;
    }
    public JCPrimitiveTypeTree TypeIdent(int typetag) {
        JCPrimitiveTypeTree tree = new JCPrimitiveTypeTree(typetag);
        tree.pos = pos;
        return tree;
    }
    public JCArrayTypeTree TypeArray(JCExpression elemtype) {
        JCArrayTypeTree tree = new JCArrayTypeTree(elemtype);
        tree.pos = pos;
        return tree;
    }
    public JCTypeApply TypeApply(JCExpression clazz, List<JCExpression> arguments) {
        JCTypeApply tree = new JCTypeApply(clazz, arguments);
        tree.pos = pos;
        return tree;
    }
    public JCTypeUnion TypeUnion(List<JCExpression> components) {
        JCTypeUnion tree = new JCTypeUnion(components);
        tree.pos = pos;
        return tree;
    }
    public JCTypeParameter TypeParameter(Name name, List<JCExpression> bounds) {
        JCTypeParameter tree = new JCTypeParameter(name, bounds);
        tree.pos = pos;
        return tree;
    }
    public JCWildcard Wildcard(TypeBoundKind kind, JCTree type) {
        JCWildcard tree = new JCWildcard(kind, type);
        tree.pos = pos;
        return tree;
    }
    public TypeBoundKind TypeBoundKind(BoundKind kind) {
        TypeBoundKind tree = new TypeBoundKind(kind);
        tree.pos = pos;
        return tree;
    }
    public JCAnnotation Annotation(JCTree annotationType, List<JCExpression> args) {
        JCAnnotation tree = new JCAnnotation(annotationType, args);
        tree.pos = pos;
        return tree;
    }
    public JCModifiers Modifiers(long flags, List<JCAnnotation> annotations) {
        JCModifiers tree = new JCModifiers(flags, annotations);
        boolean noFlags = (flags & (Flags.ModifierFlags | Flags.ANNOTATION)) == 0;
        tree.pos = (noFlags && annotations.isEmpty()) ? Position.NOPOS : pos;
        return tree;
    }
    public JCModifiers Modifiers(long flags) {
        return Modifiers(flags, List.<JCAnnotation>nil());
    }
    public JCErroneous Erroneous() {
        return Erroneous(List.<JCTree>nil());
    }
    public JCErroneous Erroneous(List<? extends JCTree> errs) {
        JCErroneous tree = new JCErroneous(errs);
        tree.pos = pos;
        return tree;
    }
    public LetExpr LetExpr(List<JCVariableDecl> defs, JCTree expr) {
        LetExpr tree = new LetExpr(defs, expr);
        tree.pos = pos;
        return tree;
    }
    public JCClassDecl AnonymousClassDef(JCModifiers mods,
                                         List<JCTree> defs)
    {
        return ClassDef(mods,
                        names.empty,
                        List.<JCTypeParameter>nil(),
                        null,
                        List.<JCExpression>nil(),
                        defs);
    }
    public LetExpr LetExpr(JCVariableDecl def, JCTree expr) {
        LetExpr tree = new LetExpr(List.of(def), expr);
        tree.pos = pos;
        return tree;
    }
    public JCIdent Ident(Symbol sym) {
        return (JCIdent)new JCIdent((sym.name != names.empty)
                                ? sym.name
                                : sym.flatName(), sym)
            .setPos(pos)
            .setType(sym.type);
    }
    public JCExpression Select(JCExpression base, Symbol sym) {
        return new JCFieldAccess(base, sym.name, sym).setPos(pos).setType(sym.type);
    }
    public JCExpression QualIdent(Symbol sym) {
        return isUnqualifiable(sym)
            ? Ident(sym)
            : Select(QualIdent(sym.owner), sym);
    }
    public JCExpression Ident(JCVariableDecl param) {
        return Ident(param.sym);
    }
    public List<JCExpression> Idents(List<JCVariableDecl> params) {
        ListBuffer<JCExpression> ids = new ListBuffer<JCExpression>();
        for (List<JCVariableDecl> l = params; l.nonEmpty(); l = l.tail)
            ids.append(Ident(l.head));
        return ids.toList();
    }
    public JCExpression This(Type t) {
        return Ident(new VarSymbol(FINAL, names._this, t, t.tsym));
    }
    public JCExpression ClassLiteral(ClassSymbol clazz) {
        return ClassLiteral(clazz.type);
    }
    public JCExpression ClassLiteral(Type t) {
        VarSymbol lit = new VarSymbol(STATIC | PUBLIC | FINAL,
                                      names._class,
                                      t,
                                      t.tsym);
        return Select(Type(t), lit);
    }
    public JCIdent Super(Type t, TypeSymbol owner) {
        return Ident(new VarSymbol(FINAL, names._super, t, owner));
    }
    public JCMethodInvocation App(JCExpression meth, List<JCExpression> args) {
        return Apply(null, meth, args).setType(meth.type.getReturnType());
    }
    public JCMethodInvocation App(JCExpression meth) {
        return Apply(null, meth, List.<JCExpression>nil()).setType(meth.type.getReturnType());
    }
    public JCExpression Create(Symbol ctor, List<JCExpression> args) {
        Type t = ctor.owner.erasure(types);
        JCNewClass newclass = NewClass(null, null, Type(t), args, null);
        newclass.constructor = ctor;
        newclass.setType(t);
        return newclass;
    }
    public JCExpression Type(Type t) {
        if (t == null) return null;
        JCExpression tp;
        switch (t.tag) {
        case BYTE: case CHAR: case SHORT: case INT: case LONG: case FLOAT:
        case DOUBLE: case BOOLEAN: case VOID:
            tp = TypeIdent(t.tag);
            break;
        case TYPEVAR:
            tp = Ident(t.tsym);
            break;
        case WILDCARD: {
            WildcardType a = ((WildcardType) t);
            tp = Wildcard(TypeBoundKind(a.kind), Type(a.type));
            break;
        }
        case CLASS:
            Type outer = t.getEnclosingType();
            JCExpression clazz = outer.tag == CLASS && t.tsym.owner.kind == TYP
                ? Select(Type(outer), t.tsym)
                : QualIdent(t.tsym);
            tp = t.getTypeArguments().isEmpty()
                ? clazz
                : TypeApply(clazz, Types(t.getTypeArguments()));
            break;
        case ARRAY:
            tp = TypeArray(Type(types.elemtype(t)));
            break;
        case ERROR:
            tp = TypeIdent(ERROR);
            break;
        default:
            throw new AssertionError("unexpected type: " + t);
        }
        return tp.setType(t);
    }
    public List<JCExpression> Types(List<Type> ts) {
        ListBuffer<JCExpression> lb = new ListBuffer<JCExpression>();
        for (List<Type> l = ts; l.nonEmpty(); l = l.tail)
            lb.append(Type(l.head));
        return lb.toList();
    }
    public JCVariableDecl VarDef(VarSymbol v, JCExpression init) {
        return (JCVariableDecl)
            new JCVariableDecl(
                Modifiers(v.flags(), Annotations(v.getAnnotationMirrors())),
                v.name,
                Type(v.type),
                init,
                v).setPos(pos).setType(v.type);
    }
    public List<JCAnnotation> Annotations(List<Attribute.Compound> attributes) {
        if (attributes == null) return List.nil();
        ListBuffer<JCAnnotation> result = new ListBuffer<JCAnnotation>();
        for (List<Attribute.Compound> i = attributes; i.nonEmpty(); i=i.tail) {
            Attribute a = i.head;
            result.append(Annotation(a));
        }
        return result.toList();
    }
    public JCLiteral Literal(Object value) {
        JCLiteral result = null;
        if (value instanceof String) {
            result = Literal(CLASS, value).
                setType(syms.stringType.constType(value));
        } else if (value instanceof Integer) {
            result = Literal(INT, value).
                setType(syms.intType.constType(value));
        } else if (value instanceof Long) {
            result = Literal(LONG, value).
                setType(syms.longType.constType(value));
        } else if (value instanceof Byte) {
            result = Literal(BYTE, value).
                setType(syms.byteType.constType(value));
        } else if (value instanceof Character) {
            int v = (int) (((Character) value).toString().charAt(0));
            result = Literal(CHAR, value).
                setType(syms.charType.constType(v));
        } else if (value instanceof Double) {
            result = Literal(DOUBLE, value).
                setType(syms.doubleType.constType(value));
        } else if (value instanceof Float) {
            result = Literal(FLOAT, value).
                setType(syms.floatType.constType(value));
        } else if (value instanceof Short) {
            result = Literal(SHORT, value).
                setType(syms.shortType.constType(value));
        } else if (value instanceof Boolean) {
            int v = ((Boolean) value) ? 1 : 0;
            result = Literal(BOOLEAN, v).
                setType(syms.booleanType.constType(v));
        } else {
            throw new AssertionError(value);
        }
        return result;
    }
    class AnnotationBuilder implements Attribute.Visitor {
        JCExpression result = null;
        public void visitConstant(Attribute.Constant v) {
            result = Literal(v.value);
        }
        public void visitClass(Attribute.Class clazz) {
            result = ClassLiteral(clazz.type).setType(syms.classType);
        }
        public void visitEnum(Attribute.Enum e) {
            result = QualIdent(e.value);
        }
        public void visitError(Attribute.Error e) {
            result = Erroneous();
        }
        public void visitCompound(Attribute.Compound compound) {
            result = visitCompoundInternal(compound);
        }
        public JCAnnotation visitCompoundInternal(Attribute.Compound compound) {
            ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
            for (List<Pair<Symbol.MethodSymbol,Attribute>> values = compound.values; values.nonEmpty(); values=values.tail) {
                Pair<MethodSymbol,Attribute> pair = values.head;
                JCExpression valueTree = translate(pair.snd);
                args.append(Assign(Ident(pair.fst), valueTree).setType(valueTree.type));
            }
            return Annotation(Type(compound.type), args.toList());
        }
        public void visitArray(Attribute.Array array) {
            ListBuffer<JCExpression> elems = new ListBuffer<JCExpression>();
            for (int i = 0; i < array.values.length; i++)
                elems.append(translate(array.values[i]));
            result = NewArray(null, List.<JCExpression>nil(), elems.toList()).setType(array.type);
        }
        JCExpression translate(Attribute a) {
            a.accept(this);
            return result;
        }
        JCAnnotation translate(Attribute.Compound a) {
            return visitCompoundInternal(a);
        }
    }
    AnnotationBuilder annotationBuilder = new AnnotationBuilder();
    public JCAnnotation Annotation(Attribute a) {
        return annotationBuilder.translate((Attribute.Compound)a);
    }
    public JCMethodDecl MethodDef(MethodSymbol m, JCBlock body) {
        return MethodDef(m, m.type, body);
    }
    public JCMethodDecl MethodDef(MethodSymbol m, Type mtype, JCBlock body) {
        return (JCMethodDecl)
            new JCMethodDecl(
                Modifiers(m.flags(), Annotations(m.getAnnotationMirrors())),
                m.name,
                Type(mtype.getReturnType()),
                TypeParams(mtype.getTypeArguments()),
                Params(mtype.getParameterTypes(), m),
                Types(mtype.getThrownTypes()),
                body,
                null,
                m).setPos(pos).setType(mtype);
    }
    public JCTypeParameter TypeParam(Name name, TypeVar tvar) {
        return (JCTypeParameter)
            TypeParameter(name, Types(types.getBounds(tvar))).setPos(pos).setType(tvar);
    }
    public List<JCTypeParameter> TypeParams(List<Type> typarams) {
        ListBuffer<JCTypeParameter> tparams = new ListBuffer<JCTypeParameter>();
        int i = 0;
        for (List<Type> l = typarams; l.nonEmpty(); l = l.tail)
            tparams.append(TypeParam(l.head.tsym.name, (TypeVar)l.head));
        return tparams.toList();
    }
    public JCVariableDecl Param(Name name, Type argtype, Symbol owner) {
        return VarDef(new VarSymbol(0, name, argtype, owner), null);
    }
    public List<JCVariableDecl> Params(List<Type> argtypes, Symbol owner) {
        ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
        MethodSymbol mth = (owner.kind == MTH) ? ((MethodSymbol)owner) : null;
        if (mth != null && mth.params != null && argtypes.length() == mth.params.length()) {
            for (VarSymbol param : ((MethodSymbol)owner).params)
                params.append(VarDef(param, null));
        } else {
            int i = 0;
            for (List<Type> l = argtypes; l.nonEmpty(); l = l.tail)
                params.append(Param(paramName(i++), l.head, owner));
        }
        return params.toList();
    }
    public JCStatement Call(JCExpression apply) {
        return apply.type.tag == VOID ? Exec(apply) : Return(apply);
    }
    public JCStatement Assignment(Symbol v, JCExpression rhs) {
        return Exec(Assign(Ident(v), rhs).setType(v.type));
    }
    public JCArrayAccess Indexed(Symbol v, JCExpression index) {
        JCArrayAccess tree = new JCArrayAccess(QualIdent(v), index);
        tree.type = ((ArrayType)v.type).elemtype;
        return tree;
    }
    public JCTypeCast TypeCast(Type type, JCExpression expr) {
        return (JCTypeCast)TypeCast(Type(type), expr).setType(type);
    }
    boolean isUnqualifiable(Symbol sym) {
        if (sym.name == names.empty ||
            sym.owner == null ||
            sym.owner.kind == MTH || sym.owner.kind == VAR) {
            return true;
        } else if (sym.kind == TYP && toplevel != null) {
            Scope.Entry e;
            e = toplevel.namedImportScope.lookup(sym.name);
            if (e.scope != null) {
                return
                  e.sym == sym &&
                  e.next().scope == null;
            }
            e = toplevel.packge.members().lookup(sym.name);
            if (e.scope != null) {
                return
                  e.sym == sym &&
                  e.next().scope == null;
            }
            e = toplevel.starImportScope.lookup(sym.name);
            if (e.scope != null) {
                return
                  e.sym == sym &&
                  e.next().scope == null;
            }
        }
        return false;
    }
    public Name paramName(int i)   { return names.fromString("x" + i); }
    public Name typaramName(int i) { return names.fromString("A" + i); }
}
