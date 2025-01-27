class JavahFileManager extends JavacFileManager {
    private JavahFileManager(Context context, Charset charset) {
        super(context, true, charset);
        setIgnoreSymbolFile(true);
    }
    static JavahFileManager create(final DiagnosticListener<? super JavaFileObject> dl, PrintWriter log) {
        Context javac_context = new Context();
        if (dl != null)
            javac_context.put(DiagnosticListener.class, dl);
        javac_context.put(com.sun.tools.javac.util.Log.outKey, log);
        return new JavahFileManager(javac_context, null);
    }
    void setIgnoreSymbolFile(boolean b) {
        ignoreSymbolFile = b;
    }
}
