public class CanReadFileFilter extends AbstractFileFilter implements Serializable {
    public static final IOFileFilter CAN_READ = new CanReadFileFilter();
    public static final IOFileFilter CANNOT_READ = new NotFileFilter(CAN_READ);
    public static final IOFileFilter READ_ONLY = new AndFileFilter(CAN_READ,
                                                CanWriteFileFilter.CANNOT_WRITE);
    protected CanReadFileFilter() {
    }
    public boolean accept(File file) {
        return file.canRead();
    }
}
