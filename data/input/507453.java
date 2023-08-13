public class Not implements IArgumentMatcher, Serializable {
    private static final long serialVersionUID = -5160559075998939348L;
    private final IArgumentMatcher first;
    public Not(IArgumentMatcher first) {
        this.first = first;
    }
    public boolean matches(Object actual) {
        return !first.matches(actual);
    }
    public void appendTo(StringBuffer buffer) {
        buffer.append("not(");
        first.appendTo(buffer);
        buffer.append(")");
    }
}
