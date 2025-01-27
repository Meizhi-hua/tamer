public class TestSymtabItems {
    public static void main(String... args) throws Exception {
        new TestSymtabItems().run();
    }
    void run() throws Exception {
        Context c = new Context();
        JavacFileManager.preRegister(c);
        Symtab syms = Symtab.instance(c);
        JavacTypes types = JavacTypes.instance(c);
        JavaCompiler.instance(c);  
        for (Field f: Symtab.class.getDeclaredFields()) {
            if (f.getName().toLowerCase().contains("methodhandle"))
                continue;
            Class<?> ft = f.getType();
            if (TypeMirror.class.isAssignableFrom(ft))
                print(f.getName(), (TypeMirror) f.get(syms), types);
            else if(Element.class.isAssignableFrom(ft))
                print(f.getName(), (Element) f.get(syms));
        }
        if (errors > 0)
            throw new Exception(errors + " errors occurred");
    }
    void print(String label, Element e) {
        ElemPrinter ep = new ElemPrinter();
        System.err.println("Test " + label);
        ep.visit(e);
        System.err.println();
    }
    void print(String label, TypeMirror t, Types types) {
        TypePrinter tp = new TypePrinter();
        System.err.println("Test " + label);
        tp.visit(t, types);
        System.err.println();
    }
    void error(String msg) {
        System.err.println("Error: " + msg);
        errors++;
    }
    int errors;
    class ElemPrinter extends ElementScanner7<Void, Void> {
        @Override
        public Void visitPackage(PackageElement e, Void p) {
            show("package", e);
            indent(+1);
            super.visitPackage(e, p);
            indent(-1);
            return null;
        }
        @Override
        public Void visitType(TypeElement e, Void p) {
            show("type", e);
            indent(+1);
            super.visitType(e, p);
            indent(-1);
            return null;
        }
        @Override
        public Void visitVariable(VariableElement e, Void p) {
            show("variable", e);
            indent(+1);
            super.visitVariable(e, p);
            indent(-1);
            return null;
        }
        @Override
        public Void visitExecutable(ExecutableElement e, Void p) {
            show("executable", e);
            indent(+1);
            super.visitExecutable(e, p);
            indent(-1);
            return null;
        }
        @Override
        public Void visitTypeParameter(TypeParameterElement e, Void p) {
            show("type parameter", e);
            indent(+1);
            super.visitTypeParameter(e, p);
            indent(-1);
            return null;
        }
        @Override
        public Void visitUnknown(Element e, Void p) {
            show("unknown", e);
            indent(+1);
            try {
                super.visitUnknown(e, p);
            } catch (UnknownElementException ex) {
                System.err.println("caught " + ex);
            }
            indent(-1);
            return null;
        }
        void indent(int i) {
            indent += i;
        }
        String sp(int w) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < w; i++)
                sb.append("    ");
            return sb.toString();
        }
        void show(String label, Element e) {
            System.err.println(sp(indent) + label
                    + ": mods:" + e.getModifiers()
                    + " " + e.getSimpleName()
                    + ", kind: " + e.getKind()
                    + ", type: " + e.asType()
                    + ", encl: " + e.getEnclosingElement());
            if (e instanceof ClassSymbol) {
                ClassSymbol csym = (ClassSymbol) e;
                if (csym.members_field == null)
                    error("members_field is null");
                if (csym.type == null)
                    System.err.println("type is null");
            }
        }
        int indent;
    };
    class TypePrinter extends SimpleTypeVisitor7<Void, Types> {
        @Override
        public Void defaultAction(TypeMirror m, Types types) {
            System.err.println(m.getKind() + " " + m + " " + types.asElement(m));
            return null;
        }
        @Override
        public Void visitUnknown(TypeMirror t, Types types) {
            try {
                return super.visitUnknown(t, types);
            } catch (UnknownTypeException ex) {
                System.err.println("caught " + ex);
                return null;
            }
        }
    };
}
