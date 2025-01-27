public class MethodDescriptor extends FeatureDescriptor {
    private Reference<Method> methodRef;
    private String[] paramNames;
    private List params;
    private ParameterDescriptor parameterDescriptors[];
    public MethodDescriptor(Method method) {
        this(method, null);
    }
    public MethodDescriptor(Method method,
                ParameterDescriptor parameterDescriptors[]) {
        setName(method.getName());
        setMethod(method);
        this.parameterDescriptors = parameterDescriptors;
    }
    public synchronized Method getMethod() {
        Method method = getMethod0();
        if (method == null) {
            Class cls = getClass0();
            String name = getName();
            if ((cls != null) && (name != null)) {
                Class[] params = getParams();
                if (params == null) {
                    for (int i = 0; i < 3; i++) {
                        method = Introspector.findMethod(cls, name, i, null);
                        if (method != null) {
                            break;
                        }
                    }
                } else {
                    method = Introspector.findMethod(cls, name, params.length, params);
                }
                setMethod(method);
            }
        }
        return method;
    }
    private synchronized void setMethod(Method method) {
        if (method == null) {
            return;
        }
        if (getClass0() == null) {
            setClass0(method.getDeclaringClass());
        }
        setParams(getParameterTypes(getClass0(), method));
        this.methodRef = getSoftReference(method);
    }
    private Method getMethod0() {
        return (this.methodRef != null)
                ? this.methodRef.get()
                : null;
    }
    private synchronized void setParams(Class[] param) {
        if (param == null) {
            return;
        }
        paramNames = new String[param.length];
        params = new ArrayList(param.length);
        for (int i = 0; i < param.length; i++) {
            paramNames[i] = param[i].getName();
            params.add(new WeakReference(param[i]));
        }
    }
    String[] getParamNames() {
        return paramNames;
    }
    private synchronized Class[] getParams() {
        Class[] clss = new Class[params.size()];
        for (int i = 0; i < params.size(); i++) {
            Reference ref = (Reference)params.get(i);
            Class cls = (Class)ref.get();
            if (cls == null) {
                return null;
            } else {
                clss[i] = cls;
            }
        }
        return clss;
    }
    public ParameterDescriptor[] getParameterDescriptors() {
        return parameterDescriptors;
    }
    MethodDescriptor(MethodDescriptor x, MethodDescriptor y) {
        super(x,y);
        methodRef = x.methodRef;
        if (y.methodRef != null) {
            methodRef = y.methodRef;
        }
        params = x.params;
        if (y.params != null) {
            params = y.params;
        }
        paramNames = x.paramNames;
        if (y.paramNames != null) {
            paramNames = y.paramNames;
        }
        parameterDescriptors = x.parameterDescriptors;
        if (y.parameterDescriptors != null) {
            parameterDescriptors = y.parameterDescriptors;
        }
    }
    MethodDescriptor(MethodDescriptor old) {
        super(old);
        methodRef = old.methodRef;
        params = old.params;
        paramNames = old.paramNames;
        if (old.parameterDescriptors != null) {
            int len = old.parameterDescriptors.length;
            parameterDescriptors = new ParameterDescriptor[len];
            for (int i = 0; i < len ; i++) {
                parameterDescriptors[i] = new ParameterDescriptor(old.parameterDescriptors[i]);
            }
        }
    }
    void appendTo(StringBuilder sb) {
        appendTo(sb, "method", this.methodRef);
        if (this.parameterDescriptors != null) {
            sb.append("; parameterDescriptors={");
            for (ParameterDescriptor pd : this.parameterDescriptors) {
                sb.append(pd).append(", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append("}");
        }
    }
}
