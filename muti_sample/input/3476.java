final class PerInterface<M> {
    PerInterface(Class<?> mbeanInterface, MBeanIntrospector<M> introspector,
                 MBeanAnalyzer<M> analyzer, MBeanInfo mbeanInfo) {
        this.mbeanInterface = mbeanInterface;
        this.introspector = introspector;
        this.mbeanInfo = mbeanInfo;
        analyzer.visit(new InitMaps());
    }
    Class<?> getMBeanInterface() {
        return mbeanInterface;
    }
    MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }
    boolean isMXBean() {
        return introspector.isMXBean();
    }
    Object getAttribute(Object resource, String attribute, Object cookie)
            throws AttributeNotFoundException,
                   MBeanException,
                   ReflectionException {
        final M cm = getters.get(attribute);
        if (cm == null) {
            final String msg;
            if (setters.containsKey(attribute))
                msg = "Write-only attribute: " + attribute;
            else
                msg = "No such attribute: " + attribute;
            throw new AttributeNotFoundException(msg);
        }
        return introspector.invokeM(cm, resource, (Object[]) null, cookie);
    }
    void setAttribute(Object resource, String attribute, Object value,
                      Object cookie)
            throws AttributeNotFoundException,
                   InvalidAttributeValueException,
                   MBeanException,
                   ReflectionException {
        final M cm = setters.get(attribute);
        if (cm == null) {
            final String msg;
            if (getters.containsKey(attribute))
                msg = "Read-only attribute: " + attribute;
            else
                msg = "No such attribute: " + attribute;
            throw new AttributeNotFoundException(msg);
        }
        introspector.invokeSetter(attribute, cm, resource, value, cookie);
    }
    Object invoke(Object resource, String operation, Object[] params,
                  String[] signature, Object cookie)
            throws MBeanException, ReflectionException {
        final List<MethodAndSig> list = ops.get(operation);
        if (list == null) {
            final String msg = "No such operation: " + operation;
            return noSuchMethod(msg, resource, operation, params, signature,
                                cookie);
        }
        if (signature == null)
            signature = new String[0];
        MethodAndSig found = null;
        for (MethodAndSig mas : list) {
            if (Arrays.equals(mas.signature, signature)) {
                found = mas;
                break;
            }
        }
        if (found == null) {
            final String badSig = sigString(signature);
            final String msg;
            if (list.size() == 1) {  
                msg = "Signature mismatch for operation " + operation +
                        ": " + badSig + " should be " +
                        sigString(list.get(0).signature);
            } else {
                msg = "Operation " + operation + " exists but not with " +
                        "this signature: " + badSig;
            }
            return noSuchMethod(msg, resource, operation, params, signature,
                                cookie);
        }
        return introspector.invokeM(found.method, resource, params, cookie);
    }
    private Object noSuchMethod(String msg, Object resource, String operation,
                                Object[] params, String[] signature,
                                Object cookie)
            throws MBeanException, ReflectionException {
        final NoSuchMethodException nsme =
            new NoSuchMethodException(operation + sigString(signature));
        final ReflectionException exception =
            new ReflectionException(nsme, msg);
        if (introspector.isMXBean())
            throw exception; 
        GetPropertyAction act = new GetPropertyAction("jmx.invoke.getters");
        String invokeGettersS;
        try {
            invokeGettersS = AccessController.doPrivileged(act);
        } catch (Exception e) {
            invokeGettersS = null;
        }
        if (invokeGettersS == null)
            throw exception;
        int rest = 0;
        Map<String, M> methods = null;
        if (signature == null || signature.length == 0) {
            if (operation.startsWith("get"))
                rest = 3;
            else if (operation.startsWith("is"))
                rest = 2;
            if (rest != 0)
                methods = getters;
        } else if (signature.length == 1 &&
                   operation.startsWith("set")) {
            rest = 3;
            methods = setters;
        }
        if (rest != 0) {
            String attrName = operation.substring(rest);
            M method = methods.get(attrName);
            if (method != null && introspector.getName(method).equals(operation)) {
                String[] msig = introspector.getSignature(method);
                if ((signature == null && msig.length == 0) ||
                        Arrays.equals(signature, msig)) {
                    return introspector.invokeM(method, resource, params, cookie);
                }
            }
        }
        throw exception;
    }
    private String sigString(String[] signature) {
        StringBuilder b = new StringBuilder("(");
        if (signature != null) {
            for (String s : signature) {
                if (b.length() > 1)
                    b.append(", ");
                b.append(s);
            }
        }
        return b.append(")").toString();
    }
    private class InitMaps implements MBeanAnalyzer.MBeanVisitor<M> {
        public void visitAttribute(String attributeName,
                                   M getter,
                                   M setter) {
            if (getter != null) {
                introspector.checkMethod(getter);
                final Object old = getters.put(attributeName, getter);
                assert(old == null);
            }
            if (setter != null) {
                introspector.checkMethod(setter);
                final Object old = setters.put(attributeName, setter);
                assert(old == null);
            }
        }
        public void visitOperation(String operationName,
                                   M operation) {
            introspector.checkMethod(operation);
            final String[] sig = introspector.getSignature(operation);
            final MethodAndSig mas = new MethodAndSig();
            mas.method = operation;
            mas.signature = sig;
            List<MethodAndSig> list = ops.get(operationName);
            if (list == null)
                list = Collections.singletonList(mas);
            else {
                if (list.size() == 1)
                    list = newList(list);
                list.add(mas);
            }
            ops.put(operationName, list);
        }
    }
    private class MethodAndSig {
        M method;
        String[] signature;
    }
    private final Class<?> mbeanInterface;
    private final MBeanIntrospector<M> introspector;
    private final MBeanInfo mbeanInfo;
    private final Map<String, M> getters = newMap();
    private final Map<String, M> setters = newMap();
    private final Map<String, List<MethodAndSig>> ops = newMap();
}
