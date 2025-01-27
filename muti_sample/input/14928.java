public class Code {
    public final boolean debugCode;
    public final boolean needStackMap;
    public enum StackMapFormat {
        NONE,
        CLDC {
            Name getAttributeName(Names names) {
                return names.StackMap;
            }
        },
        JSR202 {
            Name getAttributeName(Names names) {
                return names.StackMapTable;
            }
        };
        Name getAttributeName(Names names) {
            return names.empty;
        }
    }
    final Types types;
    final Symtab syms;
    public int max_stack = 0;
    public int max_locals = 0;
    public byte[] code = new byte[64];
    public int cp = 0;
    public boolean checkLimits(DiagnosticPosition pos, Log log) {
        if (cp > ClassFile.MAX_CODE) {
            log.error(pos, "limit.code");
            return true;
        }
        if (max_locals > ClassFile.MAX_LOCALS) {
            log.error(pos, "limit.locals");
            return true;
        }
        if (max_stack > ClassFile.MAX_STACK) {
            log.error(pos, "limit.stack");
            return true;
        }
        return false;
    }
    ListBuffer<char[]> catchInfo = new ListBuffer<char[]>();
    List<char[]> lineInfo = List.nil(); 
    public CRTable crt;
    public boolean fatcode;
    private boolean alive = true;
    State state;
    private boolean fixedPc = false;
    public int nextreg = 0;
    Chain pendingJumps = null;
    int pendingStatPos = Position.NOPOS;
    boolean pendingStackMap = false;
    StackMapFormat stackMap;
    boolean varDebugInfo;
    boolean lineDebugInfo;
    Position.LineMap lineMap;
    final Pool pool;
    final MethodSymbol meth;
    public Code(MethodSymbol meth,
                boolean fatcode,
                Position.LineMap lineMap,
                boolean varDebugInfo,
                StackMapFormat stackMap,
                boolean debugCode,
                CRTable crt,
                Symtab syms,
                Types types,
                Pool pool) {
        this.meth = meth;
        this.fatcode = fatcode;
        this.lineMap = lineMap;
        this.lineDebugInfo = lineMap != null;
        this.varDebugInfo = varDebugInfo;
        this.crt = crt;
        this.syms = syms;
        this.types = types;
        this.debugCode = debugCode;
        this.stackMap = stackMap;
        switch (stackMap) {
        case CLDC:
        case JSR202:
            this.needStackMap = true;
            break;
        default:
            this.needStackMap = false;
        }
        state = new State();
        lvar = new LocalVar[20];
        this.pool = pool;
    }
    public static int typecode(Type type) {
        switch (type.tag) {
        case BYTE: return BYTEcode;
        case SHORT: return SHORTcode;
        case CHAR: return CHARcode;
        case INT: return INTcode;
        case LONG: return LONGcode;
        case FLOAT: return FLOATcode;
        case DOUBLE: return DOUBLEcode;
        case BOOLEAN: return BYTEcode;
        case VOID: return VOIDcode;
        case CLASS:
        case ARRAY:
        case METHOD:
        case BOT:
        case TYPEVAR:
        case UNINITIALIZED_THIS:
        case UNINITIALIZED_OBJECT:
            return OBJECTcode;
        default: throw new AssertionError("typecode " + type.tag);
        }
    }
    public static int truncate(int tc) {
        switch (tc) {
        case BYTEcode: case SHORTcode: case CHARcode: return INTcode;
        default: return tc;
        }
    }
    public static int width(int typecode) {
        switch (typecode) {
        case LONGcode: case DOUBLEcode: return 2;
        case VOIDcode: return 0;
        default: return 1;
        }
    }
    public static int width(Type type) {
        return type == null ? 1 : width(typecode(type));
    }
    public static int width(List<Type> types) {
        int w = 0;
        for (List<Type> l = types; l.nonEmpty(); l = l.tail)
            w = w + width(l.head);
        return w;
    }
    public static int arraycode(Type type) {
        switch (type.tag) {
        case BYTE: return 8;
        case BOOLEAN: return 4;
        case SHORT: return 9;
        case CHAR: return 5;
        case INT: return 10;
        case LONG: return 11;
        case FLOAT: return 6;
        case DOUBLE: return 7;
        case CLASS: return 0;
        case ARRAY: return 1;
        default: throw new AssertionError("arraycode " + type);
        }
    }
    public int curPc() {
        if (pendingJumps != null) resolvePending();
        if (pendingStatPos != Position.NOPOS) markStatBegin();
        fixedPc = true;
        return cp;
    }
    private  void emit1(int od) {
        if (!alive) return;
        if (cp == code.length) {
            byte[] newcode = new byte[cp * 2];
            System.arraycopy(code, 0, newcode, 0, cp);
            code = newcode;
        }
        code[cp++] = (byte)od;
    }
    private void emit2(int od) {
        if (!alive) return;
        if (cp + 2 > code.length) {
            emit1(od >> 8);
            emit1(od);
        } else {
            code[cp++] = (byte)(od >> 8);
            code[cp++] = (byte)od;
        }
    }
    public void emit4(int od) {
        if (!alive) return;
        if (cp + 4 > code.length) {
            emit1(od >> 24);
            emit1(od >> 16);
            emit1(od >> 8);
            emit1(od);
        } else {
            code[cp++] = (byte)(od >> 24);
            code[cp++] = (byte)(od >> 16);
            code[cp++] = (byte)(od >> 8);
            code[cp++] = (byte)od;
        }
    }
    private void emitop(int op) {
        if (pendingJumps != null) resolvePending();
        if (alive) {
            if (pendingStatPos != Position.NOPOS)
                markStatBegin();
            if (pendingStackMap) {
                pendingStackMap = false;
                emitStackMap();
            }
            if (debugCode)
                System.err.println("emit@" + cp + " stack=" +
                                   state.stacksize + ": " +
                                   mnem(op));
            emit1(op);
        }
    }
    void postop() {
        Assert.check(alive || state.stacksize == 0);
    }
    public void emitMultianewarray(int ndims, int type, Type arrayType) {
        emitop(multianewarray);
        if (!alive) return;
        emit2(type);
        emit1(ndims);
        state.pop(ndims);
        state.push(arrayType);
    }
    public void emitNewarray(int elemcode, Type arrayType) {
        emitop(newarray);
        if (!alive) return;
        emit1(elemcode);
        state.pop(1); 
        state.push(arrayType);
    }
    public void emitAnewarray(int od, Type arrayType) {
        emitop(anewarray);
        if (!alive) return;
        emit2(od);
        state.pop(1);
        state.push(arrayType);
    }
    public void emitInvokeinterface(int meth, Type mtype) {
        int argsize = width(mtype.getParameterTypes());
        emitop(invokeinterface);
        if (!alive) return;
        emit2(meth);
        emit1(argsize + 1);
        emit1(0);
        state.pop(argsize + 1);
        state.push(mtype.getReturnType());
    }
    public void emitInvokespecial(int meth, Type mtype) {
        int argsize = width(mtype.getParameterTypes());
        emitop(invokespecial);
        if (!alive) return;
        emit2(meth);
        Symbol sym = (Symbol)pool.pool[meth];
        state.pop(argsize);
        if (sym.isConstructor())
            state.markInitialized((UninitializedType)state.peek());
        state.pop(1);
        state.push(mtype.getReturnType());
    }
    public void emitInvokestatic(int meth, Type mtype) {
        int argsize = width(mtype.getParameterTypes());
        emitop(invokestatic);
        if (!alive) return;
        emit2(meth);
        state.pop(argsize);
        state.push(mtype.getReturnType());
    }
    public void emitInvokevirtual(int meth, Type mtype) {
        int argsize = width(mtype.getParameterTypes());
        emitop(invokevirtual);
        if (!alive) return;
        emit2(meth);
        state.pop(argsize + 1);
        state.push(mtype.getReturnType());
    }
    public void emitInvokedynamic(int desc, Type mtype) {
        int argsize = width(mtype.getParameterTypes());
        emitop(invokedynamic);
        if (!alive) return;
        emit2(desc);
        emit2(0);
        state.pop(argsize);
        state.push(mtype.getReturnType());
    }
    public void emitop0(int op) {
        emitop(op);
        if (!alive) return;
        switch (op) {
        case aaload: {
            state.pop(1);
            Type a = state.stack[state.stacksize-1];
            state.pop(1);
            Type stackType = a.tag == BOT ?
                syms.objectType :
                types.erasure(types.elemtype(a));
            state.push(stackType); }
            break;
        case goto_:
            markDead();
            break;
        case nop:
        case ineg:
        case lneg:
        case fneg:
        case dneg:
            break;
        case aconst_null:
            state.push(syms.botType);
            break;
        case iconst_m1:
        case iconst_0:
        case iconst_1:
        case iconst_2:
        case iconst_3:
        case iconst_4:
        case iconst_5:
        case iload_0:
        case iload_1:
        case iload_2:
        case iload_3:
            state.push(syms.intType);
            break;
        case lconst_0:
        case lconst_1:
        case lload_0:
        case lload_1:
        case lload_2:
        case lload_3:
            state.push(syms.longType);
            break;
        case fconst_0:
        case fconst_1:
        case fconst_2:
        case fload_0:
        case fload_1:
        case fload_2:
        case fload_3:
            state.push(syms.floatType);
            break;
        case dconst_0:
        case dconst_1:
        case dload_0:
        case dload_1:
        case dload_2:
        case dload_3:
            state.push(syms.doubleType);
            break;
        case aload_0:
            state.push(lvar[0].sym.type);
            break;
        case aload_1:
            state.push(lvar[1].sym.type);
            break;
        case aload_2:
            state.push(lvar[2].sym.type);
            break;
        case aload_3:
            state.push(lvar[3].sym.type);
            break;
        case iaload:
        case baload:
        case caload:
        case saload:
            state.pop(2);
            state.push(syms.intType);
            break;
        case laload:
            state.pop(2);
            state.push(syms.longType);
            break;
        case faload:
            state.pop(2);
            state.push(syms.floatType);
            break;
        case daload:
            state.pop(2);
            state.push(syms.doubleType);
            break;
        case istore_0:
        case istore_1:
        case istore_2:
        case istore_3:
        case fstore_0:
        case fstore_1:
        case fstore_2:
        case fstore_3:
        case astore_0:
        case astore_1:
        case astore_2:
        case astore_3:
        case pop:
        case lshr:
        case lshl:
        case lushr:
            state.pop(1);
            break;
        case areturn:
        case ireturn:
        case freturn:
            Assert.check(state.nlocks == 0);
            state.pop(1);
            markDead();
            break;
        case athrow:
            state.pop(1);
            markDead();
            break;
        case lstore_0:
        case lstore_1:
        case lstore_2:
        case lstore_3:
        case dstore_0:
        case dstore_1:
        case dstore_2:
        case dstore_3:
        case pop2:
            state.pop(2);
            break;
        case lreturn:
        case dreturn:
            Assert.check(state.nlocks == 0);
            state.pop(2);
            markDead();
            break;
        case dup:
            state.push(state.stack[state.stacksize-1]);
            break;
        case return_:
            Assert.check(state.nlocks == 0);
            markDead();
            break;
        case arraylength:
            state.pop(1);
            state.push(syms.intType);
            break;
        case isub:
        case iadd:
        case imul:
        case idiv:
        case imod:
        case ishl:
        case ishr:
        case iushr:
        case iand:
        case ior:
        case ixor:
            state.pop(1);
            break;
        case aastore:
            state.pop(3);
            break;
        case land:
        case lor:
        case lxor:
        case lmod:
        case ldiv:
        case lmul:
        case lsub:
        case ladd:
            state.pop(2);
            break;
        case lcmp:
            state.pop(4);
            state.push(syms.intType);
            break;
        case l2i:
            state.pop(2);
            state.push(syms.intType);
            break;
        case i2l:
            state.pop(1);
            state.push(syms.longType);
            break;
        case i2f:
            state.pop(1);
            state.push(syms.floatType);
            break;
        case i2d:
            state.pop(1);
            state.push(syms.doubleType);
            break;
        case l2f:
            state.pop(2);
            state.push(syms.floatType);
            break;
        case l2d:
            state.pop(2);
            state.push(syms.doubleType);
            break;
        case f2i:
            state.pop(1);
            state.push(syms.intType);
            break;
        case f2l:
            state.pop(1);
            state.push(syms.longType);
            break;
        case f2d:
            state.pop(1);
            state.push(syms.doubleType);
            break;
        case d2i:
            state.pop(2);
            state.push(syms.intType);
            break;
        case d2l:
            state.pop(2);
            state.push(syms.longType);
            break;
        case d2f:
            state.pop(2);
            state.push(syms.floatType);
            break;
        case tableswitch:
        case lookupswitch:
            state.pop(1);
            break;
        case dup_x1: {
            Type val1 = state.pop1();
            Type val2 = state.pop1();
            state.push(val1);
            state.push(val2);
            state.push(val1);
            break;
        }
        case bastore:
            state.pop(3);
            break;
        case int2byte:
        case int2char:
        case int2short:
            break;
        case fmul:
        case fadd:
        case fsub:
        case fdiv:
        case fmod:
            state.pop(1);
            break;
        case castore:
        case iastore:
        case fastore:
        case sastore:
            state.pop(3);
            break;
        case lastore:
        case dastore:
            state.pop(4);
            break;
        case dup2:
            if (state.stack[state.stacksize-1] != null) {
                Type value1 = state.pop1();
                Type value2 = state.pop1();
                state.push(value2);
                state.push(value1);
                state.push(value2);
                state.push(value1);
            } else {
                Type value = state.pop2();
                state.push(value);
                state.push(value);
            }
            break;
        case dup2_x1:
            if (state.stack[state.stacksize-1] != null) {
                Type value1 = state.pop1();
                Type value2 = state.pop1();
                Type value3 = state.pop1();
                state.push(value2);
                state.push(value1);
                state.push(value3);
                state.push(value2);
                state.push(value1);
            } else {
                Type value1 = state.pop2();
                Type value2 = state.pop1();
                state.push(value1);
                state.push(value2);
                state.push(value1);
            }
            break;
        case dup2_x2:
            if (state.stack[state.stacksize-1] != null) {
                Type value1 = state.pop1();
                Type value2 = state.pop1();
                if (state.stack[state.stacksize-1] != null) {
                    Type value3 = state.pop1();
                    Type value4 = state.pop1();
                    state.push(value2);
                    state.push(value1);
                    state.push(value4);
                    state.push(value3);
                    state.push(value2);
                    state.push(value1);
                } else {
                    Type value3 = state.pop2();
                    state.push(value2);
                    state.push(value1);
                    state.push(value3);
                    state.push(value2);
                    state.push(value1);
                }
            } else {
                Type value1 = state.pop2();
                if (state.stack[state.stacksize-1] != null) {
                    Type value2 = state.pop1();
                    Type value3 = state.pop1();
                    state.push(value1);
                    state.push(value3);
                    state.push(value2);
                    state.push(value1);
                } else {
                    Type value2 = state.pop2();
                    state.push(value1);
                    state.push(value2);
                    state.push(value1);
                }
            }
            break;
        case dup_x2: {
            Type value1 = state.pop1();
            if (state.stack[state.stacksize-1] != null) {
                Type value2 = state.pop1();
                Type value3 = state.pop1();
                state.push(value1);
                state.push(value3);
                state.push(value2);
                state.push(value1);
            } else {
                Type value2 = state.pop2();
                state.push(value1);
                state.push(value2);
                state.push(value1);
            }
        }
            break;
        case fcmpl:
        case fcmpg:
            state.pop(2);
            state.push(syms.intType);
            break;
        case dcmpl:
        case dcmpg:
            state.pop(4);
            state.push(syms.intType);
            break;
        case swap: {
            Type value1 = state.pop1();
            Type value2 = state.pop1();
            state.push(value1);
            state.push(value2);
            break;
        }
        case dadd:
        case dsub:
        case dmul:
        case ddiv:
        case dmod:
            state.pop(2);
            break;
        case ret:
            markDead();
            break;
        case wide:
            return;
        case monitorenter:
        case monitorexit:
            state.pop(1);
            break;
        default:
            throw new AssertionError(mnem(op));
        }
        postop();
    }
    public void emitop1(int op, int od) {
        emitop(op);
        if (!alive) return;
        emit1(od);
        switch (op) {
        case bipush:
            state.push(syms.intType);
            break;
        case ldc1:
            state.push(typeForPool(pool.pool[od]));
            break;
        default:
            throw new AssertionError(mnem(op));
        }
        postop();
    }
    private Type typeForPool(Object o) {
        if (o instanceof Integer) return syms.intType;
        if (o instanceof Float) return syms.floatType;
        if (o instanceof String) return syms.stringType;
        if (o instanceof Long) return syms.longType;
        if (o instanceof Double) return syms.doubleType;
        if (o instanceof ClassSymbol) return syms.classType;
        if (o instanceof Type.ArrayType) return syms.classType;
        throw new AssertionError(o);
    }
    public void emitop1w(int op, int od) {
        if (od > 0xFF) {
            emitop(wide);
            emitop(op);
            emit2(od);
        } else {
            emitop(op);
            emit1(od);
        }
        if (!alive) return;
        switch (op) {
        case iload:
            state.push(syms.intType);
            break;
        case lload:
            state.push(syms.longType);
            break;
        case fload:
            state.push(syms.floatType);
            break;
        case dload:
            state.push(syms.doubleType);
            break;
        case aload:
            state.push(lvar[od].sym.type);
            break;
        case lstore:
        case dstore:
            state.pop(2);
            break;
        case istore:
        case fstore:
        case astore:
            state.pop(1);
            break;
        case ret:
            markDead();
            break;
        default:
            throw new AssertionError(mnem(op));
        }
        postop();
    }
    public void emitop1w(int op, int od1, int od2) {
        if (od1 > 0xFF || od2 < -128 || od2 > 127) {
            emitop(wide);
            emitop(op);
            emit2(od1);
            emit2(od2);
        } else {
            emitop(op);
            emit1(od1);
            emit1(od2);
        }
        if (!alive) return;
        switch (op) {
        case iinc:
            break;
        default:
            throw new AssertionError(mnem(op));
        }
    }
    public void emitop2(int op, int od) {
        emitop(op);
        if (!alive) return;
        emit2(od);
        switch (op) {
        case getstatic:
            state.push(((Symbol)(pool.pool[od])).erasure(types));
            break;
        case putstatic:
            state.pop(((Symbol)(pool.pool[od])).erasure(types));
            break;
        case new_:
            state.push(uninitializedObject(((Symbol)(pool.pool[od])).erasure(types), cp-3));
            break;
        case sipush:
            state.push(syms.intType);
            break;
        case if_acmp_null:
        case if_acmp_nonnull:
        case ifeq:
        case ifne:
        case iflt:
        case ifge:
        case ifgt:
        case ifle:
            state.pop(1);
            break;
        case if_icmpeq:
        case if_icmpne:
        case if_icmplt:
        case if_icmpge:
        case if_icmpgt:
        case if_icmple:
        case if_acmpeq:
        case if_acmpne:
            state.pop(2);
            break;
        case goto_:
            markDead();
            break;
        case putfield:
            state.pop(((Symbol)(pool.pool[od])).erasure(types));
            state.pop(1); 
            break;
        case getfield:
            state.pop(1); 
            state.push(((Symbol)(pool.pool[od])).erasure(types));
            break;
        case checkcast: {
            state.pop(1); 
            Object o = pool.pool[od];
            Type t = (o instanceof Symbol)
                ? ((Symbol)o).erasure(types)
                : types.erasure(((Type)o));
            state.push(t);
            break; }
        case ldc2w:
            state.push(typeForPool(pool.pool[od]));
            break;
        case instanceof_:
            state.pop(1);
            state.push(syms.intType);
            break;
        case ldc2:
            state.push(typeForPool(pool.pool[od]));
            break;
        case jsr:
            break;
        default:
            throw new AssertionError(mnem(op));
        }
    }
    public void emitop4(int op, int od) {
        emitop(op);
        if (!alive) return;
        emit4(od);
        switch (op) {
        case goto_w:
            markDead();
            break;
        case jsr_w:
            break;
        default:
            throw new AssertionError(mnem(op));
        }
    }
    public void align(int incr) {
        if (alive)
            while (cp % incr != 0) emitop0(nop);
    }
    private void put1(int pc, int op) {
        code[pc] = (byte)op;
    }
    private void put2(int pc, int od) {
        put1(pc, od >> 8);
        put1(pc+1, od);
    }
    public void put4(int pc, int od) {
        put1(pc  , od >> 24);
        put1(pc+1, od >> 16);
        put1(pc+2, od >> 8);
        put1(pc+3, od);
    }
    private int get1(int pc) {
        return code[pc] & 0xFF;
    }
    private int get2(int pc) {
        return (get1(pc) << 8) | get1(pc+1);
    }
    public int get4(int pc) {
        return
            (get1(pc) << 24) |
            (get1(pc+1) << 16) |
            (get1(pc+2) << 8) |
            (get1(pc+3));
    }
    public boolean isAlive() {
        return alive || pendingJumps != null;
    }
    public void markDead() {
        alive = false;
    }
    public int entryPoint() {
        int pc = curPc();
        alive = true;
        pendingStackMap = needStackMap;
        return pc;
    }
    public int entryPoint(State state) {
        int pc = curPc();
        alive = true;
        this.state = state.dup();
        Assert.check(state.stacksize <= max_stack);
        if (debugCode) System.err.println("entry point " + state);
        pendingStackMap = needStackMap;
        return pc;
    }
    public int entryPoint(State state, Type pushed) {
        int pc = curPc();
        alive = true;
        this.state = state.dup();
        Assert.check(state.stacksize <= max_stack);
        this.state.push(pushed);
        if (debugCode) System.err.println("entry point " + state);
        pendingStackMap = needStackMap;
        return pc;
    }
    static class StackMapFrame {
        int pc;
        Type[] locals;
        Type[] stack;
    }
    StackMapFrame[] stackMapBuffer = null;
    StackMapTableFrame[] stackMapTableBuffer = null;
    int stackMapBufferSize = 0;
    int lastStackMapPC = -1;
    StackMapFrame lastFrame = null;
    StackMapFrame frameBeforeLast = null;
    public void emitStackMap() {
        int pc = curPc();
        if (!needStackMap) return;
        switch (stackMap) {
            case CLDC:
                emitCLDCStackMap(pc, getLocalsSize());
                break;
            case JSR202:
                emitStackMapFrame(pc, getLocalsSize());
                break;
            default:
                throw new AssertionError("Should have chosen a stackmap format");
        }
        if (debugCode) state.dump(pc);
    }
    private int getLocalsSize() {
        int nextLocal = 0;
        for (int i=max_locals-1; i>=0; i--) {
            if (state.defined.isMember(i) && lvar[i] != null) {
                nextLocal = i + width(lvar[i].sym.erasure(types));
                break;
            }
        }
        return nextLocal;
    }
    void emitCLDCStackMap(int pc, int localsSize) {
        if (lastStackMapPC == pc) {
            stackMapBuffer[--stackMapBufferSize] = null;
        }
        lastStackMapPC = pc;
        if (stackMapBuffer == null) {
            stackMapBuffer = new StackMapFrame[20];
        } else if (stackMapBuffer.length == stackMapBufferSize) {
            StackMapFrame[] newStackMapBuffer =
                new StackMapFrame[stackMapBufferSize << 1];
            System.arraycopy(stackMapBuffer, 0, newStackMapBuffer,
                             0, stackMapBufferSize);
            stackMapBuffer = newStackMapBuffer;
        }
        StackMapFrame frame =
            stackMapBuffer[stackMapBufferSize++] = new StackMapFrame();
        frame.pc = pc;
        frame.locals = new Type[localsSize];
        for (int i=0; i<localsSize; i++) {
            if (state.defined.isMember(i) && lvar[i] != null) {
                Type vtype = lvar[i].sym.type;
                if (!(vtype instanceof UninitializedType))
                    vtype = types.erasure(vtype);
                frame.locals[i] = vtype;
            }
        }
        frame.stack = new Type[state.stacksize];
        for (int i=0; i<state.stacksize; i++)
            frame.stack[i] = state.stack[i];
    }
    void emitStackMapFrame(int pc, int localsSize) {
        if (lastFrame == null) {
            lastFrame = getInitialFrame();
        } else if (lastFrame.pc == pc) {
            stackMapTableBuffer[--stackMapBufferSize] = null;
            lastFrame = frameBeforeLast;
            frameBeforeLast = null;
        }
        StackMapFrame frame = new StackMapFrame();
        frame.pc = pc;
        int localCount = 0;
        Type[] locals = new Type[localsSize];
        for (int i=0; i<localsSize; i++, localCount++) {
            if (state.defined.isMember(i) && lvar[i] != null) {
                Type vtype = lvar[i].sym.type;
                if (!(vtype instanceof UninitializedType))
                    vtype = types.erasure(vtype);
                locals[i] = vtype;
                if (width(vtype) > 1) i++;
            }
        }
        frame.locals = new Type[localCount];
        for (int i=0, j=0; i<localsSize; i++, j++) {
            Assert.check(j < localCount);
            frame.locals[j] = locals[i];
            if (width(locals[i]) > 1) i++;
        }
        int stackCount = 0;
        for (int i=0; i<state.stacksize; i++) {
            if (state.stack[i] != null) {
                stackCount++;
            }
        }
        frame.stack = new Type[stackCount];
        stackCount = 0;
        for (int i=0; i<state.stacksize; i++) {
            if (state.stack[i] != null) {
                frame.stack[stackCount++] = types.erasure(state.stack[i]);
            }
        }
        if (stackMapTableBuffer == null) {
            stackMapTableBuffer = new StackMapTableFrame[20];
        } else if (stackMapTableBuffer.length == stackMapBufferSize) {
            StackMapTableFrame[] newStackMapTableBuffer =
                new StackMapTableFrame[stackMapBufferSize << 1];
            System.arraycopy(stackMapTableBuffer, 0, newStackMapTableBuffer,
                             0, stackMapBufferSize);
            stackMapTableBuffer = newStackMapTableBuffer;
        }
        stackMapTableBuffer[stackMapBufferSize++] =
                StackMapTableFrame.getInstance(frame, lastFrame.pc, lastFrame.locals, types);
        frameBeforeLast = lastFrame;
        lastFrame = frame;
    }
    StackMapFrame getInitialFrame() {
        StackMapFrame frame = new StackMapFrame();
        List<Type> arg_types = ((MethodType)meth.externalType(types)).argtypes;
        int len = arg_types.length();
        int count = 0;
        if (!meth.isStatic()) {
            Type thisType = meth.owner.type;
            frame.locals = new Type[len+1];
            if (meth.isConstructor() && thisType != syms.objectType) {
                frame.locals[count++] = UninitializedType.uninitializedThis(thisType);
            } else {
                frame.locals[count++] = types.erasure(thisType);
            }
        } else {
            frame.locals = new Type[len];
        }
        for (Type arg_type : arg_types) {
            frame.locals[count++] = types.erasure(arg_type);
        }
        frame.pc = -1;
        frame.stack = null;
        return frame;
    }
    public static class Chain {
        public final int pc;
        Code.State state;
        public final Chain next;
        public Chain(int pc, Chain next, Code.State state) {
            this.pc = pc;
            this.next = next;
            this.state = state;
        }
    }
    public static int negate(int opcode) {
        if (opcode == if_acmp_null) return if_acmp_nonnull;
        else if (opcode == if_acmp_nonnull) return if_acmp_null;
        else return ((opcode + 1) ^ 1) - 1;
    }
    public int emitJump(int opcode) {
        if (fatcode) {
            if (opcode == goto_ || opcode == jsr) {
                emitop4(opcode + goto_w - goto_, 0);
            } else {
                emitop2(negate(opcode), 8);
                emitop4(goto_w, 0);
                alive = true;
                pendingStackMap = needStackMap;
            }
            return cp - 5;
        } else {
            emitop2(opcode, 0);
            return cp - 3;
        }
    }
    public Chain branch(int opcode) {
        Chain result = null;
        if (opcode == goto_) {
            result = pendingJumps;
            pendingJumps = null;
        }
        if (opcode != dontgoto && isAlive()) {
            result = new Chain(emitJump(opcode),
                               result,
                               state.dup());
            fixedPc = fatcode;
            if (opcode == goto_) alive = false;
        }
        return result;
    }
    public void resolve(Chain chain, int target) {
        boolean changed = false;
        State newState = state;
        for (; chain != null; chain = chain.next) {
            Assert.check(state != chain.state
                    && (target > chain.pc || state.stacksize == 0));
            if (target >= cp) {
                target = cp;
            } else if (get1(target) == goto_) {
                if (fatcode) target = target + get4(target + 1);
                else target = target + get2(target + 1);
            }
            if (get1(chain.pc) == goto_ &&
                chain.pc + 3 == target && target == cp && !fixedPc) {
                cp = cp - 3;
                target = target - 3;
                if (chain.next == null) {
                    alive = true;
                    break;
                }
            } else {
                if (fatcode)
                    put4(chain.pc + 1, target - chain.pc);
                else if (target - chain.pc < Short.MIN_VALUE ||
                         target - chain.pc > Short.MAX_VALUE)
                    fatcode = true;
                else
                    put2(chain.pc + 1, target - chain.pc);
                Assert.check(!alive ||
                    chain.state.stacksize == newState.stacksize &&
                    chain.state.nlocks == newState.nlocks);
            }
            fixedPc = true;
            if (cp == target) {
                changed = true;
                if (debugCode)
                    System.err.println("resolving chain state=" + chain.state);
                if (alive) {
                    newState = chain.state.join(newState);
                } else {
                    newState = chain.state;
                    alive = true;
                }
            }
        }
        Assert.check(!changed || state != newState);
        if (state != newState) {
            setDefined(newState.defined);
            state = newState;
            pendingStackMap = needStackMap;
        }
    }
    public void resolve(Chain chain) {
        Assert.check(
            !alive ||
            chain==null ||
            state.stacksize == chain.state.stacksize &&
            state.nlocks == chain.state.nlocks);
        pendingJumps = mergeChains(chain, pendingJumps);
    }
    public void resolvePending() {
        Chain x = pendingJumps;
        pendingJumps = null;
        resolve(x, cp);
    }
    public static Chain mergeChains(Chain chain1, Chain chain2) {
        if (chain2 == null) return chain1;
        if (chain1 == null) return chain2;
        Assert.check(
            chain1.state.stacksize == chain2.state.stacksize &&
            chain1.state.nlocks == chain2.state.nlocks);
        if (chain1.pc < chain2.pc)
            return new Chain(
                chain2.pc,
                mergeChains(chain1, chain2.next),
                chain2.state);
        return new Chain(
                chain1.pc,
                mergeChains(chain1.next, chain2),
                chain1.state);
    }
    public void addCatch(
        char startPc, char endPc, char handlerPc, char catchType) {
        catchInfo.append(new char[]{startPc, endPc, handlerPc, catchType});
    }
    public void addLineNumber(char startPc, char lineNumber) {
        if (lineDebugInfo) {
            if (lineInfo.nonEmpty() && lineInfo.head[0] == startPc)
                lineInfo = lineInfo.tail;
            if (lineInfo.isEmpty() || lineInfo.head[1] != lineNumber)
                lineInfo = lineInfo.prepend(new char[]{startPc, lineNumber});
        }
    }
    public void statBegin(int pos) {
        if (pos != Position.NOPOS) {
            pendingStatPos = pos;
        }
    }
    public void markStatBegin() {
        if (alive && lineDebugInfo) {
            int line = lineMap.getLineNumber(pendingStatPos);
            char cp1 = (char)cp;
            char line1 = (char)line;
            if (cp1 == cp && line1 == line)
                addLineNumber(cp1, line1);
        }
        pendingStatPos = Position.NOPOS;
    }
    class State implements Cloneable {
        Bits defined;
        Type[] stack;
        int stacksize;
        int[] locks;
        int nlocks;
        State() {
            defined = new Bits();
            stack = new Type[16];
        }
        State dup() {
            try {
                State state = (State)super.clone();
                state.defined = defined.dup();
                state.stack = stack.clone();
                if (locks != null) state.locks = locks.clone();
                if (debugCode) {
                    System.err.println("duping state " + this);
                    dump();
                }
                return state;
            } catch (CloneNotSupportedException ex) {
                throw new AssertionError(ex);
            }
        }
        void lock(int register) {
            if (locks == null) {
                locks = new int[20];
            } else if (locks.length == nlocks) {
                int[] newLocks = new int[locks.length << 1];
                System.arraycopy(locks, 0, newLocks, 0, locks.length);
                locks = newLocks;
            }
            locks[nlocks] = register;
            nlocks++;
        }
        void unlock(int register) {
            nlocks--;
            Assert.check(locks[nlocks] == register);
            locks[nlocks] = -1;
        }
        void push(Type t) {
            if (debugCode) System.err.println("   pushing " + t);
            switch (t.tag) {
            case TypeTags.VOID:
                return;
            case TypeTags.BYTE:
            case TypeTags.CHAR:
            case TypeTags.SHORT:
            case TypeTags.BOOLEAN:
                t = syms.intType;
                break;
            default:
                break;
            }
            if (stacksize+2 >= stack.length) {
                Type[] newstack = new Type[2*stack.length];
                System.arraycopy(stack, 0, newstack, 0, stack.length);
                stack = newstack;
            }
            stack[stacksize++] = t;
            switch (width(t)) {
            case 1:
                break;
            case 2:
                stack[stacksize++] = null;
                break;
            default:
                throw new AssertionError(t);
            }
            if (stacksize > max_stack)
                max_stack = stacksize;
        }
        Type pop1() {
            if (debugCode) System.err.println("   popping " + 1);
            stacksize--;
            Type result = stack[stacksize];
            stack[stacksize] = null;
            Assert.check(result != null && width(result) == 1);
            return result;
        }
        Type peek() {
            return stack[stacksize-1];
        }
        Type pop2() {
            if (debugCode) System.err.println("   popping " + 2);
            stacksize -= 2;
            Type result = stack[stacksize];
            stack[stacksize] = null;
            Assert.check(stack[stacksize+1] == null
                    && result != null && width(result) == 2);
            return result;
        }
        void pop(int n) {
            if (debugCode) System.err.println("   popping " + n);
            while (n > 0) {
                stack[--stacksize] = null;
                n--;
            }
        }
        void pop(Type t) {
            pop(width(t));
        }
        void forceStackTop(Type t) {
            if (!alive) return;
            switch (t.tag) {
            case CLASS:
            case ARRAY:
                int width = width(t);
                Type old = stack[stacksize-width];
                Assert.check(types.isSubtype(types.erasure(old),
                                       types.erasure(t)));
                stack[stacksize-width] = t;
                break;
            default:
            }
        }
        void markInitialized(UninitializedType old) {
            Type newtype = old.initializedType();
            for (int i=0; i<stacksize; i++)
                if (stack[i] == old) stack[i] = newtype;
            for (int i=0; i<lvar.length; i++) {
                LocalVar lv = lvar[i];
                if (lv != null && lv.sym.type == old) {
                    VarSymbol sym = lv.sym;
                    sym = sym.clone(sym.owner);
                    sym.type = newtype;
                    LocalVar newlv = lvar[i] = new LocalVar(sym);
                    newlv.start_pc = lv.start_pc;
                }
            }
        }
        State join(State other) {
            defined = defined.andSet(other.defined);
            Assert.check(stacksize == other.stacksize
                    && nlocks == other.nlocks);
            for (int i=0; i<stacksize; ) {
                Type t = stack[i];
                Type tother = other.stack[i];
                Type result =
                    t==tother ? t :
                    types.isSubtype(t, tother) ? tother :
                    types.isSubtype(tother, t) ? t :
                    error();
                int w = width(result);
                stack[i] = result;
                if (w == 2) Assert.checkNull(stack[i+1]);
                i += w;
            }
            return this;
        }
        Type error() {
            throw new AssertionError("inconsistent stack types at join point");
        }
        void dump() {
            dump(-1);
        }
        void dump(int pc) {
            System.err.print("stackMap for " + meth.owner + "." + meth);
            if (pc == -1)
                System.out.println();
            else
                System.out.println(" at " + pc);
            System.err.println(" stack (from bottom):");
            for (int i=0; i<stacksize; i++)
                System.err.println("  " + i + ": " + stack[i]);
            int lastLocal = 0;
            for (int i=max_locals-1; i>=0; i--) {
                if (defined.isMember(i)) {
                    lastLocal = i;
                    break;
                }
            }
            if (lastLocal >= 0)
                System.err.println(" locals:");
            for (int i=0; i<=lastLocal; i++) {
                System.err.print("  " + i + ": ");
                if (defined.isMember(i)) {
                    LocalVar var = lvar[i];
                    if (var == null) {
                        System.err.println("(none)");
                    } else if (var.sym == null)
                        System.err.println("UNKNOWN!");
                    else
                        System.err.println("" + var.sym + " of type " +
                                           var.sym.erasure(types));
                } else {
                    System.err.println("undefined");
                }
            }
            if (nlocks != 0) {
                System.err.print(" locks:");
                for (int i=0; i<nlocks; i++) {
                    System.err.print(" " + locks[i]);
                }
                System.err.println();
            }
        }
    }
    static Type jsrReturnValue = new Type(TypeTags.INT, null);
    static class LocalVar {
        final VarSymbol sym;
        final char reg;
        char start_pc = Character.MAX_VALUE;
        char length = Character.MAX_VALUE;
        LocalVar(VarSymbol v) {
            this.sym = v;
            this.reg = (char)v.adr;
        }
        public LocalVar dup() {
            return new LocalVar(sym);
        }
        public String toString() {
            return "" + sym + " in register " + ((int)reg) + " starts at pc=" + ((int)start_pc) + " length=" + ((int)length);
        }
    };
    LocalVar[] lvar;
    private void addLocalVar(VarSymbol v) {
        int adr = v.adr;
        if (adr+1 >= lvar.length) {
            int newlength = lvar.length << 1;
            if (newlength <= adr) newlength = adr + 10;
            LocalVar[] new_lvar = new LocalVar[newlength];
            System.arraycopy(lvar, 0, new_lvar, 0, lvar.length);
            lvar = new_lvar;
        }
        Assert.checkNull(lvar[adr]);
        if (pendingJumps != null) resolvePending();
        lvar[adr] = new LocalVar(v);
        state.defined.excl(adr);
    }
    public void setDefined(Bits newDefined) {
        if (alive && newDefined != state.defined) {
            Bits diff = state.defined.dup().xorSet(newDefined);
            for (int adr = diff.nextBit(0);
                 adr >= 0;
                 adr = diff.nextBit(adr+1)) {
                if (adr >= nextreg)
                    state.defined.excl(adr);
                else if (state.defined.isMember(adr))
                    setUndefined(adr);
                else
                    setDefined(adr);
            }
        }
    }
    public void setDefined(int adr) {
        LocalVar v = lvar[adr];
        if (v == null) {
            state.defined.excl(adr);
        } else {
            state.defined.incl(adr);
            if (cp < Character.MAX_VALUE) {
                if (v.start_pc == Character.MAX_VALUE)
                    v.start_pc = (char)cp;
            }
        }
    }
    public void setUndefined(int adr) {
        state.defined.excl(adr);
        if (adr < lvar.length &&
            lvar[adr] != null &&
            lvar[adr].start_pc != Character.MAX_VALUE) {
            LocalVar v = lvar[adr];
            char length = (char)(curPc() - v.start_pc);
            if (length > 0 && length < Character.MAX_VALUE) {
                lvar[adr] = v.dup();
                v.length = length;
                putVar(v);
            } else {
                v.start_pc = Character.MAX_VALUE;
            }
        }
    }
    private void endScope(int adr) {
        LocalVar v = lvar[adr];
        if (v != null) {
            lvar[adr] = null;
            if (v.start_pc != Character.MAX_VALUE) {
                char length = (char)(curPc() - v.start_pc);
                if (length < Character.MAX_VALUE) {
                    v.length = length;
                    putVar(v);
                }
            }
        }
        state.defined.excl(adr);
    }
    void putVar(LocalVar var) {
        if (!varDebugInfo) return;
        if ((var.sym.flags() & Flags.SYNTHETIC) != 0) return;
        if (varBuffer == null)
            varBuffer = new LocalVar[20];
        else if (varBufferSize >= varBuffer.length) {
            LocalVar[] newVarBuffer = new LocalVar[varBufferSize*2];
            System.arraycopy(varBuffer, 0, newVarBuffer, 0, varBuffer.length);
            varBuffer = newVarBuffer;
        }
        varBuffer[varBufferSize++] = var;
    }
    LocalVar[] varBuffer;
    int varBufferSize;
    private int newLocal(int typecode) {
        int reg = nextreg;
        int w = width(typecode);
        nextreg = reg + w;
        if (nextreg > max_locals) max_locals = nextreg;
        return reg;
    }
    private int newLocal(Type type) {
        return newLocal(typecode(type));
    }
    public int newLocal(VarSymbol v) {
        int reg = v.adr = newLocal(v.erasure(types));
        addLocalVar(v);
        return reg;
    }
    public void newRegSegment() {
        nextreg = max_locals;
    }
    public void endScopes(int first) {
        int prevNextReg = nextreg;
        nextreg = first;
        for (int i = nextreg; i < prevNextReg; i++) endScope(i);
    }
    public static String mnem(int opcode) {
        return Mneumonics.mnem[opcode];
    }
    private static class Mneumonics {
        private final static String[] mnem = new String[ByteCodeCount];
        static {
            mnem[nop] = "nop";
            mnem[aconst_null] = "aconst_null";
            mnem[iconst_m1] = "iconst_m1";
            mnem[iconst_0] = "iconst_0";
            mnem[iconst_1] = "iconst_1";
            mnem[iconst_2] = "iconst_2";
            mnem[iconst_3] = "iconst_3";
            mnem[iconst_4] = "iconst_4";
            mnem[iconst_5] = "iconst_5";
            mnem[lconst_0] = "lconst_0";
            mnem[lconst_1] = "lconst_1";
            mnem[fconst_0] = "fconst_0";
            mnem[fconst_1] = "fconst_1";
            mnem[fconst_2] = "fconst_2";
            mnem[dconst_0] = "dconst_0";
            mnem[dconst_1] = "dconst_1";
            mnem[bipush] = "bipush";
            mnem[sipush] = "sipush";
            mnem[ldc1] = "ldc1";
            mnem[ldc2] = "ldc2";
            mnem[ldc2w] = "ldc2w";
            mnem[iload] = "iload";
            mnem[lload] = "lload";
            mnem[fload] = "fload";
            mnem[dload] = "dload";
            mnem[aload] = "aload";
            mnem[iload_0] = "iload_0";
            mnem[lload_0] = "lload_0";
            mnem[fload_0] = "fload_0";
            mnem[dload_0] = "dload_0";
            mnem[aload_0] = "aload_0";
            mnem[iload_1] = "iload_1";
            mnem[lload_1] = "lload_1";
            mnem[fload_1] = "fload_1";
            mnem[dload_1] = "dload_1";
            mnem[aload_1] = "aload_1";
            mnem[iload_2] = "iload_2";
            mnem[lload_2] = "lload_2";
            mnem[fload_2] = "fload_2";
            mnem[dload_2] = "dload_2";
            mnem[aload_2] = "aload_2";
            mnem[iload_3] = "iload_3";
            mnem[lload_3] = "lload_3";
            mnem[fload_3] = "fload_3";
            mnem[dload_3] = "dload_3";
            mnem[aload_3] = "aload_3";
            mnem[iaload] = "iaload";
            mnem[laload] = "laload";
            mnem[faload] = "faload";
            mnem[daload] = "daload";
            mnem[aaload] = "aaload";
            mnem[baload] = "baload";
            mnem[caload] = "caload";
            mnem[saload] = "saload";
            mnem[istore] = "istore";
            mnem[lstore] = "lstore";
            mnem[fstore] = "fstore";
            mnem[dstore] = "dstore";
            mnem[astore] = "astore";
            mnem[istore_0] = "istore_0";
            mnem[lstore_0] = "lstore_0";
            mnem[fstore_0] = "fstore_0";
            mnem[dstore_0] = "dstore_0";
            mnem[astore_0] = "astore_0";
            mnem[istore_1] = "istore_1";
            mnem[lstore_1] = "lstore_1";
            mnem[fstore_1] = "fstore_1";
            mnem[dstore_1] = "dstore_1";
            mnem[astore_1] = "astore_1";
            mnem[istore_2] = "istore_2";
            mnem[lstore_2] = "lstore_2";
            mnem[fstore_2] = "fstore_2";
            mnem[dstore_2] = "dstore_2";
            mnem[astore_2] = "astore_2";
            mnem[istore_3] = "istore_3";
            mnem[lstore_3] = "lstore_3";
            mnem[fstore_3] = "fstore_3";
            mnem[dstore_3] = "dstore_3";
            mnem[astore_3] = "astore_3";
            mnem[iastore] = "iastore";
            mnem[lastore] = "lastore";
            mnem[fastore] = "fastore";
            mnem[dastore] = "dastore";
            mnem[aastore] = "aastore";
            mnem[bastore] = "bastore";
            mnem[castore] = "castore";
            mnem[sastore] = "sastore";
            mnem[pop] = "pop";
            mnem[pop2] = "pop2";
            mnem[dup] = "dup";
            mnem[dup_x1] = "dup_x1";
            mnem[dup_x2] = "dup_x2";
            mnem[dup2] = "dup2";
            mnem[dup2_x1] = "dup2_x1";
            mnem[dup2_x2] = "dup2_x2";
            mnem[swap] = "swap";
            mnem[iadd] = "iadd";
            mnem[ladd] = "ladd";
            mnem[fadd] = "fadd";
            mnem[dadd] = "dadd";
            mnem[isub] = "isub";
            mnem[lsub] = "lsub";
            mnem[fsub] = "fsub";
            mnem[dsub] = "dsub";
            mnem[imul] = "imul";
            mnem[lmul] = "lmul";
            mnem[fmul] = "fmul";
            mnem[dmul] = "dmul";
            mnem[idiv] = "idiv";
            mnem[ldiv] = "ldiv";
            mnem[fdiv] = "fdiv";
            mnem[ddiv] = "ddiv";
            mnem[imod] = "imod";
            mnem[lmod] = "lmod";
            mnem[fmod] = "fmod";
            mnem[dmod] = "dmod";
            mnem[ineg] = "ineg";
            mnem[lneg] = "lneg";
            mnem[fneg] = "fneg";
            mnem[dneg] = "dneg";
            mnem[ishl] = "ishl";
            mnem[lshl] = "lshl";
            mnem[ishr] = "ishr";
            mnem[lshr] = "lshr";
            mnem[iushr] = "iushr";
            mnem[lushr] = "lushr";
            mnem[iand] = "iand";
            mnem[land] = "land";
            mnem[ior] = "ior";
            mnem[lor] = "lor";
            mnem[ixor] = "ixor";
            mnem[lxor] = "lxor";
            mnem[iinc] = "iinc";
            mnem[i2l] = "i2l";
            mnem[i2f] = "i2f";
            mnem[i2d] = "i2d";
            mnem[l2i] = "l2i";
            mnem[l2f] = "l2f";
            mnem[l2d] = "l2d";
            mnem[f2i] = "f2i";
            mnem[f2l] = "f2l";
            mnem[f2d] = "f2d";
            mnem[d2i] = "d2i";
            mnem[d2l] = "d2l";
            mnem[d2f] = "d2f";
            mnem[int2byte] = "int2byte";
            mnem[int2char] = "int2char";
            mnem[int2short] = "int2short";
            mnem[lcmp] = "lcmp";
            mnem[fcmpl] = "fcmpl";
            mnem[fcmpg] = "fcmpg";
            mnem[dcmpl] = "dcmpl";
            mnem[dcmpg] = "dcmpg";
            mnem[ifeq] = "ifeq";
            mnem[ifne] = "ifne";
            mnem[iflt] = "iflt";
            mnem[ifge] = "ifge";
            mnem[ifgt] = "ifgt";
            mnem[ifle] = "ifle";
            mnem[if_icmpeq] = "if_icmpeq";
            mnem[if_icmpne] = "if_icmpne";
            mnem[if_icmplt] = "if_icmplt";
            mnem[if_icmpge] = "if_icmpge";
            mnem[if_icmpgt] = "if_icmpgt";
            mnem[if_icmple] = "if_icmple";
            mnem[if_acmpeq] = "if_acmpeq";
            mnem[if_acmpne] = "if_acmpne";
            mnem[goto_] = "goto_";
            mnem[jsr] = "jsr";
            mnem[ret] = "ret";
            mnem[tableswitch] = "tableswitch";
            mnem[lookupswitch] = "lookupswitch";
            mnem[ireturn] = "ireturn";
            mnem[lreturn] = "lreturn";
            mnem[freturn] = "freturn";
            mnem[dreturn] = "dreturn";
            mnem[areturn] = "areturn";
            mnem[return_] = "return_";
            mnem[getstatic] = "getstatic";
            mnem[putstatic] = "putstatic";
            mnem[getfield] = "getfield";
            mnem[putfield] = "putfield";
            mnem[invokevirtual] = "invokevirtual";
            mnem[invokespecial] = "invokespecial";
            mnem[invokestatic] = "invokestatic";
            mnem[invokeinterface] = "invokeinterface";
            mnem[invokedynamic] = "invokedynamic";
            mnem[new_] = "new_";
            mnem[newarray] = "newarray";
            mnem[anewarray] = "anewarray";
            mnem[arraylength] = "arraylength";
            mnem[athrow] = "athrow";
            mnem[checkcast] = "checkcast";
            mnem[instanceof_] = "instanceof_";
            mnem[monitorenter] = "monitorenter";
            mnem[monitorexit] = "monitorexit";
            mnem[wide] = "wide";
            mnem[multianewarray] = "multianewarray";
            mnem[if_acmp_null] = "if_acmp_null";
            mnem[if_acmp_nonnull] = "if_acmp_nonnull";
            mnem[goto_w] = "goto_w";
            mnem[jsr_w] = "jsr_w";
            mnem[breakpoint] = "breakpoint";
        }
    }
}
