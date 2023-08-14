class AnnotationLister {
    private static final String PACKAGE_INFO = "package-info";
    private final Main.Arguments args;
    HashSet<String> matchInnerClassesOf = new HashSet<String>();
    HashSet<String> matchPackages = new HashSet<String>();
    AnnotationLister (Main.Arguments args) {
        this.args = args;
    }
    void process() {
        for (String path : args.files) {
            ClassPathOpener opener;
            opener = new ClassPathOpener(path, true,
                    new ClassPathOpener.Consumer() {
                public boolean processFileBytes(String name, byte[] bytes) {
                    if (!name.endsWith(".class")) {
                        return true;
                    }
                    ByteArray ba = new ByteArray(bytes);
                    DirectClassFile cf
                        = new DirectClassFile(ba, name, true);
                    cf.setAttributeFactory(StdAttributeFactory.THE_ONE);
                    AttributeList attributes = cf.getAttributes();
                    Attribute att;
                    String cfClassName
                            = cf.getThisClass().getClassType().getClassName();
                    if (cfClassName.endsWith(PACKAGE_INFO)) {
                        att = attributes.findFirst(
                                AttRuntimeInvisibleAnnotations.ATTRIBUTE_NAME);
                        for (;att != null; att = attributes.findNext(att)) {
                            BaseAnnotations ann = (BaseAnnotations)att;
                            visitPackageAnnotation(cf, ann);
                        }
                        att = attributes.findFirst(
                                AttRuntimeVisibleAnnotations.ATTRIBUTE_NAME);
                        for (;att != null; att = attributes.findNext(att)) {
                            BaseAnnotations ann = (BaseAnnotations)att;
                            visitPackageAnnotation(cf, ann);
                        }
                    } else if (isMatchingInnerClass(cfClassName)
                            || isMatchingPackage(cfClassName)) {
                        printMatch(cf);
                    } else {
                        att = attributes.findFirst(
                                AttRuntimeInvisibleAnnotations.ATTRIBUTE_NAME);
                        for (;att != null; att = attributes.findNext(att)) {
                            BaseAnnotations ann = (BaseAnnotations)att;
                            visitClassAnnotation(cf, ann);
                        }
                        att = attributes.findFirst(
                                AttRuntimeVisibleAnnotations.ATTRIBUTE_NAME);
                        for (;att != null; att = attributes.findNext(att)) {
                            BaseAnnotations ann = (BaseAnnotations)att;
                            visitClassAnnotation(cf, ann);
                        }
                    }
                    return true;
                }
                public void onException(Exception ex) {
                    throw new RuntimeException(ex);
                }
                public void onProcessArchiveStart(File file) {
                }
            });
            opener.process();
        }
    }
    private void visitClassAnnotation(DirectClassFile cf,
            BaseAnnotations ann) {
        if (!args.eTypes.contains(ElementType.TYPE)) {
            return;
        }
        for (Annotation anAnn : ann.getAnnotations().getAnnotations()) {
            String annClassName
                    = anAnn.getType().getClassType().getClassName();
            if (args.aclass.equals(annClassName)) {
                printMatch(cf);
            }
        }
    }
    private void visitPackageAnnotation(
            DirectClassFile cf, BaseAnnotations ann) {
        if (!args.eTypes.contains(ElementType.PACKAGE)) {
            return;
        }
        String packageName = cf.getThisClass().getClassType().getClassName();
        int slashIndex = packageName.lastIndexOf('/');
        if (slashIndex == -1) {
            packageName = "";
        } else {
            packageName
                    = packageName.substring(0, slashIndex);
        }
        for (Annotation anAnn : ann.getAnnotations().getAnnotations()) {
            String annClassName
                    = anAnn.getType().getClassType().getClassName();
            if (args.aclass.equals(annClassName)) {
                printMatchPackage(packageName);
            }
        }
    }
    private void printMatchPackage(String packageName) {
        for (Main.PrintType pt : args.printTypes) {
            switch (pt) {
                case CLASS:
                case INNERCLASS:
                case METHOD:
                    matchPackages.add(packageName);                    
                    break;
                case PACKAGE:
                    System.out.println(packageName.replace('/','.'));
                    break;
            }
        }
    }
    private void printMatch(DirectClassFile cf) {
        for (Main.PrintType pt : args.printTypes) {
            switch (pt) {
                case CLASS:
                    String classname;
                    classname =
                        cf.getThisClass().getClassType().getClassName();
                    classname = classname.replace('/','.');
                    System.out.println(classname);
                    break;
                case INNERCLASS:
                    matchInnerClassesOf.add(
                            cf.getThisClass().getClassType().getClassName());
                    break;
                case METHOD:
                    break;
                case PACKAGE:
                    break;
            }
        }
    }
    private boolean isMatchingInnerClass(String s) {
        int i;
        while (0 < (i = s.lastIndexOf('$'))) {
            s = s.substring(0, i);
            if (matchInnerClassesOf.contains(s)) {
                return true;
            }
        }
        return false;
    }
    private boolean isMatchingPackage(String s) {
        int slashIndex = s.lastIndexOf('/');
        String packageName;
        if (slashIndex == -1) {
            packageName = "";
        } else {
            packageName
                    = s.substring(0, slashIndex);
        }
        return matchPackages.contains(packageName);
    }
}
