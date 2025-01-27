public class SPARCCallInstruction extends SPARCInstruction
    implements CallInstruction {
    final private PCRelativeAddress addr;
    public SPARCCallInstruction(PCRelativeAddress addr) {
        super("call");
        this.addr = addr;
    }
    public String asString(long currentPc, SymbolFinder symFinder) {
        long address = addr.getDisplacement() + currentPc;
        StringBuffer buf = new StringBuffer();
        buf.append(getName());
        buf.append(spaces);
        buf.append(symFinder.getSymbolFor(address));
        return buf.toString();
    }
    public Address getBranchDestination() {
        return addr;
    }
    public boolean isCall() {
        return true;
    }
    public boolean isConditional() {
        return false;
    }
}
