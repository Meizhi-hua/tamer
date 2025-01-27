class BinaryAssignExpression extends BinaryExpression {
    Expression implementation;
    BinaryAssignExpression(int op, long where, Expression left, Expression right) {
        super(op, where, left.type, left, right);
    }
    public Expression getImplementation() {
        if (implementation != null)
            return implementation;
        return this;
    }
    public Expression order() {
        if (precedence() >= left.precedence()) {
            UnaryExpression e = (UnaryExpression)left;
            left = e.right;
            e.right = order();
            return e;
        }
        return this;
    }
    public Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        return checkValue(env, ctx, vset, exp);
    }
    public Expression inline(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.inline(env, ctx);
        return inlineValue(env, ctx);
    }
    public Expression inlineValue(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.inlineValue(env, ctx);
        left = left.inlineLHS(env, ctx);
        right = right.inlineValue(env, ctx);
        return this;
    }
    public Expression copyInline(Context ctx) {
        if (implementation != null)
            return implementation.copyInline(ctx);
        return super.copyInline(ctx);
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        if (implementation != null)
            return implementation.costInline(thresh, env, ctx);
        return super.costInline(thresh, env, ctx);
    }
}
