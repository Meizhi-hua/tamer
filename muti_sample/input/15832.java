public abstract class BooleanSeqHelper
{
    private static String  _id = "IDL:omg.org/CORBA/BooleanSeq:1.0";
    public static void insert (org.omg.CORBA.Any a, boolean[] that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }
    public static boolean[] extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }
    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                __typeCode = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean);
                __typeCode = org.omg.CORBA.ORB.init ().create_sequence_tc (0, __typeCode);
                __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (org.omg.CORBA.BooleanSeqHelper.id (), "BooleanSeq", __typeCode);
            }
        return __typeCode;
    }
    public static String id ()
    {
        return _id;
    }
    public static boolean[] read (org.omg.CORBA.portable.InputStream istream)
    {
        boolean value[] = null;
        int _len0 = istream.read_long ();
        value = new boolean[_len0];
        istream.read_boolean_array (value, 0, _len0);
        return value;
    }
    public static void write (org.omg.CORBA.portable.OutputStream ostream, boolean[] value)
    {
        ostream.write_long (value.length);
        ostream.write_boolean_array (value, 0, value.length);
    }
}
