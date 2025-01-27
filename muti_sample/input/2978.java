public class CompiledICHolder extends Oop {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    Type type    = db.lookupType("compiledICHolderOopDesc");
    holderMethod = new OopField(type.getOopField("_holder_method"), 0);
    holderKlass  = new OopField(type.getOopField("_holder_klass"), 0);
    headerSize   = type.getSize();
  }
  CompiledICHolder(OopHandle handle, ObjectHeap heap) {
    super(handle, heap);
  }
  public boolean isCompiledICHolder()  { return true; }
  private static long headerSize;
  private static OopField holderMethod;
  private static OopField holderKlass;
  public Method getHolderMethod() { return (Method) holderMethod.getValue(this); }
  public Klass  getHolderKlass()  { return (Klass)  holderKlass.getValue(this); }
  public void printValueOn(PrintStream tty) {
    tty.print("CompiledICHolder");
  }
  public long getObjectSize() {
    return alignObjectSize(headerSize);
  }
  void iterateFields(OopVisitor visitor, boolean doVMFields) {
    super.iterateFields(visitor, doVMFields);
    if (doVMFields) {
      visitor.doOop(holderMethod, true);
      visitor.doOop(holderKlass, true);
    }
  }
}
