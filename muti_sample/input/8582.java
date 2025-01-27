public abstract class PartialCompositeContext implements Context, Resolver {
    protected static final int _PARTIAL = 1;
    protected static final int _COMPONENT = 2;
    protected static final int _ATOMIC = 3;
    protected int _contextType = _PARTIAL;
    static final CompositeName _EMPTY_NAME = new CompositeName();
    static CompositeName _NNS_NAME;
    static {
        try {
            _NNS_NAME = new CompositeName("/");
        } catch (InvalidNameException e) {
        }
    }
    protected PartialCompositeContext() {
    }
    protected abstract ResolveResult p_resolveToClass(Name name,
        Class contextType, Continuation cont) throws NamingException;
    protected abstract Object p_lookup(Name name, Continuation cont)
        throws NamingException;
    protected abstract Object p_lookupLink(Name name, Continuation cont)
        throws NamingException;
    protected abstract NamingEnumeration p_list(Name name,
        Continuation cont) throws NamingException;
    protected abstract NamingEnumeration p_listBindings(Name name,
        Continuation cont) throws NamingException;
    protected abstract void p_bind(Name name, Object obj, Continuation cont)
        throws NamingException;
    protected abstract void p_rebind(Name name, Object obj, Continuation cont)
        throws NamingException;
    protected abstract void p_unbind(Name name, Continuation cont)
        throws NamingException;
    protected abstract void p_destroySubcontext(Name name, Continuation cont)
        throws NamingException;
    protected abstract Context p_createSubcontext(Name name, Continuation cont)
        throws NamingException;
    protected abstract void p_rename(Name oldname, Name newname,
                                     Continuation cont)
        throws NamingException;
    protected abstract NameParser p_getNameParser(Name name, Continuation cont)
        throws NamingException;
    protected Hashtable p_getEnvironment() throws NamingException {
        return getEnvironment();
    }
    public ResolveResult resolveToClass(String name,
                                        Class<? extends Context> contextType)
        throws NamingException
    {
        return resolveToClass(new CompositeName(name), contextType);
    }
    public ResolveResult resolveToClass(Name name,
                                        Class<? extends Context> contextType)
        throws NamingException
    {
        PartialCompositeContext ctx = this;
        Hashtable env = p_getEnvironment();
        Continuation cont = new Continuation(name, env);
        ResolveResult answer;
        Name nm = name;
        try {
            answer = ctx.p_resolveToClass(nm, contextType, cont);
            while (cont.isContinue()) {
                nm = cont.getRemainingName();
                ctx = getPCContext(cont);
                answer = ctx.p_resolveToClass(nm, contextType, cont);
            }
        } catch (CannotProceedException e) {
            Context cctx = NamingManager.getContinuationContext(e);
            if (!(cctx instanceof Resolver)) {
                throw e;
            }
            answer = ((Resolver)cctx).resolveToClass(e.getRemainingName(),
                                                     contextType);
        }
        return answer;
    }
    public Object lookup(String name) throws NamingException {
        return lookup(new CompositeName(name));
    }
    public Object lookup(Name name) throws NamingException {
        PartialCompositeContext ctx = this;
        Hashtable env = p_getEnvironment();
        Continuation cont = new Continuation(name, env);
        Object answer;
        Name nm = name;
        try {
            answer = ctx.p_lookup(nm, cont);
            while (cont.isContinue()) {
                nm = cont.getRemainingName();
                ctx = getPCContext(cont);
                answer = ctx.p_lookup(nm, cont);
            }
        } catch (CannotProceedException e) {
            Context cctx = NamingManager.getContinuationContext(e);
            answer = cctx.lookup(e.getRemainingName());
        }
        return answer;
    }
    public void bind(String name, Object newObj) throws NamingException {
        bind(new CompositeName(name), newObj);
    }
    public void bind(Name name, Object newObj) throws NamingException {
        PartialCompositeContext ctx = this;
        Name nm = name;
        Hashtable env = p_getEnvironment();
        Continuation cont = new Continuation(name, env);
        try {
            ctx.p_bind(nm, newObj, cont);
            while (cont.isContinue()) {
                nm = cont.getRemainingName();
                ctx = getPCContext(cont);
                ctx.p_bind(nm, newObj, cont);
            }
        } catch (CannotProceedException e) {
            Context cctx = NamingManager.getContinuationContext(e);
            cctx.bind(e.getRemainingName(), newObj);
        }
    }
    public void rebind(String name, Object newObj) throws NamingException {
        rebind(new CompositeName(name), newObj);
    }
    public void rebind(Name name, Object newObj) throws NamingException {
        PartialCompositeContext ctx = this;
        Name nm = name;
        Hashtable env = p_getEnvironment();
        Continuation cont = new Continuation(name, env);
        try {
            ctx.p_rebind(nm, newObj, cont);
            while (cont.isContinue()) {
                nm = cont.getRemainingName();
                ctx = getPCContext(cont);
                ctx.p_rebind(nm, newObj, cont);
            }
        } catch (CannotProceedException e) {
            Context cctx = NamingManager.getContinuationContext(e);
            cctx.rebind(e.getRemainingName(), newObj);
        }
    }
    public void unbind(String name) throws NamingException {
        unbind(new CompositeName(name));
    }
    public void unbind(Name name) throws NamingException {
        PartialCompositeContext ctx = this;
        Name nm = name;
        Hashtable env = p_getEnvironment();
        Continuation cont = new Continuation(name, env);
        try {
            ctx.p_unbind(nm, cont);
            while (cont.isContinue()) {
                nm = cont.getRemainingName();
                ctx = getPCContext(cont);
                ctx.p_unbind(nm, cont);
            }
        } catch (CannotProceedException e) {
            Context cctx = NamingManager.getContinuationContext(e);
            cctx.unbind(e.getRemainingName());
        }
    }
    public void rename(String oldName, String newName) throws NamingException {
        rename(new CompositeName(oldName), new CompositeName(newName));
    }
    public void rename(Name oldName, Name newName)
        throws NamingException
    {
        PartialCompositeContext ctx = this;
        Name nm = oldName;
        Hashtable env = p_getEnvironment();
        Continuation cont = new Continuation(oldName, env);
        try {
            ctx.p_rename(nm, newName, cont);
            while (cont.isContinue()) {
                nm = cont.getRemainingName();
                ctx = getPCContext(cont);
                ctx.p_rename(nm, newName, cont);
            }
        } catch (CannotProceedException e) {
            Context cctx = NamingManager.getContinuationContext(e);
            if (e.getRemainingNewName() != null) {
                newName = e.getRemainingNewName();
            }
            cctx.rename(e.getRemainingName(), newName);
        }
    }
    public NamingEnumeration<NameClassPair> list(String name)
        throws NamingException
    {
        return list(new CompositeName(name));
    }
    public NamingEnumeration<NameClassPair> list(Name name)
        throws NamingException
    {
        PartialCompositeContext ctx = this;
        Name nm = name;
        NamingEnumeration answer;
        Hashtable env = p_getEnvironment();
        Continuation cont = new Continuation(name, env);
        try {
            answer = ctx.p_list(nm, cont);
            while (cont.isContinue()) {
                nm = cont.getRemainingName();
                ctx = getPCContext(cont);
                answer = ctx.p_list(nm, cont);
            }
        } catch (CannotProceedException e) {
            Context cctx = NamingManager.getContinuationContext(e);
            answer = cctx.list(e.getRemainingName());
        }
        return answer;
    }
    public NamingEnumeration<Binding> listBindings(String name)
        throws NamingException
    {
        return listBindings(new CompositeName(name));
    }
    public NamingEnumeration<Binding> listBindings(Name name)
        throws NamingException
    {
        PartialCompositeContext ctx = this;
        Name nm = name;
        NamingEnumeration answer;
        Hashtable env = p_getEnvironment();
        Continuation cont = new Continuation(name, env);
        try {
            answer = ctx.p_listBindings(nm, cont);
            while (cont.isContinue()) {
                nm = cont.getRemainingName();
                ctx = getPCContext(cont);
                answer = ctx.p_listBindings(nm, cont);
            }
        } catch (CannotProceedException e) {
            Context cctx = NamingManager.getContinuationContext(e);
            answer = cctx.listBindings(e.getRemainingName());
        }
        return answer;
    }
    public void destroySubcontext(String name) throws NamingException {
        destroySubcontext(new CompositeName(name));
    }
    public void destroySubcontext(Name name) throws NamingException {
        PartialCompositeContext ctx = this;
        Name nm = name;
        Hashtable env = p_getEnvironment();
        Continuation cont = new Continuation(name, env);
        try {
            ctx.p_destroySubcontext(nm, cont);
            while (cont.isContinue()) {
                nm = cont.getRemainingName();
                ctx = getPCContext(cont);
                ctx.p_destroySubcontext(nm, cont);
            }
        } catch (CannotProceedException e) {
            Context cctx = NamingManager.getContinuationContext(e);
            cctx.destroySubcontext(e.getRemainingName());
        }
    }
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(new CompositeName(name));
    }
    public Context createSubcontext(Name name) throws NamingException {
        PartialCompositeContext ctx = this;
        Name nm = name;
        Context answer;
        Hashtable env = p_getEnvironment();
        Continuation cont = new Continuation(name, env);
        try {
            answer = ctx.p_createSubcontext(nm, cont);
            while (cont.isContinue()) {
                nm = cont.getRemainingName();
                ctx = getPCContext(cont);
                answer = ctx.p_createSubcontext(nm, cont);
            }
        } catch (CannotProceedException e) {
            Context cctx = NamingManager.getContinuationContext(e);
            answer = cctx.createSubcontext(e.getRemainingName());
        }
        return answer;
    }
    public Object lookupLink(String name) throws NamingException {
        return lookupLink(new CompositeName(name));
    }
    public Object lookupLink(Name name) throws NamingException {
        PartialCompositeContext ctx = this;
        Hashtable env = p_getEnvironment();
        Continuation cont = new Continuation(name, env);
        Object answer;
        Name nm = name;
        try {
            answer = ctx.p_lookupLink(nm, cont);
            while (cont.isContinue()) {
                nm = cont.getRemainingName();
                ctx = getPCContext(cont);
                answer = ctx.p_lookupLink(nm, cont);
            }
        } catch (CannotProceedException e) {
            Context cctx = NamingManager.getContinuationContext(e);
            answer = cctx.lookupLink(e.getRemainingName());
        }
        return answer;
    }
    public NameParser getNameParser(String name) throws NamingException {
        return getNameParser(new CompositeName(name));
    }
    public NameParser getNameParser(Name name) throws NamingException {
        PartialCompositeContext ctx = this;
        Name nm = name;
        NameParser answer;
        Hashtable env = p_getEnvironment();
        Continuation cont = new Continuation(name, env);
        try {
            answer = ctx.p_getNameParser(nm, cont);
            while (cont.isContinue()) {
                nm = cont.getRemainingName();
                ctx = getPCContext(cont);
                answer = ctx.p_getNameParser(nm, cont);
            }
        } catch (CannotProceedException e) {
            Context cctx = NamingManager.getContinuationContext(e);
            answer = cctx.getNameParser(e.getRemainingName());
        }
        return answer;
    }
    public String composeName(String name, String prefix)
            throws NamingException {
        Name fullName = composeName(new CompositeName(name),
                                    new CompositeName(prefix));
        return fullName.toString();
    }
    public Name composeName(Name name, Name prefix) throws NamingException {
        Name res = (Name)prefix.clone();
        if (name == null) {
            return res;
        }
        res.addAll(name);
        String elide = (String)
            p_getEnvironment().get("java.naming.provider.compose.elideEmpty");
        if (elide == null || !elide.equalsIgnoreCase("true")) {
            return res;
        }
        int len = prefix.size();
        if (!allEmpty(prefix) && !allEmpty(name)) {
            if (res.get(len - 1).equals("")) {
                res.remove(len - 1);
            } else if (res.get(len).equals("")) {
                res.remove(len);
            }
        }
        return res;
    }
    protected static boolean allEmpty(Name name) {
        Enumeration<String> enum_ = name.getAll();
        while (enum_.hasMoreElements()) {
            if (!enum_.nextElement().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    protected static PartialCompositeContext getPCContext(Continuation cont)
            throws NamingException {
        Object obj = cont.getResolvedObj();
        PartialCompositeContext pctx = null;
        if (obj instanceof PartialCompositeContext) {
            return (PartialCompositeContext)obj;
        } else {
            throw cont.fillInException(new CannotProceedException());
        }
    }
};
