public abstract class ZeroSizeInsn extends DalvInsn {
    public ZeroSizeInsn(SourcePosition position) {
        super(Dops.SPECIAL_FORMAT, position, RegisterSpecList.EMPTY);
    }
    @Override
    public final int codeSize() {
        return 0;
    }
    @Override
    public final void writeTo(AnnotatedOutput out) {
    }
    @Override
    public final DalvInsn withOpcode(Dop opcode) {
        throw new RuntimeException("unsupported");
    }
    @Override
    public DalvInsn withRegisterOffset(int delta) {
        return withRegisters(getRegisters().withOffset(delta));
    }
}
