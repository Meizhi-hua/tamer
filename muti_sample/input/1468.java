public class ConstMethod extends Oop {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static int HAS_LINENUMBER_TABLE;
  private static int HAS_CHECKED_EXCEPTIONS;
  private static int HAS_LOCALVARIABLE_TABLE;
  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    Type type                  = db.lookupType("constMethodOopDesc");
    method                     = new OopField(type.getOopField("_method"), 0);
    exceptionTable             = new OopField(type.getOopField("_exception_table"), 0);
    constMethodSize            = new CIntField(type.getCIntegerField("_constMethod_size"), 0);
    flags                      = new ByteField(type.getJByteField("_flags"), 0);
    HAS_LINENUMBER_TABLE      = db.lookupIntConstant("constMethodOopDesc::_has_linenumber_table").intValue();
    HAS_CHECKED_EXCEPTIONS     = db.lookupIntConstant("constMethodOopDesc::_has_checked_exceptions").intValue();
    HAS_LOCALVARIABLE_TABLE   = db.lookupIntConstant("constMethodOopDesc::_has_localvariable_table").intValue();
    codeSize                   = new CIntField(type.getCIntegerField("_code_size"), 0);
    nameIndex                  = new CIntField(type.getCIntegerField("_name_index"), 0);
    signatureIndex             = new CIntField(type.getCIntegerField("_signature_index"), 0);
    genericSignatureIndex      = new CIntField(type.getCIntegerField("_generic_signature_index"),0);
    bytecodeOffset = type.getSize();
    type                       = db.lookupType("CheckedExceptionElement");
    checkedExceptionElementSize = type.getSize();
    type                       = db.lookupType("LocalVariableTableElement");
    localVariableTableElementSize = type.getSize();
  }
  ConstMethod(OopHandle handle, ObjectHeap heap) {
    super(handle, heap);
  }
  private static OopField  method;
  private static OopField  exceptionTable;
  private static CIntField constMethodSize;
  private static ByteField flags;
  private static CIntField codeSize;
  private static CIntField nameIndex;
  private static CIntField signatureIndex;
  private static CIntField genericSignatureIndex;
  private static long bytecodeOffset;
  private static long checkedExceptionElementSize;
  private static long localVariableTableElementSize;
  public Method getMethod() {
    return (Method) method.getValue(this);
  }
  public TypeArray getExceptionTable() {
    return (TypeArray) exceptionTable.getValue(this);
  }
  public long getConstMethodSize() {
    return constMethodSize.getValue(this);
  }
  public byte getFlags() {
    return flags.getValue(this);
  }
  public long getCodeSize() {
    return codeSize.getValue(this);
  }
  public long getNameIndex() {
    return nameIndex.getValue(this);
  }
  public long getSignatureIndex() {
    return signatureIndex.getValue(this);
  }
  public long getGenericSignatureIndex() {
    return genericSignatureIndex.getValue(this);
  }
  public Symbol getName() {
    return getMethod().getName();
  }
  public Symbol getSignature() {
    return getMethod().getSignature();
  }
  public Symbol getGenericSignature() {
    return getMethod().getGenericSignature();
  }
  public int getBytecodeOrBPAt(int bci) {
    return getHandle().getJByteAt(bytecodeOffset + bci) & 0xFF;
  }
  public byte getBytecodeByteArg(int bci) {
    return (byte) getBytecodeOrBPAt(bci);
  }
  public short getBytecodeShortArg(int bci) {
    int hi = getBytecodeOrBPAt(bci);
    int lo = getBytecodeOrBPAt(bci + 1);
    return (short) ((hi << 8) | lo);
  }
  public int getBytecodeIntArg(int bci) {
    int b4 = getBytecodeOrBPAt(bci);
    int b3 = getBytecodeOrBPAt(bci + 1);
    int b2 = getBytecodeOrBPAt(bci + 2);
    int b1 = getBytecodeOrBPAt(bci + 3);
    return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
  }
  public byte[] getByteCode() {
     byte[] bc = new byte[ (int) getCodeSize() ];
     for( int i=0; i < bc.length; i++ )
     {
        long offs = bytecodeOffset + i;
        bc[i] = getHandle().getJByteAt( offs );
     }
     return bc;
  }
  public long getObjectSize() {
    return getConstMethodSize() * getHeap().getOopSize();
  }
  public void printValueOn(PrintStream tty) {
    tty.print("ConstMethod " + getName().asString() + getSignature().asString() + "@" + getHandle());
  }
  public void iterateFields(OopVisitor visitor, boolean doVMFields) {
    super.iterateFields(visitor, doVMFields);
    if (doVMFields) {
      visitor.doOop(method, true);
      visitor.doOop(exceptionTable, true);
      visitor.doCInt(constMethodSize, true);
      visitor.doByte(flags, true);
      visitor.doCInt(codeSize, true);
      visitor.doCInt(nameIndex, true);
      visitor.doCInt(signatureIndex, true);
      visitor.doCInt(genericSignatureIndex, true);
      visitor.doCInt(codeSize, true);
    }
  }
  public boolean hasLineNumberTable() {
    return (getFlags() & HAS_LINENUMBER_TABLE) != 0;
  }
  public int getLineNumberFromBCI(int bci) {
    if (!VM.getVM().isCore()) {
      if (bci == DebugInformationRecorder.SYNCHRONIZATION_ENTRY_BCI) bci = 0;
    }
    if (isNative()) {
      return -1;
    }
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(bci == 0 || 0 <= bci && bci < getCodeSize(), "illegal bci");
    }
    int bestBCI  =  0;
    int bestLine = -1;
    if (hasLineNumberTable()) {
      CompressedLineNumberReadStream stream =
        new CompressedLineNumberReadStream(getHandle(), (int) offsetOfCompressedLineNumberTable());
      while (stream.readPair()) {
        if (stream.bci() == bci) {
          return stream.line();
        } else {
          if (stream.bci() < bci && stream.bci() >= bestBCI) {
            bestBCI  = stream.bci();
            bestLine = stream.line();
          }
        }
      }
    }
    return bestLine;
  }
  public LineNumberTableElement[] getLineNumberTable() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(hasLineNumberTable(),
                  "should only be called if table is present");
    }
    int len = getLineNumberTableLength();
    CompressedLineNumberReadStream stream =
      new CompressedLineNumberReadStream(getHandle(), (int) offsetOfCompressedLineNumberTable());
    LineNumberTableElement[] ret = new LineNumberTableElement[len];
    for (int idx = 0; idx < len; idx++) {
      stream.readPair();
      ret[idx] = new LineNumberTableElement(stream.bci(), stream.line());
    }
    return ret;
  }
  public boolean hasLocalVariableTable() {
    return (getFlags() & HAS_LOCALVARIABLE_TABLE) != 0;
  }
  public Symbol getLocalVariableName(int bci, int slot) {
    return getMethod().getLocalVariableName(bci, slot);
  }
  public LocalVariableTableElement[] getLocalVariableTable() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(hasLocalVariableTable(), "should only be called if table is present");
    }
    LocalVariableTableElement[] ret = new LocalVariableTableElement[getLocalVariableTableLength()];
    long offset = offsetOfLocalVariableTable();
    for (int i = 0; i < ret.length; i++) {
      ret[i] = new LocalVariableTableElement(getHandle(), offset);
      offset += localVariableTableElementSize;
    }
    return ret;
  }
  public boolean hasCheckedExceptions() {
    return (getFlags() & HAS_CHECKED_EXCEPTIONS) != 0;
  }
  public CheckedExceptionElement[] getCheckedExceptions() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(hasCheckedExceptions(), "should only be called if table is present");
    }
    CheckedExceptionElement[] ret = new CheckedExceptionElement[getCheckedExceptionsLength()];
    long offset = offsetOfCheckedExceptions();
    for (int i = 0; i < ret.length; i++) {
      ret[i] = new CheckedExceptionElement(getHandle(), offset);
      offset += checkedExceptionElementSize;
    }
    return ret;
  }
  private boolean isNative() {
    return getMethod().isNative();
  }
  private long offsetOfCodeEnd() {
    return bytecodeOffset + getCodeSize();
  }
  private long offsetOfCompressedLineNumberTable() {
    return offsetOfCodeEnd() + (isNative() ? 2 * VM.getVM().getAddressSize() : 0);
  }
  private long offsetOfLastU2Element() {
    return getObjectSize() - 2;
  }
  private long offsetOfCheckedExceptionsLength() {
    return offsetOfLastU2Element();
  }
  private int getCheckedExceptionsLength() {
    if (hasCheckedExceptions()) {
      return (int) getHandle().getCIntegerAt(offsetOfCheckedExceptionsLength(), 2, true);
    } else {
      return 0;
    }
  }
  private long offsetOfCheckedExceptions() {
    long offset = offsetOfCheckedExceptionsLength();
    long length = getCheckedExceptionsLength();
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(length > 0, "should only be called if table is present");
    }
    offset -= length * checkedExceptionElementSize;
    return offset;
  }
  private int getLineNumberTableLength() {
    int len = 0;
    if (hasLineNumberTable()) {
      CompressedLineNumberReadStream stream =
        new CompressedLineNumberReadStream(getHandle(), (int) offsetOfCompressedLineNumberTable());
      while (stream.readPair()) {
        len += 1;
      }
    }
    return len;
  }
  private int getLocalVariableTableLength() {
    if (hasLocalVariableTable()) {
      return (int) getHandle().getCIntegerAt(offsetOfLocalVariableTableLength(), 2, true);
    } else {
      return 0;
    }
  }
  private long offsetOfLocalVariableTableLength() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(hasLocalVariableTable(), "should only be called if table is present");
    }
    if (hasCheckedExceptions()) {
      return offsetOfCheckedExceptions() - 2;
    } else {
      return offsetOfLastU2Element();
    }
  }
  private long offsetOfLocalVariableTable() {
    long offset = offsetOfLocalVariableTableLength();
    long length = getLocalVariableTableLength();
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(length > 0, "should only be called if table is present");
    }
    offset -= length * localVariableTableElementSize;
    return offset;
  }
}
