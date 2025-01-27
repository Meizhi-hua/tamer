public class BasicFloatType extends BasicType implements FloatType {
  public BasicFloatType(String name, int size) {
    this(name, size, 0);
  }
  private BasicFloatType(String name, int size, int cvAttributes) {
    super(name, size, cvAttributes);
  }
  public FloatType asFloat() { return this; }
  public void iterateObject(Address a, ObjectVisitor v, FieldIdentifier f) {
    v.doFloat(f, a.getJFloatAt(0));
  }
  protected Type createCVVariant(int cvAttributes) {
    return new BasicFloatType(getName(), getSize(), cvAttributes);
  }
  public void visit(TypeVisitor v) {
    v.doFloatType(this);
  }
}
