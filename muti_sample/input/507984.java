public class ClassObj extends Instance implements Comparable<ClassObj> {
    String mClassName;
    long mSuperclassId;
    String[] mFieldNames;
    int[] mFieldTypes;
    String[] mStaticFieldNames;
    int[] mStaticFieldTypes;
    byte[] mStaticFieldValues;
    ArrayList<Instance> mInstances = new ArrayList<Instance>();
    Set<ClassObj> mSubclasses = new HashSet<ClassObj>();
    int mSize;
    public ClassObj(long id, StackTrace stack, String className) {
        mId = id;
        mStack = stack;
        mClassName = className;
    }
    @Override
    public final void resolveReferences(State state) {
        ByteArrayInputStream bais =
            new ByteArrayInputStream(mStaticFieldValues);
        DataInputStream dis = new DataInputStream(bais);
        int[] types = mStaticFieldTypes;
        final int N = types.length;
        try {
            for (int i = 0; i < N; i++) {
                int type = types[i];
                int size = Types.getTypeSize(type);
                if (type == Types.OBJECT) {
                    long id;
                    if (size == 4) {
                        id = dis.readInt();
                    } else {
                        id = dis.readLong();
                    }
                    RootObj root = new RootObj(RootType.JAVA_STATIC, id);
                    if (id == 0) {
                        root.mComment = String.format(
                            "Static field %s:%s null",
                                mClassName,
                                mStaticFieldNames[i]);
                    } else {
                        Instance instance = state.findReference(id);
                        instance.addParent(this);
                        root.mComment = String.format(
                            "Static field %s:%s %s [%s] 0x%08x",
                                mClassName,
                                mStaticFieldNames[i],
                                instance.getTypeName(),
                                instance.mHeap.mName,
                                id);
                    }
                    mHeap.addRoot(root);
                } else {
                    dis.skipBytes(size);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (mSuperclassId != 0) {
            ClassObj superclass = state.findClass(mSuperclassId);
            superclass.addSubclass(this);
        }
    }
    public final void addSubclass(ClassObj subclass) {
        mSubclasses.add(subclass);
    }
    public final void dumpSubclasses() {
        for (ClassObj subclass: mSubclasses) {
            System.out.println("     " + subclass.mClassName);
        }
    }
    public final String toString() {
        return mClassName.replace('/', '.');
    }
    public final void addInstance(Instance instance) {
        mInstances.add(instance);
    }
    public final void setSuperclassId(long id) {
        mSuperclassId = id;
    }
    public final void setFieldNames(String[] names) {
        mFieldNames = names;
    }
    public final void setFieldTypes(int[] types) {
        mFieldTypes = types;
    }
    public final void setStaticFieldNames(String[] names) {
        mStaticFieldNames = names;
    }
    public final void setStaticFieldTypes(int[] types) {
        mStaticFieldTypes = types;
    }
    public final void setStaticFieldValues(byte[] values) {
        mStaticFieldValues = values;
    }
    public final void dump() {
        System.out.println("+----------  ClassObj dump for: " + mClassName);
        System.out.println("+-----  Static fields");
        for (int i = 0; i < mStaticFieldNames.length; i++) {
            System.out.println(mStaticFieldNames[i] + ": " 
                + mStaticFieldTypes[i]);
        }
        System.out.println("+-----  Instance fields");
        for (int i = 0; i < mFieldNames.length; i++) {
            System.out.println(mFieldNames[i] + ": " + mFieldTypes[i]);
        }
    }
    @Override
    public final String getTypeName() {
        return "class " + mClassName;
    }
    @Override
    public final void visit(Set<Instance> resultSet, Filter filter) {
        if (resultSet.contains(this)) {
            return;
        }
        if (filter != null) {
            if (filter.accept(this)) {
                resultSet.add(this);
            }
        } else {
            resultSet.add(this);
        }
        ByteArrayInputStream bais =
            new ByteArrayInputStream(mStaticFieldValues);
        DataInputStream dis = new DataInputStream(bais);
        int[] types = mStaticFieldTypes;
        final int N = types.length;
        State state = mHeap.mState;
        try {
            for (int i = 0; i < N; i++) {
                int type = types[i];
                int size = Types.getTypeSize(type);
                if (type == Types.OBJECT) {
                    long id;
                    if (size == 4) {
                        id = dis.readInt();
                    } else {
                        id = dis.readLong();
                    }
                    Instance instance = state.findReference(id);
                    if (instance != null) {
                        instance.visit(resultSet, filter);
                    }
                } else {
                    dis.skipBytes(size);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public final int compareTo(ClassObj o) {
        return mClassName.compareTo(o.mClassName);
    }
    public final boolean equals(Object o) {
        if (! (o instanceof ClassObj)) {
            return false;
        }
        return 0 == compareTo((ClassObj) o);
    }
}
