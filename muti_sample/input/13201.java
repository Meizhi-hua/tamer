public class BasicTypeDataBase implements TypeDataBase {
  private MachineDescription machDesc;
  private VtblAccess vtblAccess;
  private Map nameToTypeMap = new HashMap();
  private Map nameToIntConstantMap = new HashMap();
  private Map nameToLongConstantMap = new HashMap();
  private Type jbooleanType;
  private Type jbyteType;
  private Type jcharType;
  private Type jdoubleType;
  private Type jfloatType;
  private Type jintType;
  private Type jlongType;
  private Type jshortType;
  private static final boolean DEBUG;
  static {
    DEBUG = System.getProperty("sun.jvm.hotspot.types.basic.BasicTypeDataBase.DEBUG") != null;
  }
  public BasicTypeDataBase(MachineDescription machDesc, VtblAccess vtblAccess) {
    this.machDesc   = machDesc;
    this.vtblAccess = vtblAccess;
  }
  public Type lookupType(String cTypeName) {
    return lookupType(cTypeName, true);
  }
  public Type lookupType(String cTypeName, boolean throwException) {
    Type type = (Type) nameToTypeMap.get(cTypeName);
    if (type == null && throwException) {
      throw new RuntimeException("No type named \"" + cTypeName + "\" in database");
    }
    return type;
  }
  public Integer lookupIntConstant(String constantName) {
    return lookupIntConstant(constantName, true);
  }
  public Integer lookupIntConstant(String constantName, boolean throwException) {
    Integer i = (Integer) nameToIntConstantMap.get(constantName);
    if (i == null) {
      if (throwException) {
        throw new RuntimeException("No integer constant named \"" + constantName + "\" present in type database");
      }
    }
    return i;
  }
  public Long lookupLongConstant(String constantName) {
    return lookupLongConstant(constantName, true);
  }
  public Long lookupLongConstant(String constantName, boolean throwException) {
    Long i = (Long) nameToLongConstantMap.get(constantName);
    if (i == null) {
      if (throwException) {
        throw new RuntimeException("No long constant named \"" + constantName + "\" present in type database");
      }
    }
    return i;
  }
  public Type getJBooleanType() {
    return jbooleanType;
  }
  public Type getJByteType() {
    return jbyteType;
  }
  public Type getJCharType() {
    return jcharType;
  }
  public Type getJDoubleType() {
    return jdoubleType;
  }
  public Type getJFloatType() {
    return jfloatType;
  }
  public Type getJIntType() {
    return jintType;
  }
  public Type getJLongType() {
    return jlongType;
  }
  public Type getJShortType() {
    return jshortType;
  }
  public long getAddressSize() {
    return machDesc.getAddressSize();
  }
  public long getOopSize() {
    return VM.getVM().getOopSize();
  }
  public boolean addressTypeIsEqualToType(Address addr, Type type) {
    if (addr == null) {
      return false;
    }
    Address vtblAddr = vtblAccess.getVtblForType(type);
    if (vtblAddr == null) {
      if (DEBUG) {
        System.err.println("BasicTypeDataBase.addressTypeIsEqualToType: vtblAddr == null");
      }
      return false;
    }
    Type curType = type;
    try {
      while (curType != null) {
        if (vtblAddr.equals(addr.getAddressAt(0))) {
          return true;
        }
        long offset = curType.getSize();
        offset -= (offset % getAddressSize());
        if (offset <= 0) {
          return false;
        }
        if (vtblAddr.equals(addr.getAddressAt(offset))) {
          return true;
        }
        offset -= getAddressSize();
        if (offset <= 0) {
          return false;
        }
        if (vtblAddr.equals(addr.getAddressAt(offset))) {
          return true;
        }
        curType = curType.getSuperclass();
      }
    }
    catch (Exception e) {
      if (DEBUG) {
        System.err.println("BasicTypeDataBase.addressTypeIsEqualToType: exception occurred during lookup:");
        e.printStackTrace();
      }
      return false;
    }
    if (DEBUG) {
      System.err.println("BasicTypeDataBase.addressTypeIsEqualToType: all vptr tests failed for type " +
                         type.getName());
    }
    return false;
  }
  public Type guessTypeForAddress(Address addr) {
    for (Iterator iter = getTypes(); iter.hasNext(); ) {
      Type t = (Type) iter.next();
      if (addressTypeIsEqualToType(addr, t)) {
        return t;
      }
    }
    return null;
  }
  public long cIntegerTypeMaxValue(long sizeInBytes, boolean isUnsigned) {
    return machDesc.cIntegerTypeMaxValue(sizeInBytes, isUnsigned);
  }
  public long cIntegerTypeMinValue(long sizeInBytes, boolean isUnsigned) {
    return machDesc.cIntegerTypeMinValue(sizeInBytes, isUnsigned);
  }
  public Iterator getTypes() {
    return nameToTypeMap.values().iterator();
  }
  public Iterator getIntConstants() {
    return nameToIntConstantMap.keySet().iterator();
  }
  public Iterator getLongConstants() {
    return nameToLongConstantMap.keySet().iterator();
  }
  public void setJBooleanType(Type type) {
    jbooleanType = type;
  }
  public void setJByteType(Type type) {
    jbyteType = type;
  }
  public void setJCharType(Type type) {
    jcharType = type;
  }
  public void setJDoubleType(Type type) {
    jdoubleType = type;
  }
  public void setJFloatType(Type type) {
    jfloatType = type;
  }
  public void setJIntType(Type type) {
    jintType = type;
  }
  public void setJLongType(Type type) {
    jlongType = type;
  }
  public void setJShortType(Type type) {
    jshortType = type;
  }
  public void addType(Type type) {
    if (nameToTypeMap.get(type.getName()) != null) {
      throw new RuntimeException("type of name \"" + type.getName() + "\" already present");
    }
    nameToTypeMap.put(type.getName(), type);
  }
  public void removeType(Type type) {
    Type curType = (Type) nameToTypeMap.get(type.getName());
    if (curType == null) {
      throw new RuntimeException("type of name \"" + type.getName() + "\" not present");
    }
    if (!curType.equals(type)) {
      throw new RuntimeException("a different type of name \"" + type.getName() + "\" was present");
    }
    nameToTypeMap.remove(type.getName());
  }
  public void addIntConstant(String name, int value) {
    if (nameToIntConstantMap.get(name) != null) {
      throw new RuntimeException("int constant of name \"" + name + "\" already present");
    }
    nameToIntConstantMap.put(name, new Integer(value));
  }
  public void removeIntConstant(String name) {
    Integer curConstant = (Integer) nameToIntConstantMap.get(name);
    if (curConstant == null) {
      throw new RuntimeException("int constant of name \"" + name + "\" not present");
    }
    nameToIntConstantMap.remove(name);
  }
  public void addLongConstant(String name, long value) {
    if (nameToLongConstantMap.get(name) != null) {
      throw new RuntimeException("long constant of name \"" + name + "\" already present");
    }
    nameToLongConstantMap.put(name, new Long(value));
  }
  public void removeLongConstant(String name) {
    Long curConstant = (Long) nameToLongConstantMap.get(name);
    if (curConstant == null) {
      throw new RuntimeException("long constant of name \"" + name + "\" not present");
    }
    nameToLongConstantMap.remove(name);
  }
}
