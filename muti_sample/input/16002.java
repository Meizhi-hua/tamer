public class MethodKlass extends Klass {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    Type type  = db.lookupType("methodKlass");
    headerSize = type.getSize() + Oop.getHeaderSize();
  }
  MethodKlass(OopHandle handle, ObjectHeap heap) {
    super(handle, heap);
  }
  private static long headerSize;
  public long getObjectSize() { return alignObjectSize(headerSize); }
  public void printValueOn(PrintStream tty) {
    tty.print("MethodKlass");
  }
}
