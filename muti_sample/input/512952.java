final class FloatToByteBufferAdapter extends FloatBuffer implements
        DirectBuffer {
    static FloatBuffer wrap(ByteBuffer byteBuffer) {
        return new FloatToByteBufferAdapter(byteBuffer.slice());
    }
    private final ByteBuffer byteBuffer;
    FloatToByteBufferAdapter(ByteBuffer byteBuffer) {
        super((byteBuffer.capacity() >> 2));
        this.byteBuffer = byteBuffer;
        this.byteBuffer.clear();
    }
    public int getByteCapacity() {
        if (byteBuffer instanceof DirectBuffer) {
            return ((DirectBuffer) byteBuffer).getByteCapacity();
        }
        assert false : byteBuffer;
        return -1;
    }
    public PlatformAddress getEffectiveAddress() {
        if (byteBuffer instanceof DirectBuffer) {
            PlatformAddress addr = ((DirectBuffer)byteBuffer).getEffectiveAddress();
            effectiveDirectAddress = addr.toInt();
            return addr;
        }
        assert false : byteBuffer;
        return null;
    }
    public PlatformAddress getBaseAddress() {
        if (byteBuffer instanceof DirectBuffer) {
            return ((DirectBuffer) byteBuffer).getBaseAddress();
        }
        assert false : byteBuffer;
        return null;
    }
    public boolean isAddressValid() {
        if (byteBuffer instanceof DirectBuffer) {
            return ((DirectBuffer) byteBuffer).isAddressValid();
        }
        assert false : byteBuffer;
        return false;
    }
    public void addressValidityCheck() {
        if (byteBuffer instanceof DirectBuffer) {
            ((DirectBuffer) byteBuffer).addressValidityCheck();
        } else {
            assert false : byteBuffer;
        }
    }
    public void free() {
        if (byteBuffer instanceof DirectBuffer) {
            ((DirectBuffer) byteBuffer).free();
        } else {
            assert false : byteBuffer;
        }
    }
    @Override
    public FloatBuffer asReadOnlyBuffer() {
        FloatToByteBufferAdapter buf = new FloatToByteBufferAdapter(byteBuffer
                .asReadOnlyBuffer());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }
    @Override
    public FloatBuffer compact() {
        if (byteBuffer.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        byteBuffer.limit(limit << 2);
        byteBuffer.position(position << 2);
        byteBuffer.compact();
        byteBuffer.clear();
        position = limit - position;
        limit = capacity;
        mark = UNSET_MARK;
        return this;
    }
    @Override
    public FloatBuffer duplicate() {
        FloatToByteBufferAdapter buf = new FloatToByteBufferAdapter(byteBuffer
                .duplicate());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }
    @Override
    public float get() {
        if (position == limit) {
            throw new BufferUnderflowException();
        }
        return byteBuffer.getFloat(position++ << 2);
    }
    @Override
    public float get(int index) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        return byteBuffer.getFloat(index << 2);
    }
    @Override
    public boolean isDirect() {
        return byteBuffer.isDirect();
    }
    @Override
    public boolean isReadOnly() {
        return byteBuffer.isReadOnly();
    }
    @Override
    public ByteOrder order() {
        return byteBuffer.order();
    }
    @Override
    protected float[] protectedArray() {
        throw new UnsupportedOperationException();
    }
    @Override
    protected int protectedArrayOffset() {
        throw new UnsupportedOperationException();
    }
    @Override
    protected boolean protectedHasArray() {
        return false;
    }
    @Override
    public FloatBuffer put(float c) {
        if (position == limit) {
            throw new BufferOverflowException();
        }
        byteBuffer.putFloat(position++ << 2, c);
        return this;
    }
    @Override
    public FloatBuffer put(int index, float c) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        byteBuffer.putFloat(index << 2, c);
        return this;
    }
    @Override
    public FloatBuffer slice() {
        byteBuffer.limit(limit << 2);
        byteBuffer.position(position << 2);
        FloatBuffer result = new FloatToByteBufferAdapter(byteBuffer.slice());
        byteBuffer.clear();
        return result;
    }
}
