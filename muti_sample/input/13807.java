public class SSELogicalDecoder extends SSEInstructionDecoder {
   private int rtlOperation;
   public SSELogicalDecoder(String name, int addrMode1, int operandType1, int addrMode2,
                                                        int operandType2, int rtlOperation) {
      super(name, addrMode1, operandType1, addrMode2, operandType2);
      this.rtlOperation = rtlOperation;
   }
   protected Instruction decodeInstruction(byte[] bytesArray, boolean operandSize,
                                           boolean addrSize, X86InstructionFactory factory) {
      Operand op1 = getOperand1(bytesArray, operandSize, addrSize);
      Operand op2 = getOperand2(bytesArray, operandSize, addrSize);
      int size = byteIndex - instrStartIndex;
      return factory.newLogicInstruction(name, rtlOperation, op1, op2, size, prefixes);
   }
}
