public final class CodeItem extends OffsettedItem {
    private static final int ALIGNMENT = 4;
    private static final int HEADER_SIZE = 16;
    private final CstMethodRef ref;
    private final DalvCode code;
    private CatchStructs catches;
    private final boolean isStatic;
    private final TypeList throwsList;
    private DebugInfoItem debugInfo;
    public CodeItem(CstMethodRef ref, DalvCode code, boolean isStatic,
            TypeList throwsList) {
        super(ALIGNMENT, -1);
        if (ref == null) {
            throw new NullPointerException("ref == null");
        }
        if (code == null) {
            throw new NullPointerException("code == null");
        }
        if (throwsList == null) {
            throw new NullPointerException("throwsList == null");
        }
        this.ref = ref;
        this.code = code;
        this.isStatic = isStatic;
        this.throwsList = throwsList;
        this.catches = null;
        this.debugInfo = null;
    }
    @Override
    public ItemType itemType() {
        return ItemType.TYPE_CODE_ITEM;
    }
    public void addContents(DexFile file) {
        MixedItemSection byteData = file.getByteData();
        TypeIdsSection typeIds = file.getTypeIds();
        if (code.hasPositions() || code.hasLocals()) {
            debugInfo = new DebugInfoItem(code, isStatic, ref);
            byteData.add(debugInfo);
        }
        if (code.hasAnyCatches()) {
            for (Type type : code.getCatchTypes()) {
                typeIds.intern(type);
            }
            catches = new CatchStructs(code);
        }
        for (Constant c : code.getInsnConstants()) {
            file.internIfAppropriate(c);
        }
    }
    @Override
    public String toString() {
        return "CodeItem{" + toHuman() + "}";
    }
    @Override
    public String toHuman() {
        return ref.toHuman();
    }
    public CstMethodRef getRef() {
        return ref;
    }
    public void debugPrint(PrintWriter out, String prefix, boolean verbose) {
        out.println(ref.toHuman() + ":");
        DalvInsnList insns = code.getInsns();
        out.println("regs: " + Hex.u2(getRegistersSize()) +
                "; ins: " + Hex.u2(getInsSize()) + "; outs: " +
                Hex.u2(getOutsSize()));
        insns.debugPrint(out, prefix, verbose);
        String prefix2 = prefix + "  ";
        if (catches != null) {
            out.print(prefix);
            out.println("catches");
            catches.debugPrint(out, prefix2);
        }
        if (debugInfo != null) {
            out.print(prefix);
            out.println("debug info");
            debugInfo.debugPrint(out, prefix2);
        }
    }
    @Override
    protected void place0(Section addedTo, int offset) {
        final DexFile file = addedTo.getFile();
        int catchesSize;
        code.assignIndices(new DalvCode.AssignIndicesCallback() {
                public int getIndex(Constant cst) {
                    IndexedItem item = file.findItemOrNull(cst);
                    if (item == null) {
                        return -1;
                    }
                    return item.getIndex();
                }
            });
        if (catches != null) {
            catches.encode(file);
            catchesSize = catches.writeSize();
        } else {
            catchesSize = 0;
        }
        int insnsSize = code.getInsns().codeSize();
        if ((insnsSize & 1) != 0) {
            insnsSize++;
        }
        setWriteSize(HEADER_SIZE + (insnsSize * 2) + catchesSize);
    }
    @Override
    protected void writeTo0(DexFile file, AnnotatedOutput out) {
        boolean annotates = out.annotates();
        int regSz = getRegistersSize();
        int outsSz = getOutsSize();
        int insSz = getInsSize();
        int insnsSz = code.getInsns().codeSize();
        boolean needPadding = (insnsSz & 1) != 0;
        int triesSz = (catches == null) ? 0 : catches.triesSize();
        int debugOff = (debugInfo == null) ? 0 : debugInfo.getAbsoluteOffset();
        if (annotates) {
            out.annotate(0, offsetString() + ' ' + ref.toHuman());
            out.annotate(2, "  registers_size: " + Hex.u2(regSz));
            out.annotate(2, "  ins_size:       " + Hex.u2(insSz));
            out.annotate(2, "  outs_size:      " + Hex.u2(outsSz));
            out.annotate(2, "  tries_size:     " + Hex.u2(triesSz));
            out.annotate(4, "  debug_off:      " + Hex.u4(debugOff));
            out.annotate(4, "  insns_size:     " + Hex.u4(insnsSz));
            int size = throwsList.size();
            if (size != 0) {
                out.annotate(0, "  throws " + StdTypeList.toHuman(throwsList));
            }
        }
        out.writeShort(regSz);
        out.writeShort(insSz);
        out.writeShort(outsSz);
        out.writeShort(triesSz);
        out.writeInt(debugOff);
        out.writeInt(insnsSz);
        writeCodes(file, out);
        if (catches != null) {
            if (needPadding) {
                if (annotates) {
                    out.annotate(2, "  padding: 0");
                }
                out.writeShort(0);
            }
            catches.writeTo(file, out);
        }
        if (annotates) {
            if (debugInfo != null) {
                out.annotate(0, "  debug info");
                debugInfo.annotateTo(file, out, "    ");
            }
        }
    }
    private void writeCodes(DexFile file, AnnotatedOutput out) {
        DalvInsnList insns = code.getInsns();
        try {
            insns.writeTo(out);
        } catch (RuntimeException ex) {
            throw ExceptionWithContext.withContext(ex, "...while writing " +
                    "instructions for " + ref.toHuman());
        }
    }
    private int getInsSize() {
        return ref.getParameterWordCount(isStatic);
    }
    private int getOutsSize() {
        return code.getInsns().getOutsSize();
    }
    private int getRegistersSize() {
        return code.getInsns().getRegistersSize();
    }
}
