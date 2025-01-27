class WhileStatement extends Statement {
    Expression cond;
    Statement body;
    public WhileStatement(long where, Expression cond, Statement body) {
        super(WHILE, where);
        this.cond = cond;
        this.body = body;
    }
    Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        checkLabel(env, ctx);
        CheckContext newctx = new CheckContext(ctx, this);
        Vset vsEntry = vset.copy();
        ConditionVars cvars =
              cond.checkCondition(env, newctx, reach(env, vset), exp);
        cond = convert(env, newctx, Type.tBoolean, cond);
        vset = body.check(env, newctx, cvars.vsTrue, exp);
        vset = vset.join(newctx.vsContinue);
        ctx.checkBackBranch(env, this, vsEntry, vset);
        vset = newctx.vsBreak.join(cvars.vsFalse);
        return ctx.removeAdditionalVars(vset);
    }
    public Statement inline(Environment env, Context ctx) {
        ctx = new Context(ctx, this);
        cond = cond.inlineValue(env, ctx);
        if (body != null) {
            body = body.inline(env, ctx);
        }
        return this;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 + cond.costInline(thresh, env, ctx)
                 + ((body != null) ? body.costInline(thresh, env, ctx) : 0);
    }
    public Statement copyInline(Context ctx, boolean valNeeded) {
        WhileStatement s = (WhileStatement)clone();
        s.cond = cond.copyInline(ctx);
        if (body != null) {
            s.body = body.copyInline(ctx, valNeeded);
        }
        return s;
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        CodeContext newctx = new CodeContext(ctx, this);
        asm.add(where, opc_goto, newctx.contLabel);
        Label l1 = new Label();
        asm.add(l1);
        if (body != null) {
            body.code(env, newctx, asm);
        }
        asm.add(newctx.contLabel);
        cond.codeBranch(env, newctx, asm, l1, true);
        asm.add(newctx.breakLabel);
    }
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("while ");
        cond.print(out);
        if (body != null) {
            out.print(" ");
            body.print(out, indent);
        } else {
            out.print(";");
        }
    }
}
