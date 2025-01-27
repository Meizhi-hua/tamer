public class JSJavaTypeArrayKlass extends JSJavaArrayKlass {
   public JSJavaTypeArrayKlass(TypeArrayKlass kls, JSJavaFactory fac) {
      super(kls, fac);
   }
   public final TypeArrayKlass getTypeArrayKlass() {
       return (TypeArrayKlass) getArrayKlass();
   }
   public String getName() {
      int type = (int) getTypeArrayKlass().getElementType();
      switch (type) {
         case TypeArrayKlass.T_BOOLEAN:
            return "boolean[]";
         case TypeArrayKlass.T_CHAR:
            return "char[]";
         case TypeArrayKlass.T_FLOAT:
            return "float[]";
         case TypeArrayKlass.T_DOUBLE:
            return "double[]";
         case TypeArrayKlass.T_BYTE:
            return "byte[]";
         case TypeArrayKlass.T_SHORT:
            return "short[]";
         case TypeArrayKlass.T_INT:
            return "int[]";
         case TypeArrayKlass.T_LONG:
            return "long[]";
         default:
            if (Assert.ASSERTS_ENABLED) {
               Assert.that(false, "Unknown primitive array type");
            }
            return null;
      }
   }
   public Object getFieldValue(int index, Array array) {
      TypeArray typeArr = (TypeArray) array;
      int type = (int) getTypeArrayKlass().getElementType();
      switch (type) {
         case TypeArrayKlass.T_BOOLEAN:
            return Boolean.valueOf(typeArr.getBooleanAt(index));
         case TypeArrayKlass.T_CHAR:
            return new String(new char[] { typeArr.getCharAt(index) });
         case TypeArrayKlass.T_FLOAT:
            return new Float(typeArr.getFloatAt(index));
         case TypeArrayKlass.T_DOUBLE:
            return new Double(typeArr.getDoubleAt(index));
         case TypeArrayKlass.T_BYTE:
            return new Byte(typeArr.getByteAt(index));
         case TypeArrayKlass.T_SHORT:
            return new Short(typeArr.getShortAt(index));
         case TypeArrayKlass.T_INT:
            return new Integer(typeArr.getIntAt(index));
         case TypeArrayKlass.T_LONG:
            return new Long(typeArr.getLongAt(index));
         default:
            if (Assert.ASSERTS_ENABLED) {
               Assert.that(false, "Unknown primitive array type");
            }
            return null;
      }
   }
}
