public class InstanceKlass extends Klass {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  public static int ACCESS_FLAGS_OFFSET;
  public static int NAME_INDEX_OFFSET;
  public static int SIGNATURE_INDEX_OFFSET;
  public static int INITVAL_INDEX_OFFSET;
  public static int LOW_OFFSET;
  public static int HIGH_OFFSET;
  public static int GENERIC_SIGNATURE_INDEX_OFFSET;
  public static int NEXT_OFFSET;
  public static int IMPLEMENTORS_LIMIT;
  private static int CLASS_STATE_UNPARSABLE_BY_GC;
  private static int CLASS_STATE_ALLOCATED;
  private static int CLASS_STATE_LOADED;
  private static int CLASS_STATE_LINKED;
  private static int CLASS_STATE_BEING_INITIALIZED;
  private static int CLASS_STATE_FULLY_INITIALIZED;
  private static int CLASS_STATE_INITIALIZATION_ERROR;
  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    Type type            = db.lookupType("instanceKlass");
    arrayKlasses         = new OopField(type.getOopField("_array_klasses"), Oop.getHeaderSize());
    methods              = new OopField(type.getOopField("_methods"), Oop.getHeaderSize());
    methodOrdering       = new OopField(type.getOopField("_method_ordering"), Oop.getHeaderSize());
    localInterfaces      = new OopField(type.getOopField("_local_interfaces"), Oop.getHeaderSize());
    transitiveInterfaces = new OopField(type.getOopField("_transitive_interfaces"), Oop.getHeaderSize());
    nofImplementors      = new CIntField(type.getCIntegerField("_nof_implementors"), Oop.getHeaderSize());
    IMPLEMENTORS_LIMIT   = db.lookupIntConstant("instanceKlass::implementors_limit").intValue();
    implementors         = new OopField[IMPLEMENTORS_LIMIT];
    for (int i = 0; i < IMPLEMENTORS_LIMIT; i++) {
      long arrayOffset = Oop.getHeaderSize() + (i * db.getAddressSize());
      implementors[i]    = new OopField(type.getOopField("_implementors[0]"), arrayOffset);
    }
    fields               = new OopField(type.getOopField("_fields"), Oop.getHeaderSize());
    constants            = new OopField(type.getOopField("_constants"), Oop.getHeaderSize());
    classLoader          = new OopField(type.getOopField("_class_loader"), Oop.getHeaderSize());
    protectionDomain     = new OopField(type.getOopField("_protection_domain"), Oop.getHeaderSize());
    signers              = new OopField(type.getOopField("_signers"), Oop.getHeaderSize());
    sourceFileName       = type.getAddressField("_source_file_name");
    sourceDebugExtension = type.getAddressField("_source_debug_extension");
    innerClasses         = new OopField(type.getOopField("_inner_classes"), Oop.getHeaderSize());
    nonstaticFieldSize   = new CIntField(type.getCIntegerField("_nonstatic_field_size"), Oop.getHeaderSize());
    staticFieldSize      = new CIntField(type.getCIntegerField("_static_field_size"), Oop.getHeaderSize());
    staticOopFieldCount   = new CIntField(type.getCIntegerField("_static_oop_field_count"), Oop.getHeaderSize());
    nonstaticOopMapSize  = new CIntField(type.getCIntegerField("_nonstatic_oop_map_size"), Oop.getHeaderSize());
    isMarkedDependent    = new CIntField(type.getCIntegerField("_is_marked_dependent"), Oop.getHeaderSize());
    initState            = new CIntField(type.getCIntegerField("_init_state"), Oop.getHeaderSize());
    vtableLen            = new CIntField(type.getCIntegerField("_vtable_len"), Oop.getHeaderSize());
    itableLen            = new CIntField(type.getCIntegerField("_itable_len"), Oop.getHeaderSize());
    breakpoints          = type.getAddressField("_breakpoints");
    genericSignature     = type.getAddressField("_generic_signature");
    majorVersion         = new CIntField(type.getCIntegerField("_major_version"), Oop.getHeaderSize());
    minorVersion         = new CIntField(type.getCIntegerField("_minor_version"), Oop.getHeaderSize());
    headerSize           = alignObjectOffset(Oop.getHeaderSize() + type.getSize());
    ACCESS_FLAGS_OFFSET = db.lookupIntConstant("instanceKlass::access_flags_offset").intValue();
    NAME_INDEX_OFFSET = db.lookupIntConstant("instanceKlass::name_index_offset").intValue();
    SIGNATURE_INDEX_OFFSET = db.lookupIntConstant("instanceKlass::signature_index_offset").intValue();
    INITVAL_INDEX_OFFSET = db.lookupIntConstant("instanceKlass::initval_index_offset").intValue();
    LOW_OFFSET = db.lookupIntConstant("instanceKlass::low_offset").intValue();
    HIGH_OFFSET = db.lookupIntConstant("instanceKlass::high_offset").intValue();
    GENERIC_SIGNATURE_INDEX_OFFSET = db.lookupIntConstant("instanceKlass::generic_signature_offset").intValue();
    NEXT_OFFSET = db.lookupIntConstant("instanceKlass::next_offset").intValue();
    CLASS_STATE_UNPARSABLE_BY_GC = db.lookupIntConstant("instanceKlass::unparsable_by_gc").intValue();
    CLASS_STATE_ALLOCATED = db.lookupIntConstant("instanceKlass::allocated").intValue();
    CLASS_STATE_LOADED = db.lookupIntConstant("instanceKlass::loaded").intValue();
    CLASS_STATE_LINKED = db.lookupIntConstant("instanceKlass::linked").intValue();
    CLASS_STATE_BEING_INITIALIZED = db.lookupIntConstant("instanceKlass::being_initialized").intValue();
    CLASS_STATE_FULLY_INITIALIZED = db.lookupIntConstant("instanceKlass::fully_initialized").intValue();
    CLASS_STATE_INITIALIZATION_ERROR = db.lookupIntConstant("instanceKlass::initialization_error").intValue();
  }
  InstanceKlass(OopHandle handle, ObjectHeap heap) {
    super(handle, heap);
  }
  private static OopField  arrayKlasses;
  private static OopField  methods;
  private static OopField  methodOrdering;
  private static OopField  localInterfaces;
  private static OopField  transitiveInterfaces;
  private static CIntField nofImplementors;
  private static OopField[] implementors;
  private static OopField  fields;
  private static OopField  constants;
  private static OopField  classLoader;
  private static OopField  protectionDomain;
  private static OopField  signers;
  private static AddressField  sourceFileName;
  private static AddressField  sourceDebugExtension;
  private static OopField  innerClasses;
  private static CIntField nonstaticFieldSize;
  private static CIntField staticFieldSize;
  private static CIntField staticOopFieldCount;
  private static CIntField nonstaticOopMapSize;
  private static CIntField isMarkedDependent;
  private static CIntField initState;
  private static CIntField vtableLen;
  private static CIntField itableLen;
  private static AddressField breakpoints;
  private static AddressField  genericSignature;
  private static CIntField majorVersion;
  private static CIntField minorVersion;
  public static class ClassState {
     public static final ClassState UNPARSABLE_BY_GC = new ClassState("unparsable_by_gc");
     public static final ClassState ALLOCATED    = new ClassState("allocated");
     public static final ClassState LOADED       = new ClassState("loaded");
     public static final ClassState LINKED       = new ClassState("linked");
     public static final ClassState BEING_INITIALIZED      = new ClassState("beingInitialized");
     public static final ClassState FULLY_INITIALIZED    = new ClassState("fullyInitialized");
     public static final ClassState INITIALIZATION_ERROR = new ClassState("initializationError");
     private ClassState(String value) {
        this.value = value;
     }
     public String toString() {
        return value;
     }
     private String value;
  }
  private int  getInitStateAsInt() { return (int) initState.getValue(this); }
  public ClassState getInitState() {
     int state = getInitStateAsInt();
     if (state == CLASS_STATE_UNPARSABLE_BY_GC) {
        return ClassState.UNPARSABLE_BY_GC;
     } else if (state == CLASS_STATE_ALLOCATED) {
        return ClassState.ALLOCATED;
     } else if (state == CLASS_STATE_LOADED) {
        return ClassState.LOADED;
     } else if (state == CLASS_STATE_LINKED) {
        return ClassState.LINKED;
     } else if (state == CLASS_STATE_BEING_INITIALIZED) {
        return ClassState.BEING_INITIALIZED;
     } else if (state == CLASS_STATE_FULLY_INITIALIZED) {
        return ClassState.FULLY_INITIALIZED;
     } else if (state == CLASS_STATE_INITIALIZATION_ERROR) {
        return ClassState.INITIALIZATION_ERROR;
     } else {
        throw new RuntimeException("should not reach here");
     }
  }
  public boolean isLoaded() {
     return getInitStateAsInt() >= CLASS_STATE_LOADED;
  }
  public boolean isLinked() {
     return getInitStateAsInt() >= CLASS_STATE_LINKED;
  }
  public boolean isInitialized() {
     return getInitStateAsInt() == CLASS_STATE_FULLY_INITIALIZED;
  }
  public boolean isNotInitialized() {
     return getInitStateAsInt() < CLASS_STATE_BEING_INITIALIZED;
  }
  public boolean isBeingInitialized() {
     return getInitStateAsInt() == CLASS_STATE_BEING_INITIALIZED;
  }
  public boolean isInErrorState() {
     return getInitStateAsInt() == CLASS_STATE_INITIALIZATION_ERROR;
  }
  public int getClassStatus() {
     int result = 0;
     if (isLinked()) {
        result |= JVMDIClassStatus.VERIFIED | JVMDIClassStatus.PREPARED;
     }
     if (isInitialized()) {
        if (Assert.ASSERTS_ENABLED) {
           Assert.that(isLinked(), "Class status is not consistent");
        }
        result |= JVMDIClassStatus.INITIALIZED;
     }
     if (isInErrorState()) {
        result |= JVMDIClassStatus.ERROR;
     }
     return result;
  }
  private static long headerSize;
  public long getObjectSize(Oop object) {
    return getSizeHelper() * VM.getVM().getAddressSize();
  }
  public static long getHeaderSize() { return headerSize; }
  public Klass     getArrayKlasses()        { return (Klass)        arrayKlasses.getValue(this); }
  public ObjArray  getMethods()             { return (ObjArray)     methods.getValue(this); }
  public TypeArray getMethodOrdering()      { return (TypeArray)    methodOrdering.getValue(this); }
  public ObjArray  getLocalInterfaces()     { return (ObjArray)     localInterfaces.getValue(this); }
  public ObjArray  getTransitiveInterfaces() { return (ObjArray)     transitiveInterfaces.getValue(this); }
  public long      nofImplementors()        { return                nofImplementors.getValue(this); }
  public Klass     getImplementor()         { return (Klass)        implementors[0].getValue(this); }
  public Klass     getImplementor(int i)    { return (Klass)        implementors[i].getValue(this); }
  public TypeArray getFields()              { return (TypeArray)    fields.getValue(this); }
  public ConstantPool getConstants()        { return (ConstantPool) constants.getValue(this); }
  public Oop       getClassLoader()         { return                classLoader.getValue(this); }
  public Oop       getProtectionDomain()    { return                protectionDomain.getValue(this); }
  public ObjArray  getSigners()             { return (ObjArray)     signers.getValue(this); }
  public Symbol    getSourceFileName()      { return getSymbol(sourceFileName); }
  public Symbol    getSourceDebugExtension(){ return getSymbol(sourceDebugExtension); }
  public TypeArray getInnerClasses()        { return (TypeArray)    innerClasses.getValue(this); }
  public long      getNonstaticFieldSize()  { return                nonstaticFieldSize.getValue(this); }
  public long      getStaticOopFieldCount() { return                staticOopFieldCount.getValue(this); }
  public long      getNonstaticOopMapSize() { return                nonstaticOopMapSize.getValue(this); }
  public boolean   getIsMarkedDependent()   { return                isMarkedDependent.getValue(this) != 0; }
  public long      getVtableLen()           { return                vtableLen.getValue(this); }
  public long      getItableLen()           { return                itableLen.getValue(this); }
  public Symbol    getGenericSignature()    { return getSymbol(genericSignature); }
  public long      majorVersion()           { return                majorVersion.getValue(this); }
  public long      minorVersion()           { return                minorVersion.getValue(this); }
  public long getSizeHelper() {
    int lh = getLayoutHelper();
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(lh > 0, "layout helper initialized for instance class");
    }
    return lh / VM.getVM().getAddressSize();
  }
  public static interface InnerClassAttributeOffset {
    public static final int innerClassInnerClassInfoOffset = 0;
    public static final int innerClassOuterClassInfoOffset = 1;
    public static final int innerClassInnerNameOffset = 2;
    public static final int innerClassAccessFlagsOffset = 3;
    public static final int innerClassNextOffset = 4;
  };
  public long computeModifierFlags() {
    long access = getAccessFlags();
    TypeArray innerClassList = getInnerClasses();
    int length = ( innerClassList == null)? 0 : (int) innerClassList.getLength();
    if (length > 0) {
       if (Assert.ASSERTS_ENABLED) {
          Assert.that(length % InnerClassAttributeOffset.innerClassNextOffset == 0, "just checking");
       }
       for (int i = 0; i < length; i += InnerClassAttributeOffset.innerClassNextOffset) {
          int ioff = innerClassList.getShortAt(i +
                         InnerClassAttributeOffset.innerClassInnerClassInfoOffset);
          if (ioff != 0) {
             ConstantPool.CPSlot classInfo = getConstants().getSlotAt(ioff);
             Symbol name = null;
             if (classInfo.isOop()) {
               name = ((Klass) classInfo.getOop()).getName();
             } else if (classInfo.isMetaData()) {
               name = classInfo.getSymbol();
             } else {
                throw new RuntimeException("should not reach here");
             }
             if (name.equals(getName())) {
                access = innerClassList.getShortAt(i +
                        InnerClassAttributeOffset.innerClassAccessFlagsOffset);
                break;
             }
          }
       } 
    }
    return (access & (~JVM_ACC_SUPER)) & JVM_ACC_WRITTEN_FLAGS;
  }
  public boolean isInnerClassName(Symbol sym) {
    return isInInnerClasses(sym, false);
  }
  public boolean isInnerOrLocalClassName(Symbol sym) {
    return isInInnerClasses(sym, true);
  }
  private boolean isInInnerClasses(Symbol sym, boolean includeLocals) {
    TypeArray innerClassList = getInnerClasses();
    int length = ( innerClassList == null)? 0 : (int) innerClassList.getLength();
    if (length > 0) {
       if (Assert.ASSERTS_ENABLED) {
         Assert.that(length % InnerClassAttributeOffset.innerClassNextOffset == 0, "just checking");
       }
       for (int i = 0; i < length; i += InnerClassAttributeOffset.innerClassNextOffset) {
         int ioff = innerClassList.getShortAt(i +
                        InnerClassAttributeOffset.innerClassInnerClassInfoOffset);
         if (ioff != 0) {
            ConstantPool.CPSlot iclassInfo = getConstants().getSlotAt(ioff);
            Symbol innerName = null;
            if (iclassInfo.isOop()) {
              innerName = ((Klass) iclassInfo.getOop()).getName();
            } else if (iclassInfo.isMetaData()) {
              innerName = iclassInfo.getSymbol();
            } else {
               throw new RuntimeException("should not reach here");
            }
            Symbol myname = getName();
            int ooff = innerClassList.getShortAt(i +
                        InnerClassAttributeOffset.innerClassOuterClassInfoOffset);
            int innerNameIndex = innerClassList.getShortAt(i +
                        InnerClassAttributeOffset.innerClassInnerNameOffset);
            if (ooff == 0) {
               if (includeLocals) {
                  if (innerName.equals(sym) &&
                     innerName.asString().startsWith(myname.asString())) {
                     return (innerNameIndex != 0);
                  }
               }
            } else {
               ConstantPool.CPSlot oclassInfo = getConstants().getSlotAt(ooff);
               Symbol outerName = null;
               if (oclassInfo.isOop()) {
                 outerName = ((Klass) oclassInfo.getOop()).getName();
               } else if (oclassInfo.isMetaData()) {
                 outerName = oclassInfo.getSymbol();
               } else {
                  throw new RuntimeException("should not reach here");
               }
               if (outerName.equals(myname) && innerName.equals(sym)) {
                  return true;
               }
           }
         }
       } 
       return false;
    } else {
       return false;
    }
  }
  public boolean implementsInterface(Klass k) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(k.isInterface(), "should not reach here");
    }
    ObjArray interfaces =  getTransitiveInterfaces();
    final int len = (int) interfaces.getLength();
    for (int i = 0; i < len; i++) {
      if (interfaces.getObjAt(i).equals(k)) return true;
    }
    return false;
  }
  boolean computeSubtypeOf(Klass k) {
    if (k.isInterface()) {
      return implementsInterface(k);
    } else {
      return super.computeSubtypeOf(k);
    }
  }
  public void printValueOn(PrintStream tty) {
    tty.print("InstanceKlass for " + getName().asString());
  }
  public void iterateFields(OopVisitor visitor, boolean doVMFields) {
    super.iterateFields(visitor, doVMFields);
    if (doVMFields) {
      visitor.doOop(arrayKlasses, true);
      visitor.doOop(methods, true);
      visitor.doOop(methodOrdering, true);
      visitor.doOop(localInterfaces, true);
      visitor.doOop(transitiveInterfaces, true);
      visitor.doCInt(nofImplementors, true);
      for (int i = 0; i < IMPLEMENTORS_LIMIT; i++)
        visitor.doOop(implementors[i], true);
      visitor.doOop(fields, true);
      visitor.doOop(constants, true);
      visitor.doOop(classLoader, true);
      visitor.doOop(protectionDomain, true);
      visitor.doOop(signers, true);
      visitor.doOop(innerClasses, true);
      visitor.doCInt(nonstaticFieldSize, true);
      visitor.doCInt(staticFieldSize, true);
      visitor.doCInt(staticOopFieldCount, true);
      visitor.doCInt(nonstaticOopMapSize, true);
      visitor.doCInt(isMarkedDependent, true);
      visitor.doCInt(initState, true);
      visitor.doCInt(vtableLen, true);
      visitor.doCInt(itableLen, true);
    }
  }
  public void iterateStaticFields(OopVisitor visitor) {
    visitor.setObj(getJavaMirror());
    visitor.prologue();
    iterateStaticFieldsInternal(visitor);
    visitor.epilogue();
  }
  void iterateStaticFieldsInternal(OopVisitor visitor) {
    TypeArray fields = getFields();
    int length = (int) fields.getLength();
    for (int index = 0; index < length; index += NEXT_OFFSET) {
      short accessFlags    = fields.getShortAt(index + ACCESS_FLAGS_OFFSET);
      short signatureIndex = fields.getShortAt(index + SIGNATURE_INDEX_OFFSET);
      FieldType   type   = new FieldType(getConstants().getSymbolAt(signatureIndex));
      AccessFlags access = new AccessFlags(accessFlags);
      if (access.isStatic()) {
        visitField(visitor, type, index);
      }
    }
  }
  public Klass getJavaSuper() {
    return getSuper();
  }
  public void iterateNonStaticFields(OopVisitor visitor, Oop obj) {
    if (getSuper() != null) {
      ((InstanceKlass) getSuper()).iterateNonStaticFields(visitor, obj);
    }
    TypeArray fields = getFields();
    int length = (int) fields.getLength();
    for (int index = 0; index < length; index += NEXT_OFFSET) {
      short accessFlags    = fields.getShortAt(index + ACCESS_FLAGS_OFFSET);
      short signatureIndex = fields.getShortAt(index + SIGNATURE_INDEX_OFFSET);
      FieldType   type   = new FieldType(getConstants().getSymbolAt(signatureIndex));
      AccessFlags access = new AccessFlags(accessFlags);
      if (!access.isStatic()) {
        visitField(visitor, type, index);
      }
    }
  }
  public Field findLocalField(Symbol name, Symbol sig) {
    TypeArray fields = getFields();
    int n = (int) fields.getLength();
    ConstantPool cp = getConstants();
    for (int i = 0; i < n; i += NEXT_OFFSET) {
      int nameIndex = fields.getShortAt(i + NAME_INDEX_OFFSET);
      int sigIndex  = fields.getShortAt(i + SIGNATURE_INDEX_OFFSET);
      Symbol f_name = cp.getSymbolAt(nameIndex);
      Symbol f_sig  = cp.getSymbolAt(sigIndex);
      if (name.equals(f_name) && sig.equals(f_sig)) {
        return newField(i);
      }
    }
    return null;
  }
  public Field findInterfaceField(Symbol name, Symbol sig) {
    ObjArray interfaces = getLocalInterfaces();
    int n = (int) interfaces.getLength();
    for (int i = 0; i < n; i++) {
      InstanceKlass intf1 = (InstanceKlass) interfaces.getObjAt(i);
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(intf1.isInterface(), "just checking type");
      }
      Field f = intf1.findLocalField(name, sig);
      if (f != null) {
        if (Assert.ASSERTS_ENABLED) {
          Assert.that(f.getAccessFlagsObj().isStatic(), "interface field must be static");
        }
        return f;
      }
      f = intf1.findInterfaceField(name, sig);
      if (f != null) return f;
    }
    return null;
  }
  public Field findField(Symbol name, Symbol sig) {
    Field f = findLocalField(name, sig);
    if (f != null) return f;
    f = findInterfaceField(name, sig);
    if (f != null) return f;
    InstanceKlass supr = (InstanceKlass) getSuper();
    if (supr != null) return supr.findField(name, sig);
    return null;
  }
  public Field findField(String name, String sig) {
    SymbolTable symbols = VM.getVM().getSymbolTable();
    Symbol nameSym = symbols.probe(name);
    Symbol sigSym  = symbols.probe(sig);
    if (nameSym == null || sigSym == null) {
      return null;
    }
    return findField(nameSym, sigSym);
  }
  public Field findFieldDbg(String name, String sig) {
    return findField(name, sig);
  }
  public Field getFieldByIndex(int fieldArrayIndex) {
    return newField(fieldArrayIndex);
  }
    public List getImmediateFields() {
        TypeArray fields = getFields();
        int length = (int) fields.getLength();
        List immediateFields = new ArrayList(length / NEXT_OFFSET);
        for (int index = 0; index < length; index += NEXT_OFFSET) {
            immediateFields.add(getFieldByIndex(index));
        }
        return immediateFields;
    }
    public List getAllFields() {
        List  allFields = getImmediateFields();
        ObjArray interfaces = getTransitiveInterfaces();
        int n = (int) interfaces.getLength();
        for (int i = 0; i < n; i++) {
            InstanceKlass intf1 = (InstanceKlass) interfaces.getObjAt(i);
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(intf1.isInterface(), "just checking type");
            }
            allFields.addAll(intf1.getImmediateFields());
        }
        if (!isInterface()) {
            InstanceKlass supr;
            if  ( (supr = (InstanceKlass) getSuper()) != null) {
                allFields.addAll(supr.getImmediateFields());
            }
        }
        return allFields;
    }
    public List getImmediateMethods() {
      ObjArray methods = getMethods();
      int length = (int)methods.getLength();
      Object[] tmp = new Object[length];
      TypeArray methodOrdering = getMethodOrdering();
      if (methodOrdering.getLength() != length) {
         for (int index = 0; index < length; index++) {
            tmp[index] = methods.getObjAt(index);
         }
      } else {
         for (int index = 0; index < length; index++) {
            int originalIndex = getMethodOrdering().getIntAt(index);
            tmp[originalIndex] = methods.getObjAt(index);
         }
      }
      return Arrays.asList(tmp);
    }
    public List getDirectImplementedInterfaces() {
        ObjArray interfaces = getLocalInterfaces();
        int length = (int) interfaces.getLength();
        List directImplementedInterfaces = new ArrayList(length);
        for (int index = 0; index < length; index ++) {
            directImplementedInterfaces.add(interfaces.getObjAt(index));
        }
        return directImplementedInterfaces;
    }
  public long getObjectSize() {
    long bodySize =    alignObjectOffset(getVtableLen() * getHeap().getOopSize())
                     + alignObjectOffset(getItableLen() * getHeap().getOopSize())
                     + (getNonstaticOopMapSize()) * getHeap().getOopSize();
    return alignObjectSize(headerSize + bodySize);
  }
  public Klass arrayKlassImpl(boolean orNull, int n) {
    if (getArrayKlasses() == null) { return null; }
    ObjArrayKlass oak = (ObjArrayKlass) getArrayKlasses();
    if (orNull) {
      return oak.arrayKlassOrNull(n);
    }
    return oak.arrayKlass(n);
  }
  public Klass arrayKlassImpl(boolean orNull) {
    return arrayKlassImpl(orNull, 1);
  }
  public String signature() {
     return "L" + super.signature() + ";";
  }
  public Method findMethod(String name, String sig) {
    SymbolTable syms = VM.getVM().getSymbolTable();
    Symbol nameSym = syms.probe(name);
    Symbol sigSym  = syms.probe(sig);
    if (nameSym == null || sigSym == null) {
      return null;
    }
    return findMethod(nameSym, sigSym);
  }
  public Method findMethod(Symbol name, Symbol sig) {
    return findMethod(getMethods(), name, sig);
  }
  public BreakpointInfo getBreakpoints() {
    Address addr = getHandle().getAddressAt(Oop.getHeaderSize() + breakpoints.getOffset());
    return (BreakpointInfo) VMObjectFactory.newObject(BreakpointInfo.class, addr);
  }
  private void visitField(OopVisitor visitor, FieldType type, int index) {
    Field f = newField(index);
    if (type.isOop()) {
      visitor.doOop((OopField) f, false);
      return;
    }
    if (type.isByte()) {
      visitor.doByte((ByteField) f, false);
      return;
    }
    if (type.isChar()) {
      visitor.doChar((CharField) f, false);
      return;
    }
    if (type.isDouble()) {
      visitor.doDouble((DoubleField) f, false);
      return;
    }
    if (type.isFloat()) {
      visitor.doFloat((FloatField) f, false);
      return;
    }
    if (type.isInt()) {
      visitor.doInt((IntField) f, false);
      return;
    }
    if (type.isLong()) {
      visitor.doLong((LongField) f, false);
      return;
    }
    if (type.isShort()) {
      visitor.doShort((ShortField) f, false);
      return;
    }
    if (type.isBoolean()) {
      visitor.doBoolean((BooleanField) f, false);
      return;
    }
  }
  private Field newField(int index) {
    TypeArray fields = getFields();
    short signatureIndex = fields.getShortAt(index + SIGNATURE_INDEX_OFFSET);
    FieldType type = new FieldType(getConstants().getSymbolAt(signatureIndex));
    if (type.isOop()) {
     if (VM.getVM().isCompressedOopsEnabled()) {
        return new NarrowOopField(this, index);
     } else {
        return new OopField(this, index);
     }
    }
    if (type.isByte()) {
      return new ByteField(this, index);
    }
    if (type.isChar()) {
      return new CharField(this, index);
    }
    if (type.isDouble()) {
      return new DoubleField(this, index);
    }
    if (type.isFloat()) {
      return new FloatField(this, index);
    }
    if (type.isInt()) {
      return new IntField(this, index);
    }
    if (type.isLong()) {
      return new LongField(this, index);
    }
    if (type.isShort()) {
      return new ShortField(this, index);
    }
    if (type.isBoolean()) {
      return new BooleanField(this, index);
    }
    throw new RuntimeException("Illegal field type at index " + index);
  }
  private static Method findMethod(ObjArray methods, Symbol name, Symbol signature) {
    int len = (int) methods.getLength();
    int l = 0;
    int h = len - 1;
    while (l <= h) {
      int mid = (l + h) >> 1;
      Method m = (Method) methods.getObjAt(mid);
      int res = m.getName().fastCompare(name);
      if (res == 0) {
        if (m.getSignature().equals(signature)) return m;
        int i;
        for (i = mid - 1; i >= l; i--) {
          Method m1 = (Method) methods.getObjAt(i);
          if (!m1.getName().equals(name)) break;
          if (m1.getSignature().equals(signature)) return m1;
        }
        for (i = mid + 1; i <= h; i++) {
          Method m1 = (Method) methods.getObjAt(i);
          if (!m1.getName().equals(name)) break;
          if (m1.getSignature().equals(signature)) return m1;
        }
        if (Assert.ASSERTS_ENABLED) {
          int index = linearSearch(methods, name, signature);
          if (index != -1) {
            throw new DebuggerException("binary search bug: should have found entry " + index);
          }
        }
        return null;
      } else if (res < 0) {
        l = mid + 1;
      } else {
        h = mid - 1;
      }
    }
    if (Assert.ASSERTS_ENABLED) {
      int index = linearSearch(methods, name, signature);
      if (index != -1) {
        throw new DebuggerException("binary search bug: should have found entry " + index);
      }
    }
    return null;
  }
  private static int linearSearch(ObjArray methods, Symbol name, Symbol signature) {
    int len = (int) methods.getLength();
    for (int index = 0; index < len; index++) {
      Method m = (Method) methods.getObjAt(index);
      if (m.getSignature().equals(signature) && m.getName().equals(name)) {
        return index;
      }
    }
    return -1;
  }
}
