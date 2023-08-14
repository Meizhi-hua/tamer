public class SafepointBlob extends SingletonBlob {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static void initialize(TypeDataBase db) {
    Type type = db.lookupType("SafepointBlob");
  }
  public SafepointBlob(Address addr) {
    super(addr);
  }
  public boolean isSafepointStub() {
    return true;
  }
}