public class HierMemDirCtx implements DirContext {
    static private final boolean debug = false;
    private static final NameParser defaultParser = new HierarchicalNameParser();
    protected Hashtable myEnv;
    protected Hashtable bindings;
    protected Attributes attrs;
    protected boolean ignoreCase = false;
    protected NamingException readOnlyEx = null;
    protected NameParser myParser = defaultParser;
    private boolean alwaysUseFactory;
    public void close() throws NamingException {
        myEnv = null;
        bindings = null;
        attrs = null;
    }
    public String getNameInNamespace() throws NamingException {
        throw new OperationNotSupportedException(
            "Cannot determine full name");
    }
    public HierMemDirCtx() {
        this(null, false, false);
    }
    public HierMemDirCtx(boolean ignoreCase) {
        this(null, ignoreCase, false);
    }
    public HierMemDirCtx(Hashtable environment, boolean ignoreCase) {
        this(environment, ignoreCase, false);
    }
    protected HierMemDirCtx(Hashtable environment, boolean ignoreCase,
        boolean useFac) {
        myEnv = environment;
        this.ignoreCase = ignoreCase;
        init();
        this.alwaysUseFactory = useFac;
    }
    private void init() {
        attrs = new BasicAttributes(ignoreCase);
        bindings = new Hashtable(11, 0.75f);
    }
    public Object lookup(String name) throws NamingException {
        return lookup(myParser.parse(name));
    }
    public Object lookup(Name name) throws NamingException {
        return doLookup(name, alwaysUseFactory);
    }
    public Object doLookup(Name name, boolean useFactory)
        throws NamingException {
        Object target = null;
        name = canonizeName(name);
        switch(name.size()) {
        case 0:
            target = this;
            break;
        case 1:
            target = bindings.get(name);
            break;
        default:
            HierMemDirCtx ctx = (HierMemDirCtx)bindings.get(name.getPrefix(1));
            if(ctx == null) {
                target = null;
            } else {
                target = ctx.doLookup(name.getSuffix(1), false);
            }
            break;
        }
        if(target == null) {
            throw new NameNotFoundException(name.toString());
        }
        if (useFactory) {
            try {
                return DirectoryManager.getObjectInstance(target,
                    name, this, myEnv,
                    (target instanceof HierMemDirCtx) ?
                    ((HierMemDirCtx)target).attrs : null);
            } catch (NamingException e) {
                throw e;
            } catch (Exception e) {
                NamingException e2 = new NamingException(
                    "Problem calling getObjectInstance");
                e2.setRootCause(e);
                throw e2;
            }
        } else {
            return target;
        }
    }
    public void bind(String name, Object obj) throws NamingException {
        bind(myParser.parse(name), obj);
    }
    public void bind(Name name, Object obj) throws NamingException {
        doBind(name, obj, null, alwaysUseFactory);
    }
    public void bind(String name, Object obj, Attributes attrs)
            throws NamingException {
        bind(myParser.parse(name), obj, attrs);
    }
    public void bind(Name name, Object obj, Attributes attrs)
            throws NamingException {
        doBind(name, obj, attrs, alwaysUseFactory);
    }
    protected void doBind(Name name, Object obj, Attributes attrs,
        boolean useFactory) throws NamingException {
        if (name.isEmpty()) {
            throw new InvalidNameException("Cannot bind empty name");
        }
        if (useFactory) {
            DirStateFactory.Result res = DirectoryManager.getStateToBind(
                obj, name, this, myEnv, attrs);
            obj = res.getObject();
            attrs = res.getAttributes();
        }
        HierMemDirCtx ctx= (HierMemDirCtx) doLookup(getInternalName(name), false);
        ctx.doBindAux(getLeafName(name), obj);
        if (attrs != null && attrs.size() > 0) {
            modifyAttributes(name, ADD_ATTRIBUTE, attrs);
        }
    }
    protected void doBindAux(Name name, Object obj) throws NamingException {
        if (readOnlyEx != null) {
            throw (NamingException) readOnlyEx.fillInStackTrace();
        }
        if (bindings.get(name) != null) {
            throw new NameAlreadyBoundException(name.toString());
        }
        if(obj instanceof HierMemDirCtx) {
            bindings.put(name, obj);
        } else {
            throw new SchemaViolationException(
                "This context only supports binding objects of it's own kind");
        }
    }
    public void rebind(String name, Object obj) throws NamingException {
        rebind(myParser.parse(name), obj);
    }
    public void rebind(Name name, Object obj) throws NamingException {
        doRebind(name, obj, null, alwaysUseFactory);
    }
    public void rebind(String name, Object obj, Attributes attrs)
            throws NamingException {
        rebind(myParser.parse(name), obj, attrs);
    }
    public void rebind(Name name, Object obj, Attributes attrs)
            throws NamingException {
        doRebind(name, obj, attrs, alwaysUseFactory);
    }
    protected void doRebind(Name name, Object obj, Attributes attrs,
        boolean useFactory) throws NamingException {
        if (name.isEmpty()) {
            throw new InvalidNameException("Cannot rebind empty name");
        }
        if (useFactory) {
            DirStateFactory.Result res = DirectoryManager.getStateToBind(
                obj, name, this, myEnv, attrs);
            obj = res.getObject();
            attrs = res.getAttributes();
        }
        HierMemDirCtx ctx= (HierMemDirCtx) doLookup(getInternalName(name), false);
        ctx.doRebindAux(getLeafName(name), obj);
        if (attrs != null && attrs.size() > 0) {
            modifyAttributes(name, ADD_ATTRIBUTE, attrs);
        }
    }
    protected void doRebindAux(Name name, Object obj) throws NamingException {
        if (readOnlyEx != null) {
            throw (NamingException) readOnlyEx.fillInStackTrace();
        }
        if(obj instanceof HierMemDirCtx) {
            bindings.put(name, obj);
        } else {
            throw new SchemaViolationException(
                "This context only supports binding objects of it's own kind");
        }
    }
    public void unbind(String name) throws NamingException {
        unbind(myParser.parse(name));
    }
    public void unbind(Name name) throws NamingException {
        if (name.isEmpty()) {
            throw new InvalidNameException("Cannot unbind empty name");
        } else {
            HierMemDirCtx ctx=
                (HierMemDirCtx) doLookup(getInternalName(name), false);
            ctx.doUnbind(getLeafName(name));
        }
    }
    protected void doUnbind(Name name) throws NamingException {
        if (readOnlyEx != null) {
            throw (NamingException) readOnlyEx.fillInStackTrace();
        }
        bindings.remove(name);  
    }
    public void rename(String oldname, String newname)
            throws NamingException {
         rename(myParser.parse(oldname), myParser.parse(newname));
    }
    public void rename(Name oldname, Name newname)
            throws NamingException {
        if(newname.isEmpty() || oldname.isEmpty()) {
            throw new InvalidNameException("Cannot rename empty name");
        }
        if (!getInternalName(newname).equals(getInternalName(oldname))) {
            throw new InvalidNameException("Cannot rename across contexts");
        }
        HierMemDirCtx ctx =
            (HierMemDirCtx) doLookup(getInternalName(newname), false);
        ctx.doRename(getLeafName(oldname), getLeafName(newname));
    }
    protected void doRename(Name oldname, Name newname) throws NamingException {
        if (readOnlyEx != null) {
            throw (NamingException) readOnlyEx.fillInStackTrace();
        }
        oldname = canonizeName(oldname);
        newname = canonizeName(newname);
        if (bindings.get(newname) != null) {
            throw new NameAlreadyBoundException(newname.toString());
        }
        Object oldBinding = bindings.remove(oldname);
        if (oldBinding == null) {
            throw new NameNotFoundException(oldname.toString());
        }
        bindings.put(newname, oldBinding);
    }
    public NamingEnumeration list(String name) throws NamingException {
        return list(myParser.parse(name));
    }
    public NamingEnumeration list(Name name) throws NamingException {
        HierMemDirCtx ctx = (HierMemDirCtx) doLookup(name, false);
        return ctx.doList();
    }
    protected NamingEnumeration doList () throws NamingException {
        return new FlatNames(bindings.keys());
    }
    public NamingEnumeration listBindings(String name) throws NamingException {
        return listBindings(myParser.parse(name));
    }
    public NamingEnumeration listBindings(Name name) throws NamingException {
        HierMemDirCtx ctx = (HierMemDirCtx)doLookup(name, false);
        return ctx.doListBindings(alwaysUseFactory);
    }
    protected NamingEnumeration doListBindings(boolean useFactory)
        throws NamingException {
        return new FlatBindings(bindings, myEnv, useFactory);
    }
    public void destroySubcontext(String name) throws NamingException {
        destroySubcontext(myParser.parse(name));
    }
    public void destroySubcontext(Name name) throws NamingException {
        HierMemDirCtx ctx =
            (HierMemDirCtx) doLookup(getInternalName(name), false);
        ctx.doDestroySubcontext(getLeafName(name));
    }
    protected void doDestroySubcontext(Name name) throws NamingException {
        if (readOnlyEx != null) {
            throw (NamingException) readOnlyEx.fillInStackTrace();
        }
        name = canonizeName(name);
        bindings.remove(name);
    }
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(myParser.parse(name));
    }
    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name, null);
    }
    public DirContext createSubcontext(String name, Attributes attrs)
            throws NamingException {
        return createSubcontext(myParser.parse(name), attrs);
    }
    public DirContext createSubcontext(Name name, Attributes attrs)
            throws NamingException {
        HierMemDirCtx ctx =
            (HierMemDirCtx) doLookup(getInternalName(name), false);
        return ctx.doCreateSubcontext(getLeafName(name), attrs);
    }
    protected DirContext doCreateSubcontext(Name name, Attributes attrs)
        throws NamingException {
        if (readOnlyEx != null) {
            throw (NamingException) readOnlyEx.fillInStackTrace();
        }
        name = canonizeName(name);
        if (bindings.get(name) != null) {
            throw new NameAlreadyBoundException(name.toString());
        }
        HierMemDirCtx newCtx = createNewCtx();
        bindings.put(name, newCtx);
        if(attrs != null) {
            newCtx.modifyAttributes("", ADD_ATTRIBUTE, attrs);
        }
        return newCtx;
    }
    public Object lookupLink(String name) throws NamingException {
        return lookupLink(myParser.parse(name));
    }
    public Object lookupLink(Name name) throws NamingException {
        return lookup(name);
    }
    public NameParser getNameParser(String name) throws NamingException {
        return myParser;
    }
    public NameParser getNameParser(Name name) throws NamingException {
        return myParser;
    }
    public String composeName(String name, String prefix)
            throws NamingException {
        Name result = composeName(new CompositeName(name),
                                  new CompositeName(prefix));
        return result.toString();
    }
    public Name composeName(Name name, Name prefix)
            throws NamingException {
        name = canonizeName(name);
        prefix = canonizeName(prefix);
        Name result = (Name)(prefix.clone());
        result.addAll(name);
        return result;
    }
    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {
        myEnv = (myEnv == null) ?
            new Hashtable(11, 0.75f) : (Hashtable)myEnv.clone();
        return myEnv.put(propName, propVal);
    }
    public Object removeFromEnvironment(String propName)
            throws NamingException {
        if (myEnv == null)
            return null;
        myEnv = (Hashtable)myEnv.clone();
        return myEnv.remove(propName);
    }
    public Hashtable getEnvironment() throws NamingException {
        if (myEnv == null) {
            return new Hashtable(5, 0.75f);
        } else {
            return (Hashtable)myEnv.clone();
        }
    }
    public Attributes getAttributes(String name)
       throws NamingException {
       return getAttributes(myParser.parse(name));
    }
    public Attributes getAttributes(Name name)
        throws NamingException {
        HierMemDirCtx ctx = (HierMemDirCtx) doLookup(name, false);
        return ctx.doGetAttributes();
    }
    protected Attributes doGetAttributes() throws NamingException {
        return (Attributes)attrs.clone();
    }
    public Attributes getAttributes(String name, String[] attrIds)
        throws NamingException {
        return getAttributes(myParser.parse(name), attrIds);
    }
    public Attributes getAttributes(Name name, String[] attrIds)
        throws NamingException {
        HierMemDirCtx ctx = (HierMemDirCtx) doLookup(name, false);
        return ctx.doGetAttributes(attrIds);
    }
    protected Attributes doGetAttributes(String[] attrIds)
        throws NamingException {
        if (attrIds == null) {
            return doGetAttributes();
        }
        Attributes attrs = new BasicAttributes(ignoreCase);
        Attribute attr = null;
            for(int i=0; i<attrIds.length; i++) {
                attr = this.attrs.get(attrIds[i]);
                if (attr != null) {
                    attrs.put(attr);
                }
            }
        return attrs;
    }
    public void modifyAttributes(String name, int mod_op, Attributes attrs)
        throws NamingException   {
        modifyAttributes(myParser.parse(name), mod_op, attrs);
    }
    public void modifyAttributes(Name name, int mod_op, Attributes attrs)
        throws NamingException {
        if (attrs == null || attrs.size() == 0) {
            throw new IllegalArgumentException(
                "Cannot modify without an attribute");
        }
        NamingEnumeration attrEnum = attrs.getAll();
        ModificationItem[] mods = new ModificationItem[attrs.size()];
        for (int i = 0; i < mods.length && attrEnum.hasMoreElements(); i++) {
            mods[i] = new ModificationItem(mod_op, (Attribute)attrEnum.next());
        }
        modifyAttributes(name, mods);
    }
    public void modifyAttributes(String name, ModificationItem[] mods)
        throws NamingException   {
        modifyAttributes(myParser.parse(name), mods);
    }
    public void modifyAttributes(Name name, ModificationItem[] mods)
        throws NamingException {
        HierMemDirCtx ctx = (HierMemDirCtx) doLookup(name, false);
        ctx.doModifyAttributes(mods);
    }
    protected void doModifyAttributes(ModificationItem[] mods)
        throws NamingException {
        if (readOnlyEx != null) {
            throw (NamingException) readOnlyEx.fillInStackTrace();
        }
        applyMods(mods, attrs);
    }
    protected static Attributes applyMods(ModificationItem[] mods,
        Attributes orig) throws NamingException {
        ModificationItem mod;
        Attribute existingAttr, modAttr;
        NamingEnumeration modVals;
        for (int i = 0; i < mods.length; i++) {
            mod = mods[i];
            modAttr = mod.getAttribute();
            switch(mod.getModificationOp()) {
            case ADD_ATTRIBUTE:
                if (debug) {
                    System.out.println("HierMemDSCtx: adding " +
                                       mod.getAttribute().toString());
                }
                existingAttr = orig.get(modAttr.getID());
                if (existingAttr == null) {
                    orig.put((Attribute)modAttr.clone());
                } else {
                    modVals = modAttr.getAll();
                    while (modVals.hasMore()) {
                        existingAttr.add(modVals.next());
                    }
                }
                break;
            case REPLACE_ATTRIBUTE:
                if (modAttr.size() == 0) {
                    orig.remove(modAttr.getID());
                } else {
                    orig.put((Attribute)modAttr.clone());
                }
                break;
            case REMOVE_ATTRIBUTE:
                existingAttr = orig.get(modAttr.getID());
                if (existingAttr != null) {
                    if (modAttr.size() == 0) {
                        orig.remove(modAttr.getID());
                    } else {
                        modVals = modAttr.getAll();
                        while (modVals.hasMore()) {
                            existingAttr.remove(modVals.next());
                        }
                        if (existingAttr.size() == 0) {
                            orig.remove(modAttr.getID());
                        }
                    }
                }
                break;
            default:
                throw new AttributeModificationException("Unknown mod_op");
            }
        }
        return orig;
    }
    public NamingEnumeration search(String name,
                                    Attributes matchingAttributes)
        throws NamingException {
        return search(name, matchingAttributes, null);
    }
    public NamingEnumeration search(Name name,
                                    Attributes matchingAttributes)
        throws NamingException {
            return search(name, matchingAttributes, null);
    }
     public NamingEnumeration search(String name,
                                    Attributes matchingAttributes,
                                    String[] attributesToReturn)
        throws NamingException {
        return search(myParser.parse(name), matchingAttributes,
            attributesToReturn);
    }
     public NamingEnumeration search(Name name,
                                    Attributes matchingAttributes,
                                    String[] attributesToReturn)
         throws NamingException {
        HierMemDirCtx target = (HierMemDirCtx) doLookup(name, false);
        SearchControls cons = new SearchControls();
        cons.setReturningAttributes(attributesToReturn);
        return new LazySearchEnumerationImpl(
            target.doListBindings(false),
            new ContainmentFilter(matchingAttributes),
            cons, this, myEnv,
            false); 
    }
    public NamingEnumeration search(Name name,
                                    String filter,
                                    SearchControls cons)
        throws NamingException {
        DirContext target = (DirContext) doLookup(name, false);
        SearchFilter stringfilter = new SearchFilter(filter);
        return new LazySearchEnumerationImpl(
            new HierContextEnumerator(target,
                (cons != null) ? cons.getSearchScope() :
                SearchControls.ONELEVEL_SCOPE),
            stringfilter,
            cons, this, myEnv, alwaysUseFactory);
    }
     public NamingEnumeration search(Name name,
                                    String filterExpr,
                                    Object[] filterArgs,
                                    SearchControls cons)
            throws NamingException {
        String strfilter = SearchFilter.format(filterExpr, filterArgs);
        return search(name, strfilter, cons);
    }
    public NamingEnumeration search(String name,
                                    String filter,
                                    SearchControls cons)
        throws NamingException {
        return search(myParser.parse(name), filter, cons);
    }
    public NamingEnumeration search(String name,
                                    String filterExpr,
                                    Object[] filterArgs,
                                    SearchControls cons)
            throws NamingException {
        return search(myParser.parse(name), filterExpr, filterArgs, cons);
    }
    protected HierMemDirCtx createNewCtx() throws NamingException {
        return new HierMemDirCtx(myEnv, ignoreCase);
    }
    protected Name canonizeName(Name name) throws NamingException {
        Name canonicalName = name;
        if(!(name instanceof HierarchicalName)) {
            canonicalName = new HierarchicalName();
            int n = name.size();
            for(int i = 0; i < n; i++) {
                canonicalName.add(i, name.get(i));
            }
        }
        return canonicalName;
    }
     protected Name getInternalName(Name name) throws NamingException {
         return (name.getPrefix(name.size() - 1));
     }
     protected Name getLeafName(Name name) throws NamingException {
         return (name.getSuffix(name.size() - 1));
     }
     public DirContext getSchema(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }
     public DirContext getSchema(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }
     public DirContext getSchemaClassDefinition(String name)
        throws NamingException {
        throw new OperationNotSupportedException();
    }
    public DirContext getSchemaClassDefinition(Name name)
            throws NamingException {
        throw new OperationNotSupportedException();
    }
    public void setReadOnly(NamingException e) {
        readOnlyEx = e;
    }
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
    public void setNameParser(NameParser parser) {
        myParser = parser;
    }
    private class FlatNames implements NamingEnumeration {
        Enumeration names;
        FlatNames (Enumeration names) {
            this.names = names;
        }
        public boolean hasMoreElements() {
            try {
                return hasMore();
            } catch (NamingException e) {
                return false;
            }
        }
        public boolean hasMore() throws NamingException {
            return names.hasMoreElements();
        }
        public Object nextElement() {
            try {
                return next();
            } catch (NamingException e) {
                throw new NoSuchElementException(e.toString());
            }
        }
        public Object next() throws NamingException {
            Name name = (Name)names.nextElement();
            String className = bindings.get(name).getClass().getName();
            return new NameClassPair(name.toString(), className);
        }
        public void close() {
            names = null;
        }
    }
    private final class FlatBindings extends FlatNames {
        private Hashtable bds;
        private Hashtable env;
        private boolean useFactory;
        FlatBindings(Hashtable bindings, Hashtable env, boolean useFactory) {
            super(bindings.keys());
            this.env = env;
            this.bds = bindings;
            this.useFactory = useFactory;
        }
        public Object next() throws NamingException {
            Name name = (Name)names.nextElement();
            HierMemDirCtx obj = (HierMemDirCtx)bds.get(name);
            Object answer = obj;
            if (useFactory) {
                Attributes attrs = obj.getAttributes(""); 
                try {
                    answer = DirectoryManager.getObjectInstance(obj,
                        name, HierMemDirCtx.this, env, attrs);
                } catch (NamingException e) {
                    throw e;
                } catch (Exception e) {
                    NamingException e2 = new NamingException(
                        "Problem calling getObjectInstance");
                    e2.setRootCause(e);
                    throw e2;
                }
            }
            return new Binding(name.toString(), answer);
        }
    }
    public class HierContextEnumerator extends ContextEnumerator {
        public HierContextEnumerator(Context context, int scope)
            throws NamingException {
                super(context, scope);
        }
        protected HierContextEnumerator(Context context, int scope,
            String contextName, boolean returnSelf) throws NamingException {
            super(context, scope, contextName, returnSelf);
        }
        protected NamingEnumeration getImmediateChildren(Context ctx)
            throws NamingException {
                return ((HierMemDirCtx)ctx).doListBindings(false);
        }
        protected ContextEnumerator newEnumerator(Context ctx, int scope,
            String contextName, boolean returnSelf) throws NamingException {
                return new HierContextEnumerator(ctx, scope, contextName,
                    returnSelf);
        }
    }
}
final class HierarchicalName extends CompoundName {
    private int hashValue = -1;
    HierarchicalName() {
        super(new Enumeration() {
            public boolean hasMoreElements() {return false;}
            public Object nextElement() {throw new NoSuchElementException();}
        },
            HierarchicalNameParser.mySyntax);
    }
    HierarchicalName(Enumeration comps, Properties syntax) {
        super(comps, syntax);
    }
    HierarchicalName(String n, Properties syntax) throws InvalidNameException {
        super(n, syntax);
    }
    public int hashCode() {
        if (hashValue == -1) {
            String name = toString().toUpperCase();
            int len = name.length();
            int off = 0;
            char val[] = new char[len];
            name.getChars(0, len, val, 0);
            for (int i = len; i > 0; i--) {
                hashValue = (hashValue * 37) + val[off++];
            }
        }
        return hashValue;
    }
    public Name getPrefix(int posn) {
        Enumeration comps = super.getPrefix(posn).getAll();
        return (new HierarchicalName(comps, mySyntax));
    }
    public Name getSuffix(int posn) {
        Enumeration comps = super.getSuffix(posn).getAll();
        return (new HierarchicalName(comps, mySyntax));
    }
    public Object clone() {
        return (new HierarchicalName(getAll(), mySyntax));
    }
    private static final long serialVersionUID = -6717336834584573168L;
}
final class HierarchicalNameParser implements NameParser {
    static final Properties mySyntax = new Properties();
    static {
        mySyntax.put("jndi.syntax.direction", "left_to_right");
        mySyntax.put("jndi.syntax.separator", "/");
        mySyntax.put("jndi.syntax.ignorecase", "true");
        mySyntax.put("jndi.syntax.escape", "\\");
        mySyntax.put("jndi.syntax.beginquote", "\"");
        mySyntax.put("jndi.syntax.trimblanks", "false");
    };
    public Name parse(String name) throws NamingException {
        return new HierarchicalName(name, mySyntax);
    }
}
