public abstract class ShortSeqHelper
{
    private static String  _id = "IDL:omg.org/CORBA/ShortSeq:1.0";
    public static void insert (org.omg.CORBA.Any a, short[] that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }
    public static short[] extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }
    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                __typeCode = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_short);
                __typeCode = org.omg.CORBA.ORB.init ().create_sequence_tc (0, __typeCode);
                __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (org.omg.CORBA.ShortSeqHelper.id (), "ShortSeq", __typeCode);
            }
        return __typeCode;
    }
    public static String id ()
    {
        return _id;
    }
    public static short[] read (org.omg.CORBA.portable.InputStream istream)
    {
        short value[] = null;
        int _len0 = istream.read_long ();
        value = new short[_len0];
        istream.read_short_array (value, 0, _len0);
        return value;
    }
    public static void write (org.omg.CORBA.portable.OutputStream ostream, short[] value)
    {
        ostream.write_long (value.length);
        ostream.write_short_array (value, 0, value.length);
    }
}
