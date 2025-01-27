public final class ParameterDescriptionHelper
{
    private static String  _id = "IDL:omg.org/CORBA/ParameterDescription:1.0";
    public ParameterDescriptionHelper()
    {
    }
    public static void insert (org.omg.CORBA.Any a, com.sun.org.omg.CORBA.ParameterDescription that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }
    public static com.sun.org.omg.CORBA.ParameterDescription extract (org.omg.CORBA.Any a)
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
                                org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [4];
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
                                _tcOf_members0 = com.sun.org.omg.CORBA.ParameterModeHelper.type ();
                                _members0[3] = new org.omg.CORBA.StructMember (
                                                                               "mode",
                                                                               _tcOf_members0,
                                                                               null);
                                __typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (com.sun.org.omg.CORBA.ParameterDescriptionHelper.id (), "ParameterDescription", _members0);
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
    public static com.sun.org.omg.CORBA.ParameterDescription read (org.omg.CORBA.portable.InputStream istream)
    {
        com.sun.org.omg.CORBA.ParameterDescription value = new com.sun.org.omg.CORBA.ParameterDescription ();
        value.name = istream.read_string ();
        value.type = istream.read_TypeCode ();
        value.type_def = com.sun.org.omg.CORBA.IDLTypeHelper.read (istream);
        value.mode = com.sun.org.omg.CORBA.ParameterModeHelper.read (istream);
        return value;
    }
    public static void write (org.omg.CORBA.portable.OutputStream ostream, com.sun.org.omg.CORBA.ParameterDescription value)
    {
        ostream.write_string (value.name);
        ostream.write_TypeCode (value.type);
        com.sun.org.omg.CORBA.IDLTypeHelper.write (ostream, value.type_def);
        com.sun.org.omg.CORBA.ParameterModeHelper.write (ostream, value.mode);
    }
}
