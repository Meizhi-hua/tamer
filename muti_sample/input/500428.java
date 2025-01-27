public class DirectoryFileFilter extends AbstractFileFilter implements Serializable {
    public static final IOFileFilter DIRECTORY = new DirectoryFileFilter();
    public static final IOFileFilter INSTANCE = DIRECTORY;
    protected DirectoryFileFilter() {
    }
    public boolean accept(File file) {
        return file.isDirectory();
    }
}
