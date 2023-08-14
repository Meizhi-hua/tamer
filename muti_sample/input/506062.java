public class NotNull implements IArgumentMatcher, Serializable {
    private static final long serialVersionUID = -2689588759855326190L;
    public static final NotNull NOT_NULL = new NotNull();
    private NotNull() {
    }
    public boolean matches(Object actual) {
        return actual != null;
    }
    public void appendTo(StringBuffer buffer) {
        buffer.append("notNull()");
    }
}
