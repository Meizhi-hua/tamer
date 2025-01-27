class V9SpecialStoreDecoder extends MemoryInstructionDecoder
               implements V9InstructionDecoder {
    V9SpecialStoreDecoder(int op3) {
        super(op3, "st[x]fsr", RTLDT_UNKNOWN);
    }
    Instruction decodeMemoryInstruction(int instruction,
                               SPARCRegisterIndirectAddress addr,
                               SPARCRegister rd, SPARCInstructionFactory factory) {
        return factory.newSpecialStoreInstruction(rd == SPARCRegisters.G0? "st" : "stx",
                                                SPARCSpecialRegisters.FSR, -1,
                                                addr);
    }
}
