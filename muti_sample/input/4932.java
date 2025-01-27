public abstract class AtomicDirContext extends ComponentDirContext {
    protected AtomicDirContext() {
        _contextType = _ATOMIC;
    }
    protected abstract Attributes a_getAttributes(String name, String[] attrIds,
                                                    Continuation cont)
        throws NamingException;
    protected abstract void a_modifyAttributes(String name, int mod_op,
                                               Attributes attrs,
                                               Continuation cont)
        throws NamingException;
    protected abstract void a_modifyAttributes(String name,
                                               ModificationItem[] mods,
                                               Continuation cont)
        throws NamingException;
    protected abstract void a_bind(String name, Object obj,
                                   Attributes attrs,
                                   Continuation cont)
        throws NamingException;
    protected abstract void a_rebind(String name, Object obj,
                                     Attributes attrs,
                                     Continuation cont)
        throws NamingException;
    protected abstract DirContext a_createSubcontext(String name,
                                                    Attributes attrs,
                                                    Continuation cont)
        throws NamingException;
    protected abstract NamingEnumeration a_search(Attributes matchingAttributes,
                                                  String[] attributesToReturn,
                                                  Continuation cont)
        throws NamingException;
    protected abstract NamingEnumeration a_search(String name,
                                                  String filterExpr,
                                                  Object[] filterArgs,
                                                  SearchControls cons, Continuation cont)
        throws NamingException;
    protected abstract NamingEnumeration a_search(String name,
                                                  String filter,
                                                  SearchControls cons, Continuation cont)
        throws NamingException;
    protected abstract DirContext a_getSchema(Continuation cont)
        throws NamingException;
    protected abstract DirContext a_getSchemaClassDefinition(Continuation cont)
        throws NamingException;
    protected Attributes a_getAttributes_nns(String name,
                                               String[] attrIds,
                                               Continuation cont)
        throws NamingException  {
            a_processJunction_nns(name, cont);
            return null;
        }
    protected void a_modifyAttributes_nns(String name, int mod_op,
                                          Attributes attrs,
                                          Continuation cont)
        throws NamingException {
            a_processJunction_nns(name, cont);
        }
    protected void a_modifyAttributes_nns(String name,
                                          ModificationItem[] mods,
                                          Continuation cont)
        throws NamingException {
            a_processJunction_nns(name, cont);
        }
    protected void a_bind_nns(String name, Object obj,
                              Attributes attrs,
                              Continuation cont)
        throws NamingException  {
            a_processJunction_nns(name, cont);
        }
    protected void a_rebind_nns(String name, Object obj,
                                Attributes attrs,
                                Continuation cont)
        throws NamingException  {
            a_processJunction_nns(name, cont);
        }
    protected DirContext a_createSubcontext_nns(String name,
                                               Attributes attrs,
                                               Continuation cont)
        throws NamingException  {
            a_processJunction_nns(name, cont);
            return null;
        }
    protected NamingEnumeration a_search_nns(Attributes matchingAttributes,
                                             String[] attributesToReturn,
                                             Continuation cont)
        throws NamingException {
            a_processJunction_nns(cont);
            return null;
        }
    protected NamingEnumeration a_search_nns(String name,
                                             String filterExpr,
                                             Object[] filterArgs,
                                             SearchControls cons,
                                             Continuation cont)
        throws NamingException {
            a_processJunction_nns(name, cont);
            return null;
        }
    protected NamingEnumeration a_search_nns(String name,
                                             String filter,
                                             SearchControls cons,
                                             Continuation cont)
        throws NamingException  {
            a_processJunction_nns(name, cont);
            return null;
        }
    protected DirContext a_getSchema_nns(Continuation cont) throws NamingException {
        a_processJunction_nns(cont);
        return null;
    }
    protected DirContext a_getSchemaDefinition_nns(Continuation cont)
        throws NamingException {
            a_processJunction_nns(cont);
            return null;
        }
    protected Attributes c_getAttributes(Name name, String[] attrIds,
                                           Continuation cont)
        throws NamingException  {
            if (resolve_to_penultimate_context(name, cont))
                return a_getAttributes(name.toString(), attrIds, cont);
            return null;
        }
    protected void c_modifyAttributes(Name name, int mod_op,
                                      Attributes attrs, Continuation cont)
        throws NamingException {
            if (resolve_to_penultimate_context(name, cont))
                a_modifyAttributes(name.toString(), mod_op, attrs, cont);
        }
    protected void c_modifyAttributes(Name name, ModificationItem[] mods,
                                      Continuation cont)
        throws NamingException {
            if (resolve_to_penultimate_context(name, cont))
                a_modifyAttributes(name.toString(), mods, cont);
        }
    protected void c_bind(Name name, Object obj,
                          Attributes attrs, Continuation cont)
        throws NamingException  {
            if (resolve_to_penultimate_context(name, cont))
                a_bind(name.toString(), obj, attrs, cont);
        }
    protected void c_rebind(Name name, Object obj,
                            Attributes attrs, Continuation cont)
        throws NamingException  {
            if (resolve_to_penultimate_context(name, cont))
                a_rebind(name.toString(), obj, attrs, cont);
        }
    protected DirContext c_createSubcontext(Name name,
                                           Attributes attrs,
                                           Continuation cont)
        throws NamingException  {
            if (resolve_to_penultimate_context(name, cont))
                return a_createSubcontext(name.toString(),
                                          attrs, cont);
            return null;
        }
    protected NamingEnumeration c_search(Name name,
                                         Attributes matchingAttributes,
                                         String[] attributesToReturn,
                                         Continuation cont)
        throws NamingException  {
            if (resolve_to_context(name, cont))
                return a_search(matchingAttributes, attributesToReturn, cont);
            return null;
        }
    protected NamingEnumeration c_search(Name name,
                                         String filter,
                                         SearchControls cons, Continuation cont)
        throws NamingException {
            if (resolve_to_penultimate_context(name, cont))
                return a_search(name.toString(), filter, cons, cont);
            return null;
        }
    protected NamingEnumeration c_search(Name name,
                                         String filterExpr,
                                         Object[] filterArgs,
                                         SearchControls cons, Continuation cont)
        throws NamingException  {
            if (resolve_to_penultimate_context(name, cont))
                return a_search(name.toString(), filterExpr, filterArgs, cons, cont);
            return null;
        }
    protected DirContext c_getSchema(Name name, Continuation cont)
        throws NamingException  {
            if (resolve_to_context(name, cont))
                return a_getSchema(cont);
            return null;
        }
    protected DirContext c_getSchemaClassDefinition(Name name, Continuation cont)
        throws NamingException  {
            if (resolve_to_context(name, cont))
                return a_getSchemaClassDefinition(cont);
            return null;
        }
    protected Attributes c_getAttributes_nns(Name name, String[] attrIds,
                                           Continuation cont)
        throws NamingException  {
            if (resolve_to_penultimate_context_nns(name, cont))
                return a_getAttributes_nns(name.toString(), attrIds, cont);
            return null;
        }
    protected void c_modifyAttributes_nns(Name name, int mod_op,
                                          Attributes attrs, Continuation cont)
        throws NamingException {
            if (resolve_to_penultimate_context_nns(name, cont))
                a_modifyAttributes_nns(name.toString(), mod_op, attrs, cont);
        }
    protected void c_modifyAttributes_nns(Name name, ModificationItem[] mods,
                                      Continuation cont)
        throws NamingException {
            if (resolve_to_penultimate_context_nns(name, cont))
                a_modifyAttributes_nns(name.toString(), mods, cont);
        }
    protected void c_bind_nns(Name name, Object obj,
                              Attributes attrs, Continuation cont)
        throws NamingException  {
            if (resolve_to_penultimate_context_nns(name, cont))
                a_bind_nns(name.toString(), obj, attrs, cont);
        }
    protected void c_rebind_nns(Name name, Object obj,
                                Attributes attrs, Continuation cont)
        throws NamingException  {
            if (resolve_to_penultimate_context_nns(name, cont))
                a_rebind_nns(name.toString(), obj, attrs, cont);
        }
    protected DirContext c_createSubcontext_nns(Name name,
                                               Attributes attrs,
                                               Continuation cont)
        throws NamingException  {
            if (resolve_to_penultimate_context_nns(name, cont))
                return a_createSubcontext_nns(name.toString(), attrs, cont);
            return null;
        }
    protected NamingEnumeration c_search_nns(Name name,
                                         Attributes matchingAttributes,
                                         String[] attributesToReturn,
                                         Continuation cont)
        throws NamingException  {
            resolve_to_nns_and_continue(name, cont);
            return null;
        }
    protected NamingEnumeration c_search_nns(Name name,
                                         String filter,
                                         SearchControls cons, Continuation cont)
        throws NamingException {
            if (resolve_to_penultimate_context_nns(name, cont))
                return a_search_nns(name.toString(), filter, cons, cont);
            return null;
        }
    protected NamingEnumeration c_search_nns(Name name,
                                             String filterExpr,
                                             Object[] filterArgs,
                                             SearchControls cons,
                                             Continuation cont)
        throws NamingException  {
            if (resolve_to_penultimate_context_nns(name, cont))
                return a_search_nns(name.toString(), filterExpr, filterArgs,
                                    cons, cont);
            return null;
        }
    protected DirContext c_getSchema_nns(Name name, Continuation cont)
        throws NamingException  {
            resolve_to_nns_and_continue(name, cont);
            return null;
        }
    protected DirContext c_getSchemaClassDefinition_nns(Name name, Continuation cont)
        throws NamingException  {
            resolve_to_nns_and_continue(name, cont);
            return null;
        }
}
