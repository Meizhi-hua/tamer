abstract public class UnknownUserExceptionHelper
{
  private static String  _id = "IDL:omg.org/CORBA/UnknownUserException:1.0";
  public static void insert (org.omg.CORBA.Any a, org.omg.CORBA.UnknownUserException that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }
  public static org.omg.CORBA.UnknownUserException extract (org.omg.CORBA.Any a)
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
          org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [1];
          org.omg.CORBA.TypeCode _tcOf_members0 = null;
          _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_any);
          _members0[0] = new org.omg.CORBA.StructMember (
            "except",
            _tcOf_members0,
            null);
          __typeCode = org.omg.CORBA.ORB.init ().create_exception_tc (org.omg.CORBA.UnknownUserExceptionHelper.id (), "UnknownUserException", _members0);
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
  public static org.omg.CORBA.UnknownUserException read (org.omg.CORBA.portable.InputStream istream)
  {
    org.omg.CORBA.UnknownUserException value = new org.omg.CORBA.UnknownUserException ();
    istream.read_string ();
    value.except = istream.read_any ();
    return value;
  }
  public static void write (org.omg.CORBA.portable.OutputStream ostream, org.omg.CORBA.UnknownUserException value)
  {
    ostream.write_string (id ());
    ostream.write_any (value.except);
  }
}
