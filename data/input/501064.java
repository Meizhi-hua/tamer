public final class CstInteger
        extends CstLiteral32 {
    private static final CstInteger[] cache = new CstInteger[511];
    public static final CstInteger VALUE_M1 = make(-1);
    public static final CstInteger VALUE_0 = make(0);
    public static final CstInteger VALUE_1 = make(1);
    public static final CstInteger VALUE_2 = make(2);
    public static final CstInteger VALUE_3 = make(3);
    public static final CstInteger VALUE_4 = make(4);
    public static final CstInteger VALUE_5 = make(5);
    public static CstInteger make(int value) {
        int idx = (value & 0x7fffffff) % cache.length;
        CstInteger obj = cache[idx];
        if ((obj != null) && (obj.getValue() == value)) {
            return obj;
        }
        obj = new CstInteger(value);
        cache[idx] = obj;
        return obj;
    }
    private CstInteger(int value) {
        super(value);
    }
    @Override
    public String toString() {
        int value = getIntBits();
        return "int{0x" + Hex.u4(value) + " / " + value + '}';
    }
    public Type getType() {
        return Type.INT;
    }
    @Override
    public String typeName() {
        return "int";
    }
    public String toHuman() {
        return Integer.toString(getIntBits());
    }
    public int getValue() {
        return getIntBits();
    }
}
