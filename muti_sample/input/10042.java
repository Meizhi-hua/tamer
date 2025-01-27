public class PrintGenerator implements sun.rmi.rmic.Generator,
                                       sun.rmi.rmic.iiop.Constants {
    private static final int JAVA = 0;
    private static final int IDL = 1;
    private static final int BOTH = 2;
    private int whatToPrint; 
    private boolean global = false;
    private boolean qualified = false;
    private boolean trace = false;
    private boolean valueMethods = false;
    private IndentingWriter out;
    public PrintGenerator() {
        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        out = new IndentingWriter (writer);
    }
    public boolean parseArgs(String argv[], Main main) {
        for (int i = 0; i < argv.length; i++) {
            if (argv[i] != null) {
                String arg = argv[i].toLowerCase();
                if (arg.equals("-xprint")) {
                    whatToPrint = JAVA;
                    argv[i] = null;
                    if (i+1 < argv.length) {
                        if (argv[i+1].equalsIgnoreCase("idl")) {
                            argv[++i] = null;
                            whatToPrint = IDL;
                        } else if (argv[i+1].equalsIgnoreCase("both")) {
                            argv[++i] = null;
                            whatToPrint = BOTH;
                        }
                    }
                } else if (arg.equals("-xglobal")) {
                    global = true;
                    argv[i] = null;
                } else if (arg.equals("-xqualified")) {
                    qualified = true;
                    argv[i] = null;
                } else if (arg.equals("-xtrace")) {
                    trace = true;
                    argv[i] = null;
                } else if (arg.equals("-xvaluemethods")) {
                    valueMethods = true;
                    argv[i] = null;
                }
            }
        }
        return true;
    }
    public void generate(sun.rmi.rmic.BatchEnvironment env, ClassDefinition cdef, File destDir) {
        BatchEnvironment ourEnv = (BatchEnvironment) env;
        ContextStack stack = new ContextStack(ourEnv);
        stack.setTrace(trace);
        if (valueMethods) {
            ourEnv.setParseNonConforming(true);
        }
        CompoundType topType = CompoundType.forCompound(cdef,stack);
        if (topType != null) {
            try {
                Type[] theTypes = topType.collectMatching(TM_COMPOUND);
                for (int i = 0; i < theTypes.length; i++) {
                    out.pln("\n-----------------------------------------------------------\n");
                    Type theType = theTypes[i];
                    switch (whatToPrint) {
                    case JAVA:  theType.println(out,qualified,false,false);
                        break;
                    case IDL:   theType.println(out,qualified,true,global);
                        break;
                    case BOTH:  theType.println(out,qualified,false,false);
                        theType.println(out,qualified,true,global);
                        break;
                    default:    throw new CompilerError("Unknown type!");
                    }
                }
                out.flush();
            } catch (IOException e) {
                throw new CompilerError("PrintGenerator caught " + e);
            }
        }
    }
}
