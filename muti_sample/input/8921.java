public class ByteCodeRewriter
{
    private Method method;
    private ConstantPool cpool;
    private ConstantPoolCache cpCache;
    private byte[] code;
    private Bytes  bytes;
    public static final boolean DEBUG = false;
    private static final int jintSize = 4;
    protected void debugMessage(String message) {
        System.out.println(message);
    }
    public ByteCodeRewriter(Method method, ConstantPool cpool, byte[] code) {
        this.method = method;
        this.cpool = cpool;
        this.cpCache = cpool.getCache();
        this.code = code;
        this.bytes = VM.getVM().getBytes();
    }
    protected short getConstantPoolIndex(int rawcode, int bci) {
       String fmt = Bytecodes.format(rawcode);
       int cpCacheIndex;
       switch (fmt.length()) {
       case 2: cpCacheIndex = method.getBytecodeByteArg(bci); break;
       case 3: cpCacheIndex = method.getBytecodeShortArg(bci); break;
       case 5:
           if (fmt.indexOf("__") >= 0)
               cpCacheIndex = method.getBytecodeShortArg(bci);
           else
               cpCacheIndex = method.getBytecodeIntArg(bci);
           break;
       default: throw new IllegalArgumentException();
       }
       if (cpCache == null) {
          return (short) cpCacheIndex;
       } else if (fmt.indexOf("JJJJ") >= 0) {
          return (short) cpCache.getMainEntryAt(bytes.swapInt(cpCacheIndex)).getConstantPoolIndex();
       } else if (fmt.indexOf("JJ") >= 0) {
          return (short) cpCache.getEntryAt((int) (0xFFFF & bytes.swapShort((short)cpCacheIndex))).getConstantPoolIndex();
       } else if (fmt.indexOf("j") >= 0) {
          return (short) cpCache.getEntryAt((int) (0xFF & cpCacheIndex)).getConstantPoolIndex();
       } else {
          return (short) cpCacheIndex;
       }
    }
    static private void writeShort(byte[] buf, int index, short value) {
        buf[index] = (byte) ((value >> 8) & 0x00FF);
        buf[index + 1] = (byte) (value & 0x00FF);
    }
    public void rewrite() {
        int bytecode = Bytecodes._illegal;
        int hotspotcode = Bytecodes._illegal;
        int len = 0;
        for (int bci = 0; bci < code.length;) {
            hotspotcode = Bytecodes.codeAt(method, bci);
            bytecode = Bytecodes.javaCode(hotspotcode);
            if (Assert.ASSERTS_ENABLED) {
                int code_from_buffer = 0xFF & code[bci];
                Assert.that(code_from_buffer == hotspotcode
                          || code_from_buffer == Bytecodes._breakpoint,
                          "Unexpected bytecode found in method bytecode buffer!");
            }
            code[bci] = (byte) (0xFF & bytecode);
            short cpoolIndex = 0;
            switch (bytecode) {
                case Bytecodes._getstatic:
                case Bytecodes._putstatic:
                case Bytecodes._getfield:
                case Bytecodes._putfield:
                case Bytecodes._invokevirtual:
                case Bytecodes._invokespecial:
                case Bytecodes._invokestatic:
                case Bytecodes._invokeinterface: {
                    cpoolIndex = getConstantPoolIndex(hotspotcode, bci + 1);
                    writeShort(code, bci + 1, cpoolIndex);
                    break;
                }
                case Bytecodes._invokedynamic:
                    cpoolIndex = getConstantPoolIndex(hotspotcode, bci + 1);
                    writeShort(code, bci + 1, cpoolIndex);
                    writeShort(code, bci + 3, (short)0);  
                    break;
                case Bytecodes._ldc_w:
                    if (hotspotcode != bytecode) {
                        cpoolIndex = getConstantPoolIndex(hotspotcode, bci + 1);
                        writeShort(code, bci + 1, cpoolIndex);
                    }
                    break;
                case Bytecodes._ldc:
                    if (hotspotcode != bytecode) {
                        cpoolIndex = getConstantPoolIndex(hotspotcode, bci + 1);
                        code[bci + 1] = (byte)(cpoolIndex);
                    }
                    break;
            }
            len = Bytecodes.lengthFor(bytecode);
            if (len <= 0) len = Bytecodes.lengthAt(method, bci);
            if (DEBUG) {
                String operand = "";
                switch (len) {
                   case 2:
                        operand += code[bci + 1];
                        break;
                   case 3:
                        operand += (cpoolIndex != 0)? cpoolIndex :
                                            method.getBytecodeShortArg(bci + 1);
                        break;
                   case 5:
                        operand += method.getBytecodeIntArg(bci + 1);
                        break;
                }
                String message = "\t\t" + bci + " " + Bytecodes.name(bytecode);
                if (hotspotcode != bytecode)
                    message += " [" + Bytecodes.name(hotspotcode) + "]";
                if (operand != "")
                    message += " #" + operand;
                if (DEBUG) debugMessage(message);
            }
            bci += len;
        }
    }
}
