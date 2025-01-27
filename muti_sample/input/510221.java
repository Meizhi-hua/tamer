public final class Form31c extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form31c();
    private Form31c() {
    }
    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return regs.get(0).regString() + ", " + cstString(insn);
    }
    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        if (noteIndices) {
            return cstComment(insn);
        } else {
            return "";
        }
    }
    @Override
    public int codeSize() {
        return 3;
    }
    @Override
    public boolean isCompatible(DalvInsn insn) {
        if (!(insn instanceof CstInsn)) {
            return false;
        }
        RegisterSpecList regs = insn.getRegisters();
        RegisterSpec reg;
        switch (regs.size()) {
            case 1: {
                reg = regs.get(0);
                break;
            }
            case 2: {
                reg = regs.get(0);
                if (reg.getReg() != regs.get(1).getReg()) {
                    return false;
                }
                break;
            }
            default: {
                return false;
            }
        }
        if (!unsignedFitsInByte(reg.getReg())) {
            return false;
        }
        CstInsn ci = (CstInsn) insn;
        Constant cst = ci.getConstant();
        return ((cst instanceof CstType) ||
                (cst instanceof CstFieldRef) ||
                (cst instanceof CstString));
    }
    @Override
    public InsnFormat nextUp() {
        return null;
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int cpi = ((CstInsn) insn).getIndex();
        write(out,
                opcodeUnit(insn, regs.get(0).getReg()),
                (short) cpi,
                (short) (cpi >> 16));
    }
}
