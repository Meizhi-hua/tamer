public class BasicBitType extends BasicIntType implements BitType {
  private Type underlyingType;
  private int sizeInBits;
  private int offset;
  public BasicBitType(Type underlyingType, int sizeInBits, int lsbOffset) {
    this(underlyingType, sizeInBits, lsbOffset, 0);
  }
  private BasicBitType(Type underlyingType, int sizeInBits, int lsbOffset, int cvAttributes) {
    super(null, 0, false, cvAttributes);
    this.underlyingType = underlyingType;
    this.sizeInBits = sizeInBits;
    this.offset = lsbOffset;
  }
  public BitType asBit() { return this; }
  public int     getSize() { return underlyingType.getSize(); }
  public boolean isUnsigned() {
    if (underlyingType.isInt()) {
      return ((IntType) underlyingType).isUnsigned();
    }
    return false;
  }
  public int getSizeInBits() {
    return sizeInBits;
  }
  public int getOffset() {
    return offset;
  }
  Type resolveTypes(BasicCDebugInfoDataBase db, ResolveListener listener) {
    super.resolveTypes(db, listener);
    underlyingType = db.resolveType(this, underlyingType, listener, "resolving bit type");
    setName(underlyingType.getName());
    if (Assert.ASSERTS_ENABLED) {
      BasicType b = (BasicType) underlyingType;
      Assert.that(b.isLazy() || b.isInt(),
                  "Underlying type of bitfield must be integer type (or unresolved due to error)");
    }
    return this;
  }
  public void iterateObject(Address a, ObjectVisitor v, FieldIdentifier f) {
    long mask = maskFor(sizeInBits);
    long val = ((a.getCIntegerAt(0, getSize(), isUnsigned())) >> getOffset()) & mask;
    if (!isUnsigned()) {
      if ((val & highBit(sizeInBits)) != 0) {
        val = val | (~mask);
      }
    }
    v.doBit(f, val);
  }
  protected Type createCVVariant(int cvAttributes) {
    return new BasicBitType(underlyingType, getSizeInBits(), getOffset(), cvAttributes);
  }
  public void visit(TypeVisitor v) {
    v.doBitType(this);
  }
  private static long maskFor(int sizeInBits) {
    return ((1 << sizeInBits) - 1);
  }
  private static long highBit(int sizeInBits) {
    return (1 << (sizeInBits - 1));
  }
}
