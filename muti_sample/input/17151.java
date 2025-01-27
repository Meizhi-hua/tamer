class ModificationWatchpointsTarg {
    public static final int RepeatCount = 3;
    public static final byte ByteVal = -17;
    public static final char CharVal = 'Y';
    public static final short ShortVal = -412;
    public static final int IntVal = -711618;
    public static final long LongVal = 0x1234567890123456L;
    public static final float FloatVal = 7.986f;
    public static final double DoubleVal = 3.14159265358979d;
    public static final String StringVal = "OnceMore";
    public static final Object ObjectVal = new Object();
    static byte sByte;
    static char sChar;
    static short sShort;
    static int sInt;
    static long sLong;
    static float sFloat;
    static double sDouble;
    static String sString;
    static Object sObject;
    byte iByte;
    char iChar;
    short iShort;
    int iInt;
    long iLong;
    float iFloat;
    double iDouble;
    String iString;
    Object iObject;
    void iByteSet() {
        iByte = ByteVal;
    }
    void iCharSet() {
        iChar = CharVal;
    }
    void iShortSet() {
        iShort = ShortVal;
    }
    void iIntSet() {
        iInt = IntVal;
    }
    void iLongSet() {
        iLong = LongVal;
    }
    void iFloatSet() {
        iFloat = FloatVal;
    }
    void iDoubleSet() {
        iDouble = DoubleVal;
    }
    void iStringSet() {
        iString = StringVal;
    }
    void iObjectSet() {
        iObject = ObjectVal;
    }
    static void sByteSet() {
        sByte = ByteVal;
    }
    static void sCharSet() {
        sChar = CharVal;
    }
    static void sShortSet() {
        sShort = ShortVal;
    }
    static void sIntSet() {
        sInt = IntVal;
    }
    static void sLongSet() {
        sLong = LongVal;
    }
    static void sFloatSet() {
        sFloat = FloatVal;
    }
    static void sDoubleSet() {
        sDouble = DoubleVal;
    }
    static void sStringSet() {
        sString = StringVal;
    }
    static void sObjectSet() {
        sObject = ObjectVal;
    }
    void iUpdate(){
        iByteSet();
        iCharSet();
        iShortSet();
        iIntSet();
        iLongSet();
        iFloatSet();
        iDoubleSet();
        iStringSet();
        iObjectSet();
    }
    static void sUpdate(){
        sByteSet();
        sCharSet();
        sShortSet();
        sIntSet();
        sLongSet();
        sFloatSet();
        sDoubleSet();
        sStringSet();
        sObjectSet();
    }
    public static void main(String[] args){
        ModificationWatchpointsTarg targ = new ModificationWatchpointsTarg();
        for (int i = RepeatCount; i > 0; i--) {
            sUpdate();
            targ.iUpdate();
        }
    }
}
public class ModificationWatchpoints extends TestScaffold {
    ReferenceType targ;
    List allMWP = new ArrayList();
    ModificationWatchpoints (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new ModificationWatchpoints(args).startTests();
    }
    public void fieldModified(ModificationWatchpointEvent event) {
        MWP mwp = (MWP)event.request().getProperty("executor");
        mwp.fieldModified(event);
    }
    void set(String fieldName, String valString) {
        Value val = targ.getValue(targ.fieldByName(valString));
        MWP mwp = new MWP("ModificationWatchpointsTarg", fieldName, val);
        allMWP.add(mwp);
        mwp.set();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("ModificationWatchpointsTarg");
        targ = bpe.location().declaringType();
        set("iByte", "ByteVal");
        set("iChar", "CharVal");
        set("iShort", "ShortVal");
        set("iInt", "IntVal");
        set("iLong", "LongVal");
        set("iFloat", "FloatVal");
        set("iDouble", "DoubleVal");
        set("iString", "StringVal");
        set("iObject", "ObjectVal");
        set("sByte", "ByteVal");
        set("sChar", "CharVal");
        set("sShort", "ShortVal");
        set("sInt", "IntVal");
        set("sLong", "LongVal");
        set("sFloat", "FloatVal");
        set("sDouble", "DoubleVal");
        set("sString", "StringVal");
        set("sObject", "ObjectVal");
        listenUntilVMDisconnect();
        if (!testFailed) {
            for (Iterator it = allMWP.iterator(); it.hasNext();) {
                MWP mwp = (MWP)it.next();
                mwp.checkEventCounts(ModificationWatchpointsTarg.RepeatCount);
            }
        }
        if (!testFailed) {
            println("ModificationWatchpoints: passed");
        } else {
            throw new Exception("ModificationWatchpoints: failed");
        }
    }
    class MWP {
        private final String className;
        private final String fieldName;
        private final Value expectedValue;
        public int eventCount = 0;
        public boolean failed = false;
        public MWP(String className, String fieldName, Value value) {
            this.className = className;
            this.fieldName = fieldName;
            this.expectedValue = value;
        }
        public void set() {
            List classes = vm().classesByName(className);
            if (classes.size() != 1) {
                failure("Expected one class named " + className + " got " + classes.size());
            }
            set ((ReferenceType)classes.get(0));
        }
        public void set(ReferenceType clazz) {
            Field f = clazz.fieldByName(fieldName);
            ModificationWatchpointRequest mwr =
                       eventRequestManager().createModificationWatchpointRequest(f);
            mwr.putProperty("executor", this);
            mwr.enable();
            println("set watchpoint: " + className +"." + f);
        }
        public void fieldModified(ModificationWatchpointEvent event) {
            Value val = event.valueToBe();
           println("Watchpoint reached: " + className + "." + fieldName +
                   ", new value: " + val);
            if (!val.equals(expectedValue)) {
                failure("FAILURE: value should be: " +
                        expectedValue.getClass() + ":" + expectedValue +
                        " has - " + val.getClass() + ":" + val);
            }
            if (!event.location().method().name().equals(fieldName + "Set")) {
                failure("FAILURE: occurred in wrong place: " + event.location());
            }
            eventCount++;
        }
        public void checkEventCounts(int expectedCount) {
            if (eventCount != expectedCount) {
                failure(className + "." + fieldName +
                        " - only got " + eventCount + " events");
            }
        }
    } 
}
