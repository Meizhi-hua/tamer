class UnsafeStaticObjectFieldAccessorImpl extends UnsafeStaticFieldAccessorImpl {
    UnsafeStaticObjectFieldAccessorImpl(Field field) {
        super(field);
    }
    public Object get(Object obj) throws IllegalArgumentException {
        return unsafe.getObject(base, fieldOffset);
    }
    public boolean getBoolean(Object obj) throws IllegalArgumentException {
        throw newGetBooleanIllegalArgumentException();
    }
    public byte getByte(Object obj) throws IllegalArgumentException {
        throw newGetByteIllegalArgumentException();
    }
    public char getChar(Object obj) throws IllegalArgumentException {
        throw newGetCharIllegalArgumentException();
    }
    public short getShort(Object obj) throws IllegalArgumentException {
        throw newGetShortIllegalArgumentException();
    }
    public int getInt(Object obj) throws IllegalArgumentException {
        throw newGetIntIllegalArgumentException();
    }
    public long getLong(Object obj) throws IllegalArgumentException {
        throw newGetLongIllegalArgumentException();
    }
    public float getFloat(Object obj) throws IllegalArgumentException {
        throw newGetFloatIllegalArgumentException();
    }
    public double getDouble(Object obj) throws IllegalArgumentException {
        throw newGetDoubleIllegalArgumentException();
    }
    public void set(Object obj, Object value)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (isFinal) {
            throwFinalFieldIllegalAccessException(value);
        }
        if (value != null) {
            if (!field.getType().isAssignableFrom(value.getClass())) {
                throwSetIllegalArgumentException(value);
            }
        }
        unsafe.putObject(base, fieldOffset, value);
    }
    public void setBoolean(Object obj, boolean z)
        throws IllegalArgumentException, IllegalAccessException
    {
        throwSetIllegalArgumentException(z);
    }
    public void setByte(Object obj, byte b)
        throws IllegalArgumentException, IllegalAccessException
    {
        throwSetIllegalArgumentException(b);
    }
    public void setChar(Object obj, char c)
        throws IllegalArgumentException, IllegalAccessException
    {
        throwSetIllegalArgumentException(c);
    }
    public void setShort(Object obj, short s)
        throws IllegalArgumentException, IllegalAccessException
    {
        throwSetIllegalArgumentException(s);
    }
    public void setInt(Object obj, int i)
        throws IllegalArgumentException, IllegalAccessException
    {
        throwSetIllegalArgumentException(i);
    }
    public void setLong(Object obj, long l)
        throws IllegalArgumentException, IllegalAccessException
    {
        throwSetIllegalArgumentException(l);
    }
    public void setFloat(Object obj, float f)
        throws IllegalArgumentException, IllegalAccessException
    {
        throwSetIllegalArgumentException(f);
    }
    public void setDouble(Object obj, double d)
        throws IllegalArgumentException, IllegalAccessException
    {
        throwSetIllegalArgumentException(d);
    }
}
