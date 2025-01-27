final class CompositeLongValue extends SpecificLongValue
{
    public static final byte ADD                  = '+';
    public static final byte SUBTRACT             = '-';
    public static final byte MULTIPLY             = '*';
    public static final byte DIVIDE               = '/';
    public static final byte REMAINDER            = '%';
    public static final byte SHIFT_LEFT           = '<';
    public static final byte SHIFT_RIGHT          = '>';
    public static final byte UNSIGNED_SHIFT_RIGHT = '}';
    public static final byte AND                  = '&';
    public static final byte OR                   = '|';
    public static final byte XOR                  = '^';
    private final LongValue longValue1;
    private final byte      operation;
    private final Value     longValue2;
    public CompositeLongValue(LongValue longValue1,
                              byte      operation,
                              Value     longValue2)
    {
        this.longValue1 = longValue1;
        this.operation  = operation;
        this.longValue2 = longValue2;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.longValue1.equals(((CompositeLongValue)object).longValue1) &&
               this.operation      == ((CompositeLongValue)object).operation   &&
               this.longValue2.equals(((CompositeLongValue)object).longValue2);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               longValue1.hashCode() ^
               longValue2.hashCode();
    }
    public String toString()
    {
        return "("+longValue1+((char)operation)+longValue2+")";
    }
}