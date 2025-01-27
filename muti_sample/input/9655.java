public class StackValue {
  private int       type;
  private OopHandle handleValue;
  private long      integerValue;
  public StackValue() {
    type = BasicType.getTConflict();
  }
  public StackValue(OopHandle h, long scalar_replaced) {
    handleValue = h;
    type = BasicType.getTObject();
    integerValue = scalar_replaced;
    Assert.that(integerValue == 0 || handleValue == null, "not null object should not be marked as scalar replaced");
  }
  public StackValue(long i) {
    integerValue = i;
    type = BasicType.getTInt();
  }
  public int getType() {
    return type;
  }
  public OopHandle getObject() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(type == BasicType.getTObject(), "type check");
    }
    return handleValue;
  }
  boolean objIsScalarReplaced() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(type == BasicType.getTObject(), "type check");
    }
    return integerValue != 0;
  }
  public long getInteger() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(type == BasicType.getTInt(), "type check");
    }
    return integerValue;
  }
  public boolean equals(Object arg) {
    if (arg == null) {
      return false;
    }
    if (!arg.getClass().equals(getClass())) {
      return false;
    }
    StackValue sv = (StackValue) arg;
    if (type != sv.type) {
      return false;
    }
    if (type == BasicType.getTObject()) {
      return handleValue.equals(sv.handleValue);
    } else if (type == BasicType.getTInt()) {
      return (integerValue == sv.integerValue);
    } else {
      return true;
    }
  }
  public int hashCode() {
    if (type == BasicType.getTObject()) {
      return handleValue.hashCode();
    } else {
      return (int) integerValue;
    }
  }
  public void print() {
    printOn(System.out);
  }
  public void printOn(PrintStream tty) {
    if (type == BasicType.getTInt()) {
      tty.print(integerValue + " (long) " + (int) integerValue + " (int)");
    } else if (type == BasicType.getTObject()) {
      tty.print("<" + handleValue + ">");
    } else if (type == BasicType.getTConflict()) {
      tty.print("conflict");
    } else {
      throw new RuntimeException("should not reach here");
    }
  }
}
