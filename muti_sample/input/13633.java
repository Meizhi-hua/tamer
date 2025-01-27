public class ObjectReader {
   private static final boolean DEBUG;
   static {
      DEBUG = System.getProperty("sun.jvm.hotspot.utilities.ObjectReader.DEBUG") != null;
   }
   public ObjectReader(ClassLoader cl) {
      this.cl = cl;
      this.oopToObjMap = new HashMap();
      this.fieldMap = new HashMap();
   }
   public ObjectReader() {
      this(new ProcImageClassLoader());
   }
   public Object readObject(Oop oop) throws ClassNotFoundException {
      if (oop instanceof Instance) {
         return readInstance((Instance) oop);
      } else if (oop instanceof TypeArray){
         return readPrimitiveArray((TypeArray)oop);
      } else if (oop instanceof ObjArray){
         return readObjectArray((ObjArray)oop);
      } else {
         return null;
      }
   }
   protected final Object getDefaultPrimitiveValue(Class clz) {
      if (clz == Boolean.TYPE) {
         return Boolean.FALSE;
      } else if (clz == Character.TYPE) {
         return new Character(' ');
      } else if (clz == Byte.TYPE) {
         return new Byte((byte) 0);
      } else if (clz == Short.TYPE) {
         return new Short((short) 0);
      } else if (clz == Integer.TYPE) {
         return new Integer(0);
      } else if (clz == Long.TYPE) {
         return new Long(0L);
      } else if (clz == Float.TYPE) {
         return new Float(0.0f);
      } else if (clz == Double.TYPE) {
         return new Double(0.0);
      } else {
         throw new RuntimeException("should not reach here!");
      }
   }
   protected Symbol javaLangString;
   protected Symbol javaLangString() {
      if (javaLangString == null) {
         javaLangString = VM.getVM().getSymbolTable().probe("java/lang/String");
      }
      return javaLangString;
   }
   public Object readInstance(Instance oop) throws ClassNotFoundException {
      Object result = getFromObjTable(oop);
      if (result == null) {
         InstanceKlass kls = (InstanceKlass) oop.getKlass();
         if (kls.getName().equals(javaLangString())) {
            return OopUtilities.stringOopToString(oop);
         }
         Class clz = readClass(kls);
         try {
            result = clz.newInstance();
         } catch (Exception ex) {
            java.lang.reflect.Constructor[] ctrs = clz.getDeclaredConstructors();
            for (int n = 0; n < ctrs.length; n++) {
               java.lang.reflect.Constructor c = ctrs[n];
               Class[] paramTypes = c.getParameterTypes();
               Object[] params = new Object[paramTypes.length];
               for (int i = 0; i < params.length; i++) {
                  if (paramTypes[i].isPrimitive()) {
                     params[i] = getDefaultPrimitiveValue(paramTypes[i]);
                  }
               }
               try {
                  c.setAccessible(true);
                  result = c.newInstance(params);
                  break;
               } catch (Exception exp) {
                  if (DEBUG) {
                     System.err.println("Can't create object using " + c);
                     exp.printStackTrace();
                  }
               }
            }
         }
         if (result != null) {
            putIntoObjTable(oop, result);
            oop.iterate(new FieldSetter(result), false);
         }
      }
      return result;
   }
   public Object readPrimitiveArray(final TypeArray array) {
      Object result = getFromObjTable(array);
      if (result == null) {
         int length = (int) array.getLength();
         TypeArrayKlass klass = (TypeArrayKlass) array.getKlass();
         int type = (int) klass.getElementType();
         switch (type) {
            case TypeArrayKlass.T_BOOLEAN: {
               final boolean[] arrayObj = new boolean[length];
               array.iterate(new DefaultOopVisitor() {
                                public void doBoolean(BooleanField field, boolean isVMField) {
                                   IndexableFieldIdentifier ifd = (IndexableFieldIdentifier) field.getID();
                                   arrayObj[ifd.getIndex()] = field.getValue(array);
                                }
                            }, false);
               result = arrayObj;
               }
               break;
            case TypeArrayKlass.T_CHAR: {
               final char[] arrayObj = new char[length];
               array.iterate(new DefaultOopVisitor() {
                                public void doChar(CharField field, boolean isVMField) {
                                   IndexableFieldIdentifier ifd = (IndexableFieldIdentifier) field.getID();
                                   arrayObj[ifd.getIndex()] = field.getValue(array);
                                }
                            }, false);
               result = arrayObj;
               }
               break;
            case TypeArrayKlass.T_FLOAT: {
               final float[] arrayObj = new float[length];
               array.iterate(new DefaultOopVisitor() {
                                public void doFloat(FloatField field, boolean isVMField) {
                                   IndexableFieldIdentifier ifd = (IndexableFieldIdentifier) field.getID();
                                   arrayObj[ifd.getIndex()] = field.getValue(array);
                                }
                            }, false);
               result = arrayObj;
               }
               break;
            case TypeArrayKlass.T_DOUBLE: {
               final double[] arrayObj = new double[length];
               array.iterate(new DefaultOopVisitor() {
                                public void doDouble(DoubleField field, boolean isVMField) {
                                   IndexableFieldIdentifier ifd = (IndexableFieldIdentifier) field.getID();
                                   arrayObj[ifd.getIndex()] = field.getValue(array);
                                }
                            }, false);
               result = arrayObj;
               }
               break;
            case TypeArrayKlass.T_BYTE: {
               final byte[] arrayObj = new byte[length];
               array.iterate(new DefaultOopVisitor() {
                                public void doByte(ByteField field, boolean isVMField) {
                                   IndexableFieldIdentifier ifd = (IndexableFieldIdentifier) field.getID();
                                   arrayObj[ifd.getIndex()] = field.getValue(array);
                                }
                            }, false);
               result = arrayObj;
               }
               break;
            case TypeArrayKlass.T_SHORT: {
               final short[] arrayObj = new short[length];
               array.iterate(new DefaultOopVisitor() {
                                public void doShort(ShortField field, boolean isVMField) {
                                   IndexableFieldIdentifier ifd = (IndexableFieldIdentifier) field.getID();
                                   arrayObj[ifd.getIndex()] = field.getValue(array);
                                }
                            }, false);
               result = arrayObj;
               }
               break;
            case TypeArrayKlass.T_INT: {
               final int[] arrayObj = new int[length];
               array.iterate(new DefaultOopVisitor() {
                                public void doInt(IntField field, boolean isVMField) {
                                   IndexableFieldIdentifier ifd = (IndexableFieldIdentifier) field.getID();
                                   arrayObj[ifd.getIndex()] = field.getValue(array);
                                }
                            }, false);
               result = arrayObj;
               }
               break;
            case TypeArrayKlass.T_LONG: {
               final long[] arrayObj = new long[length];
               array.iterate(new DefaultOopVisitor() {
                                public void doLong(LongField field, boolean isVMField) {
                                   IndexableFieldIdentifier ifd = (IndexableFieldIdentifier) field.getID();
                                   arrayObj[ifd.getIndex()] = field.getValue(array);
                                }
                            }, false);
               result = arrayObj;
               }
               break;
            default:
               throw new RuntimeException("should not reach here!");
         }
         putIntoObjTable(array, result);
      }
      return result;
   }
   protected final boolean isRobust(OopHandle handle) {
      return RobustOopDeterminator.oopLooksValid(handle);
   }
   public Object readObjectArray(final ObjArray array) throws ClassNotFoundException {
       Object result = getFromObjTable(array);
       if (result == null) {
          int length = (int) array.getLength();
          ObjArrayKlass klass = (ObjArrayKlass) array.getKlass();
          Klass bottomKls = klass.getBottomKlass();
          Class bottomCls = null;
          final int dimension = (int) klass.getDimension();
          int[] dimArray = null;
          if (bottomKls instanceof InstanceKlass) {
             bottomCls = readClass((InstanceKlass) bottomKls);
             dimArray = new int[dimension];
          } else { 
             TypeArrayKlass botKls = (TypeArrayKlass) bottomKls;
             dimArray = new int[dimension -1];
          }
          dimArray[0] = length;
          final Object[] arrayObj = (Object[]) java.lang.reflect.Array.newInstance(bottomCls, dimArray);
          putIntoObjTable(array, arrayObj);
          result = arrayObj;
          array.iterate(new DefaultOopVisitor() {
                               public void doOop(OopField field, boolean isVMField) {
                                  OopHandle handle = field.getValueAsOopHandle(getObj());
                                  if (! isRobust(handle)) {
                                     return;
                                  }
                                  IndexableFieldIdentifier ifd = (IndexableFieldIdentifier) field.getID();
                                  try {
                                     arrayObj[ifd.getIndex()] = readObject(field.getValue(getObj()));
                                  } catch (Exception e) {
                                     if (DEBUG) {
                                        System.err.println("Array element set failed for " + ifd);
                                        e.printStackTrace();
                                     }
                                  }
                               }
                        }, false);
       }
       return result;
   }
   protected class FieldSetter extends DefaultOopVisitor {
      protected Object obj;
      public FieldSetter(Object obj) {
         this.obj = obj;
      }
      private void printFieldSetError(java.lang.reflect.Field f, Exception ex) {
         if (DEBUG) {
            if (f != null) System.err.println("Field set failed for " + f);
            ex.printStackTrace();
         }
      }
      public void doOop(OopField field, boolean isVMField) {
         OopHandle handle = field.getValueAsOopHandle(getObj());
         if (! isRobust(handle) ) {
            return;
         }
         java.lang.reflect.Field f = null;
         try {
            f = readField(field);
            if (Modifier.isFinal(f.getModifiers())) return;
            f.setAccessible(true);
            f.set(obj, readObject(field.getValue(getObj())));
         } catch (Exception ex) {
            printFieldSetError(f, ex);
         }
      }
      public void doByte(ByteField field, boolean isVMField) {
         java.lang.reflect.Field f = null;
         try {
            f = readField(field);
            if (Modifier.isFinal(f.getModifiers())) return;
            f.setAccessible(true);
            f.setByte(obj, field.getValue(getObj()));
         } catch (Exception ex) {
            printFieldSetError(f, ex);
         }
      }
      public void doChar(CharField field, boolean isVMField) {
         java.lang.reflect.Field f = null;
         try {
            f = readField(field);
            if (Modifier.isFinal(f.getModifiers())) return;
            f.setAccessible(true);
            f.setChar(obj, field.getValue(getObj()));
         } catch (Exception ex) {
            printFieldSetError(f, ex);
         }
      }
      public void doBoolean(BooleanField field, boolean isVMField) {
         java.lang.reflect.Field f = null;
         try {
            f = readField(field);
            if (Modifier.isFinal(f.getModifiers())) return;
            f.setAccessible(true);
            f.setBoolean(obj, field.getValue(getObj()));
         } catch (Exception ex) {
            printFieldSetError(f, ex);
         }
      }
      public void doShort(ShortField field, boolean isVMField) {
         java.lang.reflect.Field f = null;
         try {
            f = readField(field);
            if (Modifier.isFinal(f.getModifiers())) return;
            f.setAccessible(true);
            f.setShort(obj, field.getValue(getObj()));
         } catch (Exception ex) {
            printFieldSetError(f, ex);
         }
      }
      public void doInt(IntField field, boolean isVMField) {
         java.lang.reflect.Field f = null;
         try {
            f = readField(field);
            if (Modifier.isFinal(f.getModifiers())) return;
            f.setAccessible(true);
            f.setInt(obj, field.getValue(getObj()));
         } catch (Exception ex) {
            printFieldSetError(f, ex);
         }
      }
      public void doLong(LongField field, boolean isVMField) {
         java.lang.reflect.Field f = null;
         try {
            f = readField(field);
            if (Modifier.isFinal(f.getModifiers())) return;
            f.setAccessible(true);
            f.setLong(obj, field.getValue(getObj()));
         } catch (Exception ex) {
            printFieldSetError(f, ex);
         }
      }
      public void doFloat(FloatField field, boolean isVMField) {
         java.lang.reflect.Field f = null;
         try {
            f = readField(field);
            if (Modifier.isFinal(f.getModifiers())) return;
            f.setAccessible(true);
            f.setFloat(obj, field.getValue(getObj()));
         } catch (Exception ex) {
            printFieldSetError(f, ex);
         }
      }
      public void doDouble(DoubleField field, boolean isVMField) {
         java.lang.reflect.Field f = null;
         try {
            f = readField(field);
            if (Modifier.isFinal(f.getModifiers())) return;
            f.setAccessible(true);
            f.setDouble(obj, field.getValue(getObj()));
         } catch (Exception ex) {
            printFieldSetError(f, ex);
         }
      }
      public void doCInt(CIntField field, boolean isVMField) {
         throw new RuntimeException("should not reach here!");
      }
   }
   public Class readClass(InstanceKlass kls) throws ClassNotFoundException {
      Class cls = (Class) getFromObjTable(kls);
      if (cls == null) {
         cls = Class.forName(kls.getName().asString().replace('/', '.'), true, cl);
         putIntoObjTable(kls, cls);
      }
      return cls;
   }
   public Object readMethodOrConstructor(sun.jvm.hotspot.oops.Method m)
                     throws NoSuchMethodException, ClassNotFoundException {
      String name = m.getName().asString();
      if (name.equals("<init>")) {
         return readConstructor(m);
      } else {
         return readMethod(m);
      }
   }
   public java.lang.reflect.Method readMethod(sun.jvm.hotspot.oops.Method m)
            throws NoSuchMethodException, ClassNotFoundException {
      java.lang.reflect.Method result = (java.lang.reflect.Method) getFromObjTable(m);
      if (result == null) {
         Class clz = readClass((InstanceKlass)m.getMethodHolder());
         String name = m.getName().asString();
         Class[] paramTypes = getParamTypes(m.getSignature());
         result = clz.getMethod(name, paramTypes);
         putIntoObjTable(m, result);
      }
      return result;
   }
   public java.lang.reflect.Constructor readConstructor(sun.jvm.hotspot.oops.Method m)
            throws NoSuchMethodException, ClassNotFoundException {
      java.lang.reflect.Constructor result = (java.lang.reflect.Constructor) getFromObjTable(m);
      if (result == null) {
         Class clz = readClass((InstanceKlass)m.getMethodHolder());
         String name = m.getName().asString();
         Class[] paramTypes = getParamTypes(m.getSignature());
         result = clz.getDeclaredConstructor(paramTypes);
         putIntoObjTable(m, result);
      }
      return result;
   }
   public java.lang.reflect.Field readField(sun.jvm.hotspot.oops.Field f)
            throws NoSuchFieldException, ClassNotFoundException {
      java.lang.reflect.Field result = (java.lang.reflect.Field) fieldMap.get(f);
      if (result == null) {
         FieldIdentifier fieldId = f.getID();
         Class clz = readClass((InstanceKlass) f.getFieldHolder());
         String name = fieldId.getName();
         try {
            result = clz.getField(name);
         } catch (NoSuchFieldException nsfe) {
            result = clz.getDeclaredField(name);
         }
         fieldMap.put(f, result);
      }
      return result;
   }
   protected final ClassLoader cl;
   protected Map   oopToObjMap; 
   protected Map   fieldMap;    
   protected void putIntoObjTable(Oop oop, Object obj) {
      oopToObjMap.put(oop, obj);
   }
   protected Object getFromObjTable(Oop oop) {
      return oopToObjMap.get(oop);
   }
   protected class SignatureParser extends SignatureIterator {
      protected Vector tmp = new Vector(); 
      public SignatureParser(Symbol s) {
         super(s);
      }
      public void doBool  () { tmp.add(Boolean.TYPE);    }
      public void doChar  () { tmp.add(Character.TYPE);  }
      public void doFloat () { tmp.add(Float.TYPE);      }
      public void doDouble() { tmp.add(Double.TYPE);     }
      public void doByte  () { tmp.add(Byte.TYPE);       }
      public void doShort () { tmp.add(Short.TYPE);      }
      public void doInt   () { tmp.add(Integer.TYPE);    }
      public void doLong  () { tmp.add(Long.TYPE);       }
      public void doVoid  () {
         if(isReturnType()) {
            tmp.add(Void.TYPE);
         } else {
            throw new RuntimeException("should not reach here");
         }
      }
      public void doObject(int begin, int end) {
         tmp.add(getClass(begin, end));
      }
      public void doArray (int begin, int end) {
        int inner = arrayInnerBegin(begin);
        Class elemCls = null;
        switch (_signature.getByteAt(inner)) {
        case 'B': elemCls = Boolean.TYPE; break;
        case 'C': elemCls = Character.TYPE; break;
        case 'D': elemCls = Double.TYPE; break;
        case 'F': elemCls = Float.TYPE; break;
        case 'I': elemCls = Integer.TYPE; break;
        case 'J': elemCls = Long.TYPE; break;
        case 'S': elemCls = Short.TYPE; break;
        case 'Z': elemCls = Boolean.TYPE; break;
        case 'L': elemCls = getClass(inner + 1, end); break;
        default: break;
        }
        int dimension = inner - begin;
        int[] dimArray = new int[dimension];
        tmp.add(java.lang.reflect.Array.newInstance(elemCls, dimArray).getClass());
      }
      protected Class getClass(int begin, int end) {
         String className = getClassName(begin, end);
         try {
            return Class.forName(className, true, cl);
         } catch (Exception e) {
            if (DEBUG) {
               System.err.println("Can't load class " + className);
            }
            throw new RuntimeException(e);
         }
      }
      protected String getClassName(int begin, int end) {
         StringBuffer buf = new StringBuffer();
         for (int i = begin; i < end; i++) {
            char c = (char) (_signature.getByteAt(i) & 0xFF);
            if (c == '/') {
               buf.append('.');
            } else {
               buf.append(c);
            }
         }
         return buf.toString();
      }
      protected int arrayInnerBegin(int begin) {
         while (_signature.getByteAt(begin) == '[') {
           ++begin;
         }
         return begin;
      }
      public int getNumParams() {
         return tmp.size();
      }
      public Enumeration getParamTypes() {
         return tmp.elements();
      }
   }
   protected Class[] getParamTypes(Symbol signature) {
      SignatureParser sp = new SignatureParser(signature);
      sp.iterateParameters();
      Class result[] = new Class[sp.getNumParams()];
      Enumeration e = sp.getParamTypes();
      int i = 0;
      while (e.hasMoreElements()) {
         result[i] = (Class) e.nextElement();
         i++;
      }
      return result;
   }
}
