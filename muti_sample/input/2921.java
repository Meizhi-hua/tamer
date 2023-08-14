public class PCDesc extends VMObject {
  private static CIntegerField pcOffsetField;
  private static CIntegerField scopeDecodeOffsetField;
  private static CIntegerField objDecodeOffsetField;
  private static CIntegerField pcFlagsField;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static void initialize(TypeDataBase db) {
    Type type = db.lookupType("PcDesc");
    pcOffsetField          = type.getCIntegerField("_pc_offset");
    scopeDecodeOffsetField = type.getCIntegerField("_scope_decode_offset");
    objDecodeOffsetField   = type.getCIntegerField("_obj_decode_offset");
    pcFlagsField           = type.getCIntegerField("_flags");
  }
  public PCDesc(Address addr) {
    super(addr);
  }
  public int getPCOffset() {
    return (int) pcOffsetField.getValue(addr);
  }
  public int getScopeDecodeOffset() {
    return ((int) scopeDecodeOffsetField.getValue(addr));
  }
  public int getObjDecodeOffset() {
    return ((int) objDecodeOffsetField.getValue(addr));
  }
  public Address getRealPC(NMethod code) {
    return code.codeBegin().addOffsetTo(getPCOffset());
  }
  public boolean getReexecute() {
    int flags = (int)pcFlagsField.getValue(addr);
    return ((flags & 0x1)== 1); 
  }
  public void print(NMethod code) {
    printOn(System.out, code);
  }
  public void printOn(PrintStream tty, NMethod code) {
    tty.println("PCDesc(" + getRealPC(code) + "):");
    for (ScopeDesc sd = code.getScopeDescAt(getRealPC(code));
         sd != null;
         sd = sd.sender()) {
      tty.print(" ");
      sd.getMethod().printValueOn(tty);
      tty.print("  @" + sd.getBCI());
      tty.print("  reexecute=" + sd.getReexecute());
      tty.println();
    }
  }
}