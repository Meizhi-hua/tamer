public final class OperationModeHelper
{
    private static String  _id = "IDL:omg.org/CORBA/OperationMode:1.0";
    public OperationModeHelper()
    {
    }
    public static void insert (org.omg.CORBA.Any a, com.sun.org.omg.CORBA.OperationMode that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }
    public static com.sun.org.omg.CORBA.OperationMode extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }
    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                __typeCode = org.omg.CORBA.ORB.init ().create_enum_tc (com.sun.org.omg.CORBA.OperationModeHelper.id (), "OperationMode", new String[] { "OP_NORMAL", "OP_ONEWAY"} );
            }
        return __typeCode;
    }
    public static String id ()
    {
        return _id;
    }
    public static com.sun.org.omg.CORBA.OperationMode read (org.omg.CORBA.portable.InputStream istream)
    {
        return com.sun.org.omg.CORBA.OperationMode.from_int (istream.read_long ());
    }
    public static void write (org.omg.CORBA.portable.OutputStream ostream, com.sun.org.omg.CORBA.OperationMode value)
    {
        ostream.write_long (value.value ());
    }
}
