public class T6769027 {
    enum OutputKind {
        RAW("rawDiagnostics","rawDiagnostics"),
        BASIC("","");
        String key;
        String value;
        void init(Options opts) {
            opts.put(key, value);
        }
        OutputKind(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
    enum CaretKind {
        DEFAULT("", ""),
        SHOW("showCaret","true"),
        HIDE("showCaret","false");
        String key;
        String value;
        void init(Options opts) {
            opts.put(key, value);
        }
        CaretKind(String key, String value) {
            this.key = key;
            this.value = value;
        }
        boolean isEnabled() {
            return this == DEFAULT || this == SHOW;
        }
    }
    enum SourceLineKind {
        DEFAULT("", ""),
        AFTER_SUMMARY("sourcePosition", "top"),
        BOTTOM("sourcePosition", "bottom");
        String key;
        String value;
        void init(Options opts) {
            opts.put(key, value);
        }
        SourceLineKind(String key, String value) {
            this.key = key;
            this.value = value;
        }
        boolean isAfterSummary() {
            return this == DEFAULT || this == AFTER_SUMMARY;
        }
    }
    enum XDiagsSource {
        DEFAULT(""),
        SOURCE("source"),
        NO_SOURCE("-source");
        String flag;
        void init(Options opts) {
            if (this != DEFAULT) {
                String flags = opts.get("diags");
                flags = flags == null ? flag : flags + "," + flag;
                opts.put("diags", flags);
            }
        }
        XDiagsSource(String flag) {
            this.flag = flag;
        }
        String getOutput(CaretKind caretKind, IndentKind indent, OutputKind outKind) {
            String spaces = (outKind == OutputKind.BASIC) ? indent.string : "";
            return "\n" + spaces + "This is a source line" +
                   (caretKind.isEnabled() ? "\n" + spaces + "     ^" : "");
        }
    }
    enum XDiagsCompact {
        DEFAULT(""),
        COMPACT("short"),
        NO_COMPACT("-short");
        String flag;
        void init(Options opts) {
            if (this != DEFAULT) {
                String flags = opts.get("diags");
                flags = flags == null ? flag : flags + "," + flag;
                opts.put("diags", flags);
            }
        }
        XDiagsCompact(String flag) {
            this.flag = flag;
        }
    }
    enum ErrorKind {
        SINGLE("single",
            "compiler.err.single: Hello!",
            "KXThis is a test error message Hello!"),
        DOUBLE("double",
            "compiler.err.double: Hello!",
            "KXThis is a test error message.\n" +
            "KXYThis is another line of the above error message Hello!");
        String key;
        String rawOutput;
        String nonRawOutput;
        String key() {
            return key;
        }
        ErrorKind(String key, String rawOutput, String nonRawOutput) {
            this.key = key;
            this.rawOutput = rawOutput;
            this.nonRawOutput = nonRawOutput;
        }
        String getOutput(OutputKind outKind, IndentKind summaryIndent, IndentKind detailsIndent) {
            return outKind == OutputKind.RAW ?
                rawOutput :
                nonRawOutput.replace("X", summaryIndent.string).replace("Y", detailsIndent.string).replace("K", "");
        }
        String getOutput(OutputKind outKind, IndentKind summaryIndent, IndentKind detailsIndent, String indent) {
            return outKind == OutputKind.RAW ?
                rawOutput :
                nonRawOutput.replace("X", summaryIndent.string).replace("Y", detailsIndent.string).replace("K", indent);
        }
    }
    enum MultilineKind {
        NONE(0),
        DOUBLE(1),
        NESTED(2),
        DOUBLE_NESTED(3);
        static String[][] rawTemplates = {
            {"", ",{(E),(E)}", ",{(E,{(E)})}", ",{(E,{(E)}),(E,{(E)})}"}, 
            {"", "", "", "",""}, 
            {"", ",{(E)}", ",{(E,{(E)})}", ",{(E,{(E)})}"}, 
            {"", ",{(E),(E)}", ",{(E)}", ",{(E),(E)}"}, 
            {"", ",{(E)}", ",{(E)}", ",{(E)}"}}; 
        static String[][] basicTemplates = {
            {"", "\nE\nE", "\nE\nQ", "\nE\nQ\nE\nQ"}, 
            {"", "", "", "",""}, 
            {"", "\nE", "\nE\nQ", "\nE\nQ"}, 
            {"", "\nE\nE", "\nE", "\nE\nE"}, 
            {"", "\nE", "\nE", "\nE"}}; 
        int index;
        MultilineKind (int index) {
            this.index = index;
        }
        boolean isDouble() {
            return this == DOUBLE || this == DOUBLE_NESTED;
        }
        boolean isNested() {
            return this == NESTED || this == DOUBLE_NESTED;
        }
        String getOutput(OutputKind outKind, ErrorKind errKind, MultilinePolicy policy,
                IndentKind summaryIndent, IndentKind detailsIndent, IndentKind multiIndent) {
            String constIndent = (errKind == ErrorKind.DOUBLE) ?
                summaryIndent.string + detailsIndent.string :
                summaryIndent.string;
            constIndent += multiIndent.string;
            String errMsg1 = errKind.getOutput(outKind, summaryIndent, detailsIndent, constIndent);
            String errMsg2 = errKind.getOutput(outKind, summaryIndent, detailsIndent, constIndent + constIndent);
            errMsg1 = errMsg1.replaceAll("compiler.err", "compiler.misc");
            errMsg1 = errMsg1.replaceAll("error message", "subdiagnostic");
            errMsg2 = errMsg2.replaceAll("compiler.err", "compiler.misc");
            errMsg2 = errMsg2.replaceAll("error message", "subdiagnostic");
            String template = outKind == OutputKind.RAW ?
                rawTemplates[policy.index][index] :
                basicTemplates[policy.index][index];
            template = template.replaceAll("E", errMsg1);
            return template.replaceAll("Q", errMsg2);
        }
    }
    enum MultilinePolicy {
        ENABLED(0, "multilinePolicy", "enabled"),
        DISABLED(1, "multilinePolicy", "disabled"),
        LIMIT_LENGTH(2, "multilinePolicy", "limit:1:*"),
        LIMIT_DEPTH(3, "multilinePolicy", "limit:*:1"),
        LIMIT_BOTH(4, "multilinePolicy", "limit:1:1");
        String name;
        String value;
        int index;
        MultilinePolicy(int index, String name, String value) {
            this.name = name;
            this.value = value;
            this.index = index;
        }
        void init(Options options) {
            options.put(name, value);
        }
    }
    enum PositionKind {
        NOPOS(Position.NOPOS, "- ", "error: "),
        POS(5, "Test.java:1:6: ", "/Test.java:1: error: ");
        int pos;
        String rawOutput;
        String nonRawOutput;
        PositionKind(int pos, String rawOutput, String nonRawOutput) {
            this.pos = pos;
            this.rawOutput = rawOutput;
            this.nonRawOutput = nonRawOutput;
        }
        JCDiagnostic.DiagnosticPosition pos() {
            return new JCDiagnostic.SimpleDiagnosticPosition(pos);
        }
        String getOutput(OutputKind outputKind) {
            return outputKind == OutputKind.RAW ?
                rawOutput :
                nonRawOutput;
        }
    }
    static class MyFileObject extends SimpleJavaFileObject {
        private String text;
        public MyFileObject(String text) {
            super(URI.create("myfo:/Test.java"), JavaFileObject.Kind.SOURCE);
            this.text = text;
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return text;
        }
    }
    enum IndentKind {
        NONE(""),
        CUSTOM("   ");
        String string;
        IndentKind(String indent) {
            string = indent;
        }
    }
    class MyLog extends Log {
        MyLog(Context ctx) {
            super(ctx);
        }
        @Override
        protected java.io.PrintWriter getWriterForDiagnosticType(JCDiagnostic.DiagnosticType dt) {
            return new java.io.PrintWriter(System.out);
        }
        @Override
        protected boolean shouldReport(JavaFileObject jfo, int pos) {
            return true;
        }
    }
    int nerrors = 0;
    void exec(OutputKind outputKind, ErrorKind errorKind, MultilineKind multiKind,
            MultilinePolicy multiPolicy, PositionKind posKind, XDiagsSource xdiagsSource,
            XDiagsCompact xdiagsCompact, CaretKind caretKind, SourceLineKind sourceLineKind,
            IndentKind summaryIndent, IndentKind detailsIndent, IndentKind sourceIndent,
            IndentKind subdiagsIndent) {
        Context ctx = new Context();
        Options options = Options.instance(ctx);
        outputKind.init(options);
        multiPolicy.init(options);
        xdiagsSource.init(options);
        xdiagsCompact.init(options);
        caretKind.init(options);
        sourceLineKind.init(options);
        String indentString = "";
        indentString = (summaryIndent == IndentKind.CUSTOM) ? "3" : "0";
        indentString += (detailsIndent == IndentKind.CUSTOM) ? "|3" : "|0";
        indentString += (sourceIndent == IndentKind.CUSTOM) ? "|3" : "|0";
        indentString += (subdiagsIndent == IndentKind.CUSTOM) ? "|3" : "|0";
        options.put("diagsIndentation", indentString);
        MyLog log = new MyLog(ctx);
        JavacMessages messages = JavacMessages.instance(ctx);
        messages.add("tester");
        JCDiagnostic.Factory diags = JCDiagnostic.Factory.instance(ctx);
        log.useSource(new MyFileObject("This is a source line"));
        JCDiagnostic d = diags.error(log.currentSource(),
            posKind.pos(),
            errorKind.key(), "Hello!");
        if (multiKind != MultilineKind.NONE) {
            JCDiagnostic sub = diags.fragment(errorKind.key(), "Hello!");
            if (multiKind.isNested())
                sub = new JCDiagnostic.MultilineDiagnostic(sub, List.of(sub));
            List<JCDiagnostic> subdiags = multiKind.isDouble() ?
                List.of(sub, sub) :
                List.of(sub);
            d = new JCDiagnostic.MultilineDiagnostic(d, subdiags);
        }
        String diag = log.getDiagnosticFormatter().format(d, messages.getCurrentLocale());
        checkOutput(diag,
                outputKind,
                errorKind,
                multiKind,
                multiPolicy,
                posKind,
                xdiagsSource,
                xdiagsCompact,
                caretKind,
                sourceLineKind,
                summaryIndent,
                detailsIndent,
                sourceIndent,
                subdiagsIndent);
    }
    void test() {
        for (OutputKind outputKind : OutputKind.values()) {
            for (ErrorKind errKind : ErrorKind.values()) {
                for (MultilineKind multiKind : MultilineKind.values()) {
                    for (MultilinePolicy multiPolicy : MultilinePolicy.values()) {
                        for (PositionKind posKind : PositionKind.values()) {
                            for (XDiagsSource xdiagsSource : XDiagsSource.values()) {
                                for (XDiagsCompact xdiagsCompact : XDiagsCompact.values()) {
                                    for (CaretKind caretKind : CaretKind.values()) {
                                        for (SourceLineKind sourceLineKind : SourceLineKind.values()) {
                                            for (IndentKind summaryIndent : IndentKind.values()) {
                                                for (IndentKind detailsIndent : IndentKind.values()) {
                                                    for (IndentKind sourceIndent : IndentKind.values()) {
                                                        for (IndentKind subdiagsIndent : IndentKind.values()) {
                                                            exec(outputKind,
                                                                errKind,
                                                                multiKind,
                                                                multiPolicy,
                                                                posKind,
                                                                xdiagsSource,
                                                                xdiagsCompact,
                                                                caretKind,
                                                                sourceLineKind,
                                                                summaryIndent,
                                                                detailsIndent,
                                                                sourceIndent,
                                                                subdiagsIndent);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (nerrors != 0)
            throw new AssertionError(nerrors + " errors found");
    }
    void printInfo(String msg, OutputKind outputKind, ErrorKind errorKind, MultilineKind multiKind,
            MultilinePolicy multiPolicy, PositionKind posKind, XDiagsSource xdiagsSource,
            XDiagsCompact xdiagsCompact, CaretKind caretKind, SourceLineKind sourceLineKind,
            IndentKind summaryIndent, IndentKind detailsIndent, IndentKind sourceIndent,
            IndentKind subdiagsIndent, String errorLine) {
        String sep = "*********************************************************";
        String desc = "raw=" + outputKind + " pos=" + posKind + " key=" + errorKind.key() +
                " multiline=" + multiKind +" multiPolicy=" + multiPolicy.value +
                " diags= " + java.util.Arrays.asList(xdiagsSource.flag, xdiagsCompact.flag) +
                " caret=" + caretKind + " sourcePosition=" + sourceLineKind +
                " summaryIndent=" + summaryIndent + " detailsIndent=" + detailsIndent +
                " sourceIndent=" + sourceIndent + " subdiagsIndent=" + subdiagsIndent;
        System.out.println(sep);
        System.out.println(desc);
        System.out.println(sep);
        System.out.println(msg);
        System.out.println("Diagnostic formatting problem - expected diagnostic...\n" + errorLine);
    }
    void checkOutput(String msg, OutputKind outputKind, ErrorKind errorKind, MultilineKind multiKind,
            MultilinePolicy multiPolicy, PositionKind posKind, XDiagsSource xdiagsSource,
            XDiagsCompact xdiagsCompact, CaretKind caretKind, SourceLineKind sourceLineKind,
            IndentKind summaryIndent, IndentKind detailsIndent, IndentKind sourceIndent,
            IndentKind subdiagsIndent) {
        boolean shouldPrintSource = posKind == PositionKind.POS &&
                xdiagsSource != XDiagsSource.NO_SOURCE &&
                (xdiagsSource == XDiagsSource.SOURCE ||
                outputKind == OutputKind.BASIC);
        String errorLine = posKind.getOutput(outputKind) +
                errorKind.getOutput(outputKind, summaryIndent, detailsIndent);
        if (xdiagsCompact != XDiagsCompact.COMPACT)
            errorLine += multiKind.getOutput(outputKind, errorKind, multiPolicy, summaryIndent, detailsIndent, subdiagsIndent);
        String[] lines = errorLine.split("\n");
        if (xdiagsCompact == XDiagsCompact.COMPACT) {
            errorLine = lines[0];
            lines = new String[] {errorLine};
        }
        if (shouldPrintSource) {
            if (sourceLineKind.isAfterSummary()) {
                String sep = "\n";
                if (lines.length == 1) {
                    errorLine += "\n";
                    sep = "";
                }
                errorLine = errorLine.replaceFirst("\n",
                        Matcher.quoteReplacement(xdiagsSource.getOutput(caretKind, sourceIndent, outputKind) + sep));
            }
            else
                errorLine += xdiagsSource.getOutput(caretKind, sourceIndent, outputKind);
        }
        if (!msg.equals(errorLine)) {
            printInfo(msg,
                    outputKind,
                    errorKind,
                    multiKind,
                    multiPolicy,
                    posKind,
                    xdiagsSource,
                    xdiagsCompact,
                    caretKind,
                    sourceLineKind,
                    summaryIndent,
                    detailsIndent,
                    sourceIndent,
                    subdiagsIndent,
                    errorLine);
            nerrors++;
        }
    }
    public static void main(String... args) throws Exception {
        new T6769027().test();
    }
}
