public final class UseDynamicProxies_Stub
    extends java.rmi.server.RemoteStub
    implements RemoteInterface
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_passInt_0;
    private static java.lang.reflect.Method $method_passObject_1;
    private static java.lang.reflect.Method $method_passString_2;
    static {
        try {
            $method_passInt_0 = RemoteInterface.class.getMethod("passInt", new java.lang.Class[] {int.class});
            $method_passObject_1 = RemoteInterface.class.getMethod("passObject", new java.lang.Class[] {java.lang.Object.class});
            $method_passString_2 = RemoteInterface.class.getMethod("passString", new java.lang.Class[] {java.lang.String.class});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public UseDynamicProxies_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public int passInt(int $param_int_1)
        throws java.io.IOException
    {
        try {
            Object $result = ref.invoke(this, $method_passInt_0, new java.lang.Object[] {new java.lang.Integer($param_int_1)}, 8655249712495061761L);
            return ((java.lang.Integer) $result).intValue();
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.io.IOException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
    public java.lang.Object passObject(java.lang.Object $param_Object_1)
        throws java.io.IOException
    {
        try {
            Object $result = ref.invoke(this, $method_passObject_1, new java.lang.Object[] {$param_Object_1}, 3074202549763602823L);
            return ((java.lang.Object) $result);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.io.IOException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
    public java.lang.String passString(java.lang.String $param_String_1)
        throws java.io.IOException
    {
        try {
            Object $result = ref.invoke(this, $method_passString_2, new java.lang.Object[] {$param_String_1}, 6627880292288702000L);
            return ((java.lang.String) $result);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.io.IOException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
