public class GenCollectedHeap extends SharedHeap {
  private static CIntegerField nGensField;
  private static long gensOffset;
  private static AddressField genSpecsField;
  private static GenerationFactory genFactory;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("GenCollectedHeap");
    nGensField = type.getCIntegerField("_n_gens");
    gensOffset = type.getField("_gens").getOffset();
    genSpecsField = type.getAddressField("_gen_specs");
    genFactory = new GenerationFactory();
  }
  public GenCollectedHeap(Address addr) {
    super(addr);
  }
  public int nGens() {
    return (int) nGensField.getValue(addr);
  }
  public Generation getGen(int i) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that((i >= 0) && (i < nGens()), "Index " + i +
                  " out of range (should be between 0 and " + nGens() + ")");
    }
    if ((i < 0) || (i >= nGens())) {
      return null;
    }
    Address genAddr = addr.getAddressAt(gensOffset +
                                        (i * VM.getVM().getAddressSize()));
    return genFactory.newObject(addr.getAddressAt(gensOffset +
                                                  (i * VM.getVM().getAddressSize())));
  }
  public boolean isIn(Address a) {
    for (int i = 0; i < nGens(); i++) {
      Generation gen = getGen(i);
      if (gen.isIn(a)) {
        return true;
      }
    }
    return permGen().isIn(a);
  }
  public long capacity() {
    long capacity = 0;
    for (int i = 0; i < nGens(); i++) {
      capacity += getGen(i).capacity();
    }
    return capacity;
  }
  public long used() {
    long used = 0;
    for (int i = 0; i < nGens(); i++) {
      used += getGen(i).used();
    }
    return used;
  }
  GenerationSpec spec(int level) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that((level >= 0) && (level < nGens()), "Index " + level +
                  " out of range (should be between 0 and " + nGens() + ")");
    }
    if ((level < 0) || (level >= nGens())) {
      return null;
    }
    Address ptrList = genSpecsField.getValue(addr);
    if (ptrList == null) {
      return null;
    }
    return (GenerationSpec)
      VMObjectFactory.newObject(GenerationSpec.class,
                                ptrList.getAddressAt(level * VM.getVM().getAddressSize()));
  }
  public CollectedHeapName kind() {
    return CollectedHeapName.GEN_COLLECTED_HEAP;
  }
  public void printOn(PrintStream tty) {
    for (int i = 0; i < nGens(); i++) {
      tty.print("Gen " + i + ": ");
      getGen(i).printOn(tty);
      tty.println("Invocations: " + getGen(i).invocations());
      tty.println();
    }
    permGen().printOn(tty);
    tty.println("Invocations: " + permGen().invocations());
  }
}
