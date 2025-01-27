class ContinuationDirContext extends ContinuationContext implements DirContext {
    ContinuationDirContext(CannotProceedException cpe, Hashtable env) {
        super(cpe, env);
    }
    protected DirContextNamePair getTargetContext(Name name)
            throws NamingException {
        if (cpe.getResolvedObj() == null)
            throw (NamingException)cpe.fillInStackTrace();
        Context ctx = NamingManager.getContext(cpe.getResolvedObj(),
                                               cpe.getAltName(),
                                               cpe.getAltNameCtx(),
                                               env);
        if (ctx == null)
            throw (NamingException)cpe.fillInStackTrace();
        if (ctx instanceof DirContext)
            return new DirContextNamePair((DirContext)ctx, name);
        if (ctx instanceof Resolver) {
            Resolver res = (Resolver)ctx;
            ResolveResult rr = res.resolveToClass(name, DirContext.class);
            DirContext dctx = (DirContext)rr.getResolvedObj();
            return (new DirContextNamePair(dctx, rr.getRemainingName()));
        }
        Object ultimate = ctx.lookup(name);
        if (ultimate instanceof DirContext) {
            return (new DirContextNamePair((DirContext)ultimate,
                                          new CompositeName()));
        }
        throw (NamingException)cpe.fillInStackTrace();
    }
    protected DirContextStringPair getTargetContext(String name)
            throws NamingException {
        if (cpe.getResolvedObj() == null)
            throw (NamingException)cpe.fillInStackTrace();
        Context ctx = NamingManager.getContext(cpe.getResolvedObj(),
                                               cpe.getAltName(),
                                               cpe.getAltNameCtx(),
                                               env);
        if (ctx instanceof DirContext)
            return new DirContextStringPair((DirContext)ctx, name);
        if (ctx instanceof Resolver) {
            Resolver res = (Resolver)ctx;
            ResolveResult rr = res.resolveToClass(name, DirContext.class);
            DirContext dctx = (DirContext)rr.getResolvedObj();
            Name tmp = rr.getRemainingName();
            String remains = (tmp != null) ? tmp.toString() : "";
            return (new DirContextStringPair(dctx, remains));
        }
        Object ultimate = ctx.lookup(name);
        if (ultimate instanceof DirContext) {
            return (new DirContextStringPair((DirContext)ultimate, ""));
        }
        throw (NamingException)cpe.fillInStackTrace();
    }
    public Attributes getAttributes(String name) throws NamingException {
        DirContextStringPair res = getTargetContext(name);
        return res.getDirContext().getAttributes(res.getString());
    }
    public Attributes getAttributes(String name, String[] attrIds)
        throws NamingException {
            DirContextStringPair res = getTargetContext(name);
            return res.getDirContext().getAttributes(res.getString(), attrIds);
        }
    public Attributes getAttributes(Name name) throws NamingException {
        DirContextNamePair res = getTargetContext(name);
        return res.getDirContext().getAttributes(res.getName());
    }
    public Attributes getAttributes(Name name, String[] attrIds)
        throws NamingException {
            DirContextNamePair res = getTargetContext(name);
            return res.getDirContext().getAttributes(res.getName(), attrIds);
        }
    public void modifyAttributes(Name name, int mod_op, Attributes attrs)
        throws NamingException  {
            DirContextNamePair res = getTargetContext(name);
            res.getDirContext().modifyAttributes(res.getName(), mod_op, attrs);
        }
    public void modifyAttributes(String name, int mod_op, Attributes attrs)
        throws NamingException  {
            DirContextStringPair res = getTargetContext(name);
            res.getDirContext().modifyAttributes(res.getString(), mod_op, attrs);
        }
    public void modifyAttributes(Name name, ModificationItem[] mods)
        throws NamingException  {
            DirContextNamePair res = getTargetContext(name);
            res.getDirContext().modifyAttributes(res.getName(), mods);
        }
    public void modifyAttributes(String name, ModificationItem[] mods)
        throws NamingException  {
            DirContextStringPair res = getTargetContext(name);
            res.getDirContext().modifyAttributes(res.getString(), mods);
        }
    public void bind(Name name, Object obj, Attributes attrs)
        throws NamingException  {
            DirContextNamePair res = getTargetContext(name);
            res.getDirContext().bind(res.getName(), obj, attrs);
        }
    public void bind(String name, Object obj, Attributes attrs)
        throws NamingException  {
            DirContextStringPair res = getTargetContext(name);
            res.getDirContext().bind(res.getString(), obj, attrs);
        }
    public void rebind(Name name, Object obj, Attributes attrs)
                throws NamingException {
            DirContextNamePair res = getTargetContext(name);
            res.getDirContext().rebind(res.getName(), obj, attrs);
        }
    public void rebind(String name, Object obj, Attributes attrs)
                throws NamingException {
            DirContextStringPair res = getTargetContext(name);
            res.getDirContext().rebind(res.getString(), obj, attrs);
        }
    public DirContext createSubcontext(Name name, Attributes attrs)
                throws NamingException  {
            DirContextNamePair res = getTargetContext(name);
            return res.getDirContext().createSubcontext(res.getName(), attrs);
        }
    public DirContext createSubcontext(String name, Attributes attrs)
                throws NamingException  {
            DirContextStringPair res = getTargetContext(name);
            return
                res.getDirContext().createSubcontext(res.getString(), attrs);
        }
    public NamingEnumeration search(Name name,
                                    Attributes matchingAttributes,
                                    String[] attributesToReturn)
        throws NamingException  {
            DirContextNamePair res = getTargetContext(name);
            return res.getDirContext().search(res.getName(), matchingAttributes,
                                             attributesToReturn);
        }
    public NamingEnumeration search(String name,
                                    Attributes matchingAttributes,
                                    String[] attributesToReturn)
        throws NamingException  {
            DirContextStringPair res = getTargetContext(name);
            return res.getDirContext().search(res.getString(),
                                             matchingAttributes,
                                             attributesToReturn);
        }
    public NamingEnumeration search(Name name,
                                    Attributes matchingAttributes)
        throws NamingException  {
            DirContextNamePair res = getTargetContext(name);
            return res.getDirContext().search(res.getName(), matchingAttributes);
        }
    public NamingEnumeration search(String name,
                                    Attributes matchingAttributes)
        throws NamingException  {
            DirContextStringPair res = getTargetContext(name);
            return res.getDirContext().search(res.getString(),
                                             matchingAttributes);
        }
    public NamingEnumeration search(Name name,
                                    String filter,
                                    SearchControls cons)
        throws NamingException {
            DirContextNamePair res = getTargetContext(name);
            return res.getDirContext().search(res.getName(), filter, cons);
        }
    public NamingEnumeration search(String name,
                                    String filter,
                                    SearchControls cons)
        throws NamingException {
            DirContextStringPair res = getTargetContext(name);
            return res.getDirContext().search(res.getString(), filter, cons);
        }
    public NamingEnumeration search(Name name,
                                    String filterExpr,
                                    Object[] args,
                                    SearchControls cons)
        throws NamingException {
            DirContextNamePair res = getTargetContext(name);
            return res.getDirContext().search(res.getName(), filterExpr, args,
                                             cons);
        }
    public NamingEnumeration search(String name,
                                    String filterExpr,
                                    Object[] args,
                                    SearchControls cons)
        throws NamingException {
            DirContextStringPair res = getTargetContext(name);
            return res.getDirContext().search(res.getString(), filterExpr, args,
                                             cons);
        }
    public DirContext getSchema(String name) throws NamingException {
        DirContextStringPair res = getTargetContext(name);
        return res.getDirContext().getSchema(res.getString());
    }
    public DirContext getSchema(Name name) throws NamingException  {
        DirContextNamePair res = getTargetContext(name);
        return res.getDirContext().getSchema(res.getName());
    }
    public DirContext getSchemaClassDefinition(String name)
            throws NamingException  {
        DirContextStringPair res = getTargetContext(name);
        return res.getDirContext().getSchemaClassDefinition(res.getString());
    }
    public DirContext getSchemaClassDefinition(Name name)
            throws NamingException  {
        DirContextNamePair res = getTargetContext(name);
        return res.getDirContext().getSchemaClassDefinition(res.getName());
    }
}
class DirContextNamePair {
        DirContext ctx;
        Name name;
        DirContextNamePair(DirContext ctx, Name name) {
            this.ctx = ctx;
            this.name = name;
        }
        DirContext getDirContext() {
            return ctx;
        }
        Name getName() {
            return name;
        }
}
class DirContextStringPair {
        DirContext ctx;
        String str;
        DirContextStringPair(DirContext ctx, String str) {
            this.ctx = ctx;
            this.str = str;
        }
        DirContext getDirContext() {
            return ctx;
        }
        String getString() {
            return str;
        }
}
