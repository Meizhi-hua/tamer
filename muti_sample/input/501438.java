public final class BytecodeArray {
    public static final Visitor EMPTY_VISITOR = new BaseVisitor();
    private final ByteArray bytes;
    private final ConstantPool pool;
    public BytecodeArray(ByteArray bytes, ConstantPool pool) {
        if (bytes == null) {
            throw new NullPointerException("bytes == null");
        }
        if (pool == null) {
            throw new NullPointerException("pool == null");
        }
        this.bytes = bytes;
        this.pool = pool;
    }
    public ByteArray getBytes() {
        return bytes;
    }
    public int size() {
        return bytes.size();
    }
    public int byteLength() {
        return 4 + bytes.size();
    }
    public void forEach(Visitor visitor) {
        int sz = bytes.size();
        int at = 0;
        while (at < sz) {
            at += parseInstruction(at, visitor);
        }
    }
    public int[] getInstructionOffsets() {
        int sz = bytes.size();
        int[] result = Bits.makeBitSet(sz);
        int at = 0;
        while (at < sz) {
            Bits.set(result, at, true);
            int length = parseInstruction(at, null);
            at += length;
        }
        return result;
    }
    public void processWorkSet(int[] workSet, Visitor visitor) {
        if (visitor == null) {
            throw new NullPointerException("visitor == null");
        }
        for (;;) {
            int offset = Bits.findFirst(workSet, 0);
            if (offset < 0) {
                break;
            }
            Bits.clear(workSet, offset);
            parseInstruction(offset, visitor);
            visitor.setPreviousOffset(offset);
        }
    }
    public int parseInstruction(int offset, Visitor visitor) {
        if (visitor == null) {
            visitor = EMPTY_VISITOR;
        }
        try {
            int opcode = bytes.getUnsignedByte(offset);
            int info = ByteOps.opInfo(opcode);
            int fmt = info & ByteOps.FMT_MASK;
            switch (opcode) {
                case ByteOps.NOP: {
                    visitor.visitNoArgs(opcode, offset, 1, Type.VOID);
                    return 1;
                }
                case ByteOps.ACONST_NULL: {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstKnownNull.THE_ONE, 0);
                    return 1;
                }
                case ByteOps.ICONST_M1: {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstInteger.VALUE_M1, -1);
                    return 1;
                }
                case ByteOps.ICONST_0: {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstInteger.VALUE_0, 0);
                    return 1;
                }
                case ByteOps.ICONST_1: {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstInteger.VALUE_1, 1);
                    return 1;
                }
                case ByteOps.ICONST_2: {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstInteger.VALUE_2, 2);
                    return 1;
                }
                case ByteOps.ICONST_3: {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstInteger.VALUE_3, 3);
                    return 1;
                }
                case ByteOps.ICONST_4: {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstInteger.VALUE_4, 4);
                    return 1;
                }
                case ByteOps.ICONST_5:  {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstInteger.VALUE_5, 5);
                    return 1;
                }
                case ByteOps.LCONST_0: {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstLong.VALUE_0, 0);
                    return 1;
                }
                case ByteOps.LCONST_1: {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstLong.VALUE_1, 0);
                    return 1;
                }
                case ByteOps.FCONST_0: {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstFloat.VALUE_0, 0);
                    return 1;
                }
                case ByteOps.FCONST_1: {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstFloat.VALUE_1, 0);
                    return 1;
                }
                case ByteOps.FCONST_2:  {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstFloat.VALUE_2, 0);
                    return 1;
                }
                case ByteOps.DCONST_0: {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstDouble.VALUE_0, 0);
                    return 1;
                }
                case ByteOps.DCONST_1: {
                    visitor.visitConstant(ByteOps.LDC, offset, 1,
                                          CstDouble.VALUE_1, 0);
                    return 1;
                }
                case ByteOps.BIPUSH: {
                    int value = bytes.getByte(offset + 1);
                    visitor.visitConstant(ByteOps.LDC, offset, 2,
                                          CstInteger.make(value), value);
                    return 2;
                }
                case ByteOps.SIPUSH: {
                    int value = bytes.getShort(offset + 1);
                    visitor.visitConstant(ByteOps.LDC, offset, 3,
                                          CstInteger.make(value), value);
                    return 3;
                }
                case ByteOps.LDC: {
                    int idx = bytes.getUnsignedByte(offset + 1);
                    Constant cst = pool.get(idx);
                    int value = (cst instanceof CstInteger) ? 
                        ((CstInteger) cst).getValue() : 0;
                    visitor.visitConstant(ByteOps.LDC, offset, 2, cst, value);
                    return 2;
                }
                case ByteOps.LDC_W: {
                    int idx = bytes.getUnsignedShort(offset + 1);
                    Constant cst = pool.get(idx);
                    int value = (cst instanceof CstInteger) ? 
                        ((CstInteger) cst).getValue() : 0;
                    visitor.visitConstant(ByteOps.LDC, offset, 3, cst, value);
                    return 3;
                }
                case ByteOps.LDC2_W: {
                    int idx = bytes.getUnsignedShort(offset + 1);
                    Constant cst = pool.get(idx);
                    visitor.visitConstant(ByteOps.LDC2_W, offset, 3, cst, 0);
                    return 3;
                }
                case ByteOps.ILOAD: {
                    int idx = bytes.getUnsignedByte(offset + 1);
                    visitor.visitLocal(ByteOps.ILOAD, offset, 2, idx,
                                       Type.INT, 0);
                    return 2;
                }
                case ByteOps.LLOAD: {
                    int idx = bytes.getUnsignedByte(offset + 1);
                    visitor.visitLocal(ByteOps.ILOAD, offset, 2, idx,
                                       Type.LONG, 0);
                    return 2;
                }
                case ByteOps.FLOAD: {
                    int idx = bytes.getUnsignedByte(offset + 1);
                    visitor.visitLocal(ByteOps.ILOAD, offset, 2, idx,
                                       Type.FLOAT, 0);
                    return 2;
                }
                case ByteOps.DLOAD: {
                    int idx = bytes.getUnsignedByte(offset + 1);
                    visitor.visitLocal(ByteOps.ILOAD, offset, 2, idx,
                                       Type.DOUBLE, 0);
                    return 2;
                }
                case ByteOps.ALOAD: {
                    int idx = bytes.getUnsignedByte(offset + 1);
                    visitor.visitLocal(ByteOps.ILOAD, offset, 2, idx,
                                       Type.OBJECT, 0);
                    return 2;
                }
                case ByteOps.ILOAD_0:
                case ByteOps.ILOAD_1:
                case ByteOps.ILOAD_2:
                case ByteOps.ILOAD_3: {
                    int idx = opcode - ByteOps.ILOAD_0;
                    visitor.visitLocal(ByteOps.ILOAD, offset, 1, idx,
                                       Type.INT, 0);
                    return 1;
                }
                case ByteOps.LLOAD_0:
                case ByteOps.LLOAD_1:
                case ByteOps.LLOAD_2:
                case ByteOps.LLOAD_3: {
                    int idx = opcode - ByteOps.LLOAD_0;
                    visitor.visitLocal(ByteOps.ILOAD, offset, 1, idx,
                                       Type.LONG, 0);
                    return 1;
                }
                case ByteOps.FLOAD_0:
                case ByteOps.FLOAD_1:
                case ByteOps.FLOAD_2:
                case ByteOps.FLOAD_3: {
                    int idx = opcode - ByteOps.FLOAD_0;
                    visitor.visitLocal(ByteOps.ILOAD, offset, 1, idx,
                                       Type.FLOAT, 0);
                    return 1;
                }
                case ByteOps.DLOAD_0:
                case ByteOps.DLOAD_1:
                case ByteOps.DLOAD_2:
                case ByteOps.DLOAD_3: {
                    int idx = opcode - ByteOps.DLOAD_0;
                    visitor.visitLocal(ByteOps.ILOAD, offset, 1, idx,
                                       Type.DOUBLE, 0);
                    return 1;
                }
                case ByteOps.ALOAD_0:
                case ByteOps.ALOAD_1:
                case ByteOps.ALOAD_2:
                case ByteOps.ALOAD_3: {
                    int idx = opcode - ByteOps.ALOAD_0;
                    visitor.visitLocal(ByteOps.ILOAD, offset, 1, idx,
                                       Type.OBJECT, 0);
                    return 1;
                }
                case ByteOps.IALOAD: {
                    visitor.visitNoArgs(ByteOps.IALOAD, offset, 1, Type.INT);
                    return 1;
                }
                case ByteOps.LALOAD: {
                    visitor.visitNoArgs(ByteOps.IALOAD, offset, 1, Type.LONG);
                    return 1;
                }
                case ByteOps.FALOAD: {
                    visitor.visitNoArgs(ByteOps.IALOAD, offset, 1,
                                        Type.FLOAT);
                    return 1;
                }
                case ByteOps.DALOAD: {
                    visitor.visitNoArgs(ByteOps.IALOAD, offset, 1,
                                        Type.DOUBLE);
                    return 1;
                }
                case ByteOps.AALOAD: {
                    visitor.visitNoArgs(ByteOps.IALOAD, offset, 1,
                                        Type.OBJECT);
                    return 1;
                }
                case ByteOps.BALOAD: {
                    visitor.visitNoArgs(ByteOps.IALOAD, offset, 1, Type.BYTE);
                    return 1;
                }
                case ByteOps.CALOAD: {
                    visitor.visitNoArgs(ByteOps.IALOAD, offset, 1, Type.CHAR);
                    return 1;
                }
                case ByteOps.SALOAD: {
                    visitor.visitNoArgs(ByteOps.IALOAD, offset, 1,
                                        Type.SHORT);
                    return 1;
                }
                case ByteOps.ISTORE: {
                    int idx = bytes.getUnsignedByte(offset + 1);
                    visitor.visitLocal(ByteOps.ISTORE, offset, 2, idx,
                                       Type.INT, 0);
                    return 2;
                }
                case ByteOps.LSTORE: {
                    int idx = bytes.getUnsignedByte(offset + 1);
                    visitor.visitLocal(ByteOps.ISTORE, offset, 2, idx,
                                       Type.LONG, 0);
                    return 2;
                }
                case ByteOps.FSTORE: {
                    int idx = bytes.getUnsignedByte(offset + 1);
                    visitor.visitLocal(ByteOps.ISTORE, offset, 2, idx,
                                       Type.FLOAT, 0);
                    return 2;
                }
                case ByteOps.DSTORE: {
                    int idx = bytes.getUnsignedByte(offset + 1);
                    visitor.visitLocal(ByteOps.ISTORE, offset, 2, idx,
                                       Type.DOUBLE, 0);
                    return 2;
                }
                case ByteOps.ASTORE: {
                    int idx = bytes.getUnsignedByte(offset + 1);
                    visitor.visitLocal(ByteOps.ISTORE, offset, 2, idx,
                                       Type.OBJECT, 0);
                    return 2;
                }
                case ByteOps.ISTORE_0:
                case ByteOps.ISTORE_1:
                case ByteOps.ISTORE_2:
                case ByteOps.ISTORE_3: {
                    int idx = opcode - ByteOps.ISTORE_0;
                    visitor.visitLocal(ByteOps.ISTORE, offset, 1, idx,
                                       Type.INT, 0);
                    return 1;
                }
                case ByteOps.LSTORE_0:
                case ByteOps.LSTORE_1:
                case ByteOps.LSTORE_2:
                case ByteOps.LSTORE_3: {
                    int idx = opcode - ByteOps.LSTORE_0;
                    visitor.visitLocal(ByteOps.ISTORE, offset, 1, idx,
                                       Type.LONG, 0);
                    return 1;
                }
                case ByteOps.FSTORE_0:
                case ByteOps.FSTORE_1:
                case ByteOps.FSTORE_2:
                case ByteOps.FSTORE_3: {
                    int idx = opcode - ByteOps.FSTORE_0;
                    visitor.visitLocal(ByteOps.ISTORE, offset, 1, idx,
                                       Type.FLOAT, 0);
                    return 1;
                }
                case ByteOps.DSTORE_0:
                case ByteOps.DSTORE_1:
                case ByteOps.DSTORE_2:
                case ByteOps.DSTORE_3: {
                    int idx = opcode - ByteOps.DSTORE_0;
                    visitor.visitLocal(ByteOps.ISTORE, offset, 1, idx,
                                       Type.DOUBLE, 0);
                    return 1;
                }
                case ByteOps.ASTORE_0:
                case ByteOps.ASTORE_1:
                case ByteOps.ASTORE_2:
                case ByteOps.ASTORE_3: {
                    int idx = opcode - ByteOps.ASTORE_0;
                    visitor.visitLocal(ByteOps.ISTORE, offset, 1, idx,
                                       Type.OBJECT, 0);
                    return 1;
                }
                case ByteOps.IASTORE: {
                    visitor.visitNoArgs(ByteOps.IASTORE, offset, 1, Type.INT);
                    return 1;
                }
                case ByteOps.LASTORE: {
                    visitor.visitNoArgs(ByteOps.IASTORE, offset, 1,
                                        Type.LONG);
                    return 1;
                }
                case ByteOps.FASTORE: {
                    visitor.visitNoArgs(ByteOps.IASTORE, offset, 1,
                                        Type.FLOAT);
                    return 1;
                }
                case ByteOps.DASTORE: {
                    visitor.visitNoArgs(ByteOps.IASTORE, offset, 1,
                                        Type.DOUBLE);
                    return 1;
                }
                case ByteOps.AASTORE: {
                    visitor.visitNoArgs(ByteOps.IASTORE, offset, 1,
                                        Type.OBJECT);
                    return 1;
                }
                case ByteOps.BASTORE: {
                    visitor.visitNoArgs(ByteOps.IASTORE, offset, 1,
                                        Type.BYTE);
                    return 1;
                }
                case ByteOps.CASTORE: {
                    visitor.visitNoArgs(ByteOps.IASTORE, offset, 1,
                                        Type.CHAR);
                    return 1;
                }
                case ByteOps.SASTORE: {
                    visitor.visitNoArgs(ByteOps.IASTORE, offset, 1,
                                        Type.SHORT);
                    return 1;
                }
                case ByteOps.POP:
                case ByteOps.POP2:
                case ByteOps.DUP:
                case ByteOps.DUP_X1:
                case ByteOps.DUP_X2:
                case ByteOps.DUP2:
                case ByteOps.DUP2_X1:
                case ByteOps.DUP2_X2:
                case ByteOps.SWAP: {
                    visitor.visitNoArgs(opcode, offset, 1, Type.VOID);
                    return 1;
                }
                case ByteOps.IADD:
                case ByteOps.ISUB:
                case ByteOps.IMUL:
                case ByteOps.IDIV:
                case ByteOps.IREM:
                case ByteOps.INEG:
                case ByteOps.ISHL:
                case ByteOps.ISHR:
                case ByteOps.IUSHR:
                case ByteOps.IAND:
                case ByteOps.IOR:
                case ByteOps.IXOR: {
                    visitor.visitNoArgs(opcode, offset, 1, Type.INT);
                    return 1;
                }
                case ByteOps.LADD:
                case ByteOps.LSUB:
                case ByteOps.LMUL:
                case ByteOps.LDIV:
                case ByteOps.LREM:
                case ByteOps.LNEG:
                case ByteOps.LSHL:
                case ByteOps.LSHR:
                case ByteOps.LUSHR:
                case ByteOps.LAND:
                case ByteOps.LOR:
                case ByteOps.LXOR: {
                    visitor.visitNoArgs(opcode - 1, offset, 1, Type.LONG);
                    return 1;
                }
                case ByteOps.FADD:
                case ByteOps.FSUB:
                case ByteOps.FMUL:
                case ByteOps.FDIV:
                case ByteOps.FREM:
                case ByteOps.FNEG: {
                    visitor.visitNoArgs(opcode - 2, offset, 1, Type.FLOAT);
                    return 1;
                }
                case ByteOps.DADD:
                case ByteOps.DSUB:
                case ByteOps.DMUL:
                case ByteOps.DDIV:
                case ByteOps.DREM:
                case ByteOps.DNEG: {
                    visitor.visitNoArgs(opcode - 3, offset, 1, Type.DOUBLE);
                    return 1;
                }
                case ByteOps.IINC: {
                    int idx = bytes.getUnsignedByte(offset + 1);
                    int value = bytes.getByte(offset + 2);
                    visitor.visitLocal(opcode, offset, 3, idx,
                                       Type.INT, value);
                    return 3;
                }
                case ByteOps.I2L:
                case ByteOps.F2L:
                case ByteOps.D2L: {
                    visitor.visitNoArgs(opcode, offset, 1, Type.LONG);
                    return 1;
                }
                case ByteOps.I2F:
                case ByteOps.L2F:
                case ByteOps.D2F: {
                    visitor.visitNoArgs(opcode, offset, 1, Type.FLOAT);
                    return 1;
                }
                case ByteOps.I2D:
                case ByteOps.L2D:
                case ByteOps.F2D: {
                    visitor.visitNoArgs(opcode, offset, 1, Type.DOUBLE);
                    return 1;
                }
                case ByteOps.L2I:
                case ByteOps.F2I:
                case ByteOps.D2I:
                case ByteOps.I2B:
                case ByteOps.I2C:
                case ByteOps.I2S:
                case ByteOps.LCMP:
                case ByteOps.FCMPL:
                case ByteOps.FCMPG:
                case ByteOps.DCMPL:
                case ByteOps.DCMPG:
                case ByteOps.ARRAYLENGTH: {
                    visitor.visitNoArgs(opcode, offset, 1, Type.INT);
                    return 1;
                }
                case ByteOps.IFEQ:
                case ByteOps.IFNE:
                case ByteOps.IFLT:
                case ByteOps.IFGE:
                case ByteOps.IFGT:
                case ByteOps.IFLE:
                case ByteOps.IF_ICMPEQ:
                case ByteOps.IF_ICMPNE:
                case ByteOps.IF_ICMPLT:
                case ByteOps.IF_ICMPGE:
                case ByteOps.IF_ICMPGT:
                case ByteOps.IF_ICMPLE:
                case ByteOps.IF_ACMPEQ:
                case ByteOps.IF_ACMPNE:
                case ByteOps.GOTO:
                case ByteOps.JSR:
                case ByteOps.IFNULL:
                case ByteOps.IFNONNULL: {
                    int target = offset + bytes.getShort(offset + 1);
                    visitor.visitBranch(opcode, offset, 3, target);
                    return 3;
                }
                case ByteOps.RET: {
                    int idx = bytes.getUnsignedByte(offset + 1);
                    visitor.visitLocal(opcode, offset, 2, idx,
                                       Type.RETURN_ADDRESS, 0);
                    return 2;
                }
                case ByteOps.TABLESWITCH: {
                    return parseTableswitch(offset, visitor);
                }
                case ByteOps.LOOKUPSWITCH: {
                    return parseLookupswitch(offset, visitor);
                }
                case ByteOps.IRETURN: {
                    visitor.visitNoArgs(ByteOps.IRETURN, offset, 1, Type.INT);
                    return 1;
                }
                case ByteOps.LRETURN: {
                    visitor.visitNoArgs(ByteOps.IRETURN, offset, 1,
                                        Type.LONG);
                    return 1;
                }
                case ByteOps.FRETURN: {
                    visitor.visitNoArgs(ByteOps.IRETURN, offset, 1,
                                        Type.FLOAT);
                    return 1;
                }
                case ByteOps.DRETURN: {
                    visitor.visitNoArgs(ByteOps.IRETURN, offset, 1,
                                        Type.DOUBLE);
                    return 1;
                }
                case ByteOps.ARETURN: {
                    visitor.visitNoArgs(ByteOps.IRETURN, offset, 1,
                                        Type.OBJECT);
                    return 1;
                }
                case ByteOps.RETURN:
                case ByteOps.ATHROW:
                case ByteOps.MONITORENTER:
                case ByteOps.MONITOREXIT: {
                    visitor.visitNoArgs(opcode, offset, 1, Type.VOID);
                    return 1;
                }
                case ByteOps.GETSTATIC:
                case ByteOps.PUTSTATIC:
                case ByteOps.GETFIELD:
                case ByteOps.PUTFIELD:
                case ByteOps.INVOKEVIRTUAL:
                case ByteOps.INVOKESPECIAL:
                case ByteOps.INVOKESTATIC:
                case ByteOps.NEW:
                case ByteOps.ANEWARRAY:
                case ByteOps.CHECKCAST:
                case ByteOps.INSTANCEOF: {
                    int idx = bytes.getUnsignedShort(offset + 1);
                    Constant cst = pool.get(idx);
                    visitor.visitConstant(opcode, offset, 3, cst, 0);
                    return 3;
                }
                case ByteOps.INVOKEINTERFACE: {
                    int idx = bytes.getUnsignedShort(offset + 1);
                    int count = bytes.getUnsignedByte(offset + 3);
                    int expectZero = bytes.getUnsignedByte(offset + 4);
                    Constant cst = pool.get(idx);
                    visitor.visitConstant(opcode, offset, 5, cst,
                                          count | (expectZero << 8));
                    return 5;
                }
                case ByteOps.NEWARRAY: {
                    return parseNewarray(offset, visitor);
                }
                case ByteOps.WIDE: {
                    return parseWide(offset, visitor);
                }
                case ByteOps.MULTIANEWARRAY: {
                    int idx = bytes.getUnsignedShort(offset + 1);
                    int dimensions = bytes.getUnsignedByte(offset + 3);
                    Constant cst = pool.get(idx);
                    visitor.visitConstant(opcode, offset, 4, cst, dimensions);
                    return 4;
                }
                case ByteOps.GOTO_W:
                case ByteOps.JSR_W: {
                    int target = offset + bytes.getInt(offset + 1);
                    int newop =
                        (opcode == ByteOps.GOTO_W) ? ByteOps.GOTO :
                        ByteOps.JSR;
                    visitor.visitBranch(newop, offset, 5, target);
                    return 5;
                }
                default: {
                    visitor.visitInvalid(opcode, offset, 1);
                    return 1;
                }
            }
        } catch (SimException ex) {
            ex.addContext("...at bytecode offset " + Hex.u4(offset));
            throw ex;
        } catch (RuntimeException ex) {
            SimException se = new SimException(ex);
            se.addContext("...at bytecode offset " + Hex.u4(offset));
            throw se;
        }
    }
    private int parseTableswitch(int offset, Visitor visitor) {
        int at = (offset + 4) & ~3; 
        int padding = 0;
        for (int i = offset + 1; i < at; i++) {
            padding = (padding << 8) | bytes.getUnsignedByte(i);
        }
        int defaultTarget = offset + bytes.getInt(at);
        int low = bytes.getInt(at + 4);
        int high = bytes.getInt(at + 8);
        int count = high - low + 1;
        at += 12;
        if (low > high) {
            throw new SimException("low / high inversion");
        }
        SwitchList cases = new SwitchList(count);
        for (int i = 0; i < count; i++) {
            int target = offset + bytes.getInt(at);
            at += 4;
            cases.add(low + i, target);
        }
        cases.setDefaultTarget(defaultTarget);
        cases.removeSuperfluousDefaults();
        cases.setImmutable();
        int length = at - offset;
        visitor.visitSwitch(ByteOps.LOOKUPSWITCH, offset, length, cases,
                            padding);
        return length;
    }
    private int parseLookupswitch(int offset, Visitor visitor) {
        int at = (offset + 4) & ~3; 
        int padding = 0;
        for (int i = offset + 1; i < at; i++) {
            padding = (padding << 8) | bytes.getUnsignedByte(i);
        }
        int defaultTarget = offset + bytes.getInt(at);
        int npairs = bytes.getInt(at + 4);
        at += 8;
        SwitchList cases = new SwitchList(npairs);
        for (int i = 0; i < npairs; i++) {
            int match = bytes.getInt(at);
            int target = offset + bytes.getInt(at + 4);
            at += 8;
            cases.add(match, target);
        }
        cases.setDefaultTarget(defaultTarget);
        cases.removeSuperfluousDefaults();
        cases.setImmutable();
        int length = at - offset;
        visitor.visitSwitch(ByteOps.LOOKUPSWITCH, offset, length, cases,
                            padding);
        return length;
    }
    private int parseNewarray(int offset, Visitor visitor) {
        int value = bytes.getUnsignedByte(offset + 1);
        CstType type;
        switch (value) {
            case ByteOps.NEWARRAY_BOOLEAN: {
                type = CstType.BOOLEAN_ARRAY;
                break;
            }
            case ByteOps.NEWARRAY_CHAR: {
                type = CstType.CHAR_ARRAY;
                break;
            }
            case ByteOps.NEWARRAY_DOUBLE: {
                type = CstType.DOUBLE_ARRAY;
                break;
            }
            case ByteOps.NEWARRAY_FLOAT: {
                type = CstType.FLOAT_ARRAY;
                break;
            }
            case ByteOps.NEWARRAY_BYTE: {
                type = CstType.BYTE_ARRAY;
                break;
            }
            case ByteOps.NEWARRAY_SHORT: {
                type = CstType.SHORT_ARRAY;
                break;
            }
            case ByteOps.NEWARRAY_INT: {
                type = CstType.INT_ARRAY;
                break;
            }
            case ByteOps.NEWARRAY_LONG: {
                type = CstType.LONG_ARRAY;
                break;
            }
            default: {
                throw new SimException("bad newarray code " +
                        Hex.u1(value));
            }
        }
        int previousOffset = visitor.getPreviousOffset();
        ConstantParserVisitor constantVisitor = new ConstantParserVisitor();
        int arrayLength = 0;
        if (previousOffset >= 0) {
            parseInstruction(previousOffset, constantVisitor);
            if (constantVisitor.cst instanceof CstInteger &&
                    constantVisitor.length + previousOffset == offset) {
                arrayLength = constantVisitor.value;
            }
        }
        int nInit = 0;
        int curOffset = offset+2;
        int lastOffset = curOffset;
        ArrayList<Constant> initVals = new ArrayList<Constant>();
        if (arrayLength != 0) {
            while (true) {
                boolean punt = false;
                int nextByte = bytes.getUnsignedByte(curOffset++);
                if (nextByte != ByteOps.DUP)
                    break;
                parseInstruction(curOffset, constantVisitor);
                if (constantVisitor.length == 0 ||
                        !(constantVisitor.cst instanceof CstInteger) ||
                        constantVisitor.value != nInit)
                    break;
                curOffset += constantVisitor.length;
                parseInstruction(curOffset, constantVisitor);
                if (constantVisitor.length == 0 ||
                        !(constantVisitor.cst instanceof CstLiteralBits))
                    break;
                curOffset += constantVisitor.length;
                initVals.add(constantVisitor.cst);
                nextByte = bytes.getUnsignedByte(curOffset++);
                switch (value) {
                    case ByteOps.NEWARRAY_BYTE:
                    case ByteOps.NEWARRAY_BOOLEAN: {
                        if (nextByte != ByteOps.BASTORE) {
                            punt = true;
                        }
                        break;
                    }
                    case ByteOps.NEWARRAY_CHAR: {
                        if (nextByte != ByteOps.CASTORE) {
                            punt = true;
                        }
                        break;
                    }
                    case ByteOps.NEWARRAY_DOUBLE: {
                        if (nextByte != ByteOps.DASTORE) {
                            punt = true;
                        }
                        break;
                    }
                    case ByteOps.NEWARRAY_FLOAT: {
                        if (nextByte != ByteOps.FASTORE) {
                            punt = true;
                        }
                        break;
                    }
                    case ByteOps.NEWARRAY_SHORT: {
                        if (nextByte != ByteOps.SASTORE) {
                            punt = true;
                        }
                        break;
                    }
                    case ByteOps.NEWARRAY_INT: {
                        if (nextByte != ByteOps.IASTORE) {
                            punt = true;
                        }
                        break;
                    }
                    case ByteOps.NEWARRAY_LONG: {
                        if (nextByte != ByteOps.LASTORE) {
                            punt = true;
                        }
                        break;
                    }
                    default:
                        punt = true;
                        break;
                }
                if (punt) {
                    break;
                }
                lastOffset = curOffset;
                nInit++;
            }
        }
        if (nInit < 2 || nInit != arrayLength) {
            visitor.visitNewarray(offset, 2, type, null);
            return 2;
        } else {
            visitor.visitNewarray(offset, lastOffset - offset, type, initVals);
            return lastOffset - offset;
        }
     }
    private int parseWide(int offset, Visitor visitor) {
        int opcode = bytes.getUnsignedByte(offset + 1);
        int idx = bytes.getUnsignedShort(offset + 2);
        switch (opcode) {
            case ByteOps.ILOAD: {
                visitor.visitLocal(ByteOps.ILOAD, offset, 4, idx,
                                   Type.INT, 0);
                return 4;
            }
            case ByteOps.LLOAD: {
                visitor.visitLocal(ByteOps.ILOAD, offset, 4, idx,
                                   Type.LONG, 0);
                return 4;
            }
            case ByteOps.FLOAD: {
                visitor.visitLocal(ByteOps.ILOAD, offset, 4, idx,
                                   Type.FLOAT, 0);
                return 4;
            }
            case ByteOps.DLOAD: {
                visitor.visitLocal(ByteOps.ILOAD, offset, 4, idx,
                                   Type.DOUBLE, 0);
                return 4;
            }
            case ByteOps.ALOAD: {
                visitor.visitLocal(ByteOps.ILOAD, offset, 4, idx,
                                   Type.OBJECT, 0);
                return 4;
            }
            case ByteOps.ISTORE: {
                visitor.visitLocal(ByteOps.ISTORE, offset, 4, idx,
                                   Type.INT, 0);
                return 4;
            }
            case ByteOps.LSTORE: {
                visitor.visitLocal(ByteOps.ISTORE, offset, 4, idx,
                                   Type.LONG, 0);
                return 4;
            }
            case ByteOps.FSTORE: {
                visitor.visitLocal(ByteOps.ISTORE, offset, 4, idx,
                                   Type.FLOAT, 0);
                return 4;
            }
            case ByteOps.DSTORE: {
                visitor.visitLocal(ByteOps.ISTORE, offset, 4, idx,
                                   Type.DOUBLE, 0);
                return 4;
            }
            case ByteOps.ASTORE: {
                visitor.visitLocal(ByteOps.ISTORE, offset, 4, idx,
                                   Type.OBJECT, 0);
                return 4;
            }
            case ByteOps.RET: {
                visitor.visitLocal(opcode, offset, 4, idx,
                                   Type.RETURN_ADDRESS, 0);
                return 4;
            }
            case ByteOps.IINC: {
                int value = bytes.getShort(offset + 4);
                visitor.visitLocal(opcode, offset, 6, idx,
                                   Type.INT, value);
                return 6;
            }
            default: {
                visitor.visitInvalid(ByteOps.WIDE, offset, 1);
                return 1;
            }
        }
    }
    public interface Visitor {
        public void visitInvalid(int opcode, int offset, int length);
        public void visitNoArgs(int opcode, int offset, int length,
                Type type);
        public void visitLocal(int opcode, int offset, int length,
                int idx, Type type, int value);
        public void visitConstant(int opcode, int offset, int length,
                Constant cst, int value);
        public void visitBranch(int opcode, int offset, int length,
                int target);
        public void visitSwitch(int opcode, int offset, int length,
                SwitchList cases, int padding);
        public void visitNewarray(int offset, int length, CstType type,
                ArrayList<Constant> initVals);
        public void setPreviousOffset(int offset);
        public int getPreviousOffset();
    }
    public static class BaseVisitor implements Visitor {
        private int previousOffset;
        BaseVisitor() {
            previousOffset = -1;
        }
        public void visitInvalid(int opcode, int offset, int length) {
        }
        public void visitNoArgs(int opcode, int offset, int length,
                Type type) {
        }
        public void visitLocal(int opcode, int offset, int length,
                int idx, Type type, int value) {
        }
        public void visitConstant(int opcode, int offset, int length,
                Constant cst, int value) {
        }
        public void visitBranch(int opcode, int offset, int length,
                int target) {
        }
        public void visitSwitch(int opcode, int offset, int length,
                SwitchList cases, int padding) {
        }
        public void visitNewarray(int offset, int length, CstType type,
                ArrayList<Constant> initValues) {
        }
        public void setPreviousOffset(int offset) {
            previousOffset = offset;
        }
        public int getPreviousOffset() {
            return previousOffset;
        }
    }
    class ConstantParserVisitor extends BaseVisitor {
        Constant cst;
        int length;
        int value;
        ConstantParserVisitor() {
        }
        private void clear() {
            length = 0;
        }
        public void visitInvalid(int opcode, int offset, int length) {
            clear();
        }
        public void visitNoArgs(int opcode, int offset, int length,
                Type type) {
            clear();
        }
        public void visitLocal(int opcode, int offset, int length,
                int idx, Type type, int value) {
            clear();
        }
        public void visitConstant(int opcode, int offset, int length,
                Constant cst, int value) {
            this.cst = cst;
            this.length = length;
            this.value = value;
        }
        public void visitBranch(int opcode, int offset, int length,
                int target) {
            clear();
        }
        public void visitSwitch(int opcode, int offset, int length,
                SwitchList cases, int padding) {
            clear();
        }
        public void visitNewarray(int offset, int length, CstType type,
                ArrayList<Constant> initVals) {
            clear();
        }
        public void setPreviousOffset(int offset) {
        }
        public int getPreviousOffset() {
            return -1;
        }
    }
}
