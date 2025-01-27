public class SizeFileFilter extends AbstractFileFilter implements Serializable {
    private final long size;
    private final boolean acceptLarger;
    public SizeFileFilter(long size) {
        this(size, true);
    }
    public SizeFileFilter(long size, boolean acceptLarger) {
        if (size < 0) {
            throw new IllegalArgumentException("The size must be non-negative");
        }
        this.size = size;
        this.acceptLarger = acceptLarger;
    }
    public boolean accept(File file) {
        boolean smaller = file.length() < size;
        return acceptLarger ? !smaller : smaller;
    }
    public String toString() {
        String condition = acceptLarger ? ">=" : "<";
        return super.toString() + "(" + condition + size + ")";
    }
}
