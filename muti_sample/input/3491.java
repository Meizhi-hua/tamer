public class ClassTypeImpl extends ReferenceTypeImpl
    implements ClassType
{
    private SoftReference interfacesCache    = null;
    private SoftReference allInterfacesCache = null;
    private SoftReference subclassesCache    = null;
    protected ClassTypeImpl(VirtualMachine aVm, InstanceKlass aRef) {
        super(aVm, aRef);
    }
    public ClassType superclass() {
        InstanceKlass kk = (InstanceKlass)ref().getSuper();
        if (kk == null) {
            return null;
        }
        return (ClassType) vm.referenceType(kk);
    }
    public List interfaces()  {
        List interfaces = (interfacesCache != null)? (List) interfacesCache.get() : null;
        if (interfaces == null) {
            checkPrepared();
            interfaces = Collections.unmodifiableList(getInterfaces());
            interfacesCache = new SoftReference(interfaces);
        }
        return interfaces;
    }
    void addInterfaces(List list) {
        List immediate = interfaces();
        HashSet hashList = new HashSet(list);
        hashList.addAll(immediate);
        list.clear();
        list.addAll(hashList);
        Iterator iter = immediate.iterator();
        while (iter.hasNext()) {
            InterfaceTypeImpl interfaze = (InterfaceTypeImpl)iter.next();
            interfaze.addSuperinterfaces(list);
        }
        ClassTypeImpl superclass = (ClassTypeImpl)superclass();
        if (superclass != null) {
            superclass.addInterfaces(list);
        }
    }
    public List allInterfaces()  {
        List allinterfaces = (allInterfacesCache != null)? (List) allInterfacesCache.get() : null;
        if (allinterfaces == null) {
            checkPrepared();
            allinterfaces = new ArrayList();
            addInterfaces(allinterfaces);
            allinterfaces = Collections.unmodifiableList(allinterfaces);
            allInterfacesCache = new SoftReference(allinterfaces);
        }
        return allinterfaces;
    }
    public List subclasses() {
        List subclasses = (subclassesCache != null)? (List) subclassesCache.get() : null;
        if (subclasses == null) {
            List all = vm.allClasses();
            subclasses = new ArrayList(0);
            Iterator iter = all.iterator();
            while (iter.hasNext()) {
                ReferenceType refType = (ReferenceType)iter.next();
                if (refType instanceof ClassType) {
                    ClassType clazz = (ClassType)refType;
                    ClassType superclass = clazz.superclass();
                    if ((superclass != null) && superclass.equals(this)) {
                        subclasses.add(refType);
                    }
                }
            }
            subclasses = Collections.unmodifiableList(subclasses);
            subclassesCache = new SoftReference(subclasses);
        }
        return subclasses;
    }
    public Method concreteMethodByName(String name, String signature)  {
       checkPrepared();
       List methods = visibleMethods();
       Method method = null;
       Iterator iter = methods.iterator();
       while (iter.hasNext()) {
           Method candidate = (Method)iter.next();
           if (candidate.name().equals(name) &&
               candidate.signature().equals(signature) &&
               !candidate.isAbstract()) {
               method = candidate;
               break;
           }
       }
       return method;
   }
   List getAllMethods() {
        ArrayList list = new ArrayList(methods());
        ClassType clazz = superclass();
        while (clazz != null) {
            list.addAll(clazz.methods());
            clazz = clazz.superclass();
        }
        Iterator iter = allInterfaces().iterator();
        while (iter.hasNext()) {
            InterfaceType interfaze = (InterfaceType)iter.next();
            list.addAll(interfaze.methods());
        }
        return list;
    }
    List inheritedTypes() {
        List inherited = new ArrayList(interfaces());
        if (superclass() != null) {
            inherited.add(0, superclass()); 
        }
        return inherited;
    }
    public boolean isEnum() {
        ClassTypeImpl superclass = (ClassTypeImpl) superclass();
        if (superclass != null) {
            return superclass.typeNameAsSymbol().equals(vm.javaLangEnum());
        } else {
            return false;
        }
    }
    public void setValue(Field field, Value value)
        throws InvalidTypeException, ClassNotLoadedException {
        vm.throwNotReadOnlyException("ClassType.setValue(...)");
    }
    public Value invokeMethod(ThreadReference threadIntf, Method methodIntf,
                              List arguments, int options)
                                   throws InvalidTypeException,
                                          ClassNotLoadedException,
                                          IncompatibleThreadStateException,
                                          InvocationException {
        vm.throwNotReadOnlyException("ClassType.invokeMethod(...)");
        return null;
    }
    public ObjectReference newInstance(ThreadReference threadIntf,
                                       Method methodIntf,
                                       List arguments, int options)
                                   throws InvalidTypeException,
                                          ClassNotLoadedException,
                                          IncompatibleThreadStateException,
                                          InvocationException {
        vm.throwNotReadOnlyException("ClassType.newInstance(...)");
        return null;
    }
    void addVisibleMethods(Map methodMap) {
        Iterator iter = interfaces().iterator();
        while (iter.hasNext()) {
            InterfaceTypeImpl interfaze = (InterfaceTypeImpl)iter.next();
            interfaze.addVisibleMethods(methodMap);
        }
        ClassTypeImpl clazz = (ClassTypeImpl)superclass();
        if (clazz != null) {
            clazz.addVisibleMethods(methodMap);
        }
        addToMethodMap(methodMap, methods());
    }
    boolean isAssignableTo(ReferenceType type) {
        ClassTypeImpl superclazz = (ClassTypeImpl)superclass();
        if (this.equals(type)) {
            return true;
        } else if ((superclazz != null) && superclazz.isAssignableTo(type)) {
            return true;
        } else {
            List interfaces = interfaces();
            Iterator iter = interfaces.iterator();
            while (iter.hasNext()) {
                InterfaceTypeImpl interfaze = (InterfaceTypeImpl)iter.next();
                if (interfaze.isAssignableTo(type)) {
                    return true;
                }
            }
            return false;
        }
    }
    public String toString() {
       return "class " + name() + "(" + loaderString() + ")";
    }
}
