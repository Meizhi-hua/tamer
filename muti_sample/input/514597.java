public class SsaDumper extends BlockDumper {
    public static void dump(byte[] bytes, PrintStream out,
            String filePath, Args args) {
        SsaDumper sd = new SsaDumper(bytes, out, filePath, args);
        sd.dump();
    }
    private SsaDumper(byte[] bytes, PrintStream out, String filePath,
            Args args) {
        super(bytes, out, filePath, true, args);
    }
    @Override
    public void endParsingMember(ByteArray bytes, int offset, String name,
            String descriptor, Member member) {
        if (!(member instanceof Method)) {
            return;
        }
        if (!shouldDumpMethod(name)) {
            return;
        }
        ConcreteMethod meth =
            new ConcreteMethod((Method) member, classFile, true, true);
        TranslationAdvice advice = DexTranslationAdvice.THE_ONE;
        RopMethod rmeth = Ropper.convert(meth, advice);
        SsaMethod ssaMeth = null;
        boolean isStatic = AccessFlags.isStatic(meth.getAccessFlags());
        int paramWidth = computeParamWidth(meth, isStatic);
        if (args.ssaStep == null) {
            ssaMeth = Optimizer.debugNoRegisterAllocation(rmeth,
                    paramWidth, isStatic, true, advice,
                    EnumSet.allOf(Optimizer.OptionalStep.class));
        } else if ("edge-split".equals(args.ssaStep)) {
            ssaMeth = Optimizer.debugEdgeSplit(rmeth, paramWidth,
                    isStatic, true, advice);
        } else if ("phi-placement".equals(args.ssaStep)) {
            ssaMeth = Optimizer.debugPhiPlacement(
                    rmeth, paramWidth, isStatic, true, advice);
        } else if ("renaming".equals(args.ssaStep)) {
            ssaMeth = Optimizer.debugRenaming(
                    rmeth, paramWidth, isStatic, true, advice);
        } else if ("dead-code".equals(args.ssaStep)) {
            ssaMeth = Optimizer.debugDeadCodeRemover(
                    rmeth, paramWidth, isStatic,true, advice);
        }
        StringBuffer sb = new StringBuffer(2000);
        sb.append("first ");
        sb.append(Hex.u2(
                ssaMeth.blockIndexToRopLabel(ssaMeth.getEntryBlockIndex())));
        sb.append('\n');
        ArrayList<SsaBasicBlock> blocks = ssaMeth.getBlocks();
        ArrayList<SsaBasicBlock> sortedBlocks = 
            (ArrayList<SsaBasicBlock>) blocks.clone();
        Collections.sort(sortedBlocks, SsaBasicBlock.LABEL_COMPARATOR);
        for (SsaBasicBlock block : sortedBlocks) {
            sb.append("block ")
                    .append(Hex.u2(block.getRopLabel())).append('\n');
            BitSet preds = block.getPredecessors();
            for (int i = preds.nextSetBit(0); i >= 0;
                 i = preds.nextSetBit(i+1)) {
                sb.append("  pred ");
                sb.append(Hex.u2(ssaMeth.blockIndexToRopLabel(i)));
                sb.append('\n');
            }
            sb.append("  live in:" + block.getLiveInRegs());
            sb.append("\n");
            for (SsaInsn insn : block.getInsns()) {
                sb.append("  ");
                sb.append(insn.toHuman());
                sb.append('\n');
            }
            if (block.getSuccessors().cardinality() == 0) {
                sb.append("  returns\n");
            } else {
                int primary = block.getPrimarySuccessorRopLabel();
                IntList succLabelList = block.getRopLabelSuccessorList();
                int szSuccLabels = succLabelList.size();
                for (int i = 0; i < szSuccLabels; i++) {
                    sb.append("  next ");
                    sb.append(Hex.u2(succLabelList.get(i)));
                    if (szSuccLabels != 1 && primary == succLabelList.get(i)) {
                        sb.append(" *");                        
                    }
                    sb.append('\n');
                }
            }
            sb.append("  live out:" + block.getLiveOutRegs());
            sb.append("\n");
        }
        suppressDump = false;
        setAt(bytes, 0);
        parsed(bytes, 0, bytes.size(), sb.toString());
        suppressDump = true;
    }
}
