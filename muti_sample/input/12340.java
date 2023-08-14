public class BasicCompoundType extends BasicType implements CompoundType {
  private CompoundTypeKind kind;
  private List baseClasses;
  private List fields;
  public BasicCompoundType(String name, int size, CompoundTypeKind kind) {
    this(name, size, kind, 0);
  }
  private BasicCompoundType(String name, int size, CompoundTypeKind kind, int cvAttributes) {
    super(name, size, cvAttributes);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(kind != null, "null kind");
    }
    this.kind = kind;
  }
  public CompoundType asCompound() { return this; }
  public int       getNumBaseClasses() {
    return ((baseClasses == null) ? 0 : baseClasses.size());
  }
  public BaseClass getBaseClass(int i) {
    return (BaseClass) baseClasses.get(i);
  }
  public void addBaseClass(BaseClass b) {
    if (baseClasses == null) {
      baseClasses = new ArrayList();
    }
    baseClasses.add(b);
  }
  public int   getNumFields() {
    return ((fields == null) ? 0 : fields.size());
  }
  public Field getField(int i) {
    return (Field) fields.get(i);
  }
  public void addField(Field f) {
    if (fields == null) {
      fields = new ArrayList();
    }
    fields.add(f);
  }
  public boolean isClass()  { return (kind == CompoundTypeKind.CLASS); }
  public boolean isStruct() { return (kind == CompoundTypeKind.STRUCT); }
  public boolean isUnion()  { return (kind == CompoundTypeKind.UNION); }
  Type resolveTypes(BasicCDebugInfoDataBase db, ResolveListener listener) {
    super.resolveTypes(db, listener);
    if (baseClasses != null) {
      for (Iterator iter = baseClasses.iterator(); iter.hasNext(); ) {
        BasicBaseClass b = (BasicBaseClass) iter.next();
        b.resolveTypes(this, db, listener);
      }
    }
    if (fields != null) {
      for (Iterator iter = fields.iterator(); iter.hasNext(); ) {
        BasicField b = (BasicField) iter.next();
        b.resolveTypes(this, db, listener);
      }
    }
    return this;
  }
  public void iterateObject(Address a, ObjectVisitor v, FieldIdentifier f) {
    if (f == null) {
      for (int i = 0; i < getNumBaseClasses(); i++) {
        BasicCompoundType b = (BasicCompoundType) getBaseClass(i).getType();
        b.iterateObject(a, v, f);
      }
      v.enterType(this, a);
      for (int i = 0; i < getNumFields(); i++) {
        Field field = getField(i);
        BasicType fieldType = (BasicType) field.getType();
        fieldType.iterateObject(a.addOffsetTo(field.getOffset()), v, new BasicNamedFieldIdentifier(field));
      }
      v.exitType();
    } else {
      v.doCompound(f, a);
    }
  }
  protected Type createCVVariant(int cvAttributes) {
    BasicCompoundType t = new BasicCompoundType(getName(), getSize(), kind, cvAttributes);
    t.kind = kind;
    t.baseClasses = baseClasses;
    t.fields = fields;
    return t;
  }
  public void visit(TypeVisitor v) {
    v.doCompoundType(this);
  }
}
