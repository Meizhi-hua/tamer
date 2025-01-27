package com.sun.tools.classfile;
import java.io.IOException;
public class Descriptor {
    public class InvalidDescriptor extends DescriptorException {
        private static final long serialVersionUID = 1L;
        InvalidDescriptor(String desc) {
            this.desc = desc;
            this.index = -1;
        }
        InvalidDescriptor(String desc, int index) {
            this.desc = desc;
            this.index = index;
        }
        @Override
        public String getMessage() {
            if (index == -1)
                return "invalid descriptor \"" + desc + "\"";
            else
                return "descriptor is invalid at offset " + index + " in \"" + desc + "\"";
        }
        public final String desc;
        public final int index;
    }
    public Descriptor(ClassReader cr) throws IOException {
        this(cr.readUnsignedShort());
    }
    public Descriptor(int index) {
        this.index = index;
    }
    public String getValue(ConstantPool constant_pool) throws ConstantPoolException {
        return constant_pool.getUTF8Value(index);
    }
    public int getParameterCount(ConstantPool constant_pool)
            throws ConstantPoolException, InvalidDescriptor {
        String desc = getValue(constant_pool);
        int end = desc.indexOf(")");
        if (end == -1)
            throw new InvalidDescriptor(desc);
        parse(desc, 0, end + 1);
        return count;
    }
    public String getParameterTypes(ConstantPool constant_pool)
            throws ConstantPoolException, InvalidDescriptor {
        String desc = getValue(constant_pool);
        int end = desc.indexOf(")");
        if (end == -1)
            throw new InvalidDescriptor(desc);
        return parse(desc, 0, end + 1);
    }
    public String getReturnType(ConstantPool constant_pool)
            throws ConstantPoolException, InvalidDescriptor {
        String desc = getValue(constant_pool);
        int end = desc.indexOf(")");
        if (end == -1)
            throw new InvalidDescriptor(desc);
        return parse(desc, end + 1, desc.length());
    }
    public String getFieldType(ConstantPool constant_pool)
            throws ConstantPoolException, InvalidDescriptor {
        String desc = getValue(constant_pool);
        return parse(desc, 0, desc.length());
    }
    private String parse(String desc, int start, int end)
            throws InvalidDescriptor {
        int p = start;
        StringBuffer sb = new StringBuffer();
        int dims = 0;
        count = 0;
        while (p < end) {
            String type;
            char ch;
            switch (ch = desc.charAt(p++)) {
                case '(':
                    sb.append('(');
                    continue;
                case ')':
                    sb.append(')');
                    continue;
                case '[':
                    dims++;
                    continue;
                case 'B':
                    type = "byte";
                    break;
                case 'C':
                    type = "char";
                    break;
                case 'D':
                    type = "double";
                    break;
                case 'F':
                    type = "float";
                    break;
                case 'I':
                    type = "int";
                    break;
                case 'J':
                    type = "long";
                    break;
                case 'L':
                    int sep = desc.indexOf(';', p);
                    if (sep == -1)
                        throw new InvalidDescriptor(desc, p - 1);
                    type = desc.substring(p, sep).replace('/', '.');
                    p = sep + 1;
                    break;
                case 'S':
                    type = "short";
                    break;
                case 'Z':
                    type = "boolean";
                    break;
                case 'V':
                    type = "void";
                    break;
                default:
                    throw new InvalidDescriptor(desc, p - 1);
            }
            if (sb.length() > 1 && sb.charAt(0) == '(')
                sb.append(", ");
            sb.append(type);
            for ( ; dims > 0; dims-- )
                sb.append("[]");
            count++;
        }
        return sb.toString();
    }
    public final int index;
    private int count;
}
