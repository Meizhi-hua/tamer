class MLetObjectInputStream extends ObjectInputStream {
    private MLet loader;
    public MLetObjectInputStream(InputStream in, MLet loader)
        throws IOException, StreamCorruptedException {
        super(in);
        if (loader == null) {
            throw new IllegalArgumentException("Illegal null argument to MLetObjectInputStream");
        }
        this.loader = loader;
    }
    private Class<?> primitiveType(char c) {
        switch(c) {
        case 'B':
            return Byte.TYPE;
        case 'C':
            return Character.TYPE;
        case 'D':
            return Double.TYPE;
        case 'F':
            return Float.TYPE;
        case 'I':
            return Integer.TYPE;
        case 'J':
            return Long.TYPE;
        case 'S':
            return Short.TYPE;
        case 'Z':
            return Boolean.TYPE;
        }
        return null;
    }
    @Override
    protected Class<?> resolveClass(ObjectStreamClass objectstreamclass)
        throws IOException, ClassNotFoundException {
        String s = objectstreamclass.getName();
        if (s.startsWith("[")) {
            int i;
            for (i = 1; s.charAt(i) == '['; i++);
            Class<?> class1;
            if (s.charAt(i) == 'L') {
                class1 = loader.loadClass(s.substring(i + 1, s.length() - 1));
            } else {
                if (s.length() != i + 1)
                    throw new ClassNotFoundException(s);
                class1 = primitiveType(s.charAt(i));
            }
            int ai[] = new int[i];
            for (int j = 0; j < i; j++)
                ai[j] = 0;
            return Array.newInstance(class1, ai).getClass();
        } else {
            return loader.loadClass(s);
        }
    }
    public ClassLoader getClassLoader() {
        return loader;
    }
}
