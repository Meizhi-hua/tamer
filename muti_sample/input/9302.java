public class SPARCInstructionFactoryImpl implements SPARCInstructionFactory {
    public SPARCInstruction newCallInstruction(PCRelativeAddress addr) {
        return new SPARCCallInstruction(addr);
    }
    public SPARCInstruction newNoopInstruction() {
        return new SPARCNoopInstruction();
    }
    public SPARCInstruction newSethiInstruction(int imm22, SPARCRegister rd) {
        return new SPARCSethiInstruction(imm22, rd);
    }
    public SPARCInstruction newUnimpInstruction(int const22) {
        return new SPARCUnimpInstruction(const22);
    }
    public SPARCInstruction newBranchInstruction(String name, PCRelativeAddress addr, boolean isAnnuled, int conditionCode) {
        return new SPARCBranchInstruction(name, addr, isAnnuled, conditionCode);
    }
    public SPARCInstruction newSpecialLoadInstruction(String name, int specialReg, int cregNum,
                                      SPARCRegisterIndirectAddress addr) {
        return new SPARCSpecialLoadInstruction(name, specialReg, cregNum, addr);
    }
    public SPARCInstruction newSpecialStoreInstruction(String name, int specialReg, int cregNum,
                                      SPARCRegisterIndirectAddress addr) {
        return new SPARCSpecialStoreInstruction(name, specialReg, cregNum, addr);
    }
    public SPARCInstruction newLoadInstruction(String name, int opcode,
                                  SPARCRegisterIndirectAddress addr, SPARCRegister rd,
                                  int dataType) {
        return new SPARCLoadInstruction(name, opcode, addr, rd, dataType);
    }
    public SPARCInstruction newStoreInstruction(String name, int opcode,
                                  SPARCRegisterIndirectAddress addr, SPARCRegister rd,
                                  int dataType) {
        return new SPARCStoreInstruction(name, opcode, addr, rd, dataType);
    }
    public SPARCInstruction newStbarInstruction() {
        return new SPARCStbarInstruction();
    }
    public SPARCInstruction newReadInstruction(int specialReg, int asrRegNum, SPARCRegister rd) {
        return new SPARCReadInstruction(specialReg, asrRegNum, rd);
    }
    public SPARCInstruction newWriteInstruction(int specialReg, int asrRegNum, SPARCRegister rs1,
                                             ImmediateOrRegister operand2) {
        return new SPARCWriteInstruction(specialReg, asrRegNum, rs1,operand2);
    }
    public SPARCInstruction newIllegalInstruction(int instruction) {
        return new SPARCIllegalInstruction(instruction);
    }
    public SPARCInstruction newIndirectCallInstruction(SPARCRegisterIndirectAddress addr,
                                  SPARCRegister rd) {
        return new SPARCIndirectCallInstruction(addr, rd);
    }
    public SPARCInstruction newReturnInstruction(SPARCRegisterIndirectAddress addr,
                                  SPARCRegister rd, boolean isLeaf) {
        return new SPARCReturnInstruction(addr, rd, isLeaf);
    }
    public SPARCInstruction newJmplInstruction(SPARCRegisterIndirectAddress addr,
                                  SPARCRegister rd) {
        return new SPARCJmplInstruction(addr, rd);
    }
    public SPARCInstruction newFPArithmeticInstruction(String name, int opf, int rtlOperation,
                                  SPARCFloatRegister rs1, SPARCFloatRegister rs2,
                                  SPARCFloatRegister rd) {
        return new SPARCFPArithmeticInstruction(name, opf, rtlOperation, rs1, rs2, rd);
    }
    public SPARCInstruction newFPMoveInstruction(String name, int opf, SPARCFloatRegister rs, SPARCFloatRegister rd) {
        return new SPARCFPMoveInstruction(name, opf, rs, rd);
    }
    public SPARCInstruction newFP2RegisterInstruction(String name, int opf, SPARCFloatRegister rs, SPARCFloatRegister rd) {
        return new SPARCFP2RegisterInstruction(name, opf, rs, rd);
    }
    public SPARCInstruction newFlushInstruction(SPARCRegisterIndirectAddress addr) {
        return new SPARCFlushInstruction(addr);
    }
    public SPARCInstruction newSaveInstruction(SPARCRegister rs1, ImmediateOrRegister operand2, SPARCRegister rd) {
        return new SPARCSaveInstruction(rs1, operand2, rd);
    }
    public SPARCInstruction newRestoreInstruction(SPARCRegister rs1, ImmediateOrRegister operand2, SPARCRegister rd) {
        return new SPARCRestoreInstruction(rs1, operand2, rd);
    }
    public SPARCInstruction newTrapInstruction(String name, int conditionCode) {
        return new SPARCTrapInstruction(name, conditionCode);
    }
    public SPARCInstruction newRettInstruction(SPARCRegisterIndirectAddress addr) {
        return new SPARCRettInstruction(addr);
    }
    public SPARCInstruction newArithmeticInstruction(String name, int opcode, int rtlOperation,
                                                     SPARCRegister rs1, ImmediateOrRegister operand2,
                                                     SPARCRegister rd) {
        return new SPARCArithmeticInstruction(name, opcode, rtlOperation, rs1, operand2, rd);
    }
    public SPARCInstruction newLogicInstruction(String name, int opcode, int rtlOperation,
                                                     SPARCRegister rs1, ImmediateOrRegister operand2,
                                                     SPARCRegister rd) {
        return new SPARCLogicInstruction(name, opcode, rtlOperation, rs1, operand2, rd);
    }
    public SPARCInstruction newMoveInstruction(String name, int opcode,
                                               ImmediateOrRegister operand2,
                                               SPARCRegister rd) {
        return new SPARCMoveInstruction(name, opcode, operand2, rd);
    }
    public SPARCInstruction newShiftInstruction(String name, int opcode, int rtlOperation,
                                                     SPARCRegister rs1, ImmediateOrRegister operand2,
                                                     SPARCRegister rd) {
        return new SPARCShiftInstruction(name, opcode, rtlOperation, rs1, operand2, rd);
    }
    public SPARCInstruction newCoprocessorInstruction(int instruction, int cpopcode, int opcode,
                                                     int rs1Num, int rs2Num, int rd) {
        return new SPARCIllegalInstruction(instruction);
    }
    public SPARCInstruction newSwapInstruction(String name, SPARCRegisterIndirectAddress addr, SPARCRegister rd) {
       return new SPARCSwapInstruction(name, addr, rd);
    }
    public SPARCInstruction newLdstubInstruction(String name, SPARCRegisterIndirectAddress addr, SPARCRegister rd) {
       return new SPARCLdstubInstruction(name, addr, rd);
    }
}
