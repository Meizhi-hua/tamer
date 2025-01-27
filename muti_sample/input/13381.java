public class TestAnonClassNames {
    @Nesting(MEMBER)
    static class MemberClass1{}
    @Nesting(MEMBER)
    class MemberClass2{}
    @Nesting(MEMBER)
    class Win$$AtVegas { } 
    public static void main(String... argv) {
        @Nesting(LOCAL)
        class LocalClass{};
        Object o =  new  Object() { 
                public String toString() {
                    return "I have no name!";
                }
            };
        Class<?>[] classes = {
            MemberClass1.class,
            MemberClass2.class,
            LocalClass.class,
            Win$$AtVegas.class,
            o.getClass(),
            TestAnonClassNames.class,
        };
        List<String> names = new ArrayList<String>();
        for(Class<?> clazz : classes) {
            String name = clazz.getName();
            Nesting anno = clazz.getAnnotation(Nesting.class);
            System.out.format("%s is %s%n",
                              clazz.getName(),
                              anno == null ? "(unset/ANONYMOUS)" : anno.value());
            testClassName(name);
            names.add(name);
        }
        testClassNames(names);
        if (errors > 0)
            throw new RuntimeException(errors + " errors occurred");
    }
    static void testClassName(String className) {
        testClassNames(Arrays.asList(className));
    }
    static void testClassNames(List<String> classNames) {
        System.out.println("test: " + classNames);
        List<String> options = new ArrayList<String>();
        options.add("-proc:only");
        options.add("-classpath");
        options.add(System.getProperty("test.classes"));
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        JavaCompiler.CompilationTask compileTask =
            javaCompiler.getTask(null, 
                                 null, 
                                 null, 
                                 options,
                                 classNames,
                                 null); 
        List<Processor> processors = new ArrayList<Processor>();
        processors.add(new ClassNameProber());
        compileTask.setProcessors(processors);
        Boolean goodResult = compileTask.call();
        if (!goodResult) {
            error("Errors found during compile.");
        }
    }
    static int errors = 0;
    static void error(String msg) {
        System.out.println("Error: " + msg);
        errors++;
    }
}
@Retention(RUNTIME)
@interface Nesting {
    NestingKind value();
}
class ClassNameProber extends JavacTestingAbstractProcessor {
    public ClassNameProber(){super();}
    private boolean classesFound=false;
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            for(TypeElement typeElt : typesIn(roundEnv.getRootElements())) {
                classesFound = true;
                NestingKind nestingKind = typeElt.getNestingKind();
                System.out.printf("\tSimple name: ''%s''\tQualified Name: ''%s''\tKind ''%s''\tNesting ''%s''%n",
                                  typeElt.getSimpleName().toString(),
                                  typeElt.getQualifiedName().toString(),
                                  typeElt.getKind().toString(),
                                  nestingKind.toString());
                Nesting anno = typeElt.getAnnotation(Nesting.class);
                if ((anno == null ? NestingKind.ANONYMOUS : anno.value()) != nestingKind) {
                    throw new RuntimeException("Mismatch of expected and reported nesting kind.");
                }
            }
        }
        if (!classesFound) {
            throw new RuntimeException("Error: no classes processed.");
        }
        return true;
    }
}
