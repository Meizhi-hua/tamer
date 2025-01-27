class ReflectAccess implements sun.reflect.LangReflectAccess {
    public Field newField(Class<?> declaringClass,
                          String name,
                          Class<?> type,
                          int modifiers,
                          int slot,
                          String signature,
                          byte[] annotations)
    {
        return new Field(declaringClass,
                         name,
                         type,
                         modifiers,
                         slot,
                         signature,
                         annotations);
    }
    public Method newMethod(Class<?> declaringClass,
                            String name,
                            Class<?>[] parameterTypes,
                            Class<?> returnType,
                            Class<?>[] checkedExceptions,
                            int modifiers,
                            int slot,
                            String signature,
                            byte[] annotations,
                            byte[] parameterAnnotations,
                            byte[] annotationDefault)
    {
        return new Method(declaringClass,
                          name,
                          parameterTypes,
                          returnType,
                          checkedExceptions,
                          modifiers,
                          slot,
                          signature,
                          annotations,
                          parameterAnnotations,
                          annotationDefault);
    }
    public <T> Constructor<T> newConstructor(Class<T> declaringClass,
                                             Class<?>[] parameterTypes,
                                             Class<?>[] checkedExceptions,
                                             int modifiers,
                                             int slot,
                                             String signature,
                                             byte[] annotations,
                                             byte[] parameterAnnotations)
    {
        return new Constructor<>(declaringClass,
                                  parameterTypes,
                                  checkedExceptions,
                                  modifiers,
                                  slot,
                                  signature,
                                  annotations,
                                  parameterAnnotations);
    }
    public MethodAccessor getMethodAccessor(Method m) {
        return m.getMethodAccessor();
    }
    public void setMethodAccessor(Method m, MethodAccessor accessor) {
        m.setMethodAccessor(accessor);
    }
    public ConstructorAccessor getConstructorAccessor(Constructor<?> c) {
        return c.getConstructorAccessor();
    }
    public void setConstructorAccessor(Constructor<?> c,
                                       ConstructorAccessor accessor)
    {
        c.setConstructorAccessor(accessor);
    }
    public int getConstructorSlot(Constructor<?> c) {
        return c.getSlot();
    }
    public String getConstructorSignature(Constructor<?> c) {
        return c.getSignature();
    }
    public byte[] getConstructorAnnotations(Constructor<?> c) {
        return c.getRawAnnotations();
    }
    public byte[] getConstructorParameterAnnotations(Constructor<?> c) {
        return c.getRawParameterAnnotations();
    }
    public Method      copyMethod(Method arg) {
        return arg.copy();
    }
    public Field       copyField(Field arg) {
        return arg.copy();
    }
    public <T> Constructor<T> copyConstructor(Constructor<T> arg) {
        return arg.copy();
    }
}
