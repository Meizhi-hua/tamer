public final class StructMemberHelper
{
    private static String  _id = "IDL:omg.org/CORBA/StructMember:1.0";
    public StructMemberHelper()
    {
    }
    public static void insert (org.omg.CORBA.Any a, org.omg.CORBA.StructMember that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }
    public static org.omg.CORBA.StructMember extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }
    private static org.omg.CORBA.TypeCode __typeCode = null;
    private static boolean __active = false;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                synchronized (org.omg.CORBA.TypeCode.class)
                    {
                        if (__typeCode == null)
                            {
                                if (__active)
                                    {
                                        return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
                                    }
                                __active = true;
                                org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [3];
                                org.omg.CORBA.TypeCode _tcOf_members0 = null;
                                _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
                                _tcOf_members0 = org.omg.CORBA.ORB.init ().create_alias_tc (com.sun.org.omg.CORBA.IdentifierHelper.id (), "Identifier", _tcOf_members0);
                                _members0[0] = new org.omg.CORBA.StructMember (
                                                                               "name",
                                                                               _tcOf_members0,
                                                                               null);
                                _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_TypeCode);
                                _members0[1] = new org.omg.CORBA.StructMember (
                                                                               "type",
                                                                               _tcOf_members0,
                                                                               null);
                                _tcOf_members0 = com.sun.org.omg.CORBA.IDLTypeHelper.type ();
                                _members0[2] = new org.omg.CORBA.StructMember (
                                                                               "type_def",
                                                                               _tcOf_members0,
                                                                               null);
                                __typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (com.sun.org.omg.CORBA.StructMemberHelper.id (), "StructMember", _members0);
                                __active = false;
                            }
                    }
            }
        return __typeCode;
    }
    public static String id ()
    {
        return _id;
    }
    public static org.omg.CORBA.StructMember read (org.omg.CORBA.portable.InputStream istream)
    {
        org.omg.CORBA.StructMember value = new org.omg.CORBA.StructMember ();
        value.name = istream.read_string ();
        value.type = istream.read_TypeCode ();
        value.type_def = com.sun.org.omg.CORBA.IDLTypeHelper.read (istream);
        return value;
    }
    public static void write (org.omg.CORBA.portable.OutputStream ostream, org.omg.CORBA.StructMember value)
    {
        ostream.write_string (value.name);
        ostream.write_TypeCode (value.type);
        com.sun.org.omg.CORBA.IDLTypeHelper.write (ostream, value.type_def);
    }
}
