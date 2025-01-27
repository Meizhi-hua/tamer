public final class FullValueDescription implements org.omg.CORBA.portable.IDLEntity
{
    public String name = null;
    public String id = null;
    public boolean is_abstract = false;
    public boolean is_custom = false;
    public String defined_in = null;
    public String version = null;
    public com.sun.org.omg.CORBA.OperationDescription operations[] = null;
    public com.sun.org.omg.CORBA.AttributeDescription attributes[] = null;
    public org.omg.CORBA.ValueMember members[] = null;
    public com.sun.org.omg.CORBA.Initializer initializers[] = null;
    public String supported_interfaces[] = null;
    public String abstract_base_values[] = null;
    public boolean is_truncatable = false;
    public String base_value = null;
    public org.omg.CORBA.TypeCode type = null;
    public FullValueDescription ()
    {
    } 
    public FullValueDescription (String _name, String _id, boolean _is_abstract, boolean _is_custom, String _defined_in, String _version, com.sun.org.omg.CORBA.OperationDescription[] _operations, com.sun.org.omg.CORBA.AttributeDescription[] _attributes, org.omg.CORBA.ValueMember[] _members, com.sun.org.omg.CORBA.Initializer[] _initializers, String[] _supported_interfaces, String[] _abstract_base_values, boolean _is_truncatable, String _base_value, org.omg.CORBA.TypeCode _type)
    {
        name = _name;
        id = _id;
        is_abstract = _is_abstract;
        is_custom = _is_custom;
        defined_in = _defined_in;
        version = _version;
        operations = _operations;
        attributes = _attributes;
        members = _members;
        initializers = _initializers;
        supported_interfaces = _supported_interfaces;
        abstract_base_values = _abstract_base_values;
        is_truncatable = _is_truncatable;
        base_value = _base_value;
        type = _type;
    } 
} 
