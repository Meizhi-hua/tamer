public class Mangle {
    public static class Type {
        public static final int CLASS            = 1;
        public static final int FIELDSTUB        = 2;
        public static final int FIELD            = 3;
        public static final int JNI              = 4;
        public static final int SIGNATURE        = 5;
        public static final int METHOD_JDK_1     = 6;
        public static final int METHOD_JNI_SHORT = 7;
        public static final int METHOD_JNI_LONG  = 8;
    };
    private Elements elems;
    private Types types;
    Mangle(Elements elems, Types types) {
        this.elems = elems;
        this.types = types;
    }
    public final String mangle(CharSequence name, int mtype) {
        StringBuffer result = new StringBuffer(100);
        int length = name.length();
        for (int i = 0; i < length; i++) {
            char ch = name.charAt(i);
            if (isalnum(ch)) {
                result.append(ch);
            } else if ((ch == '.') &&
                       mtype == Mangle.Type.CLASS) {
                result.append('_');
            } else if (( ch == '$') &&
                       mtype == Mangle.Type.CLASS) {
                result.append('_');
                result.append('_');
            } else if (ch == '_' && mtype == Mangle.Type.FIELDSTUB) {
                result.append('_');
            } else if (ch == '_' && mtype == Mangle.Type.CLASS) {
                result.append('_');
            } else if (mtype == Mangle.Type.JNI) {
                String esc = null;
                if (ch == '_')
                    esc = "_1";
                else if (ch == '.')
                    esc = "_";
                else if (ch == ';')
                    esc = "_2";
                else if (ch == '[')
                    esc = "_3";
                if (esc != null) {
                    result.append(esc);
                } else {
                    result.append(mangleChar(ch));
                }
            } else if (mtype == Mangle.Type.SIGNATURE) {
                if (isprint(ch)) {
                    result.append(ch);
                } else {
                    result.append(mangleChar(ch));
                }
            } else {
                result.append(mangleChar(ch));
            }
        }
        return result.toString();
    }
    public String mangleMethod(ExecutableElement method, TypeElement clazz,
                                      int mtype) throws TypeSignature.SignatureException {
        StringBuffer result = new StringBuffer(100);
        result.append("Java_");
        if (mtype == Mangle.Type.METHOD_JDK_1) {
            result.append(mangle(clazz.getQualifiedName(), Mangle.Type.CLASS));
            result.append('_');
            result.append(mangle(method.getSimpleName(),
                                 Mangle.Type.FIELD));
            result.append("_stub");
            return result.toString();
        }
        result.append(mangle(getInnerQualifiedName(clazz), Mangle.Type.JNI));
        result.append('_');
        result.append(mangle(method.getSimpleName(),
                             Mangle.Type.JNI));
        if (mtype == Mangle.Type.METHOD_JNI_LONG) {
            result.append("__");
            String typesig = signature(method);
            TypeSignature newTypeSig = new TypeSignature(elems);
            String sig = newTypeSig.getTypeSignature(typesig,  method.getReturnType());
            sig = sig.substring(1);
            sig = sig.substring(0, sig.lastIndexOf(')'));
            sig = sig.replace('/', '.');
            result.append(mangle(sig, Mangle.Type.JNI));
        }
        return result.toString();
    }
        private String getInnerQualifiedName(TypeElement clazz) {
            return elems.getBinaryName(clazz).toString();
        }
    public final String mangleChar(char ch) {
        String s = Integer.toHexString(ch);
        int nzeros = 5 - s.length();
        char[] result = new char[6];
        result[0] = '_';
        for (int i = 1; i <= nzeros; i++)
            result[i] = '0';
        for (int i = nzeros+1, j = 0; i < 6; i++, j++)
            result[i] = s.charAt(j);
        return new String(result);
    }
    private String signature(ExecutableElement e) {
        StringBuffer sb = new StringBuffer();
        String sep = "(";
        for (VariableElement p: e.getParameters()) {
            sb.append(sep);
            sb.append(types.erasure(p.asType()).toString());
            sep = ",";
        }
        sb.append(")");
        return sb.toString();
    }
    private static final boolean isalnum(char ch) {
        return ch <= 0x7f && 
            ((ch >= 'A' && ch <= 'Z') ||
             (ch >= 'a' && ch <= 'z') ||
             (ch >= '0' && ch <= '9'));
    }
    private static final boolean isprint(char ch) {
        return ch >= 32 && ch <= 126;
    }
}
