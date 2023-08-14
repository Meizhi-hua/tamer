public final class Form22t extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form22t();
    private Form22t() {
    }
    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return regs.get(0).regString() + ", " + regs.get(1).regString() +
            ", " + branchString(insn);
    }
    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        return branchComment(insn);
    }
    @Override
    public int codeSize() {
        return 2;
    }
    @Override
    public boolean isCompatible(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        if (!((insn instanceof TargetInsn) &&
              (regs.size() == 2) &&
              unsignedFitsInNibble(regs.get(0).getReg()) &&
              unsignedFitsInNibble(regs.get(1).getReg()))) {
            return false;
        }
        TargetInsn ti = (TargetInsn) insn;
        return ti.hasTargetOffset() ? branchFits(ti) : true;
    }
    @Override
    public boolean branchFits(TargetInsn insn) {
        int offset = insn.getTargetOffset();
        return (offset != 0) && signedFitsInShort(offset);
    }
    @Override
    public InsnFormat nextUp() {
        return null;
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int offset = ((TargetInsn) insn).getTargetOffset();
        write(out,
              opcodeUnit(insn,
                         makeByte(regs.get(0).getReg(), regs.get(1).getReg())),
              (short) offset);
    }
}
