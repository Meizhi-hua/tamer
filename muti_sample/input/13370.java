class Assembler implements Constants {
    static final int NOTREACHED         = 0;
    static final int REACHED            = 1;
    static final int NEEDED             = 2;
    Label first = new Label();
    Instruction last = first;
    int maxdepth;
    int maxvar;
    int maxpc;
    public void add(Instruction inst) {
        if (inst != null) {
            last.next = inst;
            last = inst;
        }
    }
    public void add(long where, int opc) {
        add(new Instruction(where, opc, null));
    }
    public void add(long where, int opc, Object obj) {
        add(new Instruction(where, opc, obj));
    }
    public void add(long where, int opc, Object obj, boolean flagCondInverted) {
        add(new Instruction(where, opc, obj, flagCondInverted));
    }
    public void add(boolean flagNoCovered, long where, int opc, Object obj) {
        add(new Instruction(flagNoCovered, where, opc, obj));
    }
    public void add(long where, int opc, boolean flagNoCovered) {
        add(new Instruction(where, opc, flagNoCovered));
    }
    static Vector SourceClassList = new Vector();
    static Vector TmpCovTable = new Vector();
    static int[]  JcovClassCountArray = new int[CT_LAST_KIND + 1];
    static String JcovMagicLine     = "JCOV-DATA-FILE-VERSION: 2.0";
    static String JcovClassLine     = "CLASS: ";
    static String JcovSrcfileLine   = "SRCFILE: ";
    static String JcovTimestampLine = "TIMESTAMP: ";
    static String JcovDataLine      = "DATA: ";
    static String JcovHeadingLine   = "#kind\tcount";
    static int[]  arrayModifiers    =
                {M_PUBLIC, M_PRIVATE, M_PROTECTED, M_ABSTRACT, M_FINAL, M_INTERFACE};
    static int[]  arrayModifiersOpc =
                {PUBLIC, PRIVATE, PROTECTED, ABSTRACT, FINAL, INTERFACE};
    void optimize(Environment env, Label lbl) {
        lbl.pc = REACHED;
        for (Instruction inst = lbl.next ; inst != null ; inst = inst.next)  {
            switch (inst.pc) {
              case NOTREACHED:
                inst.optimize(env);
                inst.pc = REACHED;
                break;
              case REACHED:
                return;
              case NEEDED:
                break;
            }
            switch (inst.opc) {
              case opc_label:
              case opc_dead:
                if (inst.pc == REACHED) {
                    inst.pc = NOTREACHED;
                }
                break;
              case opc_ifeq:
              case opc_ifne:
              case opc_ifgt:
              case opc_ifge:
              case opc_iflt:
              case opc_ifle:
              case opc_if_icmpeq:
              case opc_if_icmpne:
              case opc_if_icmpgt:
              case opc_if_icmpge:
              case opc_if_icmplt:
              case opc_if_icmple:
              case opc_if_acmpeq:
              case opc_if_acmpne:
              case opc_ifnull:
              case opc_ifnonnull:
                optimize(env, (Label)inst.value);
                break;
              case opc_goto:
                optimize(env, (Label)inst.value);
                return;
              case opc_jsr:
                optimize(env, (Label)inst.value);
                break;
              case opc_ret:
              case opc_return:
              case opc_ireturn:
              case opc_lreturn:
              case opc_freturn:
              case opc_dreturn:
              case opc_areturn:
              case opc_athrow:
                return;
              case opc_tableswitch:
              case opc_lookupswitch: {
                SwitchData sw = (SwitchData)inst.value;
                optimize(env, sw.defaultLabel);
                for (Enumeration e = sw.tab.elements() ; e.hasMoreElements();) {
                    optimize(env, (Label)e.nextElement());
                }
                return;
              }
              case opc_try: {
                TryData td = (TryData)inst.value;
                td.getEndLabel().pc = NEEDED;
                for (Enumeration e = td.catches.elements() ; e.hasMoreElements();) {
                    CatchData cd = (CatchData)e.nextElement();
                    optimize(env, cd.getLabel());
                }
                break;
              }
            }
        }
    }
    boolean eliminate() {
        boolean change = false;
        Instruction prev = first;
        for (Instruction inst = first.next ; inst != null ; inst = inst.next) {
            if (inst.pc != NOTREACHED) {
                prev.next = inst;
                prev = inst;
                inst.pc = NOTREACHED;
            } else {
                change = true;
            }
        }
        first.pc = NOTREACHED;
        prev.next = null;
        return change;
    }
    public void optimize(Environment env) {
        do {
            optimize(env, first);
        } while (eliminate() && env.opt());
    }
    public void collect(Environment env, MemberDefinition field, ConstantPool tab) {
        if ((field != null) && env.debug_vars()) {
            if (field.getArguments() != null) {
                for (Enumeration e = field.getArguments().elements() ; e.hasMoreElements() ;) {
                    MemberDefinition f = (MemberDefinition)e.nextElement();
                    tab.put(f.getName().toString());
                    tab.put(f.getType().getTypeSignature());
                }
            }
        }
        for (Instruction inst = first ; inst != null ; inst = inst.next) {
            inst.collect(tab);
        }
    }
    void balance(Label lbl, int depth) {
        for (Instruction inst = lbl ; inst != null ; inst = inst.next)  {
            depth += inst.balance();
            if (depth < 0) {
               throw new CompilerError("stack under flow: " + inst.toString() + " = " + depth);
            }
            if (depth > maxdepth) {
                maxdepth = depth;
            }
            switch (inst.opc) {
              case opc_label:
                lbl = (Label)inst;
                if (inst.pc == REACHED) {
                    if (lbl.depth != depth) {
                        throw new CompilerError("stack depth error " +
                                                depth + "/" + lbl.depth +
                                                ": " + inst.toString());
                    }
                    return;
                }
                lbl.pc = REACHED;
                lbl.depth = depth;
                break;
              case opc_ifeq:
              case opc_ifne:
              case opc_ifgt:
              case opc_ifge:
              case opc_iflt:
              case opc_ifle:
              case opc_if_icmpeq:
              case opc_if_icmpne:
              case opc_if_icmpgt:
              case opc_if_icmpge:
              case opc_if_icmplt:
              case opc_if_icmple:
              case opc_if_acmpeq:
              case opc_if_acmpne:
              case opc_ifnull:
              case opc_ifnonnull:
                balance((Label)inst.value, depth);
                break;
              case opc_goto:
                balance((Label)inst.value, depth);
                return;
              case opc_jsr:
                balance((Label)inst.value, depth + 1);
                break;
              case opc_ret:
              case opc_return:
              case opc_ireturn:
              case opc_lreturn:
              case opc_freturn:
              case opc_dreturn:
              case opc_areturn:
              case opc_athrow:
                return;
              case opc_iload:
              case opc_fload:
              case opc_aload:
              case opc_istore:
              case opc_fstore:
              case opc_astore: {
                int v = ((inst.value instanceof Number)
                            ? ((Number)inst.value).intValue()
                            : ((LocalVariable)inst.value).slot) + 1;
                if (v > maxvar)
                    maxvar = v;
                break;
              }
              case opc_lload:
              case opc_dload:
              case opc_lstore:
              case opc_dstore: {
                int v = ((inst.value instanceof Number)
                            ? ((Number)inst.value).intValue()
                            : ((LocalVariable)inst.value).slot) + 2;
                if (v  > maxvar)
                    maxvar = v;
                break;
              }
              case opc_iinc: {
                  int v = ((int[])inst.value)[0] + 1;
                  if (v  > maxvar)
                      maxvar = v + 1;
                  break;
              }
              case opc_tableswitch:
              case opc_lookupswitch: {
                SwitchData sw = (SwitchData)inst.value;
                balance(sw.defaultLabel, depth);
                for (Enumeration e = sw.tab.elements() ; e.hasMoreElements();) {
                    balance((Label)e.nextElement(), depth);
                }
                return;
              }
              case opc_try: {
                TryData td = (TryData)inst.value;
                for (Enumeration e = td.catches.elements() ; e.hasMoreElements();) {
                    CatchData cd = (CatchData)e.nextElement();
                    balance(cd.getLabel(), depth + 1);
                }
                break;
              }
            }
        }
    }
    public void write(Environment env, DataOutputStream out,
                      MemberDefinition field, ConstantPool tab)
                 throws IOException {
        if ((field != null) && field.getArguments() != null) {
              int sum = 0;
              Vector v = field.getArguments();
              for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
                  MemberDefinition f = ((MemberDefinition)e.nextElement());
                  sum += f.getType().stackSize();
              }
              maxvar = sum;
        }
        try {
            balance(first, 0);
        } catch (CompilerError e) {
            System.out.println("ERROR: " + e);
            listing(System.out);
            throw e;
        }
        int pc = 0, nexceptions = 0;
        for (Instruction inst = first ; inst != null ; inst = inst.next) {
            inst.pc = pc;
            int sz = inst.size(tab);
            if (pc<65536 && (pc+sz)>=65536) {
               env.error(inst.where, "warn.method.too.long");
            }
            pc += sz;
            if (inst.opc == opc_try) {
                nexceptions += ((TryData)inst.value).catches.size();
            }
        }
        out.writeShort(maxdepth);
        out.writeShort(maxvar);
        out.writeInt(maxpc = pc);
        for (Instruction inst = first.next ; inst != null ; inst = inst.next) {
            inst.write(out, tab);
        }
        out.writeShort(nexceptions);
        if (nexceptions > 0) {
            writeExceptions(env, out, tab, first, last);
        }
    }
    void writeExceptions(Environment env, DataOutputStream out, ConstantPool tab, Instruction first, Instruction last) throws IOException {
        for (Instruction inst = first ; inst != last.next ; inst = inst.next) {
            if (inst.opc == opc_try) {
                TryData td = (TryData)inst.value;
                writeExceptions(env, out, tab, inst.next, td.getEndLabel());
                for (Enumeration e = td.catches.elements() ; e.hasMoreElements();) {
                    CatchData cd = (CatchData)e.nextElement();
                    out.writeShort(inst.pc);
                    out.writeShort(td.getEndLabel().pc);
                    out.writeShort(cd.getLabel().pc);
                    if (cd.getType() != null) {
                        out.writeShort(tab.index(cd.getType()));
                    } else {
                        out.writeShort(0);
                    }
                }
                inst = td.getEndLabel();
            }
        }
    }
    public void writeCoverageTable(Environment env, ClassDefinition c, DataOutputStream out, ConstantPool tab, long whereField) throws IOException {
        Vector TableLot = new Vector();         
        boolean begseg = false;
        boolean begmeth = false;
        long whereClass = ((SourceClass)c).getWhere();
        Vector whereTry = new Vector();
        int numberTry = 0;
        int count = 0;
        for (Instruction inst = first ; inst != null ; inst = inst.next) {
            long n = (inst.where >> WHEREOFFSETBITS);
            if (n > 0 && inst.opc != opc_label) {
                if (!begmeth) {
                  if ( whereClass == inst.where)
                        TableLot.addElement(new Cover(CT_FIKT_METHOD, whereField, inst.pc));
                  else
                        TableLot.addElement(new Cover(CT_METHOD, whereField, inst.pc));
                  count++;
                  begmeth = true;
                }
                if (!begseg && !inst.flagNoCovered ) {
                  boolean findTry = false;
                  for (Enumeration e = whereTry.elements(); e.hasMoreElements();) {
                       if ( ((Long)(e.nextElement())).longValue() == inst.where) {
                              findTry = true;
                              break;
                       }
                  }
                  if (!findTry) {
                      TableLot.addElement(new Cover(CT_BLOCK, inst.where, inst.pc));
                      count++;
                      begseg = true;
                  }
                }
            }
            switch (inst.opc) {
              case opc_label:
                begseg = false;
                break;
              case opc_ifeq:
              case opc_ifne:
              case opc_ifnull:
              case opc_ifnonnull:
              case opc_ifgt:
              case opc_ifge:
              case opc_iflt:
              case opc_ifle:
              case opc_if_icmpeq:
              case opc_if_icmpne:
              case opc_if_icmpgt:
              case opc_if_icmpge:
              case opc_if_icmplt:
              case opc_if_icmple:
              case opc_if_acmpeq:
              case opc_if_acmpne: {
                if ( inst.flagCondInverted ) {
                   TableLot.addElement(new Cover(CT_BRANCH_TRUE, inst.where, inst.pc));
                   TableLot.addElement(new Cover(CT_BRANCH_FALSE, inst.where, inst.pc));
                } else {
                   TableLot.addElement(new Cover(CT_BRANCH_FALSE, inst.where, inst.pc));
                   TableLot.addElement(new Cover(CT_BRANCH_TRUE, inst.where, inst.pc));
                }
                count += 2;
                begseg = false;
                break;
              }
              case opc_goto: {
                begseg = false;
                break;
              }
              case opc_ret:
              case opc_return:
              case opc_ireturn:
              case opc_lreturn:
              case opc_freturn:
              case opc_dreturn:
              case opc_areturn:
              case opc_athrow: {
                break;
              }
              case opc_try: {
                whereTry.addElement(new Long(inst.where));
                begseg = false;
                break;
              }
              case opc_tableswitch: {
                SwitchData sw = (SwitchData)inst.value;
                for (int i = sw.minValue; i <= sw.maxValue; i++) {
                     TableLot.addElement(new Cover(CT_CASE, sw.whereCase(new Integer(i)), inst.pc));
                     count++;
                }
                if (!sw.getDefault()) {
                     TableLot.addElement(new Cover(CT_SWITH_WO_DEF, inst.where, inst.pc));
                     count++;
                } else {
                     TableLot.addElement(new Cover(CT_CASE, sw.whereCase("default"), inst.pc));
                     count++;
                }
                begseg = false;
                break;
              }
              case opc_lookupswitch: {
                SwitchData sw = (SwitchData)inst.value;
                for (Enumeration e = sw.sortedKeys(); e.hasMoreElements() ; ) {
                     Integer v = (Integer)e.nextElement();
                     TableLot.addElement(new Cover(CT_CASE, sw.whereCase(v), inst.pc));
                     count++;
                }
                if (!sw.getDefault()) {
                     TableLot.addElement(new Cover(CT_SWITH_WO_DEF, inst.where, inst.pc));
                     count++;
                } else {
                     TableLot.addElement(new Cover(CT_CASE, sw.whereCase("default"), inst.pc));
                     count++;
                }
                begseg = false;
                break;
              }
            }
        }
        Cover Lot;
        long ln, pos;
        out.writeShort(count);
        for (int i = 0; i < count; i++) {
           Lot = (Cover)TableLot.elementAt(i);
           ln = (Lot.Addr >> WHEREOFFSETBITS);
           pos = (Lot.Addr << (64 - WHEREOFFSETBITS)) >> (64 - WHEREOFFSETBITS);
           out.writeShort(Lot.NumCommand);
           out.writeShort(Lot.Type);
           out.writeInt((int)ln);
           out.writeInt((int)pos);
           if ( !(Lot.Type == CT_CASE && Lot.Addr == 0) ) {
                JcovClassCountArray[Lot.Type]++;
           }
        }
    }
