public final class OperationDescription implements org.omg.CORBA.portable.IDLEntity
{
    public String name = null;
    public String id = null;
    public String defined_in = null;
    public String version = null;
    public org.omg.CORBA.TypeCode result = null;
    public com.sun.org.omg.CORBA.OperationMode mode = null;
    public String contexts[] = null;
    public com.sun.org.omg.CORBA.ParameterDescription parameters[] = null;
    public com.sun.org.omg.CORBA.ExceptionDescription exceptions[] = null;
    public OperationDescription ()
    {
    } 
    public OperationDescription (String _name, String _id, String _defined_in, String _version, org.omg.CORBA.TypeCode _result, com.sun.org.omg.CORBA.OperationMode _mode, String[] _contexts, com.sun.org.omg.CORBA.ParameterDescription[] _parameters, com.sun.org.omg.CORBA.ExceptionDescription[] _exceptions)
    {
        name = _name;
        id = _id;
        defined_in = _defined_in;
        version = _version;
        result = _result;
        mode = _mode;
        contexts = _contexts;
        parameters = _parameters;
        exceptions = _exceptions;
    } 
} 
