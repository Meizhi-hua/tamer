public class DocletToSigConverter {
    TypePool pool;
    Set<String> packageNames;
    public IApi convertDocletRoot(String name, RootDoc root,
            Visibility visibility, Set<String> packageNames) {
        this.pool = new TypePool();
        this.packageNames = packageNames;
        Set<IPackage> packages = new HashSet<IPackage>();
        for (PackageDoc pack : root.specifiedPackages()) {
            assert packageNames.contains(pack.name());
            packages.add(convertPackage(pack));
        }
        SigApi sources = new SigApi(name, visibility);
        sources.setPackages(packages);
        return sources;
    }
    private IPackage convertPackage(PackageDoc packageDoc) {
        Set<IClassDefinition> classes = new HashSet<IClassDefinition>();
        for (ClassDoc clazz : packageDoc.allClasses()) {
            classes.add(convertClass(clazz));
        }
        SigPackage p = new SigPackage(packageDoc.name());
        p.setClasses(classes);
        p.setAnnotations(convertAnnotations(packageDoc.annotations()));
        return p;
    }
    private SigClassDefinition convertClass(ClassDoc classDoc) {
        SigClassDefinition c = pool.getClass(classDoc.containingPackage()
                .name(), classDoc.name());
        if (c.getKind() != Kind.UNINITIALIZED) return c;
        if (classDoc.isEnum())
            c.setKind(Kind.ENUM);
        else if (classDoc.isInterface())
            c.setKind(Kind.INTERFACE);
        else if (classDoc.isClass())
            c.setKind(Kind.CLASS);
        else if (classDoc.isAnnotationType()) c.setKind(Kind.ANNOTATION);
        if (!packageNames.contains(c.getPackageName())) {
            initializeClass(c);
            return c;
        }
        c.setModifiers(convertModifiers(classDoc.modifierSpecifier()));
        if (Kind.INTERFACE.equals(c.getKind())
                || Kind.ANNOTATION.equals(c.getKind())) {
            c.getModifiers().add(Modifier.ABSTRACT);
        }
        Type superclassType = classDoc.superclassType();
        if (superclassType != null) {
            c.setSuperClass(convertTypeReference(classDoc.superclassType()));
        } else {
            c.setSuperClass(null);
        }
        Set<ITypeReference> interfaces = new HashSet<ITypeReference>();
        for (Type interfaceType : classDoc.interfaceTypes()) {
            interfaces.add(convertTypeReference(interfaceType));
        }
        c.setInterfaces(interfaces);
        ClassDoc containingClass = classDoc.containingClass();
        if (containingClass != null)
            c.setDeclaringClass(convertClass(containingClass));
        else
            c.setDeclaringClass(null);
        Set<IClassDefinition> innerClasses = new HashSet<IClassDefinition>();
        for (ClassDoc innerClass : classDoc.innerClasses()) {
            innerClasses.add(convertClass(innerClass));
        }
        c.setInnerClasses(innerClasses);
        Set<IConstructor> constructors = new HashSet<IConstructor>();
        for (ConstructorDoc constructor : classDoc.constructors()) {
            constructors.add(convertConstructor(constructor));
        }
        c.setConstructors(constructors);
        Set<IMethod> methods = new HashSet<IMethod>();
        for (MethodDoc method : classDoc.methods()) {
            methods.add(convertMethod(method));
        }
        c.setMethods(methods);
        Set<IField> fields = new HashSet<IField>();
        for (FieldDoc field : classDoc.fields()) {
            fields.add(convertField(field));
        }
        c.setFields(fields);
        Set<IEnumConstant> enumConstants = new HashSet<IEnumConstant>();
        int ordinal = 0;
        for (FieldDoc enumConstant : classDoc.enumConstants()) {
            enumConstants.add(convertEnumConstant(enumConstant, ordinal++));
        }
        c.setEnumConstants(enumConstants);
        List<ITypeVariableDefinition> typeParameters =
                new LinkedList<ITypeVariableDefinition>();
        for (TypeVariable typeVariable : classDoc.typeParameters()) {
            typeParameters
                    .add(((ITypeVariableReference) convertTypeReference(
                            typeVariable)).getTypeVariableDefinition());
        }
        c.setTypeParameters(typeParameters);
        if (classDoc.isAnnotationType()) {
            Map<SigAnnotationField, AnnotationTypeElementDoc> annotationFieldAnnotations =
                    new HashMap<SigAnnotationField, AnnotationTypeElementDoc>();
            AnnotationTypeDoc annotationType = (AnnotationTypeDoc) classDoc;
            Set<IAnnotationField> annotationFields =
                    new HashSet<IAnnotationField>();
            for (AnnotationTypeElementDoc annotationElement : annotationType
                    .elements()) {
                SigAnnotationField annotationField = new SigAnnotationField(
                        annotationElement.name());
                annotationField.setModifiers(convertModifiers(annotationElement
                        .modifierSpecifier()));
                annotationField.setType(convertTypeReference(annotationElement
                        .returnType()));
                annotationField
                        .setDefaultValue(convertAnnotationValue(
                                annotationElement.defaultValue()));
                annotationFieldAnnotations.put(annotationField,
                        annotationElement);
                annotationFields.add(annotationField);
            }
            c.setAnnotationFields(annotationFields);
            for (Entry<SigAnnotationField, AnnotationTypeElementDoc> entry :
                    annotationFieldAnnotations.entrySet()) {
                entry.getKey().setAnnotations(
                        convertAnnotations(entry.getValue().annotations()));
            }
        } else { 
            c.setAnnotationFields(null);
        }
        c.setAnnotations(convertAnnotations(classDoc.annotations()));
        return c;
    }
    private Object convertAnnotationValue(AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        Object value = annotationValue.value();
        if (value instanceof Type) {
            return convertTypeReference((Type) value);
        } else if (value instanceof String) {
            return value;
        } else if (value instanceof Double || value instanceof Float
                || value instanceof Long || value instanceof Integer
                || value instanceof Short || value instanceof Byte
                || value instanceof Character || value instanceof Boolean) {
            return value;
        } else if (value instanceof FieldDoc) {
            FieldDoc field = (FieldDoc) value;
            String name = field.name();
            ITypeReference fieldType = convertTypeReference(field.type());
            IClassReference fieldClassRef = (IClassReference) fieldType;
            IClassDefinition fieldClass = fieldClassRef.getClassDefinition();
            assert fieldClass.getKind() == Kind.ENUM;
            Set<IEnumConstant> constants = fieldClass.getEnumConstants();
            for (IEnumConstant enumConstant : constants) {
                if (enumConstant.getName().equals(name)) value = enumConstant;
            }
            assert value instanceof IEnumConstant;
            return value;
        } else if (value instanceof AnnotationDesc) {
            return convertAnnotation((AnnotationDesc) value);
        } else if (value instanceof AnnotationValue) {
            return convertAnnotationValue((AnnotationValue) value);
        } else if (value instanceof AnnotationValue[]) {
            AnnotationValue[] arr = (AnnotationValue[]) value;
            int length = arr.length;
            Object[] annotationArray = new Object[length];
            for (int i = 0; i < length; i++) {
                annotationArray[i] = convertAnnotationValue(arr[i]);
            }
            return annotationArray;
        } else {
            throw new RuntimeException("not expected case");
        }
    }
    private ITypeReference convertArrayType(Type type) {
        assert type.asWildcardType() == null;
        assert type.asAnnotationTypeDoc() == null;
        ITypeReference baseType = null;
        if (type.asTypeVariable() != null) {
            baseType = convertTypeReference(type.asTypeVariable());
        } else if (type.asParameterizedType() != null) {
            baseType = convertTypeReference(type.asParameterizedType());
        } else if (type.asClassDoc() != null) {
            baseType = new SigClassReference(convertClass(type.asClassDoc()));
        } else if (type.isPrimitive()) {
            baseType = SigPrimitiveType.valueOfTypeName(type.typeName());
        } else {
            throw new RuntimeException(type.toString());
        }
        ITypeReference arrayType = baseType;
        int dimension = type.dimension().length() / 2;
        while (dimension > 0) {
            arrayType = pool.getArrayType(arrayType);
            dimension--;
        }
        return arrayType;
    }
    private SigTypeVariableDefinition currentTypeVariableDefinition = null;
    private ITypeReference convertTypeReference(Type type) {
        assert type != null;
        if (!"".equals(type.dimension())) {
            return convertArrayType(type);
        }
        ParameterizedType pType = type.asParameterizedType();
        if (pType != null) {
            ITypeReference ownerType = null;
            Type containingType = pType.containingType();
            if (containingType != null)
                ownerType = convertTypeReference(containingType);
            IClassReference rawType = new SigClassReference(convertClass(pType
                    .asClassDoc()));
            List<ITypeReference> typeArguments =
                    new LinkedList<ITypeReference>();
            for (Type typeArgument : pType.typeArguments()) {
                typeArguments.add(convertTypeReference(typeArgument));
            }
            if (typeArguments.size() > 0) {
                return pool.getParameterizedType(ownerType, rawType,
                        typeArguments);
            } else {
                return rawType;
            }
        }
        TypeVariable tv = type.asTypeVariable();
        if (tv != null) {
            String name = tv.typeName();
            if (currentTypeVariableDefinition != null
                    && name.equals(currentTypeVariableDefinition.getName()))
                return new SigTypeVariableReference(
                        currentTypeVariableDefinition);
            IGenericDeclaration genericDeclaration = null;
            ProgramElementDoc programElement = tv.owner();
            if (programElement instanceof ClassDoc) {
                genericDeclaration = convertClass((ClassDoc) programElement);
            } else if (programElement instanceof MethodDoc
                    && currentMethod.size() > 0) {
                genericDeclaration = currentMethod.peek();
            } else if (programElement instanceof ConstructorDoc
                    && currentConstructor.size() > 0) {
                genericDeclaration = currentConstructor.peek();
            } else {
                throw new IllegalStateException("situation not expected");
            }
            SigTypeVariableDefinition typeVariable = pool.getTypeVariable(name,
                    genericDeclaration);
            List<ITypeReference> upperBounds = new LinkedList<ITypeReference>();
            for (Type upperBound : tv.bounds()) {
                assert currentTypeVariableDefinition == null;
                currentTypeVariableDefinition = typeVariable;
                upperBounds.add(convertTypeReference(upperBound));
                currentTypeVariableDefinition = null;
            }
            if (upperBounds.size() == 0) {
                upperBounds.add(pool.getClassReference("java.lang", "Object"));
            }
            typeVariable.setUpperBounds(upperBounds);
            return new SigTypeVariableReference(typeVariable);
        }
        WildcardType wt = type.asWildcardType();
        if (wt != null) {
            ITypeReference lowerBound = null;
            for (Type superBound : wt.superBounds()) {
                lowerBound = convertTypeReference(superBound);
            }
            List<ITypeReference> upperBounds = new LinkedList<ITypeReference>();
            for (Type upperBound : wt.extendsBounds()) {
                upperBounds.add(convertTypeReference(upperBound));
            }
            if (upperBounds.size() == 0) {
                upperBounds.add(pool.getClassReference("java.lang", "Object"));
            }
            return pool.getWildcardType(lowerBound, upperBounds);
        }
        ClassDoc c = type.asClassDoc();
        if (c != null) {
            return new SigClassReference(convertClass(c));
        }
        if (type.isPrimitive()) {
            return SigPrimitiveType.valueOfTypeName(type.typeName());
        }
        throw new IllegalStateException(type.toString());
    }
    private void convertExecutableMember(ExecutableMemberDoc member,
            SigExecutableMember m) {
        Set<Modifier> modifiers = convertModifiers(member.modifierSpecifier());
        if (member.containingClass().isEnum() && member.name().equals("values")
                && member.parameters().length == 0) {
            modifiers.add(Modifier.FINAL);
        }
        if (member.containingClass().isInterface()) {
            modifiers.add(Modifier.ABSTRACT);
        }
        m.setModifiers(modifiers);
        m.setAnnotations(convertAnnotations(member.annotations()));
        m.setDeclaringClass(convertClass(member.containingClass()));
        List<ITypeVariableDefinition> typeParameters =
                new LinkedList<ITypeVariableDefinition>();
        for (TypeVariable typeParameter : member.typeParameters()) {
            String name = typeParameter.typeName();
            IGenericDeclaration genericDeclaration = null;
            if (currentMethod.size() > 0)
                genericDeclaration = currentMethod.peek();
            else if (currentConstructor.size() > 0)
                genericDeclaration = currentConstructor.peek();
            else
                throw new RuntimeException();
            SigTypeVariableDefinition p = pool.getTypeVariable(name,
                    genericDeclaration);
            List<ITypeReference> upperBounds = new LinkedList<ITypeReference>();
            for (Type u : typeParameter.bounds()) {
                upperBounds.add(convertTypeReference(u));
            }
            p.setUpperBounds(upperBounds);
            typeParameters.add(p);
        }
        m.setTypeParameters(typeParameters);
        List<IParameter> parameters = new LinkedList<IParameter>();
        for (Parameter parameter : member.parameters()) {
            SigParameter p = new SigParameter(convertTypeReference(parameter
                    .type()));
            p.setAnnotations(convertAnnotations(parameter.annotations()));
            parameters.add(p);
        }
        m.setParameters(parameters);
        Set<ITypeReference> exceptions = new HashSet<ITypeReference>();
        for (Type exceptionType : member.thrownExceptionTypes()) {
            exceptions.add(convertTypeReference(exceptionType));
        }
        m.setExceptions(exceptions);
    }
    private Stack<SigMethod> currentMethod = new Stack<SigMethod>();
    private IMethod convertMethod(MethodDoc method) {
        SigMethod m = new SigMethod(method.name());
        currentMethod.push(m);
        convertExecutableMember(method, m);
        m.setReturnType(convertTypeReference(method.returnType()));
        currentMethod.pop();
        return m;
    }
    private Stack<SigConstructor> currentConstructor =
            new Stack<SigConstructor>();
    private IConstructor convertConstructor(ConstructorDoc constructor) {
        SigConstructor c = new SigConstructor(constructor.name());
        currentConstructor.push(c);
        convertExecutableMember(constructor, c);
        currentConstructor.pop();
        return c;
    }
    private IField convertField(FieldDoc field) {
        SigField f = new SigField(field.name());
        f.setAnnotations(convertAnnotations(field.annotations()));
        f.setModifiers(convertModifiers(field.modifierSpecifier()));
        f.setType(convertTypeReference(field.type()));
        return f;
    }
    private IEnumConstant convertEnumConstant(FieldDoc enumConstant,
            int ordinal) {
        SigEnumConstant ec = new SigEnumConstant(enumConstant.name());
        ec.setOrdinal(ordinal);
        ec.setAnnotations(convertAnnotations(enumConstant.annotations()));
        ec.setModifiers(convertModifiers(enumConstant.modifierSpecifier()));
        ec.setType(convertTypeReference(enumConstant.type()));
        return ec;
    }
    private Set<IAnnotation> convertAnnotations(
            AnnotationDesc[] annotationDescs) {
        Set<IAnnotation> annotations = new HashSet<IAnnotation>();
        for (AnnotationDesc annotationDesc : annotationDescs) {
            if (!annotationRetentionIsSource(annotationDesc))
                annotations.add(convertAnnotation(annotationDesc));
        }
        return annotations;
    }
    private boolean annotationRetentionIsSource(AnnotationDesc annotationDesc) {
        AnnotationTypeDoc type = annotationDesc.annotationType();
        AnnotationDesc[] annotations = type.annotations();
        for (AnnotationDesc d : annotations) {
            if ("java.lang.annotation.Retention".equals(d.annotationType()
                    .qualifiedName())) {
                for (ElementValuePair value : d.elementValues()) {
                    if ("value".equals(value.element().name())) {
                        return "java.lang.annotation.RetentionPolicy.SOURCE"
                                .equals(value.value().value().toString());
                    }
                }
            }
        }
        return false;
    }
    private IAnnotation convertAnnotation(AnnotationDesc annotationDesc) {
        SigAnnotation a = new SigAnnotation();
        IClassReference annotationType = (IClassReference) convertTypeReference(
                annotationDesc.annotationType());
        a.setType(annotationType);
        Set<IAnnotationElement> elements = new HashSet<IAnnotationElement>();
        for (AnnotationDesc.ElementValuePair pair : annotationDesc
                .elementValues()) {
            SigAnnotationElement element = new SigAnnotationElement();
            elements.add(element);
            element.setValue(convertAnnotationValue(pair.value()));
            String name = pair.element().name();
            for (IAnnotationField field : annotationType.getClassDefinition()
                    .getAnnotationFields()) {
                if (field.getName().equals(name)) {
                    element.setDeclaringField(field);
                }
            }
        }
        a.setElements(elements);
        return a;
    }
    private void initializeClass(SigClassDefinition c) {
        c.setAnnotationFields(null);
        c.setAnnotations(null);
        c.setConstructors(null);
        c.setDeclaringClass(null);
        c.setEnumConstants(null);
        c.setFields(null);
        c.setInnerClasses(null);
        c.setInterfaces(null);
        c.setMethods(null);
        c.setModifiers(null);
        c.setSuperClass(null);
        c.setTypeParameters(null);
    }
    private Set<Modifier> convertModifiers(int mod) {
        Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        if (java.lang.reflect.Modifier.isAbstract(mod))
            modifiers.add(Modifier.ABSTRACT);
        if (java.lang.reflect.Modifier.isFinal(mod))
            modifiers.add(Modifier.FINAL);
        if (java.lang.reflect.Modifier.isPrivate(mod))
            modifiers.add(Modifier.PRIVATE);
        if (java.lang.reflect.Modifier.isProtected(mod))
            modifiers.add(Modifier.PROTECTED);
        if (java.lang.reflect.Modifier.isPublic(mod))
            modifiers.add(Modifier.PUBLIC);
        if (java.lang.reflect.Modifier.isStatic(mod))
            modifiers.add(Modifier.STATIC);
        if (java.lang.reflect.Modifier.isVolatile(mod))
            modifiers.add(Modifier.VOLATILE);
        return modifiers;
    }
}
