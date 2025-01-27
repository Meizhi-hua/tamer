public class PerfDataPrologue extends VMObject {
    private static JIntField  magicField;
    private static JByteField byteOrderField;
    private static JByteField majorVersionField;
    private static JByteField minorVersionField;
    private static JByteField accessibleField;
    private static JIntField  usedField;
    private static JIntField  overflowField;
    private static JLongField modTimeStampField;
    private static JIntField  entryOffsetField;
    private static JIntField  numEntriesField;
    static {
        VM.registerVMInitializedObserver(new Observer() {
                public void update(Observable o, Object data) {
                    initialize(VM.getVM().getTypeDataBase());
                }
            });
    }
    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("PerfDataPrologue");
        magicField = type.getJIntField("magic");
        byteOrderField = type.getJByteField("byte_order");
        majorVersionField = type.getJByteField("major_version");
        minorVersionField = type.getJByteField("minor_version");
        accessibleField = type.getJByteField("accessible");
        usedField = type.getJIntField("used");
        overflowField = type.getJIntField("overflow");
        modTimeStampField = type.getJLongField("mod_time_stamp");
        entryOffsetField = type.getJIntField("entry_offset");
        numEntriesField = type.getJIntField("num_entries");
    }
    public PerfDataPrologue(Address addr) {
        super(addr);
    }
    public int magic() {
        return (int) magicField.getValue(addr);
    }
    public byte byteOrder() {
        return (byte) byteOrderField.getValue(addr);
    }
    public byte majorVersion() {
        return (byte) majorVersionField.getValue(addr);
    }
    public boolean accessible() {
        return ((byte) accessibleField.getValue(addr)) != (byte)0;
    }
    public int used() {
        return (int) usedField.getValue(addr);
    }
    public int overflow() {
        return (int) overflowField.getValue(addr);
    }
    public long modTimeStamp() {
        return (long) modTimeStampField.getValue(addr);
    }
    public int entryOffset() {
        return (int) entryOffsetField.getValue(addr);
    }
    public int numEntries() {
        return (int) numEntriesField.getValue(addr);
    }
}
