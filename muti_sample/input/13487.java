public class SPARCSpecialLoadInstruction
                        extends SPARCSpecialRegisterInstruction
                        implements  SPARCSpecialRegisters {
    final private int specialReg;
    final private int cregNum;
    final private SPARCRegisterIndirectAddress addr;
    public SPARCSpecialLoadInstruction(String name, int specialReg, int cregNum,
                                             SPARCRegisterIndirectAddress addr) {
        super(name);
        this.specialReg = specialReg;
        this.cregNum = cregNum;
        this.addr = addr;
    }
    public SPARCSpecialLoadInstruction(String name, int specialReg, SPARCRegisterIndirectAddress addr) {
        this(name, specialReg, -1, addr);
    }
    public int getSpecialRegister() {
        return specialReg;
    }
    public int getCoprocessorRegister() {
        if (Assert.ASSERTS_ENABLED)
            Assert.that(specialReg == CREG, "not a coprocesssor register");
        return cregNum;
    }
    public Address getSource() {
        return addr;
    }
    protected String getDescription() {
        StringBuffer buf = new StringBuffer();
        buf.append(getName());
        buf.append(spaces);
        buf.append(addr);
        buf.append(comma);
        if (specialReg == CREG) {
           buf.append("creg" + cregNum);
        } else {
           buf.append(getSpecialRegisterName(specialReg));
        }
        return buf.toString();
    }
}
