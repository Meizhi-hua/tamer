public class StubGenerator extends sun.rmi.rmic.iiop.Generator {
    private static final String DEFAULT_STUB_CLASS = "javax.rmi.CORBA.Stub";
    private static final String DEFAULT_TIE_CLASS = "org.omg.CORBA_2_3.portable.ObjectImpl";
    private static final String DEFAULT_POA_TIE_CLASS = "org.omg.PortableServer.Servant";
    protected boolean reverseIDs = false;
    protected boolean localStubs = true;
    protected boolean standardPackage = false;
    protected boolean useHash = true;
    protected String stubBaseClass = DEFAULT_STUB_CLASS;
    protected String tieBaseClass = DEFAULT_TIE_CLASS;
    protected HashSet namesInUse = new HashSet();
    protected Hashtable classesInUse = new Hashtable();
    protected Hashtable imports = new Hashtable();
    protected int importCount = 0;
    protected String currentPackage = null;
    protected String currentClass = null;
    protected boolean castArray = false;
    protected Hashtable transactionalObjects = new Hashtable() ;
    protected boolean POATie = false ;
    public StubGenerator() {
    }
    public void generate(
            sun.rmi.rmic.BatchEnvironment env,
            ClassDefinition cdef, File destDir) {
        ((sun.rmi.rmic.iiop.BatchEnvironment)env).
                setStandardPackage(standardPackage);
        super.generate(env, cdef, destDir);
    }
    protected boolean requireNewInstance() {
        return false;
    }
    protected boolean parseNonConforming(ContextStack stack) {
        return stack.getEnv().getParseNonConforming();
    }
    protected CompoundType getTopType(ClassDefinition cdef, ContextStack stack) {
        CompoundType result = null;
        if (cdef.isInterface()) {
            result = AbstractType.forAbstract(cdef,stack,true);
            if (result == null) {
                result = RemoteType.forRemote(cdef,stack,false);
            }
        } else {
            result = ImplementationType.forImplementation(cdef,stack,false);
        }
        return result;
    }
    public boolean parseArgs(String argv[], Main main) {
        Object marker = new Object() ;
        reverseIDs = false;
        localStubs = true;
        useHash = true;
        stubBaseClass = DEFAULT_STUB_CLASS;
        transactionalObjects = new Hashtable() ;
        boolean result = super.parseArgs(argv,main);
        if (result) {
            for (int i = 0; i < argv.length; i++) {
                if (argv[i] != null) {
                    String arg = argv[i].toLowerCase();
                    if (arg.equals("-iiop")) {
                        argv[i] = null;
                    } else if (arg.equals("-xreverseids")) {
                        reverseIDs = true;
                        argv[i] = null;
                    } else if (arg.equals("-nolocalstubs")) {
                        localStubs = false;
                        argv[i] = null;
                    } else if (arg.equals("-xnohash")) {
                        useHash = false;
                        argv[i] = null;
                    } else if (argv[i].equals("-standardPackage")) {
                        standardPackage = true;
                        argv[i] = null;
                    } else if (arg.equals("-xstubbase")) {
                        argv[i] = null;
                        if (++i < argv.length && argv[i] != null && !argv[i].startsWith("-")) {
                            stubBaseClass = argv[i];
                            argv[i] = null;
                        } else {
                            main.error("rmic.option.requires.argument", "-Xstubbase");
                            result = false;
                        }
                    } else if (arg.equals("-xtiebase")) {
                        argv[i] = null;
                        if (++i < argv.length && argv[i] != null && !argv[i].startsWith("-")) {
                            tieBaseClass = argv[i];
                            argv[i] = null;
                        } else {
                            main.error("rmic.option.requires.argument", "-Xtiebase");
                            result = false;
                        }
                    } else if (arg.equals("-transactional" )) {
                        for ( int ctr=i+1; ctr<argv.length; ctr++ ) {
                            if (argv[ctr].charAt(1) != '-') {
                                transactionalObjects.put( argv[ctr], marker ) ;
                                break ;
                            }
                        }
                        argv[i] = null;
                    } else if (arg.equals( "-poa" )) {
                        POATie = true ;
                        argv[i] = null;
                    }
                }
            }
        }
        if(POATie){
            tieBaseClass = DEFAULT_POA_TIE_CLASS;
        } else {
            tieBaseClass = DEFAULT_TIE_CLASS;
        }
        return result;
    }
    protected OutputType[] getOutputTypesFor(CompoundType topType,
                                             HashSet alreadyChecked) {
        int filter = TYPE_REMOTE | TYPE_IMPLEMENTATION;
        Type[] genTypes = topType.collectMatching(filter,alreadyChecked);
        int count = genTypes.length;
        Vector list = new Vector(count+5);
        BatchEnvironment theEnv = topType.getEnv();
        for (int i = 0; i < genTypes.length; i++) {
            Type type = genTypes[i];
            String typeName = type.getName();
            boolean createStub = true;
            if (type instanceof ImplementationType) {
                list.addElement(new OutputType(Utility.tieNameForCompiler(typeName), type));
                int remoteInterfaceCount = 0;
                InterfaceType[] interfaces = ((CompoundType)type).getInterfaces();
                for (int j = 0; j < interfaces.length; j++) {
                    if (interfaces[j].isType(TYPE_REMOTE) &&
                        !interfaces[j].isType(TYPE_ABSTRACT)) {
                        remoteInterfaceCount++;
                    }
                }
                if (remoteInterfaceCount <= 1) {
                    createStub = false;
                }
            }
            if (type instanceof AbstractType) {
                createStub = false;  
            }
            if (createStub) {
                list.addElement(new OutputType(Utility.stubNameForCompiler(typeName), type));
            }
        }
        OutputType[] outputTypes = new OutputType[list.size()];
        list.copyInto(outputTypes);
        return outputTypes;
    }
    protected String getFileNameExtensionFor(OutputType outputType) {
        return SOURCE_FILE_EXTENSION;
    }
    protected void writeOutputFor(      OutputType outputType,
                                        HashSet alreadyChecked,
                                        IndentingWriter writer) throws IOException {
        String fileName = outputType.getName();
        CompoundType theType = (CompoundType) outputType.getType();
        if (fileName.endsWith(Utility.RMI_STUB_SUFFIX)) {
            writeStub(outputType,writer);
        } else {
            writeTie(outputType,writer);
        }
    }
    protected void writeStub(OutputType outputType,
                             IndentingWriter p) throws IOException {
        CompoundType theType = (CompoundType) outputType.getType();
        RemoteType[] remoteInterfaces = getDirectRemoteInterfaces(theType);
        p.pln("
        p.pln("
        p.pln();
        setStandardClassesInUse(theType,true);
        addClassesInUse(theType,remoteInterfaces);
        writePackageAndImports(p);
        p.p("public class " + currentClass);
        p.p(" extends " + getName(stubBaseClass));
        p.p(" implements ");
        if (remoteInterfaces.length > 0) {
            for(int i = 0; i < remoteInterfaces.length; i++) {
                if (i > 0) {
                    p.pln(",");
                }
                String objName = testUtil(getName(remoteInterfaces[i]), theType);
                p.p(objName);
            }
        }
        if (!implementsRemote(theType)) {
            p.pln(",");
            p.p(getName("java.rmi.Remote"));
        }
        p.plnI(" {");
        p.pln();
        writeIds( p, theType, false );
        p.pln();
        p.plnI("public String[] _ids() { ");
        p.pln("return _type_ids;");
        p.pOln("}");
        CompoundType.Method[] remoteMethods = theType.getMethods();
        int methodCount = remoteMethods.length;
        if (methodCount > 0) {
            boolean writeHeader = true;
            for(int i = 0; i < methodCount; i++) {
                if (!remoteMethods[i].isConstructor()) {
                    if (writeHeader) {
                        writeHeader = false;
                    }
                    p.pln();
                    writeStubMethod(p, remoteMethods[i], theType);
                }
            }
        }
        writeCastArray(p);
        p.pOln("}");            
    }
    void addClassInUse(String qualifiedName) {
        String unqualifiedName = qualifiedName;
        String packageName = null;
        int index = qualifiedName.lastIndexOf('.');
        if (index > 0) {
            unqualifiedName = qualifiedName.substring(index+1);
            packageName = qualifiedName.substring(0,index);
        }
        addClassInUse(unqualifiedName,qualifiedName,packageName);
    }
    void addClassInUse(Type type) {
        if (!type.isPrimitive()) {
            Identifier id = type.getIdentifier();
            String name = IDLNames.replace(id.getName().toString(),". ",".");
            String packageName = type.getPackageName();
            String qualifiedName;
            if (packageName != null) {
                qualifiedName = packageName+"."+name;
            } else {
                qualifiedName = name;
            }
            addClassInUse(name,qualifiedName,packageName);
        }
    }
    void addClassInUse(Type[] types) {
        for (int i = 0; i < types.length; i++) {
            addClassInUse(types[i]);
        }
    }
    void addStubInUse(Type type) {
        if (type.getIdentifier() != idCorbaObject &&
            type.isType(TYPE_CORBA_OBJECT)) {
            String stubName = getStubNameFor(type,false);
            String packageName = type.getPackageName();
            String fullName;
            if (packageName == null) {
                fullName = stubName;
            } else {
                fullName = packageName + "." + stubName;
            }
            addClassInUse(stubName,fullName,packageName);
        }
        if (type.isType(TYPE_REMOTE) ||
            type.isType(TYPE_JAVA_RMI_REMOTE)) {
            addClassInUse("javax.rmi.PortableRemoteObject");
        }
    }
    String getStubNameFor(Type type, boolean qualified) {
        String stubName;
        String className;
        if (qualified) {
            className = type.getQualifiedName();
        } else {
            className = type.getName();
        }
        if (((CompoundType)type).isCORBAObject()) {
            stubName = Utility.idlStubName(className);
        } else {
            stubName = Utility.stubNameForCompiler(className);
        }
        return stubName;
    }
    void addStubInUse(Type[] types) {
        for (int i = 0; i < types.length; i++) {
            addStubInUse(types[i]);
        }
    }
    private static final String NO_IMPORT = new String();
    void addClassInUse(String unqualifiedName, String qualifiedName, String packageName) {
        String currentName = (String)classesInUse.get(qualifiedName);
        if (currentName == null) {
            String importName = (String) imports.get(unqualifiedName);
            String nameToUse = null;
            if (packageName == null) {
                nameToUse = unqualifiedName;
            } else if (packageName.equals("java.lang")) {
                nameToUse = unqualifiedName;
                if(nameToUse.endsWith("_Stub")) nameToUse = Util.packagePrefix()+qualifiedName;
            } else if (currentPackage != null && packageName.equals(currentPackage)) {
                nameToUse = unqualifiedName;
                if (importName != null) {
                    nameToUse = qualifiedName;
                }
            } else if (importName != null) {
                nameToUse = qualifiedName;
            } else if (qualifiedName.equals("org.omg.CORBA.Object")) {
                nameToUse = qualifiedName;
            } else {
                if (unqualifiedName.indexOf('.') != -1) {
                    nameToUse = qualifiedName;
                } else {
                    nameToUse = unqualifiedName;
                    imports.put(unqualifiedName,qualifiedName);
                    importCount++;
                }
            }
            classesInUse.put(qualifiedName,nameToUse);
        }
    }
    String getName(Type type) {
        if (type.isPrimitive()) {
            return type.getName() + type.getArrayBrackets();
        }
        Identifier id = type.getIdentifier();
        String name = IDLNames.replace(id.toString(),". ",".");
        return getName(name) + type.getArrayBrackets();
    }
    String getExceptionName(Type type) {
        Identifier id = type.getIdentifier();
        return IDLNames.replace(id.toString(),". ",".");
    }
    String getName(String qualifiedName) {
        return (String)classesInUse.get(qualifiedName);
    }
    String getName(Identifier id) {
        return getName(id.toString());
    }
    String getStubName(Type type) {
        String stubName = getStubNameFor(type,true);
        return getName(stubName);
    }
    void setStandardClassesInUse(CompoundType type,
                                 boolean stub) throws IOException {
        currentPackage = type.getPackageName();
        imports.clear();
        classesInUse.clear();
        namesInUse.clear();
        importCount = 0;
        castArray = false;
        addClassInUse(type);
        if (stub) {
            currentClass = Utility.stubNameForCompiler(type.getName());
        } else {
            currentClass = Utility.tieNameForCompiler(type.getName());
        }
        if (currentPackage == null) {
            addClassInUse(currentClass,currentClass,currentPackage);
        } else {
            addClassInUse(currentClass,(currentPackage+"."+currentClass),currentPackage);
        }
        addClassInUse("javax.rmi.CORBA.Util");
        addClassInUse(idRemote.toString());
        addClassInUse(idRemoteException.toString());
        addClassInUse(idOutputStream.toString());
        addClassInUse(idInputStream.toString());
        addClassInUse(idSystemException.toString());
        addClassInUse(idJavaIoSerializable.toString());
        addClassInUse(idCorbaORB.toString());
        addClassInUse(idReplyHandler.toString());
        if (stub) {
            addClassInUse(stubBaseClass);
            addClassInUse("java.rmi.UnexpectedException");
            addClassInUse(idRemarshalException.toString());
            addClassInUse(idApplicationException.toString());
            if (localStubs) {
                addClassInUse("org.omg.CORBA.portable.ServantObject");
            }
        } else {
            addClassInUse(type);
            addClassInUse(tieBaseClass);
            addClassInUse(idTieInterface.toString());
            addClassInUse(idBadMethodException.toString());
            addClassInUse(idPortableUnknownException.toString());
            addClassInUse(idJavaLangThrowable.toString());
        }
    }
    void addClassesInUse(CompoundType type, RemoteType[] interfaces) {
        CompoundType.Method[] methods = type.getMethods();
        for (int i = 0; i < methods.length; i++) {
            addClassInUse(methods[i].getReturnType());
            addStubInUse(methods[i].getReturnType());
            addClassInUse(methods[i].getArguments());
            addStubInUse(methods[i].getArguments());
            addClassInUse(methods[i].getExceptions());
            addClassInUse(methods[i].getImplExceptions());
        }
        if (interfaces != null) {
            addClassInUse(interfaces);
        }
    }
    void writePackageAndImports(IndentingWriter p) throws IOException {
        if (currentPackage != null) {
            p.pln("package " +
                     Util.correctPackageName(
                          currentPackage, false, standardPackage)
                   + ";");
            p.pln();
        }
        String[] names = new String[importCount];
        int index = 0;
        for (Enumeration e = imports.elements() ; e.hasMoreElements() ;) {
            String it = (String) e.nextElement();
            if (it != NO_IMPORT) {
                names[index++] = it;
            }
        }
        Arrays.sort(names,new StringComparator());
        for (int i = 0; i < importCount; i++) {
            if(
               Util.isOffendingPackage(names[i])
               && names[i].endsWith("_Stub")
               && String.valueOf(names[i].charAt(names[i].lastIndexOf(".")+1)).equals("_")
               ){
                p.pln("import " + PackagePrefixChecker.packagePrefix()+names[i]+";");
            } else{
                p.pln("import " + names[i] + ";");
            }
        }
        p.pln();
        if ( currentPackage!=null && Util.isOffendingPackage(currentPackage) ){
            p.pln("import " + currentPackage +".*  ;");
        }
        p.pln();
    }
    boolean implementsRemote(CompoundType theType) {
        boolean result = theType.isType(TYPE_REMOTE) && !theType.isType(TYPE_ABSTRACT);
        if (!result) {
            InterfaceType[] interfaces = theType.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                result = implementsRemote(interfaces[i]);
                if (result) {
                    break;
                }
            }
        }
        return result;
    }
    void writeStubMethod (  IndentingWriter p,
                            CompoundType.Method method,
                            CompoundType theType) throws IOException {
        String methodName = method.getName();
        String methodIDLName = method.getIDLName();
        Type paramTypes[] = method.getArguments();
        String paramNames[] = method.getArgumentNames();
        Type returnType = method.getReturnType();
        ValueType[] exceptions = getStubExceptions(method,false);
        addNamesInUse(method);
        addNameInUse("_type_ids");
        String objName = testUtil(getName(returnType), returnType);
        p.p("public " + objName + " " + methodName + "(");
        for(int i = 0; i < paramTypes.length; i++) {
            if (i > 0)
                p.p(", ");
            p.p(getName(paramTypes[i]) + " " + paramNames[i]);
        }
        p.p(")");
        if (exceptions.length > 0) {
            p.p(" throws ");
            for(int i = 0; i < exceptions.length; i++) {
                if (i > 0) {
                    p.p(", ");
                }
                p.p(getExceptionName(exceptions[i]));
            }
        }
        p.plnI(" {");
        if (localStubs) {
            writeLocalStubMethodBody(p,method,theType);
        } else {
            writeNonLocalStubMethodBody(p,method,theType);
        }
        p.pOln("}");
    }
    void writeLocalStubMethodBody (IndentingWriter p,
                                   CompoundType.Method method,
                                   CompoundType theType) throws IOException {
        String objName;
        String paramNames[] = method.getArgumentNames();
        Type returnType = method.getReturnType();
        ValueType[] exceptions = getStubExceptions(method,false);
        String methodName = method.getName();
        String methodIDLName = method.getIDLName();
        p.plnI("if (!Util.isLocal(this)) {");
        writeNonLocalStubMethodBody(p,method,theType);
        p.pOlnI("} else {");
        String so = getVariableName("so");
        p.pln("ServantObject "+so+" = _servant_preinvoke(\""+methodIDLName+"\","+getName(theType)+".class);");
        p.plnI("if ("+so+" == null) {");
        if (!returnType.isType(TYPE_VOID)) {
            p.p("return ");
        }
        p.p(methodName+"(");
        for (int i = 0; i < paramNames.length; i++) {
            if (i > 0)
                p.p(", ");
            p.p(paramNames[i]);
        }
        p.pln(");");
        if (returnType.isType(TYPE_VOID)) {
            p.pln( "return ;" ) ;
        }
        p.pOln("}");
        p.plnI("try {");
        String[] argNames = writeCopyArguments(method,p);
        boolean copyReturn = mustCopy(returnType);
        String resultName = null;
        if (!returnType.isType(TYPE_VOID)) {
            if (copyReturn) {
                resultName = getVariableName("result");
                objName = testUtil(getName(returnType), returnType);
                p.p(objName+" "+resultName + " = ");
            } else {
                p.p("return ");
            }
        }
        objName = testUtil(getName(theType), theType);
        p.p("(("+objName+")"+so+".servant)."+methodName+"(");
        for (int i = 0; i < argNames.length; i++) {
            if (i > 0)
                p.p(", ");
            p.p(argNames[i]);
        }
        if (copyReturn) {
            p.pln(");");
            objName = testUtil(getName(returnType), returnType);
            p.pln("return ("+objName+")Util.copyObject("+resultName+",_orb());");
        } else {
            p.pln(");");
        }
        String e1 = getVariableName("ex");
        String e2 = getVariableName("exCopy");
        p.pOlnI("} catch (Throwable "+e1+") {");
        p.pln("Throwable "+e2+" = (Throwable)Util.copyObject("+e1+",_orb());");
        for(int i = 0; i < exceptions.length; i++) {
            if (exceptions[i].getIdentifier() != idRemoteException &&
                exceptions[i].isType(TYPE_VALUE)) {
                p.plnI("if ("+e2+" instanceof "+getExceptionName(exceptions[i])+") {");
                p.pln("throw ("+getExceptionName(exceptions[i])+")"+e2+";");
                p.pOln("}");
            }
        }
        p.pln("throw Util.wrapException("+e2+");");
        p.pOlnI("} finally {");
        p.pln("_servant_postinvoke("+so+");");
        p.pOln("}");
        p.pOln("}");
    }
    void writeNonLocalStubMethodBody (  IndentingWriter p,
                                        CompoundType.Method method,
                                        CompoundType theType) throws IOException {
        String methodName = method.getName();
        String methodIDLName = method.getIDLName();
        Type paramTypes[] = method.getArguments();
        String paramNames[] = method.getArgumentNames();
        Type returnType = method.getReturnType();
        ValueType[] exceptions = getStubExceptions(method,true);
        String in = getVariableName("in");
        String out = getVariableName("out");
        String ex = getVariableName("ex");
        boolean needNewReadStreamClass = false;
        for (int i = 0; i < exceptions.length; i++) {
            if (exceptions[i].getIdentifier() != idRemoteException &&
                exceptions[i].isType(TYPE_VALUE) &&
                needNewReadStreamClass(exceptions[i])) {
                needNewReadStreamClass = true;
                break;
            }
        }
        if (!needNewReadStreamClass) {
            for (int i = 0; i < paramTypes.length; i++) {
                if (needNewReadStreamClass(paramTypes[i])) {
                    needNewReadStreamClass = true;
                    break;
                }
            }
        }
        if (!needNewReadStreamClass) {
            needNewReadStreamClass = needNewReadStreamClass(returnType);
        }
        boolean needNewWriteStreamClass = false;
        for (int i = 0; i < paramTypes.length; i++) {
            if (needNewWriteStreamClass(paramTypes[i])) {
                needNewWriteStreamClass = true;
                break;
            }
        }
        p.plnI("try {");
        if (needNewReadStreamClass) {
            p.pln(idExtInputStream + " "+in+" = null;");
        } else {
            p.pln(idInputStream + " "+in+" = null;");
        }
        p.plnI("try {");
        String argStream = "null";
        if (needNewWriteStreamClass) {
            p.plnI(idExtOutputStream + " "+out+" = ");
            p.pln("(" + idExtOutputStream + ")");
            p.pln("_request(\"" + methodIDLName + "\", true);");
            p.pO();
        } else {
            p.pln("OutputStream "+out+" = _request(\"" + methodIDLName + "\", true);");
        }
        if (paramTypes.length > 0) {
            writeMarshalArguments(p, out, paramTypes, paramNames);
            p.pln();
        }
        argStream = out;
        if (returnType.isType(TYPE_VOID)) {
            p.pln("_invoke(" + argStream + ");" );
        } else {
            if (needNewReadStreamClass) {
                p.plnI(in+" = (" + idExtInputStream + ")_invoke(" + argStream + ");");
                p.pO();
            } else {
                p.pln(in+" = _invoke(" + argStream + ");");
            }
            p.p("return ");
            writeUnmarshalArgument(p, in, returnType, null);
            p.pln();
        }
        p.pOlnI("} catch ("+getName(idApplicationException)+" "+ex+") {");
        if (needNewReadStreamClass) {
            p.pln(in + " = (" + idExtInputStream + ") "+ex+".getInputStream();");
        } else {
            p.pln(in + " = "+ex+".getInputStream();");
        }
        boolean idRead = false;
        boolean idAllocated = false;
        for(int i = 0; i < exceptions.length; i++) {
            if (exceptions[i].getIdentifier() != idRemoteException) {
                if (exceptions[i].isIDLEntityException() && !exceptions[i].isCORBAUserException()) {
                    if (!idAllocated && !idRead) {
                        p.pln("String $_id = "+ex+".getId();");
                        idAllocated = true;
                    }
                    String helperName = IDLNames.replace(exceptions[i].getQualifiedIDLName(false),"::",".");
                    helperName += "Helper";
                    p.plnI("if ($_id.equals("+helperName+".id())) {");
                    p.pln("throw "+helperName+".read("+in+");");
                } else {
                    if (!idAllocated && !idRead) {
        p.pln("String $_id = "+in+".read_string();");
                        idAllocated = true;
                        idRead = true;
                    } else if (idAllocated && !idRead) {
                        p.pln("$_id = "+in+".read_string();");
                        idRead = true;
                    }
                    p.plnI("if ($_id.equals(\""+getExceptionRepositoryID(exceptions[i])+"\")) {");
                    p.pln("throw ("+getExceptionName(exceptions[i])+") "+in+".read_value(" + getExceptionName(exceptions[i]) + ".class);");
                }
                p.pOln("}");
            }
        }
        if (!idAllocated && !idRead) {
            p.pln("String $_id = "+in+".read_string();");
            idAllocated = true;
            idRead = true;
        } else if (idAllocated && !idRead) {
            p.pln("$_id = "+in+".read_string();");
            idRead = true;
        }
        p.pln("throw new UnexpectedException($_id);");
        p.pOlnI("} catch ("+getName(idRemarshalException)+" "+ex+") {");
        if (!returnType.isType(TYPE_VOID)) {
            p.p("return ");
        }
        p.p(methodName + "(");
        for(int i = 0; i < paramTypes.length; i++) {
            if (i > 0) {
                p.p(",");
            }
            p.p(paramNames[i]);
        }
        p.pln(");");
        p.pOlnI("} finally {");
        p.pln("_releaseReply("+in+");");
        p.pOln("}");
        p.pOlnI("} catch (SystemException "+ex+") {");
        p.pln("throw Util.mapSystemException("+ex+");");
        p.pOln("}");
    }
    void allocateResult (IndentingWriter p,
                         Type returnType) throws IOException {
        if (!returnType.isType(TYPE_VOID)) {
            String objName = testUtil(getName(returnType), returnType);
            p.p(objName + " result = ");
        }
    }
    int getTypeCode(Type type) {
        int typeCode = type.getTypeCode();
        if ((type instanceof CompoundType) &&
            ((CompoundType)type).isAbstractBase()) {
            typeCode = TYPE_ABSTRACT;
        }
        return typeCode;
    }
    void writeMarshalArgument(IndentingWriter p,
                              String streamName,
                              Type type, String name) throws IOException {
        int typeCode = getTypeCode(type);
        switch (typeCode) {
        case TYPE_BOOLEAN:
            p.p(streamName + ".write_boolean(" + name + ");");
            break;
        case TYPE_BYTE:
            p.p(streamName + ".write_octet(" + name + ");");
            break;
        case TYPE_CHAR:
            p.p(streamName + ".write_wchar(" + name + ");");
            break;
        case TYPE_SHORT:
            p.p(streamName + ".write_short(" + name + ");");
            break;
        case TYPE_INT:
            p.p(streamName + ".write_long(" + name + ");");
            break;
        case TYPE_LONG:
            p.p(streamName + ".write_longlong(" + name + ");");
            break;
        case TYPE_FLOAT:
            p.p(streamName + ".write_float(" + name + ");");
            break;
        case TYPE_DOUBLE:
            p.p(streamName + ".write_double(" + name + ");");
            break;
        case TYPE_STRING:
            p.p(streamName + ".write_value(" + name + "," + getName(type) + ".class);");
            break;
        case TYPE_ANY:
            p.p("Util.writeAny("+ streamName + "," + name + ");");
            break;
        case TYPE_CORBA_OBJECT:
            p.p(streamName + ".write_Object(" + name + ");");
            break;
        case TYPE_REMOTE:
            p.p("Util.writeRemoteObject("+ streamName + "," + name + ");");
            break;
        case TYPE_ABSTRACT:
            p.p("Util.writeAbstractObject("+ streamName + "," + name + ");");
            break;
        case TYPE_NC_INTERFACE:
            p.p(streamName + ".write_value((Serializable)" + name + "," + getName(type) + ".class);");
            break;
        case TYPE_VALUE:
            p.p(streamName + ".write_value(" + name + "," + getName(type) + ".class);");
            break;
        case TYPE_IMPLEMENTATION:
            p.p(streamName + ".write_value((Serializable)" + name + "," + getName(type) + ".class);");
            break;
        case TYPE_NC_CLASS:
            p.p(streamName + ".write_value((Serializable)" + name + "," + getName(type) + ".class);");
            break;
        case TYPE_ARRAY:
            castArray = true;
            p.p(streamName + ".write_value(cast_array(" + name + ")," + getName(type) + ".class);");
            break;
        case TYPE_JAVA_RMI_REMOTE:
            p.p("Util.writeRemoteObject("+ streamName + "," + name + ");");
            break;
        default:
            throw new Error("unexpected type code: " + typeCode);
        }
    }
    void writeUnmarshalArgument(IndentingWriter p,
                                String streamName,
                                Type type,
                                String name) throws IOException {
        int typeCode = getTypeCode(type);
        if (name != null) {
            p.p(name + " = ");
        }
        switch (typeCode) {
        case TYPE_BOOLEAN:
            p.p(streamName + ".read_boolean();");
            break;
        case TYPE_BYTE:
            p.p(streamName + ".read_octet();");
            break;
        case TYPE_CHAR:
            p.p(streamName + ".read_wchar();");
            break;
        case TYPE_SHORT:
            p.p(streamName + ".read_short();");
            break;
        case TYPE_INT:
            p.p(streamName + ".read_long();");
            break;
        case TYPE_LONG:
            p.p(streamName + ".read_longlong();");
            break;
        case TYPE_FLOAT:
            p.p(streamName + ".read_float();");
            break;
        case TYPE_DOUBLE:
            p.p(streamName + ".read_double();");
            break;
        case TYPE_STRING:
            p.p("(String) " + streamName + ".read_value(" + getName(type) + ".class);");
            break;
        case TYPE_ANY:
            if (type.getIdentifier() != idJavaLangObject) {
                p.p("(" + getName(type) + ") ");
            }
            p.p("Util.readAny(" + streamName + ");");
            break;
        case TYPE_CORBA_OBJECT:
            if (type.getIdentifier() == idCorbaObject) {
                p.p("(" + getName(type) + ") " + streamName + ".read_Object();");
            } else {
                p.p("(" + getName(type) + ") " + streamName + ".read_Object(" + getStubName(type) + ".class);");
            }
            break;
        case TYPE_REMOTE:
            String objName = testUtil(getName(type), type);
            p.p("(" + objName + ") " +
                "PortableRemoteObject.narrow(" + streamName + ".read_Object(), " + objName + ".class);");
            break;
        case TYPE_ABSTRACT:
            p.p("(" + getName(type) + ") " + streamName + ".read_abstract_interface();");
            break;
        case TYPE_NC_INTERFACE:
            p.p("(" + getName(type) + ") " + streamName + ".read_value(" + getName(type) + ".class);");
            break;
        case TYPE_VALUE:
            p.p("(" + getName(type) + ") " + streamName + ".read_value(" + getName(type) + ".class);");
            break;
        case TYPE_IMPLEMENTATION:
            p.p("(" + getName(type) + ") " + streamName + ".read_value(" + getName(type) + ".class);");
            break;
        case TYPE_NC_CLASS:
            p.p("(" + getName(type) + ") " + streamName + ".read_value(" + getName(type) + ".class);");
            break;
        case TYPE_ARRAY:
            p.p("(" + getName(type) + ") " + streamName + ".read_value(" + getName(type) + ".class);");
            break;
        case TYPE_JAVA_RMI_REMOTE:
            p.p("(" + getName(type) + ") " +
                "PortableRemoteObject.narrow(" + streamName + ".read_Object(), " + getName(type) + ".class);");
            break;
        default:
            throw new Error("unexpected type code: " + typeCode);
        }
    }
    String[] getAllRemoteRepIDs (CompoundType theType) {
        String[] result;
        Type[] types = collectAllRemoteInterfaces(theType);
        int length = types.length;
        boolean haveImpl = theType instanceof ImplementationType;
        InterfaceType[] interfaces = theType.getInterfaces();
        int remoteCount = countRemote(interfaces,false);
        int offset = 0;
        if (haveImpl && remoteCount > 1) {
            result = new String[length + 1];
            result[0] = getRepositoryID(theType);
            offset = 1;
        } else {
            result = new String[length];
            if (length > 1) {
                String mostDerived = null;
                if (haveImpl) {
                    for (int i = 0; i < interfaces.length; i++) {
                        if (interfaces[i].isType(TYPE_REMOTE)) {
                            mostDerived = interfaces[i].getRepositoryID();
                            break;
                        }
                    }
                } else {
                    mostDerived = theType.getRepositoryID();
                }
                for (int i = 0; i < length; i++) {
                    if (types[i].getRepositoryID() == mostDerived) {
                        if (i > 0) {
                            Type temp = types[0];
                            types[0] = types[i];
                            types[i] = temp;
                        }
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < types.length; i++) {
            result[offset++] = getRepositoryID(types[i]);
        }
        if (reverseIDs) {
            int start = 0;
            int end = result.length -1;
            while (start < end) {
                String temp = result[start];
                result[start++] = result[end];
                result[end--] = temp;
            }
        }
        return result;
    }
    Type[] collectAllRemoteInterfaces (CompoundType theType) {
        Vector list = new Vector();
        addRemoteInterfaces(list,theType);
        Type[] result = new Type[list.size()];
        list.copyInto(result);
        return result;
    }
    void addRemoteInterfaces(Vector list, CompoundType theType) {
        if (theType != null) {
            if (theType.isInterface() && !list.contains(theType)) {
                list.addElement(theType);
            }
            InterfaceType[] interfaces = theType.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i].isType(TYPE_REMOTE)) {
                    addRemoteInterfaces(list,interfaces[i]);
                }
            }
            addRemoteInterfaces(list,theType.getSuperclass());
        }
    }
    RemoteType[] getDirectRemoteInterfaces (CompoundType theType) {
        RemoteType[] result;
        InterfaceType[] interfaces = theType.getInterfaces();
        InterfaceType[] list;
        if (theType instanceof ImplementationType) {
            list = interfaces;
        } else {
            list = new InterfaceType[1];
            list[0] = (InterfaceType) theType;
        }
        int remoteCount = countRemote(list,false);
        if (remoteCount == 0) {
            throw new CompilerError("iiop.StubGenerator: No remote interfaces!");
        }
        result = new RemoteType[remoteCount];
        int offset = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i].isType(TYPE_REMOTE)) {
                result[offset++] = (RemoteType)list[i];
            }
        }
        return result;
    }
    int countRemote (Type[] list, boolean includeAbstract) {
        int remoteCount = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i].isType(TYPE_REMOTE) &&
                (includeAbstract || !list[i].isType(TYPE_ABSTRACT))) {
                remoteCount++;
            }
        }
        return remoteCount;
    }
    void writeCastArray(IndentingWriter p) throws IOException {
        if (castArray) {
            p.pln();
            p.pln("
            p.pln("
            p.pln();
            p.plnI("private "+getName(idJavaIoSerializable)+" cast_array(Object obj) {");
            p.pln("return ("+getName(idJavaIoSerializable)+")obj;");
            p.pOln("}");
        }
    }
    void writeIds(IndentingWriter p, CompoundType theType, boolean isTie
                  ) throws IOException {
        p.plnI("private static final String[] _type_ids = {");
        String[] ids = getAllRemoteRepIDs(theType);
        if (ids.length >0 ) {
            for(int i = 0; i < ids.length; i++) {
                if (i > 0)
                    p.pln(", ");
                p.p("\"" + ids[i] + "\"");
            }
        } else {
           p.pln("\"\"");
        }
        String qname = theType.getQualifiedName() ;
        boolean isTransactional = isTie && transactionalObjects.containsKey( qname ) ;
        if (isTransactional) {
            p.pln( ", " ) ;
            p.pln( "\"IDL:omg.org/CosTransactions/TransactionalObject:1.0\"" ) ;
        } else if (ids.length > 0) {
            p.pln();
        }
        p.pOln("};");
    }
    protected void writeTie(OutputType outputType,
                            IndentingWriter p) throws IOException
    {
        CompoundType theType = (CompoundType) outputType.getType();
        RemoteType[] remoteInterfaces = null;
        p.pln("
        p.pln("
        p.pln();
        setStandardClassesInUse(theType,false);
        addClassesInUse(theType,remoteInterfaces);
        writePackageAndImports(p);
        p.p("public class " + currentClass + " extends " +
            getName(tieBaseClass) + " implements Tie");
        if (!implementsRemote(theType)) {
            p.pln(",");
            p.p(getName("java.rmi.Remote"));
        }
        p.plnI(" {");
        p.pln();
        p.pln("private " + getName(theType) + " target = null;");
        p.pln();
        writeIds( p, theType, true ) ;
        p.pln();
        p.plnI("public void setTarget(Remote target) {");
        p.pln("this.target = (" + getName(theType) + ") target;");
        p.pOln("}");
        p.pln();
        p.plnI("public Remote getTarget() {");
        p.pln("return target;");
        p.pOln("}");
        p.pln();
        write_tie_thisObject_method(p,idCorbaObject);
        p.pln();
        write_tie_deactivate_method(p);
        p.pln();
        p.plnI("public ORB orb() {");
        p.pln("return _orb();");
        p.pOln("}");
        p.pln();
        write_tie_orb_method(p);
        p.pln();
        write_tie__ids_method(p);
        CompoundType.Method[] remoteMethods = theType.getMethods();
        addNamesInUse(remoteMethods);
        addNameInUse("target");
        addNameInUse("_type_ids");
        p.pln();
        String in = getVariableName("in");
        String _in = getVariableName("_in");
        String ex = getVariableName("ex");
        String method = getVariableName("method");
        String reply = getVariableName("reply");
        p.plnI("public OutputStream  _invoke(String "+method+", InputStream "+_in+", " +
               "ResponseHandler "+reply+") throws SystemException {");
        if (remoteMethods.length > 0) {
            p.plnI("try {");
            p.plnI(idExtInputStream + " "+in+" = ");
            p.pln("(" + idExtInputStream + ") "+_in+";");
            p.pO();
            StaticStringsHash hash = getStringsHash(remoteMethods);
            if (hash != null) {
                p.plnI("switch ("+method+"."+hash.method+") {");
                for (int i = 0; i < hash.buckets.length; i++) {
                    p.plnI("case "+hash.keys[i]+": ");
                    for (int j = 0; j < hash.buckets[i].length; j++) {
                        CompoundType.Method current = remoteMethods[hash.buckets[i][j]];
                        if (j > 0) {
                            p.pO("} else ");
                        }
                        p.plnI("if ("+method+".equals(\""+ current.getIDLName() +"\")) {");
                        writeTieMethod(p, theType,current);
                    }
                    p.pOln("}");
                    p.pO();
                }
            } else {
                for(int i = 0; i < remoteMethods.length; i++) {
                CompoundType.Method current = remoteMethods[i];
                if (i > 0) {
                    p.pO("} else ");
                }
                p.plnI("if ("+method+".equals(\""+ current.getIDLName() +"\")) {");
                writeTieMethod(p, theType, current);
            }
            }
            if (hash != null) {
                p.pI();
            } else {
            }
            if (hash != null) {
                p.pO();
            }
            p.pOln("}");
            p.pln("throw new "+getName(idBadMethodException)+"();");
            p.pOlnI("} catch ("+getName(idSystemException)+" "+ex+") {");
            p.pln("throw "+ex+";");
            p.pOlnI("} catch ("+getName(idJavaLangThrowable)+" "+ex+") {");
            p.pln("throw new " + getName(idPortableUnknownException) + "("+ex+");");
            p.pOln("}");
        } else {
            p.pln("throw new " + getName(idBadMethodException) + "();");
        }
        p.pOln("}");            
        writeCastArray(p);
        p.pOln("}");
    }
    public void catchWrongPolicy(IndentingWriter p) throws IOException {
        p.pln("");
    }
    public void catchServantNotActive(IndentingWriter p) throws IOException {
        p.pln("");
    }
    public void catchObjectNotActive(IndentingWriter p) throws IOException {
        p.pln("");
    }
    public void write_tie_thisObject_method(IndentingWriter p,
                                            Identifier idCorbaObject)
        throws IOException
    {
        if(POATie){
            p.plnI("public " + idCorbaObject + " thisObject() {");
            p.pln("return _this_object();");
            p.pOln("}");
        } else {
            p.plnI("public " + idCorbaObject + " thisObject() {");
            p.pln("return this;");
            p.pOln("}");
        }
    }
    public void write_tie_deactivate_method(IndentingWriter p)
        throws IOException
    {
        if(POATie){
            p.plnI("public void deactivate() {");
            p.pln("try{");
            p.pln("_poa().deactivate_object(_poa().servant_to_id(this));");
            p.pln("}catch (org.omg.PortableServer.POAPackage.WrongPolicy exception){");
            catchWrongPolicy(p);
            p.pln("}catch (org.omg.PortableServer.POAPackage.ObjectNotActive exception){");
            catchObjectNotActive(p);
            p.pln("}catch (org.omg.PortableServer.POAPackage.ServantNotActive exception){");
            catchServantNotActive(p);
            p.pln("}");
            p.pOln("}");
        } else {
            p.plnI("public void deactivate() {");
            p.pln("_orb().disconnect(this);");
            p.pln("_set_delegate(null);");
            p.pln("target = null;");
            p.pOln("}");
        }
    }
    public void write_tie_orb_method(IndentingWriter p)
        throws IOException
    {
        if(POATie){
        p.plnI("public void orb(ORB orb) {");
        p.pln("try {");
        p.pln("    ((org.omg.CORBA_2_3.ORB)orb).set_delegate(this);");
        p.pln("}");
        p.pln("catch(ClassCastException e) {");
        p.pln("    throw new org.omg.CORBA.BAD_PARAM");
        p.pln("        (\"POA Servant requires an instance of org.omg.CORBA_2_3.ORB\");");
        p.pln("}");
        p.pOln("}");
        } else {
        p.plnI("public void orb(ORB orb) {");
        p.pln("orb.connect(this);");
        p.pOln("}");
        }
    }
    public void write_tie__ids_method(IndentingWriter p)
        throws IOException
    {
        if(POATie){
        p.plnI("public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId){");
        p.pln("return _type_ids;");
        p.pOln("}");
        } else {
        p.plnI("public String[] _ids() { ");
        p.pln("return _type_ids;");
        p.pOln("}");
        }
    }
    StaticStringsHash getStringsHash (CompoundType.Method[] methods) {
        if (useHash && methods.length > 1) {
            String[] methodNames = new String[methods.length];
            for (int i = 0; i < methodNames.length; i++) {
                methodNames[i] = methods[i].getIDLName();
            }
            return new StaticStringsHash(methodNames);
        }
        return null;
    }
    static boolean needNewReadStreamClass(Type type) {
        if (type.isType(TYPE_ABSTRACT)) {
            return true;
        }
        if ((type instanceof CompoundType) &&
            ((CompoundType)type).isAbstractBase()) {
            return true;
        }
        return needNewWriteStreamClass(type);
    }
    static boolean needNewWriteStreamClass(Type type) {
        switch (type.getTypeCode()) {
        case TYPE_VOID:
        case TYPE_BOOLEAN:
        case TYPE_BYTE:
        case TYPE_CHAR:
        case TYPE_SHORT:
        case TYPE_INT:
        case TYPE_LONG:
        case TYPE_FLOAT:
        case TYPE_DOUBLE:           return false;
        case TYPE_STRING:           return true;
        case TYPE_ANY:              return false;
        case TYPE_CORBA_OBJECT:     return false;
        case TYPE_REMOTE:           return false;
        case TYPE_ABSTRACT:         return false;
        case TYPE_NC_INTERFACE:     return true;
        case TYPE_VALUE:            return true;
        case TYPE_IMPLEMENTATION:   return true;
        case TYPE_NC_CLASS:         return true;
        case TYPE_ARRAY:            return true;
        case TYPE_JAVA_RMI_REMOTE:  return false;
        default: throw new Error("unexpected type code: " + type.getTypeCode());
        }
    }
    String[] writeCopyArguments(CompoundType.Method method,
                                IndentingWriter p) throws IOException {
        Type[] args = method.getArguments();
        String[] origNames = method.getArgumentNames();
        String[] result = new String[origNames.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = origNames[i];
        }
        boolean realCopy = false;
        boolean[] copyArg = new boolean[args.length];
        int copyCount = 0;
        int firstCopiedArg = 0; 
        for (int i = 0; i < args.length; i++) {
            if (mustCopy(args[i])) {
                copyArg[i] = true;
                copyCount++;
                firstCopiedArg = i;
                if (args[i].getTypeCode() != TYPE_REMOTE &&
                    args[i].getTypeCode() != TYPE_IMPLEMENTATION) {
                    realCopy = true;
                }
            } else {
                copyArg[i] = false;
            }
        }
        if (copyCount > 0) {
            if (realCopy) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i].getTypeCode() == TYPE_STRING) {
                        copyArg[i] = true;
                        copyCount++;
                    }
                }
            }
            if (copyCount > 1) {
                String arrayName = getVariableName("copies");
                p.p("Object[] " + arrayName + " = Util.copyObjects(new Object[]{");
                boolean first = true;
                for (int i = 0; i < args.length; i++) {
                    if (copyArg[i]) {
                        if (!first) {
                            p.p(",");
                        }
                        first = false;
                        p.p(origNames[i]);
                    }
                }
                p.pln("},_orb());");
                int copyIndex = 0 ;
                for (int i = 0; i < args.length; i++) {
                    if (copyArg[i]) {
                        result[i] = getVariableName(result[i]+"Copy");
                        p.pln( getName(args[i]) + " " + result[i] + " = (" + getName(args[i]) + ") " +
                               arrayName + "[" + copyIndex++ +"];");
                    }
                }
            } else {
                result[firstCopiedArg] = getVariableName(result[firstCopiedArg]+"Copy");
                p.pln( getName(args[firstCopiedArg]) + " " + result[firstCopiedArg] + " = (" +
                       getName(args[firstCopiedArg]) + ") Util.copyObject(" +
                       origNames[firstCopiedArg] + ",_orb());");
            }
        }
        return result;
    }
    static final String SINGLE_SLASH = "\\";
    static final String DOUBLE_SLASH = SINGLE_SLASH + SINGLE_SLASH;
    String getRepositoryID(Type type) {
        return IDLNames.replace(type.getRepositoryID(), SINGLE_SLASH, DOUBLE_SLASH);
    }
    String getExceptionRepositoryID(Type type) {
        ClassType theType = (ClassType) type;
        return IDLNames.getIDLRepositoryID(theType.getQualifiedIDLExceptionName(false));
    }
    String getVariableName(String proposed) {
        while (namesInUse.contains(proposed)) {
            proposed = "$" + proposed;
        }
        return proposed;
    }
    void addNamesInUse(CompoundType.Method[] methods) {
        for (int i = 0; i < methods.length; i++) {
            addNamesInUse(methods[i]);
        }
    }
    void addNamesInUse(CompoundType.Method method) {
        String paramNames[] = method.getArgumentNames();
        for (int i = 0; i < paramNames.length; i++) {
            addNameInUse(paramNames[i]);
        }
    }
    void addNameInUse(String name) {
        namesInUse.add(name);
    }
    static boolean mustCopy(Type type) {
        switch (type.getTypeCode()) {
        case TYPE_VOID:
        case TYPE_BOOLEAN:
        case TYPE_BYTE:
        case TYPE_CHAR:
        case TYPE_SHORT:
        case TYPE_INT:
        case TYPE_LONG:
        case TYPE_FLOAT:
        case TYPE_DOUBLE:
        case TYPE_STRING:           return false;
        case TYPE_ANY:              return true;
        case TYPE_CORBA_OBJECT:     return false;
        case TYPE_REMOTE:
        case TYPE_ABSTRACT:
        case TYPE_NC_INTERFACE:
        case TYPE_VALUE:
        case TYPE_IMPLEMENTATION:
        case TYPE_NC_CLASS:
        case TYPE_ARRAY:
        case TYPE_JAVA_RMI_REMOTE:  return true;
        default: throw new Error("unexpected type code: " + type.getTypeCode());
        }
    }
    ValueType[] getStubExceptions (CompoundType.Method method, boolean sort) {
        ValueType[] list = method.getFilteredStubExceptions(method.getExceptions());
        if (sort) {
            Arrays.sort(list,new UserExceptionComparator());
            }
        return list;
                }
    ValueType[] getTieExceptions (CompoundType.Method method) {
        return method.getUniqueCatchList(method.getImplExceptions());
    }
    void writeTieMethod(IndentingWriter p, CompoundType type,
                        CompoundType.Method method) throws IOException {
        String methodName = method.getName();
        Type paramTypes[] = method.getArguments();
        String paramNames[] = method.getArgumentNames();
        Type returnType = method.getReturnType();
        ValueType[] exceptions = getTieExceptions(method);
        String in = getVariableName("in");
        String ex = getVariableName("ex");
        String out = getVariableName("out");
        String reply = getVariableName("reply");
        for (int i = 0; i < paramTypes.length; i++) {
            p.p(getName(paramTypes[i])+" "+paramNames[i]+" = ");
            writeUnmarshalArgument(p, in, paramTypes[i], null);
            p.pln();
        }
        boolean handleExceptions = exceptions != null;
        boolean doReturn = !returnType.isType(TYPE_VOID);
        if (handleExceptions && doReturn) {
            String objName = testUtil(getName(returnType), returnType);
            p.pln(objName+" result;");
        }
        if (handleExceptions)
            p.plnI("try {");
        if (doReturn) {
            if (handleExceptions) {
                p.p("result = ");
            } else {
                p.p(getName(returnType)+" result = ");
            }
        }
        p.p("target."+methodName+"(");
        for(int i = 0; i < paramNames.length; i++) {
            if (i > 0)
                p.p(", ");
            p.p(paramNames[i]);
        }
        p.pln(");");
        if (handleExceptions) {
            for(int i = 0; i < exceptions.length; i++) {
                p.pOlnI("} catch ("+getName(exceptions[i])+" "+ex+") {");
                if (exceptions[i].isIDLEntityException() && !exceptions[i].isCORBAUserException()) {
                    String helperName = IDLNames.replace(exceptions[i].getQualifiedIDLName(false),"::",".");
                    helperName += "Helper";
                    p.pln(idOutputStream+" "+out +" = "+reply+".createExceptionReply();");
                    p.pln(helperName+".write("+out+","+ex+");");
                } else {
                    p.pln("String id = \"" + getExceptionRepositoryID(exceptions[i]) + "\";");
                p.plnI(idExtOutputStream + " "+out+" = ");
                p.pln("(" + idExtOutputStream + ") "+reply+".createExceptionReply();");
                p.pOln(out+".write_string(id);");
                    p.pln(out+".write_value("+ex+"," + getName(exceptions[i]) + ".class);");
                }
                p.pln("return "+out+";");
            }
            p.pOln("}");
        }
        if (needNewWriteStreamClass(returnType)) {
            p.plnI(idExtOutputStream + " "+out+" = ");
            p.pln("(" + idExtOutputStream + ") "+reply+".createReply();");
            p.pO();
        } else {
            p.pln("OutputStream "+out+" = "+reply+".createReply();");
        }
        if (doReturn) {
            writeMarshalArgument(p, out, returnType, "result");
            p.pln();
        }
        p.pln("return "+out+";");
    }
    void writeMarshalArguments(IndentingWriter p,
                               String streamName,
                               Type[] types, String[] names)
        throws IOException
    {
        if (types.length != names.length) {
            throw new Error("paramter type and name arrays different sizes");
        }
        for (int i = 0; i < types.length; i++) {
            writeMarshalArgument(p, streamName, types[i], names[i]);
            if (i != types.length -1) {
                p.pln();
            }
        }
    }
    String testUtil(String objectName, Type ttype) {
        if (objectName.equals("Util")) {
                String correctedName = (String)ttype.getPackageName() + "." + objectName;
                return correctedName;
        } else {
                return objectName;
        }
    }
}
class StringComparator implements java.util.Comparator {
    public int compare(Object o1, Object o2) {
        String s1 = (String)o1;
        String s2 = (String)o2;
        return s1.compareTo(s2);
    }
}
class UserExceptionComparator implements java.util.Comparator {
    public int compare(Object o1, Object o2) {
        ValueType v1 = (ValueType)o1;
        ValueType v2 = (ValueType)o2;
        int result = 0;
        if (isUserException(v1)) {
            if (!isUserException(v2)) {
                result = -1;
            }
        } else if (isUserException(v2)) {
            if (!isUserException(v1)) {
                result = 1;
            }
        }
        return result;
    }
    final boolean isUserException(ValueType it) {
        return it.isIDLEntityException() && !it.isCORBAUserException();
    }
}
