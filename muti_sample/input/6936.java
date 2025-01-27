public class SPARCRegister extends Register {
  private static final int nofRegisters = 32;  
  private static final int GLOBAL_BASE = 0;
  private static final int OUT_BASE    = 8;
  private static final int LOCAL_BASE  = 16;
  private static final int IN_BASE     = 24;
  private static final int LOCAL_SP_WORD_OFFSET = 0;
  private static final int IN_SP_WORD_OFFSET    = 8;
  public SPARCRegister(int number) {
    super(number);
  }
  public SPARCRegister(SPARCRegisterType type, int number) {
    if (type == SPARCRegisterType.GLOBAL) {
      this.number = number + GLOBAL_BASE;
    } else if (type == SPARCRegisterType.OUT) {
      this.number = number + OUT_BASE;
    } else if (type == SPARCRegisterType.LOCAL) {
      this.number = number + LOCAL_BASE;
    } else if (type == SPARCRegisterType.IN) {
      this.number = number + IN_BASE;
    } else {
      throw new IllegalArgumentException("Invalid SPARC register type");
    }
  }
  public int getNumberOfRegisters() {
    return nofRegisters;
  }
  public boolean isIn() {
    return (IN_BASE <= getNumber());
  }
  public boolean isLocal() {
    return (LOCAL_BASE <= getNumber() && getNumber() < IN_BASE);
  }
  public boolean isOut() {
    return (OUT_BASE <= getNumber() && getNumber() < LOCAL_BASE);
  }
  public boolean isGlobal() {
    return (GLOBAL_BASE <= getNumber() && getNumber() < OUT_BASE);
  }
  public SPARCRegister afterSave() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isOut() || isGlobal(), "register not visible after save");
    }
    return isOut() ? new SPARCRegister(getNumber() + (IN_BASE - OUT_BASE)) : this;
  }
  public SPARCRegister afterRestore() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isIn() || isGlobal(), "register not visible after save");
    }
    return isIn() ? new SPARCRegister(getNumber() + (OUT_BASE - IN_BASE)) : this;
  }
  public long spOffsetInSavedWindow() {
    if (isIn()) {
      return VM.getVM().getAddressSize() * (getNumber() - IN_BASE + IN_SP_WORD_OFFSET);
    } else if (isLocal()) {
      return VM.getVM().getAddressSize() * (getNumber() - LOCAL_BASE + LOCAL_SP_WORD_OFFSET);
    }
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isIn() || isLocal(), "only ins and locals are saved in my frame");
    }
    return 0;
  }
  public String toString() {
    return SPARCRegisters.getRegisterName(number);
  }
  public boolean isFramePointer() {
    return number == 30; 
  }
  public boolean isStackPointer() {
    return number == 14; 
  }
  public boolean isFloat() {
    return false;
  }
  public boolean isV9Only() {
    return false;
  }
}
