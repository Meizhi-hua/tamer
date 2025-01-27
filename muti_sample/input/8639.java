public class SPARCV9WriteInstruction extends SPARCV9SpecialRegisterInstruction {
    final private int specialReg;
    final private int asrRegNum;
    final private SPARCRegister rs1;
    final private ImmediateOrRegister operand2;
    public SPARCV9WriteInstruction(int specialReg, int asrRegNum, SPARCRegister rs1, ImmediateOrRegister operand2)  {
        super("wr");
        this.specialReg = specialReg;
        this.asrRegNum = asrRegNum;
        this.rs1 = rs1;
        this.operand2 = operand2;
    }
    public int getSpecialRegister() {
        return specialReg;
    }
    public int getAncillaryRegister() {
        if (Assert.ASSERTS_ENABLED)
            Assert.that(specialReg == ASR, "not an ancillary register");
        return asrRegNum;
    }
    protected String getDescription() {
        StringBuffer buf = new StringBuffer();
        buf.append(getName());
        buf.append(spaces);
        buf.append(rs1.toString());
        buf.append(comma);
        if (operand2.isRegister()) {
            buf.append(operand2.toString());
        } else {
            Number number = ((Immediate)operand2).getNumber();
            buf.append("0x");
            buf.append(Integer.toHexString(number.intValue()));
        }
        buf.append(comma);
        if(specialReg == ASR)
            buf.append("%asr" + asrRegNum);
        else
            buf.append(getSpecialRegisterName(specialReg));
        return buf.toString();
    }
}
