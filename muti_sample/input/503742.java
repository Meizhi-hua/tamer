final class DoubleToByteBufferAdapter extends DoubleBuffer implements
        DirectBuffer {
    static DoubleBuffer wrap(ByteBuffer byteBuffer) {
        return new DoubleToByteBufferAdapter(byteBuffer.slice());
    }
    private final ByteBuffer byteBuffer;
    DoubleToByteBufferAdapter(ByteBuffer byteBuffer) {
        super((byteBuffer.capacity() >> 3));
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
    public DoubleBuffer asReadOnlyBuffer() {
        DoubleToByteBufferAdapter buf = new DoubleToByteBufferAdapter(
                byteBuffer.asReadOnlyBuffer());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }
    @Override
    public DoubleBuffer compact() {
        if (byteBuffer.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        byteBuffer.limit(limit << 3);
        byteBuffer.position(position << 3);
        byteBuffer.compact();
        byteBuffer.clear();
        position = limit - position;
        limit = capacity;
        mark = UNSET_MARK;
        return this;
    }
    @Override
    public DoubleBuffer duplicate() {
        DoubleToByteBufferAdapter buf = new DoubleToByteBufferAdapter(
                byteBuffer.duplicate());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }
    @Override
    public double get() {
        if (position == limit) {
            throw new BufferUnderflowException();
        }
        return byteBuffer.getDouble(position++ << 3);
    }
    @Override
    public double get(int index) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        return byteBuffer.getDouble(index << 3);
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
    protected double[] protectedArray() {
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
    public DoubleBuffer put(double c) {
        if (position == limit) {
            throw new BufferOverflowException();
        }
        byteBuffer.putDouble(position++ << 3, c);
        return this;
    }
    @Override
    public DoubleBuffer put(int index, double c) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        byteBuffer.putDouble(index << 3, c);
        return this;
    }
    @Override
    public DoubleBuffer slice() {
        byteBuffer.limit(limit << 3);
        byteBuffer.position(position << 3);
        DoubleBuffer result = new DoubleToByteBufferAdapter(byteBuffer.slice());
        byteBuffer.clear();
        return result;
    }
}
