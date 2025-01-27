public class JmpDecoder extends InstructionDecoder {
    public JmpDecoder(String name, int addrMode1, int operandType1) {
        super(name, addrMode1, operandType1);
    }
    protected Instruction decodeInstruction(byte[] bytesArray, boolean operandSize, boolean addrSize, X86InstructionFactory factory) {
        Operand operand = getOperand1(bytesArray, operandSize, addrSize);
        int size = byteIndex - instrStartIndex;
        Address address;
        if (operand instanceof X86Register) {
            address = new X86RegisterDirectAddress((X86Register)operand);
        } else {
            address = (Address) operand;
        }
        return factory.newJmpInstruction(name, address, size, prefixes);
    }
}
