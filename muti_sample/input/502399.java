public class JavacBuildStep extends BuildStep {
    private final String destPath;
    private final String classPath;
    private final Set<String> sourceFiles = new HashSet<String>();
    public JavacBuildStep(String destPath, String classPath) {
        this.destPath = destPath;
        this.classPath = classPath;
    }
    public void addSourceFile(String sourceFile)
    {
        sourceFiles.add(sourceFile);
    }
    @Override
    boolean build() {
        if (super.build())
        {
            if (sourceFiles.isEmpty())
            {
                return true;
            }
            File destFile = new File(destPath);
            if (!destFile.exists() && !destFile.mkdirs())
            {
                System.err.println("failed to create destination dir");
                return false;
            }
            int args = 4;
            String[] commandLine = new String[sourceFiles.size()+args];
            commandLine[0] = "-classpath";
            commandLine[1] = classPath;
            commandLine[2] = "-d";
            commandLine[3] = destPath;
            String[] files = new String[sourceFiles.size()];
            sourceFiles.toArray(files);
            System.arraycopy(files, 0, commandLine, args, files.length);
            return Main.compile(commandLine, new PrintWriter(System.err)) == 0;
        }
        return false;
    }
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj))
        {
            JavacBuildStep other = (JavacBuildStep) obj;
            return destPath.equals(other.destPath) 
                && classPath.equals(other.classPath)
                && sourceFiles.equals(other.sourceFiles);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return destPath.hashCode() ^ classPath.hashCode() ^ sourceFiles.hashCode();
    }
}
