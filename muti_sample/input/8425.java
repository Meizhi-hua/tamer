public class BasicMemberFunctionType extends BasicFunctionType implements MemberFunctionType {
  private Type containingClass;
  private Type thisType;
  private long thisAdjust;
  public BasicMemberFunctionType(String name,
                                 int size,
                                 Type returnType,
                                 Type containingClass,
                                 Type thisType,
                                 long thisAdjust) {
    this(name, size, returnType, containingClass, thisType, thisAdjust, 0);
  }
  private BasicMemberFunctionType(String name,
                                  int size,
                                  Type returnType,
                                  Type containingClass,
                                  Type thisType,
                                  long thisAdjust,
                                  int cvAttributes) {
    super(name, size, returnType, cvAttributes);
    this.containingClass = containingClass;
    this.thisType        = thisType;
    this.thisAdjust      = thisAdjust;
  }
  public MemberFunctionType asMemberFunction() { return this; }
  public Type getContainingClass()       { return containingClass; }
  public Type getThisType()              { return thisType; }
  public long getThisAdjust()            { return thisAdjust; }
  Type resolveTypes(BasicCDebugInfoDataBase db, ResolveListener listener) {
    super.resolveTypes(db, listener);
    containingClass = db.resolveType(this, containingClass, listener, "resolving member function class");
    thisType        = db.resolveType(this, thisType,        listener, "resolving member function \"this\" type");
    return this;
  }
  public void iterateObject(Address a, ObjectVisitor v, FieldIdentifier f) {
  }
  protected Type createCVVariant(int cvAttributes) {
    return new BasicMemberFunctionType(getName(),
                                       getSize(),
                                       getReturnType(),
                                       getContainingClass(),
                                       getThisType(),
                                       getThisAdjust(),
                                       cvAttributes);
  }
  public void visit(TypeVisitor v) {
    v.doMemberFunctionType(this);
  }
}
