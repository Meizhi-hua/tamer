class V9WriteDecoder extends InstructionDecoder
              implements V9InstructionDecoder {
    Instruction decode(int instruction, SPARCInstructionFactory factory) {
        SPARCV9InstructionFactory v9factory = (SPARCV9InstructionFactory) factory;
        Instruction instr = null;
        int specialRegNum = getDestinationRegister(instruction);
        if (specialRegNum == 1 || specialRegNum == 4 || specialRegNum == 5
            || (specialRegNum > 6 && specialRegNum < 15)) {
            instr = v9factory.newIllegalInstruction(instruction);
        } else {
            int rs1Num = getSourceRegister1(instruction);
            if (specialRegNum == 15) {
                if (isIBitSet(instruction) && rs1Num == 0) {
                    instr = v9factory.newV9SirInstruction();
                } else {
                    instr = v9factory.newIllegalInstruction(instruction);
                }
            } else {
                int asrRegNum = -1;
                if (specialRegNum > 15) {
                    asrRegNum = specialRegNum;
                    specialRegNum = SPARCV9SpecialRegisters.ASR;
                }
                SPARCRegister rs1 = SPARCRegisters.getRegister(rs1Num);
                instr = v9factory.newV9WriteInstruction(specialRegNum, asrRegNum, rs1, getOperand2(instruction));
            }
        }
        return instr;
    }
}
