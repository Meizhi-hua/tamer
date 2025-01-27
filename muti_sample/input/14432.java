public class X86FPStoreInstruction extends X86FPInstruction {
   final private Operand dest;
   public X86FPStoreInstruction(String name, Operand op, int size, int prefixes) {
      super(name, size, prefixes);
      this.dest = op;
   }
   public String asString(long currentPc, SymbolFinder symFinder) {
      StringBuffer buf = new StringBuffer();
      buf.append(getPrefixString());
      buf.append(getName());
      buf.append(spaces);
      buf.append(dest.toString());
      return buf.toString();
   }
}
