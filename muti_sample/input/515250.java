import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.evaluation.value.*;
public class Processor
extends      SimplifiedVisitor
implements   InstructionVisitor,
             ConstantVisitor
{
    private final Variables      variables;
    private final Stack          stack;
    private final ValueFactory   valueFactory;
    private final BranchUnit     branchUnit;
    private final InvocationUnit invocationUnit;
    private boolean handleClassConstantAsClassValue;
    private Value   cpValue;
    public Processor(Variables      variables,
                     Stack          stack,
                     ValueFactory   valueFactory,
                     BranchUnit     branchUnit,
                     InvocationUnit invocationUnit)
    {
        this.variables      = variables;
        this.stack          = stack;
        this.valueFactory   = valueFactory;
        this.branchUnit     = branchUnit;
        this.invocationUnit = invocationUnit;
    }
    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        switch (simpleInstruction.opcode)
        {
            case InstructionConstants.OP_NOP:
                break;
            case InstructionConstants.OP_ACONST_NULL:
                stack.push(valueFactory.createReferenceValueNull());
                break;
            case InstructionConstants.OP_ICONST_M1:
            case InstructionConstants.OP_ICONST_0:
            case InstructionConstants.OP_ICONST_1:
            case InstructionConstants.OP_ICONST_2:
            case InstructionConstants.OP_ICONST_3:
            case InstructionConstants.OP_ICONST_4:
            case InstructionConstants.OP_ICONST_5:
            case InstructionConstants.OP_BIPUSH:
            case InstructionConstants.OP_SIPUSH:
                stack.push(valueFactory.createIntegerValue(simpleInstruction.constant));
                break;
            case InstructionConstants.OP_LCONST_0:
            case InstructionConstants.OP_LCONST_1:
                stack.push(valueFactory.createLongValue(simpleInstruction.constant));
                break;
            case InstructionConstants.OP_FCONST_0:
            case InstructionConstants.OP_FCONST_1:
            case InstructionConstants.OP_FCONST_2:
                stack.push(valueFactory.createFloatValue((float)simpleInstruction.constant));
                break;
            case InstructionConstants.OP_DCONST_0:
            case InstructionConstants.OP_DCONST_1:
                stack.push(valueFactory.createDoubleValue((double)simpleInstruction.constant));
                break;
            case InstructionConstants.OP_IALOAD:
            case InstructionConstants.OP_BALOAD:
            case InstructionConstants.OP_CALOAD:
            case InstructionConstants.OP_SALOAD:
                stack.ipop();
                stack.apop();
                stack.push(valueFactory.createIntegerValue());
                break;
            case InstructionConstants.OP_LALOAD:
                stack.ipop();
                stack.apop();
                stack.push(valueFactory.createLongValue());
                break;
            case InstructionConstants.OP_FALOAD:
                stack.ipop();
                stack.apop();
                stack.push(valueFactory.createFloatValue());
                break;
            case InstructionConstants.OP_DALOAD:
                stack.ipop();
                stack.apop();
                stack.push(valueFactory.createDoubleValue());
                break;
            case InstructionConstants.OP_AALOAD:
            {
                IntegerValue   arrayIndex     = stack.ipop();
                ReferenceValue arrayReference = stack.apop();
                stack.push(arrayReference.arrayLoad(arrayIndex, valueFactory));
                break;
            }
            case InstructionConstants.OP_IASTORE:
            case InstructionConstants.OP_BASTORE:
            case InstructionConstants.OP_CASTORE:
            case InstructionConstants.OP_SASTORE:
                stack.ipop();
                stack.ipop();
                stack.apop();
                break;
            case InstructionConstants.OP_LASTORE:
                stack.lpop();
                stack.ipop();
                stack.apop();
                break;
            case InstructionConstants.OP_FASTORE:
                stack.fpop();
                stack.ipop();
                stack.apop();
                break;
            case InstructionConstants.OP_DASTORE:
                stack.dpop();
                stack.ipop();
                stack.apop();
                break;
            case InstructionConstants.OP_AASTORE:
                stack.apop();
                stack.ipop();
                stack.apop();
                break;
            case InstructionConstants.OP_POP:
                stack.pop1();
                break;
            case InstructionConstants.OP_POP2:
                stack.pop2();
                break;
            case InstructionConstants.OP_DUP:
                stack.dup();
                break;
            case InstructionConstants.OP_DUP_X1:
                stack.dup_x1();
                break;
            case InstructionConstants.OP_DUP_X2:
                stack.dup_x2();
                break;
            case InstructionConstants.OP_DUP2:
                stack.dup2();
                break;
            case InstructionConstants.OP_DUP2_X1:
                stack.dup2_x1();
                break;
            case InstructionConstants.OP_DUP2_X2:
                stack.dup2_x2();
                break;
            case InstructionConstants.OP_SWAP:
                stack.swap();
                break;
            case InstructionConstants.OP_IADD:
                stack.push(stack.ipop().add(stack.ipop()));
                break;
            case InstructionConstants.OP_LADD:
                stack.push(stack.lpop().add(stack.lpop()));
                break;
            case InstructionConstants.OP_FADD:
                stack.push(stack.fpop().add(stack.fpop()));
                break;
            case InstructionConstants.OP_DADD:
                stack.push(stack.dpop().add(stack.dpop()));
                break;
            case InstructionConstants.OP_ISUB:
                stack.push(stack.ipop().subtractFrom(stack.ipop()));
                break;
            case InstructionConstants.OP_LSUB:
                stack.push(stack.lpop().subtractFrom(stack.lpop()));
                break;
            case InstructionConstants.OP_FSUB:
                stack.push(stack.fpop().subtractFrom(stack.fpop()));
                break;
            case InstructionConstants.OP_DSUB:
                stack.push(stack.dpop().subtractFrom(stack.dpop()));
                break;
            case InstructionConstants.OP_IMUL:
                stack.push(stack.ipop().multiply(stack.ipop()));
                break;
            case InstructionConstants.OP_LMUL:
                stack.push(stack.lpop().multiply(stack.lpop()));
                break;
            case InstructionConstants.OP_FMUL:
                stack.push(stack.fpop().multiply(stack.fpop()));
                break;
            case InstructionConstants.OP_DMUL:
                stack.push(stack.dpop().multiply(stack.dpop()));
                break;
            case InstructionConstants.OP_IDIV:
                try
                {
                    stack.push(stack.ipop().divideOf(stack.ipop()));
                }
                catch (ArithmeticException ex)
                {
                    stack.push(valueFactory.createIntegerValue());
                }
                break;
            case InstructionConstants.OP_LDIV:
                try
                {
                    stack.push(stack.lpop().divideOf(stack.lpop()));
                }
                catch (ArithmeticException ex)
                {
                    stack.push(valueFactory.createLongValue());
                }
                break;
            case InstructionConstants.OP_FDIV:
                stack.push(stack.fpop().divideOf(stack.fpop()));
                break;
            case InstructionConstants.OP_DDIV:
                stack.push(stack.dpop().divideOf(stack.dpop()));
                break;
            case InstructionConstants.OP_IREM:
                try
                {
                    stack.push(stack.ipop().remainderOf(stack.ipop()));
                }
                catch (ArithmeticException ex)
                {
                    stack.push(valueFactory.createIntegerValue());
                }
                break;
            case InstructionConstants.OP_LREM:
                try
                {
                    stack.push(stack.lpop().remainderOf(stack.lpop()));
                }
                catch (ArithmeticException ex)
                {
                    stack.push(valueFactory.createLongValue());
                }
                break;
            case InstructionConstants.OP_FREM:
                stack.push(stack.fpop().remainderOf(stack.fpop()));
                break;
            case InstructionConstants.OP_DREM:
                stack.push(stack.dpop().remainderOf(stack.dpop()));
                break;
            case InstructionConstants.OP_INEG:
                stack.push(stack.ipop().negate());
                break;
            case InstructionConstants.OP_LNEG:
                stack.push(stack.lpop().negate());
                break;
            case InstructionConstants.OP_FNEG:
                stack.push(stack.fpop().negate());
                break;
            case InstructionConstants.OP_DNEG:
                stack.push(stack.dpop().negate());
                break;
            case InstructionConstants.OP_ISHL:
                stack.push(stack.ipop().shiftLeftOf(stack.ipop()));
                break;
            case InstructionConstants.OP_LSHL:
                stack.push(stack.ipop().shiftLeftOf(stack.lpop()));
                break;
            case InstructionConstants.OP_ISHR:
                stack.push(stack.ipop().shiftRightOf(stack.ipop()));
                break;
            case InstructionConstants.OP_LSHR:
                stack.push(stack.ipop().shiftRightOf(stack.lpop()));
                break;
            case InstructionConstants.OP_IUSHR:
                stack.push(stack.ipop().unsignedShiftRightOf(stack.ipop()));
                break;
            case InstructionConstants.OP_LUSHR:
                stack.push(stack.ipop().unsignedShiftRightOf(stack.lpop()));
                break;
            case InstructionConstants.OP_IAND:
                stack.push(stack.ipop().and(stack.ipop()));
                break;
            case InstructionConstants.OP_LAND:
                stack.push(stack.lpop().and(stack.lpop()));
                break;
            case InstructionConstants.OP_IOR:
                stack.push(stack.ipop().or(stack.ipop()));
                break;
            case InstructionConstants.OP_LOR:
                stack.push(stack.lpop().or(stack.lpop()));
                break;
            case InstructionConstants.OP_IXOR:
                stack.push(stack.ipop().xor(stack.ipop()));
                break;
            case InstructionConstants.OP_LXOR:
                stack.push(stack.lpop().xor(stack.lpop()));
                break;
            case InstructionConstants.OP_I2L:
                stack.push(stack.ipop().convertToLong());
                break;
            case InstructionConstants.OP_I2F:
                stack.push(stack.ipop().convertToFloat());
                break;
            case InstructionConstants.OP_I2D:
                stack.push(stack.ipop().convertToDouble());
                break;
            case InstructionConstants.OP_L2I:
                stack.push(stack.lpop().convertToInteger());
                break;
            case InstructionConstants.OP_L2F:
                stack.push(stack.lpop().convertToFloat());
                break;
            case InstructionConstants.OP_L2D:
                stack.push(stack.lpop().convertToDouble());
                break;
            case InstructionConstants.OP_F2I:
                stack.push(stack.fpop().convertToInteger());
                break;
            case InstructionConstants.OP_F2L:
                stack.push(stack.fpop().convertToLong());
                break;
            case InstructionConstants.OP_F2D:
                stack.push(stack.fpop().convertToDouble());
                break;
            case InstructionConstants.OP_D2I:
                stack.push(stack.dpop().convertToInteger());
                break;
            case InstructionConstants.OP_D2L:
                stack.push(stack.dpop().convertToLong());
                break;
            case InstructionConstants.OP_D2F:
                stack.push(stack.dpop().convertToFloat());
                break;
            case InstructionConstants.OP_I2B:
                stack.push(stack.ipop().convertToByte());
                break;
            case InstructionConstants.OP_I2C:
                stack.push(stack.ipop().convertToCharacter());
                break;
            case InstructionConstants.OP_I2S:
                stack.push(stack.ipop().convertToShort());
                break;
            case InstructionConstants.OP_LCMP:
                LongValue longValue1 = stack.lpop();
                LongValue longValue2 = stack.lpop();
                stack.push(longValue2.compare(longValue1));
                break;
            case InstructionConstants.OP_FCMPL:
                FloatValue floatValue1 = stack.fpop();
                FloatValue floatValue2 = stack.fpop();
                stack.push(floatValue2.compare(floatValue1));
                break;
            case InstructionConstants.OP_FCMPG:
                stack.push(stack.fpop().compareReverse(stack.fpop()));
                break;
            case InstructionConstants.OP_DCMPL:
                DoubleValue doubleValue1 = stack.dpop();
                DoubleValue doubleValue2 = stack.dpop();
                stack.push(doubleValue2.compare(doubleValue1));
                break;
            case InstructionConstants.OP_DCMPG:
                stack.push(stack.dpop().compareReverse(stack.dpop()));
                break;
            case InstructionConstants.OP_IRETURN:
                invocationUnit.exitMethod(clazz, method, stack.ipop());
                branchUnit.returnFromMethod();
                break;
            case InstructionConstants.OP_LRETURN:
                invocationUnit.exitMethod(clazz, method, stack.lpop());
                branchUnit.returnFromMethod();
                break;
            case InstructionConstants.OP_FRETURN:
                invocationUnit.exitMethod(clazz, method, stack.fpop());
                branchUnit.returnFromMethod();
                break;
            case InstructionConstants.OP_DRETURN:
                invocationUnit.exitMethod(clazz, method, stack.dpop());
                branchUnit.returnFromMethod();
                break;
            case InstructionConstants.OP_ARETURN:
                invocationUnit.exitMethod(clazz, method, stack.apop());
                branchUnit.returnFromMethod();
                break;
            case InstructionConstants.OP_RETURN:
                branchUnit.returnFromMethod();
                break;
            case InstructionConstants.OP_NEWARRAY:
                IntegerValue arrayLength = stack.ipop();
                stack.push(valueFactory.createArrayReferenceValue(String.valueOf(InstructionUtil.internalTypeFromArrayType((byte)simpleInstruction.constant)),
                                                                  null,
                                                                  arrayLength));
                break;
            case InstructionConstants.OP_ARRAYLENGTH:
                stack.apop();
                stack.push(valueFactory.createIntegerValue());
                break;
            case InstructionConstants.OP_ATHROW:
                ReferenceValue exceptionReferenceValue = stack.apop();
                stack.clear();
                stack.push(exceptionReferenceValue);
                branchUnit.throwException();
                break;
            case InstructionConstants.OP_MONITORENTER:
            case InstructionConstants.OP_MONITOREXIT:
                stack.apop();
                break;
            default:
                throw new IllegalArgumentException("Unknown simple instruction ["+simpleInstruction.opcode+"]");
        }
    }
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        int constantIndex = constantInstruction.constantIndex;
        switch (constantInstruction.opcode)
        {
            case InstructionConstants.OP_LDC:
            case InstructionConstants.OP_LDC_W:
            case InstructionConstants.OP_LDC2_W:
                stack.push(cpValue(clazz, constantIndex, true));
                break;
            case InstructionConstants.OP_GETSTATIC:
            case InstructionConstants.OP_PUTSTATIC:
            case InstructionConstants.OP_GETFIELD:
            case InstructionConstants.OP_PUTFIELD:
            case InstructionConstants.OP_INVOKEVIRTUAL:
            case InstructionConstants.OP_INVOKESPECIAL:
            case InstructionConstants.OP_INVOKESTATIC:
            case InstructionConstants.OP_INVOKEINTERFACE:
                invocationUnit.invokeMember(clazz, method, codeAttribute, offset, constantInstruction, stack);
                break;
            case InstructionConstants.OP_NEW:
                stack.push(cpValue(clazz, constantIndex).referenceValue());
                break;
            case InstructionConstants.OP_ANEWARRAY:
            {
                ReferenceValue referenceValue = cpValue(clazz, constantIndex).referenceValue();
                stack.push(valueFactory.createArrayReferenceValue(referenceValue.internalType(),
                                                                  referenceValue.getReferencedClass(),
                                                                  stack.ipop()));
                break;
            }
            case InstructionConstants.OP_CHECKCAST:
                ReferenceValue castValue = stack.apop();
                ReferenceValue castResultValue =
                    castValue.isNull() == Value.ALWAYS ? castValue :
                    castValue.isNull() == Value.NEVER  ? cpValue(clazz, constantIndex).referenceValue() :
                                                         cpValue(clazz, constantIndex).referenceValue().generalize(valueFactory.createReferenceValueNull());
                stack.push(castResultValue);
                break;
            case InstructionConstants.OP_INSTANCEOF:
            {
                ReferenceValue referenceValue = cpValue(clazz, constantIndex).referenceValue();
                int instanceOf = stack.apop().instanceOf(referenceValue.getType(),
                                                         referenceValue.getReferencedClass());
                stack.push(instanceOf == Value.NEVER  ? valueFactory.createIntegerValue(0) :
                           instanceOf == Value.ALWAYS ? valueFactory.createIntegerValue(1) :
                                                        valueFactory.createIntegerValue());
                break;
            }
            case InstructionConstants.OP_MULTIANEWARRAY:
            {
                int dimensionCount = constantInstruction.constant;
                for (int dimension = 0; dimension < dimensionCount; dimension++)
                {
                    IntegerValue arrayLength = stack.ipop();
                }
                stack.push(cpValue(clazz, constantIndex).referenceValue());
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown constant pool instruction ["+constantInstruction.opcode+"]");
        }
    }
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        int variableIndex = variableInstruction.variableIndex;
        switch (variableInstruction.opcode)
        {
            case InstructionConstants.OP_ILOAD:
            case InstructionConstants.OP_ILOAD_0:
            case InstructionConstants.OP_ILOAD_1:
            case InstructionConstants.OP_ILOAD_2:
            case InstructionConstants.OP_ILOAD_3:
                stack.push(variables.iload(variableIndex));
                break;
            case InstructionConstants.OP_LLOAD:
            case InstructionConstants.OP_LLOAD_0:
            case InstructionConstants.OP_LLOAD_1:
            case InstructionConstants.OP_LLOAD_2:
            case InstructionConstants.OP_LLOAD_3:
                stack.push(variables.lload(variableIndex));
                break;
            case InstructionConstants.OP_FLOAD:
            case InstructionConstants.OP_FLOAD_0:
            case InstructionConstants.OP_FLOAD_1:
            case InstructionConstants.OP_FLOAD_2:
            case InstructionConstants.OP_FLOAD_3:
                stack.push(variables.fload(variableIndex));
                break;
            case InstructionConstants.OP_DLOAD:
            case InstructionConstants.OP_DLOAD_0:
            case InstructionConstants.OP_DLOAD_1:
            case InstructionConstants.OP_DLOAD_2:
            case InstructionConstants.OP_DLOAD_3:
                stack.push(variables.dload(variableIndex));
                break;
            case InstructionConstants.OP_ALOAD:
            case InstructionConstants.OP_ALOAD_0:
            case InstructionConstants.OP_ALOAD_1:
            case InstructionConstants.OP_ALOAD_2:
            case InstructionConstants.OP_ALOAD_3:
                stack.push(variables.aload(variableIndex));
                break;
            case InstructionConstants.OP_ISTORE:
            case InstructionConstants.OP_ISTORE_0:
            case InstructionConstants.OP_ISTORE_1:
            case InstructionConstants.OP_ISTORE_2:
            case InstructionConstants.OP_ISTORE_3:
                variables.store(variableIndex, stack.ipop());
                break;
            case InstructionConstants.OP_LSTORE:
            case InstructionConstants.OP_LSTORE_0:
            case InstructionConstants.OP_LSTORE_1:
            case InstructionConstants.OP_LSTORE_2:
            case InstructionConstants.OP_LSTORE_3:
                variables.store(variableIndex, stack.lpop());
                break;
            case InstructionConstants.OP_FSTORE:
            case InstructionConstants.OP_FSTORE_0:
            case InstructionConstants.OP_FSTORE_1:
            case InstructionConstants.OP_FSTORE_2:
            case InstructionConstants.OP_FSTORE_3:
                variables.store(variableIndex, stack.fpop());
                break;
            case InstructionConstants.OP_DSTORE:
            case InstructionConstants.OP_DSTORE_0:
            case InstructionConstants.OP_DSTORE_1:
            case InstructionConstants.OP_DSTORE_2:
            case InstructionConstants.OP_DSTORE_3:
                variables.store(variableIndex, stack.dpop());
                break;
            case InstructionConstants.OP_ASTORE:
            case InstructionConstants.OP_ASTORE_0:
            case InstructionConstants.OP_ASTORE_1:
            case InstructionConstants.OP_ASTORE_2:
            case InstructionConstants.OP_ASTORE_3:
                variables.store(variableIndex, stack.pop());
                break;
            case InstructionConstants.OP_IINC:
                variables.store(variableIndex,
                                variables.iload(variableIndex).add(
                                valueFactory.createIntegerValue(variableInstruction.constant)));
                break;
            case InstructionConstants.OP_RET:
                InstructionOffsetValue instructionOffsetValue = variables.oload(variableIndex);
                branchUnit.branch(clazz,
                                  codeAttribute,
                                  offset,
                                  instructionOffsetValue.instructionOffset(instructionOffsetValue.instructionOffsetCount()-1));
                break;
            default:
                throw new IllegalArgumentException("Unknown variable instruction ["+variableInstruction.opcode+"]");
        }
    }
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        int branchTarget = offset + branchInstruction.branchOffset;
        switch (branchInstruction.opcode)
        {
            case InstructionConstants.OP_IFEQ:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.ipop().equal(valueFactory.createIntegerValue(0)));
                break;
            case InstructionConstants.OP_IFNE:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.ipop().notEqual(valueFactory.createIntegerValue(0)));
                break;
            case InstructionConstants.OP_IFLT:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.ipop().lessThan(valueFactory.createIntegerValue(0)));
                break;
            case InstructionConstants.OP_IFGE:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.ipop().greaterThanOrEqual(valueFactory.createIntegerValue(0)));
                break;
            case InstructionConstants.OP_IFGT:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.ipop().greaterThan(valueFactory.createIntegerValue(0)));
                break;
            case InstructionConstants.OP_IFLE:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.ipop().lessThanOrEqual(valueFactory.createIntegerValue(0)));
                break;
            case InstructionConstants.OP_IFICMPEQ:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.ipop().equal(stack.ipop()));
                break;
            case InstructionConstants.OP_IFICMPNE:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.ipop().notEqual(stack.ipop()));
                break;
            case InstructionConstants.OP_IFICMPLT:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.ipop().greaterThan(stack.ipop()));
                break;
            case InstructionConstants.OP_IFICMPGE:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.ipop().lessThanOrEqual(stack.ipop()));
                break;
            case InstructionConstants.OP_IFICMPGT:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.ipop().lessThan(stack.ipop()));
                break;
            case InstructionConstants.OP_IFICMPLE:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.ipop().greaterThanOrEqual(stack.ipop()));
                break;
            case InstructionConstants.OP_IFACMPEQ:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.apop().equal(stack.apop()));
                break;
            case InstructionConstants.OP_IFACMPNE:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.apop().notEqual(stack.apop()));
                break;
            case InstructionConstants.OP_GOTO:
            case InstructionConstants.OP_GOTO_W:
                branchUnit.branch(clazz, codeAttribute, offset, branchTarget);
                break;
            case InstructionConstants.OP_JSR:
            case InstructionConstants.OP_JSR_W:
                stack.push(new InstructionOffsetValue(offset +
                                                      branchInstruction.length(offset)));
                branchUnit.branch(clazz, codeAttribute, offset, branchTarget);
                break;
            case InstructionConstants.OP_IFNULL:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.apop().isNull());
                break;
            case InstructionConstants.OP_IFNONNULL:
                branchUnit.branchConditionally(clazz, codeAttribute, offset, branchTarget,
                    stack.apop().isNotNull());
                break;
            default:
                throw new IllegalArgumentException("Unknown branch instruction ["+branchInstruction.opcode+"]");
        }
    }
    public void visitTableSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        IntegerValue indexValue = stack.ipop();
        branchUnit.branch(clazz, codeAttribute,
                          offset,
                          offset + tableSwitchInstruction.defaultOffset);
        for (int index = 0; index < tableSwitchInstruction.jumpOffsets.length; index++)
        {
            int conditional = indexValue.equal(valueFactory.createIntegerValue(
                tableSwitchInstruction.lowCase + index));
            branchUnit.branchConditionally(clazz, codeAttribute,
                                           offset,
                                           offset + tableSwitchInstruction.jumpOffsets[index],
                                           conditional);
            if (conditional == Value.ALWAYS)
            {
                break;
            }
        }
    }
    public void visitLookUpSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        IntegerValue indexValue = stack.ipop();
        branchUnit.branch(clazz, codeAttribute,
                          offset,
                          offset + lookUpSwitchInstruction.defaultOffset);
        for (int index = 0; index < lookUpSwitchInstruction.jumpOffsets.length; index++)
        {
            int conditional = indexValue.equal(valueFactory.createIntegerValue(
                lookUpSwitchInstruction.cases[index]));
            branchUnit.branchConditionally(clazz, codeAttribute,
                                           offset,
                                           offset + lookUpSwitchInstruction.jumpOffsets[index],
                                           conditional);
            if (conditional == Value.ALWAYS)
            {
                break;
            }
        }
    }
    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
    {
        cpValue = valueFactory.createIntegerValue(integerConstant.getValue());
    }
    public void visitLongConstant(Clazz clazz, LongConstant longConstant)
    {
        cpValue = valueFactory.createLongValue(longConstant.getValue());
    }
    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
    {
        cpValue = valueFactory.createFloatValue(floatConstant.getValue());
    }
    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
    {
        cpValue = valueFactory.createDoubleValue(doubleConstant.getValue());
    }
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        cpValue = valueFactory.createReferenceValue(ClassConstants.INTERNAL_NAME_JAVA_LANG_STRING,
                                                    stringConstant.javaLangStringClass,
                                                    false);
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        cpValue = handleClassConstantAsClassValue ?
            valueFactory.createReferenceValue(ClassConstants.INTERNAL_NAME_JAVA_LANG_CLASS,
                                              classConstant.javaLangClassClass,
                                              false) :
            valueFactory.createReferenceValue(classConstant.getName(clazz),
                                              classConstant.referencedClass,
                                              false);
    }
    private Value cpValue(Clazz clazz,
                          int   constantIndex)
    {
        return cpValue(clazz, constantIndex, false);
    }
    private Value cpValue(Clazz   clazz,
                          int     constantIndex,
                          boolean handleClassConstantAsClassValue)
    {
        this.handleClassConstantAsClassValue = handleClassConstantAsClassValue;
        clazz.constantPoolEntryAccept(constantIndex, this);
        return cpValue;
    }
}
