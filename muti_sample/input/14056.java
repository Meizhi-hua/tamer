final public class ldapURLContext
        extends com.sun.jndi.toolkit.url.GenericURLDirContext {
    ldapURLContext(Hashtable env) {
        super(env);
    }
    protected ResolveResult getRootURLContext(String name, Hashtable env)
    throws NamingException {
        return ldapURLContextFactory.getUsingURLIgnoreRootDN(name, env);
    }
    protected Name getURLSuffix(String prefix, String url)
        throws NamingException {
        LdapURL ldapUrl = new LdapURL(url);
        String dn = (ldapUrl.getDN() != null? ldapUrl.getDN() : "");
        CompositeName remaining = new CompositeName();
        if (!"".equals(dn)) {
            remaining.add(dn);
        }
        return remaining;
    }
    public Object lookup(String name) throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            return super.lookup(name);
        }
    }
    public Object lookup(Name name) throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.lookup(name);
        }
    }
    public void bind(String name, Object obj) throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            super.bind(name, obj);
        }
    }
    public void bind(Name name, Object obj) throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            super.bind(name, obj);
        }
    }
    public void rebind(String name, Object obj) throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            super.rebind(name, obj);
        }
    }
    public void rebind(Name name, Object obj) throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            super.rebind(name, obj);
        }
    }
    public void unbind(String name) throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            super.unbind(name);
        }
    }
    public void unbind(Name name) throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            super.unbind(name);
        }
    }
    public void rename(String oldName, String newName) throws NamingException {
        if (LdapURL.hasQueryComponents(oldName)) {
            throw new InvalidNameException(oldName);
        } else if (LdapURL.hasQueryComponents(newName)) {
            throw new InvalidNameException(newName);
        } else {
            super.rename(oldName, newName);
        }
    }
    public void rename(Name oldName, Name newName) throws NamingException {
        if (LdapURL.hasQueryComponents(oldName.get(0))) {
            throw new InvalidNameException(oldName.toString());
        } else if (LdapURL.hasQueryComponents(newName.get(0))) {
            throw new InvalidNameException(newName.toString());
        } else {
            super.rename(oldName, newName);
        }
    }
    public NamingEnumeration list(String name)  throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            return super.list(name);
        }
    }
    public NamingEnumeration list(Name name) throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.list(name);
        }
    }
    public NamingEnumeration listBindings(String name) throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            return super.listBindings(name);
        }
    }
    public NamingEnumeration listBindings(Name name) throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.listBindings(name);
        }
    }
    public void destroySubcontext(String name) throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            super.destroySubcontext(name);
        }
    }
    public void destroySubcontext(Name name) throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            super.destroySubcontext(name);
        }
    }
    public Context createSubcontext(String name) throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            return super.createSubcontext(name);
        }
    }
    public Context createSubcontext(Name name) throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.createSubcontext(name);
        }
    }
    public Object lookupLink(String name) throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            return super.lookupLink(name);
        }
    }
    public Object lookupLink(Name name) throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.lookupLink(name);
        }
    }
    public NameParser getNameParser(String name) throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            return super.getNameParser(name);
        }
    }
    public NameParser getNameParser(Name name) throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.getNameParser(name);
        }
    }
    public String composeName(String name, String prefix)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else if (LdapURL.hasQueryComponents(prefix)) {
            throw new InvalidNameException(prefix);
        } else {
            return super.composeName(name, prefix);
        }
    }
    public Name composeName(Name name, Name prefix) throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else if (LdapURL.hasQueryComponents(prefix.get(0))) {
            throw new InvalidNameException(prefix.toString());
        } else {
            return super.composeName(name, prefix);
        }
    }
    public Attributes getAttributes(String name) throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            return super.getAttributes(name);
        }
    }
    public Attributes getAttributes(Name name) throws NamingException  {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.getAttributes(name);
        }
    }
    public Attributes getAttributes(String name, String[] attrIds)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            return super.getAttributes(name, attrIds);
        }
    }
    public Attributes getAttributes(Name name, String[] attrIds)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.getAttributes(name, attrIds);
        }
    }
    public void modifyAttributes(String name, int mod_op, Attributes attrs)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            super.modifyAttributes(name, mod_op, attrs);
        }
    }
    public void modifyAttributes(Name name, int mod_op, Attributes attrs)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            super.modifyAttributes(name, mod_op, attrs);
        }
    }
    public void modifyAttributes(String name, ModificationItem[] mods)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            super.modifyAttributes(name, mods);
        }
    }
    public void modifyAttributes(Name name, ModificationItem[] mods)
        throws NamingException  {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            super.modifyAttributes(name, mods);
        }
    }
    public void bind(String name, Object obj, Attributes attrs)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            super.bind(name, obj, attrs);
        }
    }
    public void bind(Name name, Object obj, Attributes attrs)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            super.bind(name, obj, attrs);
        }
    }
    public void rebind(String name, Object obj, Attributes attrs)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            super.rebind(name, obj, attrs);
        }
    }
    public void rebind(Name name, Object obj, Attributes attrs)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            super.rebind(name, obj, attrs);
        }
    }
    public DirContext createSubcontext(String name, Attributes attrs)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            return super.createSubcontext(name, attrs);
        }
    }
    public DirContext createSubcontext(Name name, Attributes attrs)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.createSubcontext(name, attrs);
        }
    }
    public DirContext getSchema(String name) throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            return super.getSchema(name);
        }
    }
    public DirContext getSchema(Name name) throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.getSchema(name);
        }
    }
    public DirContext getSchemaClassDefinition(String name)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            throw new InvalidNameException(name);
        } else {
            return super.getSchemaClassDefinition(name);
        }
    }
    public DirContext getSchemaClassDefinition(Name name)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.getSchemaClassDefinition(name);
        }
    }
    public NamingEnumeration search(String name,
        Attributes matchingAttributes)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            return searchUsingURL(name);
        } else {
            return super.search(name, matchingAttributes);
        }
    }
    public NamingEnumeration search(Name name,
        Attributes matchingAttributes)
        throws NamingException {
        if (name.size() == 1) {
            return search(name.get(0), matchingAttributes);
        } else if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.search(name, matchingAttributes);
        }
    }
    public NamingEnumeration search(String name,
        Attributes matchingAttributes,
        String[] attributesToReturn)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            return searchUsingURL(name);
        } else {
            return super.search(name, matchingAttributes, attributesToReturn);
        }
    }
    public NamingEnumeration search(Name name,
        Attributes matchingAttributes,
        String[] attributesToReturn)
        throws NamingException {
        if (name.size() == 1) {
            return search(name.get(0), matchingAttributes, attributesToReturn);
        } else if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.search(name, matchingAttributes, attributesToReturn);
        }
    }
    public NamingEnumeration search(String name,
        String filter,
        SearchControls cons)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            return searchUsingURL(name);
        } else {
            return super.search(name, filter, cons);
        }
    }
    public NamingEnumeration search(Name name,
        String filter,
        SearchControls cons)
        throws NamingException {
        if (name.size() == 1) {
            return search(name.get(0), filter, cons);
        } else if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.search(name, filter, cons);
        }
    }
    public NamingEnumeration search(String name,
        String filterExpr,
        Object[] filterArgs,
        SearchControls cons)
        throws NamingException {
        if (LdapURL.hasQueryComponents(name)) {
            return searchUsingURL(name);
        } else {
            return super.search(name, filterExpr, filterArgs, cons);
        }
    }
    public NamingEnumeration search(Name name,
        String filterExpr,
        Object[] filterArgs,
        SearchControls cons)
        throws NamingException {
        if (name.size() == 1) {
            return search(name.get(0), filterExpr, filterArgs, cons);
        } else if (LdapURL.hasQueryComponents(name.get(0))) {
            throw new InvalidNameException(name.toString());
        } else {
            return super.search(name, filterExpr, filterArgs, cons);
        }
    }
    private NamingEnumeration searchUsingURL(String name)
        throws NamingException {
        LdapURL url = new LdapURL(name);
        ResolveResult res = getRootURLContext(name, myEnv);
        DirContext ctx = (DirContext)res.getResolvedObj();
        try {
            return ctx.search(res.getRemainingName(),
                              setFilterUsingURL(url),
                              setSearchControlsUsingURL(url));
        } finally {
            ctx.close();
        }
    }
    private static String setFilterUsingURL(LdapURL url) {
        String filter = url.getFilter();
        if (filter == null) {
            filter = "(objectClass=*)"; 
        }
        return filter;
    }
    private static SearchControls setSearchControlsUsingURL(LdapURL url) {
        SearchControls cons = new SearchControls();
        String scope = url.getScope();
        String attributes = url.getAttributes();
        if (scope == null) {
            cons.setSearchScope(SearchControls.OBJECT_SCOPE); 
        } else {
            if (scope.equals("sub")) {
                cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
            } else if (scope.equals("one")) {
                cons.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            } else if (scope.equals("base")) {
                cons.setSearchScope(SearchControls.OBJECT_SCOPE);
            }
        }
        if (attributes == null) {
            cons.setReturningAttributes(null); 
        } else {
            StringTokenizer tokens = new StringTokenizer(attributes, ",");
            int count = tokens.countTokens();
            String[] attrs = new String[count];
            for (int i = 0; i < count; i ++) {
                attrs[i] = tokens.nextToken();
            }
            cons.setReturningAttributes(attrs);
        }
        return cons;
    }
}
