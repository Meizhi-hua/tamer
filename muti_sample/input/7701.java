class jvmtiEnvFill {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("usage: <filledFile> <stubFile> <resultFile>");
            System.exit(1);
        }
        String filledFN = args[0];
        String stubFN = args[1];
        String resultFN = args[2];
        SourceFile filledSF = new SourceFile(filledFN);
        SourceFile stubSF = new SourceFile(stubFN);
        stubSF.fill(filledSF);
        PrintWriter out = new PrintWriter(new FileWriter(resultFN));
        stubSF.output(out);
        out.close();
    }
}
class SourceFile {
    static final String endFilePrefix = "
    static final String functionPrefix = "JvmtiEnv::";
    final String fn;
    LineNumberReader in;
    String line;
    List<String> top = new ArrayList<String>();
    List<String> before = new ArrayList<String>();
    boolean inFilePrefix = true;
    List<Function> functions = new ArrayList<Function>();
    Map<String, Function> functionMap = new HashMap<String, Function>();
    class Function {
      String name;
      String args;
      String compareArgs;
      List comment;
      List<String> body = new ArrayList<String>();
      Function() throws IOException {
        line = in.readLine();
        String trimmed = line.trim();
        if (!trimmed.startsWith(functionPrefix)) {
            error("expected '" + functionPrefix + "'");
        }
        int index = trimmed.indexOf('(', functionPrefix.length());
        if (index == -1) {
            error("missing open paren");
        }
        name = trimmed.substring(functionPrefix.length(), index);
        int index2 = trimmed.indexOf(')', index);
        if (index2 == -1) {
            error("missing close paren - must be on same line");
        }
        args = trimmed.substring(index+1, index2);
        compareArgs = args.replaceAll("\\s", "");
        String tail = trimmed.substring(index2+1).trim();
        if (!tail.equals("{")) {
            error("function declaration first line must end with open bracket '{', instead got '" +
                   tail + "'");
        }
        while(true) {
            line = in.readLine();
            if (line == null) {
                line = ""; 
                error("unexpected end of file");
            }
            if (line.startsWith("}")) {
                break;
            }
            body.add(line);
        }
        String expected = "} ";
        trimmed = line.replaceAll("\\s","");
        if (!trimmed.equals(expected.replaceAll("\\s",""))) {
            error("function end is malformed - should be: " + expected);
        }
        comment = before;
        before = new ArrayList<String>();
      }
      void remove() {
        functionMap.remove(name);
      }
      String fileName() {
        return fn;
      }
      void fill(Function filledFunc) {
        if (filledFunc == null) {
            System.err.println("Warning: function " + name + " missing from filled file");
            body.add(0, "    ");
        } else {
            int fbsize = filledFunc.body.size();
            int bsize = body.size();
            if (fbsize > bsize  || !body.subList(bsize-fbsize,bsize).equals(filledFunc.body)) {
                body = filledFunc.body;
                if (!compareArgs.equals(filledFunc.compareArgs)) {
                    System.err.println("Warning: function " + name +
                                       ": filled and stub arguments differ");
                    System.err.println("  old (filled): " + filledFunc.args);
                    System.err.println("  new (stub): " + args);
                    body.add(0, "    ");
                }
            }
            filledFunc.remove();  
        }
      }
      void output(PrintWriter out) {
            Iterator it = comment.iterator();
            while (it.hasNext()) {
                out.println(it.next());
            }
            out.println("jvmtiError");
            out.print(functionPrefix);
            out.print(name);
            out.print('(');
            out.print(args);
            out.println(") {");
            it = body.iterator();
            while (it.hasNext()) {
                out.println(it.next());
            }
            out.print("} ");
      }
    }
    SourceFile(String fn) throws IOException {
        this.fn = fn;
        Reader reader = new FileReader(fn);
        in = new LineNumberReader(reader);
        while (readGaps()) {
            Function func = new Function();
            functionMap.put(func.name, func);
            functions.add(func);
        }
        in.close();
    }
    void error(String msg) {
        System.err.println("Fatal error parsing file: " + fn);
        System.err.println("Line number: " + in.getLineNumber());
        System.err.println("Error message: " + msg);
        System.err.println("Source line: " + line);
        System.exit(1);
    }
    boolean readGaps() throws IOException {
        while(true) {
            line = in.readLine();
            if (line == null) {
                return false; 
            }
            if (!inFilePrefix && line.startsWith("}")) {
                error("unexpected close bracket in first column, outside of function.\n");
            }
            String trimmed = line.trim();
            if (line.startsWith("jvmtiError")) {
                if (trimmed.equals("jvmtiError")) {
                    if (inFilePrefix) {
                        error("unexpected 'jvmtiError' line in file prefix.\n" +
                              "is '" + endFilePrefix + "'... line missing?");
                    }
                    return true; 
                } else {
                    error("extra characters at end of 'jvmtiError'");
                }
            }
            if (inFilePrefix) {
                top.add(line);
            } else {
                trimmed = line.trim();
                if (!trimmed.equals("") && !trimmed.startsWith("
                    error("only comments and blank lines allowed between functions");
                }
                before.add(line);
            }
            if (line.replaceAll("\\s","").toLowerCase().startsWith(endFilePrefix.replaceAll("\\s",""))) {
                if (!inFilePrefix) {
                    error("excess '" + endFilePrefix + "'");
                }
                inFilePrefix = false;
            }
        }
    }
    void fill(SourceFile filledSF) {
        top = filledSF.top;
        Iterator it = functions.iterator();
        while (it.hasNext()) {
            Function stubFunc = (Function)(it.next());
            Function filledFunc = (Function)filledSF.functionMap.get(stubFunc.name);
            stubFunc.fill(filledFunc);
        }
        if (filledSF.functionMap.size() > 0) {
            System.err.println("Warning: the following functions were present in the " +
                                "filled file but missing in the stub file and thus not copied:");
            it  = filledSF.functionMap.values().iterator();
            while (it.hasNext()) {
                System.err.println("        " + ((Function)(it.next())).name);
            }
        }
    }
    void output(PrintWriter out) {
        Iterator it = top.iterator();
        while (it.hasNext()) {
            out.println(it.next());
        }
        it = functions.iterator();
        while (it.hasNext()) {
            Function stubFunc = (Function)(it.next());
            stubFunc.output(out);
        }
    }
}
