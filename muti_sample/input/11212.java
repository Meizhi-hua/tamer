public class Round1Apf implements AnnotationProcessorFactory {
    private static final Collection<String> supportedAnnotations
        = unmodifiableCollection(Arrays.asList("Round1"));
    private static final Collection<String> supportedOptions = emptySet();
    public Collection<String> supportedAnnotationTypes() {
        return supportedAnnotations;
    }
    public Collection<String> supportedOptions() {
        return supportedOptions;
    }
    private static int round = 0;
    public AnnotationProcessor getProcessorFor(
            Set<AnnotationTypeDeclaration> atds,
            AnnotationProcessorEnvironment env) {
        return new Round1Ap(env, atds.size() == 0);
    }
    private static class Round1Ap implements AnnotationProcessor, RoundCompleteListener {
        private final AnnotationProcessorEnvironment env;
        private final boolean empty;
        Round1Ap(AnnotationProcessorEnvironment env, boolean empty) {
            this.env = env;
            this.empty = empty;
        }
        public void process() {
            Round1Apf.round++;
            try {
                if (!empty) {
                    Filer f = env.getFiler();
                    f.createSourceFile("Dummy2").println("@Round2 class Dummy2{}");
                    f.createTextFile(Filer.Location.SOURCE_TREE,
                                     "",
                                     new File("foo.txt"),
                                     null).println("xxyzzy");
                    f.createClassFile("Vacant");
                    f.createBinaryFile(Filer.Location.CLASS_TREE,
                                       "",
                                       new File("onezero"));
                }
            } catch (java.io.IOException ioe) {
                throw new RuntimeException(ioe);
            }
            System.out.println("Round1Apf: " + round);
            env.addListener(this);
        }
        public void roundComplete(RoundCompleteEvent event) {
            RoundState rs = event.getRoundState();
            if (event.getSource() != this.env)
                throw new RuntimeException("Wrong source!");
            Filer f = env.getFiler();
            try {
                f.createSourceFile("AfterTheBell").println("@Round2 class AfterTheBell{}");
                throw new RuntimeException("Inappropriate source file creation.");
            } catch (java.io.IOException ioe) {}
            System.out.printf("\t[final round: %b, error raised: %b, "+
                              "source files created: %b, class files created: %b]%n",
                              rs.finalRound(),
                              rs.errorRaised(),
                              rs.sourceFilesCreated(),
                              rs.classFilesCreated());
            System.out.println("Round1Apf: " + round + " complete");
        }
    }
}
