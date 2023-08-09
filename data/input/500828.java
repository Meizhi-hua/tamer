public class OneLocalsArray extends LocalsArray {
    private final TypeBearer[] locals;
    public OneLocalsArray(int maxLocals) {
        super(maxLocals != 0);
        locals = new TypeBearer[maxLocals];
    }
    public OneLocalsArray copy() {
        OneLocalsArray result = new OneLocalsArray(locals.length);
        System.arraycopy(locals, 0, result.locals, 0, locals.length);
        return result;
    }
    public void annotate(ExceptionWithContext ex) {
        for (int i = 0; i < locals.length; i++) {
            TypeBearer type = locals[i];
            String s = (type == null) ? "<invalid>" : type.toString();
            ex.addContext("locals[" + Hex.u2(i) + "]: " + s);
        }
    }
    public String toHuman() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < locals.length; i++) {
            TypeBearer type = locals[i];
            String s = (type == null) ? "<invalid>" : type.toString();
            sb.append("locals[" + Hex.u2(i) + "]: " + s + "\n");
        }
        return sb.toString();
    }
    public void makeInitialized(Type type) {
        int len = locals.length;
        if (len == 0) {
            return;
        }
        throwIfImmutable();
        Type initializedType = type.getInitializedType();
        for (int i = 0; i < len; i++) {
            if (locals[i] == type) {
                locals[i] = initializedType;
            }
        }
    }
    public int getMaxLocals() {
        return locals.length;
    }
    public void set(int idx, TypeBearer type) {
        throwIfImmutable();
        try {
            type = type.getFrameType();
        } catch (NullPointerException ex) {
            throw new NullPointerException("type == null");
        }
        if (idx < 0) {
            throw new IndexOutOfBoundsException("idx < 0");
        }
        if (type.getType().isCategory2()) {
            locals[idx + 1] = null;
        }
        locals[idx] = type;
        if (idx != 0) {
            TypeBearer prev = locals[idx - 1];
            if ((prev != null) && prev.getType().isCategory2()) {
                locals[idx - 1] = null;
            }
        }
    }
    public void set(RegisterSpec spec) {
        set(spec.getReg(), spec);
    }
    public void invalidate(int idx) {
        throwIfImmutable();
        locals[idx] = null;
    }
    public TypeBearer getOrNull(int idx) {
        return locals[idx];
    }
    public TypeBearer get(int idx) {
        TypeBearer result = locals[idx];
        if (result == null) {
            return throwSimException(idx, "invalid");
        }
        return result;
    }
    public TypeBearer getCategory1(int idx) {
        TypeBearer result = get(idx);
        Type type = result.getType();
        if (type.isUninitialized()) {
            return throwSimException(idx, "uninitialized instance");
        }
        if (type.isCategory2()) {
            return throwSimException(idx, "category-2");
        }
        return result;
    }
    public TypeBearer getCategory2(int idx) {
        TypeBearer result = get(idx);
        if (result.getType().isCategory1()) {
            return throwSimException(idx, "category-1");
        }
        return result;
    }
    @Override
    public LocalsArray merge(LocalsArray other) {
        if (other instanceof OneLocalsArray) {
            return merge((OneLocalsArray)other);
        } else { 
            return other.merge(this);
        }
    }
    public OneLocalsArray merge(OneLocalsArray other) {
        try {
            return Merger.mergeLocals(this, other);
        } catch (SimException ex) {
            ex.addContext("underlay locals:");
            annotate(ex);
            ex.addContext("overlay locals:");
            other.annotate(ex);
            throw ex;
        }
    }
    @Override
    public LocalsArraySet mergeWithSubroutineCaller
            (LocalsArray other, int predLabel) {
        LocalsArraySet result = new LocalsArraySet(getMaxLocals());
        return result.mergeWithSubroutineCaller(other, predLabel);
    }
    @Override
    protected OneLocalsArray getPrimary() {
        return this;
    }
    private static TypeBearer throwSimException(int idx, String msg) {
        throw new SimException("local " + Hex.u2(idx) + ": " + msg);
    }
}
