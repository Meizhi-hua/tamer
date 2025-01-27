public class X86ArithmeticInstruction extends X86Instruction
                                        implements ArithmeticInstruction {
   final private int operation; 
   final private Operand operand1;
   final private Operand operand2;
   final private Operand operand3;
   final private String description;
   public X86ArithmeticInstruction(String name, int operation, Operand op1, Operand op2, int size, int prefixes) {
      super(name, size, prefixes);
      this.operation = operation;
      this.operand1 = op1;
      this.operand2 = op2;
      this.operand3 = null;
      description = initDescription();
   }
   public X86ArithmeticInstruction(String name, int operation, Operand op1, Operand op2, Operand op3, int size, int prefixes) {
      super(name, size, prefixes);
      this.operation = operation;
      this.operand1 = op1;
      this.operand2 = op2;
      this.operand3 = op3;
      description = initDescription();
   }
   protected String initDescription() {
      StringBuffer buf = new StringBuffer();
      buf.append(getPrefixString());
      buf.append(getName());
      buf.append(spaces);
      if (operand1 != null) {
         buf.append(getOperandAsString(operand1));
      }
      if (operand2 != null) {
         buf.append(comma);
         buf.append(getOperandAsString(operand2));
      }
      if(operand3 != null) {
         buf.append(comma);
         buf.append(getOperandAsString(operand3));
      }
      return buf.toString();
   }
   public String asString(long currentPc, SymbolFinder symFinder) {
      return description;
   }
   public Operand getArithmeticDestination() {
      return operand1;
   }
   public Operand getOperand1() {
      return operand1;
   }
   public Operand getOperand2() {
      return operand2;
   }
   public Operand getOperand3() {
      return operand3;
   }
   public Operand[] getArithmeticSources() {
      return (new Operand[] { operand1, operand2, operand3 });
   }
   public int getOperation() {
      return operation;
   }
   public boolean isArithmetic() {
      return true;
   }
}