public void addNativeToJcovTab(Environment env, ClassDefinition c) {
        JcovClassCountArray[CT_METHOD]++;
}
private String createClassJcovElement(Environment env, ClassDefinition c) {
        String SourceClass = (Type.mangleInnerType((c.getClassDeclaration()).getName())).toString();
        String ConvSourceClass;
        String classJcovLine;
        SourceClassList.addElement(SourceClass);
        ConvSourceClass = SourceClass.replace('.', '/');
        classJcovLine = JcovClassLine + ConvSourceClass;
        classJcovLine = classJcovLine + " [";
        String blank = "";
        for (int i = 0; i < arrayModifiers.length; i++ ) {
            if ((c.getModifiers() & arrayModifiers[i]) != 0) {
                classJcovLine = classJcovLine + blank + opNames[arrayModifiersOpc[i]];
                blank = " ";
            }
        }
        classJcovLine = classJcovLine + "]";
        return classJcovLine;
}
public void GenVecJCov(Environment env, ClassDefinition c, long Time) {
        String SourceFile = ((SourceClass)c).getAbsoluteName();
        TmpCovTable.addElement(createClassJcovElement(env, c));
        TmpCovTable.addElement(JcovSrcfileLine + SourceFile);
        TmpCovTable.addElement(JcovTimestampLine + Time);
        TmpCovTable.addElement(JcovDataLine + "A");             
        TmpCovTable.addElement(JcovHeadingLine);
        for (int i = CT_FIRST_KIND; i <= CT_LAST_KIND; i++) {
            if (JcovClassCountArray[i] != 0) {
                TmpCovTable.addElement(new String(i + "\t" + JcovClassCountArray[i]));
                JcovClassCountArray[i] = 0;
            }
        }
}
public void GenJCov(Environment env) {
     try {
        File outFile = env.getcovFile();
        if( outFile.exists()) {
           DataInputStream JCovd = new DataInputStream(
                                                       new BufferedInputStream(
                                                                               new FileInputStream(outFile)));
           String CurrLine = null;
           boolean first = true;
           String Class;
           CurrLine = JCovd.readLine();
           if ((CurrLine != null) && CurrLine.startsWith(JcovMagicLine)) {
                   while((CurrLine = JCovd.readLine()) != null ) {
                      if ( CurrLine.startsWith(JcovClassLine) ) {
                             first = true;
                             for(Enumeration e = SourceClassList.elements(); e.hasMoreElements();) {
                                 String clsName = CurrLine.substring(JcovClassLine.length());
                                 int idx = clsName.indexOf(' ');
                                 if (idx != -1) {
                                     clsName = clsName.substring(0, idx);
                                 }
                                 Class = (String)e.nextElement();
                                 if ( Class.compareTo(clsName) == 0) {
                                     first = false;
                                     break;
                                 }
                             }
                      }
                      if (first)        
                          TmpCovTable.addElement(CurrLine);
                   }
           }
           JCovd.close();
        }
        PrintStream CovFile = new PrintStream(new DataOutputStream(new FileOutputStream(outFile)));
        CovFile.println(JcovMagicLine);
        for(Enumeration e = TmpCovTable.elements(); e.hasMoreElements();) {
              CovFile.println(e.nextElement());
        }
        CovFile.close();
    }
    catch (FileNotFoundException e) {
       System.out.println("ERROR: " + e);
    }
    catch (IOException e) {
       System.out.println("ERROR: " + e);
    }
}
    public void writeLineNumberTable(Environment env, DataOutputStream out, ConstantPool tab) throws IOException {
        long ln = -1;
        int count = 0;
        for (Instruction inst = first ; inst != null ; inst = inst.next) {
            long n = (inst.where >> WHEREOFFSETBITS);
            if ((n > 0) && (ln != n)) {
                ln = n;
                count++;
            }
        }
        ln = -1;
        out.writeShort(count);
        for (Instruction inst = first ; inst != null ; inst = inst.next) {
            long n = (inst.where >> WHEREOFFSETBITS);
            if ((n > 0) && (ln != n)) {
                ln = n;
                out.writeShort(inst.pc);
                out.writeShort((int)ln);
            }
        }
    }
    void flowFields(Environment env, Label lbl, MemberDefinition locals[]) {
        if (lbl.locals != null) {
            MemberDefinition f[] = lbl.locals;
            for (int i = 0 ; i < maxvar ; i++) {
                if (f[i] != locals[i]) {
                    f[i] = null;
                }
            }
            return;
        }
        lbl.locals = new MemberDefinition[maxvar];
        System.arraycopy(locals, 0, lbl.locals, 0, maxvar);
        MemberDefinition newlocals[] = new MemberDefinition[maxvar];
        System.arraycopy(locals, 0, newlocals, 0, maxvar);
        locals = newlocals;
        for (Instruction inst = lbl.next ; inst != null ; inst = inst.next)  {
            switch (inst.opc) {
              case opc_istore:   case opc_istore_0: case opc_istore_1:
              case opc_istore_2: case opc_istore_3:
              case opc_fstore:   case opc_fstore_0: case opc_fstore_1:
              case opc_fstore_2: case opc_fstore_3:
              case opc_astore:   case opc_astore_0: case opc_astore_1:
              case opc_astore_2: case opc_astore_3:
              case opc_lstore:   case opc_lstore_0: case opc_lstore_1:
              case opc_lstore_2: case opc_lstore_3:
              case opc_dstore:   case opc_dstore_0: case opc_dstore_1:
              case opc_dstore_2: case opc_dstore_3:
                if (inst.value instanceof LocalVariable) {
                    LocalVariable v = (LocalVariable)inst.value;
                    locals[v.slot] = v.field;
                }
                break;
              case opc_label:
                flowFields(env, (Label)inst, locals);
                return;
              case opc_ifeq: case opc_ifne: case opc_ifgt:
              case opc_ifge: case opc_iflt: case opc_ifle:
              case opc_if_icmpeq: case opc_if_icmpne: case opc_if_icmpgt:
              case opc_if_icmpge: case opc_if_icmplt: case opc_if_icmple:
              case opc_if_acmpeq: case opc_if_acmpne:
              case opc_ifnull: case opc_ifnonnull:
              case opc_jsr:
                flowFields(env, (Label)inst.value, locals);
                break;
              case opc_goto:
                flowFields(env, (Label)inst.value, locals);
                return;
              case opc_return:   case opc_ireturn:  case opc_lreturn:
              case opc_freturn:  case opc_dreturn:  case opc_areturn:
              case opc_athrow:   case opc_ret:
                return;
              case opc_tableswitch:
              case opc_lookupswitch: {
                SwitchData sw = (SwitchData)inst.value;
                flowFields(env, sw.defaultLabel, locals);
                for (Enumeration e = sw.tab.elements() ; e.hasMoreElements();) {
                    flowFields(env, (Label)e.nextElement(), locals);
                }
                return;
              }
              case opc_try: {
                Vector catches = ((TryData)inst.value).catches;
                for (Enumeration e = catches.elements(); e.hasMoreElements();) {
                    CatchData cd = (CatchData)e.nextElement();
                    flowFields(env, cd.getLabel(), locals);
                }
                break;
              }
            }
        }
    }
    public void writeLocalVariableTable(Environment env, MemberDefinition field, DataOutputStream out, ConstantPool tab) throws IOException {
        MemberDefinition locals[] = new MemberDefinition[maxvar];
        int i = 0;
        if ((field != null) && (field.getArguments() != null)) {
            int reg = 0;
            Vector v = field.getArguments();
            for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
                MemberDefinition f = ((MemberDefinition)e.nextElement());
                locals[reg] = f;
                reg += f.getType().stackSize();
            }
        }
        flowFields(env, first, locals);
        LocalVariableTable lvtab = new LocalVariableTable();
        for (i = 0; i < maxvar; i++)
            locals[i] = null;
        if ((field != null) && (field.getArguments() != null)) {
            int reg = 0;
            Vector v = field.getArguments();
            for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
                MemberDefinition f = ((MemberDefinition)e.nextElement());
                locals[reg] = f;
                lvtab.define(f, reg, 0, maxpc);
                reg += f.getType().stackSize();
            }
        }
        int pcs[] = new int[maxvar];
        for (Instruction inst = first ; inst != null ; inst = inst.next)  {
            switch (inst.opc) {
              case opc_istore:   case opc_istore_0: case opc_istore_1:
              case opc_istore_2: case opc_istore_3: case opc_fstore:
              case opc_fstore_0: case opc_fstore_1: case opc_fstore_2:
              case opc_fstore_3:
              case opc_astore:   case opc_astore_0: case opc_astore_1:
              case opc_astore_2: case opc_astore_3:
              case opc_lstore:   case opc_lstore_0: case opc_lstore_1:
              case opc_lstore_2: case opc_lstore_3:
              case opc_dstore:   case opc_dstore_0: case opc_dstore_1:
              case opc_dstore_2: case opc_dstore_3:
                if (inst.value instanceof LocalVariable) {
                    LocalVariable v = (LocalVariable)inst.value;
                    int pc = (inst.next != null) ? inst.next.pc : inst.pc;
                    if (locals[v.slot] != null) {
                        lvtab.define(locals[v.slot], v.slot, pcs[v.slot], pc);
                    }
                    pcs[v.slot] = pc;
                    locals[v.slot] = v.field;
                }
                break;
              case opc_label: {
                for (i = 0 ; i < maxvar ; i++) {
                    if (locals[i] != null) {
                        lvtab.define(locals[i], i, pcs[i], inst.pc);
                    }
                }
                int pc = inst.pc;
                MemberDefinition[] labelLocals = ((Label)inst).locals;
                if (labelLocals == null) { 
                    for (i = 0; i < maxvar; i++)
                        locals[i] = null;
                } else {
                    System.arraycopy(labelLocals, 0, locals, 0, maxvar);
                }
                for (i = 0 ; i < maxvar ; i++) {
                    pcs[i] = pc;
                }
                break;
              }
            }
        }
        for (i = 0 ; i < maxvar ; i++) {
            if (locals[i] != null) {
                lvtab.define(locals[i], i, pcs[i], maxpc);
            }
        }
        lvtab.write(env, out, tab);
    }
    public boolean empty() {
        return first == last;
    }
    public void listing(PrintStream out) {
        out.println("-- listing --");
        for (Instruction inst = first ; inst != null ; inst = inst.next) {
            out.println(inst.toString());
        }
    }
}
