class SourceOrderDeclScanner extends DeclarationScanner {
    static class SourceOrderComparator implements java.util.Comparator<Declaration> {
        SourceOrderComparator(){}
        static boolean equals(Declaration d1, Declaration d2) {
            return d1 == d2 || (d1 != null && d1.equals(d2));
        }
        private static class DeclPartialOrder extends com.sun.mirror.util.SimpleDeclarationVisitor {
            private int value = 1000;
            private static int staticAdjust(Declaration d) {
                return d.getModifiers().contains(Modifier.STATIC)?0:1;
            }
            DeclPartialOrder() {}
            public int getValue() { return value; }
            @Override
            public void visitTypeParameterDeclaration(TypeParameterDeclaration d) {value = 0;}
            @Override
            public void visitEnumConstantDeclaration(EnumConstantDeclaration d) {value = 1;}
            @Override
            public void visitClassDeclaration(ClassDeclaration d) {value = 2 + staticAdjust(d);}
            @Override
            public void visitInterfaceDeclaration(InterfaceDeclaration d) {value = 4;}
            @Override
            public void visitEnumDeclaration(EnumDeclaration d) {value = 6;}
            @Override
            public void visitAnnotationTypeDeclaration(AnnotationTypeDeclaration d) {value = 8;}
            @Override
            public void visitFieldDeclaration(FieldDeclaration d) {value = 10 + staticAdjust(d);}
            @Override
            public void visitConstructorDeclaration(ConstructorDeclaration d) {value = 12;}
            @Override
            public void visitMethodDeclaration(MethodDeclaration d) {value = 14 + staticAdjust(d);}
        }
        @SuppressWarnings("cast")
        private int compareEqualPosition(Declaration d1, Declaration d2) {
            assert
                (d1.getPosition() == d2.getPosition()) || 
                (d1.getPosition().file().compareTo(d2.getPosition().file()) == 0 &&
                 d1.getPosition().line()   == d2.getPosition().line() &&
                 d1.getPosition().column() == d2.getPosition().column());
            DeclPartialOrder dpo1 = new DeclPartialOrder();
            DeclPartialOrder dpo2 = new DeclPartialOrder();
            d1.accept(dpo1);
            d2.accept(dpo2);
            int difference = dpo1.getValue() - dpo2.getValue();
            if (difference != 0)
                return difference;
            else {
                int result = d1.getSimpleName().compareTo(d2.getSimpleName());
                if (result != 0)
                    return result;
                return (int)( Long.signum((long)System.identityHashCode(d1) -
                                          (long)System.identityHashCode(d2)));
            }
        }
        public int compare(Declaration d1, Declaration d2) {
            if (equals(d1, d2))
                return 0;
            SourcePosition p1 = d1.getPosition();
            SourcePosition p2 = d2.getPosition();
            if (p1 == null && p2 != null)
                return 1;
            else if (p1 != null && p2 == null)
                return -1;
            else if(p1 == null && p2 == null)
                return compareEqualPosition(d1, d2);
            else {
                assert p1 != null && p2 != null;
                int fileComp = p1.file().compareTo(p2.file()) ;
                if (fileComp == 0) {
                    long diff = (long)p1.line() - (long)p2.line();
                    if (diff == 0) {
                        diff = Long.signum((long)p1.column() - (long)p2.column());
                        if (diff != 0)
                            return (int)diff;
                        else {
                            return compareEqualPosition(d1, d2);
                        }
                    } else
                        return (diff<0)? -1:1;
                } else
                    return fileComp;
            }
        }
    }
    final static java.util.Comparator<Declaration> comparator = new SourceOrderComparator();
    SourceOrderDeclScanner(DeclarationVisitor pre, DeclarationVisitor post) {
        super(pre, post);
    }
    public void visitTypeDeclaration(TypeDeclaration d) {
        d.accept(pre);
        SortedSet<Declaration> decls = new
            TreeSet<Declaration>(SourceOrderDeclScanner.comparator) ;
        for(TypeParameterDeclaration tpDecl: d.getFormalTypeParameters()) {
            decls.add(tpDecl);
        }
        for(FieldDeclaration fieldDecl: d.getFields()) {
            decls.add(fieldDecl);
        }
        for(MethodDeclaration methodDecl: d.getMethods()) {
            decls.add(methodDecl);
        }
        for(TypeDeclaration typeDecl: d.getNestedTypes()) {
            decls.add(typeDecl);
        }
        for(Declaration decl: decls )
            decl.accept(this);
        d.accept(post);
    }
    public void visitClassDeclaration(ClassDeclaration d) {
        d.accept(pre);
        SortedSet<Declaration> decls = new
            TreeSet<Declaration>(SourceOrderDeclScanner.comparator) ;
        for(TypeParameterDeclaration tpDecl: d.getFormalTypeParameters()) {
            decls.add(tpDecl);
        }
        for(FieldDeclaration fieldDecl: d.getFields()) {
            decls.add(fieldDecl);
        }
        for(MethodDeclaration methodDecl: d.getMethods()) {
            decls.add(methodDecl);
        }
        for(TypeDeclaration typeDecl: d.getNestedTypes()) {
            decls.add(typeDecl);
        }
        for(ConstructorDeclaration ctorDecl: d.getConstructors()) {
            decls.add(ctorDecl);
        }
        for(Declaration decl: decls )
            decl.accept(this);
        d.accept(post);
    }
    public void visitExecutableDeclaration(ExecutableDeclaration d) {
        d.accept(pre);
        SortedSet<Declaration> decls = new
            TreeSet<Declaration>(SourceOrderDeclScanner.comparator) ;
        for(TypeParameterDeclaration tpDecl: d.getFormalTypeParameters())
            decls.add(tpDecl);
        for(ParameterDeclaration pDecl: d.getParameters())
            decls.add(pDecl);
        for(Declaration decl: decls )
            decl.accept(this);
        d.accept(post);
    }
}
