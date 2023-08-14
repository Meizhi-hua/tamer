class PacketStream {
    final VirtualMachineImpl vm;
    private int inCursor = 0;
    final Packet pkt;
    private ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
    private boolean isCommitted = false;
    PacketStream(VirtualMachineImpl vm, int cmdSet, int cmd) {
        this.vm = vm;
        this.pkt = new Packet();
        pkt.cmdSet = (short)cmdSet;
        pkt.cmd = (short)cmd;
    }
    PacketStream(VirtualMachineImpl vm, Packet pkt) {
        this.vm = vm;
        this.pkt = pkt;
        this.isCommitted = true; 
    }
    int id() {
        return pkt.id;
    }
    void send() {
        if (!isCommitted) {
            pkt.data = dataStream.toByteArray();
            vm.sendToTarget(pkt);
            isCommitted = true;
        }
    }
    void waitForReply() throws JDWPException {
        if (!isCommitted) {
            throw new InternalException("waitForReply without send");
        }
        vm.waitForTargetReply(pkt);
        if (pkt.errorCode != Packet.ReplyNoError) {
            throw new JDWPException(pkt.errorCode);
        }
    }
    void writeBoolean(boolean data) {
        if(data) {
            dataStream.write( 1 );
        } else {
            dataStream.write( 0 );
        }
    }
    void writeByte(byte data) {
        dataStream.write( data );
    }
    void writeChar(char data) {
        dataStream.write( (byte)((data >>> 8) & 0xFF) );
        dataStream.write( (byte)((data >>> 0) & 0xFF) );
    }
    void writeShort(short data) {
        dataStream.write( (byte)((data >>> 8) & 0xFF) );
        dataStream.write( (byte)((data >>> 0) & 0xFF) );
    }
    void writeInt(int data) {
        dataStream.write( (byte)((data >>> 24) & 0xFF) );
        dataStream.write( (byte)((data >>> 16) & 0xFF) );
        dataStream.write( (byte)((data >>> 8) & 0xFF) );
        dataStream.write( (byte)((data >>> 0) & 0xFF) );
    }
    void writeLong(long data) {
        dataStream.write( (byte)((data >>> 56) & 0xFF) );
        dataStream.write( (byte)((data >>> 48) & 0xFF) );
        dataStream.write( (byte)((data >>> 40) & 0xFF) );
        dataStream.write( (byte)((data >>> 32) & 0xFF) );
        dataStream.write( (byte)((data >>> 24) & 0xFF) );
        dataStream.write( (byte)((data >>> 16) & 0xFF) );
        dataStream.write( (byte)((data >>> 8) & 0xFF) );
        dataStream.write( (byte)((data >>> 0) & 0xFF) );
    }
    void writeFloat(float data) {
        writeInt(Float.floatToIntBits(data));
    }
    void writeDouble(double data) {
        writeLong(Double.doubleToLongBits(data));
    }
    void writeID(int size, long data) {
        switch (size) {
            case 8:
                writeLong(data);
                break;
            case 4:
                writeInt((int)data);
                break;
            case 2:
                writeShort((short)data);
                break;
            default:
                throw new UnsupportedOperationException("JDWP: ID size not supported: " + size);
        }
    }
    void writeNullObjectRef() {
        writeObjectRef(0);
    }
    void writeObjectRef(long data) {
        writeID(vm.sizeofObjectRef, data);
    }
    void writeClassRef(long data) {
        writeID(vm.sizeofClassRef, data);
    }
    void writeMethodRef(long data) {
        writeID(vm.sizeofMethodRef, data);
    }
    void writeFieldRef(long data) {
        writeID(vm.sizeofFieldRef, data);
    }
    void writeFrameRef(long data) {
        writeID(vm.sizeofFrameRef, data);
    }
    void writeByteArray(byte[] data) {
        dataStream.write(data, 0, data.length);
    }
    void writeString(String string) {
        try {
            byte[] stringBytes = string.getBytes("UTF8");
            writeInt(stringBytes.length);
            writeByteArray(stringBytes);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new InternalException("Cannot convert string to UTF8 bytes");
        }
    }
    void writeLocation(Location location) {
        ReferenceTypeImpl refType = (ReferenceTypeImpl)location.declaringType();
        byte tag;
        if (refType instanceof ClassType) {
            tag = JDWP.TypeTag.CLASS;
        } else if (refType instanceof InterfaceType) {
            tag = JDWP.TypeTag.INTERFACE;
        } else {
            throw new InternalException("Invalid Location");
        }
        writeByte(tag);
        writeClassRef(refType.ref());
        writeMethodRef(((MethodImpl)location.method()).ref());
        writeLong(location.codeIndex());
    }
    void writeValue(Value val) {
        try {
            writeValueChecked(val);
        } catch (InvalidTypeException exc) {  
            throw new RuntimeException(
                "Internal error: Invalid Tag/Type pair");
        }
    }
    void writeValueChecked(Value val) throws InvalidTypeException {
        writeByte(ValueImpl.typeValueKey(val));
        writeUntaggedValue(val);
    }
    void writeUntaggedValue(Value val) {
        try {
            writeUntaggedValueChecked(val);
        } catch (InvalidTypeException exc) {  
            throw new RuntimeException(
                "Internal error: Invalid Tag/Type pair");
        }
    }
    void writeUntaggedValueChecked(Value val) throws InvalidTypeException {
        byte tag = ValueImpl.typeValueKey(val);
        if (isObjectTag(tag)) {
            if (val == null) {
                 writeObjectRef(0);
            } else {
                if (!(val instanceof ObjectReference)) {
                    throw new InvalidTypeException();
                }
                writeObjectRef(((ObjectReferenceImpl)val).ref());
            }
        } else {
            switch (tag) {
                case JDWP.Tag.BYTE:
                    if(!(val instanceof ByteValue))
                        throw new InvalidTypeException();
                    writeByte(((PrimitiveValue)val).byteValue());
                    break;
                case JDWP.Tag.CHAR:
                    if(!(val instanceof CharValue))
                        throw new InvalidTypeException();
                    writeChar(((PrimitiveValue)val).charValue());
                    break;
                case JDWP.Tag.FLOAT:
                    if(!(val instanceof FloatValue))
                        throw new InvalidTypeException();
                    writeFloat(((PrimitiveValue)val).floatValue());
                    break;
                case JDWP.Tag.DOUBLE:
                    if(!(val instanceof DoubleValue))
                        throw new InvalidTypeException();
                    writeDouble(((PrimitiveValue)val).doubleValue());
                    break;
                case JDWP.Tag.INT:
                    if(!(val instanceof IntegerValue))
                        throw new InvalidTypeException();
                    writeInt(((PrimitiveValue)val).intValue());
                    break;
                case JDWP.Tag.LONG:
                    if(!(val instanceof LongValue))
                        throw new InvalidTypeException();
                    writeLong(((PrimitiveValue)val).longValue());
                    break;
                case JDWP.Tag.SHORT:
                    if(!(val instanceof ShortValue))
                        throw new InvalidTypeException();
                    writeShort(((PrimitiveValue)val).shortValue());
                    break;
                case JDWP.Tag.BOOLEAN:
                    if(!(val instanceof BooleanValue))
                        throw new InvalidTypeException();
                    writeBoolean(((PrimitiveValue)val).booleanValue());
                    break;
            }
        }
    }
    byte readByte() {
        byte ret = pkt.data[inCursor];
        inCursor += 1;
        return ret;
    }
    boolean readBoolean() {
        byte ret = readByte();
        return (ret != 0);
    }
    char readChar() {
        int b1, b2;
        b1 = pkt.data[inCursor++] & 0xff;
        b2 = pkt.data[inCursor++] & 0xff;
        return (char)((b1 << 8) + b2);
    }
    short readShort() {
        int b1, b2;
        b1 = pkt.data[inCursor++] & 0xff;
        b2 = pkt.data[inCursor++] & 0xff;
        return (short)((b1 << 8) + b2);
    }
    int readInt() {
        int b1,b2,b3,b4;
        b1 = pkt.data[inCursor++] & 0xff;
        b2 = pkt.data[inCursor++] & 0xff;
        b3 = pkt.data[inCursor++] & 0xff;
        b4 = pkt.data[inCursor++] & 0xff;
        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
    }
    long readLong() {
        long b1,b2,b3,b4;
        long b5,b6,b7,b8;
        b1 = pkt.data[inCursor++] & 0xff;
        b2 = pkt.data[inCursor++] & 0xff;
        b3 = pkt.data[inCursor++] & 0xff;
        b4 = pkt.data[inCursor++] & 0xff;
        b5 = pkt.data[inCursor++] & 0xff;
        b6 = pkt.data[inCursor++] & 0xff;
        b7 = pkt.data[inCursor++] & 0xff;
        b8 = pkt.data[inCursor++] & 0xff;
        return ((b1 << 56) + (b2 << 48) + (b3 << 40) + (b4 << 32)
                + (b5 << 24) + (b6 << 16) + (b7 << 8) + b8);
    }
    float readFloat() {
        return Float.intBitsToFloat(readInt());
    }
    double readDouble() {
        return Double.longBitsToDouble(readLong());
    }
    String readString() {
        String ret;
        int len = readInt();
        try {
            ret = new String(pkt.data, inCursor, len, "UTF8");
        } catch(java.io.UnsupportedEncodingException e) {
            System.err.println(e);
            ret = "Conversion error!";
        }
        inCursor += len;
        return ret;
    }
    private long readID(int size) {
        switch (size) {
          case 8:
              return readLong();
          case 4:
              return (long)readInt();
          case 2:
              return (long)readShort();
          default:
              throw new UnsupportedOperationException("JDWP: ID size not supported: " + size);
        }
    }
    long readObjectRef() {
        return readID(vm.sizeofObjectRef);
    }
    long readClassRef() {
        return readID(vm.sizeofClassRef);
    }
    ObjectReferenceImpl readTaggedObjectReference() {
        byte typeKey = readByte();
        return vm.objectMirror(readObjectRef(), typeKey);
    }
    ObjectReferenceImpl readObjectReference() {
        return vm.objectMirror(readObjectRef());
    }
    StringReferenceImpl readStringReference() {
        long ref = readObjectRef();
        return vm.stringMirror(ref);
    }
    ArrayReferenceImpl readArrayReference() {
        long ref = readObjectRef();
        return vm.arrayMirror(ref);
    }
    ThreadReferenceImpl readThreadReference() {
        long ref = readObjectRef();
        return vm.threadMirror(ref);
    }
    ThreadGroupReferenceImpl readThreadGroupReference() {
        long ref = readObjectRef();
        return vm.threadGroupMirror(ref);
    }
    ClassLoaderReferenceImpl readClassLoaderReference() {
        long ref = readObjectRef();
        return vm.classLoaderMirror(ref);
    }
    ClassObjectReferenceImpl readClassObjectReference() {
        long ref = readObjectRef();
        return vm.classObjectMirror(ref);
    }
    ReferenceTypeImpl readReferenceType() {
        byte tag = readByte();
        long ref = readObjectRef();
        return vm.referenceType(ref, tag);
    }
    long readMethodRef() {
        return readID(vm.sizeofMethodRef);
    }
    long readFieldRef() {
        return readID(vm.sizeofFieldRef);
    }
    Field readField() {
        ReferenceTypeImpl refType = readReferenceType();
        long fieldRef = readFieldRef();
        return refType.getFieldMirror(fieldRef);
    }
    long readFrameRef() {
        return readID(vm.sizeofFrameRef);
    }
    ValueImpl readValue() {
        byte typeKey = readByte();
        return readUntaggedValue(typeKey);
    }
    ValueImpl readUntaggedValue(byte typeKey) {
        ValueImpl val = null;
        if (isObjectTag(typeKey)) {
            val = vm.objectMirror(readObjectRef(), typeKey);
        } else {
            switch(typeKey) {
                case JDWP.Tag.BYTE:
                    val = new ByteValueImpl(vm, readByte());
                    break;
                case JDWP.Tag.CHAR:
                    val = new CharValueImpl(vm, readChar());
                    break;
                case JDWP.Tag.FLOAT:
                    val = new FloatValueImpl(vm, readFloat());
                    break;
                case JDWP.Tag.DOUBLE:
                    val = new DoubleValueImpl(vm, readDouble());
                    break;
                case JDWP.Tag.INT:
                    val = new IntegerValueImpl(vm, readInt());
                    break;
                case JDWP.Tag.LONG:
                    val = new LongValueImpl(vm, readLong());
                    break;
                case JDWP.Tag.SHORT:
                    val = new ShortValueImpl(vm, readShort());
                    break;
                case JDWP.Tag.BOOLEAN:
                    val = new BooleanValueImpl(vm, readBoolean());
                    break;
                case JDWP.Tag.VOID:
                    val = new VoidValueImpl(vm);
                    break;
            }
        }
        return val;
    }
    Location readLocation() {
        byte tag = readByte();
        long classRef = readObjectRef();
        long methodRef = readMethodRef();
        long codeIndex = readLong();
        if (classRef != 0) {
            ReferenceTypeImpl refType = vm.referenceType(classRef, tag);
            return new LocationImpl(vm, refType, methodRef, codeIndex);
        } else {
           return null;
        }
    }
    byte[] readByteArray(int length) {
        byte[] array = new byte[length];
        System.arraycopy(pkt.data, inCursor, array, 0, length);
        inCursor += length;
        return array;
    }
    List<Value> readArrayRegion() {
        byte typeKey = readByte();
        int length = readInt();
        List<Value> list = new ArrayList<Value>(length);
        boolean gettingObjects = isObjectTag(typeKey);
        for (int i = 0; i < length; i++) {
            if (gettingObjects) {
                typeKey = readByte();
            }
            Value value = readUntaggedValue(typeKey);
            list.add(value);
        }
        return list;
    }
    void writeArrayRegion(List<Value> srcValues) {
        writeInt(srcValues.size());
        for (int i = 0; i < srcValues.size(); i++) {
            Value value = srcValues.get(i);
            writeUntaggedValue(value);
        }
    }
    int skipBytes(int n) {
        inCursor += n;
        return n;
    }
    byte command() {
        return (byte)pkt.cmd;
    }
    static boolean isObjectTag(byte tag) {
        return (tag == JDWP.Tag.OBJECT) ||
               (tag == JDWP.Tag.ARRAY) ||
               (tag == JDWP.Tag.STRING) ||
               (tag == JDWP.Tag.THREAD) ||
               (tag == JDWP.Tag.THREAD_GROUP) ||
               (tag == JDWP.Tag.CLASS_LOADER) ||
               (tag == JDWP.Tag.CLASS_OBJECT);
    }
}