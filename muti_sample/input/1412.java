class GreaterOrEqualExpression extends BinaryCompareExpression {
    public GreaterOrEqualExpression(long where, Expression left, Expression right) {
        super(GE, where, left, right);
    }
    Expression eval(int a, int b) {
        return new BooleanExpression(where, a >= b);
    }
    Expression eval(long a, long b) {
        return new BooleanExpression(where, a >= b);
    }
    Expression eval(float a, float b) {
        return new BooleanExpression(where, a >= b);
    }
    Expression eval(double a, double b) {
        return new BooleanExpression(where, a >= b);
    }
    Expression simplify() {
        if (left.isConstant() && !right.isConstant()) {
            return new LessOrEqualExpression(where, right, left);
        }
        return this;
    }
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        left.codeValue(env, ctx, asm);
        switch (left.type.getTypeCode()) {
          case TC_INT:
            if (!right.equals(0)) {
                right.codeValue(env, ctx, asm);
                asm.add(where, whenTrue ? opc_if_icmpge : opc_if_icmplt, lbl, whenTrue);
                return;
            }
            break;
          case TC_LONG:
            right.codeValue(env, ctx, asm);
            asm.add(where, opc_lcmp);
            break;
          case TC_FLOAT:
            right.codeValue(env, ctx, asm);
            asm.add(where, opc_fcmpl);
            break;
          case TC_DOUBLE:
            right.codeValue(env, ctx, asm);
            asm.add(where, opc_dcmpl);
            break;
          default:
            throw new CompilerError("Unexpected Type");
        }
        asm.add(where, whenTrue ? opc_ifge : opc_iflt, lbl, whenTrue);
    }
}