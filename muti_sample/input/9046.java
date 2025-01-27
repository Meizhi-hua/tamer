class NotEqualExpression extends BinaryEqualityExpression {
    public NotEqualExpression(long where, Expression left, Expression right) {
        super(NE, where, left, right);
    }
    Expression eval(int a, int b) {
        return new BooleanExpression(where, a != b);
    }
    Expression eval(long a, long b) {
        return new BooleanExpression(where, a != b);
    }
    Expression eval(float a, float b) {
        return new BooleanExpression(where, a != b);
    }
    Expression eval(double a, double b) {
        return new BooleanExpression(where, a != b);
    }
    Expression eval(boolean a, boolean b) {
        return new BooleanExpression(where, a != b);
    }
    Expression simplify() {
        if (left.isConstant() && !right.isConstant()) {
            return new NotEqualExpression(where, right, left);
        }
        return this;
    }
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        left.codeValue(env, ctx, asm);
        switch (left.type.getTypeCode()) {
          case TC_BOOLEAN:
          case TC_INT:
            if (!right.equals(0)) {
                right.codeValue(env, ctx, asm);
                asm.add(where, whenTrue ? opc_if_icmpne : opc_if_icmpeq, lbl, whenTrue);
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
          case TC_ARRAY:
          case TC_CLASS:
          case TC_NULL:
            if (right.equals(0)) {
                asm.add(where, whenTrue ? opc_ifnonnull : opc_ifnull, lbl, whenTrue);
            } else {
                right.codeValue(env, ctx, asm);
                asm.add(where, whenTrue ? opc_if_acmpne : opc_if_acmpeq, lbl, whenTrue);
            }
            return;
          default:
            throw new CompilerError("Unexpected Type");
        }
        asm.add(where, whenTrue ? opc_ifne : opc_ifeq, lbl, whenTrue);
    }
}
