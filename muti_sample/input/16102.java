public abstract class DoubleSeqHelper
{
    private static String  _id = "IDL:omg.org/CORBA/DoubleSeq:1.0";
    public static void insert (org.omg.CORBA.Any a, double[] that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }
    public static double[] extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }
    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                __typeCode = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_double);
                __typeCode = org.omg.CORBA.ORB.init ().create_sequence_tc (0, __typeCode);
                __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (org.omg.CORBA.DoubleSeqHelper.id (), "DoubleSeq", __typeCode);
            }
        return __typeCode;
    }
    public static String id ()
    {
        return _id;
    }
    public static double[] read (org.omg.CORBA.portable.InputStream istream)
    {
        double value[] = null;
        int _len0 = istream.read_long ();
        value = new double[_len0];
        istream.read_double_array (value, 0, _len0);
        return value;
    }
    public static void write (org.omg.CORBA.portable.OutputStream ostream, double[] value)
    {
        ostream.write_long (value.length);
        ostream.write_double_array (value, 0, value.length);
    }
}
