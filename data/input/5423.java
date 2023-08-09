class NotSerializableObject {
    private String m_str;
    private Integer m_int;
    public NotSerializableObject(String m_str, Integer m_int) {
        this.m_str = m_str;
        this.m_int = m_int;
    }
}
class SerializableObject extends NotSerializableObject
    implements Serializable
{
    public SerializableObject(String m_str, Integer m_int) {
        super(m_str, m_int);
    }
    public SerializableObject() {
        super("test", 10);
    }
}
public class ExpectedStackTrace {
    private static final String SER_METHOD_NAME = "checkSerializable";
    public static final void main(String[] args) throws Exception {
        System.err.println("\nRegression test for CR6317435");
        checkSerializable(getObject());
    }
    private static Object getObject() throws Exception {
        ObjectStreamClass osc =
            ObjectStreamClass.lookup(SerializableObject.class);
        SerializableObject initObj =
            (SerializableObject) osc.forClass().newInstance();
        return initObj;
    }
    private static void checkSerializable(Object initObj) throws Exception {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
            ObjectOutputStream out = new ObjectOutputStream(bos) ;
            out.writeObject(initObj);
            out.close();
            byte[] buf = bos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            ObjectInputStream in = new ObjectInputStream(bais);
            SerializableObject finObj = (SerializableObject) in.readObject();
            in.close();
            throw new Error();
        } catch(ObjectStreamException ex) {
            StackTraceElement[] stes = ex.getStackTrace();
            boolean found = false;
            for (int i = 0; i<stes.length-1; i++) {
                StackTraceElement ste = stes[i];
                String nme = ste.getMethodName();
                if (nme.equals(SER_METHOD_NAME)) {
                    found = true;
                }
            }
            if (found) {
                System.err.println("\nTEST PASSED");
            } else {
                throw new Error();
            }
        }
    }
}
