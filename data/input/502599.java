public final class ClassDefItem extends IndexedItem {
    public static final int WRITE_SIZE = 32;
    private final CstType thisClass;
    private final int accessFlags;
    private final CstType superclass;
    private TypeListItem interfaces;
    private final CstUtf8 sourceFile;
    private final ClassDataItem classData;
    private EncodedArrayItem staticValuesItem;
    private AnnotationsDirectoryItem annotationsDirectory;
    public ClassDefItem(CstType thisClass, int accessFlags,
            CstType superclass, TypeList interfaces, CstUtf8 sourceFile) {
        if (thisClass == null) {
            throw new NullPointerException("thisClass == null");
        }
        if (interfaces == null) {
            throw new NullPointerException("interfaces == null");
        }
        this.thisClass = thisClass;
        this.accessFlags = accessFlags;
        this.superclass = superclass;
        this.interfaces = 
            (interfaces.size() == 0) ? null :  new TypeListItem(interfaces);
        this.sourceFile = sourceFile;
        this.classData = new ClassDataItem(thisClass);
        this.staticValuesItem = null;
        this.annotationsDirectory = new AnnotationsDirectoryItem();
    }
    @Override
    public ItemType itemType() {
        return ItemType.TYPE_CLASS_DEF_ITEM;
    }
    @Override
    public int writeSize() {
        return WRITE_SIZE;
    }
    @Override
    public void addContents(DexFile file) {
        TypeIdsSection typeIds = file.getTypeIds();
        MixedItemSection byteData = file.getByteData();
        MixedItemSection wordData = file.getWordData();
        MixedItemSection typeLists = file.getTypeLists();
        StringIdsSection stringIds = file.getStringIds();
        typeIds.intern(thisClass);
        if (!classData.isEmpty()) {
            MixedItemSection classDataSection = file.getClassData();
            classDataSection.add(classData);
            CstArray staticValues = classData.getStaticValuesConstant();
            if (staticValues != null) {
                staticValuesItem =
                    byteData.intern(new EncodedArrayItem(staticValues));
            }
        }
        if (superclass != null) {
            typeIds.intern(superclass);
        }
        if (interfaces != null) {
            interfaces = typeLists.intern(interfaces);
        }
        if (sourceFile != null) {
            stringIds.intern(sourceFile);
        }
        if (! annotationsDirectory.isEmpty()) {
            if (annotationsDirectory.isInternable()) {
                annotationsDirectory = wordData.intern(annotationsDirectory);
            } else {
                wordData.add(annotationsDirectory);
            }
        }
    }
    @Override
    public void writeTo(DexFile file, AnnotatedOutput out) {
        boolean annotates = out.annotates();
        TypeIdsSection typeIds = file.getTypeIds();
        int classIdx = typeIds.indexOf(thisClass);
        int superIdx = (superclass == null) ? -1 :
            typeIds.indexOf(superclass);
        int interOff = OffsettedItem.getAbsoluteOffsetOr0(interfaces);
        int annoOff = annotationsDirectory.isEmpty() ? 0 :
            annotationsDirectory.getAbsoluteOffset();
        int sourceFileIdx = (sourceFile == null) ? -1 :
            file.getStringIds().indexOf(sourceFile);
        int dataOff = classData.isEmpty()? 0 : classData.getAbsoluteOffset();
        int staticValuesOff =
            OffsettedItem.getAbsoluteOffsetOr0(staticValuesItem);
        if (annotates) {
            out.annotate(0, indexString() + ' ' + thisClass.toHuman());
            out.annotate(4, "  class_idx:           " + Hex.u4(classIdx));
            out.annotate(4, "  access_flags:        " + 
                         AccessFlags.classString(accessFlags));
            out.annotate(4, "  superclass_idx:      " + Hex.u4(superIdx) +
                         " 
                          superclass.toHuman()));
            out.annotate(4, "  interfaces_off:      " + Hex.u4(interOff));
            if (interOff != 0) {
                TypeList list = interfaces.getList();
                int sz = list.size();
                for (int i = 0; i < sz; i++) {
                    out.annotate(0, "    " + list.getType(i).toHuman());
                }
            }
            out.annotate(4, "  source_file_idx:     " + Hex.u4(sourceFileIdx) +
                         " 
                          sourceFile.toHuman()));
            out.annotate(4, "  annotations_off:     " + Hex.u4(annoOff));
            out.annotate(4, "  class_data_off:      " + Hex.u4(dataOff));
            out.annotate(4, "  static_values_off:   " +
                    Hex.u4(staticValuesOff));
        }
        out.writeInt(classIdx);
        out.writeInt(accessFlags);
        out.writeInt(superIdx);
        out.writeInt(interOff);
        out.writeInt(sourceFileIdx);
        out.writeInt(annoOff);
        out.writeInt(dataOff);
        out.writeInt(staticValuesOff);
    }
    public CstType getThisClass() {
        return thisClass;
    }
    public int getAccessFlags() {
        return accessFlags;
    }
    public CstType getSuperclass() {
        return superclass;
    }
    public TypeList getInterfaces() {
        if (interfaces == null) {
            return StdTypeList.EMPTY;
        }
        return interfaces.getList();
    }
    public CstUtf8 getSourceFile() {
        return sourceFile;
    }
    public void addStaticField(EncodedField field, Constant value) {
        classData.addStaticField(field, value);
    }
    public void addInstanceField(EncodedField field) {
        classData.addInstanceField(field);
    }
    public void addDirectMethod(EncodedMethod method) {
        classData.addDirectMethod(method);
    }
    public void addVirtualMethod(EncodedMethod method) {
        classData.addVirtualMethod(method);
    }
    public ArrayList<EncodedMethod> getMethods() {
        return classData.getMethods();
    }
    public void setClassAnnotations(Annotations annotations) {
        annotationsDirectory.setClassAnnotations(annotations);
    }
    public void addFieldAnnotations(CstFieldRef field,
            Annotations annotations) {
        annotationsDirectory.addFieldAnnotations(field, annotations);
    }
    public void addMethodAnnotations(CstMethodRef method,
            Annotations annotations) {
        annotationsDirectory.addMethodAnnotations(method, annotations);
    }
    public void addParameterAnnotations(CstMethodRef method,
            AnnotationsList list) {
        annotationsDirectory.addParameterAnnotations(method, list);
    }
    public Annotations getMethodAnnotations(CstMethodRef method) {
        return annotationsDirectory.getMethodAnnotations(method);
    }
    public AnnotationsList getParameterAnnotations(CstMethodRef method) {
        return annotationsDirectory.getParameterAnnotations(method);
    }
    public void debugPrint(Writer out, boolean verbose) {
        PrintWriter pw = Writers.printWriterFor(out);
        pw.println(getClass().getName() + " {");
        pw.println("  accessFlags: " + Hex.u2(accessFlags));
        pw.println("  superclass: " + superclass);
        pw.println("  interfaces: " +
                ((interfaces == null) ? "<none>" : interfaces));
        pw.println("  sourceFile: " +
                ((sourceFile == null) ? "<none>" : sourceFile.toQuoted()));
        classData.debugPrint(out, verbose);
        annotationsDirectory.debugPrint(pw);
        pw.println("}");
    }
}
