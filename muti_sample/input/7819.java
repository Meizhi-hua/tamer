public class Oop {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    Type type  = db.lookupType("oopDesc");
    mark       = new CIntField(type.getCIntegerField("_mark"), 0);
    klass      = new OopField(type.getOopField("_metadata._klass"), 0);
    compressedKlass  = new NarrowOopField(type.getOopField("_metadata._compressed_klass"), 0);
    headerSize = type.getSize();
  }
  private OopHandle  handle;
  private ObjectHeap heap;
  Oop(OopHandle handle, ObjectHeap heap) {
    this.handle = handle;
    this.heap   = heap;
  }
  ObjectHeap getHeap()   { return heap; }
  public OopHandle getHandle() { return handle; }
  private static long headerSize;
  public  static long getHeaderSize() { return headerSize; } 
  private static CIntField mark;
  private static OopField  klass;
  private static NarrowOopField compressedKlass;
  public boolean isShared() {
    return CompactingPermGenGen.isShared(handle);
  }
  public boolean isSharedReadOnly() {
    return CompactingPermGenGen.isSharedReadOnly(handle);
  }
  public boolean isSharedReadWrite() {
    return CompactingPermGenGen.isSharedReadWrite(handle);
  }
  public Mark  getMark()   { return new Mark(getHandle()); }
  public Klass getKlass() {
    if (VM.getVM().isCompressedOopsEnabled()) {
      return (Klass) compressedKlass.getValue(this);
    } else {
      return (Klass) klass.getValue(this);
    }
  }
  public boolean isA(Klass k) {
    return getKlass().isSubtypeOf(k);
  }
  public long getObjectSize() {
    Klass k = getKlass();
    return ((InstanceKlass)k).getObjectSize(this);
  }
  public boolean isInstance()          { return false; }
  public boolean isInstanceRef()       { return false; }
  public boolean isArray()             { return false; }
  public boolean isObjArray()          { return false; }
  public boolean isTypeArray()         { return false; }
  public boolean isSymbol()            { return false; }
  public boolean isKlass()             { return false; }
  public boolean isThread()            { return false; }
  public boolean isMethod()            { return false; }
  public boolean isMethodData()        { return false; }
  public boolean isConstantPool()      { return false; }
  public boolean isConstantPoolCache() { return false; }
  public boolean isCompiledICHolder()  { return false; }
  public static long alignObjectSize(long size) {
    return VM.getVM().alignUp(size, VM.getVM().getMinObjAlignmentInBytes());
  }
  public static long alignObjectOffset(long offset) {
    return VM.getVM().alignUp(offset, VM.getVM().getBytesPerLong());
  }
  public boolean equals(Object obj) {
    if (obj != null && (obj instanceof Oop)) {
      return getHandle().equals(((Oop) obj).getHandle());
    }
    return false;
 }
  public int hashCode() { return getHandle().hashCode(); }
  public long identityHash() {
    Mark mark = getMark();
    if (mark.isUnlocked() && (!mark.hasNoHash())) {
      return (int) mark.hash();
    } else if (mark.isMarked()) {
      return (int) mark.hash();
    } else {
      return slowIdentityHash();
    }
  }
  public long slowIdentityHash() {
    return VM.getVM().getObjectSynchronizer().identityHashValueFor(this);
  }
  public void iterate(OopVisitor visitor, boolean doVMFields) {
    visitor.setObj(this);
    visitor.prologue();
    iterateFields(visitor, doVMFields);
    visitor.epilogue();
  }
  void iterateFields(OopVisitor visitor, boolean doVMFields) {
    if (doVMFields) {
      visitor.doCInt(mark, true);
      if (VM.getVM().isCompressedOopsEnabled()) {
        visitor.doOop(compressedKlass, true);
      } else {
        visitor.doOop(klass, true);
      }
    }
  }
  public void print()      { printOn(System.out); }
  public void printValue() { printValueOn(System.out); }
  public void printRaw()   { printRawOn(System.out); }
  public static void printOopValueOn(Oop obj, PrintStream tty) {
    if (obj == null) {
      tty.print("null");
    } else {
      obj.printValueOn(tty);
      tty.print(" @ " + obj.getHandle());
    }
  }
  public static void printOopAddressOn(Oop obj, PrintStream tty) {
    if (obj == null) {
      tty.print("null");
    } else {
      tty.print(obj.getHandle().toString());
    }
  }
  public void printOn(PrintStream tty) {
    OopPrinter printer = new OopPrinter(tty);
    iterate(printer, true);
  }
  public void printValueOn(PrintStream tty) {
    try {
      tty.print("Oop for " + getKlass().getName().asString());
    } catch (java.lang.NullPointerException e) {
      tty.print("Oop");
    }
  }
  public void printRawOn(PrintStream tty) {
    tty.print("Dumping raw memory for ");
    printValueOn(tty);
    tty.println();
    long size = getObjectSize() * 4;
    for (long i = 0; i < size; i += 4) {
      long memVal = getHandle().getCIntegerAt(i, 4, true);
      tty.println(Long.toHexString(memVal));
    }
  }
  public boolean verify() { return true;}
  static OopHandle getKlassForOopHandle(OopHandle handle) {
    if (handle == null) {
      return null;
    }
    if (VM.getVM().isCompressedOopsEnabled()) {
      return handle.getCompOopHandleAt(compressedKlass.getOffset());
    } else {
      return handle.getOopHandleAt(klass.getOffset());
    }
  }
};
