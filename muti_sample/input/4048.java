public final class AttributeModeHelper
{
    private static String  _id = "IDL:omg.org/CORBA/AttributeMode:1.0";
    public AttributeModeHelper()
    {
    }
    public static void insert (org.omg.CORBA.Any a, com.sun.org.omg.CORBA.AttributeMode that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }
    public static com.sun.org.omg.CORBA.AttributeMode extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }
    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                __typeCode = org.omg.CORBA.ORB.init ().create_enum_tc (com.sun.org.omg.CORBA.AttributeModeHelper.id (), "AttributeMode", new String[] { "ATTR_NORMAL", "ATTR_READONLY"} );
            }
        return __typeCode;
    }
    public static String id ()
    {
        return _id;
    }
    public static com.sun.org.omg.CORBA.AttributeMode read (org.omg.CORBA.portable.InputStream istream)
    {
        return com.sun.org.omg.CORBA.AttributeMode.from_int (istream.read_long ());
    }
    public static void write (org.omg.CORBA.portable.OutputStream ostream, com.sun.org.omg.CORBA.AttributeMode value)
    {
        ostream.write_long (value.value ());
    }
}
