public final class Form30t extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form30t();
    private Form30t() {
    }
    @Override
    public String insnArgString(DalvInsn insn) {
        return branchString(insn);
    }
    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        return branchComment(insn);
    }
    @Override
    public int codeSize() {
        return 3;
    }
    @Override
    public boolean isCompatible(DalvInsn insn) {
        if (!((insn instanceof TargetInsn) &&
              (insn.getRegisters().size() == 0))) {
            return false;
        }
        return true;
    }
    @Override
    public boolean branchFits(TargetInsn insn) {
        return true;
    }
    @Override
    public InsnFormat nextUp() {
        return null;
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        int offset = ((TargetInsn) insn).getTargetOffset();
        write(out, opcodeUnit(insn, 0),
                (short) offset,
                (short) (offset >> 16));
    }
}
